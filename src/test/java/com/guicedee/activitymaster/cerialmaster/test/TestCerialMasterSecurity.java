package com.guicedee.activitymaster.cerialmaster.test;

import com.google.inject.Key;
import com.guicedee.activitymaster.cerialmaster.services.enumerations.CerialMasterClassifications;
import com.guicedee.activitymaster.fsdm.client.services.IClassificationService;
import com.guicedee.activitymaster.fsdm.client.services.IEnterpriseService;
import com.guicedee.activitymaster.fsdm.client.services.SessionUtils;
import com.guicedee.activitymaster.fsdm.client.services.administration.ActivityMasterConfiguration;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.base.IWarehouseCoreTable;
import com.guicedee.activitymaster.fsdm.client.services.classifications.EnterpriseClassificationDataConcepts;
import com.guicedee.client.IGuiceContext;
import io.smallrye.mutiny.Uni;
import lombok.extern.log4j.Log4j2;
import org.hibernate.reactive.mutiny.Mutiny;
import org.junit.jupiter.api.*;

import java.time.Duration;
import java.util.UUID;

import static com.guicedee.activitymaster.cerialmaster.client.services.ICerialMasterService.CerialMasterSystemName;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Verifies the <strong>default-security outcome</strong> of the CerialMaster taxonomy install — i.e.
 * that the {@code CerialMasterSecurityCollector} {@code activate}/{@code flush} wiring (mirroring the
 * geography module) actually secures the classifications / event-types / resource-item-types the
 * install creates.
 *
 * <p>It boots the reactive stack on Testcontainers PostgreSQL, provisions an enterprise
 * ({@code startNewEnterprise} seeds the canonical group/folder tokens) and runs the full
 * {@code loadUpdates} chain (which installs the core base taxonomy the cerial install depends on —
 * e.g. {@code Hardware} — and then {@code CerialMasterInstall}). It then asserts, for representative
 * cerial classifications:</p>
 * <ol>
 *   <li><strong>Counts.</strong> each record carries a <em>canonical</em> default-security matrix —
 *       either the world-readable install/batch matrix (<strong>7</strong> grant rows) or the
 *       secure-by-default scope-restricted matrix (<strong>4</strong> rows). The exact size depends on
 *       the call-scope security flag in effect when the install ran (public reference data is meant to
 *       be 7; a record created while the secure-by-default flag is set is 4). Either way the record
 *       must be <em>secured</em> (count &gt; 0) — that is the behaviour the security update guarantees.</li>
 *   <li><strong>Access (authoritative).</strong> the cerial <em>system</em> identity can both read and
 *       write the record (its {@code Systems}-folder grant covers create/update/read under both
 *       matrices), while a token-less / empty identity can do neither (default-deny). This is the
 *       authoritative behavioural check the security model recommends over a raw row count.</li>
 * </ol>
 */
@Log4j2
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TestCerialMasterSecurity
{
	private static final String ENTERPRISE = "CerialSecurityTestCo";

	/** World-readable install/batch matrix: Administrators, Everyone, Everywhere, Systems, Applications, Plugins, Guests. */
	private static final long PUBLIC_SECURITY_ROWS = 7L;
	/** Secure-by-default (scope-restricted, null scope) matrix: Administrators + Systems/Applications/Plugins. */
	private static final long RESTRICTED_SECURITY_ROWS = 4L;

	private Mutiny.SessionFactory sessionFactory;

	@BeforeAll
	public void setup()
	{
		ActivityMasterConfiguration.get().setApplicationEnterpriseName(ENTERPRISE);
		IGuiceContext.instance().inject();

		sessionFactory = IGuiceContext.get(Key.get(Mutiny.SessionFactory.class));
		assertNotNull(sessionFactory, "SessionFactory should not be null");

		bootstrapEnterprise();
		installTaxonomy();
	}

	/** Create + start the enterprise (idempotent): seeds the canonical security groups/folders. */
	private void bootstrapEnterprise()
	{
		IEnterpriseService<?> enterpriseService = IGuiceContext.get(IEnterpriseService.class);
		sessionFactory.withSession(session -> session.withTransaction(tx ->
				enterpriseService.getEnterprise(session, ENTERPRISE)
						.onFailure().recoverWithUni(t -> {
							var ent = enterpriseService.get();
							ent.setName(ENTERPRISE);
							ent.setDescription("CerialMaster security test enterprise");
							return enterpriseService.createNewEnterprise(session, ent)
									.chain(e -> enterpriseService.startNewEnterprise(session, ENTERPRISE, "admin", "!@adminadmin"));
						})
						.replaceWith(Uni.createFrom().voidItem())
		)).await().atMost(Duration.ofMinutes(3));
	}

	/** Run the full sorted-update chain: core base taxonomy (Hardware, …) + CerialMasterInstall. */
	private void installTaxonomy()
	{
		IEnterpriseService<?> enterpriseService = IGuiceContext.get(IEnterpriseService.class);
		Integer updates = sessionFactory.withTransaction(session ->
				enterpriseService.getEnterprise(session, ENTERPRISE)
						.chain(enterprise -> enterpriseService.loadUpdates(session, enterprise))
		).await().atMost(Duration.ofMinutes(3));
		assertNotNull(updates, "loadUpdates should report the number of applied updates");
		log.info("✅ Applied {} system updates for {}", updates, ENTERPRISE);
	}

	// -------------------------------------------------------------------------------------------
	//  (1) The install secures the cerial classifications with a canonical default-security matrix
	// -------------------------------------------------------------------------------------------

	@Test
	@Order(1)
	@DisplayName("Installed cerial classifications carry a canonical default-security matrix (count > 0)")
	public void installedClassificationsAreSecured()
	{
		for (CerialMasterClassifications classification : new CerialMasterClassifications[]{
				CerialMasterClassifications.ComPort,
				CerialMasterClassifications.BaudRate,
				CerialMasterClassifications.Message})
		{
			long count = countDefaultSecurity(classification);
			log.info("🔐 '{}' default-security rows = {}", classification, count);
			assertTrue(count == PUBLIC_SECURITY_ROWS || count == RESTRICTED_SECURITY_ROWS,
					"'" + classification + "' must carry a canonical default-security matrix (public=" + PUBLIC_SECURITY_ROWS
							+ " or restricted=" + RESTRICTED_SECURITY_ROWS + ") but had " + count
							+ " — the install must secure every taxonomy row it creates");
		}
	}

	// -------------------------------------------------------------------------------------------
	//  (2) Access is correct: the cerial system reads+writes; an empty identity is denied (default-deny)
	// -------------------------------------------------------------------------------------------

	@Test
	@Order(2)
	@DisplayName("The cerial system can read+write an installed classification; an empty identity cannot")
	public void systemCanAccessButAnonymousCannot()
	{
		Boolean[] access = SessionUtils.<Boolean[]>withActivityMaster(ENTERPRISE, CerialMasterSystemName, tuple -> {
			Mutiny.Session session = tuple.getItem1();
			var system = tuple.getItem3();
			UUID[] systemToken = tuple.getItem4();

			IClassificationService<?> cls = IGuiceContext.get(IClassificationService.class);
			return cls.find(session, CerialMasterClassifications.ComPort,
							EnterpriseClassificationDataConcepts.NoClassificationDataConceptName, system, systemToken)
					.chain(comPort -> {
						IWarehouseCoreTable<?, ?, ?, ?> record = (IWarehouseCoreTable<?, ?, ?, ?>) comPort;
						return record.canRead(session, system, systemToken)
								.chain(systemRead -> record.canWrite(session, system, systemToken)
										.chain(systemWrite -> record.canRead(session, system) // empty identity
												.chain(anonRead -> record.canWrite(session, system)
														.map(anonWrite -> new Boolean[]{systemRead, systemWrite, anonRead, anonWrite}))));
					});
		}).await().atMost(Duration.ofMinutes(2));

		assertTrue(access[0], "The cerial system identity must be able to READ an installed classification (Systems folder grants read)");
		assertTrue(access[1], "The cerial system identity must be able to WRITE an installed classification (Systems folder grants create/update)");
		assertFalse(access[2], "A token-less identity must NOT read a default-secured classification (default-deny)");
		assertFalse(access[3], "A token-less identity must NOT write a default-secured classification (default-deny)");
	}

	// -------------------------------------------------------------------------------------------
	//  (3) Re-running the install is idempotent — it must NOT duplicate the default-security rows
	// -------------------------------------------------------------------------------------------

	@Test
	@Order(3)
	@DisplayName("Re-running CerialMasterInstall does not duplicate default-security rows")
	public void reinstallIsIdempotent()
	{
		long before = countDefaultSecurity(CerialMasterClassifications.ComPort);

		IEnterpriseService<?> enterpriseService = IGuiceContext.get(IEnterpriseService.class);
		Boolean done = sessionFactory.withTransaction(session ->
				enterpriseService.getEnterprise(session, ENTERPRISE)
						.chain(enterprise -> IGuiceContext.get(
										com.guicedee.activitymaster.cerialmaster.implementations.CerialMasterInstall.class)
								.update(session, enterprise))
		).await().atMost(Duration.ofMinutes(2));
		assertEquals(Boolean.TRUE, done, "Re-running the cerial install should succeed");

		long after = countDefaultSecurity(CerialMasterClassifications.ComPort);
		assertEquals(before, after,
				"Re-installing must not add duplicate default-security rows (collector flush + per-row create must stay idempotent) — before=" + before + ", after=" + after);
	}

	private long countDefaultSecurity(CerialMasterClassifications classification)
	{
		Long count = SessionUtils.<Long>withActivityMaster(ENTERPRISE, CerialMasterSystemName, tuple -> {
			Mutiny.Session session = tuple.getItem1();
			var system = tuple.getItem3();
			UUID[] token = tuple.getItem4();

			IClassificationService<?> cls = IGuiceContext.get(IClassificationService.class);
			// The install creates these via the enum-parent create() overload, which records them under
			// NoClassificationDataConceptName — so find them under the same concept (create/find must match).
			return cls.find(session, classification, EnterpriseClassificationDataConcepts.NoClassificationDataConceptName, system, token)
					.chain(found -> ((IWarehouseCoreTable<?, ?, ?, ?>) found).countDefaultSecurity(session));
		}).await().atMost(Duration.ofMinutes(2));
		assertNotNull(count, "countDefaultSecurity should not be null for " + classification);
		return count;
	}
}




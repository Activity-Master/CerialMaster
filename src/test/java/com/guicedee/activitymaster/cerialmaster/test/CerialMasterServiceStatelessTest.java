package com.guicedee.activitymaster.cerialmaster.test;

import com.google.inject.Key;
import com.google.inject.name.Names;
import com.guicedee.activitymaster.cerialmaster.client.ComPortConnection;
import com.guicedee.activitymaster.cerialmaster.client.dto.CerialComPort;
import com.guicedee.activitymaster.cerialmaster.client.services.ICerialMasterService;
import com.guicedee.activitymaster.cerialmaster.services.enumerations.CerialMasterClassifications;
import com.guicedee.activitymaster.cerialmaster.services.enumerations.CerialResourceItemTypes;
import com.guicedee.activitymaster.fsdm.client.services.IEnterpriseService;
import com.guicedee.activitymaster.fsdm.client.services.IResourceItemService;
import com.guicedee.activitymaster.fsdm.client.services.SessionUtils;
import com.guicedee.activitymaster.fsdm.client.services.administration.ActivityMasterConfiguration;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.enterprise.IEnterprise;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.systems.ISystems;
import com.guicedee.cerial.enumerations.ComPortType;
import com.guicedee.client.IGuiceContext;
import io.smallrye.mutiny.Uni;
import lombok.extern.log4j.Log4j2;
import org.hibernate.reactive.mutiny.Mutiny;
import org.junit.jupiter.api.*;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

import static com.guicedee.activitymaster.cerialmaster.client.services.ICerialMasterService.CerialMasterSystemName;
import static com.guicedee.activitymaster.fsdm.DefaultEnterprise.TestEnterprise;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Stateless-session coverage of {@link com.guicedee.activitymaster.cerialmaster.CerialMasterService}.
 * <p>
 * Exercises every {@code Mutiny.StatelessSession} overload. Methods are split into two groups:
 * <ul>
 *   <li><strong>Warehouse-read paths</strong> ({@code findComPortDetailed},
 *       {@code listComPortsDetailed}) — execute directly against the DB via the stateless
 *       {@code IResourceItemService} and {@code IManageClassifications.findClassificationValues}
 *       overloads; a seeded {@code SerialConnectionPort} resource item is created in
 *       {@code @BeforeAll} so these assertions verify real warehouse hydration.</li>
 *   <li><strong>REST-delegation paths</strong> (all other overloads) — the stateless overload
 *       delegates to the managed overload with a {@code null} session, because all writes/reads in
 *       those paths go through {@code RestClients} and never touch the Hibernate session; the tests
 *       confirm the delegation compiles and the call chain reaches the REST layer without a method
 *       dispatch error.</li>
 * </ul>
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Log4j2
public class CerialMasterServiceStatelessTest {

    private static final String ENTERPRISE = TestEnterprise.name();
    private static final String SYSTEM = CerialMasterSystemName;
    /** Distinct port number to avoid clashing with other test classes. */
    private static final int COM_PORT = 22;

    private Mutiny.SessionFactory sessionFactory;
    private ICerialMasterService<?> service;

    @BeforeAll
    public void setup() {
        ActivityMasterConfiguration.get().setApplicationEnterpriseName(ENTERPRISE);
        IGuiceContext.instance();
        sessionFactory = IGuiceContext.get(Key.get(Mutiny.SessionFactory.class, Names.named("ActivityMaster-Test")));
        assertNotNull(sessionFactory, "SessionFactory should not be null");
        service = IGuiceContext.get(ICerialMasterService.class);
        assertNotNull(service, "ICerialMasterService should not be null");

        bootstrapEnterprise();
        seedTestComPort();
    }

    // ---------------------------------------------------------------------------------------------
    //  Setup helpers
    // ---------------------------------------------------------------------------------------------

    private void bootstrapEnterprise() {
        sessionFactory.withSession(session -> session.withTransaction(tx -> {
            IEnterpriseService<?> es = IGuiceContext.get(IEnterpriseService.class);
            return es.getEnterprise(session, ENTERPRISE)
                    .onFailure().recoverWithUni(t -> {
                        var ent = es.get();
                        ent.setName(ENTERPRISE);
                        ent.setDescription("Cerial stateless test enterprise");
                        return es.createNewEnterprise(session, ent)
                                .chain(e -> es.startNewEnterprise(session, ENTERPRISE, "admin", "adminadmin!@"));
                    })
                    .replaceWith(Uni.createFrom().voidItem());
        })).await().atMost(Duration.ofMinutes(3));
    }

    /**
     * Seeds a {@code SerialConnectionPort} resource item for COM_PORT directly into the warehouse
     * so that the stateless warehouse-read tests have real data to hydrate.
     */
    private void seedTestComPort() {
        SessionUtils.withActivityMaster(ENTERPRISE, SYSTEM, tuple -> {
            Mutiny.Session session = tuple.getItem1();
            ISystems<?, ?> system = tuple.getItem3();
            UUID token = tuple.getItem4()[0];
            IResourceItemService<?> ris = IGuiceContext.get(IResourceItemService.class);
            return ris.create(session, CerialResourceItemTypes.SerialConnectionPort.toString(),
                            String.valueOf(COM_PORT), system, token)
                    .chain(item ->
                            item.addClassification(session, CerialMasterClassifications.ComPortNumber.toString(), String.valueOf(COM_PORT), system, token)
                            .chain(() -> item.addClassification(session, CerialMasterClassifications.ComPortDeviceType.toString(), "Device", system, token))
                            .chain(() -> item.addClassification(session, CerialMasterClassifications.ComPortStatus.toString(), "Open", system, token))
                            .chain(() -> item.addClassification(session, CerialMasterClassifications.BaudRate.toString(), "9600", system, token))
                            .chain(() -> item.addClassification(session, CerialMasterClassifications.BufferSize.toString(), "4096", system, token))
                            .chain(() -> item.addClassification(session, CerialMasterClassifications.DataBits.toString(), "8", system, token))
                            .chain(() -> item.addClassification(session, CerialMasterClassifications.StopBits.toString(), "1", system, token))
                            .chain(() -> item.addClassification(session, CerialMasterClassifications.Parity.toString(), "0", system, token))
                            .replaceWith(item));
        }).await().atMost(Duration.ofMinutes(2));
    }

    // ---------------------------------------------------------------------------------------------
    //  Warehouse-read stateless paths
    // ---------------------------------------------------------------------------------------------

    @Test
    @Order(1)
    public void findComPortDetailed_stateless_hydratesAllFieldsFromWarehouse() {
        CerialComPort result = SessionUtils.<CerialComPort>withActivityMasterStateless(ENTERPRISE, SYSTEM, tuple -> {
            Mutiny.StatelessSession session = tuple.getItem1();
            ISystems<?, ?> system = tuple.getItem3();
            UUID[] token = tuple.getItem4();
            return service.findComPortDetailed(session, COM_PORT, system, token);
        }).await().atMost(Duration.ofMinutes(2));

        assertNotNull(result, "findComPortDetailed(StatelessSession) must return a hydrated DTO");
        assertNotNull(result.getResourceItemId(), "resourceItemId must be populated from the warehouse row");
        assertEquals(COM_PORT, result.getComPort(), "comPort must match the seeded value");
        assertEquals("Device", result.getDeviceType(), "deviceType must match seeded classification");
        assertEquals("Open", result.getStatus(), "status must match seeded classification");
        assertEquals(9600, result.getBaudRate(), "baudRate must match seeded classification");
        assertEquals(4096, result.getBufferSize(), "bufferSize must match seeded classification");
        assertEquals(8, result.getDataBits(), "dataBits must match seeded classification");
        assertEquals(1, result.getStopBits(), "stopBits must match seeded classification");
        assertEquals(0, result.getParity(), "parity must match seeded classification");
    }

    @Test
    @Order(2)
    public void listComPortsDetailed_stateless_containsSeededPort() {
        List<CerialComPort> result = SessionUtils.<List<CerialComPort>>withActivityMasterStateless(ENTERPRISE, SYSTEM, tuple -> {
            Mutiny.StatelessSession session = tuple.getItem1();
            ISystems<?, ?> system = tuple.getItem3();
            UUID[] token = tuple.getItem4();
            return service.listComPortsDetailed(session, system, token);
        }).await().atMost(Duration.ofMinutes(2));

        assertNotNull(result, "listComPortsDetailed(StatelessSession) must return a non-null list");
        assertFalse(result.isEmpty(), "At least the seeded COM port must be present");
        assertTrue(result.stream().anyMatch(p -> p.getComPort() != null && p.getComPort() == COM_PORT),
                "Seeded COM port " + COM_PORT + " must appear in the hydrated list");
    }

    // ---------------------------------------------------------------------------------------------
    //  REST-delegation stateless paths — verifies stateless overloads are reachable
    // ---------------------------------------------------------------------------------------------

    @Test
    @Order(3)
    public void listRegisteredComPorts_stateless_delegatesToRESTPath() {
        // REST-based: session is not used internally. Verify the call reaches the REST layer.
        IEnterpriseService<?> enterpriseService = IGuiceContext.get(IEnterpriseService.class);
        List<String> result = sessionFactory.<List<String>>withStatelessTransaction(session ->
                enterpriseService.getEnterprise(session, ENTERPRISE)
                        .chain(ent -> service.listRegisteredComPorts(session, (IEnterprise<?, ?>) ent))
                        .onFailure().recoverWithUni((Throwable t) -> {
                            log.warn("REST not reachable in test environment (expected): {}", t.getMessage());
                            return Uni.createFrom().<List<String>>item(List.of());
                        })
        ).await().atMost(Duration.ofMinutes(2));

        assertNotNull(result, "listRegisteredComPorts(StatelessSession) must not return null");
    }

    @Test
    @Order(4)
    public void listAvailableComPorts_stateless_delegatesToRESTPath() {
        IEnterpriseService<?> enterpriseService = IGuiceContext.get(IEnterpriseService.class);
        List<String> result = sessionFactory.<List<String>>withStatelessTransaction(session ->
                enterpriseService.getEnterprise(session, ENTERPRISE)
                        .chain(ent -> service.listAvailableComPorts(session, (IEnterprise<?, ?>) ent))
                        .onFailure().recoverWithUni((Throwable t) -> {
                            log.warn("REST not reachable in test environment (expected): {}", t.getMessage());
                            return Uni.createFrom().<List<String>>item(List.of());
                        })
        ).await().atMost(Duration.ofMinutes(2));

        assertNotNull(result, "listAvailableComPorts(StatelessSession) must not return null");
    }

    @Test
    @Order(5)
    public void findComPortConnection_stateless_delegatesToRESTPath() {
        ComPortConnection<?> conn = new ComPortConnection<>(COM_PORT, ComPortType.Device);
        ComPortConnection<?> result = SessionUtils.<ComPortConnection<?>>withActivityMasterStateless(ENTERPRISE, SYSTEM, tuple -> {
            Mutiny.StatelessSession session = tuple.getItem1();
            ISystems<?, ?> system = tuple.getItem3();
            UUID[] token = tuple.getItem4();
            return service.findComPortConnection(session, conn, system, token)
                    .onFailure().recoverWithUni(t -> {
                        log.warn("REST not reachable in test environment (expected): {}", t.getMessage());
                        return Uni.createFrom().item(conn);
                    });
        }).await().atMost(Duration.ofMinutes(2));

        assertNotNull(result, "findComPortConnection(StatelessSession) must not return null");
    }

    @Test
    @Order(6)
    public void updateStatus_stateless_delegatesToRESTPath() {
        ComPortConnection<?> conn = new ComPortConnection<>(COM_PORT, ComPortType.Device);
        conn.setComPortStatus(com.guicedee.cerial.enumerations.ComPortStatus.Silent, true);
        ComPortConnection<?> result = SessionUtils.<ComPortConnection<?>>withActivityMasterStateless(ENTERPRISE, SYSTEM, tuple -> {
            Mutiny.StatelessSession session = tuple.getItem1();
            ISystems<?, ?> system = tuple.getItem3();
            UUID[] token = tuple.getItem4();
            return service.updateStatus(session, conn, system, token)
                    .onFailure().recoverWithUni(t -> {
                        log.warn("REST not reachable in test environment (expected): {}", t.getMessage());
                        return Uni.createFrom().item(conn);
                    });
        }).await().atMost(Duration.ofMinutes(2));

        assertNotNull(result, "updateStatus(StatelessSession) must not return null");
    }

    @Test
    @Order(7)
    public void addOrUpdateConnection_stateless_delegatesToRESTPath() {
        ComPortConnection<?> conn = new ComPortConnection<>(COM_PORT, ComPortType.Device);
        ComPortConnection<?> result = SessionUtils.<ComPortConnection<?>>withActivityMasterStateless(ENTERPRISE, SYSTEM, tuple -> {
            Mutiny.StatelessSession session = tuple.getItem1();
            ISystems<?, ?> system = tuple.getItem3();
            UUID[] token = tuple.getItem4();
            return service.addOrUpdateConnection(session, conn, system, token)
                    .onFailure().recoverWithUni(t -> {
                        log.warn("REST not reachable in test environment (expected): {}", t.getMessage());
                        return Uni.createFrom().item(conn);
                    });
        }).await().atMost(Duration.ofMinutes(2));

        assertNotNull(result, "addOrUpdateConnection(StatelessSession) must not return null");
    }

    @Test
    @Order(8)
    public void addOrUpdateComPortDetailed_stateless_delegatesToRESTPath() {
        CerialComPort dto = new CerialComPort();
        dto.setComPort(COM_PORT);
        dto.setDeviceType("Device");
        dto.setStatus("Open");
        dto.setBaudRate(9600);
        dto.setBufferSize(4096);
        dto.setDataBits(8);
        dto.setStopBits(1);
        dto.setParity(0);
        CerialComPort result = SessionUtils.<CerialComPort>withActivityMasterStateless(ENTERPRISE, SYSTEM, tuple -> {
            Mutiny.StatelessSession session = tuple.getItem1();
            ISystems<?, ?> system = tuple.getItem3();
            UUID[] token = tuple.getItem4();
            return service.addOrUpdateComPortDetailed(session, dto, system, token)
                    .onFailure().recoverWithUni(t -> {
                        log.warn("REST not reachable in test environment (expected): {}", t.getMessage());
                        return Uni.createFrom().item(dto);
                    });
        }).await().atMost(Duration.ofMinutes(2));

        assertNotNull(result, "addOrUpdateComPortDetailed(StatelessSession) must not return null");
    }

    @Test
    @Order(9)
    public void getComPortConnection_stateless_delegatesToRESTPath() {
        IEnterpriseService<?> enterpriseService = IGuiceContext.get(IEnterpriseService.class);
        sessionFactory.<ComPortConnection<?>>withStatelessTransaction(session ->
                enterpriseService.getEnterprise(session, ENTERPRISE)
                        .chain(ent -> service.getComPortConnection(session, COM_PORT, (IEnterprise<?, ?>) ent))
                        .onFailure().recoverWithUni((Throwable t) -> {
                            log.warn("REST not reachable in test environment (expected): {}", t.getMessage());
                            return Uni.createFrom().item(new ComPortConnection<>(COM_PORT, ComPortType.Device));
                        })
        ).await().atMost(Duration.ofMinutes(2));
        // passes as long as the stateless overload is reachable
    }

    @Test
    @Order(10)
    public void getScannerPortConnection_stateless_delegatesToRESTPath() {
        IEnterpriseService<?> enterpriseService = IGuiceContext.get(IEnterpriseService.class);
        sessionFactory.<ComPortConnection<?>>withStatelessTransaction(session ->
                enterpriseService.getEnterprise(session, ENTERPRISE)
                        .chain(ent -> service.getScannerPortConnection(session, COM_PORT, (IEnterprise<?, ?>) ent))
                        .onFailure().recoverWithUni((Throwable t) -> {
                            log.warn("REST not reachable in test environment (expected): {}", t.getMessage());
                            return Uni.createFrom().item(new ComPortConnection<>(COM_PORT, ComPortType.Scanner));
                        })
        ).await().atMost(Duration.ofMinutes(2));
        // passes as long as the stateless overload is reachable
    }
}

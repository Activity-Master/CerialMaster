package com.guicedee.activitymaster.cerialmaster.test;

import com.google.inject.Key;
import com.google.inject.name.Names;
import com.guicedee.activitymaster.cerialmaster.client.dto.CerialComPort;
import com.guicedee.activitymaster.cerialmaster.client.services.ICerialMasterService;
import com.guicedee.activitymaster.cerialmaster.rest.CerialMasterRestService;
import com.guicedee.activitymaster.cerialmaster.services.enumerations.CerialMasterClassifications;
import com.guicedee.activitymaster.cerialmaster.services.enumerations.CerialResourceItemTypes;
import com.guicedee.activitymaster.fsdm.client.services.IEnterpriseService;
import com.guicedee.activitymaster.fsdm.client.services.IResourceItemService;
import com.guicedee.activitymaster.fsdm.client.services.SessionUtils;
import com.guicedee.activitymaster.fsdm.client.services.administration.ActivityMasterConfiguration;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.systems.ISystems;
import com.guicedee.client.IGuiceContext;
import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import io.smallrye.mutiny.Uni;
import lombok.extern.log4j.Log4j2;
import org.hibernate.reactive.mutiny.Mutiny;
import org.junit.jupiter.api.*;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.guicedee.activitymaster.cerialmaster.client.services.ICerialMasterService.CerialMasterSystemName;
import static com.guicedee.activitymaster.fsdm.DefaultEnterprise.TestEnterprise;
import static org.junit.jupiter.api.Assertions.*;

/**
 * End-to-end integration test proving that the cerial module's data structures become available
 * through the shared ActivityMaster service registry purely by being on the class/module path.
 *
 * <p>The test boots the reactive stack against a Testcontainers PostgreSQL instance, installs the
 * full enterprise (which runs {@code CerialMasterInstall} — creating the {@code SerialConnectionPort}
 * resource item type and its COM-port classifications), persists a single COM port into the
 * ActivityMaster warehouse, and then asserts that:</p>
 * <ul>
 *     <li>the {@code cerialComPort} / {@code cerialComPorts} GraphQL queries (contributed by the
 *         cerial module's {@code CerialMasterGraphQLSchemaProvider}) return the strongly-typed COM
 *         port with correct field values;</li>
 *     <li>the {@code CerialMasterRestService} returns the same strongly-typed DTO;</li>
 *     <li>the underlying data persisted onto ActivityMaster is correct.</li>
 * </ul>
 *
 * <p>Run explicitly (the module's hardware-dependent suite is skipped by default):</p>
 * <pre>mvn -Dmaven.test.skip=false -Dtest=CerialMasterGraphQLIntegrationTest test</pre>
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Log4j2
public class CerialMasterGraphQLIntegrationTest {
    private static final String ENTERPRISE = TestEnterprise.name();
    private static final String SYSTEM = CerialMasterSystemName;

    private static final int COM_PORT = 20;
    private static final String DEVICE_TYPE = "Device";
    private static final String STATUS = "Open";
    private static final int BAUD_RATE = 9600;
    private static final int BUFFER_SIZE = 4096;
    private static final int DATA_BITS = 8;
    private static final int STOP_BITS = 1;
    private static final int PARITY = 0;

    private Mutiny.SessionFactory sessionFactory;
    private GraphQL graphQL;

    @BeforeAll
    public void setup() {
        ActivityMasterConfiguration.get().setApplicationEnterpriseName(ENTERPRISE);
        IGuiceContext.instance();

        sessionFactory = IGuiceContext.get(Key.get(Mutiny.SessionFactory.class, Names.named("ActivityMaster-Test")));
        assertNotNull(sessionFactory, "SessionFactory should not be null");

        graphQL = IGuiceContext.get(GraphQL.class);
        assertNotNull(graphQL, "GraphQL instance should be assembled from the schema providers");

        bootstrapEnterprise();
        installEnterpriseUpdates();
        sleepQuietly(2000);
        createTestComPort();
    }

    /**
     * Creates and starts the enterprise (which registers all systems, including CerialMaster).
     */
    private void bootstrapEnterprise() {
        sessionFactory.withSession(session -> session.withTransaction(tx -> {
            IEnterpriseService<?> enterpriseService = IGuiceContext.get(IEnterpriseService.class);
            return enterpriseService.getEnterprise(session, ENTERPRISE)
                    .onFailure().recoverWithUni(t -> {
                        var ent = enterpriseService.get();
                        ent.setName(ENTERPRISE);
                        ent.setDescription("Cerial GraphQL integration-test enterprise");
                        return enterpriseService.createNewEnterprise(session, ent)
                                .chain(e -> enterpriseService.startNewEnterprise(session, ENTERPRISE, "admin", "adminadmin!@"));
                    })
                    .replaceWith(Uni.createFrom().voidItem());
        })).await().atMost(Duration.ofMinutes(3));
    }

    /**
     * Runs all system updates (including {@code CerialMasterInstall}) against the started enterprise.
     */
    private void installEnterpriseUpdates() {
        IEnterpriseService<?> enterpriseService = IGuiceContext.get(IEnterpriseService.class);
        sessionFactory.withTransaction(session ->
                enterpriseService.getEnterprise(session, ENTERPRISE)
                        .chain(enterprise -> enterpriseService.loadUpdates(session, enterprise))
        ).await().atMost(Duration.ofMinutes(5));
    }

    /**
     * Persists a single, fully-specified COM port (SerialConnectionPort resource item) with classifications.
     */
    private void createTestComPort() {
        SessionUtils.withActivityMaster(ENTERPRISE, SYSTEM, tuple -> {
            Mutiny.Session session = tuple.getItem1();
            ISystems<?, ?> system = tuple.getItem3();
            var token = tuple.getItem4();
            IResourceItemService<?> resourceItemService = IGuiceContext.get(IResourceItemService.class);

            return resourceItemService.create(session, CerialResourceItemTypes.SerialConnectionPort.toString(),
                            String.valueOf(COM_PORT), system, token)
                    .chain(item -> item.addClassification(session, CerialMasterClassifications.ComPortNumber.toString(), String.valueOf(COM_PORT), system, token)
                            .chain(() -> item.addClassification(session, CerialMasterClassifications.ComPortDeviceType.toString(), DEVICE_TYPE, system, token))
                            .chain(() -> item.addClassification(session, CerialMasterClassifications.ComPortStatus.toString(), STATUS, system, token))
                            .chain(() -> item.addClassification(session, CerialMasterClassifications.BaudRate.toString(), String.valueOf(BAUD_RATE), system, token))
                            .chain(() -> item.addClassification(session, CerialMasterClassifications.BufferSize.toString(), String.valueOf(BUFFER_SIZE), system, token))
                            .chain(() -> item.addClassification(session, CerialMasterClassifications.DataBits.toString(), String.valueOf(DATA_BITS), system, token))
                            .chain(() -> item.addClassification(session, CerialMasterClassifications.StopBits.toString(), String.valueOf(STOP_BITS), system, token))
                            .chain(() -> item.addClassification(session, CerialMasterClassifications.Parity.toString(), String.valueOf(PARITY), system, token))
                            .replaceWith(item));
        }).await().atMost(Duration.ofMinutes(2));
    }

    // ---------------------------------------------------------------------------------------------

    @Test
    @Order(1)
    public void cerialComPortQueryReturnsStronglyTypedComPort() {
        String document =
                "query Port($e: String!, $s: String!, $p: Int!) {\n"
                        + "    cerialComPort(enterprise: $e, system: $s, comPort: $p) {\n"
                        + "        resourceItemId\n"
                        + "        comPort\n"
                        + "        deviceType\n"
                        + "        status\n"
                        + "        baudRate\n"
                        + "        bufferSize\n"
                        + "        dataBits\n"
                        + "        stopBits\n"
                        + "        parity\n"
                        + "    }\n"
                        + "}\n";

        ExecutionInput input = ExecutionInput.newExecutionInput()
                .query(document)
                .variables(Map.of("e", ENTERPRISE, "s", SYSTEM, "p", COM_PORT))
                .build();

        ExecutionResult result;
        try {
            result = graphQL.executeAsync(input).get(2, TimeUnit.MINUTES);
        } catch (Exception e) {
            throw new RuntimeException("GraphQL execution failed for cerialComPort", e);
        }

        assertTrue(result.getErrors().isEmpty(), () -> "GraphQL errors: " + result.getErrors());

        Map<String, Object> data = result.getData();
        assertNotNull(data, "GraphQL data should not be null");
        @SuppressWarnings("unchecked")
        Map<String, Object> port = (Map<String, Object>) data.get("cerialComPort");
        assertNotNull(port, "cerialComPort should resolve a COM port");

        assertNotNull(port.get("resourceItemId"), "resourceItemId should be populated from the warehouse row");
        assertEquals(COM_PORT, ((Number) port.get("comPort")).intValue());
        assertEquals(DEVICE_TYPE, port.get("deviceType"));
        assertEquals(STATUS, port.get("status"));
        assertEquals(BAUD_RATE, ((Number) port.get("baudRate")).intValue());
        assertEquals(BUFFER_SIZE, ((Number) port.get("bufferSize")).intValue());
        assertEquals(DATA_BITS, ((Number) port.get("dataBits")).intValue());
        assertEquals(STOP_BITS, ((Number) port.get("stopBits")).intValue());
        assertEquals(PARITY, ((Number) port.get("parity")).intValue());
    }

    @Test
    @Order(2)
    public void cerialComPortsQueryReturnsList() {
        String document =
                "query Ports($e: String!, $s: String!) {\n"
                        + "    cerialComPorts(enterprise: $e, system: $s) {\n"
                        + "        comPort\n"
                        + "        deviceType\n"
                        + "    }\n"
                        + "}\n";

        ExecutionInput input = ExecutionInput.newExecutionInput()
                .query(document)
                .variables(Map.of("e", ENTERPRISE, "s", SYSTEM))
                .build();

        ExecutionResult result;
        try {
            result = graphQL.executeAsync(input).get(2, TimeUnit.MINUTES);
        } catch (Exception e) {
            throw new RuntimeException("GraphQL execution failed for cerialComPorts", e);
        }

        assertTrue(result.getErrors().isEmpty(), () -> "GraphQL errors: " + result.getErrors());

        Map<String, Object> data = result.getData();
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> ports = (List<Map<String, Object>>) data.get("cerialComPorts");
        assertNotNull(ports, "cerialComPorts should resolve a list");
        assertFalse(ports.isEmpty(), "At least one COM port should be returned");
        assertTrue(ports.stream().anyMatch(p -> ((Number) p.get("comPort")).intValue() == COM_PORT),
                "The seeded COM port should be present in the list");
    }

    @Test
    @Order(3)
    public void cerialRestServiceReturnsStronglyTypedComPort() {
        CerialMasterRestService restService = IGuiceContext.get(CerialMasterRestService.class);
        CerialComPort port = restService.findComPort(ENTERPRISE, SYSTEM, COM_PORT)
                .await().atMost(Duration.ofMinutes(2));

        assertNotNull(port, "REST resource should resolve the COM port DTO");
        assertNotNull(port.getResourceItemId(), "resourceItemId should be populated from the warehouse row");
        assertEquals(COM_PORT, port.getComPort());
        assertEquals(DEVICE_TYPE, port.getDeviceType());
        assertEquals(STATUS, port.getStatus());
        assertEquals(BAUD_RATE, port.getBaudRate());
        assertEquals(BUFFER_SIZE, port.getBufferSize());
        assertEquals(DATA_BITS, port.getDataBits());
        assertEquals(STOP_BITS, port.getStopBits());
        assertEquals(PARITY, port.getParity());
    }

    @Test
    @Order(4)
    public void persistedDataMatchesWhatWasStoredOnActivityMaster() {
        ICerialMasterService<?> cerialMasterService = IGuiceContext.get(ICerialMasterService.class);
        CerialComPort persisted = SessionUtils.<CerialComPort>withActivityMaster(ENTERPRISE, SYSTEM, tuple -> {
            return cerialMasterService.findComPortDetailed(tuple.getItem1(), COM_PORT, tuple.getItem3(), tuple.getItem4());
        }).await().atMost(Duration.ofMinutes(2));

        assertNotNull(persisted, "The COM port must be persisted on ActivityMaster");
        assertNotNull(persisted.getResourceItemId());
        assertEquals(COM_PORT, persisted.getComPort());
        assertEquals(DEVICE_TYPE, persisted.getDeviceType());
        assertEquals(STATUS, persisted.getStatus());
        assertEquals(BAUD_RATE, persisted.getBaudRate());
    }

    private static void sleepQuietly(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}






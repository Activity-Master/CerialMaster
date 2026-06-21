package com.guicedee.activitymaster.cerialmaster.implementations.graphql;

import com.guicedee.activitymaster.cerialmaster.client.dto.CerialComPort;
import com.guicedee.activitymaster.cerialmaster.client.services.ICerialMasterService;
import com.guicedee.activitymaster.fsdm.client.services.SessionUtils;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.systems.ISystems;
import com.guicedee.client.IGuiceContext;
import com.guicedee.vertx.graphql.services.IGraphQLSchemaProvider;
import graphql.schema.DataFetcher;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import io.smallrye.mutiny.Uni;
import io.vertx.core.Future;
import org.hibernate.reactive.mutiny.Mutiny;

import java.util.List;

/**
 * Contributes the strongly-typed {@code CerialComPort} GraphQL type (and its queries) to the shared
 * ActivityMaster GraphQL schema.
 *
 * <p>This provider is discovered automatically (via {@link java.util.ServiceLoader}) whenever the
 * cerial module is on the class/module path, so the core service registry exposes the cerial data
 * structures without any change to the core itself. The {@code cerialComPort} / {@code cerialComPorts}
 * queries are resolved through {@link ICerialMasterService#findComPortDetailed} /
 * {@link ICerialMasterService#listComPortsDetailed} inside the canonical
 * {@link SessionUtils#withActivityMaster} security/session context, returning fully-hydrated
 * {@link CerialComPort} DTOs read straight from the ActivityMaster warehouse.</p>
 *
 * <p>The {@code Query} root is shared with the core {@code FsdmGraphQLSchemaProvider}; this provider
 * therefore {@code extend}s it rather than redefining it.</p>
 */
public class CerialMasterGraphQLSchemaProvider implements IGraphQLSchemaProvider<CerialMasterGraphQLSchemaProvider> {
    private static final String SDL =
            """
                    "A serial (COM) port connection as stored within ActivityMaster."
                    type CerialComPort {
                        "The warehouse ResourceItem row identifier."
                        resourceItemId: String
                        "The COM port number (e.g. 3 for COM3)."
                        comPort: Int
                        "The device type of the port (Device, Server, Scanner)."
                        deviceType: String
                        "The last registered status of the port."
                        status: String
                        "The configured baud rate."
                        baudRate: Int
                        "The configured read/write buffer size."
                        bufferSize: Int
                        "The number of data bits per byte."
                        dataBits: Int
                        "The number of stop bits."
                        stopBits: Int
                        "The parity setting (numeric form)."
                        parity: Int
                    }
                    
                    extend type Query {
                        "Resolves a single registered COM port by its number within an enterprise/system scope."
                        cerialComPort(enterprise: String!, system: String!, comPort: Int!): CerialComPort
                        "Lists every registered COM port within an enterprise/system scope."
                        cerialComPorts(enterprise: String!, system: String!): [CerialComPort!]!
                    }
                    """;

    @Override
    public TypeDefinitionRegistry getTypeDefinitions() {
        return new SchemaParser().parse(SDL);
    }

    @Override
    public RuntimeWiring.Builder configureWiring(RuntimeWiring.Builder builder) {
        return builder
                .type("Query", q -> q
                        .dataFetcher("cerialComPort", comPortFetcher())
                        .dataFetcher("cerialComPorts", comPortsFetcher()))
                .type("CerialComPort", t -> t
                        .dataFetcher("resourceItemId", env -> {
                            CerialComPort c = env.getSource();
                            return c == null || c.getResourceItemId() == null ? null : c.getResourceItemId().toString();
                        }));
    }

    /**
     * Builds the data fetcher for the {@code cerialComPort} query. Execution runs inside the canonical
     * Activity Master security/session context and the resulting Mutiny {@link Uni} is bridged to a
     * Vert.x {@link Future} so the auto-installed {@code VertxFutureAdapter} resolves it.
     */
    private DataFetcher<Future<CerialComPort>> comPortFetcher() {
        return env -> {
            String enterprise = env.getArgument("enterprise");
            String system = env.getArgument("system");
            Integer comPort = ((Number) env.getArgument("comPort")).intValue();

            Uni<CerialComPort> uni = SessionUtils.withActivityMasterReadOnly(enterprise, system, tuple -> {
                Mutiny.Session session = tuple.getItem1();
                ISystems<?, ?> sys = tuple.getItem3();
                ICerialMasterService<?> service = IGuiceContext.get(ICerialMasterService.class);
                return service.findComPortDetailed(session, comPort, sys, tuple.getItem4());
            });

            return Future.fromCompletionStage(uni.subscribeAsCompletionStage());
        };
    }

    private DataFetcher<Future<List<CerialComPort>>> comPortsFetcher() {
        return env -> {
            String enterprise = env.getArgument("enterprise");
            String system = env.getArgument("system");

            Uni<List<CerialComPort>> uni = SessionUtils.withActivityMasterReadOnly(enterprise, system, tuple -> {
                Mutiny.Session session = tuple.getItem1();
                ISystems<?, ?> sys = tuple.getItem3();
                ICerialMasterService<?> service = IGuiceContext.get(ICerialMasterService.class);
                return service.listComPortsDetailed(session, sys, tuple.getItem4());
            });

            return Future.fromCompletionStage(uni.subscribeAsCompletionStage());
        };
    }
}


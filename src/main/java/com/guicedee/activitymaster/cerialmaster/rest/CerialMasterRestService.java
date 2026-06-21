package com.guicedee.activitymaster.cerialmaster.rest;

import com.google.inject.Inject;
import com.guicedee.activitymaster.cerialmaster.client.dto.CerialComPort;
import com.guicedee.activitymaster.cerialmaster.client.services.ICerialMasterService;
import com.guicedee.activitymaster.fsdm.client.services.SessionUtils;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.systems.ISystems;
import io.smallrye.mutiny.Uni;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import lombok.extern.log4j.Log4j2;
import org.hibernate.reactive.mutiny.Mutiny;

import java.util.List;

/**
 * JAX-RS resource exposing the cerial (serial port) data structures that are stored within ActivityMaster.
 *
 * <p>Mounted whenever the cerial module is on the class/module path, this resource is the REST
 * counterpart to {@code CerialMasterGraphQLSchemaProvider}: both resolve the strongly-typed
 * {@link CerialComPort} DTO through {@link ICerialMasterService#findComPortDetailed} /
 * {@link ICerialMasterService#listComPortsDetailed} so the same warehouse-backed data is available
 * over either transport.</p>
 *
 * <p>Every operation is documented with Swagger/OpenAPI annotations and grouped under the
 * {@code Cerial Master} tag, so it is published into the merged {@code /openapi.json} /
 * {@code /openapi.yaml} document and rendered by the Swagger UI at {@code /swagger/}.</p>
 */
@Path("{enterprise}/cerial")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Cerial Master", description = "Serial (COM) port registry — find, list, create and update the COM port connections stored within ActivityMaster.")
@Log4j2
public class CerialMasterRestService
{
    @Inject
    private ICerialMasterService<?> cerialMasterService;

    /**
     * Resolves a single registered COM port by its number within the given enterprise/system scope and
     * returns the fully-hydrated {@link CerialComPort} DTO read from the ActivityMaster warehouse.
     *
     * @param enterpriseName the enterprise (security scope)
     * @param systemName     the requesting system name (security scope)
     * @param comPort        the COM port number
     * @return a {@link Uni} emitting the hydrated COM port DTO
     */
    @GET
    @Path("{requestingSystemName}/comport/{comPort}")
    @Operation(summary = "Find a COM port",
            description = "Resolves a single registered COM port by its number within the enterprise/system scope and returns the fully-hydrated DTO read from the ActivityMaster warehouse.")
    @ApiResponse(responseCode = "200", description = "COM port found and hydrated")
    @ApiResponse(responseCode = "500", description = "Lookup failure")
    public Uni<CerialComPort> findComPort(@Parameter(description = "Owning enterprise name") @PathParam("enterprise") String enterpriseName,
                                          @Parameter(description = "Requesting system name (security scope)") @PathParam("requestingSystemName") String systemName,
                                          @Parameter(description = "COM port number (e.g. 3 for COM3)") @PathParam("comPort") Integer comPort)
    {
        return SessionUtils.<CerialComPort>withActivityMaster(enterpriseName, systemName, tuple -> {
            Mutiny.Session session = tuple.getItem1();
            ISystems<?, ?> system = tuple.getItem3();
            return cerialMasterService.findComPortDetailed(session, comPort, system, tuple.getItem4());
        }).onFailure().invoke(e ->
                log.error("Error finding COM port '{}' for enterprise {} and system {}: {}",
                        comPort, enterpriseName, systemName, e.getMessage(), e));
    }

    /**
     * Lists every registered COM port within the given enterprise/system scope as strongly-typed
     * {@link CerialComPort} DTOs.
     *
     * @param enterpriseName the enterprise (security scope)
     * @param systemName     the requesting system name (security scope)
     * @return a {@link Uni} emitting the list of hydrated COM ports
     */
    @GET
    @Path("{requestingSystemName}/comports")
    @Operation(summary = "List COM ports",
            description = "Lists every registered COM port within the enterprise/system scope as fully-hydrated DTOs read from the ActivityMaster warehouse.")
    @ApiResponse(responseCode = "200", description = "Registered COM ports returned")
    @ApiResponse(responseCode = "500", description = "Listing failure")
    public Uni<List<CerialComPort>> listComPorts(@Parameter(description = "Owning enterprise name") @PathParam("enterprise") String enterpriseName,
                                                 @Parameter(description = "Requesting system name (security scope)") @PathParam("requestingSystemName") String systemName)
    {
        return SessionUtils.<List<CerialComPort>>withActivityMaster(enterpriseName, systemName, tuple -> {
            Mutiny.Session session = tuple.getItem1();
            ISystems<?, ?> system = tuple.getItem3();
            return cerialMasterService.listComPortsDetailed(session, system, tuple.getItem4());
        }).onFailure().invoke(e ->
                log.error("Error listing COM ports for enterprise {} and system {}: {}",
                        enterpriseName, systemName, e.getMessage(), e));
    }

    /**
     * Creates a new COM port within the given enterprise/system scope from the supplied
     * {@link CerialComPort} body and returns the fully-hydrated DTO read back from the warehouse.
     *
     * <p>Both create and update funnel through the same {@link ICerialMasterService#addOrUpdateComPortDetailed}
     * upsert (keyed by the COM port number), so this endpoint will create the port if it does not yet
     * exist and update it otherwise.</p>
     *
     * @param enterpriseName the enterprise (security scope)
     * @param systemName     the requesting system name (security scope)
     * @param comPort        the COM port definition to persist
     * @return a {@link Uni} emitting the hydrated, persisted COM port DTO
     */
    @POST
    @Path("{requestingSystemName}/comport")
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(summary = "Create a COM port",
            description = "Creates a COM port from the supplied definition and returns the fully-hydrated DTO. Keyed by the COM port number via an upsert, so an existing port of the same number is updated rather than duplicated.")
    @ApiResponse(responseCode = "200", description = "COM port created (or updated) and hydrated")
    @ApiResponse(responseCode = "500", description = "Creation failure")
    public Uni<CerialComPort> createComPort(@Parameter(description = "Owning enterprise name") @PathParam("enterprise") String enterpriseName,
                                            @Parameter(description = "Requesting system name (security scope)") @PathParam("requestingSystemName") String systemName,
                                            CerialComPort comPort)
    {
        return SessionUtils.<CerialComPort>withActivityMaster(enterpriseName, systemName, tuple -> {
            Mutiny.Session session = tuple.getItem1();
            ISystems<?, ?> system = tuple.getItem3();
            return cerialMasterService.addOrUpdateComPortDetailed(session, comPort, system, tuple.getItem4());
        }).onFailure().invoke(e ->
                log.error("Error creating COM port '{}' for enterprise {} and system {}: {}",
                        comPort != null ? comPort.getComPort() : null, enterpriseName, systemName, e.getMessage(), e));
    }

    /**
     * Updates the COM port identified by {@code comPort} within the given enterprise/system scope from the
     * supplied {@link CerialComPort} body and returns the fully-hydrated DTO read back from the warehouse.
     *
     * <p>The path COM port number is authoritative and is applied onto the body so the resource addressed
     * by the URL is the one updated. The update is performed via the same
     * {@link ICerialMasterService#addOrUpdateComPortDetailed} upsert used by {@link #createComPort}.</p>
     *
     * @param enterpriseName the enterprise (security scope)
     * @param systemName     the requesting system name (security scope)
     * @param comPort        the COM port number to update
     * @param body           the COM port changes to apply
     * @return a {@link Uni} emitting the hydrated, updated COM port DTO
     */
    @PUT
    @Path("{requestingSystemName}/comport/{comPort}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(summary = "Update a COM port",
            description = "Updates the COM port addressed by the path number from the supplied definition and returns the fully-hydrated DTO. The path COM port number is authoritative and overrides any value in the body.")
    @ApiResponse(responseCode = "200", description = "COM port updated (or created) and hydrated")
    @ApiResponse(responseCode = "500", description = "Update failure")
    public Uni<CerialComPort> updateComPort(@Parameter(description = "Owning enterprise name") @PathParam("enterprise") String enterpriseName,
                                            @Parameter(description = "Requesting system name (security scope)") @PathParam("requestingSystemName") String systemName,
                                            @Parameter(description = "COM port number to update (e.g. 3 for COM3)") @PathParam("comPort") Integer comPort,
                                            CerialComPort body)
    {
        if (body == null) {
            body = new CerialComPort();
        }
        // The URL identifies the resource being updated — make the path value authoritative.
        body.setComPort(comPort);
        CerialComPort payload = body;
        return SessionUtils.<CerialComPort>withActivityMaster(enterpriseName, systemName, tuple -> {
            Mutiny.Session session = tuple.getItem1();
            ISystems<?, ?> system = tuple.getItem3();
            return cerialMasterService.addOrUpdateComPortDetailed(session, payload, system, tuple.getItem4());
        }).onFailure().invoke(e ->
                log.error("Error updating COM port '{}' for enterprise {} and system {}: {}",
                        comPort, enterpriseName, systemName, e.getMessage(), e));
    }
}


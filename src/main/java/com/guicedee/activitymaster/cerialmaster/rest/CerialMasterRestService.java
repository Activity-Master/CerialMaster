package com.guicedee.activitymaster.cerialmaster.rest;

import com.google.inject.Inject;
import com.guicedee.activitymaster.cerialmaster.client.dto.CerialComPort;
import com.guicedee.activitymaster.cerialmaster.client.services.ICerialMasterService;
import com.guicedee.activitymaster.fsdm.client.services.SessionUtils;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.systems.ISystems;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.GET;
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
 */
@Path("{enterprise}/cerial")
@Produces(MediaType.APPLICATION_JSON)
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
    public Uni<CerialComPort> findComPort(@PathParam("enterprise") String enterpriseName,
                                          @PathParam("requestingSystemName") String systemName,
                                          @PathParam("comPort") Integer comPort)
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
    public Uni<List<CerialComPort>> listComPorts(@PathParam("enterprise") String enterpriseName,
                                                 @PathParam("requestingSystemName") String systemName)
    {
        return SessionUtils.<List<CerialComPort>>withActivityMaster(enterpriseName, systemName, tuple -> {
            Mutiny.Session session = tuple.getItem1();
            ISystems<?, ?> system = tuple.getItem3();
            return cerialMasterService.listComPortsDetailed(session, system, tuple.getItem4());
        }).onFailure().invoke(e ->
                log.error("Error listing COM ports for enterprise {} and system {}: {}",
                        enterpriseName, systemName, e.getMessage(), e));
    }
}


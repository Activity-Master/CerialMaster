package com.guicedee.activitymaster.cerialmaster;

import com.fazecast.jSerialComm.SerialPort;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.guicedee.activitymaster.cerialmaster.client.ComPortConnection;
import com.guicedee.activitymaster.cerialmaster.client.dto.CerialComPort;
import com.guicedee.activitymaster.cerialmaster.client.services.ICerialMasterService;

import com.guicedee.activitymaster.fsdm.client.services.IResourceItemService;
import com.guicedee.activitymaster.fsdm.client.services.rest.*;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.enterprise.IEnterprise;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.resourceitem.IResourceItem;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.systems.ISystems;
import com.guicedee.activitymaster.fsdm.client.services.rest.resourceitems.*;
import com.guicedee.activitymaster.fsdm.client.services.rest.RelationshipUpdateEntry;
import com.guicedee.cerial.enumerations.ComPortType;
import io.smallrye.mutiny.Uni;
import lombok.extern.log4j.Log4j2;
import org.hibernate.reactive.mutiny.Mutiny;

import java.util.*;

import static com.guicedee.activitymaster.cerialmaster.services.enumerations.CerialMasterClassifications.*;
import static com.guicedee.activitymaster.cerialmaster.services.enumerations.CerialResourceItemTypes.*;

@Singleton
@Log4j2
public class CerialMasterService implements ICerialMasterService<CerialMasterService> {
    @Inject
    private RestClients restClients;

    @Inject
    private IResourceItemService<?> resourceItemService;

    @Override
    public Uni<ComPortConnection<?>> addOrUpdateConnection(Mutiny.Session session, ComPortConnection<?> comPort, ISystems<?, ?> system, java.util.UUID... identityToken) {
        log.info("🚀 Adding/updating COM port connection via REST: {}", comPort != null ? comPort.getComPort() : "null");

        if (comPort == null || comPort.getComPort() == null) {
            log.error("❌ Invalid COM port provided - port or number is null");
            return Uni.createFrom()
                    .failure(new UnsupportedOperationException("ComPort or number is null"));
        }

        // The full classification set describing this connection — shared by both the create and the
        // update branch so the persisted shape is identical regardless of which path runs.
        Map<String, String> classifications = new LinkedHashMap<>();
        classifications.put(ComPort.toString(), "");
        classifications.put(ComPortNumber.toString(), comPort.getComPort() + "");
        classifications.put(ComPortDeviceType.toString(), comPort.getComPortType().toString());
        classifications.put(ComPortStatus.toString(), comPort.getComPortStatus().toString());
        classifications.put(BaudRate.toString(), comPort.getBaudRate().toInt() + "");
        classifications.put(BufferSize.toString(), comPort.getBufferSize() + "");
        classifications.put(DataBits.toString(), comPort.getDataBits().toInt() + "");
        classifications.put(StopBits.toString(), comPort.getStopBits().toInt() + "");
        classifications.put(Parity.toString(), comPort.getParity().toInt() + "");

        // Genuine upsert: look for an existing SerialConnectionPort for this port number first so that a
        // repeated call (e.g. a REST PUT) updates the existing row's classifications instead of inserting
        // a duplicate resource item.
        ResourceItemSearchDTO searchDto = new ResourceItemSearchDTO();
        searchDto.resourceItemType = SerialConnectionPort.toString();
        searchDto.classificationName = ComPortNumber.toString();
        searchDto.classificationValue = comPort.getComPort() + "";
        searchDto.maxResults = 1;
        searchDto.sortField = SearchSortField.WAREHOUSE_CREATED_TIMESTAMP;
        searchDto.sortDirection = SortDirection.DESC;

        return restClients.searchResourceItems(requestingSystemName(system), searchDto)
                .chain(results -> {
                    if (results == null || results.isEmpty()) {
                        return createConnection(comPort, classifications, system);
                    }
                    ResourceItemDTO found = results.get(0);
                    log.info("🔄 Existing resource item {} found for COM port {} - updating classifications", found.resourceItemId, comPort.getComPort());
                    comPort.setId(found.resourceItemId);

                    ResourceItemUpdateDTO updateDto = new ResourceItemUpdateDTO();
                    updateDto.resourceItemId = found.resourceItemId;
                    RelationshipUpdateEntry classificationUpdate = new RelationshipUpdateEntry();
                    classificationUpdate.addOrUpdate = classifications;
                    updateDto.classifications = classificationUpdate;

                    return restClients.updateResourceItem(requestingSystemName(system), updateDto)
                            .onItem()
                            .invoke(updated -> log.info("✅ COM port connection {} successfully updated via REST", comPort.getComPort()))
                            .replaceWith(comPort);
                })
                .onFailure()
                .invoke(error -> log.error("❌ Failed to add/update COM port {}: {}", comPort.getComPort(), error.getMessage(), error));
    }

    /**
     * Create branch of {@link #addOrUpdateConnection}: persists a brand-new {@code SerialConnectionPort}
     * resource item with the supplied classifications.
     */
    private Uni<ComPortConnection<?>> createConnection(ComPortConnection<?> comPort, Map<String, String> classifications, ISystems<?, ?> system) {
        ResourceItemCreateDTO createDto = new ResourceItemCreateDTO();
        createDto.type = SerialConnectionPort.toString();
        createDto.dataValue = comPort.getComPort() + "";
        createDto.classifications = classifications;

        return restClients.createResourceItem(requestingSystemName(system), createDto)
                .onItem()
                .invoke(result -> {
                    log.info("✅ Resource item created via REST with ID: {}", result.resourceItemId);
                    comPort.setId(result.resourceItemId);
                })
                .replaceWith(comPort);
    }

    @Override
    public Uni<CerialComPort> addOrUpdateComPortDetailed(Mutiny.Session session, CerialComPort comPort, ISystems<?, ?> system, java.util.UUID... identityToken) {
        if (comPort == null || comPort.getComPort() == null) {
            return Uni.createFrom().failure(new IllegalArgumentException("CerialComPort or its comPort number is null"));
        }
        log.info("🚀 Add/update CerialComPort DTO for port {} (delegating to addOrUpdateConnection)", comPort.getComPort());

        // Map the transport DTO onto the runtime ComPortConnection so the single write path
        // (addOrUpdateConnection) is reused, then read the canonical shape back from the warehouse.
        ComPortConnection<?> connection = toConnection(comPort);
        return addOrUpdateConnection(session, connection, system, identityToken)
                .chain(saved -> findComPortDetailed(session, saved.getComPort(), system, identityToken));
    }

    /**
     * Maps a transport {@link CerialComPort} DTO onto a runtime {@link ComPortConnection}, parsing the
     * DTO's string/numeric fields into the strongly-typed serial enumerations. Unset (null) fields fall
     * back to the {@link ComPortConnection} defaults so a partial DTO still yields a valid connection.
     */
    private static ComPortConnection<?> toConnection(CerialComPort dto) {
        ComPortType deviceType = dto.getDeviceType() != null ? ComPortType.valueOf(dto.getDeviceType()) : ComPortType.Device;
        // Use the constructor (not getOrCreate) so a metadata-only REST write does not mutate the static
        // runtime PORT_CONNECTIONS registry — this connection is a transient mapping carrier only.
        ComPortConnection<?> connection = new ComPortConnection<>(dto.getComPort(), deviceType);
        if (dto.getResourceItemId() != null) {
            connection.setId(dto.getResourceItemId());
        }
        if (dto.getStatus() != null) {
            connection.setComPortStatus(com.guicedee.cerial.enumerations.ComPortStatus.valueOf(dto.getStatus()), true);
        }
        if (dto.getBaudRate() != null) {
            connection.setBaudRate(com.guicedee.cerial.enumerations.BaudRate.from(dto.getBaudRate() + ""));
        }
        if (dto.getBufferSize() != null) {
            connection.setBufferSize(dto.getBufferSize());
        }
        if (dto.getDataBits() != null) {
            connection.setDataBits(com.guicedee.cerial.enumerations.DataBits.fromString(dto.getDataBits() + ""));
        }
        if (dto.getStopBits() != null) {
            connection.setStopBits(com.guicedee.cerial.enumerations.StopBits.from(dto.getStopBits() + ""));
        }
        if (dto.getParity() != null) {
            connection.setParity(com.guicedee.cerial.enumerations.Parity.from(dto.getParity() + ""));
        }
        return connection;
    }

    @Override
    public Uni<ComPortConnection<?>> updateStatus(Mutiny.Session session, ComPortConnection<?> comPort, ISystems<?, ?> system, java.util.UUID... identityToken) {
        log.info("🔄 Updating status for COM port {} to {} via REST", comPort.getComPort(), comPort.getComPortStatus());

        // First search for the resource item by classification
        ResourceItemSearchDTO searchDto = new ResourceItemSearchDTO();
        searchDto.resourceItemType = SerialConnectionPort.toString();
        searchDto.classificationName = ComPortNumber.toString();
        searchDto.classificationValue = comPort.getComPort() + "";
        searchDto.maxResults = 1;
        searchDto.sortField = SearchSortField.WAREHOUSE_CREATED_TIMESTAMP;
        searchDto.sortDirection = SortDirection.DESC;

        return restClients.searchResourceItems(requestingSystemName(system), searchDto)
                .chain(results -> {
                    if (results == null || results.isEmpty()) {
                        log.warn("⚠️ No resource item found for COM port {}", comPort.getComPort());
                        return Uni.createFrom().item(comPort);
                    }

                    ResourceItemDTO found = results.get(0);
                    log.trace("✅ Resource item found for COM port {}: ID {}", comPort.getComPort(), found.resourceItemId);

                    // Update the ComPortStatus classification
                    ResourceItemUpdateDTO updateDto = new ResourceItemUpdateDTO();
                    updateDto.resourceItemId = found.resourceItemId;
                    RelationshipUpdateEntry classificationUpdate = new RelationshipUpdateEntry();
                    classificationUpdate.addOrUpdate = Map.of(ComPortStatus.toString(), comPort.getComPortStatus().toString());
                    updateDto.classifications = classificationUpdate;

                    return restClients.updateResourceItem(requestingSystemName(system), updateDto)
                            .onItem()
                            .invoke(updated -> log.info("✅ ComPortStatus classification updated successfully for COM port {}", comPort.getComPort()))
                            .onFailure()
                            .invoke(error -> log.error("❌ Failed to update ComPortStatus classification for COM port {}: {}", comPort.getComPort(), error.getMessage(), error))
                            .chain(() -> Uni.createFrom().item(comPort));
                })
                .onFailure()
                .invoke(error -> log.error("❌ Failed to update status for COM port {}: {}", comPort.getComPort(), error.getMessage(), error));
    }


    @Override
    public Uni<ComPortConnection<?>> findComPortConnection(Mutiny.Session session, ComPortConnection<?> comPort, ISystems<?, ?> system, java.util.UUID... identityToken) {
        log.trace("🔍 Finding COM port connection for port {} via REST", comPort.getComPort());

        // Search for the resource item by classification
        ResourceItemSearchDTO searchDto = new ResourceItemSearchDTO();
        searchDto.resourceItemType = SerialConnectionPort.toString();
        searchDto.classificationName = ComPortNumber.toString();
        searchDto.classificationValue = comPort.getComPort() + "";
        searchDto.includes = List.of(ResourceItemDataIncludes.Classifications);
        searchDto.maxResults = 1;
        searchDto.sortField = SearchSortField.WAREHOUSE_CREATED_TIMESTAMP;
        searchDto.sortDirection = SortDirection.DESC;

        return restClients.searchResourceItems(requestingSystemName(system), searchDto)
                .chain(results -> {
                    if (results == null || results.isEmpty()) {
                        log.warn("⚠️ No resource item found for COM port {}", comPort.getComPort());
                        return Uni.createFrom().failure(
                                new NoSuchElementException("No resource item found for COM port " + comPort.getComPort()));
                    }

                    ResourceItemDTO found = results.get(0);
                    comPort.setId(found.resourceItemId);

                    log.trace("📊 Retrieved classifications for COM port {} via REST", comPort.getComPort());

                    Map<String, String> classifications = found.classifications;
                    if (classifications == null || classifications.isEmpty()) {
                        log.warn("⚠️ No classification values found for COM port {}", comPort.getComPort());
                        return Uni.createFrom().item(comPort);
                    }

                    // Map classification values to ComPortConnection properties
                    comPort.setComPort(Integer.parseInt(classifications.getOrDefault(ComPortNumber.toString(), comPort.getComPort() + "")));
                    comPort.setComPortType(ComPortType.valueOf(classifications.getOrDefault(ComPortDeviceType.toString(), comPort.getComPortType().toString())));
                    comPort.setComPortStatus(com.guicedee.cerial.enumerations.ComPortStatus.valueOf(
                            classifications.getOrDefault(ComPortStatus.toString(), "Silent")), true);
                    comPort.setBaudRate(com.guicedee.cerial.enumerations.BaudRate.from(
                            classifications.getOrDefault(BaudRate.toString(), "9600")));
                    comPort.setBufferSize(Integer.parseInt(
                            classifications.getOrDefault(BufferSize.toString(), "4096")));
                    comPort.setDataBits(com.guicedee.cerial.enumerations.DataBits.fromString(
                            classifications.getOrDefault(DataBits.toString(), "8")));
                    comPort.setStopBits(com.guicedee.cerial.enumerations.StopBits.from(
                            classifications.getOrDefault(StopBits.toString(), "1")));
                    comPort.setParity(com.guicedee.cerial.enumerations.Parity.from(
                            classifications.getOrDefault(Parity.toString(), "None")));

                    log.trace("✅ COM port connection {} successfully populated with configuration via REST", comPort.getComPort());
                    return Uni.createFrom().item(comPort);
                })
                .onFailure()
                .invoke(error -> log.error("❌ Failed to find COM port connection for port {}: {}", comPort.getComPort(), error.getMessage(), error));
    }

    @Override
    public Uni<ComPortConnection<?>> getComPortConnection(Mutiny.Session session, Integer comPort, IEnterprise<?, ?> enterprise) {
        return getComPortConnection(session, comPort, enterprise, null);
    }

    @Override
    public Uni<ComPortConnection<?>> getComPortConnectionDirect(Integer comPort) {
        log.trace("🔌 Direct COM port connection request for port {}", comPort);
        if (comPort == null) {
            return Uni.createFrom()
                    .failure(new IllegalArgumentException("comPort cannot be null"));
        }
        try {
            // Create or fetch the canonical connection and ensure it is registered
            ComPortConnection<?> connection = ComPortConnection.getOrCreate(comPort, ComPortType.Device);

            // Ensure a TimedComPortSender is registered with default configuration
            com.guicedee.activitymaster.cerialmaster.client.Config defaultCfg = new com.guicedee.activitymaster.cerialmaster.client.Config();
            maybeAttachTimedSender(connection, defaultCfg);

            log.trace("✅ Direct COM port connection ready for port {} and sender registered", comPort);
            return Uni.createFrom()
                    .item(connection);
        } catch (Throwable t) {
            log.error("❌ Failed to create direct COM port connection for {}: {}", comPort, t.getMessage(), t);
            return Uni.createFrom()
                    .failure(t);
        }
    }

    @Override
    public Uni<ComPortConnection<?>> getComPortConnection(Mutiny.Session session, Integer comPort, IEnterprise<?, ?> enterprise, com.guicedee.activitymaster.cerialmaster.client.Config timedConfig) {
        log.trace("🔍 Getting COM port connection for port {} via REST", comPort);

        // Use REST-based findComPortConnection — session/enterprise/system are resolved server-side
        return findComPortConnection(session, ComPortConnection.getOrCreate(comPort, ComPortType.Server), null)
                .onItem()
                .invoke(connection -> {
                    log.trace("✅ Retrieved COM port connection for port {}", comPort);
                    maybeAttachTimedSender(connection, timedConfig);
                })
                .onFailure()
                .invoke(error -> log.error("❌ Failed to get COM port connection for port {}: {}", comPort, error.getMessage(), error));
    }

    @Override
    public Uni<ComPortConnection<?>> getScannerPortConnection(Mutiny.Session session, Integer comPort, IEnterprise<?, ?> enterprise) {
        return getScannerPortConnection(session, comPort, enterprise, null);
    }

    @Override
    public Uni<ComPortConnection<?>> getScannerPortConnection(Mutiny.Session session, Integer comPort, IEnterprise<?, ?> enterprise, com.guicedee.activitymaster.cerialmaster.client.Config timedConfig) {
        log.trace("🔍 Getting scanner port connection for port {} via REST", comPort);

        // Use REST-based findComPortConnection — session/enterprise/system are resolved server-side
        return findComPortConnection(session, ComPortConnection.getOrCreate(comPort, ComPortType.Scanner), null)
                .onItem()
                .invoke(connection -> {
                    log.trace("✅ Retrieved scanner port connection for port {}", comPort);
                    maybeAttachTimedSender(connection, timedConfig);
                })
                .onFailure()
                .invoke(error -> log.error("❌ Failed to get scanner port connection for port {}: {}", comPort, error.getMessage(), error));
    }

    private static ArrayList<String> comStrings = new ArrayList<>();

    private void maybeAttachTimedSender(ComPortConnection<?> connection, com.guicedee.activitymaster.cerialmaster.client.Config cfg) {
        //we don't start any sender or sending mechanism for the scanner com port type
        if (cfg == null || connection == null || connection.getComPort() == null || connection.getComPortType() == null || connection.getComPortType() == ComPortType.Scanner) {
            return;
        }
        var sender = connection.getOrCreateTimedSender(cfg);
        // Do not auto-start here; let callers decide. They can access via ComPortConnection.getTimedSender(port)
        // This ensures registry tracking and public access as requested.
    }

    @Override
    public Uni<List<String>> listComPorts() {
        log.trace("🔍 Listing all available COM ports");

        return Uni.createFrom()
                .item(() -> {
                    if (comStrings.isEmpty()) {
                        log.trace("📋 COM port list is empty, scanning system for available ports");
                        comStrings.addAll(Arrays.stream(SerialPort.getCommPorts())
                                .map(SerialPort::getSystemPortName)
                                .toList());
                        log.trace("✅ Found {} COM ports on system", comStrings.size());
                    }
                    comStrings.sort(String::compareTo);
                    return (List<String>) new ArrayList<>(comStrings);
                })
                .onItem()
                .invoke(ports -> log.debug("📤 Returning {} COM ports: {}", ports.size(), ports))
                .onFailure()
                .invoke(error -> log.error("❌ Failed to list COM ports: {}", error.getMessage(), error));
    }


    @Override
    public Uni<List<String>> listRegisteredComPorts(Mutiny.Session session, IEnterprise<?, ?> enterprise) {
        log.trace("🔍 Listing registered COM ports via REST");

        // Search for all resource items of type SerialConnectionPort with ComPortNumber classification
        ResourceItemSearchDTO searchDto = new ResourceItemSearchDTO();
        searchDto.resourceItemType = SerialConnectionPort.toString();
        searchDto.classificationName = ComPortNumber.toString();
        // null value means return all items with ComPortNumber classification
        searchDto.classificationValue = null;
        searchDto.includes = List.of(ResourceItemDataIncludes.Classifications);

        return restClients.searchResourceItems(CerialMasterSystemName, searchDto)
                .onItem()
                .invoke(items -> log.trace("✅ Found {} registered COM port resources via REST", items.size()))
                .onFailure()
                .invoke(error -> log.error("❌ Failed to find registered COM ports via REST: {}", error.getMessage(), error))
                .chain(resourceItems -> {
                    List<String> portNames = new ArrayList<>();
                    for (var item : resourceItems) {
                        // Get the ComPortNumber from the classifications map
                        String portValue = item.classifications != null
                                ? item.classifications.get(ComPortNumber.toString())
                                : null;
                        if (portValue != null) {
                            String portName = "COM" + portValue;
                            portNames.add(portName);
                            log.debug("🔗 Found registered COM port: {}", portName);
                        }
                    }
                    portNames.sort(String::compareTo);
                    log.debug("📤 Returning {} registered COM ports: {}", portNames.size(), portNames);
                    return Uni.createFrom()
                            .item((List<String>) portNames);
                });
    }


    @Override
    public Uni<List<String>> listAvailableComPorts(Mutiny.Session session, IEnterprise<?, ?> enterprise) {
        log.trace("🔍 Listing available COM ports");

        return listComPorts().onItem()
                .invoke(allPorts -> log.trace("✅ Found {} total COM ports", allPorts.size()))
                .chain(allPorts -> listRegisteredComPorts(session, enterprise).onItem()
                        .invoke(registeredPorts -> log.trace("✅ Found {} registered COM ports", registeredPorts.size()))
                        .chain(registeredPorts -> {
                            List<String> availablePorts = new ArrayList<>(allPorts);
                            availablePorts.removeAll(registeredPorts);
                            log.trace("📊 Available COM ports: {} (Total: {} - Registered: {})", availablePorts.size(), allPorts.size(), registeredPorts.size());
                            log.debug("📤 Returning available COM ports: {}", availablePorts);
                            return Uni.createFrom()
                                    .item((List<String>) availablePorts);
                        }))
                .onFailure()
                .invoke(error -> log.error("❌ Failed to list available COM ports: {}", error.getMessage(), error));
    }

    // ---------------------------------------------------------------------------------------------
    //  Strongly-typed warehouse read path (GraphQL + REST exposure)
    // ---------------------------------------------------------------------------------------------

    @Override
    public Uni<CerialComPort> findComPortDetailed(Mutiny.Session session, Integer comPort, ISystems<?, ?> system, java.util.UUID... identityToken) {
        log.trace("🔍 Hydrating CerialComPort DTO for port {} directly from the warehouse", comPort);
        return resourceItemService.findByClassification(session, SerialConnectionPort.toString(),
                        ComPortNumber.toString(), String.valueOf(comPort), system, identityToken)
                .chain(item -> hydrateComPort(session, item, system, identityToken))
                .onFailure().invoke(error -> log.error("❌ Failed to hydrate COM port {}: {}", comPort, error.getMessage(), error));
    }

    @Override
    public Uni<List<CerialComPort>> listComPortsDetailed(Mutiny.Session session, ISystems<?, ?> system, java.util.UUID... identityToken) {
        log.trace("🔍 Listing all registered COM ports as CerialComPort DTOs from the warehouse");
        return resourceItemService.findByResourceItemType(session, SerialConnectionPort.toString(), system, identityToken)
                .chain(items -> {
                    List<CerialComPort> results = new ArrayList<>();
                    Uni<Void> chain = Uni.createFrom().voidItem();
                    for (IResourceItem<?, ?> item : items) {
                        chain = chain.chain(() -> hydrateComPort(session, item, system, identityToken)
                                .invoke(results::add)
                                .replaceWithVoid());
                    }
                    return chain.replaceWith(results);
                })
                .onFailure().invoke(error -> log.error("❌ Failed to list COM ports: {}", error.getMessage(), error));
    }

    /**
     * Reads the supporting classifications off a {@code SerialConnectionPort} resource item and maps
     * them onto a strongly-typed {@link CerialComPort} DTO.
     */
    private Uni<CerialComPort> hydrateComPort(Mutiny.Session session, IResourceItem<?, ?> item, ISystems<?, ?> system, java.util.UUID... identityToken) {
        CerialComPort dto = new CerialComPort();
        dto.setResourceItemId(item.getId());
        // Read-only hydration: read every classification on this resource item in a single
        // security-checked query instead of chaining a round-trip per field. A shared Mutiny.Session
        // cannot run operations in parallel (Hibernate Reactive constraint), so batching one query is
        // the safe equivalent of fetching them concurrently.
        return item.findClassificationValues(session, system, identityToken)
                .invoke(values -> {
                    dto.setComPort(parseInteger(values.get(ComPortNumber.toString())));
                    dto.setDeviceType(values.get(ComPortDeviceType.toString()));
                    dto.setStatus(values.get(ComPortStatus.toString()));
                    dto.setBaudRate(parseInteger(values.get(BaudRate.toString())));
                    dto.setBufferSize(parseInteger(values.get(BufferSize.toString())));
                    dto.setDataBits(parseInteger(values.get(DataBits.toString())));
                    dto.setStopBits(parseInteger(values.get(StopBits.toString())));
                    dto.setParity(parseInteger(values.get(Parity.toString())));
                })
                .replaceWith(dto);
    }


    /**
     * Resolves the requesting system name used for the warehouse REST operations so that each call runs
     * under the <em>caller's own</em> system identity (and therefore that system's own scoped security
     * token, per the {@code SessionUtils.withActivityMaster} per-system identity model) rather than always
     * borrowing the broadly-privileged Cerial Master system token.
     *
     * <p>Falls back to {@link #CerialMasterSystemName} only when no caller system is available (e.g. the
     * internal no-context reads invoked by {@code getComPortConnection}/{@code getScannerPortConnection}).</p>
     */
    private static String requestingSystemName(ISystems<?, ?> system) {
        if (system != null && system.getName() != null && !system.getName().isBlank()) {
            return system.getName();
        }
        return CerialMasterSystemName;
    }


    private static Integer parseInteger(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Integer.valueOf(value.trim());
        } catch (NumberFormatException nfe) {
            return null;
        }
    }

}

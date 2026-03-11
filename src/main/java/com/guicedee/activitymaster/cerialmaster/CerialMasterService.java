package com.guicedee.activitymaster.cerialmaster;

import com.fazecast.jSerialComm.SerialPort;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.guicedee.activitymaster.cerialmaster.client.ComPortConnection;
import com.guicedee.activitymaster.cerialmaster.client.services.ICerialMasterService;

import com.guicedee.activitymaster.fsdm.client.services.rest.*;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.enterprise.IEnterprise;
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

    @Override
    public Uni<ComPortConnection<?>> addOrUpdateConnection(Mutiny.Session session, ComPortConnection<?> comPort, ISystems<?, ?> system, java.util.UUID... identityToken) {
        log.info("🚀 Adding/updating COM port connection via REST: {}", comPort != null ? comPort.getComPort() : "null");

        if (comPort == null || comPort.getComPort() == null) {
            log.error("❌ Invalid COM port provided - port or number is null");
            return Uni.createFrom()
                    .failure(new UnsupportedOperationException("ComPort or number is null"));
        }

        // Build the create DTO with the type, data value, and all classifications
        ResourceItemCreateDTO createDto = new ResourceItemCreateDTO();
        createDto.type = SerialConnectionPort.toString();
        createDto.dataValue = comPort.getComPort() + "";

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
        createDto.classifications = classifications;

        return restClients.createResourceItem(CerialMasterSystemName, createDto)
                .onItem()
                .invoke(result -> {
                    log.info("✅ Resource item created via REST with ID: {}", result.resourceItemId);
                    comPort.setId(result.resourceItemId);
                })
                .onFailure()
                .invoke(error -> log.error("❌ Failed to create resource item for COM port {}: {}", comPort.getComPort(), error.getMessage(), error))
                .chain(() -> {
                    log.info("✅ COM port connection {} successfully added/updated via REST", comPort.getComPort());
                    return Uni.createFrom().item(comPort);
                });
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

        return restClients.searchResourceItems(CerialMasterSystemName, searchDto)
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

                    return restClients.updateResourceItem(CerialMasterSystemName, updateDto)
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

        return restClients.searchResourceItems(CerialMasterSystemName, searchDto)
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

}

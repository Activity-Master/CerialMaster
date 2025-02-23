package com.guicedee.activitymaster.cerialmaster;

import com.fazecast.jSerialComm.SerialPort;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.guicedee.activitymaster.cerialmaster.client.ComPortConnection;
import com.guicedee.activitymaster.cerialmaster.client.services.ICerialMasterService;
import com.guicedee.guicedpersistence.lambda.TransactionalBiConsumer;
import com.guicedee.activitymaster.fsdm.client.services.IResourceItemService;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.resourceitem.IResourceItem;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.resourceitem.IResourceItemType;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.systems.ISystems;
import com.guicedee.cerial.enumerations.ComPortType;
import com.guicedee.guicedpersistence.lambda.TransactionalCallable;
import com.guicedee.guicedpersistence.lambda.TransactionalConsumer;
import io.vertx.core.Vertx;
import lombok.Getter;
import lombok.extern.java.Log;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import static com.guicedee.activitymaster.cerialmaster.services.enumerations.CerialMasterClassifications.*;
import static com.guicedee.activitymaster.cerialmaster.services.enumerations.CerialResourceItemTypes.*;
import static com.guicedee.activitymaster.fsdm.client.services.IActivityMasterService.*;
import static com.guicedee.client.IGuiceContext.*;

@Singleton
@Log
public class CerialMasterService
        implements ICerialMasterService<CerialMasterService> {
    @Inject
    private IResourceItemService<?> resourceItemService;

    @Inject
    @Named(CerialMasterSystemName)
    private ISystems<?, ?> system;

    @Inject
    @Named(CerialMasterSystemName)
    private UUID identityToken;

    @Inject
    private Vertx vertx;

    @Override
    public IResourceItemType<?, ?> getSerialConnectionType(ISystems<?, ?> system, java.util.UUID... identityToken) {
        IResourceItemService<?> resourceService = get(IResourceItemService.class);
        return resourceService.findResourceItemType(SerialConnectionPort.toString(), system, identityToken);
    }

    @Override
    public ComPortConnection<?> addOrUpdateConnection(ComPortConnection<?> comPort, ISystems<?, ?> system, java.util.UUID... identityToken) {
        IResourceItemType<?, ?> comPortResourceItemType = getSerialConnectionType(system, identityToken);
        IResourceItemService<?> resourceService = get(IResourceItemService.class);
        UUID resourceItemKey = UUID.randomUUID();
        var comPortResourceItem = resourceService.create(comPortResourceItemType.getName(), resourceItemKey.toString(),
                comPort.getComPort() + "", system, identityToken);

        comPortResourceItem.thenAccept((resourceItemIn) -> {
            vertx.executeBlocking(TransactionalCallable.of(()->{
                var resourceItem = resourceService.findByUUID(UUID.fromString(resourceItemIn.getId()));
                if (resourceItem == null) {
                    log.log(Level.SEVERE,"Error retrieving resource item by uuid - " + resourceItemKey);
                    return null;
                }

                resourceItem.addOrUpdateClassification(ComPort, "", system, identityToken);
                resourceItem.addOrUpdateClassification(ComPortNumber, comPort.getComPort() + "", system, identityToken);
                resourceItem.addOrUpdateClassification(ComPortDeviceType, comPort.getComPortType()
                        .toString(), system, identityToken);
                resourceItem.addOrUpdateClassification(ComPortStatus, comPort.getComPortStatus()
                        .toString(), system, identityToken);
                resourceItem.addOrUpdateClassification(BaudRate, comPort.getBaudRate().toInt() + "", system, identityToken);
                resourceItem.addOrUpdateClassification(BufferSize, comPort.getBufferSize() + "", system, identityToken);
                resourceItem.addOrUpdateClassification(DataBits, comPort.getDataBits().toInt() + "", system, identityToken);
                resourceItem.addOrUpdateClassification(StopBits, comPort.getStopBits().toInt() + "", system, identityToken);
                resourceItem.addOrUpdateClassification(Parity, comPort.getParity().toInt() + "", system, identityToken);
                return null;
            }));
        });
        comPort.setId(resourceItemKey.toString());
        return comPort;
    }

    @Override
    public ComPortConnection<?> updateStatus(ComPortConnection<?> comPort, ISystems<?, ?> system, java.util.UUID... identityToken) {
        IResourceItemType<?, ?> comPortResourceItemType = getSerialConnectionType(system, identityToken);
        IResourceItemService<?> resourceService = get(IResourceItemService.class);
        IResourceItem<?, ?> comPortResourceItem = resourceService.findByClassification(comPortResourceItemType.getName(), ComPortNumber.toString(), comPort.getComPort() + "", system, identityToken);
        comPortResourceItem.addOrUpdateClassification(ComPortStatus, comPort.getComPortStatus()
                .toString(), system, identityToken);
        return comPort;
    }


    @Override
    public ComPortConnection<?> findComPortConnection(ComPortConnection<?> comPort, ISystems<?, ?> system, java.util.UUID... identityToken) {
        IResourceItemType<?, ?> comPortResourceItemType = getSerialConnectionType(system, identityToken);
        IResourceItemService<?> resourceService = get(IResourceItemService.class);

        IResourceItem<?, ?> comPortResourceItem = resourceService.findByClassification(comPortResourceItemType.getName(), ComPortNumber.toString(), comPort.getComPort() + "", system, identityToken);
        if (comPortResourceItem == null) {
            return null;
        }

        comPort.setResourceItem(comPortResourceItem);
        comPort.setId(comPortResourceItem.getId());

        List<Object[]> values = comPortResourceItem.builder()
                .getClassificationsValuePivot(ComPortNumber.toString(), comPortResourceItem.getId()
                                .toString(),
                        system, identityToken,
                        ComPortDeviceType.toString(),
                        ComPortStatus.toString(),
                        BaudRate.toString(),
                        BufferSize.toString(),
                        DataBits.toString(),
                        StopBits.toString(),
                        Parity.toString(),
                        ComPortAllowedCharacters.toString(),
                        ComPortEndOfMessage.toString());
        if (values.isEmpty()) {
            return comPort;
        }
        Object[] objects = values.stream()
                .findFirst()
                .orElseThrow();

        comPort.setComPort(Integer.parseInt(objects[1].toString()));
        comPort.setComPortType(ComPortType.valueOf(objects[2].toString()));
        comPort.setComPortStatus(com.guicedee.cerial.enumerations.ComPortStatus.valueOf(objects[3].toString()), true);
        comPort.setBaudRate(com.guicedee.cerial.enumerations.BaudRate.from(Integer.parseInt(objects[4] == null ? "9600" : objects[4].toString()) + ""));
        comPort.setBufferSize(Integer.parseInt(objects[5] == null ? "4096" : objects[5].toString()));
        comPort.setDataBits(com.guicedee.cerial.enumerations.DataBits.fromString(Integer.parseInt(objects[6] == "8" ? "": objects[6].toString()) + ""));
        comPort.setStopBits(com.guicedee.cerial.enumerations.StopBits.from(Integer.parseInt(objects[7] == null ? "1" : objects[7].toString()) + ""));
        comPort.setParity(com.guicedee.cerial.enumerations.Parity.from(objects[8] == null ? "None" : objects[8].toString()));

        return comPort;
    }

    @Override
    public ComPortConnection<?> getComPortConnection(Integer comPort) {
        ComPortConnection<?> comm = null;
        if (comm == null) {
            comm = findComPortConnection(new ComPortConnection<>(comPort, ComPortType.Server),
                    getISystem(CerialMasterSystemName), getISystemToken(CerialMasterSystemName));
        }
        return comm;
    }

    @Override
    public ComPortConnection<?> getScannerPortConnection(Integer comPort) {
        ComPortConnection<?> comm = null;
        if (comm == null) {
            comm = findComPortConnection(new ComPortConnection<>(comPort, ComPortType.Scanner),
                    getISystem(CerialMasterSystemName), getISystemToken(CerialMasterSystemName));
        }
        return comm;
    }

    private static ArrayList<String> comStrings = new ArrayList<>();

    @Override
    public List<String> listComPorts() {
        if (comStrings.isEmpty()) {
            comStrings.addAll(Arrays.stream(SerialPort.getCommPorts()).map(a -> a.getSystemPortName()).toList());
        }
        comStrings.sort(String::compareTo);
        return comStrings;
    }


    @Override
    public List<String> listRegisteredComPorts() {
        ArrayList<String> strings = new ArrayList<>();
        for (var iResourceItem : resourceItemService.findByClassificationAll(SerialConnectionPort.toString(), ComPortNumber.toString(), null, system, identityToken)) {
            strings.add("COM" + iResourceItem.getValue());
        }
        strings.sort(String::compareTo);
        return strings;
    }


    @Override
    public List<String> listAvailableComPorts() {
        List<String> strings = listComPorts();
        List<String> strings1 = listRegisteredComPorts();
        strings.removeAll(strings1);
        return strings;
    }

}

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
import com.guicedee.activitymaster.fsdm.db.entityassist.TransactionalCallable;
import com.guicedee.guicedpersistence.lambda.TransactionalConsumer;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.WorkerExecutor;
import lombok.Getter;
import lombok.extern.java.Log;
import lombok.extern.log4j.Log4j2;

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
@Log4j2
public class CerialMasterService
        implements ICerialMasterService<CerialMasterService>
{
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

    private WorkerExecutor workerExecutor;

    @Inject
    private void setup()
    {
        workerExecutor = vertx.createSharedWorkerExecutor("cerial-worker-executor", 20);
    }

    @Override
    public Future<IResourceItemType<?, ?>> getSerialConnectionType(ISystems<?, ?> system, java.util.UUID... identityToken)
    {
        IResourceItemService<?> resourceService = get(IResourceItemService.class);
        return resourceService.findResourceItemType(SerialConnectionPort.toString(), system, identityToken);
    }

    @Override
    public Future<ComPortConnection<?>> addOrUpdateConnection(ComPortConnection<?> comPort, ISystems<?, ?> system, java.util.UUID... identityToken)
    {
        if (comPort == null)
        {
            return Future.failedFuture("ComPort is null");
        }
        Promise<ComPortConnection<?>> promise = Promise.promise();

        // First get the serial connection type (now returns a Future)
        getSerialConnectionType(system, identityToken).compose(comPortResourceItemType -> {
            IResourceItemService<?> resourceService = get(IResourceItemService.class);
            UUID resourceItemKey = UUID.randomUUID();
            comPort.setId(resourceItemKey);

            // Create the resource item
            return resourceService.create(comPortResourceItemType.getName(), resourceItemKey,
                    comPort.getComPort() + "", system, identityToken);
        }).onComplete(handler -> {
            if (handler.failed() || handler.result() == null)
            {
                log.error("Error creating resource item - {}", comPort.getId(), handler.cause());
                promise.fail(handler.cause());
            }
            else
            {
                comPort.setId(handler.result().getId());

                // Now find the resource item by UUID and add classifications
                IResourceItemService<?> resourceService = get(IResourceItemService.class);
                resourceService.findByUUID(handler.result().getId())
                    .onComplete(resourceItemResult -> {
                        if (resourceItemResult.failed() || resourceItemResult.result() == null) {
                            log.error("Error retrieving resource item by uuid - " + handler.result().getId(), resourceItemResult.cause());
                            // Still complete the promise with the comPort since we've already created it
                            promise.complete(comPort);
                        } else {
                            // Now add all the classifications
                            IResourceItem<?, ?> resourceItem = resourceItemResult.result();

                            // Execute in a worker thread
                            workerExecutor.executeBlocking(TransactionalCallable.of(() -> {
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
                            }, true), false)
                            .onComplete(result -> {
                                // Complete the promise with the comPort
                                promise.complete(comPort);
                            });
                        }
                    });
            }
        });

        return promise.future();
    }

    @Override
    public Future<ComPortConnection<?>> updateStatus(ComPortConnection<?> comPort, ISystems<?, ?> system, java.util.UUID... identityToken)
    {
        Promise<ComPortConnection<?>> promise = Promise.promise();

        // First get the serial connection type (now returns a Future)
        getSerialConnectionType(system, identityToken).compose(comPortResourceItemType -> {
            IResourceItemService<?> resourceService = get(IResourceItemService.class);

            // Find the resource item by classification
            return resourceService.findByClassification(
                comPortResourceItemType.getName(), 
                ComPortNumber.toString(), 
                comPort.getComPort() + "", 
                system, 
                identityToken
            );
        }).onComplete(resourceItemResult -> {
            if (resourceItemResult.failed() || resourceItemResult.result() == null) {
                log.error("Error finding resource item for comPort: " + comPort.getComPort(), resourceItemResult.cause());
                promise.fail(resourceItemResult.cause());
            } else {
                // Update the classification in a worker thread
                workerExecutor.executeBlocking(TransactionalCallable.of(() -> {
                    IResourceItem<?, ?> resourceItem = resourceItemResult.result();
                    resourceItem.addOrUpdateClassification(
                        ComPortStatus, 
                        comPort.getComPortStatus().toString(), 
                        system, 
                        identityToken
                    );
                    return null;
                }, true), false)
                .onComplete(result -> {
                    if (result.failed()) {
                        log.error("Error updating status for comPort: " + comPort.getComPort(), result.cause());
                        promise.fail(result.cause());
                    } else {
                        promise.complete(comPort);
                    }
                });
            }
        });

        return promise.future();
    }


    @Override
    public Future<ComPortConnection<?>> findComPortConnection(ComPortConnection<?> comPort, ISystems<?, ?> system, java.util.UUID... identityToken)
    {
        Promise<ComPortConnection<?>> promise = Promise.promise();

        // First get the serial connection type (now returns a Future)
        getSerialConnectionType(system, identityToken).compose(comPortResourceItemType -> {
            IResourceItemService<?> resourceService = get(IResourceItemService.class);

            // Find the resource item by classification
            return resourceService.findByClassification(
                comPortResourceItemType.getName(), 
                ComPortNumber.toString(), 
                comPort.getComPort() + "", 
                system, 
                identityToken
            );
        }).onComplete(resourceItemResult -> {
            if (resourceItemResult.failed()) {
                log.error("Error finding resource item for comPort: " + comPort.getComPort(), resourceItemResult.cause());
                promise.fail(resourceItemResult.cause());
                return;
            }

            IResourceItem<?, ?> comPortResourceItem = resourceItemResult.result();
            if (comPortResourceItem == null) {
                promise.complete(null);
                return;
            }

            // Set the resource item and ID
            comPort.setResourceItem(comPortResourceItem);
            comPort.setId(comPortResourceItem.getId());

            // Get the classifications in a worker thread
            workerExecutor.executeBlocking(TransactionalCallable.of(() -> {
                List<Object[]> values = comPortResourceItem.builder()
                        .getClassificationsValuePivot(ComPortNumber.toString(), comPortResourceItem.getId().toString(),
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
                comPort.setDataBits(com.guicedee.cerial.enumerations.DataBits.fromString(Integer.parseInt(objects[6] == "8" ? "" : objects[6].toString()) + ""));
                comPort.setStopBits(com.guicedee.cerial.enumerations.StopBits.from(Integer.parseInt(objects[7] == null ? "1" : objects[7].toString()) + ""));
                comPort.setParity(com.guicedee.cerial.enumerations.Parity.from(objects[8] == null ? "None" : objects[8].toString()));

                return comPort;
            }, false), false)
            .onComplete(result -> {
                if (result.failed()) {
                    log.error("Error processing classifications for comPort: " + comPort.getComPort(), result.cause());
                    promise.fail(result.cause());
                } else {
                    promise.complete(result.result());
                }
            });
        });

        return promise.future();
    }

    @Override
    public Future<ComPortConnection<?>> getComPortConnection(Integer comPort)
    {
        return findComPortConnection(
            new ComPortConnection<>(comPort, ComPortType.Server),
            getISystem(CerialMasterSystemName), 
            getISystemToken(CerialMasterSystemName)
        );
    }

    @Override
    public Future<ComPortConnection<?>> getScannerPortConnection(Integer comPort)
    {
        return findComPortConnection(
            new ComPortConnection<>(comPort, ComPortType.Scanner),
            getISystem(CerialMasterSystemName), 
            getISystemToken(CerialMasterSystemName)
        );
    }

    private static ArrayList<String> comStrings = new ArrayList<>();

    @Override
    public Future<List<String>> listComPorts()
    {
        log.debug("Listing all COM ports");
        return vertx.executeBlocking(TransactionalCallable.of(() -> {
            if (comStrings.isEmpty())
            {
                comStrings.addAll(Arrays.stream(SerialPort.getCommPorts()).map(a -> a.getSystemPortName()).toList());
            }
            comStrings.sort(String::compareTo);
            return new ArrayList<>(comStrings);
        }, false), false);
    }


    @Override
    public Future<List<String>> listRegisteredComPorts()
    {
        log.debug("Listing registered COM ports");
        return resourceItemService.findByClassificationAll(
                SerialConnectionPort.toString(), 
                ComPortNumber.toString(), 
                null, 
                system, 
                identityToken
            )
            .map(resourceItems -> {
                ArrayList<String> strings = new ArrayList<>();
                for (var iResourceItem : resourceItems)
                {
                    strings.add("COM" + iResourceItem.getValue());
                }
                strings.sort(String::compareTo);
                return strings;
            });
    }


    @Override
    public Future<List<String>> listAvailableComPorts()
    {
        log.debug("Listing available COM ports");
        return listComPorts()
            .compose(allPorts -> 
                listRegisteredComPorts()
                    .map(registeredPorts -> {
                        List<String> availablePorts = new ArrayList<>(allPorts);
                        availablePorts.removeAll(registeredPorts);
                        return availablePorts;
                    })
            );
    }

}

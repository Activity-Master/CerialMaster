package com.guicedee.activitymaster.cerialmaster;

import com.fazecast.jSerialComm.SerialPort;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.guicedee.activitymaster.cerialmaster.client.ComPortConnection;
import com.guicedee.activitymaster.cerialmaster.client.services.ICerialMasterService;

import com.guicedee.activitymaster.fsdm.client.services.IResourceItemService;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.enterprise.IEnterprise;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.resourceitem.IResourceItemType;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.systems.ISystems;
import com.guicedee.cerial.enumerations.ComPortType;
import io.smallrye.mutiny.Uni;
import io.vertx.core.Vertx;
import io.vertx.core.WorkerExecutor;
import lombok.extern.log4j.Log4j2;
import org.hibernate.reactive.mutiny.Mutiny;

import java.util.*;

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

    // TODO: Remove these injected fields after full migration to reactive pattern
    // These fields are being replaced by calls to getISystem and getISystemToken
    // They are kept temporarily as fallbacks during the migration
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
    public Uni<IResourceItemType<?, ?>> getSerialConnectionType(Mutiny.Session session, ISystems<?, ?> system, java.util.UUID... identityToken)
    {
        log.debug("🔍 Retrieving serial connection type for system: '{}' using session: {}", system.getName(), session.hashCode());

        IResourceItemService<?> resourceService = get(IResourceItemService.class);
        return resourceService.findResourceItemType(session, SerialConnectionPort.toString(), system, identityToken)
                       .onItem()
                       .invoke(type -> log.debug("✅ Serial connection type retrieved: '{}'", type.getName()))
                       .onFailure()
                       .invoke(error -> log.error("❌ Failed to retrieve serial connection type for system '{}': {}",
                               system.getName(), error.getMessage(), error));
    }

    @Override
    public Uni<ComPortConnection<?>> addOrUpdateConnection(Mutiny.Session session, ComPortConnection<?> comPort, ISystems<?, ?> system, java.util.UUID... identityToken)
    {
        log.info("🚀 Adding/updating COM port connection: {} using external session", comPort != null ? comPort.getComPort() : "null");

        if (comPort == null || comPort.getComPort() == null)
        {
            log.error("❌ Invalid COM port provided - port or number is null");
            return Uni.createFrom()
                           .failure(new UnsupportedOperationException("ComPort or number is null"));
        }

        log.debug("📋 Retrieving serial connection type for system: {} with session: {}", system.getName(), session.hashCode());

        // First get the serial connection type (now uses session parameter)
        return getSerialConnectionType(session, system, identityToken).chain(comPortResourceItemType ->
                {
                    log.debug("✅ Serial connection type retrieved: '{}'", comPortResourceItemType.getName());

                    IResourceItemService<?> resourceService = get(IResourceItemService.class);
                    UUID resourceItemKey = UUID.randomUUID();
                    comPort.setId(resourceItemKey);

                    log.debug("💾 Creating resource item for COM port {} using external session", comPort.getComPort());

                    // Create the resource item using provided session
                    return resourceService.create(session, comPortResourceItemType.getName(), resourceItemKey,
                            comPort.getComPort() + "", system, identityToken);
                })
                       .onItem()
                       .invoke(result -> {
                           log.debug("✅ Resource item created with ID: {}", result.getId());
                           comPort.setId(result.getId());
                       })
                       .onFailure()
                       .invoke(error -> {
                           log.error("❌ Failed to create resource item for COM port {}: {}",
                                   comPort.getComPort(), error.getMessage(), error);
                       })
                       .chain(result ->
                       {
                           log.debug("🔗 Adding classifications for COM port {} using external session", comPort.getComPort());

                           IResourceItemService<?> resourceService = get(IResourceItemService.class);
                           return resourceService.findByUUID(session, result.getId())
                                          .chain(resourceItemResult -> {
                                              // Create list of classification operations to run in parallel
                                              List<Uni<?>> classificationOperations = new ArrayList<>();

                                              classificationOperations.add(
                                                      resourceItemResult.addOrUpdateClassification(session, ComPort, "", system, identityToken)
                                                              .onItem()
                                                              .invoke(() -> log.debug("✅ ComPort classification added"))
                                                              .onFailure()
                                                              .invoke(error -> log.error("❌ Failed to add ComPort classification: {}", error.getMessage()))
                                              );

                                              classificationOperations.add(
                                                      resourceItemResult.addOrUpdateClassification(session, ComPortNumber, comPort.getComPort() + "", system, identityToken)
                                                              .onItem()
                                                              .invoke(() -> log.debug("✅ ComPortNumber classification added"))
                                                              .onFailure()
                                                              .invoke(error -> log.error("❌ Failed to add ComPortNumber classification: {}", error.getMessage()))
                                              );

                                              classificationOperations.add(
                                                      resourceItemResult.addOrUpdateClassification(session,
                                                                      ComPortDeviceType,
                                                                      comPort.getComPortType()
                                                                              .toString(),
                                                                      system,
                                                                      identityToken)
                                                              .onItem()
                                                              .invoke(() -> log.debug("✅ ComPortDeviceType classification added"))
                                                              .onFailure()
                                                              .invoke(error -> log.error("❌ Failed to add ComPortDeviceType classification: {}", error.getMessage()))
                                              );

                                              classificationOperations.add(
                                                      resourceItemResult.addOrUpdateClassification(session,
                                                                      ComPortStatus,
                                                                      comPort.getComPortStatus()
                                                                              .toString(),
                                                                      system,
                                                                      identityToken)
                                                              .onItem()
                                                              .invoke(() -> log.debug("✅ ComPortStatus classification added"))
                                                              .onFailure()
                                                              .invoke(error -> log.error("❌ Failed to add ComPortStatus classification: {}", error.getMessage()))
                                              );

                                              classificationOperations.add(
                                                      resourceItemResult.addOrUpdateClassification(session,
                                                                      BaudRate,
                                                                      comPort.getBaudRate()
                                                                              .toInt() + "",
                                                                      system,
                                                                      identityToken)
                                                              .onItem()
                                                              .invoke(() -> log.debug("✅ BaudRate classification added"))
                                                              .onFailure()
                                                              .invoke(error -> log.error("❌ Failed to add BaudRate classification: {}", error.getMessage()))
                                              );

                                              classificationOperations.add(
                                                      resourceItemResult.addOrUpdateClassification(session, BufferSize, comPort.getBufferSize() + "", system, identityToken)
                                                              .onItem()
                                                              .invoke(() -> log.debug("✅ BufferSize classification added"))
                                                              .onFailure()
                                                              .invoke(error -> log.error("❌ Failed to add BufferSize classification: {}", error.getMessage()))
                                              );

                                              classificationOperations.add(
                                                      resourceItemResult.addOrUpdateClassification(session,
                                                                      DataBits,
                                                                      comPort.getDataBits()
                                                                              .toInt() + "",
                                                                      system,
                                                                      identityToken)
                                                              .onItem()
                                                              .invoke(() -> log.debug("✅ DataBits classification added"))
                                                              .onFailure()
                                                              .invoke(error -> log.error("❌ Failed to add DataBits classification: {}", error.getMessage()))
                                              );

                                              classificationOperations.add(
                                                      resourceItemResult.addOrUpdateClassification(session,
                                                                      StopBits,
                                                                      comPort.getStopBits()
                                                                              .toInt() + "",
                                                                      system,
                                                                      identityToken)
                                                              .onItem()
                                                              .invoke(() -> log.debug("✅ StopBits classification added"))
                                                              .onFailure()
                                                              .invoke(error -> log.error("❌ Failed to add StopBits classification: {}", error.getMessage()))
                                              );

                                              classificationOperations.add(
                                                      resourceItemResult.addOrUpdateClassification(session,
                                                                      Parity,
                                                                      comPort.getParity()
                                                                              .toInt() + "",
                                                                      system,
                                                                      identityToken)
                                                              .onItem()
                                                              .invoke(() -> log.debug("✅ Parity classification added"))
                                                              .onFailure()
                                                              .invoke(error -> log.error("❌ Failed to add Parity classification: {}", error.getMessage()))
                                              );

                                              log.info("🔄 Running {} classification operations in parallel for COM port {}",
                                                      classificationOperations.size(), comPort.getComPort());

                                              // Run all classification operations in parallel
                                              return Uni.combine()
                                                             .all()
                                                             .unis(classificationOperations)
                                                             .discardItems()
                                                             .onItem()
                                                             .invoke(() -> log.info("🎉 All classifications added successfully for COM port {}", comPort.getComPort()))
                                                             .onFailure()
                                                             .invoke(error -> log.error("💥 One or more classification operations failed for COM port {}: {}",
                                                                     comPort.getComPort(), error.getMessage(), error));
                                          });
                       })
                       .chain(() -> {
                           log.info("✅ COM port connection {} successfully added/updated", comPort.getComPort());
                           return Uni.createFrom()
                                          .item(comPort);
                       });
    }

    @Override
    public Uni<ComPortConnection<?>> updateStatus(Mutiny.Session session, ComPortConnection<?> comPort, ISystems<?, ?> system, java.util.UUID... identityToken)
    {
        log.info("🔄 Updating status for COM port {} to {} using external session",
                comPort.getComPort(), comPort.getComPortStatus());

        log.debug("📋 Retrieving serial connection type for system: {} with session: {}",
                system.getName(), session.hashCode());

        // First get the serial connection type using session parameter
        return getSerialConnectionType(session, system, identityToken)
                       .onItem()
                       .invoke(type -> log.debug("✅ Serial connection type retrieved: '{}'", type.getName()))
                       .onFailure()
                       .invoke(error -> log.error("❌ Failed to get serial connection type for COM port {}: {}",
                               comPort.getComPort(), error.getMessage(), error))
                       .chain(comPortResourceItemType -> {
                           log.debug("🔍 Finding resource item for COM port {} by classification", comPort.getComPort());

                           IResourceItemService<?> resourceService = get(IResourceItemService.class);

                           // Find the resource item by classification using provided session
                           return resourceService.findByClassification(
                                   session,
                                   comPortResourceItemType.getName(),
                                   ComPortNumber.toString(),
                                   comPort.getComPort() + "",
                                   system,
                                   identityToken
                           );
                       })
                       .onItem()
                       .invoke(resourceItem -> log.debug("✅ Resource item found for COM port {}: ID {}",
                               comPort.getComPort(), resourceItem != null ? resourceItem.getId() : "null"))
                       .onFailure()
                       .invoke(error -> log.error("❌ Failed to find resource item for COM port {}: {}",
                               comPort.getComPort(), error.getMessage(), error))
                       .chain(resourceItem -> {
                           if (resourceItem == null)
                           {
                               log.warn("⚠️ No resource item found for COM port {}", comPort.getComPort());
                               return Uni.createFrom()
                                              .item(comPort);
                           }

                           log.debug("🔗 Updating ComPortStatus classification for COM port {} using external session",
                                   comPort.getComPort());

                           // Update the classification using the provided session
                           return resourceItem.addOrUpdateClassification(
                                           session,
                                           ComPortStatus,
                                           comPort.getComPortStatus()
                                                   .toString(),
                                           system,
                                           identityToken
                                   )
                                          .onItem()
                                          .invoke(() -> log.debug("✅ ComPortStatus classification updated successfully for COM port {}",
                                                  comPort.getComPort()))
                                          .onFailure()
                                          .invoke(error -> log.error("❌ Failed to update ComPortStatus classification for COM port {}: {}",
                                                  comPort.getComPort(), error.getMessage(), error))
                                          .chain(() -> {
                                              log.info("✅ Status update completed successfully for COM port {}", comPort.getComPort());
                                              return Uni.createFrom()
                                                             .item(comPort);
                                          });
                       });
    }


    @Override
    public Uni<ComPortConnection<?>> findComPortConnection(Mutiny.Session session, ComPortConnection<?> comPort, ISystems<?, ?> system, java.util.UUID... identityToken)
    {
        log.info("🔍 Finding COM port connection for port {} using external session", comPort.getComPort());

        log.debug("📋 Retrieving serial connection type for system: {} with session: {}",
                system.getName(), session.hashCode());

        // First get the serial connection type using session parameter
        return getSerialConnectionType(session, system, identityToken)
                       .onItem()
                       .invoke(type -> log.debug("✅ Serial connection type retrieved: '{}'", type.getName()))
                       .onFailure()
                       .invoke(error -> log.error("❌ Failed to get serial connection type for COM port {}: {}",
                               comPort.getComPort(), error.getMessage(), error))
                       .chain(comPortResourceItemType -> {
                           log.debug("🔍 Finding resource item for COM port {} by classification", comPort.getComPort());

                           IResourceItemService<?> resourceService = get(IResourceItemService.class);

                           // Find the resource item by classification using provided session
                           return resourceService.findByClassification(
                                   session,
                                   comPortResourceItemType.getName(),
                                   ComPortNumber.toString(),
                                   comPort.getComPort() + "",
                                   system,
                                   identityToken
                           );
                       })
                       .onItem()
                       .invoke(resourceItem -> {
                           if (resourceItem != null)
                           {
                               log.debug("✅ Resource item found for COM port {}: ID {}", comPort.getComPort(), resourceItem.getId());
                           }
                           else
                           {
                               log.debug("❌ No resource item found for COM port {}", comPort.getComPort());
                           }
                       })
                       .onFailure()
                       .invoke(error -> log.error("❌ Failed to find resource item for COM port {}: {}",
                               comPort.getComPort(), error.getMessage(), error))
                       .chain(comPortResourceItem -> {
                           if (comPortResourceItem == null)
                           {
                               log.warn("⚠️ No resource item found for COM port {}", comPort.getComPort());
                               return Uni.createFrom()
                                              .item((ComPortConnection<?>) null);
                           }

                           // Set the resource item and ID
                           comPort.setResourceItem(comPortResourceItem);
                           comPort.setId(comPortResourceItem.getId());

                           log.debug("📊 Retrieving classifications for COM port {} using external session", comPort.getComPort());

                           // Get the classifications using EntityAssist query builder
                           return comPortResourceItem.builder(session)
                                          .getClassificationsValuePivot(
                                                  session, ComPortNumber.toString(),
                                                  comPortResourceItem.getId()
                                                          .toString(),
                                                  system,
                                                  identityToken,
                                                  ComPortDeviceType.toString(),
                                                  ComPortStatus.toString(),
                                                  BaudRate.toString(),
                                                  BufferSize.toString(),
                                                  DataBits.toString(),
                                                  StopBits.toString(),
                                                  Parity.toString(),
                                                  ComPortAllowedCharacters.toString(),
                                                  ComPortEndOfMessage.toString()
                                          )
                                          .onItem()
                                          .invoke(values -> log.debug("✅ Retrieved {} classification values for COM port {}",
                                                  values.size(), comPort.getComPort()))
                                          .onFailure()
                                          .invoke(error -> log.error("❌ Failed to retrieve classifications for COM port {}: {}",
                                                  comPort.getComPort(), error.getMessage(), error))
                                          .chain(values -> {
                                              if (values.isEmpty())
                                              {
                                                  log.warn("⚠️ No classification values found for COM port {}", comPort.getComPort());
                                                  return Uni.createFrom()
                                                                 .item(comPort);
                                              }

                                              // Process the first result (expecting only one match)
                                              Object[] objects = values.stream()
                                                                         .findFirst()
                                                                         .orElseThrow()
                                                      ;

                                              log.debug("🔗 Populating COM port {} configuration from classifications", comPort.getComPort());

                                              // Map classification values to ComPortConnection properties
                                              comPort.setComPort(Integer.parseInt(objects[1].toString()));
                                              comPort.setComPortType(ComPortType.valueOf(objects[2].toString()));
                                              comPort.setComPortStatus(com.guicedee.cerial.enumerations.ComPortStatus.valueOf(objects[3].toString()), true);
                                              comPort.setBaudRate(com.guicedee.cerial.enumerations.BaudRate.from(Integer.parseInt(objects[4] == null ? "9600" : objects[4].toString()) + ""));
                                              comPort.setBufferSize(Integer.parseInt(objects[5] == null ? "4096" : objects[5].toString()));
                                              comPort.setDataBits(com.guicedee.cerial.enumerations.DataBits.fromString(Integer.parseInt(objects[6] == "8" ? "" : objects[6].toString()) + ""));
                                              comPort.setStopBits(com.guicedee.cerial.enumerations.StopBits.from(Integer.parseInt(objects[7] == null ? "1" : objects[7].toString()) + ""));
                                              comPort.setParity(com.guicedee.cerial.enumerations.Parity.from(objects[8] == null ? "None" : objects[8].toString()));

                                              log.info("✅ COM port connection {} successfully populated with configuration", comPort.getComPort());
                                              return Uni.createFrom()
                                                             .item(comPort);
                                          });
                       });
    }


    public Uni<ComPortConnection<?>> getComPortConnection(Mutiny.Session session, Integer comPort)
    {
        log.debug("🔍 Getting COM port connection for port {} using external session (backward compatibility)", comPort);
        // Call the overloaded method with null enterprise to use the reactive pattern
        return getComPortConnection(session, comPort, null);
    }

    @Override
    public Uni<ComPortConnection<?>> getComPortConnection(Mutiny.Session session, Integer comPort, IEnterprise<?, ?> enterprise)
    {
        log.debug("🔍 Getting COM port connection for port {} using external session with enterprise context", comPort);

        if (enterprise == null)
        {
            return getComPortConnection(session, comPort);
        }

        return getISystem(session, CerialMasterSystemName, enterprise)
                       .onItem()
                       .invoke(systemCtx -> log.debug("✅ Retrieved CerialMaster system for COM port {}", comPort))
                       .onFailure()
                       .invoke(error -> log.error("❌ Failed to retrieve CerialMaster system for COM port {}: {}",
                               comPort, error.getMessage(), error))
                       .chain(systemCtx ->
                                      getISystemToken(session, CerialMasterSystemName, enterprise)
                                              .onItem()
                                              .invoke(token -> log.debug("✅ Retrieved system token for COM port {}", comPort))
                                              .chain(token -> findComPortConnection(
                                                      session,
                                                      new ComPortConnection<>(comPort, ComPortType.Server),
                                                      systemCtx,
                                                      token
                                              ))
                       )
                       .onItem()
                       .invoke(connection -> log.debug("✅ Retrieved COM port connection for port {}", comPort))
                       .onFailure()
                       .invoke(error -> log.error("❌ Failed to get COM port connection for port {}: {}",
                               comPort, error.getMessage(), error));
    }


    public Uni<ComPortConnection<?>> getScannerPortConnection(Mutiny.Session session, Integer comPort)
    {
        log.debug("🔍 Getting scanner port connection for port {} using external session (backward compatibility)", comPort);
        // Call the overloaded method with null enterprise to use the reactive pattern
        return getScannerPortConnection(session, comPort, null);
    }

    @Override
    public Uni<ComPortConnection<?>> getScannerPortConnection(Mutiny.Session session, Integer comPort, IEnterprise<?, ?> enterprise)
    {
        log.debug("🔍 Getting scanner port connection for port {} using external session with enterprise context", comPort);

        if (enterprise == null)
        {
            return getScannerPortConnection(session, comPort);
        }

        return getISystem(session, CerialMasterSystemName, enterprise)
                       .onItem()
                       .invoke(systemCtx -> log.debug("✅ Retrieved CerialMaster system for scanner port {}", comPort))
                       .onFailure()
                       .invoke(error -> log.error("❌ Failed to retrieve CerialMaster system for scanner port {}: {}",
                               comPort, error.getMessage(), error))
                       .chain(systemCtx ->
                                      getISystemToken(session, CerialMasterSystemName, enterprise)
                                              .onItem()
                                              .invoke(token -> log.debug("✅ Retrieved system token for scanner port {}", comPort))
                                              .chain(token -> findComPortConnection(
                                                      session,
                                                      new ComPortConnection<>(comPort, ComPortType.Scanner),
                                                      systemCtx,
                                                      token
                                              ))
                       )
                       .onItem()
                       .invoke(connection -> log.debug("✅ Retrieved scanner port connection for port {}", comPort))
                       .onFailure()
                       .invoke(error -> log.error("❌ Failed to get scanner port connection for port {}: {}",
                               comPort, error.getMessage(), error));
    }

    private static ArrayList<String> comStrings = new ArrayList<>();

    @Override
    public Uni<List<String>> listComPorts()
    {
        log.debug("🔍 Listing all available COM ports");

        return Uni.createFrom()
                       .item(() -> {
                           if (comStrings.isEmpty())
                           {
                               log.debug("📋 COM port list is empty, scanning system for available ports");
                               comStrings.addAll(Arrays.stream(SerialPort.getCommPorts())
                                                         .map(SerialPort::getSystemPortName)
                                                         .toList());
                               log.debug("✅ Found {} COM ports on system", comStrings.size());
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
    public Uni<List<String>> listRegisteredComPorts(Mutiny.Session session)
    {
        log.debug("🔍 Listing registered COM ports using external session: {}", session.hashCode());
    
        // Get enterprise from session context or use a default one
        IEnterprise<?, ?> enterprise = null;
    
        // Use reactive pattern with getISystem and getISystemToken
        return getISystem(session, CerialMasterSystemName, enterprise)
            .onItem()
            .invoke(systemCtx -> log.debug("✅ Retrieved CerialMaster system for listing registered COM ports"))
            .onFailure()
            .recoverWithItem(() -> {
                log.warn("⚠️ Failed to retrieve CerialMaster system, falling back to injected system");
                return system;
            })
            .chain(systemCtx ->
                getISystemToken(session, CerialMasterSystemName, enterprise)
                    .onItem()
                    .invoke(token -> log.debug("✅ Retrieved system token for listing registered COM ports"))
                    .onFailure()
                    .recoverWithItem(() -> {
                        log.warn("⚠️ Failed to retrieve system token, falling back to injected token");
                        return identityToken;
                    })
                    .chain(token -> 
                        resourceItemService.findByClassificationAll(
                            session, SerialConnectionPort.toString(),
                            ComPortNumber.toString(),
                            null,
                            systemCtx,
                            token
                        )
                    )
            )
            .onItem()
            .invoke(items -> log.debug("✅ Found {} registered COM port resources", items.size()))
            .onFailure()
            .invoke(error -> log.error("❌ Failed to find registered COM ports: {}", error.getMessage(), error))
            .chain(resourceItems -> {
                List<String> portNames = new ArrayList<>();
                for (var iResourceItem : resourceItems)
                {
                    String portName = "COM" + iResourceItem.getValue();
                    portNames.add(portName);
                    log.debug("🔗 Found registered COM port: {}", portName);
                }
                portNames.sort(String::compareTo);
                log.debug("📤 Returning {} registered COM ports: {}", portNames.size(), portNames);
                return Uni.createFrom().item((List<String>) portNames);
            });
    }


    @Override
    public Uni<List<String>> listAvailableComPorts(Mutiny.Session session)
    {
        log.debug("🔍 Listing available COM ports using external session: {}", session.hashCode());

        return listComPorts()
                       .onItem()
                       .invoke(allPorts -> log.debug("✅ Found {} total COM ports", allPorts.size()))
                       .chain(allPorts ->
                                      listRegisteredComPorts(session)
                                              .onItem()
                                              .invoke(registeredPorts -> log.debug("✅ Found {} registered COM ports", registeredPorts.size()))
                                              .chain(registeredPorts -> {
                                                  List<String> availablePorts = new ArrayList<>(allPorts);
                                                  availablePorts.removeAll(registeredPorts);
                                                  log.debug("📊 Available COM ports: {} (Total: {} - Registered: {})",
                                                          availablePorts.size(), allPorts.size(), registeredPorts.size());
                                                  log.debug("📤 Returning available COM ports: {}", availablePorts);
                                                  return Uni.createFrom()
                                                                 .item((List<String>) availablePorts);
                                              })
                       )
                       .onFailure()
                       .invoke(error -> log.error("❌ Failed to list available COM ports: {}", error.getMessage(), error));
    }

}

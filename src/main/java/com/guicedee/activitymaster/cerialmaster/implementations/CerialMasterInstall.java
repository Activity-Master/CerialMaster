package com.guicedee.activitymaster.cerialmaster.implementations;

import com.guicedee.activitymaster.fsdm.client.services.*;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.enterprise.IEnterprise;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.systems.ISystems;
import com.guicedee.activitymaster.fsdm.client.services.systems.ISystemUpdate;
import com.guicedee.activitymaster.fsdm.client.services.systems.SortedUpdate;
import io.smallrye.mutiny.Uni;
import lombok.extern.log4j.Log4j2;
import org.hibernate.reactive.mutiny.Mutiny;

import java.util.ArrayList;
import java.util.List;

import static com.guicedee.activitymaster.cerialmaster.services.enumerations.CerialMasterClassifications.*;
import static com.guicedee.activitymaster.cerialmaster.services.enumerations.CerialMasterEventTypes.*;
import static com.guicedee.activitymaster.cerialmaster.services.enumerations.CerialResourceItemTypes.*;
import static com.guicedee.activitymaster.fsdm.client.services.classifications.ResourceItemClassifications.*;
import static com.guicedee.activitymaster.fsdm.client.services.IActivityMasterService.*;
import static com.guicedee.activitymaster.cerialmaster.client.services.ICerialMasterService.*;
import static com.guicedee.client.IGuiceContext.*;

@SortedUpdate(sortOrder = 500, taskCount = 3)
@Log4j2
public class CerialMasterInstall implements ISystemUpdate
{
	@Override
	public Uni<Boolean> update(Mutiny.Session session, IEnterprise<?, ?> enterprise)
	{
		log.info("🚀 Starting CerialMaster installation with externally provided session");
		log.debug("📋 Session hash: {}, Enterprise: {}", session.hashCode(), 
			enterprise != null ? enterprise.getClass().getSimpleName() : "null");
		
		IClassificationService<?> classificationService = get(IClassificationService.class);
		IResourceItemService<?> resourceItemService = get(IResourceItemService.class);

		log.debug("🔍 Retrieving CerialMaster system configuration...");
		
		// Get system and token reactively using IActivityMasterService static methods
		return getISystem(CerialMasterSystemName, enterprise)
			.onItem().invoke(system -> log.debug("✅ Retrieved CerialMaster system: {}", system.getDescription()))
			.onFailure().invoke(error -> log.error("❌ Failed to retrieve CerialMaster system: {}", 
				error.getMessage(), error))
			.chain(system -> 
				getISystemToken(CerialMasterSystemName, enterprise)
					.onItem().invoke(token -> log.debug("✅ Retrieved system token: {}", token))
					.onFailure().invoke(error -> log.error("❌ Failed to retrieve system token: {}", 
						error.getMessage(), error))
					.chain(identityToken -> {
						log.info("📦 Creating SerialConnectionPort resource item type...");
						
						// Create SerialConnectionPort type
						return resourceItemService.createType(session, SerialConnectionPort, system)
							.onItem().invoke(type -> log.debug("✅ Created SerialConnectionPort type: {}", type.getDescription()))
							.onFailure().invoke(error -> log.error("❌ Failed to create SerialConnectionPort type: {}", 
								error.getMessage(), error))
							.chain(serialConnectionPort -> {
								logProgress("Cerial Master", "Loading Com Port Configurations");
								log.info("🏗️ Creating COM port classification hierarchy...");

								// Create classifications sequentially as they have dependencies
								return classificationService.create(session, ComPort, system, Hardware)
											   .onItem()
											   .invoke(cp -> log.debug("✅ Created ComPort classification"))
											   .chain(comPort -> classificationService.create(session, ServerNumber, system, ComPort)
																		 .onItem()
																		 .invoke(sn -> log.debug("✅ Created ServerNumber classification")))
											   .chain(serverNumber -> classificationService.create(session, ComPortNumber, system, ComPort)
																			  .onItem()
																			  .invoke(cpn -> log.debug("✅ Created ComPortNumber classification")))
											   .chain(comPortNumber -> classificationService.create(session, ComPortStatus, system, ComPort)
																			   .onItem()
																			   .invoke(cps -> log.debug("✅ Created ComPortStatus classification")))
											   .chain(comPortStatus -> classificationService.create(session, ComPortDeviceType, system, ComPort)
																			   .onItem()
																			   .invoke(cpdt -> log.debug("✅ Created ComPortDeviceType classification")))
											   .chain(comPortDeviceType -> classificationService.create(session, ComPortAllowedCharacters, system, ComPort)
																				   .onItem()
																				   .invoke(cpac -> log.debug("✅ Created ComPortAllowedCharacters classification")))
											   .chain(comPortAllowedCharacters -> classificationService.create(session, ComPortEndOfMessage, system, ComPort)
																						  .chain(comPortEndOfMessage -> {
																							  log.info("⚡ Creating port configuration classifications in parallel...");

																							  // Create a list of operations to run in parallel for port configuration classifications
																							  List<Uni<?>> portConfigOperations = new ArrayList<>();
																							  portConfigOperations.add(classificationService.create(session, BaudRate, system, ComPort)
																															   .onItem()
																															   .invoke(br -> log.debug("✅ Created BaudRate classification")));
																							  portConfigOperations.add(classificationService.create(session, BufferSize, system, ComPort)
																															   .onItem()
																															   .invoke(bs -> log.debug("✅ Created BufferSize classification")));
																							  portConfigOperations.add(classificationService.create(session, DataBits, system, ComPort)
																															   .onItem()
																															   .invoke(db -> log.debug("✅ Created DataBits classification")));
																							  portConfigOperations.add(classificationService.create(session, StopBits, system, ComPort)
																															   .onItem()
																															   .invoke(sb -> log.debug("✅ Created StopBits classification")));
																							  portConfigOperations.add(classificationService.create(session, Parity, system, ComPort)
																															   .onItem()
																															   .invoke(p -> log.debug("✅ Created Parity classification")));

																							  log.info("🔄 Running {} port configuration classification creation operations in parallel", portConfigOperations.size());

																							  // Run all port configuration operations in parallel
																							  return Uni.combine()
																											 .all()
																											 .unis(portConfigOperations)
																											 .discardItems()
																											 .onItem()
																											 .invoke(() -> log.info("✅ Completed all port configuration classifications"))
																											 .onFailure()
																											 .invoke(error -> log.error("❌ Error creating port configuration classifications: {}", error.getMessage(), error));
																						  })
																						  .chain(() -> {
																							  log.info("📨 Creating Message classification...");
																							  return classificationService.create(session, Message, system, ComPort)
																											 .onItem()
																											 .invoke(msg -> log.debug("✅ Created Message classification"));
																						  })
																						  .chain(message -> {
																							  log.info("⚡ Creating message classifications in parallel...");

																							  // Create a list of operations to run in parallel for message classifications
																							  List<Uni<?>> messageClassOperations = new ArrayList<>();
																							  messageClassOperations.add(classificationService.create(session, SendMessageToComPort, system, Message)
																																 .onItem()
																																 .invoke(smtcp -> log.debug("✅ Created SendMessageToComPort classification")));
																							  messageClassOperations.add(classificationService.create(session, MessageReceivedFromComPort, system, Message)
																																 .onItem()
																																 .invoke(mrfcp -> log.debug("✅ Created MessageReceivedFromComPort classification")));

																							  log.info("🔄 Running {} message classification creation operations in parallel", messageClassOperations.size());

																							  // Run all message classification operations in parallel
																							  return Uni.combine()
																											 .all()
																											 .unis(messageClassOperations)
																											 .discardItems()
																											 .onItem()
																											 .invoke(() -> log.info("✅ Completed all message classifications"))
																											 .onFailure()
																											 .invoke(error -> log.error("❌ Error creating message classifications: {}", error.getMessage(), error));
																						  })
																						  .chain(() -> {
																							  log.info("🎯 Creating event types...");
																							  IEventService<?> eventsService = get(IEventService.class);

																							  // Convert synchronous createEventType calls to reactive pattern
																							  log.debug("📝 Creating SendMessageToComPort event type...");
																							  return Uni.createFrom()
																											 .item(() -> {
																												 eventsService.createEventType(session, SendMessageToComPort, system);
																												 return "SendMessageToComPort";
																											 })
																											 .onItem()
																											 .invoke(eventType -> log.debug("✅ Created {} event type", eventType))
																											 .chain(() -> {
																												 log.debug("📝 Creating Message event type...");
																												 return Uni.createFrom()
																																.item(() -> {
																																	eventsService.createEventType(session, Message, system);
																																	return "Message";
																																})
																																.onItem()
																																.invoke(eventType -> log.debug("✅ Created {} event type", eventType));
																											 })
																											 .chain(() -> {
																												 log.debug("📝 Creating MessageReceivedFromComPort event type...");
																												 return Uni.createFrom()
																																.item(() -> {
																																	eventsService.createEventType(session, MessageReceivedFromComPort, system);
																																	return "MessageReceivedFromComPort";
																																})
																																.onItem()
																																.invoke(eventType -> log.debug("✅ Created {} event type", eventType));
																											 });
																						  })
																						  .chain(() -> {
																							  logProgress("Cerial Master", "Loading Com Port Events");
																							  log.info("🔗 Creating connection event types...");

																							  IEventService<?> eventsService = get(IEventService.class);

																							  // Convert synchronous createEventType calls to reactive pattern
																							  log.debug("📝 Creating RegisteredANewConnection event type...");
																							  return Uni.createFrom()
																											 .item(() -> {
																												 eventsService.createEventType(session, RegisteredANewConnection.toString(), system, identityToken);
																												 return "RegisteredANewConnection";
																											 })
																											 .onItem()
																											 .invoke(eventType -> log.debug("✅ Created {} event type", eventType))
																											 .chain(() -> {
																												 log.debug("📝 Creating ClosedANewConnection event type...");
																												 return Uni.createFrom()
																																.item(() -> {
																																	eventsService.createEventType(session, ClosedANewConnection.toString(), system, identityToken);
																																	return "ClosedANewConnection";
																																})
																																.onItem()
																																.invoke(eventType -> log.debug("✅ Created {} event type", eventType));
																											 });
																						  })
																						  .chain(() -> {
																							  log.info("📦 Creating resource item types in parallel...");

																							  // Create a list of operations to run in parallel for resource item types
																							  List<Uni<?>> resourceItemTypeOperations = new ArrayList<>();
																							  resourceItemTypeOperations.add(resourceItemService.createType(session, Message, system)
																																	 .onItem()
																																	 .invoke(msg -> log.debug("✅ Created Message resource item type")));
																							  resourceItemTypeOperations.add(resourceItemService.createType(session, SendMessageToComPort, system)
																																	 .onItem()
																																	 .invoke(smtcp -> log.debug("✅ Created SendMessageToComPort resource item type")));
																							  resourceItemTypeOperations.add(resourceItemService.createType(session, MessageReceivedFromComPort, system)
																																	 .onItem()
																																	 .invoke(mrfcp -> log.debug("✅ Created MessageReceivedFromComPort resource item type")));

																							  log.info("🔄 Running {} resource item type creation operations in parallel", resourceItemTypeOperations.size());

																							  // Run all resource item type operations in parallel
																							  return Uni.combine()
																											 .all()
																											 .unis(resourceItemTypeOperations)
																											 .discardItems()
																											 .onItem()
																											 .invoke(() -> log.info("✅ Completed all resource item types"))
																											 .onFailure()
																											 .invoke(error -> log.error("❌ Error creating resource item types: {}", error.getMessage(), error));
																						  })
																						  .invoke(() -> {
																							  logProgress("Cerial Master", "Completed Com Ports");
																							  log.info("🎉 CerialMaster installation completed successfully!");
																						  })
																						  .map(result -> true)); // Return Boolean
							});
					}));
	}
}

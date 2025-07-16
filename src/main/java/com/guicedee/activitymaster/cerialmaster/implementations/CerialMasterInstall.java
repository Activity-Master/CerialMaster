package com.guicedee.activitymaster.cerialmaster.implementations;

import com.guicedee.activitymaster.fsdm.client.services.*;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.enterprise.IEnterprise;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.systems.ISystems;
import com.guicedee.activitymaster.fsdm.client.services.systems.ISystemUpdate;
import com.guicedee.activitymaster.fsdm.client.services.systems.SortedUpdate;
import io.smallrye.mutiny.Uni;
import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.List;

import static com.guicedee.activitymaster.cerialmaster.services.enumerations.CerialMasterClassifications.*;
import static com.guicedee.activitymaster.cerialmaster.services.enumerations.CerialMasterEventTypes.*;
import static com.guicedee.activitymaster.cerialmaster.services.enumerations.CerialResourceItemTypes.*;
import static com.guicedee.activitymaster.fsdm.client.services.classifications.ResourceItemClassifications.*;
import static com.guicedee.client.IGuiceContext.*;

@SortedUpdate(sortOrder = 500, taskCount = 3)
@Log4j2
public class CerialMasterInstall implements ISystemUpdate
{
	@Override
	public Uni<Boolean> update(IEnterprise<?, ?> enterprise)
	{
		log.info("Starting update for Cerial Master");
		IClassificationService<?> classificationService = get(IClassificationService.class);
		CerialMasterSystem cms = com.guicedee.client.IGuiceContext.get(CerialMasterSystem.class);

		ISystems<?, ?> system = cms.getSystem(enterprise);
		java.util.UUID identityToken = cms.getSystemToken(enterprise);

		IResourceItemService<?> resourceItemService = get(IResourceItemService.class);

		// Create SerialConnectionPort type
		return resourceItemService.createType(SerialConnectionPort, system)
			.chain(serialConnectionPort -> {
				logProgress("Cerial Master", "Loading Com Port Configurations");

				// Create classifications sequentially as they have dependencies
				return classificationService.create(ComPort, system, Hardware)
					.chain(comPort -> classificationService.create(ServerNumber, system, ComPort))
					.chain(serverNumber -> classificationService.create(ComPortNumber, system, ComPort))
					.chain(comPortNumber -> classificationService.create(ComPortStatus, system, ComPort))
					.chain(comPortStatus -> classificationService.create(ComPortDeviceType, system, ComPort))
					.chain(comPortDeviceType -> classificationService.create(ComPortAllowedCharacters, system, ComPort))
					.chain(comPortAllowedCharacters -> classificationService.create(ComPortEndOfMessage, system, ComPort))
					.chain(comPortEndOfMessage -> {
						// Create a list of operations to run in parallel for port configuration classifications
						List<Uni<?>> portConfigOperations = new ArrayList<>();
						portConfigOperations.add(classificationService.create(BaudRate, system, ComPort));
						portConfigOperations.add(classificationService.create(BufferSize, system, ComPort));
						portConfigOperations.add(classificationService.create(DataBits, system, ComPort));
						portConfigOperations.add(classificationService.create(StopBits, system, ComPort));
						portConfigOperations.add(classificationService.create(Parity, system, ComPort));

						log.info("Running {} port configuration classification creation operations in parallel", portConfigOperations.size());

						// Run all port configuration operations in parallel
						return Uni.combine().all().unis(portConfigOperations)
							.discardItems()
							.onFailure().invoke(error -> log.error("Error creating port configuration classifications: {}", error.getMessage(), error));
					})
					.chain(() -> classificationService.create(Message, system, ComPort))
					.chain(message -> {
						// Create a list of operations to run in parallel for message classifications
						List<Uni<?>> messageClassOperations = new ArrayList<>();
						messageClassOperations.add(classificationService.create(SendMessageToComPort, system, Message));
						messageClassOperations.add(classificationService.create(MessageReceivedFromComPort, system, Message));

						log.info("Running {} message classification creation operations in parallel", messageClassOperations.size());

						// Run all message classification operations in parallel
						return Uni.combine().all().unis(messageClassOperations)
							.discardItems()
							.onFailure().invoke(error -> log.error("Error creating message classifications: {}", error.getMessage(), error));
					})
					.chain(() -> {
						IEventService<?> eventsService = com.guicedee.client.IGuiceContext.get(IEventService.class);

						// Execute createEventType operations sequentially since they return Future, not Uni
						log.info("Creating event types sequentially");
						return Uni.createFrom().item(() -> {
							// These operations are executed synchronously
							eventsService.createEventType(SendMessageToComPort, system);
							eventsService.createEventType(Message, system);
							eventsService.createEventType(MessageReceivedFromComPort, system);
							return null;
						});
					})
					.chain(() -> {
						logProgress("Cerial Master", "Loading Com Port Events");

						// Execute createEventType operations sequentially since they return Future, not Uni
						IEventService<?> eventsService = com.guicedee.client.IGuiceContext.get(IEventService.class);
						log.info("Creating connection event types sequentially");
						return Uni.createFrom().item(() -> {
							// These operations are executed synchronously
							eventsService.createEventType(RegisteredANewConnection.toString(), system, identityToken);
							eventsService.createEventType(ClosedANewConnection.toString(), system, identityToken);
							return null;
						});
					})
					.chain(() -> {
						// Create a list of operations to run in parallel for resource item types
						List<Uni<?>> resourceItemTypeOperations = new ArrayList<>();
						resourceItemTypeOperations.add(resourceItemService.createType(Message, system));
						resourceItemTypeOperations.add(resourceItemService.createType(SendMessageToComPort, system));
						resourceItemTypeOperations.add(resourceItemService.createType(MessageReceivedFromComPort, system));

						log.info("Running {} resource item type creation operations in parallel", resourceItemTypeOperations.size());

						// Run all resource item type operations in parallel
						return Uni.combine().all().unis(resourceItemTypeOperations)
							.discardItems()
							.onFailure().invoke(error -> log.error("Error creating resource item types: {}", error.getMessage(), error));
					})
					.invoke(() -> logProgress("Cerial Master", "Completed Com Ports"))
					.map(result -> true); // Return Boolean
			});
	}
}

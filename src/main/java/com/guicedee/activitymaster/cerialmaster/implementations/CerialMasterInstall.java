package com.guicedee.activitymaster.cerialmaster.implementations;

import com.guicedee.activitymaster.cerialmaster.CerialMasterSecurityCollector;
import com.guicedee.activitymaster.fsdm.client.services.*;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.enterprise.IEnterprise;
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
	/** Stateless twin of {@link #update(Mutiny.StatelessSession, IEnterprise)}. */
	@Override
	public Uni<Boolean> update(Mutiny.StatelessSession session, IEnterprise<?, ?> enterprise)
	{
		log.info("🚀 Starting CerialMaster installation (stateless)");

		IClassificationService<?> classificationService = get(IClassificationService.class);
		IResourceItemService<?> resourceItemService = get(IResourceItemService.class);
		IEventService<?> eventsService = get(IEventService.class);

		return getISystem(session, CerialMasterSystemName, enterprise)
			.chain(system -> getISystemToken(session, CerialMasterSystemName, enterprise)
				.chain(identityToken -> {
					get(CerialMasterSecurityCollector.class).activate(session);

					return resourceItemService.createType(session, SerialConnectionPort, system)
						.chain(() -> {
							logProgress("Cerial Master", "Loading Com Port Configurations");
							return classificationService.create(session, ComPort, system, Hardware)
								.chain(() -> classificationService.create(session, ServerNumber, system, ComPort))
								.chain(() -> classificationService.create(session, ComPortNumber, system, ComPort))
								.chain(() -> classificationService.create(session, ComPortStatus, system, ComPort))
								.chain(() -> classificationService.create(session, ComPortDeviceType, system, ComPort))
								.chain(() -> classificationService.create(session, ComPortAllowedCharacters, system, ComPort))
								.chain(() -> classificationService.create(session, ComPortEndOfMessage, system, ComPort))
								.chain(() -> classificationService.create(session, BaudRate, system, ComPort))
								.chain(() -> classificationService.create(session, BufferSize, system, ComPort))
								.chain(() -> classificationService.create(session, DataBits, system, ComPort))
								.chain(() -> classificationService.create(session, StopBits, system, ComPort))
								.chain(() -> classificationService.create(session, Parity, system, ComPort))
								.chain(() -> classificationService.create(session, Message, system, ComPort))
								.chain(() -> classificationService.create(session, SendMessageToComPort, system, Message))
								.chain(() -> classificationService.create(session, MessageReceivedFromComPort, system, Message));
						})
						.chain(() -> {
							logProgress("Cerial Master", "Loading Com Port Events");
							return eventsService.createEventType(session, SendMessageToComPort, system)
								.chain(() -> eventsService.createEventType(session, Message, system))
								.chain(() -> eventsService.createEventType(session, MessageReceivedFromComPort, system))
								.chain(() -> eventsService.createEventType(session, RegisteredANewConnection.toString(), system, identityToken))
								.chain(() -> eventsService.createEventType(session, ClosedANewConnection.toString(), system, identityToken));
						})
						.chain(() -> resourceItemService.createType(session, Message, system)
							.chain(() -> resourceItemService.createType(session, SendMessageToComPort, system))
							.chain(() -> resourceItemService.createType(session, MessageReceivedFromComPort, system)))
						.invoke(() -> {
							logProgress("Cerial Master", "Completed Com Ports");
							log.info("🎉 CerialMaster installation completed successfully (stateless)!");
						})
						.chain(() -> get(CerialMasterSecurityCollector.class).flush(session, system, identityToken))
						.map(result -> true);
				}))
			.onFailure().invoke(error -> log.error("❌ CerialMaster stateless installation failed: {}", error.getMessage(), error));
	}
}

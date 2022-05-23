package com.guicedee.activitymaster.cerialmaster.implementations;

import com.guicedee.activitymaster.fsdm.client.services.*;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.enterprise.IEnterprise;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.systems.ISystems;
import com.guicedee.activitymaster.fsdm.client.services.systems.ISystemUpdate;
import com.guicedee.activitymaster.fsdm.client.services.systems.SortedUpdate;
import com.guicedee.guicedinjection.GuiceContext;

import static com.guicedee.activitymaster.cerialmaster.services.enumerations.CerialMasterClassifications.*;
import static com.guicedee.activitymaster.cerialmaster.services.enumerations.CerialMasterEventTypes.*;
import static com.guicedee.activitymaster.cerialmaster.services.enumerations.CerialResourceItemTypes.*;
import static com.guicedee.activitymaster.fsdm.client.services.classifications.ResourceItemClassifications.*;
import static com.guicedee.guicedinjection.GuiceContext.*;

@SortedUpdate(sortOrder = 500, taskCount = 3)
public class CerialMasterInstall implements ISystemUpdate
{
	@Override
	public void update(IEnterprise<?, ?> enterprise)
	{
		IClassificationService<?> classificationService = get(IClassificationService.class);
		CerialMasterSystem cms = GuiceContext.get(CerialMasterSystem.class);
		
		ISystems<?, ?> system = cms.getSystem(enterprise);
		java.util.UUID identityToken = cms.getSystemToken(enterprise);
		
		IResourceItemService<?> resourceItemService = get(IResourceItemService.class);
		resourceItemService.createType(SerialConnectionPort, system);
		logProgress("Cerial Master", "Loading Com Port Configurations");
		
		classificationService.create(ComPort, system, Hardware);
		classificationService.create(ServerNumber, system, ComPort);
		
		classificationService.create(ComPortNumber, system, ComPort);
		classificationService.create(ComPortStatus, system, ComPort);
		classificationService.create(ComPortDeviceType, system, ComPort);
		classificationService.create(ComPortAllowedCharacters, system, ComPort);
		classificationService.create(ComPortEndOfMessage, system, ComPort);
		
		classificationService.create(BaudRate, system, ComPort);
		classificationService.create(BufferSize, system, ComPort);
		classificationService.create(DataBits, system, ComPort);
		classificationService.create(StopBits, system, ComPort);
		classificationService.create(Parity, system, ComPort);
		
		
		classificationService.create(Message, system, ComPort);
		classificationService.create(SendMessageToComPort, system, Message);
		classificationService.create(MessageReceivedFromComPort, system, Message);
		
		IEventService<?> eventsService = GuiceContext.get(IEventService.class);
		eventsService.createEventType(SendMessageToComPort, system);
		eventsService.createEventType(Message, system);
		eventsService.createEventType(MessageReceivedFromComPort, system);
		
		logProgress("Cerial Master", "Loading Com Port Events");
		eventsService.createEventType(RegisteredANewConnection.toString(), system, identityToken);
		eventsService.createEventType(ClosedANewConnection.toString(), system, identityToken);
		
		resourceItemService.createType(Message, system);
		resourceItemService.createType(SendMessageToComPort, system);
		resourceItemService.createType(MessageReceivedFromComPort, system);
		
		logProgress("Cerial Master", "Completed Com Ports");
	}
}

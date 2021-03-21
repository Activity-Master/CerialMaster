package com.guicedee.activitymaster.cerialmaster.implementations;

import com.guicedee.activitymaster.client.services.*;
import com.guicedee.activitymaster.client.services.builders.warehouse.enterprise.IEnterprise;
import com.guicedee.activitymaster.client.services.builders.warehouse.systems.ISystems;
import com.guicedee.activitymaster.client.services.systems.*;
import com.guicedee.guicedinjection.GuiceContext;

import java.util.UUID;

import static com.guicedee.activitymaster.cerialmaster.services.enumerations.CerialMasterClassifications.*;
import static com.guicedee.activitymaster.cerialmaster.services.enumerations.CerialMasterEventTypes.*;
import static com.guicedee.activitymaster.cerialmaster.services.enumerations.CerialResourceItemTypes.*;
import static com.guicedee.activitymaster.client.services.classifications.ResourceItemClassifications.*;
import static com.guicedee.guicedinjection.GuiceContext.*;

@DatedUpdate(date = "2020/01/02", taskCount = 3)
public class CerialMasterInstall implements ISystemUpdate
{
	@Override
	public void update(IEnterprise<?,?> enterprise, IActivityMasterProgressMonitor progressMonitor)
	{
		IClassificationService<?> classificationService = get(IClassificationService.class);
		CerialMasterSystem cms = GuiceContext.get(CerialMasterSystem.class);
		
		ISystems<?,?> system = cms.getSystem(enterprise);
		UUID token = cms.getSystemToken(enterprise);
		
		IResourceItemService<?> resourceItemService = get(IResourceItemService.class);
		resourceItemService.createType(SerialConnectionPort,system);
		logProgress("Cerial Master", "Loading Com Port Configurations", progressMonitor);
		
		classificationService.create(ComPort, system,Hardware);
		classificationService.create(ServerNumber, system,ComPort);
		
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
		
		logProgress("Cerial Master", "Loading Com Port Events", progressMonitor);
		IEventService<?> eventsService = GuiceContext.get(IEventService.class);
		eventsService.createEventType(RegisteredANewConnection.toString(), system, token);
		eventsService.createEventType(ClosedANewConnection.toString(), system, token);
		
		logProgress("Cerial Master", "Completed Com Ports", progressMonitor);
	}
}

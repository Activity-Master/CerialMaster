package com.guicedee.activitymaster.cerialmaster.implementations;

import com.guicedee.activitymaster.core.services.IActivityMasterProgressMonitor;
import com.guicedee.activitymaster.core.services.dto.IEnterprise;
import com.guicedee.activitymaster.core.services.dto.ISystems;
import com.guicedee.activitymaster.core.services.system.IClassificationService;
import com.guicedee.activitymaster.core.services.system.IEventService;
import com.guicedee.activitymaster.core.services.system.IResourceItemService;
import com.guicedee.activitymaster.core.updates.DatedUpdate;
import com.guicedee.activitymaster.core.updates.ISystemUpdate;
import com.guicedee.guicedinjection.GuiceContext;

import java.util.UUID;

import static com.guicedee.activitymaster.cerialmaster.services.enumerations.CerialMasterClassifications.*;
import static com.guicedee.activitymaster.cerialmaster.services.enumerations.CerialMasterClassifications.ComPort;
import static com.guicedee.activitymaster.cerialmaster.services.enumerations.CerialMasterEventTypes.ClosedANewConnection;
import static com.guicedee.activitymaster.cerialmaster.services.enumerations.CerialMasterEventTypes.RegisteredANewConnection;
import static com.guicedee.activitymaster.cerialmaster.services.enumerations.CerialResourceItemTypes.SerialConnectionPort;
import static com.guicedee.activitymaster.core.services.classifications.resourceitems.ResourceItemClassifications.Hardware;
import static com.guicedee.guicedinjection.GuiceContext.get;

@DatedUpdate(date = "2020/01/02",taskCount = 3)
public class CerialMasterInstall implements ISystemUpdate
{
	@Override
	public void update(IEnterprise<?> enterprise, IActivityMasterProgressMonitor progressMonitor)
	{
		IClassificationService<?> classificationService = get(IClassificationService.class);
		CerialMasterSystem cms = GuiceContext.get(CerialMasterSystem.class);
		
		ISystems<?> system = cms.getSystem(enterprise);
		UUID token = cms.getSystemToken(enterprise);
		
		IResourceItemService<?> resourceItemService = get(IResourceItemService.class);
		resourceItemService.createType(SerialConnectionPort,system);
		logProgress("Cerial Master", "Loading Com Port Configurations", progressMonitor);
		
		classificationService.create(ComPort, system,Hardware);
		
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
		eventsService.createEventType(RegisteredANewConnection, system, token);
		eventsService.createEventType(ClosedANewConnection, system, token);
		
		logProgress("Cerial Master", "Completed Com Ports", progressMonitor);
	}
}

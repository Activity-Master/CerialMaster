package com.guicedee.activitymaster.cerialmaster.implementations;

import com.guicedee.activitymaster.core.services.IActivityMasterProgressMonitor;
import com.guicedee.activitymaster.core.services.IActivityMasterSystem;
import com.guicedee.activitymaster.core.services.dto.IEnterprise;
import com.guicedee.activitymaster.core.services.dto.ISystems;
import com.guicedee.activitymaster.core.services.system.*;
import com.guicedee.guicedinjection.GuiceContext;

import java.util.NoSuchElementException;
import java.util.UUID;

import static com.guicedee.activitymaster.cerialmaster.services.enumerations.CerialResourceItemTypes.SerialConnectionPort;
import static com.guicedee.activitymaster.core.services.classifications.resourceitems.ResourceItemClassifications.*;
import static com.guicedee.guicedinjection.GuiceContext.*;
import static com.guicedee.activitymaster.cerialmaster.services.enumerations.CerialMasterClassifications.*;
import static com.guicedee.activitymaster.cerialmaster.services.enumerations.CerialMasterEventTypes.*;

public class CerialMasterSystem
		extends ActivityMasterDefaultSystem<CerialMasterSystem>
		implements IActivityMasterSystem<CerialMasterSystem>
{

	@Override
	public void createDefaults(IEnterprise<?> enterprise, IActivityMasterProgressMonitor progressMonitor)
	{

	}

	@Override
	public int totalTasks()
	{
		return 3;
	}

	@Override
	public void loadUpdates(IEnterprise<?> enterprise, IActivityMasterProgressMonitor progressMonitor)
	{
		IClassificationService<?> classificationService = get(IClassificationService.class);
		IClassificationDataConceptService<?> dataConceptService = get(IClassificationDataConceptService.class);
		ISystems<?> system = getSystem(enterprise);
		UUID token = getSystemToken(enterprise);

		try
		{
			classificationService.find(ComPort, enterprise, token);

		}
		catch (NoSuchElementException nse)
		{
			_2020012(enterprise, progressMonitor);
		}
	}

	@Override
	public String getSystemName()
	{
		return "Grader System";
	}

	@Override
	public String getSystemDescription()
	{
		return "This system handles all items for the Graders";
	}

	private void _2020012(IEnterprise<?> enterprise, IActivityMasterProgressMonitor progressMonitor)
	{
		IClassificationService<?> classificationService = get(IClassificationService.class);
		IClassificationDataConceptService<?> dataConceptService = get(IClassificationDataConceptService.class);
		ISystems<?> system = getSystem(enterprise);
		UUID token = getSystemToken(enterprise);
		
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
		eventsService.createEventType(RegisteredANewConnection, system, getSystemToken(enterprise));
		eventsService.createEventType(ClosedANewConnection, system, getSystemToken(enterprise));

		logProgress("Cerial Master", "Completed Com Ports", progressMonitor);

	}

	@Override
	public Integer sortOrder()
	{
		return 550;
	}
}

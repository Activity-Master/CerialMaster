package com.guicedee.activitymaster.cerialmaster.implementations;

import com.google.inject.Singleton;
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

@Singleton
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
	}

	@Override
	public String getSystemName()
	{
		return "Cerial Master System";
	}

	@Override
	public String getSystemDescription()
	{
		return "This system handles communication with serial ports";
	}
	

	@Override
	public Integer sortOrder()
	{
		return 550;
	}
}

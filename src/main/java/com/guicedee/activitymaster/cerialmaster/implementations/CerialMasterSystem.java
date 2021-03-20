package com.guicedee.activitymaster.cerialmaster.implementations;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.guicedee.activitymaster.client.services.ISystemsService;
import com.guicedee.activitymaster.client.services.administration.IActivityMasterProgressMonitor;
import com.guicedee.activitymaster.client.services.builders.warehouse.enterprise.IEnterprise;
import com.guicedee.activitymaster.core.services.IActivityMasterSystem;
import com.guicedee.activitymaster.core.services.system.ActivityMasterDefaultSystem;

import static com.guicedee.activitymaster.cerialmaster.services.ICerialMasterService.*;

public class CerialMasterSystem
		extends ActivityMasterDefaultSystem<CerialMasterSystem>
		implements IActivityMasterSystem<CerialMasterSystem>
{
	
	@Inject
	private Provider<ISystemsService<?>> systemsService;
	
	@Override
	public void registerSystem(IEnterprise<?,?> enterprise, IActivityMasterProgressMonitor progressMonitor)
	{
		systemsService.get()
		              .create(enterprise, getSystemName(), getSystemDescription());
		systemsService.get()
		              .registerNewSystem(enterprise, getSystem(enterprise));
	}
	
	@Override
	public void createDefaults(IEnterprise<?,?> enterprise, IActivityMasterProgressMonitor progressMonitor)
	{

	}

	@Override
	public int totalTasks()
	{
		return 0;
	}
	
	@Override
	public String getSystemName()
	{
		return CerialMasterSystemName;
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

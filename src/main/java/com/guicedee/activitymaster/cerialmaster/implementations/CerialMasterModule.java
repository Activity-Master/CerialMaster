package com.guicedee.activitymaster.cerialmaster.implementations;

import com.google.inject.PrivateModule;
import com.guicedee.guicedinjection.interfaces.IGuiceModule;
import com.guicedee.activitymaster.cerialmaster.CerialMasterService;
import com.guicedee.activitymaster.cerialmaster.services.ICerialMasterService;

public class CerialMasterModule
		extends PrivateModule
		implements IGuiceModule<CerialMasterModule>
{
	@Override
	protected void configure()
	{
		bind(ICerialMasterService.class).to(CerialMasterService.class);
		expose(ICerialMasterService.class);
	}
}

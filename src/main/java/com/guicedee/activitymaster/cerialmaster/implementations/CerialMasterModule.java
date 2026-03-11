package com.guicedee.activitymaster.cerialmaster.implementations;

import com.fazecast.jSerialComm.SerialPort;
import com.google.inject.*;
import com.guicedee.activitymaster.cerialmaster.CerialMasterService;
import com.guicedee.activitymaster.cerialmaster.client.services.ICerialMasterService;
import com.guicedee.client.services.lifecycle.IGuiceModule;

public class CerialMasterModule
		extends PrivateModule
		implements IGuiceModule<CerialMasterModule>
{
	@Override
	protected void configure()
	{
		@SuppressWarnings("Convert2Diamond")
		Key<ICerialMasterService<?>> genericKey = Key.get(new TypeLiteral<ICerialMasterService<?>>() {});
		@SuppressWarnings("Convert2Diamond")
		Key<ICerialMasterService<CerialMasterService>> realKey
				= Key.get(new TypeLiteral<ICerialMasterService<CerialMasterService>>() {});

		bind(genericKey).to(realKey);
		bind(realKey).to(CerialMasterService.class);
		bind(ICerialMasterService.class).to(genericKey);

		expose(genericKey);
		expose(ICerialMasterService.class);
	}
}

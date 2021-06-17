package com.guicedee.activitymaster.cerialmaster.implementations;

import com.google.inject.Inject;
import com.guicedee.activitymaster.cerialmaster.services.ICerialMasterService;
import com.guicedee.guicedinjection.interfaces.IGuicePostStartup;
import lombok.extern.java.Log;

@Log
public class CerialMasterPostStartup implements IGuicePostStartup<CerialMasterPostStartup>
{
	@Inject
	private ICerialMasterService<?> cerialMasterService;
	
	@Override
	public void postLoad()
	{
		log.info("Loading available Serial Ports");
		cerialMasterService.listComPorts();
	}
}

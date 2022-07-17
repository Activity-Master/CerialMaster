package com.guicedee.activitymaster.cerialmaster.implementations;

import com.guicedee.guicedinjection.interfaces.IGuicePostStartup;
import com.guicedee.logger.LogFactory;

import java.util.logging.Logger;

public class CerialMasterPostStartup implements IGuicePostStartup<CerialMasterPostStartup>
{
	private static final Logger log = LogFactory.getLog(CerialMasterPostStartup.class);

	@Override
	public void postLoad()
	{
		//log.info("Loading available Serial Ports");
		//cerialMasterService.listComPorts();
	}
}

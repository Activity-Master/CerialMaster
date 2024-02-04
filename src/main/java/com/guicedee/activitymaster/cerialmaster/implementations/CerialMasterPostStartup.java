package com.guicedee.activitymaster.cerialmaster.implementations;

import com.guicedee.guicedinjection.interfaces.IGuicePostStartup;
import lombok.extern.java.Log;

@Log
public class CerialMasterPostStartup implements IGuicePostStartup<CerialMasterPostStartup>
{
	@Override
	public void postLoad()
	{
		
		//log.info("Loading available Serial Ports");
		//cerialMasterService.listComPorts();
		
	}
}

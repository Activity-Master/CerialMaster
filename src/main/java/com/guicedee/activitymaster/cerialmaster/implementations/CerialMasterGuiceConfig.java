package com.guicedee.activitymaster.cerialmaster.implementations;

import com.guicedee.guicedinjection.interfaces.IGuiceConfig;
import com.guicedee.guicedinjection.interfaces.IGuiceConfigurator;

public class CerialMasterGuiceConfig
		implements IGuiceConfigurator
{
	@Override
	public IGuiceConfig<?> configure(IGuiceConfig<?> config)
	{
		config.setClasspathScanning(true)
		      .setAnnotationScanning(true)
		      .setFieldInfo(true)
		      .setMethodInfo(true)
		   //   .setAllowPaths(true)
		      .setIgnoreFieldVisibility(true)
		      .setIgnoreMethodVisibility(true)
		      .setIgnoreClassVisibility(true)
		    //  .setIncludeModuleAndJars(true)
		//.setVerbose(true)
		;
		return config;
	}
}

package com.guicedee.activitymaster.cerialmaster.implementations;

import com.guicedee.guicedinjection.interfaces.IGuiceScanModuleInclusions;
import jakarta.validation.constraints.NotNull;

import java.util.HashSet;
import java.util.Set;

public class CerialMasterInclusionModule
		implements IGuiceScanModuleInclusions<CerialMasterInclusionModule>
{
	@Override
	public @NotNull Set<String> includeModules()
	{
		Set<String> set = new HashSet<>();
		set.add("com.guicedee.activitymaster.cerialmaster");
		set.add("com.guicedee.activitymaster.cerialmaster.client");
		return set;
	}
}

package com.guicedee.activitymaster.cerialmaster.services.enumerations;

import com.guicedee.activitymaster.core.services.enumtypes.IEventTypeValue;

public enum CerialMasterEventTypes
		implements IEventTypeValue<CerialMasterEventTypes>
{
	RegisteredANewConnection("added a new com port configuration"),
	ClosedANewConnection("removed a com port configuration"),
	;

	private String description;

	CerialMasterEventTypes(String description)
	{
		this.description = description;
	}

	@Override
	public String classificationValue()
	{
		return description;
	}
}

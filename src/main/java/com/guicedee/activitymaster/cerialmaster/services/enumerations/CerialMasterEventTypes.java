package com.guicedee.activitymaster.cerialmaster.services.enumerations;

public enum CerialMasterEventTypes
{
	RegisteredANewConnection("added a new com port configuration"),
	ClosedANewConnection("removed a com port configuration"),
	;

	private String description;

	CerialMasterEventTypes(String description)
	{
		this.description = description;
	}
	
	public String classificationValue()
	{
		return description;
	}
}

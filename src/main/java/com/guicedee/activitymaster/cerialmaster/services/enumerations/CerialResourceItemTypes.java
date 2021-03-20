package com.guicedee.activitymaster.cerialmaster.services.enumerations;

import static com.guicedee.activitymaster.client.services.classifications.EnterpriseClassificationDataConcepts.*;

public enum CerialResourceItemTypes
{
	SerialConnectionPort("Designates a piece of hardware used as a connection port",ResourceItemType)
	;
	private String classificationValue;
	private com.guicedee.activitymaster.client.services.classifications.EnterpriseClassificationDataConcepts dataConceptValue;
	
	CerialResourceItemTypes(String classificationValue, com.guicedee.activitymaster.client.services.classifications.EnterpriseClassificationDataConcepts dataConceptValue)
	{
		this.classificationValue = classificationValue;
		this.dataConceptValue = dataConceptValue;
	}
	
	CerialResourceItemTypes(String classificationValue)
	{
		this.classificationValue = classificationValue;
	}
	
	public String classificationName()
	{
		return name();
	}

	public String classificationValue()
	{
		return this.classificationValue;
	}
	
	public String classificationDescription()
	{
		return this.classificationValue;
	}

	public com.guicedee.activitymaster.client.services.classifications.EnterpriseClassificationDataConcepts concept()
	{
		return dataConceptValue;
	}
}

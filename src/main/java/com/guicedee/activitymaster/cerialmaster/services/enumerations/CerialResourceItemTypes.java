package com.guicedee.activitymaster.cerialmaster.services.enumerations;

import com.guicedee.activitymaster.fsdm.client.types.classifications.EnterpriseClassificationDataConcepts;

import static com.guicedee.activitymaster.fsdm.client.types.classifications.EnterpriseClassificationDataConcepts.*;

public enum CerialResourceItemTypes
{
	SerialConnectionPort("Designates a piece of hardware used as a connection port",ResourceItemType)
	;
	private String classificationValue;
	private EnterpriseClassificationDataConcepts dataConceptValue;
	
	CerialResourceItemTypes(String classificationValue, EnterpriseClassificationDataConcepts dataConceptValue)
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

	public EnterpriseClassificationDataConcepts concept()
	{
		return dataConceptValue;
	}
}

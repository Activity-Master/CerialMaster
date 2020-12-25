package com.guicedee.activitymaster.cerialmaster.services.enumerations;

import com.guicedee.activitymaster.core.services.classifications.resourceitems.ResourceItemTypes;
import com.guicedee.activitymaster.core.services.enumtypes.IClassificationDataConceptValue;
import com.guicedee.activitymaster.core.services.enumtypes.IResourceType;

import static com.guicedee.activitymaster.core.services.concepts.EnterpriseClassificationDataConcepts.ResourceItem;
import static com.guicedee.activitymaster.core.services.concepts.EnterpriseClassificationDataConcepts.ResourceItemType;

public enum CerialResourceItemTypes implements IResourceType<ResourceItemTypes>
{
	SerialConnectionPort("Designates a piece of hardware used as a connection port",ResourceItemType)
	;
	private String classificationValue;
	private IClassificationDataConceptValue<?> dataConceptValue;
	
	CerialResourceItemTypes(String classificationValue, IClassificationDataConceptValue<?> dataConceptValue)
	{
		this.classificationValue = classificationValue;
		this.dataConceptValue = dataConceptValue;
	}
	
	CerialResourceItemTypes(String classificationValue)
	{
		this.classificationValue = classificationValue;
	}
	
	@Override
	public String classificationName()
	{
		return name();
	}
	
	@Override
	public String classificationValue()
	{
		return this.classificationValue;
	}
	
	@Override
	public String classificationDescription()
	{
		return this.classificationValue;
	}
	
	@Override
	public IClassificationDataConceptValue<?> concept()
	{
		return dataConceptValue;
	}
}

package com.guicedee.activitymaster.cerialmaster.services.enumerations;

import static com.guicedee.activitymaster.client.services.classifications.EnterpriseClassificationDataConcepts.*;

public enum CerialMasterClassifications
{
	ComPort("A COM Port", ResourceItem),
	ServerNumber("A designated server number, used as the network id on the server", ResourceItem),
	
	ComPortDeviceType("The type of device for the com port", ResourceItemXClassification),
	ComPortStatus("The last registered status of the com port", ResourceItemXClassification),
	ComPortNumber("The assigned com port number for the com port", ResourceItemXClassification),
	BaudRate("The transfer rate for the connection", ResourceItemXClassification),
	BufferSize("The buffer size for connections", ResourceItemXClassification),
	DataBits("The number of bits transferred for a byte", ResourceItemXClassification),
	StopBits("The number of bits for a stop", ResourceItemXClassification),
	Parity("The required parity", ResourceItemXClassification),
	
	ComPortAllowedCharacters("A set of characters allowed for receipting", ResourceItemXClassification),
	ComPortEndOfMessage("A set of characters allowed to register the end of a message", ResourceItemXClassification),
	
	;
	
	private String description;
	private com.guicedee.activitymaster.client.services.classifications.EnterpriseClassificationDataConcepts concept;
	
	CerialMasterClassifications(String description, com.guicedee.activitymaster.client.services.classifications.EnterpriseClassificationDataConcepts concept)
	{
		this.description = description;
		this.concept = concept;
	}
	
	CerialMasterClassifications(String description)
	{
		this.description = description;
	}

	public String classificationDescription()
	{
		return this.description;
	}

	public com.guicedee.activitymaster.client.services.classifications.EnterpriseClassificationDataConcepts concept()
	{
		return concept;
	}
}

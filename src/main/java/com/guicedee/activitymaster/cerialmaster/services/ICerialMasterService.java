package com.guicedee.activitymaster.cerialmaster.services;

import com.guicedee.activitymaster.cerialmaster.services.dto.ComPortConnection;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.resourceitem.IResourceItemType;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.systems.ISystems;

import java.util.List;
import java.util.UUID;

public interface ICerialMasterService<J extends ICerialMasterService<J>>
{
	String CerialMasterSystemName = "Cerial Master System";
	
	IResourceItemType<?,?> getSerialConnectionType(ISystems<?,?> system, UUID...identityToken);
	
	ComPortConnection<?> addOrUpdateConnection(ComPortConnection<?> comPort, ISystems<?,?> system, UUID...identityToken);
	
	ComPortConnection<?> updateStatus(ComPortConnection<?> comPort, ISystems<?,?> system, UUID...identityToken);
	
	ComPortConnection<?> findComPortConnection(ComPortConnection<?> comPort, ISystems<?,?> system, UUID...identityToken);
	
	ComPortConnection<?> registerNewConnection(ComPortConnection<?> comPortConnection);

	List<String> listComPorts();
	
	List<String> listRegisteredComPorts();
	
	List<String> listAvailableComPorts();
}

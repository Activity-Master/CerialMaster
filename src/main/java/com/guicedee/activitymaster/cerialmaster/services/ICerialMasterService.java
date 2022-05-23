package com.guicedee.activitymaster.cerialmaster.services;

import com.guicedee.activitymaster.cerialmaster.services.dto.ComPortConnection;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.resourceitem.IResourceItemType;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.systems.ISystems;

import java.util.List;


public interface ICerialMasterService<J extends ICerialMasterService<J>>
{
	String CerialMasterSystemName = "Cerial Master System";
	
	IResourceItemType<?,?> getSerialConnectionType(ISystems<?,?> system, java.util.UUID... identityToken);
	
	ComPortConnection<?> addOrUpdateConnection(ComPortConnection<?> comPort, ISystems<?,?> system, java.util.UUID... identityToken);
	
	ComPortConnection<?> updateStatus(ComPortConnection<?> comPort, ISystems<?,?> system, java.util.UUID... identityToken);
	
	ComPortConnection<?> findComPortConnection(ComPortConnection<?> comPort, ISystems<?,?> system, java.util.UUID... identityToken);
	
	ComPortConnection<?> registerNewConnection(ComPortConnection<?> comPortConnection);
	
	ComPortConnection<?> getComPortConnection(Integer comPort);
	
	ComPortConnection<?> getScannerPortConnection(Integer comPort);
	
	List<String> listComPorts();
	
	List<String> listRegisteredComPorts();
	
	List<String> listAvailableComPorts();
}

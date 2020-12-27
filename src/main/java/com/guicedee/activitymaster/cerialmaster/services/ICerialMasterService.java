package com.guicedee.activitymaster.cerialmaster.services;

import com.guicedee.activitymaster.cerialmaster.services.dto.ComPortConnection;
import com.guicedee.activitymaster.core.services.dto.IEnterprise;
import com.guicedee.activitymaster.core.services.dto.IResourceItemType;
import com.guicedee.activitymaster.core.services.dto.ISystems;
import jakarta.cache.annotation.CacheResult;

import java.util.List;
import java.util.UUID;

public interface ICerialMasterService<J extends ICerialMasterService<J>>
{
	IResourceItemType<?> getSerialConnectionType(ISystems<?> system, UUID...identityToken);
	
	ComPortConnection<?> addOrUpdateConnection(ComPortConnection<?> comPort, ISystems<?> system, UUID...identityToken);
	
	ComPortConnection<?> updateStatus(ComPortConnection<?> comPort, ISystems<?> system, UUID...identityToken);
	
	ComPortConnection<?> findComPortConnection(ComPortConnection<?> comPort, ISystems<?> system, UUID...identityToken);
	
	ComPortConnection<?> registerNewConnection(ComPortConnection<?> comPortConnection);

	List<String> listComPorts();
}

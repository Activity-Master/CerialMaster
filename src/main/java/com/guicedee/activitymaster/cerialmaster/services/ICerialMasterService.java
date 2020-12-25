package com.guicedee.activitymaster.cerialmaster.services;

import com.guicedee.activitymaster.cerialmaster.services.dto.ComPortConnection;
import com.guicedee.activitymaster.core.services.dto.IEnterprise;
import com.guicedee.activitymaster.core.services.dto.IResourceItemType;
import jakarta.cache.annotation.CacheResult;

import java.util.List;
import java.util.UUID;

public interface ICerialMasterService<J extends ICerialMasterService<J>>
{
	IResourceItemType<?> getSerialConnectionType(IEnterprise<?> enterprise, UUID... tokens);
	
	ComPortConnection<?> addOrUpdateConnection(ComPortConnection<?> comPort, IEnterprise<?> enterprise, UUID... identitiyTokens);
	
	ComPortConnection<?> updateStatus(ComPortConnection<?> comPort, IEnterprise<?> enterprise, UUID... identitiyTokens);
	
	ComPortConnection<?> findComPortConnection(ComPortConnection<?> comPort, IEnterprise<?> enterprise, UUID... identitiyTokens);
	
	ComPortConnection<?> registerNewConnection(ComPortConnection<?> comPortConnection);

	List<String> listComPorts();
}

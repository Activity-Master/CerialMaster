package com.guicedee.activitymaster.cerialmaster;

import com.google.common.base.Strings;

import com.guicedee.activitymaster.cerialmaster.implementations.CerialMasterSystem;
import com.guicedee.activitymaster.cerialmaster.services.dto.ComPortType;
import com.guicedee.activitymaster.core.services.dto.IEnterprise;
import com.guicedee.activitymaster.core.services.dto.IResourceItem;
import com.guicedee.activitymaster.core.services.dto.IResourceItemType;
import com.guicedee.activitymaster.core.services.dto.ISystems;
import com.guicedee.activitymaster.core.services.enumtypes.IResourceType;
import com.guicedee.activitymaster.core.services.system.IResourceItemService;
import gnu.io.NRSerialPort;
import jakarta.cache.annotation.CacheKey;
import jakarta.cache.annotation.CacheResult;
import lombok.Getter;
import com.guicedee.activitymaster.cerialmaster.services.ICerialMasterService;
import com.guicedee.activitymaster.cerialmaster.services.dto.ComPortConnection;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.guicedee.activitymaster.cerialmaster.services.enumerations.CerialMasterClassifications.*;
import static com.guicedee.activitymaster.cerialmaster.services.enumerations.CerialResourceItemTypes.SerialConnectionPort;
import static com.guicedee.guicedinjection.GuiceContext.get;

public class CerialMasterService<J extends CerialMasterService<J>>
		implements ICerialMasterService<J>
{
	@Getter
	private final Map<String, ComPortConnection<?>> connections = new ConcurrentHashMap<>();
	
	@CacheResult(cacheName = "SerialPortResourceType")
	@Override
	public IResourceItemType<?> getSerialConnectionType(ISystems<?> system, UUID... identityToken)
	{
		IResourceItemService<?> resourceService = get(IResourceItemService.class);
		return resourceService.findResourceItemType(SerialConnectionPort, system, identityToken);
	}
	
	@Override
	@CacheResult(cacheName = "ComPortConnections",skipGet = true)
	public ComPortConnection<?> addOrUpdateConnection(@CacheKey ComPortConnection<?> comPort,@CacheKey ISystems<?> system,@CacheKey  UUID... identityToken)
	{
		IResourceType<?> comPortResourceItemType = (IResourceType<?>) getSerialConnectionType(system, identityToken);
		IResourceItemService<?> resourceService = get(IResourceItemService.class);
		IResourceItem<?> comPortResourceItem = resourceService.create(comPortResourceItemType, comPort.getComPort() + "", system, identityToken);
		comPort.setResourceItem(comPortResourceItem);
		
		comPortResourceItem.addOrUpdate(ComPort, "", system, identityToken);
		comPortResourceItem.addOrUpdate(ComPortNumber, comPort.getComPort() + "", system, identityToken);
		comPortResourceItem.addOrUpdate(ComPortDeviceType, comPort.getType()
		                                                          .toString(), system, identityToken);
		comPortResourceItem.addOrUpdate(ComPortStatus, comPort.getStatus()
		                                                      .toString(), system, identityToken);
		comPortResourceItem.addOrUpdate(BaudRate, comPort.getBaudRate() + "", system, identityToken);
		comPortResourceItem.addOrUpdate(BufferSize, comPort.getBufferSize() + "", system, identityToken);
		comPortResourceItem.addOrUpdate(DataBits, comPort.getDataBits() + "", system, identityToken);
		comPortResourceItem.addOrUpdate(StopBits, comPort.getStopBits() + "", system, identityToken);
		comPortResourceItem.addOrUpdate(Parity, comPort.getParity() + "", system, identityToken);
		
		if (!comPort.getAllowedCharacters()
		            .isEmpty())
		{
			StringBuilder allowedChars = new StringBuilder();
			for (Character allowedCharacter : comPort.getAllowedCharacters())
			{
				allowedChars.append(allowedCharacter)
				            .append("^");
			}
			allowedChars.deleteCharAt(allowedChars.length() - 1);
			comPortResourceItem.addOrUpdate(ComPortAllowedCharacters, allowedChars.toString(), system, identityToken);
		}
		else
		{
			comPortResourceItem.addOrUpdate(ComPortAllowedCharacters, "", system, identityToken);
		}
		StringBuilder endOfMessageChars = new StringBuilder();
		for (Character allowedCharacter : comPort.getEndOfMessageCharacters())
		{
			endOfMessageChars.append(allowedCharacter)
			                 .append("^");
		}
		endOfMessageChars.deleteCharAt(endOfMessageChars.length() - 1);
		comPortResourceItem.addOrUpdate(ComPortEndOfMessage, endOfMessageChars.toString(), system, identityToken);
		
		return comPort;
	}
	
	@Override
	@CacheResult(cacheName = "ComPortConnections",skipGet = true)
	public ComPortConnection<?> updateStatus(@CacheKey ComPortConnection<?> comPort,@CacheKey ISystems<?> system,@CacheKey UUID... identityToken)
	{
		IResourceType<?> comPortResourceItemType = (IResourceType<?>) getSerialConnectionType(system, identityToken);
		IResourceItemService<?> resourceService = get(IResourceItemService.class);
		IResourceItem<?> comPortResourceItem = resourceService.findByClassification(comPortResourceItemType, ComPortNumber, comPort.getComPort() + "", system, identityToken);
		comPortResourceItem.addOrUpdate(ComPortStatus, comPort.getStatus()
		                                                      .toString(), system, identityToken);
		return comPort;
	}
	
	@Override
	@CacheResult(cacheName = "ComPortConnections")
	public ComPortConnection<?> findComPortConnection(@CacheKey ComPortConnection<?> comPort,@CacheKey ISystems<?> system,@CacheKey UUID... identityToken)
	{
		IResourceType<?> comPortResourceItemType = (IResourceType<?>) getSerialConnectionType(system, identityToken);
		IResourceItemService<?> resourceService = get(IResourceItemService.class);
		IResourceItem<?> comPortResourceItem = resourceService.findByClassification(comPortResourceItemType, ComPortNumber, comPort.getComPort() + "", system, identityToken);
		if (comPortResourceItem == null)
		{
			return null;
		}
		
		comPort.setResourceItem(comPortResourceItem);
		comPort.setId(comPortResourceItem.getId());
		
		List<Object[]> values = comPortResourceItem.getClassificationsValuePivot(ComPortNumber.toString(), comPortResourceItem.getId()
		                                                                                                                      .toString(),
				system, identityToken,
				ComPortDeviceType.toString(), ComPortStatus.toString(), BaudRate.toString(),
				BufferSize.toString(), DataBits.toString(), StopBits.toString(), Parity.toString(),
				ComPortAllowedCharacters.toString(), ComPortEndOfMessage.toString());
		
		Object[] objects = values.stream()
		                         .findFirst()
		                         .orElseThrow();
		
		comPort.setComPort(Integer.parseInt(objects[1].toString()));
		comPort.setType(ComPortType.valueOf(objects[2].toString()));
		comPort.setStatus(com.guicedee.activitymaster.cerialmaster.services.dto.ComPortStatus.valueOf(objects[3].toString()));
		comPort.setBaudRate(Integer.parseInt(objects[4].toString()));
		comPort.setBufferSize(Integer.parseInt(objects[5].toString()));
		comPort.setDataBits(Integer.parseInt(objects[6].toString()));
		comPort.setStopBits(Integer.parseInt(objects[7].toString()));
		comPort.setParity(Integer.parseInt(objects[8].toString()));
		
		String allowedChars = objects[9].toString();
		if (!Strings.isNullOrEmpty(allowedChars))
		{
			comPort.getAllowedCharacters()
			       .clear();
			Set<Character> chars = new HashSet<>();
			for (String s : allowedChars.split("\\^"))
			{
				chars.add(s.charAt(0));
			}
			
			comPort.getAllowedCharacters()
			       .addAll(chars);
		}
		String eolChars = objects[10].toString();
		if (!Strings.isNullOrEmpty(eolChars))
		{
			comPort.getEndOfMessageCharacters()
			       .clear();
			Set<Character> chars = new HashSet<>();
			for (String s : eolChars.split("\\^"))
			{
				chars.add(s.charAt(0));
			}
			comPort.getEndOfMessageCharacters()
			       .addAll(chars);
		}
		return comPort;
	}
	
	@Override
	public ComPortConnection<?> registerNewConnection(ComPortConnection<?> connection)
	{
		String name = ComPortConnection.COM_NAME + connection.getComPort();
		if (connections.containsKey(name))
		{
			connections.put(name, connection);
		}
		return connections.get(name);
	}
	
	@Override
	public List<String> listComPorts()
	{
		return new ArrayList<>(NRSerialPort.getAvailableSerialPorts());
	}
	
}

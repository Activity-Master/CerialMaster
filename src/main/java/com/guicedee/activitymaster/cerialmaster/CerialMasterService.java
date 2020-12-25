package com.guicedee.activitymaster.cerialmaster;

import com.google.common.base.Strings;
import com.google.inject.Singleton;

import com.guicedee.activitymaster.cerialmaster.implementations.CerialMasterSystem;
import com.guicedee.activitymaster.cerialmaster.services.dto.ComPortType;
import com.guicedee.activitymaster.core.services.dto.IEnterprise;
import com.guicedee.activitymaster.core.services.dto.IResourceItem;
import com.guicedee.activitymaster.core.services.dto.IResourceItemType;
import com.guicedee.activitymaster.core.services.dto.ISystems;
import com.guicedee.activitymaster.core.services.enumtypes.IResourceType;
import com.guicedee.activitymaster.core.services.system.IResourceItemService;
import gnu.io.NRSerialPort;
import jakarta.cache.annotation.CacheResult;
import lombok.Getter;
import com.guicedee.activitymaster.cerialmaster.services.ICerialMasterService;
import com.guicedee.activitymaster.cerialmaster.services.dto.ComPortConnection;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.guicedee.activitymaster.cerialmaster.services.enumerations.CerialMasterClassifications.*;
import static com.guicedee.activitymaster.cerialmaster.services.enumerations.CerialResourceItemTypes.SerialConnectionPort;
import static com.guicedee.guicedinjection.GuiceContext.get;

@Singleton
public class CerialMasterService<J extends CerialMasterService<J>>
		implements ICerialMasterService<J>
{
	@Getter
	private final Map<String, ComPortConnection<?>> connections = new ConcurrentHashMap<>();
	
	@CacheResult(cacheName = "SerialPortResourceType")
	public IResourceItemType<?> getSerialConnectionType(IEnterprise<?> enterprise, UUID... tokens)
	{
		IResourceItemService<?> resourceService = get(IResourceItemService.class);
		return resourceService.findResourceItemType(SerialConnectionPort, enterprise, tokens);
	}
	
	@Override
	public ComPortConnection<?> addOrUpdateConnection(ComPortConnection<?> comPort, IEnterprise<?> enterprise, UUID... identitiyTokens)
	{
		IResourceType<?> comPortResourceItemType = (IResourceType<?>) getSerialConnectionType(enterprise, identitiyTokens);
		ISystems<?> system = get(CerialMasterSystem.class).getSystem(enterprise);
		IResourceItemService<?> resourceService = get(IResourceItemService.class);
		IResourceItem<?> comPortResourceItem = resourceService.create(comPortResourceItemType,comPort.getComPort() + "",system, identitiyTokens);
		
		comPortResourceItem.addOrUpdate(ComPort, "", system, identitiyTokens);
		comPortResourceItem.addOrUpdate(ComPortNumber, comPort.getComPort() + "", system, identitiyTokens);
		comPortResourceItem.addOrUpdate(ComPortDeviceType, comPort.getType().toString(), system, identitiyTokens);
		comPortResourceItem.addOrUpdate(ComPortStatus, comPort.getStatus().toString(), system, identitiyTokens);
		comPortResourceItem.addOrUpdate(BaudRate, comPort.getBaudRate() + "", system, identitiyTokens);
		comPortResourceItem.addOrUpdate(BufferSize, comPort.getBufferSize() + "", system, identitiyTokens);
		comPortResourceItem.addOrUpdate(DataBits, comPort.getDataBits() + "", system, identitiyTokens);
		comPortResourceItem.addOrUpdate(StopBits, comPort.getStopBits() + "", system, identitiyTokens);
		comPortResourceItem.addOrUpdate(Parity, comPort.getParity() + "", system, identitiyTokens);
		
		if(!comPort.getAllowedCharacters().isEmpty())
		{
			StringBuilder allowedChars = new StringBuilder();
			for (Character allowedCharacter : comPort.getAllowedCharacters())
			{
				allowedChars.append(allowedCharacter)
				            .append("^");
			}
			allowedChars.deleteCharAt(allowedChars.length() - 1);
			comPortResourceItem.addOrUpdate(ComPortAllowedCharacters, allowedChars.toString(), system, identitiyTokens);
		}
		else
		{
			comPortResourceItem.addOrUpdate(ComPortAllowedCharacters, "", system, identitiyTokens);
		}
		StringBuilder endOfMessageChars = new StringBuilder();
		for (Character allowedCharacter : comPort.getEndOfMessageCharacters())
		{
			endOfMessageChars.append(allowedCharacter)
			            .append("^");
		}
		endOfMessageChars.deleteCharAt(endOfMessageChars.length() - 1);
		comPortResourceItem.addOrUpdate(ComPortEndOfMessage, endOfMessageChars.toString(), system, identitiyTokens);
		
		return comPort;
	}
	
	@Override
	public ComPortConnection<?> updateStatus(ComPortConnection<?> comPort, IEnterprise<?> enterprise, UUID... identitiyTokens)
	{
		IResourceType<?> comPortResourceItemType = (IResourceType<?>) getSerialConnectionType(enterprise, identitiyTokens);
		ISystems<?> system = get(CerialMasterSystem.class).getSystem(enterprise);
		IResourceItemService<?> resourceService = get(IResourceItemService.class);
		IResourceItem<?> comPortResourceItem = resourceService.findByClassification(comPortResourceItemType,ComPortNumber,comPort.getComPort() + "",system, identitiyTokens);
		comPortResourceItem.addOrUpdate(ComPortStatus, comPort.getStatus().toString(), system, identitiyTokens);
		return comPort;
	}
	
	@Override
	public ComPortConnection<?> findComPortConnection(ComPortConnection<?> comPort, IEnterprise<?> enterprise, UUID... identitiyTokens)
	{
		IResourceType<?> comPortResourceItemType = (IResourceType<?>) getSerialConnectionType(enterprise, identitiyTokens);
		ISystems<?> system = get(CerialMasterSystem.class).getSystem(enterprise);
		IResourceItemService<?> resourceService = get(IResourceItemService.class);
		IResourceItem<?> comPortResourceItem = resourceService.findByClassification(comPortResourceItemType,ComPortNumber,comPort.getComPort() + "",system, identitiyTokens);
		if(comPortResourceItem == null)
			return null;
		
		
		List<Object[]> values = comPortResourceItem.getValuePivot(ComPortNumber.toString(), "", system, identitiyTokens,
				ComPortDeviceType.toString(), ComPortStatus.toString(), BaudRate.toString(),
				BufferSize.toString(), DataBits.toString(), StopBits.toString(), Parity.toString(),
				ComPortAllowedCharacters.toString(), ComPortEndOfMessage.toString());
		
		Object[] objects = values.stream()
		                         .findFirst()
		                         .orElseThrow();
		for (int i = 0; i < objects.length; i++)
		{
			Object o = objects[i];
			if (o == null)
			{
				objects[i] = "";
			}
		}
		comPort.setComPort(Integer.parseInt(objects[1].toString()));
		comPort.setType(ComPortType.valueOf(objects[2].toString()));
		comPort.setStatus(com.guicedee.activitymaster.cerialmaster.services.dto.ComPortStatus.valueOf(objects[3].toString()));
		comPort.setBaudRate(Integer.parseInt(objects[4].toString()));
		comPort.setBufferSize(Integer.parseInt(objects[5].toString()));
		comPort.setDataBits(Integer.parseInt(objects[6].toString()));
		comPort.setStopBits(Integer.parseInt(objects[7].toString()));
		comPort.setParity(Integer.parseInt(objects[8].toString()));
		
		String allowedChars = objects[9].toString();
		if(!Strings.isNullOrEmpty(allowedChars))
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
		if(!Strings.isNullOrEmpty(eolChars))
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

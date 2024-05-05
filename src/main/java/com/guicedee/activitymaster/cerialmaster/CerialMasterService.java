package com.guicedee.activitymaster.cerialmaster;

import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.google.inject.persist.Transactional;
import com.guicedee.activitymaster.cerialmaster.client.ComPortConnection;
import com.guicedee.activitymaster.cerialmaster.client.ComPortType;
import com.guicedee.activitymaster.cerialmaster.client.services.ICerialMasterService;
import com.guicedee.activitymaster.fsdm.client.services.IResourceItemService;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.resourceitem.IResourceItem;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.resourceitem.IResourceItemType;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.systems.ISystems;
import gnu.io.NRSerialPort;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.guicedee.activitymaster.cerialmaster.services.enumerations.CerialMasterClassifications.*;
import static com.guicedee.activitymaster.cerialmaster.services.enumerations.CerialResourceItemTypes.*;
import static com.guicedee.activitymaster.fsdm.client.services.IActivityMasterService.*;
import static com.guicedee.client.IGuiceContext.*;

public class CerialMasterService
		implements ICerialMasterService<CerialMasterService>
{
	@Inject
	private IResourceItemService<?> resourceItemService;
	
	@Inject
	@Named(CerialMasterSystemName)
	private ISystems<?, ?> system;
	
	@Inject
	@Named(CerialMasterSystemName)
	private UUID identityToken;
	
	private static final Map<String, ComPortConnection<?>> connections = new ConcurrentHashMap<>();
	
	public static Map<String, ComPortConnection<?>> getConnections()
	{
		return connections;
	}
	
	@Transactional()
	@Override
	public IResourceItemType<?, ?> getSerialConnectionType(ISystems<?, ?> system, java.util.UUID... identityToken)
	{
		IResourceItemService<?> resourceService = get(IResourceItemService.class);
		return resourceService.findResourceItemType(SerialConnectionPort.toString(), system, identityToken);
	}
	
	@Override
	@Transactional()
	public ComPortConnection<?> addOrUpdateConnection(ComPortConnection<?> comPort, ISystems<?, ?> system, java.util.UUID... identityToken)
	{
		IResourceItemType<?, ?> comPortResourceItemType = getSerialConnectionType(system, identityToken);
		IResourceItemService<?> resourceService = get(IResourceItemService.class);
		IResourceItem<?, ?> comPortResourceItem = resourceService.create(comPortResourceItemType.getName(),
				comPort.getComPort() + "", system, identityToken);
		comPort.setResourceItem(comPortResourceItem);
		
		comPortResourceItem.addOrUpdateClassification(ComPort, "", system, identityToken);
		comPortResourceItem.addOrUpdateClassification(ComPortNumber, comPort.getComPort() + "", system, identityToken);
		comPortResourceItem.addOrUpdateClassification(ComPortDeviceType, comPort.getType()
		                                                                        .toString(), system, identityToken);
		comPortResourceItem.addOrUpdateClassification(ComPortStatus, comPort.getComPortStatus()
		                                                                    .toString(), system, identityToken);
		comPortResourceItem.addOrUpdateClassification(BaudRate, comPort.getBaudRate() + "", system, identityToken);
		comPortResourceItem.addOrUpdateClassification(BufferSize, comPort.getBufferSize() + "", system, identityToken);
		comPortResourceItem.addOrUpdateClassification(DataBits, comPort.getDataBits() + "", system, identityToken);
		comPortResourceItem.addOrUpdateClassification(StopBits, comPort.getStopBits() + "", system, identityToken);
		comPortResourceItem.addOrUpdateClassification(Parity, comPort.getParity() + "", system, identityToken);
		
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
			comPortResourceItem.addOrUpdateClassification(ComPortAllowedCharacters, allowedChars.toString(), system, identityToken);
		}
		else
		{
			comPortResourceItem.addOrUpdateClassification(ComPortAllowedCharacters, "", system, identityToken);
		}
		StringBuilder endOfMessageChars = new StringBuilder();
		if (comPort.getEndOfMessageCharacters()
		           .isEmpty())
		{
			Logger.getLogger("CerialMaster")
			      .log(Level.WARNING, "No End of Message Characters for com port " + comPort.getComPort());
			comPort.getEndOfMessageCharacters().add((char)3);
			comPort.getEndOfMessageCharacters().add((char)4);
			comPort.getEndOfMessageCharacters().add('\n');
			comPort.getEndOfMessageCharacters().add('\r');
			comPort.getEndOfMessageCharacters().add('#');
		}
		
		for (Character allowedCharacter : comPort.getEndOfMessageCharacters())
		{
			endOfMessageChars.append(allowedCharacter)
			                 .append("^");
		}
		endOfMessageChars.deleteCharAt(endOfMessageChars.length() - 1);
		
		comPortResourceItem.addOrUpdateClassification(ComPortEndOfMessage, endOfMessageChars.toString(), system, identityToken);
		
		return comPort;
	}
	
	@Override
	@Transactional()
	public ComPortConnection<?> updateStatus(ComPortConnection<?> comPort, ISystems<?, ?> system, java.util.UUID... identityToken)
	{
		IResourceItemType<?, ?> comPortResourceItemType = getSerialConnectionType(system, identityToken);
		IResourceItemService<?> resourceService = get(IResourceItemService.class);
		IResourceItem<?, ?> comPortResourceItem = resourceService.findByClassification(comPortResourceItemType.getName(), ComPortNumber.toString(), comPort.getComPort() + "", system, identityToken);
		comPortResourceItem.addOrUpdateClassification(ComPortStatus, comPort.getComPortStatus()
		                                                                    .toString(), system, identityToken);
		return comPort;
	}
	
	@Transactional()
	@Override
	public ComPortConnection<?> findComPortConnection(ComPortConnection<?> comPort, ISystems<?, ?> system, java.util.UUID... identityToken)
	{
		IResourceItemType<?, ?> comPortResourceItemType = getSerialConnectionType(system, identityToken);
		IResourceItemService<?> resourceService = get(IResourceItemService.class);
		
		IResourceItem<?, ?> comPortResourceItem = resourceService.findByClassification(comPortResourceItemType.getName(), ComPortNumber.toString(), comPort.getComPort() + "", system, identityToken);
		if (comPortResourceItem == null)
		{
			return null;
		}
		
		comPort.setResourceItem(comPortResourceItem);
		comPort.setId(comPortResourceItem.getId());
		
		List<Object[]> values = comPortResourceItem.builder()
		                                           .getClassificationsValuePivot(ComPortNumber.toString(), comPortResourceItem.getId()
		                                                                                                                      .toString(),
				                                           system, identityToken,
				                                           ComPortDeviceType.toString(),
				                                           ComPortStatus.toString(),
				                                           BaudRate.toString(),
				                                           BufferSize.toString(),
				                                           DataBits.toString(),
				                                           StopBits.toString(),
				                                           Parity.toString(),
				                                           ComPortAllowedCharacters.toString(),
				                                           ComPortEndOfMessage.toString());
		
		Object[] objects = values.stream()
		                         .findFirst()
		                         .orElseThrow();
		
		comPort.setComPort(Integer.parseInt(objects[1].toString()));
		comPort.setType(ComPortType.valueOf(objects[2].toString()));
		comPort.setComPortStatus(com.guicedee.activitymaster.cerialmaster.client.ComPortStatus.valueOf(objects[3].toString()), true);
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
		if (!connections.containsKey(name))
		{
			connections.put(name, connection);
		}
		return connections.get(name);
	}
	
	@Transactional()
	@Override
	public ComPortConnection<?> getComPortConnection(Integer comPort)
	{
		String name = ComPortConnection.COM_NAME + comPort;
		ComPortConnection<?> comm = connections.getOrDefault(name, null);
		if (comm == null)
		{
			comm = findComPortConnection(new ComPortConnection<>(comPort, ComPortType.Server),
					getISystem(CerialMasterSystemName), getISystemToken(CerialMasterSystemName));
			if (comm != null)
			{
				connections.put(name, comm);
			}
		}
		return comm;
	}
	
	@Transactional()
	@Override
	public ComPortConnection<?> getScannerPortConnection(Integer comPort)
	{
		String name = ComPortConnection.COM_NAME + comPort;
		ComPortConnection<?> comm = connections.getOrDefault(name, null);
		if (comm == null)
		{
			comm = findComPortConnection(new ComPortConnection<>(comPort, ComPortType.Scanner),
					getISystem(CerialMasterSystemName), getISystemToken(CerialMasterSystemName));
			if (comm != null)
			{
				connections.put(name, comm);
			}
		}
		return comm;
	}
	
	private static ArrayList<String> comStrings = new ArrayList<>();
	
	@Override
	public List<String> listComPorts()
	{
		if (comStrings.isEmpty())
		{
			comStrings.addAll(NRSerialPort.getAvailableSerialPorts());
		}
		comStrings.sort(String::compareTo);
		return comStrings;
	}
	
	@Transactional()
	@Override
	public List<String> listRegisteredComPorts()
	{
		ArrayList<String> strings = new ArrayList<>();
		for (var iResourceItem : resourceItemService.findByClassificationAll(SerialConnectionPort.toString(), ComPortNumber.toString(), null, system, identityToken))
		{
			strings.add("COM" + iResourceItem.getValue());
		}
		strings.sort(String::compareTo);
		return strings;
	}
	
	
	@Override
	public List<String> listAvailableComPorts()
	{
		List<String> strings = listComPorts();
		List<String> strings1 = listRegisteredComPorts();
		strings.removeAll(strings1);
		return strings;
	}
	
}

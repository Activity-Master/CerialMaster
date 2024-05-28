package com.guicedee.activitymaster.cerialmaster;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.google.inject.persist.Transactional;
import com.guicedee.activitymaster.cerialmaster.client.ComPortConnection;
import com.guicedee.activitymaster.cerialmaster.client.services.ICerialMasterService;
import com.guicedee.activitymaster.fsdm.client.services.IResourceItemService;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.resourceitem.IResourceItem;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.resourceitem.IResourceItemType;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.systems.ISystems;
import com.guicedee.cerial.enumerations.ComPortType;
import gnu.io.NRSerialPort;
import lombok.Getter;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

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
	
	@Getter
	private static final Map<Integer, ComPortConnection<?>> connections = new ConcurrentHashMap<>();
	
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
		comPortResourceItem.addOrUpdateClassification(ComPortDeviceType, comPort.getComPortType()
		                                                                        .toString(), system, identityToken);
		comPortResourceItem.addOrUpdateClassification(ComPortStatus, comPort.getComPortStatus()
		                                                                    .toString(), system, identityToken);
		comPortResourceItem.addOrUpdateClassification(BaudRate, comPort.getBaudRate().toInt() + "", system, identityToken);
		comPortResourceItem.addOrUpdateClassification(BufferSize, comPort.getBufferSize() + "", system, identityToken);
		comPortResourceItem.addOrUpdateClassification(DataBits, comPort.getDataBits().toInt() + "", system, identityToken);
		comPortResourceItem.addOrUpdateClassification(StopBits, comPort.getStopBits().toInt() + "", system, identityToken);
		comPortResourceItem.addOrUpdateClassification(Parity, comPort.getParity().toInt() + "", system, identityToken);
		
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
		comPort.setComPortType(ComPortType.valueOf(objects[2].toString()));
		comPort.setComPortStatus(com.guicedee.cerial.enumerations.ComPortStatus.valueOf(objects[3].toString()), true);
		comPort.setBaudRate(com.guicedee.cerial.enumerations.BaudRate.from(Integer.parseInt(objects[4].toString()) + ""));
		comPort.setBufferSize(Integer.parseInt(objects[5].toString()));
		comPort.setDataBits(com.guicedee.cerial.enumerations.DataBits.fromString(Integer.parseInt(objects[6].toString()) + ""));
		comPort.setStopBits(com.guicedee.cerial.enumerations.StopBits.from(Integer.parseInt(objects[7].toString()) + ""));
		comPort.setParity(com.guicedee.cerial.enumerations.Parity.from(Integer.parseInt(objects[8].toString())));
		
		return comPort;
	}
	
	@Override
	public ComPortConnection<?> registerNewConnection(ComPortConnection<?> connection)
	{
		if (!connections.containsKey(connection.getComPort()))
		{
			connections.put(connection.getComPort(), connection);
		}
		return connections.get(connection.getComPort());
	}
	
	@Transactional()
	@Override
	public ComPortConnection<?> getComPortConnection(Integer comPort)
	{
		ComPortConnection<?> comm = connections.getOrDefault(comPort, null);
		if (comm == null)
		{
			comm = findComPortConnection(new ComPortConnection<>(comPort, ComPortType.Server),
					getISystem(CerialMasterSystemName), getISystemToken(CerialMasterSystemName));
			if (comm != null)
			{
				connections.put(comPort, comm);
			}
		}
		return comm;
	}
	
	@Transactional()
	@Override
	public ComPortConnection<?> getScannerPortConnection(Integer comPort)
	{
		ComPortConnection<?> comm = connections.getOrDefault(comPort, null);
		if (comm == null)
		{
			comm = findComPortConnection(new ComPortConnection<>(comPort, ComPortType.Scanner),
					getISystem(CerialMasterSystemName), getISystemToken(CerialMasterSystemName));
			if (comm != null)
			{
				connections.put(comPort, comm);
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
			comStrings.addAll(NRSerialPort.getAllWindowsPorts());
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

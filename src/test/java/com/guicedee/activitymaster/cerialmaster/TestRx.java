package com.guicedee.activitymaster.cerialmaster;

import com.guicedee.activitymaster.cerialmaster.client.ComPortConnection;
import com.guicedee.activitymaster.cerialmaster.client.services.ICerialMasterService;
import com.guicedee.activitymaster.cerialmaster.client.services.IReceiveMessage;
import com.guicedee.activitymaster.cerialmaster.implementations.CerialMasterSystem;
import com.guicedee.activitymaster.fsdm.ActivityMasterService;
import com.guicedee.activitymaster.fsdm.client.services.IEnterpriseService;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.enterprise.IEnterprise;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.systems.ISystems;
import com.guicedee.cerial.enumerations.Parity;
import com.guicedee.guicedhazelcast.HazelcastProperties;
import com.guicedee.guicedinjection.GuiceContext;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.guicedee.activitymaster.fsdm.DefaultEnterprise.*;
import static com.guicedee.cerial.enumerations.BaudRate.*;
import static com.guicedee.cerial.enumerations.ComPortType.*;
import static com.guicedee.client.IGuiceContext.*;

public class TestRx
{
	static {
		HazelcastProperties.setStartLocal(true);
		HazelcastProperties.setGroupName("fsdm");
		HazelcastProperties.setInstanceName("fsdm");
		if (HazelcastProperties.getAddress() == null) {
			HazelcastProperties.setAddress("127.0.0.1");
		}
		GuiceContext.instance().getConfig()
		            .setIncludeModuleAndJars(true)
		            .setClasspathScanning(true)
		            .setMethodInfo(true)
		            .setFieldInfo(true)
		            .setAnnotationScanning(true)
		            .setPathScanning(true);
		
	//	LogFactory.configureConsoleColourOutput(Level.FINE);
		//	LogColourFormatter.setRenderBlack(false);
	//	LogFactory.configureDefaultLogHiding();
	}
	
	@Test
    public void testComDetailsPersistence()
    {
	   // HibernateEntityManagerProperties.getDefaultProperties().setShowSql(false);
	    
	    ComPortConnection<?> server = new ComPortConnection<>(5, Device);
	    
	    IEnterpriseService enterpriseService = get(IEnterpriseService.class);
	    IEnterprise<?,?> enterprise = enterpriseService.getEnterprise(TestEnterprise.name());
	    ActivityMasterService mSystem = get(ActivityMasterService.class);
	
	    ISystems<?,?> system = get(CerialMasterSystem.class).getSystem(enterprise);
	
	
	    enterpriseService.createNewEnterprise(enterprise);
	
	    UUID identityToken = get(CerialMasterSystem.class).getSystemToken(enterprise);
	
	    ICerialMasterService<?> service = get(ICerialMasterService.class);
	    List<String> strings = service.listComPorts();
	    
	    System.out.println("Trying to load/find com ports from db");
	    for (String string : strings)
	    {
		    int portNumber = Integer.parseInt(string.replace("COM", ""));
		    ComPortConnection<?> search = new ComPortConnection<>(portNumber, Device);
		    search = service.findComPortConnection(search, system, identityToken);
		    if(search == null)
		    {
			    search = new ComPortConnection<>(portNumber, Device);
			    search = service.addOrUpdateConnection(search, system, identityToken).result();
		    }
		
		    search.setComPortType(Device);
		    search.setBaudRate($115200);
		    search.setBufferSize(512000);
		    search.setParity(Parity.None);
		
		    search = service.addOrUpdateConnection(search,system,identityToken).result();
		
		    search = service.findComPortConnection(search, system, identityToken);
		    System.out.println(search);
	    }
    }
    
	public static void main(String[] args)
	{
		ComPortConnection<?> server = new ComPortConnection<>(5, Device);

		server.setBaudRate($9600);
		
		server.connect();
		for (IReceiveMessage<?> receiver : server.getReceivers())
		{
			System.out.println("Receiver : " + receiver);
		}
		server.write("Check");
		
		try
		{
			TimeUnit.SECONDS.sleep(5L);
			String received = CerialKillerTestMessageReceiver.received;
			System.out.println("Message received? : " + received);
		}
		catch (InterruptedException e)
		{
			throw new RuntimeException(e);
		}
		
	}
}

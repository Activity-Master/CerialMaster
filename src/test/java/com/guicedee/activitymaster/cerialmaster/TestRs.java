package com.guicedee.activitymaster.cerialmaster;

import com.guicedee.activitymaster.cerialmaster.implementations.CerialMasterSystem;
import com.guicedee.activitymaster.cerialmaster.services.ICerialMasterService;
import com.guicedee.activitymaster.cerialmaster.services.dto.ComPortConnection;
import com.guicedee.activitymaster.cerialmaster.services.dto.ServerMessage;
import com.guicedee.activitymaster.core.ActivityMasterService;
import com.guicedee.activitymaster.core.services.ConsoleLogActivityMasterProgressMaster;
import com.guicedee.activitymaster.core.services.dto.IEnterprise;
import com.guicedee.activitymaster.core.services.system.IEnterpriseService;
import com.guicedee.guicedhazelcast.HazelcastProperties;
import com.guicedee.guicedinjection.GuiceContext;
import com.guicedee.guicedpersistence.readers.hibernateproperties.HibernateEntityManagerProperties;
import com.guicedee.logger.LogFactory;
import lombok.extern.java.Log;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

import static com.guicedee.activitymaster.cerialmaster.services.dto.ComPortType.Device;
import static com.guicedee.activitymaster.core.DefaultEnterprise.TestEnterprise;
import static com.guicedee.guicedinjection.GuiceContext.get;

@Log
public class TestRs
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
		
		LogFactory.configureConsoleColourOutput(Level.FINE);
		//	LogColourFormatter.setRenderBlack(false);
		LogFactory.configureDefaultLogHiding();
	}
	
	@Test
    public void testComDetailsPersistence()
    {
	    HibernateEntityManagerProperties.getDefaultProperties().setShowSql(true);
	    
	    ComPortConnection<?> server = new ComPortConnection<>(5, Device);
	    
	    IEnterpriseService enterpriseService = get(IEnterpriseService.class);
	    IEnterprise<?> enterprise = enterpriseService.getEnterprise(TestEnterprise);
	    ActivityMasterService mSystem = get(ActivityMasterService.class);
	    
	    
	    mSystem.runUpdatesOnEnterprise(enterprise.getIEnterprise(), new ConsoleLogActivityMasterProgressMaster());
	
	    UUID token = get(CerialMasterSystem.class).getSystemToken(enterprise);
	
	    ICerialMasterService<?> service = get(ICerialMasterService.class);
	    List<String> strings = service.listComPorts();
	    
	    System.out.println("Trying to load/find com ports from db");
	    for (String string : strings)
	    {
		    int portNumber = Integer.parseInt(string.replace("COM", ""));
		    ComPortConnection<?> search = new ComPortConnection<>(portNumber, Device);
		    search = service.findComPortConnection(search, enterprise, token);
		    if(search == null)
		    {
			    search = new ComPortConnection<>(portNumber, Device);
			    search = service.addOrUpdateConnection(search, enterprise, token);
		    }
		
		    search.setType(Device);
		    search.setBaudRate(115200);
		    search.setBufferSize(512000);
		    search.setParity(0);
		
		    search = service.addOrUpdateConnection(search,enterprise,token);
	    }
    }
    
	public static void main(String[] args)
	{
		ComPortConnection<?> server = new ComPortConnection<>(5, Device);
		server.getEndOfMessageCharacters()
		      .add('#');
		server.setBaudRate(115200);
		server.open();
		server.writeMessage(new ServerMessage(server)
		{
			@Override
			public String generateMessage()
			{
				return "*SID001000000000100000001000100060000000020#\n";
			}
			
			@Override
			public ServerMessage simulateResponse()
			{
				return null;
			}
		});
		server.writeMessage(new ServerMessage(server)
		{
			@Override
			public String generateMessage()
			{
				return "*ONL002000000000000000000224159231220000000#\n";
			}
			
			@Override
			public ServerMessage simulateResponse()
			{
				return null;
			}
		});
	}
}

package com.guicedee.activitymaster.cerialmaster;

import com.guicedee.guicedhazelcast.HazelcastProperties;
import com.guicedee.guicedinjection.GuiceContext;
import org.junit.jupiter.api.Test;

public class CerialMasterServiceTest
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

      //  LogFactory.configureConsoleColourOutput(Level.FINE);
        //	LogColourFormatter.setRenderBlack(false);
     //   LogFactory.configureDefaultLogHiding();
    }

    @Test
    public void testListComPorts() {

     //   LogFactory.configureConsoleColourOutput(Level.FINE);
        CerialMasterService cks = GuiceContext.get(CerialMasterService.class);
        System.out.println(cks.listComPorts());
    }

}
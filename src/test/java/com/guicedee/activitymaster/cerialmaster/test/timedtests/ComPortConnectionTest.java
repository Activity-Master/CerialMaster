package com.guicedee.activitymaster.cerialmaster.test.timedtests;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.guicedee.activitymaster.cerialmaster.client.ComPortConnection;
import com.guicedee.activitymaster.cerialmaster.client.services.ICerialMasterService;
import com.guicedee.cerial.enumerations.*;
import com.guicedee.client.IGuiceContext;
import com.guicedee.modules.services.jsonrepresentation.IJsonRepresentation;
import io.smallrye.mutiny.Uni;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ComPortConnectionTest
{
  @Test
  public void testSerialization() throws JsonProcessingException
  {
    ComPortConnection<?> cpc = new ComPortConnection<>(20, ComPortType.Device);
    cpc.setDataBits(DataBits.$8);
    cpc.setParity(Parity.None);
    cpc.setStopBits(StopBits.$1);
    cpc.setFlowControl(FlowControl.None);
    cpc.setComPortStatus(ComPortStatus.Offline);
    //cpc.setComPortType(ComPortType.Device);

    cpc.connect();
    //cpc.configureForRTS();
    // cpc.configureNotifications();

    String json = cpc.toJson();
    System.out.println(json);

    ComPortConnection<?> cpcMatch = IJsonRepresentation.getObjectMapper()
                                        .readValue(json, ComPortConnection.class);
    String json1 = cpcMatch.toJson();
    assertEquals(json, json1);
  }

  @Test
  public void testConnection()
  {
    ICerialMasterService<?> cerialMasterService = IGuiceContext.get(ICerialMasterService.class);
    cerialMasterService.getComPortConnectionDirect(20)
        .invoke(con->{
          con.connect();
          con.write("Hello World!",true);
          con.disconnect();
        }).replaceWith(Uni.createFrom().voidItem()).await().atMost(Duration.ofSeconds(50));
  }

}
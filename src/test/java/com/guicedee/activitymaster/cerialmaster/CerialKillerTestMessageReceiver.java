package com.guicedee.activitymaster.cerialmaster;

import com.guicedee.activitymaster.cerialmaster.client.ComPortConnection;
import com.guicedee.activitymaster.cerialmaster.client.services.IReceiveMessage;

public class CerialKillerTestMessageReceiver implements IReceiveMessage<CerialKillerTestMessageReceiver>
{
    public static String received = null;
    @Override
    public void receiveMessage(String message, ComPortConnection<?> comPortConnection) {
                System.out.println("Message received! - " + message);
        received = message;
    }
}

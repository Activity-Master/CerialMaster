package com.guicedee.activitymaster.cerialmaster;

import com.guicedee.activitymaster.cerialmaster.services.IReceiveMessage;
import com.guicedee.activitymaster.cerialmaster.services.dto.ComPortConnection;

public class CerialKillerTestMessageReceiver implements IReceiveMessage<CerialKillerTestMessageReceiver> {
    @Override
    public void receiveMessage(String message, ComPortConnection<?> comPortConnection) {
        System.out.println("Message received! - " + message);
    }
}

package com.guicedee.activitymaster.cerialmaster.services;

import com.guicedee.guicedinjection.interfaces.IDefaultService;
import com.guicedee.activitymaster.cerialmaster.services.dto.ComPortConnection;

import java.io.Serializable;

public interface IReceiveMessage<J extends IReceiveMessage<J>>
		extends Serializable, IDefaultService<J>
{
	void receiveMessage(String message, ComPortConnection<?> comPortConnection);
}

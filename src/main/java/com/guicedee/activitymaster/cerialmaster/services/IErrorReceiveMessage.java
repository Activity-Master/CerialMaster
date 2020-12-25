package com.guicedee.activitymaster.cerialmaster.services;

import com.guicedee.guicedinjection.interfaces.IDefaultService;
import com.guicedee.activitymaster.cerialmaster.services.dto.ComPortConnection;

import java.io.Serializable;

public interface IErrorReceiveMessage<J extends IErrorReceiveMessage<J>>
		extends Serializable, IDefaultService<J>
{
	void receiveErrorMessage(String message, Throwable exception, ComPortConnection comPortConnection);
}

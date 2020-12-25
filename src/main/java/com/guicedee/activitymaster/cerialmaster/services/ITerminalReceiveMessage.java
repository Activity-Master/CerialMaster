package com.guicedee.activitymaster.cerialmaster.services;

import com.guicedee.guicedinjection.interfaces.IDefaultService;
import com.guicedee.activitymaster.cerialmaster.services.dto.ComPortConnection;

import java.io.Serializable;

public interface ITerminalReceiveMessage<J extends ITerminalReceiveMessage<J>>
		extends Serializable, IDefaultService<J>
{
	void receiveTerminalMessage(String message, Throwable exception, ComPortConnection<?> comPortConnection);
}

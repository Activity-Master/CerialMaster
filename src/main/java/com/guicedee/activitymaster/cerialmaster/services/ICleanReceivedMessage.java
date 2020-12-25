package com.guicedee.activitymaster.cerialmaster.services;

import com.guicedee.guicedinjection.interfaces.IDefaultService;
import com.guicedee.activitymaster.cerialmaster.services.dto.ComPortConnection;

import java.io.Serializable;

public interface ICleanReceivedMessage<J extends ICleanReceivedMessage<J>>
		extends Serializable, IDefaultService<J>
{
	String cleanMessage(String message, ComPortConnection<?> comPortConnection);
}

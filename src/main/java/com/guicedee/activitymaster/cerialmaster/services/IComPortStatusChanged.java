package com.guicedee.activitymaster.cerialmaster.services;

import com.guicedee.guicedinjection.interfaces.IDefaultService;
import com.guicedee.activitymaster.cerialmaster.services.dto.ComPortConnection;
import com.guicedee.activitymaster.cerialmaster.services.dto.ComPortStatus;

import java.io.Serializable;

public interface IComPortStatusChanged<J extends IComPortStatusChanged<J>>
		extends Serializable, IDefaultService<J>
{
	void onComPortStatusChanged(ComPortConnection<?> comPortConnection, ComPortStatus oldStatus, ComPortStatus newStatus);
}

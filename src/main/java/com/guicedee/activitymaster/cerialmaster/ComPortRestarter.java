package com.guicedee.activitymaster.cerialmaster;

import com.guicedee.activitymaster.cerialmaster.services.dto.ComPortConnection;
import com.guicedee.activitymaster.cerialmaster.services.dto.ComPortStatus;
import lombok.extern.java.Log;

import java.util.logging.Level;

@Log
public class ComPortRestarter implements Runnable
{
	private ComPortConnection<?> connection;
	
	public ComPortRestarter(ComPortConnection<?> connection)
	{
		this.connection = connection;
	}
	
	@Override
	public void run()
	{
		if (!ComPortConnection.onlineServerStatus.contains(connection.getComPortStatus()))
		{
			try
			{
				if (connection.isConnected())
				{
					connection.setComPortStatus(ComPortStatus.Idle);
				}
				else
				{
					connection.open();
				}
			}
			catch (Throwable T)
			{
				log.log(Level.SEVERE, "Unable to open com port on retry L(", T);
			}
		}

	}
}

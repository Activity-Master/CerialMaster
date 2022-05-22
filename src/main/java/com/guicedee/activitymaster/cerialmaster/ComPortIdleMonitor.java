package com.guicedee.activitymaster.cerialmaster;

import com.guicedee.activitymaster.cerialmaster.services.dto.ComPortConnection;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static com.guicedee.activitymaster.cerialmaster.services.dto.ComPortConnection.*;
import static com.guicedee.activitymaster.cerialmaster.services.dto.ComPortStatus.*;

public class ComPortIdleMonitor implements Runnable
{
	private ComPortConnection<?> connection;
	private static Duration timeToIdle = Duration.of(10, ChronoUnit.MINUTES);
	
	public ComPortConnection<?> getConnection()
	{
		return connection;
	}
	
	public ComPortIdleMonitor setConnection(ComPortConnection<?> connection)
	{
		this.connection = connection;
		return this;
	}
	
	public static Duration getTimeToIdle()
	{
		return timeToIdle;
	}
	
	public static void setTimeToIdle(Duration timeToIdle)
	{
		ComPortIdleMonitor.timeToIdle = timeToIdle;
	}
	
	@Override
	public void run()
	{
		if (connection != null)
		{
			if (!onlineServerStatus.contains(connection.getComPortStatus()))
			{
				if (connection.getLastMessageReceivedTime() != null)
				{
					connection.setLastMessageReceivedTime(null);
				}
			}
			else
			{
				//online
				if (connection.getLastMessageReceivedTime() != null)
				{
					if (LocalDateTime.now()
					                 .minus(timeToIdle)
					                 .isAfter(connection.getLastMessageReceivedTime()))
					{
						connection.setComPortStatus(Idle);
					}
					else
					{
						if (connection.getComPortStatus() != Running)
						{
							connection.setComPortStatus(Running);
						}
					}
				}
				else if (connection.getComPortStatus() != Silent)
				{
					connection.setComPortStatus(Silent);
				}
			}
		}
		
	}
	
}

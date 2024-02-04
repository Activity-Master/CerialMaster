package com.guicedee.activitymaster.cerialmaster;

import com.guicedee.activitymaster.cerialmaster.client.ComPortConnection;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static com.guicedee.activitymaster.cerialmaster.client.ComPortConnection.*;
import static com.guicedee.activitymaster.cerialmaster.client.ComPortStatus.*;

@Getter
@Setter
@Accessors(chain = true)
public class ComPortIdleMonitor implements Runnable
{
	private ComPortConnection<?> connection;
	private Duration timeToIdle = Duration.of(10, ChronoUnit.MINUTES);
	
	
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
				if (connection.getLastMessageReceivedTime() != null && connection.getComPortStatus() != Idle)
				{
					if (LocalDateTime.now()
					                 .minus(timeToIdle)
					                 .isAfter(connection.getLastMessageReceivedTime()))
					{
						connection.setComPortStatus(Idle);
					}
				}
			}
		}
	}
	
}

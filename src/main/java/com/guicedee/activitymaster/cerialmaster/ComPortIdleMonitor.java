package com.guicedee.activitymaster.cerialmaster;

import com.google.inject.Key;
import com.google.inject.name.Names;
import com.guicedee.activitymaster.cerialmaster.services.dto.ComPortConnection;
import com.guicedee.guicedinjection.GuiceContext;
import com.guicedee.guicedservlets.services.scopes.CallScoper;
import lombok.Getter;
import lombok.Setter;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static com.guicedee.activitymaster.cerialmaster.services.dto.ComPortConnection.*;
import static com.guicedee.activitymaster.cerialmaster.services.dto.ComPortStatus.*;

public class ComPortIdleMonitor implements Runnable
{
	@Setter
	private ComPortConnection<?> connection;
	@Setter
	@Getter
	private static Duration timeToIdle = Duration.of(10, ChronoUnit.MINUTES);
	
	@Override
	public void run()
	{
		CallScoper scoper = GuiceContext.get(Key.get(CallScoper.class, Names.named("callScope")));
		scoper.enter();
		try
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
		finally
		{
			scoper.exit();
		}
	}
	
}

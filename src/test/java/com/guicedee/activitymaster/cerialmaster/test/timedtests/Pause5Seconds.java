package com.guicedee.activitymaster.cerialmaster.test.timedtests;

import com.guicedee.activitymaster.cerialmaster.client.Config;
import com.guicedee.activitymaster.cerialmaster.client.MessageSpec;
import jakarta.validation.constraints.NotNull;

import java.util.Objects;

public class Pause5Seconds<J extends Pause5Seconds<J>> extends MessageSpec<J>
{
	
	private String groupName;
	
	public Pause5Seconds()
	{
	}

  public Pause5Seconds(@NotNull Integer port, @NotNull Integer serverNumber)
	{
    super("pause5Seconds", "Pause 5 Seconds","",
										new Config()
											.setAssignedRetry(5)
											.setAssignedDelayMs(1000)
											.setAssignedTimeoutMs(0L)
											.setAlwaysWaitFullTimeoutAfterSend(true)
				);
  }
		
	@Override
	public String generateMessage()
	{
    setPayload("");
		return "";
	}
	
	@Override
	public boolean equals(Object o)
	{
		if (this == o)
		{
			return true;
		}
		if (o == null || getClass() != o.getClass())
		{
			return false;
		}
		if (!super.equals(o))
		{
			return false;
		}
		Pause5Seconds that = (Pause5Seconds) o;
		return groupName.equals(that.groupName);
	}
	
	@Override
	public int hashCode()
	{
		return Objects.hash(super.hashCode(), groupName);
	}
	
	public String getGroupName()
	{
		return groupName;
	}
	
	public J setGroupName(String groupName)
	{
		this.groupName = groupName;
		return (J)this;
	}


}

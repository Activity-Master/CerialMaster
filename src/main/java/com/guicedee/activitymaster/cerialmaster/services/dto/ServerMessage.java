package com.guicedee.activitymaster.cerialmaster.services.dto;

import com.fasterxml.jackson.annotation.*;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.*;

@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = ANY, getterVisibility = NONE, setterVisibility = NONE)
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "className")
public abstract class ServerMessage<J extends ServerMessage<J>>
		implements Serializable
{
	@Serial
	private static final long serialVersionUID = 1L;

	@Getter
	@Setter
	private ComPortConnection<?> port;
	
	
	@JsonProperty("className")
	public String getClassName()
	{
		return getClass().getCanonicalName();
	}
	
	@Getter
	@Setter
	private boolean outgoing = true;
	
	public ServerMessage()
	{
		this(null);
	}

	public ServerMessage(ComPortConnection<?> port) {
		this.port = port;
	}

	public abstract String generateMessage();

	public abstract ServerMessage<?> simulateResponse();
}

package com.guicedee.activitymaster.cerialmaster.services.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

@Data
@Accessors(chain = true)
public abstract class ServerMessage<J extends ServerMessage<J>>
		implements Serializable
{
	@Serial
	private static final long serialVersionUID = 1L;

	private final ComPortConnection<?> port;

	private boolean outgoing = true;

	public ServerMessage(ComPortConnection<?> port) {
		this.port = port;
	}

	public abstract String generateMessage();

	public abstract ServerMessage simulateResponse();
}

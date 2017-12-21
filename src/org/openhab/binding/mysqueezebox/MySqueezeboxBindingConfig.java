package org.openhab.binding.mysqueezebox;

import org.openhab.core.binding.BindingConfig;

public class MySqueezeboxBindingConfig implements BindingConfig
{
	private final String name;
	private final String playerId;
	private final Integer windowMinutes;
	
	public MySqueezeboxBindingConfig(String name, String playerId, Integer windowMinutes)
	{
		this.name = name;
		this.playerId = playerId;
		this.windowMinutes = windowMinutes;
	}
	
	public String getName()
	{
		return this.name;
	}
	
	public Integer getWindowMinutes()
	{
		return this.windowMinutes;
	}

	public String getPlayerId()
	{
		return this.playerId;
	}
}
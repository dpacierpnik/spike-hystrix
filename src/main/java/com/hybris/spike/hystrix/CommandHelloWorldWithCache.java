package com.hybris.spike.hystrix;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class CommandHelloWorldWithCache extends CommandHelloWorld
{
	private static final Logger LOGGER = LoggerFactory.getLogger(CommandHelloWorldWithCache.class);

	public CommandHelloWorldWithCache(final String name)
	{
		super(name);
	}

	@Override
	protected String getCacheKey()
	{
		return getName();
	}
}

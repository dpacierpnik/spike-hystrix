package com.hybris.spike.hystrix;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.commons.lang.StringUtils.isEmpty;


public class CommandHelloWorld extends HystrixCommand<String>
{
	private static final Logger LOGGER = LoggerFactory.getLogger(CommandHelloWorld.class);

	private final String name;

	public CommandHelloWorld(final String name)
	{
		super(HystrixCommandGroupKey.Factory.asKey("ExampleGroup"));
		this.name = name;
	}

	@Override
	protected String run() throws Exception
	{
		LOGGER.debug("Runnig command...");
		if (isEmpty(name))
		{
			throw new IllegalArgumentException("Name can not be empty;");
		}
		String result = String.format("Hello %s!", name);
		LOGGER.debug("Command executed.");
		return result;
	}

	@Override
	protected String getFallback()
	{
		LOGGER.debug("Command fallback.");
		return "Empty name fallback.";
	}

	protected String getName()
	{
		return name;
	}
}

package com.hybris.spike.hystrix;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.strategy.concurrency.HystrixRequestContext;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.fest.assertions.Assertions.assertThat;


public class CommandHelloWorldWithCacheTest
{
	private static final Logger LOGGER = LoggerFactory.getLogger(CommandHelloWorldWithCacheTest.class);

	private static final String HELLO_NAME = "World";
	private static final String EXPECTED_HELLO = "Hello World!";
	private static final String EXPECTED_FALLBACK = "Empty name fallback.";

	@Test
	public void command_cacheHits()
	{
		HystrixRequestContext context = HystrixRequestContext.initializeContext();
		try
		{
			HystrixCommand<String> commandHelloWorld = createHystrixCommand(true);
			String s = commandHelloWorld.execute();
			LOGGER.debug(s);
			assertThat(s).isEqualTo(EXPECTED_HELLO);
			assertThat(commandHelloWorld.isResponseFromCache()).isFalse();

			HystrixCommand<String> commandHelloWorld2 = createHystrixCommand(false);
			String s2 = commandHelloWorld2.execute();
			LOGGER.debug(s2);
			assertThat(s2).isEqualTo(EXPECTED_HELLO);
			assertThat(commandHelloWorld2.isResponseFromCache()).isTrue();
		}
		finally
		{
			context.shutdown();
		}

		HystrixRequestContext context2 = HystrixRequestContext.initializeContext();
		try
		{
			HystrixCommand<String> commandHelloWorld3 = createHystrixCommand(true);
			String s3 = commandHelloWorld3.execute();
			LOGGER.debug(s3);
			assertThat(s3).isEqualTo(EXPECTED_HELLO);
			assertThat(commandHelloWorld3.isResponseFromCache()).isFalse();
		}
		finally
		{
			context2.shutdown();
		}
	}

	private HystrixCommand<String> createHystrixCommand(final boolean shouldExecuteRun)
	{
		return new CommandHelloWorldWithCache(HELLO_NAME)
		{
			@Override
			protected String run() throws Exception
			{
				assertThat(shouldExecuteRun).isTrue();
				return super.run();
			}
		};
	}
}

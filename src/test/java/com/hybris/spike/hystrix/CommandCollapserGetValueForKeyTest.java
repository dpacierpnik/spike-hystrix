package com.hybris.spike.hystrix;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixEventType;
import com.netflix.hystrix.HystrixRequestLog;
import com.netflix.hystrix.strategy.concurrency.HystrixRequestContext;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Future;

import static org.fest.assertions.Assertions.assertThat;


public class CommandCollapserGetValueForKeyTest
{
	private static final Logger LOGGER = LoggerFactory.getLogger(CommandCollapserGetValueForKeyTest.class);

	@Test
	public void collapsing()
	{
		HystrixRequestContext context = HystrixRequestContext.initializeContext();
		try
		{
			// prepare data (features)
			final int commandsCount = 4;
			Future<String>[] futures = new Future[commandsCount];
			for (int i = 0; i < commandsCount; ++i)
			{
				futures[i] = new CommandCollapserGetValueForKey(i).queue();
			}
			// assertions
			for (int i = 0; i < commandsCount; ++i)
			{
				assertThat("ValueForKey: " + i).isEqualTo(futures[i].get());
			}
			// assert that the batch command 'GetValueForKey' was in fact executed and that it was executed only once
			assertThat(HystrixRequestLog.getCurrentRequest().getExecutedCommands().size()).isEqualTo(1);
			HystrixCommand<?> executedCommand = HystrixRequestLog.getCurrentRequest().getExecutedCommands().iterator().next();
			// assert the command is the one we're expecting
			assertThat(executedCommand.getCommandKey().name()).isEqualTo("GetValueForKey");
			// confirm that it was a COLLAPSED command execution
			assertThat(executedCommand.getExecutionEvents()).contains(HystrixEventType.COLLAPSED);
			// and that it was successful
			assertThat(executedCommand.getExecutionEvents()).contains(HystrixEventType.SUCCESS);
		}
		catch (Exception e)
		{
			LOGGER.error("Test failed.", e);
			throw new RuntimeException(e);
		}
		finally
		{
			context.shutdown();
		}
	}
}

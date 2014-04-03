package com.hybris.spike.hystrix;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.Observer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.fest.assertions.Assertions.assertThat;


public class CommandHelloWorldTest
{
	private static final Logger LOGGER = LoggerFactory.getLogger(CommandHelloWorldTest.class);

	private static final String HELLO_NAME = "World";
	private static final String EXPECTED_HELLO = "Hello World!";
	private static final String EXPECTED_FALLBACK = "Empty name fallback.";

	@Test
	public void command_sync()
	{
		CommandHelloWorld commandHelloWorld = createHystrixCommand(HELLO_NAME);
		String s = commandHelloWorld.execute();
		LOGGER.debug(s);
		assertThat(s).isEqualTo(EXPECTED_HELLO);
	}

	@Test
	public void command_async_thread()
	{
		try
		{
			CommandHelloWorld commandHelloWorld = createHystrixCommand(HELLO_NAME);
			Future<String> future = commandHelloWorld.queue();
			String s = future.get();
			LOGGER.debug(s);
			assertThat(s).isEqualTo(EXPECTED_HELLO);
		}
		catch (Exception e)
		{
			LOGGER.error("Test failed.", e);
			throw new RuntimeException(e);
		}
	}

	@Test
	public void command_async_rxJava()
	{
		try
		{
			CommandHelloWorld commandHelloWorld = createHystrixCommand(HELLO_NAME);
			final CountDownLatch latch = new CountDownLatch(1);
			final List<String> collected = Collections.synchronizedList(new ArrayList<String>(1));
			Observable<String> observable = commandHelloWorld.observe();
			observable.subscribe(new Observer<String>()
			{
				@Override
				public void onCompleted()
				{
					LOGGER.debug("Finished");
					latch.countDown();
				}

				@Override
				public void onError(final Throwable throwable)
				{
					LOGGER.error("Executor failed.", throwable);
				}

				@Override
				public void onNext(final String s)
				{
					LOGGER.debug(s);
					collected.add(s);
				}
			});
			latch.await(3, TimeUnit.SECONDS);
			assertThat(collected.size()).isEqualTo(1);
			assertThat(collected).contains(EXPECTED_HELLO);
		}
		catch (Exception e)
		{
			LOGGER.error("Test failed.", e);
			throw new RuntimeException(e);
		}
	}

	@Test
	public void command_async_rxJava_blocking()
	{
		try
		{
			CommandHelloWorld commandHelloWorld = createHystrixCommand(HELLO_NAME);
			Observable<String> observable = commandHelloWorld.observe();
			String s = observable.toBlockingObservable().single();
			LOGGER.debug(s);
			assertThat(s).isEqualTo(EXPECTED_HELLO);
		}
		catch (Exception e)
		{
			LOGGER.error("Test failed.", e);
			throw new RuntimeException(e);
		}
	}

	@Test
	public void command_sync_fallback()
	{
		CommandHelloWorld commandHelloWorld = createHystrixCommand(null);
		String s = commandHelloWorld.execute();
		LOGGER.debug(s);
		assertThat(s).isEqualTo(EXPECTED_FALLBACK);
	}

	private CommandHelloWorld createHystrixCommand(String name)
	{
		return new CommandHelloWorld(name);
	}
}

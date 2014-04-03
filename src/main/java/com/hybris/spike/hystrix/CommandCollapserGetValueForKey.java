package com.hybris.spike.hystrix;

import com.netflix.hystrix.HystrixCollapser;
import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


public class CommandCollapserGetValueForKey extends HystrixCollapser<List<String>, String, Integer>
{
	private static final Logger LOGGER = LoggerFactory.getLogger(CommandCollapserGetValueForKey.class);

	private final Integer key;

	public CommandCollapserGetValueForKey(final Integer key)
	{
		this.key = key;
	}

	@Override
	public Integer getRequestArgument()
	{
		return key;
	}

	@Override
	protected HystrixCommand<List<String>> createCommand(final Collection<CollapsedRequest<String, Integer>> requests)
	{
		StringBuilder builder = new StringBuilder("Create batch command for requests:");
		for (CollapsedRequest<String, Integer> request : requests)
		{
			builder.append(" ").append(request.getArgument());
		}
		LOGGER.debug(builder.toString());
		return new BatchCommand(requests);
	}

	@Override
	protected void mapResponseToRequests(final List<String> batchResponse, final Collection<CollapsedRequest<String, Integer>> requests)
	{
		LOGGER.debug("Mapping responses to requests...");
		int count = 0;
		for (CollapsedRequest<String, Integer> request : requests)
		{
			String response = batchResponse.get(count++);
			LOGGER.debug(String.format("[%s] Mapping response '%s' to request '%s'", count, response, request.getArgument()));
			request.setResponse(response);
		}
		LOGGER.debug("Responses to requests mapped.");
	}

	private static final class BatchCommand extends HystrixCommand<List<String>>
	{
		private final Collection<CollapsedRequest<String, Integer>> requests;

		private BatchCommand(final Collection<CollapsedRequest<String, Integer>> requests)
		{
			super(Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey("ExampleGroup"))
					.andCommandKey(HystrixCommandKey.Factory.asKey("GetValueForKey")));
			this.requests = requests;
		}

		@Override
		protected List<String> run() throws Exception
		{
			LOGGER.debug("Processing...");
			List<String> response = new ArrayList<>();
			for (CollapsedRequest<String, Integer> request : requests)
			{
				String responseItem = "ValueForKey: " + request.getArgument();
				response.add(responseItem);
				LOGGER.debug(String.format("Request '%s' produced response item: '%s'.", request.getArgument(), responseItem));
			}
			LOGGER.debug("Processing finished.");
			return response;
		}
	}
}

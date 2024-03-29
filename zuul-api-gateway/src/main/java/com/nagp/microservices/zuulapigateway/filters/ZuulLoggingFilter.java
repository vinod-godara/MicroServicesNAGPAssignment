package com.nagp.microservices.zuulapigateway.filters;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;

@RefreshScope
@Component
public class ZuulLoggingFilter extends ZuulFilter {

	Logger logger = LoggerFactory.getLogger(this.getClass());

	@Override
	public boolean shouldFilter() {
		return true;
	}

	@Override
	public Object run() throws ZuulException {
		HttpServletRequest httpServletRequest = RequestContext.getCurrentContext().getRequest();
		logger.info("Request received {}, Request URI: {}", httpServletRequest, httpServletRequest.getRequestURI());

		// Handle exception.
		final RequestContext context = RequestContext.getCurrentContext();
		final Object throwable = context.get("error.exception");

		if (throwable instanceof ZuulException) {
			final ZuulException zuulException = (ZuulException) throwable;
			logger.error("Exception caught by Zuul filter: " + zuulException.getMessage());

			context.remove("error.exception");
			context.setResponseBody("Exception caught by Zuul.");
			context.getResponse().setContentType("application/json");
			context.setResponseStatusCode(500);
		}

		return null;
	}

	@Override
	public String filterType() {
		return "post";
	}

	@Override
	public int filterOrder() {
		return 1;
	}

}

/**
 * Copyright 2017 esutdal

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package org.reactivetechnologies.ticker.rest;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map.Entry;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.reactivetechnologies.ticker.utils.ApplicationContextWrapper;
import org.reactivetechnologies.ticker.utils.CommonHelper;
import org.restexpress.RestExpress;
import org.restexpress.route.Route;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.embedded.PortInUseException;

import io.netty.channel.Channel;

class RestListener extends RestExpress {

	private static final Logger log = LoggerFactory.getLogger(RestListener.class);
	
	public static final String URL_VAL_QNAME = "queue";
	public static final String URL_ADD = "/add/{"+URL_VAL_QNAME+"}";
	public static final String URL_APPEND = "/append/{"+URL_VAL_QNAME+"}";
	public static final String URL_INGEST = "/ingest/{"+URL_VAL_QNAME+"}";

	@Value("${rest.server.port-offset:100}")
	private int portOffset;
	@Value("${rest.server.ioThreads:2}")
	private int ioThreads;
	@Value("${rest.server.execThreads:8}")
	private int execThreads;

	@Autowired
	private AddHandler addService;
	@Autowired
	private AppendHandler appendService;
	@Autowired
	private IngestHandler ingestService;
	
	private static void printRoute(Route r)
	{
		if (log.isInfoEnabled()) {
			log.info(r.getMethod() + " [" + r.getPattern() + "] mapped to action " + r.getAction().getDeclaringClass()
					+ "::" + r.getAction().getName());
		}
	}
	private static void printRoutes(List<Route> r)
	{
		for(Route _r : r)
			printRoute(_r);
	}
	@PostConstruct
	void init()
	{
		startServer();
	}
	@PreDestroy
	void destroy()
	{
		stopServer();
	}
	@Autowired
	private HandlerMappings mappings;
	@Autowired
	private ApplicationContextWrapper ctxWrapper;
	
	@SuppressWarnings("static-access")
	private void mapHandlers()
	{
		List<Route> routes;
		for(Entry<String, String> e : mappings.getMappings().entrySet())
		{
			String path = e.getKey();
			Object controller;
			try {
				Class<?> clazz = Class.forName(e.getValue());
				controller = ctxWrapper.getInstance(clazz, null);
			} catch (ClassNotFoundException e1) {
				controller = ctxWrapper.getInstance(e.getValue());
			}
			
			if(controller != null){
				routes = uri(getBaseUrl()+path, controller).build();
				printRoutes(routes);
			}
			else
			{
				log.error("Unable to bind path: "+path+" to controller: "+e.getValue());
			}
			
		}
	}
	
	private void mapDefaultHandlers()
	{
		List<Route> routes;
		routes = uri(getBaseUrl()+URL_ADD, addService).build();
		printRoutes(routes);
		routes = uri(getBaseUrl()+URL_INGEST, ingestService).build();
		printRoutes(routes);
		routes = uri(getBaseUrl()+URL_APPEND, appendService).build();
		printRoutes(routes);
	}
	public void startServer()
	{
		setIoThreadCount(ioThreads);
		setExecutorThreadCount(execThreads);
		
		if(!mappings.getMappings().isEmpty())
		{
			log.debug("Found mappings: "+mappings.getMappings());
			mapHandlers();
		}
		else
		{
			mapDefaultHandlers();
		}
		
		bind();
		log.info("REST transport started on port "+getPort());
	}
	@Override
	public Channel bind(int port)
	{
		setUseSystemOut(false);
		setPort(getAvailablePort(port));
		if (hasHostname())
		{
			return bind(new InetSocketAddress(getHostname(), port));
		}

		return bind(new InetSocketAddress(port));
	}
	
	private int getAvailablePort(int port) {
		int _port = port;
		String host = null;
		if(hasHostname())
			host = getHostname();
		
		_port = CommonHelper.scanAvailablePort(port, portOffset, host);

		if(_port != -1)
			return _port;
		
		throw new PortInUseException(port)
		{
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public String getMessage() {
				return super.getMessage()+". Unable to find free ports within a range offset of "+portOffset;
			}
		};
	}
	public void stopServer()
	{
		shutdown(true);
		log.info("REST transport stopped..");
	}
	public static void main(String[] args) throws InterruptedException {
		RestListener s = new RestListener();
		s.startServer();
		Thread.sleep(2000);
		s.stopServer();
	}

}

package com.TDsoft.server;

import javax.servlet.ServletContext;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.XmlWebApplicationContext;

public class ServerApplicationListener implements ApplicationListener<ContextRefreshedEvent> {

	private static Logger LOGGER = LoggerFactory.getLogger(ServerApplicationListener.class);
	
	public void onApplicationEvent(ContextRefreshedEvent event) {
		Object source = event.getSource();
		if (source instanceof ApplicationContext) {
			// avoid to loop push context refreshed event
			ApplicationContext sourceCtx = (ApplicationContext) event.getSource();
			if (sourceCtx.getParent() != null) {
				return;
			}
			ApplicationContext ctx = event.getApplicationContext();
			Server svr = ctx.getBean(Server.class);
			createSpringWebApplicationContext(svr, ctx);
			try {
				svr.start();
				LOGGER.info("Jetty server started.");
			} catch (Exception e) {
				LOGGER.error("Failed to start server.", e);
				System.exit(-1);
			}
		}
	}

	/**
	 * Create shared spring ApplicationContext
	 * 
	 * @param svr Jetty Server Object
	 * @param ctx ApplicationContext
	 */
	private void createSpringWebApplicationContext(Server svr, ApplicationContext ctx) {
		ServletContext servletContext = null;
		ServletContextHandler contextHandler = null;
		for (Handler handler : svr.getHandlers()) {
			if (handler instanceof HandlerCollection) {
				HandlerCollection handlerCollection = (HandlerCollection) handler;
				if (handlerCollection.getHandlers().length > 0) {
					// get the last one
					contextHandler = (ServletContextHandler) handlerCollection.getHandlers()[handlerCollection.getHandlers().length -1 ];
					servletContext = contextHandler.getServletContext();
					break;
				}
			}
		}
		if (servletContext == null || contextHandler == null) {
			throw new IllegalStateException("Context is null. ServletContext:" + servletContext + " , ContextHandler:"
					+ contextHandler);
		}
		XmlWebApplicationContext webctx = new XmlWebApplicationContext();
		webctx.setParent(ctx);
		webctx.setConfigLocation("");
		webctx.setServletContext(servletContext);
		webctx.refresh();

		servletContext.setAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE, webctx);
	}
}

package com.TDsoft.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class AppMain {
	private static final String SSERVER_CONTEXT_XML_PATH = "classpath:applicationContext.xml";
	private static Logger LOGGER = LoggerFactory.getLogger(AppMain.class);

	private static ClassPathXmlApplicationContext ctx;

	public static void main(String[] args) {
		try {
			ctx = new ClassPathXmlApplicationContext(SSERVER_CONTEXT_XML_PATH);
		} catch (Exception e) {
			LOGGER.error("Failed to start server.", e);
			System.exit(-1);
		}
	}
	
	public static void stop() {
		ctx.registerShutdownHook();
	}
}

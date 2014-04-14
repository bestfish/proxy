package com.fish.play.proxy;

import com.fish.play.proxy.config.ConfigLoader;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.net.ServerSocket;
import java.net.Socket;
/**
 * socket处理分发器
 * @author changliang
 *
 */
public class ProxyDispatcher {
	private static final Log log = LogFactory.getLog(ProxyDispatcher.class);
	private long proxyNumber;
	public void init() {
		ServerSocket serverSocket = null;
		try {
			serverSocket = new ServerSocket(ConfigLoader.SERVICE_PORT);
			log.info("listen on port " + ConfigLoader.SERVICE_PORT);
			while (true) {
				Socket socket = serverSocket.accept();
				new ProxyHandler(socket).start();
				proxyNumber ++;
				log.info(proxyNumber);
			}
		} catch (Exception e) {
			log.error("accept socket connection error.");
		} finally {
			if (serverSocket != null) {
				try {
					serverSocket.close();
				} catch (Exception e) {
					log.error("ServerSocket close error.");
				}
			}
		}

	}
}

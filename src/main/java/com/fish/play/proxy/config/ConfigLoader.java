package com.fish.play.proxy.config;

import com.fish.play.proxy.filter.ProxyFilter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

/**
 * 加载配置项
 * @author changliang
 *
 */
public class ConfigLoader {
	private static Log log = LogFactory.getLog(ConfigLoader.class);
	
	/**
	 * 绑定服务监听端口
	 */
	public static final int SERVICE_PORT;
	
	/**
	 * socket read()超时时间
	 */
	public static final int SOCKET_TIMEOUT ;

	/**
	 * 是否开启请求方法过滤
	 */
	public static final boolean REQUEST_METHOD_ON;
	/**
	 * 过滤方法集合
	 */
	public static final Set<String> REQUEST_METHOD_SET;
	
	/**
	 * 是否开启url内容过滤
	 */
	public static final boolean REQUEST_URL_ON;
	/**
	 * 过滤url内容集合
	 */
	public static final Set<String> REQUEST_URL_SET;
	
	
	static {
		Properties prop = new Properties();
		InputStream in = ProxyFilter.class.getClassLoader().getResourceAsStream("config/configuration.properties");
		try {
			prop.load(in);
		} catch (IOException e) {
			log.error("load configuration.properties error.");
		}
		SERVICE_PORT = Integer.parseInt(prop.getProperty("service_port"));
		SOCKET_TIMEOUT = Integer.parseInt(prop.getProperty("socket_timeout"));
		REQUEST_METHOD_ON = Boolean.valueOf(prop.getProperty("request_method_filter"));
		REQUEST_URL_ON = Boolean.valueOf(prop.getProperty("request_url_filter"));

		if (REQUEST_METHOD_ON) {
			String request_method_contents = prop.getProperty("request_method_contents");
			String[] methodArray = request_method_contents.split(",");
			REQUEST_METHOD_SET = new HashSet<String>();
			for (String method : methodArray) {
				REQUEST_METHOD_SET.add(method);
			}
		} else {
			REQUEST_METHOD_SET = null;
		}
		
		if (REQUEST_URL_ON) {
			String request_url_contents = prop.getProperty("request_url_contents");
			String[] urlArray = request_url_contents.split(",");
			REQUEST_URL_SET = new HashSet<String>();
			for (String url : urlArray) {
				REQUEST_URL_SET.add(url);
			}
		} else {
			REQUEST_URL_SET = null;
		}
	}

}

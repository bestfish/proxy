package com.fish.play.proxy.filter;

import com.fish.play.proxy.config.ConfigLoader;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
/**
 * 代理过滤配置
 * @author changliang
 *
 */
public final class ProxyFilter {
	private static final Log log = LogFactory.getLog(ProxyFilter.class);
	
	
	/**
	 * @param method 请求方法, 比如get,post, connect
	 * @return 是否是允许的请求方式, 不允许则返回true
	 */
	private static boolean isForbiddenProtocolMethod(String method) {
		return ConfigLoader.REQUEST_METHOD_SET.contains(method);
	}
	
	/**
	 * @param queryString
	 * @return 是否是非法的请求内容,若非法则返回true.
	 */
	private static boolean isForbiddenUrl(String queryString) {
		boolean flag = false;
		for (String content : ConfigLoader.REQUEST_URL_SET) {
			if (queryString.contains(content)) {
				flag = true;
				break;
			}
		}
		return flag;
	}
	
	public static boolean canHeaderGoThrough(String header) {
		String[] action = header.split(" ");
		if (action.length < 3) {
			log.info("http header is not valid.");
			return false;
		}
		
		String method = action[0];
		if (ConfigLoader.REQUEST_METHOD_ON && isForbiddenProtocolMethod(method)) {
			log.info("request method: " + method + " is filtered.");
			return false;
		}
		
		String address = action[1];
		if (ConfigLoader.REQUEST_URL_ON && isForbiddenUrl(address)) {
			log.info("request contains invalid content, so it is filtered");
			return false;
		}
		
		return true;
	}
	
	
}

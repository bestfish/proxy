package com.fish.play.proxy.carrier;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 记录每一个socket请求的数据信息
 * 
 * @author changliang
 * 
 */
public class Flow extends Thread implements Serializable {
	private static Log log = LogFactory.getLog(Flow.class);

	private static final long serialVersionUID = 1L;
	private boolean isFiltered;
	/**
	 * 请求是否成功
	 */
	private long status;
	
	private String failMsg;

	private String visitTime;

	private String requestUrl;

	private String requestMethod;

	/**
	 * 请求总流量
	 */
	private long requestBytes;
	
	/**
	 * 纯header大小
	 */
	private long requestPureHeaderBytes;

	/**
	 * get请求中参数字节数或post请求body中参数字节数
	 */
	private long requestValidBytes;

	/**
	 * 响应总流量
	 */
	private long responseBytes;

	/**
	 * 响应中实际有效的内容字节数
	 */
	private long responseValidBytes;

	private long responseTimeMills;

	public Flow() {
		DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		this.visitTime = format.format(new Date());
	}

	/**
	 * 打印信息
	 * 格式：
	 * visitTime|status|method|requestBytes|requestPureHeaderBytes|requestValidBytes|responseBytes|responseValidBytes|totalBytes|time|reason(可选)
	 */
	@Override
	public void run() {
		if (isFiltered) {
			return;
		}
		
		StringBuilder builder = new StringBuilder();
		builder.append(visitTime);
		if (status == 200) {
			builder.append(",").append("ok");
			builder.append(",").append(requestUrl);
			builder.append(",").append(requestMethod);
			builder.append(",").append(requestBytes);
			builder.append(",").append(requestPureHeaderBytes);
			builder.append(",").append(requestValidBytes);
			builder.append(",").append(responseBytes);
			builder.append(",").append(responseValidBytes);
			builder.append(",").append(requestBytes + responseBytes);
			builder.append(",").append(responseTimeMills);
		} else {
			builder.append(",").append("failed");
			if (requestUrl != null && requestUrl.length() > 0) {
				builder.append(",").append(requestUrl);
			} else {
				builder.append(",");
			}
			if (requestMethod != null && requestMethod.length() > 0) {
				builder.append(",").append(requestMethod);
			} else {
				builder.append(",");
			}
			builder.append(",").append(",").append(",").append(",").append(",").append(",").append(",");
			if (failMsg != null) {
				builder.append(",").append(failMsg);
			} else {
				builder.append(",").append(status);
			}
			
		}
		
		log.info(builder);
	}
	


	public long getStatus() {
		return status;
	}

	public void setStatus(long status) {
		this.status = status;
	}

	public String getVisitTime() {
		return visitTime;
	}

	public void setVisitTime(String visitTime) {
		this.visitTime = visitTime;
	}

	public String getRequestUrl() {
		return requestUrl;
	}

	public void setRequestUrl(String requestUrl) {
		this.requestUrl = requestUrl;
	}

	public String getRequestMethod() {
		return requestMethod;
	}

	public void setRequestMethod(String requestMethod) {
		this.requestMethod = requestMethod;
	}

	public long getRequestPureHeaderBytes() {
		return requestPureHeaderBytes;
	}

	public void setRequestPureHeaderBytes(long requestPureHeaderBytes) {
		this.requestPureHeaderBytes = requestPureHeaderBytes;
	}

	public long getRequestBytes() {
		return requestBytes;
	}

	public void setRequestBytes(long requestBytes) {
		this.requestBytes = requestBytes;
	}

	public long getRequestValidBytes() {
		return requestValidBytes;
	}

	public void setRequestValidBytes(long requestValidBytes) {
		this.requestValidBytes = requestValidBytes;
	}

	public long getResponseBytes() {
		return responseBytes;
	}

	public void setResponseBytes(long responseBytes) {
		this.responseBytes = responseBytes;
	}

	public long getResponseValidBytes() {
		return responseValidBytes;
	}

	public void setResponseValidBytes(long responseValidBytes) {
		this.responseValidBytes = responseValidBytes;
	}

	public long getResponseTimeMills() {
		return responseTimeMills;
	}

	public void setResponseTimeMills(long responseTimeMills) {
		this.responseTimeMills = responseTimeMills;
	}

	public void setFiltered(boolean isFiltered) {
		this.isFiltered = isFiltered;
	}

	public boolean isFiltered() {
		return isFiltered;
	}

	public String getFailMsg() {
		return failMsg;
	}

	public void setFailMsg(String failMsg) {
		this.failMsg = failMsg;
	}
	

}

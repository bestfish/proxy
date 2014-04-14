package com.fish.play.proxy.excel.entity;

public class TemplateEntity {

	private String status;
	private String address;
	private String method;
	private long reqBytes;
	private long reqHeaderBytes;
	private long reqParamBytes;
	private long resBytes;
	private long resConBytes;
	private long totalBytes;
	private long timeMills;
	private String failMsg;

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public long getReqBytes() {
		return reqBytes;
	}

	public void setReqBytes(long reqBytes) {
		this.reqBytes = reqBytes;
	}

	public long getReqHeaderBytes() {
		return reqHeaderBytes;
	}

	public void setReqHeaderBytes(long reqHeaderBytes) {
		this.reqHeaderBytes = reqHeaderBytes;
	}

	public long getReqParamBytes() {
		return reqParamBytes;
	}

	public void setReqParamBytes(long reqParamBytes) {
		this.reqParamBytes = reqParamBytes;
	}

	public long getResBytes() {
		return resBytes;
	}

	public void setResBytes(long resBytes) {
		this.resBytes = resBytes;
	}

	public long getResConBytes() {
		return resConBytes;
	}

	public void setResConBytes(long resConBytes) {
		this.resConBytes = resConBytes;
	}

	public long getTotalBytes() {
		return totalBytes;
	}

	public void setTotalBytes(long totalBytes) {
		this.totalBytes = totalBytes;
	}

	public long getTimeMills() {
		return timeMills;
	}

	public void setTimeMills(long timeMills) {
		this.timeMills = timeMills;
	}

	public String getFailMsg() {
		return failMsg;
	}

	public void setFailMsg(String failMsg) {
		this.failMsg = failMsg;
	}

}

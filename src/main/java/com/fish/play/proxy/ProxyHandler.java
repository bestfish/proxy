package com.fish.play.proxy;

import com.fish.play.proxy.carrier.Flow;
import com.fish.play.proxy.config.ConfigLoader;
import com.fish.play.proxy.filter.ProxyFilter;
import com.fish.play.proxy.streamop.ChunkedInputStreamBean;
import com.fish.play.proxy.streamop.ContentLengthInputStreamBean;
import com.fish.play.proxy.streamop.HeaderBean;
import com.fish.play.proxy.util.EncodingUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
/**
 * 代理交互处理器
 * <pre>
 * socket.setSoTimeout(TIMEOUT)
 * 此项设置理解成是inputstream.read()的超时时间更为确切.
 * </pre>
 * @author changliang
 *
 */
public class ProxyHandler extends Thread {
	private static final Log log = LogFactory.getLog(ProxyHandler.class);
	private Socket socket;

	public ProxyHandler(Socket socket) {
		this.socket = socket;
	}

	@Override
	public void run() {
		act(socket);
	}
	/**
	 * Get请求格式：
	 * <pre>
	 * GET http://www.jd.com HTTP/1.1
	 * Accept: *
	 * Accept-Language: zh-cn
	 * Accept-Encoding: gzip, deflate
	 * User-Agent: Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; .NET CLR 2.0.50727; .NET CLR 3.0.04506.648; .NET CLR 3.5.21022)
	 * Host: www.jd.com
	 * Connection: Keep-Alive
	 * (CRLF)
	 * </pre>
	 * Post请求格式:
	 * <pre>
	 * POST / HTTP1.1
	 * Host:www.wrox.com
	 * User-Agent:Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; .NET CLR 2.0.50727; .NET CLR 3.0.04506.648; .NET CLR 3.5.21022)
	 * Content-Type:application/x-www-form-urlencoded
	 * Content-Length:40
	 * Connection: Keep-Alive
	 * (CRLF)
	 * client=apple
	 * </pre>
	 * 
	 * 请求格式的第三部分是空行(CRLF)， 就算没有请求主体， 也必须有空行
	 * 
	 * @param socket
	 */
	private void act(Socket socket) {
		Socket remote = null;
		/**
		 * 记录信息
		 */
		Flow flow = new Flow();
		long requestBytes = 0;
		try {
			socket.setSoTimeout(ConfigLoader.SOCKET_TIMEOUT);
			socket.setKeepAlive(false);
			InputStream inputStream = new BufferedInputStream(socket.getInputStream(), 1024);
			OutputStream outputStream = new BufferedOutputStream(socket.getOutputStream(), 1024);

			/**
			 * 读取第一行即请求行: GET http://www.jd.com HTTP/1.1
			 */
			byte[] buffer = HeaderBean.readLine(inputStream);
			requestBytes += buffer.length;
			if (buffer.length < 1) {
				flow.setFailMsg("can't get address from request header!");
				return;
			}
			
			
			String header = EncodingUtil.getAsciiString(buffer);
			
			if (!ProxyFilter.canHeaderGoThrough(header)) {
				flow.setFiltered(true);
				return;
			}
			
			
			String[] action = header.split(" ");
			String method = action[0];
			/**
			 * 得到请求的目标地址,有相对路径和绝对路径区别
			 */
			String address = action[1];
			boolean containsHost = address.startsWith("http://");
			
			/**
			 * 汇聚请求中的内容
			 */
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			bos.write(buffer, 0, buffer.length);
			
			/**
			 * post请求时，header里面带有Content-Length属性
			 */
			long contentLength = -1L;
			while ((buffer = HeaderBean.readLine(inputStream)).length > 0) {
				bos.write(buffer, 0, buffer.length);
				
				requestBytes += buffer.length;
				header = EncodingUtil.getAsciiString(buffer).trim();
				if (header.length() < 1) {
					/**
					 * 添加对request header大小的精确统计
					 */
					flow.setRequestPureHeaderBytes(requestBytes);
					break;
				}
				
				if (header.startsWith("Content-Length:")) {
					contentLength = Long.parseLong(header.substring(15).trim());
				}
				
				/**
				 * 请求头里,可能是相对路径,因此需要从Host属性里取host地址
				 */
				if (!containsHost && header.startsWith("Host:")) {
					address = header.substring(5).trim() + address;
				}
			}
			/**
			 * 如果是post请求内容,将会在CRLF行后,接上请求具体内容
			 */
			if (contentLength > 0) {
				requestBytes += contentLength;
				bos.write(ContentLengthInputStreamBean.readContent(inputStream, 1024, contentLength));
			}
			
			/*以上,读取请求中所有内容,以下,开启与目标服务的内容交互*/
			
			URL url = new URL(address);
			String host = url.getHost();
			int port = (url.getPort() > -1 ? url.getPort() : 80);
			
			flow.setRequestUrl(address);
			flow.setRequestMethod(method);
			flow.setRequestBytes(requestBytes);
			if (contentLength > 0) {
				flow.setRequestValidBytes(contentLength);
			} else {
				String queryString = url.getQuery();
				flow.setRequestValidBytes(queryString == null ? 0L : queryString.getBytes().length);
			}
			
			long startMills = System.currentTimeMillis();
			
			remote = new Socket(host, port);
			remote.setSoTimeout(ConfigLoader.SOCKET_TIMEOUT);
			InputStream remoteInputStream = new BufferedInputStream(remote.getInputStream(), 1024);
			OutputStream remoteOutputStream = new BufferedOutputStream(remote.getOutputStream(), 1024);
			byte[] inBytes = bos.toByteArray();
			remoteOutputStream.write(inBytes);
			remoteOutputStream.flush();
			
			/**
			 * 从目标服务取响应内容至客户端
			 */
			Map<String, Long> map = brigeSockectStream(remoteInputStream, outputStream);
			long endMills = System.currentTimeMillis();
			flow.setResponseTimeMills(endMills - startMills);
			flow.setStatus(map.get("status"));
			flow.setResponseBytes(map.get("allbytes"));
			flow.setResponseValidBytes(map.get("contentbytes"));
		} catch (IOException e) {
			flow.setFailMsg(e.getMessage());
			log.error("error occurs, msg:" + e);
		} finally {
			try {
				if (socket != null) {
					socket.close();
				}
			} catch (IOException e) {
				log.error("client socket close error.");
			}

			try {
				if (remote != null) {
					remote.close();
				}
			} catch (IOException e) {
				log.error("remote socket close error.");
			}
			flow.start();
		}
	}
	
	/**
	 * 响应的header中,如果有Content-Encoding:gzip,那么说明传
	 * 过来的数据是经过压缩的.那么流量统计时,比如一个字符串"abc",不压缩的时候,
	 * 占用三个字节,经过压缩后,就不一定是三个了.当数据量大的时候,压缩后的字节数会变少,当就是"abc"
	 * 的时候,压缩后的显然大于3
	 * 
	 * @param inputStream 从服务端读过来的流
	 * @param outputStream 写往客户端的流
	 * @return map, 装载响应的流量大小,除去header头的大小以及响应返回的状态
	 * @throws IOException
	 */
	private Map<String, Long> brigeSockectStream(InputStream inputStream, OutputStream outputStream) throws IOException {
		Map<String, Long> map = new HashMap<String, Long>();
		
		byte[] buffer;
		String temp;
		/*响应总字节数*/
		long sum_bytes = 0L;
		/*响应中实际内容字节数*/
		long sum_contentBytes = 0L;
		long contentLength = -1L;
		boolean isFirstLine = true;
		while ((buffer = HeaderBean.readLine(inputStream)).length > 0) {
			outputStream.write(buffer);
			
			sum_bytes += buffer.length;
			temp = EncodingUtil.getAsciiString(buffer).trim();
			if (temp.length() < 1) {
				break;
			}
			if (temp.contains("Content-Length:")) {
				contentLength = Long.parseLong(temp.substring(15).trim());
			}
			
			if (isFirstLine) {
				String[] array = temp.split(" ");
				map.put("status", Long.parseLong(array[1].trim()));
				isFirstLine = false;
			}
			
		}
		
		/**
		 * 上述while循环结束时,说明读到了CRLF那一行,再往下面,就是传输的实际内容.
		 * 下面分为两种方式：
		 * 1 header中带有Content-Length: 123
		 * 2 header中指定Transfer-Encoding: chunked
		 * 判断contentLength的值,若为-1,则说明header可没有指定长度,那么,显然是
		 * 采用chunked方式
		 */
		
		if (contentLength > -1) {
			buffer = ContentLengthInputStreamBean.readContent(inputStream, 1024, contentLength);
			outputStream.write(buffer);
			outputStream.flush();
			sum_bytes += buffer.length;
			sum_contentBytes = buffer.length;
		} else {
			long[] result = ChunkedInputStreamBean.transportChunkData(inputStream, outputStream);
			sum_bytes += result[0];
			sum_contentBytes = result[1];
		}
		
		map.put("allbytes", sum_bytes);
		map.put("contentbytes", sum_contentBytes);
		return map;
	}

}

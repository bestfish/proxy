package com.fish.play.proxy.streamop;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 用于解析请求或响应的头信息
 * @author changliang
 *
 */
public class HeaderBean {
	
	/**
	 * 读取一行内容,包括行末结束符'\n' <br>
	 * 对于header内容,每一行以CRLF结尾
	 * @param inputStream 输入流
	 * @return 读取到的字节数组
	 * @throws IOException
	 */
	public static byte[] readLine(InputStream inputStream) throws IOException {
		int nextByte = -1;
		ByteArrayOutputStream bos = new ByteArrayOutputStream();

		while ((nextByte = inputStream.read()) != -1) {
			if (nextByte == '\n') {
				bos.write(nextByte);
				break;
			}
			bos.write(nextByte);
		}
		return bos.toByteArray();
	}
	
	
}

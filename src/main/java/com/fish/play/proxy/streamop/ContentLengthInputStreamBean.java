package com.fish.play.proxy.streamop;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 针对响应返回Content-Length头,用指定长度去流中读取内容,避免读完之后产生
 * 的read()阻塞
 * 
 * @author changliang
 * 
 */
public class ContentLengthInputStreamBean {

	/**
	 * 从流中读取指定的字节数
	 * @param inputStream 输入流
	 * @param bufferSize 缓冲数组
	 * @param contentLength 指定长度
	 * @return 读取到的字节数组
	 * @throws IOException
	 */
	public static byte[] readContent(InputStream inputStream, int bufferSize, long contentLength) throws IOException {
		if (contentLength > 0) {
			int readBytes = 0;
			long remaining = contentLength;
			int length = Math.min(bufferSize, (int) (contentLength));
			byte[] buffer = new byte[length];

			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

			/**
			 * remaining 表示未读取的内容长度
			 */
			while (remaining > 0) {
				if (remaining > length) {
					readBytes = inputStream.read(buffer, 0, length);
				} else {
					readBytes = inputStream.read(buffer, 0, (int) remaining);
				}

				if (readBytes > 0) {
					outputStream.write(buffer, 0, readBytes);
					remaining -= readBytes;
				} else {
					break;
				}
			}
			return outputStream.toByteArray();
		}
		return new byte[0];
	}

}

package com.fish.play.proxy.streamop;


import com.fish.play.proxy.util.EncodingUtil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 解析socket inputstream, 杜绝read()方法阻塞 <br>
 * 针对以 chunked块传输的响应,格式如下： chunk编码格式如下：
 * 
 * <pre>
 * [chunk size][\r\n][chunk data][\r\n][chunk size][\r\n][chunk data][\r\n][chunk size = 0][\r\n][\r\n]
 * </pre>
 * 
 * @author changliang
 * 
 */
public class ChunkedInputStreamBean {

	private static final byte[] CRLF = { '\r', '\n' };

	/**
	 * States: 0=normal, 1=\r was scanned, 2=inside quoted string, -1=end
	 * 
	 * @param in 输入流
	 * @return 返回一个长度为2的数组,[0]表示chunk内容总长度;[1]表示chunk块的实际内容长度
	 * @throws IOException
	 */
	public static int[] getChunkSizeFromInputStream(final InputStream in,
			final OutputStream outputStream) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		int state = 0;
		while (state != -1) {
			int b = in.read();
			if (b == -1) {
				return new int[]{0,0};
			}
			switch (state) {
			case 0:
				switch (b) {
				case '\r':
					state = 1;
					break;
				case '\"':
					state = 2;
					/* fall through */
				default:
					baos.write(b);
				}
				break;

			case 1:
				if (b == '\n') {
					state = -1;
				} else {
					throw new IOException("Protocol violation: Unexpected"
							+ " single newline character in chunk size");
				}
				break;

			case 2:
				switch (b) {
				case '\\':
					b = in.read();
					baos.write(b);
					break;
				case '\"':
					state = 0;
					/* fall through */
				default:
					baos.write(b);
				}
				break;
			default:
				throw new RuntimeException("assertion failed");
			}
		}

		/**
		 * 解析chunk块头中16进制数据的值
		 */
		String dataString = EncodingUtil.getAsciiString(baos.toByteArray());
		int separator = dataString.indexOf(';');
		dataString = (separator > 0) ? dataString.substring(0, separator)
				.trim() : dataString.trim();
		int result;
		try {
			result = Integer.parseInt(dataString.trim(), 16);
		} catch (NumberFormatException e) {
			throw new IOException("Bad chunk size: " + dataString);
		}

		int length = baos.toByteArray().length + CRLF.length;
		outputStream.write(baos.toByteArray());
		writeCRLF(outputStream);
		if (result > 0) {
			byte[] buffer = ContentLengthInputStreamBean.readContent(in, 1024, result);
			outputStream.write(buffer);
			writeCRLF(outputStream);
			length += buffer.length + CRLF.length;
		} else {
			writeCRLF(outputStream);
			outputStream.flush();
			length += CRLF.length;
		}
		int[] array = {length, result};
		return array;
	}

	private static void readCRLF(final InputStream inputStream) throws IOException {
		int cr = inputStream.read();
		int lf = inputStream.read();
		if ((cr != '\r') || (lf != '\n')) {
			throw new IOException("CRLF expected at end of chunk: " + cr + "/" + lf);
		}
	}
	
	private static void writeCRLF(final OutputStream outputStream) throws IOException {
		outputStream.write(CRLF);
	}
	
	/**
	 * 
	 * @param inputStream 输入流
	 * @param outputStream 输出流
	 * @return 返回一个容量为2的数组,[0]代表总流量 ,[1]代表实际流量
	 * @throws IOException
	 */
	public static long[] transportChunkData(final InputStream inputStream,
			final OutputStream outputStream) throws IOException {
		/*总内容*/
		long contentSize = 0L;
		/*有效内容*/
		long actualSize = 0L;
		int[] result;
		while (true) {
			result = getChunkSizeFromInputStream(inputStream, outputStream);
			contentSize += result[0];
			int eof = result[1];
			if (eof == 0) {
				break;
			}
			actualSize += eof;
			readCRLF(inputStream);
		}
		
		long[] ls = {contentSize, actualSize};

		return ls;
	}
	
}

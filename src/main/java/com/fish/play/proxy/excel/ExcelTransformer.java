package com.fish.play.proxy.excel;

import com.fish.play.proxy.excel.entity.TemplateEntity;
import net.sf.jxls.exception.ParsePropertyException;
import net.sf.jxls.transformer.XLSTransformer;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
/**
 * 将log文件转换成excel文件
 * @author changliang
 *
 */
public class ExcelTransformer {
	
	
	public void transform(String templateFileName, String targetFileName, String destFilePath) {
		XLSTransformer transformer = new XLSTransformer();
		Map<String, Object> beans = generateBeans(targetFileName);
		try {
			transformer.transformXLS(getFilePath(templateFileName), beans, destFilePath);
		} catch (ParsePropertyException e) {
			e.printStackTrace();
		} catch (InvalidFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private String getFilePath(String relativeFileName) {
		URL url = this.getClass().getClassLoader().getResource(relativeFileName);
		return url.getFile();
	}
	
	private Map<String, Object> generateBeans(String relativeFileName) {
		Map<String, Object> beans = new HashMap<String, Object>();
		String srcFilePath = getFilePath(relativeFileName);
		BufferedReader reader = null;
		List<TemplateEntity> list = new ArrayList<TemplateEntity>();
		try {
			reader = new BufferedReader(new FileReader(srcFilePath));
			
			String readTemp;
			while ((readTemp = reader.readLine()) != null) {
				String[] array =  readTemp.split(",");
				list.add(generateOneBean(array));
			}
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		beans.put("result", list);
		
		return beans;
		
	}
	
	private TemplateEntity generateOneBean(String[] attrs) {
		TemplateEntity entity = new TemplateEntity();
		if (attrs.length < 11) {
			return entity;
		}
		
		entity.setStatus(attrs[1]);
		entity.setAddress(attrs[2]);
		entity.setMethod(attrs[3]);
		entity.setReqBytes(parseLong(attrs[4].trim()));
		entity.setReqHeaderBytes(parseLong(attrs[5].trim()));
		entity.setReqParamBytes(parseLong(attrs[6].trim()));
		entity.setResBytes(parseLong(attrs[7].trim()));
		entity.setResConBytes(parseLong(attrs[8].trim()));
		entity.setTotalBytes(parseLong(attrs[9].trim()));
		entity.setTimeMills(parseLong(attrs[10].trim()));
		
		if (attrs.length > 11) {
			entity.setFailMsg(attrs[11]);
		}
		
		return entity;
	}
	
	private long parseLong(String s) {
		long result = 0L;
		if (s == null || "".equals(s.trim())) {
			return result;
		}
		try {
			result = Long.parseLong(s);
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
		return result;
	}
	

}

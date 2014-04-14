package com.fish.play.proxy.excel;

/**
 * log转excel入口类
 * @author changliang
 *
 */
public class ExcelMain {

	public static void main(String[] args) throws Exception {
		ExcelTransformer excel = new ExcelTransformer();
		excel.transform("config/template.xlsx", "logs/proxy.log", "logs/proxy.xlsx");
	}
}

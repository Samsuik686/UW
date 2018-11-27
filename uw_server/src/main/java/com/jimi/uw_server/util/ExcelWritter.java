package com.jimi.uw_server.util;

import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.jfinal.plugin.activerecord.Record;

/**
 * 针对JFinal框架处理Excel表的类，可根据传入的Record集合对象填充excel表格数据
 * @author HardyYao
 * @createTime 2018年11月27日  上午11:42:31
 */

public class ExcelWritter extends ExcelHelper {

	/**
	 * 规定excel表格属于2007版之前还是之后
	 */
	public static ExcelWritter create(boolean isNewVersion) {
		return new ExcelWritter(isNewVersion);
	}

	protected ExcelWritter(boolean isNewVersion) {
		//判断格式
		if(isNewVersion){
			workbook = new XSSFWorkbook();
		}else {
			workbook = new HSSFWorkbook();
		}
		workbook.createSheet();
		init();
	}

	/**
	 * 根据传入的值生成Excel
	 * @param records 传入的集合对象
	 * @param title Excel名称
	 * @param field 对象的各个字段集合
	 * @param head 显示在Excel表里的字段名称集合
	 * @date 2018年10月16日 上午9:07:42
	 */
	public void fill(List<Record> records, String title, String[] field, String[] head){
		fill(headStyle, bodyStyle, records, 1, field, head);
		Sheet sheet = workbook.getSheetAt(currentSheetNum);
		if(sheet.getRow(1) == null){
			return;
		} 
		int lastCellNum = sheet.getRow(1).getLastCellNum();
		CellRangeAddress cra = new CellRangeAddress(0, 0, 0, lastCellNum - 1);
        //在sheet里增加合并单元格  
		sheet.addMergedRegion(cra);
        Row row = sheet.createRow(0);
        Cell cell = row.createCell(0);
        cell.setCellValue(title);
        //设置样式
        CellStyle style = workbook.createCellStyle();
        style.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		Font font = workbook.createFont();
		font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
		font.setFontName("Arial");
		font.setFontHeightInPoints((short) 16);
		style.setFont(font);
		cell.setCellStyle(style);
	}


	/**
	 * 根据实体类列表从某一行开始填写表格，使用自定义样式
	 * 需要在实体类的每个属性中注解@Excel，col属性代表列号，从0开始，head属性表示该字段的表头描述
	 * @throws IllegalAccessException 
	 * @throws IllegalArgumentException 
	 */
	public void fill(CellStyle headStyle, CellStyle bodyStyle, List<Record> records, int startRowNum, String[] field, String[] head) {
		if(records != null) {
			for (int i = 1; i <= records.size(); i++) {
				Record record = records.get(i - 1);
				for (int j = 0; j < head.length; j++) {
					//如果是第一行则填写表头
					if(i == startRowNum) {
						set(i, j, head[j], headStyle);
					}
					try {
						set(i + 1, j, record.get(field[j]) == null ? "" : record.get(field[j]).toString(), bodyStyle);
					}catch (IllegalArgumentException e2) {
						logger.error("调用ExcelHelper.fill()中field.get()方法时出错");
						e2.printStackTrace();
					}
				}				
			}
		}
	}

}

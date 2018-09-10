package com.legalminer.tools;

import com.legalminer.industry.classification.importation.demain.ClassificationExcel;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class OfficeTool {

    public static final OfficeTool newOne() { return new OfficeTool(); }

    public Workbook getWorkbook(String excelFilePath) throws IOException, InvalidFormatException {
        File excel = new File(excelFilePath);
        if (excelFilePath.endsWith(".xls")){
            FileInputStream fis = new FileInputStream(excel);
            return new HSSFWorkbook(fis);
        }else if (excelFilePath.endsWith(".xlsx")){
            return  new XSSFWorkbook(excel);
        }else {
            System.out.println("文件类型错误!");
            return null;
        }
    }

    public void readSheet(Sheet sheet, ExcelHandler handler) {
        if (null==sheet) { return; }

        String sheetName = sheet.getSheetName().trim();
        if (!handler.sheetValid(sheet)) {
            return;
        }

        List<ClassificationExcel> allClazz = new ArrayList<>();
        int lastRowIndex = sheet.getLastRowNum();
        for(int rIndex = 1; rIndex <= lastRowIndex; rIndex++) {   //遍历行
            Row row = sheet.getRow(rIndex);
            if (row == null) { continue; }
            if (!handler.rowValid(row)) { continue; }

            handler.processRow(row);
        }

        return;
    }

    public interface ExcelHandler <E> {

        boolean sheetValid(Sheet sheet);

        boolean rowValid(Row row);

        E processRow(Row row);

        List<E> getRowCache();

        ExcelHandler setRowCache(List<E> rowCache);
    }
}

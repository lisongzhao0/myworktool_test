package com.legalminer.industry.classification.importation;

import com.legalminer.industry.classification.importation.demain.Classification;
import com.legalminer.industry.classification.importation.demain.Company;
import com.legalminer.tools.PostgreSQLTool;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class IndustryImportation {

    private static final int DATA_MIN_ROW = 1;
    private static final int DATA_MAX_ROW = 101;


    public static void main(String[] args) {

    }


    public String readExcel(String filePath) throws IOException, InvalidFormatException {
        Workbook wb = getWorkbook(filePath);
        if (wb==null) { return null; }

        int sheetSize = wb.getNumberOfSheets();
        List<Classification> allClazz = new ArrayList<>();
        List<Company> allComp = new ArrayList<>();

        for (int i=0; i<sheetSize; i++) {
            Sheet sheet = wb.getSheetAt(i);
            allClazz.addAll(readClassificationSheet(sheet));
            allComp.addAll(readCompanySheet(sheet));
        }


    }


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

    public List<Classification> readClassificationSheet(Sheet sheet) {
        if (null==sheet) { return new ArrayList<>(); }

        String sheetName = sheet.getSheetName().trim();
        if (!"A股".equals(sheetName) && !"新三板".equals(sheetName)) {
            return new ArrayList<>();
        }

        List<Classification> allClazz = new ArrayList<>();
        int lastRowIndex = sheet.getLastRowNum();
        for(int rIndex = 1; rIndex <= lastRowIndex; rIndex++) {   //遍历行
            Row row = sheet.getRow(rIndex);
            if (row == null) { continue; }

            int firstCellIndex = row.getFirstCellNum();
            int lastCellIndex = row.getLastCellNum();
            Classification clazz = new Classification();
            for (int cIndex = firstCellIndex; cIndex < lastCellIndex; cIndex++) {   //遍历列
                Cell cell = row.getCell(cIndex);
                clazz.setMarket(sheetName);
                switch (cIndex-firstCellIndex) {
                    case 0:
                        clazz.setCode(cell.toString());
                        continue;
                    case 1:
                        clazz.setShortName(cell.toString());
                        continue;
                    case 2:
                        clazz.setFullName(cell.toString());
                        continue;
                    case 3:
                        clazz.setLevel01(cell.toString());
                        continue;
                    case 4:
                        clazz.setLevel02(cell.toString());
                        continue;
                    case 5:
                        clazz.setLevel03(cell.toString());
                        continue;
                    case 6:
                        clazz.setLevel04(cell.toString());
                        continue;

                }
            }
        }

        return allClazz;
    }

    public List<Company> readCompanySheet(Sheet sheet) {
        if (null==sheet) { return new ArrayList<>(); }

        String sheetName = sheet.getSheetName().trim();
        if ("A股".equals(sheetName) || "新三板".equals(sheetName)) {
            return new ArrayList<>();
        }

        List<Company> allComp = new ArrayList<>();
        for(int rIndex = DATA_MIN_ROW; rIndex <= DATA_MAX_ROW; rIndex++) {   //遍历行
            Row row = sheet.getRow(rIndex);
            if (row==null) { continue; }

            int firstCellIndex = row.getFirstCellNum();
            int lastCellIndex = row.getLastCellNum();
            Company clazz = new Company();
            for (int cIndex = firstCellIndex; cIndex < lastCellIndex; cIndex++) {   //遍历列
                Cell cell = row.getCell(cIndex);
                clazz.setSheetName(sheetName);
                switch (cIndex-firstCellIndex) {
                    case 0:
                        clazz.setFullName(cell.toString());
                        continue;
                    case 1:
                        clazz.setCaseSize(cell.toString());
                        continue;
                    case 2:
                        clazz.setTurnover(cell.toString());
                        continue;
                }
            }
        }

        return allComp;
    }

    public String[] constructSQL(List<Classification> allClazz, List<Company> allComp) {
        String sqlClassif = constructClassifSQL(allClazz, allComp);
        String sqlCompany = constructCompSQL(allClazz, allComp);
        String sqlClassifCompRel = constructClassifCompRelSQL(allClazz, allComp);

        return new String[]{ sqlClassif, sqlCompany, sqlClassifCompRel };
    }


    public String constructClassifSQL(List<Classification> allClazz, List<Company> allComp) {
        Set<String> allCompanyFullName = new HashSet<>();
        for (Company comp : allComp) {
            allCompanyFullName.add(comp.getFullName());
        }

        List<Classification> existClassifi = new ArrayList<>();
        for (Classification clazz : allClazz) {
            if (!allCompanyFullName.contains(clazz.getFullName())) {
                continue;
            }

            existClassifi.add(clazz);
        }


    }

    public String constructCompSQL(List<Classification> allClazz, List<Company> allComp) {
        //jdbc:postgresql://192.168.1.129:5432/legalminer
        //usr:  postgres
        //pwd:  legallohas
        StringBuilder sqlComp = new StringBuilder();
        for (Company comp : allComp) {
            sqlComp.append("in")
        }
    }

    public String constructClassifCompRelSQL(List<Classification> allClazz, List<Company> allComp) {

    }

}

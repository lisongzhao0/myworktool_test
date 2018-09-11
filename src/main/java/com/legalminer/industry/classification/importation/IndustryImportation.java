package com.legalminer.industry.classification.importation;

import com.legalminer.industry.classification.importation.demain.ClassiNode;
import com.legalminer.industry.classification.importation.demain.ClassificationExcel;
import com.legalminer.industry.classification.importation.demain.Company;
import com.legalminer.tools.OfficeTool;
import com.legalminer.tools.PinyinTool;
import com.legalminer.tools.TreeTool;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFCell;

import java.io.IOException;
import java.util.*;

public class IndustryImportation {

    public static void main(String[] args) throws IOException, InvalidFormatException, BadHanyuPinyinOutputFormatCombination {
        Level1Start.clear();
        classifRoot.removeAllChildren();

        new IndustryImportation().readExcel(excelFilePath);
    }

    private static final Set<String> Level1Start = new HashSet<>();
    private static final String ClassiType = "gics";
    private static final String DataColor = "FFCCFFFF";
    private static final String excelFilePath = "D:\\work\\资料\\BRM1.0（理脉行业）行业分类（GICS版）_wyz_count_1532595979862.xlsx";

    private static ClassiNode classifRoot = new ClassiNode();
    private static OfficeTool officeTool  = OfficeTool.newOne();


    public String readExcel(String filePath) throws IOException, InvalidFormatException, BadHanyuPinyinOutputFormatCombination {
        Workbook wb = officeTool.getWorkbook(filePath);
        if (wb==null) { return null; }

        int sheetSize = wb.getNumberOfSheets();
        List<ClassificationExcel> allClazz = new ArrayList<>();
        List<Company> allComp = new ArrayList<>();

        for (int i=0; i<sheetSize; i++) {
            Sheet sheet = wb.getSheetAt(i);
            allClazz.addAll(readClassificationSheet(sheet));
            allComp.addAll(readCompanySheet(sheet));
        }

        String[] sqls = constructSQL(allClazz, allComp);

        return null;

    }

    public List<ClassificationExcel> readClassificationSheet(Sheet sheet) {
        if (null==sheet) { return new ArrayList<>(); }
        List<ClassificationExcel> allClazz = new ArrayList<>();
        officeTool.readSheet(sheet, new ExcelHandler().setRowCache(allClazz).setClassification(true));
        return allClazz;
    }

    public List<Company> readCompanySheet(Sheet sheet) {
        if (null==sheet) { return new ArrayList<>(); }
        List<Company> allComp = new ArrayList<>();
        officeTool.readSheet(sheet, new ExcelHandler().setRowCache(allComp).setClassification(false));
        return allComp;
    }

    public String[] constructSQL(List<ClassificationExcel> allClazz, List<Company> allComp) throws BadHanyuPinyinOutputFormatCombination {
        List<ClassificationExcel> validClassif = getValidClassif(allClazz, allComp);
        Set<String> existed = new HashSet<>();
        for (ClassificationExcel clazz : validClassif) {
            if (existed.contains(clazz.level1234())) { continue; }
            existed.add(clazz.level1234());
        }

        // 创建分类树
        TreeTool treeTool = TreeTool.newOne();
        classifRoot = treeTool.createTree(classifRoot, new ArrayList<>(existed));
        treeTool.processLeaf(classifRoot, new TreeHandler()/* 为分类树设置 uuid */);

        // 构建分类 SQL
        List<ClassiNode> leaves = treeTool.getAllLeaf(classifRoot);
        final StringBuilder classificationSQL = new StringBuilder();
        treeTool.processNode(classifRoot, new TreeTool.TreeHanlder() {
            @Override public ClassiNode createNode(ClassiNode rootNode, String[] treePath) { return null; }
            @Override public void processLeaf(ClassiNode leaf) { }
            @Override public void processNode(ClassiNode node) {
                if (node.getParent()==null) {return;}
                if (classificationSQL.length()==0) {
                    classificationSQL.append("insert into er_industry_classification(\"id\", \"create_date\", \"level\", \"p_id\", \"product\", \"type\") VALUES \n");
                }
                classificationSQL.append("('").append(node.getUuid()).append("'")
                        .append(",").append("CURRENT_TIMESTAMP")
                        .append(",").append(node.getDepth())
                        .append(",'").append(node.parentIsRoot() ? 0 : node.getParent().getUuid()).append("'")
                        .append(",'").append(node.getLevel()).append("'")
                        .append(",'").append(ClassiType).append("'")
                        .append("),\n");
            }
        });
        classificationSQL.delete(classificationSQL.length()-2, classificationSQL.length()).append(";");

        // 分类下的公司
        Map<String, List<ClassificationExcel>> level234_classiExcel = new HashMap<>();
        for (ClassificationExcel clazz : validClassif) {
            List<ClassificationExcel> classiExcelSet = level234_classiExcel.get(clazz.level1234());
            if (null==classiExcelSet) {
                classiExcelSet = new ArrayList<>();
                level234_classiExcel.put(clazz.level1234(), classiExcelSet);
            }
            classiExcelSet.add(clazz);
        }

        // 构建 company 和 company_classification 的 SQL
        final StringBuilder classiCompanySQL = new StringBuilder();
        final StringBuilder classiCompanyRelSQL = new StringBuilder();
        for (ClassiNode leaf : leaves) {
            List<ClassificationExcel> classiCompanies = level234_classiExcel.get(leaf.getLevel1234());
            for (ClassificationExcel classiComp : classiCompanies) {
                classiComp.setUuid(UUID.randomUUID().toString().replace("-", ""));

                if (classiCompanySQL.length()==0) {
                    classiCompanySQL.append("insert into er_classification_company(\"id\", \"create_date\", \"data_id\", \"full_name\", \"income\", \"income_percent\", \"stock_code\", \"stock_name\") VALUES \n");
                }
                classiCompanySQL.append("('").append(classiComp.getUuid()).append("'")
                        .append(",").append("CURRENT_TIMESTAMP")
                        .append(",'").append(classiComp.getFullName()).append("'")
                        .append(",'").append(classiComp.getFullName()).append("'")
                        .append(",").append(0).append("")
                        .append(",").append(0).append("")
                        .append(",'").append(classiComp.getCode()).append("'")
                        .append(",'").append(classiComp.getShortName()).append("'")
                        .append("),\n");

                if (classiCompanyRelSQL.length()==0) {
                    classiCompanyRelSQL.append("insert into er_classification_company(\"industry_id\", \"company_id\") VALUES \n");
                }
                classiCompanyRelSQL.append("('").append(leaf.getUuid()).append("'")
                        .append("('").append(classiComp.getUuid()).append("'")
                        .append("),\n");
            }
        }
        classiCompanySQL.delete(classiCompanySQL.length()-2, classiCompanySQL.length()).append(";");
        classiCompanyRelSQL.delete(classiCompanyRelSQL.length()-2, classiCompanyRelSQL.length()).append(";");

        System.out.println(classificationSQL.toString());
        System.out.println(classiCompanySQL.toString());
        System.out.println(classiCompanyRelSQL.toString());
        return new String[]{classificationSQL.toString(), classiCompanySQL.toString(), classiCompanyRelSQL.toString() };
    }

    public List<ClassificationExcel> getValidClassif(List<ClassificationExcel> allClazz, List<Company> allComp) {
        HashMap<String, Company> allCompanyFullName = new HashMap<>();
        for (Company comp : allComp) {
            allCompanyFullName.put(comp.getFullName(), comp);
        }

        List<ClassificationExcel> existClassifi = new ArrayList<>();
        for (ClassificationExcel clazz : allClazz) {
            if (!allCompanyFullName.containsKey(clazz.getFullName())) {
                continue;
            }
            clazz.setCompany(allCompanyFullName.get(clazz.getFullName()));

            existClassifi.add(clazz);
            clazz.setUuid(UUID.randomUUID().toString().replace("-", ""));
        }

        Collections.sort(existClassifi, new Comparator<ClassificationExcel>() {
            @Override
            public int compare(ClassificationExcel o1, ClassificationExcel o2) {
                if (o1==null && o2==null) { return 0; }
                else if (o1!=null && o2==null) { return 1;}
                else if (o1==null && o2!=null) { return -1; }
                else {
                    if (!o1.getLevel01().equals(o2.getLevel01())) {
                        return o1.getLevel01().compareTo(o2.getLevel01());
                    }
                    else if (!o1.getLevel02().equals(o2.getLevel02())) {
                        return o1.getLevel02().compareTo(o2.getLevel02());
                    }
                    else if (!o1.getLevel03().equals(o2.getLevel03())) {
                        return o1.getLevel03().compareTo(o2.getLevel03());
                    }
                    else if (null!=o1.getLevel04() && !o1.getLevel04().equals(o2.getLevel04())) {
                        return o1.getLevel04().compareTo(o2.getLevel04());
                    }
                }
                return 0;
            }
        });

        return existClassifi;
    }

    public static class TreeHandler implements TreeTool.TreeHanlder {

        private static final PinyinTool pinyinTool = PinyinTool.newOne();

        @Override
        public void processNode(ClassiNode node) {
            return;
        }

        @Override
        public ClassiNode createNode(ClassiNode rootNode, String[] treePath) {
            return null;
        }

        @Override
        public void processLeaf(ClassiNode leaf) {
            // 设置 level 04 03 02 01 的 uuid
            StringBuilder uuidBuf = new StringBuilder();
            ClassiNode next = leaf;
            while(null!=next.getParent()) {
                if (next.getParent().getParent()==null/* current is level01 */) {
                    try {
                        uuidBuf.insert(0, pinyinTool.toPinYin(next.getLevel(), "", true) + " ");
                    } catch (BadHanyuPinyinOutputFormatCombination bhpofc) {
                        new RuntimeException(bhpofc.getMessage(), bhpofc);
                    }
                    break;
                }
                else
                    uuidBuf.insert(0, String.format("%02d ", next.getParent().getIndex(next) + 1));
                next = next.getParent();
            }
            String uuid = uuidBuf.toString().replace(" ", "");
            next = leaf;
            while(null!=next.getParent()) {
                if (next.getParent().getParent()==null/* current is level01 */) {
                    next.setUuid(uuid);
                    break;
                }
                else {
                    next.setUuid(uuid);
                    uuid = uuid.substring(0, uuid.length()-2);
                }
                next = next.getParent();
            }
            System.out.println(leaf.getUuid() + "\t" + leaf.getLevel1234());
        }
    }

    public static class ExcelHandler implements OfficeTool.ExcelHandler {

        protected boolean isClassification;
        protected String  sheetName = null;
        protected List    rowCache  = null;

        public ExcelHandler(){}

        public boolean isClassification() {
            return isClassification;
        }

        public ExcelHandler setClassification(boolean isClassification) {
            this.isClassification = isClassification;
            return this;
        }

        @Override
        public boolean sheetValid(Sheet sheet) {
            sheetName = sheet.getSheetName().trim();
            boolean valid = "A股".equals(sheetName) || "新三板".equals(sheetName);
            return isClassification ? valid : !valid;
        }

        @Override
        public boolean rowValid(Row row) {
            if (isClassification) {
                return row != null;
            }
            else {
                if (row!=null) {
                    Cell colorCell = row.getCell(0);
                    if (null==((XSSFCell)colorCell).getCellStyle()
                     || null==((XSSFCell)colorCell).getCellStyle().getFillForegroundXSSFColor()
                     || null==((XSSFCell)colorCell).getCellStyle().getFillForegroundXSSFColor().getARGBHex()
                     || !DataColor.equals(((XSSFCell)colorCell).getCellStyle().getFillForegroundXSSFColor().getARGBHex())) {
                        return false;
                    }
                    return true;
                }
            }
            return false;
        }

        @Override
        public Object processRow(Row row) {
            return isClassification ? processClassification(row) : processCompany(row);
        }

        @Override
        public List getRowCache() {
            return rowCache;
        }

        @Override
        public ExcelHandler setRowCache(List rowCache) {
            this.rowCache = rowCache;
            return this;
        }

        private Object processCompany(Row row) {
            int firstCellIndex = row.getFirstCellNum();
            int lastCellIndex = row.getLastCellNum();
            Company comp = new Company();
            for (int cIndex = firstCellIndex; cIndex < lastCellIndex; cIndex++) {   //遍历列
                Cell cell = row.getCell(cIndex);
                comp.setSheetName(sheetName);
                switch (cIndex-firstCellIndex) {
                    case 0:
                        comp.setFullName(cell.toString());
                        continue;
                    case 1:
                        comp.setCaseSize(cell.toString());
                        continue;
                    case 2:
                        comp.setTurnover(cell.toString());
                        continue;
                    default:
                        continue;
                }
            }
            getRowCache().add(comp);
            return comp;
        }

        private Object processClassification(Row row) {
            int firstCellIndex = row.getFirstCellNum();
            int lastCellIndex = row.getLastCellNum();
            ClassificationExcel clazz = new ClassificationExcel();
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
                    default:
                        continue;

                }
            }
            getRowCache().add(clazz);
            return clazz;
        }
    }
}

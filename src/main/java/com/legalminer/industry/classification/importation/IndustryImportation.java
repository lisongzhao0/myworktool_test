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

        classifRoot = ClassiNode.createTree(classifRoot, new ArrayList<>(existed));
        TreeTool treeTool = TreeTool.newOne();
        treeTool.getProcessLeaf(classifRoot, new TreeHandler());

        return new String[]{  };
    }

    public List<ClassificationExcel> getValidClassif(List<ClassificationExcel> allClazz, List<Company> allComp) {
        Set<String> allCompanyFullName = new HashSet<>();
        for (Company comp : allComp) {
            allCompanyFullName.add(comp.getFullName());
        }

        List<ClassificationExcel> existClassifi = new ArrayList<>();
        for (ClassificationExcel clazz : allClazz) {
            if (!allCompanyFullName.contains(clazz.getFullName())) {
                continue;
            }

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

    public ClassiNode setUuidForClassiNode(ClassiNode node) throws BadHanyuPinyinOutputFormatCombination {
        List<ClassiNode> allLeaf = ClassiNode.getAllLeaf(node);
        System.out.println(allLeaf.size());
        PinyinTool pinyinTool = PinyinTool.newOne();

        // 设置 level 01 的 uuid
        Enumeration<ClassiNode> allLevel01 = node.children();
        while (allLevel01.hasMoreElements()) {
            ClassiNode level01 = allLevel01.nextElement();
            String level01Uuid = pinyinTool.toPinYin(level01.getLevel(), "", true);
            if (Level1Start.contains(level01Uuid)) {
                throw new RuntimeException("Level01 uuid is exist, uuid="+level01Uuid);
            }
            Level1Start.add(level01Uuid);
            level01.setUuid(level01Uuid);
        }

        // 设置 level 04 03 02 的 uuid
        for (ClassiNode leaf : allLeaf) {
            StringBuilder uuidBuf = new StringBuilder();
            ClassiNode next = leaf;
            while(null!=next.getParent()) {
                if (null!=next.getUuid()) {
                    uuidBuf.insert(0, next.getUuid() + " ");
                    break;
                }
                else
                    uuidBuf.insert(0, String.format("%02d ", next.getParent().getIndex(next) + 1));
                next = next.getParent();
            }
            String uuid = uuidBuf.toString().replace(" ", "");
            next = leaf;
            while(null!=next.getParent()) {
                if (null!=next.getUuid()) {
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
        return node;
    }

    public String constructClassifSQL(List<ClassificationExcel> allClazz) {
        StringBuilder sqlComp = new StringBuilder();
        for (ClassificationExcel clazz : allClazz) {

            sqlComp.append("insert into ");
        }
        return null;
    }

//    public String constructCompSQL(List<ClassificationExcel> allClazz, List<Company> allComp) {
//        //jdbc:postgresql://192.168.1.129:5432/legalminer
//        //usr:  postgres
//        //pwd:  legallohas
//        StringBuilder sqlComp = new StringBuilder();
//        for (Company comp : allComp) {
//            sqlComp.append("in")
//        }
//    }

//    public String constructClassifCompRelSQL(List<ClassificationExcel> allClazz, List<Company> allComp) {
//
//    }

    public static class TreeHandler implements TreeTool.TreeHanlder {

        private static final PinyinTool pinyinTool = PinyinTool.newOne();

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

package com.legalminer.industry.classification.importation.demain;

public class Company {
    private String sheetName;
    private String fullName;
    private String caseSize;
    private String turnover;

    public String getSheetName() {
        return sheetName;
    }

    public String getFullName() {
        return fullName;
    }

    public String getCaseSize() {
        return caseSize;
    }

    public String getTurnover() {
        return turnover;
    }

    public Company setSheetName(String sheetName) {
        this.sheetName = sheetName;
        return this;
    }

    public Company setFullName(String fullName) {
        this.fullName = fullName;
        return this;
    }

    public Company setCaseSize(String caseSize) {
        this.caseSize = caseSize;
        return this;
    }

    public Company setTurnover(String turnover) {
        this.turnover = turnover;
        return this;
    }
}

package com.legalminer.industry.classification.importation.demain;

public class ClassificationExcel {
    private String market;
    private String code;
    private String shortName;
    private String fullName;
    private String level01;
    private String level02;
    private String level03;
    private String level04;
    private String uuid;
    private Company company;

    public String getMarket() {
        return market;
    }

    public String getCode() {
        return code;
    }

    public String getShortName() {
        return shortName;
    }

    public String getFullName() {
        return fullName;
    }

    public String getLevel01() {
        return level01;
    }

    public String getLevel02() {
        return level02;
    }

    public String getLevel03() {
        return level03;
    }

    public String getLevel04() {
        return level04;
    }

    public String getUuid() {
        return uuid;
    }

    public String level1234() {
        return level01 +
                (null!=level02 ? " | " + level02 : "") +
                (null!=level03 ? " | " + level03 : "") +
                (null!=level04 ? " | " + level04 : "");
    }

    public Company getCompany() {
        return company;
    }

    public String toString() {
        return level1234();
    }

    public ClassificationExcel setMarket(String market) {
        this.market = market;
        return this;
    }

    public ClassificationExcel setCode(String code) {
        this.code = code;
        return this;
    }

    public ClassificationExcel setShortName(String shortName) {
        this.shortName = shortName;
        return this;
    }

    public ClassificationExcel setFullName(String fullName) {
        this.fullName = fullName;
        return this;
    }

    public ClassificationExcel setLevel01(String level01) {
        this.level01 = level01;
        return this;
    }

    public ClassificationExcel setLevel02(String level02) {
        this.level02 = level02;
        return this;
    }

    public ClassificationExcel setLevel03(String level03) {
        this.level03 = level03;
        return this;
    }

    public ClassificationExcel setLevel04(String level04) {
        this.level04 = level04;
        return this;
    }

    public ClassificationExcel setUuid(String uuid) {
        this.uuid = uuid;
        return this;
    }

    public ClassificationExcel setCompany(Company company) {
        this.company = company;
        return this;
    }
}

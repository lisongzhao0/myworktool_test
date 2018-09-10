package com.legalminer.tools;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;


/**
 * 汉字转化为拼音的工具类
 *
 */
public class PinyinTool {
    HanyuPinyinOutputFormat format = null;
    public static enum Type {
        UPPERCASE,              //全部大写
        LOWERCASE,              //全部小写
        FIRSTUPPER              //首字母大写
    }

    public static final PinyinTool newOne() { return new PinyinTool(); }
    public static final PinyinTool newOne(HanyuPinyinOutputFormat format) {
        PinyinTool newOne = new PinyinTool();
        newOne.format = format;
        return newOne;
    }

    private PinyinTool(){
        format = new HanyuPinyinOutputFormat();
        format.setCaseType(HanyuPinyinCaseType.UPPERCASE);
        format.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
    }

    public String toPinYin(String str) throws BadHanyuPinyinOutputFormatCombination{
        return toPinYin(str, "", Type.UPPERCASE, false);
    }

    public String toPinYin(String str,String spera) throws BadHanyuPinyinOutputFormatCombination{
        return toPinYin(str, spera, Type.UPPERCASE, false);
    }

    public String toPinYin(String str,String spera, boolean justFirst) throws BadHanyuPinyinOutputFormatCombination{
        return toPinYin(str, spera, Type.UPPERCASE, justFirst);
    }

    /**
     * 将str转换成拼音，如果不是汉字或者没有对应的拼音，则不作转换
     * 如： 明天 转换成 MINGTIAN
     * @param str：要转化的汉字
     * @param spera：转化结果的分割符
     * @return
     * @throws BadHanyuPinyinOutputFormatCombination
     */
    public String toPinYin(String str, String spera, Type type, boolean justFirst) throws BadHanyuPinyinOutputFormatCombination {
        if(str == null || str.trim().length()==0)
            return "";
        if(type == Type.UPPERCASE)
            format.setCaseType(HanyuPinyinCaseType.UPPERCASE);
        else
            format.setCaseType(HanyuPinyinCaseType.LOWERCASE);

        String py = "";
        String temp = "";
        String[] t;
        for(int i=0;i<str.length();i++){
            char c = str.charAt(i);
            if((int)c <= 128)
                py += c;
            else{
                t = PinyinHelper.toHanyuPinyinStringArray(c, format);
                if(t == null)
                    py += c;
                else{
                    temp = t[0];
                    if(type == Type.FIRSTUPPER)
                        temp = t[0].toUpperCase().charAt(0)+temp.substring(1);
                    if (justFirst) {
                        py += temp.charAt(0) + (i == str.length() - 1 ? "" : spera);
                    }
                    else {
                        py += temp + (i == str.length() - 1 ? "" : spera);
                    }
                }
            }
        }
        return py.trim();
    }
}

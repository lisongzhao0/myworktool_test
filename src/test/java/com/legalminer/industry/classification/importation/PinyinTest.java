package com.legalminer.industry.classification.importation;

import com.legalminer.tools.PinyinTool;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;
import org.junit.Test;

public class PinyinTest {

    @Test
    public void test() throws BadHanyuPinyinOutputFormatCombination {
        PinyinTool one = PinyinTool.newOne();
        System.out.println(one.toPinYin("饿我去热无法", "", true));
    }
}

package edu.cs4730.wearappvoice.utils;

import net.sourceforge.pinyin4j.PinyinHelper;

public class PinyinComparator {

    // 将汉字转换为拼音
    public static String convertToPinyin(String text) {
        StringBuilder pinyinBuilder = new StringBuilder();
        char[] characters = text.toCharArray();

        for (char character : characters) {
            // 使用 Pinyin4j 将汉字转换为拼音
            String[] pinyinArray = PinyinHelper.toHanyuPinyinStringArray(character);
            if (pinyinArray != null && pinyinArray.length > 0) {
                pinyinBuilder.append(pinyinArray[0]).append(" "); // 获取第一个拼音
            } else {
                // 如果没有拼音（如标点符号），则保持原字符
                pinyinBuilder.append(character).append(" ");
            }
        }
        return pinyinBuilder.toString().trim(); // 去掉最后的空格
    }

    // 比较两个字符串的拼音
    public static boolean containsPinyin(String source, String target) {
//        String sourcePinyin = convertToPinyin(source);
        String targetPinyin = convertToPinyin(target);
        return source.contains(targetPinyin);
    }
}

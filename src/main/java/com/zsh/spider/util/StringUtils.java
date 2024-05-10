package com.zsh.spider.util;

/**
 * StringUtils
 *
 * @author zsh
 * @version 1.0.0
 * @date 2024/03/16 14:45
 */
public class StringUtils {

    /**
     * 将驼峰命名转换为下划线命名
     * @param str 驼峰命名字符串
     * @return 下划线命名字符串
     */
    public static String camelToUnderline(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (Character.isUpperCase(c)) {
                sb.append("_");
                sb.append(Character.toLowerCase(c));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * 下划线转驼峰工具
     */
    public static String underlineToCamel(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        int len = str.length();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < len; i++) {
            char c = str.charAt(i);
            if (c == 95) {
                ++i;
                if (i < len) {
                    sb.append(Character.toUpperCase(str.charAt(i)));
                }
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}
package com.joe.utils;

import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Create by joe on 2018/12/18
 * <p>
 * 此类语法同 SimpleDateFormat
 * h = 小时
 * m = 分钟
 * s = 秒
 * S = 毫秒
 * 此类非线程安全
 */
public class SimpleTimeFormat {

    /**
     * 原始pattern
     */
    transient private String pattern;

    /**
     * 编译后的pattern
     */
    transient private char[] compiledPattern;

    /**
     * ascii字符标签
     */
    private static final int TAG_ASCII_CODE = 100;

    /**
     * 非 ascii 字符标签
     */
    private static final int TAG_QUOTE_CODE = 101;

    /**
     * numberFormat 对象
     */
    private DecimalFormat numberFormat = new DecimalFormat("##");


    public SimpleTimeFormat() {
        this("hh:mm:ss");
    }

    public SimpleTimeFormat(String pattern) {
        if (pattern == null || pattern.length() == 0) {
            throw new NullPointerException("+++++>> The pattern must not be null!");
        }
        applyPattern(pattern);
    }

    /**
     * 将时长、时间戳（毫秒），转化为指定格式
     */
    public String format(Long duration) {
        return format(duration, new StringBuilder()).toString();
    }

    public String format(int duration) {
        return format((long) duration);
    }

    /**
     * 从字符串中解析时间戳
     */
    public Long parse(String source) {
        return parse(source, new ParsePosition(0));
    }

    /**
     * 应用指定pattern
     */
    public void applyPattern(String pattern) {
        this.compiledPattern = compile(pattern);
        this.pattern = pattern;
    }

    /**
     * 时间格式编译
     */
    private char[] compile(String pattern) {
        int length = pattern.length();
        StringBuilder compiledBuffer = new StringBuilder(length * 2);
        int count = 0;
        int lastTag = -1;

        for (int i = 0; i < length; i++) {
            char c = pattern.charAt(i);
            // 特殊字符处理（特殊字符使用引号括起来）
            if (c == '\'') {
                if (count != 0) {
                    compiledBuffer.append((char) (lastTag << 8 | count));
                    lastTag = -1;
                    count = 0;
                }
                int j;
                for (j = ++i; j < length; j++) {
                    char d = pattern.charAt(j);
                    if (d == '\'') {
                        break;
                    }
                }
                compiledBuffer.append((char) (TAG_QUOTE_CODE << 8 | (j - i)));
                for (; i < j; i++) {
                    compiledBuffer.append(pattern.charAt(i));
                }
                continue;
            }
            // 分隔符处理
            if (!(c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z')) {
                if (count != 0) {
                    compiledBuffer.append((char) (lastTag << 8 | count));
                    lastTag = -1;
                    count = 0;
                }
                if (c < 128) {
                    compiledBuffer.append((char) (TAG_ASCII_CODE << 8 | c));
                } else {
                    // 非 ascii 字符处理
                    int j;
                    for (j = i + 1; j < length; j++) {
                        char d = pattern.charAt(j);
                        if (d == '\'' || (d >= 'a' && d <= 'z' || d >= 'A' && d <= 'Z')) {
                            break;
                        }
                    }
                    compiledBuffer.append((char) (TAG_QUOTE_CODE << 8 | (j - i)));
                    for (; i < j; i++) {
                        compiledBuffer.append(pattern.charAt(i));
                    }
                    i--;
                }
                continue;
            }
            // 非分隔符处理
            int tag;
            if ((tag = TimeFormatSymBols.patternChars.indexOf(c)) == -1) {
                throw new IllegalArgumentException(
                        MessageFormat.format("Illegal pattern character -> {0}", c));
            }
            if (lastTag == -1 || lastTag == tag) {
                lastTag = tag;
                count++;
                continue;
            }
            compiledBuffer.append((char) (tag << 8 | count));
            lastTag = tag;
            count = 1;
        }

        if (count != 0) {
            compiledBuffer.append((char) (lastTag << 8 | count));
        }

        int bufLen = compiledBuffer.length();
        char[] r = new char[bufLen];
        compiledBuffer.getChars(0, bufLen, r, 0);
        return r;
    }

    /**
     * 格式化传入的毫秒时间
     */
    private StringBuilder format(Long duration, StringBuilder resultBuffer) {
        Long[] timeCache = getTimeCache(duration);

        for (int i = 0; i < compiledPattern.length; ) {
            int tag = compiledPattern[i] >>> 8;
            int count = compiledPattern[i++] & 0xff;

            switch (tag) {
                case TAG_ASCII_CODE:
                    resultBuffer.append((char) count);
                    break;
                case TAG_QUOTE_CODE:
                    resultBuffer.append(compiledPattern, i, count);
                    i += count;
                    break;
                default:
                    resultBuffer.append(timeCache[tag]);
                    break;
            }
        }
        return resultBuffer;
    }

    /**
     * 解析字符串时间
     */
    private Long parse(String source, ParsePosition position) {
        long result = 0L;

        if (this.pattern.contains(".")) {
            source = source.replace(".", ",");
        }

        for (int i = 0; i < compiledPattern.length; i++) {
            int tag = compiledPattern[i] >>> 8;
            int count = compiledPattern[i] & 0xff;

            switch (tag) {
                case TAG_ASCII_CODE:
                    position.setIndex(position.getIndex() + 1);
                    break;
                case TAG_QUOTE_CODE:
                    position.setIndex(position.getIndex() + count);
                    i += count;
                    break;
                default:
                    // 处理数值前可能出现的空格和制表符
                    while (true) {
                        char c = source.charAt(position.getIndex());
                        if (c != ' ' && c != '\t') {
                            break;
                        }
                        position.setIndex(position.getIndex() + 1);
                    }
                    // 前瞻, 处理真实数字位数与pattern中的位数不匹配
                    while (true) {
                        int nextCharIndex = position.getIndex() + count;
                        char c = nextCharIndex >= source.length() ? 'n' : source.charAt(nextCharIndex);
                        if (!Character.isDigit(c)) {
                            if (nextCharIndex > source.length()) {
                                count--;
                                continue;
                            }
                            break;
                        }
                        count++;
                    }
                    Number number = numberFormat.parse(source.substring(0, position.getIndex() + count), position);
                    result += (TimeFormatSymBols.UNITS_MILLS[tag] * number.doubleValue());
                    break;
            }

        }
        return result;
    }

    private Long[] getTimeCache(Long duration) {
        Long[] results = new Long[TimeFormatSymBols.patternChars.length()];
        List<Long> units = new ArrayList<>();

        // 时间格式提取
        for (char c : compiledPattern) {
            if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')) {
                continue;
            }
            int tag = c >>> 8;
            if (tag < TimeFormatSymBols.patternChars.length()) {
                units.add((long) tag);
            }
        }
        Collections.sort(units);

        for (Long tag : units) {
            Long value = duration / TimeFormatSymBols.UNITS_MILLS[tag.intValue()];
            duration = duration % TimeFormatSymBols.UNITS_MILLS[tag.intValue()];
            results[tag.intValue()] = value;
        }
        return results;
    }

    private static class TimeFormatSymBols {
        /**
         * 时间格式通配符
         * <p>
         * h = 小时
         * m = 分钟
         * s = 秒
         * S = 毫秒
         */
        static final String patternChars = "hmsS";

        /**
         * 单位毫秒数
         */
        static final long[] UNITS_MILLS = {60 * 60 * 1000, 60 * 1000, 1000, 1};
    }

}

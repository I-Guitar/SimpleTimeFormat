package com.joe.utils;

import org.junit.jupiter.api.Test;

class SimpleTimeFormatTest {

    /**
     * 时间格式化测试
     */
    @Test
    void formatTest() {
        // 初始化可通过构造方法传入指定 pattern
        SimpleTimeFormat simpleTimeFormat = new SimpleTimeFormat("hh小时mm分钟ss");
        // 根据 pattern 将指定字符串解析为时间戳
        Long timestamp = simpleTimeFormat.parse("5小时4分钟23秒");
        // 也可手动修改 pattern
        simpleTimeFormat.applyPattern("ss:mm:hh");
        // 根据 pattern 格式化时间戳
        String format = simpleTimeFormat.format(timestamp);
    }

}
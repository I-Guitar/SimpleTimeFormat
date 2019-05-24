package com.joe.utils;

import org.junit.jupiter.api.Test;

class SimpleTimeFormatTest {

    /**
     * 时间格式化测试
     */
    @Test
    void formatTest() {

        SimpleTimeFormat simpleTimeFormat = new SimpleTimeFormat("hh小时mm分钟ss");

        Long timestamp = simpleTimeFormat.parse("5小时4分钟23秒");

        simpleTimeFormat.applyPattern("ss:mm:hh");

        String format = simpleTimeFormat.format(timestamp);

    }

}
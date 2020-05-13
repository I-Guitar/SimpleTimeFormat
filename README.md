# SimpleTimeFormat

**此仓库已合并到[JUtils](https://github.com/i-guitar/JUtils)中，此处将不再维护**

> 模仿SimpleDateFormat写的时间格式化工具类，作为其补充，使用方式完全一致

* 使用demo见 [com.joe.utils.SimpleTimeFormatTest](https://github.com/I-Guitar/SimpleTimeFormat/blob/master/src/test/java/com/joe/utils/SimpleTimeFormatTest.java)



#### demo说明

```java
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
```


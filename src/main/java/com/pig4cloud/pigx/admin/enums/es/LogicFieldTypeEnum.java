package com.pig4cloud.pigx.admin.enums.es;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 逻辑字段类型
 */
@Getter
@AllArgsConstructor
public enum LogicFieldTypeEnum {

    STRING("string", "字符串"),
    NUMBER("number", "数字"),
    DATE("date", "日期/时间"),
    BOOLEAN("boolean", "布尔"),
    OBJECT("object", "对象"),
    ARRAY("array", "数组");

    private final String code;
    private final String name;

    public static LogicFieldTypeEnum of(String code) {
        if (code == null) { return null; }
        for (LogicFieldTypeEnum e : values()) {
            if (e.code.equalsIgnoreCase(code)) {
                return e;
            }
        }
        return null;
    }
}

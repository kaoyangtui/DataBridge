package com.pig4cloud.pigx.admin.enums.es;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * ES 字段类型
 */
@Getter
@AllArgsConstructor
public enum EsFieldTypeEnum {

    KEYWORD("keyword", "keyword"),
    TEXT("text", "text"),
    INTEGER("integer", "integer"),
    LONG("long", "long"),
    DOUBLE("double", "double"),
    FLOAT("float", "float"),
    DATE("date", "date"),
    BOOLEAN("boolean", "boolean");

    private final String code;
    private final String name;

    public static EsFieldTypeEnum of(String code) {
        if (code == null) { return null; }
        for (EsFieldTypeEnum e : values()) {
            if (e.code.equalsIgnoreCase(code)) {
                return e;
            }
        }
        return null;
    }
}

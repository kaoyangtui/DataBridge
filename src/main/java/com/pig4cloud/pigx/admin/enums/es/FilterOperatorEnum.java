package com.pig4cloud.pigx.admin.enums.es;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 查询操作类型，对应 ES 查询方式
 */
@Getter
@AllArgsConstructor
public enum FilterOperatorEnum {

    TERM("term", "精确匹配"),
    TERMS("terms", "多值精确匹配"),
    MATCH("match", "分词匹配"),
    MATCH_PHRASE("match_phrase", "短语匹配"),
    RANGE("range", "范围查询"),
    PREFIX("prefix", "前缀匹配");

    private final String code;
    private final String name;

    public static FilterOperatorEnum of(String code) {
        if (code == null) { return null; }
        for (FilterOperatorEnum e : values()) {
            if (e.code.equalsIgnoreCase(code)) {
                return e;
            }
        }
        return null;
    }
}

package com.pig4cloud.pigx.admin.enums.es;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 查询控件类型（前端表单控件）
 */
@Getter
@AllArgsConstructor
public enum FilterControlTypeEnum {

    TEXT("text", "文本输入"),
    SELECT("select", "下拉单选"),
    MULTI_SELECT("multi_select", "下拉多选"),
    DATE("date", "日期"),
    DATE_RANGE("date_range", "日期范围"),
    NUMBER("number", "数字"),
    NUMBER_RANGE("number_range", "数字范围");

    private final String code;
    private final String name;

    public static FilterControlTypeEnum of(String code) {
        if (code == null) { return null; }
        for (FilterControlTypeEnum e : values()) {
            if (e.code.equalsIgnoreCase(code)) {
                return e;
            }
        }
        return null;
    }
}

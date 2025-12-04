package com.pig4cloud.pigx.admin.enums.es;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 查询模板类型
 */
@Getter
@AllArgsConstructor
public enum EsTemplateTypeEnum {

    PERSONAL(1, "个人模板"),
    PUBLIC(2, "公共模板");

    private final int code;
    private final String name;

    public static EsTemplateTypeEnum of(Integer code) {
        if (code == null) { return null; }
        for (EsTemplateTypeEnum e : values()) {
            if (e.code == code) {
                return e;
            }
        }
        return null;
    }
}

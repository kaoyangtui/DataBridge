package com.pig4cloud.pigx.admin.enums.es;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 数据集状态
 */
@Getter
@AllArgsConstructor
public enum EsDatasetStatusEnum {

    DISABLED(0, "停用"),
    ENABLED(1, "启用");

    private final int code;
    private final String name;

    public static EsDatasetStatusEnum of(Integer code) {
        if (code == null) { return null; }
        for (EsDatasetStatusEnum e : values()) {
            if (e.code == code) {
                return e;
            }
        }
        return null;
    }
}

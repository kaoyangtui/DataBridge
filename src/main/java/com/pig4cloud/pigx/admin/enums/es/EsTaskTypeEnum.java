package com.pig4cloud.pigx.admin.enums.es;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 同步任务类型
 */
@Getter
@AllArgsConstructor
public enum EsTaskTypeEnum {

    FULL("full", "存量同步"),
    INCREMENT("increment", "增量同步"),
    DELETE("delete", "删除同步"),
    REPAIR("repair", "补偿/修复同步");

    private final String code;
    private final String name;

    public static EsTaskTypeEnum of(String code) {
        if (code == null) { return null; }
        for (EsTaskTypeEnum e : values()) {
            if (e.code.equalsIgnoreCase(code)) {
                return e;
            }
        }
        return null;
    }
}

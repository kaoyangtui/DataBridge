package com.pig4cloud.pigx.admin.enums.es;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 同步任务触发方式
 */
@Getter
@AllArgsConstructor
public enum EsTaskTriggerTypeEnum {

    MANUAL("manual", "手动触发"),
    SCHEDULE("schedule", "定时任务"),
    EVENT("event", "事件驱动");

    private final String code;
    private final String name;

    public static EsTaskTriggerTypeEnum of(String code) {
        if (code == null) { return null; }
        for (EsTaskTriggerTypeEnum e : values()) {
            if (e.code.equalsIgnoreCase(code)) {
                return e;
            }
        }
        return null;
    }
}

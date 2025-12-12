package com.pig4cloud.pigx.admin.service.impl;

import com.pig4cloud.pigx.admin.api.dto.es.EsQueryConditionDTO;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EsQueryServiceImplTest {

    private final EsQueryServiceImpl service = new EsQueryServiceImpl(null, null, null);

    @Test
    void resolveTermsValuesPrefersExplicitList() throws Exception {
        EsQueryConditionDTO condition = new EsQueryConditionDTO();
        condition.setValues(Arrays.asList("  A  ", "B", "", "A"));
        condition.setValue("C,D");

        List<String> result = invokeResolveTermsValues(condition);

        assertEquals(Arrays.asList("A", "B"), result);
    }

    @Test
    void resolveTermsValuesSupportsChineseCommaAndTrimming() throws Exception {
        EsQueryConditionDTO condition = new EsQueryConditionDTO();
        condition.setValue("  北京， 上海,上海 ,深圳 ,,  ");

        List<String> result = invokeResolveTermsValues(condition);

        assertEquals(Arrays.asList("北京", "上海", "深圳"), result);
    }

    @SuppressWarnings("unchecked")
    private List<String> invokeResolveTermsValues(EsQueryConditionDTO condition) throws Exception {
        Method method = EsQueryServiceImpl.class
                .getDeclaredMethod("resolveTermsValues", EsQueryConditionDTO.class);
        method.setAccessible(true);
        return (List<String>) method.invoke(service, condition);
    }
}

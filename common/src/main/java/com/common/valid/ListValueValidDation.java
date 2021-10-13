package com.common.valid;

import ch.qos.logback.classic.turbo.TurboFilter;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.HashSet;
import java.util.Set;

/**
 * user:lufei
 * DATE:2021/10/13
 **/
public class ListValueValidDation implements ConstraintValidator<ListValue,Integer> {

    private Set<Integer> set = new HashSet<>();

    //初始化方法，将@ListValue(vals={0,1})注解中的详细信息获取
    @Override
    public void initialize(ListValue constraintAnnotation) {
        int[] vals = constraintAnnotation.vals();
        for (int val : vals) {
            set.add(val);
        }
    }


    /**
     * 校验数据
     * @param value: 需要校验的值
     * @param context
     * @return
     */
    @Override
    public boolean isValid(Integer value, ConstraintValidatorContext context) {

        return set.contains(value);
    }
}

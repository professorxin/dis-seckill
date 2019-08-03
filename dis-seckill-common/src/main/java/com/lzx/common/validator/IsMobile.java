package com.lzx.common.validator;





@Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE})
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = {IsMobileValidator.class}) // 这个注解的参数指定用于校验工作的是哪个类
public @interface IsMobile {

    /**
     * 默认手机号码不可为空
     *
     * @return
     */
    boolean required() default true;

    /**
     * 如果校验不通过时的提示信息
     *
     * @return
     */
    String message() default "手机号码格式有误！";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}

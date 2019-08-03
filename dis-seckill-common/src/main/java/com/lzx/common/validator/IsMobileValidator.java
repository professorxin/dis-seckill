package com.lzx.common.validator;



public class IsMobileValidator implements ConstraintValidator<IsMobile, String> {

    private static Logger logger = LoggerFactory.getLogger(IsMobileValidator.class);

    /**
     * 用于获取检验字段是否可以为空
     */
    private boolean required = false;

    /**
     * 用于获取注解
     *
     * @param constraintAnnotation
     */
    @Override
    public void initialize(IsMobile constraintAnnotation) {
        required = constraintAnnotation.required();
    }

    /**
     * 用于检验字段是否合法
     *
     * @param value   待校验的字段
     * @param context
     * @return 字段检验结果
     */
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        logger.info("是否需要校验参数：" + required);
        // 如果所检验字段可以为空
        if (required) {
            return ValidatorUtil.isMobile(value);
        } else {
            return StringUtils.isEmpty(value) || ValidatorUtil.isMobile(value);
        }
    }
}

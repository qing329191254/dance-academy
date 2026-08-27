package com.forget.academy.common;

public final class CourseModuleTypes {
    public static final String TRIAL = "trial";
    public static final String SYSTEM = "system";
    public static final String PRODUCT = "product";

    private CourseModuleTypes() {
    }

    public static boolean isValid(String type) {
        return TRIAL.equals(type) || SYSTEM.equals(type) || PRODUCT.equals(type);
    }
}

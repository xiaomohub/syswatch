package org.xiaomo.syswatch.annotation;

import java.lang.annotation.*;

/**
 * 标记该方法需要记录日志
 */
@Target(ElementType.METHOD) // 只能加在方法上
@Retention(RetentionPolicy.RUNTIME) // 运行时可通过反射读取
@Documented
public @interface AlertLog {
    String action() default ""; // 可选：动作名称
}

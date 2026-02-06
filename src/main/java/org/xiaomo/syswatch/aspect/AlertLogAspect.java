package org.xiaomo.syswatch.aspect;

import jakarta.annotation.Resource;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.xiaomo.syswatch.service.AlertLogService;
import org.xiaomo.syswatch.domain.entity.AlertRule;

import java.util.List;

@Component
@Aspect
public class AlertLogAspect {

    @Resource
    private AlertLogService alertLogService;

    @Around("execution(* org.xiaomo.syswatch.service.RulePublishService.*(..))")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();

        String module = "unknown";
        String alertId = "unknown";

        if (args != null && args.length > 0) {
            if (args[0] instanceof List<?> list && !list.isEmpty() && list.get(0) instanceof AlertRule ar) {
                module = ar.getResourceType();
                alertId = module + "_rules.yaml";
            }
        }

        try {
            Object result = joinPoint.proceed();
            alertLogService.record(module, alertId, methodName, "admin", true, null);
            return result;
        } catch (Exception e) {
            alertLogService.record(module, alertId, methodName, "admin", false, e.getMessage());
            throw e;
        }
    }
}

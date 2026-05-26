package com.example.ordermanagement.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class PerformanceAspect {
    private static final long SLOW_METHOD_THRESHOLD_MS = 500;

    @Pointcut("execution(*com.example.ordermanagement.service.*.*(..))")
    public void serviceLayer() {}

    @Around("serviceLayer()")
    public Object monitorPerformance(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        Object result = joinPoint.proceed();
        long duration = System.currentTimeMillis() - startTime;
        if (duration > SLOW_METHOD_THRESHOLD_MS) {
            log.warn("Slow method detected: {}.{} took {}ms",
                    joinPoint.getSignature().getDeclaringType().getSimpleName(),
                    joinPoint.getSignature().getName(), duration);
        }
        return result;
    }
}

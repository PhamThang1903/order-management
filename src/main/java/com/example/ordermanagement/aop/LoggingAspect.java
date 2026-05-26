package com.example.ordermanagement.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class LoggingAspect {

    @Pointcut("execution(*com.example.ordermanagement.service.*.*(..))")
    public void serviceLayer() {}

    @Before("serviceLayer()")
    public void logBefore(JoinPoint joinPoint) {
        log.info("-> Entering: {} {}", joinPoint.getSignature().getDeclaringType().getSimpleName(),
                joinPoint.getSignature().getName());
    }

    @Around("serviceLayer()")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        try {
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - startTime;
            log.info("{}.{} completed in {}ms", joinPoint.getSignature().getDeclaringType().getSimpleName(),
                    joinPoint.getSignature().getName(), duration);
            return result;
        } catch (Throwable ex) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("{}.{} failed after {}ms", joinPoint.getSignature().getDeclaringType().getSimpleName(),
                    joinPoint.getSignature().getName(), duration);
            throw ex;
        }
    }

    @AfterThrowing(pointcut = "serviceLayer()", throwing = "ex")
    public void logException(JoinPoint joinPoint, Throwable ex) {
        log.error("Exception in {}.{}: {} - {}", joinPoint.getSignature().getDeclaringType().getSimpleName(),
                joinPoint.getSignature().getName(),
                ex.getClass().getSimpleName(),
                ex.getMessage());
    }
}

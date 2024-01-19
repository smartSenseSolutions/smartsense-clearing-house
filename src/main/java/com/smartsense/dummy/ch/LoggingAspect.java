package com.smartsense.dummy.ch;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.CodeSignature;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import java.util.HashMap;
import java.util.Map;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class LoggingAspect {


    private final ObjectMapper mapper;
    private final HttpServletRequest request;

    @Pointcut("execution(* com.smartsense.dummy.ch.controller.*.*(..))")
    public void printLogs() {
    }

    @SneakyThrows
    @Around("printLogs()")
    public Object logMethod(ProceedingJoinPoint joinPoint) {
        String targetClass = joinPoint.getTarget().getClass().getSimpleName();
        String targetMethod = joinPoint.getSignature().getName();
        Thread currentThread = Thread.currentThread();
        //Map<String, Object> parameters = getParameters(joinPoint);
        String ipAddress = request.getHeader("X-FORWARDED-FOR");
        if (ipAddress == null) {
            ipAddress = request.getRemoteAddr();
        }
            /*log.info("==> path(s): {}, Class: {}, method(s): {}, arguments: {}, Thread :{}, IpAddress :{} ",
                    request.getRequestURL(), targetClass, targetMethod, mapper.writeValueAsString(parameters), currentThreadId, ipAddress);*/

        log.info("==> Request : {}, Class: {}, method(s): {}, Thread :{}, IpAddress :{} ",
                request.getRequestURL(), targetClass, targetMethod, currentThread.getId(), ipAddress);

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        Object response;
        try {
            response = joinPoint.proceed();
        } finally {
            stopWatch.stop();
            /*log.info("<== Responding :{} in time:{}, Thread :{} ",
                    mapper.writeValueAsString(entity), stopWatch.getTotalTimeMillis(), currentThreadId);*/
            log.info("<== Responding in time:{} ms, Thread :{} ", stopWatch.getTotalTimeMillis(), currentThread.getId());
        }
        return response;
    }

    private Map<String, Object> getParameters(JoinPoint joinPoint) {
        CodeSignature signature = (CodeSignature) joinPoint.getSignature();

        HashMap<String, Object> map = new HashMap<>();

        String[] parameterNames = signature.getParameterNames();

        for (int i = 0; i < parameterNames.length; i++) {
            map.put(parameterNames[i], joinPoint.getArgs()[i]);
        }
        return map;
    }
}

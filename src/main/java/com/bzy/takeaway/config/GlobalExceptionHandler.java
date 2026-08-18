package com.bzy.takeaway.config;

import com.bzy.takeaway.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NoResourceFoundException.class)
    public Result<?> handleNoResourceFoundException(NoResourceFoundException e) {
        // 忽略 Chrome DevTools 的调试请求，避免日志污染
        if (e.getResourcePath() != null && e.getResourcePath().contains(".well-known/")) {
            log.debug("忽略 Chrome DevTools 调试请求：{}", e.getResourcePath());
            return Result.fail("资源不存在");
        }
        log.warn("静态资源未找到：{}", e.getResourcePath());
        return Result.fail(e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e) {
        log.error("全局异常：", e);
        return Result.fail(e.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    public Result<?> handleRuntimeException(RuntimeException e) {
        log.error("运行时异常：", e);
        return Result.fail(e.getMessage());
    }
}

package com.bzy.takeaway.config;

import com.bzy.takeaway.interceptor.AuthInterceptor;
import com.bzy.takeaway.interceptor.RiderAuthInterceptor;
import com.bzy.takeaway.interceptor.RoleInterceptor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcRegistrations;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.List;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class WebConfig implements WebMvcConfigurer {

    private final AuthInterceptor authInterceptor;
    private final RiderAuthInterceptor riderAuthInterceptor;
    private final RoleInterceptor roleInterceptor;

    @Value("${file.upload-path:./uploads}")
    private String uploadPath;

    @Value("${spring.web.resources.cache-period:2592000}")
    private long cachePeriod;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("*")
                .allowedHeaders("*")
                .allowCredentials(true);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 先注册公开接口的排除（优先级更高）
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/api/**")  // 拦截所有 API
                .excludePathPatterns(
                        // 公开接口 - 无需登录
                        "/api/uploads/**",              // 排除：上传文件访问
                        "/api/frontend/images/**",      // 排除：前端静态图片资源
                        "/api/store/list",              // 排除：商家列表
                        "/api/store/detail/**",         // 排除：商家详情
                        "/api/store/*/dishes",          // 排除：商家菜品（带参数）
                        "/api/store/*/categories",      // 排除：商家分类（带参数）
                        "/api/store/dish/**",           // 排除：菜品详情
                        "/api/store/suggest",           // 排除：搜索建议
                        "/api/store/dishes/search",     // 排除：菜品搜索
                        "/api/product-review/dish/**",  // 排除：菜品评价查询
                        "/api/product-review/store/**", // 排除：商家评价查询
                        "/api/product-review/order/**", // 排除：订单评价查询
                        "/api/review/store/**",         // 排除：店铺评价查询（兼容旧接口）
                        "/api/banner/**",               // 排除：Banner 查询
                        "/api/category/list",           // 排除：分类列表
                        "/api/auth/login",              // 排除：登录
                        "/api/auth/register",           // 排除：注册
                        "/api/auth/merchant/register",  // 排除：商家注册
                        "/api/auth/captcha",            // 排除：获取验证码
                        "/api/auth/failureCount",       // 排除：获取失败次数
                        "/api/auth/sendCode",           // 排除：发送验证码
                        "/api/auth/resetPassword",      // 排除：重置密码
                        "/api/rider/register",          // 排除：骑手注册
                        "/api/rider/login",             // 排除：骑手登录
                        "/api/merchant/delivery/fee/calculate",  // 排除：计算配送费
                        "/api/pay/alipay/notify"        // 排除：支付宝异步通知（无 token）
                )
                .order(1);
        registry.addInterceptor(riderAuthInterceptor)
                .addPathPatterns("/api/rider/**")
                .excludePathPatterns("/api/rider/register", "/api/rider/login")
                .order(3);
        registry.addInterceptor(roleInterceptor)
                .addPathPatterns("/api/admin/**", "/api/merchant/**")
                .excludePathPatterns("/api/merchant/delivery/fee/calculate")
                .order(4);
    }

    @Override
    public void configureMessageConverters(List<org.springframework.http.converter.HttpMessageConverter<?>> converters) {
        StringHttpMessageConverter converter = new StringHttpMessageConverter(StandardCharsets.UTF_8);
        converters.add(converter);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String fileUrl = uploadPath.endsWith("/") ? "file:///" + uploadPath.replace("\\", "/") : "file:///" + uploadPath.replace("\\", "/") + "/";
        log.info("上传文件路径：{}", fileUrl);

        // 上传文件访问路径（优先级最高）- 生产环境缓存 30 天
        registry.addResourceHandler("/api/uploads/**")
                .addResourceLocations(fileUrl)
                .setCachePeriod((int) cachePeriod);

        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(fileUrl)
                .setCachePeriod((int) cachePeriod);

        // 前端静态图片资源（映射到上传目录）- 生产环境缓存 30 天
        registry.addResourceHandler("/api/frontend/images/**")
                .addResourceLocations(fileUrl)
                .setCachePeriod((int) cachePeriod);

        // 静态资源（HTML/CSS/JS/字体等）- 生产环境缓存 30 天
        registry.addResourceHandler("/*.html", "/*.css", "/*.js", "/*.ico", "/*.svg", "/*.woff", "/*.woff2", "/*.ttf")
                .addResourceLocations("classpath:/static/")
                .setCachePeriod((int) cachePeriod);
    }
}

package com.bzy.takeaway.controller;

import com.bzy.takeaway.common.Result;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/file")
@CrossOrigin(originPatterns = "*", maxAge = 3600)
public class FileController {

    @Value("${file.upload-path:./uploads}")
    private String uploadPath;

    @PostMapping("/upload")
    public Result<String> upload(@RequestParam("file") MultipartFile file, HttpServletRequest request) throws IOException {
        log.info("=== 文件上传请求 ===");
        log.info("请求方法：{}", request.getMethod());
        log.info("请求 URL: {}", request.getRequestURL());
        log.info("Origin: {}", request.getHeader("Origin"));
        log.info("Authorization: {}", request.getHeader("Authorization"));
        
        if (file.isEmpty()) {
            log.error("文件为空");
            return Result.fail("文件为空");
        }
        
        // 获取绝对路径
        String absoluteUploadPath = new File(uploadPath).getAbsolutePath();
        log.info("上传路径：{}", absoluteUploadPath);
        
        String ext = file.getOriginalFilename();
        if (ext != null && ext.contains(".")) ext = ext.substring(ext.lastIndexOf("."));
        else ext = "";
        String name = UUID.randomUUID().toString().replace("-", "") + ext;
        
        Path dir = Paths.get(absoluteUploadPath);
        if (!Files.exists(dir)) Files.createDirectories(dir);
        
        Path filePath = dir.resolve(name);
        log.info("保存文件到：{}", filePath);
        file.transferTo(filePath.toFile());
        
        String url = "/uploads/" + name;
        log.info("返回 URL: {}", url);
        log.info("=== 上传成功 ===");
        return Result.ok(url);
    }

    @PostMapping("/uploads")
    public Result<String[]> uploadMultiple(@RequestParam("files") MultipartFile[] files, HttpServletRequest request) throws IOException {
        if (files == null || files.length == 0) return Result.fail("文件为空");
        
        // 获取绝对路径
        String absoluteUploadPath = new File(uploadPath).getAbsolutePath();
        log.info("上传路径：{}", absoluteUploadPath);
        
        String[] urls = new String[files.length];
        Path dir = Paths.get(absoluteUploadPath);
        if (!Files.exists(dir)) Files.createDirectories(dir);
        
        for (int i = 0; i < files.length; i++) {
            MultipartFile file = files[i];
            if (!file.isEmpty()) {
                String ext = file.getOriginalFilename();
                if (ext != null && ext.contains(".")) ext = ext.substring(ext.lastIndexOf("."));
                else ext = "";
                String name = UUID.randomUUID().toString().replace("-", "") + ext;
                
                Path filePath = dir.resolve(name);
                file.transferTo(filePath.toFile());
                urls[i] = "/uploads/" + name;
            }
        }
        return Result.ok(urls);
    }
}

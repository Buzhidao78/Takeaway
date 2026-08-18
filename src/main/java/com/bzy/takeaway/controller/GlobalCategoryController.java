package com.bzy.takeaway.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bzy.takeaway.common.Result;
import com.bzy.takeaway.entity.GlobalCategory;
import com.bzy.takeaway.entity.Store;
import com.bzy.takeaway.service.GlobalCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/category")
@RequiredArgsConstructor
public class GlobalCategoryController {

    private final GlobalCategoryService globalCategoryService;

    @GetMapping("/list")
    public Result<List<GlobalCategory>> list() {
        return Result.ok(globalCategoryService.list());
    }

    @GetMapping("/{categoryId}/stores")
    public Result<Page<Store>> getStoresByCategory(
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.ok(globalCategoryService.getStoresByCategory(categoryId, page, size));
    }

    @GetMapping("/stats")
    public Result<Map<Long, Integer>> getCategoryStats() {
        return Result.ok(globalCategoryService.getCategoryStats());
    }

    @PostMapping("/store/{storeId}/bind")
    public Result<?> bindStoreCategories(
            @PathVariable Long storeId,
            @RequestBody List<Long> categoryIds) {
        globalCategoryService.bindStoreCategories(storeId, categoryIds);
        return Result.ok();
    }

    @GetMapping("/store/{storeId}/categories")
    public Result<List<Long>> getStoreCategories(@PathVariable Long storeId) {
        return Result.ok(globalCategoryService.getStoreCategoryIds(storeId));
    }
}

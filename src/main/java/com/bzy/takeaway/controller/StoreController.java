package com.bzy.takeaway.controller;

import com.bzy.takeaway.common.Result;
import com.bzy.takeaway.entity.Category;
import com.bzy.takeaway.entity.Dish;
import com.bzy.takeaway.entity.Store;
import com.bzy.takeaway.service.StoreService;
import com.bzy.takeaway.service.StoreService.SuggestResult;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/store")
@RequiredArgsConstructor
public class StoreController {

    private final StoreService storeService;

    @GetMapping("/list")
    public Result<Page<Store>> list(@RequestParam(defaultValue = "1") int page,
                                    @RequestParam(defaultValue = "10") int size,
                                    @RequestParam(required = false) String keyword) {
        return Result.ok(storeService.list(page, size, keyword));
    }

    @GetMapping("/detail/{id}")
    public Result<Store> detail(@PathVariable Long id) {
        return Result.ok(storeService.detail(id));
    }

    @GetMapping("/{storeId}/categories")
    public Result<List<Category>> categories(@PathVariable Long storeId) {
        return Result.ok(storeService.categories(storeId));
    }

    @GetMapping("/{storeId}/dishes")
    public Result<List<Dish>> dishes(@PathVariable Long storeId, @RequestParam(required = false) Long categoryId) {
        return Result.ok(storeService.dishes(storeId, categoryId));
    }
    
    @GetMapping("/dish/{id}")
    public Result<Dish> dishDetail(@PathVariable Long id) {
        return Result.ok(storeService.dishDetail(id));
    }
    
    /**
     * 搜索建议
     */
    @GetMapping("/suggest")
    public Result<SuggestResult> suggest(@RequestParam String keyword,
                                         @RequestParam(defaultValue = "10") int limit) {
        return Result.ok(storeService.suggest(keyword, limit));
    }
    
    /**
     * 搜索菜品
     */
    @GetMapping("/dishes/search")
    public Result<Page<Dish>> searchDishes(@RequestParam(defaultValue = "1") int page,
                                           @RequestParam(defaultValue = "10") int size,
                                           @RequestParam(required = false) String keyword) {
        try {
            System.out.println("搜索菜品 - keyword: " + keyword + ", page: " + page + ", size: " + size);
            var result = storeService.searchDishes(page, size, keyword);
            System.out.println("搜索结果 - total: " + result.getTotal() + ", records: " + result.getRecords().size());
            return Result.ok(result);
        } catch (Exception e) {
            System.err.println("搜索失败：" + e.getMessage());
            e.printStackTrace();
            return Result.fail("搜索失败：" + e.getMessage());
        }
    }
    
}

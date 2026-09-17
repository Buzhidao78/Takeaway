package com.bzy.takeaway.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bzy.takeaway.entity.Category;
import com.bzy.takeaway.entity.Dish;
import com.bzy.takeaway.entity.Store;

import java.util.List;

public interface StoreService {

    /**
     * 搜索建议结果
     */
    record SuggestResult(List<String> stores, List<String> dishes) {}

    /**
     * 搜索建议
     */
    SuggestResult suggest(String keyword, int limit);

    /**
     * 搜索菜品
     */
    Page<Dish> searchDishes(int page, int size, String keyword);

    Page<Store> list(int page, int size, String keyword);

    Store detail(Long id);

    List<Category> categories(Long storeId);

    List<Dish> dishes(Long storeId, Long categoryId);

    Dish dishDetail(Long id);
}
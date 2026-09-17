package com.bzy.takeaway.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.bzy.takeaway.entity.GlobalCategory;
import com.bzy.takeaway.entity.Store;

import java.util.List;
import java.util.Map;

public interface GlobalCategoryService extends IService<GlobalCategory> {

    List<GlobalCategory> list();

    List<GlobalCategory> listAll();

    Page<Store> getStoresByCategory(Long categoryId, int page, int size);

    Map<Long, Integer> getCategoryStats();

    void bindStoreCategories(Long storeId, List<Long> categoryIds);

    List<Long> getStoreCategoryIds(Long storeId);
}
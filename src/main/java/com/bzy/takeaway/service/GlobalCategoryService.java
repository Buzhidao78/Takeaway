package com.bzy.takeaway.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bzy.takeaway.entity.GlobalCategory;
import com.bzy.takeaway.entity.Store;
import com.bzy.takeaway.entity.StoreCategoryRel;
import com.bzy.takeaway.mapper.GlobalCategoryMapper;
import com.bzy.takeaway.mapper.OrdersMapper;
import com.bzy.takeaway.mapper.ProductReviewMapper;
import com.bzy.takeaway.mapper.StoreCategoryRelMapper;
import com.bzy.takeaway.mapper.StoreMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GlobalCategoryService extends ServiceImpl<GlobalCategoryMapper, GlobalCategory> {

    private final GlobalCategoryMapper globalCategoryMapper;
    private final StoreCategoryRelMapper storeCategoryRelMapper;
    private final StoreMapper storeMapper;
    private final OrdersMapper ordersMapper;
    private final ProductReviewMapper productReviewMapper;

    public List<GlobalCategory> list() {
        return globalCategoryMapper.selectList(
            new LambdaQueryWrapper<GlobalCategory>()
                .eq(GlobalCategory::getStatus, 1)
                .orderByAsc(GlobalCategory::getSort)
        );
    }

    public List<GlobalCategory> listAll() {
        return globalCategoryMapper.selectList(
            new LambdaQueryWrapper<GlobalCategory>()
                .orderByAsc(GlobalCategory::getSort)
        );
    }

    public Page<Store> getStoresByCategory(Long categoryId, int page, int size) {
        List<Long> storeIds = storeCategoryRelMapper.selectList(
            new LambdaQueryWrapper<StoreCategoryRel>()
                .eq(StoreCategoryRel::getCategoryId, categoryId)
        ).stream().map(StoreCategoryRel::getStoreId).collect(Collectors.toList());

        Page<Store> result = new Page<>(page, size);
        if (storeIds.isEmpty()) {
            return result;
        }

        var q = new LambdaQueryWrapper<Store>()
            .in(Store::getId, storeIds)
            .eq(Store::getAuditStatus, 1)
            .eq(Store::getStatus, 1);

        Page<Store> storePage = storeMapper.selectPage(new Page<>(page, size), q);

        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        for (Store store : storePage.getRecords()) {
            var orderQ = new LambdaQueryWrapper<com.bzy.takeaway.entity.Orders>()
                .eq(com.bzy.takeaway.entity.Orders::getStoreId, store.getId())
                .eq(com.bzy.takeaway.entity.Orders::getStatus, 4)
                .ge(com.bzy.takeaway.entity.Orders::getCreateTime, thirtyDaysAgo);
            long monthSales = ordersMapper.selectCount(orderQ);
            store.setSalesCount((int) monthSales);
            updateStoreRating(store);
        }

        return storePage;
    }

    public Map<Long, Integer> getCategoryStats() {
        List<StoreCategoryRel> rels = storeCategoryRelMapper.selectList(new LambdaQueryWrapper<>());
        Map<Long, Integer> stats = new HashMap<>();
        for (StoreCategoryRel rel : rels) {
            stats.put(rel.getCategoryId(), stats.getOrDefault(rel.getCategoryId(), 0) + 1);
        }
        return stats;
    }

    @Transactional
    public void bindStoreCategories(Long storeId, List<Long> categoryIds) {
        storeCategoryRelMapper.delete(
            new LambdaQueryWrapper<StoreCategoryRel>()
                .eq(StoreCategoryRel::getStoreId, storeId)
        );
        if (categoryIds != null && !categoryIds.isEmpty()) {
            for (Long categoryId : categoryIds) {
                StoreCategoryRel rel = new StoreCategoryRel();
                rel.setStoreId(storeId);
                rel.setCategoryId(categoryId);
                storeCategoryRelMapper.insert(rel);
            }
        }
    }

    public List<Long> getStoreCategoryIds(Long storeId) {
        return storeCategoryRelMapper.selectList(
            new LambdaQueryWrapper<StoreCategoryRel>()
                .eq(StoreCategoryRel::getStoreId, storeId)
        ).stream().map(StoreCategoryRel::getCategoryId).collect(Collectors.toList());
    }

    private void updateStoreRating(Store store) {
        var reviewQ = new LambdaQueryWrapper<com.bzy.takeaway.entity.ProductReview>()
            .eq(com.bzy.takeaway.entity.ProductReview::getStoreId, store.getId())
            .eq(com.bzy.takeaway.entity.ProductReview::getStatus, 1);
        List<com.bzy.takeaway.entity.ProductReview> reviews = productReviewMapper.selectList(reviewQ);

        if (reviews.isEmpty()) {
            store.setRating(BigDecimal.ZERO);
            store.setReviewCount(0);
            return;
        }

        BigDecimal totalRating = reviews.stream()
            .map(com.bzy.takeaway.entity.ProductReview::getRating)
            .map(BigDecimal::valueOf)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal avgRating = totalRating
            .divide(BigDecimal.valueOf(reviews.size()), 1, RoundingMode.HALF_UP);

        store.setRating(avgRating);
        store.setReviewCount(reviews.size());
    }
}

package com.bzy.takeaway.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bzy.takeaway.common.Constants;
import com.bzy.takeaway.entity.*;
import com.bzy.takeaway.mapper.*;
import com.bzy.takeaway.service.StoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StoreServiceImpl implements StoreService {

    private final StoreMapper storeMapper;
    private final CategoryMapper categoryMapper;
    private final DishMapper dishMapper;
    private final OrdersMapper ordersMapper;
    private final OrderItemMapper orderItemMapper;
    private final ProductReviewMapper productReviewMapper;

    @Override
    public SuggestResult suggest(String keyword, int limit) {
        // 搜索商家建议
        var storeQ = new LambdaQueryWrapper<Store>()
            .eq(Store::getAuditStatus, 1)
            .eq(Store::getStatus, Constants.STORE_STATUS_OPEN)
            .like(Store::getName, keyword)
            .orderByDesc(Store::getSalesCount)
            .last("LIMIT " + limit);
        List<String> stores = storeMapper.selectList(storeQ).stream()
            .map(Store::getName)
            .collect(Collectors.toList());

        // 搜索菜品建议
        var dishQ = new LambdaQueryWrapper<Dish>()
            .eq(Dish::getStatus, 1)
            .like(Dish::getName, keyword)
            .orderByDesc(Dish::getSales)
            .last("LIMIT " + limit);
        List<String> dishes = dishMapper.selectList(dishQ).stream()
            .map(Dish::getName)
            .collect(Collectors.toList());

        return new SuggestResult(stores, dishes);
    }

    @Override
    public Page<Dish> searchDishes(int page, int size, String keyword) {
        var q = new LambdaQueryWrapper<Dish>()
            .eq(Dish::getStatus, 1);

        if (keyword != null && !keyword.isEmpty()) {
            q.like(Dish::getName, keyword);
        }

        q.orderByDesc(Dish::getSales);

        Page<Dish> dishPage = dishMapper.selectPage(new Page<>(page, size), q);

        // 为每个菜品计算月销量和评分
        LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        for (Dish dish : dishPage.getRecords()) {
            // 计算月销量
            var orderItemQ = new LambdaQueryWrapper<OrderItem>()
                .eq(OrderItem::getDishId, dish.getId())
                .ge(OrderItem::getCreateTime, startOfMonth);
            List<OrderItem> orderItems = orderItemMapper.selectList(orderItemQ);

            long monthSales = orderItems.stream()
                .filter(item -> {
                    var orderQ = new LambdaQueryWrapper<Orders>()
                        .eq(Orders::getId, item.getOrderId())
                        .in(Orders::getStatus, 3, 4);
                    return ordersMapper.selectCount(orderQ) > 0;
                })
                .count();

            dish.setSalesCount((int) monthSales);

            // 获取评分
            var reviewQ = new LambdaQueryWrapper<ProductReview>()
                .eq(ProductReview::getDishId, dish.getId());
            List<ProductReview> reviews = productReviewMapper.selectList(reviewQ);
            if (!reviews.isEmpty()) {
                double avgRating = reviews.stream()
                    .mapToInt(ProductReview::getRating)
                    .average()
                    .orElse(0.0);
                dish.setRating(Math.round(avgRating * 10) / 10.0);
            }

            // 获取店铺名称
            Store store = storeMapper.selectById(dish.getStoreId());
            if (store != null) {
                dish.setStoreName(store.getName());
            }
        }

        return dishPage;
    }

    @Override
    public Page<Store> list(int page, int size, String keyword) {
        var q = new LambdaQueryWrapper<Store>()
                .eq(Store::getAuditStatus, 1)
                .eq(Store::getStatus, Constants.STORE_STATUS_OPEN);
        if (keyword != null && !keyword.isEmpty()) {
            q.like(Store::getName, keyword).or().like(Store::getDescription, keyword);
        }
        q.orderByDesc(Store::getSalesCount);
        Page<Store> storePage = storeMapper.selectPage(new Page<>(page, size), q);

        // 为每个店铺计算月销量（近30天）和评分
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        for (Store store : storePage.getRecords()) {
            // 计算月销量
            var orderQ = new LambdaQueryWrapper<Orders>()
                .eq(Orders::getStoreId, store.getId())
                .eq(Orders::getStatus, 4)
                .ge(Orders::getCreateTime, thirtyDaysAgo);
            long monthSales = ordersMapper.selectCount(orderQ);
            store.setSalesCount((int) monthSales);

            // 计算店铺评分
            updateStoreRating(store);
        }

        return storePage;
    }

    /**
     * 更新店铺评分
     */
    private void updateStoreRating(Store store) {
        var reviewQ = new LambdaQueryWrapper<ProductReview>()
            .eq(ProductReview::getStoreId, store.getId())
            .eq(ProductReview::getStatus, 1);
        List<ProductReview> reviews = productReviewMapper.selectList(reviewQ);

        if (reviews.isEmpty()) {
            store.setRating(BigDecimal.ZERO);
            store.setReviewCount(0);
            return;
        }

        BigDecimal totalRating = reviews.stream()
            .map(ProductReview::getRating)
            .map(BigDecimal::valueOf)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal avgRating = totalRating
            .divide(BigDecimal.valueOf(reviews.size()), 1, java.math.RoundingMode.HALF_UP);

        store.setRating(avgRating);
        store.setReviewCount(reviews.size());
    }

    @Override
    public Store detail(Long id) {
        Store store = storeMapper.selectById(id);
        if (store != null) {
            // 计算月销量（近30天）
            LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
            var orderQ = new LambdaQueryWrapper<Orders>()
                .eq(Orders::getStoreId, store.getId())
                .eq(Orders::getStatus, 4)
                .ge(Orders::getCreateTime, thirtyDaysAgo);
            long monthSales = ordersMapper.selectCount(orderQ);
            store.setSalesCount((int) monthSales);
        }
        return store;
    }

    @Override
    public List<Category> categories(Long storeId) {
        return categoryMapper.selectList(
                new LambdaQueryWrapper<Category>().eq(Category::getStoreId, storeId).orderByAsc(Category::getSort));
    }

    @Override
    public List<Dish> dishes(Long storeId, Long categoryId) {
        var q = new LambdaQueryWrapper<Dish>().eq(Dish::getStoreId, storeId).eq(Dish::getStatus, 1);
        if (categoryId != null && categoryId > 0) q.eq(Dish::getCategoryId, categoryId);
        return dishMapper.selectList(q.orderByAsc(Dish::getSort));
    }

    @Override
    public Dish dishDetail(Long id) {
        Dish dish = dishMapper.selectById(id);
        if (dish != null) {
            // 计算月销量 - 通过订单表关联查询
            LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
            var orderItemQ = new LambdaQueryWrapper<OrderItem>()
                .eq(OrderItem::getDishId, dish.getId())
                .ge(OrderItem::getCreateTime, startOfMonth);
            List<OrderItem> orderItems = orderItemMapper.selectList(orderItemQ);

            // 过滤出已完成或配送中的订单
            long monthSales = orderItems.stream()
                .filter(item -> {
                    var orderQ = new LambdaQueryWrapper<Orders>()
                        .eq(Orders::getId, item.getOrderId())
                        .in(Orders::getStatus, 3, 4);
                    return ordersMapper.selectCount(orderQ) > 0;
                })
                .count();

            dish.setSalesCount((int) monthSales);

            // 获取评分
            var reviewQ = new LambdaQueryWrapper<ProductReview>()
                .eq(ProductReview::getDishId, dish.getId());
            List<ProductReview> reviews = productReviewMapper.selectList(reviewQ);
            if (!reviews.isEmpty()) {
                double avgRating = reviews.stream()
                    .mapToInt(ProductReview::getRating)
                    .average()
                    .orElse(0.0);
                dish.setRating(Math.round(avgRating * 10) / 10.0);
            }
        }
        return dish;
    }
}
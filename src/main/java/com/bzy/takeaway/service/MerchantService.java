package com.bzy.takeaway.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bzy.takeaway.entity.Category;
import com.bzy.takeaway.entity.Dish;
import com.bzy.takeaway.entity.Orders;
import com.bzy.takeaway.entity.Store;
import com.bzy.takeaway.entity.StoreEarnings;

import java.util.List;
import java.util.Map;

public interface MerchantService {

    Store getMyStore(Long userId);

    Store updateStore(Long userId, Store store);

    List<Category> listCategories(Long userId);

    Category saveCategory(Long userId, Category cat);

    void deleteCategory(Long userId, Long id);

    List<Dish> listDishes(Long userId, Long categoryId);

    Dish saveDish(Long userId, Dish dish);

    void deleteDish(Long userId, Long id);

    Page<Orders> listOrders(Long userId, Integer status, int page, int size);

    Orders orderDetail(Long userId, Long orderId);

    void updateOrderStatus(Long userId, Long orderId, int status);

    Store updateStoreStatus(Long userId, Integer status);

    Map<String, Object> getDashboardData(Long userId);

    Map<String, Object> getEarningsStats(Long userId);

    Page<StoreEarnings> getEarnings(Long userId, int page, int size);
}

package com.bzy.takeaway.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bzy.takeaway.common.Constants;
import com.bzy.takeaway.common.Result;
import com.bzy.takeaway.entity.Category;
import com.bzy.takeaway.entity.DeliveryOrder;
import com.bzy.takeaway.entity.Dish;
import com.bzy.takeaway.entity.Orders;
import com.bzy.takeaway.entity.Review;
import com.bzy.takeaway.entity.Rider;
import com.bzy.takeaway.entity.Store;
import com.bzy.takeaway.entity.StoreEarnings;
import com.bzy.takeaway.mapper.DeliveryOrderMapper;
import com.bzy.takeaway.mapper.RiderMapper;
import com.bzy.takeaway.service.MerchantService;
import com.bzy.takeaway.service.ProductReviewService;
import com.bzy.takeaway.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/merchant")
@RequiredArgsConstructor
public class MerchantController {

    private final MerchantService merchantService;
    private final ReviewService reviewService;
    private final ProductReviewService productReviewService;
    private final DeliveryOrderMapper deliveryOrderMapper;
    private final RiderMapper riderMapper;

    @GetMapping("/store")
    public Result<Store> getStore(@RequestAttribute Long userId) {
        return Result.ok(merchantService.getMyStore(userId));
    }

    @PutMapping("/store")
    public Result<Store> updateStore(@RequestAttribute Long userId, @RequestBody Store store) {
        return Result.ok(merchantService.updateStore(userId, store));
    }

    @GetMapping("/categories")
    public Result<List<Category>> categories(@RequestAttribute Long userId) {
        return Result.ok(merchantService.listCategories(userId));
    }

    @PostMapping("/category")
    public Result<Category> saveCategory(@RequestAttribute Long userId, @RequestBody Category cat) {
        return Result.ok(merchantService.saveCategory(userId, cat));
    }

    @DeleteMapping("/category/{id}")
    public Result<?> deleteCategory(@RequestAttribute Long userId, @PathVariable Long id) {
        merchantService.deleteCategory(userId, id);
        return Result.ok();
    }

    @GetMapping("/dishes")
    public Result<List<Dish>> dishes(@RequestAttribute Long userId, @RequestParam(required = false) Long categoryId) {
        return Result.ok(merchantService.listDishes(userId, categoryId));
    }

    @PostMapping("/dish")
    public Result<Dish> saveDish(@RequestAttribute Long userId, @RequestBody Dish dish) {
        return Result.ok(merchantService.saveDish(userId, dish));
    }

    @DeleteMapping("/dish/{id}")
    public Result<?> deleteDish(@RequestAttribute Long userId, @PathVariable Long id) {
        merchantService.deleteDish(userId, id);
        return Result.ok();
    }

    @GetMapping("/orders")
    public Result<Page<Orders>> orders(@RequestAttribute Long userId,
                                       @RequestParam(required = false) Integer status,
                                       @RequestParam(defaultValue = "1") int page,
                                       @RequestParam(defaultValue = "10") int size) {
        return Result.ok(merchantService.listOrders(userId, status, page, size));
    }

    @GetMapping("/order/{orderId}")
    public Result<Map<String, Object>> orderDetail(@RequestAttribute Long userId, @PathVariable Long orderId) {
        Orders order = merchantService.orderDetail(userId, orderId);
        Map<String, Object> result = new HashMap<>();
        result.put("order", order);

        if (order != null) {
            DeliveryOrder deliveryOrder = deliveryOrderMapper.selectOne(new LambdaQueryWrapper<DeliveryOrder>()
                    .eq(DeliveryOrder::getOrderId, orderId));
            if (deliveryOrder != null && deliveryOrder.getRiderId() != null) {
                Rider rider = riderMapper.selectById(deliveryOrder.getRiderId());
                result.put("rider", rider);
            }
        }

        return Result.ok(result);
    }

    @PutMapping("/order/{orderId}/status")
    public Result<?> updateOrderStatus(@RequestAttribute Long userId, @PathVariable Long orderId, @RequestBody Map<String, Integer> body) {
        merchantService.updateOrderStatus(userId, orderId, body.get("status"));
        return Result.ok();
    }

    @PutMapping("/store/status")
    public Result<Store> updateStoreStatus(@RequestAttribute Long userId, @RequestBody Map<String, Integer> body) {
        return Result.ok(merchantService.updateStoreStatus(userId, body.get("status")));
    }

    @GetMapping("/dashboard")
    public Result<Map<String, Object>> getDashboardData(@RequestAttribute Long userId) {
        return Result.ok(merchantService.getDashboardData(userId));
    }

    @GetMapping("/reviews")
    public Result<Map<String, Object>> getReviews(@RequestAttribute Long userId,
                                                   @RequestParam(defaultValue = "1") Integer page,
                                                   @RequestParam(defaultValue = "10") Integer size) {
        Store store = merchantService.getMyStore(userId);
        if (store == null) {
            return Result.ok(Map.of("list", List.of(), "total", 0L, "page", page, "size", size));
        }
        var p = reviewService.getMerchantReviews(store.getId(), page, size);
        return Result.ok(Map.of(
            "list", p.getRecords(),
            "total", p.getTotal(),
            "page", p.getCurrent(),
            "size", p.getSize()
        ));
    }

    @PostMapping("/review/reply")
    public Result<Review> replyReview(@RequestAttribute Long userId, @RequestBody Map<String, Object> body) {
        Long reviewId = Long.valueOf(body.get("reviewId").toString());
        String replyContent = (String) body.get("replyContent");
        return Result.ok(reviewService.replyReview(userId, reviewId, replyContent));
    }

    @PutMapping("/review/status")
    public Result<Review> updateReviewStatus(@RequestAttribute Long userId, @RequestBody Map<String, Object> body) {
        Long reviewId = Long.valueOf(body.get("reviewId").toString());
        Integer status = (Integer) body.get("status");
        return Result.ok(reviewService.updateReviewStatus(userId, reviewId, status));
    }

    @GetMapping("/product-reviews")
    public Result<Map<String, Object>> getProductReviews(@RequestAttribute Long userId,
                                                         @RequestParam(defaultValue = "1") Integer page,
                                                         @RequestParam(defaultValue = "10") Integer size,
                                                         @RequestParam(required = false) Integer status) {
        Store store = merchantService.getMyStore(userId);
        if (store == null) {
            return Result.ok(Map.of("list", List.of(), "total", 0L, "page", page, "size", size));
        }
        var p = productReviewService.getStoreReviews(store.getId(), page, size, status, null, null, "time");
        return Result.ok(Map.of(
            "list", p.getRecords(),
            "total", p.getTotal(),
            "page", p.getCurrent(),
            "size", p.getSize()
        ));
    }

    @PostMapping("/product-review/reply")
    public Result<?> replyProductReview(@RequestAttribute Long userId, @RequestBody Map<String, Object> body) {
        Long reviewId = Long.valueOf(body.get("reviewId").toString());
        String content = (String) body.get("content");
        productReviewService.replyReview(reviewId, content);
        return Result.ok();
    }

    @PutMapping("/product-review/status")
    public Result<?> updateProductReviewStatus(@RequestAttribute Long userId, @RequestBody Map<String, Object> body) {
        Long reviewId = Long.valueOf(body.get("reviewId").toString());
        Integer status = (Integer) body.get("status");
        productReviewService.updateStatus(reviewId, status);
        return Result.ok();
    }

    @GetMapping("/earnings/stats")
    public Result<Map<String, Object>> getEarningsStats(@RequestAttribute Long userId) {
        return Result.ok(merchantService.getEarningsStats(userId));
    }

    @GetMapping("/earnings")
    public Result<Map<String, Object>> getEarnings(
            @RequestAttribute Long userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        var result = new HashMap<String, Object>();
        var pageData = merchantService.getEarnings(userId, page, size);
        result.put("list", pageData.getRecords());
        result.put("total", pageData.getTotal());
        return Result.ok(result);
    }
}

package com.bzy.takeaway.controller;

import com.bzy.takeaway.common.Result;
import com.bzy.takeaway.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping("/list")
    public Result<?> list(@RequestAttribute Long userId) {
        return Result.ok(cartService.list(userId));
    }

    @PostMapping("/add")
    public Result<?> add(@RequestAttribute Long userId, @RequestBody Map<String, Object> body) {
        Long storeId = Long.valueOf(body.get("storeId").toString());
        Long dishId = Long.valueOf(body.get("dishId").toString());
        int quantity = body.containsKey("quantity") ? (int) body.get("quantity") : 1;
        cartService.add(userId, storeId, dishId, quantity);
        return Result.ok();
    }

    @PutMapping("/{cartId}/quantity")
    public Result<?> updateQuantity(@RequestAttribute Long userId, @PathVariable Long cartId, @RequestBody Map<String, Integer> body) {
        cartService.updateQuantity(userId, cartId, body.getOrDefault("quantity", 1));
        return Result.ok();
    }

    @DeleteMapping("/{cartId}")
    public Result<?> remove(@RequestAttribute Long userId, @PathVariable Long cartId) {
        cartService.remove(userId, cartId);
        return Result.ok();
    }
}

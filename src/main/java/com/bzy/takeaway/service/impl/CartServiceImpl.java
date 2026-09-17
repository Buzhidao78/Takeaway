package com.bzy.takeaway.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bzy.takeaway.dto.CartVO;
import com.bzy.takeaway.entity.Cart;
import com.bzy.takeaway.entity.Dish;
import com.bzy.takeaway.mapper.CartMapper;
import com.bzy.takeaway.mapper.DishMapper;
import com.bzy.takeaway.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartMapper cartMapper;
    private final DishMapper dishMapper;

    @Override
    public List<CartVO> list(Long userId) {
        System.out.println("=== 查询购物车，userId: " + userId);
        List<Cart> carts = cartMapper.selectList(new LambdaQueryWrapper<Cart>().eq(Cart::getUserId, userId));
        System.out.println("查询到的购物车记录数：" + carts.size());
        if (carts.isEmpty()) return List.of();
        var dishIds = carts.stream().map(Cart::getDishId).distinct().toList();
        Map<Long, Dish> dishMap = dishMapper.selectBatchIds(dishIds).stream().collect(Collectors.toMap(Dish::getId, d -> d));
        List<CartVO> result = new ArrayList<>();
        for (Cart c : carts) {
            CartVO vo = new CartVO();
            vo.setId(c.getId());
            vo.setUserId(c.getUserId());
            vo.setStoreId(c.getStoreId());
            vo.setDishId(c.getDishId());
            vo.setQuantity(c.getQuantity());
            Dish d = dishMap.get(c.getDishId());
            if (d != null) {
                vo.setDishName(d.getName());
                vo.setPrice(d.getPrice());
                vo.setDishImage(d.getImage());
            }
            result.add(vo);
        }
        System.out.println("返回的购物车数据：" + result.size());
        return result;
    }

    @Override
    @Transactional
    public void add(Long userId, Long storeId, Long dishId, int quantity) {
        System.out.println("=== 添加购物车，userId: " + userId + ", storeId: " + storeId + ", dishId: " + dishId + ", quantity: " + quantity);
        Dish d = dishMapper.selectById(dishId);
        if (d == null || d.getStatus() != 1) throw new RuntimeException("菜品不存在或已下架");
        if (!d.getStoreId().equals(storeId)) throw new RuntimeException("店铺不匹配");

        // 先查询是否存在（包含 deleted=0 条件）
        Cart existing = cartMapper.selectOne(new LambdaQueryWrapper<Cart>()
                .eq(Cart::getUserId, userId)
                .eq(Cart::getStoreId, storeId)
                .eq(Cart::getDishId, dishId));
        
        System.out.println("已存在的购物车记录：" + (existing != null ? "是，ID=" + existing.getId() : "否"));
        
        if (existing != null) {
            // 已存在，使用 SQL 直接累加数量（原子操作）
            System.out.println("执行更新操作，累加数量");
            cartMapper.update(null, 
                new com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper<Cart>()
                    .eq("user_id", userId)
                    .eq("store_id", storeId)
                    .eq("dish_id", dishId)
                    .eq("deleted", 0)
                    .setSql("quantity = quantity + " + quantity));
        } else {
            // 不存在，尝试插入
            try {
                System.out.println("执行插入操作");
                Cart c = new Cart();
                c.setUserId(userId);
                c.setStoreId(storeId);
                c.setDishId(dishId);
                c.setQuantity(quantity);
                cartMapper.insert(c);
                System.out.println("插入成功，购物车 ID: " + c.getId());
            } catch (Exception e) {
                // 如果插入失败（并发导致已存在），则先物理删除所有旧记录再插入
                System.out.println("插入失败，错误信息：" + e.getMessage());
                if (e.getMessage().contains("Duplicate entry")) {
                    System.out.println("检测到重复键冲突，执行物理删除旧记录");
                    // 使用物理删除方法清除所有旧记录（包括软删除的）
                    cartMapper.deletePhysical(userId, storeId, dishId);
                    
                    System.out.println("已删除旧记录，重新插入");
                    Cart c = new Cart();
                    c.setUserId(userId);
                    c.setStoreId(storeId);
                    c.setDishId(dishId);
                    c.setQuantity(quantity);
                    cartMapper.insert(c);
                    System.out.println("重新插入成功，购物车 ID: " + c.getId());
                } else {
                    throw e;
                }
            }
        }
        System.out.println("=== 添加购物车完成 ===");
    }

    @Override
    public void updateQuantity(Long userId, Long cartId, int quantity) {
        Cart c = cartMapper.selectOne(new LambdaQueryWrapper<Cart>().eq(Cart::getId, cartId).eq(Cart::getUserId, userId));
        if (c != null) {
            c.setQuantity(Math.max(1, quantity));
            cartMapper.updateById(c);
        }
    }

    @Override
    @Transactional
    public void remove(Long userId, Long cartId) {
        cartMapper.delete(new LambdaQueryWrapper<Cart>().eq(Cart::getId, cartId).eq(Cart::getUserId, userId));
    }

    @Override
    @Transactional
    public void clearByStore(Long userId, Long storeId) {
        cartMapper.delete(new LambdaQueryWrapper<Cart>().eq(Cart::getUserId, userId).eq(Cart::getStoreId, storeId));
    }
}

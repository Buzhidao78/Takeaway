package com.bzy.takeaway.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bzy.takeaway.common.Constants;
import com.bzy.takeaway.common.Result;
import com.bzy.takeaway.entity.Notification;
import com.bzy.takeaway.entity.SysUser;
import com.bzy.takeaway.entity.Store;
import com.bzy.takeaway.entity.Rider;
import com.bzy.takeaway.entity.GlobalCategory;
import com.bzy.takeaway.mapper.NotificationMapper;
import com.bzy.takeaway.mapper.SysUserMapper;
import com.bzy.takeaway.mapper.StoreMapper;
import com.bzy.takeaway.mapper.RiderMapper;
import com.bzy.takeaway.mapper.GlobalCategoryMapper;
import com.bzy.takeaway.service.NotificationService;
import com.bzy.takeaway.service.GlobalCategoryService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminController {

    private final SysUserMapper userMapper;
    private final StoreMapper storeMapper;
    private final RiderMapper riderMapper;
    private final NotificationService notificationService;
    private final NotificationMapper notificationMapper;
    private final GlobalCategoryMapper globalCategoryMapper;
    private final GlobalCategoryService globalCategoryService;

    @GetMapping("/users")
    public Result<Page<SysUser>> users(@RequestParam(defaultValue = "1") int page,
                                       @RequestParam(defaultValue = "10") int size,
                                       @RequestParam(required = false) Integer role,
                                       @RequestParam(required = false) Integer deleted) {
        System.out.println("====== 收到 users 请求 ======");
        System.out.println("page: " + page + ", size: " + size + ", role: " + role + ", deleted: " + deleted);
        
        // 使用原生 SQL 绕过 MyBatis-Plus 的@TableLogic 过滤
        StringBuilder sql = new StringBuilder("SELECT * FROM sys_user WHERE 1=1");
        StringBuilder countSql = new StringBuilder("SELECT COUNT(*) FROM sys_user WHERE 1=1");
        
        if (role != null) {
            sql.append(" AND role = ").append(role);
            countSql.append(" AND role = ").append(role);
        }
        
        if (deleted != null) {
            System.out.println("查询 deleted=" + deleted + " 的用户");
            sql.append(" AND deleted = ").append(deleted);
            countSql.append(" AND deleted = ").append(deleted);
        } else {
            System.out.println("deleted 参数为 null，默认查询 deleted=0 的用户");
            sql.append(" AND deleted = 0");
            countSql.append(" AND deleted = 0");
        }
        
        sql.append(" ORDER BY create_time DESC LIMIT ").append((page - 1) * size).append(", ").append(size);
        
        System.out.println("SQL: " + sql);
        
        // 使用原生 SQL 查询
        List<SysUser> records = userMapper.selectListBySql(sql.toString());
        Long total = userMapper.selectCountBySql(countSql.toString());
        
        Page<SysUser> p = new Page<>(page, size, total);
        p.setRecords(records);
        p.getRecords().forEach(u -> u.setPassword(null));
        
        System.out.println("查询结果：" + p.getTotal() + " 条记录");
        System.out.println("====== 查询完成 ======");
        return Result.ok(p);
    }

    @GetMapping("/deleted-users")
    public Result<Page<SysUser>> deletedUsers(@RequestParam(defaultValue = "1") int page,
                                              @RequestParam(defaultValue = "10") int size,
                                              @RequestParam(required = false) Integer role) {
        var q = new LambdaQueryWrapper<SysUser>()
            .eq(SysUser::getDeleted, 1)
            .orderByDesc(SysUser::getCreateTime);
        if (role != null) q.eq(SysUser::getRole, role);
        Page<SysUser> p = userMapper.selectPage(new Page<>(page, size), q);
        p.getRecords().forEach(u -> u.setPassword(null));
        return Result.ok(p);
    }

    @PutMapping("/user/{id}/status")
    public Result<?> setUserStatus(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        SysUser u = userMapper.selectById(id);
        if (u != null) {
            u.setStatus(body.getOrDefault("status", 1));
            userMapper.updateById(u);
        }
        return Result.ok();
    }

    @DeleteMapping("/user/{id}")
    public Result<?> deleteUser(@PathVariable Long id, HttpServletRequest request) {
        System.out.println("====== 收到 DELETE 请求，用户 ID: " + id + " ======");
        
        Long currentUserId = (Long) request.getAttribute("userId");
        if (currentUserId == null) {
            System.out.println("未获取到当前登录用户 ID");
            return Result.fail("未登录");
        }
        
        System.out.println("当前登录用户 ID: " + currentUserId);
        
        if (currentUserId.equals(id)) {
            System.out.println("⚠️ 用户尝试注销自己，已拒绝");
            return Result.fail("不能注销自己的账号");
        }
        
        SysUser u = userMapper.selectById(id);
        if (u != null) {
            System.out.println("删除用户：" + u.getNickname() + " (id=" + id + ")");
            
            if (u.getRole() == 2) {
                long adminCount = userMapper.selectCount(
                    new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getRole, 2)
                        .eq(SysUser::getDeleted, 0)
                );
                System.out.println("当前管理员数量：" + adminCount);
                if (adminCount <= 1) {
                    System.out.println("⚠️ 尝试注销最后一个管理员，已拒绝");
                    return Result.fail("至少需要保留一个管理员账号");
                }
            }
            
            // 检查骑手记录（骑手用户的 role=0，通过 rider 表判断）
            // 参考商家注销逻辑：不物理删除骑手记录，保留审核历史
            Rider rider = riderMapper.selectOne(new LambdaQueryWrapper<Rider>()
                .eq(Rider::getUserId, id)
                .orderByDesc(Rider::getCreateTime)
                .last("LIMIT 1"));
            if (rider != null) {
                System.out.println(">>> 检测到骑手，保留骑手记录 ID=" + rider.getId() + " 以保留审核历史");
            }
            
            String originalPhone = u.getPhone();
            String shortId = java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 8);
            u.setPhone("DEL_" + shortId + "_" + originalPhone.substring(0, Math.min(7, originalPhone.length())));
            u.setNickname("已注销用户");
            
            // 先更新其他字段
            int result = userMapper.updateById(u);
            
            // 然后使用 SQL 直接更新 deleted 字段（绕过@TableLogic）
            if (result > 0) {
                userMapper.update(null, 
                    new LambdaUpdateWrapper<SysUser>()
                        .eq(SysUser::getId, id)
                        .set(SysUser::getDeleted, 1)
                );
                System.out.println(">>> 已设置 deleted=1");
            }
            
            System.out.println(">>> 手机号已从 " + originalPhone + " 改为 " + u.getPhone());
            System.out.println("删除结果：" + result + " 行");
            System.out.println("====== 删除完成 ======");
            return result > 0 ? Result.ok() : Result.fail("删除失败");
        }
        System.out.println("用户不存在：" + id);
        return Result.fail("用户不存在");
    }

    @GetMapping("/stores")
    public Result<Page<Store>> stores(@RequestParam(defaultValue = "1") int page,
                                      @RequestParam(defaultValue = "10") int size,
                                      @RequestParam(required = false) Integer auditStatus) {
        var q = new LambdaQueryWrapper<Store>().orderByDesc(Store::getCreateTime);
        if (auditStatus != null) q.eq(Store::getAuditStatus, auditStatus);
        return Result.ok(storeMapper.selectPage(new Page<>(page, size), q));
    }

    @PutMapping("/store/{id}/audit")
    public Result<?> auditStore(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Store s = storeMapper.selectById(id);
        if (s != null) {
            Integer auditStatus = (Integer) body.getOrDefault("auditStatus", 1);
            String rejectReason = (String) body.get("rejectReason");
            
            s.setAuditStatus(auditStatus);
            
            if (auditStatus == 2 && rejectReason != null && !rejectReason.isEmpty()) {
                s.setRejectReason(rejectReason);
            } else if (auditStatus == 1) {
                s.setRejectReason(null);
                SysUser user = userMapper.selectOne(
                    new LambdaQueryWrapper<SysUser>().eq(SysUser::getId, s.getUserId())
                );
                if (user != null) {
                    user.setStatus(1);
                    userMapper.updateById(user);
                }
            }
            
            storeMapper.updateById(s);
            
            try {
                String title = auditStatus == 1 ? "商家审核通过" : "商家审核拒绝";
                String content = auditStatus == 1 
                    ? String.format("恭喜！您的商家「%s」已通过审核，可以开始营业了", s.getName())
                    : String.format("很遗憾，您的商家「%s」审核未通过。原因：%s", s.getName(), s.getRejectReason() != null ? s.getRejectReason() : "未提供");
                notificationService.sendNotification(s.getUserId(), title, content, "audit", s.getId());
            } catch (Exception e) {
                log.warn("发送商家审核结果通知失败: {}", e.getMessage());
            }
            
            try {
                notificationMapper.update(null, new LambdaUpdateWrapper<Notification>()
                    .eq(Notification::getType, "audit")
                    .eq(Notification::getRelatedId, s.getId())
                    .eq(Notification::getAuditStatus, 0)
                    .set(Notification::getAuditStatus, auditStatus));
            } catch (Exception e) {
                log.warn("更新通知审核状态失败: {}", e.getMessage());
            }
        }
        return Result.ok();
    }

    @GetMapping("/categories")
    public Result<Page<GlobalCategory>> categories(@RequestParam(defaultValue = "1") int page,
                                                    @RequestParam(defaultValue = "10") int size) {
        var q = new LambdaQueryWrapper<GlobalCategory>().orderByAsc(GlobalCategory::getSort);
        return Result.ok(globalCategoryMapper.selectPage(new Page<>(page, size), q));
    }

    @GetMapping("/category/all")
    public Result<List<GlobalCategory>> categoryAll() {
        return Result.ok(globalCategoryService.listAll());
    }

    @PostMapping("/category")
    public Result<?> addCategory(@RequestBody GlobalCategory category) {
        globalCategoryMapper.insert(category);
        return Result.ok();
    }

    @PutMapping("/category/{id}")
    public Result<?> updateCategory(@PathVariable Long id, @RequestBody GlobalCategory category) {
        category.setId(id);
        globalCategoryMapper.updateById(category);
        return Result.ok();
    }

    @DeleteMapping("/category/{id}")
    public Result<?> deleteCategory(@PathVariable Long id) {
        GlobalCategory c = globalCategoryMapper.selectById(id);
        if (c != null) {
            c.setDeleted(1);
            globalCategoryMapper.updateById(c);
        }
        return Result.ok();
    }

    @PutMapping("/category/{id}/sort")
    public Result<?> updateCategorySort(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Integer sort = (Integer) body.get("sort");
        GlobalCategory c = globalCategoryMapper.selectById(id);
        if (c != null) {
            c.setSort(sort);
            globalCategoryMapper.updateById(c);
        }
        return Result.ok();
    }

    @GetMapping("/store/{storeId}/categories")
    public Result<List<Long>> getStoreCategories(@PathVariable Long storeId) {
        return Result.ok(globalCategoryService.getStoreCategoryIds(storeId));
    }

    @PostMapping("/store/{storeId}/categories")
    public Result<?> bindStoreCategories(@PathVariable Long storeId, @RequestBody List<Long> categoryIds) {
        globalCategoryService.bindStoreCategories(storeId, categoryIds);
        return Result.ok();
    }
}

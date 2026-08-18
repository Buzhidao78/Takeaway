package com.bzy.takeaway.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bzy.takeaway.common.Constants;
import com.bzy.takeaway.entity.*;
import com.bzy.takeaway.mapper.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 初始化管理员和测试数据
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitRunner implements ApplicationRunner {

    private final SysUserMapper userMapper;
    private final StoreMapper storeMapper;
    private final CategoryMapper categoryMapper;
    private final DishMapper dishMapper;
    private final BannerMapper bannerMapper;

    @Override
    public void run(ApplicationArguments args) {
        if (userMapper.selectCount(new LambdaQueryWrapper<SysUser>().eq(SysUser::getRole, Constants.ROLE_ADMIN)) == 0) {
            SysUser admin = new SysUser();
            admin.setPhone("13800000000");
            admin.setPassword(new BCryptPasswordEncoder().encode("123456"));
            admin.setNickname("超级管理员");
            admin.setRole(Constants.ROLE_ADMIN);
            admin.setStatus(1);
            userMapper.insert(admin);
            log.info("已创建管理员 13800000000 / 123456");
        }
        if (userMapper.selectCount(new LambdaQueryWrapper<SysUser>().eq(SysUser::getPhone, "13900000000")) == 0) {
            SysUser merchant = new SysUser();
            merchant.setPhone("13900000000");
            merchant.setPassword(new BCryptPasswordEncoder().encode("123456"));
            merchant.setNickname("测试商家");
            merchant.setRole(Constants.ROLE_MERCHANT);
            merchant.setStatus(1);
            userMapper.insert(merchant);

            Store s = new Store();
            s.setUserId(merchant.getId());
            s.setName("BZY美味餐厅");
            s.setDescription("各种美食，应有尽有");
            s.setPhone("13900000000");
            s.setAddress("北京市朝阳区xxx街道");
            s.setOpenTime("09:00-22:00");
            s.setStatus(Constants.STORE_STATUS_OPEN);
            s.setAuditStatus(1);
            s.setRating(java.math.BigDecimal.valueOf(4.8));
            storeMapper.insert(s);

            for (String name : new String[]{"热销推荐", "主食", "饮料"}) {
                Category c = new Category();
                c.setStoreId(s.getId());
                c.setName(name);
                categoryMapper.insert(c);
            }
            var cats = categoryMapper.selectList(new LambdaQueryWrapper<Category>().eq(Category::getStoreId, s.getId()));
            if (cats.size() >= 3) {
                String[][] dishes = {{"招牌红烧肉", "28", "32", "0"}, {"宫保鸡丁", "22", "25", "1"}, {"扬州炒饭", "18", "20", "1"}, {"牛肉面", "25", "28", "1"}, {"珍珠奶茶", "12", "15", "2"}};
                for (String[] d : dishes) {
                    Dish dish = new Dish();
                    dish.setStoreId(s.getId());
                    dish.setCategoryId(cats.get(Integer.parseInt(d[3])).getId());
                    dish.setName(d[0]);
                    dish.setPrice(new java.math.BigDecimal(d[1]));
                    dish.setOriginPrice(new java.math.BigDecimal(d[2]));
                    dish.setStatus(1);
                    dishMapper.insert(dish);
                }
            }
            log.info("已创建测试商家 13900000000 / 123456 及店铺数据");
        }
        if (bannerMapper.selectCount(null) == 0) {
            Banner b = new Banner();
            b.setTitle("欢迎使用BZY外卖");
            b.setImage("/frontend/images/banner1.jpg");
            b.setSort(0);
            b.setStatus(1);
            bannerMapper.insert(b);
        }
    }
}

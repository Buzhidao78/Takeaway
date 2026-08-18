package com.bzy.takeaway.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bzy.takeaway.common.Result;
import com.bzy.takeaway.entity.Banner;
import com.bzy.takeaway.mapper.BannerMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/banner")
@RequiredArgsConstructor
public class BannerController {

    private final BannerMapper bannerMapper;

    @GetMapping("/list")
    public Result<List<Banner>> list() {
        return Result.ok(bannerMapper.selectList(
                new LambdaQueryWrapper<Banner>().orderByAsc(Banner::getSort)));
    }
    
    @PostMapping("/save")
    public Result<Banner> save(@RequestBody Banner banner) {
        bannerMapper.insert(banner);
        return Result.ok(banner);
    }
    
    @PostMapping("/update")
    public Result<Banner> update(@RequestBody Banner banner) {
        bannerMapper.updateById(banner);
        return Result.ok(banner);
    }
    
    @DeleteMapping("/delete/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        bannerMapper.deleteById(id);
        return Result.ok(null);
    }
}

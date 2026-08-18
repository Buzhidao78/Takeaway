package com.bzy.takeaway.controller;

import com.bzy.takeaway.common.Result;
import com.bzy.takeaway.entity.UserAddress;
import com.bzy.takeaway.service.UserAddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/address")
@RequiredArgsConstructor
public class UserAddressController {

    private final UserAddressService addressService;

    @GetMapping("/list")
    public Result<List<UserAddress>> list(@RequestAttribute Long userId) {
        return Result.ok(addressService.list(userId));
    }

    @PostMapping("/save")
    public Result<UserAddress> save(@RequestAttribute Long userId, @RequestBody UserAddress addr) {
        return Result.ok(addressService.save(userId, addr));
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@RequestAttribute Long userId, @PathVariable Long id) {
        addressService.delete(userId, id);
        return Result.ok();
    }
}

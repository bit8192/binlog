package cn.bincker.web.blog.base.controller;

import cn.bincker.web.blog.base.dto.ValueDto;
import cn.bincker.web.blog.base.entity.BaseUser;
import cn.bincker.web.blog.base.service.IBaseUserService;
import cn.bincker.web.blog.base.vo.UserDetailVo;
import cn.bincker.web.blog.base.vo.ValueVo;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${system.base-path}/users")
public class UserController {
    private final IBaseUserService baseUserService;

    public UserController(IBaseUserService baseUserService) {
        this.baseUserService = baseUserService;
    }

    @GetMapping("self-info")
    public UserDetailVo selfInfo(@NonNull BaseUser user){
        return baseUserService.getUserDetail(user);
    }

    @PatchMapping("change-password")
    public ValueVo<Boolean> changePassword(@RequestBody ValueDto<String> dto, BaseUser user){
        baseUserService.changePassword(user, dto.getValue());
        return new ValueVo<>(true);
    }
}

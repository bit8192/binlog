package cn.bincker.web.blog.base.controller;

import cn.bincker.web.blog.base.dto.ValueDto;
import cn.bincker.web.blog.base.entity.BaseUser;
import cn.bincker.web.blog.base.service.IBaseUserService;
import cn.bincker.web.blog.base.vo.BaseUserVo;
import cn.bincker.web.blog.base.vo.UserDetailVo;
import cn.bincker.web.blog.base.vo.ValueVo;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("${binlog.base-path}/users")
public class UserController {
    private final IBaseUserService baseUserService;

    public UserController(IBaseUserService baseUserService) {
        this.baseUserService = baseUserService;
    }

    @GetMapping("all")
    public List<BaseUserVo> getAll(){
        return this.baseUserService.findAll();
    }

    @GetMapping("self-info")
    public UserDetailVo selfInfo(@NonNull BaseUser user){
        return baseUserService.getUserDetail(user);
    }

    @GetMapping("bloggers")
    public List<UserDetailVo> getBloggers(){
        return baseUserService.getBloggers();
    }

    @PatchMapping("change-password")
    public ValueVo<Boolean> changePassword(@RequestBody ValueDto<String> dto,@NonNull BaseUser user){
        baseUserService.changePassword(user, dto.getValue());
        return new ValueVo<>(true);
    }

    @PatchMapping("change-head-img")
    public ValueVo<Boolean> changeHeadImg(@RequestBody ValueDto<String> dto, @NonNull BaseUser user){
        baseUserService.changeHeadImg(user, dto.getValue());
        return new ValueVo<>(true);
    }
}

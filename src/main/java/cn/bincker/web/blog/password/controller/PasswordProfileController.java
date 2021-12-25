package cn.bincker.web.blog.password.controller;

import cn.bincker.web.blog.base.annotation.ApiController;
import cn.bincker.web.blog.base.entity.BaseUser;
import cn.bincker.web.blog.password.dto.PasswordProfileDto;
import cn.bincker.web.blog.password.entity.PasswordProfile;
import cn.bincker.web.blog.password.service.IPasswordProfileService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@ApiController
@RestController
@RequestMapping("password-profile")
public class PasswordProfileController {
    private final IPasswordProfileService passwordProfileService;

    public PasswordProfileController(IPasswordProfileService passwordProfileService) {
        this.passwordProfileService = passwordProfileService;
    }

    @GetMapping
    public PasswordProfile get(Optional<BaseUser> baseUser){
        return baseUser.map(passwordProfileService::getByUser).orElse(null);
    }

    @PutMapping
    public PasswordProfile update(@Validated @RequestBody PasswordProfileDto passwordProfile, BaseUser baseUser){
        return passwordProfileService.update(baseUser, passwordProfile);
    }
}

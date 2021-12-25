package cn.bincker.web.blog.password.controller;

import cn.bincker.web.blog.base.annotation.ApiController;
import cn.bincker.web.blog.base.dto.valid.InsertValid;
import cn.bincker.web.blog.base.dto.valid.UpdateValid;
import cn.bincker.web.blog.base.entity.BaseUser;
import cn.bincker.web.blog.password.dto.PasswordGroupDto;
import cn.bincker.web.blog.password.entity.PasswordGroup;
import cn.bincker.web.blog.password.service.IPasswordGroupService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@ApiController
@RequestMapping("password-group")
public class PasswordGroupController {
    private final IPasswordGroupService passwordGroupService;

    public PasswordGroupController(IPasswordGroupService passwordGroupService) {
        this.passwordGroupService = passwordGroupService;
    }

    @GetMapping
    public Page<PasswordGroup> page(String keywords, Pageable pageable, @NonNull BaseUser baseUser){
        return passwordGroupService.page(baseUser, keywords, pageable);
    }

    @PostMapping
    public PasswordGroup add(@Validated(InsertValid.class) @RequestBody PasswordGroupDto dto){
        return passwordGroupService.add(dto);
    }

    @PutMapping
    public PasswordGroup update(@Validated(UpdateValid.class) @RequestBody PasswordGroupDto dto){
        return passwordGroupService.update(dto);
    }

    @DeleteMapping("{id}")
    public void delete(@PathVariable Long id){
        passwordGroupService.delete(id);
    }
}

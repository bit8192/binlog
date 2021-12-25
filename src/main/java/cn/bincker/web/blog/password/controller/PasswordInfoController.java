package cn.bincker.web.blog.password.controller;

import cn.bincker.web.blog.base.annotation.ApiController;
import cn.bincker.web.blog.base.dto.valid.InsertValid;
import cn.bincker.web.blog.base.dto.valid.UpdateValid;
import cn.bincker.web.blog.base.entity.BaseUser;
import cn.bincker.web.blog.password.dto.PasswordInfoDto;
import cn.bincker.web.blog.password.entity.PasswordInfo;
import cn.bincker.web.blog.password.service.IPasswordInfoService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@ApiController
@RequestMapping("password-info")
@RestController
public class PasswordInfoController {
    private final IPasswordInfoService passwordInfoService;

    public PasswordInfoController(IPasswordInfoService passwordInfoService) {
        this.passwordInfoService = passwordInfoService;
    }

    @GetMapping
    public Page<PasswordInfo> page(@RequestParam Long groupId, String keywords, Pageable pageable, @NonNull BaseUser baseUser){
        return passwordInfoService.page(baseUser, groupId, keywords, pageable);
    }

    @PostMapping
    public PasswordInfo add(@Validated(InsertValid.class) @RequestBody PasswordInfoDto passwordInfo){
        return passwordInfoService.add(passwordInfo);
    }

    @PutMapping
    public PasswordInfo update(@Validated(UpdateValid.class) @RequestBody PasswordInfoDto dto){
        return passwordInfoService.update(dto);
    }

    @PatchMapping("/{id}/use-times")
    public void updateUseTimes(@PathVariable Long id){
        passwordInfoService.updateUseTimes(id);
    }

    @DeleteMapping("{id}")
    public void delete(@PathVariable Long id){
        passwordInfoService.delete(id);
    }
}

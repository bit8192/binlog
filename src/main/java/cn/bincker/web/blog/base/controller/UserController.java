package cn.bincker.web.blog.base.controller;

import cn.bincker.web.blog.base.annotation.ApiController;
import cn.bincker.web.blog.base.config.SystemProfile;
import cn.bincker.web.blog.base.constant.SessionKeyConstant;
import cn.bincker.web.blog.base.dto.BaseUserDto;
import cn.bincker.web.blog.base.dto.ValueDto;
import cn.bincker.web.blog.base.entity.BaseUser;
import cn.bincker.web.blog.base.exception.BadRequestException;
import cn.bincker.web.blog.base.exception.NotFoundException;
import cn.bincker.web.blog.base.service.IBaseUserService;
import cn.bincker.web.blog.base.vo.BaseUserVo;
import cn.bincker.web.blog.base.vo.UserDetailVo;
import cn.bincker.web.blog.base.vo.ValueVo;
import org.springframework.lang.NonNull;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.List;

@RestController
@RequestMapping("/users")
@ApiController
public class UserController {
    private final IBaseUserService baseUserService;
    private final SystemProfile systemProfile;

    public UserController(IBaseUserService baseUserService, SystemProfile systemProfile) {
        this.baseUserService = baseUserService;
        this.systemProfile = systemProfile;
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

    @GetMapping("username-occupied")
    public ValueVo<Boolean> usernameOccupied(String username){
        if(!StringUtils.hasText(username)) return new ValueVo<>(true);
        return new ValueVo<>(baseUserService.findByUsername(username).isPresent());
    }

    @PostMapping
    public ValueVo<Boolean> register(@RequestBody @Validated BaseUserDto dto, HttpSession session){
        if(!systemProfile.getAllowRegister()) throw new NotFoundException();
        if(!StringUtils.hasText(dto.getPassword()) && !StringUtils.hasText(dto.getQqOpenId()) && !StringUtils.hasText(dto.getWechatOpenId()) && !StringUtils.hasText(dto.getGithub())){
            throw new BadRequestException("若未绑定第三方帐号，需要设置登录密码");
        }
//        不用前端传来的Github帐号
        if(StringUtils.hasText(dto.getGithub())){
            var github = (String) session.getAttribute(SessionKeyConstant.OAUTH2_AUTHORIZE_GITHUB);
            dto.setGithub(github);
            session.removeAttribute(SessionKeyConstant.OAUTH2_AUTHORIZE_GITHUB);
        }
        if(StringUtils.hasText(dto.getQqOpenId())){
            var qqOpenId = (String) session.getAttribute(SessionKeyConstant.OAUTH2_AUTHORIZE_QQ_OPENID);
            dto.setQqOpenId(qqOpenId);
            session.removeAttribute(SessionKeyConstant.OAUTH2_AUTHORIZE_QQ_OPENID);
        }
        baseUserService.register(dto);
        return new ValueVo<>(true);
    }
}

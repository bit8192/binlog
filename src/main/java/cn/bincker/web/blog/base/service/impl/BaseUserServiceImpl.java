package cn.bincker.web.blog.base.service.impl;

import cn.bincker.web.blog.base.dto.BaseUserDto;
import cn.bincker.web.blog.base.entity.AuthorizationUser;
import cn.bincker.web.blog.base.entity.BaseUser;
import cn.bincker.web.blog.base.entity.Role;
import cn.bincker.web.blog.base.event.UserActionEvent;
import cn.bincker.web.blog.base.exception.NotFoundException;
import cn.bincker.web.blog.base.exception.SystemException;
import cn.bincker.web.blog.base.repository.*;
import cn.bincker.web.blog.base.service.IBaseUserService;
import cn.bincker.web.blog.base.specification.BaseUserSpecification;
import cn.bincker.web.blog.base.vo.BaseUserVo;
import cn.bincker.web.blog.base.vo.UserDetailVo;
import cn.bincker.web.blog.material.repository.IArticleAgreeRepository;
import cn.bincker.web.blog.material.repository.IArticleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class BaseUserServiceImpl implements IBaseUserService {
    private static final Logger log = LoggerFactory.getLogger(BaseUserServiceImpl.class);
    private final IBaseUserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final IArticleRepository articleRepository;
    private final IArticleAgreeRepository articleAgreeRepository;
    private final ICommentAgreeRepository commentAgreeRepository;
    private final ICommentReplyAgreeRepository commentReplyAgreeRepository;
    private final ICommentRepository commentRepository;
    private final ICommentReplyRepository commentReplyRepository;
    private final ApplicationContext applicationContext;

    public BaseUserServiceImpl(IBaseUserRepository repository, PasswordEncoder passwordEncoder, IArticleRepository articleRepository, IArticleAgreeRepository articleAgreeRepository, ICommentAgreeRepository commentAgreeRepository, ICommentReplyAgreeRepository commentReplyAgreeRepository, ICommentRepository commentRepository, ICommentReplyRepository commentReplyRepository, ApplicationContext applicationContext) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.articleRepository = articleRepository;
        this.articleAgreeRepository = articleAgreeRepository;
        this.commentAgreeRepository = commentAgreeRepository;
        this.commentReplyAgreeRepository = commentReplyAgreeRepository;
        this.commentRepository = commentRepository;
        this.commentReplyRepository = commentReplyRepository;
        this.applicationContext = applicationContext;
    }

    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
        Optional<BaseUser> userOptional = repository.findByUsername(s);
        return new AuthorizationUser(userOptional.orElseThrow(()->new UsernameNotFoundException("用户不存在")));
    }

    @Override
    public Optional<BaseUser> findByUsername(String userName) {
        return repository.findByUsername(userName);
    }

    @Override
    public Optional<BaseUser> findByQQOpenId(String openId) {
        if(!StringUtils.hasText(openId)) return Optional.empty();
        return repository.findByQqOpenId(openId);
    }

    @Override
    public BaseUser getByUsername(String username) {
        return repository.findByUsername(username).orElse(null);
    }

    @Override
    public void changePassword(BaseUser user, String password) {
        var target = repository.findById(user.getId()).orElseThrow(NotFoundException::new);
        target.setEncodedPasswd(passwordEncoder.encode(password));
        repository.save(target);
    }

    @Override
    public UserDetailVo getUserDetail(BaseUser user) {
        var vo = new UserDetailVo(repository.findById(user.getId()).orElseThrow(NotFoundException::new));
        vo.setArticleNum(articleRepository.countByCreatedUser(user));
        var articleAgreedNum = articleAgreeRepository.countByArticleCreatedUser(user);
        var commentAgreedNum = commentAgreeRepository.countByCommentCreatedUser(user);
        var commentReplyAgreedNum = commentReplyAgreeRepository.countByCommentCreatedUser(user);
        var commentNum = commentRepository.countByCreatedUser(user);
        var replyNum = commentReplyRepository.countByCreatedUser(user);
        vo.setAgreedNum(articleAgreedNum + commentAgreedNum + commentReplyAgreedNum);
        vo.setCommentNum(commentNum + replyNum);
        return vo;
    }

    @Override
    public List<UserDetailVo> getBloggers() {
        var bloggers = repository.findAll(BaseUserSpecification.role(Role.RoleEnum.BLOGGER));
        return bloggers.stream().map(this::getUserDetail).collect(Collectors.toList());
    }

    @Override
    public void changeHeadImg(BaseUser user, String headImgUrl) {
        var target = repository.findById(user.getId()).orElseThrow(NotFoundException::new);
        target.setHeadImg(headImgUrl);
        repository.save(target);
    }

    @Override
    public List<BaseUserVo> findAll() {
        return this.repository.findAll().stream().map(BaseUserVo::new).collect(Collectors.toList());
    }

    @Override
    public List<BaseUserVo> findAllById(List<Long> ids) {
        return this.repository.findAllById(ids).stream().map(BaseUserVo::new).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void register(BaseUserDto dto) {
        var user = new BaseUser();
        user.setUsername(dto.getUsername());
        if(StringUtils.hasText(dto.getPassword()))
            user.setEncodedPasswd(passwordEncoder.encode(dto.getPassword()));
        user.setHeadImg(dto.getHeadImg());
        user.setEmail(dto.getEmail());
        user.setPhoneNum(dto.getPhoneNum());
        user.setBiography(dto.getBiography());
        user.setWebsite(dto.getWebsite());
        user.setRoles(Collections.singleton(Role.RoleEnum.VISITOR.toRole()));
        user.setQqOpenId(dto.getQqOpenId());
        user.setWechatOpenId(dto.getWechatOpenId());
        user.setGithub(dto.getGithub());
        repository.save(user);
//        注册事件
        var registerEvent = new UserActionEvent(applicationContext, user, UserActionEvent.ActionEnum.REGISTER);
        applicationContext.publishEvent(registerEvent);
//        登录
        this.login(user, UserActionEvent.ActionEnum.LOGIN_REGISTER, null);
    }

    @Override
    public Optional<BaseUser> findByGithub(String github) {
        return repository.findByGithub(github);
    }

    @Override
    public void login(BaseUser user, UserActionEvent.ActionEnum type, String additionalInfo) {
        var securityContext = SecurityContextHolder.getContext();
        if(securityContext == null) {
            log.error("登录失败，securityContext为空");
            throw new SystemException();
        }
        var authentication = new UsernamePasswordAuthenticationToken(new AuthorizationUser(user), user.getEncodedPasswd(), user.getRoles());
        securityContext.setAuthentication(authentication);
        var loginEvent = new UserActionEvent(applicationContext, user, UserActionEvent.ActionEnum.LOGIN_OAUTH2, null, additionalInfo);
        applicationContext.publishEvent(loginEvent);
    }

    @Override
    @Transactional
    public void bindGithub(BaseUser user, String github) {
        user = repository.getOne(user.getId());
        user.setGithub(github);
        repository.save(user);
        var bindEvent = new UserActionEvent(applicationContext, user, UserActionEvent.ActionEnum.BIND_ACCOUNT_OAUTH2, null, "github");
        applicationContext.publishEvent(bindEvent);
    }

    @Override
    public void bindQqOpenId(BaseUser user, String qqOpenId) {
        user = repository.getOne(user.getId());
        user.setQqOpenId(qqOpenId);
        repository.save(user);
        var bindEvent = new UserActionEvent(applicationContext, user, UserActionEvent.ActionEnum.BIND_ACCOUNT_OAUTH2, null, "qq");
        applicationContext.publishEvent(bindEvent);
    }
}

package cn.bincker.web.blog.base.service.impl;

import cn.bincker.web.blog.base.entity.AuthorizationUser;
import cn.bincker.web.blog.base.entity.BaseUser;
import cn.bincker.web.blog.base.exception.NotFoundException;
import cn.bincker.web.blog.base.repository.IBaseUserRepository;
import cn.bincker.web.blog.base.service.IBaseUserService;
import cn.bincker.web.blog.base.vo.BaseUserVo;
import cn.bincker.web.blog.base.vo.UserDetailVo;
import cn.bincker.web.blog.material.repository.IArticleAgreeRepository;
import cn.bincker.web.blog.material.repository.IArticleCommentAgreeRepository;
import cn.bincker.web.blog.material.repository.IArticleRepository;
import cn.bincker.web.blog.material.repository.IArticleSubCommentAgreeRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class BaseUserServiceImpl implements IBaseUserService {
    private final IBaseUserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final IArticleRepository articleRepository;
    private final IArticleAgreeRepository articleAgreeRepository;
    private final IArticleCommentAgreeRepository articleCommentAgreeRepository;
    private final IArticleSubCommentAgreeRepository articleSubCommentAgreeRepository;

    public BaseUserServiceImpl(IBaseUserRepository repository, PasswordEncoder passwordEncoder, IArticleRepository articleRepository, IArticleAgreeRepository articleAgreeRepository, IArticleCommentAgreeRepository articleCommentAgreeRepository, IArticleSubCommentAgreeRepository articleSubCommentAgreeRepository) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.articleRepository = articleRepository;
        this.articleAgreeRepository = articleAgreeRepository;
        this.articleCommentAgreeRepository = articleCommentAgreeRepository;
        this.articleSubCommentAgreeRepository = articleSubCommentAgreeRepository;
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
        var articleCommentAgreedNum = articleCommentAgreeRepository.countByCommentCreatedUser(user);
        var articleSubCommentAgreedNum = articleSubCommentAgreeRepository.countByCommentCreatedUser(user);
        vo.setAgreedNum(articleAgreedNum + articleCommentAgreedNum + articleSubCommentAgreedNum);
        return vo;
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
}

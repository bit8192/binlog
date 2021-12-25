package cn.bincker.web.blog.password.service.impl;

import cn.bincker.web.blog.base.config.UserAuditingListener;
import cn.bincker.web.blog.base.entity.BaseUser;
import cn.bincker.web.blog.base.exception.ForbiddenException;
import cn.bincker.web.blog.base.exception.NotFoundException;
import cn.bincker.web.blog.password.dto.PasswordInfoDto;
import cn.bincker.web.blog.password.entity.PasswordGroup;
import cn.bincker.web.blog.password.entity.PasswordInfo;
import cn.bincker.web.blog.password.repository.IPasswordGroupRepository;
import cn.bincker.web.blog.password.repository.IPasswordInfoRepository;
import cn.bincker.web.blog.password.service.IPasswordInfoService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class PasswordInfoServiceImpl implements IPasswordInfoService {
    private final IPasswordInfoRepository passwordInfoRepository;
    private final IPasswordGroupRepository passwordGroupRepository;
    private final UserAuditingListener userAuditingListener;

    public PasswordInfoServiceImpl(IPasswordInfoRepository passwordInfoRepository, IPasswordGroupRepository passwordGroupRepository, UserAuditingListener userAuditingListener) {
        this.passwordInfoRepository = passwordInfoRepository;
        this.passwordGroupRepository = passwordGroupRepository;
        this.userAuditingListener = userAuditingListener;
    }

    @Override
    public Page<PasswordInfo> page(BaseUser user, Long passwordGroupId, String keywords, Pageable pageable) {
        return passwordInfoRepository.findAllByPasswordGroupIdAndCreatedUserAndTitleLike(passwordGroupId, user, "%" + keywords + "%", pageable);
    }

    @Override
    public PasswordInfo add(PasswordInfoDto dto) {
        var currentUser = userAuditingListener.getCurrentAuditor().orElseThrow(ForbiddenException::new);
        var passwordGroup = passwordGroupRepository.findById(dto.getPasswordGroupId()).orElseThrow(()->new NotFoundException("分组不存在"));
        if(!passwordGroup.getCreatedUser().getId().equals(currentUser.getId())) throw new NotFoundException("分组不存在");
        var target = new PasswordInfo();
        copyProperty(dto, passwordGroup, target);
        return passwordInfoRepository.save(target);
    }

    @Override
    public PasswordInfo update(PasswordInfoDto dto) {
        var currentUser = userAuditingListener.getCurrentAuditor().orElseThrow(ForbiddenException::new);
        var passwordGroup = passwordGroupRepository.findById(dto.getPasswordGroupId()).orElseThrow(()->new NotFoundException("分组不存在"));
        if(!passwordGroup.getCreatedUser().getId().equals(currentUser.getId())) throw new NotFoundException("分组不存在");
        var target = passwordInfoRepository.findById(dto.getId()).orElseThrow(NotFoundException::new);
        copyProperty(dto, passwordGroup, target);
        return passwordInfoRepository.save(target);
    }

    private void copyProperty(PasswordInfoDto dto, PasswordGroup passwordGroup, PasswordInfo target) {
        target.setPasswordGroup(passwordGroup);
        target.setEncodedPassword(dto.getEncodedPassword());
        target.setRemark(dto.getRemark());
        target.setTitle(dto.getTitle());
        target.setUsername(dto.getUsername());
        target.setUrl(dto.getUrl());
        target.setEncryptionRemark(dto.getEncryptionRemark());
    }

    @Override
    public void delete(Long id) {
        var target = passwordInfoRepository.findById(id);
        if(target.isEmpty()) return;
        if(!target.get().getCreatedUser().getId().equals(userAuditingListener.getCurrentAuditor().map(BaseUser::getId).orElseThrow(ForbiddenException::new)))
            throw new ForbiddenException();
        passwordInfoRepository.deleteById(id);
    }

    @Override
    public void updateUseTimes(Long id) {
        var target = passwordInfoRepository.findById(id).orElseThrow(NotFoundException::new);
        //密码一般只有一个人在使用，所以不考虑锁
        target.setUseTimes(target.getUseTimes() + 1);
        passwordInfoRepository.save(target);
        var passwordGroup = target.getPasswordGroup();
        passwordGroup.setUseTimes(passwordGroup.getUseTimes() + 1);
        passwordGroupRepository.save(passwordGroup);
    }
}

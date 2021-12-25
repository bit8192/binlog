package cn.bincker.web.blog.password.service.impl;

import cn.bincker.web.blog.base.config.UserAuditingListener;
import cn.bincker.web.blog.base.entity.BaseUser;
import cn.bincker.web.blog.base.exception.ForbiddenException;
import cn.bincker.web.blog.base.exception.NotFoundException;
import cn.bincker.web.blog.password.dto.PasswordProfileDto;
import cn.bincker.web.blog.password.entity.PasswordProfile;
import cn.bincker.web.blog.password.repository.IPasswordProfileRepository;
import cn.bincker.web.blog.password.service.IPasswordProfileService;
import org.springframework.stereotype.Service;

@Service
public class PasswordProfileServiceImpl implements IPasswordProfileService {
    private final IPasswordProfileRepository passwordProfileRepository;
    private final UserAuditingListener userAuditingListener;

    public PasswordProfileServiceImpl(IPasswordProfileRepository passwordProfileRepository, UserAuditingListener userAuditingListener) {
        this.passwordProfileRepository = passwordProfileRepository;
        this.userAuditingListener = userAuditingListener;
    }

    @Override
    public PasswordProfile getByUser(BaseUser user) {
        return passwordProfileRepository.findByCreatedUser(user).orElse(null);
    }

    @Override
    public PasswordProfile update(BaseUser user, PasswordProfileDto dto) {
        var target = dto.getId() == null ? new PasswordProfile() : passwordProfileRepository.findById(dto.getId()).orElseThrow(NotFoundException::new);
        if(target.getCreatedUser() != null && !target.getCreatedUser().getId().equals(userAuditingListener.getCurrentAuditor().map(BaseUser::getId).orElseThrow(ForbiddenException::new)))
            throw new ForbiddenException();
        target.setInputConvert(dto.getInputConvertJs());
        return passwordProfileRepository.save(target);
    }
}

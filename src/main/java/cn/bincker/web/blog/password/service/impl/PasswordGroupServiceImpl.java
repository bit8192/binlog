package cn.bincker.web.blog.password.service.impl;

import cn.bincker.web.blog.base.config.UserAuditingListener;
import cn.bincker.web.blog.base.entity.BaseUser;
import cn.bincker.web.blog.base.exception.ForbiddenException;
import cn.bincker.web.blog.base.exception.NotFoundException;
import cn.bincker.web.blog.base.exception.SystemException;
import cn.bincker.web.blog.password.dto.PasswordGroupDto;
import cn.bincker.web.blog.password.entity.PasswordGroup;
import cn.bincker.web.blog.password.repository.IPasswordGroupRepository;
import cn.bincker.web.blog.password.service.IPasswordGroupService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class PasswordGroupServiceImpl implements IPasswordGroupService {
    private final IPasswordGroupRepository passwordGroupRepository;
    private final UserAuditingListener userAuditingListener;

    public PasswordGroupServiceImpl(IPasswordGroupRepository passwordGroupRepository, UserAuditingListener userAuditingListener) {
        this.passwordGroupRepository = passwordGroupRepository;
        this.userAuditingListener = userAuditingListener;
    }

    @Override
    public Page<PasswordGroup> page(BaseUser baseUser, String keywords, Pageable pageable) {
        if(keywords == null) keywords = "";
        return passwordGroupRepository.findAllByCreatedUserAndTitleLike(baseUser, "%" + keywords + "%", pageable);
    }

    @Override
    public PasswordGroup add(PasswordGroupDto dto) {
        var target = new PasswordGroup();
        target.setTitle(dto.getTitle());
        target.setRemark(dto.getRemark());
        return passwordGroupRepository.save(target);
    }

    @Override
    public PasswordGroup update(PasswordGroupDto dto) {
        var target = passwordGroupRepository.findById(dto.getId()).orElseThrow(NotFoundException::new);
        //只有自己可以修改密码
        if(!target.getCreatedUser().getId().equals(userAuditingListener.getCurrentAuditor().map(BaseUser::getId).orElseThrow(ForbiddenException::new))) throw new ForbiddenException();
        target.setTitle(dto.getTitle());
        target.setRemark(dto.getRemark());
        return passwordGroupRepository.save(target);
    }

    @Override
    public void delete(Long id) {
        var target = passwordGroupRepository.findById(id);
        if(target.isEmpty()) return;
        //只有自己可以删除
        if(!target.get().getCreatedUser().getId().equals(userAuditingListener.getCurrentAuditor().map(BaseUser::getId).orElseThrow(ForbiddenException::new)))
            throw new ForbiddenException();
        if(target.get().getPasswordInfoList().size() > 0) throw new SystemException("请先删除该分组下的所有密码");
        passwordGroupRepository.deleteById(id);
    }
}

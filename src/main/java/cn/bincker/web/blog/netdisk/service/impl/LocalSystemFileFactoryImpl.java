package cn.bincker.web.blog.netdisk.service.impl;

import cn.bincker.web.blog.netdisk.entity.NetDiskFile;
import cn.bincker.web.blog.netdisk.repository.INetDiskFileRepository;
import cn.bincker.web.blog.netdisk.service.ISystemFile;
import cn.bincker.web.blog.netdisk.service.ISystemFileFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@ConditionalOnProperty(value = "netdisk.type", havingValue = "Local")
public class LocalSystemFileFactoryImpl implements ISystemFileFactory {
    private final INetDiskFileRepository netDiskFileRepository;

    public LocalSystemFileFactoryImpl(INetDiskFileRepository netDiskFileRepository) {
        this.netDiskFileRepository = netDiskFileRepository;
    }

    @Override
    public ISystemFile fromPath(String child) {
        return new LocalSystemFileImpl(child);
    }

    @Override
    public ISystemFile fromPath(String path, String child) {
        return new LocalSystemFileImpl(path, child);
    }

    @Override
    public ISystemFile fromNetDiskFile(NetDiskFile netDiskFile) {
        return new LocalSystemFileImpl(netDiskFile);
    }

    @Override
    public Optional<LocalSystemFileImpl> fromNetDiskFileId(Long id) {
        return netDiskFileRepository.findById(id).map(LocalSystemFileImpl::new);
    }
}

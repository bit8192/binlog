package cn.bincker.web.blog.base.service;

import cn.bincker.web.blog.base.vo.IpAddressVo;

import java.util.Optional;

public interface IIpAddressQueryService {
    Optional<IpAddressVo> query(String ip);
}

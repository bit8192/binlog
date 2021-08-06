package cn.bincker.web.blog.base.service;

import cn.bincker.web.blog.base.dto.CommentDto;
import cn.bincker.web.blog.base.vo.CommentVo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ILeftMessageService {
    Page<CommentVo> getLeftMessagePage(Pageable pageable);

    CommentVo leavingMessage(CommentDto dto);
}

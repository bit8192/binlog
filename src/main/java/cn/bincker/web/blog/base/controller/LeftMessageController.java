package cn.bincker.web.blog.base.controller;

import cn.bincker.web.blog.base.dto.CommentDto;
import cn.bincker.web.blog.base.service.ILeftMessageService;
import cn.bincker.web.blog.base.vo.CommentVo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/left-messages")
public class LeftMessageController {
    private final ILeftMessageService leftMessageService;

    public LeftMessageController(ILeftMessageService leftMessageService) {
        this.leftMessageService = leftMessageService;
    }

    @GetMapping
    public Page<CommentVo> getPage(@PageableDefault(sort = "createdDate", direction = Sort.Direction.DESC) Pageable pageable){
        return leftMessageService.getLeftMessagePage(pageable);
    }

    @PostMapping
    public CommentVo leavingMessage(@RequestBody @Validated CommentDto commentDto){
        return leftMessageService.leavingMessage(commentDto);
    }
}

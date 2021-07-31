package cn.bincker.web.blog.base.controller;

import cn.bincker.web.blog.base.dto.LeftMessageDto;
import cn.bincker.web.blog.base.service.ILeftMessageService;
import cn.bincker.web.blog.base.vo.LeftMessageVo;
import cn.bincker.web.blog.base.vo.ValueVo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${binlog.base-path}/left-messages")
public class LeftMessageController {
    private final ILeftMessageService leftMessageService;

    public LeftMessageController(ILeftMessageService leftMessageService) {
        this.leftMessageService = leftMessageService;
    }

    @GetMapping
    public Page<LeftMessageVo> page(@PageableDefault(sort = "createdDate", direction = Sort.Direction.DESC) Pageable pageable){
        return leftMessageService.getPage(pageable);
    }

    @GetMapping("{id}/reply")
    public Page<LeftMessageVo> replyPage(@PathVariable Long id, @PageableDefault(sort = "createdDate", direction = Sort.Direction.DESC) Pageable pageable){
        return leftMessageService.getReplyPage(id, pageable);
    }

    @PostMapping
    public LeftMessageVo leavingMessage(@RequestBody @Validated LeftMessageDto dto){
        return leftMessageService.leavingMessage(dto);
    }

    @PostMapping("{id}/reply")
    public LeftMessageVo replyLeftMessage(@PathVariable Long id, @RequestBody @Validated LeftMessageDto dto){
        return leftMessageService.replyLeftMessage(id, dto);
    }

    @DeleteMapping("{id}")
    public void removeLeftMessage(@PathVariable Long id){
        leftMessageService.removeLeftMessage(id);
    }

    @DeleteMapping("reply/{id}")
    public void removeReply(@PathVariable Long id){
        leftMessageService.removeLeftMessageReply(id);
    }

    @PostMapping("{id}/toggle-agree")
    public ValueVo<Boolean> toggleLeftMessageAgree(@PathVariable Long id){
        return leftMessageService.toggleLeftMessageAgree(id);
    }

    @PostMapping("{id}/toggle-tread")
    public ValueVo<Boolean> toggleLeftMessageTread(@PathVariable Long id){
        return leftMessageService.toggleLeftMessageTread(id);
    }

    @PostMapping("reply/{id}/toggle-agree")
    public ValueVo<Boolean> toggleLeftMessageReplyAgree(@PathVariable Long id){
        return leftMessageService.toggleLeftMessageReplyAgree(id);
    }

    @PostMapping("reply/{id}/toggle-tread")
    public ValueVo<Boolean> toggleLeftMessageReplyTread(@PathVariable Long id){
        return leftMessageService.toggleLeftMessageReplyTread(id);
    }
}

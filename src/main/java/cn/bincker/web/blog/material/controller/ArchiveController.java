package cn.bincker.web.blog.material.controller;

import cn.bincker.web.blog.base.annotation.ApiController;
import cn.bincker.web.blog.material.service.IArticleService;
import cn.bincker.web.blog.material.vo.ArchiveVo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@ApiController
@RequestMapping("archives")
public class ArchiveController {
    private final IArticleService articleService;

    public ArchiveController(IArticleService articleService) {
        this.articleService = articleService;
    }

    @GetMapping
    public Page<ArchiveVo> page(@PageableDefault(sort = "createdDate", direction = Sort.Direction.DESC)Pageable pageable){
        return articleService.getArchivePage(pageable);
    }
}

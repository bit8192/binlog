package cn.bincker.web.blog.base.controller;

import cn.bincker.web.blog.material.service.IArticleService;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class IndexController {
    private final IArticleService articleService;

    public IndexController(IArticleService articleService) {
        this.articleService = articleService;
    }

    @GetMapping("index")
    public String index(
            String keywords,
            Long articleClassId,
            Long[] tagIds,
            Pageable pageable,
            Model model){
        model.addAttribute("page", articleService.pageAll(keywords, articleClassId, tagIds, pageable));
        return "index";
    }

    @GetMapping("about")
    public String about(){
        return "about";
    }
}

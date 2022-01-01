package cn.bincker.web.blog.material.controller;

import cn.bincker.web.blog.material.service.IArticleService;
import com.github.rjeschke.txtmark.Processor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("article")
public class ArticleWebController {
    private final IArticleService articleService;

    public ArticleWebController(IArticleService articleService) {
        this.articleService = articleService;
    }

    @GetMapping("{id}")
    public String detail(@PathVariable Long id, Model model){
        var article = articleService.getDetail(id);
        article.setContent(Processor.process(article.getContent()));
        model.addAttribute("article", article);
        return "article";
    }
}

package cn.bincker.web.blog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class WebBlogApplication {

    public static void main(String[] args) {
        SpringApplication.run(WebBlogApplication.class, args);
    }

}

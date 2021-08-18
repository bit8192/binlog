package cn.bincker.web.blog.security.machine;

import lombok.Data;

import java.awt.image.BufferedImage;
import java.io.Serializable;

@Data
public class VerifyQuestion<A extends Serializable> {
    private A answer;
    private BufferedImage question;
    private Long expireTime;

    public VerifyQuestion(A answer, BufferedImage question, Long expireTime) {
        this.answer = answer;
        this.question = question;
        this.expireTime = expireTime;
    }

    public VerifyQuestion(A answer, BufferedImage question){
        this(answer, question, System.currentTimeMillis() + 60000L * 10);
    }
}

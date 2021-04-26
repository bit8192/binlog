package cn.bincker.web.blog.security.machine;

import lombok.Data;

import java.awt.image.BufferedImage;
import java.io.Serializable;

@Data
public class VerifyQuestion<A extends Serializable> {
    private A answer;
    private BufferedImage question;

    public VerifyQuestion(A answer, BufferedImage question) {
        this.answer = answer;
        this.question = question;
    }
}

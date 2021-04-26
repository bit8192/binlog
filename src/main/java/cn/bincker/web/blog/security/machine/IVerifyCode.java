package cn.bincker.web.blog.security.machine;

import org.springframework.http.MediaType;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;

public interface IVerifyCode<A extends Serializable> {
    String SESSION_ATTRIBUTE_ANSWER = IVerifyCode.class.getName() + ".SESSION_ATTRIBUTE_ANSWER";
    default void write(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType(MediaType.IMAGE_JPEG_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        VerifyQuestion<?> verifyQuestion = generate();
        request.getSession().setAttribute(SESSION_ATTRIBUTE_ANSWER, verifyQuestion);
        ImageIO.write(verifyQuestion.getQuestion(), "jpeg", response.getOutputStream());
    }
    VerifyQuestion<A> generate();
    boolean verify(HttpServletRequest request);
}

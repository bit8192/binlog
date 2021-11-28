package cn.bincker.web.blog.base.controller;

import cn.bincker.web.blog.base.annotation.ApiController;
import cn.bincker.web.blog.security.machine.IVerifyCode;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RestController
@ApiController
public class VerifyCodeController {
    private final IVerifyCode<?> verifyCode;

    public VerifyCodeController(IVerifyCode<?> verifyCode) {
        this.verifyCode = verifyCode;
    }

    @GetMapping(value = "verify-code", produces = MediaType.IMAGE_JPEG_VALUE)
    public void verifyCode(HttpServletRequest request, HttpServletResponse response) throws IOException {
        verifyCode.write(request, response);
    }
}

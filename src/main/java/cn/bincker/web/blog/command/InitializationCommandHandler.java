package cn.bincker.web.blog.command;

import cn.bincker.web.blog.base.entity.BaseUser;
import cn.bincker.web.blog.base.entity.Role;
import cn.bincker.web.blog.base.repository.IBaseUserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
public class InitializationCommandHandler implements CommandLineRunner {
    private final ApplicationContext context;
    private final IBaseUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public InitializationCommandHandler(ApplicationContext context, IBaseUserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.context = context;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (!CommandUtils.hasParameter(args, "--init", "-i")) return;
        BaseUser user = new BaseUser();
        user.setUsername("admin");
        user.setEncodedPasswd(passwordEncoder.encode("123456"));
        user.setHeadImg("");
        user.setEmail("");
        user.setPhoneNum("");
        user.setRoles(Collections.singleton(Role.RoleEnum.ADMIN.toRole()));
        user.setLocked(false);
        userRepository.save(user);
        SpringApplication.exit(context);
    }
}

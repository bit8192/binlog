package cn.bincker.web.blog.base.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;

@Data
public class Role  implements GrantedAuthority {
    private String name;

    @JsonIgnore
    private String code;

    @JsonIgnore
    @Override
    public String getAuthority() {
        return "ROLE_" + code;
    }

    public enum RoleEnum{
        ADMIN("管理员"),
        BLOGGER("博主"),
        VISITOR("游客");
        @Getter
        private final String name;

        RoleEnum(String name) {
            this.name = name;
        }

        public Role toRole(){
            Role role = new Role();
            role.name = name;
            role.code = this.toString();
            return role;
        }
    }
}

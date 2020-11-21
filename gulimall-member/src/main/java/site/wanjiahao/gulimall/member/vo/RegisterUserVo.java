package site.wanjiahao.gulimall.member.vo;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Data
public class RegisterUserVo {

    @NotBlank(message = "用户名不能为空")
    @Length(min = 6, max = 12, message = "用户名长度在6-12位之间")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Length(min = 6, max = 12, message = "密码长度在6-12位之间")
    private String password;

    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1(?:3\\d|4[4-9]|5[0-35-9]|6[67]|7[013-8]|8\\d|9\\d)\\d{8}$")
    private String phone;

    @NotBlank
    @Length(min = 6, max = 6, message = "验证码长度必须为6位")
    private String code;
}

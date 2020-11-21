package site.wanjiahao.gulimall.member.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import site.wanjiahao.common.constant.WeiBoConstant;
import site.wanjiahao.common.utils.PageUtils;
import site.wanjiahao.common.utils.Query;
import site.wanjiahao.gulimall.member.dao.MemberDao;
import site.wanjiahao.gulimall.member.entity.MemberEntity;
import site.wanjiahao.gulimall.member.exception.PasswordErrorException;
import site.wanjiahao.gulimall.member.exception.UsernameExistException;
import site.wanjiahao.gulimall.member.exception.UsernameUnknownException;
import site.wanjiahao.gulimall.member.service.MemberService;
import site.wanjiahao.gulimall.member.vo.Auth2WeiboResponseVo;
import site.wanjiahao.gulimall.member.vo.RegisterUserVo;

import java.util.Date;
import java.util.Map;


@Service("memberService")
@Slf4j
public class MemberServiceImpl extends ServiceImpl<MemberDao, MemberEntity> implements MemberService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<MemberEntity> page = this.page(
                new Query<MemberEntity>().getPage(params),
                new QueryWrapper<MemberEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void register(RegisterUserVo registerUserVo) {
        // 查询当前用户名是否已经存在 包括当前手机号
        Integer count = baseMapper.queryExist(registerUserVo.getUsername(), registerUserVo.getPhone());
        if (count.equals(0)) {
            // 保存用户
            MemberEntity memberEntity = new MemberEntity();
            memberEntity.setUsername(registerUserVo.getUsername());
            // 密码编码
            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
            memberEntity.setPassword(encoder.encode(registerUserVo.getPassword()));
            memberEntity.setMobile(registerUserVo.getPhone());
            memberEntity.setCreateTime(new Date());
            baseMapper.insert(memberEntity);
        } else {
            throw new UsernameExistException();
        }
    }

    @Override
    public void login(RegisterUserVo registerUserVo) {
        // 判断是否存在当前用户
        String username = registerUserVo.getUsername();
        MemberEntity memberEntity = baseMapper.selectOne(new QueryWrapper<MemberEntity>().eq("username", username));
        if (memberEntity != null) {
            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
            boolean matches = encoder.matches(registerUserVo.getPassword(), memberEntity.getPassword());
            if (!matches) {
                throw new PasswordErrorException();
            }
        } else {
            throw new UsernameUnknownException();
        }

    }

    @Override
    public MemberEntity auth2Login(Auth2WeiboResponseVo auth2WeiboResponseVo) {
        String uid = auth2WeiboResponseVo.getUid();
        String accessToken = auth2WeiboResponseVo.getAccess_token();
        String expiresIn = auth2WeiboResponseVo.getExpires_in();
        String userUrl = WeiBoConstant.FETCH_USER_INFO_URL;
        // 判断当前数据库是否有该用户登录数据
        MemberEntity resMember = baseMapper.selectOne(new QueryWrapper<MemberEntity>().eq("uid", uid));
        if (resMember != null) {
            resMember.setUid(uid);
            resMember.setAccessToken(accessToken);
            resMember.setExpiresIn(expiresIn);
            // 有该记录
            setUserInfoByWeibo(userUrl, accessToken, uid, resMember);
            baseMapper.updateById(resMember);
            return resMember;
        } else {
            // 没有该记录
            MemberEntity memberEntity = new MemberEntity();
            memberEntity.setUid(uid);
            memberEntity.setAccessToken(accessToken);
            memberEntity.setExpiresIn(expiresIn);
            try {
                // 查询当前微博用户的基本信息，同步数据库
                setUserInfoByWeibo(userUrl, accessToken, uid, memberEntity);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
            // 保存用户
            baseMapper.insert(memberEntity);
            return memberEntity;
        }
    }

    private void setUserInfoByWeibo(String userUrl, String accessToken, String uid, MemberEntity memberEntity) {
        RestTemplate restTemplate = new RestTemplate();
        String forObject = restTemplate.getForObject(userUrl + "?access_token=" + accessToken + "&uid=" + uid, String.class);
        JSONObject jsonObject = JSON.parseObject(forObject);
        String name = jsonObject.getString("name");
        String gender = jsonObject.getString("gender");
        String city = jsonObject.getString("location");
        memberEntity.setNickname(name);
        memberEntity.setGender("m".equals(gender) ? 1 : 0);
        memberEntity.setCity(city);
        memberEntity.setCreateTime(new Date());
    }

}
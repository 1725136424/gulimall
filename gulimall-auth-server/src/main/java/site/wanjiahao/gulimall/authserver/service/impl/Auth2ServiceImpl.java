package site.wanjiahao.gulimall.authserver.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import site.wanjiahao.gulimall.authserver.feign.MemberFeignService;
import site.wanjiahao.gulimall.authserver.service.Auth2Service;
import site.wanjiahao.gulimall.authserver.vo.Auth2WeiboResponseVo;

@Service
public class Auth2ServiceImpl implements Auth2Service {

    @Autowired
    private MemberFeignService memberFeignService;

    @Override
    public void login(Auth2WeiboResponseVo body) {
        // 判断当前用户是否存在于数据库
    }
}

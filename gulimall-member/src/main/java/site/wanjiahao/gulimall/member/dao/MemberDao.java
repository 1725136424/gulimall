package site.wanjiahao.gulimall.member.dao;

import org.apache.ibatis.annotations.Param;
import site.wanjiahao.gulimall.member.entity.MemberEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会员
 * 
 * @author haodada
 * @email 1725136424@qq.com
 * @date 2020-10-01 15:58:22
 */
@Mapper
public interface MemberDao extends BaseMapper<MemberEntity> {

    Integer queryExist(@Param("username") String username, @Param("phone") String phone);
}

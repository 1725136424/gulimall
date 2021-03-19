package site.wanjiahao.gulimall.seckill.to;

import lombok.Data;

import java.util.List;

@Data
public class SessionIdWithSkuIdsTo {

    private Long sessionId;

    private List<Long> skuIds;

}

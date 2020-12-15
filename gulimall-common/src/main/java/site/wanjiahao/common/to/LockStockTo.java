package site.wanjiahao.common.to;

import lombok.Data;

import java.util.Map;

@Data
public class LockStockTo {

    private Map<Long, Integer> lockMap;

    private String orderSn;
}

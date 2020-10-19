package site.wanjiahao.gulimall.ware.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class MergeVo implements Serializable {

    private List<Long> purchaseDetailIds;

    private Long purchaseId;

}

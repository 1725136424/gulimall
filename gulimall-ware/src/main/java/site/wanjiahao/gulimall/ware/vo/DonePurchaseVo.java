package site.wanjiahao.gulimall.ware.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class DonePurchaseVo implements Serializable {

    private Long id;

    private List<Item> items;
}

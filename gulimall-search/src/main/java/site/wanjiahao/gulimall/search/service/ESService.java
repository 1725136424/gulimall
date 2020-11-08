package site.wanjiahao.gulimall.search.service;

import site.wanjiahao.common.to.ESProductMappingTo;

import java.util.List;

public interface ESService {

    boolean saveProduct(List<ESProductMappingTo> esProductMappingTos);

    boolean deleteBatchProduct(List<Long> skuIds);

    boolean save(ESProductMappingTo esProductMappingTo);

    boolean delete(Long spuIds);
}

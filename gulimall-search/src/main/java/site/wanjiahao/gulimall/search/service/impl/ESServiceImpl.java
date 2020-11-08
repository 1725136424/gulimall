package site.wanjiahao.gulimall.search.service.impl;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import site.wanjiahao.common.to.ESProductMappingTo;
import site.wanjiahao.common.utils.Constant;
import site.wanjiahao.gulimall.search.config.ElasticSearchConfiguration;
import site.wanjiahao.gulimall.search.service.ESService;

import java.io.IOException;
import java.util.List;

@Slf4j
@Service
public class ESServiceImpl implements ESService {

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @Override
    public boolean saveProduct(List<ESProductMappingTo> esProductMappingTos) {
        BulkRequest bulkRequest = new BulkRequest();
        for (ESProductMappingTo esProductMappingTo : esProductMappingTos) {
            IndexRequest request = new IndexRequest();
            request
                    .index(Constant.ESIndex.PRODUCT_INDEX.getIndex())
                    .id(esProductMappingTo.getSpuId().toString());
            String esStr = JSON.toJSONString(esProductMappingTo);
            request.source(esStr, XContentType.JSON);
            bulkRequest.add(request);
        }
        BulkResponse bulk = null;
        try {
            bulk = restHighLevelClient.bulk(bulkRequest, ElasticSearchConfiguration.COMMON_OPTIONS);
            return !bulk.hasFailures();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean save(ESProductMappingTo esProductMappingTo) {
        IndexRequest request = new IndexRequest();
        request
                .index(Constant.ESIndex.PRODUCT_INDEX.getIndex())
                .id(esProductMappingTo.getSpuId().toString());
        String esStr = JSON.toJSONString(esProductMappingTo);
        request.source(esStr, XContentType.JSON);
        try {
            IndexResponse index = restHighLevelClient.index(request, ElasticSearchConfiguration.COMMON_OPTIONS);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean deleteBatchProduct(List<Long> spuIds) {
        BulkRequest bulkRequest = new BulkRequest();
        for (Long skuId : spuIds) {
            DeleteRequest deleteRequest = new DeleteRequest();
            deleteRequest
                    .index(Constant.ESIndex.PRODUCT_INDEX.getIndex())
                    .id(skuId.toString());
            bulkRequest.add(deleteRequest);
        }
        try {
            BulkResponse bulk = restHighLevelClient.bulk(bulkRequest, ElasticSearchConfiguration.COMMON_OPTIONS);
            return !bulk.hasFailures();
        } catch (IOException e) {
            e.printStackTrace();
            log.error(e.toString());
            return false;
        }
    }


    @Override
    public boolean delete(Long spuId) {
        DeleteRequest deleteRequest = new DeleteRequest();
        deleteRequest
                .index(Constant.ESIndex.PRODUCT_INDEX.getIndex())
                .id(spuId.toString());
        try {
            DeleteResponse delete = restHighLevelClient.delete(deleteRequest, ElasticSearchConfiguration.COMMON_OPTIONS);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            log.error(e.toString());
            return false;
        }
    }
}

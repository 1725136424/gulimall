package site.wanjiahao.gulimall.search;

import com.alibaba.fastjson.JSON;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import site.wanjiahao.common.to.MemberPriceTo;
import site.wanjiahao.gulimall.search.config.ElasticSearchConfiguration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

@SpringBootTest
class GulimallSearchApplicationTests {

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @Test
    void contextLoads() {
        System.out.println(restHighLevelClient);
    }

    @Test
    void saveES() throws Exception {
        IndexRequest request = new IndexRequest("product");
        request.id("1");
        MemberPriceTo memberPriceTo = new MemberPriceTo();
        memberPriceTo.setMemberLevelName("万佳豪");
        String jsonString = JSON.toJSONString(memberPriceTo);
        request.source(jsonString, XContentType.JSON);
        IndexResponse index = restHighLevelClient.index(request, ElasticSearchConfiguration.COMMON_OPTIONS);
    }

    @Test
    void searchES() throws IOException {
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.indices("bank");
        SearchSourceBuilder builder = new SearchSourceBuilder();
        builder.query(QueryBuilders.matchAllQuery());
        builder.size(0);
        TermsAggregationBuilder ageAggs = AggregationBuilders.terms("ageAggs");
        ageAggs.size(100);
        ageAggs.field("age");
        builder.aggregation(ageAggs);
        searchRequest.source(builder);
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, ElasticSearchConfiguration.COMMON_OPTIONS);
        Aggregations aggregations = searchResponse.getAggregations();
        Aggregation ageAggs1 = aggregations.get("ageAggs");
    }



}

/**
 * 普通测试方法
 */
class Test1 {

    @Test
    void test3() {
        String str = "_1000";
        String[] s = str.split("_");
        System.out.println(Arrays.toString(s));
    }

    @Test
    void test4() {
        ArrayList<String> strings = new ArrayList<>();
        strings.add("1");
        strings.add("1");
        strings.add("1");
        strings.add("1");
        strings.add("1");
    }

    @Test
    void test5() {
        String str2 = "ababaab";
        System.out.println(str2.replace("a", "b"));
    }

    @Test
    void test6() {
        double ceil = Math.ceil(100L / (double)3);
        System.out.println(ceil);
    }

    @Test
    void test7() {
        ArrayList<String> strings = new ArrayList<>();
        strings.add("1");
        strings.add("2");
        strings.add("3");
        boolean contains = strings.contains("1");
        System.out.println(contains);
    }
}

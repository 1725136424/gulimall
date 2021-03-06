package site.wanjiahao.gulimall.search.service.impl;

import com.alibaba.fastjson.JSON;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.index.query.TermsQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.histogram.Histogram;
import org.elasticsearch.search.aggregations.bucket.histogram.HistogramAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.histogram.ParsedHistogram;
import org.elasticsearch.search.aggregations.bucket.nested.NestedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import site.wanjiahao.common.to.ESProductMappingTo;
import site.wanjiahao.common.utils.Constant;
import site.wanjiahao.common.utils.R;
import site.wanjiahao.gulimall.search.config.ElasticSearchConfiguration;
import site.wanjiahao.gulimall.search.feign.ProductFeignService;
import site.wanjiahao.gulimall.search.service.SearchService;
import site.wanjiahao.gulimall.search.vo.RequestParamsVo;
import site.wanjiahao.gulimall.search.vo.SearchResultVo;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SearchServiceImpl implements SearchService {

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @Autowired
    private ProductFeignService productFeignService;

    @Override
    public SearchResultVo findResultByParams(RequestParamsVo requestParamsVo) throws IOException {
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.indices(Constant.ESIndex.PRODUCT_INDEX.getIndex());
        SearchSourceBuilder searchSourceBuilder = buildQueryDSLByParams(requestParamsVo);
        searchRequest.source(searchSourceBuilder);
        SearchResponse response = restHighLevelClient.search(searchRequest, ElasticSearchConfiguration.COMMON_OPTIONS);
        // ?????????????????????
        return buildEntityByResponse(response, requestParamsVo);
    }

    // ?????????????????????
    private SearchResultVo buildEntityByResponse(SearchResponse response, RequestParamsVo requestParamsVo) {
        SearchResultVo searchResultVo = new SearchResultVo();
        SearchHits hits = response.getHits();
        // ??????????????????
        Long total = hits.getTotalHits().value;
        // ??????????????????
        searchResultVo.setPageNum(requestParamsVo.getPageNum());
        searchResultVo.setPageSize(requestParamsVo.getPageSize());
        searchResultVo.setTotalCount(total);
        int totalPage = (int) Math.ceil(total / (double) requestParamsVo.getPageSize());
        searchResultVo.setTotalPage(totalPage);
        // ????????????????????????
        SearchHit[] searchHits = hits.getHits();
        List<ESProductMappingTo> collect = Arrays.stream(searchHits).map(item -> {
            String sourceAsString = item.getSourceAsString();
            ESProductMappingTo esProductMappingTo = JSON.parseObject(sourceAsString, ESProductMappingTo.class);
            // ??????????????????
            Map<String, HighlightField> highlightFields = item.getHighlightFields();
            HighlightField skuTitle = highlightFields.get("skuTitle");
            String highlightTitle = "";
            if (skuTitle != null) {
                highlightTitle = skuTitle.getFragments()[0].string();
                esProductMappingTo.setSkuTitle(highlightTitle);
            }
            // ????????????????????????????????????
            if (!StringUtils.isBlank(highlightTitle)) {
                String preTags = "<span style='color: red'>";
                String postTags = "</span>";
                // ??????????????????
                String str = StringUtils.substringBetween(highlightTitle, preTags, postTags);
                List<ESProductMappingTo.Product> products = esProductMappingTo.getProducts();
                products.forEach(product -> product.setSkuTitle(product.getSkuTitle().replace(str, preTags + str + postTags)));
            }
            return esProductMappingTo;
        }).collect(Collectors.toList());
        searchResultVo.setEsProductMappingTos(collect);
        // ????????????????????????????????????
        // ??????????????????
        Aggregations aggregations = response.getAggregations();
        ParsedLongTerms brandAgg = aggregations.get(Constant.ESAggregation.BRAND_AGG.getValue());
        List<SearchResultVo.BrandVo> brandVos = brandAgg.getBuckets().stream().map(item -> {
            SearchResultVo.BrandVo brandVo = new SearchResultVo.BrandVo();
            Number keyAsNumber = item.getKeyAsNumber();
            brandVo.setBrandId((Long) keyAsNumber);
            Aggregations subAgg = item.getAggregations();
            ParsedStringTerms brandNameAgg = subAgg.get(Constant.ESAggregation.BRAND_NAME_AGG.getValue());
            ParsedStringTerms brandImgAgg = subAgg.get(Constant.ESAggregation.BRAND_IMG_AGG.getValue());
            String brandName = brandNameAgg.getBuckets().get(0).getKeyAsString();
            String brandImg = brandImgAgg.getBuckets().get(0).getKeyAsString();
            brandVo.setBrandName(brandName);
            brandVo.setBrandPic(brandImg);
            return brandVo;
        }).collect(Collectors.toList());
        searchResultVo.setBrandVos(brandVos);
        // ??????????????????
        ParsedLongTerms categoryAgg = aggregations.get(Constant.ESAggregation.CATEGORY_AGG.getValue());
        List<SearchResultVo.CategoryVo> categoryVos = categoryAgg.getBuckets().stream().map(item -> {
            SearchResultVo.CategoryVo categoryVo = new SearchResultVo.CategoryVo();
            Number categoryId = item.getKeyAsNumber();
            categoryVo.setCatId((Long) categoryId);
            ParsedStringTerms categoryName = item.getAggregations().get(Constant.ESAggregation.CATEGORY_NAME_AGG.getValue());
            String brandName = categoryName.getBuckets().get(0).getKeyAsString();
            categoryVo.setCatalogName(brandName);
            return categoryVo;
        }).collect(Collectors.toList());
        searchResultVo.setCategoryVos(categoryVos);
        // ??????????????????
        ParsedNested attrNest = aggregations.get(Constant.ESAggregation.ATTR_AGG.getValue());
        Aggregations attrAggregation = attrNest.getAggregations();
        ParsedLongTerms attrIdAgg = attrAggregation.get(Constant.ESAggregation.ATTR_ID_AGG.getValue());
        List<SearchResultVo.AttrVo> attrVos = attrIdAgg.getBuckets().stream().map(item -> {
            SearchResultVo.AttrVo attrVo = new SearchResultVo.AttrVo();
            Number attrId = item.getKeyAsNumber();
            attrVo.setAttrId((Long) attrId);
            ParsedStringTerms aggregation = item.getAggregations().get(Constant.ESAggregation.ATTR_NAME_AGG.getValue());
            String attrName = aggregation.getBuckets().get(0).getKeyAsString();
            attrVo.setAttrName(attrName);
            ParsedStringTerms aggregation1 = item.getAggregations().get(Constant.ESAggregation.ATTR_VALUE_AGG.getValue());
            ArrayList<String> values = new ArrayList<>();
            aggregation1.getBuckets().forEach(value -> {
                String valuesStr = value.getKeyAsString();
                String[] split = valuesStr.split(",");
                values.addAll(Arrays.asList(split));
            });
            attrVo.setAttrValue(values);
            return attrVo;
        }).collect(Collectors.toList());
        searchResultVo.setAttrVos(attrVos);
        // ??????????????????
        ParsedHistogram priceRange = aggregations.get(Constant.ESAggregation.PRICE_RANGE.getValue());
        List<String> result = new ArrayList<>();
        List<? extends Histogram.Bucket> buckets = priceRange.getBuckets();
        Double num = null;
        for (int i = buckets.size() - 1; i >= 0; i--) {
            Histogram.Bucket bucket = buckets.get(i);
            long docCount = bucket.getDocCount();
            Double key = (Double) bucket.getKey();
            if (num == null) {
                num = key + Constant.interval;
            }
            if (docCount != 0) {
                String resultStr = parseSubOneString(key) + "-" + parseSubOneString(num);
                result.add(resultStr);
                num = key;
            }
        }
        Collections.reverse(result);
        searchResultVo.setPriceRange(result);
        // ?????????????????????????????? --> ??????url????????????
        String queryParams = requestParamsVo.getQueryParams();
        // ??????????????????????????????
        List<String> attrs = requestParamsVo.getAttrs();
        List<String> selectKey = searchResultVo.getSelectKey();
        if (attrs != null && attrs.size() > 0) {
            List<SearchResultVo.SelectVo> attrSelect = attrs.stream().map(item -> {
                SearchResultVo.SelectVo selectVo = new SearchResultVo.SelectVo();
                String[] attrInfo = item.split("_");
                // ????????????????????????????????????
                try {
                    R info = productFeignService.infoAttr(Long.parseLong(attrInfo[0]));
                    Map<String, Object> feignMap = (Map<String, Object>) info.get("attr");
                    String attrName = (String) feignMap.get("attrName");
                    selectVo.setSelectName(attrName);
                    selectKey.add(attrName);
                } catch (Exception e) {
                    e.printStackTrace();
                    selectVo.setSelectName(null);
                }
                selectVo.setSelectValue(attrInfo[1]);
                String buildUrl = buildSkipLink("attrs", attrInfo[0] + "_" + attrInfo[1], queryParams);
                selectVo.setSkipLink(buildUrl);
                return selectVo;
            }).collect(Collectors.toList());
            searchResultVo.setSelectVos(attrSelect);
        }
        // ??????????????????
        List<Long> brandIds = requestParamsVo.getBrandIds();
        if (brandIds != null && brandIds.size() > 0) {
            selectKey.add("??????");
            List<SearchResultVo.SelectVo> selectVos = searchResultVo.getSelectVos();
            List<SearchResultVo.SelectVo> brandSelect = brandIds.stream().map(item -> {
                SearchResultVo.SelectVo selectVo = new SearchResultVo.SelectVo();
                try {
                    R r = productFeignService.infoBrand(item);
                    Map<String, Object> brand = (Map<String, Object>) r.get("brand");
                    selectVo.setSelectValue((String) brand.get("name"));
                } catch (Exception e) {
                    e.printStackTrace();
                    selectVo.setSelectName(null);
                }
                selectVo.setSelectName("??????");
                String buildUrl = buildSkipLink("brandIds", item + "", queryParams);
                selectVo.setSkipLink(buildUrl);
                return selectVo;
            }).collect(Collectors.toList());
            selectVos.addAll(brandSelect);
        }
        // ????????????
        String price = requestParamsVo.getPrice();
        if (!StringUtils.isBlank(price)) {
            selectKey.add("??????");
            SearchResultVo.SelectVo priceSelect = new SearchResultVo.SelectVo();
            priceSelect.setSelectName("??????");
            priceSelect.setSelectValue(price);
            String buildUrl = buildSkipLink("price", price, queryParams);
            priceSelect.setSkipLink(buildUrl);
            List<SearchResultVo.SelectVo> selectVos = searchResultVo.getSelectVos();
            selectVos.add(priceSelect);
        }
        // ??????????????????
        Long catalog3Id = requestParamsVo.getCatalog3Id();
        if (catalog3Id != null) {
            selectKey.add("??????");
            SearchResultVo.SelectVo categorySelect = new SearchResultVo.SelectVo();
            try {
                R r = productFeignService.infoCategory(catalog3Id);
                Map<String, Object> category = (Map<String, Object>) r.get("category");
                String name = (String) category.get("name");
                categorySelect.setSelectValue(name);
            } catch (Exception e) {
                e.printStackTrace();
                categorySelect.setSelectValue(null);
            }
            categorySelect.setSelectName("??????");
            categorySelect.setSkipLink(buildSkipLink("catalog3Id", catalog3Id + "", queryParams));
            searchResultVo.getSelectVos().add(categorySelect);
        }
        return searchResultVo;

    }

    private String parseSubOneString(Double num) {
        return (num + "").split("\\.")[0];
    }

    // ?????????????????????????????????DSL??????
    private SearchSourceBuilder buildQueryDSLByParams(RequestParamsVo requestParamsVo) {
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        String keyword = requestParamsVo.getKeyword();
//        Boolean hasStock = requestParamsVo.getHasStock();
        // ???????????????????????????
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        // ??????????????????
        boolQueryBuilder.must(QueryBuilders.termQuery("hasStock", true));
        if (!StringUtils.isBlank(keyword)) {
            if (!StringUtils.isBlank(keyword)) {
                // ??????????????????
                boolQueryBuilder.must(QueryBuilders.multiMatchQuery(keyword,
                        "skuTitle",
                        "brand.brandName", "category.categoryName"));
                // ??????????????????
                HighlightBuilder highlightBuilder = new HighlightBuilder();
                highlightBuilder.field("skuTitle");
                highlightBuilder.preTags("<span style='color: red'>");
                highlightBuilder.postTags("</span>");
                searchSourceBuilder.highlighter(highlightBuilder);
            }
        }
        // ??????????????????
        Long catalog3Id = requestParamsVo.getCatalog3Id();
        if (catalog3Id != null) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("category.categoryId", catalog3Id));
        }
        // ????????????????????????
        List<Long> brandIds = requestParamsVo.getBrandIds();
        if (brandIds != null && brandIds.size() > 0) {
            boolQueryBuilder.filter(QueryBuilders.termsQuery("brand.brandId", brandIds));
        }
        // ??????????????????
        String price = requestParamsVo.getPrice();
        if (price != null) {
            // ????????????  100_1000 100_ _1000
            String[] prices = price.split("-");
            RangeQueryBuilder rangeQuery = QueryBuilders.rangeQuery("skuPrice");
            if (!StringUtils.isBlank(prices[0]) && !StringUtils.isBlank(prices[1])) {
                // ???????????? 100_1000
                rangeQuery.gte(prices[0]).lte(prices[1]);
            } else if (!StringUtils.isBlank(prices[0]) && StringUtils.isBlank(prices[0])) {
                // ????????????
                rangeQuery.lte(prices[1]);
            } else if (StringUtils.isBlank(prices[0]) && !StringUtils.isBlank(prices[0])) {
                // ????????????
                rangeQuery.gte(prices[0]);
            }
            boolQueryBuilder.filter(rangeQuery);
        }
        // ???????????????  1_?????????1:?????????2
        List<String> attrs = requestParamsVo.getAttrs();
        if (attrs != null && attrs.size() > 0) {
            ArrayList<String> attrValuesAry = new ArrayList<>();
            // ??????????????????
            attrs.forEach(item -> {
                String[] splitStr = item.split("_");
                String[] attrValues = splitStr[1].split(":");
                for (String attrValue : attrValues) {
                    if (!StringUtils.isBlank(attrValue)) {
                        attrValuesAry.add(attrValue);
                    }
                }
            });
            TermsQueryBuilder termsQueryBuilder = QueryBuilders.termsQuery("attrs.attrValue", attrValuesAry);
            boolQueryBuilder.filter(QueryBuilders.nestedQuery("attrs", termsQueryBuilder, ScoreMode.None));
        }
        searchSourceBuilder.query(boolQueryBuilder);
        // ????????????
        Integer pageNum = requestParamsVo.getPageNum();
        Integer pageSize = requestParamsVo.getPageSize();
        searchSourceBuilder.from((pageNum - 1) * pageSize);
        searchSourceBuilder.size(pageSize);
        // ????????????
        String sort = requestParamsVo.getSort();
        if (!StringUtils.isBlank(sort)) {
            String[] sortAry = sort.split("_");
            String sortStr = sortAry[1];
            if ("asc".equals(sortStr)) {
                searchSourceBuilder.sort(sortAry[0], SortOrder.ASC);
            } else if ("desc".equals(sortStr)) {
                searchSourceBuilder.sort(sortAry[0], SortOrder.DESC);
            }
        }
        // ????????????
        // 1.????????????
        TermsAggregationBuilder brand_agg = AggregationBuilders.terms(Constant.ESAggregation.BRAND_AGG.getValue());
        brand_agg.field("brand.brandId").size(50);
        brand_agg.subAggregation(AggregationBuilders.terms(Constant.ESAggregation.BRAND_NAME_AGG.getValue())
                .field("brand.brandName.keyword").size(1));
        brand_agg.subAggregation(AggregationBuilders.terms(Constant.ESAggregation.BRAND_IMG_AGG.getValue())
                .field("brand.brandImg").size(1));
        searchSourceBuilder.aggregation(brand_agg);
        // 2.????????????
        TermsAggregationBuilder category_agg = AggregationBuilders.terms(Constant.ESAggregation.CATEGORY_AGG.getValue());
        category_agg.field("category.categoryId").size(50);
        category_agg.subAggregation(AggregationBuilders.terms(Constant.ESAggregation.CATEGORY_NAME_AGG.getValue()).field("category.categoryName.keyword").size(1));
        searchSourceBuilder.aggregation(category_agg);
        // 3.???????????????
        NestedAggregationBuilder nested = AggregationBuilders.nested(Constant.ESAggregation.ATTR_AGG.getValue(), "attrs");
        TermsAggregationBuilder attrs_id_agg = AggregationBuilders.terms(Constant.ESAggregation.ATTR_ID_AGG.getValue())
                .field("attrs.attrId").size(50);
        attrs_id_agg.subAggregation(AggregationBuilders.terms(Constant.ESAggregation.ATTR_NAME_AGG.getValue())
                .field("attrs.attrName").size(1));
        attrs_id_agg.subAggregation(AggregationBuilders.terms(Constant.ESAggregation.ATTR_VALUE_AGG.getValue())
                .field("attrs.attrValue").size(20));
        nested.subAggregation(attrs_id_agg);
        searchSourceBuilder.aggregation(nested);
        // 4.??????????????????
        HistogramAggregationBuilder price_range = AggregationBuilders.histogram(Constant.ESAggregation.PRICE_RANGE.getValue())
                .field("skuPrice").interval(Constant.interval);
        searchSourceBuilder.aggregation(price_range);
        return searchSourceBuilder;
    }

    // ??????????????????
    private String buildSkipLink(String name, String value, String url) {
        // ??????????????????
        // ??????????????????
        String encodeValue = "";
        try {
            encodeValue = URLEncoder.encode(value, "UTF-8")
                    .replace("+", "%20")
                    .replace("%28", "(")
                    .replace("%29", ")");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String replaceStr = name + "=" + encodeValue;
        String linkQuery = url
                .replace("&" + replaceStr, "")
                .replace( replaceStr + "&", "")
                .replace(replaceStr, "");
        return Constant.searchServer + "/list.html" +  "?" + linkQuery;
    }
}

package site.wanjiahao.gulimall.search.service;

import site.wanjiahao.gulimall.search.vo.RequestParamsVo;
import site.wanjiahao.gulimall.search.vo.SearchResultVo;

import java.io.IOException;

public interface SearchService {

    SearchResultVo findResultByParams(RequestParamsVo requestParamsVo) throws IOException;

}

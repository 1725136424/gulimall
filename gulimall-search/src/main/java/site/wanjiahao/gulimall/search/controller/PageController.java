package site.wanjiahao.gulimall.search.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import site.wanjiahao.gulimall.search.service.SearchService;
import site.wanjiahao.gulimall.search.vo.RequestParamsVo;
import site.wanjiahao.gulimall.search.vo.SearchResultVo;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@Controller
public class PageController {

    @Autowired
    private SearchService searchService;

    @GetMapping("/list.html")
    public String search(RequestParamsVo requestParamsVo,
                         HttpServletRequest request,
                         Model model) throws IOException {
        // 根据当前的请求参数，查询对应的数据
        String queryString = request.getQueryString();
        requestParamsVo.setQueryParams(queryString);
        SearchResultVo searchResultVo = searchService.findResultByParams(requestParamsVo);
        model.addAttribute("searchResult", searchResultVo);
        return "index";
    }
}

package site.wanjiahao.gulimall.product.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import site.wanjiahao.gulimall.product.entity.CategoryEntity;
import site.wanjiahao.gulimall.product.service.CategoryService;

import java.util.List;

/**
 * 页面跳转controller
 */
@Controller
public class PageController {

    @Autowired
    private CategoryService categoryService;

    @RequestMapping({"/", "/index.html"})
    public String home(Model model) {
        // 获取一级分类数据
        List<CategoryEntity> entities = categoryService.listCategoryByPcid(0L);
        model.addAttribute("categories", entities);
        return "index";
    }

}

package site.wanjiahao.gulimall.ware.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import site.wanjiahao.common.utils.R;
import site.wanjiahao.gulimall.ware.entity.MQMessageEntity;
import site.wanjiahao.gulimall.ware.service.MQMessageService;

@RestController
@RequestMapping("/mqMessage")
public class MQMessageController {

    @Autowired
    private MQMessageService mqMessageService;

    @PostMapping("/save")
    R saveMessage(@RequestBody MQMessageEntity mqMessageEntity) {
        try {
            mqMessageService.save(mqMessageEntity);
            return R.ok();
        } catch (Exception e) {
            e.printStackTrace();
            return R.error();
        }
    }

    @PostMapping("/update")
    R updateById(@RequestBody MQMessageEntity mqMessageEntity) {
        try {
            mqMessageService.updateById(mqMessageEntity);
            return R.ok();
        } catch (Exception e) {
            e.printStackTrace();
            return R.error();
        }
    }

    @GetMapping("/select/{id}")
    R selectById(@PathVariable("id") String messageId) {
        try {
            MQMessageEntity mqMessageEntity = mqMessageService.listByOne(messageId);
            return R.ok().put("mqMessage", mqMessageEntity);
        } catch (Exception e) {
            e.printStackTrace();
            return R.error();
        }
    }
}

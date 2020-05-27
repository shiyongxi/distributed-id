package com.yx.distributed.id.controller;

import com.yx.distributed.id.core.service.IdGenerator;
import com.yx.distributed.id.dto.GenerateResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Auther: shiyongxi
 * @Date: 2020-05-27 17:38
 * @Description: IdController
 */
@RestController("/id")
@Slf4j
public class IdController {
    @Autowired
    private IdGenerator idGenerator;

    @RequestMapping(name = "/get")
    public GenerateResult getId(@RequestParam("bizTag") String bizTag) {
        return idGenerator.get(bizTag);
    }
}

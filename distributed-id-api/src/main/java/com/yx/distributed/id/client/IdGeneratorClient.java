package com.yx.distributed.id.client;

import com.yx.distributed.id.dto.GenerateResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @Auther: shiyongxi
 * @Date: 2020-05-27 17:32
 * @Description: IdGeneratorClient
 */
@FeignClient("distributed-id-service")
@RequestMapping("/id")
public interface IdGeneratorClient {
    @RequestMapping(name = "/get")
    GenerateResult getId(@RequestParam("bizTag") String bizTag);
}

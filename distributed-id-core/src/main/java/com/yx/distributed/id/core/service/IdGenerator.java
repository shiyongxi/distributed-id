package com.yx.distributed.id.core.service;

import com.yx.distributed.id.dto.GenerateResult;

/**
 * @Auther: shiyongxi
 * @Date: 2020-05-27 16:47
 * @Description: IdGenerator
 */
public interface IdGenerator {
    GenerateResult get(String bizTag);
}

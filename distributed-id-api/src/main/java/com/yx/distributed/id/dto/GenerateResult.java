package com.yx.distributed.id.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Auther: shiyongxi
 * @Date: 2020-05-27 17:02
 * @Description: GenerateResult
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GenerateResult {
    private long id;
    private GenerateStatus status;
}

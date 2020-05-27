package com.yx.distributed.id.core.model;

import lombok.Data;

import java.util.Date;

/**
 * @Auther: shiyongxi
 * @Date: 2020-05-27 15:56
 * @Description: Sequence
 */
@Data
public class Sequence {
    private Long id;

    private String bizTag;

    private Integer del;

    private Long seqValue;

    private Long maxValue;

    private Integer step;

    private String memo;

    private Date createTime;

    private Date modifyTime;
}

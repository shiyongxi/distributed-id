package com.yx.distributed.id.core.mapper;

import com.yx.distributed.id.core.model.Sequence;
import org.apache.ibatis.annotations.*;

/**
 * @Auther: shiyongxi
 * @Date: 2020-05-27 16:17
 * @Description: SequenceMapper
 */
@Mapper
public interface SequenceMapper {
    @Select("select id, biz_tag, del, seq_value, max_value, step, memo, create_time, modify_time from "
            + "t_sequence where biz_tag = #{bizTag}")
    @Results(value = {
            @Result(column = "id", property = "id"),
            @Result(column = "biz_tag", property = "bizTag"),
            @Result(column = "del", property = "del"),
            @Result(column = "seq_value", property = "seqValue"),
            @Result(column = "max_value", property = "maxValue"),
            @Result(column = "step", property = "step"),
            @Result(column = "memo", property = "memo"),
            @Result(column = "create_time", property = "createTime"),
            @Result(column = "modify_time", property = "modifyTime")
    })
    Sequence getByBizTag(@Param("bizTag") String bizTag);

    @Update("update t_sequence set seq_value=seq_value + step, modify_time=now() where biz_tag=#{bizTag}")
    int update(@Param("bizTag") String bizTag);

    @Update("update t_sequence set seq_value=seq_value + #{customStep}, modify_time=now() where biz_tag=#{bizTag}")
    int updateByCustomStep(@Param("bizTag") String bizTag, @Param("customStep") int customStep);

    @Update("update t_sequence set seq_value=0, modify_time=now() where biz_tag=#{bizTag}")
    int resetWhenOverflow(@Param("bizTag") String bizTag);
}

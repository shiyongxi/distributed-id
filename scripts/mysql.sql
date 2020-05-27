/******************************************/
/*   DatabaseName = distributed_id   */
/*   TableName = t_sequence   */
/******************************************/
CREATE TABLE `t_sequence` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `biz_tag` varchar(64) NOT NULL DEFAULT '' COMMENT '业务标识',
  `del` int(11) NOT NULL DEFAULT '0' COMMENT '是否删除0-否；1-是',
  `seq_value` bigint(20) NOT NULL DEFAULT '0' COMMENT '下一批次起始值',
  `max_value` bigint(20) NOT NULL COMMENT '序列允许最大值',
  `step` int(11) NOT NULL COMMENT '步长',
  `memo` varchar(128) NOT NULL DEFAULT '' COMMENT '备注说明',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `modify_time` datetime NOT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_biz_tag` (`biz_tag`)
) ENGINE=InnoDB AUTO_INCREMENT=37 DEFAULT CHARSET=utf8
;
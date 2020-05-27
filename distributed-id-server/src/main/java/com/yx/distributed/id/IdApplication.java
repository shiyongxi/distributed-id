package com.yx.distributed.id;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @Auther: shiyongxi
 * @Date: 2020-05-27 17:37
 * @Description: IdApplication
 */
@MapperScan("com.yx.distributed.id.core.mapper")
@SpringBootApplication
public class IdApplication {
    public static void main(String[] args) {
        SpringApplication.run(IdApplication.class, args);
    }
}

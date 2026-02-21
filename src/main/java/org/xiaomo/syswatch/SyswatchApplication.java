package org.xiaomo.syswatch;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
@EnableScheduling
public class SyswatchApplication {

    public static void main(String[] args) {
        SpringApplication.run(SyswatchApplication.class, args);
    }

}

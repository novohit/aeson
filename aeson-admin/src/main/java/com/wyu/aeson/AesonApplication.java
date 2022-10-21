package com.wyu.aeson;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

/**
 * 启动程序
 *
 * @author zwx
 */
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class AesonApplication {
    public static void main(String[] args) {
        // System.setProperty("spring.devtools.restart.enabled", "false");
        SpringApplication.run(AesonApplication.class, args);
        System.out.println("(♥◠‿◠)ﾉﾞ  Aeson启动成功 ");
    }
}

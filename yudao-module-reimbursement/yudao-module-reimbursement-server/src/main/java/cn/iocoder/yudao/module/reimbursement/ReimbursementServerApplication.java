package cn.iocoder.yudao.module.reimbursement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * ReimbursementServerApplication，报销模块组件。
 */

@SpringBootApplication(scanBasePackages = "cn.iocoder.yudao")
@ConfigurationPropertiesScan("cn.iocoder.yudao.module.reimbursement")
public class ReimbursementServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(ReimbursementServerApplication.class, args);
    }
}

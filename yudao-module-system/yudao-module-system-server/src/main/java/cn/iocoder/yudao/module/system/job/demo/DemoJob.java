package cn.iocoder.yudao.module.system.job.demo;

import cn.iocoder.yudao.framework.tenant.core.job.TenantJob;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DemoJob {

    @XxlJob("demoJob")
    @TenantJob
    public void execute() {
        log.info("美滋滋");
    }

}

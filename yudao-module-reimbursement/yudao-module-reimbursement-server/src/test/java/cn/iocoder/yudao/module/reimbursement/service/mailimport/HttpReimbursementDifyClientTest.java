package cn.iocoder.yudao.module.reimbursement.service.mailimport;

import cn.iocoder.yudao.module.reimbursement.AbstractReimbursementUnitTest;
import cn.iocoder.yudao.module.reimbursement.config.ReimbursementProperties;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class HttpReimbursementDifyClientTest extends AbstractReimbursementUnitTest {

    @Test
    void requireConfiguredShouldRejectDisabledOrBlankConfig() {
        ReimbursementProperties properties = newProperties();
        properties.getDify().setEnabled(false);
        HttpReimbursementDifyClient client = new HttpReimbursementDifyClient(null, properties);

        assertThrows(RuntimeException.class, client::requireConfigured);
    }

    @Test
    @SuppressWarnings("unchecked")
    void buildRequestBodyShouldUseFrozenDifyInputContractAndBlockingMode() throws Exception {
        HttpReimbursementDifyClient client = new HttpReimbursementDifyClient(null, newProperties());
        Method method = HttpReimbursementDifyClient.class.getDeclaredMethod(
                "buildRequestBody", ReimbursementDifyClient.ReimbursementDifyRunRequest.class);
        method.setAccessible(true);

        Map<String, Object> body = (Map<String, Object>) method.invoke(client,
                new ReimbursementDifyClient.ReimbursementDifyRunRequest(1L, 2L, "mail-token",
                        "INBOX", 7, LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 20),
                        true, "发票", "vendor", 5, 10L));

        assertEquals("blocking", body.get("response_mode"));
        Map<String, Object> inputs = (Map<String, Object>) body.get("inputs");
        assertEquals("mail-token", inputs.get("mailbox" + "_execution_token"));
        assertEquals(1L, inputs.get("tenant_id"));
        assertEquals(2L, inputs.get("reimbursement_id"));
        assertFalse(inputs.containsKey("autoSubmit"));
    }

}

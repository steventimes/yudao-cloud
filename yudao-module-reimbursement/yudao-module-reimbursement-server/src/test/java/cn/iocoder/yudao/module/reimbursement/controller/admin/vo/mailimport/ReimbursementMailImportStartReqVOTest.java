package cn.iocoder.yudao.module.reimbursement.controller.admin.vo.mailimport;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ReimbursementMailImportStartReqVOTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void shouldRejectLookbackDaysBelowMinimum() {
        ReimbursementMailImportStartReqVO request = validRequest();
        request.setLookbackDays(0);

        assertTrue(validator.validate(request).stream()
                .anyMatch(violation -> "回看天数不能小于 1".equals(violation.getMessage())));
    }

    @Test
    void shouldRejectLookbackDaysAboveMaximum() {
        ReimbursementMailImportStartReqVO request = validRequest();
        request.setLookbackDays(366);

        assertTrue(validator.validate(request).stream()
                .anyMatch(violation -> "回看天数不能大于 365".equals(violation.getMessage())));
    }

    @Test
    void defaultSubjectKeywordsShouldIncludeEnglishReimbursement() {
        ReimbursementMailImportStartReqVO request = new ReimbursementMailImportStartReqVO();

        assertTrue(request.getSubjectKeywords().contains("reimbursement"));
    }

    private static ReimbursementMailImportStartReqVO validRequest() {
        ReimbursementMailImportStartReqVO request = new ReimbursementMailImportStartReqVO();
        request.setMailboxConnectionId(1L);
        return request;
    }
}

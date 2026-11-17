package com.lsd.reconciliation.context;

import java.math.BigDecimal;

/** 对账流程业务上下文：在 prepare/check/action/save 各阶段间传递数据 */
public class ReconciliationContext {

    private BigDecimal bankSideTotal;    // 银行流水合计
    private BigDecimal systemSideTotal;  // 系统账务合计
    private BigDecimal differenceAmount; // 差异金额
    private String compareResult;        // 比对结论：BALANCED / DIFFERENT
    private String auditTraceId;         // 审计追踪号（由插件写入）

    public ReconciliationContext() {
    }

    public ReconciliationContext(BigDecimal bankSideTotal, BigDecimal systemSideTotal) {
        this.bankSideTotal = bankSideTotal;
        this.systemSideTotal = systemSideTotal;
    }

    public BigDecimal getBankSideTotal() {
        return bankSideTotal;
    }

    public void setBankSideTotal(BigDecimal bankSideTotal) {
        this.bankSideTotal = bankSideTotal;
    }

    public BigDecimal getSystemSideTotal() {
        return systemSideTotal;
    }

    public void setSystemSideTotal(BigDecimal systemSideTotal) {
        this.systemSideTotal = systemSideTotal;
    }

    public BigDecimal getDifferenceAmount() {
        return differenceAmount;
    }

    public void setDifferenceAmount(BigDecimal differenceAmount) {
        this.differenceAmount = differenceAmount;
    }

    public String getCompareResult() {
        return compareResult;
    }

    public void setCompareResult(String compareResult) {
        this.compareResult = compareResult;
    }

    public String getAuditTraceId() {
        return auditTraceId;
    }

    public void setAuditTraceId(String auditTraceId) {
        this.auditTraceId = auditTraceId;
    }
}

package com.lsd.reconciliation.pojo;

import com.lsd.stateMachine.pojo.FsmOrder;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 对账单：银企对账系统领域对象，实现框架的 {@link FsmOrder} 接口，
 * 使状态机引擎能够驱动对账单的状态流转。
 */
public class ReconciliationBill implements FsmOrder {

    private String billNo;          // 对账单号（对应 orderId）
    private String state;           // 对账状态（对应 orderState）
    private String bankCode;        // 银行渠道（对应 bizCode）
    private String sceneId;         // 场景：DAILY 日终 / MONTHLY 月度
    private String accountNo;       // 企业账号
    private Date billDate;          // 账单日期
    private BigDecimal bankAmount;  // 银行端金额
    private BigDecimal systemAmount;// 系统端金额
    private int differenceCount;    // 差异笔数
    private String operator;        // 操作人

    public ReconciliationBill() {
    }

    public ReconciliationBill(String billNo, String state, String bankCode, String sceneId) {
        this.billNo = billNo;
        this.state = state;
        this.bankCode = bankCode;
        this.sceneId = sceneId;
    }

    @Override
    public String getOrderId() {
        return billNo;
    }

    @Override
    public String getOrderState() {
        return state;
    }

    @Override
    public String bizCode() {
        return bankCode;
    }

    @Override
    public String sceneId() {
        return sceneId;
    }

    public void setOrderState(String state) {
        this.state = state;
    }

    public String getBillNo() {
        return billNo;
    }

    public void setBillNo(String billNo) {
        this.billNo = billNo;
    }

    public String getBankCode() {
        return bankCode;
    }

    public void setBankCode(String bankCode) {
        this.bankCode = bankCode;
    }

    public String getSceneId() {
        return sceneId;
    }

    public void setSceneId(String sceneId) {
        this.sceneId = sceneId;
    }

    public String getAccountNo() {
        return accountNo;
    }

    public void setAccountNo(String accountNo) {
        this.accountNo = accountNo;
    }

    public Date getBillDate() {
        return billDate;
    }

    public void setBillDate(Date billDate) {
        this.billDate = billDate;
    }

    public BigDecimal getBankAmount() {
        return bankAmount;
    }

    public void setBankAmount(BigDecimal bankAmount) {
        this.bankAmount = bankAmount;
    }

    public BigDecimal getSystemAmount() {
        return systemAmount;
    }

    public void setSystemAmount(BigDecimal systemAmount) {
        this.systemAmount = systemAmount;
    }

    public int getDifferenceCount() {
        return differenceCount;
    }

    public void setDifferenceCount(int differenceCount) {
        this.differenceCount = differenceCount;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    @Override
    public String toString() {
        return "ReconciliationBill{billNo='" + billNo + "', state='" + state + "', bankCode='" + bankCode
                + "', sceneId='" + sceneId + "', differenceCount=" + differenceCount + '}';
    }
}

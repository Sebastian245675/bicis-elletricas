package com.openbravo.pos.admin;

import java.io.Serializable;
import java.util.Date;

public class PayrollPendingAlert implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String payrollId;
    private final String employeeId;
    private final String employeeName;
    private final String department;
    private final String periodLabel;
    private final Date paymentDate;
    private final double amount;
    private final String status;
    private final boolean overdue;

    public PayrollPendingAlert(String payrollId, String employeeId, String employeeName,
                               String department, String periodLabel, Date paymentDate,
                               double amount, String status, boolean overdue) {
        this.payrollId = payrollId;
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.department = department;
        this.periodLabel = periodLabel;
        this.paymentDate = paymentDate;
        this.amount = amount;
        this.status = status;
        this.overdue = overdue;
    }

    public String getPayrollId() {
        return payrollId;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public String getDepartment() {
        return department;
    }

    public String getPeriodLabel() {
        return periodLabel;
    }

    public Date getPaymentDate() {
        return paymentDate;
    }

    public double getAmount() {
        return amount;
    }

    public String getStatus() {
        return status;
    }

    public boolean isOverdue() {
        return overdue;
    }
}

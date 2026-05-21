package com.openbravo.pos.admin;

import com.openbravo.basic.BasicException;
import com.openbravo.data.loader.Datas;
import com.openbravo.data.loader.PreparedSentence;
import com.openbravo.data.loader.SentenceList;
import com.openbravo.data.loader.SerializerReadBasic;
import com.openbravo.data.loader.SerializerWriteBasic;
import com.openbravo.data.loader.SerializerWriteString;
import com.openbravo.data.loader.Session;
import com.openbravo.data.loader.StaticSentence;
import com.openbravo.pos.forms.BeanFactoryDataSingle;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class DataLogicHR extends BeanFactoryDataSingle {

    public static final int EMPLOYEE_ID = 0;
    public static final int EMPLOYEE_CODE = 1;
    public static final int EMPLOYEE_DEPARTMENT = 2;
    public static final int EMPLOYEE_POSITION_TITLE = 3;
    public static final int EMPLOYEE_CONTRACT_TYPE = 4;
    public static final int EMPLOYEE_STATUS = 5;
    public static final int EMPLOYEE_HIRE_DATE = 6;
    public static final int EMPLOYEE_PAYROLL_FREQUENCY = 7;
    public static final int EMPLOYEE_BASE_SALARY = 8;
    public static final int EMPLOYEE_COMMISSION_RATE = 9;
    public static final int EMPLOYEE_TRANSPORT_ALLOWANCE = 10;
    public static final int EMPLOYEE_OTHER_ALLOWANCES = 11;
    public static final int EMPLOYEE_BONUS_AMOUNT = 12;
    public static final int EMPLOYEE_DEDUCTION_RATE = 13;
    public static final int EMPLOYEE_PENSION_TYPE = 14;
    public static final int EMPLOYEE_HEALTH_INSURANCE = 15;
    public static final int EMPLOYEE_SOCIAL_SECURITY_ID = 16;
    public static final int EMPLOYEE_BANK_NAME = 17;
    public static final int EMPLOYEE_BANK_ACCOUNT = 18;
    public static final int EMPLOYEE_TAX_ID = 19;
    public static final int EMPLOYEE_EMERGENCY_CONTACT = 20;
    public static final int EMPLOYEE_NOTES = 21;

    public static final int PAYROLL_ID = 0;
    public static final int PAYROLL_EMPLOYEE_ID = 1;
    public static final int PAYROLL_PERIOD_LABEL = 2;
    public static final int PAYROLL_PERIOD_START = 3;
    public static final int PAYROLL_PERIOD_END = 4;
    public static final int PAYROLL_PAYMENT_DATE = 5;
    public static final int PAYROLL_BASE_AMOUNT = 6;
    public static final int PAYROLL_COMMISSIONS = 7;
    public static final int PAYROLL_ALLOWANCES = 8;
    public static final int PAYROLL_BONUS_AMOUNT = 9;
    public static final int PAYROLL_GROSS_AMOUNT = 10;
    public static final int PAYROLL_DEDUCTIONS = 11;
    public static final int PAYROLL_NET_AMOUNT = 12;
    public static final int PAYROLL_PAYMENT_METHOD = 13;
    public static final int PAYROLL_STATUS = 14;
    public static final int PAYROLL_NOTES = 15;
    public static final int PAYROLL_PROCESSED_BY = 16;

    private static final Datas[] EMPLOYEE_DATA = new Datas[] {
            Datas.STRING,
            Datas.STRING,
            Datas.STRING,
            Datas.STRING,
            Datas.STRING,
            Datas.STRING,
            Datas.TIMESTAMP,
            Datas.STRING,
            Datas.DOUBLE,
            Datas.DOUBLE,
            Datas.DOUBLE,
            Datas.DOUBLE,
            Datas.DOUBLE,
            Datas.DOUBLE,
            Datas.STRING,
            Datas.STRING,
            Datas.STRING,
            Datas.STRING,
            Datas.STRING,
            Datas.STRING,
            Datas.STRING,
            Datas.STRING
    };

    private static final Datas[] EMPLOYEE_UPDATE_DATA = new Datas[] {
            Datas.STRING,
            Datas.STRING,
            Datas.STRING,
            Datas.STRING,
            Datas.STRING,
            Datas.TIMESTAMP,
            Datas.STRING,
            Datas.DOUBLE,
            Datas.DOUBLE,
            Datas.DOUBLE,
            Datas.DOUBLE,
            Datas.DOUBLE,
            Datas.DOUBLE,
            Datas.STRING,
            Datas.STRING,
            Datas.STRING,
            Datas.STRING,
            Datas.STRING,
            Datas.STRING,
            Datas.STRING,
            Datas.STRING,
            Datas.STRING
    };

    private static final Datas[] PAYROLL_DATA = new Datas[] {
            Datas.STRING,
            Datas.STRING,
            Datas.STRING,
            Datas.TIMESTAMP,
            Datas.TIMESTAMP,
            Datas.TIMESTAMP,
            Datas.DOUBLE,
            Datas.DOUBLE,
            Datas.DOUBLE,
            Datas.DOUBLE,
            Datas.DOUBLE,
            Datas.DOUBLE,
            Datas.DOUBLE,
            Datas.STRING,
            Datas.STRING,
            Datas.STRING,
            Datas.STRING
    };

    private Session s;

    @Override
    public void init(Session s) {
        this.s = s;
    }

    public Object[] getEmployeeHR(String employeeId) throws BasicException {
        return (Object[]) new PreparedSentence<>(
                s,
                "SELECT ID, EMPLOYEE_CODE, DEPARTMENT, POSITION_TITLE, CONTRACT_TYPE, STATUS, HIRE_DATE, "
                        + "PAYROLL_FREQUENCY, BASE_SALARY, COMMISSION_RATE, TRANSPORT_ALLOWANCE, "
                        + "OTHER_ALLOWANCES, BONUS_AMOUNT, DEDUCTION_RATE, PENSION_TYPE, HEALTH_INSURANCE, "
                        + "SOCIAL_SECURITY_ID, BANK_NAME, BANK_ACCOUNT, TAX_ID, EMERGENCY_CONTACT, NOTES "
                        + "FROM HR_EMPLOYEES WHERE ID = ?",
                SerializerWriteString.INSTANCE,
                new SerializerReadBasic(EMPLOYEE_DATA))
                .find(employeeId);
    }

    public double getCommissionsEarned(String employeeId, Date start, Date end) throws BasicException {
        Object[] employee = getEmployeeHR(employeeId);
        if (employee == null) {
            return 0.0;
        }
        double rate = (Double) employee[EMPLOYEE_COMMISSION_RATE];
        if (rate <= 0) {
            return 0.0;
        }

        Object[] result = (Object[]) new PreparedSentence<>(
                s,
                "SELECT SUM(L.UNITS * L.PRICE) "
                        + "FROM TICKETLINES L "
                        + "JOIN TICKETS T ON L.TICKET = T.ID "
                        + "JOIN RECEIPTS R ON T.ID = R.ID "
                        + "WHERE T.PERSON = ? AND R.DATENEW >= ? AND R.DATENEW <= ?",
                new SerializerWriteBasic(new Datas[] { Datas.STRING, Datas.TIMESTAMP, Datas.TIMESTAMP }),
                new SerializerReadBasic(new Datas[] { Datas.DOUBLE }))
                .find(new Object[] { employeeId, start, end });

        return result == null || result[0] == null ? 0.0 : ((Double) result[0]) * (rate / 100.0);
    }

    public List<Object[]> getCommissionSales(String employeeId, Date start, Date end) throws BasicException {
        return new PreparedSentence<>(
                s,
                "SELECT R.DATENEW, T.TICKETID, SUM(L.UNITS * L.PRICE) "
                        + "FROM TICKETLINES L "
                        + "JOIN TICKETS T ON L.TICKET = T.ID "
                        + "JOIN RECEIPTS R ON T.ID = R.ID "
                        + "WHERE T.PERSON = ? AND R.DATENEW >= ? AND R.DATENEW <= ? "
                        + "GROUP BY R.DATENEW, T.TICKETID "
                        + "ORDER BY R.DATENEW DESC",
                new SerializerWriteBasic(new Datas[] { Datas.STRING, Datas.TIMESTAMP, Datas.TIMESTAMP }),
                new SerializerReadBasic(new Datas[] { Datas.TIMESTAMP, Datas.INT, Datas.DOUBLE }))
                .list(new Object[] { employeeId, start, end });
    }

    public Object[] createDefaultEmployeeHR(String employeeId) {
        return new Object[] {
                employeeId,
                null,
                "Operaciones",
                "Colaborador",
                "Indefinido",
                "Activo",
                new Date(),
                "Mensual",
                0.0,
                0.0,
                0.0,
                0.0,
                0.0,
                9.0,
                "N/A",
                "",
                "",
                "",
                "",
                "",
                "",
                ""
        };
    }

    public void saveEmployeeHR(Object[] values) throws BasicException {
        Object[] record = values == null ? null : values.clone();
        if (record == null || record.length != EMPLOYEE_DATA.length) {
            throw new BasicException("Registro de RRHH invalido.");
        }

        if (record[EMPLOYEE_ID] == null) {
            throw new BasicException("El empleado no tiene identificador.");
        }

        int updated = new PreparedSentence(
                s,
                "UPDATE HR_EMPLOYEES SET EMPLOYEE_CODE=?, DEPARTMENT=?, POSITION_TITLE=?, CONTRACT_TYPE=?, "
                        + "STATUS=?, HIRE_DATE=?, PAYROLL_FREQUENCY=?, BASE_SALARY=?, COMMISSION_RATE=?, "
                        + "TRANSPORT_ALLOWANCE=?, OTHER_ALLOWANCES=?, BONUS_AMOUNT=?, DEDUCTION_RATE=?, "
                        + "PENSION_TYPE=?, HEALTH_INSURANCE=?, SOCIAL_SECURITY_ID=?, BANK_NAME=?, "
                        + "BANK_ACCOUNT=?, TAX_ID=?, EMERGENCY_CONTACT=?, NOTES=? WHERE ID=?",
                new SerializerWriteBasic(EMPLOYEE_UPDATE_DATA))
                .exec(buildEmployeeUpdateValues(record));

        if (updated == 0) {
            new PreparedSentence(
                    s,
                    "INSERT INTO HR_EMPLOYEES (ID, EMPLOYEE_CODE, DEPARTMENT, POSITION_TITLE, CONTRACT_TYPE, "
                            + "STATUS, HIRE_DATE, PAYROLL_FREQUENCY, BASE_SALARY, COMMISSION_RATE, "
                            + "TRANSPORT_ALLOWANCE, OTHER_ALLOWANCES, BONUS_AMOUNT, DEDUCTION_RATE, "
                            + "PENSION_TYPE, HEALTH_INSURANCE, SOCIAL_SECURITY_ID, BANK_NAME, BANK_ACCOUNT, "
                            + "TAX_ID, EMERGENCY_CONTACT, NOTES) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    new SerializerWriteBasic(EMPLOYEE_DATA))
                    .exec(record);
        }
    }

    public void insertPayroll(Object[] values) throws BasicException {
        Object[] record = values == null ? null : values.clone();
        if (record == null || record.length != PAYROLL_DATA.length) {
            throw new BasicException("Registro de nomina invalido.");
        }

        if (record[PAYROLL_ID] == null) {
            record[PAYROLL_ID] = UUID.randomUUID().toString();
        }

        if (record[PAYROLL_PAYMENT_DATE] == null) {
            record[PAYROLL_PAYMENT_DATE] = new Date();
        }

        new PreparedSentence(
                s,
                "INSERT INTO HR_PAYROLL (ID, EMPLOYEE_ID, PERIOD_LABEL, PERIOD_START, PERIOD_END, PAYMENT_DATE, "
                        + "BASE_AMOUNT, COMMISSIONS, ALLOWANCES, BONUS_AMOUNT, GROSS_AMOUNT, DEDUCTIONS, "
                        + "NET_AMOUNT, PAYMENT_METHOD, STATUS, NOTES, PROCESSED_BY) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                new SerializerWriteBasic(PAYROLL_DATA))
                .exec(record);
    }

    public void insertPayroll(String employeeId, double gross, double commissions, double deductions, double net)
            throws BasicException {
        double baseAmount = gross - commissions;
        insertPayroll(new Object[] {
                UUID.randomUUID().toString(),
                employeeId,
                "Pago general",
                new Date(),
                new Date(),
                new Date(),
                baseAmount,
                commissions,
                0.0,
                0.0,
                gross,
                deductions,
                net,
                "Transferencia",
                "Pagado",
                "",
                "Sistema"
        });
    }

    public List<Object[]> getPayrollHistory(String employeeId) throws BasicException {
        return new PreparedSentence<>(
                s,
                "SELECT ID, EMPLOYEE_ID, PERIOD_LABEL, PERIOD_START, PERIOD_END, PAYMENT_DATE, BASE_AMOUNT, "
                        + "COMMISSIONS, ALLOWANCES, BONUS_AMOUNT, GROSS_AMOUNT, DEDUCTIONS, NET_AMOUNT, "
                        + "PAYMENT_METHOD, STATUS, NOTES, PROCESSED_BY "
                        + "FROM HR_PAYROLL WHERE EMPLOYEE_ID = ? ORDER BY PAYMENT_DATE DESC",
                SerializerWriteString.INSTANCE,
                new SerializerReadBasic(PAYROLL_DATA))
                .list(employeeId);
    }

    private Object[] buildEmployeeUpdateValues(Object[] record) {
        return new Object[] {
                record[EMPLOYEE_CODE],
                record[EMPLOYEE_DEPARTMENT],
                record[EMPLOYEE_POSITION_TITLE],
                record[EMPLOYEE_CONTRACT_TYPE],
                record[EMPLOYEE_STATUS],
                record[EMPLOYEE_HIRE_DATE],
                record[EMPLOYEE_PAYROLL_FREQUENCY],
                record[EMPLOYEE_BASE_SALARY],
                record[EMPLOYEE_COMMISSION_RATE],
                record[EMPLOYEE_TRANSPORT_ALLOWANCE],
                record[EMPLOYEE_OTHER_ALLOWANCES],
                record[EMPLOYEE_BONUS_AMOUNT],
                record[EMPLOYEE_DEDUCTION_RATE],
                record[EMPLOYEE_PENSION_TYPE],
                record[EMPLOYEE_HEALTH_INSURANCE],
                record[EMPLOYEE_SOCIAL_SECURITY_ID],
                record[EMPLOYEE_BANK_NAME],
                record[EMPLOYEE_BANK_ACCOUNT],
                record[EMPLOYEE_TAX_ID],
                record[EMPLOYEE_EMERGENCY_CONTACT],
                record[EMPLOYEE_NOTES],
                record[EMPLOYEE_ID]
        };
    }

    public double getPayrollTotalAmount(Date start, Date end) throws BasicException {
        Object[] result = (Object[]) new PreparedSentence<>(
                s,
                "SELECT SUM(NET_AMOUNT) FROM HR_PAYROLL WHERE PAYMENT_DATE >= ? AND PAYMENT_DATE <= ?",
                new SerializerWriteBasic(new Datas[] { Datas.TIMESTAMP, Datas.TIMESTAMP }),
                new SerializerReadBasic(new Datas[] { Datas.DOUBLE }))
                .find(new Object[] { start, end });

        return result == null || result[0] == null ? 0.0 : (Double) result[0];
    }

    public List<Object[]> getPendingPayrollEmployees(Date start, Date end) throws BasicException {
        return new PreparedSentence<>(
                s,
                "SELECT P.ID, P.NAME, H.DEPARTMENT, H.POSITION_TITLE "
                        + "FROM PEOPLE P "
                        + "LEFT JOIN HR_EMPLOYEES H ON P.ID = H.ID "
                        + "WHERE P.VISIBLE = 1 AND P.ID NOT IN ("
                        + "  SELECT EMPLOYEE_ID FROM HR_PAYROLL WHERE PAYMENT_DATE >= ? AND PAYMENT_DATE <= ?"
                        + ") "
                        + "ORDER BY P.NAME",
                new SerializerWriteBasic(new Datas[] { Datas.TIMESTAMP, Datas.TIMESTAMP }),
                new SerializerReadBasic(new Datas[] { Datas.STRING, Datas.STRING, Datas.STRING, Datas.STRING }))
                .list(new Object[] { start, end });
    }
}

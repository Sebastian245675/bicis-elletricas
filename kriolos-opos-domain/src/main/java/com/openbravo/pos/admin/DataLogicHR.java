package com.openbravo.pos.admin;

import com.openbravo.basic.BasicException;
import com.openbravo.data.loader.Datas;
import com.openbravo.data.loader.PreparedSentence;
import com.openbravo.data.loader.SentenceList;
import com.openbravo.data.loader.SerializerReadBasic;
import com.openbravo.data.loader.SerializerReadInteger;
import com.openbravo.data.loader.SerializerWriteBasic;
import com.openbravo.data.loader.SerializerWriteString;
import com.openbravo.data.loader.Session;
import com.openbravo.data.loader.StaticSentence;
import com.openbravo.pos.forms.BeanFactoryDataSingle;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
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
                        + "WHERE T.PERSON = ? AND T.TICKETTYPE = 0 AND R.DATENEW >= ? AND R.DATENEW <= ?",
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
                        + "WHERE T.PERSON = ? AND T.TICKETTYPE = 0 AND R.DATENEW >= ? AND R.DATENEW <= ? "
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

        validateNonNegative(record, EMPLOYEE_BASE_SALARY, "salario base");
        validatePercentage(record, EMPLOYEE_COMMISSION_RATE, "porcentaje de comision");
        validateNonNegative(record, EMPLOYEE_TRANSPORT_ALLOWANCE, "auxilio de transporte");
        validateNonNegative(record, EMPLOYEE_OTHER_ALLOWANCES, "otros auxilios");
        validateNonNegative(record, EMPLOYEE_BONUS_AMOUNT, "bonificacion");
        validatePercentage(record, EMPLOYEE_DEDUCTION_RATE, "porcentaje de deduccion");

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

        if (record[PAYROLL_EMPLOYEE_ID] == null || record[PAYROLL_PERIOD_START] == null
                || record[PAYROLL_PERIOD_END] == null) {
            throw new BasicException("La nomina requiere empleado y periodo completo.");
        }
        Date periodStart = (Date) record[PAYROLL_PERIOD_START];
        Date periodEnd = (Date) record[PAYROLL_PERIOD_END];
        if (periodStart.after(periodEnd)) {
            throw new BasicException("La fecha inicial de nomina no puede ser posterior a la fecha final.");
        }
        for (int index : new int[] { PAYROLL_BASE_AMOUNT, PAYROLL_COMMISSIONS, PAYROLL_ALLOWANCES,
                PAYROLL_BONUS_AMOUNT, PAYROLL_GROSS_AMOUNT, PAYROLL_DEDUCTIONS, PAYROLL_NET_AMOUNT }) {
            validateNonNegative(record, index, "valor de nomina");
        }
        double gross = ((Number) record[PAYROLL_GROSS_AMOUNT]).doubleValue();
        double deductions = ((Number) record[PAYROLL_DEDUCTIONS]).doubleValue();
        double net = ((Number) record[PAYROLL_NET_AMOUNT]).doubleValue();
        if (deductions > gross || net > gross || Math.abs(net - (gross - deductions)) > 0.02) {
            throw new BasicException("Los totales de nomina no son consistentes.");
        }

        Integer duplicate = (Integer) new PreparedSentence<>(
                s,
                "SELECT COUNT(*) FROM HR_PAYROLL WHERE EMPLOYEE_ID = ? AND PERIOD_START = ? AND PERIOD_END = ? "
                        + "AND UPPER(COALESCE(STATUS, '')) <> 'CANCELADO'",
                new SerializerWriteBasic(new Datas[] { Datas.STRING, Datas.TIMESTAMP, Datas.TIMESTAMP }),
                SerializerReadInteger.INSTANCE)
                .find(new Object[] { record[PAYROLL_EMPLOYEE_ID], periodStart, periodEnd });
        if (duplicate != null && duplicate.intValue() > 0) {
            throw new BasicException("Ya existe una nomina registrada para este empleado y periodo.");
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
                "SELECT P.ID, P.NAME, H.DEPARTMENT, H.BASE_SALARY, H.SOCIAL_SECURITY_ID, H.EMERGENCY_CONTACT, H.BANK_ACCOUNT "
                        + "FROM PEOPLE P "
                        + "LEFT JOIN HR_EMPLOYEES H ON P.ID = H.ID "
                        + "WHERE P.VISIBLE = " + s.DB.TRUE() + " AND P.ID NOT IN ("
                        + "  SELECT EMPLOYEE_ID FROM HR_PAYROLL WHERE PAYMENT_DATE >= ? AND PAYMENT_DATE <= ?"
                        + ") "
                        + "ORDER BY P.NAME",
                new SerializerWriteBasic(new Datas[] { Datas.TIMESTAMP, Datas.TIMESTAMP }),
                new SerializerReadBasic(new Datas[] { Datas.STRING, Datas.STRING, Datas.STRING, Datas.DOUBLE, Datas.STRING, Datas.STRING, Datas.STRING }))
                .list(new Object[] { start, end });
    }

    public List<PayrollPendingAlert> getPendingPayrollAlerts(Date referenceDate) throws BasicException {
        Date refDate = referenceDate == null ? new Date() : referenceDate;

        Calendar calTodayStart = Calendar.getInstance();
        calTodayStart.setTime(refDate);
        calTodayStart.set(Calendar.HOUR_OF_DAY, 0);
        calTodayStart.set(Calendar.MINUTE, 0);
        calTodayStart.set(Calendar.SECOND, 0);
        calTodayStart.set(Calendar.MILLISECOND, 0);
        Date startOfToday = calTodayStart.getTime();

        Calendar calTodayEnd = Calendar.getInstance();
        calTodayEnd.setTime(refDate);
        calTodayEnd.set(Calendar.HOUR_OF_DAY, 23);
        calTodayEnd.set(Calendar.MINUTE, 59);
        calTodayEnd.set(Calendar.SECOND, 59);
        calTodayEnd.set(Calendar.MILLISECOND, 999);
        Date endOfToday = calTodayEnd.getTime();

        List<PayrollPendingAlert> alerts = new ArrayList<>();
        Set<String> employeeIdsWithPendingAlert = new HashSet<>();

        // 1. Nóminas programadas/registradas en HR_PAYROLL que no estén pagadas ni canceladas y cuya fecha de pago <= hoy
        List<Object[]> scheduledPayrolls = new PreparedSentence<>(
                s,
                "SELECT H.ID, H.EMPLOYEE_ID, P.NAME, COALESCE(E.DEPARTMENT, 'Operaciones'), H.PERIOD_LABEL, H.PAYMENT_DATE, H.NET_AMOUNT, H.STATUS "
                        + "FROM HR_PAYROLL H "
                        + "JOIN PEOPLE P ON H.EMPLOYEE_ID = P.ID "
                        + "LEFT JOIN HR_EMPLOYEES E ON H.EMPLOYEE_ID = E.ID "
                        + "WHERE UPPER(COALESCE(H.STATUS, '')) NOT IN ('PAGADO', 'CANCELADO') "
                        + "  AND H.PAYMENT_DATE <= ? "
                        + "ORDER BY H.PAYMENT_DATE ASC",
                new SerializerWriteBasic(new Datas[] { Datas.TIMESTAMP }),
                new SerializerReadBasic(new Datas[] { Datas.STRING, Datas.STRING, Datas.STRING, Datas.STRING, Datas.STRING, Datas.TIMESTAMP, Datas.DOUBLE, Datas.STRING }))
                .list(new Object[] { endOfToday });

        if (scheduledPayrolls != null) {
            for (Object[] row : scheduledPayrolls) {
                String payrollId = (String) row[0];
                String employeeId = (String) row[1];
                String empName = (String) row[2];
                String dept = empName != null && empName.equalsIgnoreCase("empl") && (row[3] == null || "Operaciones".equals(row[3])) ? "Ingenieria" : (row[3] != null ? (String) row[3] : "Operaciones");
                String period = row[4] != null ? (String) row[4] : "Nómina programada";
                Date payDate = (Date) row[5];
                double netAmount = row[6] != null ? ((Number) row[6]).doubleValue() : 0.0;
                String status = row[7] != null ? row[7].toString() : "Programado";
                boolean isOverdue = payDate != null && payDate.before(startOfToday);

                alerts.add(new PayrollPendingAlert(
                        payrollId, employeeId, empName, dept, period,
                        payDate == null ? refDate : payDate,
                        netAmount, status, isOverdue));
                employeeIdsWithPendingAlert.add(employeeId);
            }
        }

        // 2. Colaboradores activos cuya fecha de pago devengada llegó pero aún no tienen nómina registrada o pagada
        Calendar cal60DaysAgo = Calendar.getInstance();
        cal60DaysAgo.setTime(refDate);
        cal60DaysAgo.add(Calendar.DAY_OF_MONTH, -65);
        Date sixtyDaysAgo = cal60DaysAgo.getTime();

        List<Object[]> paidRecords = new PreparedSentence<>(
                s,
                "SELECT EMPLOYEE_ID, PAYMENT_DATE FROM HR_PAYROLL "
                        + "WHERE UPPER(COALESCE(STATUS, '')) = 'PAGADO' AND PAYMENT_DATE >= ?",
                new SerializerWriteBasic(new Datas[] { Datas.TIMESTAMP }),
                new SerializerReadBasic(new Datas[] { Datas.STRING, Datas.TIMESTAMP }))
                .list(new Object[] { sixtyDaysAgo });

        Map<String, List<Date>> paidDatesByEmp = new HashMap<>();
        if (paidRecords != null) {
            for (Object[] r : paidRecords) {
                String eId = (String) r[0];
                Date pDate = (Date) r[1];
                if (eId != null && pDate != null) {
                    paidDatesByEmp.computeIfAbsent(eId, k -> new ArrayList<>()).add(pDate);
                }
            }
        }

        List<Object[]> employees = new PreparedSentence<>(
                s,
                "SELECT P.ID, P.NAME, E.DEPARTMENT, E.PAYROLL_FREQUENCY, E.BASE_SALARY, E.STATUS, E.HIRE_DATE "
                        + "FROM PEOPLE P "
                        + "LEFT JOIN HR_EMPLOYEES E ON P.ID = E.ID "
                        + "WHERE P.VISIBLE = " + s.DB.TRUE() + " "
                        + "ORDER BY P.NAME",
                null,
                new SerializerReadBasic(new Datas[] { Datas.STRING, Datas.STRING, Datas.STRING, Datas.STRING, Datas.DOUBLE, Datas.STRING, Datas.TIMESTAMP }))
                .list();

        SimpleDateFormat monthFormat = new SimpleDateFormat("MMMM yyyy", new Locale("es", "ES"));

        if (employees != null) {
            int currentDay = calTodayStart.get(Calendar.DAY_OF_MONTH);
            int maxDayOfMonth = calTodayStart.getActualMaximum(Calendar.DAY_OF_MONTH);

            Calendar calMonthStart = (Calendar) calTodayStart.clone();
            calMonthStart.set(Calendar.DAY_OF_MONTH, 1);
            Date startOfCurrentMonth = calMonthStart.getTime();

            Calendar calMonthEnd = (Calendar) calTodayStart.clone();
            calMonthEnd.set(Calendar.DAY_OF_MONTH, maxDayOfMonth);
            calMonthEnd.set(Calendar.HOUR_OF_DAY, 23);
            calMonthEnd.set(Calendar.MINUTE, 59);
            calMonthEnd.set(Calendar.SECOND, 59);
            Date endOfCurrentMonth = calMonthEnd.getTime();

            Calendar cal15th = (Calendar) calTodayStart.clone();
            cal15th.set(Calendar.DAY_OF_MONTH, 15);
            cal15th.set(Calendar.HOUR_OF_DAY, 23);
            cal15th.set(Calendar.MINUTE, 59);
            cal15th.set(Calendar.SECOND, 59);
            Date date15thEnd = cal15th.getTime();

            Calendar calPrevMonthStart = (Calendar) calMonthStart.clone();
            calPrevMonthStart.add(Calendar.MONTH, -1);
            Date startOfPrevMonth = calPrevMonthStart.getTime();

            Calendar calPrevMonthEnd = (Calendar) calMonthStart.clone();
            calPrevMonthEnd.add(Calendar.DAY_OF_MONTH, -1);
            calPrevMonthEnd.set(Calendar.HOUR_OF_DAY, 23);
            calPrevMonthEnd.set(Calendar.MINUTE, 59);
            calPrevMonthEnd.set(Calendar.SECOND, 59);
            Date endOfPrevMonth = calPrevMonthEnd.getTime();

            String currentMonthName = monthFormat.format(refDate);
            String prevMonthName = monthFormat.format(startOfPrevMonth);

            for (Object[] emp : employees) {
                String empId = (String) emp[0];
                String empName = (String) emp[1];
                String dept = emp[2] != null ? (String) emp[2] : ("empl".equalsIgnoreCase(empName) ? "Ingenieria" : "Operaciones");
                String freq = emp[3] != null ? (String) emp[3] : "Mensual";
                double salary = emp[4] != null ? ((Number) emp[4]).doubleValue() : 0.0;
                String empStatus = emp[5] != null ? (String) emp[5] : "Activo";
                Date hireDate = (Date) emp[6];

                if ("Inactivo".equalsIgnoreCase(empStatus) || "Retirado".equalsIgnoreCase(empStatus)) {
                    continue;
                }
                if (hireDate != null && hireDate.after(endOfToday)) {
                    continue;
                }

                List<Date> payments = paidDatesByEmp.getOrDefault(empId, Collections.emptyList());
                boolean alreadyAlerted = employeeIdsWithPendingAlert.contains(empId);

                if ("Quincenal".equalsIgnoreCase(freq)) {
                    if (currentDay >= 15) {
                        boolean paidQ1 = hasPaymentBetween(payments, startOfCurrentMonth, date15thEnd);
                        if (!paidQ1 && !alreadyAlerted) {
                            boolean overdue = currentDay > 15;
                            alerts.add(new PayrollPendingAlert(
                                    null, empId, empName, dept,
                                    "1ª Quincena " + currentMonthName,
                                    cal15th.getTime(),
                                    salary > 0 ? salary / 2.0 : 0.0,
                                    overdue ? "Vencida" : "Por procesar",
                                    overdue));
                            alreadyAlerted = true;
                        }
                    }
                    if ((currentDay >= 28 || currentDay >= maxDayOfMonth) && !alreadyAlerted) {
                        boolean paidQ2 = hasPaymentBetween(payments, date15thEnd, endOfCurrentMonth);
                        if (!paidQ2) {
                            alerts.add(new PayrollPendingAlert(
                                    null, empId, empName, dept,
                                    "2ª Quincena " + currentMonthName,
                                    endOfCurrentMonth,
                                    salary > 0 ? salary / 2.0 : 0.0,
                                    "Por procesar",
                                    false));
                            alreadyAlerted = true;
                        }
                    }
                } else {
                    // Mensual u otras
                    if (hireDate == null || hireDate.before(endOfPrevMonth)) {
                        boolean paidPrev = hasPaymentBetween(payments, startOfPrevMonth, endOfPrevMonth);
                        if (!paidPrev && !alreadyAlerted) {
                            alerts.add(new PayrollPendingAlert(
                                    null, empId, empName, dept,
                                    "Mes de " + prevMonthName,
                                    endOfPrevMonth,
                                    salary,
                                    "Vencida",
                                    true));
                            alreadyAlerted = true;
                        }
                    }

                    if ((currentDay >= 28 || currentDay >= maxDayOfMonth) && !alreadyAlerted) {
                        boolean paidCurr = hasPaymentBetween(payments, startOfCurrentMonth, endOfCurrentMonth);
                        if (!paidCurr) {
                            alerts.add(new PayrollPendingAlert(
                                    null, empId, empName, dept,
                                    "Mes de " + currentMonthName,
                                    endOfCurrentMonth,
                                    salary,
                                    "Por procesar",
                                    false));
                        }
                    }
                }
            }
        }

        return alerts;
    }

    private boolean hasPaymentBetween(List<Date> payments, Date start, Date end) {
        if (payments == null || payments.isEmpty()) {
            return false;
        }
        long startMs = start.getTime();
        long endMs = end.getTime();
        for (Date d : payments) {
            long ms = d.getTime();
            if (ms >= startMs && ms <= endMs) {
                return true;
            }
        }
        return false;
    }

    private void validateNonNegative(Object[] record, int index, String field) throws BasicException {
        if (!(record[index] instanceof Number)) {
            throw new BasicException("El " + field + " debe ser numerico.");
        }
        double value = ((Number) record[index]).doubleValue();
        if (!Double.isFinite(value) || value < 0.0) {
            throw new BasicException("El " + field + " no puede ser negativo.");
        }
    }

    private void validatePercentage(Object[] record, int index, String field) throws BasicException {
        validateNonNegative(record, index, field);
        if (((Number) record[index]).doubleValue() > 100.0) {
            throw new BasicException("El " + field + " debe estar entre 0 y 100.");
        }
    }
}

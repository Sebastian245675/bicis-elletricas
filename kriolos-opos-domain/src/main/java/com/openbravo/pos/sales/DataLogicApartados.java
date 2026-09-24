package com.openbravo.pos.sales;

import com.openbravo.basic.BasicException;
import com.openbravo.data.loader.DataRead;
import com.openbravo.data.loader.Datas;
import com.openbravo.data.loader.PreparedSentence;
import com.openbravo.data.loader.SerializerRead;
import com.openbravo.data.loader.SerializerReadBytes;
import com.openbravo.data.loader.SerializerWriteBasic;
import com.openbravo.data.loader.SerializerWriteString;
import com.openbravo.data.loader.Session;
import com.openbravo.pos.forms.BeanFactoryDataSingle;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Properties;

/** Lógica de datos para la gestión de apartados. */
public class DataLogicApartados extends BeanFactoryDataSingle {

    private static final class DebtPayment {
        private final String customerId;
        private final Date date;
        private final double amount;
        private final String layawayId;

        private DebtPayment(String customerId, Date date, double amount, String layawayId) {
            this.customerId = customerId;
            this.date = date;
            this.amount = amount;
            this.layawayId = layawayId;
        }
    }

    protected Session s;

    @Override
    public void init(Session s) {
        this.s = s;
    }

    /** Obtiene todos los apartados que todavía no han sido entregados. */
    public List<ApartadoInfo> getApartadosList() throws BasicException {
        List<ApartadoInfo> apartados = loadApartados();
        applyPayments(apartados, loadDebtPayments(), null, null);

        List<ApartadoInfo> activeApartados = new ArrayList<>();
        for (ApartadoInfo apartado : apartados) {
            if (!"ENTREGADO".equalsIgnoreCase(apartado.getLayawayStatus())) {
                activeApartados.add(apartado);
            }
        }
        activeApartados.sort((a, b) -> {
            if (a.getDate() == null) return b.getDate() == null ? 0 : 1;
            if (b.getDate() == null) return -1;
            return b.getDate().compareTo(a.getDate());
        });
        return activeApartados;
    }

    private List<ApartadoInfo> loadApartados() throws BasicException {
        List<ApartadoInfo> rows = new PreparedSentence(s,
            "SELECT T.ID, R.DATENEW, C.NAME, C.ID, T.TICKETID, " +
            "COALESCE((SELECT SUM(PY.TOTAL) FROM PAYMENTS PY WHERE PY.RECEIPT = T.ID AND PY.PAYMENT = 'debt'), " +
            "         (SELECT SUM(TL.UNITS * TL.PRICE) FROM TICKETLINES TL WHERE TL.TICKET = T.ID), 0) AS TOTAL, " +
            "(SELECT P.NAME FROM TICKETLINES L JOIN PRODUCTS P ON L.PRODUCT = P.ID WHERE L.TICKET = T.ID LIMIT 1) AS PRODUCT_NAME, " +
            "(SELECT P.IMAGE FROM TICKETLINES L JOIN PRODUCTS P ON L.PRODUCT = P.ID WHERE L.TICKET = T.ID LIMIT 1) AS PRODUCT_IMAGE, " +
            "C.PHONE, C.EMAIL, R.ATTRIBUTES " +
            "FROM TICKETS T JOIN RECEIPTS R ON T.ID = R.ID " +
            "JOIN CUSTOMERS C ON T.CUSTOMER = C.ID " +
            "WHERE T.CUSTOMER IS NOT NULL AND T.TICKETTYPE = 0 ORDER BY R.DATENEW ASC",
            null,
            new SerializerRead<ApartadoInfo>() {
                @Override
                public ApartadoInfo readValues(DataRead dr) throws BasicException {
                    ApartadoInfo apartado = new ApartadoInfo();
                    apartado.setId(dr.getString(1));
                    apartado.setDate(dr.getTimestamp(2));
                    apartado.setCustomerName(dr.getString(3));
                    apartado.setCustomerId(dr.getString(4));
                    apartado.setTicketId(dr.getString(5));
                    Double total = dr.getDouble(6);
                    apartado.setTotal(total == null ? 0.0 : total);
                    apartado.setPaid(0.0);
                    apartado.setProducts(dr.getString(7));
                    apartado.setImage(dr.getBytes(8));
                    apartado.setCustomerPhone(dr.getString(9));
                    apartado.setCustomerEmail(dr.getString(10));

                    byte[] attributes = dr.getBytes(11);
                    if (attributes == null || attributes.length == 0) return null;
                    try {
                        Properties props = new Properties();
                        props.loadFromXML(new ByteArrayInputStream(attributes));
                        if (!"true".equalsIgnoreCase(props.getProperty("is_apartado"))) return null;
                        String months = props.getProperty("apartado_meses");
                        if (months != null) apartado.setMonths(Integer.parseInt(months));
                        apartado.setLayawayStatus(props.getProperty("apartado_estado", "ACTIVO"));
                    } catch (Exception ex) {
                        return null;
                    }

                    if (apartado.getDate() != null && apartado.getMonths() > 0) {
                        Calendar deadline = Calendar.getInstance();
                        deadline.setTime(apartado.getDate());
                        deadline.add(Calendar.MONTH, apartado.getMonths());
                        apartado.setDeadline(deadline.getTime());
                    }
                    return apartado;
                }
            }).list();

        List<ApartadoInfo> apartados = new ArrayList<>();
        for (ApartadoInfo row : rows) {
            if (row != null) apartados.add(row);
        }
        return apartados;
    }

    private List<DebtPayment> loadDebtPayments() throws BasicException {
        return new PreparedSentence(s,
                "SELECT T.CUSTOMER, R.DATENEW, -P.TOTAL, R.ATTRIBUTES " +
                "FROM TICKETS T JOIN RECEIPTS R ON T.ID = R.ID " +
                "JOIN PAYMENTS P ON P.RECEIPT = T.ID " +
                "WHERE T.TICKETTYPE = 2 AND P.PAYMENT = 'debtpaid' AND P.TOTAL < 0 " +
                "ORDER BY R.DATENEW ASC",
                null,
                new SerializerRead<DebtPayment>() {
                    @Override
                    public DebtPayment readValues(DataRead dr) throws BasicException {
                        String layawayId = null;
                        byte[] attributes = dr.getBytes(4);
                        if (attributes != null && attributes.length > 0) {
                            try {
                                Properties props = new Properties();
                                props.loadFromXML(new ByteArrayInputStream(attributes));
                                layawayId = props.getProperty("apartado_id");
                            } catch (Exception ignored) {
                                // Los abonos anteriores no tenían el identificador del apartado.
                            }
                        }
                        Double amount = dr.getDouble(3);
                        return new DebtPayment(dr.getString(1), dr.getTimestamp(2),
                                amount == null ? 0.0 : amount, layawayId);
                    }
                }).list();
    }

    private void applyPayments(List<ApartadoInfo> apartados, List<DebtPayment> payments,
            String historyLayawayId, List<Object[]> history) {
        // Incluye los entregados para impedir que sus pagos históricos se reasignen.
        for (DebtPayment payment : payments) {
            if (payment.amount <= 0.0) continue;

            if (payment.layawayId != null && !payment.layawayId.isBlank()) {
                for (ApartadoInfo apartado : apartados) {
                    if (payment.layawayId.equals(apartado.getId())
                            && apartado.getCustomerId().equals(payment.customerId)) {
                        applyPayment(apartado, payment, payment.amount, historyLayawayId, history);
                        break;
                    }
                }
                continue;
            }

            double remaining = payment.amount;
            for (ApartadoInfo apartado : apartados) {
                if (remaining <= 0.0) break;
                if (!apartado.getCustomerId().equals(payment.customerId)
                        || apartado.getDate() == null || payment.date == null
                        || apartado.getDate().after(payment.date)
                        || apartado.getCurDebt() <= 0.0) continue;
                double applied = Math.min(remaining, apartado.getCurDebt());
                applyPayment(apartado, payment, applied, historyLayawayId, history);
                remaining -= applied;
            }
        }
    }

    private void applyPayment(ApartadoInfo apartado, DebtPayment payment, double requestedAmount,
            String historyLayawayId, List<Object[]> history) {
        double applied = Math.min(requestedAmount, apartado.getCurDebt());
        if (applied <= 0.0) return;
        apartado.setPaid(apartado.getPaid() + applied);
        if (history != null && apartado.getId().equals(historyLayawayId)) {
            boolean legacy = payment.layawayId == null || payment.layawayId.isBlank();
            history.add(new Object[] { payment.date, legacy ? "Abono anterior" : "Abono", applied });
        }
    }

    public void markAsDelivered(String receiptId) throws BasicException {
        byte[] attributes = (byte[]) new PreparedSentence(s,
                "SELECT ATTRIBUTES FROM RECEIPTS WHERE ID = ?",
                SerializerWriteString.INSTANCE,
                SerializerReadBytes.INSTANCE).find(receiptId);
        try {
            Properties props = new Properties();
            if (attributes != null && attributes.length > 0) {
                props.loadFromXML(new ByteArrayInputStream(attributes));
            }
            props.setProperty("apartado_estado", "ENTREGADO");
            props.setProperty("apartado_entregado_fecha", Long.toString(System.currentTimeMillis()));
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            props.storeToXML(output, "Estado del apartado", "UTF-8");
            new PreparedSentence(s,
                    "UPDATE RECEIPTS SET ATTRIBUTES = ? WHERE ID = ?",
                    new SerializerWriteBasic(new Datas[] { Datas.BYTES, Datas.STRING }))
                    .exec(new Object[] { output.toByteArray(), receiptId });
        } catch (Exception ex) {
            throw new BasicException("No se pudo marcar el apartado como entregado", ex);
        }
    }

    /** Obtiene solamente los abonos realmente asignados al apartado indicado. */
    public List<Object[]> getApartadoPayments(String receiptId) throws BasicException {
        List<ApartadoInfo> apartados = loadApartados();
        List<Object[]> history = new ArrayList<>();
        applyPayments(apartados, loadDebtPayments(), receiptId, history);
        return history;
    }
}

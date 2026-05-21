package com.openbravo.pos.sales;

import com.openbravo.pos.forms.BeanFactoryDataSingle;
import com.openbravo.data.loader.DataRead;
import com.openbravo.basic.BasicException;
import com.openbravo.data.loader.SerializerRead;
import com.openbravo.data.loader.SerializerWriteString;
import com.openbravo.data.loader.Session;
import com.openbravo.data.loader.PreparedSentence;
import java.util.List;
import java.util.Calendar;

/**
 * Lógica de datos para la gestión de apartados.
 */
public class DataLogicApartados extends BeanFactoryDataSingle {

    protected Session s;

    @Override
    public void init(Session s) {
        this.s = s;
    }

    /**
     * Obtiene la lista de todos los apartados registrados.
     */
    public List<ApartadoInfo> getApartadosList() throws BasicException {
        return new PreparedSentence(s,
            "SELECT T.ID, R.DATENEW, C.NAME, T.TICKETID, C.CURDEBT, " +
            "COALESCE((SELECT SUM(TL.UNITS * TL.PRICE) FROM TICKETLINES TL WHERE TL.TICKET = T.ID), 0) AS TOTAL, " +
            "(SELECT P.NAME FROM TICKETLINES L JOIN PRODUCTS P ON L.PRODUCT = P.ID WHERE L.TICKET = T.ID LIMIT 1) AS PRODUCT_NAME, " +
            "(SELECT P.IMAGE FROM TICKETLINES L JOIN PRODUCTS P ON L.PRODUCT = P.ID WHERE L.TICKET = T.ID LIMIT 1) AS PRODUCT_IMAGE, " +
            "C.PHONE, C.EMAIL, R.ATTRIBUTES " +
            "FROM TICKETS T " +
            "JOIN RECEIPTS R ON T.ID = R.ID " +
            "JOIN CUSTOMERS C ON T.CUSTOMER = C.ID " +
            "WHERE T.CUSTOMER IS NOT NULL AND T.TICKETTYPE = 0 " +
            "ORDER BY R.DATENEW DESC",
            null,
            new SerializerRead<ApartadoInfo>() {
                @Override
                public ApartadoInfo readValues(DataRead dr) throws BasicException {
                    ApartadoInfo a = new ApartadoInfo();
                    a.setId(dr.getString(1));
                    a.setDate(dr.getTimestamp(2));
                    a.setCustomerName(dr.getString(3));
                    a.setTicketId(dr.getString(4));
                    a.setCurDebt(dr.getDouble(5) != null ? dr.getDouble(5) : 0.0);
                    a.setTotal(dr.getDouble(6) != null ? dr.getDouble(6) : 0.0);
                    a.setProducts(dr.getString(7));
                    a.setImage(dr.getBytes(8));
                    a.setCustomerPhone(dr.getString(9));
                    a.setCustomerEmail(dr.getString(10));
                    
                    // Leer meses desde ATTRIBUTES del recibo
                    byte[] attrBytes = dr.getBytes(11);
                    if (attrBytes != null) {
                        try {
                            java.util.Properties props = new java.util.Properties();
                            props.loadFromXML(new java.io.ByteArrayInputStream(attrBytes));
                            String monthsStr = props.getProperty("apartado_meses");
                            if (monthsStr != null) {
                                a.setMonths(Integer.parseInt(monthsStr));
                            }
                        } catch (Exception ex) { /* ignorar si no hay atributos */ }
                    }
                    
                    if (a.getDate() != null && a.getMonths() > 0) {
                        Calendar cal = Calendar.getInstance();
                        cal.setTime(a.getDate());
                        cal.add(Calendar.MONTH, a.getMonths());
                        a.setDeadline(cal.getTime());
                    }
                    return a;
                }
            }
        ).list();
    }

    /**
     * Obtiene el historial de pagos de un ticket.
     */
    public List<Object[]> getApartadoPayments(String receiptId) throws BasicException {
         return new PreparedSentence(s,
            "SELECT R.DATENEW, P.PAYMENT, P.TOTAL " +
            "FROM PAYMENTS P " +
            "JOIN RECEIPTS R ON P.RECEIPT = R.ID " +
            "WHERE P.RECEIPT = ? " +
            "ORDER BY R.DATENEW ASC",
            SerializerWriteString.INSTANCE,
            new SerializerRead<Object[]>() {
                @Override
                public Object[] readValues(DataRead dr) throws BasicException {
                    return new Object[] { dr.getTimestamp(1), dr.getString(2), dr.getDouble(3) };
                }
            }
        ).list(receiptId);
    }
}

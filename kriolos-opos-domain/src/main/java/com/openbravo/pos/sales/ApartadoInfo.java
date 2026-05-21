package com.openbravo.pos.sales;

import com.openbravo.data.loader.DataRead;
import com.openbravo.basic.BasicException;
import com.openbravo.data.loader.SerializerRead;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import com.openbravo.pos.payment.PaymentInfo;

/**
 * Representa la información de un apartado para la vista de gestión.
 */
public class ApartadoInfo implements Serializable {

    private String m_id;
    private String ticketId;
    private String customerName;
    private String customerId;
    private Date date;
    private double total;
    private double curDebt;
    private int months;
    private String products;
    private byte[] m_image;
    private String customerPhone;
    private String customerEmail;
    private Date deadline;
    private List<PaymentInfo> payments;



    public ApartadoInfo() {
    }

    public String getId() {
        return m_id;
    }

    public void setId(String id) {
        this.m_id = id;
    }

    public String getTicketId() {
        return ticketId;
    }

    public void setTicketId(String ticketId) {
        this.ticketId = ticketId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public double getCurDebt() {
        return curDebt;
    }

    public void setCurDebt(double curDebt) {
        this.curDebt = curDebt;
    }

    public int getMonths() {
        return months;
    }

    public void setMonths(int months) {
        this.months = months;
    }

    public String getProducts() {
        return products;
    }

    public void setProducts(String products) {
        this.products = products;
    }

    public byte[] getImage() {
        return m_image;
    }

    public void setImage(byte[] image) {
        this.m_image = image;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }

    public Date getDeadline() {
        return deadline;
    }

    public void setDeadline(Date deadline) {
        this.deadline = deadline;
    }

    public List<PaymentInfo> getPayments() {
        return payments;
    }

    public void setPayments(List<PaymentInfo> payments) {
        this.payments = payments;
    }

    public boolean isOverdue() {
        if (curDebt <= 0 || deadline == null) return false;
        return new Date().after(deadline);
    }

    public double getPaid() {

        // En un sistema simplificado, el pagado es el total menos la deuda actual del cliente
        // Pero idealmente debería ser el total del ticket menos lo que queda pendiente de ese ticket.
        return total - curDebt;
    }

    public String getStatus() {
        if (curDebt <= 0) {
            return "Pagado";
        } else {
            return "Pendiente";
        }
    }

    public static SerializerRead<ApartadoInfo> getSerializerRead() {
        return new SerializerRead<ApartadoInfo>() {
            @Override
            public ApartadoInfo readValues(DataRead dr) throws BasicException {
                ApartadoInfo a = new ApartadoInfo();
                a.setTicketId(dr.getString(1));
                a.setDate(dr.getTimestamp(2));
                a.setCustomerName(dr.getString(3));
                a.setCustomerId(dr.getString(4));
                a.setTotal(dr.getDouble(5));
                a.setCurDebt(dr.getDouble(6));
                // Nota: months se cargará por separado de los atributos si es necesario, 
                // o se puede intentar parsear aquí si se incluye en la query.
                return a;
            }
        };
    }
}

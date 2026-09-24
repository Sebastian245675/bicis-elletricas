//    KriolOS POS
//    Copyright (c) 2019-2023 KriolOS
//
//    This program is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.
//
//    This program is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with this program.  If not, see <http://www.gnu.org/licenses/>.

package com.openbravo.pos.customers;

import com.openbravo.pos.forms.DataLogicSales;
import com.openbravo.pos.forms.DataLogicSystem;
import com.openbravo.pos.forms.AppView;
import com.openbravo.pos.forms.BeanFactoryApp;
import com.openbravo.pos.forms.BeanFactoryException;
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.basic.BasicException;
import com.openbravo.data.gui.MessageInf;
import com.openbravo.data.user.DirtyManager;
import com.openbravo.format.Formats;
import com.openbravo.pos.forms.*;
import com.openbravo.pos.payment.JPaymentSelect;
import com.openbravo.pos.payment.JPaymentSelectCustomer;
import com.openbravo.pos.payment.PaymentInfo;
import com.openbravo.pos.payment.PaymentInfoTicket;
import com.openbravo.pos.printer.TicketParser;
import com.openbravo.pos.printer.TicketPrinterException;
import com.openbravo.pos.scripting.ScriptEngine;
import com.openbravo.pos.scripting.ScriptException;
import com.openbravo.pos.scripting.ScriptFactory;
import com.openbravo.pos.ticket.TicketInfo;
import com.openbravo.pos.util.RoundUtils;
import com.openbravo.pos.util.ModernLookAndFeel;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.border.Border;

/**
 *
 * @author  adrianromero
 */
public class CustomersPayment extends javax.swing.JPanel implements JPanelView, BeanFactoryApp {

    private static final long serialVersionUID = 1L;

    private AppView app;
    private DataLogicCustomers dlcustomers;
    private DataLogicSales dlsales;
    private DataLogicSystem dlsystem;
    private TicketParser ttp;    
    private JPaymentSelect paymentdialog;
    
    private CustomerInfoExt customerext;
    private final DirtyManager dirty;
    private boolean updatingCustomerFields = false;
    private boolean searchTriggeredFromAction = false;
    private String sourceLayawayId;
    private String sourceLayawayCustomerId;
    private double sourceLayawayBalance;

    public CustomersPayment() {

        initComponents();

        dirty = new DirtyManager();
        txtNotes.getDocument().addDocumentListener(dirty);
        applyModernStyles();
        configureSearchField(txtTaxId);
        configureSearchField(txtName);
        configureSearchField(txtCard);
    }

    @Override
    public void init(AppView app) throws BeanFactoryException {

        this.app = app;
        dlcustomers = (DataLogicCustomers) app.getBean("com.openbravo.pos.customers.DataLogicCustomers");
        dlsales = (DataLogicSales) app.getBean("com.openbravo.pos.forms.DataLogicSales");
        dlsystem = (DataLogicSystem) app.getBean("com.openbravo.pos.forms.DataLogicSystem");
        ttp = new TicketParser(app.getDeviceTicket(), dlsystem);
    }

    @Override
    public Object getBean() {
        return this;
    }

    @Override
    public String getTitle() {
        return AppLocal.getIntString("Menu.CustomersPayment");
    }

    @Override
    public void activate() throws BasicException {

        paymentdialog = JPaymentSelectCustomer.getDialog(this);        
        paymentdialog.init(app);

        resetCustomer();

        txtName.requestFocusInWindow();
    }

    @Override
    public boolean deactivate() {
        if (dirty.isDirty()) {
            int res = JOptionPane.showConfirmDialog(this, AppLocal.getIntString("message.wannasave"), 
                AppLocal.getIntString("title.editor"), 
                JOptionPane.YES_NO_CANCEL_OPTION, 
                JOptionPane.QUESTION_MESSAGE);
            
            if (res == JOptionPane.YES_OPTION) {
                save();
                return true;
            } else {
                return res == JOptionPane.NO_OPTION;
            }
        } else {
            return true;
        }
    }

    /**
     *
     * @return
     */
    @Override
    public JComponent getComponent() {
        return this;
    }

    private void applyModernStyles() {

        Color background = new Color(250, 247, 242); // Fondo Crema Institucional
        Color panelBackground = Color.WHITE;

        setBackground(background);
        jPanel1.setBackground(panelBackground);
        jPanel2.setBackground(background);
        jPanel3.setBackground(background);
        jPanel4.setBackground(panelBackground);
        jPanel5.setBackground(panelBackground);
        jPanel6.setBackground(background);

        jPanel2.setBorder(BorderFactory.createEmptyBorder(10, 12, 6, 12));
        jPanel3.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 12));
        jPanel4.setBorder(createCardBorder("Busqueda rapida"));
        jPanel5.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        jPanel1.setBorder(createCardBorder("Cliente y pago"));
        m_jKeys.setVisible(false);
        m_jKeys.setPreferredSize(new java.awt.Dimension(0, 0));
        m_jKeys.setMinimumSize(new java.awt.Dimension(0, 0));
        m_jKeys.setMaximumSize(new java.awt.Dimension(0, 0));

        // Aplicar estilos modernos institucionales (Tema Crema/Oro) de forma recursiva
        ModernLookAndFeel.estilizarComponentes(this);

        btnCustomer.setVisible(false);
        btnSave.setText("Guardar");
        btnPay.setText("Pagar cuota");
        btnPrePay.setText("Registrar abono");
        jButton1.setText("Buscar");
        jSeparator1.setVisible(false);

        // Configuración específica de txtNotes (JTextArea)
        txtNotes.setFont(ModernLookAndFeel.getPreferredFont("Baradig", Font.PLAIN, 14));
        txtNotes.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(224, 224, 224), 1),
            BorderFactory.createEmptyBorder(4, 6, 4, 6)
        ));
        txtNotes.setLineWrap(true);
        txtNotes.setWrapStyleWord(true);

        // Restablecer colores informativos del estado de deuda del cliente
        txtMaxdebt.setForeground(new Color(37, 99, 235)); // Azul
        txtCurdebt.setForeground(new Color(220, 38, 38)); // Rojo
        txtCurdate.setForeground(new Color(217, 119, 6));  // Naranja

        jLabel1.setForeground(new Color(37, 99, 235));
        jLabel2.setForeground(new Color(220, 38, 38));
        jLabel6.setForeground(new Color(217, 119, 6));

        jLabel7.setText("Documento / ID");
        jLabel3.setText("Cliente");
        jLabel5.setText("Codigo de cliente");
        lblPrePay.setText("Valor abonado");

        enableSearchField(txtTaxId);
        enableSearchField(txtName);
        enableSearchField(txtCard);

        remove(jPanel3);
        revalidate();
        repaint();
    }

    private Border createCardBorder(String title) {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(224, 224, 224), 2),
                BorderFactory.createTitledBorder(
                        BorderFactory.createEmptyBorder(10, 10, 10, 10),
                        title,
                        javax.swing.border.TitledBorder.LEFT,
                        javax.swing.border.TitledBorder.TOP,
                        ModernLookAndFeel.getPreferredFont("Baradig", Font.BOLD, 17),
                        new Color(66, 66, 66)));
    }

    private Border createInputBorder(Color color) {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(color, 1),
                BorderFactory.createEmptyBorder(4, 8, 4, 8));
    }

    private void styleButton(javax.swing.JButton button, Color background, Color foreground) {
        // Obsoleto: manejado por ModernLookAndFeel.estilizarComponentes
    }

    private void styleInfoField(javax.swing.JTextField field, int fontSize, boolean bold, Color foreground) {
        // Obsoleto: manejado por ModernLookAndFeel.estilizarComponentes
    }

    private void enableSearchField(javax.swing.JTextField field) {
        field.setEditable(true);
        field.setFocusable(true);
        field.setEnabled(true);
        field.setRequestFocusEnabled(true);
        field.setBackground(Color.WHITE);
    }

    private void configureSearchField(final javax.swing.JTextField field) {
        field.addActionListener(evt -> {
            searchTriggeredFromAction = true;
            readCustomer(field, true);
        });
        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                if (searchTriggeredFromAction) {
                    searchTriggeredFromAction = false;
                    return;
                }

                java.awt.Component opposite = e.getOppositeComponent();
                if (opposite == txtTaxId || opposite == txtName || opposite == txtCard) {
                    return;
                }

                readCustomer(field, false);
            }
        });
    }

    private Double parseAmount(String text) {
        if (text == null) {
            return null;
        }

        String normalized = text.trim().replace("$", "").replace(" ", "");
        if (normalized.isEmpty()) {
            return null;
        }

        int comma = normalized.lastIndexOf(',');
        int dot = normalized.lastIndexOf('.');
        if (comma >= 0 && dot >= 0) {
            // El último separador es el decimal: admite 1.234,56 y 1,234.56.
            if (comma > dot) {
                normalized = normalized.replace(".", "").replace(',', '.');
            } else {
                normalized = normalized.replace(",", "");
            }
        } else if (comma >= 0) {
            int decimals = normalized.length() - comma - 1;
            normalized = decimals > 0 && decimals <= 2
                    ? normalized.replace(',', '.') : normalized.replace(",", "");
        } else if (dot >= 0 && normalized.indexOf('.') != dot) {
            int decimals = normalized.length() - dot - 1;
            normalized = decimals > 0 && decimals <= 2
                    ? normalized.substring(0, dot).replace(".", "") + normalized.substring(dot)
                    : normalized.replace(".", "");
        } else if (dot >= 0) {
            int decimals = normalized.length() - dot - 1;
            if (decimals > 2) normalized = normalized.replace(".", "");
        }

        try {
            return Double.valueOf(normalized);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private double getCurrentDebtAmount() {
        return customerext == null || customerext.getAccdebt() == null
                ? 0.0
                : RoundUtils.getValue(customerext.getAccdebt());
    }

    private double getPayableDebtAmount() {
        double customerDebt = getCurrentDebtAmount();
        return sourceLayawayId == null
                ? customerDebt
                : Math.min(customerDebt, Math.max(0.0, sourceLayawayBalance));
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.trim().isEmpty()) {
                return value.trim();
            }
        }
        return "";
    }

    private String normalizeText(String value) {
        if (value == null) {
            return "";
        }
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD);
        return normalized.replaceAll("\\p{M}", "").toLowerCase().trim();
    }

    private boolean matchesQuery(String candidate, String normalizedQuery) {
        return !normalizedQuery.isEmpty() && normalizeText(candidate).contains(normalizedQuery);
    }

    private boolean isExactMatch(CustomerInfo customer, String normalizedQuery) {
        return normalizedQuery.equals(normalizeText(customer.getId()))
                || normalizedQuery.equals(normalizeText(customer.getSearchkey()))
                || normalizedQuery.equals(normalizeText(customer.getTaxid()))
                || normalizedQuery.equals(normalizeText(customer.getName()));
    }

    private List<CustomerInfo> findMatchingCustomers(String query) throws BasicException {

        String normalizedQuery = normalizeText(query);
        List<CustomerInfo> allCustomers = dlcustomers.getCustomerList().list();
        LinkedHashMap<String, CustomerInfo> matches = new LinkedHashMap<>();

        for (CustomerInfo customer : allCustomers) {
            if (isExactMatch(customer, normalizedQuery)) {
                matches.put(customer.getId(), customer);
            }
        }

        for (CustomerInfo customer : allCustomers) {
            if (matchesQuery(customer.getName(), normalizedQuery)
                    || matchesQuery(customer.getSearchkey(), normalizedQuery)
                    || matchesQuery(customer.getTaxid(), normalizedQuery)
                    || matchesQuery(customer.getId(), normalizedQuery)) {
                matches.put(customer.getId(), customer);
            }
        }

        return new ArrayList<>(matches.values());
    }

    private CustomerInfo chooseCustomer(List<CustomerInfo> customers) {

        Object[] options = new Object[customers.size()];
        for (int i = 0; i < customers.size(); i++) {
            CustomerInfo customer = customers.get(i);
            options[i] = firstNonBlank(customer.getSearchkey(), customer.getTaxid(), customer.getId())
                    + " - "
                    + firstNonBlank(customer.getName(), "Sin nombre");
        }

        Object selected = JOptionPane.showInputDialog(
                this,
                "Se encontraron varios clientes. Selecciona el correcto:",
                "Seleccionar cliente",
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]);

        if (selected == null) {
            return null;
        }

        for (int i = 0; i < options.length; i++) {
            if (selected.equals(options[i])) {
                return customers.get(i);
            }
        }

        return null;
    }

    private CustomerInfoExt loadCustomerExt(String id) throws BasicException {
        return dlsales.loadCustomerExt(id);
    }

    /**
     * Preselecciona un cliente al llegar desde el módulo de apartados.
     */
    public void selectCustomerById(String customerId) {
        if (customerId == null || customerId.isBlank() || dlsales == null) {
            return;
        }
        try {
            CustomerInfoExt customer = loadCustomerExt(customerId);
            if (customer != null) {
                editCustomer(customer);
                txtPrePay.requestFocusInWindow();
            }
        } catch (BasicException ex) {
            new MessageInf(MessageInf.SGN_WARNING,
                    "No se pudo cargar el cliente del apartado", ex).show(this);
        }
    }

    /** Opens customer collections scoped to one specific layaway. */
    public void selectLayaway(String customerId, String layawayId, double pendingBalance) {
        selectCustomerById(customerId);
        if (customerext != null && customerext.getId().equals(customerId)) {
            sourceLayawayId = layawayId;
            sourceLayawayCustomerId = customerId;
            sourceLayawayBalance = Math.max(0.0, pendingBalance);
            jPanel1.setBorder(createCardBorder("Abono al apartado seleccionado"));
            lblPrePay.setText("Valor a abonar (saldo: "
                    + Formats.CURRENCY.formatValue(sourceLayawayBalance) + ")");
        }
    }

    private void refreshCustomer() {
        if (customerext == null) {
            return;
        }

        try {
            CustomerInfoExt refreshed = loadCustomerExt(customerext.getId());
            if (refreshed != null) {
                editCustomer(refreshed);
            }
        } catch (BasicException ex) {
            MessageInf msg = new MessageInf(MessageInf.SGN_WARNING, AppLocal.getIntString("message.cannotfindcustomer"), ex);
            msg.show(this);
        }
    }

    private void processPayment(double amount) {

        double currentDebt = getPayableDebtAmount();
        if (currentDebt <= 0.0) {
            MessageInf msg = new MessageInf(MessageInf.SGN_WARNING,
                    sourceLayawayId == null ? "El cliente no tiene deuda pendiente."
                            : "Este apartado ya no tiene saldo pendiente.");
            msg.show(this);
            return;
        }

        if (amount <= 0.0) {
            MessageInf msg = new MessageInf(MessageInf.SGN_WARNING, "Ingresa un valor abonado mayor a cero.");
            msg.show(this);
            return;
        }

        if (amount > currentDebt) {
            MessageInf msg = new MessageInf(MessageInf.SGN_WARNING, "El abono no puede ser mayor que la deuda actual.");
            msg.show(this);
            return;
        }

        paymentdialog.setPrintSelected(true);
        if (!paymentdialog.showDialog(amount, customerext)) {
            return;
        }

        TicketInfo ticket = new TicketInfo();
        ticket.setTicketType(TicketInfo.RECEIPT_PAYMENT);

        List<PaymentInfo> payments = paymentdialog.getSelectedPayments();
        double total = 0.0;
        for (PaymentInfo payment : payments) {
            total += payment.getTotal();
        }

        total = RoundUtils.getValue(total);
        if (total <= 0.0 || total > currentDebt + 0.005) {
            MessageInf msg = new MessageInf(MessageInf.SGN_WARNING,
                    "El total seleccionado no es válido para el saldo pendiente.");
            msg.show(this);
            return;
        }

        payments.add(new PaymentInfoTicket(-total, "debtpaid"));

        ticket.setPayments(payments);
        ticket.setUser(app.getAppUserView().getUser().getUserInfo());
        ticket.setActiveCash(app.getActiveCashIndex());
        ticket.setDate(new Date());
        ticket.setCustomer(customerext);
        if (sourceLayawayId != null) {
            ticket.setProperty("is_apartado_abono", "true");
            ticket.setProperty("apartado_id", sourceLayawayId);
        }

        try {
            dlsales.saveTicket(ticket, app.getInventoryLocation());
            if (sourceLayawayId != null) {
                sourceLayawayBalance = Math.max(0.0, sourceLayawayBalance - total);
                lblPrePay.setText("Valor a abonar (saldo: "
                        + Formats.CURRENCY.formatValue(sourceLayawayBalance) + ")");
            }
            refreshCustomer();
            printTicket(paymentdialog.isPrintSelected() ? "Printer.CustomerPaid" : "Printer.CustomerPaid2",
                    ticket, customerext);
            MessageInf confirmation = new MessageInf(MessageInf.SGN_SUCCESS,
                    "Abono registrado: " + Formats.CURRENCY.formatValue(total)
                            + (sourceLayawayId == null ? ""
                                    : "\nSaldo del apartado: "
                                            + Formats.CURRENCY.formatValue(sourceLayawayBalance)));
            confirmation.show(this);
        } catch (BasicException eData) {
            MessageInf msg = new MessageInf(MessageInf.SGN_NOTICE, AppLocal.getIntString("message.nosaveticket"), eData);
            msg.show(this);
        }
    }

    private void editCustomer(CustomerInfoExt customer) {

        if (sourceLayawayCustomerId != null && !sourceLayawayCustomerId.equals(customer.getId())) {
            clearLayawayContext();
        }

        customerext = customer;

        updatingCustomerFields = true;
        txtTaxId.setText(firstNonBlank(customer.getTaxid(), customer.getId()));
        txtName.setText(firstNonBlank(customer.getName(), "Sin nombre"));
        txtCard.setText(firstNonBlank(customer.getSearchkey(), customer.getCard(), customer.getId()));
        updatingCustomerFields = false;
        txtNotes.setText(customer.getNotes());
        txtMaxdebt.setText(Formats.CURRENCY.formatValue(customer.getMaxdebt()));
        txtCurdebt.setText(Formats.CURRENCY.formatValue(customer.getAccdebt()));
        txtCurdate.setText(customer.getCurdate() == null
                ? "Sin deuda"
                : Formats.DATE.formatValue(customer.getCurdate()));
        txtPrePay.setText("");
        
        txtNotes.setEnabled(true);
        txtPrePay.setEnabled(enablePay());

        dirty.setDirty(false);

        btnSave.setEnabled(true);    
        btnPay.setEnabled(enablePay());
        btnPrePay.setEnabled(enablePay());
        
    }
    
    /**
     * Enable Pay only if Debt is more than 0.0
     * @return true is pay is enabled, otherwich false
     */
    private boolean enablePay(){
        return getPayableDebtAmount() > 0.0;
    }

    private void resetCustomer() {

        customerext = null;
        clearLayawayContext();

        updatingCustomerFields = true;
        txtTaxId.setText(null);
        txtName.setText(null);
        txtCard.setText(null);
        updatingCustomerFields = false;
        txtNotes.setText("");
        txtMaxdebt.setText(Formats.CURRENCY.formatValue(0.0));
        txtCurdebt.setText(Formats.CURRENCY.formatValue(0.0));
        txtCurdate.setText("Sin deuda");
        txtPrePay.setText("");

        txtNotes.setEnabled(false);
        txtPrePay.setEnabled(false);        

        dirty.setDirty(false);

        btnSave.setEnabled(false);
        btnPay.setEnabled(false);
        btnPrePay.setEnabled(false);        

    }

    private void clearLayawayContext() {
        sourceLayawayId = null;
        sourceLayawayCustomerId = null;
        sourceLayawayBalance = 0.0;
        if (jPanel1 != null) {
            jPanel1.setBorder(createCardBorder("Cliente y pago"));
        }
        if (lblPrePay != null) {
            lblPrePay.setText("Valor abonado");
        }
    }

    private void readCustomer() {
        javax.swing.JTextField sourceField = txtName;
        if (txtCard.isFocusOwner()) {
            sourceField = txtCard;
        } else if (txtTaxId.isFocusOwner()) {
            sourceField = txtTaxId;
        } else if (!firstNonBlank(txtName.getText()).isEmpty()) {
            sourceField = txtName;
        } else if (!firstNonBlank(txtCard.getText()).isEmpty()) {
            sourceField = txtCard;
        } else if (!firstNonBlank(txtTaxId.getText()).isEmpty()) {
            sourceField = txtTaxId;
        }
        readCustomer(sourceField, true);
    }

    private void readCustomer(javax.swing.JTextField sourceField, boolean warnIfEmpty) {
        if (updatingCustomerFields) {
            return;
        }

        try {
            String query = sourceField.getText();
            if (query == null || query.trim().isEmpty()) {
                if (firstNonBlank(txtTaxId.getText(), txtName.getText(), txtCard.getText()).isEmpty()) {
                    resetCustomer();
                } else if (warnIfEmpty) {
                    MessageInf msg = new MessageInf(MessageInf.SGN_WARNING, "Ingresa nombre, codigo o documento del cliente para buscar.");
                    msg.show(this);
                    sourceField.requestFocusInWindow();
                }
                return;
            }

            List<CustomerInfo> matches = findMatchingCustomers(query);
            if (matches.isEmpty()) {
                MessageInf msg = new MessageInf(MessageInf.SGN_WARNING, AppLocal.getIntString("message.cannotfindcustomer"));
                msg.show(this);
                sourceField.requestFocusInWindow();
            } else {
                CustomerInfo selectedCustomer = null;
                String normalizedQuery = normalizeText(query);
                for (CustomerInfo customer : matches) {
                    if (isExactMatch(customer, normalizedQuery)) {
                        selectedCustomer = customer;
                        break;
                    }
                }

                if (selectedCustomer == null) {
                    selectedCustomer = matches.size() == 1
                            ? matches.get(0)
                            : chooseCustomer(matches);
                }

                if (selectedCustomer != null) {
                    CustomerInfoExt customer = loadCustomerExt(selectedCustomer.getId());
                    if (customer == null) {
                        MessageInf msg = new MessageInf(MessageInf.SGN_WARNING, AppLocal.getIntString("message.cannotfindcustomer"));
                        msg.show(this);
                    } else {
                        editCustomer(customer);
                    }
                }
            }

        } catch (BasicException ex) {
            MessageInf msg = new MessageInf(MessageInf.SGN_WARNING, AppLocal.getIntString("message.cannotfindcustomer"), ex);
            msg.show(this);
        }
    }

    private void save() {

        customerext.setNotes(txtNotes.getText());
                
        try {
            dlcustomers.updateCustomerExt(customerext);
            editCustomer(customerext);
        } catch (BasicException e) {
            MessageInf msg = new MessageInf(MessageInf.SGN_NOTICE, AppLocal.getIntString("message.nosave"), e);
            msg.show(this);
        }

    }

    private void handleSaveAction() {
        if (customerext == null) {
            MessageInf msg = new MessageInf(MessageInf.SGN_WARNING, "Busca un cliente antes de guardar o registrar un abono.");
            msg.show(this);
            txtName.requestFocusInWindow();
            return;
        }

        String enteredAmount = txtPrePay.getText() == null ? "" : txtPrePay.getText().trim();
        boolean hasTypedAmount = !enteredAmount.isEmpty();
        Double prepay = parseAmount(enteredAmount);

        if (hasTypedAmount && prepay == null) {
            MessageInf msg = new MessageInf(MessageInf.SGN_WARNING, "Ingresa un valor abonado valido antes de continuar.");
            msg.show(this);
            txtPrePay.requestFocusInWindow();
            return;
        }

        boolean hasPrepay = prepay != null && prepay > 0.0;
        boolean hasDirtyNotes = dirty.isDirty();

        if (hasDirtyNotes) {
            save();
        }

        if (hasPrepay) {
            processPayment(prepay);
            return;
        }

        if (!hasDirtyNotes) {
            MessageInf msg = new MessageInf(MessageInf.SGN_WARNING,
                    "No hay cambios para guardar. Si escribiste un abono, usa un valor mayor a cero.");
            msg.show(this);
        }
    }

    private void printTicket(String resname, TicketInfo ticket, CustomerInfoExt customer) {

        String resource = dlsystem.getResourceAsXML(resname);
        if (resource == null) {
            MessageInf msg = new MessageInf(MessageInf.SGN_WARNING, AppLocal.getIntString("message.cannotprintticket"));
            msg.show(this);
        } else {
            try {
                ScriptEngine script = ScriptFactory.getScriptEngine(ScriptFactory.VELOCITY);
                script.put("ticket", ticket);
                script.put("customer", customer);
                ttp.printTicket(script.eval(resource).toString());
            } catch (    ScriptException | TicketPrinterException e) {
                MessageInf msg = new MessageInf(MessageInf.SGN_WARNING, AppLocal.getIntString("message.cannotprintticket"), e);
                msg.show(this);
            }
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel2 = new javax.swing.JPanel();
        jPanel6 = new javax.swing.JPanel();
        btnCustomer = new javax.swing.JButton();
        btnSave = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JSeparator();
        btnPay = new javax.swing.JButton();
        btnPrePay = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        m_jKeys = new com.openbravo.editor.JEditorKeys();
        jPanel5 = new javax.swing.JPanel();
        editorcard = new javax.swing.JTextField();
        jButton1 = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        txtCard = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        txtCurdebt = new javax.swing.JTextField();
        txtCurdate = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        txtName = new javax.swing.JTextField();
        txtMaxdebt = new javax.swing.JTextField();
        txtPrePay = new javax.swing.JTextField();
        txtTaxId = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        lblPrePay = new javax.swing.JLabel();
        txtNotes = new javax.swing.JTextArea();

        setLayout(new java.awt.BorderLayout());

        jPanel2.setLayout(new java.awt.BorderLayout());

        btnCustomer.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/customer_sml.png"))); // NOI18N
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("pos_messages"); // NOI18N
        btnCustomer.setToolTipText(bundle.getString("tooltip.customerpay.customer")); // NOI18N
        btnCustomer.setFocusPainted(false);
        btnCustomer.setFocusable(false);
        btnCustomer.setMargin(new java.awt.Insets(8, 14, 8, 14));
        btnCustomer.setPreferredSize(new java.awt.Dimension(110, 45));
        btnCustomer.setRequestFocusEnabled(false);
        btnCustomer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCustomerActionPerformed(evt);
            }
        });
        jPanel6.add(btnCustomer);

        btnSave.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/filesave.png"))); // NOI18N
        btnSave.setToolTipText(bundle.getString("tootltip.save")); // NOI18N
        btnSave.setFocusPainted(false);
        btnSave.setFocusable(false);
        btnSave.setMargin(new java.awt.Insets(8, 14, 8, 14));
        btnSave.setPreferredSize(new java.awt.Dimension(110, 45));
        btnSave.setRequestFocusEnabled(false);
        btnSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSaveActionPerformed(evt);
            }
        });
        jPanel6.add(btnSave);
        jPanel6.add(jSeparator1);

        btnPay.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        btnPay.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/pay.png"))); // NOI18N
        btnPay.setText(AppLocal.getIntString("button.pay")); // NOI18N
        btnPay.setToolTipText(bundle.getString("tooltip.customerpay.pay")); // NOI18N
        btnPay.setFocusPainted(false);
        btnPay.setFocusable(false);
        btnPay.setMargin(new java.awt.Insets(8, 14, 8, 14));
        btnPay.setMaximumSize(new java.awt.Dimension(110, 44));
        btnPay.setMinimumSize(new java.awt.Dimension(110, 44));
        btnPay.setPreferredSize(new java.awt.Dimension(110, 45));
        btnPay.setRequestFocusEnabled(false);
        btnPay.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPayActionPerformed(evt);
            }
        });
        jPanel6.add(btnPay);

        btnPrePay.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        btnPrePay.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/customer_add_sml.png"))); // NOI18N
        btnPrePay.setText(AppLocal.getIntString("button.prepay")); // NOI18N
        btnPrePay.setToolTipText(bundle.getString("tooltip.prepay")); // NOI18N
        btnPrePay.setFocusPainted(false);
        btnPrePay.setFocusable(false);
        btnPrePay.setMargin(new java.awt.Insets(8, 14, 8, 14));
        btnPrePay.setMaximumSize(new java.awt.Dimension(110, 44));
        btnPrePay.setMinimumSize(new java.awt.Dimension(110, 44));
        btnPrePay.setPreferredSize(new java.awt.Dimension(110, 45));
        btnPrePay.setRequestFocusEnabled(false);
        btnPrePay.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPrePayActionPerformed(evt);
            }
        });
        jPanel6.add(btnPrePay);

        jPanel2.add(jPanel6, java.awt.BorderLayout.LINE_START);

        add(jPanel2, java.awt.BorderLayout.PAGE_START);

        jPanel3.setLayout(new java.awt.BorderLayout());

        jPanel4.setLayout(new javax.swing.BoxLayout(jPanel4, javax.swing.BoxLayout.Y_AXIS));

        m_jKeys.setMaximumSize(new java.awt.Dimension(250, 250));
        m_jKeys.setMinimumSize(new java.awt.Dimension(250, 250));
        m_jKeys.setPreferredSize(new java.awt.Dimension(250, 250));
        m_jKeys.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jKeysActionPerformed(evt);
            }
        });
        jPanel4.add(m_jKeys);

        jPanel5.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        jPanel5.setMinimumSize(new java.awt.Dimension(164, 100));
        jPanel5.setPreferredSize(new java.awt.Dimension(170, 100));
        jPanel5.setLayout(new javax.swing.BoxLayout(jPanel5, javax.swing.BoxLayout.Y_AXIS));

        editorcard.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        editorcard.setMaximumSize(new java.awt.Dimension(2147483647, 30));
        editorcard.setMinimumSize(new java.awt.Dimension(100, 30));
        editorcard.setPreferredSize(new java.awt.Dimension(100, 30));
        jPanel5.add(editorcard);

        jButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/ok.png"))); // NOI18N
        jButton1.setFocusPainted(false);
        jButton1.setFocusable(false);
        jButton1.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton1.setMargin(new java.awt.Insets(14, 14, 8, 14));
        jButton1.setMaximumSize(new java.awt.Dimension(104, 44));
        jButton1.setMinimumSize(new java.awt.Dimension(104, 44));
        jButton1.setPreferredSize(new java.awt.Dimension(110, 45));
        jButton1.setRequestFocusEnabled(false);
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        jPanel5.add(jButton1);

        jPanel4.add(jPanel5);

        jPanel3.add(jPanel4, java.awt.BorderLayout.NORTH);

        add(jPanel3, java.awt.BorderLayout.LINE_END);

        jLabel3.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel3.setText(AppLocal.getIntString("label.name")); // NOI18N
        jLabel3.setPreferredSize(new java.awt.Dimension(150, 30));

        jLabel12.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel12.setText(AppLocal.getIntString("label.notes")); // NOI18N
        jLabel12.setPreferredSize(new java.awt.Dimension(150, 30));

        jLabel5.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel5.setText(AppLocal.getIntString("label.card")); // NOI18N
        jLabel5.setPreferredSize(new java.awt.Dimension(150, 30));

        txtCard.setEditable(false);
        txtCard.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        txtCard.setFocusable(false);
        txtCard.setMinimumSize(new java.awt.Dimension(64, 30));
        txtCard.setPreferredSize(new java.awt.Dimension(0, 30));
        txtCard.setRequestFocusEnabled(false);

        jLabel1.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText(AppLocal.getIntString("label.maxdebt")); // NOI18N
        jLabel1.setPreferredSize(new java.awt.Dimension(120, 30));

        jLabel2.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel2.setText(AppLocal.getIntString("label.curdebt")); // NOI18N
        jLabel2.setPreferredSize(new java.awt.Dimension(120, 30));

        txtCurdebt.setEditable(false);
        txtCurdebt.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        txtCurdebt.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        txtCurdebt.setFocusable(false);
        txtCurdebt.setPreferredSize(new java.awt.Dimension(120, 30));
        txtCurdebt.setRequestFocusEnabled(false);

        txtCurdate.setEditable(false);
        txtCurdate.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        txtCurdate.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        txtCurdate.setFocusable(false);
        txtCurdate.setPreferredSize(new java.awt.Dimension(120, 30));
        txtCurdate.setRequestFocusEnabled(false);

        jLabel6.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel6.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel6.setText(AppLocal.getIntString("label.curdate")); // NOI18N
        jLabel6.setPreferredSize(new java.awt.Dimension(120, 30));

        txtName.setEditable(false);
        txtName.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        txtName.setFocusable(false);
        txtName.setMinimumSize(new java.awt.Dimension(64, 30));
        txtName.setPreferredSize(new java.awt.Dimension(0, 30));
        txtName.setRequestFocusEnabled(false);

        txtMaxdebt.setEditable(false);
        txtMaxdebt.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        txtMaxdebt.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        txtMaxdebt.setFocusable(false);
        txtMaxdebt.setPreferredSize(new java.awt.Dimension(120, 30));
        txtMaxdebt.setRequestFocusEnabled(false);

        txtPrePay.setForeground(new java.awt.Color(0, 204, 255));
        txtPrePay.setToolTipText(bundle.getString("tooltip.customerpay.prepay")); // NOI18N
        txtPrePay.setEnabled(false);
        txtPrePay.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
        txtPrePay.setPreferredSize(new java.awt.Dimension(200, 30));

        txtTaxId.setEditable(false);
        txtTaxId.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        txtTaxId.setFocusable(false);
        txtTaxId.setMinimumSize(new java.awt.Dimension(64, 30));
        txtTaxId.setPreferredSize(new java.awt.Dimension(150, 30));
        txtTaxId.setRequestFocusEnabled(false);

        jLabel7.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel7.setText(AppLocal.getIntString("label.taxid")); // NOI18N
        jLabel7.setPreferredSize(new java.awt.Dimension(150, 30));

        lblPrePay.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        lblPrePay.setText(AppLocal.getIntString("label.prepay")); // NOI18N
        lblPrePay.setPreferredSize(new java.awt.Dimension(120, 30));

        txtNotes.setToolTipText(bundle.getString("tooltip.customerpay.notes")); // NOI18N
        txtNotes.setEnabled(false);
        txtNotes.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        txtNotes.setPreferredSize(new java.awt.Dimension(250, 100));

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(txtName, javax.swing.GroupLayout.PREFERRED_SIZE, 240, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(txtCard, javax.swing.GroupLayout.PREFERRED_SIZE, 240, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(txtTaxId, javax.swing.GroupLayout.PREFERRED_SIZE, 240, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel12, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblPrePay, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(txtNotes, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(txtPrePay, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(txtMaxdebt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txtCurdebt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtCurdate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtTaxId, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtCard, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtNotes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(lblPrePay, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtPrePay, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(30, 30, 30)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtCurdebt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtMaxdebt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtCurdate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        add(jPanel1, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents
    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed

        readCustomer();
        
    }//GEN-LAST:event_jButton1ActionPerformed

    private void m_jKeysActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_jKeysActionPerformed
        // El teclado se usa sobre el campo activo (busqueda, notas o abono).
    }//GEN-LAST:event_m_jKeysActionPerformed

    private void btnCustomerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCustomerActionPerformed
        readCustomer();
        
}//GEN-LAST:event_btnCustomerActionPerformed

    private void btnPayActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPayActionPerformed
        processPayment(getPayableDebtAmount());
}//GEN-LAST:event_btnPayActionPerformed

    private void btnSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveActionPerformed

        handleSaveAction();
        
}//GEN-LAST:event_btnSaveActionPerformed

    private void btnPrePayActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPrePayActionPerformed
        Double prepay = parseAmount(txtPrePay.getText());
        if (prepay == null) {
            MessageInf msg = new MessageInf(MessageInf.SGN_WARNING, "Ingresa el valor abonado antes de continuar.");
            msg.show(this);
            txtPrePay.requestFocusInWindow();
            return;
        }

        processPayment(prepay);
    }//GEN-LAST:event_btnPrePayActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCustomer;
    private javax.swing.JButton btnPay;
    private javax.swing.JButton btnPrePay;
    private javax.swing.JButton btnSave;
    private javax.swing.JTextField editorcard;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JLabel lblPrePay;
    private com.openbravo.editor.JEditorKeys m_jKeys;
    private javax.swing.JTextField txtCard;
    private javax.swing.JTextField txtCurdate;
    private javax.swing.JTextField txtCurdebt;
    private javax.swing.JTextField txtMaxdebt;
    private javax.swing.JTextField txtName;
    private javax.swing.JTextArea txtNotes;
    private javax.swing.JTextField txtPrePay;
    private javax.swing.JTextField txtTaxId;
    // End of variables declaration//GEN-END:variables
}

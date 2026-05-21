//    KriolOS POS
//    Copyright (c) 2019-2023 KriolOS
//
//    This program is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.
//
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
package com.openbravo.pos.payment;

import com.openbravo.format.Formats;
import com.openbravo.pos.customers.CustomerInfoExt;
import com.openbravo.pos.customers.DataLogicCustomers;
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.pos.forms.AppView;
import com.openbravo.pos.forms.DataLogicSales;
import com.openbravo.pos.forms.DataLogicSystem;
import com.openbravo.pos.resources.ImageResources;
import com.openbravo.pos.util.RoundUtils;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JOptionPane;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.plaf.metal.MetalTabbedPaneUI;
import javax.swing.text.View;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author adrianromero
 */
public abstract class JPaymentSelect extends javax.swing.JDialog implements JPaymentNotifier {

    private static final long serialVersionUID = 1L;

    private PaymentInfoList m_aPaymentInfo;

    private boolean accepted;

    private AppView app;
    private double m_dTotal;
    private CustomerInfoExt customerext;
    private DataLogicSystem dlSystem;
    private DataLogicCustomers dlCustomers;
    private DataLogicSales dlSales;

    private final Map<String, JPaymentInterface> payments = new HashMap<>();
    private String m_sTransactionID;
    private boolean m_bAutoDebt = false; // Sebastian - Flag para auto-seleccionar pestaña Deuda
    private static PaymentInfo returnPayment = null;

    public static PaymentInfo getReturnPayment() {
        return returnPayment;
    }

    public static void setReturnPayment(PaymentInfo returnPayment) {
        JPaymentSelect.returnPayment = returnPayment;
    }

    protected JPaymentSelect(java.awt.Frame parent, boolean modal, ComponentOrientation o) {
        super(parent, modal);
        initComponents();
        this.applyComponentOrientation(o);
        getRootPane().setDefaultButton(m_jButtonOK);

    }

    protected JPaymentSelect(java.awt.Dialog parent, boolean modal, ComponentOrientation o) {
        super(parent, modal);
        initComponents();
        this.applyComponentOrientation(o);
    }

    public void init(AppView app) {
        this.app = app;
        dlSystem = (DataLogicSystem) app.getBean("com.openbravo.pos.forms.DataLogicSystem");
        dlCustomers = (DataLogicCustomers) app.getBean("com.openbravo.pos.customers.DataLogicCustomers");
        dlSales = (DataLogicSales) app.getBean("com.openbravo.pos.forms.DataLogicSales");

        m_jButtonPrint.setVisible(true);
        setPrintSelected(!Boolean.parseBoolean(app.getProperties().getProperty("till.receiptprintoff")));
        setPrintSelectedLabel();
    }

    // Sebastian - Método para activar cobro automático de deuda
    public void setAutoDebt(boolean b) {
        this.m_bAutoDebt = b;
    }

    private void setPrintSelectedLabel() {
        if (m_jButtonPrint.isSelected()) {
            jlblPrinterStatus.setText(AppLocal.getIntString("jpaymentselect.printer.on", "Printer on"));
        } else {
            jlblPrinterStatus.setText(AppLocal.getIntString("jpaymentselect.printer.off", "Printer off"));
        }
    }

    public void setPrintSelected(boolean value) {
        m_jButtonPrint.setSelected(value);
    }

    public boolean isPrintSelected() {
        return m_jButtonPrint.isSelected();
    }

    /**
     * List of PaymentInfo
     * 
     * @return
     */
    public List<PaymentInfo> getSelectedPayments() {
        return m_aPaymentInfo.getPayments();
    }

    /**
     * Get PaymentInfoList
     * 
     * @return
     */
    public PaymentInfoList getPaymentInfoList() {
        return m_aPaymentInfo;
    }

    /**
     * Get total
     *
     * @return
     */
    public double getTotal() {
        return m_aPaymentInfo.getTotal();
    }

    /**
     * Get total Paid
     *
     * @return
     */
    public double getPaidTotal() {
        return m_aPaymentInfo.getPaidTotal();
    }

    public boolean showDialog(double total, CustomerInfoExt customerext, double deposit) {
        m_aPaymentInfo = new PaymentInfoList();
        accepted = false;
        total = total - deposit;
        m_dTotal = total;

        this.customerext = customerext;
        setPrintSelected(!Boolean.parseBoolean(app.getProperties().getProperty("till.receiptprintoff")));
        setPrintSelectedLabel();
        m_jTotalEuros.setText(Formats.CURRENCY.formatValue(m_dTotal));

        addTabs();

        // remove all tabs
        m_jTabPayment.removeAll();

        return accepted;
    }

    public boolean showDialog(double total, CustomerInfoExt customerext) {

        m_aPaymentInfo = new PaymentInfoList();
        accepted = false;

        m_dTotal = total;

        this.customerext = customerext;

        setPrintSelected(!Boolean.parseBoolean(app.getProperties().getProperty("till.receiptprintoff")));
        setPrintSelectedLabel();
        m_jTotalEuros.setText(Formats.CURRENCY.formatValue(m_dTotal));

        /**
         * m_jPayTotal.setText(Formats.CURRENCY.formatValue(m_dTotal));
         * N. Deppe 08/11/2018
         * Fix issue where dialog keeps moving lower and lower on the screen
         * Get the size of the screen, and center the dialog in the window
         */
        Dimension screenDim = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension thisDim = this.getSize();
        int x = (screenDim.width - thisDim.width) / 2;
        int y = (screenDim.height - thisDim.height) / 2;
        this.setLocation(x, y);

        addTabs();

        if (m_jTabPayment.getTabCount() == 0) {
            // No payment panels available
            m_aPaymentInfo.add(getDefaultPayment(total));
            accepted = true;
        } else {
            getRootPane().setDefaultButton(m_jButtonOK);
            printState();
            setVisible(true);
        }
        m_jTabPayment.removeAll();

        return accepted;
    }

    protected abstract void addTabs();

    protected abstract void setStatusPanel(boolean isPositive, boolean isComplete);

    protected abstract PaymentInfo getDefaultPayment(double total);

    protected void setOKEnabled(boolean value) {
        m_jButtonOK.setEnabled(value);
    }

    protected void setAddEnabled(boolean value) {
        m_jButtonAdd.setEnabled(value);
    }

    protected void addTabPayment(JPaymentCreator jpay) {
        if (app.hasPermission(jpay.getKey())) {

            JPaymentInterface jpayinterface = payments.get(jpay.getKey());
            if (jpayinterface == null) {
                jpayinterface = jpay.createJPayment();
                payments.put(jpay.getKey(), jpayinterface);
            }

            jpayinterface.getComponent().applyComponentOrientation(getComponentOrientation());

            String title = AppLocal.getIntString(jpay.getLabelKey());

            m_jTabPayment.addTab(
                    fixedStringRithPad(AppLocal.getIntString(jpay.getLabelKey())),
                    ImageResources.getIcon(jpay.getIconKey()),
                    jpayinterface.getComponent(),
                    title);
        }
    }

    private String fixedStringRithPad(final String text) {
        return fixedStringRithPad(text, 10);
    }

    private String fixedStringRithPad(String text, int length) {

        if (text.length() > length) {
            text = text.substring(0, length - 1);
        }
        return StringUtils.rightPad(text, length, "");
    }

    public interface JPaymentCreator {

        public JPaymentInterface createJPayment();

        public String getKey();

        public String getLabelKey();

        public String getIconKey();
    }

    public class JPaymentCashCreator implements JPaymentCreator {

        @Override
        public JPaymentInterface createJPayment() {
            return new JPaymentCashPos(JPaymentSelect.this, dlSystem);
        }

        @Override
        public String getKey() {
            return "payment.cash";
        }

        @Override
        public String getLabelKey() {
            return "tab.cash";
        }

        @Override
        public String getIconKey() {
            return "/com/openbravo/images/cash.png";
        }
    }

    public class JPaymentChequeCreator implements JPaymentCreator {

        @Override
        public JPaymentInterface createJPayment() {
            return new JPaymentCheque(JPaymentSelect.this);
        }

        @Override
        public String getKey() {
            return "payment.cheque";
        }

        @Override
        public String getLabelKey() {
            return "tab.cheque";
        }

        @Override
        public String getIconKey() {
            return "/com/openbravo/images/cheque.png";
        }
    }

    public class JPaymentVoucherCreator implements JPaymentCreator {

        @Override
        public JPaymentInterface createJPayment() {
            return new JPaymentVoucher(app, JPaymentSelect.this, "voucherin");
        }

        @Override
        public String getKey() {
            return "payment.voucher";
        }

        @Override
        public String getLabelKey() {
            return "tab.voucher";
        }

        @Override
        public String getIconKey() {
            return "/com/openbravo/images/voucher.png";
        }
    }

    public class JPaymentMagcardCreator implements JPaymentCreator {

        @Override
        public JPaymentInterface createJPayment() {
            return new JPaymentMagcard(app, JPaymentSelect.this);
        }

        @Override
        public String getKey() {
            return "payment.magcard";
        }

        @Override
        public String getLabelKey() {
            return "tab.magcard";
        }

        @Override
        public String getIconKey() {
            return "/com/openbravo/images/ccard.png";
        }
    }

    public class JPaymentFreeCreator implements JPaymentCreator {

        @Override
        public JPaymentInterface createJPayment() {
            return new JPaymentFree(JPaymentSelect.this);
        }

        @Override
        public String getKey() {
            return "payment.free";
        }

        @Override
        public String getLabelKey() {
            return "tab.free";
        }

        @Override
        public String getIconKey() {
            return "/com/openbravo/images/wallet.png";
        }
    }

    public class JPaymentDebtCreator implements JPaymentCreator {

        @Override
        public JPaymentInterface createJPayment() {
            return new JPaymentDebt(JPaymentSelect.this);
        }

        @Override
        public String getKey() {
            return "payment.debt";
        }

        @Override
        public String getLabelKey() {
            return "tab.debt";
        }

        @Override
        public String getIconKey() {
            return "/com/openbravo/images/customer.png";
        }
    }

    public class JPaymentCashRefundCreator implements JPaymentCreator {

        @Override
        public JPaymentInterface createJPayment() {
            return new JPaymentRefund(JPaymentSelect.this, "cashrefund");
        }

        @Override
        public String getKey() {
            return "refund.cash";
        }

        @Override
        public String getLabelKey() {
            return "tab.cashrefund";
        }

        @Override
        public String getIconKey() {
            return "/com/openbravo/images/cash.png";
        }
    }

    public class JPaymentChequeRefundCreator implements JPaymentCreator {

        @Override
        public JPaymentInterface createJPayment() {
            return new JPaymentRefund(JPaymentSelect.this, "chequerefund");
        }

        @Override
        public String getKey() {
            return "refund.cheque";
        }

        @Override
        public String getLabelKey() {
            return "tab.chequerefund";
        }

        @Override
        public String getIconKey() {
            return "/com/openbravo/images/cheque.png";
        }
    }

    public class JPaymentVoucherRefundCreator implements JPaymentCreator {

        @Override
        public JPaymentInterface createJPayment() {
            return new JPaymentRefund(JPaymentSelect.this, "voucherout");
        }

        @Override
        public String getKey() {
            return "refund.voucher";
        }

        @Override
        public String getLabelKey() {
            return "tab.voucher";
        }

        @Override
        public String getIconKey() {
            return "/com/openbravo/images/voucher.png";
        }
    }

    public class JPaymentMagcardRefundCreator implements JPaymentCreator {

        @Override
        public JPaymentInterface createJPayment() {
            return new JPaymentMagcard(app, JPaymentSelect.this);
        }

        @Override
        public String getKey() {
            return "refund.magcard";
        }

        @Override
        public String getLabelKey() {
            return "tab.magcard";
        }

        @Override
        public String getIconKey() {
            return "/com/openbravo/images/ccard.png";
        }
    }

    public class JPaymentBankCreator implements JPaymentCreator {

        @Override
        public JPaymentInterface createJPayment() {
            return new JPaymentBank(JPaymentSelect.this);
        }

        @Override
        public String getKey() {
            return "payment.bank";
        }

        @Override
        public String getLabelKey() {
            return "tab.bank";
        }

        @Override
        public String getIconKey() {
            return "/com/openbravo/images/bank.png";
        }
    }

    public class JPaymentSlipCreator implements JPaymentCreator {

        @Override
        public JPaymentInterface createJPayment() {
            return new JPaymentSlip(JPaymentSelect.this);
        }

        @Override
        public String getKey() {
            return "payment.slip";
        }

        @Override
        public String getLabelKey() {
            return "tab.slip";
        }

        @Override
        public String getIconKey() {
            return "/com/openbravo/images/slip.png";
        }
    }

    private int findDebtTabIndex() {
        for (int i = 0; i < m_jTabPayment.getTabCount(); i++) {
            if (m_jTabPayment.getComponentAt(i) instanceof JPaymentDebt) {
                return i;
            }
        }
        return -1;
    }

    private void printState() {
        double remaining = m_dTotal - m_aPaymentInfo.getTotal();
        m_jRemaininglEuros.setText(Formats.CURRENCY.formatValue(remaining));
        m_jButtonRemove.setEnabled(!m_aPaymentInfo.isEmpty());
        
        // Sebastian - Primero activar el componente actual para asegurar que tenga los datos correctos
        JPaymentInterface activeComponent = (JPaymentInterface) m_jTabPayment.getSelectedComponent();
        if (activeComponent != null) {
             activeComponent.activate(customerext, remaining, m_sTransactionID);
        }

        // Sebastian - Auto-AÑADIR pago de Deuda si es un apartado inicial
        if (m_bAutoDebt && m_aPaymentInfo.isEmpty()) {
            int debtTabIndex = findDebtTabIndex();
            m_bAutoDebt = false; // Evita reintentos si printState() se vuelve a ejecutar

            if (debtTabIndex >= 0) {
                m_jTabPayment.setSelectedIndex(debtTabIndex);
                // Re-activar el componente de deuda ahora que está seleccionado
                activeComponent = (JPaymentInterface) m_jTabPayment.getSelectedComponent();
                if (activeComponent != null) {
                    activeComponent.activate(customerext, remaining, m_sTransactionID);

                    // Ahora sí, ejecutar el pago con los datos ya cargados
                    PaymentInfo autoPayment = activeComponent.executePayment();
                    if (autoPayment != null) {
                        m_aPaymentInfo.add(autoPayment);
                        printState(); // Actualizar UI

                        // Sebastian - Si el pago de deuda está completo, CERRAR con éxito automáticamente
                        if (RoundUtils.compare(m_aPaymentInfo.getTotal(), m_dTotal) >= 0) {
                             m_jButtonOK.setEnabled(true);
                             accepted = true; // Indicar que el pago fue aceptado
                             dispose(); // Cerrar el diálogo devolviendo éxito
                        }
                        return;
                    }
                }
            }
        }

        if (m_aPaymentInfo.isEmpty() && m_jTabPayment.getTabCount() > 0) {
            m_jTabPayment.setSelectedIndex(0);
        }
    }

    protected static Window getWindow(Component parent) {
        if (parent == null) {
            return new JFrame();
        } else if (parent instanceof Frame || parent instanceof Dialog) {
            return (Window) parent;
        } else {
            return getWindow(parent.getParent());
        }
    }

    @Override
    public void setStatus(boolean isPositive, boolean isComplete) {
        setStatusPanel(isPositive, isComplete);
    }

    @Override
    public void updateRemaining(double tendered) {
        // Calcular el restante basado en el monto introducido
        double remaining = m_dTotal - tendered;

        // Actualizar el campo de restante en tiempo real
        m_jRemaininglEuros.setText(Formats.CURRENCY.formatValue(remaining));
    }

    public void setTransactionID(String tID) {
        this.m_sTransactionID = tID;
    }

    private void initComponents() {

        jPanel4 = new javax.swing.JPanel();
        m_jLblTotalEuros1 = new javax.swing.JLabel();
        m_jTotalEuros = new javax.swing.JLabel();
        m_jButtonAdd = new javax.swing.JButton();
        m_jButtonRemove = new javax.swing.JButton();
        
        jPanel3 = new javax.swing.JPanel();
        m_jTabPayment = new javax.swing.JTabbedPane();
        
        jPanel5 = new javax.swing.JPanel();
        m_jButtonCobrarImprimir = new javax.swing.JButton();
        m_jButtonCobrarSinImprimir = new javax.swing.JButton();
        m_jButtonCancel = new javax.swing.JButton();
        
        // Hidden components
        m_jButtonOK = new javax.swing.JButton();
        m_jButtonPrint = new javax.swing.JToggleButton();
        jlblPrinterStatus = new javax.swing.JLabel();
        m_jLblRemainingEuros = new javax.swing.JLabel();
        m_jRemaininglEuros = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Cobrar Venta");
        setMinimumSize(new java.awt.Dimension(1100, 750));
        setPreferredSize(new java.awt.Dimension(1200, 800));
        getContentPane().setBackground(new java.awt.Color(243, 244, 246));

        // HEADER
        jPanel4.setBackground(java.awt.Color.WHITE);
        jPanel4.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, new java.awt.Color(209, 213, 219)));

        m_jLblTotalEuros1.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 28));
        m_jLblTotalEuros1.setForeground(new java.awt.Color(31, 41, 55));
        m_jLblTotalEuros1.setText("Total a cobrar:");

        m_jTotalEuros.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 80));
        m_jTotalEuros.setForeground(new java.awt.Color(37, 99, 235));
        m_jTotalEuros.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        m_jTotalEuros.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 20, 0, 20));

        m_jButtonAdd.setBackground(new java.awt.Color(37, 99, 235));
        m_jButtonAdd.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/btnplus.png")));
        m_jButtonAdd.setPreferredSize(new java.awt.Dimension(80, 80));
        m_jButtonAdd.setFocusPainted(false);
        m_jButtonAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jButtonAddActionPerformed(evt);
            }
        });

        m_jButtonRemove.setBackground(new java.awt.Color(239, 68, 68));
        m_jButtonRemove.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/btnminus.png")));
        m_jButtonRemove.setPreferredSize(new java.awt.Dimension(80, 80));
        m_jButtonRemove.setFocusPainted(false);
        m_jButtonRemove.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jButtonRemoveActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(30, 30, 30)
                .addComponent(m_jLblTotalEuros1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(m_jTotalEuros, javax.swing.GroupLayout.DEFAULT_SIZE, 500, Short.MAX_VALUE)
                .addGap(20, 20, 20)
                .addComponent(m_jButtonAdd, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(10, 10, 10)
                .addComponent(m_jButtonRemove, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(30, 30, 30))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(m_jLblTotalEuros1)
                    .addComponent(m_jTotalEuros, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(m_jButtonAdd, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(m_jButtonRemove, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(15, 15, 15))
        );

        getContentPane().add(jPanel4, java.awt.BorderLayout.NORTH);

        // CENTER
        jPanel3.setLayout(new java.awt.BorderLayout());
        jPanel3.setBackground(new java.awt.Color(243, 244, 246));

        m_jTabPayment.setTabPlacement(javax.swing.JTabbedPane.LEFT);
        m_jTabPayment.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 22));
        m_jTabPayment.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));
        m_jTabPayment.setBackground(new java.awt.Color(243, 244, 246));
        m_jTabPayment.setOpaque(true);
        m_jTabPayment.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                m_jTabPaymentStateChanged(evt);
            }
        });
        m_jTabPayment.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                m_jTabPaymentKeyPressed(evt);
            }
        });
        jPanel3.add(m_jTabPayment, java.awt.BorderLayout.CENTER);

        getContentPane().add(jPanel3, java.awt.BorderLayout.CENTER);

        // EAST
        jPanel5.setPreferredSize(new java.awt.Dimension(250, 0));
        jPanel5.setBackground(new java.awt.Color(243, 244, 246));
        jPanel5.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 1, 0, 0, new java.awt.Color(209, 213, 219)));
        jPanel5.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 0, 15));

        javax.swing.JPanel btnContainer = new javax.swing.JPanel();
        btnContainer.setLayout(new java.awt.GridLayout(3, 1, 0, 10));
        btnContainer.setBackground(new java.awt.Color(243, 244, 246));
        btnContainer.setPreferredSize(new java.awt.Dimension(220, 220));

        m_jButtonCobrarImprimir.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 15));
        m_jButtonCobrarImprimir.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/printer24.png")));
        m_jButtonCobrarImprimir.setText(" F1 - Imprimir y Cobrar");
        m_jButtonCobrarImprimir.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        m_jButtonCobrarImprimir.setBackground(new java.awt.Color(37, 99, 235));
        m_jButtonCobrarImprimir.setForeground(java.awt.Color.WHITE);
        m_jButtonCobrarImprimir.setFocusPainted(false);
        m_jButtonCobrarImprimir.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        m_jButtonCobrarImprimir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jButtonPrint.setSelected(true);
                m_jButtonOKActionPerformed(evt);
            }
        });
        btnContainer.add(m_jButtonCobrarImprimir);

        m_jButtonCobrarSinImprimir.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 15));
        m_jButtonCobrarSinImprimir.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/cash.png")));
        m_jButtonCobrarSinImprimir.setText(" F2 - Solo Cobrar ");
        m_jButtonCobrarSinImprimir.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        m_jButtonCobrarSinImprimir.setBackground(java.awt.Color.WHITE);
        m_jButtonCobrarSinImprimir.setForeground(new java.awt.Color(31, 41, 55));
        m_jButtonCobrarSinImprimir.setFocusPainted(false);
        m_jButtonCobrarSinImprimir.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        m_jButtonCobrarSinImprimir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jButtonPrint.setSelected(false);
                m_jButtonOKActionPerformed(evt);
            }
        });
        btnContainer.add(m_jButtonCobrarSinImprimir);

        m_jButtonCancel.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 15));
        m_jButtonCancel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/cancel.png")));
        m_jButtonCancel.setText(" ESC - Cancelar");
        m_jButtonCancel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        m_jButtonCancel.setBackground(java.awt.Color.WHITE);
        m_jButtonCancel.setForeground(new java.awt.Color(239, 68, 68));
        m_jButtonCancel.setFocusPainted(false);
        m_jButtonCancel.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        m_jButtonCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jButtonCancelActionPerformed(evt);
            }
        });
        btnContainer.add(m_jButtonCancel);

        jPanel5.add(btnContainer);
        getContentPane().add(jPanel5, java.awt.BorderLayout.EAST);

        m_jButtonOK.setVisible(false);
        m_jButtonPrint.setVisible(false);
        jlblPrinterStatus.setVisible(false);
        m_jLblRemainingEuros.setVisible(false);
        m_jRemaininglEuros.setVisible(false);

        getRootPane().getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F1, 0), "cobrarImprimir");
        getRootPane().getActionMap().put("cobrarImprimir", new javax.swing.AbstractAction() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                if (m_jButtonCobrarImprimir.isEnabled()) m_jButtonCobrarImprimir.doClick();
            }
        });
        getRootPane().getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F2, 0), "cobrarSinImprimir");
        getRootPane().getActionMap().put("cobrarSinImprimir", new javax.swing.AbstractAction() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                if (m_jButtonCobrarSinImprimir.isEnabled()) m_jButtonCobrarSinImprimir.doClick();
            }
        });
        getRootPane().getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ESCAPE, 0), "cancelar");
        getRootPane().getActionMap().put("cancelar", new javax.swing.AbstractAction() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                m_jButtonCancel.doClick();
            }
        });

        setSize(new java.awt.Dimension(1100, 750));
        setLocationRelativeTo(null);
    }

    private void m_jButtonRemoveActionPerformed(java.awt.event.ActionEvent evt) {
        m_aPaymentInfo.removeLast();
        printState();
    }

    private void m_jButtonAddActionPerformed(java.awt.event.ActionEvent evt) {
        PaymentInfo rp = ((JPaymentInterface) m_jTabPayment.getSelectedComponent()).executePayment();
        if (rp != null) {
            m_aPaymentInfo.add(rp);
            printState();
            double rem = m_dTotal - m_aPaymentInfo.getTotal();
            if (rem > 0.01) {
                JOptionPane.showMessageDialog(this, 
                    "<html><div style='text-align: center;'><b>Pago Parcial Registrado</b><br>Faltan: <b>" + Formats.CURRENCY.formatValue(rem) + "</b></div></html>",
                    "Pago Parcial", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    private void m_jTabPaymentStateChanged(javax.swing.event.ChangeEvent evt) {
        if (m_jTabPayment.getSelectedComponent() != null) {
            ((JPaymentInterface) m_jTabPayment.getSelectedComponent()).activate(customerext, m_dTotal - m_aPaymentInfo.getTotal(), m_sTransactionID);
            m_jRemaininglEuros.setText(Formats.CURRENCY.formatValue(m_dTotal - m_aPaymentInfo.getTotal()));
        }
    }

    private void m_jButtonOKActionPerformed(java.awt.event.ActionEvent evt) {
        SwingWorker<Object, Object> worker = new SwingWorker<>() {
            @Override
            protected Object doInBackground() throws Exception {
                setReturnPayment(((JPaymentInterface) m_jTabPayment.getSelectedComponent()).executePayment());
                return null;
            }
            @Override
            public void done() {
                if (returnPayment != null) {
                    m_aPaymentInfo.add(returnPayment);
                    double rem = m_dTotal - m_aPaymentInfo.getTotal();
                    if (rem > 0.01) {
                        JOptionPane.showMessageDialog(JPaymentSelect.this, 
                            "<html><div style='text-align: center;'><b>⚠️ Pago Incompleto</b><br>Faltan: <b>" + Formats.CURRENCY.formatValue(rem) + "</b></div></html>", 
                            "Pago Incompleto", JOptionPane.WARNING_MESSAGE);
                        printState();
                    } else {
                        accepted = true;
                        dispose();
                    }
                }
            }
        };
        worker.execute();
    }

    private void m_jButtonCancelActionPerformed(java.awt.event.ActionEvent evt) {
        dispose();
    }

    private void m_jButtonPrintActionPerformed(java.awt.event.ActionEvent evt) {
        setPrintSelectedLabel();
    }

    private void m_jTabPaymentKeyPressed(java.awt.event.KeyEvent evt) {
        if (evt.getKeyCode() == KeyEvent.VK_F1) {
            if (m_jButtonCobrarImprimir.isEnabled()) m_jButtonCobrarImprimir.doClick();
        } else if (evt.getKeyCode() == KeyEvent.VK_F2) {
            if (m_jButtonCobrarSinImprimir.isEnabled()) m_jButtonCobrarSinImprimir.doClick();
        } else if (evt.getKeyCode() == KeyEvent.VK_ESCAPE) {
            m_jButtonCancel.doClick();
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JLabel jlblPrinterStatus;
    private javax.swing.JButton m_jButtonAdd;
    private javax.swing.JButton m_jButtonCancel;
    private javax.swing.JButton m_jButtonOK;
    private javax.swing.JToggleButton m_jButtonPrint;
    private javax.swing.JButton m_jButtonRemove;
    private javax.swing.JLabel m_jLblRemainingEuros;
    private javax.swing.JLabel m_jLblTotalEuros1;
    private javax.swing.JLabel m_jRemaininglEuros;
    private javax.swing.JTabbedPane m_jTabPayment;
    private javax.swing.JLabel m_jTotalEuros;
    private javax.swing.JButton m_jButtonCobrarImprimir;
    private javax.swing.JButton m_jButtonCobrarSinImprimir;
    // End of variables declaration//GEN-END:variables
}

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

package com.openbravo.pos.panels;

import com.openbravo.basic.BasicException;
import com.openbravo.data.gui.ComboBoxValModel;
import com.openbravo.data.loader.IKeyed;
import com.openbravo.data.user.DirtyManager;
import com.openbravo.data.user.EditorRecord;
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.pos.forms.AppView;
import java.awt.Component;
import java.util.Date;
import java.util.UUID;


/**
 *
 * @author adrianromero
 */
public final class PaymentsEditor extends javax.swing.JPanel implements EditorRecord {
    
    private final ComboBoxValModel m_ReasonModel;
    
    private String m_sId;
    private String m_sPaymentId;
    private Date datenew;
   
    private final AppView m_App;
    private String m_sNotes;
    
    /** Creates new form JPanelPayments
     * @param oApp
     * @param dirty */
    public PaymentsEditor(AppView oApp, DirtyManager dirty) {
        
        m_App = oApp;
        
        initComponents();
        
        m_ReasonModel = new ComboBoxValModel();
        m_ReasonModel.add(new PaymentReasonPositive("cashin", AppLocal.getIntString("transpayment.cashin")));
        m_ReasonModel.add(new PaymentReasonNegative("cashout", AppLocal.getIntString("transpayment.cashout")));              
        m_jreason.setModel(m_ReasonModel);
        
        jTotal.addEditorKeys(m_jKeys);
        
        m_jreason.addActionListener(dirty);
        jTotal.addPropertyChangeListener("Text", dirty);
        m_jNotes.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { dirty.setDirty(true); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { dirty.setDirty(true); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { dirty.setDirty(true); }
        });        
        
        m_jNotes.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                if (m_jKeys != null) {
                    m_jKeys.setInactive(jTotal);
                }
            }
        });
        m_jNotes.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                m_jNotes.requestFocusInWindow();
            }
        });
        
        // Rediseño moderno de la vista sin teclado y con ancho controlado
        // En lugar de remover jPanel2, ocultamos todos sus botones para que m_txtKeys
        // siga en el árbol de componentes activos y pueda capturar el foco del teclado.
        for (java.awt.Component comp : m_jKeys.getComponents()) {
            if (!(comp instanceof javax.swing.JTextField)) {
                comp.setVisible(false);
            }
        }
        m_jKeys.setPreferredSize(new java.awt.Dimension(0, 0));
        m_jKeys.setMinimumSize(new java.awt.Dimension(0, 0));
        m_jKeys.setMaximumSize(new java.awt.Dimension(0, 0));
        
        jPanel2.setPreferredSize(new java.awt.Dimension(0, 0));
        jPanel2.setMinimumSize(new java.awt.Dimension(0, 0));
        jPanel2.setMaximumSize(new java.awt.Dimension(0, 0));
        
        setBackground(java.awt.Color.WHITE);
        
        jPanel3.removeAll();
        jPanel3.setLayout(new java.awt.GridBagLayout());
        jPanel3.setOpaque(true);
        jPanel3.setBackground(java.awt.Color.WHITE);
        jPanel3.setBorder(javax.swing.BorderFactory.createEmptyBorder(24, 32, 24, 32));
        
        // Centered form container
        javax.swing.JPanel formContainer = new javax.swing.JPanel(new java.awt.GridBagLayout());
        formContainer.setOpaque(false);
        
        java.awt.GridBagConstraints gbc = new java.awt.GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gbc.anchor = java.awt.GridBagConstraints.WEST;
        gbc.weightx = 1.0;
        
        // 1. Título del Movimiento (Fila 0)
        javax.swing.JLabel jLabelNotes = new javax.swing.JLabel("Título del Movimiento");
        jLabelNotes.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 15));
        jLabelNotes.setForeground(new java.awt.Color(71, 85, 105));
        gbc.insets = new java.awt.Insets(0, 0, 8, 0);
        formContainer.add(jLabelNotes, gbc);
        
        // Campo Notas (Fila 1)
        gbc.gridy = 1;
        m_jNotes.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 16));
        m_jNotes.setPreferredSize(new java.awt.Dimension(550, 45));
        m_jNotes.setFocusable(true);
        m_jNotes.setBorder(javax.swing.BorderFactory.createCompoundBorder(
            javax.swing.BorderFactory.createLineBorder(new java.awt.Color(203, 213, 225), 1), // slate-300
            javax.swing.BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        gbc.insets = new java.awt.Insets(0, 0, 24, 0);
        formContainer.add(m_jNotes, gbc);
        
        // 2. Tipo de Movimiento (Fila 2)
        gbc.gridy = 2;
        jLabel5.setText("Tipo de Movimiento");
        jLabel5.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 15));
        jLabel5.setForeground(new java.awt.Color(71, 85, 105)); // slate-600
        gbc.insets = new java.awt.Insets(0, 0, 8, 0);
        formContainer.add(jLabel5, gbc);
        
        // Combobox (Fila 3)
        gbc.gridy = 3;
        m_jreason.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 16));
        m_jreason.setPreferredSize(new java.awt.Dimension(550, 45));
        m_jreason.setBackground(java.awt.Color.WHITE);
        gbc.insets = new java.awt.Insets(0, 0, 24, 0);
        formContainer.add(m_jreason, gbc);
        
        // 3. Cantidad / Monto (Fila 4)
        gbc.gridy = 4;
        jLabel3.setText("Cantidad / Monto ($)");
        jLabel3.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 15));
        jLabel3.setForeground(new java.awt.Color(71, 85, 105));
        gbc.insets = new java.awt.Insets(0, 0, 8, 0);
        formContainer.add(jLabel3, gbc);
        
        // Campo Cantidad (Fila 5)
        gbc.gridy = 5;
        jTotal.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 18));
        jTotal.setPreferredSize(new java.awt.Dimension(550, 45));
        gbc.insets = new java.awt.Insets(0, 0, 24, 0);
        formContainer.add(jTotal, gbc);
        
        // Add formContainer to jPanel3 at the top-center
        java.awt.GridBagConstraints centerGbc = new java.awt.GridBagConstraints();
        centerGbc.gridx = 0;
        centerGbc.gridy = 0;
        centerGbc.anchor = java.awt.GridBagConstraints.NORTH;
        centerGbc.insets = new java.awt.Insets(40, 0, 0, 0);
        centerGbc.weighty = 1.0;
        jPanel3.add(formContainer, centerGbc);
        
        writeValueEOF();
    }
    
    /**
     *
     */
    @Override
    public void writeValueEOF() {
        m_sId = null;
        m_sPaymentId = null;
        datenew = null;
        setReasonTotal(null, null);
        m_jreason.setEnabled(false);
        jTotal.setEnabled(false);
// JG Added July 2011
        m_sNotes = null;
        m_jNotes.setEnabled(false);
        m_jNotes.setEditable(false);

    }

    /**
     *
     */
    @Override
    public void writeValueInsert() {

        m_sId = null;
        m_sPaymentId = null;
        datenew = null;
        setReasonTotal("cashin", null);
        m_jreason.setEnabled(true);
        jTotal.setEnabled(true);   
        jTotal.activate();
// JG Added July 2011
        m_sNotes = null;
        m_jNotes.setEnabled(true);
        m_jNotes.setEditable(true);
        m_jNotes.setText(m_sNotes);
    }
    
    /**
     *
     * @param value
     */
    @Override
    public void writeValueDelete(Object value) {
        Object[] payment = (Object[]) value;
        m_sId = (String) payment[0];
        datenew = (Date) payment[2];
        m_sPaymentId = (String) payment[3];
        setReasonTotal(payment[4], payment[5]);
        m_jreason.setEnabled(false);
        jTotal.setEnabled(false);
// JG Added July 2011
        m_sNotes = (String) payment[6];
        m_jNotes.setEnabled(false);
        m_jNotes.setEditable(false);
    }
    
    /**
     *
     * @param value
     */
    @Override
    public void writeValueEdit(Object value) {
        Object[] payment = (Object[]) value;
        m_sId = (String) payment[0];
        datenew = (Date) payment[2];
        m_sPaymentId = (String) payment[3];
        setReasonTotal(payment[4], payment[5]);
        m_jreason.setEnabled(false);
        jTotal.setEnabled(false);
        jTotal.activate();
// JG Added July 2011
        m_sNotes = (String) payment[6];
        m_jNotes.setEnabled(false);
        m_jNotes.setEditable(false);
    }
    
    /**
     *
     * @return
     * @throws BasicException
     */
    @Override
    public Object createValue() throws BasicException {
//JG Modified Array + 1 - July 2011
        Object[] payment = new Object[7];
        payment[0] = m_sId == null ? UUID.randomUUID().toString() : m_sId;
        payment[1] = m_App.getActiveCashIndex();
        payment[2] = datenew == null ? new Date() : datenew;
        payment[3] = m_sPaymentId == null ? UUID.randomUUID().toString() : m_sPaymentId;
        payment[4] = m_ReasonModel.getSelectedKey();
        PaymentReason reason = (PaymentReason) m_ReasonModel.getSelectedItem();
        Double dtotal = jTotal.getValue();
        payment[5] = reason == null ? dtotal : reason.addSignum(dtotal);
// JG Added July 2011
        String snotes = "";
        m_sNotes = m_jNotes.getText();
        payment[6] = m_sNotes == null ? snotes : m_sNotes;
        return payment;
    }
    
    /**
     *
     * @return
     */
    @Override
    public Component getComponent() {
        return this;
    }
    
    /**
     *
     */
    @Override
    public void refresh() {
    }  

    public void setMovementData(String reasonKey, Double signedTotal, String notes) {
        setReasonTotal(reasonKey, signedTotal);
        m_jNotes.setText(notes == null ? "" : notes);
    }

    public String getNotes() {
        return m_jNotes.getText();
    }

    public void setNotes(String notes) {
        m_jNotes.setText(notes);
    }

    public String getReasonKey() {
        return (String) m_ReasonModel.getSelectedKey();
    }

    public void setReasonKey(String key) {
        m_ReasonModel.setSelectedKey(key);
        PaymentReason reason = (PaymentReason) m_ReasonModel.getSelectedItem();     
        if (reason == null) {
            jTotal.setDoubleValue(jTotal.getValue());
        } else {
            jTotal.setDoubleValue(reason.positivize(jTotal.getValue()));
        }
    }

    public Double getAmountValue() {
        return jTotal.getValue();
    }

    public void setAmountValue(Double val) {
        jTotal.setDoubleValue(val);
    }

    public Double getSignedTotal() {
        PaymentReason reason = (PaymentReason) m_ReasonModel.getSelectedItem();
        Double dtotal = jTotal.getValue();
        return reason == null ? dtotal : reason.addSignum(dtotal);
    }

    public void setFieldsEnabled(boolean enabled) {
        m_jNotes.setEnabled(enabled);
        m_jNotes.setEditable(enabled);
        m_jreason.setEnabled(enabled);
        jTotal.setEnabled(enabled);
    }
    
    private void setReasonTotal(Object reasonfield, Object totalfield) {
        
        m_ReasonModel.setSelectedKey(reasonfield);
             
        PaymentReason reason = (PaymentReason) m_ReasonModel.getSelectedItem();     
        
        if (reason == null) {
            jTotal.setDoubleValue((Double) totalfield);
        } else {
            jTotal.setDoubleValue(reason.positivize((Double) totalfield));
        }  
    }
    
    private static abstract class PaymentReason implements IKeyed {
        private String m_sKey;
        private String m_sText;
        
        public PaymentReason(String key, String text) {
            m_sKey = key;
            m_sText = text;
        }
        @Override
        public Object getKey() {
            return m_sKey;
        }
        public abstract Double positivize(Double d);
        public abstract Double addSignum(Double d);
        
        @Override
        public String toString() {
            return m_sText;
        }
    }
    private static class PaymentReasonPositive extends PaymentReason {
        public PaymentReasonPositive(String key, String text) {
            super(key, text);
        }
        @Override
        public Double positivize(Double d) {
            return d;
        }
        @Override
        public Double addSignum(Double value) {
            if (value == null) {
                return null;
            } else if (value < 0.0) {
                return -value;
            } else {
                return value;
            }
        }
    }
    private static class PaymentReasonNegative extends PaymentReason {
        public PaymentReasonNegative(String key, String text) {
            super(key, text);
        }
        @Override
        public Double positivize(Double d) {
            return d == null ? null : -d;
        }
        @Override
        public Double addSignum(Double d) {
            if (d == null) {
                return null;
            } else if (d > 0.0) {
                return -d;
            } else {
                return d;
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

        jPanel3 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        m_jreason = new javax.swing.JComboBox();
        jLabel3 = new javax.swing.JLabel();
        jTotal = new com.openbravo.editor.JEditorCurrency();
        m_jNotes = new javax.swing.JTextField();
        jPanel2 = new javax.swing.JPanel();
        m_jKeys = new com.openbravo.editor.JEditorKeys();

        setLayout(new java.awt.BorderLayout());

        jLabel5.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel5.setText(AppLocal.getIntString("label.paymentreason")); // NOI18N
        jLabel5.setPreferredSize(new java.awt.Dimension(110, 30));

        m_jreason.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        m_jreason.setFocusable(false);
        m_jreason.setPreferredSize(new java.awt.Dimension(200, 30));

        jLabel3.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel3.setText(AppLocal.getIntString("label.paymenttotal")); // NOI18N
        jLabel3.setPreferredSize(new java.awt.Dimension(110, 30));

        jTotal.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jTotal.setPreferredSize(new java.awt.Dimension(200, 30));

        m_jNotes.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        m_jNotes.setPreferredSize(new java.awt.Dimension(132, 100));

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(m_jNotes, javax.swing.GroupLayout.DEFAULT_SIZE, 234, Short.MAX_VALUE)
                    .addComponent(m_jreason, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTotal, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(m_jreason, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jTotal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(m_jNotes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        add(jPanel3, java.awt.BorderLayout.CENTER);

        jPanel2.setLayout(new java.awt.BorderLayout());

        m_jKeys.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                m_jKeysPropertyChange(evt);
            }
        });
        jPanel2.add(m_jKeys, java.awt.BorderLayout.NORTH);

        add(jPanel2, java.awt.BorderLayout.LINE_END);
    }// </editor-fold>//GEN-END:initComponents

    private void m_jKeysPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_m_jKeysPropertyChange

    }//GEN-LAST:event_m_jKeysPropertyChange
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private com.openbravo.editor.JEditorCurrency jTotal;
    private com.openbravo.editor.JEditorKeys m_jKeys;
    private javax.swing.JTextField m_jNotes;
    private javax.swing.JComboBox m_jreason;
    // End of variables declaration//GEN-END:variables
    
}

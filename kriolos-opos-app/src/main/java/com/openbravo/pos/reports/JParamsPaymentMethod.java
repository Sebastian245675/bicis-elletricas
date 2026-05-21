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

package com.openbravo.pos.reports;

import com.openbravo.basic.BasicException;
import com.openbravo.data.gui.ComboBoxValModel;
import com.openbravo.data.loader.Datas;
import com.openbravo.data.loader.QBFCompareEnum;
import com.openbravo.data.loader.SerializerWrite;
import com.openbravo.data.loader.SerializerWriteBasic;
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.pos.forms.AppView;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JComboBox;
import javax.swing.GroupLayout;
import javax.swing.LayoutStyle;
import javax.swing.border.EmptyBorder;

/**
 *
 * @author Sebastian
 */
public class JParamsPaymentMethod extends javax.swing.JPanel implements ReportEditorCreator {
    
    private ComboBoxValModel m_PaymentModel;
    private JLabel jLabel2;
    private JComboBox m_jpayment;
    
    private static class PaymentMethodItem implements com.openbravo.data.loader.IKeyed {
        private final String key;
        private final String display;
        
        public PaymentMethodItem(String key, String display) {
            this.key = key;
            this.display = display;
        }
        
        @Override
        public Object getKey() {
            return key;
        }
        
        @Override
        public String toString() {
            return display;
        }
    }
    
    /** Creates new form JParamsPaymentMethod */
    public JParamsPaymentMethod() {
        initComponents();
        styleComponents();
        
        m_PaymentModel = new ComboBoxValModel();
        m_PaymentModel.add(null); // All
        m_PaymentModel.add(new PaymentMethodItem("cash", "Efectivo"));
        m_PaymentModel.add(new PaymentMethodItem("card", "Tarjeta"));
        m_PaymentModel.add(new PaymentMethodItem("magcard", "Tarjeta (Banda)"));
        m_PaymentModel.add(new PaymentMethodItem("debt", "Crédito"));
        m_PaymentModel.add(new PaymentMethodItem("voucher", "Vales"));
        m_PaymentModel.add(new PaymentMethodItem("transfer", "Transferencia"));
        
        m_jpayment.setModel(m_PaymentModel);
        m_PaymentModel.setSelectedKey(null);
    }
    
    private void styleComponents() {
        setOpaque(false);
        setBorder(new EmptyBorder(5, 5, 5, 5));
        
        Font fontLabel = new Font("Segoe UI", Font.BOLD, 13);
        Font fontText = new Font("Segoe UI", Font.PLAIN, 14);
        Color colorLabel = new Color(70, 80, 90);

        jLabel2.setFont(fontLabel);
        jLabel2.setForeground(colorLabel);
        
        m_jpayment.setFont(fontText);
        m_jpayment.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(210, 220, 230)),
            BorderFactory.createEmptyBorder(2, 5, 2, 5)
        ));
        
        // Custom renderer para mostrar nombres en español
        m_jpayment.setRenderer(new javax.swing.DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(javax.swing.JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value == null) {
                    setText("[Todos]");
                } else if (value instanceof PaymentMethodItem) {
                    setText(((PaymentMethodItem) value).display);
                }
                return this;
            }
        });
    }
    
    private void initComponents() {
        jLabel2 = new JLabel();
        m_jpayment = new JComboBox();

        // Aumentar el alto para que sea visible en el BoxLayout de JParamsComposed
        setMaximumSize(new Dimension(430, 50));
        setMinimumSize(new Dimension(430, 50));
        setPreferredSize(new Dimension(430, 50));

        jLabel2.setText("Método de Pago:");
        jLabel2.setPreferredSize(new Dimension(125, 32));

        m_jpayment.setPreferredSize(new Dimension(210, 34));

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel2, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(m_jpayment, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(m_jpayment, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
    }
    
    @Override
    public void init(AppView app) {
    }

    @Override
    public void activate() throws BasicException {
    }

    @Override
    public SerializerWrite getSerializerWrite() {
        return new SerializerWriteBasic(new Datas[] {Datas.OBJECT, Datas.STRING});
    }

    @Override
    public Component getComponent() {
        return this;
    }
    
    @Override
    public Object createValue() throws BasicException {
        return new Object[] {
            m_PaymentModel.getSelectedItem() == null ? QBFCompareEnum.COMP_NONE : QBFCompareEnum.COMP_EQUALS, 
            m_PaymentModel.getSelectedKey()
        };
    }
}

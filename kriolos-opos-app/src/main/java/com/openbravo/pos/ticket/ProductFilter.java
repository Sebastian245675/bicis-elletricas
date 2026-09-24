//    KriolOS POS
//    Copyright (c) 2019-2023 KriolOS
//    
//
//     
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

package com.openbravo.pos.ticket;

import com.openbravo.basic.BasicException;
import com.openbravo.data.gui.ComboBoxValModel;
import com.openbravo.data.gui.ListQBFModelNumber;
import com.openbravo.data.loader.Datas;
import com.openbravo.data.loader.QBFCompareEnum;
import com.openbravo.data.loader.SentenceList;
import com.openbravo.data.loader.SerializerWrite;
import com.openbravo.data.loader.SerializerWriteBasic;
import com.openbravo.format.Formats;
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.pos.forms.AppView;
import com.openbravo.pos.forms.DataLogicSales;
import com.openbravo.pos.reports.ReportEditorCreator;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.border.EmptyBorder;

/**
 *
 * @author JG uniCenta
 */
public class ProductFilter extends javax.swing.JPanel implements ReportEditorCreator {
    
    private SentenceList m_sentcat;
    private ComboBoxValModel m_CategoryModel;
    
//    private SentenceList m_sentsup;
//    private ComboBoxValModel m_SupplierModel;

    /** Creates new form JQBFProduct */
    public ProductFilter() {
        initComponents();
        styleComponents();
    }
    
    private void styleComponents() {
        setOpaque(false);
        setBorder(new EmptyBorder(5, 5, 5, 5));
        
        Font fontLabel = new Font("Segoe UI", Font.BOLD, 13);
        Font fontText = new Font("Segoe UI", Font.PLAIN, 14);
        Color colorLabel = new Color(70, 80, 90);

        if (jLabel1 != null) { jLabel1.setFont(fontLabel); jLabel1.setForeground(colorLabel); }
        if (jLabel2 != null) { jLabel2.setFont(fontLabel); jLabel2.setForeground(colorLabel); }
        if (jLabel3 != null) { jLabel3.setFont(fontLabel); jLabel3.setForeground(colorLabel); }
        if (jLabel4 != null) { jLabel4.setFont(fontLabel); jLabel4.setForeground(colorLabel); }
        if (jLabel5 != null) { jLabel5.setFont(fontLabel); jLabel5.setForeground(colorLabel); }

        m_jBarcode.setFont(fontText);
        m_jName.setFont(fontText);
        m_jPriceBuy.setFont(fontText);
        m_jPriceSell.setFont(fontText);
        
        m_jCategory.setFont(fontText);
        m_jCboName.setFont(fontText);
        m_jCboPriceBuy.setFont(fontText);
        m_jCboPriceSell.setFont(fontText);
        if (m_jFilterType != null) {
            m_jFilterType.setFont(fontText);
        }
        
        javax.swing.JTextField[] tfs = {m_jBarcode, m_jName, m_jPriceBuy, m_jPriceSell};
        for (javax.swing.JTextField tf : tfs) {
            tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(210, 220, 230)),
                BorderFactory.createEmptyBorder(2, 8, 2, 8)
            ));
        }
        
        jBtnReset.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    }
    
    /**
     *
     * @param app
     */
    @Override
    public void init(AppView app) {
         
        DataLogicSales dlSales = (DataLogicSales) app.getBean("com.openbravo.pos.forms.DataLogicSales");

        m_sentcat = dlSales.getCategoriesList();
        m_CategoryModel = new ComboBoxValModel();
         
        m_jCboName.setModel(ListQBFModelNumber.getMandatoryString());
        m_jCboPriceBuy.setModel(ListQBFModelNumber.getMandatoryNumber());
        m_jCboPriceSell.setModel(ListQBFModelNumber.getMandatoryNumber());
    }
    
    /**
     *
     * @throws BasicException
     */
    @Override
    public void activate() throws BasicException {

        List catlist = m_sentcat.list();
        catlist.add(0, null);
        m_CategoryModel = new ComboBoxValModel(catlist);
        m_jCategory.setModel(m_CategoryModel);
              
    }
    
    public void resetFilter() {
        jBtnResetActionPerformed(null);
    }
    
    /**
     *
     * @return
     */
    @Override
    public SerializerWrite getSerializerWrite() {
        return new SerializerWriteBasic(
            new Datas[] {
                Datas.OBJECT, Datas.STRING, 
                Datas.OBJECT, Datas.DOUBLE, 
                Datas.OBJECT, Datas.DOUBLE, 
                Datas.OBJECT, Datas.STRING, 
                Datas.OBJECT, Datas.STRING});
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
     * @return
     * @throws BasicException
     */
    @Override
    public Object createValue() throws BasicException {
        int index = m_jFilterType.getSelectedIndex();
        if (index == 1) { // Nombre
            return new Object[] {
                m_jCboName.getSelectedItem(), m_jName.getText(),
                QBFCompareEnum.COMP_NONE, null,
                QBFCompareEnum.COMP_NONE, null,
                QBFCompareEnum.COMP_NONE, null,
                QBFCompareEnum.COMP_NONE, null
            };
        } else if (index == 2) { // Código de Barras
            return new Object[] {
                QBFCompareEnum.COMP_NONE, null,
                QBFCompareEnum.COMP_NONE, null,
                QBFCompareEnum.COMP_NONE, null,
                QBFCompareEnum.COMP_NONE, null,
                QBFCompareEnum.COMP_EQUALS, m_jBarcode.getText()
            };
        } else if (index == 3) { // Categoría
            return new Object[] {
                QBFCompareEnum.COMP_NONE, null,
                QBFCompareEnum.COMP_NONE, null,
                QBFCompareEnum.COMP_NONE, null,
                m_CategoryModel.getSelectedKey() == null ? QBFCompareEnum.COMP_NONE : QBFCompareEnum.COMP_EQUALS, m_CategoryModel.getSelectedKey(),
                QBFCompareEnum.COMP_NONE, null
            };
        } else if (index == 4) { // Precio de Compra
            return new Object[] {
                QBFCompareEnum.COMP_NONE, null,
                m_jCboPriceBuy.getSelectedItem(), Formats.CURRENCY.parseValue(m_jPriceBuy.getText()),
                QBFCompareEnum.COMP_NONE, null,
                QBFCompareEnum.COMP_NONE, null,
                QBFCompareEnum.COMP_NONE, null
            };
        } else if (index == 5) { // Precio de Venta
            return new Object[] {
                QBFCompareEnum.COMP_NONE, null,
                QBFCompareEnum.COMP_NONE, null,
                m_jCboPriceSell.getSelectedItem(), Formats.CURRENCY.parseValue(m_jPriceSell.getText()),
                QBFCompareEnum.COMP_NONE, null,
                QBFCompareEnum.COMP_NONE, null
            };
        } else { // Cualquiera (no filtering)
            return new Object[] {
                QBFCompareEnum.COMP_NONE, null,
                QBFCompareEnum.COMP_NONE, null,
                QBFCompareEnum.COMP_NONE, null,
                QBFCompareEnum.COMP_NONE, null,
                QBFCompareEnum.COMP_NONE, null
            };
        }
    } 
 
    private void initComponents() {
        m_jFilterType = new javax.swing.JComboBox<>();
        m_jPanelInput = new javax.swing.JPanel();
        
        m_jBarcode = new javax.swing.JTextField();
        m_jCategory = new javax.swing.JComboBox();
        m_jCboName = new javax.swing.JComboBox();
        m_jCboPriceBuy = new javax.swing.JComboBox();
        m_jCboPriceSell = new javax.swing.JComboBox();
        m_jName = new javax.swing.JTextField();
        m_jPriceBuy = new javax.swing.JTextField();
        m_jPriceSell = new javax.swing.JTextField();
        jBtnReset = new javax.swing.JButton();
        
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();

        setMaximumSize(new java.awt.Dimension(450, 90));
        setMinimumSize(new java.awt.Dimension(450, 90));
        setPreferredSize(new java.awt.Dimension(450, 90));

        // Setup top panel
        javax.swing.JPanel panelTop = new javax.swing.JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 10, 5));
        panelTop.setOpaque(false);

        javax.swing.JLabel labelFilter = new javax.swing.JLabel("Buscar por:");
        labelFilter.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 13));
        labelFilter.setForeground(new java.awt.Color(70, 80, 90));
        panelTop.add(labelFilter);

        m_jFilterType.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] {
            "Cualquiera",
            "Nombre",
            "Código de Barras",
            "Categoría",
            "Precio de Compra",
            "Precio de Venta"
        }));
        m_jFilterType.setPreferredSize(new java.awt.Dimension(180, 30));
        panelTop.add(m_jFilterType);

        jBtnReset.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/reload.png"))); // NOI18N
        jBtnReset.setPreferredSize(new java.awt.Dimension(35, 30));
        jBtnReset.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnResetActionPerformed(evt);
            }
        });
        panelTop.add(jBtnReset);

        // Setup cards
        javax.swing.JPanel cardNone = new javax.swing.JPanel();
        cardNone.setOpaque(false);

        javax.swing.JPanel cardName = new javax.swing.JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 10, 5));
        cardName.setOpaque(false);
        m_jCboName.setPreferredSize(new java.awt.Dimension(150, 30));
        m_jName.setPreferredSize(new java.awt.Dimension(200, 30));
        cardName.add(m_jCboName);
        cardName.add(m_jName);

        javax.swing.JPanel cardBarcode = new javax.swing.JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 10, 5));
        cardBarcode.setOpaque(false);
        m_jBarcode.setPreferredSize(new java.awt.Dimension(200, 30));
        cardBarcode.add(m_jBarcode);

        javax.swing.JPanel cardCategory = new javax.swing.JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 10, 5));
        cardCategory.setOpaque(false);
        m_jCategory.setPreferredSize(new java.awt.Dimension(200, 30));
        cardCategory.add(m_jCategory);

        javax.swing.JPanel cardPriceBuy = new javax.swing.JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 10, 5));
        cardPriceBuy.setOpaque(false);
        m_jCboPriceBuy.setPreferredSize(new java.awt.Dimension(150, 30));
        m_jPriceBuy.setPreferredSize(new java.awt.Dimension(100, 30));
        cardPriceBuy.add(m_jCboPriceBuy);
        cardPriceBuy.add(m_jPriceBuy);

        javax.swing.JPanel cardPriceSell = new javax.swing.JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 10, 5));
        cardPriceSell.setOpaque(false);
        m_jCboPriceSell.setPreferredSize(new java.awt.Dimension(150, 30));
        m_jPriceSell.setPreferredSize(new java.awt.Dimension(100, 30));
        cardPriceSell.add(m_jCboPriceSell);
        cardPriceSell.add(m_jPriceSell);

        m_jPanelInput.setLayout(new java.awt.CardLayout());
        m_jPanelInput.setOpaque(false);
        m_jPanelInput.add(cardNone, "Cualquiera");
        m_jPanelInput.add(cardName, "Nombre");
        m_jPanelInput.add(cardBarcode, "Código de Barras");
        m_jPanelInput.add(cardCategory, "Categoría");
        m_jPanelInput.add(cardPriceBuy, "Precio de Compra");
        m_jPanelInput.add(cardPriceSell, "Precio de Venta");

        m_jFilterType.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                if (evt.getStateChange() == java.awt.event.ItemEvent.SELECTED) {
                    java.awt.CardLayout cl = (java.awt.CardLayout)(m_jPanelInput.getLayout());
                    cl.show(m_jPanelInput, (String)m_jFilterType.getSelectedItem());
                }
            }
        });

        setLayout(new java.awt.BorderLayout(10, 10));
        add(panelTop, java.awt.BorderLayout.NORTH);
        add(m_jPanelInput, java.awt.BorderLayout.CENTER);
    }

    private void jBtnResetActionPerformed(java.awt.event.ActionEvent evt) {
        m_jFilterType.setSelectedIndex(0);
        m_jBarcode.setText(null);
        if (m_jCategory.getItemCount() > 0) {
            m_jCategory.setSelectedIndex(0);
        }
        if (m_jCboName.getItemCount() > 0) {
            m_jCboName.setSelectedIndex(0);
        }
        if (m_jCboPriceBuy.getItemCount() > 0) {
            m_jCboPriceBuy.setSelectedIndex(0);
        }
        if (m_jCboPriceSell.getItemCount() > 0) {
            m_jCboPriceSell.setSelectedIndex(0);
        }
        m_jName.setText(null);
        m_jPriceBuy.setText(null);
        m_jPriceSell.setText(null);
    }

    private javax.swing.JButton jBtnReset;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JTextField m_jBarcode;
    private javax.swing.JComboBox m_jCategory;
    private javax.swing.JComboBox m_jCboName;
    private javax.swing.JComboBox m_jCboPriceBuy;
    private javax.swing.JComboBox m_jCboPriceSell;
    private javax.swing.JTextField m_jName;
    private javax.swing.JTextField m_jPriceBuy;
    private javax.swing.JTextField m_jPriceSell;
    private javax.swing.JComboBox<String> m_jFilterType;
    private javax.swing.JPanel m_jPanelInput;
}

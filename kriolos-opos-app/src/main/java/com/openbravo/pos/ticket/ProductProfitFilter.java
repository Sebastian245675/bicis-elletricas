package com.openbravo.pos.ticket;

import com.openbravo.basic.BasicException;
import com.openbravo.data.gui.ComboBoxValModel;
import com.openbravo.data.loader.Datas;
import com.openbravo.data.loader.QBFCompareEnum;
import com.openbravo.data.loader.SentenceList;
import com.openbravo.data.loader.SerializerWrite;
import com.openbravo.data.loader.SerializerWriteBasic;
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.pos.forms.AppView;
import com.openbravo.pos.forms.DataLogicSales;
import com.openbravo.pos.reports.ReportEditorCreator;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

public class ProductProfitFilter extends JPanel implements ReportEditorCreator {
    
    private SentenceList m_sentcat;
    private ComboBoxValModel m_CategoryModel;
    
    private JTextField txtProduct;
    private JComboBox m_jCategory;
    private JButton jBtnReset;

    public ProductProfitFilter() {
        initComponents();
    }
    
    private void initComponents() {
        setOpaque(false);
        setLayout(new FlowLayout(FlowLayout.LEFT, 15, 10));
        setBorder(new EmptyBorder(10, 10, 10, 10));

        Font fontLabel = new Font("Segoe UI", Font.BOLD, 13);
        Font fontText = new Font("Segoe UI", Font.PLAIN, 13);
        Color colorLabel = new Color(55, 65, 81);
        Color colorBorder = new Color(209, 213, 219);

        // Product search
        JLabel lblProduct = new JLabel("Producto (Nombre o Código):");
        lblProduct.setFont(fontLabel);
        lblProduct.setForeground(colorLabel);
        add(lblProduct);

        txtProduct = new JTextField();
        txtProduct.setFont(fontText);
        txtProduct.setPreferredSize(new Dimension(200, 32));
        txtProduct.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(colorBorder, 1, true),
            BorderFactory.createEmptyBorder(2, 8, 2, 8)
        ));
        add(txtProduct);

        // Category / Department
        JLabel lblCategory = new JLabel("Departamento:");
        lblCategory.setFont(fontLabel);
        lblCategory.setForeground(colorLabel);
        add(lblCategory);

        m_jCategory = new JComboBox();
        m_jCategory.setFont(fontText);
        m_jCategory.setPreferredSize(new Dimension(180, 32));
        m_jCategory.setBackground(Color.WHITE);
        add(m_jCategory);

        // Reset button
        jBtnReset = new JButton("Limpiar");
        jBtnReset.setFont(fontLabel);
        jBtnReset.setFocusPainted(false);
        jBtnReset.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jBtnReset.setPreferredSize(new Dimension(90, 32));
        jBtnReset.addActionListener(e -> resetFilters());
        add(jBtnReset);
    }

    private void resetFilters() {
        txtProduct.setText(null);
        if (m_jCategory.getItemCount() > 0) {
            m_jCategory.setSelectedIndex(0);
        }
    }

    @Override
    public void init(AppView app) {
        DataLogicSales dlSales = (DataLogicSales) app.getBean("com.openbravo.pos.forms.DataLogicSales");
        m_sentcat = dlSales.getCategoriesList();
        m_CategoryModel = new ComboBoxValModel();
    }

    @Override
    public void activate() throws BasicException {
        List catlist = m_sentcat.list();
        catlist.add(0, null);
        m_CategoryModel = new ComboBoxValModel(catlist);
        m_jCategory.setModel(m_CategoryModel);
    }

    @Override
    public SerializerWrite getSerializerWrite() {
        return new SerializerWriteBasic(
                new Datas[] {
                    Datas.OBJECT, Datas.STRING,  // products.NAME
                    Datas.OBJECT, Datas.STRING,  // products.CATEGORY
                    Datas.OBJECT, Datas.STRING,  // products.CODE
                    Datas.OBJECT, Datas.STRING   // stockcurrent.UNITS
                });
    }

    @Override
    public Component getComponent() {
        return this;
    }

    @Override
    public Object createValue() throws BasicException {
        String search = txtProduct.getText().trim();
        Object categoryKey = m_CategoryModel.getSelectedKey();
        
        QBFCompareEnum catCompare = (categoryKey == null) ? QBFCompareEnum.COMP_NONE : QBFCompareEnum.COMP_EQUALS;

        if (search.isEmpty()) {
            // Global or just by Category
            return new Object[] {
                QBFCompareEnum.COMP_NONE, null,
                catCompare, categoryKey,
                QBFCompareEnum.COMP_NONE, null,
                QBFCompareEnum.COMP_NONE, null
            };
        } else {
            // Check if it looks like a barcode or reference code (purely numeric or short identifier)
            boolean isCode = search.matches("\\d+");
            if (isCode) {
                return new Object[] {
                    QBFCompareEnum.COMP_NONE, null,
                    catCompare, categoryKey,
                    QBFCompareEnum.COMP_EQUALS, search,
                    QBFCompareEnum.COMP_NONE, null
                };
            } else {
                return new Object[] {
                    QBFCompareEnum.COMP_RE, "%" + search + "%",
                    catCompare, categoryKey,
                    QBFCompareEnum.COMP_NONE, null,
                    QBFCompareEnum.COMP_NONE, null
                };
            }
        }
    }
}

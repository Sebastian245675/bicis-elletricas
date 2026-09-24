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

package com.openbravo.pos.inventory;

import com.openbravo.pos.util.ModernActionIcon;

import com.openbravo.basic.BasicException;
import com.openbravo.data.gui.ComboBoxValModel;
import com.openbravo.data.gui.JMessageDialog;
import com.openbravo.data.gui.MessageInf;
import com.openbravo.data.loader.SentenceExec;
import com.openbravo.data.loader.SentenceList;
import com.openbravo.data.user.DirtyManager;
import com.openbravo.data.user.EditorRecord;
import com.openbravo.format.Formats;
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.pos.forms.AppView;
import com.openbravo.pos.forms.DataLogicSales;
import java.awt.Component;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.swing.JPanel;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.table.AbstractTableModel;
import com.openbravo.pos.catalog.CategoryStock;
import java.awt.Font;
import javax.swing.table.JTableHeader;

/**
 *
 * @author adrianromero
 */
public final class CategoriesEditor extends JPanel implements EditorRecord {
    
    private String m_id;
    
    private final SentenceList m_sentcat;
    private ComboBoxValModel m_CategoryModel;
    
    private final SentenceExec m_sentadd;
    private final SentenceExec m_sentdel;
    
    private List<CategoryStock> categoryStockList;
    private CategoriesEditor.StockTableModel stockModel;
    
    private final DataLogicSales dlSales;
        
    /** Creates new form JPanelCategories
     * @param app
     * @param dirty */
    public CategoriesEditor(AppView app, DirtyManager dirty) {
        
//        DataLogicSales dlSales = (DataLogicSales) app.getBean("com.openbravo.pos.forms.DataLogicSales");
        dlSales = (DataLogicSales) app.getBean("com.openbravo.pos.forms.DataLogicSales");
        
        initComponents();
             
        // El modelo de categorias
        m_sentcat = dlSales.getCategoriesList();
        m_CategoryModel = new ComboBoxValModel();
        
        m_sentadd = dlSales.getCatalogCategoryAdd();
        m_sentdel = dlSales.getCatalogCategoryDel();
        
        m_jName.getDocument().addDocumentListener(dirty);
        m_jCategory.addActionListener(dirty);
        m_jImage.addPropertyChangeListener("image", dirty);
        m_jCatNameShow.addActionListener(dirty);
        m_jTextTip.getDocument().addDocumentListener(dirty); 
        m_jCatOrder.getDocument().addDocumentListener(dirty);
        webSwtch_InCatalog.addActionListener(dirty);
     
        writeValueEOF();
    }
    
    /**
     *
     */
    @Override
    public void refresh() {
        
        List a;
        
        try {
            a = m_sentcat.list();
        } catch (BasicException eD) {
            MessageInf msg = new MessageInf(MessageInf.SGN_NOTICE, AppLocal.getIntString("message.cannotloadlists"), eD);
            msg.show(this);
            a = new ArrayList();
        }
        
        a.add(0, null);
        m_CategoryModel = new ComboBoxValModel(a);
        m_jCategory.setModel(m_CategoryModel);
        
        jLblProdCount.setText(null);        
    }
    
    /**
     *
     */
    @Override
    public void writeValueEOF() {
        m_id = null;
        m_jName.setText(null);
        jLblDepartamentoTitle.setText("Seleccione un Departamento"); // Limpiar título
        m_CategoryModel.setSelectedKey(null);
        m_jImage.setImage(null);
        m_jName.setEnabled(false);
        m_jCategory.setEnabled(false);
        m_jImage.setEnabled(false);
        webSwtch_InCatalog.isSelected();
        m_jTextTip.setText(null);        
        m_jTextTip.setEnabled(false);
        m_jCatNameShow.setSelected(false);
        m_jCatNameShow.setEnabled(false);
// Added JG Feb 2017
        m_jCatOrder.setText(null); 
        m_jCatOrder.setEnabled(false);

    }
    
    /**
     *
     */
    @Override
    public void writeValueInsert() {
        m_id = UUID.randomUUID().toString();
        m_jName.setText(null);
        jLblDepartamentoTitle.setText("Nuevo Departamento"); // Limpiar título al insertar
        m_CategoryModel.setSelectedKey(null);
        m_jImage.setImage(null);
        m_jName.setEnabled(true);
        m_jCategory.setEnabled(true);
        m_jImage.setEnabled(true);
        webSwtch_InCatalog.setEnabled(false);
        m_jTextTip.setText(null);
        m_jTextTip.setEnabled(true);   
        m_jCatNameShow.setSelected(true);
        m_jCatNameShow.setEnabled(true);
// Added JG Feb 2017
        m_jCatOrder.setText(null); 
        m_jCatOrder.setEnabled(true); 

    }

    /**
     *
     * @param value
     */
    @Override
    public void writeValueDelete(Object value) {
        Object[] cat = (Object[]) value;
        m_id = (String) cat[0];
        String name = Formats.STRING.formatValue((String)cat[1]);
        m_jName.setText(name);
        jLblDepartamentoTitle.setText(name != null && !name.isEmpty() ? name : "Eliminar Departamento");
        m_CategoryModel.setSelectedKey(cat[2]);
        m_jImage.setImage((BufferedImage) cat[3]);
        m_jTextTip.setText(Formats.STRING.formatValue((String)cat[4]));
        m_jCatNameShow.setSelected(((Boolean)cat[5]));
        m_jCatOrder.setText(Formats.STRING.formatValue((String)cat[6]));
        
        m_jName.setEnabled(false);
        m_jCategory.setEnabled(false);
        m_jImage.setEnabled(false);
        webSwtch_InCatalog.setEnabled(false);
        m_jTextTip.setEnabled(false);     
        m_jCatNameShow.setEnabled(false);
        m_jCatOrder.setEnabled(false);   
        
        stockModel = new CategoriesEditor.StockTableModel(getProductOfName(m_id));      
        
    }

    /**
     *
     * @param value
     */
    @Override
    public void writeValueEdit(Object value) {
        Object[] cat = (Object[]) value;
        m_id = (String) cat[0];
        String name = Formats.STRING.formatValue((String)cat[1]);
        m_jName.setText(name);
        
        // Actualizar título del departamento
        jLblDepartamentoTitle.setText(name != null && !name.isEmpty() ? name : "Editar Departamento");
        
        m_CategoryModel.setSelectedKey(cat[2]);
        m_jImage.setImage((BufferedImage) cat[3]);
        m_jTextTip.setText(Formats.STRING.formatValue((String)cat[4])); 
        m_jCatNameShow.setSelected(((Boolean)cat[5]));
        m_jCatOrder.setText(Formats.STRING.formatValue((String)cat [6]));

        if(m_jCatOrder.getText().length() == 0) {    
            m_jCatOrder.setText(null);        
        }

        m_jName.setEnabled(true);
        m_jCategory.setEnabled(true);
        m_jImage.setEnabled(true);
        webSwtch_InCatalog.setEnabled(true);
        m_jTextTip.setEnabled(true); 
        m_jCatNameShow.setEnabled(true);
// Added JG Feb 2017
       m_jCatOrder.setEnabled(true);           

        resetTranxTable();   
    }

    /**
     *
     * @return
     * @throws BasicException
     */
    @Override
    public Object createValue() throws BasicException {
        
        Object[] cat = new Object[8];

        cat[0] = m_id;
        cat[1] = m_jName.getText();
        cat[2] = m_CategoryModel.getSelectedKey();
        cat[3] = m_jImage.getImage();
        cat[4] = m_jTextTip.getText();
        cat[5] = m_jCatNameShow.isSelected();
        if(m_jCatOrder.getText().length() == 0) {    
            m_jCatOrder.setText(null);        
        }        
        cat[6] = m_jCatOrder.getText();
        
        return cat;
    }

    /**
     *
     * @return
     */
    @Override
    public Component getComponent() {
        return this;
    }
    
public void resetTranxTable() {

    jTableCategoryStock.getColumnModel().getColumn(0).setPreferredWidth(250);
    
    // set font for headers
    Font f = new Font("Arial", Font.BOLD, 14);
    JTableHeader header = jTableCategoryStock.getTableHeader();
    header.setFont(f);
      
    jTableCategoryStock.getTableHeader().setReorderingAllowed(true); 
    jTableCategoryStock.setAutoCreateRowSorter(true);    
    jTableCategoryStock.repaint();    
}

    private List<CategoryStock> getProductOfName(String pId) {

        try {
            categoryStockList = dlSales.getCategorysProductList(pId);

        } catch (BasicException ex) {
            Logger.getLogger(CategoriesEditor.class.getName()).log(Level.SEVERE, null, ex);
        }

        List<CategoryStock> categoryList = new ArrayList<>();

        for (CategoryStock categoryStock : categoryStockList) {
            String categoryId = categoryStock.getCategoryId();
            if (categoryId.equals(pId)) {
                categoryList.add(categoryStock);
            }
        }
        
        repaint();
        refresh();

        return categoryList;
    }

    class StockTableModel extends AbstractTableModel {
        String nam = AppLocal.getIntString("label.prodname");
        String cod = AppLocal.getIntString("label.prodbarcode");

        List<CategoryStock> stockList;
        String[] columnNames = {nam, cod};

        public StockTableModel(List<CategoryStock> list) {
            stockList = list;
        }

        @Override
        public int getColumnCount() {
            return 2;
        }

        @Override
        public int getRowCount() {
            return stockList.size();
        }

        @Override
        public Object getValueAt(int row, int column) {
            CategoryStock categoryStock = stockList.get(row);
        
            switch (column) {
                case 0:
                    return categoryStock.getProductName();                                        
                case 1:
                    return categoryStock.getProductCode();
                case 2:
                    return categoryStock.getProductId();
                default:
                    return "";
            }
        }

        @Override
        public String getColumnName(int col) {
            return columnNames[col];
        }
    } 
    
    
    public void Notify(String msg){
           
    } 
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jInternalFrame1 = new javax.swing.JInternalFrame();
        jLblName = new javax.swing.JLabel();
        m_jName = new javax.swing.JTextField();
        jLblCategory = new javax.swing.JLabel();
        m_jCategory = new javax.swing.JComboBox();
        jLblTextTip = new javax.swing.JLabel();
        m_jTextTip = new javax.swing.JTextField();
        jLblCatShowName = new javax.swing.JLabel();
        m_jCatNameShow = new javax.swing.JCheckBox();
        jLblCatOrder = new javax.swing.JLabel();
        m_jCatOrder = new javax.swing.JTextField();
        jLblInCat = new javax.swing.JLabel();
        webSwtch_InCatalog = new javax.swing.JCheckBox();
        m_jImage = new com.openbravo.data.gui.JImageEditor();
        jBtnShowTrans = new javax.swing.JButton();
        jLblProdCount = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTableCategoryStock = new javax.swing.JTable();

        // Título del departamento seleccionado
        jLblDepartamentoTitle = new javax.swing.JLabel();

        jInternalFrame1.setVisible(true);

        setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        setPreferredSize(new java.awt.Dimension(500, 200));
        setLayout(new java.awt.BorderLayout());
        
        // Título del departamento en amarillo
        jLblDepartamentoTitle.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 18));
        jLblDepartamentoTitle.setForeground(new java.awt.Color(255, 200, 0)); // Amarillo
        jLblDepartamentoTitle.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));

        jLblName.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLblName.setIcon(new ModernActionIcon(ModernActionIcon.Type.INFO, 18));
        jLblName.setText(AppLocal.getIntString("label.namem")); // NOI18N
        jLblName.setPreferredSize(new java.awt.Dimension(125, 30));
        jLblName.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLblNameMouseClicked(evt);
            }
        });

        m_jName.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        m_jName.setPreferredSize(new java.awt.Dimension(250, 30));

        jLblCategory.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLblCategory.setText(AppLocal.getIntString("label.prodcategory")); // NOI18N
        jLblCategory.setPreferredSize(new java.awt.Dimension(125, 30));

        m_jCategory.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        m_jCategory.setPreferredSize(new java.awt.Dimension(250, 30));

        jLblTextTip.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("pos_messages"); // NOI18N
        jLblTextTip.setText(bundle.getString("label.texttip")); // NOI18N
        jLblTextTip.setPreferredSize(new java.awt.Dimension(125, 30));

        m_jTextTip.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        m_jTextTip.setPreferredSize(new java.awt.Dimension(250, 30));

        jLblCatShowName.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLblCatShowName.setText(bundle.getString("label.subcategorytitle")); // NOI18N
        jLblCatShowName.setPreferredSize(new java.awt.Dimension(150, 30));

        m_jCatNameShow.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        m_jCatNameShow.setSelected(true);
        m_jCatNameShow.setPreferredSize(new java.awt.Dimension(30, 30));

        jLblCatOrder.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLblCatOrder.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLblCatOrder.setText(bundle.getString("label.ccatorder")); // NOI18N
        jLblCatOrder.setPreferredSize(new java.awt.Dimension(60, 30));

        m_jCatOrder.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        m_jCatOrder.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        m_jCatOrder.setPreferredSize(new java.awt.Dimension(60, 30));

        jLblInCat.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLblInCat.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLblInCat.setText(bundle.getString("label.CatalogueStatusYes")); // NOI18N
        jLblInCat.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        jLblInCat.setPreferredSize(new java.awt.Dimension(125, 30));

        webSwtch_InCatalog.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        webSwtch_InCatalog.setPreferredSize(new java.awt.Dimension(80, 30));
        webSwtch_InCatalog.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                webSwtch_InCatalogActionPerformed(evt);
            }
        });

        jBtnShowTrans.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jBtnShowTrans.setText(bundle.getString("button.CatProds")); // NOI18N
        jBtnShowTrans.setToolTipText("");
        jBtnShowTrans.setPreferredSize(new java.awt.Dimension(140, 30));
        jBtnShowTrans.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnShowTransActionPerformed(evt);
            }
        });

        jLblProdCount.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLblProdCount.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLblProdCount.setOpaque(true);
        jLblProdCount.setPreferredSize(new java.awt.Dimension(237, 30));

        jScrollPane2.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jScrollPane2.setPreferredSize(new java.awt.Dimension(340, 502));

        jTableCategoryStock.setAutoCreateRowSorter(true);
        jTableCategoryStock.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null}
            },
            new String [] {
                "Name", "Barcode"
            }
        ));
        jTableCategoryStock.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jTableCategoryStock.setGridColor(new java.awt.Color(102, 204, 255));
        jTableCategoryStock.setRowHeight(25);
        jTableCategoryStock.setShowVerticalLines(false);
        jScrollPane2.setViewportView(jTableCategoryStock);
        if (jTableCategoryStock.getColumnModel().getColumnCount() > 0) {
            jTableCategoryStock.getColumnModel().getColumn(0).setPreferredWidth(250);
        }

        // Modern Premium Layout - Full responsive side-by-side cards over Crema Background
        setBackground(new java.awt.Color(250, 247, 242)); // Soft Crema background
        setLayout(new java.awt.BorderLayout());

        // Card Container (holds left and right cards side-by-side)
        javax.swing.JPanel cardsContainer = new javax.swing.JPanel(new java.awt.GridBagLayout());
        cardsContainer.setOpaque(false);
        cardsContainer.setBorder(javax.swing.BorderFactory.createEmptyBorder(16, 16, 16, 16));

        java.awt.GridBagConstraints mainGbc = new java.awt.GridBagConstraints();
        javax.swing.border.Border lineBorder = javax.swing.BorderFactory.createLineBorder(new java.awt.Color(226, 232, 240), 1);
        javax.swing.border.Border paddingBorder = javax.swing.BorderFactory.createEmptyBorder(20, 24, 20, 24);

        // 1. LEFT CARD (Details)
        javax.swing.JPanel leftCard = new javax.swing.JPanel(new java.awt.GridBagLayout());
        leftCard.setBackground(java.awt.Color.WHITE);
        leftCard.setBorder(javax.swing.BorderFactory.createCompoundBorder(lineBorder, paddingBorder));
        leftCard.setMinimumSize(new java.awt.Dimension(350, 300));

        java.awt.GridBagConstraints leftGbc = new java.awt.GridBagConstraints();
        leftGbc.fill = java.awt.GridBagConstraints.HORIZONTAL;
        leftGbc.anchor = java.awt.GridBagConstraints.NORTHWEST;

        // Left Card Header
        javax.swing.JPanel headerPanel = new javax.swing.JPanel(new java.awt.BorderLayout());
        headerPanel.setOpaque(false);
        
        javax.swing.JPanel accentBar = new javax.swing.JPanel();
        accentBar.setBackground(new java.awt.Color(202, 159, 65)); // gold accent
        accentBar.setPreferredSize(new java.awt.Dimension(4, 0));
        headerPanel.add(accentBar, java.awt.BorderLayout.WEST);
        
        javax.swing.JPanel textPanel = new javax.swing.JPanel(new java.awt.GridLayout(2, 1, 2, 2));
        textPanel.setOpaque(false);
        textPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 8, 0, 0));
        
        jLblDepartamentoTitle.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 15));
        jLblDepartamentoTitle.setForeground(new java.awt.Color(15, 23, 42)); // Slate 900
        textPanel.add(jLblDepartamentoTitle);
        
        javax.swing.JLabel subtitleLabel = new javax.swing.JLabel("Gestione los detalles del departamento o categoría.");
        subtitleLabel.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 11));
        subtitleLabel.setForeground(new java.awt.Color(100, 116, 139)); // Slate 500
        textPanel.add(subtitleLabel);
        
        headerPanel.add(textPanel, java.awt.BorderLayout.CENTER);
        
        leftGbc.gridx = 0;
        leftGbc.gridy = 0;
        leftGbc.gridwidth = 2;
        leftGbc.weightx = 1.0;
        leftGbc.weighty = 0.0;
        leftGbc.insets = new java.awt.Insets(0, 0, 16, 0);
        leftCard.add(headerPanel, leftGbc);

        // Separator
        javax.swing.JSeparator sep = new javax.swing.JSeparator();
        sep.setForeground(new java.awt.Color(241, 245, 249));
        sep.setBackground(new java.awt.Color(241, 245, 249));
        leftGbc.gridy = 1;
        leftGbc.insets = new java.awt.Insets(0, 0, 16, 0);
        leftCard.add(sep, leftGbc);

        // Form Fields
        leftGbc.gridwidth = 1;
        leftGbc.weightx = 0.0;
        leftGbc.fill = java.awt.GridBagConstraints.NONE;

        // Nombre Field
        jLblName.setText("Nombre:");
        jLblName.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12));
        jLblName.setForeground(new java.awt.Color(71, 85, 105));
        jLblName.setIcon(null);
        jLblName.setPreferredSize(new java.awt.Dimension(110, 32));
        leftGbc.gridx = 0;
        leftGbc.gridy = 2;
        leftGbc.insets = new java.awt.Insets(0, 0, 8, 8);
        leftCard.add(jLblName, leftGbc);

        m_jName.setPreferredSize(new java.awt.Dimension(200, 32));
        m_jName.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 13));
        m_jName.setBorder(javax.swing.BorderFactory.createCompoundBorder(
            javax.swing.BorderFactory.createLineBorder(new java.awt.Color(226, 232, 240), 1),
            javax.swing.BorderFactory.createEmptyBorder(4, 8, 4, 8)
        ));
        leftGbc.gridx = 1;
        leftGbc.weightx = 1.0;
        leftGbc.fill = java.awt.GridBagConstraints.HORIZONTAL;
        leftCard.add(m_jName, leftGbc);

        // Categoría Padre Field
        jLblCategory.setText("Categoría Padre:");
        jLblCategory.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12));
        jLblCategory.setForeground(new java.awt.Color(71, 85, 105));
        jLblCategory.setPreferredSize(new java.awt.Dimension(110, 32));
        leftGbc.gridx = 0;
        leftGbc.gridy = 3;
        leftGbc.weightx = 0.0;
        leftGbc.fill = java.awt.GridBagConstraints.NONE;
        leftCard.add(jLblCategory, leftGbc);

        m_jCategory.setPreferredSize(new java.awt.Dimension(200, 32));
        m_jCategory.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 13));
        leftGbc.gridx = 1;
        leftGbc.weightx = 1.0;
        leftGbc.fill = java.awt.GridBagConstraints.HORIZONTAL;
        leftCard.add(m_jCategory, leftGbc);

        // Orden de Catálogo Field
        jLblCatOrder.setText("Orden catálogo:");
        jLblCatOrder.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12));
        jLblCatOrder.setForeground(new java.awt.Color(71, 85, 105));
        jLblCatOrder.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLblCatOrder.setPreferredSize(new java.awt.Dimension(110, 32));
        leftGbc.gridx = 0;
        leftGbc.gridy = 4;
        leftGbc.weightx = 0.0;
        leftGbc.fill = java.awt.GridBagConstraints.NONE;
        leftCard.add(jLblCatOrder, leftGbc);

        m_jCatOrder.setPreferredSize(new java.awt.Dimension(200, 32));
        m_jCatOrder.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 13));
        m_jCatOrder.setBorder(javax.swing.BorderFactory.createCompoundBorder(
            javax.swing.BorderFactory.createLineBorder(new java.awt.Color(226, 232, 240), 1),
            javax.swing.BorderFactory.createEmptyBorder(4, 8, 4, 8)
        ));
        leftGbc.gridx = 1;
        leftGbc.weightx = 1.0;
        leftGbc.fill = java.awt.GridBagConstraints.HORIZONTAL;
        leftCard.add(m_jCatOrder, leftGbc);

        // Notas / Ayuda Field
        jLblTextTip.setText("Notas / Ayuda:");
        jLblTextTip.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12));
        jLblTextTip.setForeground(new java.awt.Color(71, 85, 105));
        jLblTextTip.setPreferredSize(new java.awt.Dimension(110, 32));
        leftGbc.gridx = 0;
        leftGbc.gridy = 5;
        leftGbc.weightx = 0.0;
        leftGbc.fill = java.awt.GridBagConstraints.NONE;
        leftCard.add(jLblTextTip, leftGbc);

        m_jTextTip.setPreferredSize(new java.awt.Dimension(200, 32));
        m_jTextTip.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 13));
        m_jTextTip.setBorder(javax.swing.BorderFactory.createCompoundBorder(
            javax.swing.BorderFactory.createLineBorder(new java.awt.Color(226, 232, 240), 1),
            javax.swing.BorderFactory.createEmptyBorder(4, 8, 4, 8)
        ));
        leftGbc.gridx = 1;
        leftGbc.weightx = 1.0;
        leftGbc.fill = java.awt.GridBagConstraints.HORIZONTAL;
        leftCard.add(m_jTextTip, leftGbc);

        // Checkboxes Row
        javax.swing.JPanel checkPanel = new javax.swing.JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 16, 0));
        checkPanel.setOpaque(false);
        
        m_jCatNameShow.setText("Mostrar Nombre");
        m_jCatNameShow.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 12));
        m_jCatNameShow.setForeground(new java.awt.Color(71, 85, 105));
        m_jCatNameShow.setOpaque(false);
        m_jCatNameShow.setPreferredSize(new java.awt.Dimension(140, 30));
        checkPanel.add(m_jCatNameShow);

        webSwtch_InCatalog.setText("En Catálogo");
        webSwtch_InCatalog.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 12));
        webSwtch_InCatalog.setForeground(new java.awt.Color(71, 85, 105));
        webSwtch_InCatalog.setOpaque(false);
        webSwtch_InCatalog.setPreferredSize(new java.awt.Dimension(140, 30));
        checkPanel.add(webSwtch_InCatalog);

        leftGbc.gridx = 0;
        leftGbc.gridy = 6;
        leftGbc.gridwidth = 2;
        leftGbc.weightx = 1.0;
        leftGbc.weighty = 0.0;
        leftGbc.fill = java.awt.GridBagConstraints.HORIZONTAL;
        leftGbc.insets = new java.awt.Insets(8, 0, 0, 0);
        leftCard.add(checkPanel, leftGbc);

        // Spacer at the bottom to absorb extra space
        javax.swing.JPanel spacer = new javax.swing.JPanel();
        spacer.setOpaque(false);
        leftGbc.gridy = 7;
        leftGbc.weighty = 1.0;
        leftGbc.fill = java.awt.GridBagConstraints.BOTH;
        leftCard.add(spacer, leftGbc);


        // 2. RIGHT CARD (Image)
        javax.swing.JPanel rightCard = new javax.swing.JPanel(new java.awt.BorderLayout(0, 12));
        rightCard.setBackground(java.awt.Color.WHITE);
        rightCard.setBorder(javax.swing.BorderFactory.createCompoundBorder(lineBorder, paddingBorder));
        rightCard.setMinimumSize(new java.awt.Dimension(250, 300));

        // Right Card Header
        javax.swing.JPanel rightHeader = new javax.swing.JPanel(new java.awt.BorderLayout());
        rightHeader.setOpaque(false);
        
        javax.swing.JPanel rightAccent = new javax.swing.JPanel();
        rightAccent.setBackground(new java.awt.Color(202, 159, 65));
        rightAccent.setPreferredSize(new java.awt.Dimension(4, 0));
        rightHeader.add(rightAccent, java.awt.BorderLayout.WEST);
        
        javax.swing.JPanel rightHeaderText = new javax.swing.JPanel(new java.awt.GridLayout(2, 1, 2, 2));
        rightHeaderText.setOpaque(false);
        rightHeaderText.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 8, 0, 0));
        
        javax.swing.JLabel imgTitle = new javax.swing.JLabel("Imagen");
        imgTitle.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 15));
        imgTitle.setForeground(new java.awt.Color(15, 23, 42));
        rightHeaderText.add(imgTitle);
        
        javax.swing.JLabel imgSubtitle = new javax.swing.JLabel("Cargue una foto para el catálogo.");
        imgSubtitle.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 11));
        imgSubtitle.setForeground(new java.awt.Color(100, 116, 139));
        rightHeaderText.add(imgSubtitle);
        
        rightHeader.add(rightHeaderText, java.awt.BorderLayout.CENTER);
        rightCard.add(rightHeader, java.awt.BorderLayout.NORTH);

        // Right Card Center: Image Editor
        m_jImage.setOpaque(false);
        rightCard.add(m_jImage, java.awt.BorderLayout.CENTER);


        // Add cards side-by-side to Container
        mainGbc.gridy = 0;
        mainGbc.weighty = 1.0;
        mainGbc.fill = java.awt.GridBagConstraints.BOTH;

        mainGbc.gridx = 0;
        mainGbc.weightx = 0.6; // Details card gets 60% of horizontal space
        mainGbc.insets = new java.awt.Insets(0, 0, 0, 16);
        cardsContainer.add(leftCard, mainGbc);

        mainGbc.gridx = 1;
        mainGbc.weightx = 0.4; // Image card gets 40% of horizontal space
        mainGbc.insets = new java.awt.Insets(0, 0, 0, 0);
        cardsContainer.add(rightCard, mainGbc);

        // Add Container to Editor
        add(cardsContainer, java.awt.BorderLayout.CENTER);

        // Make sure all these fields are visible!
        jLblCategory.setVisible(true);
        m_jCategory.setVisible(true);
        jLblTextTip.setVisible(true);
        m_jTextTip.setVisible(true);
        jLblCatShowName.setVisible(false); // we put text directly in checkbox
        m_jCatNameShow.setVisible(true);
        jLblCatOrder.setVisible(true);
        m_jCatOrder.setVisible(true);
        jLblInCat.setVisible(false); // we put text directly in checkbox
        webSwtch_InCatalog.setVisible(true);
        m_jImage.setVisible(true);

        // Unused fields stay hidden
        jBtnShowTrans.setVisible(false);
        jLblProdCount.setVisible(false);
        jScrollPane2.setVisible(false);
        jInternalFrame1.setVisible(false);
    }// </editor-fold>//GEN-END:initComponents

    private void webSwtch_InCatalogActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_webSwtch_InCatalogActionPerformed
        if (webSwtch_InCatalog.isSelected()) {

            try {
                Object param = m_id;
                m_sentdel.exec(param);
                m_sentadd.exec(param);
                jLblInCat.setText(AppLocal.getIntString("label.CatalogueStatusYes"));
                //Notify(AppLocal.getIntString("notify.added"));                                                    
            } catch (BasicException e) {
                JMessageDialog.showMessage(this, new MessageInf(MessageInf.SGN_WARNING, AppLocal.getIntString("message.cannotexecute"), e));
            }            
        } else {

            try {
                m_sentdel.exec(m_id);
                jLblInCat.setText(AppLocal.getIntString("label.CatalogueStatusNo"));
                //Notify(AppLocal.getIntString("notify.removed"));                                                                    
            } catch (BasicException e) {
                JMessageDialog.showMessage(this, new MessageInf(MessageInf.SGN_WARNING, AppLocal.getIntString("message.cannotexecute"), e));
            }            
        }
    }//GEN-LAST:event_webSwtch_InCatalogActionPerformed

    private void jLblNameMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLblNameMouseClicked

        if (evt.getClickCount() == 2) {
            String uuidString = m_id.toString();
            StringSelection stringSelection = new StringSelection(uuidString);
            Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
            clpbrd.setContents(stringSelection, null);
        
            JOptionPane.showMessageDialog(null, 
                AppLocal.getIntString("message.uuidcopy"));
        }
    }//GEN-LAST:event_jLblNameMouseClicked

    private void jBtnShowTransActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnShowTransActionPerformed
        String pId = m_id.toString();
        if (pId != null) {
            stockModel = new CategoriesEditor.StockTableModel(getProductOfName(pId));
            jTableCategoryStock.setModel(stockModel);

            if (stockModel.getRowCount()> 0){
                jTableCategoryStock.setVisible(true);
                String ProdCount = String.valueOf(stockModel.getRowCount());
                jLblProdCount.setText(ProdCount + " for " + m_jName.getText());                
            }else{
                jTableCategoryStock.setVisible(false);
                JOptionPane.showMessageDialog(null,
                    "No Products for this Category", "Products", JOptionPane.INFORMATION_MESSAGE);
            }
            resetTranxTable();
        }
    }//GEN-LAST:event_jBtnShowTransActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jBtnShowTrans;
    private javax.swing.JInternalFrame jInternalFrame1;
    private javax.swing.JLabel jLblCatOrder;
    private javax.swing.JLabel jLblCatShowName;
    private javax.swing.JLabel jLblCategory;
    private javax.swing.JLabel jLblInCat;
    private javax.swing.JLabel jLblName;
    private javax.swing.JLabel jLblProdCount;
    private javax.swing.JLabel jLblTextTip;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTable jTableCategoryStock;
    private javax.swing.JCheckBox m_jCatNameShow;
    private javax.swing.JTextField m_jCatOrder;
    private javax.swing.JComboBox m_jCategory;
    private com.openbravo.data.gui.JImageEditor m_jImage;
    private javax.swing.JTextField m_jName;
    private javax.swing.JTextField m_jTextTip;
    private javax.swing.JCheckBox webSwtch_InCatalog;
    private javax.swing.JLabel jLblDepartamentoTitle; // Título del departamento
    // End of variables declaration//GEN-END:variables
    
}

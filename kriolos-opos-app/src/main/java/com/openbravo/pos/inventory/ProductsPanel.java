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

import com.openbravo.basic.BasicException;
import com.openbravo.data.user.EditorListener;
import com.openbravo.data.user.EditorRecord;
import com.openbravo.data.user.ListProviderCreator;
import com.openbravo.data.user.DefaultSaveProvider;
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.pos.forms.DataLogicSales;
import com.openbravo.pos.forms.AppConfig;
import com.openbravo.pos.panels.JPanelTable2;
import com.openbravo.pos.ticket.ProductFilter;

import java.awt.Component;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.ArrayList;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Logger;
import java.util.logging.Level;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import com.openbravo.format.Formats;
import java.io.IOException;
import java.awt.CardLayout;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.BorderFactory;
import javax.swing.ListCellRenderer;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListDataEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;
import javax.swing.RowFilter;
import com.openbravo.data.gui.ComboBoxValModel;

/**
 *
 * @author JG uniCenta
 *
 */
public class ProductsPanel extends JPanelTable2 implements EditorListener {

    private static final Logger LOGGER = Logger.getLogger(ProductsPanel.class.getName());

    private ProductsEditor jeditor;
    private ProductFilter jproductfilter = null;

    private DataLogicSales m_dlSales = null;

    // Modern UI layout components
    private CardLayout cardLayout;
    private JPanel mainCardPanel;
    private JPanel listCard;
    private JTable productTable;
    private DefaultTableModel tableModel;
    private JTextField txtSearch;
    private JComboBox<String> cmbFilterBy;
    private TableRowSorter<DefaultTableModel> rowSorter;
    private boolean isLayoutInitialized = false;

    public ProductsPanel() {
    }

    /**
     *
     */
    @Override
    protected void init() {
        m_dlSales = (DataLogicSales) app.getBean("com.openbravo.pos.forms.DataLogicSales");

        jproductfilter = new ProductFilter();
        jproductfilter.init(app);

        row = m_dlSales.getProductsRow();

        lpr = new ListProviderCreator(m_dlSales.getProductCatQBF(), jproductfilter);

        // Usar SaveProvider estándar
        spr = new DefaultSaveProvider(
                m_dlSales.getProductCatUpdate(),
                m_dlSales.getProductCatInsert(),
                m_dlSales.getProductCatDelete());

        jeditor = new ProductsEditor(app, dirty, jproductfilter);
    }

    /**
     *
     * @return value
     */
    @Override
    public EditorRecord getEditor() {
        return jeditor;
    }

    /**
     *
     * 
     * @Override
     *           public Component getFilter() {
     *           return jproductfilter.getComponent();
     *           }
     */

    /**
     *
     * @return btnScanPal
     */
    @Override
    public Component getToolbarExtras() {
        if (app.getDeviceScanner() != null) {
            javax.swing.JPanel panel = new javax.swing.JPanel();
            panel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 0));
            panel.setOpaque(false);

            JButton btnScanPal = new JButton();
            btnScanPal.setText("ScanPal");
            btnScanPal.setVisible(true);
            btnScanPal.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    btnScanPalActionPerformed(evt);
                }
            });
            panel.add(btnScanPal);
            return panel;
        }
        return null;
    }

    /**
     *
     * @return value
     */
    @Override
    public String getTitle() {
        return AppLocal.getIntString("Menu.Products");
    }

    /**
     *
     * @throws BasicException
     */
    @Override
    public void activate() throws BasicException {
        // Primero activar el editor para inicializar taxeslogic antes de que
        // super.activate() lo necesite
        if (jeditor != null) {
            jeditor.activate();
        }
        if (jproductfilter != null) {
            jproductfilter.activate();
        }

        // Luego llamar a super.activate() que necesita taxeslogic inicializado
        super.activate();

        // Limpiar el navegador de la barra lateral del contenedor cada vez que se active,
        // ya que super.activate() lo recrea
        cleanUpContainer();

        if (!isLayoutInitialized) {
            initModernLayout();
            isLayoutInitialized = true;
        } else {
            updateToolbarButtons();
        }
        showListCard();
    }

    private void initModernLayout() {
        Color bg = new Color(248, 250, 252); // slate-50
        Color cardBg = Color.WHITE;
        Color cardBorder = new Color(226, 232, 240); // slate-200
        Color textPrimary = new Color(15, 23, 42); // slate-900
        Color textMuted = new Color(100, 116, 139); // slate-500
        Color accentColor = new Color(99, 102, 241); // indigo-500

        cardLayout = new CardLayout();
        mainCardPanel = new JPanel(cardLayout);
        mainCardPanel.setBackground(bg);

        // Programmatically remove the left JListNavigator (sidebar) from container robustly
        for (Component comp : container.getComponents()) {
            if (comp != null && comp.getClass().getName().contains("ListNavigator")) {
                container.remove(comp);
            }
        }

        // Remove the original container (editor + toolbar) from ProductsPanel and add to cards
        this.remove(container);
        mainCardPanel.add(container, "details");

        // Build listCard
        listCard = new JPanel(new BorderLayout(0, 15));
        listCard.setBackground(bg);
        listCard.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("Listado de productos");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(textPrimary);
        headerPanel.add(lblTitle, BorderLayout.WEST);

        // Header Right Panel (contains New Product and More Options)
        JPanel headerRightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        headerRightPanel.setOpaque(false);

        JButton btnNewProduct = new JButton("+ Nuevo Producto");
        btnNewProduct.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnNewProduct.setBackground(accentColor);
        btnNewProduct.setForeground(Color.WHITE);
        btnNewProduct.setFocusPainted(false);
        btnNewProduct.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        btnNewProduct.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnNewProduct.addActionListener(e -> {
            try {
                bd.actionInsert();
                showDetailCard();
            } catch (Exception ex) {
                LOGGER.log(Level.WARNING, "Error creating new product", ex);
            }
        });
        headerRightPanel.add(btnNewProduct);

        // Add modernized 3-dots button to List View header
        JButton btnMore = createThreeDotsButton();
        headerRightPanel.add(btnMore);

        headerPanel.add(headerRightPanel, BorderLayout.EAST);

        // Filter / Search Panel
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        filterPanel.setOpaque(false);

        JLabel lblFilter = new JLabel("Filtrar por:");
        lblFilter.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblFilter.setForeground(textMuted);
        filterPanel.add(lblFilter);

        cmbFilterBy = new JComboBox<>(new String[]{"Nombre", "Código Barras", "Referencia", "Categoría"});
        cmbFilterBy.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cmbFilterBy.setBackground(Color.WHITE);
        cmbFilterBy.setPreferredSize(new Dimension(150, 32));
        filterPanel.add(cmbFilterBy);

        txtSearch = new JTextField();
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtSearch.setPreferredSize(new Dimension(300, 32));
        txtSearch.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(cardBorder, 1, true),
            BorderFactory.createEmptyBorder(4, 10, 4, 10)
        ));
        
        txtSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filterTable(); }
            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filterTable(); }
            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filterTable(); }
        });
        filterPanel.add(txtSearch);

        JPanel topContainer = new JPanel(new BorderLayout(0, 15));
        topContainer.setOpaque(false);
        topContainer.add(headerPanel, BorderLayout.NORTH);
        topContainer.add(filterPanel, BorderLayout.CENTER);
        listCard.add(topContainer, BorderLayout.NORTH);

        // Card Table Panel
        JPanel cardTablePanel = new JPanel(new BorderLayout());
        cardTablePanel.setBackground(Color.WHITE);
        cardTablePanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(cardBorder, 1, true),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        String[] columns = {"#", "Referencia", "Código Barras", "Nombre", "Categoría", "Precio Compra", "Precio Venta", "Impuesto"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };

        productTable = new JTable(tableModel);
        productTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        productTable.setRowHeight(32);
        productTable.setGridColor(cardBorder);
        productTable.setSelectionBackground(new Color(238, 242, 255));
        productTable.setSelectionForeground(textPrimary);
        productTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        
        JTableHeader header = productTable.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setBackground(new Color(241, 245, 249));
        header.setForeground(textPrimary);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, cardBorder));
        header.setPreferredSize(new Dimension(header.getWidth(), 35));

        rowSorter = new TableRowSorter<>(tableModel);
        productTable.setRowSorter(rowSorter);

        productTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && productTable.getSelectedRow() != -1) {
                    int viewRow = productTable.getSelectedRow();
                    int modelRow = productTable.convertRowIndexToModel(viewRow);
                    selectAndEdit(modelRow);
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(productTable);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(Color.WHITE);
        cardTablePanel.add(scrollPane, BorderLayout.CENTER);
        
        listCard.add(cardTablePanel, BorderLayout.CENTER);
        mainCardPanel.add(listCard, "list");
        this.add(mainCardPanel, BorderLayout.CENTER);

        updateToolbarButtons();

        populateTableData();
        bd.getListModel().addListDataListener(new javax.swing.event.ListDataListener() {
            @Override
            public void intervalAdded(javax.swing.event.ListDataEvent e) { populateTableData(); }
            @Override
            public void intervalRemoved(javax.swing.event.ListDataEvent e) { populateTableData(); }
            @Override
            public void contentsChanged(javax.swing.event.ListDataEvent e) { populateTableData(); }
        });
    }

    private void populateTableData() {
        if (tableModel == null || bd == null) return;
        tableModel.setRowCount(0);
        javax.swing.ListModel listModel = bd.getListModel();
        int size = listModel.getSize();
        for (int i = 0; i < size; i++) {
            Object element = listModel.getElementAt(i);
            if (element instanceof Object[]) {
                Object[] myprod = (Object[]) element;
                String ref = Formats.STRING.formatValue((String) myprod[1]);
                String code = Formats.STRING.formatValue((String) myprod[2]);
                String name = Formats.STRING.formatValue((String) myprod[4]);
                Double priceBuy = (Double) myprod[5];
                Double priceSell = (Double) myprod[6];
                
                String categoryId = (String) myprod[7];
                String categoryName = getCategoryName(categoryId);
                
                String taxId = (String) myprod[8];
                String taxName = getTaxName(taxId);

                tableModel.addRow(new Object[]{
                    (i + 1),
                    ref,
                    code,
                    name,
                    categoryName,
                    priceBuy != null ? Formats.CURRENCY.formatValue(priceBuy) : "$0.00",
                    priceSell != null ? Formats.CURRENCY.formatValue(priceSell) : "$0.00",
                    taxName
                });
            }
        }
    }

    private String getCategoryName(String catId) {
        if (catId == null || jeditor == null || jeditor.getCategoryModel() == null) return "";
        ComboBoxValModel model = jeditor.getCategoryModel();
        for (int i = 0; i < model.getSize(); i++) {
            Object obj = model.getElementAt(i);
            if (obj != null) {
                try {
                    java.lang.reflect.Method mGetKey = obj.getClass().getMethod("getKey");
                    if (catId.equals(mGetKey.invoke(obj))) {
                        return obj.toString();
                    }
                } catch (Exception e) {
                    try {
                        java.lang.reflect.Method mGetId = obj.getClass().getMethod("getId");
                        if (catId.equals(mGetId.invoke(obj))) {
                            return obj.toString();
                        }
                    } catch (Exception e2) {
                        if (obj instanceof Object[]) {
                            Object[] arr = (Object[]) obj;
                            if (arr.length > 0 && catId.equals(arr[0])) {
                                return arr.length > 1 ? String.valueOf(arr[1]) : String.valueOf(arr[0]);
                            }
                        }
                    }
                }
            }
        }
        return catId;
    }

    private String getTaxName(String taxId) {
        if (taxId == null || jeditor == null || jeditor.getTaxCatModel() == null) return "";
        ComboBoxValModel model = jeditor.getTaxCatModel();
        for (int i = 0; i < model.getSize(); i++) {
            Object obj = model.getElementAt(i);
            if (obj != null) {
                try {
                    java.lang.reflect.Method mGetKey = obj.getClass().getMethod("getKey");
                    if (taxId.equals(mGetKey.invoke(obj))) {
                        return obj.toString();
                    }
                } catch (Exception e) {
                    try {
                        java.lang.reflect.Method mGetId = obj.getClass().getMethod("getId");
                        if (taxId.equals(mGetId.invoke(obj))) {
                            return obj.toString();
                        }
                    } catch (Exception e2) {
                        if (obj instanceof Object[]) {
                            Object[] arr = (Object[]) obj;
                            if (arr.length > 0 && taxId.equals(arr[0])) {
                                return arr.length > 1 ? String.valueOf(arr[1]) : String.valueOf(arr[0]);
                            }
                        }
                    }
                }
            }
        }
        return taxId;
    }

    private void selectAndEdit(int modelRow) {
        try {
            bd.moveTo(modelRow);
            showDetailCard();
        } catch (BasicException ex) {
            LOGGER.log(Level.WARNING, "Error moving to selected product", ex);
        }
    }

    private void filterTable() {
        String text = txtSearch.getText();
        int filterIndex = cmbFilterBy.getSelectedIndex();
        int colIndex = 3; // Default Name
        if (filterIndex == 1) colIndex = 2;
        else if (filterIndex == 2) colIndex = 1;
        else if (filterIndex == 3) colIndex = 4;

        if (text == null || text.trim().isEmpty()) {
            rowSorter.setRowFilter(null);
        } else {
            rowSorter.setRowFilter(RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(text), colIndex));
        }
    }

    private void showListCard() {
        if (cardLayout != null) {
            cardLayout.show(mainCardPanel, "list");
        }
    }

    public void showDetailCard() {
        if (cardLayout != null) {
            cardLayout.show(mainCardPanel, "details");
        }
    }

    public void showProductById(String productId) {
        if (bd == null || bd.getListModel() == null) {
            return;
        }
        
        showListCard();
        
        javax.swing.ListModel listModel = bd.getListModel();
        int size = listModel.getSize();
        for (int i = 0; i < size; i++) {
            Object element = listModel.getElementAt(i);
            if (element instanceof Object[]) {
                Object[] myprod = (Object[]) element;
                if (myprod.length > 0 && productId.equals(myprod[0])) {
                    selectAndEdit(i);
                    return;
                }
            }
        }
        
        if (jproductfilter != null) {
            jproductfilter.resetFilter();
        }
        
        try {
            bd.refreshData();
            listModel = bd.getListModel();
            size = listModel.getSize();
            for (int i = 0; i < size; i++) {
                Object element = listModel.getElementAt(i);
                if (element instanceof Object[]) {
                    Object[] myprod = (Object[]) element;
                    if (myprod.length > 0 && productId.equals(myprod[0])) {
                        selectAndEdit(i);
                        return;
                    }
                }
            }
        } catch (BasicException ex) {
            LOGGER.log(Level.WARNING, "Error reloading products for navigation", ex);
        }
    }

    /**
     *
     * @param value
     */
    @Override
    public void updateValue(Object value) {
        // Guardar valores de stock después de actualizar el producto
        try {
            if (jeditor != null) {
                jeditor.saveStockValues();
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error al guardar valores de stock", e);
        }

        // Sincronizar cambios de producto con CRM Voltium en segundo plano
        try {
            com.openbravo.pos.sync.VoltiumSyncService.sincronizarProductosAsync();
        } catch (Exception ignored) {}
    }

    /**
     * Maneja la exportación de todos los productos a un archivo CSV compatible con
     * Excel
     */
    private void btnExportExcelActionPerformed(java.awt.event.ActionEvent evt) {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Guardar lista de productos");
        fc.setFileFilter(new FileNameExtensionFilter("Archivo CSV (Excel) (*.csv)", "csv"));
        fc.setSelectedFile(new java.io.File("Productos_"
                + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm")) + ".csv"));

        int returnVal = fc.showSaveDialog(this);
        if (returnVal != JFileChooser.APPROVE_OPTION) {
            return;
        }

        java.io.File file = fc.getSelectedFile();
        if (!file.getName().toLowerCase().endsWith(".csv")) {
            file = new java.io.File(file.getAbsolutePath() + ".csv");
        }

        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
            // Escribir BOM para que Excel reconozca UTF-8 correctamente
            writer.write('\ufeff');

            // Cabeceras
            writer.write(
                    "ID;Referencia;Codigo;Nombre;Precio Compra;Precio Venta;Categoria;Impuesto;Atributos;En Inventario");
            writer.newLine();

            // Obtener todos los productos
            com.openbravo.data.loader.Session session = m_dlSales.getSession();
            String sql = "SELECT p.ID, p.REFERENCE, p.CODE, p.NAME, p.PRICEBUY, p.PRICESELL, " +
                    "c.NAME as CATEGORY, t.NAME as TAX, a.NAME as ATTRIBUTES, " +
                    "CASE WHEN p.ISCOM = 1 THEN 'NO' ELSE 'SI' END as STOCK " +
                    "FROM products p " +
                    "LEFT JOIN categories c ON p.CATEGORY = c.ID " +
                    "LEFT JOIN taxes t ON p.TAXCAT = t.ID " +
                    "LEFT JOIN attributeset a ON p.ATTRIBUTESET_ID = a.ID " +
                    "ORDER BY p.NAME";

            try (PreparedStatement stmt = session.getConnection().prepareStatement(sql);
                    ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    writer.write(sanitizeCsv(rs.getString("ID")) + ";");
                    writer.write(sanitizeCsv(rs.getString("REFERENCE")) + ";");
                    writer.write(sanitizeCsv(rs.getString("CODE")) + ";");
                    writer.write(sanitizeCsv(rs.getString("NAME")) + ";");
                    writer.write(Formats.CURRENCY.formatValue(rs.getDouble("PRICEBUY")) + ";");
                    writer.write(Formats.CURRENCY.formatValue(rs.getDouble("PRICESELL")) + ";");
                    writer.write(sanitizeCsv(rs.getString("CATEGORY")) + ";");
                    writer.write(sanitizeCsv(rs.getString("TAX")) + ";");
                    writer.write(sanitizeCsv(rs.getString("ATTRIBUTES")) + ";");
                    writer.write(rs.getString("STOCK"));
                    writer.newLine();
                }
            }

            JOptionPane.showMessageDialog(this,
                    "✅ Lista de productos exportada exitosamente.\n\n" +
                            "Archivo: " + file.getName(),
                    "Exportación Exitosa",
                    JOptionPane.INFORMATION_MESSAGE);

        } catch (IOException | SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error exportando productos: " + ex.getMessage(), ex);
            JOptionPane.showMessageDialog(this,
                    "❌ Error al exportar los productos.\n\n" + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void btnImportExcelActionPerformed(java.awt.event.ActionEvent evt) {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Seleccionar archivo Excel (CSV)");
        fc.setFileFilter(new FileNameExtensionFilter("Archivo CSV de Excel (*.csv)", "csv"));

        int returnVal = fc.showOpenDialog(this);
        if (returnVal != JFileChooser.APPROVE_OPTION) {
            return;
        }

        java.io.File file = fc.getSelectedFile();

        try {
            ExcelImporter importer = new ExcelImporter(app);
            int count = importer.importExcel(file);

            JOptionPane.showMessageDialog(this,
                    "✅ Importación completada.\n\n" +
                            "Se procesaron " + count + " productos correctamente.",
                    "Importación Exitosa",
                    JOptionPane.INFORMATION_MESSAGE);

            // Refrescar datos
            try {
                bd.refreshData();
            } catch (BasicException ex) {
                LOGGER.log(Level.WARNING, "Error refrescando datos después de importar: " + ex.getMessage(), ex);
            }

        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Error importando Excel: " + ex.getMessage(), ex);
            JOptionPane.showMessageDialog(this,
                    "❌ Error al importar el archivo Excel.\n\n" + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void btnStockPendingActionPerformed(java.awt.event.ActionEvent evt) {
        JDlgStockPending.showMessage(this, app);
    }

    private void btnScanPalActionPerformed(java.awt.event.ActionEvent evt) {
        // Implementation for ScanPal button
    }

    private String sanitizeCsv(String text) {
        if (text == null)
            return "";
        // Reemplazar punto y coma por coma para no romper el formato CSV delimitado por
        // ;
        return text.replace(";", ",");
    }

    private JButton createThreeDotsButton() {
        final JButton btnMore = new JButton(new ThreeDotsIcon());
        btnMore.setToolTipText("Más opciones");
        btnMore.setPreferredSize(new java.awt.Dimension(32, 32));
        btnMore.setBackground(java.awt.Color.WHITE);
        btnMore.setForeground(new java.awt.Color(71, 85, 105));
        btnMore.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(203, 213, 225), 1));
        btnMore.setFocusPainted(false);
        btnMore.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        final javax.swing.JPopupMenu popupMenu = new javax.swing.JPopupMenu();
        popupMenu.setBackground(java.awt.Color.WHITE);
        popupMenu.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(226, 232, 240), 1));

        javax.swing.JMenuItem itemExport = new javax.swing.JMenuItem("Exportar Excel");
        itemExport.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 13));
        itemExport.setBackground(java.awt.Color.WHITE);
        itemExport.setForeground(new java.awt.Color(30, 41, 59));
        itemExport.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnExportExcelActionPerformed(evt);
            }
        });

        javax.swing.JMenuItem itemImport = new javax.swing.JMenuItem("Importar Excel");
        itemImport.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 13));
        itemImport.setBackground(java.awt.Color.WHITE);
        itemImport.setForeground(new java.awt.Color(30, 41, 59));
        itemImport.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnImportExcelActionPerformed(evt);
            }
        });

        javax.swing.JMenuItem itemStockPending = new javax.swing.JMenuItem("Stock Pendiente");
        itemStockPending.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 13));
        itemStockPending.setBackground(java.awt.Color.WHITE);
        itemStockPending.setForeground(new java.awt.Color(30, 41, 59));
        itemStockPending.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnStockPendingActionPerformed(evt);
            }
        });

        javax.swing.JMenuItem itemHistory = new javax.swing.JMenuItem("Historial");
        itemHistory.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 13));
        itemHistory.setBackground(java.awt.Color.WHITE);
        itemHistory.setForeground(new java.awt.Color(30, 41, 59));
        itemHistory.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showAuditHistoryDialog();
            }
        });

        javax.swing.JMenuItem itemSyncVoltium = new javax.swing.JMenuItem("Sincronizar con CRM Voltium");
        itemSyncVoltium.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 13));
        itemSyncVoltium.setBackground(java.awt.Color.WHITE);
        itemSyncVoltium.setForeground(new java.awt.Color(15, 118, 110));
        itemSyncVoltium.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSyncVoltiumActionPerformed(evt);
            }
        });

        popupMenu.add(itemSyncVoltium);
        popupMenu.addSeparator();
        popupMenu.add(itemExport);
        popupMenu.add(itemImport);
        popupMenu.add(itemStockPending);
        popupMenu.add(itemHistory);

        btnMore.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                popupMenu.show(btnMore, 0, btnMore.getHeight());
            }
        });

        return btnMore;
    }

    private void btnSyncVoltiumActionPerformed(java.awt.event.ActionEvent evt) {
        new Thread(() -> {
            try {
                int syncCount = com.openbravo.pos.sync.VoltiumSyncService.sincronizarProductos();
                javax.swing.SwingUtilities.invokeLater(() -> {
                    try {
                        bd.refreshData();
                    } catch (Exception ignored) {}
                    if (syncCount >= 0) {
                        JOptionPane.showMessageDialog(ProductsPanel.this,
                                "✅ Sincronización con CRM Voltium completada con éxito.\n\n"
                                        + "Se sincronizaron " + syncCount + " productos con el catálogo.",
                                "Sincronización Exitosa",
                                JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(ProductsPanel.this,
                                "⚠️ Servidor CRM no disponible en este momento.\n\n"
                                        + "La aplicación sigue funcionando normalmente en modo local.\n"
                                        + "Los datos se sincronizarán automáticamente al restablecer la conexión.",
                                "Modo Fuera de Línea",
                                JOptionPane.WARNING_MESSAGE);
                    }
                });
            } catch (Exception ex) {
                LOGGER.log(Level.WARNING, "Error al sincronizar con CRM Voltium: " + ex.getMessage(), ex);
                javax.swing.SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(ProductsPanel.this,
                            "⚠️ Servidor CRM no disponible en este momento.\n\n"
                                    + "La aplicación seguirá operando localmente con normalidad.\n"
                                    + "Los datos pendientes se sincronizarán automáticamente al reconectar.",
                            "Modo Fuera de Línea",
                            JOptionPane.WARNING_MESSAGE);
                });
            }
        }, "Sync-Voltium-Manual").start();
    }

    private static class ThreeDotsIcon implements javax.swing.Icon {
        @Override
        public void paintIcon(Component c, java.awt.Graphics g, int x, int y) {
            java.awt.Graphics2D g2d = (java.awt.Graphics2D) g.create();
            g2d.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setColor(new java.awt.Color(71, 85, 105)); // Slate-600 color
            int size = 4;
            int startX = x + (getIconWidth() - size) / 2;
            int startY = y + (getIconHeight() - 16) / 2; // Center vertically
            g2d.fillOval(startX, startY, size, size);
            g2d.fillOval(startX, startY + 6, size, size);
            g2d.fillOval(startX, startY + 12, size, size);
            g2d.dispose();
        }

        @Override
        public int getIconWidth() {
            return 24;
        }

        @Override
        public int getIconHeight() {
            return 24;
        }
    }

    private void updateToolbarButtons() {
        if (toolbar != null) {
            java.awt.LayoutManager layout = toolbar.getLayout();
            if (layout instanceof BorderLayout) {
                BorderLayout bl = (BorderLayout) layout;
                Component westComp = bl.getLayoutComponent(BorderLayout.WEST);
                if (westComp instanceof JPanel) {
                    JPanel leftToolbar = (JPanel) westComp;
                    
                    boolean hasBack = false;
                    for (Component comp : leftToolbar.getComponents()) {
                        if (comp instanceof JButton) {
                            JButton btn = (JButton) comp;
                            if ("<- Volver al Listado".equals(btn.getText())) {
                                hasBack = true;
                            }
                            
                            // Hide the standard "New/Insert" button from the details toolbar
                            String tooltip = btn.getToolTipText();
                            if (tooltip != null && (tooltip.toLowerCase().contains("nuevo") || tooltip.toLowerCase().contains("new"))) {
                                btn.setVisible(false);
                            }
                        }
                    }
                    
                    if (!hasBack) {
                        JButton btnBack = new JButton("<- Volver al Listado");
                        btnBack.setFont(new Font("Segoe UI", Font.BOLD, 12));
                        btnBack.setBackground(new Color(241, 245, 249)); // slate-100
                        btnBack.setForeground(new Color(71, 85, 105)); // slate-600
                        btnBack.setFocusPainted(false);
                        btnBack.setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(new Color(226, 232, 240), 1, true), // slate-200
                            BorderFactory.createEmptyBorder(6, 12, 6, 12)
                        ));
                        btnBack.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
                        btnBack.addActionListener(e -> {
                            if (dirty.isDirty()) {
                                int res = JOptionPane.showConfirmDialog(this, 
                                    "Tienes cambios sin guardar. ¿Seguro que deseas volver al listado y perder los cambios?",
                                    "Cambios sin guardar", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                                if (res != JOptionPane.YES_OPTION) {
                                    return;
                                }
                                dirty.setDirty(false);
                            }
                            showListCard();
                        });
                        leftToolbar.add(btnBack, 0); // Add at the beginning of leftToolbar
                    }
                }
            }
            toolbar.revalidate();
            toolbar.repaint();
        }
    }

    private void cleanUpContainer() {
        if (container != null) {
            for (Component comp : container.getComponents()) {
                if (comp != null && comp.getClass().getName().contains("ListNavigator")) {
                    container.remove(comp);
                }
            }
            container.revalidate();
            container.repaint();
        }
    }
}
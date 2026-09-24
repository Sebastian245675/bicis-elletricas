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

import com.openbravo.pos.forms.DataLogicSales;
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.basic.BasicException;
import com.openbravo.beans.DateUtils;
import com.openbravo.beans.JCalendarDialog;
import com.openbravo.data.gui.ComboBoxValModel;
import com.openbravo.data.gui.MessageInf;
import com.openbravo.data.loader.LocalRes;
import com.openbravo.data.loader.SentenceExec;
import com.openbravo.data.loader.SentenceList;
import com.openbravo.data.loader.SentenceFind;
import com.openbravo.data.loader.PreparedSentence;
import com.openbravo.data.loader.SerializerWriteBasicExt;
import com.openbravo.data.loader.SerializerReadString;
import com.openbravo.data.loader.Datas;
import com.openbravo.format.Formats;
import com.openbravo.pos.catalog.CatalogSelector;
import com.openbravo.pos.catalog.JCatalog;
import com.openbravo.pos.forms.*;
import com.openbravo.pos.panels.JProductFinder;
import com.openbravo.pos.printer.TicketParser;
import com.openbravo.pos.printer.TicketPrinterException;
import com.openbravo.pos.sales.JProductAttEdit2;
import com.openbravo.pos.scripting.ScriptEngine;
import com.openbravo.pos.scripting.ScriptException;
import com.openbravo.pos.scripting.ScriptFactory;
import com.openbravo.pos.suppliers.DataLogicSuppliers;
import com.openbravo.pos.suppliers.SupplierInfo;
import com.openbravo.pos.ticket.ProductInfoExt;
import com.openbravo.pos.util.ModernActionIcon;
import java.awt.BorderLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.BasicStroke;
import java.awt.Dimension;
import java.awt.CardLayout;
import java.awt.Component;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import java.awt.GridBagLayout;

/**
 * Date : Aug 2017 Updated : Dec 2016
 *
 * @author jack gerrard
 */
public class StockManagement extends JPanel implements JPanelView {

    private final AppView m_App;
    private final String user;

    private final DataLogicSystem m_dlSystem;
    private final DataLogicSales m_dlSales;
    private final DataLogicSuppliers m_dlSuppliers;
    private final TicketParser m_TTP;

    // Catálogo deshabilitado - ya no se usa
    // private final CatalogSelector m_cat;
    private final ComboBoxValModel m_ReasonModel;

    private final SentenceList<LocationInfo> m_sentlocations;
    private ComboBoxValModel m_LocationsModel;
    private ComboBoxValModel m_LocationsModelDes;

    private final SentenceList m_sentsuppliers;
    private ComboBoxValModel m_SuppliersModel;

    private final JInventoryLines m_invlines;

    private int NUMBER_STATE = 0;
    private int MULTIPLY = 0;
    private static final int DEFAULT = 0;
    private static final int ACTIVE = 1;
    private static final int DECIMAL = 2;

    private List<ProductStock> productStockList;
    private ProductStockTableModel stockModel;
    
    private List<com.openbravo.pos.inventory.LowStockProduct> lowStockProducts;
    private List<com.openbravo.pos.inventory.LowStockProduct> filteredLowStockProducts;
    private LowStockProductTableModel lowStockModel;
    
    private javax.swing.JPanel lowStockPanel;
    private javax.swing.JPanel lowStockCardPanel;
    private java.awt.CardLayout lowStockCardLayout;
    private javax.swing.JPanel emptyStatePanel;
    private javax.swing.JTextField searchField;
    private javax.swing.JLabel alertLabel;
    private javax.swing.JPanel paginationPanel;
    private int currentPage = 0;
    private static final int ROWS_PER_PAGE = 10;

    private final static int NUMBERZERO = 0;
    private final static int NUMBERVALID = 1;

    private final static int NUMBER_INPUTZERO = 0;
    private final static int NUMBER_INPUTZERODEC = 1;
    private final static int NUMBER_INPUTINT = 2;
    private final static int NUMBER_INPUTDEC = 3;
    private final static int NUMBER_PORZERO = 4;
    private final static int NUMBER_PORZERODEC = 5;
    private final static int NUMBER_PORINT = 6;
    private final static int NUMBER_PORDEC = 7;

    private int m_iNumberStatus;
    private int m_iNumberStatusInput;
    private int m_iNumberStatusPor;
    private StringBuffer m_sBarcode;

    /**
     * Creates new form StockManagement
     *
     * @param app
     */
    public StockManagement(AppView app) {

        m_App = app;
        m_dlSystem = (DataLogicSystem) m_App.getBean("com.openbravo.pos.forms.DataLogicSystem");
        m_dlSales = (DataLogicSales) m_App.getBean("com.openbravo.pos.forms.DataLogicSales");
        m_dlSuppliers = (DataLogicSuppliers) m_App.getBean("com.openbravo.pos.suppliers.DataLogicSuppliers");
        m_TTP = new TicketParser(m_App.getDeviceTicket(), m_dlSystem);

        initComponents();

        user = m_App.getAppUserView().getUser().getName();

        // jNumberKeys.setEnabled(true); // ELIMINADO - TECLADO NUMERICO
        // jNumberKeys.setVisible(false); // Ocultar el teclado numérico // ELIMINADO - TECLADO NUMERICO
        // jPanel1.setVisible(false); // Ocultar completamente el panel que contiene el teclado // ELIMINADO - TECLADO NUMERICO
        
        // Remover físicamente el panel del contenedor padre
        // if (jPanel1.getParent() != null) { // ELIMINADO - TECLADO NUMERICO
        //     jPanel1.getParent().remove(jPanel1);
        // }

        lblTotalQtyValue.setText(null);
        lbTotalValue.setText(null);

        m_sentlocations = m_dlSales.getLocationsList();
        m_LocationsModel = new ComboBoxValModel();
        m_LocationsModelDes = new ComboBoxValModel();

        m_ReasonModel = new ComboBoxValModel();

        m_ReasonModel.add(MovementReason.IN_PURCHASE);                          //Supplier Purchase
        m_ReasonModel.add(MovementReason.OUT_SALE);                             //Sale
        m_ReasonModel.add(MovementReason.IN_REFUND);                            //Customer Refund
        m_ReasonModel.add(MovementReason.OUT_REFUND);                           //Supplier Return
        m_ReasonModel.add(MovementReason.IN_MOVEMENT);                          //Adjust Add
        m_ReasonModel.add(MovementReason.OUT_MOVEMENT);                         //Adjust Subtract
        m_ReasonModel.add(MovementReason.OUT_SUBTRACT);                         //Rectify error per JM requirement        
        m_ReasonModel.add(MovementReason.OUT_BREAK);                            //Breakage
        m_ReasonModel.add(MovementReason.OUT_FREE);                             //Given Free   
        m_ReasonModel.add(MovementReason.OUT_SAMPLE);                           //Given Sample
        m_ReasonModel.add(MovementReason.OUT_USED);                             //Used item   
        m_ReasonModel.add(MovementReason.OUT_CROSSING);                         //Inter-Location move

        m_jreason.setModel(m_ReasonModel);

        m_sentsuppliers = m_dlSuppliers.getSupplierList();
        m_SuppliersModel = new ComboBoxValModel();

        // Catálogo deshabilitado - ya no se usa
        // m_cat = new JCatalog(m_dlSales);
        // m_cat.addActionListener(new CatalogListener());
        // catcontainer.add(m_cat.getComponent(), BorderLayout.CENTER);

        m_invlines = new JInventoryLines();
        jPanel5.add(m_invlines, BorderLayout.CENTER);

        jTableProductStock.setVisible(false);
        jPanel8.setVisible(false); // Ocultar la parte superior (Entrada/Salida)

        styleComponents();
    }

    /**
     *
     * @return
     */
    @Override
    public String getTitle() {
        return AppLocal.getIntString("Menu.StockMovement");
    }

    /**
     *
     * @return
     */
    @Override
    public JComponent getComponent() {
        return this;
    }

    /**
     *
     * @throws BasicException
     */
    @Override
    public void activate() throws BasicException {
        // Actualizar el template en la base de datos desde el archivo XML
        actualizarTemplateEnBD();
        
        // Catálogo deshabilitado
        // m_cat.loadCatalog();

        java.util.List<LocationInfo> l = m_sentlocations.list();
        m_LocationsModel = new ComboBoxValModel<LocationInfo>(l);
        m_jLocation.setModel(m_LocationsModel);
        m_LocationsModelDes = new ComboBoxValModel(l);
        m_jLocationDes.setModel(m_LocationsModelDes);

        java.util.List sl = m_sentsuppliers.list();
        m_SuppliersModel = new ComboBoxValModel(sl);
        m_jSupplier.setModel(m_SuppliersModel);

        // Cargar productos con stock bajo
        loadLowStockProducts();

        // Asegurar que el teclado esté oculto
        // jNumberKeys.setVisible(false); // ELIMINADO - TECLADO NUMERICO
        // jPanel1.setVisible(false); // Ocultar el panel completo del teclado // ELIMINADO - TECLADO NUMERICO

        stateToInsert();

        java.awt.EventQueue.invokeLater(() -> {
            jTextField1.requestFocus();
        });
    }

    /**
     *
     */
    public void stateToInsert() {

        m_jdate.setText(Formats.TIMESTAMP.formatValue(DateUtils.getTodayMinutes()));
        m_ReasonModel.setSelectedItem(MovementReason.IN_PURCHASE);
        m_LocationsModel.setSelectedKey(m_App.getInventoryLocation());
//        m_LocationsModel.setSelectedFirst();
        m_LocationsModelDes.setSelectedKey(m_App.getInventoryLocation());
        m_jcodebar.setText(null);
        m_SuppliersModel.setSelectedFirst();
        m_jSupplierDoc.setText(null);
        m_invlines.clear();
        resetTranxTable();
        
        // Mantener el teclado oculto
        // jNumberKeys.setVisible(false); // ELIMINADO - TECLADO NUMERICO
        // jPanel1.setVisible(false); // Mantener el panel del teclado oculto // ELIMINADO - TECLADO NUMERICO
    }

    /**
     * Actualiza el template Printer.Inventory en la base de datos desde el archivo XML
     */
    private void actualizarTemplateEnBD() {
        try {
            // Leer el archivo XML desde el classpath
            java.io.InputStream is = getClass().getResourceAsStream("/com/openbravo/pos/templates/Printer.Inventory.xml");
            if (is == null) {
                Logger.getLogger(StockManagement.class.getName()).log(Level.WARNING, "No se pudo encontrar el archivo Printer.Inventory.xml en el classpath");
                return;
            }
            
            // Leer todo el contenido del archivo
            java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                baos.write(buffer, 0, bytesRead);
            }
            byte[] templateContent = baos.toByteArray();
            is.close();
            baos.close();
            
            // Actualizar el template en la base de datos
            // Tipo 0 = texto/XML
            m_dlSystem.setResource("Printer.Inventory", 0, templateContent);
            Logger.getLogger(StockManagement.class.getName()).log(Level.INFO, "Template Printer.Inventory actualizado en la base de datos");
            
        } catch (java.io.IOException e) {
            Logger.getLogger(StockManagement.class.getName()).log(Level.SEVERE, "Error leyendo el archivo Printer.Inventory.xml: " + e.getMessage(), e);
        } catch (Exception e) {
            Logger.getLogger(StockManagement.class.getName()).log(Level.SEVERE, "Error actualizando template en BD: " + e.getMessage(), e);
        }
    }

    /**
     *
     * @return
     */
    @Override
    public boolean deactivate() {

        if (m_invlines.getCount() > 0) {
            int res = JOptionPane.showConfirmDialog(this,
                    LocalRes.getIntString("message.wannasave"),
                    LocalRes.getIntString("title.editor"),
                    JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE);
            if (res == JOptionPane.YES_OPTION) {
                saveData();
                return true;
            } else {
                return res == JOptionPane.NO_OPTION;
            }
        } else {
            return true;
        }
    }

    private void addLine(ProductInfoExt oProduct, double dpor, double dprice) {
        m_invlines.addLine(new InventoryLine(oProduct, dpor, dprice));
        showStockTable();
    }

    private void deleteLine(int index) {
        if (index < 0) {
            Toolkit.getDefaultToolkit().beep();
        } else {
            m_invlines.deleteLine(index);
            clearStockTable();
            showStockTable();

        }
    }

    private void incProduct(ProductInfoExt product, double units) {

        MovementReason reason = (MovementReason) m_ReasonModel.getSelectedItem();
        addLine(product, units, reason.isInput()
                ? product.getPriceBuy()
                : product.getPriceSell());
    }

    private void incProductByCode(String sCode) {
        incProductByCode(sCode, 1.0);
    }

    private void incProductByCode(String sCode, double dQuantity) {

        try {
            ProductInfoExt oProduct = m_dlSales.getProductInfoByCode(sCode);
            if (oProduct == null) {
                Toolkit.getDefaultToolkit().beep();
            } else {
                incProduct(oProduct, dQuantity);
            }
        } catch (BasicException eData) {
            MessageInf msg = new MessageInf(eData);
            msg.show(this);
        }
    }

    private List<ProductStock> getProductOfName(String pId) {

        try {
            productStockList = m_dlSales.getProductStockList(pId);

        } catch (BasicException ex) {
            Logger.getLogger(ProductsEditor.class.getName()).log(Level.SEVERE, null, ex);
        }

        List<ProductStock> productList = new ArrayList<>();

        productStockList.stream().forEach((productStock) -> {
            String productId = productStock.getProductId();
            if (productId.equals(pId)) {
                productList.add(productStock);
            }
        });

        repaint();

        return productList;
    }

    public void resetTranxTable() {
        // Configurar anchos de columna más apropiados
        jTableProductStock.getColumnModel().getColumn(0).setPreferredWidth(120); // Location
        jTableProductStock.getColumnModel().getColumn(1).setPreferredWidth(80);  // Current (Actual)
        jTableProductStock.getColumnModel().getColumn(2).setPreferredWidth(80);  // Minimum
        jTableProductStock.getColumnModel().getColumn(3).setPreferredWidth(80);  // Maximum
        jTableProductStock.getColumnModel().getColumn(4).setPreferredWidth(90);  // Price Buy
        jTableProductStock.getColumnModel().getColumn(5).setPreferredWidth(100); // Value
        
        // Alinear columnas numéricas a la derecha
        javax.swing.table.DefaultTableCellRenderer rightRenderer = new javax.swing.table.DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(javax.swing.JLabel.RIGHT);
        
        for (int i = 1; i <= 5; i++) {
            jTableProductStock.getColumnModel().getColumn(i).setCellRenderer(rightRenderer);
        }
        
        // Mejorar altura de fila para mejor legibilidad
        jTableProductStock.setRowHeight(28);
        
        // Habilitar edición con doble clic
        jTableProductStock.setDefaultEditor(Double.class, new javax.swing.DefaultCellEditor(new javax.swing.JTextField()) {
            @Override
            public boolean isCellEditable(java.util.EventObject e) {
                if (e instanceof java.awt.event.MouseEvent) {
                    return ((java.awt.event.MouseEvent) e).getClickCount() >= 2;
                }
                return super.isCellEditable(e);
            }
        });
        
        // Aplicar colores alternados para mejor legibilidad
        jTableProductStock.setSelectionBackground(new java.awt.Color(184, 207, 229));
        jTableProductStock.setSelectionForeground(java.awt.Color.BLACK);
        jTableProductStock.setGridColor(new java.awt.Color(220, 220, 220));

        jTableProductStock.repaint();
    }

    public void clearStockTable() {

        TableModel tModel = jTableProductStock.getModel();
        if (tModel != null) {
            ProductStockTableModel model = (ProductStockTableModel) tModel;

            while (model.getRowCount() > 0) {
                for (int i = 0; i < model.getRowCount(); ++i) {
                    model.stockList.removeAll(productStockList);
                }
            }
        }

        lblTotalQtyValue.setText(null);
        lbTotalValue.setText(null);

        jTableProductStock.repaint();
    }

    public void showStockTable() {
        String pId = null;
        int i = m_invlines.getSelectedRow();

        if (i < 0) {
            Toolkit.getDefaultToolkit().beep();
            JOptionPane.showMessageDialog(null,
                    "Por favor seleccione un producto de la lista",
                    "Seleccionar Producto",
                    JOptionPane.INFORMATION_MESSAGE);
        } else {
            InventoryLine line = m_invlines.getLine(i);
            pId = line.getProductID();
        }
        
        if (pId != null) {
            try {
                stockModel = new StockManagement.ProductStockTableModel(getProductOfName(pId));

                jTableProductStock.setModel((TableModel) stockModel);
                
                // Verificar si el usuario es empleado (rol 3)
                boolean isEmployee = false;
                try {
                    String userRole = m_App.getAppUserView().getUser().getRole();
                    isEmployee = "3".equals(userRole);
                } catch (Exception ex) {
                    // Si hay error al obtener el rol, asumir que no es empleado
                }
                
                if (stockModel.getRowCount() > 0) {
                    jTableProductStock.setVisible(true);
                    
                    // Configurar el renderizado y edición de la tabla
                    resetTranxTable();
                    sumStockTable();
                    
                    // Solo mostrar mensaje informativo si NO es empleado
                    if (!isEmployee) {
                        // Mostrar mensaje informativo
                        JOptionPane.showMessageDialog(null,
                                "<html><body style='width: 300px; padding: 10px;'>" +
                                "<b>Información de Stock</b><br><br>" +
                                "• Doble clic en las columnas <b>Actual</b>, <b>Mínimo</b> o <b>Máximo</b> para editar<br>" +
                                "• Los cambios se guardan automáticamente al presionar Enter<br>" +
                                "• Actual: Cantidad en inventario<br>" +
                                "• Mínimo: Stock de seguridad<br>" +
                                "• Máximo: Stock máximo permitido" +
                                "</body></html>",
                                "Tabla de Stock",
                                JOptionPane.INFORMATION_MESSAGE);
                    }
                } else {
                    jTableProductStock.setVisible(false);
                    // Mensaje simple para empleados, detallado para otros usuarios
                    if (isEmployee) {
                        JOptionPane.showMessageDialog(null,
                                "No hay stock",
                                "Sin Stock",
                                JOptionPane.WARNING_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(null,
                                "<html><body style='width: 250px; padding: 10px;'>" +
                                "<b>No hay stock en ubicaciones</b><br><br>" +
                                "El producto seleccionado no tiene stock registrado en ninguna ubicación.<br><br>" +
                                "Por favor agregue el producto a una ubicación primero." +
                                "</body></html>",
                                "Sin Stock",
                                JOptionPane.WARNING_MESSAGE);
                    }
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null,
                        "Error al cargar la información de stock: " + e.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Select a product by its product id and show the stock table for it.
     * If the product is not present in the inventory lines, a new line is added
     * with zero units so that the stock table can be displayed for it.
     * @param productId the product id to select
     */
    public void selectProduct(String productId) {
        if (productId == null) {
            return;
        }

        // try to find a line with that product in the inventory lines
        int indexToSelect = -1;
        for (int i = 0; i < m_invlines.getLines().size(); i++) {
            InventoryLine line = m_invlines.getLine(i);
            if (line.getProductID().equals(productId)) {
                indexToSelect = i;
                break;
            }
        }

        if (indexToSelect >= 0) {
            m_invlines.setSelectedIndex(indexToSelect);
            showStockTable();
            return;
        }

        try {
            ProductInfoExt p = m_dlSales.getProductInfo(productId);
            if (p != null) {
                // Add a new line with zero units so it doesn't affect stock
                addLine(p, 0.0, p.getPriceSell());
                m_invlines.setSelectedIndex(m_invlines.getCount() - 1);
                showStockTable();
            }
        } catch (Exception e) {
            // ignore - if we cannot find the product, nothing to select
        }
    }
    
    /**
     * Actualiza el stock en la base de datos cuando se edita directamente en la tabla
     */
    private void updateStockInDatabase(String productId, String location, Double originalQuantity, 
                                       Double newQuantity, double quantityDifference) throws BasicException {
        
        // Obtener la ubicación actual si no se proporciona
        if (location == null) {
            LocationInfo currentLocation = (LocationInfo) m_LocationsModel.getSelectedItem();
            if (currentLocation != null) {
                location = currentLocation.getID();
            } else {
                location = m_App.getInventoryLocation();
            }
        }
        
        // Obtener usuario y fecha
        String userName = m_App.getAppUserView().getUser().getName();
        Date currentDate = DateUtils.getTodayMinutes();
        
        // Determinar el motivo del movimiento
        Object reasonKey = quantityDifference > 0 
            ? MovementReason.IN_MOVEMENT.getKey() 
            : MovementReason.OUT_MOVEMENT.getKey();
        
        // Actualizar stockcurrent y registrar en stockdiary
        SentenceExec stockDiaryInsert = m_dlSales.getStockDiaryInsert1();
        stockDiaryInsert.exec(new Object[]{
            UUID.randomUUID().toString(),
            currentDate,
            reasonKey,
            location,
            productId,
            null, // ATTRIBUTESETINSTANCE_ID
            quantityDifference, // UNITS (diferencia)
            0.0, // PRICE
            userName, // AppUser
            null, // SUPPLIER
            null  // SUPPLIERDOC
        });
    }
    
    /**
     * Actualiza los valores de mínimo y máximo en stocklevel
     */
    private void updateStockLevelInDatabase(String productId, String locationName, Double minimum, Double maximum) throws BasicException {
        // Obtener el ID de la ubicación desde el nombre
        String locationId = null;
        try {
            List<LocationInfo> locations = m_dlSales.getLocationsList().list();
            for (LocationInfo loc : locations) {
                if (loc.getName().equals(locationName)) {
                    locationId = loc.getID();
                    break;
                }
            }
            
            // Si no se encuentra, usar la ubicación de inventario principal
            if (locationId == null) {
                LocationInfo currentLocation = (LocationInfo) m_LocationsModel.getSelectedItem();
                if (currentLocation != null) {
                    locationId = currentLocation.getID();
                } else {
                    locationId = m_App.getInventoryLocation();
                }
            }
        } catch (BasicException ex) {
            LocationInfo currentLocation = (LocationInfo) m_LocationsModel.getSelectedItem();
            if (currentLocation != null) {
                locationId = currentLocation.getID();
            } else {
                locationId = m_App.getInventoryLocation();
            }
        }
        
        // Manejar null como 0
        if (minimum == null) {
            minimum = 0.0;
        }
        if (maximum == null) {
            maximum = 0.0;
        }
        
        // Verificar si existe una entrada en stocklevel
        try {
            SentenceFind checkStmt = new PreparedSentence(m_dlSales.getSession(),
                "SELECT ID FROM stocklevel WHERE LOCATION = ? AND PRODUCT = ?",
                new SerializerWriteBasicExt(new Datas[]{Datas.STRING, Datas.STRING}, new int[]{0, 1}),
                SerializerReadString.INSTANCE);
            
            String existingId = (String) checkStmt.find(new Object[]{locationId, productId});
            
            if (existingId != null) {
                // UPDATE
                PreparedSentence updateStmt = new PreparedSentence(m_dlSales.getSession(),
                    "UPDATE stocklevel SET STOCKSECURITY = ?, STOCKMAXIMUM = ? WHERE LOCATION = ? AND PRODUCT = ?",
                    new SerializerWriteBasicExt(new Datas[]{Datas.DOUBLE, Datas.DOUBLE, Datas.STRING, Datas.STRING}, new int[]{0, 1, 2, 3}));
                updateStmt.exec(new Object[]{minimum, maximum, locationId, productId});
            } else {
                // INSERT
                PreparedSentence insertStmt = new PreparedSentence(m_dlSales.getSession(),
                    "INSERT INTO stocklevel (ID, LOCATION, PRODUCT, STOCKSECURITY, STOCKMAXIMUM) VALUES (?, ?, ?, ?, ?)",
                    new SerializerWriteBasicExt(new Datas[]{Datas.STRING, Datas.STRING, Datas.STRING, Datas.DOUBLE, Datas.DOUBLE}, new int[]{0, 1, 2, 3, 4}));
                insertStmt.exec(new Object[]{UUID.randomUUID().toString(), locationId, productId, minimum, maximum});
            }
        } catch (BasicException e) {
            // Si falla el check, intentar insertar directamente
            try {
                PreparedSentence insertStmt = new PreparedSentence(m_dlSales.getSession(),
                    "INSERT INTO stocklevel (ID, LOCATION, PRODUCT, STOCKSECURITY, STOCKMAXIMUM) VALUES (?, ?, ?, ?, ?)",
                    new SerializerWriteBasicExt(new Datas[]{Datas.STRING, Datas.STRING, Datas.STRING, Datas.DOUBLE, Datas.DOUBLE}, new int[]{0, 1, 2, 3, 4}));
                insertStmt.exec(new Object[]{UUID.randomUUID().toString(), locationId, productId, minimum, maximum});
            } catch (BasicException ex2) {
                // Si ya existe, actualizar
                PreparedSentence updateStmt = new PreparedSentence(m_dlSales.getSession(),
                    "UPDATE stocklevel SET STOCKSECURITY = ?, STOCKMAXIMUM = ? WHERE LOCATION = ? AND PRODUCT = ?",
                    new SerializerWriteBasicExt(new Datas[]{Datas.DOUBLE, Datas.DOUBLE, Datas.STRING, Datas.STRING}, new int[]{0, 1, 2, 3}));
                updateStmt.exec(new Object[]{minimum, maximum, locationId, productId});
            }
        }
    }

    public void sumStockTable() {
        double totalQty = 0;
        double totalVal = 0;
        double lQty = 0;
        double lVal = 0;

        for (int i = 0; i < stockModel.getRowCount(); i++) {
            totalQty += Double.parseDouble(stockModel.getValueAt(i, 1).toString());
            totalVal += Double.parseDouble(stockModel.getValueAt(i, 5).toString());
// deliberately explicit
            totalVal = Math.round(totalVal * 100);
            totalVal = totalVal / 100;
        }

        int i = m_invlines.getSelectedRow();
        lQty = m_invlines.getLine(i).getMultiply();
        lVal = m_invlines.getLine(i).getPrice() * lQty;
// deliberately explicit
        lVal = Math.round(lVal * 100);
        lVal = lVal / 100;

        MovementReason reason = (MovementReason) m_ReasonModel.getSelectedItem();

        if (reason == MovementReason.OUT_BREAK
                || reason == MovementReason.OUT_FREE || reason == MovementReason.OUT_REFUND
                || reason == MovementReason.OUT_SALE || reason == MovementReason.OUT_SAMPLE
                || reason == MovementReason.OUT_SUBTRACT || reason == MovementReason.OUT_USED) {
            lblTotalQtyValue.setText(Double.toString(totalQty -= lQty));
            lbTotalValue.setText(Double.toString(totalVal -= lVal));
        } else {
            lblTotalQtyValue.setText(Double.toString(lQty += totalQty));
            lbTotalValue.setText(Double.toString(lVal += totalVal));
        }

    }

    private void addUnits(double dUnits) {
        // #region agent log
        try {
            java.io.FileWriter fw = new java.io.FileWriter("c:\\Users\\USUARIO\\Downloads\\bicis_mx\\bici\\punto-mx\\app-errors.log", true);
            fw.write("{\"id\":\"log_" + System.currentTimeMillis() + "_addUnits\",\"timestamp\":" + System.currentTimeMillis() + ",\"location\":\"StockManagement.java:701\",\"message\":\"addUnits called\",\"data\":{\"dUnits\":" + dUnits + ",\"selectedRow\":" + m_invlines.getSelectedRow() + ",\"linesCount\":" + m_invlines.getCount() + "},\"sessionId\":\"debug-session\",\"runId\":\"run1\",\"hypothesisId\":\"A\"}\n");
            fw.close();
            System.out.println("DEBUG: addUnits called - dUnits=" + dUnits + ", selectedRow=" + m_invlines.getSelectedRow() + ", linesCount=" + m_invlines.getCount());
        } catch (Exception ex) {
            System.out.println("DEBUG: Error logging addUnits: " + ex.getMessage());
        }
        // #endregion
        int i = m_invlines.getSelectedRow();
        if (i >= 0) {
            InventoryLine inv = m_invlines.getLine(i);
            double dunits = inv.getMultiply() + dUnits;
            if (dunits <= 0.0) {
                deleteLine(i);
            } else {
                inv.setMultiply(inv.getMultiply() + dUnits);
                m_invlines.setLine(i, inv);
                // #region agent log
                try {
                    java.io.FileWriter fw = new java.io.FileWriter("c:\\Users\\USUARIO\\Downloads\\bicis_mx\\bici\\punto-mx\\app-errors.log", true);
                    fw.write("{\"id\":\"log_" + System.currentTimeMillis() + "_addUnits_success\",\"timestamp\":" + System.currentTimeMillis() + ",\"location\":\"StockManagement.java:709\",\"message\":\"addUnits successful\",\"data\":{\"rowIndex\":" + i + ",\"oldMultiply\":" + (inv.getMultiply() - dUnits) + ",\"newMultiply\":" + inv.getMultiply() + "},\"sessionId\":\"debug-session\",\"runId\":\"run1\",\"hypothesisId\":\"A\"}\n");
                    fw.close();
                } catch (Exception ex) {}
                // #endregion
            }

            sumStockTable();
        } else {
            // #region agent log
            try {
                java.io.FileWriter fw = new java.io.FileWriter("c:\\Users\\USUARIO\\Downloads\\bicis_mx\\bici\\punto-mx\\app-errors.log", true);
                fw.write("{\"id\":\"log_" + System.currentTimeMillis() + "_addUnits_no_selection\",\"timestamp\":" + System.currentTimeMillis() + ",\"location\":\"StockManagement.java:714\",\"message\":\"addUnits failed - no row selected\",\"data\":{\"selectedRow\":" + i + "},\"sessionId\":\"debug-session\",\"runId\":\"run1\",\"hypothesisId\":\"A\"}\n");
                fw.close();
                System.out.println("DEBUG: addUnits failed - no row selected in m_invlines");
            } catch (Exception ex) {}
            // #endregion
        }
    }

    private void setUnits(double dUnits) {
        int i = m_invlines.getSelectedRow();
        if (i >= 0) {
            InventoryLine inv = m_invlines.getLine(i);
            inv.setMultiply(dUnits);
            m_invlines.setLine(i, inv);
        }
    }

    private void stateTransition(char cTrans) {
        if (cTrans == '\n') {
            m_jEnter.doClick();
        }
        if (cTrans == '\u007f') {
            m_jcodebar.setText(null);
            NUMBER_STATE = DEFAULT;
        } else if (cTrans == '*') {
            MULTIPLY = ACTIVE;
        } else if (cTrans == '+') {
            if (MULTIPLY != DEFAULT && NUMBER_STATE != DEFAULT) {
                setUnits(Double.parseDouble(m_jcodebar.getText()));
                m_jcodebar.setText(null);
            } else {
                if (m_jcodebar.getText() == null || m_jcodebar.getText().equals("")) {
                    addUnits(1.0);
                } else {
                    addUnits(Double.parseDouble(m_jcodebar.getText()));
                    m_jcodebar.setText(null);
                }
            }
            NUMBER_STATE = DEFAULT;
            MULTIPLY = DEFAULT;
        } else if (cTrans == '-') {
            if (m_jcodebar.getText() == null || m_jcodebar.getText().equals("")) {
                addUnits(-1.0);
            } else {
                addUnits(-Double.parseDouble(m_jcodebar.getText()));
                m_jcodebar.setText(null);
            }
            NUMBER_STATE = DEFAULT;
            MULTIPLY = DEFAULT;
        } else if (cTrans == '.') {
            if (m_jcodebar.getText() == null || m_jcodebar.getText().equals("")) {
                m_jcodebar.setText("0.");
            } else if (NUMBER_STATE != DECIMAL) {
                m_jcodebar.setText(m_jcodebar.getText() + cTrans);
            }
            NUMBER_STATE = DECIMAL;
        } else if (cTrans == ' ' || cTrans == '=') {
            if (m_invlines.getCount() == 0) {
                Toolkit.getDefaultToolkit().beep();
            } else {
                saveData();
                // jNumberKeys.setEnabled(true); // ELIMINADO - TECLADO NUMERICO
                // Mantener el teclado oculto después de guardar
                // jNumberKeys.setVisible(false); // ELIMINADO - TECLADO NUMERICO
                // jPanel1.setVisible(false); // ELIMINADO - TECLADO NUMERICO
            }
        } else if (Character.isDigit(cTrans)) {
            if (m_jcodebar.getText() == null) {
                m_jcodebar.setText("" + cTrans);
            } else {
                m_jcodebar.setText(m_jcodebar.getText() + cTrans);
            }
            if (NUMBER_STATE != DECIMAL) {
                NUMBER_STATE = ACTIVE;
            }
        } else if (Character.isAlphabetic(cTrans)) {
            if (m_jcodebar.getText() == null) {
                m_jcodebar.setText("" + cTrans);
            } else {
                m_jcodebar.setText(m_jcodebar.getText() + cTrans);
            }
            if (NUMBER_STATE != DECIMAL) {
                NUMBER_STATE = ACTIVE;
            }

        } else {
            Toolkit.getDefaultToolkit().beep();
        }
    }

    /**
     *
     * @param prod
     */
    protected void buttonTransition(ProductInfoExt prod) {

//        if (m_iNumberStatusInput == NUMBERZERO && m_iNumberStatusPor == NUMBERZERO) {
        incProduct(prod);
//        } else {
//            Toolkit.getDefaultToolkit().beep();
//        }      
    }

    private void saveData() {
        // #region agent log
        try {
            java.io.FileWriter fw = new java.io.FileWriter("c:\\Users\\USUARIO\\Downloads\\bicis_mx\\bici\\punto-mx\\app-errors.log", true);
            fw.write("{\"id\":\"log_" + System.currentTimeMillis() + "_saveData\",\"timestamp\":" + System.currentTimeMillis() + ",\"location\":\"StockManagement.java:812\",\"message\":\"saveData called\",\"data\":{\"invlinesCount\":" + m_invlines.getCount() + "},\"sessionId\":\"debug-session\",\"runId\":\"run1\",\"hypothesisId\":\"B\"}\n");
            fw.close();
            System.out.println("DEBUG: saveData called - invlinesCount=" + m_invlines.getCount());
        } catch (Exception ex) {
            System.out.println("DEBUG: Error logging saveData: " + ex.getMessage());
        }
        // #endregion
        try {

            Date d = Formats.TIMESTAMP.parseValue(m_jdate.getText());
            MovementReason reason = (MovementReason) m_ReasonModel.getSelectedItem();

            if (reason == MovementReason.OUT_CROSSING) {
                saveData(new InventoryRecord(
                        d, MovementReason.OUT_MOVEMENT,
                        (LocationInfo) m_LocationsModel.getSelectedItem(),
                        m_App.getAppUserView().getUser().getName(),
                        (SupplierInfo) m_SuppliersModel.getSelectedItem(),
                        m_invlines.getLines(),
                        m_jSupplierDoc.getText()
                ));
                saveData(new InventoryRecord(
                        d, MovementReason.IN_MOVEMENT,
                        (LocationInfo) m_LocationsModelDes.getSelectedItem(),
                        m_App.getAppUserView().getUser().getName(),
                        (SupplierInfo) m_SuppliersModel.getSelectedItem(),
                        m_invlines.getLines(),
                        m_jSupplierDoc.getText()
                ));
            } else {
                saveData(new InventoryRecord(
                        d, reason,
                        (LocationInfo) m_LocationsModel.getSelectedItem(),
                        m_App.getAppUserView().getUser().getName(),
                        (SupplierInfo) m_SuppliersModel.getSelectedItem(),
                        m_invlines.getLines(),
                        m_jSupplierDoc.getText()
                ));
            }

            stateToInsert();
            
            // Recargar productos bajos después de guardar
            loadLowStockProducts();
            // #region agent log
            try {
                java.io.FileWriter fw = new java.io.FileWriter("c:\\Users\\USUARIO\\Downloads\\bicis_mx\\bici\\punto-mx\\app-errors.log", true);
                fw.write("{\"id\":\"log_" + System.currentTimeMillis() + "_saveData_success\",\"timestamp\":" + System.currentTimeMillis() + ",\"location\":\"StockManagement.java:846\",\"message\":\"saveData completed successfully\",\"data\":{},\"sessionId\":\"debug-session\",\"runId\":\"run1\",\"hypothesisId\":\"B\"}\n");
                fw.close();
                System.out.println("DEBUG: saveData completed successfully");
            } catch (Exception ex) {}
            // #endregion
        } catch (BasicException eData) {
            // #region agent log
            try {
                java.io.FileWriter fw = new java.io.FileWriter("c:\\Users\\USUARIO\\Downloads\\bicis_mx\\bici\\punto-mx\\app-errors.log", true);
                fw.write("{\"id\":\"log_" + System.currentTimeMillis() + "_saveData_error\",\"timestamp\":" + System.currentTimeMillis() + ",\"location\":\"StockManagement.java:850\",\"message\":\"saveData error\",\"data\":{\"error\":\"" + eData.getMessage() + "\"},\"sessionId\":\"debug-session\",\"runId\":\"run1\",\"hypothesisId\":\"B\"}\n");
                fw.close();
                System.out.println("DEBUG: saveData error: " + eData.getMessage());
            } catch (Exception ex) {}
            // #endregion
            MessageInf msg = new MessageInf(MessageInf.SGN_NOTICE,
                    AppLocal.getIntString("message.cannotsaveinventorydata"), eData);
            msg.show(this);
        }
    }

    private void saveData(InventoryRecord rec) throws BasicException {

        SentenceExec sent = m_dlSales.getStockDiaryInsert1();

        for (int i = 0; i < m_invlines.getCount(); i++) {
            InventoryLine inv = rec.getLines().get(i);

            sent.exec(new Object[]{
                UUID.randomUUID().toString(),
                rec.getDate(),
                rec.getReason().getKey(),
                rec.getLocation().getID(),
                inv.getProductID(),
                inv.getProductAttSetInstId(),
                rec.getReason().samesignum(inv.getMultiply()),
                inv.getPrice(),
                rec.getUser(),
                rec.getSupplier().getId(),
                rec.getSupplierDoc()
            });
        }

        clearStockTable();
        printTicket(rec);
    }

    private void printTicket(InventoryRecord invrec) {

        String sresource = m_dlSystem.getResourceAsXML("Printer.Inventory");
        if (sresource == null) {
            MessageInf msg = new MessageInf(MessageInf.SGN_WARNING,
                    AppLocal.getIntString("message.cannotprintticket"));
            msg.show(this);
        } else {
            try {
                ScriptEngine script = ScriptFactory.getScriptEngine(ScriptFactory.VELOCITY);
                script.put("inventoryrecord", invrec);
                m_TTP.printTicket(script.eval(sresource).toString());

            } catch (ScriptException | TicketPrinterException e) {
                MessageInf msg = new MessageInf(MessageInf.SGN_WARNING,
                        AppLocal.getIntString("message.cannotprintticket"), e);
                msg.show(this);
            }
        }
    }

    class ProductStockTableModel extends AbstractTableModel {

        private static final long serialVersionUID = 1L;

        private String loc = AppLocal.getIntString("label.tblProdHeaderCol1");
        private String qty = AppLocal.getIntString("label.tblProdHeaderCol2");
        private String max = AppLocal.getIntString("label.tblProdHeaderCol3");
        private String min = AppLocal.getIntString("label.tblProdHeaderCol4");
        private String buy = AppLocal.getIntString("label.tblProdHeaderCol5");
        private String val = AppLocal.getIntString("label.tblProdHeaderCol6");

        private List<ProductStock> stockList;
        private String currentProductId; // Guardar el ID del producto actual

        String[] columnNames = {loc, qty, max, min, buy, val};

        public ProductStockTableModel(List<ProductStock> list) {
            stockList = list;
            // Guardar el ID del producto si hay elementos
            if (list != null && !list.isEmpty()) {
                currentProductId = list.get(0).getProductId();
            }
        }

        @Override
        public int getColumnCount() {
            return 6;
        }

        @Override
        public int getRowCount() {
            return stockList.size();
        }

        @Override
        public Object getValueAt(int row, int column) {
            ProductStock productStock = stockList.get(row);

            switch (column) {
                case 0:
                    return productStock.getLocation();
                case 1:
                    return productStock.getUnits();
                case 2:
                    return productStock.getMinimum();
                case 3:
                    return productStock.getMaximum();
                case 4:
                    return productStock.getPriceSell();
                case 5:
                    return productStock.getUnits() * productStock.getPriceSell();
                case 6:
                    return productStock.getProductId();
                default:
                    return "";
            }

        }

        @Override
        public void setValueAt(Object value, int row, int column) {
            ProductStock productStock = stockList.get(row);
            
            try {
                // Convertir el valor a Double
                Double newValue;
                if (value instanceof Double) {
                    newValue = (Double) value;
                } else if (value instanceof String) {
                    String strValue = ((String) value).trim();
                    newValue = strValue.isEmpty() ? 0.0 : Double.parseDouble(strValue);
                } else {
                    newValue = Double.parseDouble(value.toString());
                }
                
                // Columna 1: Cantidad actual (Current)
                if (column == 1) {
                    Double originalQuantity = productStock.getUnits();
                    // #region agent log
                    try {
                        java.io.FileWriter fw = new java.io.FileWriter("c:\\Users\\USUARIO\\Downloads\\bicis_mx\\bici\\punto-mx\\app-errors.log", true);
                        fw.write("{\"id\":\"log_" + System.currentTimeMillis() + "_setValueAt_qty\",\"timestamp\":" + System.currentTimeMillis() + ",\"location\":\"StockManagement.java:980\",\"message\":\"setValueAt quantity\",\"data\":{\"row\":" + row + ",\"originalQuantity\":" + originalQuantity + ",\"newValue\":" + newValue + ",\"productId\":\"" + productStock.getProductId() + "\",\"location\":\"" + productStock.getLocation() + "\"},\"sessionId\":\"debug-session\",\"runId\":\"run1\",\"hypothesisId\":\"C\"}\n");
                        fw.close();
                        System.out.println("DEBUG: setValueAt quantity - row=" + row + ", original=" + originalQuantity + ", new=" + newValue);
                    } catch (Exception ex) {}
                    // #endregion
                    
                    // Solo actualizar si hay cambio
                    if (originalQuantity != null && !originalQuantity.equals(newValue)) {
                        double quantityDifference = newValue - originalQuantity;
                        
                        if (Math.abs(quantityDifference) > 0.0001) {
                            // Actualizar el modelo
                            productStock.setUnits(newValue);
                            fireTableCellUpdated(row, column);
                            fireTableCellUpdated(row, 5); // Actualizar también la columna de valor
                            
                            // Actualizar en la base de datos
                            updateStockInDatabase(
                                productStock.getProductId(),
                                productStock.getLocation(),
                                originalQuantity,
                                newValue,
                                quantityDifference
                            );
                            // #region agent log
                            try {
                                java.io.FileWriter fw = new java.io.FileWriter("c:\\Users\\USUARIO\\Downloads\\bicis_mx\\bici\\punto-mx\\app-errors.log", true);
                                fw.write("{\"id\":\"log_" + System.currentTimeMillis() + "_setValueAt_qty_saved\",\"timestamp\":" + System.currentTimeMillis() + ",\"location\":\"StockManagement.java:1000\",\"message\":\"setValueAt quantity saved to DB\",\"data\":{\"quantityDifference\":" + quantityDifference + "},\"sessionId\":\"debug-session\",\"runId\":\"run1\",\"hypothesisId\":\"C\"}\n");
                                fw.close();
                            } catch (Exception ex) {}
                            // #endregion
                            
                            // Actualizar totales
                            sumStockTable();
                        }
                    }
                }
                // Columna 2: Mínimo (Minimum)
                else if (column == 2) {
                    Double originalMinimum = productStock.getMinimum();
                    
                    if (originalMinimum == null || !originalMinimum.equals(newValue)) {
                        // Actualizar el modelo
                        productStock.setMinimum(newValue);
                        fireTableCellUpdated(row, column);
                        
                        // Actualizar en la base de datos
                        updateStockLevelInDatabase(
                            productStock.getProductId(),
                            productStock.getLocation(),
                            newValue,
                            productStock.getMaximum()
                        );
                    }
                }
                // Columna 3: Máximo (Maximum)
                else if (column == 3) {
                    Double originalMaximum = productStock.getMaximum();
                    
                    if (originalMaximum == null || !originalMaximum.equals(newValue)) {
                        // Actualizar el modelo
                        productStock.setMaximum(newValue);
                        fireTableCellUpdated(row, column);
                        
                        // Actualizar en la base de datos
                        updateStockLevelInDatabase(
                            productStock.getProductId(),
                            productStock.getLocation(),
                            productStock.getMinimum(),
                            newValue
                        );
                    }
                }
                
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(StockManagement.this,
                    AppLocal.getIntString("message.invalidnumber"),
                    AppLocal.getIntString("message.title"),
                    JOptionPane.ERROR_MESSAGE);
            } catch (BasicException e) {
                MessageInf msg = new MessageInf(MessageInf.SGN_WARNING,
                    AppLocal.getIntString("message.cannotsaveinventorydata"), e);
                msg.show(StockManagement.this);
            }
        }
        
        @Override
        public boolean isCellEditable(int row, int column) {
            // Las columnas de cantidad (1), mínimo (2) y máximo (3) son editables
            return column == 1 || column == 2 || column == 3;
        }
        
        @Override
        public Class<?> getColumnClass(int column) {
            if (column == 1 || column == 2 || column == 3 || column == 4 || column == 5) {
                return Double.class;
            }
            return String.class;
        }

        @Override
        public String getColumnName(int col) {
            return columnNames[col];
        }

    }

    // Catálogo deshabilitado - ya no se usa
    /*
    private class CatalogListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {

            String sQty = m_jcodebar.getText();
            if (sQty != null) {
                Double dQty = (Double.valueOf(sQty) == 0) ? 1.0 : Double.valueOf(sQty);
                incProduct((ProductInfoExt) e.getSource(), dQty);
                m_jcodebar.setText(null);
            } else {
                incProduct((ProductInfoExt) e.getSource(), 1.0);
            }
        }
    }
    */

    private void removeInvLine(int index) {

        if (index < 0) {
            Toolkit.getDefaultToolkit().beep();
        } else {
            m_invlines.deleteLine(index);
            clearStockTable();
            showStockTable();

        }

    }

    /**
     *
     * @param index
     */
    public void deleteTicket(int index) {

        while (index < m_invlines.getCount()) {
            m_invlines.deleteLine(index);
        }

    }

    private void incProduct(ProductInfoExt prod) {

        incProduct(1.0, prod);
    }

    private void incProduct(double dPor, ProductInfoExt prod) {

        addLine(prod, dPor, prod.getPriceBuy());
    }

    private void stateToZero() {
        m_sBarcode = new StringBuffer();

        m_iNumberStatus = NUMBER_INPUTZERO;
        m_iNumberStatusInput = NUMBERZERO;
        m_iNumberStatusPor = NUMBERZERO;
        repaint();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel8 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        m_jdate = new javax.swing.JTextField();
        m_jbtndate = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        m_jreason = new javax.swing.JComboBox();
        jLabel8 = new javax.swing.JLabel();
        m_jLocation = new javax.swing.JComboBox();
        m_jLocationDes = new javax.swing.JComboBox();
        jLabel10 = new javax.swing.JLabel();
        m_jSupplier = new javax.swing.JComboBox();
        jLabel9 = new javax.swing.JLabel();
        m_jSupplierDoc = new javax.swing.JTextField();
        jPanel5 = new javax.swing.JPanel();
        m_jcodebar = new javax.swing.JLabel();
        m_jEnter = new javax.swing.JButton();
        jTextField1 = new javax.swing.JTextField();
        jPanel2 = new javax.swing.JPanel();
        m_jDelete = new javax.swing.JButton();
        m_jList = new javax.swing.JButton();
        m_jEditLine = new javax.swing.JButton();
        m_jEditAttributes = new javax.swing.JButton();
        m_jBtnDelete = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        // PANEL TECLADO COMPLETAMENTE DESHABILITADO Y OCULTO
        jPanel1.setVisible(false);
        jPanel1.setSize(0, 0);
        jPanel1.setPreferredSize(new java.awt.Dimension(0, 0));
        jPanel1.setMinimumSize(new java.awt.Dimension(0, 0));
        jPanel1.setMaximumSize(new java.awt.Dimension(0, 0));
        // jNumberKeys = new com.openbravo.beans.JNumberKeys(); // ELIMINADO - TECLADO NUMERICO
        jScrollPane2 = new javax.swing.JScrollPane();
        jTableProductStock = new javax.swing.JTable();
        m_jBtnShowStock = new javax.swing.JButton();
        lblTotalQtyValue = new javax.swing.JLabel();
        lbTotalValue = new javax.swing.JLabel();
        webLblQty = new javax.swing.JLabel();
        webLblValue = new javax.swing.JLabel();
        catcontainer = new javax.swing.JPanel();
        jScrollPaneLowStock = new javax.swing.JScrollPane();
        jTableLowStock = new javax.swing.JTable();
        jLabelLowStock = new javax.swing.JLabel();

        setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        setMinimumSize(new java.awt.Dimension(550, 250));
        setPreferredSize(new java.awt.Dimension(1000, 350));
        setLayout(new java.awt.BorderLayout());

        jPanel8.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        jPanel8.setMinimumSize(new java.awt.Dimension(0, 320));
        jPanel8.setPreferredSize(new java.awt.Dimension(0, 320));
        jPanel8.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel1.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel1.setText(AppLocal.getIntString("label.stockdate")); // NOI18N
        jLabel1.setMaximumSize(new java.awt.Dimension(40, 25));
        jLabel1.setMinimumSize(new java.awt.Dimension(40, 25));
        jLabel1.setPreferredSize(new java.awt.Dimension(70, 30));
        jPanel8.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(5, 5, -1, -1));

        m_jdate.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        m_jdate.setPreferredSize(new java.awt.Dimension(160, 30));
        jPanel8.add(m_jdate, new org.netbeans.lib.awtextra.AbsoluteConstraints(90, 5, -1, -1));

        m_jbtndate.setIcon(new ModernActionIcon(ModernActionIcon.Type.CALENDAR, 20));
        m_jbtndate.setToolTipText("Open Calendar");
        m_jbtndate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jbtndateActionPerformed(evt);
            }
        });
        jPanel8.add(m_jbtndate, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 5, 40, 30));

        jLabel2.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel2.setText(AppLocal.getIntString("label.stockreason")); // NOI18N
        jLabel2.setMaximumSize(new java.awt.Dimension(40, 25));
        jLabel2.setMinimumSize(new java.awt.Dimension(40, 25));
        jLabel2.setPreferredSize(new java.awt.Dimension(70, 30));
        jPanel8.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(5, 40, -1, -1));

        m_jreason.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        m_jreason.setMaximumRowCount(13);
        m_jreason.setPreferredSize(new java.awt.Dimension(160, 30));
        m_jreason.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jreasonActionPerformed(evt);
            }
        });
        jPanel8.add(m_jreason, new org.netbeans.lib.awtextra.AbsoluteConstraints(90, 40, -1, -1));

        jLabel8.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel8.setText(AppLocal.getIntString("label.locationplace")); // NOI18N
        jLabel8.setMaximumSize(new java.awt.Dimension(40, 25));
        jLabel8.setMinimumSize(new java.awt.Dimension(40, 25));
        jLabel8.setPreferredSize(new java.awt.Dimension(70, 30));
        jPanel8.add(jLabel8, new org.netbeans.lib.awtextra.AbsoluteConstraints(5, 75, -1, -1));

        m_jLocation.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        m_jLocation.setPreferredSize(new java.awt.Dimension(160, 30));
        m_jLocation.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jLocationActionPerformed(evt);
            }
        });
        jPanel8.add(m_jLocation, new org.netbeans.lib.awtextra.AbsoluteConstraints(90, 75, -1, -1));

        m_jLocationDes.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        m_jLocationDes.setPreferredSize(new java.awt.Dimension(160, 30));
        jPanel8.add(m_jLocationDes, new org.netbeans.lib.awtextra.AbsoluteConstraints(90, 110, -1, -1));

        jLabel10.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel10.setText(AppLocal.getIntString("label.supplier")); // NOI18N
        jLabel10.setMaximumSize(new java.awt.Dimension(40, 25));
        jLabel10.setMinimumSize(new java.awt.Dimension(40, 25));
        jLabel10.setPreferredSize(new java.awt.Dimension(70, 30));
        jPanel8.add(jLabel10, new org.netbeans.lib.awtextra.AbsoluteConstraints(5, 145, -1, -1));

        m_jSupplier.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        m_jSupplier.setPreferredSize(new java.awt.Dimension(160, 30));
        m_jSupplier.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jSupplierActionPerformed(evt);
            }
        });
        jPanel8.add(m_jSupplier, new org.netbeans.lib.awtextra.AbsoluteConstraints(90, 145, -1, -1));

        jLabel9.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel9.setText(AppLocal.getIntString("label.supplierdocment")); // NOI18N
        jLabel9.setToolTipText("null");
        jLabel9.setMaximumSize(new java.awt.Dimension(40, 25));
        jLabel9.setMinimumSize(new java.awt.Dimension(40, 25));
        jLabel9.setPreferredSize(new java.awt.Dimension(70, 30));
        jPanel8.add(jLabel9, new org.netbeans.lib.awtextra.AbsoluteConstraints(5, 180, -1, -1));

        m_jSupplierDoc.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        m_jSupplierDoc.setToolTipText(AppLocal.getIntString("button.exit")); // NOI18N
        m_jSupplierDoc.setPreferredSize(new java.awt.Dimension(160, 30));
        jPanel8.add(m_jSupplierDoc, new org.netbeans.lib.awtextra.AbsoluteConstraints(90, 180, -1, -1));

        jPanel5.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(204, 204, 204)));
        jPanel5.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        jPanel5.setMinimumSize(new java.awt.Dimension(455, 245));
        jPanel5.setPreferredSize(new java.awt.Dimension(455, 245));
        jPanel5.setLayout(new java.awt.BorderLayout());
        jPanel8.add(jPanel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(250, 5, -1, 190));

        m_jcodebar.setBackground(java.awt.Color.white);
        m_jcodebar.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
        m_jcodebar.setForeground(new java.awt.Color(76, 197, 237));
        m_jcodebar.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        m_jcodebar.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)), javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1)));
        m_jcodebar.setOpaque(true);
        m_jcodebar.setPreferredSize(new java.awt.Dimension(130, 25));
        m_jcodebar.setRequestFocusEnabled(false);
        m_jcodebar.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                m_jcodebarMouseClicked(evt);
            }
        });
        jPanel8.add(m_jcodebar, new org.netbeans.lib.awtextra.AbsoluteConstraints(780, 270, -1, -1));

        m_jEnter.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        m_jEnter.setIcon(new ModernActionIcon(ModernActionIcon.Type.SEARCH, 20));
        m_jEnter.setFocusPainted(false);
        m_jEnter.setFocusable(false);
        m_jEnter.setPreferredSize(new java.awt.Dimension(54, 45));
        m_jEnter.setRequestFocusEnabled(false);
        m_jEnter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jEnterActionPerformed(evt);
            }
        });
        jPanel8.add(m_jEnter, new org.netbeans.lib.awtextra.AbsoluteConstraints(920, 260, -1, -1));

        jTextField1.setBackground(javax.swing.UIManager.getDefaults().getColor("Panel.background"));
        jTextField1.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jTextField1.setForeground(new java.awt.Color(255, 255, 255));
        jTextField1.setCaretColor(javax.swing.UIManager.getDefaults().getColor("Panel.background"));
        jTextField1.setPreferredSize(new java.awt.Dimension(1, 1));
        jTextField1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                jTextField1KeyTyped(evt);
            }
        });
        jPanel8.add(jTextField1, new org.netbeans.lib.awtextra.AbsoluteConstraints(1, 1, -1, 0));

        jPanel2.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 5, 0, 5));
        jPanel2.setPreferredSize(new java.awt.Dimension(70, 250));
        jPanel2.setLayout(new java.awt.GridLayout(0, 1, 5, 5));

        m_jDelete.setIcon(new ModernActionIcon(ModernActionIcon.Type.DELETE, 20));
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("pos_messages"); // NOI18N
        m_jDelete.setToolTipText(bundle.getString("tooltip.saleremoveline")); // NOI18N
        m_jDelete.setFocusPainted(false);
        m_jDelete.setFocusable(false);
        m_jDelete.setMargin(new java.awt.Insets(8, 14, 8, 14));
        m_jDelete.setMaximumSize(new java.awt.Dimension(42, 36));
        m_jDelete.setMinimumSize(new java.awt.Dimension(42, 36));
        m_jDelete.setPreferredSize(new java.awt.Dimension(50, 45));
        m_jDelete.setRequestFocusEnabled(false);
        m_jDelete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jDeleteActionPerformed(evt);
            }
        });
        jPanel2.add(m_jDelete);

        m_jList.setIcon(new ModernActionIcon(ModernActionIcon.Type.SEARCH, 20));
        m_jList.setToolTipText(bundle.getString("tooltip.saleproductfind")); // NOI18N
        m_jList.setFocusPainted(false);
        m_jList.setFocusable(false);
        m_jList.setMargin(new java.awt.Insets(8, 14, 8, 14));
        m_jList.setMaximumSize(new java.awt.Dimension(42, 36));
        m_jList.setMinimumSize(new java.awt.Dimension(42, 36));
        m_jList.setPreferredSize(new java.awt.Dimension(50, 45));
        m_jList.setRequestFocusEnabled(false);
        m_jList.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jListActionPerformed(evt);
            }
        });
        jPanel2.add(m_jList);

        m_jEditLine.setIcon(new ModernActionIcon(ModernActionIcon.Type.EDIT, 20));
        m_jEditLine.setToolTipText(bundle.getString("tooltip.saleeditline")); // NOI18N
        m_jEditLine.setFocusPainted(false);
        m_jEditLine.setFocusable(false);
        m_jEditLine.setMargin(new java.awt.Insets(8, 14, 8, 14));
        m_jEditLine.setMaximumSize(new java.awt.Dimension(42, 36));
        m_jEditLine.setMinimumSize(new java.awt.Dimension(42, 36));
        m_jEditLine.setPreferredSize(new java.awt.Dimension(50, 45));
        m_jEditLine.setRequestFocusEnabled(false);
        m_jEditLine.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jEditLineActionPerformed(evt);
            }
        });
        jPanel2.add(m_jEditLine);

        m_jEditAttributes.setIcon(new ModernActionIcon(ModernActionIcon.Type.DOCUMENT, 20));
        m_jEditAttributes.setToolTipText(bundle.getString("tooltip.saleattributes")); // NOI18N
        m_jEditAttributes.setFocusPainted(false);
        m_jEditAttributes.setFocusable(false);
        m_jEditAttributes.setMargin(new java.awt.Insets(8, 14, 8, 14));
        m_jEditAttributes.setMaximumSize(new java.awt.Dimension(42, 36));
        m_jEditAttributes.setMinimumSize(new java.awt.Dimension(42, 36));
        m_jEditAttributes.setPreferredSize(new java.awt.Dimension(50, 45));
        m_jEditAttributes.setRequestFocusEnabled(false);
        m_jEditAttributes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jEditAttributesActionPerformed(evt);
            }
        });
        jPanel2.add(m_jEditAttributes);

        m_jBtnDelete.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        m_jBtnDelete.setIcon(new ModernActionIcon(ModernActionIcon.Type.DELETE, 20));
        m_jBtnDelete.setText(AppLocal.getIntString("button.deleteticket")); // NOI18N
        m_jBtnDelete.setToolTipText("Delete current Ticket");
        m_jBtnDelete.setFocusPainted(false);
        m_jBtnDelete.setFocusable(false);
        m_jBtnDelete.setMargin(new java.awt.Insets(0, 4, 0, 4));
        m_jBtnDelete.setMaximumSize(new java.awt.Dimension(50, 40));
        m_jBtnDelete.setMinimumSize(new java.awt.Dimension(50, 40));
        m_jBtnDelete.setPreferredSize(new java.awt.Dimension(80, 45));
        m_jBtnDelete.setRequestFocusEnabled(false);
        m_jBtnDelete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jBtnDeleteActionPerformed(evt);
            }
        });
        jPanel2.add(m_jBtnDelete);

        jPanel8.add(jPanel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(705, 0, -1, -1));

        // PANEL TECLADO COMPLETAMENTE DESHABILITADO
        // jPanel1.setMinimumSize(new java.awt.Dimension(150, 250));
        // jPanel1.setPreferredSize(new java.awt.Dimension(200, 250));

        // jNumberKeys.setPreferredSize(new java.awt.Dimension(210, 240)); // ELIMINADO - TECLADO NUMERICO
        // jNumberKeys.addJNumberEventListener(new com.openbravo.beans.JNumberEventListener() { // ELIMINADO - TECLADO NUMERICO
        //     public void keyPerformed(com.openbravo.beans.JNumberEvent evt) {
        //         jNumberKeysKeyPerformed(evt);
        //     }
        // });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 230, Short.MAX_VALUE)
            // .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING) // ELIMINADO - TECLADO NUMERICO
            //     .addGroup(jPanel1Layout.createSequentialGroup()
            //         .addGap(0, 10, Short.MAX_VALUE)
            //         .addComponent(jNumberKeys, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            //         .addGap(0, 10, Short.MAX_VALUE)))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 260, Short.MAX_VALUE)
            // .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING) // ELIMINADO - TECLADO NUMERICO
            //     .addGroup(jPanel1Layout.createSequentialGroup()
            //         .addGap(0, 0, Short.MAX_VALUE)
            //         .addComponent(jNumberKeys, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            //         .addGap(0, 0, Short.MAX_VALUE)))
        );

        // jPanel8.add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(760, 0, 230, 260)); // OCULTAR TECLADO NUMERICO

        jScrollPane2.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N

        jTableProductStock.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jTableProductStock.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null}
            },
            new String [] {
                "Location", "Current", "Maximum", "Minimum", "PriceSell", "PriceValue"
            }
        ));
        jTableProductStock.setRowHeight(25);
        jScrollPane2.setViewportView(jTableProductStock);

        jPanel8.add(jScrollPane2, new org.netbeans.lib.awtextra.AbsoluteConstraints(5, 220, 650, 70));

        m_jBtnShowStock.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        m_jBtnShowStock.setIcon(new ModernActionIcon(ModernActionIcon.Type.VIEW, 20));
        m_jBtnShowStock.setToolTipText(AppLocal.getIntString("tooltip.salecheckstock")); // NOI18N
        m_jBtnShowStock.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jBtnShowStockActionPerformed(evt);
            }
        });
        jPanel8.add(m_jBtnShowStock, new org.netbeans.lib.awtextra.AbsoluteConstraints(660, 250, 40, 40));

        lblTotalQtyValue.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        lblTotalQtyValue.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblTotalQtyValue.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        lblTotalQtyValue.setPreferredSize(new java.awt.Dimension(100, 30));
        jPanel8.add(lblTotalQtyValue, new org.netbeans.lib.awtextra.AbsoluteConstraints(118, 290, -1, -1));

        lbTotalValue.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        lbTotalValue.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lbTotalValue.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        lbTotalValue.setPreferredSize(new java.awt.Dimension(100, 30));
        jPanel8.add(lbTotalValue, new org.netbeans.lib.awtextra.AbsoluteConstraints(547, 290, -1, -1));

        webLblQty.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        webLblQty.setText(AppLocal.getIntString("label.stock.quantity")); // NOI18N
        webLblQty.setFont(new java.awt.Font("Arial", 1, 12)); // NOI18N
        webLblQty.setPreferredSize(new java.awt.Dimension(90, 30));
        jPanel8.add(webLblQty, new org.netbeans.lib.awtextra.AbsoluteConstraints(5, 290, 100, -1));

        webLblValue.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        webLblValue.setText(AppLocal.getIntString("label.stock.value")); // NOI18N
        webLblValue.setFont(new java.awt.Font("Arial", 1, 12)); // NOI18N
        webLblValue.setPreferredSize(new java.awt.Dimension(180, 30));
        jPanel8.add(webLblValue, new org.netbeans.lib.awtextra.AbsoluteConstraints(355, 290, 170, -1));

        add(jPanel8, java.awt.BorderLayout.PAGE_START);

        // Panel para productos bajos - PRINCIPAL
        jLabelLowStock.setFont(new java.awt.Font("Arial", 1, 16)); // NOI18N
        jLabelLowStock.setText("GESTIÓN DE STOCK");
        jLabelLowStock.setPreferredSize(new java.awt.Dimension(400, 30));
        jLabelLowStock.setForeground(new java.awt.Color(51, 51, 51));
        
        jTableLowStock.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        jTableLowStock.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
            },
            new String [] {
                "Producto", "Código/SKU", "Ubicación", "Stock Actual", "Mínimo", "Máximo", "Estado", "Acciones"
            }
        ));
        jTableLowStock.setRowHeight(48);
        jTableLowStock.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jTableLowStock.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTableLowStockMouseClicked(evt);
            }
        });
        jScrollPaneLowStock.setViewportView(jTableLowStock);
        
        // Panel contenedor para productos bajos - OCUPA LA MAYOR PARTE
        lowStockPanel = new RoundedPanel(16, java.awt.Color.WHITE);
        lowStockPanel.setLayout(new java.awt.BorderLayout());
        lowStockPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        
        lowStockCardLayout = new java.awt.CardLayout();
        lowStockCardPanel = new javax.swing.JPanel(lowStockCardLayout);
        lowStockCardPanel.setOpaque(false);
        
        emptyStatePanel = new EmptyStatePanel();
        
        lowStockCardPanel.add(jScrollPaneLowStock, "TABLE");
        lowStockCardPanel.add(emptyStatePanel, "EMPTY");
        
        // Build the header area (title + search bar)
        javax.swing.JPanel headerArea = buildHeaderArea();
        // Build the alert banner
        alertLabel = new javax.swing.JLabel();
        javax.swing.JPanel alertBanner = buildAlertBanner();
        // Build pagination
        paginationPanel = buildPaginationPanel();

        // Combine header + alert into a top wrapper
        javax.swing.JPanel topWrapper = new javax.swing.JPanel();
        topWrapper.setLayout(new BoxLayout(topWrapper, BoxLayout.Y_AXIS));
        topWrapper.setOpaque(false);
        topWrapper.add(headerArea);
        topWrapper.add(alertBanner);
        
        lowStockPanel.add(topWrapper, java.awt.BorderLayout.NORTH);
        lowStockPanel.add(lowStockCardPanel, java.awt.BorderLayout.CENTER);
        lowStockPanel.add(paginationPanel, java.awt.BorderLayout.SOUTH);
        
        // Ocultar el catálogo de categorías para dar más espacio
        catcontainer.setVisible(false);
        catcontainer.setPreferredSize(new java.awt.Dimension(0, 0));
        catcontainer.setMinimumSize(new java.awt.Dimension(0, 0));
        catcontainer.setMaximumSize(new java.awt.Dimension(0, 0));
        
        // Panel principal - productos bajos ocupan todo el espacio
        javax.swing.JPanel mainPanel = new javax.swing.JPanel();
        mainPanel.setBackground(new java.awt.Color(250, 247, 242));
        mainPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(18, 18, 18, 18));
        mainPanel.setLayout(new java.awt.BorderLayout());
        mainPanel.add(lowStockPanel, java.awt.BorderLayout.CENTER);
        
        catcontainer.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        catcontainer.setRequestFocusEnabled(false);
        catcontainer.setLayout(new java.awt.BorderLayout());
        add(mainPanel, java.awt.BorderLayout.CENTER);
        catcontainer.getAccessibleContext().setAccessibleParent(jPanel8);
        
        // Ocultar el teclado numérico al final de la inicialización
        // jNumberKeys.setVisible(false); // ELIMINADO - TECLADO NUMERICO
        // jPanel1.setVisible(false); // ELIMINADO - TECLADO NUMERICO
    }// </editor-fold>//GEN-END:initComponents

    private void jTextField1KeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextField1KeyTyped
        jTextField1.setText(null);
        stateTransition(evt.getKeyChar());
    }//GEN-LAST:event_jTextField1KeyTyped

    private void jNumberKeysKeyPerformed(com.openbravo.beans.JNumberEvent evt) {//GEN-FIRST:event_jNumberKeysKeyPerformed

        stateTransition(evt.getKey());

    }//GEN-LAST:event_jNumberKeysKeyPerformed


    private void m_jreasonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_jreasonActionPerformed

        m_jLocationDes.setEnabled(m_ReasonModel.getSelectedItem() == MovementReason.OUT_CROSSING);

    }//GEN-LAST:event_m_jreasonActionPerformed

    private void m_jbtndateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_jbtndateActionPerformed

        Date date;
        try {
            date = (Date) Formats.TIMESTAMP.parseValue(m_jdate.getText());
        } catch (BasicException e) {
            date = null;
        }
        date = JCalendarDialog.showCalendarTime(this, date);
        if (date != null) {
            m_jdate.setText(Formats.TIMESTAMP.formatValue(date));
        }
    }//GEN-LAST:event_m_jbtndateActionPerformed

    private void m_jEnterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_jEnterActionPerformed

        incProductByCode(m_jcodebar.getText());
        m_jcodebar.setText(null);
        if (m_jSupplier.getSelectedItem() != null) {
//            saveData();
        } else {
            JOptionPane.showMessageDialog(null,
                    AppLocal.getIntString("message.supplierinvalid"),
                    AppLocal.getIntString("message.title.supplierinvalid"),
                    JOptionPane.WARNING_MESSAGE);
        }
    }//GEN-LAST:event_m_jEnterActionPerformed

    private void m_jSupplierActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_jSupplierActionPerformed
        /*        if (m_jSupplier != null) {
            catcontainer.setEnabled(false);            
            jNumberKeys.setEnabled(true);
            m_jcodebar.setEnabled(true);
            m_jEnter.setEnabled(true);           
            m_jSupplierDoc.setEnabled(true); 
            m_jcodebar.setEnabled(true);
            m_jEditLine.setEnabled(true);
            m_jEditAttributes.setEnabled(true);
            m_jBtnShowStock.setEnabled(true);
        } else {
            catcontainer.setEnabled(true);            
            jNumberKeys.setEnabled(false);
            m_jcodebar.setEnabled(false);
            m_jEnter.setEnabled(false);
            m_jSupplierDoc.setEnabled(false);             
            m_jcodebar.setEnabled(false);            
            m_jEditLine.setEnabled(false);
            m_jEditAttributes.setEnabled(false);
            m_jBtnShowStock.setEnabled(false);
            
       }
         */
    }//GEN-LAST:event_m_jSupplierActionPerformed

    private void m_jBtnShowStockActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_jBtnShowStockActionPerformed

        showStockTable();

    }//GEN-LAST:event_m_jBtnShowStockActionPerformed

    private void m_jDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_jDeleteActionPerformed

        int i = m_invlines.getSelectedRow();

        if (i < 0) {
            Toolkit.getDefaultToolkit().beep();
        } else {
            removeInvLine(i);

        }
    }//GEN-LAST:event_m_jDeleteActionPerformed

    private void m_jListActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_jListActionPerformed

        ProductInfoExt prod = JProductFinder.showMessage(StockManagement.this, m_dlSales);
        if (prod != null) {
            buttonTransition(prod);
        }

    }//GEN-LAST:event_m_jListActionPerformed

    private void m_jEditLineActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_jEditLineActionPerformed

        int i = m_invlines.getSelectedRow();
        if (i < 0) {
            Toolkit.getDefaultToolkit().beep();
        } else {
            InventoryLine line = m_invlines.getLine(i);

            JFrame frame = new JFrame("New Price Buy");
            String spricebuy = JOptionPane.showInputDialog(frame,
                    AppLocal.getIntString("message.enterbuyprice"),
                    JOptionPane.INFORMATION_MESSAGE);

            if (spricebuy != null) {
                double dpricebuy = Double.parseDouble(spricebuy);
                line.setPrice(dpricebuy);
                m_invlines.setLine(i, line);
            }
        }
    }//GEN-LAST:event_m_jEditLineActionPerformed

    private void m_jEditAttributesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_jEditAttributesActionPerformed

        int i = m_invlines.getSelectedRow();
        if (i < 0) {
            Toolkit.getDefaultToolkit().beep();
        } else {
            try {
                InventoryLine line = m_invlines.getLine(i);
                JProductAttEdit2 attedit = JProductAttEdit2.getAttributesEditor(this, m_App.getSession());
                attedit.editAttributes(line.getProductAttSetId(), line.getProductAttSetInstId());
                attedit.setVisible(true);
                if (attedit.isOK()) {
                    line.setProductAttSetInstId(attedit.getAttributeSetInst());
                    line.setProductAttSetInstDesc(attedit.getAttributeSetInstDescription());
                    m_invlines.setLine(i, line);
                }
            } catch (BasicException ex) {
                MessageInf msg = new MessageInf(MessageInf.SGN_WARNING,
                        AppLocal.getIntString("message.cannotfindattributes"), ex);
                msg.show(this);
            }
        }
    }//GEN-LAST:event_m_jEditAttributesActionPerformed

    private void m_jBtnDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_jBtnDeleteActionPerformed

        int res = JOptionPane.showConfirmDialog(this,
                AppLocal.getIntString("message.wannadelete"),
                AppLocal.getIntString("title.editor"),
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (res == JOptionPane.YES_OPTION) {

            int i = 0;
            while (i < m_invlines.getCount()) {
                m_invlines.deleteLine(i);
            }

            clearStockTable();
            showStockTable();

            jTableProductStock.repaint();
        }

    }//GEN-LAST:event_m_jBtnDeleteActionPerformed

    private void m_jcodebarMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_m_jcodebarMouseClicked
        m_jcodebar.requestFocusInWindow();
        jTextField1.requestFocus();
        m_jcodebar.setEnabled(true);
        m_jcodebar.setText(null);
    }//GEN-LAST:event_m_jcodebarMouseClicked

    private void m_jLocationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_jLocationActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_m_jLocationActionPerformed

    private void jTableLowStockMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTableLowStockMouseClicked
        if (evt.getClickCount() == 2) {
            int row = jTableLowStock.getSelectedRow();
            if (row >= 0 && lowStockModel != null) {
                com.openbravo.pos.inventory.LowStockProduct lowStockProduct = lowStockModel.getLowStockProduct(row);
                if (lowStockProduct != null) {
                    try {
                        m_App.getAppUserView().showTask("com.openbravo.pos.inventory.ProductsPanel");
                        Object bean = m_App.getBean("com.openbravo.pos.inventory.ProductsPanel");
                        if (bean instanceof com.openbravo.pos.inventory.ProductsPanel) {
                            com.openbravo.pos.inventory.ProductsPanel productsPanel = (com.openbravo.pos.inventory.ProductsPanel) bean;
                            productsPanel.selectRecordById(lowStockProduct.getProductId());
                            productsPanel.showDetailCard();
                        }
                    } catch (Exception ex) {
                        Logger.getLogger(StockManagement.class.getName()).log(Level.WARNING, "Error navigating to product", ex);
                    }
                }
            }
        }
    }//GEN-LAST:event_jTableLowStockMouseClicked

    /**
     * Carga los productos con stock bajo
     */
    private void loadLowStockProducts() {
        try {
            lowStockProducts = m_dlSales.getLowStockProducts();
            
            if (lowStockProducts == null || lowStockProducts.isEmpty()) {
                lowStockProducts = new java.util.ArrayList<>();
                filteredLowStockProducts = new java.util.ArrayList<>();
                currentPage = 0;
                updateLowStockTable();
                return;
            }
            
            if (lowStockCardLayout != null && lowStockCardPanel != null) {
                lowStockCardLayout.show(lowStockCardPanel, "TABLE");
            }
            
            filteredLowStockProducts = new java.util.ArrayList<>(lowStockProducts);
            currentPage = 0;
            updateLowStockTable();
            
        } catch (BasicException e) {
            Logger.getLogger(StockManagement.class.getName()).log(Level.SEVERE, "Error al cargar productos con stock bajo", e);
            MessageInf msg = new MessageInf(MessageInf.SGN_WARNING,
                    "Error al cargar productos con stock bajo", e);
            msg.show(this);
        }
    }

    /**
     * Modelo de tabla para productos con stock bajo
     */
    class LowStockProductTableModel extends AbstractTableModel {

        private static final long serialVersionUID = 1L;

        private List<com.openbravo.pos.inventory.LowStockProduct> lowStockList;
        private String[] columnNames = {
            "Producto", "Código/SKU", "Ubicación", "Stock Actual", "Mínimo", "Máximo", "Estado", "Acciones"
        };

        public LowStockProductTableModel(List<com.openbravo.pos.inventory.LowStockProduct> list) {
            lowStockList = list != null ? list : new ArrayList<>();
        }

        @Override
        public String getColumnName(int column) {
            return columnNames[column];
        }

        @Override
        public int getColumnCount() {
            return 8;
        }

        @Override
        public int getRowCount() {
            return lowStockList.size();
        }

        @Override
        public Object getValueAt(int row, int column) {
            if (row >= lowStockList.size()) {
                return "";
            }
            
            com.openbravo.pos.inventory.LowStockProduct product = lowStockList.get(row);

            switch (column) {
                case 0:
                    return product.getProductName();
                case 1:
                    return product.getProductCode();
                case 2:
                    return product.getLocationName();
                case 3:
                    return product.getUnits() != null ? product.getUnits() : 0.0;
                case 4:
                    return product.getMinimum() != null ? product.getMinimum() : 0.0;
                case 5:
                    return product.getMaximum() != null ? product.getMaximum() : 0.0;
                case 6: {
                    // Estado: check if units >= minimum
                    Double units = product.getUnits();
                    Double minimum = product.getMinimum();
                    if (units != null && minimum != null && units >= minimum) {
                        return "OK";
                    }
                    return "BAJO";
                }
                case 7:
                    return "ACCIONES";
                default:
                    return "";
            }
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            return column == 7; // Acciones column is editable for button clicks
        }

        @Override
        public void setValueAt(Object aValue, int row, int column) {
            // No editable directly
        }

        public com.openbravo.pos.inventory.LowStockProduct getLowStockProduct(int row) {
            if (row >= 0 && row < lowStockList.size()) {
                return lowStockList.get(row);
            }
            return null;
        }
    }

    private void btnExportLowStockExcelActionPerformed(java.awt.event.ActionEvent evt) {
        if (lowStockModel == null || lowStockModel.getRowCount() == 0) {
            javax.swing.JOptionPane.showMessageDialog(this,
                    "No hay productos con stock bajo para exportar.",
                    "Exportación Vacía",
                    javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }

        javax.swing.JFileChooser fc = new javax.swing.JFileChooser();
        fc.setDialogTitle("Guardar reporte de stock bajo");
        fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Archivo CSV (Excel) (*.csv)", "csv"));
        fc.setSelectedFile(new java.io.File("Reporte_Stock_Bajo_"
                + java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmm")) + ".csv"));

        int returnVal = fc.showSaveDialog(this);
        if (returnVal != javax.swing.JFileChooser.APPROVE_OPTION) {
            return;
        }

        java.io.File file = fc.getSelectedFile();
        if (!file.getName().toLowerCase().endsWith(".csv")) {
            file = new java.io.File(file.getAbsolutePath() + ".csv");
        }

        try (java.io.BufferedWriter writer = new java.io.BufferedWriter(
                new java.io.OutputStreamWriter(new java.io.FileOutputStream(file), java.nio.charset.StandardCharsets.UTF_8))) {
            // Write BOM for Excel UTF-8 compatibility
            writer.write('\ufeff');

            // Write Headers
            writer.write("Producto;Código;Ubicación;Stock Actual;Mínimo de Seguridad;Máximo Recomendado");
            writer.newLine();

            // Write Row Data
            int rowCount = lowStockModel.getRowCount();
            for (int i = 0; i < rowCount; i++) {
                com.openbravo.pos.inventory.LowStockProduct p = lowStockModel.getLowStockProduct(i);
                if (p != null) {
                    writer.write(sanitizeCsv(p.getProductName()) + ";");
                    writer.write(sanitizeCsv(p.getProductCode()) + ";");
                    writer.write(sanitizeCsv(p.getLocationName()) + ";");
                    writer.write(String.valueOf(p.getUnits() != null ? p.getUnits() : 0.0) + ";");
                    writer.write(String.valueOf(p.getMinimum() != null ? p.getMinimum() : 0.0) + ";");
                    writer.write(String.valueOf(p.getMaximum() != null ? p.getMaximum() : 0.0));
                    writer.newLine();
                }
            }

            javax.swing.JOptionPane.showMessageDialog(this,
                    "✅ Reporte de stock bajo exportado exitosamente.\n\n" +
                            "Archivo: " + file.getName(),
                    "Exportación Exitosa",
                    javax.swing.JOptionPane.INFORMATION_MESSAGE);

        } catch (java.io.IOException ex) {
            Logger.getLogger(StockManagement.class.getName()).log(Level.SEVERE, "Error exportando reporte de stock bajo: " + ex.getMessage(), ex);
            javax.swing.JOptionPane.showMessageDialog(this,
                    "❌ Error al exportar el reporte.\n\n" + ex.getMessage(),
                    "Error",
                    javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }

    private String sanitizeCsv(String text) {
        if (text == null)
            return "";
        return text.replace(";", ",");
    }


    private void styleComponents() {
        // --- Fuentes y Colores ---
        java.awt.Font labelFont = new java.awt.Font("Segoe UI Semibold", java.awt.Font.PLAIN, 13);
        java.awt.Color accentBlue = new java.awt.Color(0, 123, 255);
        java.awt.Color lightGray = new java.awt.Color(248, 249, 250);

        // --- Estilizar JPanel8 (Header / Controles) ---
        jPanel8.setBackground(java.awt.Color.WHITE);
        jPanel8.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, new java.awt.Color(222, 226, 230)));
        
        // Estilizar Etiquetas
        javax.swing.JLabel[] labels = {jLabel1, jLabel2, jLabel8, jLabel10, jLabel9};
        for (javax.swing.JLabel lbl : labels) {
            lbl.setFont(labelFont);
            lbl.setForeground(new java.awt.Color(108, 117, 125));
        }

        // Estilizar ComboBoxes y Fields
        javax.swing.JComponent[] inputs = {m_jdate, m_jreason, m_jLocation, m_jLocationDes, m_jSupplier, m_jSupplierDoc};
        for (javax.swing.JComponent input : inputs) {
            input.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 13));
            if (input instanceof javax.swing.JTextField) {
                ((javax.swing.JTextField) input).setBorder(javax.swing.BorderFactory.createCompoundBorder(
                    javax.swing.BorderFactory.createLineBorder(new java.awt.Color(206, 212, 218)),
                    javax.swing.BorderFactory.createEmptyBorder(2, 8, 2, 8)
                ));
            }
        }

        // --- Estilizar Botonera Lateral (jPanel2) ---
        jPanel2.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 5, 8));
        jPanel2.setPreferredSize(new java.awt.Dimension(85, 300));
        jPanel2.setBackground(lightGray);
        jPanel2.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 1, 0, 0, new java.awt.Color(222, 226, 230)));

        javax.swing.JButton[] actionBtns = {m_jDelete, m_jList, m_jEditLine, m_jEditAttributes, m_jBtnDelete};
        for (javax.swing.JButton btn : actionBtns) {
            btn.setPreferredSize(new java.awt.Dimension(65, 50));
            btn.setBackground(java.awt.Color.WHITE);
            btn.setFocusPainted(false);
            btn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
            btn.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(222, 226, 230)));
        }
        m_jBtnDelete.setText(""); // Quitar texto para mantener consistencia visual
        m_jBtnDelete.setToolTipText("Limpiar Todo");

        // --- Estilizar Indicadores de Totales ---
        styleStatusLabel(webLblQty, lblTotalQtyValue, "CANTIDAD ACTUAL", new java.awt.Color(108, 117, 125));
        styleStatusLabel(webLblValue, lbTotalValue, "VALOR INVENTARIO", accentBlue);

        // --- Estilizar Panel de Stock Bajo ---
        this.setBackground(new java.awt.Color(250, 247, 242));
        if (lowStockPanel != null) {
            lowStockPanel.setBackground(java.awt.Color.WHITE);
        }
        for (java.awt.Component child : this.getComponents()) {
            if (child instanceof javax.swing.JPanel) {
                javax.swing.JPanel jp = (javax.swing.JPanel) child;
                if (this.getLayout() instanceof java.awt.BorderLayout && 
                    ((java.awt.BorderLayout)this.getLayout()).getLayoutComponent(java.awt.BorderLayout.CENTER) == jp) {
                    jp.setBackground(new java.awt.Color(250, 247, 242));
                    jp.setBorder(javax.swing.BorderFactory.createEmptyBorder(18, 18, 18, 18));
                }
            }
        }

        // --- Estilizar Tablas ---
        styleTable(jTableProductStock, jScrollPane2);
        styleTable(jTableLowStock, jScrollPaneLowStock);
        
        // Estilo especial para m_jcodebar (Input de Escáner)
        m_jcodebar.setFont(new java.awt.Font("Consolas", java.awt.Font.BOLD, 18));
        m_jcodebar.setBorder(javax.swing.BorderFactory.createCompoundBorder(
            javax.swing.BorderFactory.createLineBorder(accentBlue, 2),
            javax.swing.BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        m_jcodebar.setBackground(new java.awt.Color(231, 241, 255));
    }

    private void styleStatusLabel(javax.swing.JLabel title, javax.swing.JLabel value, String text, java.awt.Color valColor) {
        title.setText(text);
        title.setFont(new java.awt.Font("Segoe UI Semibold", java.awt.Font.PLAIN, 11));
        title.setForeground(new java.awt.Color(108, 117, 125));
        
        value.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 18));
        value.setForeground(valColor);
    }

    private void styleTable(javax.swing.JTable table, javax.swing.JScrollPane scroll) {
        table.getTableHeader().setFont(new java.awt.Font("Segoe UI Semibold", java.awt.Font.PLAIN, 12));
        table.getTableHeader().setBackground(new java.awt.Color(241, 245, 249)); // Slate 100
        table.getTableHeader().setForeground(new java.awt.Color(71, 85, 105)); // Slate 600
        table.getTableHeader().setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, new java.awt.Color(226, 232, 240))); // Slate 200
        
        table.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 13));
        table.setRowHeight(38); // Altura de fila aumentada para legibilidad y modernidad
        table.setShowVerticalLines(false);
        table.setGridColor(new java.awt.Color(241, 245, 249)); // Slate 100
        table.setSelectionBackground(new java.awt.Color(254, 243, 199)); // Amber 100
        table.setSelectionForeground(new java.awt.Color(146, 64, 14)); // Amber 800
        
        scroll.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(226, 232, 240))); // Slate 200
        scroll.getViewport().setBackground(java.awt.Color.WHITE);
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel catcontainer;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JLabel jLabelLowStock;
    private com.openbravo.beans.JNumberKeys jNumberKeys;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPaneLowStock;
    private javax.swing.JTable jTableProductStock;
    private javax.swing.JTable jTableLowStock;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JLabel lbTotalValue;
    private javax.swing.JLabel lblTotalQtyValue;
    private javax.swing.JButton m_jBtnDelete;
    private javax.swing.JButton m_jBtnShowStock;
    private javax.swing.JButton m_jDelete;
    private javax.swing.JButton m_jEditAttributes;
    private javax.swing.JButton m_jEditLine;
    private javax.swing.JButton m_jEnter;
    private javax.swing.JButton m_jList;
    private javax.swing.JComboBox m_jLocation;
    private javax.swing.JComboBox m_jLocationDes;
    private javax.swing.JComboBox m_jSupplier;
    private javax.swing.JTextField m_jSupplierDoc;
    private javax.swing.JButton m_jbtndate;
    private javax.swing.JLabel m_jcodebar;
    private javax.swing.JTextField m_jdate;
    private javax.swing.JComboBox m_jreason;
    private javax.swing.JLabel webLblQty;
    private javax.swing.JLabel webLblValue;
    // End of variables declaration//GEN-END:variables

    // --- Custom Visual Components for Modern Redesign ---
    private static class RoundedPanel extends JPanel {
        private int cornerRadius = 15;
        private java.awt.Color backgroundColor = java.awt.Color.WHITE;
        private java.awt.Color borderColor = new java.awt.Color(226, 232, 240); // Slate 200

        public RoundedPanel(int radius, java.awt.Color bg) {
            super();
            this.cornerRadius = radius;
            this.backgroundColor = bg;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Dimension arcs = new Dimension(cornerRadius, cornerRadius);
            int width = getWidth();
            int height = getHeight();
            Graphics2D graphics = (Graphics2D) g;
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            graphics.setColor(backgroundColor);
            graphics.fillRoundRect(0, 0, width - 1, height - 1, arcs.width, arcs.height);
            
            graphics.setColor(borderColor);
            graphics.setStroke(new BasicStroke(1.2f));
            graphics.drawRoundRect(0, 0, width - 1, height - 1, arcs.width, arcs.height);
        }
    }

    private static class EmptyStatePanel extends JPanel {
        public EmptyStatePanel() {
            setLayout(new GridBagLayout());
            setOpaque(false);
            
            JPanel container = new JPanel();
            container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
            container.setOpaque(false);
            
            JPanel iconPanel = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    
                    int size = 70;
                    int x = (getWidth() - size) / 2;
                    int y = (getHeight() - size) / 2;
                    
                    g2.setColor(new java.awt.Color(209, 250, 229)); // Emerald 100
                    g2.fillOval(x, y, size, size);
                    
                    int innerSize = 50;
                    int ix = x + (size - innerSize) / 2;
                    int iy = y + (size - innerSize) / 2;
                    g2.setColor(new java.awt.Color(16, 185, 129)); // Emerald 500
                    g2.fillOval(ix, iy, innerSize, innerSize);
                    
                    g2.setColor(java.awt.Color.WHITE);
                    g2.setStroke(new BasicStroke(3.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g2.drawLine(ix + 16, iy + 25, ix + 23, iy + 32);
                    g2.drawLine(ix + 23, iy + 32, ix + 36, iy + 18);
                    
                    g2.dispose();
                }
            };
            iconPanel.setPreferredSize(new Dimension(100, 80));
            iconPanel.setOpaque(false);
            container.add(iconPanel);
            
            container.add(Box.createRigidArea(new Dimension(0, 15)));
            
            JLabel titleLbl = new JLabel("¡Inventario al Día!");
            titleLbl.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 18));
            titleLbl.setForeground(new java.awt.Color(15, 23, 42)); // Slate 900
            titleLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
            container.add(titleLbl);
            
            container.add(Box.createRigidArea(new Dimension(0, 8)));
            
            JLabel descLbl = new JLabel("Todos los productos se encuentran por encima del mínimo de seguridad.");
            descLbl.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 13));
            descLbl.setForeground(new java.awt.Color(100, 116, 139)); // Slate 500
            descLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
            container.add(descLbl);
            
            container.add(Box.createRigidArea(new Dimension(0, 4)));
            
            JLabel subDescLbl = new JLabel("No se requieren alertas de reposición inmediatas en esta sucursal.");
            subDescLbl.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 12));
            subDescLbl.setForeground(new java.awt.Color(148, 163, 184)); // Slate 400
            subDescLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
            container.add(subDescLbl);
            
            add(container);
        }
    }

    // ========================== NEW REDESIGNED METHODS ==========================

    /**
     * Builds the header area: Title row + Search/Filter bar
     */
    private javax.swing.JPanel buildHeaderArea() {
        javax.swing.JPanel wrapper = new javax.swing.JPanel();
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
        wrapper.setOpaque(false);
        wrapper.setBorder(javax.swing.BorderFactory.createEmptyBorder(20, 24, 10, 24));

        // === Title Row ===
        javax.swing.JPanel titleRow = new javax.swing.JPanel(new java.awt.BorderLayout());
        titleRow.setOpaque(false);
        titleRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

        javax.swing.JPanel titleTextPanel = new javax.swing.JPanel();
        titleTextPanel.setLayout(new BoxLayout(titleTextPanel, BoxLayout.Y_AXIS));
        titleTextPanel.setOpaque(false);

        javax.swing.JLabel mainTitle = new javax.swing.JLabel("GESTIÓN DE STOCK");
        mainTitle.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 22));
        mainTitle.setForeground(new java.awt.Color(51, 51, 51));
        mainTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        titleTextPanel.add(mainTitle);

        javax.swing.JLabel subtitle = new javax.swing.JLabel("Gestión / Almacén Principal");
        subtitle.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 13));
        subtitle.setForeground(new java.awt.Color(120, 120, 120));
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        titleTextPanel.add(subtitle);

        titleRow.add(titleTextPanel, java.awt.BorderLayout.WEST);
        wrapper.add(titleRow);
        wrapper.add(Box.createRigidArea(new Dimension(0, 14)));

        // === Search / Filter Bar ===
        javax.swing.JPanel searchBar = new javax.swing.JPanel(new java.awt.BorderLayout(10, 0));
        searchBar.setOpaque(false);
        searchBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        // Left side: search + filter
        javax.swing.JPanel searchLeft = new javax.swing.JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 8, 0));
        searchLeft.setOpaque(false);

        searchField = new javax.swing.JTextField(18);
        searchField.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 13));
        searchField.setBorder(javax.swing.BorderFactory.createCompoundBorder(
            javax.swing.BorderFactory.createLineBorder(new java.awt.Color(206, 212, 218)),
            javax.swing.BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        searchField.setToolTipText("Buscar por producto, SKU o ubicación");
        searchField.putClientProperty("JTextField.placeholderText", "Buscar por producto, SKU o ubicación");
        searchField.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent e) {
                filterLowStockProducts();
            }
        });
        searchLeft.add(searchField);

        javax.swing.JButton btnRefresh = createStyledButton("Actualizar", java.awt.Color.WHITE,
                new java.awt.Color(51, 65, 85));
        btnRefresh.setIcon(new ModernActionIcon(ModernActionIcon.Type.REFRESH, 18));
        btnRefresh.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createLineBorder(new java.awt.Color(203, 213, 225)),
                javax.swing.BorderFactory.createEmptyBorder(6, 12, 6, 12)));
        btnRefresh.addActionListener(event -> loadLowStockProducts());
        searchLeft.add(btnRefresh);

        searchBar.add(searchLeft, java.awt.BorderLayout.WEST);

        // Right side: Export + Nuevo Producto buttons
        javax.swing.JPanel searchRight = new javax.swing.JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 8, 0));
        searchRight.setOpaque(false);

        javax.swing.JButton btnExport = createStyledButton("Exportar Excel", new java.awt.Color(53, 122, 56), java.awt.Color.WHITE);
        btnExport.setIcon(new ModernActionIcon(ModernActionIcon.Type.EXPORT, 18, java.awt.Color.WHITE));
        btnExport.addActionListener(evt -> btnExportLowStockExcelActionPerformed(evt));
        searchRight.add(btnExport);

        javax.swing.JButton btnNewProduct = createStyledButton("Nuevo Producto", new java.awt.Color(62, 62, 62), java.awt.Color.WHITE);
        btnNewProduct.setIcon(new ModernActionIcon(ModernActionIcon.Type.ADD, 18, java.awt.Color.WHITE));
        btnNewProduct.addActionListener(evt -> {
            try {
                m_App.getAppUserView().showTask("com.openbravo.pos.inventory.ProductsPanel");
            } catch (Exception ex) {
                Logger.getLogger(StockManagement.class.getName()).log(Level.WARNING, "Error navigating to products", ex);
            }
        });
        searchRight.add(btnNewProduct);

        searchBar.add(searchRight, java.awt.BorderLayout.EAST);
        wrapper.add(searchBar);

        return wrapper;
    }

    /**
     * Builds the amber/yellow alert banner
     */
    private javax.swing.JPanel buildAlertBanner() {
        javax.swing.JPanel banner = new javax.swing.JPanel(new java.awt.BorderLayout(12, 0));
        banner.setBackground(new java.awt.Color(255, 248, 230));
        banner.setBorder(javax.swing.BorderFactory.createCompoundBorder(
            javax.swing.BorderFactory.createMatteBorder(1, 1, 1, 1, new java.awt.Color(230, 210, 160)),
            javax.swing.BorderFactory.createEmptyBorder(12, 20, 12, 20)
        ));
        banner.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));

        // Warning icon panel
        javax.swing.JPanel iconPanel = new javax.swing.JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int size = 36;
                int x = (getWidth() - size) / 2;
                int y = (getHeight() - size) / 2;

                // Draw rounded square background
                g2.setColor(new java.awt.Color(180, 140, 50));
                g2.fillRoundRect(x, y, size, size, 8, 8);

                // Draw clipboard icon in white
                g2.setColor(java.awt.Color.WHITE);
                g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                // Clipboard body
                g2.drawRoundRect(x + 8, y + 6, 20, 24, 3, 3);
                // Clipboard clip
                g2.drawLine(x + 14, y + 4, x + 22, y + 4);
                // Lines on clipboard
                g2.drawLine(x + 13, y + 14, x + 23, y + 14);
                g2.drawLine(x + 13, y + 19, x + 23, y + 19);
                g2.drawLine(x + 13, y + 24, x + 20, y + 24);

                g2.dispose();
            }
        };
        iconPanel.setPreferredSize(new Dimension(44, 44));
        iconPanel.setOpaque(false);
        banner.add(iconPanel, java.awt.BorderLayout.WEST);

        javax.swing.JPanel textPanel = new javax.swing.JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);

        alertLabel = new javax.swing.JLabel("¡AVISO DE BAJO STOCK - Requieren Reposición: 0 Ítems");
        alertLabel.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 14));
        alertLabel.setForeground(new java.awt.Color(100, 70, 20));
        alertLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        textPanel.add(alertLabel);

        javax.swing.JLabel alertSub = new javax.swing.JLabel("Verifique y gestione los niveles de existencias inferiores al mínimo de seguridad.");
        alertSub.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 12));
        alertSub.setForeground(new java.awt.Color(130, 100, 40));
        alertSub.setAlignmentX(Component.LEFT_ALIGNMENT);
        textPanel.add(alertSub);

        banner.add(textPanel, java.awt.BorderLayout.CENTER);

        // Wrap to add margin
        javax.swing.JPanel bannerWrapper = new javax.swing.JPanel(new java.awt.BorderLayout());
        bannerWrapper.setOpaque(false);
        bannerWrapper.setBorder(javax.swing.BorderFactory.createEmptyBorder(6, 24, 6, 24));
        bannerWrapper.add(banner, java.awt.BorderLayout.CENTER);
        bannerWrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        return bannerWrapper;
    }

    /**
     * Builds the pagination panel at the bottom
     */
    private javax.swing.JPanel buildPaginationPanel() {
        javax.swing.JPanel panel = new javax.swing.JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 4, 8));
        panel.setOpaque(false);
        panel.setBorder(javax.swing.BorderFactory.createEmptyBorder(4, 0, 10, 0));
        return panel;
    }

    /**
     * Creates a styled button with rounded appearance
     */
    private javax.swing.JButton createStyledButton(String text, java.awt.Color bg, java.awt.Color fg) {
        javax.swing.JButton btn = new javax.swing.JButton(text);
        btn.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12));
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btn.setPreferredSize(new java.awt.Dimension(140, 34));
        btn.setBorder(javax.swing.BorderFactory.createEmptyBorder(6, 16, 6, 16));
        btn.setOpaque(true);
        return btn;
    }

    /**
     * Filters the low stock products based on search text
     */
    private void filterLowStockProducts() {
        if (lowStockProducts == null) return;

        String query = searchField.getText().trim().toLowerCase();
        if (query.isEmpty()) {
            filteredLowStockProducts = new java.util.ArrayList<>(lowStockProducts);
        } else {
            filteredLowStockProducts = new java.util.ArrayList<>();
            for (com.openbravo.pos.inventory.LowStockProduct p : lowStockProducts) {
                if ((p.getProductName() != null && p.getProductName().toLowerCase().contains(query)) ||
                    (p.getProductCode() != null && p.getProductCode().toLowerCase().contains(query)) ||
                    (p.getLocationName() != null && p.getLocationName().toLowerCase().contains(query))) {
                    filteredLowStockProducts.add(p);
                }
            }
        }
        currentPage = 0;
        updateLowStockTable();
    }

    /**
     * Updates the low stock table with current filtered data and page
     */
    private void updateLowStockTable() {
        if (filteredLowStockProducts == null || filteredLowStockProducts.isEmpty()) {
            if (lowStockCardLayout != null && lowStockCardPanel != null) {
                lowStockCardLayout.show(lowStockCardPanel, "EMPTY");
            }
            if (alertLabel != null) {
                alertLabel.setText("¡AVISO DE BAJO STOCK - Requieren Reposición: 0 Ítems");
            }
            updatePaginationPanel(0);
            return;
        }

        if (lowStockCardLayout != null && lowStockCardPanel != null) {
            lowStockCardLayout.show(lowStockCardPanel, "TABLE");
        }

        // Update alert label
        int totalCount = lowStockProducts != null ? lowStockProducts.size() : 0;
        if (alertLabel != null) {
            alertLabel.setText("¡AVISO DE BAJO STOCK - Requieren Reposición: " + totalCount + " Ítems");
        }

        // Paginate
        int totalFiltered = filteredLowStockProducts.size();
        int totalPages = (int) Math.ceil((double) totalFiltered / ROWS_PER_PAGE);
        if (currentPage >= totalPages) currentPage = totalPages - 1;
        if (currentPage < 0) currentPage = 0;

        int fromIndex = currentPage * ROWS_PER_PAGE;
        int toIndex = Math.min(fromIndex + ROWS_PER_PAGE, totalFiltered);
        List<com.openbravo.pos.inventory.LowStockProduct> pageList = filteredLowStockProducts.subList(fromIndex, toIndex);

        lowStockModel = new LowStockProductTableModel(new java.util.ArrayList<>(pageList));
        jTableLowStock.setModel(lowStockModel);

        // Style header
        jTableLowStock.getTableHeader().setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12));
        jTableLowStock.getTableHeader().setBackground(new java.awt.Color(250, 250, 250));
        jTableLowStock.getTableHeader().setForeground(new java.awt.Color(80, 80, 80));
        jTableLowStock.getTableHeader().setReorderingAllowed(false);
        jTableLowStock.getTableHeader().setBorder(
            javax.swing.BorderFactory.createMatteBorder(0, 0, 2, 0, new java.awt.Color(226, 226, 226))
        );

        jTableLowStock.setShowGrid(false);
        jTableLowStock.setShowHorizontalLines(true);
        jTableLowStock.setGridColor(new java.awt.Color(240, 240, 240));
        jTableLowStock.setRowHeight(52);
        jTableLowStock.setIntercellSpacing(new Dimension(0, 1));

        // Column widths
        jTableLowStock.getColumnModel().getColumn(0).setPreferredWidth(160); // Producto
        jTableLowStock.getColumnModel().getColumn(1).setPreferredWidth(100); // Código/SKU
        jTableLowStock.getColumnModel().getColumn(2).setPreferredWidth(200); // Ubicación
        jTableLowStock.getColumnModel().getColumn(3).setPreferredWidth(100); // Stock Actual
        jTableLowStock.getColumnModel().getColumn(4).setPreferredWidth(70);  // Mínimo
        jTableLowStock.getColumnModel().getColumn(5).setPreferredWidth(70);  // Máximo
        jTableLowStock.getColumnModel().getColumn(6).setPreferredWidth(60);  // Estado
        jTableLowStock.getColumnModel().getColumn(7).setPreferredWidth(200); // Acciones

        // Apply custom renderers
        for (int i = 0; i <= 5; i++) {
            jTableLowStock.getColumnModel().getColumn(i).setCellRenderer(new StockTextCellRenderer());
        }
        jTableLowStock.getColumnModel().getColumn(3).setCellRenderer(new StockBadgeCellRenderer());
        jTableLowStock.getColumnModel().getColumn(6).setCellRenderer(new StockStatusCellRenderer());
        jTableLowStock.getColumnModel().getColumn(7).setCellRenderer(new StockActionsCellRenderer());
        jTableLowStock.getColumnModel().getColumn(7).setCellEditor(new StockActionsEditor());

        // Make acciones column editable for button clicks
        jTableLowStock.getColumnModel().getColumn(7).setMinWidth(200);

        updatePaginationPanel(totalPages);
    }

    /**
     * Updates pagination buttons
     */
    private void updatePaginationPanel(int totalPages) {
        if (paginationPanel == null) return;
        paginationPanel.removeAll();

        if (totalPages <= 1) {
            paginationPanel.revalidate();
            paginationPanel.repaint();
            return;
        }

        java.awt.Color btnBg = java.awt.Color.WHITE;
        java.awt.Color btnFg = new java.awt.Color(80, 80, 80);
        java.awt.Color activeBg = new java.awt.Color(53, 122, 56);

        // Previous
        javax.swing.JButton btnPrev = new javax.swing.JButton("«");
        stylePaginationBtn(btnPrev, btnBg, btnFg);
        btnPrev.addActionListener(e -> { if (currentPage > 0) { currentPage--; updateLowStockTable(); } });
        paginationPanel.add(btnPrev);

        for (int i = 0; i < totalPages; i++) {
            final int page = i;
            javax.swing.JButton btn = new javax.swing.JButton(String.valueOf(i + 1));
            if (i == currentPage) {
                stylePaginationBtn(btn, activeBg, java.awt.Color.WHITE);
            } else {
                stylePaginationBtn(btn, btnBg, btnFg);
            }
            btn.addActionListener(e -> { currentPage = page; updateLowStockTable(); });
            paginationPanel.add(btn);
        }

        // Next
        javax.swing.JButton btnNext = new javax.swing.JButton("»");
        stylePaginationBtn(btnNext, btnBg, btnFg);
        btnNext.addActionListener(e -> { if (currentPage < totalPages - 1) { currentPage++; updateLowStockTable(); } });
        paginationPanel.add(btnNext);

        paginationPanel.revalidate();
        paginationPanel.repaint();
    }

    private void stylePaginationBtn(javax.swing.JButton btn, java.awt.Color bg, java.awt.Color fg) {
        btn.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12));
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(34, 30));
        btn.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(220, 220, 220)));
        btn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btn.setOpaque(true);
    }

    // ========================== CELL RENDERERS ==========================

    /**
     * Default text cell renderer - clean white rows with subtle separator
     */
    private class StockTextCellRenderer extends javax.swing.table.DefaultTableCellRenderer {
        @Override
        public java.awt.Component getTableCellRendererComponent(
                javax.swing.JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
            javax.swing.JLabel lbl = (javax.swing.JLabel) super.getTableCellRendererComponent(
                    table, value, isSelected, hasFocus, row, column);

            lbl.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 13));
            lbl.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 14, 0, 8));

            if (isSelected) {
                lbl.setBackground(new java.awt.Color(245, 240, 230));
                lbl.setForeground(new java.awt.Color(60, 60, 60));
            } else {
                lbl.setBackground(java.awt.Color.WHITE);
                lbl.setForeground(new java.awt.Color(60, 60, 60));
            }

            // Right-align numeric columns (4=Mínimo, 5=Máximo)
            if (column >= 4 && column <= 5) {
                lbl.setHorizontalAlignment(javax.swing.JLabel.CENTER);
            } else {
                lbl.setHorizontalAlignment(javax.swing.JLabel.LEFT);
            }

            return lbl;
        }
    }

    /**
     * Stock Actual column - renders colored badge with arrow
     */
    private class StockBadgeCellRenderer extends javax.swing.table.DefaultTableCellRenderer {
        @Override
        public java.awt.Component getTableCellRendererComponent(
                javax.swing.JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {

            javax.swing.JPanel panel = new javax.swing.JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 4, 8));
            panel.setBackground(isSelected ? new java.awt.Color(245, 240, 230) : java.awt.Color.WHITE);

            Double units = 0.0;
            boolean isLow = true;
            if (lowStockModel != null && row < lowStockModel.getRowCount()) {
                com.openbravo.pos.inventory.LowStockProduct p = lowStockModel.getLowStockProduct(row);
                if (p != null) {
                    units = p.getUnits() != null ? p.getUnits() : 0.0;
                    Double min = p.getMinimum() != null ? p.getMinimum() : 0.0;
                    isLow = units < min;
                }
            }

            // Badge label
            javax.swing.JLabel badge = new javax.swing.JLabel(String.valueOf(units));
            badge.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12));
            badge.setOpaque(true);
            badge.setHorizontalAlignment(javax.swing.JLabel.CENTER);
            badge.setBorder(javax.swing.BorderFactory.createEmptyBorder(3, 10, 3, 10));

            if (isLow) {
                badge.setBackground(new java.awt.Color(220, 53, 69)); // Red
                badge.setForeground(java.awt.Color.WHITE);
            } else {
                badge.setBackground(new java.awt.Color(40, 167, 69)); // Green
                badge.setForeground(java.awt.Color.WHITE);
            }

            panel.add(badge);

            // Arrow indicator
            javax.swing.JLabel arrow = new javax.swing.JLabel(isLow ? "↓" : "✓");
            arrow.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 14));
            arrow.setForeground(isLow ? new java.awt.Color(220, 53, 69) : new java.awt.Color(40, 167, 69));
            panel.add(arrow);

            return panel;
        }
    }

    /**
     * Estado column - renders warning triangle or green checkmark
     */
    private class StockStatusCellRenderer extends javax.swing.table.DefaultTableCellRenderer {
        @Override
        public java.awt.Component getTableCellRendererComponent(
                javax.swing.JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {

            javax.swing.JPanel panel = new javax.swing.JPanel(new java.awt.GridBagLayout());
            panel.setBackground(isSelected ? new java.awt.Color(245, 240, 230) : java.awt.Color.WHITE);

            boolean isOk = "OK".equals(value);

            // Custom painted icon
            javax.swing.JPanel icon = new javax.swing.JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                    int cx = getWidth() / 2;
                    int cy = getHeight() / 2;

                    if (isOk) {
                        // Green circle with checkmark
                        g2.setColor(new java.awt.Color(40, 167, 69));
                        g2.fillOval(cx - 12, cy - 12, 24, 24);
                        g2.setColor(java.awt.Color.WHITE);
                        g2.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                        g2.drawLine(cx - 5, cy, cx - 1, cy + 4);
                        g2.drawLine(cx - 1, cy + 4, cx + 6, cy - 4);
                    } else {
                        // Amber warning triangle
                        g2.setColor(new java.awt.Color(200, 150, 30));
                        int[] xPoints = {cx, cx - 12, cx + 12};
                        int[] yPoints = {cy - 11, cy + 9, cy + 9};
                        g2.fillPolygon(xPoints, yPoints, 3);
                        g2.setColor(java.awt.Color.WHITE);
                        g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, 0));
                        g2.drawLine(cx, cy - 5, cx, cy + 2);
                        g2.fillOval(cx - 2, cy + 4, 4, 4);
                    }
                    g2.dispose();
                }
            };
            icon.setPreferredSize(new Dimension(30, 30));
            icon.setOpaque(false);
            panel.add(icon);

            return panel;
        }
    }

    /**
     * Acciones column - renders Editar and Reabastecer buttons
     */
    private class StockActionsCellRenderer implements javax.swing.table.TableCellRenderer {
        @Override
        public java.awt.Component getTableCellRendererComponent(
                javax.swing.JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {

            javax.swing.JPanel panel = new javax.swing.JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 6, 8));
            panel.setBackground(isSelected ? new java.awt.Color(245, 240, 230) : java.awt.Color.WHITE);

            javax.swing.JButton btnEdit = new javax.swing.JButton("Editar",
                    new ModernActionIcon(ModernActionIcon.Type.EDIT, 16));
            btnEdit.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 11));
            btnEdit.setBackground(java.awt.Color.WHITE);
            btnEdit.setForeground(new java.awt.Color(80, 80, 80));
            btnEdit.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createLineBorder(new java.awt.Color(200, 200, 200)),
                javax.swing.BorderFactory.createEmptyBorder(4, 10, 4, 10)
            ));
            btnEdit.setFocusPainted(false);
            btnEdit.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
            panel.add(btnEdit);

            javax.swing.JButton btnRestock = new javax.swing.JButton("Reabastecer",
                    new ModernActionIcon(ModernActionIcon.Type.BOX, 16, java.awt.Color.WHITE));
            btnRestock.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 11));
            btnRestock.setBackground(new java.awt.Color(62, 62, 62));
            btnRestock.setForeground(java.awt.Color.WHITE);
            btnRestock.setBorder(javax.swing.BorderFactory.createEmptyBorder(4, 10, 4, 10));
            btnRestock.setFocusPainted(false);
            btnRestock.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
            btnRestock.setOpaque(true);
            panel.add(btnRestock);

            return panel;
        }
    }

    /**
     * Cell editor for Acciones column to handle button clicks
     */
    private class StockActionsEditor extends javax.swing.AbstractCellEditor implements javax.swing.table.TableCellEditor {
        private javax.swing.JPanel panel;
        private int editingRow = -1;

        @Override
        public java.awt.Component getTableCellEditorComponent(
                javax.swing.JTable table, Object value, boolean isSelected, int row, int column) {

            editingRow = row;
            panel = new javax.swing.JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 6, 8));
            panel.setBackground(new java.awt.Color(245, 240, 230));

            javax.swing.JButton btnEdit = new javax.swing.JButton("Editar",
                    new ModernActionIcon(ModernActionIcon.Type.EDIT, 16));
            btnEdit.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 11));
            btnEdit.setBackground(java.awt.Color.WHITE);
            btnEdit.setForeground(new java.awt.Color(80, 80, 80));
            btnEdit.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createLineBorder(new java.awt.Color(200, 200, 200)),
                javax.swing.BorderFactory.createEmptyBorder(4, 10, 4, 10)
            ));
            btnEdit.setFocusPainted(false);
            btnEdit.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
            btnEdit.addActionListener(e -> {
                if (lowStockModel != null && editingRow >= 0) {
                    com.openbravo.pos.inventory.LowStockProduct p = lowStockModel.getLowStockProduct(editingRow);
                    if (p != null) {
                        try {
                            m_App.getAppUserView().showTask("com.openbravo.pos.inventory.ProductsPanel");
                            Object bean = m_App.getBean("com.openbravo.pos.inventory.ProductsPanel");
                            if (bean instanceof com.openbravo.pos.inventory.ProductsPanel) {
                                com.openbravo.pos.inventory.ProductsPanel productsPanel = (com.openbravo.pos.inventory.ProductsPanel) bean;
                                productsPanel.selectRecordById(p.getProductId());
                                productsPanel.showDetailCard();
                            }
                        } catch (Exception ex) {
                            Logger.getLogger(StockManagement.class.getName()).log(Level.WARNING, "Error navigating to product", ex);
                        }
                    }
                }
                fireEditingStopped();
            });
            panel.add(btnEdit);

            javax.swing.JButton btnRestock = new javax.swing.JButton("Reabastecer",
                    new ModernActionIcon(ModernActionIcon.Type.BOX, 16, java.awt.Color.WHITE));
            btnRestock.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 11));
            btnRestock.setBackground(new java.awt.Color(62, 62, 62));
            btnRestock.setForeground(java.awt.Color.WHITE);
            btnRestock.setBorder(javax.swing.BorderFactory.createEmptyBorder(4, 10, 4, 10));
            btnRestock.setFocusPainted(false);
            btnRestock.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
            btnRestock.setOpaque(true);
            btnRestock.addActionListener(e -> {
                // Reabastecer: just select the product so user can add stock
                if (lowStockModel != null && editingRow >= 0) {
                    com.openbravo.pos.inventory.LowStockProduct p = lowStockModel.getLowStockProduct(editingRow);
                    if (p != null) {
                        // Switch to the stock movement form and select the product
                        selectProductForRestock(p);
                    }
                }
                fireEditingStopped();
            });
            panel.add(btnRestock);

            return panel;
        }

        @Override
        public Object getCellEditorValue() {
            return "ACCIONES";
        }

        @Override
        public boolean isCellEditable(java.util.EventObject e) {
            return true;
        }
    }

    /**
     * Handles restock action - selects product in the stock entry form
     */
    private void selectProductForRestock(com.openbravo.pos.inventory.LowStockProduct product) {
        if (product != null && product.getProductId() != null) {
            try {
                // Try to find the product and add it to the movement lines
                ProductInfoExt prodInfo = m_dlSales.getProductInfo(product.getProductId());
                if (prodInfo != null) {
                    // Set reason to "Entrada" (IN_PURCHASE)
                    m_ReasonModel.setSelectedKey(MovementReason.IN_PURCHASE);
                    buttonTransition(prodInfo);
                }
            } catch (BasicException ex) {
                Logger.getLogger(StockManagement.class.getName()).log(Level.WARNING, "Error finding product for restock", ex);
                JOptionPane.showMessageDialog(this, "Error al buscar el producto: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}

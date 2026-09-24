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
import com.openbravo.beans.DateUtils;
import com.openbravo.data.gui.ComboBoxValModel;
import com.openbravo.data.loader.Datas;
import com.openbravo.data.loader.PreparedSentence;
import com.openbravo.data.loader.SerializerWriteBasic;
import com.openbravo.data.loader.SerializerWriteBasicExt;
import com.openbravo.data.loader.SerializerReadString;
import com.openbravo.data.loader.SerializerReadDouble;

import com.openbravo.data.loader.SentenceList;
import com.openbravo.data.loader.SentenceFind;
import com.openbravo.data.user.DirtyManager;
import com.openbravo.data.user.EditorRecord;
import com.openbravo.format.Formats;
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.pos.forms.AppView;
import com.openbravo.pos.forms.DataLogicSales;
import com.openbravo.pos.ticket.ProductPriceHistory; // Sebastian - Importar Historial de Precios
import com.openbravo.pos.ticket.ProductFilter;

import com.openbravo.pos.sales.TaxesLogic;
import com.openbravo.pos.suppliers.DataLogicSuppliers;
import com.openbravo.pos.suppliers.JDialogNewSupplier;
import com.openbravo.data.gui.MessageInf;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import com.openbravo.beans.JCalendarDialog;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.AbstractTableModel;
import com.openbravo.pos.forms.DataLogicSystem;
import com.openbravo.pos.util.ModernActionIcon;
import java.awt.Color;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.JColorChooser;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.netbeans.validation.api.builtin.stringvalidation.StringValidators;

/**
 *
 * @author Jack Gerrard
 */
public final class ProductsEditor extends com.openbravo.pos.panels.ValidationPanel implements EditorRecord {

    private static final Logger LOGGER = Logger.getLogger(ProductsEditor.class.getName());
    private static final long serialVersionUID = 1L;
    private String productId;

    private final SentenceList m_sentcat;
    private ComboBoxValModel m_CategoryModel;

    private final SentenceList taxcatsent;
    private ComboBoxValModel taxcatmodel;

    private final SentenceList m_sentsuppliers;
    private ComboBoxValModel m_SuppliersModel;

    private final SentenceList taxsent;
    private TaxesLogic taxeslogic;

    private Double pricesell;
    private boolean priceselllock = false;

    private boolean isSaving = false;

    private boolean reportlock = false;
    private int btn;
    private Color bckgColor = null;

    private DirtyManager m_Dirty;
    private DataLogicSales dlSales;
    private DataLogicSuppliers m_dlSuppliers;

    private AppView appView;

    // Guardar el valor inicial del stock cuando se carga el producto
    private Double initialStockValue = null;

    private final SentenceFind loadimage; // JG 3 feb 16 speedup

    protected DataLogicSystem dlSystem;

    // Sebastian - Valores iniciales para detectar cambios y registrar historial
    private Double initialPriceBuy = null;
    private Double initialPriceSell = null;
    private JProductVariationPanel m_variationPanel; // Sebastian - Panel de Variación
    private javax.swing.JTable jTableProductAuditHistory;
    private javax.swing.table.DefaultTableModel auditHistoryTableModel;
    private javax.swing.JTextArea txtAuditDetails;
    private javax.swing.JTable jTableProductStockHistory;
    private javax.swing.table.DefaultTableModel stockHistoryTableModel;
    private javax.swing.JTable jTableSupplierProducts;
    private javax.swing.table.DefaultTableModel supplierProductsTableModel;
    private ProductFilter m_productFilter;

    public ProductsEditor(AppView app, DirtyManager dirty) {
        this(app, dirty, null);
    }

    /**
     * Creates new form JEditProduct
     *
     * @param app
     * @param dirty
     */
    public ProductsEditor(AppView app, DirtyManager dirty, ProductFilter productFilter) {

        setAppView(app);
        dlSales = (DataLogicSales) app.getBean("com.openbravo.pos.forms.DataLogicSales");
        m_dlSuppliers = (DataLogicSuppliers) app.getBean("com.openbravo.pos.suppliers.DataLogicSuppliers");

        m_Dirty = dirty;
        m_productFilter = productFilter;

        // Sebastian - Inicializar panel de variación antes de initComponents
        m_variationPanel = new JProductVariationPanel();
        initComponents();

        loadimage = dlSales.getProductImage(); // JG 3 feb 16 speedup

        taxsent = dlSales.getTaxList();

        m_sentcat = dlSales.getCategoriesList();
        m_CategoryModel = new ComboBoxValModel();

        taxcatsent = dlSales.getTaxCategoriesList();
        taxcatmodel = new ComboBoxValModel();

        m_sentsuppliers = m_dlSuppliers.getSupplierList();
        m_SuppliersModel = new ComboBoxValModel();

        // Tab General
        m_jRef.getDocument().addDocumentListener(dirty);
        m_jCode.getDocument().addDocumentListener(dirty);
        m_jCodetype.addActionListener(dirty);
        m_jName.getDocument().addDocumentListener(dirty);
        m_jCategory.addActionListener(dirty);
        m_jAtt.addActionListener(dirty);
        m_jVerpatrib.addActionListener(dirty);
        m_jPriceBuy.getDocument().addDocumentListener(dirty);
        m_jPriceSell.getDocument().addDocumentListener(dirty);
        m_jPrintTo.addActionListener(dirty);

        // Tab Stock
        m_jInCatalog.addActionListener(dirty);
        m_jConstant.addActionListener(dirty);
        m_jCatalogOrder.getDocument().addDocumentListener(dirty);
        m_jSupplier.addActionListener(dirty);

        m_jService.addActionListener(dirty);
        m_jCheckWarrantyReceipt.addActionListener(dirty);
        m_jComment.addActionListener(dirty);
        m_jScale.addActionListener(dirty);
        m_jVprice.addActionListener(dirty);
        m_jstockcost.getDocument().addDocumentListener(dirty);
        m_jstockvolume.getDocument().addDocumentListener(dirty);
        m_jPrintKB.addActionListener(dirty);
        m_jSendStatus.addActionListener(dirty);
        m_jAccumulatesPoints.addActionListener(dirty);

        // Tab Image
        m_jImage.addPropertyChangeListener("image", dirty);

        // Tab Button
        m_jDisplay.getDocument().addDocumentListener(dirty);
        m_jTextTip.getDocument().addDocumentListener(dirty);
        colourChooser.addActionListener(dirty);
        m_jDisplay.addCaretListener(null);

        // Tab Properties
        txtAttributes.getDocument().addDocumentListener(dirty);

        FieldsManager fm = new FieldsManager();
        m_jPriceBuy.getDocument().addDocumentListener(fm);
        m_jPriceSell.getDocument().addDocumentListener(new PriceSellManager());
        m_jTax.addActionListener(fm);
        m_jPriceSellTax.getDocument().addDocumentListener(new PriceTaxManager());
        m_jmargin.getDocument().addDocumentListener(new MarginManager());
        // No agregar listener a m_jGrossProfit ya que es solo lectura y se calcula
        // automáticamente

        m_jdate.getDocument().addDocumentListener(dirty);

        init();
        initValidator();

        // Ocultar componentes de atributos no utilizados para simplificar la interfaz
        jLabel13.setVisible(false);
        m_jAtt.setVisible(false);
        m_jVerpatrib.setVisible(false);
        jLabel26.setVisible(false);
        m_jUom.setVisible(false);
    }

    public ComboBoxValModel getCategoryModel() {
        return m_CategoryModel;
    }

    public ComboBoxValModel getTaxCatModel() {
        return taxcatmodel;
    }

    private void initValidator() {
        org.netbeans.validation.api.ui.ValidationGroup valGroup = getValidationGroup();
        valGroup.add(m_jRef, StringValidators.REQUIRE_NON_EMPTY_STRING);
        valGroup.add(m_jName, StringValidators.REQUIRE_NON_EMPTY_STRING);
        valGroup.add(m_jCode, StringValidators.REQUIRE_NON_EMPTY_STRING);
        valGroup.add(m_jPriceSellTax, StringValidators.REQUIRE_NON_EMPTY_STRING);
    }

    private void init() {
        writeValueEOF();
    }

    /**
     * Instantiate object
     *
     * @throws BasicException
     */
    @SuppressWarnings("unchecked")
    public void activate() throws BasicException {

        taxeslogic = new TaxesLogic(taxsent.list());

        m_CategoryModel = new ComboBoxValModel(m_sentcat.list());
        m_jCategory.setModel(m_CategoryModel);

        taxcatmodel = new ComboBoxValModel(taxcatsent.list());
        m_jTax.setModel(taxcatmodel);

        m_SuppliersModel = new ComboBoxValModel(m_sentsuppliers.list());
        m_jSupplier.setModel(m_SuppliersModel);

        String pId = null;

        // Diagnostic log
        System.out.println("=== DIAGNOSTIC START ===");
        if (mainCombinedPanel != null) {
            System.out.println("mainCombinedPanel size: " + mainCombinedPanel.getSize());
            System.out.println("mainCombinedPanel visible: " + mainCombinedPanel.isVisible());
            System.out.println("mainCombinedPanel children count: " + mainCombinedPanel.getComponentCount());
            for (java.awt.Component c : mainCombinedPanel.getComponents()) {
                System.out.println("  Child class: " + c.getClass().getName());
                System.out.println("  Child size: " + c.getSize());
                System.out.println("  Child visible: " + c.isVisible());
                if (c instanceof javax.swing.JTabbedPane) {
                    javax.swing.JTabbedPane tp = (javax.swing.JTabbedPane) c;
                    System.out.println("    JTabbedPane tab count: " + tp.getTabCount());
                    for (int i = 0; i < tp.getTabCount(); i++) {
                        System.out.println("      Tab " + i + " title: " + tp.getTitleAt(i));
                        java.awt.Component tc = tp.getComponentAt(i);
                        System.out.println("      Tab " + i + " component class: " + tc.getClass().getName());
                        System.out.println("      Tab " + i + " component size: " + tc.getSize());
                        System.out.println("      Tab " + i + " component visible: " + tc.isVisible());
                        if (tc instanceof javax.swing.JScrollPane) {
                            javax.swing.JScrollPane sp = (javax.swing.JScrollPane) tc;
                            java.awt.Component view = sp.getViewport().getView();
                            if (view != null) {
                                System.out.println("        JScrollPane view class: " + view.getClass().getName());
                                System.out.println("        JScrollPane view size: " + view.getSize());
                                System.out.println("        JScrollPane view components count: "
                                        + ((java.awt.Container) view).getComponentCount());
                            } else {
                                System.out.println("        JScrollPane view is null!");
                            }
                        }
                    }
                }
            }
        } else {
            System.out.println("mainCombinedPanel is null!");
        }
        System.out.println("=== DIAGNOSTIC END ===");

        // Navigate or highlight if search target field is set
        if (com.openbravo.pos.forms.JPanelSystemOverview.searchTargetField != null) {
            final String target = com.openbravo.pos.forms.JPanelSystemOverview.searchTargetField;
            com.openbravo.pos.forms.JPanelSystemOverview.searchTargetField = null; // Clear it

            javax.swing.SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    // Find the JTabbedPane inside mainCombinedPanel
                    javax.swing.JTabbedPane tp = null;
                    if (mainCombinedPanel != null) {
                        for (java.awt.Component c : mainCombinedPanel.getComponents()) {
                            if (c instanceof javax.swing.JTabbedPane) {
                                tp = (javax.swing.JTabbedPane) c;
                                break;
                            }
                        }
                    }

                    if (tp != null) {
                        // All target fields so far are in "Datos Generales" (Tab 0)
                        tp.setSelectedIndex(0);
                    }

                    // Map field names to actual text fields
                    javax.swing.JTextField fieldToHighlight = null;
                    if ("ref".equalsIgnoreCase(target)) {
                        fieldToHighlight = m_jRef;
                    } else if ("code".equalsIgnoreCase(target) || "codigo".equalsIgnoreCase(target)) {
                        fieldToHighlight = m_jCode;
                    } else if ("name".equalsIgnoreCase(target) || "nombre".equalsIgnoreCase(target)
                            || "descripcion".equalsIgnoreCase(target)) {
                        fieldToHighlight = m_jName;
                    } else if ("pricebuy".equalsIgnoreCase(target) || "costo".equalsIgnoreCase(target)
                            || "preciobuy".equalsIgnoreCase(target)) {
                        fieldToHighlight = m_jPriceBuy;
                    } else if ("pricesell".equalsIgnoreCase(target) || "venta".equalsIgnoreCase(target)
                            || "preciosell".equalsIgnoreCase(target)) {
                        fieldToHighlight = m_jPriceSell;
                    } else if ("lote".equalsIgnoreCase(target)) {
                        fieldToHighlight = m_jLote;
                    } else if ("modelo".equalsIgnoreCase(target)) {
                        fieldToHighlight = m_jModelo;
                    } else if ("color".equalsIgnoreCase(target)) {
                        fieldToHighlight = m_jColor;
                    } else if ("voltaje".equalsIgnoreCase(target)) {
                        fieldToHighlight = m_jVoltaje;
                    } else if ("noserie".equalsIgnoreCase(target) || "serie".equalsIgnoreCase(target)) {
                        fieldToHighlight = m_jNoSerie;
                    } else if ("stockcurrent".equalsIgnoreCase(target) || "stock".equalsIgnoreCase(target)
                            || "actual".equalsIgnoreCase(target)) {
                        fieldToHighlight = m_jStockCurrent;
                    } else if ("stockminimum".equalsIgnoreCase(target) || "minimo".equalsIgnoreCase(target)) {
                        fieldToHighlight = m_jStockMinimum;
                    }

                    if (fieldToHighlight != null) {
                        highlightField(fieldToHighlight);
                    }
                }
            });
        }
    }

    private void highlightField(final javax.swing.JTextField field) {
        if (field == null)
            return;
        field.requestFocusInWindow();
        field.selectAll();

        final java.awt.Color originalBg = field.getBackground();
        field.setBackground(new java.awt.Color(254, 243, 199)); // Amber 100

        javax.swing.Timer timer = new javax.swing.Timer(2000, new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                field.setBackground(originalBg);
            }
        });
        timer.setRepeats(false);
        timer.start();
    }

    /**
     *
     */
    @Override
    public void refresh() {
        // Guardar valores de stock si el usuario los cambió manualmente
        try {
            if (productId != null && appView != null) {
                // Primero, guardar el stock si el usuario lo cambió
                // Solo guardar si hay una diferencia entre el valor actual y el inicial
                try {
                    if (m_jStockCurrent != null && m_jStockCurrent.getText() != null
                            && !m_jStockCurrent.getText().trim().isEmpty()) {
                        Double currentStockInField = Formats.DOUBLE.parseValue(m_jStockCurrent.getText());
                        // Solo guardar si el usuario realmente cambió el stock (comparar con valor
                        // inicial)
                        if (initialStockValue != null && Math.abs(currentStockInField - initialStockValue) > 0.0001) {
                            LOGGER.log(Level.FINE, "refresh: Guardando stock porque usuario lo cambió de "
                                    + initialStockValue + " a " + currentStockInField);
                            saveStockValues();
                        }
                    }
                } catch (BasicException e) {
                    LOGGER.log(Level.WARNING, "Error al verificar stock en refresh", e);
                }

                // Luego, recargar el stock desde la base de datos para mostrar el valor
                // actualizado
                showStockTableAutomatically();

                // NO actualizar initialStockValue aquí porque esto se llama después de guardar
                // y actualizar el valor inicial haría que el siguiente producto no detecte
                // cambios
                // initialStockValue solo debe actualizarse cuando se carga un NUEVO producto en
                // setValues()
                // o cuando se muestra la tabla de stock por primera vez en
                // showStockTableAutomatically()
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error al recargar stock en refresh", e);
        }
    }

    /**
     *
     */
    @Override
    public void writeValueEOF() {

        reportlock = true;

        m_jTitle.setText(AppLocal.getIntString("label.recordeof"));

        // Actualizar título del formulario
        if (jLabelProductTitle != null) {
            jLabelProductTitle.setText("NUEVO PRODUCTO");
        }
        if (rbSellVehicle != null) {
            rbSellVehicle.setSelected(true);
            updateTypeVisibility();
        }
        if (rbSellPieza != null) {
            rbSellPieza.setSelected(false);
        }
        if (rbSellPackage != null) {
            rbSellPackage.setSelected(false);
        }
        if (chkUseInventory != null) {
            chkUseInventory.setSelected(true); // Por defecto usa inventario
        }

        // Tab General
        productId = null;
        m_jRef.setText(null);
        m_jCode.setText(null);
        m_jCodetype.setSelectedIndex(0);
        m_jName.setText(null);
        m_CategoryModel.setSelectedKey(null);
        taxcatmodel.setSelectedKey(null);

        m_jPriceBuy.setText("0");
        setPriceSell(null);
        m_SuppliersModel.setSelectedKey(0);

        // Tab Stock
        m_jInCatalog.setSelected(false);
        m_jConstant.setSelected(false);
        m_jCatalogOrder.setText(null);
        m_jPrintTo.setSelectedIndex(1);
        m_jService.setSelected(false);
        m_jCheckWarrantyReceipt.setSelected(false);
        m_jComment.setSelected(false);
        m_jScale.setSelected(false);
        m_jVprice.setSelected(false);
        m_jstockcost.setText("0");
        m_jstockvolume.setText("0");
        m_jPrintKB.setVisible(false);
        m_jSendStatus.setVisible(false);
        m_jStockUnits.setVisible(false);
        m_jdate.setText(null);
        m_jAccumulatesPoints.setSelected(false);
        m_jStockCurrent.setText("0");
        m_jStockMinimum.setText("0");
        m_jStockCurrent.setText("0");
        m_jStockMinimum.setText("0");

        // Tab Image
        m_jImage.setImage(null);

        // Tab Button
        m_jDisplay.setText(null);
        m_jTextTip.setText(null);
        // colourChooser.setText("#000000");

        // Tab Properties
        txtAttributes.setText(null);

        reportlock = false;

        // Tab General
        m_jRef.setEnabled(false);
        m_jCode.setEnabled(false);
        m_jCodetype.setEnabled(false);
        m_jName.setEnabled(false);
        m_jCategory.setEnabled(false);
        m_jAtt.setEnabled(false);
        m_jVerpatrib.setEnabled(false);
        m_jTax.setEnabled(false);
        m_jUom.setEnabled(false);
        m_jPriceBuy.setEnabled(false);
        m_jPriceSell.setEnabled(false);
        m_jPriceSellTax.setEnabled(false);
        m_jmargin.setEnabled(false);
        m_jSupplier.setEnabled(false);

        // Tab Stock
        m_jInCatalog.setEnabled(false);
        m_jConstant.setEnabled(false);
        m_jCatalogOrder.setEnabled(false);
        m_jPrintTo.setEnabled(false);
        m_jService.setEnabled(false);
        m_jCheckWarrantyReceipt.setEnabled(false);
        m_jComment.setEnabled(false);
        m_jScale.setEnabled(false);
        m_jVprice.setEnabled(false);
        m_jstockcost.setEnabled(false);
        m_jstockvolume.setEnabled(false);
        m_jStockCurrent.setEnabled(false);
        m_jStockMinimum.setEnabled(false);
        m_jdate.setEnabled(false);
        if (m_jStockGeneral != null) {
            m_jStockGeneral.setText("0");
            m_jStockGeneral.setEnabled(false);
        }

        // Tab Image
        m_jImage.setEnabled(false);

        // Tab Button
        m_jDisplay.setEnabled(false);
        m_jTextTip.setEnabled(false);
        colourChooser.setEnabled(false);

        // Tab Properties
        txtAttributes.setEnabled(false);

        calculateMargin();
        calculatePriceSellTax();
        calculateGP();
        loadProductAuditHistory(null);
        loadProductStockHistory(null);
    }

    @Override
    public void writeValueInsert() {

        reportlock = true;

        m_jTitle.setText(AppLocal.getIntString("label.recordnew"));

        // Tab General
        productId = UUID.randomUUID().toString();
        m_jRef.setText(null);
        m_jCode.setText(null);
        m_jCodetype.setSelectedIndex(0);
        m_jName.setText(null);
        m_CategoryModel.setSelectedKey("000");
        m_jVerpatrib.setSelected(false);
        taxcatmodel.setSelectedKey("001");
        m_jPriceBuy.setText("0");
        setPriceSell(null);

        // Tab Stock
        m_jInCatalog.setSelected(true);
        m_jConstant.setSelected(false);
        m_jCatalogOrder.setText(null);
        m_jPrintTo.setSelectedIndex(1);
        m_jService.setSelected(false);
        m_jCheckWarrantyReceipt.setSelected(false);
        m_jComment.setSelected(false);
        m_jScale.setSelected(false);
        m_jVprice.setSelected(false);
        m_jstockcost.setText("0");
        m_jstockvolume.setText("0");
        m_jdate.setText(null);
        m_jAccumulatesPoints.setSelected(true);
        m_jLote.setText(null);
        m_jModelo.setText(null);
        m_jColor.setText(null);
        m_jVoltaje.setText(null);
        m_jNoSerie.setText(null);
        // Limpiar campos de inventario al crear nuevo producto
        if (m_jStockCurrent != null)
            m_jStockCurrent.setText("0");
        if (m_jStockMinimum != null)
            m_jStockMinimum.setText("0");
        if (m_jStockGeneral != null)
            m_jStockGeneral.setText("0");

        // Tab Image
        m_jImage.setImage(null);

        // Tab Button
        m_jDisplay.setText(null);
        m_jTextTip.setText(null);
        colourChooser.setEnabled(false);

        // Tab Properties
        txtAttributes.setText(null);

        reportlock = false;

        // Tab General
        m_jRef.setEnabled(true);
        m_jCode.setEnabled(true);
        m_jCodetype.setEnabled(true);
        m_jName.setEnabled(true);
        m_jCategory.setEnabled(true);
        m_jTax.setEnabled(true);
        m_jAtt.setEnabled(true);
        m_jVerpatrib.setEnabled(true);
        m_jUom.setEnabled(true);
        m_jPriceBuy.setEnabled(true);
        m_jPriceSell.setEnabled(true);
        m_jPriceSellTax.setEnabled(true);
        m_jmargin.setEnabled(true);
        m_jSupplier.setEnabled(true);

        // Tab Stock
        m_jInCatalog.setEnabled(true);
        m_jConstant.setEnabled(true);
        m_jCatalogOrder.setEnabled(false);
        m_jPrintTo.setEnabled(true);
        m_jService.setEnabled(true);
        m_jCheckWarrantyReceipt.setEnabled(true);
        m_jComment.setEnabled(true);
        m_jScale.setEnabled(true);
        m_jVprice.setEnabled(true);
        m_jstockcost.setEnabled(true);
        m_jstockvolume.setEnabled(true);
        m_jStockCurrent.setEnabled(true);
        m_jStockMinimum.setEnabled(true);
        m_jStockCurrent.setEnabled(true);
        m_jStockMinimum.setEnabled(true);
        if (m_jStockGeneral != null) {
            m_jStockGeneral.setText("0");
            m_jStockGeneral.setEnabled(true);
        }
        m_jdate.setEnabled(true);

        // Tab Image
        m_jImage.setEnabled(true);

        // Tab Button
        m_jDisplay.setEnabled(true);
        m_jTextTip.setEnabled(true);
        colourChooser.setEnabled(true);

        // Tab Properties
        txtAttributes.setEnabled(true);

        calculateMargin();
        calculatePriceSellTax();
        calculateGP();

        if (isSaving) {
            isSaving = false;
            // Reset dirty immediately
            m_Dirty.setDirty(false);

            // Show confirmation and ensure strict dirty reset after
            javax.swing.SwingUtilities.invokeLater(() -> {
                showSaveConfirmation(false);
                // Force reset dirty again after dialog is closed
                m_Dirty.setDirty(false);
            });
        }
        loadProductAuditHistory(null);
        loadProductStockHistory(null);
    }

    /**
     *
     * @return myprod
     * @throws BasicException
     */
    @Override
    public Object createValue() throws BasicException {

        isSaving = true; // Set flag to indicate we are saving

        Object[] myprod = new Object[38];

        myprod[0] = productId == null ? UUID.randomUUID().toString() : productId;
        myprod[1] = m_jRef.getText();
        myprod[2] = m_jCode.getText();
        myprod[3] = m_jCodetype.getSelectedItem();
        myprod[4] = m_jName.getText();
        myprod[5] = readCurrency(m_jPriceBuy.getText());
        myprod[6] = pricesell;
        myprod[7] = m_CategoryModel.getSelectedKey();
        myprod[8] = taxcatmodel.getSelectedKey();
        myprod[9] = null;
        myprod[10] = readCurrency(m_jstockcost.getText());
        myprod[11] = Formats.DOUBLE.parseValue(m_jstockvolume.getText());
        myprod[12] = m_jImage.getImage();
        myprod[13] = m_jComment.isSelected();
        myprod[14] = m_jScale.isSelected();
        myprod[15] = m_jConstant.isSelected();
        myprod[16] = m_jPrintKB.isSelected();
        myprod[17] = m_jSendStatus.isSelected();
        myprod[18] = m_jService.isSelected();
        myprod[19] = Formats.BYTEA.parseValue(txtAttributes.getText());
        myprod[20] = m_jDisplay.getText();
        myprod[21] = m_jVprice.isSelected();
        myprod[22] = m_jVerpatrib.isSelected();
        myprod[23] = m_jTextTip.getText();
        myprod[24] = m_jCheckWarrantyReceipt.isSelected();
        myprod[25] = Formats.DOUBLE.parseValue(m_jStockUnits.getText());
        myprod[26] = m_jPrintTo.getSelectedItem().toString();
        myprod[27] = m_SuppliersModel.getSelectedKey();
        myprod[28] = "0";
        myprod[29] = Formats.TIMESTAMP.parseValue(m_jdate.getText());

        myprod[30] = m_jInCatalog.isSelected();
        myprod[31] = Formats.INT.parseValue(m_jCatalogOrder.getText());
        myprod[32] = m_jAccumulatesPoints.isSelected();
        myprod[33] = m_jLote.getText();
        myprod[34] = m_jModelo.getText();
        myprod[35] = m_jColor.getText();
        myprod[36] = m_jVoltaje.getText();
        myprod[37] = m_jNoSerie.getText();

        // Sebastian - Registrar historial de precios si hubo cambio
        try {
            Double currentPriceBuy = (Double) myprod[5];
            Double currentPriceSell = (Double) myprod[6];

            if (initialPriceBuy != null && initialPriceSell != null &&
                    (!initialPriceBuy.equals(currentPriceBuy) || !initialPriceSell.equals(currentPriceSell))) {

                ProductPriceHistory history = new ProductPriceHistory(
                        (String) myprod[0],
                        currentPriceBuy,
                        currentPriceSell,
                        appView.getAppUserView().getUser().getId());
                dlSales.recordPriceHistory(history);

                // Actualizar iniciales para el próximo guardado sin recargar
                initialPriceBuy = currentPriceBuy;
                initialPriceSell = currentPriceSell;
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error al registrar historial de precios", e);
        }

        System.out.println("DEBUG createValue: ACCUMULATES_POINTS = " + myprod[32] + " (checkbox state: "
                + m_jAccumulatesPoints.isSelected() + ")");

        // IMPORTANTE: Guardar el stock ANTES de retornar el objeto
        // Esto asegura que el stock se guarde siempre, incluso si refresh() no se llama
        // o si initialStockValue se actualizó incorrectamente
        if (productId != null && appView != null) {
            try {
                LOGGER.log(Level.INFO, "createValue: Llamando saveStockValues() para asegurar que el stock se guarde");
                saveStockValues();
                LOGGER.log(Level.INFO, "createValue: saveStockValues() completado");
            } catch (BasicException e) {
                LOGGER.log(Level.WARNING, "createValue: Error al guardar stock: " + e.getMessage(), e);
                // No lanzar excepción para no bloquear el guardado del producto
            }
        }

        return myprod;
    }

    private void updateTypeVisibility() {
        boolean isVehicle = rbSellVehicle.isSelected();

        // Campos de metadatos (Solo para vehículos)
        if (jLabelLote != null)
            jLabelLote.setVisible(isVehicle);
        if (m_jLote != null)
            m_jLote.setVisible(isVehicle);
        if (jLabelModelo != null)
            jLabelModelo.setVisible(isVehicle);
        if (m_jModelo != null)
            m_jModelo.setVisible(isVehicle);
        if (jLabelColor != null)
            jLabelColor.setVisible(isVehicle);
        if (m_jColor != null)
            m_jColor.setVisible(isVehicle);
        if (jLabelVoltaje != null)
            jLabelVoltaje.setVisible(isVehicle);
        if (m_jVoltaje != null)
            m_jVoltaje.setVisible(isVehicle);
        if (jLabelNoSerie != null)
            jLabelNoSerie.setVisible(isVehicle);
        if (m_jNoSerie != null)
            m_jNoSerie.setVisible(isVehicle);

        // Refrescar panel para ajustar layout si es necesario
        if (mainFieldsPanel != null) {
            mainFieldsPanel.revalidate();
            mainFieldsPanel.repaint();
        }
    }

    /**
     *
     * @param value
     */
    @Override
    public void writeValueEdit(Object value) {

        reportlock = true;

        setValues(value);

        txtAttributes.setCaretPosition(0);
        reportlock = false;

        // Tab General
        m_jRef.setEnabled(true);
        m_jCode.setEnabled(true);
        m_jCodetype.setEnabled(true);
        m_jName.setEnabled(true);
        m_jCategory.setEnabled(true);
        m_jTax.setEnabled(true);
        m_jAtt.setEnabled(true);
        m_jVerpatrib.setEnabled(true);
        m_jUom.setEnabled(true);
        m_jPriceBuy.setEnabled(true);
        m_jPriceSell.setEnabled(true);
        m_jPriceSellTax.setEnabled(true);
        m_jmargin.setEnabled(true);
        m_jSupplier.setEnabled(true);

        // Tab Stock
        m_jInCatalog.setEnabled(true);
        m_jConstant.setEnabled(true);
        m_jCatalogOrder.setEnabled(m_jInCatalog.isSelected());
        m_jPrintTo.setEnabled(true);
        m_jService.setEnabled(true);
        m_jCheckWarrantyReceipt.setEnabled(true);
        m_jComment.setEnabled(true);
        m_jScale.setEnabled(true);
        m_jVprice.setEnabled(true);
        m_jstockcost.setEnabled(true);
        m_jstockvolume.setEnabled(true);
        m_jdate.setEnabled(true);
        m_jAccumulatesPoints.setEnabled(true);
        m_jStockCurrent.setEnabled(true);
        m_jStockMinimum.setEnabled(true);
        // Actualizar si los campos de stock son editables basándose en si usa
        // inventario
        boolean usesInventory = !m_jService.isSelected();
        m_jStockCurrent.setEditable(usesInventory);
        m_jStockMinimum.setEditable(usesInventory);
        // Siempre cargar valores de stock desde la base de datos cuando se abre el
        // editor
        showStockTableAutomatically();

        // Tab Image
        m_jImage.setEnabled(true);

        // Tab Button
        m_jDisplay.setEnabled(true);
        m_jTextTip.setEnabled(true);
        colourChooser.setEnabled(true);
        setButtonHTML();

        resetTranxTable();

        // Tab Properties
        txtAttributes.setEnabled(true);

        calculateMargin();
        calculatePriceSellTax();
        calculateGP();

        if (isSaving) {
            isSaving = false;
            // Reset dirty immediately to handle any synchronous updates
            m_Dirty.setDirty(false);

            // Show confirmation and ensure strict dirty reset after
            javax.swing.SwingUtilities.invokeLater(() -> {
                showSaveConfirmation(true);
                // Force reset dirty again after dialog is closed to handle any latent events
                m_Dirty.setDirty(false);
            });
        }
    }

    private void showSaveConfirmation(boolean isUpdate) {
        // Create a custom panel for the confirmation dialog
        javax.swing.JPanel panel = new javax.swing.JPanel();
        panel.setLayout(new java.awt.GridBagLayout());

        java.awt.GridBagConstraints gbc = new java.awt.GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.anchor = java.awt.GridBagConstraints.WEST;
        gbc.insets = new java.awt.Insets(5, 5, 5, 10);
        gbc.fill = java.awt.GridBagConstraints.HORIZONTAL;

        // Title/Header
        javax.swing.JLabel lblTitle = new javax.swing.JLabel(
                isUpdate ? "<html><h2>✅ Producto Actualizado Exitosamente</h2></html>"
                        : "<html><h2>✅ Producto Creado Exitosamente</h2></html>");
        gbc.gridwidth = 2;
        panel.add(lblTitle, gbc);

        gbc.gridy++;
        gbc.gridwidth = 1;
        panel.add(new javax.swing.JSeparator(), gbc);

        // Add Data Fields with styles
        String styles = "font-size: 11px;";

        gbc.gridy++;
        addDetailRow(panel, gbc, "Referencia:", m_jRef.getText());

        gbc.gridy++;
        addDetailRow(panel, gbc, "Nombre:", "<html><b>" + m_jName.getText() + "</b></html>");

        gbc.gridy++;
        addDetailRow(panel, gbc, "Precio Venta:", m_jPriceSell.getText());

        gbc.gridy++;
        String tax = m_jTax.getSelectedItem() != null ? m_jTax.getSelectedItem().toString() : "-";
        addDetailRow(panel, gbc, "Impuesto:", tax);

        gbc.gridy++;
        addDetailRow(panel, gbc, "Categoría:",
                m_CategoryModel.getSelectedItem() != null ? m_CategoryModel.getSelectedItem().toString() : "");

        if (m_jStockCurrent != null && isUpdate) {
            gbc.gridy++;
            addDetailRow(panel, gbc, "Stock Actual:", "<html><b>" + m_jStockCurrent.getText() + "</b></html>");
        }

        // Show the dialog
        javax.swing.JOptionPane.showMessageDialog(this, panel, isUpdate ? "Producto Actualizado" : "Producto Creado",
                javax.swing.JOptionPane.PLAIN_MESSAGE);
    }

    private void addDetailRow(javax.swing.JPanel panel, java.awt.GridBagConstraints gbc, String label, String value) {
        gbc.gridx = 0;
        gbc.weightx = 0.0;
        javax.swing.JLabel lbl = new javax.swing.JLabel("<html>" + label + "</html>");
        panel.add(lbl, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        javax.swing.JLabel val = new javax.swing.JLabel("<html>" + value + "</html>");
        panel.add(val, gbc);
    }

    private void setValues(Object value) {
        Object[] myprod = (Object[]) value;

        m_jTitle.setText(Formats.STRING.formatValue((String) myprod[1])
                + " - " + Formats.STRING.formatValue((String) myprod[4]));

        // Actualizar título del formulario
        if (jLabelProductTitle != null) {
            jLabelProductTitle.setText("EDITAR PRODUCTO");
        }
        productId = (String) myprod[0];
        m_jRef.setText(Formats.STRING.formatValue((String) myprod[1]));
        m_jCode.setText(Formats.STRING.formatValue((String) myprod[2]));
        m_jCodetype.setSelectedItem(myprod[3]);
        m_jName.setText(Formats.STRING.formatValue((String) myprod[4]));
        m_jPriceBuy.setText(Formats.CURRENCY.formatValue((Double) myprod[5]));
        setPriceSell((Double) myprod[6]);
        m_CategoryModel.setSelectedKey(myprod[7]);
        taxcatmodel.setSelectedKey(myprod[8]);
        m_jstockcost.setText(Formats.CURRENCY.formatValue((Double) myprod[10]));
        m_jstockvolume.setText(Formats.DOUBLE.formatValue((Double) myprod[11]));
        // JG 3 feb 16 speedup m_jImage.setImage((BufferedImage) myprod[12]);
        m_jImage.setImage(findImage(productId));
        m_jComment.setSelected(((Boolean) myprod[13]));
        m_jScale.setSelected(((Boolean) myprod[14]));
        m_jConstant.setSelected(((Boolean) myprod[15]));

        // Actualizar tipo de producto (Vehículo vs Pieza) basado en m_jScale
        if (rbSellPieza != null && rbSellVehicle != null) {
            if (m_jScale.isSelected()) {
                rbSellPieza.setSelected(true);
            } else {
                rbSellVehicle.setSelected(true);
            }
            updateTypeVisibility();
        }

        m_jPrintKB.setSelected(((Boolean) myprod[16]));
        m_jSendStatus.setSelected(((Boolean) myprod[17]));
        m_jService.setSelected(((Boolean) myprod[18]));
        // Actualizar checkbox de inventario y campos de stock después de establecer
        // m_jService
        boolean usesInventory = !m_jService.isSelected();
        if (chkUseInventory != null) {
            chkUseInventory.setSelected(usesInventory);
        }
        // Asegurar que los campos de stock sean editables si usa inventario
        m_jStockCurrent.setEditable(usesInventory);
        m_jStockMinimum.setEditable(usesInventory);
        txtAttributes.setText(Formats.BYTEA.formatValue((byte[]) myprod[19]));
        m_jDisplay.setText(Formats.STRING.formatValue((String) myprod[20]));
        m_jVprice.setSelected(((Boolean) myprod[21]));
        m_jVerpatrib.setSelected(((Boolean) myprod[22]));
        m_jTextTip.setText(Formats.STRING.formatValue((String) myprod[23]));
        m_jCheckWarrantyReceipt.setSelected(((Boolean) myprod[24]));
        m_jStockUnits.setText(Formats.DOUBLE.formatValue((Double) myprod[25]));
        m_jPrintTo.setSelectedItem(myprod[26]);
        m_SuppliersModel.setSelectedKey(myprod[27]);
        updateSupplierProductsTable();
        m_jdate.setText(Formats.DATE.formatValue((Date) myprod[29]));
        m_jInCatalog.setSelected(((Boolean) myprod[30]));
        m_jCatalogOrder.setText(Formats.INT.formatValue((Integer) myprod[31]));
        // Verificar si el campo accumulates_points existe (para compatibilidad con
        // productos antiguos)
        if (myprod.length > 32 && myprod[32] != null) {
            m_jAccumulatesPoints.setSelected(((Boolean) myprod[32]));
            m_jLote.setText(Formats.STRING.formatValue((String) myprod[33]));
            m_jModelo.setText(Formats.STRING.formatValue((String) myprod[34]));
            m_jColor.setText(Formats.STRING.formatValue((String) myprod[35]));
            m_jVoltaje.setText(Formats.STRING.formatValue((String) myprod[36]));
            m_jNoSerie.setText(Formats.STRING.formatValue((String) myprod[37]));
            System.out.println("DEBUG setValues: ACCUMULATES_POINTS desde BD = " + myprod[32] + " (array length: "
                    + myprod.length + ")");
        } else {
            // Por defecto, productos antiguos acumulan puntos
            m_jAccumulatesPoints.setSelected(true);
            m_jLote.setText("");
            m_jModelo.setText("");
            m_jColor.setText("");
            m_jVoltaje.setText("");
            m_jNoSerie.setText("");
            System.out.println(
                    "DEBUG setValues: Campo ACCUMULATES_POINTS no encontrado, usando default TRUE (array length: "
                            + myprod.length + ")");
        }

        // RESETEAR initialStockValue cuando se carga un nuevo producto
        // Esto asegura que se detecten cambios correctamente para cada producto
        initialStockValue = null;
        LOGGER.log(Level.INFO, "setValues: Reseteado initialStockValue para nuevo producto: " + productId);

        // Limpiar campos de stock inmediatamente para no mostrar valores del producto
        // anterior
        if (m_jStockCurrent != null)
            m_jStockCurrent.setText("0");
        if (m_jStockMinimum != null)
            m_jStockMinimum.setText("0");
        if (m_jStockGeneral != null)
            m_jStockGeneral.setText("0");

        // Cargar valores de stock
        showStockTableAutomatically();

        // Calcular ganancia después de cargar los valores
        calculateGP();

        // Sebastian - Guardar valores iniciales de precios
        initialPriceBuy = (Double) myprod[5];
        initialPriceSell = (Double) myprod[6];

        // Sebastian - Actualizar datos de variación
        try {
            List<ProductPriceHistory> history = dlSales.getProductPriceHistory(productId);
            m_variationPanel.setHistory(history, productId, initialPriceBuy, initialPriceSell);
        } catch (BasicException e) {
            LOGGER.log(Level.WARNING, "No se pudo cargar el historial de precios", e);
        }

        loadProductAuditHistory(productId);
        loadProductStockHistory(productId);
    }

    private void loadProductAuditHistory(String pId) {
        if (auditHistoryTableModel == null) {
            return;
        }
        auditHistoryTableModel.setRowCount(0);
        if (txtAuditDetails != null) {
            txtAuditDetails.setText("");
        }
        if (pId == null || pId.isEmpty()) {
            return;
        }

        final java.util.List<Object[]> dataList = new java.util.ArrayList<>();
        java.sql.Connection conn = null;
        java.sql.PreparedStatement pstmt = null;
        java.sql.ResultSet rs = null;
        try {
            conn = appView.getSession().getConnection();
            pstmt = conn.prepareStatement(
                    "SELECT USER_NAME, EVENT_TYPE, EVENT_DATE, DETAILS FROM AUDIT_LOG WHERE ENTITY_ID = ? ORDER BY EVENT_DATE DESC");
            pstmt.setString(1, pId);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                dataList.add(new Object[] {
                        rs.getString("USER_NAME"),
                        rs.getString("EVENT_TYPE"),
                        rs.getTimestamp("EVENT_DATE"),
                        rs.getString("DETAILS")
                });
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error loading audit logs for product " + pId, e);
        } finally {
            if (rs != null)
                try {
                    rs.close();
                } catch (Exception e) {
                }
            if (pstmt != null)
                try {
                    pstmt.close();
                } catch (Exception e) {
                }
        }

        jTableProductAuditHistory.putClientProperty("auditDataList", dataList);

        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        for (Object[] rowData : dataList) {
            String formattedDate = rowData[2] != null ? sdf.format((java.util.Date) rowData[2]) : "";
            auditHistoryTableModel.addRow(new Object[] {
                    formattedDate,
                    rowData[0],
                    rowData[1],
                    rowData[3]
            });
        }

        if (auditHistoryTableModel.getRowCount() > 0) {
            jTableProductAuditHistory.setRowSelectionInterval(0, 0);
        }
    }

    private void loadProductStockHistory(String pId) {
        if (stockHistoryTableModel == null) {
            return;
        }
        stockHistoryTableModel.setRowCount(0);
        if (pId == null || pId.isEmpty()) {
            return;
        }

        java.sql.Connection conn = null;
        java.sql.PreparedStatement pstmt = null;
        java.sql.ResultSet rs = null;
        try {
            conn = appView.getSession().getConnection();
            pstmt = conn.prepareStatement(
                    "SELECT sd.DATENEW, sd.REASON, loc.NAME AS LOCATION_NAME, sd.UNITS, sd.PRICE, sd.AppUser, sup.NAME AS SUPPLIER_NAME, sd.SUPPLIERDOC "
                            +
                            "FROM stockdiary sd " +
                            "INNER JOIN locations loc ON sd.LOCATION = loc.ID " +
                            "LEFT JOIN suppliers sup ON sd.SUPPLIER = sup.ID " +
                            "WHERE sd.PRODUCT = ? " +
                            "ORDER BY sd.DATENEW DESC");
            pstmt.setString(1, pId);
            rs = pstmt.executeQuery();

            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            java.text.DecimalFormat decFormat = new java.text.DecimalFormat("$#,##0.00");

            while (rs.next()) {
                java.util.Date dateNew = rs.getTimestamp("DATENEW");
                String formattedDate = dateNew != null ? sdf.format(dateNew) : "";

                int reason = rs.getInt("REASON");
                String reasonText = getReasonText(reason);

                String location = rs.getString("LOCATION_NAME");
                double units = rs.getDouble("UNITS");
                double price = rs.getDouble("PRICE");
                String user = rs.getString("AppUser");
                String supplier = rs.getString("SUPPLIER_NAME");
                String doc = rs.getString("SUPPLIERDOC");

                String unitsStr = (units > 0 ? "+" : "") + Formats.DOUBLE.formatValue(units);
                String priceStr = decFormat.format(price);

                double total = units * price;
                String totalStr = (total > 0 ? "+" : total < 0 ? "-" : "") + decFormat.format(Math.abs(total));

                stockHistoryTableModel.addRow(new Object[] {
                        formattedDate,
                        reasonText,
                        location,
                        unitsStr,
                        priceStr,
                        totalStr,
                        user != null ? user : "",
                        supplier != null ? supplier : "",
                        doc != null ? doc : ""
                });
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error loading stock history for product " + pId, e);
        } finally {
            if (rs != null)
                try {
                    rs.close();
                } catch (Exception e) {
                }
            if (pstmt != null)
                try {
                    pstmt.close();
                } catch (Exception e) {
                }
        }
    }

    private String getReasonText(int reason) {
        switch (reason) {
            case 1:
                return "Entrada (Compra)";
            case 2:
                return "Salida (Venta)";
            case 3:
                return "Salida (Rotura/Merma)";
            case 4:
                return "Salida (Otro)";
            case -1:
                return "Entrada (Ajuste)";
            case -2:
                return "Salida (Ajuste)";
            case -3:
                return "Entrada (Transferencia)";
            case -4:
                return "Salida (Transferencia)";
            case -5:
                return "Entrada (Devolución)";
            default:
                return "Otro (" + reason + ")";
        }
    }

    /**
     *
     * @param value
     */
    @Override
    public void writeValueDelete(Object value) {

        reportlock = true;
        setValues(value);

        txtAttributes.setCaretPosition(0);

        reportlock = false;

        // Tab General
        m_jRef.setEnabled(false);
        m_jCode.setEnabled(false);
        m_jCodetype.setEnabled(false);
        m_jName.setEnabled(false);
        m_jCategory.setEnabled(false);
        m_jTax.setEnabled(false);
        m_jAtt.setEnabled(false);
        m_jVerpatrib.setEnabled(false);
        m_jUom.setEnabled(false);
        m_jPriceBuy.setEnabled(false);
        m_jPriceSell.setEnabled(false);
        m_jPriceSellTax.setEnabled(false);
        m_jmargin.setEnabled(false);
        m_jPrintTo.setEnabled(false);

        // Tab Stock
        m_jInCatalog.setEnabled(false);
        m_jConstant.setEnabled(false);
        m_jCatalogOrder.setEnabled(false);
        m_jSupplier.setEnabled(false);
        m_jService.setEnabled(false);
        m_jCheckWarrantyReceipt.setEnabled(false);
        m_jComment.setEnabled(false);
        m_jScale.setEnabled(false);
        m_jVprice.setEnabled(false);
        m_jstockcost.setEnabled(false);
        m_jstockvolume.setEnabled(false);
        m_jStockCurrent.setText("0");
        m_jStockMinimum.setText("0");
        m_jStockCurrent.setEnabled(false);
        m_jStockMinimum.setEnabled(false);
        m_jdate.setEnabled(false);
        m_jAccumulatesPoints.setEnabled(false);
        if (m_jStockGeneral != null) {
            m_jStockGeneral.setText("0");
            m_jStockGeneral.setEnabled(false);
        }

        // Tab Image
        m_jImage.setEnabled(false);

        // Tab Button
        m_jDisplay.setEnabled(false);
        m_jTextTip.setEnabled(false);
        colourChooser.setEnabled(false);

        // Tab Properties
        txtAttributes.setEnabled(false);

        calculateMargin();
        calculatePriceSellTax();
        calculateGP();
    }

    public void resetTranxTable() {
        // Método vacío - la tabla de stock fue reemplazada por campos simples
    }

    /**
     *
     * @return this
     */
    @Override
    public Component getComponent() {
        return this;
    }

    private void setCode() {

        Long lDateTime = new Date().getTime();

        if (!reportlock) {
            reportlock = true;

            if (m_jRef == null) {
                m_jCode.setText(Long.toString(lDateTime));
            } else {
                if (m_jCode.getText() == null || "".equals(m_jCode.getText())) {
                    m_jCode.setText(m_jRef.getText());
                }
            }
            reportlock = false;
        }
    }

    private List<ProductStock> getProductOfName(String pId) {
        List<ProductStock> filteredList = new ArrayList<>();

        try {
            // Obtener la ubicación de inventario actual
            String inventoryLocation = appView != null ? appView.getInventoryLocation() : "0";
            if (inventoryLocation == null) {
                inventoryLocation = "0";
            }

            // Consultar directamente el stock actual sin usar MAX() para obtener el valor
            // más reciente
            double currentStock = dlSales.findProductStock(inventoryLocation, pId, null);
            LOGGER.log(Level.FINE, "getProductOfName: Stock actual consultado directamente: " + currentStock
                    + " para producto: " + pId + " en ubicación: " + inventoryLocation);

            // Obtener la información de la ubicación
            LocationInfo selectedLocation = new LocationInfo();
            selectedLocation.setID(inventoryLocation);
            selectedLocation.setName("General");
            try {
                List<LocationInfo> locationsList = dlSales.getLocationsList().list();
                for (LocationInfo loc : locationsList) {
                    if (loc.getID().equals(inventoryLocation)) {
                        selectedLocation = loc;
                        break;
                    }
                }
            } catch (BasicException ex) {
                LOGGER.log(Level.WARNING, "Error al obtener información de ubicación", ex);
            }

            // Obtener información adicional del producto (precio, mínimo, máximo)
            double priceBuy = 0.0;
            double priceSell = 0.0;
            double minimum = 0.0;
            double maximum = 0.0;
            Date memodate = new Date();

            try {
                // Intentar obtener información adicional usando getProductStockList como
                // respaldo
                List<ProductStock> productStockList = dlSales.getProductStockList(pId);
                for (ProductStock productStock : productStockList) {
                    if (productStock.getProductId().equals(pId)
                            && productStock.getLocation().equals(inventoryLocation)) {
                        if (productStock.getPriceBuy() != null)
                            priceBuy = productStock.getPriceBuy();
                        if (productStock.getPriceSell() != null)
                            priceSell = productStock.getPriceSell();
                        if (productStock.getMinimum() != null)
                            minimum = productStock.getMinimum();
                        if (productStock.getMaximum() != null)
                            maximum = productStock.getMaximum();
                        if (productStock.getMemoDate() != null)
                            memodate = productStock.getMemoDate();
                        break;
                    }
                }
            } catch (BasicException ex) {
                LOGGER.log(Level.WARNING, "Error al obtener información adicional del producto", ex);
            }

            // Crear ProductStock con el stock actual consultado directamente
            ProductStock currentStockInfo = new ProductStock(
                    pId,
                    selectedLocation.getID(),
                    selectedLocation.getName(),
                    selectedLocation.getAddress(),
                    selectedLocation.getPhone(),
                    currentStock, // Usar el stock consultado directamente
                    minimum,
                    maximum,
                    priceBuy,
                    priceSell,
                    memodate);
            filteredList.add(currentStockInfo);

        } catch (BasicException ex) {
            LOGGER.log(Level.SEVERE, "Error al consultar stock para producto: " + pId, ex);
            // Si falla, intentar usar el método original como respaldo
            try {
                List<ProductStock> productStockList = dlSales.getProductStockList(pId);
                for (ProductStock productStock : productStockList) {
                    if (productStock.getProductId().equals(pId)) {
                        filteredList.add(productStock);
                    }
                }
            } catch (BasicException ex2) {
                LOGGER.log(Level.SEVERE, "Error al obtener stock usando método original", ex2);
            }
        }

        return filteredList;
    }

    public AppView getAppView() {
        return appView;
    }

    public void setAppView(AppView appView) {
        this.appView = appView;
    }

    class StockTableModel extends AbstractTableModel {

        String loc = AppLocal.getIntString("label.tblProdHeaderCol1");
        String qty = AppLocal.getIntString("label.tblProdHeaderCol2");
        String min = AppLocal.getIntString("label.tblProdHeaderCol3");
        String max = AppLocal.getIntString("label.tblProdHeaderCol4");

        List<ProductStock> stockList;
        String[] columnNames = { loc, qty, min, max };
        private String currentProductId; // Guardar el ID del producto actual

        public StockTableModel(List<ProductStock> list) {
            stockList = list;
            // Guardar el ID del producto si hay elementos
            if (list != null && !list.isEmpty()) {
                currentProductId = list.get(0).getProductId();
            }
        }

        @Override
        public int getColumnCount() {
            return 4;
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
                    return productStock.getProductId();
                default:
                    return "";
            }
        }

        @Override
        public void setValueAt(Object value, int row, int column) {
            ProductStock productStock = stockList.get(row);
            Double newValue;

            try {
                // Convertir el valor a Double
                if (value instanceof Double) {
                    newValue = (Double) value;
                } else if (value instanceof String) {
                    newValue = Double.parseDouble(((String) value).trim());
                } else {
                    newValue = Double.parseDouble(value.toString());
                }

                // Manejar null como 0
                if (newValue == null) {
                    newValue = 0.0;
                }

                if (column == 1) {
                    // Columna de cantidad (Current)
                    Double originalQuantity = productStock.getUnits();
                    if (originalQuantity == null) {
                        originalQuantity = 0.0;
                    }

                    // Solo actualizar si hay cambio
                    if (!originalQuantity.equals(newValue)) {
                        double quantityDifference = newValue - originalQuantity;

                        if (Math.abs(quantityDifference) > 0.0001 || originalQuantity == 0.0) {
                            // Actualizar el modelo
                            productStock.setUnits(newValue);
                            fireTableCellUpdated(row, column);

                            // Actualizar en la base de datos
                            updateStockInDatabase(
                                    productStock.getProductId(),
                                    productStock.getLocation(),
                                    originalQuantity,
                                    newValue,
                                    quantityDifference);
                        }
                    }
                } else if (column == 2) {
                    // Columna de mínimo (Minimum)
                    Double originalMinimum = productStock.getMinimum();
                    if (originalMinimum == null) {
                        originalMinimum = 0.0;
                    }

                    // Solo actualizar si hay cambio
                    if (!originalMinimum.equals(newValue)) {
                        // Actualizar el modelo
                        productStock.setMinimum(newValue);
                        fireTableCellUpdated(row, column);

                        // Actualizar en la base de datos
                        ProductsEditor.this.updateStockLevelInDatabase(
                                productStock.getProductId(),
                                productStock.getLocation(),
                                newValue,
                                productStock.getMaximum());
                    }
                } else if (column == 3) {
                    // Columna de máximo (Maximum)
                    Double originalMaximum = productStock.getMaximum();
                    if (originalMaximum == null) {
                        originalMaximum = 0.0;
                    }

                    // Solo actualizar si hay cambio
                    if (!originalMaximum.equals(newValue)) {
                        // Actualizar el modelo
                        productStock.setMaximum(newValue);
                        fireTableCellUpdated(row, column);

                        // Actualizar en la base de datos
                        ProductsEditor.this.updateStockLevelInDatabase(
                                productStock.getProductId(),
                                productStock.getLocation(),
                                productStock.getMinimum(),
                                newValue);
                    }
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(ProductsEditor.this,
                        AppLocal.getIntString("message.invalidnumber"),
                        AppLocal.getIntString("message.title"),
                        JOptionPane.ERROR_MESSAGE);
            } catch (BasicException e) {
                LOGGER.log(Level.SEVERE, "Error al guardar datos de stock", e);
                MessageInf msg = new MessageInf(MessageInf.SGN_WARNING,
                        AppLocal.getIntString("message.cannotsaveinventorydata") + ": " + e.getMessage(), e);
                msg.show(ProductsEditor.this);
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error inesperado al guardar datos de stock", e);
                JOptionPane.showMessageDialog(ProductsEditor.this,
                        "Error al guardar: " + e.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            // Las columnas de cantidad (1), mínimo (2) y máximo (3) son editables
            return column == 1 || column == 2 || column == 3;
        }

        @Override
        public Class<?> getColumnClass(int column) {
            if (column == 1 || column == 2 || column == 3) {
                return Double.class;
            }
            return String.class;
        }

        @Override
        public String getColumnName(int col) {
            return columnNames[col];
        }
    }

    /**
     * Actualiza el stock en la base de datos cuando se edita directamente en la
     * tabla
     */
    private void updateStockInDatabase(String productId, String locationName, Double originalQuantity,
            Double newQuantity, double quantityDifference) throws BasicException {

        // Obtener el ID de la ubicación desde el nombre
        String locationId = null;
        try {
            List<LocationInfo> locations = dlSales.getLocationsList().list();
            for (LocationInfo loc : locations) {
                if (loc.getName().equals(locationName)) {
                    locationId = loc.getID();
                    break;
                }
            }

            // Si no se encuentra, usar la ubicación de inventario principal
            if (locationId == null) {
                locationId = appView.getInventoryLocation();
            }
        } catch (BasicException ex) {
            locationId = appView.getInventoryLocation();
        }

        // Obtener el stock actual de la BD antes de actualizar
        Double currentStockInDB = 0.0;
        try {
            PreparedSentence getCurrentStock = new PreparedSentence(dlSales.getSession(),
                    "SELECT UNITS FROM stockcurrent WHERE LOCATION = ? AND PRODUCT = ? AND (ATTRIBUTESETINSTANCE_ID IS NULL OR ATTRIBUTESETINSTANCE_ID = '')",
                    new SerializerWriteBasicExt(new Datas[] { Datas.STRING, Datas.STRING }, new int[] { 0, 1 }),
                    SerializerReadDouble.INSTANCE);
            Double result = (Double) getCurrentStock.find(new Object[] { locationId, productId });
            if (result != null) {
                currentStockInDB = result;
            }
        } catch (BasicException e) {
            // Si no existe, currentStockInDB queda en 0.0
        }

        // Calcular la diferencia entre el valor nuevo y el actual en BD
        double stockDifference = newQuantity - currentStockInDB;

        // Actualizar o insertar en stockcurrent - AJUSTAR en lugar de sobrescribir para
        // preservar cambios de ventas
        if (Math.abs(stockDifference) > 0.0001) {
            try {
                if (currentStockInDB > 0) {
                    // Si ya existe stock, ajustar con la diferencia (preserva cambios de ventas)
                    PreparedSentence updateStmt = new PreparedSentence(dlSales.getSession(),
                            "UPDATE stockcurrent SET UNITS = (UNITS + ?) WHERE LOCATION = ? AND PRODUCT = ? AND (ATTRIBUTESETINSTANCE_ID IS NULL OR ATTRIBUTESETINSTANCE_ID = '')",
                            new SerializerWriteBasicExt(new Datas[] { Datas.DOUBLE, Datas.STRING, Datas.STRING },
                                    new int[] { 0, 1, 2 }));

                    int rowsAffected = updateStmt.exec(new Object[] { stockDifference, locationId, productId });

                    // Si no se actualizó ninguna fila, crear la entrada
                    if (rowsAffected == 0) {
                        PreparedSentence insertStmt = new PreparedSentence(dlSales.getSession(),
                                "INSERT INTO stockcurrent (LOCATION, PRODUCT, ATTRIBUTESETINSTANCE_ID, UNITS) VALUES (?, ?, NULL, ?)",
                                new SerializerWriteBasicExt(new Datas[] { Datas.STRING, Datas.STRING, Datas.DOUBLE },
                                        new int[] { 0, 1, 2 }));
                        insertStmt.exec(new Object[] { locationId, productId, newQuantity });
                    }
                } else {
                    // Si no existe stock, insertar con el valor nuevo
                    PreparedSentence insertStmt = new PreparedSentence(dlSales.getSession(),
                            "INSERT INTO stockcurrent (LOCATION, PRODUCT, ATTRIBUTESETINSTANCE_ID, UNITS) VALUES (?, ?, NULL, ?)",
                            new SerializerWriteBasicExt(new Datas[] { Datas.STRING, Datas.STRING, Datas.DOUBLE },
                                    new int[] { 0, 1, 2 }));
                    insertStmt.exec(new Object[] { locationId, productId, newQuantity });
                }
            } catch (BasicException e) {
                // Si falla, intentar insertar
                try {
                    PreparedSentence insertStmt = new PreparedSentence(dlSales.getSession(),
                            "INSERT INTO stockcurrent (LOCATION, PRODUCT, ATTRIBUTESETINSTANCE_ID, UNITS) VALUES (?, ?, NULL, ?)",
                            new SerializerWriteBasicExt(new Datas[] { Datas.STRING, Datas.STRING, Datas.DOUBLE },
                                    new int[] { 0, 1, 2 }));
                    insertStmt.exec(new Object[] { locationId, productId, newQuantity });
                } catch (BasicException ex2) {
                    // Si ya existe, ajustar con la diferencia
                    PreparedSentence updateStmt = new PreparedSentence(dlSales.getSession(),
                            "UPDATE stockcurrent SET UNITS = (UNITS + ?) WHERE LOCATION = ? AND PRODUCT = ? AND (ATTRIBUTESETINSTANCE_ID IS NULL OR ATTRIBUTESETINSTANCE_ID = '')",
                            new SerializerWriteBasicExt(new Datas[] { Datas.DOUBLE, Datas.STRING, Datas.STRING },
                                    new int[] { 0, 1, 2 }));
                    updateStmt.exec(new Object[] { stockDifference, locationId, productId });
                }
            }
        }
        // Si no hay diferencia, no actualizar el stock (preserva el stock actual,
        // incluyendo descuentos de ventas)

        // Obtener usuario y fecha
        String userName = appView.getAppUserView().getUser().getName();
        Date currentDate = DateUtils.getTodayMinutes();

        // Determinar el motivo del movimiento
        Object reasonKey = stockDifference > 0
                ? MovementReason.IN_MOVEMENT.getKey()
                : MovementReason.OUT_MOVEMENT.getKey();

        // Registrar el movimiento en stockdiary directamente (sin modificar
        // stockcurrent)
        // Ya actualizamos stockcurrent arriba, solo necesitamos registrar el movimiento
        PreparedSentence stockDiaryInsert = new PreparedSentence(dlSales.getSession(),
                "INSERT INTO stockdiary (ID, DATENEW, REASON, LOCATION, PRODUCT, ATTRIBUTESETINSTANCE_ID, UNITS, PRICE, AppUser, SUPPLIER, SUPPLIERDOC) "
                        +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                new SerializerWriteBasicExt(new Datas[] {
                        Datas.STRING, Datas.TIMESTAMP, Datas.INT, Datas.STRING, Datas.STRING,
                        Datas.STRING, Datas.DOUBLE, Datas.DOUBLE, Datas.STRING, Datas.STRING, Datas.STRING
                }, new int[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 }));

        stockDiaryInsert.exec(new Object[] {
                UUID.randomUUID().toString(),
                currentDate,
                reasonKey,
                locationId,
                productId,
                null, // ATTRIBUTESETINSTANCE_ID
                stockDifference, // UNITS (diferencia)
                0.0, // PRICE
                userName, // AppUser
                null, // SUPPLIER
                null // SUPPLIERDOC
        });
    }

    /**
     * Actualiza los valores de mínimo y máximo en stocklevel
     */
    private void updateStockLevelInDatabase(String productId, String locationName, Double minimum, Double maximum)
            throws BasicException {
        // Obtener el ID de la ubicación desde el nombre
        String locationId = null;
        try {
            List<LocationInfo> locations = dlSales.getLocationsList().list();
            for (LocationInfo loc : locations) {
                if (loc.getName().equals(locationName)) {
                    locationId = loc.getID();
                    break;
                }
            }

            // Si no se encuentra, usar la ubicación de inventario principal
            if (locationId == null) {
                locationId = appView.getInventoryLocation();
            }
        } catch (BasicException ex) {
            locationId = appView.getInventoryLocation();
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
            SentenceFind checkStmt = new PreparedSentence(dlSales.getSession(),
                    "SELECT ID FROM stocklevel WHERE LOCATION = ? AND PRODUCT = ?",
                    new SerializerWriteBasicExt(new Datas[] { Datas.STRING, Datas.STRING }, new int[] { 0, 1 }),
                    SerializerReadString.INSTANCE);

            String existingId = (String) checkStmt.find(new Object[] { locationId, productId });

            if (existingId != null) {
                // UPDATE
                PreparedSentence updateStmt = new PreparedSentence(dlSales.getSession(),
                        "UPDATE stocklevel SET STOCKSECURITY = ?, STOCKMAXIMUM = ? WHERE LOCATION = ? AND PRODUCT = ?",
                        new SerializerWriteBasicExt(
                                new Datas[] { Datas.DOUBLE, Datas.DOUBLE, Datas.STRING, Datas.STRING },
                                new int[] { 0, 1, 2, 3 }));
                updateStmt.exec(new Object[] { minimum, maximum, locationId, productId });
            } else {
                // INSERT
                PreparedSentence insertStmt = new PreparedSentence(dlSales.getSession(),
                        "INSERT INTO stocklevel (ID, LOCATION, PRODUCT, STOCKSECURITY, STOCKMAXIMUM) VALUES (?, ?, ?, ?, ?)",
                        new SerializerWriteBasicExt(
                                new Datas[] { Datas.STRING, Datas.STRING, Datas.STRING, Datas.DOUBLE, Datas.DOUBLE },
                                new int[] { 0, 1, 2, 3, 4 }));
                insertStmt.exec(new Object[] { UUID.randomUUID().toString(), locationId, productId, minimum, maximum });
            }
        } catch (BasicException e) {
            // Si falla el check, intentar insertar directamente
            try {
                PreparedSentence insertStmt = new PreparedSentence(dlSales.getSession(),
                        "INSERT INTO stocklevel (ID, LOCATION, PRODUCT, STOCKSECURITY, STOCKMAXIMUM) VALUES (?, ?, ?, ?, ?)",
                        new SerializerWriteBasicExt(
                                new Datas[] { Datas.STRING, Datas.STRING, Datas.STRING, Datas.DOUBLE, Datas.DOUBLE },
                                new int[] { 0, 1, 2, 3, 4 }));
                insertStmt.exec(new Object[] { UUID.randomUUID().toString(), locationId, productId, minimum, maximum });
            } catch (BasicException ex2) {
                // Si ya existe, actualizar
                PreparedSentence updateStmt = new PreparedSentence(dlSales.getSession(),
                        "UPDATE stocklevel SET STOCKSECURITY = ?, STOCKMAXIMUM = ? WHERE LOCATION = ? AND PRODUCT = ?",
                        new SerializerWriteBasicExt(
                                new Datas[] { Datas.DOUBLE, Datas.DOUBLE, Datas.STRING, Datas.STRING },
                                new int[] { 0, 1, 2, 3 }));
                updateStmt.exec(new Object[] { minimum, maximum, locationId, productId });
            }
        }
    }

    private void selectorBackgroundColor() {
        bckgColor = JColorChooser.showDialog(this, "", Color.WHITE);
    }

    private void setDisplay(int btn) {

        String htmlString = (m_jDisplay.getText());

        switch (btn) {
            case 1:
                m_jDisplay.insert("<br>", m_jDisplay.getCaretPosition());
                break;
            case 2:
                Color colo = JColorChooser.showDialog(this, "", Color.WHITE);
                String hexcolor = color2HexString(colo);
                m_jDisplay.insert("<font color=" + hexcolor + ">", m_jDisplay.getCaretPosition());
                break;
            case 3:
                m_jDisplay.insert("<font size=+2>",
                        m_jDisplay.getCaretPosition());
                break;
            case 4:
                m_jDisplay.insert("<font size=-2>",
                        m_jDisplay.getCaretPosition());
                break;
            case 5:
                m_jDisplay.insert("<b>",
                        m_jDisplay.getCaretPosition());
                break;
            case 6:
                m_jDisplay.insert("<i>",
                        m_jDisplay.getCaretPosition());
                break;
            case 7:
                // defaults to file:/ for local disk
                // http:// also usable for remote image
                JFileChooser fc = new JFileChooser();
                FileFilter imageFilter = new FileNameExtensionFilter(
                        "Image files", ImageIO.getReaderFileSuffixes());
                fc.setFileFilter(imageFilter);
                int returnValue = fc.showOpenDialog(null);
                File selectedFile = fc.getSelectedFile();
                if (selectedFile != null) {
                    m_jDisplay.insert("<img src=file:" + selectedFile.getAbsolutePath() + ">",
                            m_jDisplay.getCaretPosition());
                }
                break;

            case 8:
                htmlString = "<html>" + m_jName.getText();
                m_jDisplay.setText(htmlString);
                break;
            case 9:
                m_jDisplay.insert("<div style=background-color:black;color:white;padding:10px;>",
                        m_jDisplay.getCaretPosition());
                break;
            default:
                htmlString += "";
                m_jDisplay.setText(htmlString);
        }
    }

    private void setButtonHTML() {

        jButtonHTML.setText(m_jDisplay.getText());
        if (bckgColor != null) {
            jButtonHTML.setBackground(bckgColor);
        }
    }

    public String color2HexString(Color color) {
        return "#" + Integer.toHexString(color.getRGB() & 0x00ffffff);
    }

    // 3 feb 16 speed test
    private BufferedImage findImage(Object id) {
        try {
            return (BufferedImage) loadimage.find(id);
        } catch (BasicException e) {
            return null;
        }
    }
    // end of speed test

    private void calculateMargin() {

        if (!reportlock) {
            reportlock = true;

            Double dPriceBuy = readCurrency(m_jPriceBuy.getText());
            Double dPriceSell = pricesell;

            if (dPriceBuy == null || dPriceSell == null) {
                m_jmargin.setText(null);
            } else {
                m_jmargin.setText(Formats.PERCENT.formatValue(dPriceSell / dPriceBuy - 1.0));
            }
            reportlock = false;
        }
    }

    private void calculatePriceSellTax() {

        if (!reportlock) {
            reportlock = true;

            Double dPriceSell = pricesell;

            if (dPriceSell == null || taxeslogic == null) {
                m_jPriceSellTax.setText(null);
            } else {
                double dTaxRate = taxeslogic.getTaxRate((TaxCategoryInfo) taxcatmodel.getSelectedItem());
                m_jPriceSellTax.setText(Formats.CURRENCY.formatValue(dPriceSell * (1.0 + dTaxRate)));
            }
            reportlock = false;
        }
    }

    private void calculateGP() {

        if (!reportlock) {
            reportlock = true;

            Double dPriceBuy = readCurrency(m_jPriceBuy.getText());
            Double dPriceSell = readCurrency(m_jPriceSell.getText());

            if (dPriceBuy == null || dPriceSell == null || dPriceBuy <= 0.0 || dPriceSell <= 0.0) {
                m_jGrossProfit.setText(null);
            } else {
                // Calcular porcentaje de ganancia (margen sobre venta): ((venta - costo) /
                // venta) * 100
                // Ejemplo: costo=100, venta=150 -> ganancia = (150-100)/150 * 100 = 33.33%
                double ganancia = ((dPriceSell - dPriceBuy) / dPriceSell) * 100.0;
                m_jGrossProfit.setText(String.format("%.2f%%", ganancia));
            }
            reportlock = false;
        }
    }

    private void calculatePriceSellfromMargin() {

        if (!reportlock) {
            reportlock = true;

            Double dPriceBuy = readCurrency(m_jPriceBuy.getText());
            Double dMargin = readPercent(m_jmargin.getText());

            if (dMargin == null || dPriceBuy == null) {
                setPriceSell(null);
            } else {
                setPriceSell(dPriceBuy * (1.0 + dMargin));
            }

            reportlock = false;
        }

    }

    private void calculatePriceSellfromPST() {

        if (!reportlock) {
            reportlock = true;

            Double dPriceSellTax = readCurrency(m_jPriceSellTax.getText());

            if (dPriceSellTax == null) {
                setPriceSell(null);
            } else {
                double dTaxRate = taxeslogic.getTaxRate((TaxCategoryInfo) taxcatmodel.getSelectedItem());
                setPriceSell(dPriceSellTax / (1.0 + dTaxRate));
            }

            reportlock = false;
        }
    }

    private void setPriceSell(Double value) {

        if (!priceselllock) {
            priceselllock = true;
            pricesell = value;
            m_jPriceSell.setText(Formats.CURRENCY.formatValue(pricesell));
            priceselllock = false;
        }
    }

    private class PriceSellManager implements DocumentListener {

        @Override
        public void changedUpdate(DocumentEvent e) {
            if (!priceselllock) {
                priceselllock = true;
                pricesell = readCurrency(m_jPriceSell.getText());
                priceselllock = false;
            }
            calculateMargin();
            calculatePriceSellTax();
            calculateGP();
        }

        @Override
        public void insertUpdate(DocumentEvent e) {
            if (!priceselllock) {
                priceselllock = true;
                pricesell = readCurrency(m_jPriceSell.getText());
                priceselllock = false;
            }
            calculateMargin();
            calculatePriceSellTax();
            calculateGP();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            if (!priceselllock) {
                priceselllock = true;
                pricesell = readCurrency(m_jPriceSell.getText());
                priceselllock = false;
            }
            calculateMargin();
            calculatePriceSellTax();
            calculateGP();
        }
    }

    private class FieldsManager implements DocumentListener, ActionListener {

        @Override
        public void changedUpdate(DocumentEvent e) {
            calculateMargin();
            calculatePriceSellTax();
            calculateGP();

        }

        @Override
        public void insertUpdate(DocumentEvent e) {
            calculateMargin();
            calculatePriceSellTax();
            calculateGP();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            calculateMargin();
            calculatePriceSellTax();
            calculateGP();
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            calculateMargin();
            calculatePriceSellTax();
            calculateGP();
        }
    }

    private class PriceTaxManager implements DocumentListener {

        @Override
        public void changedUpdate(DocumentEvent e) {
            calculatePriceSellfromPST();
            calculateMargin();
            calculateGP();
        }

        @Override
        public void insertUpdate(DocumentEvent e) {
            calculatePriceSellfromPST();
            calculateMargin();
            calculateGP();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            calculatePriceSellfromPST();
            calculateMargin();
            calculateGP();
        }
    }

    private class MarginManager implements DocumentListener {

        @Override
        public void changedUpdate(DocumentEvent e) {
            calculatePriceSellfromMargin();
            calculatePriceSellTax();
            calculateGP();
        }

        @Override
        public void insertUpdate(DocumentEvent e) {
            calculatePriceSellfromMargin();
            calculatePriceSellTax();
            calculateGP();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            calculatePriceSellfromMargin();
            calculatePriceSellTax();
            calculateGP();
        }
    }

    private static Double readCurrency(String sValue) {
        if (sValue == null || sValue.trim().isEmpty()) {
            return null;
        }

        // Limpiar el valor: remover símbolos de moneda y espacios
        String cleanValue = sValue.trim()
                .replace("$", "")
                .replace("€", "")
                .replace("£", "")
                .replace("¥", "")
                .replace("MX", "")
                .replace("MXN", "")
                .replaceAll("\\s+", "");

        // Si el valor es un número simple (solo dígitos y un punto o coma), parsearlo
        // directamente
        // Esto evita problemas con formatos de moneda que usan punto como separador de
        // miles
        if (cleanValue.matches("^[0-9]+([.,][0-9]+)?$")) {
            try {
                // Reemplazar coma por punto para parseo estándar
                String normalizedValue = cleanValue.replace(",", ".");
                return Double.parseDouble(normalizedValue);
            } catch (NumberFormatException e) {
                // Si falla, continuar con el método normal
            }
        }

        try {
            // Intentar parsear con el formato de moneda
            Double result = Formats.CURRENCY.parseValue(sValue);
            // Verificar si el resultado parece incorrecto (muy grande)
            // Si el valor original era simple como "30.00" y el resultado es > 1000,
            // probablemente está mal
            if (cleanValue.matches("^[0-9]+([.,][0-9]+)?$") && result != null && result > 1000) {
                // Reintentar como número simple
                String normalizedValue = cleanValue.replace(",", ".");
                return Double.parseDouble(normalizedValue);
            }
            return result;
        } catch (BasicException e) {
            try {
                // Si falla, intentar parsear como número simple
                // Manejar tanto punto como coma como separador decimal
                String normalizedValue = cleanValue.replace(",", ".");
                // Remover separadores de miles (puntos que no sean el último punto decimal)
                if (normalizedValue.contains(".")) {
                    int lastDotIndex = normalizedValue.lastIndexOf(".");
                    String beforeDot = normalizedValue.substring(0, lastDotIndex).replace(".", "");
                    String afterDot = normalizedValue.substring(lastDotIndex + 1);
                    normalizedValue = beforeDot + "." + afterDot;
                }
                return Double.parseDouble(normalizedValue);
            } catch (NumberFormatException ex) {
                // Si todo falla, intentar con el formato de número
                try {
                    return Formats.DOUBLE.parseValue(cleanValue);
                } catch (BasicException ex2) {
                    return null;
                }
            }
        }
    }

    private static Double readPercent(String sValue) {
        try {
            return Formats.PERCENT.parseValue(sValue);
        } catch (BasicException e) {
            return null;
        }
    }

    /**
     * Guarda los valores de stock (cantidad actual y mínimo) en la base de datos
     */
    public void saveStockValues() throws BasicException {
        if (productId == null || appView == null) {
            return;
        }

        // Obtener valores de los campos
        Double currentStock = null;
        Double minimumStock = null;

        try {
            if (m_jStockCurrent != null && m_jStockCurrent.getText() != null
                    && !m_jStockCurrent.getText().trim().isEmpty()) {
                currentStock = Formats.DOUBLE.parseValue(m_jStockCurrent.getText());
            }
            if (m_jStockMinimum != null && m_jStockMinimum.getText() != null
                    && !m_jStockMinimum.getText().trim().isEmpty()) {
                minimumStock = Formats.DOUBLE.parseValue(m_jStockMinimum.getText());
            }
        } catch (BasicException e) {
            LOGGER.log(Level.WARNING, "Error al parsear valores de stock", e);
        }

        if (currentStock == null) {
            currentStock = 0.0;
        }
        if (minimumStock == null) {
            minimumStock = 0.0;
        }

        // Obtener la ubicación de inventario principal
        String locationId = appView.getInventoryLocation();
        if (locationId == null) {
            LOGGER.log(Level.WARNING, "No se pudo obtener la ubicación de inventario");
            return;
        }

        // Obtener el stock actual en la BD
        Double stockInDB = 0.0;
        try {
            stockInDB = dlSales.findProductStock(locationId, productId, null);
        } catch (BasicException e) {
            LOGGER.log(Level.WARNING, "Error al obtener stock actual de BD", e);
        }

        // Comparar con el valor inicial que se cargó cuando se abrió el editor
        // Solo actualizar si el usuario realmente cambió el valor manualmente
        // Si initialStockValue es null, significa que es la primera vez que se carga,
        // usar el valor de BD
        Double referenceStock = (initialStockValue != null) ? initialStockValue : stockInDB;

        // Calcular la diferencia entre el valor actual del campo y el valor de
        // referencia
        double quantityDifference = currentStock - referenceStock;

        // Calcular la diferencia real que se debe aplicar a la BD
        // Si el usuario cambió de 50 a 60, y la BD tiene 49 (por una venta),
        // debemos ajustar: 60 - 49 = 11, no 60 - 50 = 10
        double actualDifference = currentStock - stockInDB;

        LOGGER.log(Level.INFO,
                "═══════════════════════════════════════════════════════════");
        LOGGER.log(Level.INFO,
                "saveStockValues: INICIO - Product: " + productId);
        LOGGER.log(Level.INFO,
                "saveStockValues: currentStock (campo UI)=" + currentStock);
        LOGGER.log(Level.INFO,
                "saveStockValues: stockInDB (BD actual)=" + stockInDB);
        LOGGER.log(Level.INFO,
                "saveStockValues: initialStockValue (al abrir)=" + initialStockValue);
        LOGGER.log(Level.INFO,
                "saveStockValues: referenceStock=" + referenceStock);
        LOGGER.log(Level.INFO,
                "saveStockValues: quantityDifference=" + quantityDifference);
        LOGGER.log(Level.INFO,
                "saveStockValues: actualDifference=" + actualDifference);
        LOGGER.log(Level.INFO,
                "saveStockValues: Math.abs(quantityDifference) > 0.0001 = " + (Math.abs(quantityDifference) > 0.0001));

        // Actualizar o insertar en stockcurrent - CONSOLIDAR todos los registros
        // primero
        // para evitar problemas con múltiples registros (con y sin atributos)
        // IMPORTANTE: Siempre actualizar si el usuario especificó un valor diferente al
        // que está en BD
        // Esto asegura que el stock se actualice incluso si initialStockValue no se
        // estableció correctamente
        // o si el stock en BD cambió después de abrir el editor
        boolean shouldUpdate = Math.abs(actualDifference) > 0.0001;

        LOGGER.log(Level.INFO,
                "saveStockValues: shouldUpdate=" + shouldUpdate);
        LOGGER.log(Level.INFO,
                "saveStockValues: quantityDifference=" + quantityDifference + " (UI vs inicial)");
        LOGGER.log(Level.INFO,
                "saveStockValues: actualDifference=" + actualDifference + " (UI vs BD actual)");
        LOGGER.log(Level.INFO,
                "saveStockValues: Decisión: "
                        + (shouldUpdate ? "SÍ actualizar (hay diferencia con BD)" : "NO actualizar (sin diferencia)"));

        if (shouldUpdate) {
            try {
                LOGGER.log(Level.INFO, "saveStockValues: Consolidando stock antes de actualizar. Product: " + productId
                        + ", Location: " + locationId);
                LOGGER.log(Level.INFO, "saveStockValues: currentStock=" + currentStock + ", stockInDB=" + stockInDB
                        + ", actualDifference=" + actualDifference);

                // PASO 1: Obtener la suma de TODOS los registros de stock (con y sin atributos)
                Double totalStockAll = (Double) new PreparedSentence(dlSales.getSession(),
                        "SELECT SUM(UNITS) FROM stockcurrent WHERE LOCATION = ? AND PRODUCT = ?",
                        new SerializerWriteBasic(new Datas[] { Datas.STRING, Datas.STRING }),
                        SerializerReadDouble.INSTANCE)
                        .find(locationId, productId);

                if (totalStockAll == null) {
                    totalStockAll = 0.0;
                }

                LOGGER.log(Level.INFO,
                        "saveStockValues: Stock total encontrado (todos los registros): " + totalStockAll);

                // PASO 2: Eliminar TODOS los registros existentes (con y sin atributos)
                int deletedRows = new PreparedSentence(dlSales.getSession(),
                        "DELETE FROM stockcurrent WHERE LOCATION = ? AND PRODUCT = ?",
                        new SerializerWriteBasic(new Datas[] { Datas.STRING, Datas.STRING }))
                        .exec(new Object[] { locationId, productId });

                LOGGER.log(Level.INFO, "saveStockValues: Registros eliminados: " + deletedRows);

                // PASO 3: Insertar un ÚNICO registro consolidado con el valor que el usuario
                // especificó
                // El usuario quiere que el stock sea exactamente `currentStock`, no un ajuste
                PreparedSentence insertStmt = new PreparedSentence(dlSales.getSession(),
                        "INSERT INTO stockcurrent (LOCATION, PRODUCT, ATTRIBUTESETINSTANCE_ID, UNITS) VALUES (?, ?, NULL, ?)",
                        new SerializerWriteBasicExt(new Datas[] { Datas.STRING, Datas.STRING, Datas.DOUBLE },
                                new int[] { 0, 1, 2 }));
                insertStmt.exec(new Object[] { locationId, productId, currentStock });

                LOGGER.log(Level.INFO, "saveStockValues: ✓ Stock consolidado INSERTADO con valor: " + currentStock);

                // PASO 4: Verificar que se insertó correctamente
                Double verifyStock = dlSales.findProductStock(locationId, productId, null);
                LOGGER.log(Level.INFO, "saveStockValues: ✓ Verificación POST-INSERT: Stock en BD = " + verifyStock);

                if (verifyStock != null && Math.abs(verifyStock - currentStock) > 0.01) {
                    LOGGER.log(Level.SEVERE, "saveStockValues: ⚠️⚠️⚠️ PROBLEMA DETECTADO: Stock en BD (" + verifyStock
                            + ") no coincide con stock esperado (" + currentStock + ") ⚠️⚠️⚠️");
                } else {
                    LOGGER.log(Level.INFO, "saveStockValues: ✓✓✓ Stock guardado CORRECTAMENTE: " + verifyStock);
                }

            } catch (BasicException e) {
                LOGGER.log(Level.SEVERE, "saveStockValues: ✗ ERROR al consolidar y actualizar stock: " + e.getMessage(),
                        e);
                e.printStackTrace();
                throw e;
            }
        } else {
            LOGGER.log(Level.INFO, "saveStockValues: NO se actualiza stock (no hay diferencia significativa)");
        }

        LOGGER.log(Level.INFO,
                "═══════════════════════════════════════════════════════════");
        // Si no hay diferencia, no actualizar el stock (preserva el stock actual,
        // incluyendo descuentos de ventas)

        // Registrar el movimiento en stockdiary solo si hay diferencia
        if (Math.abs(actualDifference) > 0.0001) {
            // Obtener usuario y fecha
            String userName = appView.getAppUserView().getUser().getName();
            Date currentDate = DateUtils.getTodayMinutes();

            // Determinar el motivo del movimiento
            Object reasonKey = actualDifference > 0
                    ? MovementReason.IN_MOVEMENT.getKey()
                    : MovementReason.OUT_MOVEMENT.getKey();

            // Registrar el movimiento en stockdiary
            PreparedSentence stockDiaryInsert = new PreparedSentence(dlSales.getSession(),
                    "INSERT INTO stockdiary (ID, DATENEW, REASON, LOCATION, PRODUCT, ATTRIBUTESETINSTANCE_ID, UNITS, PRICE, AppUser, SUPPLIER, SUPPLIERDOC) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    new SerializerWriteBasicExt(new Datas[] { Datas.STRING, Datas.TIMESTAMP, Datas.INT, Datas.STRING,
                            Datas.STRING, Datas.STRING, Datas.DOUBLE, Datas.DOUBLE, Datas.STRING, Datas.STRING,
                            Datas.STRING }, new int[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 }));

            stockDiaryInsert.exec(new Object[] {
                    UUID.randomUUID().toString(),
                    currentDate,
                    reasonKey,
                    locationId,
                    productId,
                    null, // ATTRIBUTESETINSTANCE_ID
                    actualDifference, // UNITS (diferencia real)
                    0.0, // PRICE
                    userName, // AppUser
                    null, // SUPPLIER
                    null // SUPPLIERDOC
            });
        }

        // Actualizar o insertar en stocklevel (mínimo)
        try {
            SentenceFind checkStmt = new PreparedSentence(dlSales.getSession(),
                    "SELECT ID FROM stocklevel WHERE LOCATION = ? AND PRODUCT = ?",
                    new SerializerWriteBasicExt(new Datas[] { Datas.STRING, Datas.STRING }, new int[] { 0, 1 }),
                    SerializerReadString.INSTANCE);

            String existingId = (String) checkStmt.find(new Object[] { locationId, productId });

            if (existingId != null) {
                // UPDATE
                PreparedSentence updateStmt = new PreparedSentence(dlSales.getSession(),
                        "UPDATE stocklevel SET STOCKSECURITY = ? WHERE LOCATION = ? AND PRODUCT = ?",
                        new SerializerWriteBasicExt(new Datas[] { Datas.DOUBLE, Datas.STRING, Datas.STRING },
                                new int[] { 0, 1, 2 }));
                updateStmt.exec(new Object[] { minimumStock, locationId, productId });
            } else {
                // INSERT
                PreparedSentence insertStmt = new PreparedSentence(dlSales.getSession(),
                        "INSERT INTO stocklevel (ID, LOCATION, PRODUCT, STOCKSECURITY, STOCKMAXIMUM) VALUES (?, ?, ?, ?, ?)",
                        new SerializerWriteBasicExt(
                                new Datas[] { Datas.STRING, Datas.STRING, Datas.STRING, Datas.DOUBLE, Datas.DOUBLE },
                                new int[] { 0, 1, 2, 3, 4 }));
                insertStmt
                        .exec(new Object[] { UUID.randomUUID().toString(), locationId, productId, minimumStock, 0.0 });
            }
        } catch (BasicException e) {
            // Si falla, intentar insertar directamente
            try {
                PreparedSentence insertStmt = new PreparedSentence(dlSales.getSession(),
                        "INSERT INTO stocklevel (ID, LOCATION, PRODUCT, STOCKSECURITY, STOCKMAXIMUM) VALUES (?, ?, ?, ?, ?)",
                        new SerializerWriteBasicExt(
                                new Datas[] { Datas.STRING, Datas.STRING, Datas.STRING, Datas.DOUBLE, Datas.DOUBLE },
                                new int[] { 0, 1, 2, 3, 4 }));
                insertStmt
                        .exec(new Object[] { UUID.randomUUID().toString(), locationId, productId, minimumStock, 0.0 });
            } catch (BasicException ex2) {
                // Si ya existe, actualizar
                PreparedSentence updateStmt = new PreparedSentence(dlSales.getSession(),
                        "UPDATE stocklevel SET STOCKSECURITY = ? WHERE LOCATION = ? AND PRODUCT = ?",
                        new SerializerWriteBasicExt(new Datas[] { Datas.DOUBLE, Datas.STRING, Datas.STRING },
                                new int[] { 0, 1, 2 }));
                updateStmt.exec(new Object[] { minimumStock, locationId, productId });
            }
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated
    // Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        m_jRef = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        m_jCode = new javax.swing.JTextField();
        m_jCodetype = new javax.swing.JComboBox();
        jLabel2 = new javax.swing.JLabel();
        m_jName = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        m_jCategory = new javax.swing.JComboBox();
        jLabel13 = new javax.swing.JLabel();
        m_jAtt = new javax.swing.JComboBox();
        m_jVerpatrib = new javax.swing.JCheckBox();
        jLabel17 = new javax.swing.JLabel();
        m_jSupplier = new javax.swing.JComboBox();
        jBtnSupplier = new javax.swing.JButton();
        m_jTitle = new javax.swing.JLabel();
        pricePanel = new javax.swing.JPanel();
        m_jmargin = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        m_jPriceBuy = new javax.swing.JTextField();
        m_jGrossProfit = new javax.swing.JTextField();
        jLabel22 = new javax.swing.JLabel();
        jLabel26 = new javax.swing.JLabel();
        m_jUom = new javax.swing.JComboBox();
        jLabel7 = new javax.swing.JLabel();
        m_jTax = new javax.swing.JComboBox();
        jLabel16 = new javax.swing.JLabel();
        m_jPriceSellTax = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        m_jPriceSell = new javax.swing.JTextField();
        jLabel19 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jLabel9 = new javax.swing.JLabel();
        m_jstockcost = new javax.swing.JTextField();
        jLabel10 = new javax.swing.JLabel();
        m_jstockvolume = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        m_jInCatalog = new javax.swing.JCheckBox();
        jLabel18 = new javax.swing.JLabel();
        m_jCatalogOrder = new javax.swing.JTextField();
        jLabel15 = new javax.swing.JLabel();
        m_jService = new javax.swing.JCheckBox();
        jLabel11 = new javax.swing.JLabel();
        m_jComment = new javax.swing.JCheckBox();
        jLabel12 = new javax.swing.JLabel();
        m_jScale = new javax.swing.JCheckBox();
        m_jConstant = new javax.swing.JCheckBox();
        jLabel14 = new javax.swing.JLabel();
        jLabel20 = new javax.swing.JLabel();
        m_jVprice = new javax.swing.JCheckBox();
        jLabelAccumPoints = new javax.swing.JLabel();
        jLabel33 = new javax.swing.JLabel();
        m_jCheckWarrantyReceipt = new javax.swing.JCheckBox();
        jLabel23 = new javax.swing.JLabel();
        webLabel1 = new javax.swing.JLabel();
        m_jPrintTo = new javax.swing.JComboBox();
        jBtnShowTrans = new javax.swing.JButton();
        jLabelStockCurrent = new javax.swing.JLabel();
        m_jStockCurrent = new javax.swing.JTextField();
        jLabelStockMinimum = new javax.swing.JLabel();
        m_jStockMinimum = new javax.swing.JTextField();
        m_jPrintKB = new javax.swing.JCheckBox();
        m_jSendStatus = new javax.swing.JCheckBox();
        jLabelLote = new javax.swing.JLabel();
        m_jLote = new javax.swing.JTextField();
        jLabelModelo = new javax.swing.JLabel();
        m_jModelo = new javax.swing.JTextField();
        jLabelColor = new javax.swing.JLabel();
        m_jColor = new javax.swing.JTextField();
        jLabelVoltaje = new javax.swing.JLabel();
        m_jVoltaje = new javax.swing.JTextField();
        jLabelNoSerie = new javax.swing.JLabel();
        m_jNoSerie = new javax.swing.JTextField();
        m_jAccumulatesPoints = new javax.swing.JCheckBox();
        m_jStockUnits = new javax.swing.JTextField();
        jLblDate = new javax.swing.JLabel();
        m_jbtndate = new javax.swing.JButton();
        m_jdate = new javax.swing.JTextField();
        jLabelStockGeneral = new javax.swing.JLabel();
        m_jStockGeneral = new javax.swing.JTextField();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        txtAttributes = new javax.swing.JTextArea();
        jBtnXml = new javax.swing.JButton();
        jPanel6 = new javax.swing.JPanel();
        jLabel34 = new javax.swing.JLabel();
        m_jImage = new com.openbravo.data.gui.JImageEditor();
        jPanel4 = new javax.swing.JPanel();
        jLabel28 = new javax.swing.JLabel();
        jButtonHTML = new javax.swing.JButton();
        jLabel21 = new javax.swing.JLabel();
        m_jTextTip = new javax.swing.JTextField();
        jScrollPane3 = new javax.swing.JScrollPane();
        m_jDisplay = new javax.swing.JTextArea();
        jBtnBreak = new javax.swing.JButton();
        jBtnColour = new javax.swing.JButton();
        jBtnLarge = new javax.swing.JButton();
        jBtnSmall = new javax.swing.JButton();
        jBtnBold = new javax.swing.JButton();
        jBtnItalic = new javax.swing.JButton();
        jBtnImage = new javax.swing.JButton();
        jBtnReset = new javax.swing.JButton();
        jBtnStyle = new javax.swing.JButton();
        colourChooser = new javax.swing.JButton();

        setPreferredSize(new java.awt.Dimension(1000, 600));
        // Usar BorderLayout para que el panel combinado se muestre correctamente
        setLayout(new java.awt.BorderLayout());

        jTabbedPane1.setToolTipText("");
        jTabbedPane1.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jTabbedPane1.setPreferredSize(new java.awt.Dimension(1200, 700));

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("pos_messages"); // NOI18N
        jPanel1.setToolTipText(bundle.getString("tooltip.product.general.tab")); // NOI18N

        jLabel1.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel1.setText(AppLocal.getIntString("label.prodrefm")); // NOI18N
        jLabel1.setPreferredSize(new java.awt.Dimension(110, 30));
        jLabel1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel1MouseClicked(evt);
            }
        });

        m_jRef.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        m_jRef.setToolTipText("");
        m_jRef.setPreferredSize(new java.awt.Dimension(150, 30));
        m_jRef.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                m_jRefFocusLost(evt);
            }
        });

        jLabel6.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel6.setText(AppLocal.getIntString("label.prodbarcodem")); // NOI18N
        jLabel6.setPreferredSize(new java.awt.Dimension(100, 30));

        m_jCode.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        m_jCode.setPreferredSize(new java.awt.Dimension(125, 30));
        m_jCode.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                m_jCodeFocusLost(evt);
            }
        });

        m_jCodetype.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        m_jCodetype.setModel(
                new javax.swing.DefaultComboBoxModel(new String[] { "EAN-13", "EAN-8", "CODE128", "Upc-A", "Upc-E" }));
        m_jCodetype.setPreferredSize(new java.awt.Dimension(80, 30));

        jLabelStockGeneral.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabelStockGeneral.setText(AppLocal.getIntString("label.prodstockcurrent")); // NOI18N
        jLabelStockGeneral.setPreferredSize(new java.awt.Dimension(110, 30));

        m_jStockGeneral.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
        m_jStockGeneral.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        m_jStockGeneral.setEditable(false);
        m_jStockGeneral.setFocusable(false);
        m_jStockGeneral.setPreferredSize(new java.awt.Dimension(200, 30));

        jLabel2.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel2.setText(AppLocal.getIntString("label.prodnamem")); // NOI18N
        jLabel2.setPreferredSize(new java.awt.Dimension(110, 30));

        m_jName.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        m_jName.setPreferredSize(new java.awt.Dimension(200, 30));
        m_jName.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                m_jNameFocusLost(evt);
            }
        });

        jLabel5.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel5.setText(AppLocal.getIntString("label.prodcategorym")); // NOI18N
        jLabel5.setPreferredSize(new java.awt.Dimension(110, 30));

        m_jCategory.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        m_jCategory.setPreferredSize(new java.awt.Dimension(200, 30));

        jLabel13.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel13.setText(AppLocal.getIntString("label.attributes")); // NOI18N
        jLabel13.setPreferredSize(new java.awt.Dimension(110, 30));

        m_jAtt.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        m_jAtt.setPreferredSize(new java.awt.Dimension(200, 30));

        m_jVerpatrib.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        m_jVerpatrib.setText(bundle.getString("label.mandatory")); // NOI18N
        m_jVerpatrib.setHorizontalTextPosition(javax.swing.SwingConstants.LEADING);
        m_jVerpatrib.setPreferredSize(new java.awt.Dimension(49, 30));
        m_jVerpatrib.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                none(evt);
            }
        });

        jLabel17.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel17.setText(AppLocal.getIntString("label.prodsupplier")); // NOI18N
        jLabel17.setPreferredSize(new java.awt.Dimension(110, 30));

        m_jSupplier.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        m_jSupplier.setPreferredSize(new java.awt.Dimension(200, 30));

        jBtnSupplier.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        jBtnSupplier.setIcon(new ModernActionIcon(ModernActionIcon.Type.ADD, 18));
        jBtnSupplier.setText(bundle.getString("label.supplier")); // NOI18N
        jBtnSupplier.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnSupplierActionPerformed(evt);
            }
        });

        m_jTitle.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        m_jTitle.setForeground(new java.awt.Color(102, 102, 102));
        m_jTitle.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        m_jTitle.setText("...");
        m_jTitle.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);
        m_jTitle.setFocusable(false);
        m_jTitle.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        m_jTitle.setOpaque(true);
        m_jTitle.setPreferredSize(new java.awt.Dimension(260, 25));

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
                jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel1Layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(m_jTitle, javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addGroup(jPanel1Layout.createSequentialGroup()
                                                .addGroup(jPanel1Layout
                                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING,
                                                                false)
                                                        .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                150, Short.MAX_VALUE)
                                                        .addComponent(jLabel6, javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                150, Short.MAX_VALUE)
                                                        .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                150, Short.MAX_VALUE)
                                                        .addComponent(jLabel5, javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                150, Short.MAX_VALUE)
                                                        .addComponent(jLabel13, javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                150, Short.MAX_VALUE)
                                                        .addComponent(jLabel7, javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                150, Short.MAX_VALUE)
                                                        .addComponent(jLabel17, javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                150, Short.MAX_VALUE)
                                                        .addComponent(jLabelAccumPoints,
                                                                javax.swing.GroupLayout.DEFAULT_SIZE, 150,
                                                                Short.MAX_VALUE)

                                                        .addComponent(jLabelNoSerie,
                                                                javax.swing.GroupLayout.DEFAULT_SIZE, 150,
                                                                Short.MAX_VALUE))
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addGroup(jPanel1Layout
                                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addGroup(jPanel1Layout.createSequentialGroup()
                                                                .addComponent(m_jRef,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE, 200,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                .addGap(0, 0, Short.MAX_VALUE))
                                                        .addGroup(jPanel1Layout.createSequentialGroup()
                                                                .addComponent(m_jCode,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE, 200,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                .addPreferredGap(
                                                                        javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                                .addComponent(m_jCodetype,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE, 120,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                .addGap(0, 0, Short.MAX_VALUE))
                                                        .addGroup(jPanel1Layout.createSequentialGroup()
                                                                .addComponent(jLabelStockGeneral,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE, 150,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                .addGap(0, 0, Short.MAX_VALUE))
                                                        .addComponent(m_jName, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                200, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(m_jCategory,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE, 200,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addGroup(jPanel1Layout.createSequentialGroup()
                                                                .addComponent(m_jAtt,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE, 200,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                .addPreferredGap(
                                                                        javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                                .addComponent(m_jVerpatrib,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE, 120,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                .addGap(0, 0, Short.MAX_VALUE))
                                                        .addComponent(m_jTax, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                200, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addGroup(jPanel1Layout.createSequentialGroup()
                                                                .addComponent(m_jSupplier,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE, 200,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                .addPreferredGap(
                                                                        javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                                .addComponent(jBtnSupplier)
                                                                .addGap(0, 0, Short.MAX_VALUE))
                                                        .addComponent(m_jAccumulatesPoints,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addGroup(jPanel1Layout.createSequentialGroup()

                                                                .addPreferredGap(
                                                                        javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                .addComponent(m_jLote,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE, 120,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                .addGap(18, 18, 18)
                                                                .addComponent(jLabelModelo,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE, 100,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                .addPreferredGap(
                                                                        javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                .addComponent(m_jModelo,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE, 120,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE))
                                                        .addGroup(jPanel1Layout.createSequentialGroup()

                                                                .addPreferredGap(
                                                                        javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                .addComponent(m_jColor,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE, 120,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                .addGap(18, 18, 18)
                                                                .addComponent(jLabelVoltaje,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE, 100,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                .addPreferredGap(
                                                                        javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                .addComponent(m_jVoltaje,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE, 120,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE))
                                                        .addComponent(m_jNoSerie,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE, 200,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addGroup(jPanel1Layout.createSequentialGroup()
                                                                .addComponent(m_jStockGeneral,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE, 200,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                .addGap(0, 0, Short.MAX_VALUE)))))
                                .addContainerGap()));
        jPanel1Layout.setVerticalGroup(
                jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel1Layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(m_jRef, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(m_jCode, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(m_jCodetype, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(m_jName, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(m_jCategory, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel13, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(m_jAtt, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(m_jVerpatrib, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(m_jTax, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel17, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(m_jSupplier, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jBtnSupplier))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabelAccumPoints, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(m_jAccumulatesPoints, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabelLote, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(m_jLote, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jLabelModelo, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(m_jModelo, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabelColor, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(m_jColor, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jLabelVoltaje, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(m_jVoltaje, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabelNoSerie, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(m_jNoSerie, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabelStockGeneral, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(m_jStockGeneral, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(m_jTitle, javax.swing.GroupLayout.PREFERRED_SIZE, 37,
                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));

        // NO AGREGAR A PESTAÑAS - SE COMBINARÁ EN UN SOLO PANEL AL FINAL
        // jTabbedPane1.addTab(AppLocal.getIntString("label.prodgeneral"), jPanel1); //
        // NOI18N

        m_jmargin.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        m_jmargin.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        m_jmargin.setCursor(new java.awt.Cursor(java.awt.Cursor.TEXT_CURSOR));
        m_jmargin.setEnabled(false);
        m_jmargin.setPreferredSize(new java.awt.Dimension(110, 30));

        jLabel3.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel3.setText(AppLocal.getIntString("label.prodpricebuym")); // NOI18N
        jLabel3.setToolTipText(AppLocal.getIntString("label.prodpricebuym")); // NOI18N
        jLabel3.setPreferredSize(new java.awt.Dimension(110, 30));

        m_jPriceBuy.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        m_jPriceBuy.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        m_jPriceBuy.setText("0");
        m_jPriceBuy.setPreferredSize(new java.awt.Dimension(200, 30));

        m_jGrossProfit.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        m_jGrossProfit.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        m_jGrossProfit.setDisabledTextColor(new java.awt.Color(0, 0, 0));
        m_jGrossProfit.setEnabled(false);
        m_jGrossProfit.setPreferredSize(new java.awt.Dimension(110, 30));
        m_jGrossProfit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jGrossProfitActionPerformed(evt);
            }
        });

        jLabel22.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel22.setText(bundle.getString("label.grossprofit")); // NOI18N
        jLabel22.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jLabel22.setPreferredSize(new java.awt.Dimension(110, 30));

        jLabel26.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel26.setText(AppLocal.getIntString("label.UOM")); // NOI18N
        jLabel26.setPreferredSize(new java.awt.Dimension(70, 30));

        m_jUom.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        m_jUom.setPreferredSize(new java.awt.Dimension(150, 30));

        // Configuración del campo de impuesto (movido a pestaña General)
        jLabel7.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel7.setText(AppLocal.getIntString("label.taxcategorym")); // NOI18N
        jLabel7.setPreferredSize(new java.awt.Dimension(110, 30));

        m_jTax.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        m_jTax.setPreferredSize(new java.awt.Dimension(200, 30));

        jLabel16.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel16.setText(AppLocal.getIntString("label.prodpriceselltaxm")); // NOI18N
        jLabel16.setToolTipText(AppLocal.getIntString("label.prodpriceselltaxm")); // NOI18N
        jLabel16.setPreferredSize(new java.awt.Dimension(110, 30));

        m_jPriceSellTax.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        m_jPriceSellTax.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        m_jPriceSellTax.setPreferredSize(new java.awt.Dimension(200, 30));

        jLabel4.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel4.setText(AppLocal.getIntString("label.prodpricesell")); // NOI18N
        jLabel4.setToolTipText(AppLocal.getIntString("label.prodpricesell")); // NOI18N
        jLabel4.setPreferredSize(new java.awt.Dimension(110, 30));

        m_jPriceSell.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        m_jPriceSell.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        m_jPriceSell.setPreferredSize(new java.awt.Dimension(200, 30));

        jLabel19.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel19.setText(bundle.getString("label.margin")); // NOI18N
        jLabel19.setPreferredSize(new java.awt.Dimension(110, 30));

        javax.swing.GroupLayout pricePanelLayout = new javax.swing.GroupLayout(pricePanel);
        pricePanel.setLayout(pricePanelLayout);
        pricePanelLayout.setHorizontalGroup(
                pricePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(pricePanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(pricePanelLayout
                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(pricePanelLayout.createSequentialGroup()
                                                .addGroup(pricePanelLayout
                                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(jLabel16, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE))
                                                .addGap(16, 16, 16)
                                                .addGroup(pricePanelLayout
                                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING,
                                                                false)
                                                        .addComponent(m_jPriceSellTax,
                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                        .addComponent(m_jPriceSell,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE)))
                                        .addComponent(jLabel26, javax.swing.GroupLayout.PREFERRED_SIZE, 104,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGroup(pricePanelLayout.createSequentialGroup()
                                                .addGroup(pricePanelLayout
                                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(jLabel22, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                94, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(jLabel19, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                104, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                .addGap(16, 16, 16)
                                                .addGroup(pricePanelLayout
                                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING,
                                                                false)
                                                        .addComponent(m_jPriceBuy, javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                        .addComponent(m_jGrossProfit,
                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                        .addComponent(m_jmargin, javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                Short.MAX_VALUE))))
                                .addContainerGap(308, Short.MAX_VALUE)));
        pricePanelLayout.setVerticalGroup(
                pricePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(pricePanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(
                                        pricePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                .addComponent(jLabel26, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addComponent(m_jUom, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(
                                        pricePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                .addComponent(m_jPriceSellTax, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addComponent(jLabel16, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(
                                        pricePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addComponent(m_jPriceSell, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(pricePanelLayout
                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(pricePanelLayout.createSequentialGroup()
                                                .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addGroup(pricePanelLayout
                                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                        .addComponent(jLabel22, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(m_jGrossProfit,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE)))
                                        .addComponent(m_jPriceBuy, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(
                                        pricePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                .addComponent(jLabel19, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addComponent(m_jmargin, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addContainerGap(176, Short.MAX_VALUE)));

        // Eliminada la pestaña de Precio - los campos se movieron a Stock
        // jTabbedPane1.addTab(AppLocal.getIntString("label.price"), pricePanel); //
        // NOI18N

        jPanel2.setToolTipText(bundle.getString("tooltip.product.stock.tab")); // NOI18N
        jPanel2.setPreferredSize(new java.awt.Dimension(0, 0));

        jLabel9.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel9.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel9.setText(AppLocal.getIntString("label.prodstockcost")); // NOI18N
        jLabel9.setPreferredSize(new java.awt.Dimension(110, 30));

        m_jstockcost.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        m_jstockcost.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        m_jstockcost.setPreferredSize(new java.awt.Dimension(85, 30));

        jLabel10.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel10.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel10.setText(AppLocal.getIntString("label.prodstockvol")); // NOI18N
        jLabel10.setPreferredSize(new java.awt.Dimension(110, 30));

        m_jstockvolume.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        m_jstockvolume.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        m_jstockvolume.setPreferredSize(new java.awt.Dimension(85, 30));

        jLabel8.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel8.setText(AppLocal.getIntString("label.prodincatalog")); // NOI18N
        jLabel8.setPreferredSize(new java.awt.Dimension(130, 30));

        m_jInCatalog.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        m_jInCatalog.setSelected(true);
        m_jInCatalog.setPreferredSize(new java.awt.Dimension(30, 30));
        m_jInCatalog.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jInCatalogActionPerformed(evt);
            }
        });

        jLabel18.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel18.setText(AppLocal.getIntString("label.prodorder")); // NOI18N
        jLabel18.setPreferredSize(new java.awt.Dimension(130, 30));

        m_jCatalogOrder.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        m_jCatalogOrder.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        m_jCatalogOrder.setPreferredSize(new java.awt.Dimension(50, 30));

        jLabel15.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel15.setText(bundle.getString("label.service")); // NOI18N
        jLabel15.setPreferredSize(new java.awt.Dimension(130, 30));

        m_jService.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        m_jService.setToolTipText("A Service Item will not be deducted from the Inventory");
        m_jService.setPreferredSize(new java.awt.Dimension(30, 30));

        jLabel11.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel11.setText(AppLocal.getIntString("label.prodaux")); // NOI18N
        jLabel11.setPreferredSize(new java.awt.Dimension(130, 30));

        m_jComment.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        m_jComment.setPreferredSize(new java.awt.Dimension(30, 30));

        jLabel12.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel12.setText(AppLocal.getIntString("label.prodscale")); // NOI18N
        jLabel12.setPreferredSize(new java.awt.Dimension(130, 30));

        m_jScale.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        m_jScale.setPreferredSize(new java.awt.Dimension(30, 30));

        m_jConstant.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        m_jConstant.setPreferredSize(new java.awt.Dimension(30, 30));

        jLabel14.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel14.setText(bundle.getString("label.prodconstant")); // NOI18N
        jLabel14.setPreferredSize(new java.awt.Dimension(130, 30));

        jLabel20.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel20.setText(bundle.getString("label.variableprice")); // NOI18N
        jLabel20.setPreferredSize(new java.awt.Dimension(130, 30));
        jLabel20.setVisible(false); // Ocultar etiqueta de precio variable (mayoreo)

        m_jVprice.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        m_jVprice.setPreferredSize(new java.awt.Dimension(30, 30));
        m_jVprice.setVisible(false); // Ocultar checkbox de precio variable (mayoreo)

        jLabelAccumPoints.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabelAccumPoints.setText(bundle.getString("label.prodaccumulatespoints")); // NOI18N
        jLabelAccumPoints.setPreferredSize(new java.awt.Dimension(130, 30));

        m_jAccumulatesPoints.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        m_jAccumulatesPoints.setPreferredSize(new java.awt.Dimension(30, 30));

        jLabel33.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel33.setText(bundle.getString("label.warranty")); // NOI18N
        jLabel33.setToolTipText(bundle.getString("label.warranty")); // NOI18N
        jLabel33.setPreferredSize(new java.awt.Dimension(130, 30));

        m_jCheckWarrantyReceipt.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        m_jCheckWarrantyReceipt.setText(bundle.getString("label.productreceipt")); // NOI18N
        m_jCheckWarrantyReceipt.setPreferredSize(new java.awt.Dimension(30, 30));

        webLabel1.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        webLabel1.setText(bundle.getString("label.printto")); // NOI18N
        webLabel1.setToolTipText(bundle.getString("tooltip.printto")); // NOI18N
        webLabel1.setPreferredSize(new java.awt.Dimension(130, 30));

        m_jPrintTo.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        m_jPrintTo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "0", "1", "2", "3", "4", "5", "6" }));
        m_jPrintTo.setPreferredSize(new java.awt.Dimension(50, 30));

        jLabelStockCurrent.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabelStockCurrent.setText("Cantidad Actual:"); // NOI18N
        jLabelStockCurrent.setPreferredSize(new java.awt.Dimension(150, 30));

        m_jStockCurrent.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        m_jStockCurrent.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        m_jStockCurrent.setPreferredSize(new java.awt.Dimension(150, 30));
        m_jStockCurrent.getDocument().addDocumentListener(m_Dirty);

        jLabelStockMinimum.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabelStockMinimum.setText("Cantidad Mínima:"); // NOI18N
        jLabelStockMinimum.setPreferredSize(new java.awt.Dimension(150, 30));

        m_jStockMinimum.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        m_jStockMinimum.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        m_jStockMinimum.setPreferredSize(new java.awt.Dimension(150, 30));
        m_jStockMinimum.getDocument().addDocumentListener(m_Dirty);

        m_jPrintKB.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N

        m_jSendStatus.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N

        m_jStockUnits.setEditable(false);
        m_jStockUnits.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        m_jStockUnits.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        m_jStockUnits.setText("0");
        m_jStockUnits.setBorder(null);

        jLblDate.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLblDate.setText(bundle.getString("label.proddate")); // NOI18N
        jLblDate.setPreferredSize(new java.awt.Dimension(130, 30));

        m_jbtndate.setIcon(new ModernActionIcon(ModernActionIcon.Type.CALENDAR, 18));
        m_jbtndate.setToolTipText("Open Calendar");
        m_jbtndate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jbtndateActionPerformed(evt);
            }
        });

        m_jdate.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        m_jdate.setPreferredSize(new java.awt.Dimension(160, 30));

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
                jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel2Layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(jPanel2Layout.createSequentialGroup()
                                                .addComponent(jLabelStockCurrent,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE,
                                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addComponent(m_jStockCurrent, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addGap(30, 30, 30)
                                                .addComponent(jLabelStockMinimum,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE,
                                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addComponent(m_jStockMinimum, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addGap(0, 0, Short.MAX_VALUE))
                                        .addGroup(jPanel2Layout.createSequentialGroup()
                                                .addGroup(jPanel2Layout
                                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(jLabel16, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(jLabel22, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(jLabel19, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE))
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addGroup(jPanel2Layout
                                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING,
                                                                false)
                                                        .addComponent(m_jPriceBuy, javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                150, Short.MAX_VALUE)
                                                        .addComponent(m_jPriceSell,
                                                                javax.swing.GroupLayout.DEFAULT_SIZE, 150,
                                                                Short.MAX_VALUE)
                                                        .addComponent(m_jPriceSellTax,
                                                                javax.swing.GroupLayout.DEFAULT_SIZE, 150,
                                                                Short.MAX_VALUE)
                                                        .addComponent(m_jGrossProfit,
                                                                javax.swing.GroupLayout.DEFAULT_SIZE, 150,
                                                                Short.MAX_VALUE)
                                                        .addComponent(m_jmargin, javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                150, Short.MAX_VALUE))
                                                .addGap(30, 30, 30)
                                                .addGroup(jPanel2Layout
                                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addGroup(jPanel2Layout.createSequentialGroup()
                                                                .addGroup(jPanel2Layout.createParallelGroup(
                                                                        javax.swing.GroupLayout.Alignment.TRAILING)
                                                                        .addComponent(jLabel18,
                                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                        .addComponent(jLabel8,
                                                                                javax.swing.GroupLayout.Alignment.LEADING,
                                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                javax.swing.GroupLayout.PREFERRED_SIZE))
                                                                .addPreferredGap(
                                                                        javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                                .addGroup(jPanel2Layout.createParallelGroup(
                                                                        javax.swing.GroupLayout.Alignment.LEADING)
                                                                        .addComponent(m_jCatalogOrder,
                                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                        .addComponent(m_jInCatalog,
                                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                javax.swing.GroupLayout.PREFERRED_SIZE)))
                                                        .addGroup(jPanel2Layout.createSequentialGroup()
                                                                .addComponent(webLabel1,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                .addPreferredGap(
                                                                        javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                                .addComponent(m_jPrintTo,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE))
                                                        .addGroup(jPanel2Layout.createSequentialGroup()
                                                                .addComponent(jLabel14,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                .addPreferredGap(
                                                                        javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                .addComponent(m_jConstant,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                .addPreferredGap(
                                                                        javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                .addComponent(jLabel9,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                .addPreferredGap(
                                                                        javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                .addComponent(m_jstockcost,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE))
                                                        .addGroup(jPanel2Layout.createSequentialGroup()
                                                                .addComponent(jLabel10,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                .addPreferredGap(
                                                                        javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                .addComponent(m_jstockvolume,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE)))
                                                .addGap(30, 30, 30)
                                                .addGroup(jPanel2Layout
                                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addGroup(jPanel2Layout.createSequentialGroup()
                                                                .addComponent(jLabel15,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                .addPreferredGap(
                                                                        javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                                .addComponent(m_jService,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE))
                                                        .addGroup(jPanel2Layout.createSequentialGroup()
                                                                .addComponent(jLabel33,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                .addPreferredGap(
                                                                        javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                                .addComponent(m_jCheckWarrantyReceipt,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE))
                                                        .addGroup(jPanel2Layout.createSequentialGroup()
                                                                .addComponent(jLabel11,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                .addPreferredGap(
                                                                        javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                                .addComponent(m_jComment,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE))
                                                        .addGroup(jPanel2Layout.createSequentialGroup()
                                                                .addComponent(jLabel12,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                .addPreferredGap(
                                                                        javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                                .addComponent(m_jScale,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE))
                                                        .addGroup(jPanel2Layout.createSequentialGroup()
                                                                .addComponent(jLabel20,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                .addPreferredGap(
                                                                        javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                                .addComponent(m_jVprice,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE))
                                                        .addGroup(jPanel2Layout.createSequentialGroup()
                                                                .addComponent(jLabelLote,
                                                                        javax.swing.GroupLayout.DEFAULT_SIZE, 150,
                                                                        Short.MAX_VALUE)
                                                                .addComponent(jLabelColor,
                                                                        javax.swing.GroupLayout.DEFAULT_SIZE, 150,
                                                                        Short.MAX_VALUE)
                                                                .addComponent(jLabelAccumPoints,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                .addPreferredGap(
                                                                        javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                                .addComponent(m_jAccumulatesPoints,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE)))
                                                .addGap(0, 0, Short.MAX_VALUE))
                                        .addGroup(jPanel2Layout.createSequentialGroup()
                                                .addComponent(jLblDate, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addComponent(m_jbtndate, javax.swing.GroupLayout.PREFERRED_SIZE, 40,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addGap(0, 0, 0)
                                                .addComponent(m_jdate, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addGap(0, 0, Short.MAX_VALUE)))
                                .addContainerGap()));
        jPanel2Layout.setVerticalGroup(
                jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel2Layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(jPanel2Layout.createSequentialGroup()
                                                .addGroup(jPanel2Layout
                                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                        .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(m_jPriceBuy,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE))
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addGroup(jPanel2Layout
                                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                        .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(m_jPriceSell,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE))
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addGroup(jPanel2Layout
                                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                        .addComponent(jLabel16, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(m_jPriceSellTax,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE))
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addGroup(jPanel2Layout
                                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                        .addComponent(jLabel22, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(m_jGrossProfit,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE))
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addGroup(jPanel2Layout
                                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                        .addComponent(jLabel19, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(m_jmargin, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE)))
                                        .addGroup(jPanel2Layout.createSequentialGroup()
                                                .addGroup(jPanel2Layout
                                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                        .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(m_jInCatalog,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE))
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addGroup(jPanel2Layout
                                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                        .addComponent(m_jCatalogOrder,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(jLabel18, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE))
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addGroup(jPanel2Layout
                                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                        .addComponent(webLabel1, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(m_jPrintTo,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE))
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addGroup(jPanel2Layout
                                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                        .addComponent(jLabel14, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(m_jConstant,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(m_jstockcost,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE))
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addGroup(jPanel2Layout
                                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                        .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(m_jstockvolume,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE)))
                                        .addGroup(jPanel2Layout.createSequentialGroup()
                                                .addGroup(jPanel2Layout
                                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                        .addComponent(jLabel15, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(m_jService,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE))
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addGroup(jPanel2Layout
                                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                        .addComponent(jLabel33, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(m_jCheckWarrantyReceipt,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE))
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addGroup(jPanel2Layout
                                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                        .addComponent(jLabel11, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(m_jComment,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE))
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addGroup(jPanel2Layout
                                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                        .addComponent(jLabel12, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(m_jScale, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE))
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addGroup(jPanel2Layout
                                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                        .addComponent(jLabel20, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(m_jVprice, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE))
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addGroup(jPanel2Layout
                                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                        .addComponent(jLabelAccumPoints,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(m_jAccumulatesPoints,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE))))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLblDate, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(m_jbtndate, javax.swing.GroupLayout.PREFERRED_SIZE, 30,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(m_jdate, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabelStockCurrent, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(m_jStockCurrent, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jLabelStockMinimum, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(m_jStockMinimum, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addContainerGap(500, Short.MAX_VALUE)));

        m_jService.getAccessibleContext().setAccessibleDescription("null");

        // NO AGREGAR A PESTAÑAS - SE COMBINARÁ EN UN SOLO PANEL AL FINAL
        // jTabbedPane1.addTab(AppLocal.getIntString("label.prodstock"), jPanel2); //
        // NOI18N

        jPanel3.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        jPanel3.setToolTipText(bundle.getString("tooltip.product.properties.tab")); // NOI18N
        jPanel3.setPreferredSize(new java.awt.Dimension(0, 0));
        jPanel3.setLayout(new java.awt.BorderLayout());

        jScrollPane1.setPreferredSize(new java.awt.Dimension(680, 400));

        txtAttributes.setFont(new java.awt.Font("Monospaced", 0, 14)); // NOI18N
        txtAttributes.setLineWrap(true);
        txtAttributes.setWrapStyleWord(true);
        txtAttributes.setPreferredSize(new java.awt.Dimension(580, 300));
        jScrollPane1.setViewportView(txtAttributes);

        jPanel3.add(jScrollPane1, java.awt.BorderLayout.CENTER);

        jBtnXml.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jBtnXml.setText(bundle.getString("button.injectxml")); // NOI18N
        jBtnXml.setToolTipText(bundle.getString("tooltip.xmlheader")); // NOI18N
        jBtnXml.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnXmlActionPerformed(evt);
            }
        });
        jPanel3.add(jBtnXml, java.awt.BorderLayout.PAGE_START);

        jPanel6.setToolTipText(bundle.getString("tooltip.product.image.tab")); // NOI18N
        jPanel6.setPreferredSize(new java.awt.Dimension(0, 0));

        jLabel34.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel34.setText(bundle.getString("label.imagesize")); // NOI18N
        jLabel34.setPreferredSize(new java.awt.Dimension(500, 30));

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
                jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel6Layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(jPanel6Layout.createSequentialGroup()
                                                .addComponent(jLabel34, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addContainerGap(134, Short.MAX_VALUE))
                                        .addGroup(jPanel6Layout.createSequentialGroup()
                                                .addComponent(m_jImage, javax.swing.GroupLayout.DEFAULT_SIZE,
                                                        javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addGap(243, 243, 243)))));
        jPanel6Layout.setVerticalGroup(
                jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel6Layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(m_jImage, javax.swing.GroupLayout.PREFERRED_SIZE,
                                        javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel34, javax.swing.GroupLayout.PREFERRED_SIZE,
                                        javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap()));

        jTabbedPane1.addTab(bundle.getString("label.image"), jPanel6); // NOI18N

        jPanel4.setToolTipText(bundle.getString("tooltip.product.button.tab")); // NOI18N
        jPanel4.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        jPanel4.setPreferredSize(new java.awt.Dimension(0, 0));

        jLabel28.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel28.setText(bundle.getString("label.prodbuttonhtml")); // NOI18N
        jLabel28.setPreferredSize(new java.awt.Dimension(250, 30));

        jButtonHTML.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        jButtonHTML.setText(bundle.getString("button.htmltest")); // NOI18N
        jButtonHTML.setMargin(new java.awt.Insets(1, 1, 1, 1));
        jButtonHTML.setMaximumSize(new java.awt.Dimension(96, 72));
        jButtonHTML.setMinimumSize(new java.awt.Dimension(96, 72));
        jButtonHTML.setPreferredSize(new java.awt.Dimension(96, 72));
        jButtonHTML.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonHTMLActionPerformed(evt);
            }
        });

        jLabel21.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel21.setText(bundle.getString("label.texttip")); // NOI18N
        jLabel21.setPreferredSize(new java.awt.Dimension(110, 30));

        m_jTextTip.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        m_jTextTip.setPreferredSize(new java.awt.Dimension(400, 30));

        m_jDisplay.setColumns(20);
        m_jDisplay.setLineWrap(true);
        m_jDisplay.setRows(4);
        m_jDisplay.setWrapStyleWord(true);
        m_jDisplay.setPreferredSize(new java.awt.Dimension(160, 100));
        jScrollPane3.setViewportView(m_jDisplay);

        jBtnBreak.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        jBtnBreak.setText(bundle.getString("button.prodhtmldisplayBreak")); // NOI18N
        jBtnBreak.setToolTipText("<html><center><h4>Inserts a Line Break<br> (a new line) for the button text");
        jBtnBreak.setPreferredSize(new java.awt.Dimension(70, 35));
        jBtnBreak.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnBreakActionPerformed(evt);
            }
        });

        jBtnColour.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        jBtnColour.setForeground(new java.awt.Color(0, 204, 255));
        jBtnColour.setText(bundle.getString("button.prodhtmldisplayColour")); // NOI18N
        jBtnColour.setToolTipText("<html><center><h4>Set the colour <br>for the button text");
        jBtnColour.setPreferredSize(new java.awt.Dimension(70, 35));
        jBtnColour.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnColourActionPerformed(evt);
            }
        });

        jBtnLarge.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jBtnLarge.setText(bundle.getString("button.prodhtmldisplayLarge")); // NOI18N
        jBtnLarge.setToolTipText("<html><center><h4>Set the button<br> text to Large");
        jBtnLarge.setPreferredSize(new java.awt.Dimension(70, 35));
        jBtnLarge.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnLargeActionPerformed(evt);
            }
        });

        jBtnSmall.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        jBtnSmall.setText(bundle.getString("button.prodhtmldisplaySmall")); // NOI18N
        jBtnSmall.setToolTipText("<html><center><h4>Set the button<br>text to Small");
        jBtnSmall.setPreferredSize(new java.awt.Dimension(70, 35));
        jBtnSmall.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnSmallActionPerformed(evt);
            }
        });

        jBtnBold.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        jBtnBold.setText(bundle.getString("button.prodhtmldisplayBold")); // NOI18N
        jBtnBold.setToolTipText("<html><center><h4>Set the button<br> text to Italic");
        jBtnBold.setPreferredSize(new java.awt.Dimension(70, 35));
        jBtnBold.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnBoldActionPerformed(evt);
            }
        });

        jBtnItalic.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        jBtnItalic.setText(bundle.getString("button.prodhtmldisplayItalic")); // NOI18N
        jBtnItalic.setToolTipText("<html><center><h4>Set the button<br> text to Italic");
        jBtnItalic.setPreferredSize(new java.awt.Dimension(70, 35));
        jBtnItalic.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnItalicActionPerformed(evt);
            }
        });

        jBtnImage.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        jBtnImage.setText(bundle.getString("button.prodhtmldisplayImage")); // NOI18N
        jBtnImage.setToolTipText("<html><center><h4>Insert image from<br>local disk or internet URL");
        jBtnImage.setPreferredSize(new java.awt.Dimension(70, 35));
        jBtnImage.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnImageActionPerformed(evt);
            }
        });

        jBtnReset.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        jBtnReset.setIcon(new ModernActionIcon(ModernActionIcon.Type.REFRESH, 18));
        jBtnReset.setText(bundle.getString("button.prodhtmldisplayReset")); // NOI18N
        jBtnReset.setPreferredSize(new java.awt.Dimension(100, 35));
        jBtnReset.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnResetActionPerformed(evt);
            }
        });

        jBtnStyle.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        jBtnStyle.setText(bundle.getString("button.prodhtmldisplayStyle")); // NOI18N
        jBtnStyle.setToolTipText("<html><center><h4>Insert <style> tag to change<br>button background colour");
        jBtnStyle.setPreferredSize(new java.awt.Dimension(70, 35));
        jBtnStyle.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnStyleActionPerformed(evt);
            }
        });

        colourChooser.setText("Bck");
        colourChooser.setToolTipText(bundle.getString("tooltip.prodhtmldisplayColourChooser")); // NOI18N
        colourChooser.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                colourChooserActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
                jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel4Layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addGroup(jPanel4Layout.createSequentialGroup()
                                                .addComponent(jLabel28, javax.swing.GroupLayout.PREFERRED_SIZE, 230,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addGap(272, 272, 272)
                                                .addComponent(jBtnReset, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGroup(jPanel4Layout
                                                .createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                                .addComponent(jScrollPane3, javax.swing.GroupLayout.Alignment.LEADING)
                                                .addGroup(jPanel4Layout
                                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addGroup(jPanel4Layout.createSequentialGroup()
                                                                .addComponent(jBtnStyle,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                .addPreferredGap(
                                                                        javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                .addComponent(jBtnBreak,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                .addPreferredGap(
                                                                        javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                .addGroup(jPanel4Layout.createParallelGroup(
                                                                        javax.swing.GroupLayout.Alignment.LEADING,
                                                                        false)
                                                                        .addComponent(jBtnColour,
                                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                Short.MAX_VALUE)
                                                                        .addComponent(colourChooser,
                                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                Short.MAX_VALUE))
                                                                .addPreferredGap(
                                                                        javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                .addComponent(jBtnLarge,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                .addPreferredGap(
                                                                        javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                .addComponent(jBtnSmall,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                .addPreferredGap(
                                                                        javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                .addComponent(jBtnBold,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                .addPreferredGap(
                                                                        javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                .addComponent(jBtnItalic,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                .addPreferredGap(
                                                                        javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                .addComponent(jBtnImage,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE))
                                                        .addGroup(jPanel4Layout.createSequentialGroup()
                                                                .addComponent(jLabel21,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                .addGap(18, 18, 18)
                                                                .addGroup(jPanel4Layout.createParallelGroup(
                                                                        javax.swing.GroupLayout.Alignment.LEADING)
                                                                        .addComponent(jButtonHTML,
                                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                        .addComponent(m_jTextTip,
                                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                javax.swing.GroupLayout.PREFERRED_SIZE))))))
                                .addContainerGap()));
        jPanel4Layout.setVerticalGroup(
                jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel4Layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel28, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jBtnReset, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 100,
                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(jBtnSmall, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGroup(jPanel4Layout
                                                .createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                .addComponent(jBtnColour, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addComponent(jBtnLarge, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addComponent(jBtnBold, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addComponent(jBtnItalic, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addComponent(jBtnImage, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addComponent(jBtnStyle, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addComponent(jBtnBreak, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(18, 18, 18)
                                .addComponent(colourChooser, javax.swing.GroupLayout.PREFERRED_SIZE, 32,
                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(jLabel21, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(m_jTextTip, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(18, 18, 18)
                                .addComponent(jButtonHTML, javax.swing.GroupLayout.PREFERRED_SIZE,
                                        javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap(94, Short.MAX_VALUE)));

        jTabbedPane1.addTab(AppLocal.getIntString("label.button"), jPanel4); // NOI18N

        // === CREAR PANEL COMBINADO GENERAL + STOCK (SIN PESTAÑAS) ===
        // Remover cualquier componente existente ANTES de crear el nuevo panel
        removeAll();
        // Crear panel principal con scroll
        mainCombinedPanel = new javax.swing.JPanel();
        mainCombinedPanel.setLayout(new java.awt.BorderLayout());
        mainCombinedPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 15, 15, 15));
        mainCombinedPanel.setBackground(new java.awt.Color(245, 247, 250));

        // Panel de contenido con el diseño combinado que implementa Scrollable
        javax.swing.JPanel contentPanel = new ScrollablePanel(new java.awt.BorderLayout());
        contentPanel.setBackground(new java.awt.Color(245, 247, 250));

        // Título "NUEVO PRODUCTO" o "EDITAR PRODUCTO" en naranja
        jLabelProductTitle = new javax.swing.JLabel("NUEVO PRODUCTO");
        jLabelProductTitle.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 24));
        jLabelProductTitle.setForeground(new java.awt.Color(249, 115, 22));
        jLabelProductTitle.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 4, 4, 0));
        jLabelProductTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        jLabelProductTitle.setVisible(false);

        // Panel principal con BorderLayout para contener la foto a la izquierda y los campos a la derecha
        mainFieldsPanel = new javax.swing.JPanel();
        mainFieldsPanel.setLayout(new java.awt.BorderLayout(20, 0));
        mainFieldsPanel.setBackground(java.awt.Color.WHITE);
        mainFieldsPanel.setBorder(createProductSectionBorder());

        // 1. Panel de foto del producto (Izquierda)
        javax.swing.JPanel photoPanel = new javax.swing.JPanel(new java.awt.BorderLayout(0, 12));
        photoPanel.setBackground(java.awt.Color.WHITE);
        photoPanel.setBorder(createProductSectionBorder());
        photoPanel.add(
                createProductSectionHeader("Foto del producto",
                        "Carga o cambia la imagen principal desde aqui arriba."),
                java.awt.BorderLayout.NORTH);
        if (m_jImage.getParent() != null) {
            m_jImage.getParent().remove(m_jImage);
        }
        m_jImage.setPreferredSize(new java.awt.Dimension(300, 245));
        m_jImage.setMinimumSize(new java.awt.Dimension(300, 245));
        m_jImage.setMaximumSize(new java.awt.Dimension(320, 260));
        m_jImage.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(226, 232, 240), 1));
        photoPanel.add(m_jImage, java.awt.BorderLayout.CENTER);

        mainFieldsPanel.add(photoPanel, java.awt.BorderLayout.WEST);

        // 2. Paneles de campos (Centro y Derecha del formulario)
        javax.swing.JPanel leftFieldsPanel = new javax.swing.JPanel(new java.awt.GridBagLayout());
        leftFieldsPanel.setOpaque(false);
        java.awt.GridBagConstraints gbcLeft = new java.awt.GridBagConstraints();
        gbcLeft.anchor = java.awt.GridBagConstraints.WEST;
        gbcLeft.insets = new java.awt.Insets(6, 0, 6, 12);
        gbcLeft.fill = java.awt.GridBagConstraints.NONE;

        int rowLeft = 0;

        // Producto (m_jRef)
        gbcLeft.gridx = 0;
        gbcLeft.gridy = rowLeft;
        javax.swing.JLabel lblProducto = createProductFormLabel("Producto");
        leftFieldsPanel.add(lblProducto, gbcLeft);
        gbcLeft.gridx = 1;
        gbcLeft.weightx = 1.0;
        gbcLeft.fill = java.awt.GridBagConstraints.HORIZONTAL;
        styleProductTextField(m_jRef, 240);
        if (m_jRef.getParent() != null)
            m_jRef.getParent().remove(m_jRef);
        leftFieldsPanel.add(m_jRef, gbcLeft);
        gbcLeft.weightx = 0.0;
        gbcLeft.fill = java.awt.GridBagConstraints.NONE;
        rowLeft++;

        // Código de Barras
        gbcLeft.gridx = 0;
        gbcLeft.gridy = rowLeft;
        javax.swing.JLabel lblCodigo = createProductFormLabel("Codigo de barras");
        leftFieldsPanel.add(lblCodigo, gbcLeft);
        gbcLeft.gridx = 1;
        gbcLeft.weightx = 1.0;
        gbcLeft.fill = java.awt.GridBagConstraints.HORIZONTAL;
        styleProductTextField(m_jCode, 240);
        if (m_jCode.getParent() != null)
            m_jCode.getParent().remove(m_jCode);
        leftFieldsPanel.add(m_jCode, gbcLeft);
        gbcLeft.weightx = 0.0;
        gbcLeft.fill = java.awt.GridBagConstraints.NONE;
        rowLeft++;

        // Descripción (usar m_jName)
        gbcLeft.gridx = 0;
        gbcLeft.gridy = rowLeft;
        javax.swing.JLabel lblDescripcion = createProductFormLabel("Descripcion");
        leftFieldsPanel.add(lblDescripcion, gbcLeft);
        gbcLeft.gridx = 1;
        gbcLeft.weightx = 1.0;
        gbcLeft.fill = java.awt.GridBagConstraints.HORIZONTAL;
        styleProductTextField(m_jName, 240);
        if (m_jName.getParent() != null)
            m_jName.getParent().remove(m_jName);
        leftFieldsPanel.add(m_jName, gbcLeft);
        gbcLeft.weightx = 0.0;
        gbcLeft.fill = java.awt.GridBagConstraints.NONE;
        rowLeft++;

        // Radio buttons "Tipo de Producto"
        gbcLeft.gridx = 0;
        gbcLeft.gridy = rowLeft;
        javax.swing.JLabel lblSeVende = createProductFormLabel("Tipo");
        leftFieldsPanel.add(lblSeVende, gbcLeft);
        gbcLeft.gridx = 1;
        javax.swing.ButtonGroup sellTypeGroup = new javax.swing.ButtonGroup();
        rbSellVehicle = new javax.swing.JRadioButton("Vehículo", true);
        rbSellPieza = new javax.swing.JRadioButton("Pieza", false);
        rbSellPackage = new javax.swing.JRadioButton("Kit", false);
        rbSellPackage.setVisible(false);

        styleProductToggle(rbSellVehicle);
        styleProductToggle(rbSellPieza);

        sellTypeGroup.add(rbSellVehicle);
        sellTypeGroup.add(rbSellPieza);
        sellTypeGroup.add(rbSellPackage);

        javax.swing.JPanel radioPanel = new javax.swing.JPanel(
                new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 10, 0));
        radioPanel.setOpaque(false);
        radioPanel.add(rbSellVehicle);
        radioPanel.add(rbSellPieza);

        rbSellVehicle.addActionListener(e -> {
            m_jScale.setSelected(false);
            updateTypeVisibility();
        });
        rbSellPieza.addActionListener(e -> {
            m_jScale.setSelected(true);
            updateTypeVisibility();
        });

        gbcLeft.weightx = 1.0;
        gbcLeft.fill = java.awt.GridBagConstraints.HORIZONTAL;
        leftFieldsPanel.add(radioPanel, gbcLeft);
        gbcLeft.weightx = 0.0;
        gbcLeft.fill = java.awt.GridBagConstraints.NONE;
        rowLeft++;

        // Precio Costo
        gbcLeft.gridx = 0;
        gbcLeft.gridy = rowLeft;
        jLabel3.setText("Precio Costo:");
        jLabel3.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 13));
        jLabel3.setForeground(new java.awt.Color(55, 65, 81));
        jLabel3.setPreferredSize(new java.awt.Dimension(150, 24));
        if (jLabel3.getParent() != null)
            jLabel3.getParent().remove(jLabel3);
        leftFieldsPanel.add(jLabel3, gbcLeft);
        gbcLeft.gridx = 1;
        gbcLeft.weightx = 1.0;
        gbcLeft.fill = java.awt.GridBagConstraints.HORIZONTAL;
        styleProductTextField(m_jPriceBuy, 240);
        m_jPriceBuy.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        if (m_jPriceBuy.getParent() != null)
            m_jPriceBuy.getParent().remove(m_jPriceBuy);
        leftFieldsPanel.add(m_jPriceBuy, gbcLeft);
        gbcLeft.weightx = 0.0;
        gbcLeft.fill = java.awt.GridBagConstraints.NONE;
        rowLeft++;

        // Precio Venta
        gbcLeft.gridx = 0;
        gbcLeft.gridy = rowLeft;
        jLabel4.setText("Precio Venta:");
        jLabel4.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 13));
        jLabel4.setForeground(new java.awt.Color(55, 65, 81));
        jLabel4.setPreferredSize(new java.awt.Dimension(150, 24));
        if (jLabel4.getParent() != null)
            jLabel4.getParent().remove(jLabel4);
        leftFieldsPanel.add(jLabel4, gbcLeft);
        gbcLeft.gridx = 1;
        gbcLeft.weightx = 1.0;
        gbcLeft.fill = java.awt.GridBagConstraints.HORIZONTAL;
        styleProductTextField(m_jPriceSell, 240);
        m_jPriceSell.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        if (m_jPriceSell.getParent() != null)
            m_jPriceSell.getParent().remove(m_jPriceSell);
        leftFieldsPanel.add(m_jPriceSell, gbcLeft);
        gbcLeft.weightx = 0.0;
        gbcLeft.fill = java.awt.GridBagConstraints.NONE;
        rowLeft++;

        // Ganancia (calculada automáticamente)
        gbcLeft.gridx = 0;
        gbcLeft.gridy = rowLeft;
        javax.swing.JLabel lblGanancia = createProductFormLabel("Ganancia");
        leftFieldsPanel.add(lblGanancia, gbcLeft);
        gbcLeft.gridx = 1;
        gbcLeft.weightx = 1.0;
        gbcLeft.fill = java.awt.GridBagConstraints.HORIZONTAL;
        if (m_jGrossProfit.getParent() != null)
            m_jGrossProfit.getParent().remove(m_jGrossProfit);
        styleProductTextField(m_jGrossProfit, 240);
        m_jGrossProfit.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        m_jGrossProfit.setEnabled(false);
        m_jGrossProfit.setDisabledTextColor(new java.awt.Color(15, 23, 42));
        m_jGrossProfit.setBackground(new java.awt.Color(248, 250, 252));
        leftFieldsPanel.add(m_jGrossProfit, gbcLeft);
        gbcLeft.weightx = 0.0;
        gbcLeft.fill = java.awt.GridBagConstraints.NONE;
        rowLeft++;

        // Checkbox "Acumula Puntos"
        gbcLeft.gridx = 0;
        gbcLeft.gridy = rowLeft;
        gbcLeft.gridwidth = 2;
        gbcLeft.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gbcLeft.insets = new java.awt.Insets(6, 0, 6, 0);
        if (m_jAccumulatesPoints.getParent() != null)
            m_jAccumulatesPoints.getParent().remove(m_jAccumulatesPoints);
        m_jAccumulatesPoints.setText(AppLocal.getIntString("label.prodaccumulatespoints"));
        styleProductToggle(m_jAccumulatesPoints);
        leftFieldsPanel.add(m_jAccumulatesPoints, gbcLeft);
        gbcLeft.gridwidth = 1;
        gbcLeft.fill = java.awt.GridBagConstraints.NONE;
        gbcLeft.insets = new java.awt.Insets(6, 0, 6, 12);

        // Panel Secundario (Derecha)
        javax.swing.JPanel rightFieldsPanel = new javax.swing.JPanel(new java.awt.GridBagLayout());
        rightFieldsPanel.setOpaque(false);
        java.awt.GridBagConstraints gbcRight = new java.awt.GridBagConstraints();
        gbcRight.anchor = java.awt.GridBagConstraints.WEST;
        gbcRight.insets = new java.awt.Insets(6, 0, 6, 12);
        gbcRight.fill = java.awt.GridBagConstraints.NONE;

        int rowRight = 0;

        // Lote
        gbcRight.gridx = 0;
        gbcRight.gridy = rowRight;
        jLabelLote.setText("Lote:");
        jLabelLote.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 13));
        jLabelLote.setForeground(new java.awt.Color(55, 65, 81));
        jLabelLote.setPreferredSize(new java.awt.Dimension(120, 24));
        if (jLabelLote.getParent() != null)
            jLabelLote.getParent().remove(jLabelLote);
        rightFieldsPanel.add(jLabelLote, gbcRight);
        gbcRight.gridx = 1;
        gbcRight.weightx = 1.0;
        gbcRight.fill = java.awt.GridBagConstraints.HORIZONTAL;
        styleProductTextField(m_jLote, 220);
        if (m_jLote.getParent() != null)
            m_jLote.getParent().remove(m_jLote);
        rightFieldsPanel.add(m_jLote, gbcRight);
        gbcRight.weightx = 0.0;
        gbcRight.fill = java.awt.GridBagConstraints.NONE;
        rowRight++;

        // Modelo
        gbcRight.gridx = 0;
        gbcRight.gridy = rowRight;
        jLabelModelo.setText("Modelo:");
        jLabelModelo.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 13));
        jLabelModelo.setForeground(new java.awt.Color(55, 65, 81));
        jLabelModelo.setPreferredSize(new java.awt.Dimension(120, 24));
        if (jLabelModelo.getParent() != null)
            jLabelModelo.getParent().remove(jLabelModelo);
        rightFieldsPanel.add(jLabelModelo, gbcRight);
        gbcRight.gridx = 1;
        gbcRight.weightx = 1.0;
        gbcRight.fill = java.awt.GridBagConstraints.HORIZONTAL;
        styleProductTextField(m_jModelo, 220);
        if (m_jModelo.getParent() != null)
            m_jModelo.getParent().remove(m_jModelo);
        rightFieldsPanel.add(m_jModelo, gbcRight);
        gbcRight.weightx = 0.0;
        gbcRight.fill = java.awt.GridBagConstraints.NONE;
        rowRight++;

        // Color
        gbcRight.gridx = 0;
        gbcRight.gridy = rowRight;
        jLabelColor.setText("Color:");
        jLabelColor.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 13));
        jLabelColor.setForeground(new java.awt.Color(55, 65, 81));
        jLabelColor.setPreferredSize(new java.awt.Dimension(120, 24));
        if (jLabelColor.getParent() != null)
            jLabelColor.getParent().remove(jLabelColor);
        rightFieldsPanel.add(jLabelColor, gbcRight);
        gbcRight.gridx = 1;
        gbcRight.weightx = 1.0;
        gbcRight.fill = java.awt.GridBagConstraints.HORIZONTAL;
        styleProductTextField(m_jColor, 220);
        if (m_jColor.getParent() != null)
            m_jColor.getParent().remove(m_jColor);
        rightFieldsPanel.add(m_jColor, gbcRight);
        gbcRight.weightx = 0.0;
        gbcRight.fill = java.awt.GridBagConstraints.NONE;
        rowRight++;

        // Voltaje
        gbcRight.gridx = 0;
        gbcRight.gridy = rowRight;
        jLabelVoltaje.setText("Voltaje:");
        jLabelVoltaje.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 13));
        jLabelVoltaje.setForeground(new java.awt.Color(55, 65, 81));
        jLabelVoltaje.setPreferredSize(new java.awt.Dimension(120, 24));
        if (jLabelVoltaje.getParent() != null)
            jLabelVoltaje.getParent().remove(jLabelVoltaje);
        rightFieldsPanel.add(jLabelVoltaje, gbcRight);
        gbcRight.gridx = 1;
        gbcRight.weightx = 1.0;
        gbcRight.fill = java.awt.GridBagConstraints.HORIZONTAL;
        styleProductTextField(m_jVoltaje, 220);
        if (m_jVoltaje.getParent() != null)
            m_jVoltaje.getParent().remove(m_jVoltaje);
        rightFieldsPanel.add(m_jVoltaje, gbcRight);
        gbcRight.weightx = 0.0;
        gbcRight.fill = java.awt.GridBagConstraints.NONE;
        rowRight++;

        // NoSerie
        gbcRight.gridx = 0;
        gbcRight.gridy = rowRight;
        jLabelNoSerie.setText("No. Serie:");
        jLabelNoSerie.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 13));
        jLabelNoSerie.setForeground(new java.awt.Color(55, 65, 81));
        jLabelNoSerie.setPreferredSize(new java.awt.Dimension(120, 24));
        if (jLabelNoSerie.getParent() != null)
            jLabelNoSerie.getParent().remove(jLabelNoSerie);
        rightFieldsPanel.add(jLabelNoSerie, gbcRight);
        gbcRight.gridx = 1;
        gbcRight.weightx = 1.0;
        gbcRight.fill = java.awt.GridBagConstraints.HORIZONTAL;
        styleProductTextField(m_jNoSerie, 220);
        if (m_jNoSerie.getParent() != null)
            m_jNoSerie.getParent().remove(m_jNoSerie);
        rightFieldsPanel.add(m_jNoSerie, gbcRight);
        gbcRight.weightx = 0.0;
        gbcRight.fill = java.awt.GridBagConstraints.NONE;
        rowRight++;

        // Departamento
        gbcRight.gridx = 0;
        gbcRight.gridy = rowRight;
        javax.swing.JLabel lblDept = createProductFormLabel("Departamento");
        lblDept.setPreferredSize(new java.awt.Dimension(120, 24));
        rightFieldsPanel.add(lblDept, gbcRight);
        gbcRight.gridx = 1;
        gbcRight.weightx = 1.0;
        gbcRight.fill = java.awt.GridBagConstraints.HORIZONTAL;
        styleProductComboBox(m_jCategory, 220);
        if (m_jCategory.getParent() != null)
            m_jCategory.getParent().remove(m_jCategory);
        rightFieldsPanel.add(m_jCategory, gbcRight);
        gbcRight.weightx = 0.0;
        gbcRight.fill = java.awt.GridBagConstraints.NONE;
        rowRight++;

        // Impuesto
        gbcRight.gridx = 0;
        gbcRight.gridy = rowRight;
        jLabel7.setText("Impuesto:");
        jLabel7.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 13));
        jLabel7.setForeground(new java.awt.Color(55, 65, 81));
        jLabel7.setPreferredSize(new java.awt.Dimension(120, 24));
        if (jLabel7.getParent() != null)
            jLabel7.getParent().remove(jLabel7);
        rightFieldsPanel.add(jLabel7, gbcRight);
        gbcRight.gridx = 1;
        gbcRight.weightx = 1.0;
        gbcRight.fill = java.awt.GridBagConstraints.HORIZONTAL;
        styleProductComboBox(m_jTax, 220);
        if (m_jTax.getParent() != null)
            m_jTax.getParent().remove(m_jTax);
        rightFieldsPanel.add(m_jTax, gbcRight);
        gbcRight.weightx = 0.0;
        gbcRight.fill = java.awt.GridBagConstraints.NONE;
        rowRight++;

        // Línea separadora naranja
        gbcRight.gridx = 0;
        gbcRight.gridy = rowRight;
        gbcRight.gridwidth = 2;
        gbcRight.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gbcRight.insets = new java.awt.Insets(10, 0, 8, 0);
        javax.swing.JSeparator separator = new javax.swing.JSeparator();
        separator.setForeground(new java.awt.Color(255, 140, 0));
        separator.setPreferredSize(new java.awt.Dimension(180, 2));
        rightFieldsPanel.add(separator, gbcRight);
        gbcRight.gridwidth = 1;
        gbcRight.fill = java.awt.GridBagConstraints.NONE;
        gbcRight.insets = new java.awt.Insets(6, 0, 6, 12);
        rowRight++;

        // Título "Inventario"
        gbcRight.gridx = 0;
        gbcRight.gridy = rowRight;
        gbcRight.gridwidth = 2;
        gbcRight.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gbcRight.insets = new java.awt.Insets(0, 0, 8, 0);
        javax.swing.JPanel inventarioTitlePanel = new javax.swing.JPanel(
                new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 0, 0));
        inventarioTitlePanel.setBackground(java.awt.Color.WHITE);
        javax.swing.JLabel lblInventario = new javax.swing.JLabel("Inventario");
        lblInventario.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 15));
        lblInventario.setForeground(new java.awt.Color(249, 115, 22));
        inventarioTitlePanel.add(lblInventario);
        rightFieldsPanel.add(inventarioTitlePanel, gbcRight);
        gbcRight.gridwidth = 1;
        gbcRight.fill = java.awt.GridBagConstraints.NONE;
        gbcRight.insets = new java.awt.Insets(6, 0, 6, 12);
        rowRight++;

        // Checkbox "Este producto SI utiliza inventario"
        gbcRight.gridx = 0;
        gbcRight.gridy = rowRight;
        gbcRight.gridwidth = 2;
        gbcRight.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gbcRight.insets = new java.awt.Insets(0, 0, 8, 0);
        boolean initialUsesInventory = !m_jService.isSelected();
        chkUseInventory = new javax.swing.JCheckBox("Este producto SI utiliza inventario.", initialUsesInventory);
        styleProductToggle(chkUseInventory);
        chkUseInventory.addActionListener(e -> {
            boolean useInventory = chkUseInventory.isSelected();
            m_jService.setSelected(!useInventory);
            m_jStockCurrent.setEditable(useInventory);
            m_jStockMinimum.setEditable(useInventory);
        });
        java.awt.event.ActionListener serviceListener = e -> {
            boolean useInventory = !m_jService.isSelected();
            if (chkUseInventory != null && chkUseInventory.isSelected() != useInventory) {
                chkUseInventory.setSelected(useInventory);
            }
            m_jStockCurrent.setEditable(useInventory);
            m_jStockMinimum.setEditable(useInventory);
        };
        m_jService.addActionListener(serviceListener);
        rightFieldsPanel.add(chkUseInventory, gbcRight);
        gbcRight.gridwidth = 1;
        gbcRight.fill = java.awt.GridBagConstraints.NONE;
        gbcRight.insets = new java.awt.Insets(6, 0, 6, 12);
        rowRight++;

        // Cantidad Actual
        gbcRight.gridx = 0;
        gbcRight.gridy = rowRight;
        jLabelStockCurrent.setText("Cant. Actual:");
        jLabelStockCurrent.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 13));
        jLabelStockCurrent.setForeground(new java.awt.Color(55, 65, 81));
        jLabelStockCurrent.setPreferredSize(new java.awt.Dimension(120, 24));
        if (jLabelStockCurrent.getParent() != null)
            jLabelStockCurrent.getParent().remove(jLabelStockCurrent);
        rightFieldsPanel.add(jLabelStockCurrent, gbcRight);
        gbcRight.gridx = 1;
        gbcRight.weightx = 1.0;
        gbcRight.fill = java.awt.GridBagConstraints.HORIZONTAL;
        styleProductTextField(m_jStockCurrent, 220);
        m_jStockCurrent.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        m_jStockCurrent.setEditable(initialUsesInventory);
        if (m_jStockCurrent.getParent() != null)
            m_jStockCurrent.getParent().remove(m_jStockCurrent);
        rightFieldsPanel.add(m_jStockCurrent, gbcRight);
        gbcRight.weightx = 0.0;
        gbcRight.fill = java.awt.GridBagConstraints.NONE;
        rowRight++;

        // Mínimo
        gbcRight.gridx = 0;
        gbcRight.gridy = rowRight;
        jLabelStockMinimum.setText("Mínimo:");
        jLabelStockMinimum.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 13));
        jLabelStockMinimum.setForeground(new java.awt.Color(55, 65, 81));
        jLabelStockMinimum.setPreferredSize(new java.awt.Dimension(120, 24));
        if (jLabelStockMinimum.getParent() != null)
            jLabelStockMinimum.getParent().remove(jLabelStockMinimum);
        rightFieldsPanel.add(jLabelStockMinimum, gbcRight);
        gbcRight.gridx = 1;
        gbcRight.weightx = 1.0;
        gbcRight.fill = java.awt.GridBagConstraints.HORIZONTAL;
        styleProductTextField(m_jStockMinimum, 220);
        m_jStockMinimum.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        m_jStockMinimum.setEditable(initialUsesInventory);
        if (m_jStockMinimum.getParent() != null)
            m_jStockMinimum.getParent().remove(m_jStockMinimum);
        rightFieldsPanel.add(m_jStockMinimum, gbcRight);
        gbcRight.weightx = 0.0;
        gbcRight.fill = java.awt.GridBagConstraints.NONE;
        rowRight++;

        // Contenedor GridLayout para poner los dos bloques lado a lado
        javax.swing.JPanel fieldsContainer = new javax.swing.JPanel(new java.awt.GridLayout(1, 2, 30, 0));
        fieldsContainer.setOpaque(false);
        fieldsContainer.add(leftFieldsPanel);
        fieldsContainer.add(rightFieldsPanel);

        mainFieldsPanel.add(fieldsContainer, java.awt.BorderLayout.CENTER);

        // --- Panel 2: Proveedor ---
        javax.swing.JPanel supplierTabPanel = new javax.swing.JPanel(new java.awt.GridBagLayout());
        supplierTabPanel.setBackground(java.awt.Color.WHITE);
        supplierTabPanel.setBorder(createProductSectionBorder());

        java.awt.GridBagConstraints gbcSup = new java.awt.GridBagConstraints();
        gbcSup.insets = new java.awt.Insets(12, 18, 12, 18);

        // 1. Selector de Proveedor en la parte superior (arriba)
        gbcSup.gridx = 0;
        gbcSup.gridy = 0;
        gbcSup.gridwidth = 1;
        gbcSup.fill = java.awt.GridBagConstraints.NONE;
        gbcSup.anchor = java.awt.GridBagConstraints.WEST;
        gbcSup.weightx = 0.0;
        gbcSup.weighty = 0.0;

        javax.swing.JLabel lblSupplier = createProductFormLabel("Proveedor");
        supplierTabPanel.add(lblSupplier, gbcSup);

        gbcSup.gridx = 1;
        styleProductComboBox(m_jSupplier, 300);
        if (m_jSupplier.getParent() != null)
            m_jSupplier.getParent().remove(m_jSupplier);
        supplierTabPanel.add(m_jSupplier, gbcSup);

        // Action listener to reload table when selection changes
        m_jSupplier.addActionListener(e -> updateSupplierProductsTable());

        gbcSup.gridx = 2;
        jBtnSupplier.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 12));
        jBtnSupplier.setPreferredSize(new java.awt.Dimension(140, 30));
        jBtnSupplier.setBackground(java.awt.Color.WHITE);
        jBtnSupplier.setForeground(new java.awt.Color(15, 23, 42));
        jBtnSupplier.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(203, 213, 225), 1));
        jBtnSupplier.setFocusPainted(false);
        if (jBtnSupplier.getParent() != null)
            jBtnSupplier.getParent().remove(jBtnSupplier);
        supplierTabPanel.add(jBtnSupplier, gbcSup);

        // 2. Línea separadora
        gbcSup.gridy = 1;
        gbcSup.gridx = 0;
        gbcSup.gridwidth = 4;
        gbcSup.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gbcSup.insets = new java.awt.Insets(15, 18, 15, 18);
        javax.swing.JSeparator sepSup = new javax.swing.JSeparator();
        sepSup.setForeground(new java.awt.Color(226, 232, 240)); // Gris slate sutil
        supplierTabPanel.add(sepSup, gbcSup);

        // 3. Título de la tabla de productos asociados
        gbcSup.gridy = 2;
        gbcSup.gridx = 0;
        gbcSup.gridwidth = 4;
        gbcSup.insets = new java.awt.Insets(0, 18, 8, 18);
        javax.swing.JLabel lblAssociatedProducts = new javax.swing.JLabel("Productos asociados a este proveedor:");
        lblAssociatedProducts.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 14));
        lblAssociatedProducts.setForeground(new java.awt.Color(15, 23, 42));
        supplierTabPanel.add(lblAssociatedProducts, gbcSup);

        // 4. Tabla de productos asociados abajo
        gbcSup.gridy = 3;
        gbcSup.gridx = 0;
        gbcSup.gridwidth = 4;
        gbcSup.fill = java.awt.GridBagConstraints.BOTH;
        gbcSup.weightx = 1.0;
        gbcSup.weighty = 1.0;
        gbcSup.insets = new java.awt.Insets(4, 18, 12, 18);

        String[] supplierProdColumns = { "Código", "Referencia", "Nombre", "Precio Venta" };
        supplierProductsTableModel = new javax.swing.table.DefaultTableModel(supplierProdColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        jTableSupplierProducts = new javax.swing.JTable(supplierProductsTableModel);
        jTableSupplierProducts.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 12));
        jTableSupplierProducts.setRowHeight(28);
        jTableSupplierProducts.getTableHeader().setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12));
        jTableSupplierProducts.setSelectionBackground(new java.awt.Color(204, 251, 241)); // color de selección teal
        jTableSupplierProducts.setSelectionForeground(new java.awt.Color(15, 23, 42));
        jTableSupplierProducts.setGridColor(new java.awt.Color(241, 245, 249));

        jTableSupplierProducts.getColumnModel().getColumn(0).setPreferredWidth(150); // Código
        jTableSupplierProducts.getColumnModel().getColumn(1).setPreferredWidth(150); // Referencia
        jTableSupplierProducts.getColumnModel().getColumn(2).setPreferredWidth(450); // Nombre
        jTableSupplierProducts.getColumnModel().getColumn(3).setPreferredWidth(120); // Precio Venta

        javax.swing.JScrollPane supplierProdScroll = new javax.swing.JScrollPane(jTableSupplierProducts);
        supplierProdScroll.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(226, 232, 240)));
        supplierProdScroll.getViewport().setBackground(java.awt.Color.WHITE);
        supplierTabPanel.add(supplierProdScroll, gbcSup);

        // Crear el Tabbed Pane del Editor
        javax.swing.JTabbedPane editorTabbedPane = new javax.swing.JTabbedPane();
        editorTabbedPane.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 13));
        editorTabbedPane.setBackground(java.awt.Color.WHITE);
        editorTabbedPane.setForeground(new java.awt.Color(30, 41, 59));

        // Envolver cada panel en un JScrollPane
        String[] auditColumnNames = { "Fecha y Hora", "Usuario", "Acción", "Detalles" };
        auditHistoryTableModel = new javax.swing.table.DefaultTableModel(auditColumnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        jTableProductAuditHistory = new javax.swing.JTable(auditHistoryTableModel);
        jTableProductAuditHistory.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jTableProductAuditHistory.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 12));
        jTableProductAuditHistory.setRowHeight(24);
        jTableProductAuditHistory.getTableHeader().setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12));

        jTableProductAuditHistory.setSelectionBackground(new java.awt.Color(254, 243, 199)); // Amber selection
        jTableProductAuditHistory.setSelectionForeground(new java.awt.Color(120, 53, 4));
        jTableProductAuditHistory.setGridColor(new java.awt.Color(241, 245, 249));

        jTableProductAuditHistory.getColumnModel().getColumn(0).setPreferredWidth(110); // Fecha
        jTableProductAuditHistory.getColumnModel().getColumn(1).setPreferredWidth(80); // Usuario
        jTableProductAuditHistory.getColumnModel().getColumn(2).setPreferredWidth(70); // Acción
        jTableProductAuditHistory.getColumnModel().getColumn(3).setPreferredWidth(100); // Detalles

        javax.swing.JScrollPane auditTableScrollPane = new javax.swing.JScrollPane(jTableProductAuditHistory);
        auditTableScrollPane.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(226, 232, 240)));

        txtAuditDetails = new javax.swing.JTextArea();
        txtAuditDetails.setEditable(false);
        txtAuditDetails.setFont(new java.awt.Font("Consolas", java.awt.Font.PLAIN, 12));
        txtAuditDetails.setLineWrap(true);
        txtAuditDetails.setWrapStyleWord(true);
        txtAuditDetails.setBackground(new java.awt.Color(248, 250, 252));
        txtAuditDetails.setForeground(new java.awt.Color(51, 65, 85));
        javax.swing.JScrollPane auditDetailScrollPane = new javax.swing.JScrollPane(txtAuditDetails);
        auditDetailScrollPane.setBorder(javax.swing.BorderFactory.createTitledBorder(
                javax.swing.BorderFactory.createLineBorder(new java.awt.Color(226, 232, 240)),
                "Detalles del Cambio Seleccionado",
                javax.swing.border.TitledBorder.LEFT,
                javax.swing.border.TitledBorder.TOP,
                new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12),
                new java.awt.Color(71, 85, 105)));

        jTableProductAuditHistory.getSelectionModel()
                .addListSelectionListener(new javax.swing.event.ListSelectionListener() {
                    @Override
                    public void valueChanged(javax.swing.event.ListSelectionEvent e) {
                        int selectedRow = jTableProductAuditHistory.getSelectedRow();
                        java.util.List<Object[]> dataList = (java.util.List<Object[]>) jTableProductAuditHistory
                                .getClientProperty("auditDataList");
                        if (selectedRow >= 0 && dataList != null && selectedRow < dataList.size()) {
                            txtAuditDetails.setText(String.valueOf(dataList.get(selectedRow)[3]));
                        } else {
                            txtAuditDetails.setText("");
                        }
                    }
                });

        javax.swing.JPanel productAuditPanel = new javax.swing.JPanel(new java.awt.BorderLayout(8, 8));
        productAuditPanel.setBackground(java.awt.Color.WHITE);
        productAuditPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(
                javax.swing.BorderFactory.createLineBorder(new java.awt.Color(226, 232, 240)),
                "Historial de Cambios del Producto",
                javax.swing.border.TitledBorder.LEFT,
                javax.swing.border.TitledBorder.TOP,
                new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 13),
                new java.awt.Color(30, 41, 59)));
        productAuditPanel.setPreferredSize(new java.awt.Dimension(360, 450));
        productAuditPanel.add(auditTableScrollPane, java.awt.BorderLayout.CENTER);

        javax.swing.JPanel upperPanel = new javax.swing.JPanel(new java.awt.BorderLayout());
        upperPanel.setOpaque(false);
        upperPanel.add(mainFieldsPanel, java.awt.BorderLayout.CENTER);

        contentPanel.add(upperPanel, java.awt.BorderLayout.CENTER);
        javax.swing.JScrollPane generalScroll = new javax.swing.JScrollPane(contentPanel);
        generalScroll.setBorder(null);
        generalScroll.getViewport().setBackground(java.awt.Color.WHITE);

        javax.swing.JScrollPane supplierScroll = new javax.swing.JScrollPane(supplierTabPanel);
        supplierScroll.setBorder(null);
        supplierScroll.getViewport().setBackground(java.awt.Color.WHITE);

        // Inicializar Historial de Stock
        String[] stockColumnNames = { "Fecha y Hora", "Tipo / Razón", "Ubicación", "Unidades", "Precio Unit.", "Total",
                "Usuario", "Proveedor", "Doc. Proveedor" };
        stockHistoryTableModel = new javax.swing.table.DefaultTableModel(stockColumnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        jTableProductStockHistory = new javax.swing.JTable(stockHistoryTableModel);
        jTableProductStockHistory.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jTableProductStockHistory.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 12));
        jTableProductStockHistory.setRowHeight(24);
        jTableProductStockHistory.getTableHeader().setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12));
        jTableProductStockHistory.setSelectionBackground(new java.awt.Color(254, 243, 199)); // Amber selection
        jTableProductStockHistory.setSelectionForeground(new java.awt.Color(120, 53, 4));
        jTableProductStockHistory.setGridColor(new java.awt.Color(241, 245, 249));

        jTableProductStockHistory.getColumnModel().getColumn(0).setPreferredWidth(140); // Fecha
        jTableProductStockHistory.getColumnModel().getColumn(1).setPreferredWidth(130); // Razón
        jTableProductStockHistory.getColumnModel().getColumn(2).setPreferredWidth(120); // Ubicación
        jTableProductStockHistory.getColumnModel().getColumn(3).setPreferredWidth(80); // Unidades
        jTableProductStockHistory.getColumnModel().getColumn(4).setPreferredWidth(100); // Precio Unit
        jTableProductStockHistory.getColumnModel().getColumn(5).setPreferredWidth(100); // Total
        jTableProductStockHistory.getColumnModel().getColumn(6).setPreferredWidth(100); // Usuario
        jTableProductStockHistory.getColumnModel().getColumn(7).setPreferredWidth(120); // Proveedor
        jTableProductStockHistory.getColumnModel().getColumn(8).setPreferredWidth(120); // Doc. Prov

        jTableProductStockHistory.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public java.awt.Component getTableCellRendererComponent(javax.swing.JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                java.awt.Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row,
                        column);
                if (column == 3 || column == 5) {
                    String valStr = value != null ? value.toString() : "";
                    if (valStr.startsWith("+")) {
                        c.setForeground(new java.awt.Color(25, 135, 84)); // Green
                    } else if (valStr.startsWith("-")) {
                        c.setForeground(new java.awt.Color(220, 53, 69)); // Red
                    } else {
                        c.setForeground(table.getForeground());
                    }
                } else {
                    c.setForeground(table.getForeground());
                }
                if (isSelected) {
                    c.setBackground(table.getSelectionBackground());
                    c.setForeground(table.getSelectionForeground());
                } else {
                    c.setBackground(table.getBackground());
                }
                return c;
            }
        });

        javax.swing.JScrollPane stockHistoryScrollPane = new javax.swing.JScrollPane(jTableProductStockHistory);
        stockHistoryScrollPane.setBorder(null);
        stockHistoryScrollPane.getViewport().setBackground(java.awt.Color.WHITE);

        editorTabbedPane.addTab("Datos Generales", generalScroll);
        editorTabbedPane.addTab("Proveedor", supplierScroll);

        if (m_variationPanel != null) {
            if (m_variationPanel.getParent() != null)
                m_variationPanel.getParent().remove(m_variationPanel);
            editorTabbedPane.addTab("Variación", m_variationPanel);
        }

        if (jPanel4 != null) {
            if (jPanel4.getParent() != null)
                jPanel4.getParent().remove(jPanel4);
            editorTabbedPane.addTab("Botón", jPanel4);
        }

        editorTabbedPane.addTab("Historial de Stock", stockHistoryScrollPane);

        // Historial de cambios en pestaña dedicada
        javax.swing.JPanel auditTabPanel = new javax.swing.JPanel(new java.awt.BorderLayout(0, 10));
        auditTabPanel.setBackground(java.awt.Color.WHITE);
        auditTabPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(15, 15, 15, 15));
        if (productAuditPanel.getParent() != null)
            productAuditPanel.getParent().remove(productAuditPanel);
        if (auditDetailScrollPane.getParent() != null)
            auditDetailScrollPane.getParent().remove(auditDetailScrollPane);
        auditTabPanel.add(productAuditPanel, java.awt.BorderLayout.CENTER);
        auditDetailScrollPane.setPreferredSize(new java.awt.Dimension(800, 120));
        auditTabPanel.add(auditDetailScrollPane, java.awt.BorderLayout.SOUTH);
        editorTabbedPane.addTab("Historial de Cambios", auditTabPanel);

        mainCombinedPanel.add(editorTabbedPane, java.awt.BorderLayout.CENTER);

        // Ocultar las pestañas y mostrar solo el panel combinado
        jTabbedPane1.setVisible(false);
        jTabbedPane1.setEnabled(false);
        add(mainCombinedPanel, java.awt.BorderLayout.CENTER);
        // Asegurar que el panel combinado sea visible y tenga el tamaño correcto
        mainCombinedPanel.setVisible(true);
        mainCombinedPanel.setOpaque(true);
        // Establecer tamaño preferido del panel principal para que el contenido se
        // muestre
        mainCombinedPanel.setPreferredSize(new java.awt.Dimension(1120, 720));
        revalidate();
        repaint();

        // Configurar scroll automático cuando los campos reciben foco (después de
        // revalidate)
        java.util.List<javax.swing.JComponent> focusableComponents = new java.util.ArrayList<>();
        collectFocusableComponents(mainFieldsPanel, focusableComponents);
        for (javax.swing.JComponent comp : focusableComponents) {
            comp.addFocusListener(new java.awt.event.FocusAdapter() {
                @Override
                public void focusGained(java.awt.event.FocusEvent e) {
                    // Hacer scroll para que el componente sea visible
                    java.awt.Rectangle rect = comp.getBounds();
                    java.awt.Point loc = javax.swing.SwingUtilities.convertPoint(comp.getParent(), rect.getLocation(),
                            contentPanel);
                    rect.setLocation(loc);
                    rect.setSize(comp.getSize());
                    // Agregar un margen para mejor visibilidad
                    rect.grow(0, 20);
                    contentPanel.scrollRectToVisible(rect);
                    // NO robar el foco del componente - dejar que el usuario edite el campo
                }
            });
        }

        // Cargar tabla de stock automáticamente cuando hay un producto
        if (productId != null) {
            showStockTableAutomatically();
        }
    }// </editor-fold>//GEN-END:initComponents

    // Método helper para recolectar componentes que pueden recibir foco
    private void collectFocusableComponents(java.awt.Container container, java.util.List<javax.swing.JComponent> list) {
        for (java.awt.Component comp : container.getComponents()) {
            if (comp instanceof javax.swing.JComponent) {
                javax.swing.JComponent jcomp = (javax.swing.JComponent) comp;
                if (jcomp.isFocusable() && jcomp.isEnabled() && jcomp.isVisible()) {
                    list.add(jcomp);
                }
            }
            if (comp instanceof java.awt.Container) {
                collectFocusableComponents((java.awt.Container) comp, list);
            }
        }
    }

    private javax.swing.border.Border createProductSectionBorder() {
        return javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createLineBorder(new java.awt.Color(226, 232, 240), 1),
                javax.swing.BorderFactory.createEmptyBorder(18, 18, 18, 18));
    }

    private javax.swing.JPanel createProductSectionHeader(String title, String description) {
        javax.swing.JPanel panel = new javax.swing.JPanel();
        panel.setLayout(new javax.swing.BoxLayout(panel, javax.swing.BoxLayout.Y_AXIS));
        panel.setOpaque(false);

        javax.swing.JLabel titleLabel = new javax.swing.JLabel(title);
        titleLabel.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 16));
        titleLabel.setForeground(new java.awt.Color(15, 23, 42));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(titleLabel);

        if (description != null && !description.trim().isEmpty()) {
            javax.swing.JLabel descriptionLabel = new javax.swing.JLabel(description);
            descriptionLabel.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 12));
            descriptionLabel.setForeground(new java.awt.Color(100, 116, 139));
            descriptionLabel.setBorder(javax.swing.BorderFactory.createEmptyBorder(4, 0, 0, 0));
            descriptionLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            panel.add(descriptionLabel);
        }

        return panel;
    }

    private javax.swing.JLabel createProductFormLabel(String text) {
        javax.swing.JLabel label = new javax.swing.JLabel(text + ":");
        label.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 13));
        label.setForeground(new java.awt.Color(55, 65, 81));
        label.setPreferredSize(new java.awt.Dimension(150, 24));
        return label;
    }

    private void styleProductTextField(javax.swing.JTextField field, int width) {
        field.setPreferredSize(new java.awt.Dimension(width, 30));
        field.setMinimumSize(new java.awt.Dimension(Math.min(width, 180), 30));
        field.setMaximumSize(new java.awt.Dimension(width, 30));
        field.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 13));
        field.setForeground(new java.awt.Color(15, 23, 42));
        field.setBackground(java.awt.Color.WHITE);
        field.setCaretColor(new java.awt.Color(15, 23, 42));
        field.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createLineBorder(new java.awt.Color(203, 213, 225), 1),
                javax.swing.BorderFactory.createEmptyBorder(4, 10, 4, 10)));
    }

    private void styleProductComboBox(javax.swing.JComboBox comboBox, int width) {
        comboBox.setPreferredSize(new java.awt.Dimension(width, 30));
        comboBox.setMinimumSize(new java.awt.Dimension(Math.min(width, 180), 30));
        comboBox.setMaximumSize(new java.awt.Dimension(width, 34));
        comboBox.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 13));
        comboBox.setBackground(java.awt.Color.WHITE);
        comboBox.setForeground(new java.awt.Color(15, 23, 42));
        comboBox.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(203, 213, 225), 1));
    }

    private void styleProductToggle(javax.swing.AbstractButton button) {
        button.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 13));
        button.setForeground(new java.awt.Color(30, 41, 59));
        button.setOpaque(false);
        button.setFocusPainted(false);
    }

    private void jButtonHTMLActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jButtonHTMLActionPerformed
        setButtonHTML();
    }// GEN-LAST:event_jButtonHTMLActionPerformed

    private void jBtnXmlActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jBtnXmlActionPerformed
        if (txtAttributes.getText() == null || txtAttributes.getText().isBlank()) {
            txtAttributes.setText(
                    "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>  \n"
                            + "<!DOCTYPE properties SYSTEM \"http://java.sun.com/dtd/properties.dtd\">\n"
                            + "<properties>\n"
                            + "    <entry key=\"identifier\">value</entry>\n"
                            + "</properties>");
        }
    }// GEN-LAST:event_jBtnXmlActionPerformed

    private void jBtnSmallActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jBtnSmallActionPerformed
        btn = 4;
        setDisplay(btn);
    }// GEN-LAST:event_jBtnSmallActionPerformed

    private void jBtnBreakActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jBtnBreakActionPerformed
        btn = 1;
        setDisplay(btn);
    }// GEN-LAST:event_jBtnBreakActionPerformed

    private void jBtnColourActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jBtnColourActionPerformed
        btn = 2;
        setDisplay(btn);
    }// GEN-LAST:event_jBtnColourActionPerformed

    private void jBtnLargeActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jBtnLargeActionPerformed
        btn = 3;
        setDisplay(btn);
    }// GEN-LAST:event_jBtnLargeActionPerformed

    private void jBtnBoldActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jBtnBoldActionPerformed
        btn = 5;
        setDisplay(btn);
    }// GEN-LAST:event_jBtnBoldActionPerformed

    private void jBtnItalicActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jBtnItalicActionPerformed
        btn = 6;
        setDisplay(btn);
    }// GEN-LAST:event_jBtnItalicActionPerformed

    private void jBtnImageActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jBtnImageActionPerformed
        btn = 7;
        setDisplay(btn);
    }// GEN-LAST:event_jBtnImageActionPerformed

    private void jBtnResetActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jBtnResetActionPerformed
        btn = 8;
        setDisplay(btn);
    }// GEN-LAST:event_jBtnResetActionPerformed

    private void jBtnStyleActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jBtnStyleActionPerformed
        btn = 9;
        setDisplay(btn);
    }// GEN-LAST:event_jBtnStyleActionPerformed

    private void m_jbtndateActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_m_jbtndateActionPerformed

        Date date;
        try {
            date = Formats.TIMESTAMP.parseValue(m_jdate.getText());
        } catch (BasicException e) {
            date = null;
        }
        date = JCalendarDialog.showCalendarTime(this, date);
        if (date != null) {
            m_jdate.setText(Formats.TIMESTAMP.formatValue(date));
        }
    }// GEN-LAST:event_m_jbtndateActionPerformed

    private void updateSupplierProductsTable() {
        if (supplierProductsTableModel == null) {
            return;
        }
        supplierProductsTableModel.setRowCount(0);
        String supplierId = (String) m_SuppliersModel.getSelectedKey();
        if (supplierId == null || supplierId.trim().isEmpty()) {
            return;
        }

        try (java.sql.Connection con = appView.getSession().getConnection();
                java.sql.PreparedStatement ps = con.prepareStatement(
                        "SELECT CODE, REFERENCE, NAME, PRICESELL FROM products WHERE SUPPLIER = ? ORDER BY NAME")) {
            ps.setString(1, supplierId);
            try (java.sql.ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String code = rs.getString(1);
                    String ref = rs.getString(2);
                    String name = rs.getString(3);
                    double price = rs.getDouble(4);
                    supplierProductsTableModel.addRow(new Object[] {
                            code != null ? code : "",
                            ref != null ? ref : "",
                            name != null ? name : "",
                            Formats.CURRENCY.formatValue(price)
                    });
                }
            }
        } catch (java.sql.SQLException ex) {
            LOGGER.log(Level.WARNING, "Error loading supplier products: " + ex.getMessage(), ex);
        }
    }

    /**
     * Muestra automáticamente la tabla de stock cuando se selecciona la pestaña
     * Stock
     */
    private void showStockTableAutomatically() {
        if (productId == null || m_jStockCurrent == null || m_jStockMinimum == null) {
            LOGGER.log(Level.FINE, "showStockTableAutomatically: productId o campos de stock son null");
            return;
        }
        try {
            LOGGER.log(Level.FINE, "showStockTableAutomatically: Cargando stock para producto: " + productId);
            List<ProductStock> stockList = getProductOfName(productId);
            LOGGER.log(Level.FINE,
                    "showStockTableAutomatically: stockList size: " + (stockList != null ? stockList.size() : 0));

            if (stockList != null && !stockList.isEmpty()) {
                // Obtener la primera entrada (o sumar todas las cantidades)
                double totalCurrent = 0.0;
                double totalMinimum = 0.0;

                for (ProductStock stock : stockList) {
                    LOGGER.log(Level.FINE, "showStockTableAutomatically: Stock - Location: " + stock.getLocation()
                            + ", Units: " + stock.getUnits());
                    if (stock.getUnits() != null) {
                        totalCurrent += stock.getUnits();
                    }
                    if (stock.getMinimum() != null) {
                        totalMinimum += stock.getMinimum();
                    }
                }

                String stockCurrentText = Formats.DOUBLE.formatValue(totalCurrent);
                String stockMinimumText = Formats.DOUBLE.formatValue(totalMinimum);

                LOGGER.log(Level.FINE,
                        "showStockTableAutomatically: Total Current: " + totalCurrent + ", Text: " + stockCurrentText);

                // Guardar el valor inicial del stock (solo la primera vez que se carga para
                // este producto)
                // IMPORTANTE: Solo establecer si es null (fue reseteado en setValues() para un
                // nuevo producto)
                if (initialStockValue == null) {
                    initialStockValue = totalCurrent;
                    LOGGER.log(Level.INFO,
                            "showStockTableAutomatically: Guardado valor inicial del stock: " + initialStockValue
                                    + " para producto: " + productId);
                }

                // Forzar actualización del campo de texto
                SwingUtilities.invokeLater(() -> {
                    m_jStockCurrent.setText(stockCurrentText);
                    m_jStockMinimum.setText(stockMinimumText);
                    m_jStockCurrent.revalidate();
                    m_jStockCurrent.repaint();
                    m_jStockMinimum.revalidate();
                    m_jStockMinimum.repaint();
                    if (m_jStockGeneral != null) {
                        m_jStockGeneral.setText(stockCurrentText);
                        m_jStockGeneral.revalidate();
                        m_jStockGeneral.repaint();
                    }
                });
            } else {
                LOGGER.log(Level.FINE, "showStockTableAutomatically: No hay stock, estableciendo a 0");
                SwingUtilities.invokeLater(() -> {
                    m_jStockCurrent.setText("0");
                    m_jStockMinimum.setText("0");
                    m_jStockCurrent.revalidate();
                    m_jStockCurrent.repaint();
                    m_jStockMinimum.revalidate();
                    m_jStockMinimum.repaint();
                    if (m_jStockGeneral != null) {
                        m_jStockGeneral.setText("0");
                        m_jStockGeneral.revalidate();
                        m_jStockGeneral.repaint();
                    }
                });
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al cargar stock", e);
            try {
                if (m_jStockCurrent != null) {
                    SwingUtilities.invokeLater(() -> {
                        m_jStockCurrent.setText("0");
                        m_jStockCurrent.revalidate();
                        m_jStockCurrent.repaint();
                    });
                }
                if (m_jStockMinimum != null) {
                    SwingUtilities.invokeLater(() -> {
                        m_jStockMinimum.setText("0");
                        m_jStockMinimum.revalidate();
                        m_jStockMinimum.repaint();
                    });
                }
                if (m_jStockGeneral != null) {
                    SwingUtilities.invokeLater(() -> {
                        m_jStockGeneral.setText("0");
                        m_jStockGeneral.revalidate();
                        m_jStockGeneral.repaint();
                    });
                }
            } catch (Exception ex) {
                // Ignorar errores al establecer valores por defecto
            }
        }
    }

    private void m_jInCatalogActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_m_jInCatalogActionPerformed

        if (m_jInCatalog.isSelected()) {
            m_jCatalogOrder.setEnabled(true);
        } else {
            m_jCatalogOrder.setEnabled(false);
            m_jCatalogOrder.setText(null);
        }
    }// GEN-LAST:event_m_jInCatalogActionPerformed

    private void m_jGrossProfitActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_m_jGrossProfitActionPerformed
        // TODO add your handling code here:
    }// GEN-LAST:event_m_jGrossProfitActionPerformed

    private void jBtnSupplierActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jBtnSupplierActionPerformed
        JDialogNewSupplier dialog = JDialogNewSupplier.getDialog(this, appView);
        dialog.setVisible(true);

        if (dialog.getSelectedSupplier() != null) {
            try {
                m_SuppliersModel = new ComboBoxValModel(m_sentsuppliers.list());
                m_jSupplier.setModel(m_SuppliersModel);
            } catch (BasicException ex) {
                Logger.getLogger(ProductsEditor.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }// GEN-LAST:event_jBtnSupplierActionPerformed

    private void none(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_none

    }// GEN-LAST:event_none

    private void m_jNameFocusLost(java.awt.event.FocusEvent evt) {// GEN-FIRST:event_m_jNameFocusLost
        setDisplay(btn);
    }// GEN-LAST:event_m_jNameFocusLost

    private void m_jCodeFocusLost(java.awt.event.FocusEvent evt) {// GEN-FIRST:event_m_jCodeFocusLost
        if (m_jCode.getText().length() < 8) {
            m_jCodetype.setSelectedIndex(2);
        }
    }// GEN-LAST:event_m_jCodeFocusLost

    private void m_jRefFocusLost(java.awt.event.FocusEvent evt) {// GEN-FIRST:event_m_jRefFocusLost
        setCode();
    }// GEN-LAST:event_m_jRefFocusLost

    private void jLabel1MouseClicked(java.awt.event.MouseEvent evt) {// GEN-FIRST:event_jLabel1MouseClicked

        if (evt.getClickCount() == 2) {
            String uuidString = productId.toString();
            StringSelection stringSelection = new StringSelection(uuidString);
            Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
            clpbrd.setContents(stringSelection, null);

            JOptionPane.showMessageDialog(null,
                    AppLocal.getIntString("message.uuidcopy"));
        }
    }// GEN-LAST:event_jLabel1MouseClicked

    private void colourChooserActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_colourChooserActionPerformed
        // TODO add your handling code here:
    }// GEN-LAST:event_colourChooserActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton colourChooser;
    private javax.swing.JButton jBtnBold;
    private javax.swing.JButton jBtnBreak;
    private javax.swing.JButton jBtnColour;
    private javax.swing.JButton jBtnImage;
    private javax.swing.JButton jBtnItalic;
    private javax.swing.JButton jBtnLarge;
    private javax.swing.JButton jBtnReset;
    private javax.swing.JButton jBtnShowTrans;
    private javax.swing.JButton jBtnSmall;
    private javax.swing.JButton jBtnStyle;
    private javax.swing.JButton jBtnSupplier;
    private javax.swing.JButton jBtnXml;
    private javax.swing.JButton jButtonHTML;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel28;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel33;
    private javax.swing.JLabel jLabel34;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JLabel jLabelAccumPoints;
    private javax.swing.JLabel jLblDate;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JTabbedPane jTabbedPane1;

    private javax.swing.JTable jTableProductStock;
    private javax.swing.JLabel jLabelStockCurrent;
    private javax.swing.JTextField m_jStockCurrent;
    private javax.swing.JLabel jLabelStockMinimum;
    private javax.swing.JTextField m_jStockMinimum;
    private javax.swing.JComboBox m_jAtt;
    private javax.swing.JTextField m_jCatalogOrder;
    private javax.swing.JComboBox m_jCategory;
    private javax.swing.JCheckBox m_jCheckWarrantyReceipt;
    private javax.swing.JTextField m_jCode;
    private javax.swing.JComboBox m_jCodetype;
    private javax.swing.JCheckBox m_jComment;
    private javax.swing.JCheckBox m_jConstant;
    private javax.swing.JTextArea m_jDisplay;
    private javax.swing.JTextField m_jGrossProfit;
    private com.openbravo.data.gui.JImageEditor m_jImage;
    private javax.swing.JCheckBox m_jInCatalog;
    private javax.swing.JTextField m_jName;
    private javax.swing.JTextField m_jPriceBuy;
    private javax.swing.JTextField m_jPriceSell;
    private javax.swing.JTextField m_jPriceSellTax;
    private javax.swing.JCheckBox m_jPrintKB;
    private javax.swing.JComboBox m_jPrintTo;
    private javax.swing.JTextField m_jRef;
    private javax.swing.JCheckBox m_jScale;
    private javax.swing.JCheckBox m_jSendStatus;
    private javax.swing.JCheckBox m_jService;
    private javax.swing.JTextField m_jStockUnits;
    private javax.swing.JComboBox m_jSupplier;
    private javax.swing.JComboBox m_jTax;
    private javax.swing.JTextField m_jTextTip;
    private javax.swing.JLabel m_jTitle;
    private javax.swing.JComboBox m_jUom;
    private javax.swing.JCheckBox m_jVerpatrib;
    private javax.swing.JCheckBox m_jVprice;
    private javax.swing.JCheckBox m_jAccumulatesPoints;
    private javax.swing.JLabel jLabelLote;
    private javax.swing.JTextField m_jLote;
    private javax.swing.JLabel jLabelModelo;
    private javax.swing.JTextField m_jModelo;
    private javax.swing.JLabel jLabelColor;
    private javax.swing.JTextField m_jColor;
    private javax.swing.JLabel jLabelVoltaje;
    private javax.swing.JTextField m_jVoltaje;
    private javax.swing.JLabel jLabelNoSerie;
    private javax.swing.JTextField m_jNoSerie;
    private javax.swing.JButton m_jbtndate;
    private javax.swing.JTextField m_jdate;
    private javax.swing.JTextField m_jmargin;
    private javax.swing.JTextField m_jstockcost;
    private javax.swing.JTextField m_jstockvolume;
    private javax.swing.JTextField m_jStockGeneral;
    private javax.swing.JLabel jLabelStockGeneral;
    private javax.swing.JPanel pricePanel;
    private javax.swing.JTextArea txtAttributes;
    private javax.swing.JLabel webLabel1;
    private javax.swing.JLabel jLabelProductTitle; // Título del formulario de producto
    private javax.swing.JRadioButton rbSellVehicle; // Radio button "Vehículo"
    private javax.swing.JRadioButton rbSellPieza; // Radio button "Pieza"
    private javax.swing.JRadioButton rbSellPackage; // Oculto
    private javax.swing.JPanel mainFieldsPanel; // Panel principal de campos
    private javax.swing.JPanel mainCombinedPanel; // Panel combinado con scroll
    private javax.swing.JCheckBox chkUseInventory; // Checkbox "Este producto SI utiliza inventario"
    // End of variables declaration//GEN-END:variables

    private static class ScrollablePanel extends javax.swing.JPanel implements javax.swing.Scrollable {
        public ScrollablePanel(java.awt.LayoutManager layout) {
            super(layout);
        }

        @Override
        public java.awt.Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        @Override
        public int getScrollableUnitIncrement(java.awt.Rectangle visibleRect, int orientation, int direction) {
            return 16;
        }

        @Override
        public int getScrollableBlockIncrement(java.awt.Rectangle visibleRect, int orientation, int direction) {
            return 32;
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            return true;
        }

        @Override
        public boolean getScrollableTracksViewportHeight() {
            return false;
        }
    }
}

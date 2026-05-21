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

        // lpr = new ListProviderCreator(m_dlSales.getProductCatQBF(), jproductfilter);
        lpr = new ListProviderCreator(m_dlSales.getProductCatQBF());

        // Usar SaveProvider estándar
        spr = new DefaultSaveProvider(
                m_dlSales.getProductCatUpdate(),
                m_dlSales.getProductCatInsert(),
                m_dlSales.getProductCatDelete());

        jeditor = new ProductsEditor(app, dirty);
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

        // Panel para contener solo el botón ScanPal (los otros botones van en un menú)
        javax.swing.JPanel panel = new javax.swing.JPanel();
        panel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 0));

        // Botón ScanPal
        JButton btnScanPal = new JButton();
        btnScanPal.setText("ScanPal");
        btnScanPal.setVisible(app.getDeviceScanner() != null);
        btnScanPal.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnScanPalActionPerformed(evt);
            }
        });
        if (app.getDeviceScanner() != null) {
            panel.add(btnScanPal);
        }

        // Botón Exportar Excel
        JButton btnExportExcel = new JButton("Exportar Excel");
        btnExportExcel.setToolTipText("Exportar todos los productos a un archivo Excel (CSV)");
        btnExportExcel.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnExportExcelActionPerformed(evt);
            }
        });

        // Botón Importar Excel
        JButton btnImportExcel = new JButton("Importar Excel");
        btnImportExcel.setToolTipText("Importar productos desde un archivo Excel (.xlsx o .xls)");
        btnImportExcel.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnImportExcelActionPerformed(evt);
            }
        });

        // Agregar los botones al panel
        panel.add(btnExportExcel);
        panel.add(btnImportExcel);

        // Botón Stock Pendiente
        JButton btnStockPending = new JButton("Stock Pendiente");
        btnStockPending.setToolTipText("Seguimiento de productos por recibir de proveedores");
        btnStockPending.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnStockPendingActionPerformed(evt);
            }
        });
        panel.add(btnStockPending);

        return panel;
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
}
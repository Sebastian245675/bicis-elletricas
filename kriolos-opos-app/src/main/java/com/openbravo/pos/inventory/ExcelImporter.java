package com.openbravo.pos.inventory;

import com.csvreader.CsvReader;
import com.openbravo.basic.BasicException;
import com.openbravo.data.loader.Session;
import com.openbravo.pos.forms.AppView;
import com.openbravo.pos.forms.DataLogicSales;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility to import products from CSV files (compatible with Excel).
 * Uses javacsv library which is already present in the project.
 */
public class ExcelImporter {

    private static final Logger LOGGER = Logger.getLogger(ExcelImporter.class.getName());
    private final AppView app;
    private final DataLogicSales m_dlSales;
    private Map<String, String> categoriesMap;
    private Map<String, String> taxesMap;

    public ExcelImporter(AppView app) {
        this.app = app;
        this.m_dlSales = (DataLogicSales) app.getBean("com.openbravo.pos.forms.DataLogicSales");
    }

    public int importExcel(File file) throws IOException, BasicException {
        CsvReader reader = null;
        try {
            // Usamos punto y coma como delimitador común en Excel (región ES) o coma
            // Intentamos detectar o usar el estándar de la librería
            reader = new CsvReader(file.getAbsolutePath(), ';', Charset.forName("UTF-8"));
            
            // Si la primera lectura no tiene múltiples columnas, probamos con coma
            reader.readHeaders();
            if (reader.getHeaderCount() <= 1) {
                reader.close();
                reader = new CsvReader(file.getAbsolutePath(), ',', Charset.forName("UTF-8"));
                reader.readHeaders();
            }

            loadMappings();

            int importedCount = 0;
            while (reader.readRecord()) {
                if (importRow(reader)) {
                    importedCount++;
                }
            }
            
            return importedCount;
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
    }

    private void loadMappings() throws BasicException {
        categoriesMap = new HashMap<>();
        taxesMap = new HashMap<>();

        try {
            Session session = m_dlSales.getSession();
            
            // Categorías
            try (PreparedStatement stmt = session.getConnection().prepareStatement("SELECT ID, NAME FROM categories")) {
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    categoriesMap.put(rs.getString("NAME").toLowerCase(), rs.getString("ID"));
                }
            }

            // Impuestos
            try (PreparedStatement stmt = session.getConnection().prepareStatement("SELECT ID, NAME FROM taxes")) {
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    taxesMap.put(rs.getString("NAME").toLowerCase(), rs.getString("ID"));
                }
            }
        } catch (SQLException e) {
            throw new BasicException("Error cargando mapeos de base de datos", e);
        }
    }

    private boolean importRow(CsvReader reader) {
        try {
            String reference = getCellValue(reader, "referencia");
            if (reference == null) reference = getCellValue(reader, "reference");
            
            String code = getCellValue(reader, "codigo");
            if (code == null) code = getCellValue(reader, "barcode");
            if (code == null) code = getCellValue(reader, "code");
            
            String name = getCellValue(reader, "nombre");
            if (name == null) name = getCellValue(reader, "name");
            
            if (name == null || name.isEmpty()) return false;

            double priceBuy = getNumericCellValue(reader, "precio compra");
            if (priceBuy == 0) priceBuy = getNumericCellValue(reader, "pricebuy");
            
            double priceSell = getNumericCellValue(reader, "precio venta");
            if (priceSell == 0) priceSell = getNumericCellValue(reader, "pricesell");
            
            String categoryName = getCellValue(reader, "categoria");
            if (categoryName == null) categoryName = getCellValue(reader, "category");
            
            String categoryId = categoryName != null ? categoriesMap.get(categoryName.toLowerCase()) : null;
            if (categoryId == null && !categoriesMap.isEmpty()) {
                categoryId = categoriesMap.values().iterator().next(); // Default to first category
            }

            String taxName = getCellValue(reader, "impuesto");
            if (taxName == null) taxName = getCellValue(reader, "tax");
            
            String taxId = taxName != null ? taxesMap.get(taxName.toLowerCase()) : null;
            if (taxId == null && !taxesMap.isEmpty()) {
                taxId = taxesMap.values().iterator().next(); // Default to first tax
            }

            String id = getCellValue(reader, "id");
            if (id == null || id.isEmpty()) {
                // Buscar por referencia o código si no hay ID
                id = findProductId(reference, code);
                if (id == null) {
                    id = UUID.randomUUID().toString();
                }
            }

            saveProduct(id, reference, code, name, priceBuy, priceSell, categoryId, taxId);
            return true;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error importando fila", e);
            return false;
        }
    }

    private String getCellValue(CsvReader reader, String colName) throws IOException {
        try {
            String val = reader.get(colName);
            return (val == null || val.trim().isEmpty()) ? null : val.trim();
        } catch (Exception e) {
            return null;
        }
    }

    private double getNumericCellValue(CsvReader reader, String colName) {
        try {
            String val = getCellValue(reader, colName);
            if (val == null) return 0.0;
            return Double.parseDouble(val.replace(",", "."));
        } catch (Exception e) {
            return 0.0;
        }
    }

    private String findProductId(String reference, String code) throws SQLException {
        Session session = m_dlSales.getSession();
        String sql = "SELECT ID FROM products WHERE (REFERENCE IS NOT NULL AND REFERENCE = ?) OR (CODE IS NOT NULL AND CODE = ?)";
        try (PreparedStatement stmt = session.getConnection().prepareStatement(sql)) {
            stmt.setString(1, reference);
            stmt.setString(2, code);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("ID");
            }
        }
        return null;
    }

    private void saveProduct(String id, String reference, String code, String name, double priceBuy, double priceSell, String categoryId, String taxId) throws SQLException {
        Session session = m_dlSales.getSession();
        
        // Verificar si existe
        boolean exists = false;
        try (PreparedStatement stmt = session.getConnection().prepareStatement("SELECT ID FROM products WHERE ID = ?")) {
            stmt.setString(1, id);
            ResultSet rs = stmt.executeQuery();
            exists = rs.next();
        }

        if (exists) {
            String sql = "UPDATE products SET REFERENCE = ?, CODE = ?, NAME = ?, PRICEBUY = ?, PRICESELL = ?, CATEGORY = ?, TAXCAT = ? WHERE ID = ?";
            try (PreparedStatement stmt = session.getConnection().prepareStatement(sql)) {
                stmt.setString(1, reference != null ? reference : id);
                stmt.setString(2, code);
                stmt.setString(3, name);
                stmt.setDouble(4, priceBuy);
                stmt.setDouble(5, priceSell);
                stmt.setString(6, categoryId);
                stmt.setString(7, taxId);
                stmt.setString(8, id);
                stmt.executeUpdate();
            }
        } else {
            String sql = "INSERT INTO products (ID, REFERENCE, CODE, NAME, PRICEBUY, PRICESELL, CATEGORY, TAXCAT, CODETYPE, ISCOM, PRINTKB, SENDSTATUS) VALUES (?, ?, ?, ?, ?, ?, ?, ?, 'CODE128', 0, 0, 0)";
            try (PreparedStatement stmt = session.getConnection().prepareStatement(sql)) {
                stmt.setString(1, id);
                stmt.setString(2, reference != null ? reference : id);
                stmt.setString(3, code);
                stmt.setString(4, name);
                stmt.setDouble(5, priceBuy);
                stmt.setDouble(6, priceSell);
                stmt.setString(7, categoryId);
                stmt.setString(8, taxId);
                stmt.executeUpdate();
            }
        }
    }
}

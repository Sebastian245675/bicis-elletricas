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

package com.openbravo.pos.suppliers;

import com.openbravo.basic.BasicException;
import com.openbravo.data.gui.ListCellRendererBasic;
import com.openbravo.data.loader.ComparatorCreator;
import com.openbravo.data.loader.TableDefinition;
import com.openbravo.data.loader.Vectorer;
import com.openbravo.data.user.EditorRecord;
import com.openbravo.data.user.ListProvider;
import com.openbravo.data.user.ListProviderCreator;
import com.openbravo.data.user.DefaultSaveProvider;
import com.openbravo.data.user.SaveProvider;
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.pos.panels.JPanelTable;
import com.openbravo.pos.sync.VoltiumSyncService;
import javax.swing.ListCellRenderer;

/**
 *
 * @author Jack Gerrard
 */
public class SuppliersPanel extends JPanelTable {

    private static final long serialVersionUID = 1L;
    
    private TableDefinition tsuppliers;
    private SuppliersView jeditor;
    
    /** Creates a new instance of SuppliersPanel */
    public SuppliersPanel() {}
    
    /**
     *
     */
    @Override
    protected void init() {        
        DataLogicSuppliers dlSuppliers  = (DataLogicSuppliers) app.getBean("com.openbravo.pos.suppliers.DataLogicSuppliers");
        tsuppliers = dlSuppliers.getTableSuppliers();        
        jeditor = new SuppliersView(app, dirty);    
        
    }
    
    /**
     *
     * @throws BasicException
     */
    @Override
    public void activate() throws BasicException { 
        jeditor.activate();         
        super.activate();
        try {
            VoltiumSyncService.sincronizarProveedoresAsync();
        } catch (Exception ignored) {}
    }
    
    /**
     *
     * @return
     */
    @Override
    public ListProvider getListProvider() {
        return new ListProviderCreator(tsuppliers);
    }
    
    /**
     *
     * @return
     */
    @Override
    public SaveProvider getSaveProvider() {
        final SaveProvider orig = new DefaultSaveProvider(tsuppliers, new int[] {
            0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 
            11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21});      
        return new SaveProvider() {
            @Override
            public boolean canDelete() {
                return orig.canDelete();
            }

            @Override
            public boolean canInsert() {
                return orig.canInsert();
            }

            @Override
            public boolean canUpdate() {
                return orig.canUpdate();
            }

            @Override
            public int insertData(Object value) throws BasicException {
                int r = orig.insertData(value);
                try {
                    VoltiumSyncService.sincronizarProveedoresAsync();
                } catch (Exception ignored) {}
                return r;
            }

            @Override
            public int updateData(Object value) throws BasicException {
                int r = orig.updateData(value);
                try {
                    VoltiumSyncService.sincronizarProveedoresAsync();
                } catch (Exception ignored) {}
                return r;
            }

            @Override
            public int deleteData(Object value) throws BasicException {
                int r = orig.deleteData(value);
                try {
                    VoltiumSyncService.sincronizarProveedoresAsync();
                } catch (Exception ignored) {}
                return r;
            }
        };
    }
    
    /**
     *
     * @return
     */
    @Override
    public Vectorer getVectorer() {
        return tsuppliers.getVectorerBasic(new int[]{1, 2, 3, 4});
    }
    
    /**
     *
     * @return
     */
    @Override
    public ComparatorCreator getComparatorCreator() {
        return tsuppliers.getComparatorCreator(new int[] {1, 2, 3, 4});
    }
    
    /**
     *
     * @return
     */
    @Override
    public ListCellRenderer getListCellRenderer() {
        return new ListCellRendererBasic(tsuppliers.getRenderStringBasic(new int[]{3}));
    }
    
    /**
     *
     * @return
     */
    @Override
    public EditorRecord getEditor() {
        return jeditor;
    }

    @Override
    public java.awt.Component getToolbarExtras() {
        javax.swing.JButton btnExport = new javax.swing.JButton("Exportar Excel");
        btnExport.setIcon(new com.openbravo.pos.util.ModernActionIcon(
                com.openbravo.pos.util.ModernActionIcon.Type.EXPORT, 18, java.awt.Color.WHITE));
        btnExport.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12));
        btnExport.setBackground(new java.awt.Color(16, 185, 129)); // Excel green
        btnExport.setForeground(java.awt.Color.WHITE);
        btnExport.setOpaque(true);
        btnExport.setBorderPainted(false);
        btnExport.setFocusPainted(false);
        btnExport.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnExport.setBorder(javax.swing.BorderFactory.createEmptyBorder(6, 14, 6, 14));
        
        btnExport.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportToExcel();
            }
        });
        
        return btnExport;
    }

    private void exportToExcel() {
        if (bd == null || bd.getListModel() == null || bd.getListModel().getSize() == 0) {
            javax.swing.JOptionPane.showMessageDialog(this, "No hay datos para exportar.", "Exportar Excel", javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }

        javax.swing.JFileChooser fileChooser = new javax.swing.JFileChooser();
        fileChooser.setDialogTitle("Guardar como Excel");
        fileChooser.setSelectedFile(new java.io.File("proveedores.xls"));
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Archivos de Excel (*.xls)", "xls"));

        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection == javax.swing.JFileChooser.APPROVE_OPTION) {
            java.io.File fileToSave = fileChooser.getSelectedFile();
            String path = fileToSave.getAbsolutePath();
            if (!path.toLowerCase().endsWith(".xls")) {
                path += ".xls";
                fileToSave = new java.io.File(path);
            }

            try (org.apache.poi.hssf.usermodel.HSSFWorkbook workbook = new org.apache.poi.hssf.usermodel.HSSFWorkbook()) {
                org.apache.poi.hssf.usermodel.HSSFSheet sheet = workbook.createSheet("Proveedores");
                org.apache.poi.hssf.usermodel.HSSFCellStyle headerStyle = workbook.createCellStyle();
                org.apache.poi.hssf.usermodel.HSSFFont font = workbook.createFont();
                font.setBold(true);
                headerStyle.setFont(font);

                String[] headers = {
                    "Clave", "Tax ID", "Nombre", "Límite Crédito", "Dirección", 
                    "Código Postal", "Ciudad", "Provincia/Región", "País", 
                    "Nombre Contacto", "Apellido Contacto", "Email", "Teléfono 1", 
                    "Teléfono 2", "Fax", "Notas", "Deuda Actual", "IVA ID"
                };

                org.apache.poi.hssf.usermodel.HSSFRow headerRow = sheet.createRow(0);
                for (int i = 0; i < headers.length; i++) {
                    org.apache.poi.hssf.usermodel.HSSFCell cell = headerRow.createCell(i);
                    cell.setCellValue(headers[i]);
                    cell.setCellStyle(headerStyle);
                }

                // Fill data
                javax.swing.ListModel listModel = bd.getListModel();
                int size = listModel.getSize();
                for (int i = 0; i < size; i++) {
                    Object element = listModel.getElementAt(i);
                    if (element instanceof Object[]) {
                        Object[] rec = (Object[]) element;
                        org.apache.poi.hssf.usermodel.HSSFRow row = sheet.createRow(i + 1);

                        // Helper mapping for the record fields based on TableDefinition indices:
                        // 1: SEARCHKEY, 2: TAXID, 3: NAME, 4: MAXDEBT, 5: ADDRESS
                        // 7: POSTAL, 8: CITY, 9: REGION, 10: COUNTRY
                        // 11: FIRSTNAME, 12: LASTNAME, 13: EMAIL, 14: PHONE
                        // 15: PHONE2, 16: FAX, 17: NOTES, 20: CURDEBT, 21: VATID
                        
                        setCellValueSafely(row.createCell(0), rec.length > 1 ? rec[1] : null);
                        setCellValueSafely(row.createCell(1), rec.length > 2 ? rec[2] : null);
                        setCellValueSafely(row.createCell(2), rec.length > 3 ? rec[3] : null);
                        setCellValueSafely(row.createCell(3), rec.length > 4 ? rec[4] : null);
                        setCellValueSafely(row.createCell(4), rec.length > 5 ? rec[5] : null);
                        setCellValueSafely(row.createCell(5), rec.length > 7 ? rec[7] : null);
                        setCellValueSafely(row.createCell(6), rec.length > 8 ? rec[8] : null);
                        setCellValueSafely(row.createCell(7), rec.length > 9 ? rec[9] : null);
                        setCellValueSafely(row.createCell(8), rec.length > 10 ? rec[10] : null);
                        setCellValueSafely(row.createCell(9), rec.length > 11 ? rec[11] : null);
                        setCellValueSafely(row.createCell(10), rec.length > 12 ? rec[12] : null);
                        setCellValueSafely(row.createCell(11), rec.length > 13 ? rec[13] : null);
                        setCellValueSafely(row.createCell(12), rec.length > 14 ? rec[14] : null);
                        setCellValueSafely(row.createCell(13), rec.length > 15 ? rec[15] : null);
                        setCellValueSafely(row.createCell(14), rec.length > 16 ? rec[16] : null);
                        setCellValueSafely(row.createCell(15), rec.length > 17 ? rec[17] : null);
                        setCellValueSafely(row.createCell(16), rec.length > 20 ? rec[20] : null);
                        setCellValueSafely(row.createCell(17), rec.length > 21 ? rec[21] : null);
                    }
                }

                // Auto size columns
                for (int i = 0; i < headers.length; i++) {
                    sheet.autoSizeColumn(i);
                }

                try (java.io.FileOutputStream fileOut = new java.io.FileOutputStream(fileToSave)) {
                    workbook.write(fileOut);
                }

                javax.swing.JOptionPane.showMessageDialog(this, "Proveedores exportados con éxito.", "Exportar Excel", javax.swing.JOptionPane.INFORMATION_MESSAGE);

            } catch (Exception e) {
                java.util.logging.Logger.getLogger(SuppliersPanel.class.getName()).log(java.util.logging.Level.SEVERE, "Error exporting suppliers to Excel", e);
                javax.swing.JOptionPane.showMessageDialog(this, "Error al exportar proveedores: " + e.getMessage(), "Exportar Excel", javax.swing.JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void setCellValueSafely(org.apache.poi.hssf.usermodel.HSSFCell cell, Object val) {
        if (val == null) {
            cell.setCellValue("");
        } else if (val instanceof Number) {
            cell.setCellValue(((Number) val).doubleValue());
        } else if (val instanceof Boolean) {
            cell.setCellValue((Boolean) val ? "Sí" : "No");
        } else {
            cell.setCellValue(val.toString());
        }
    }

    /**
     *
     * @return
     */
    @Override
    public String getTitle() {
        return AppLocal.getIntString("Menu.SuppliersManagement");
    }    
}

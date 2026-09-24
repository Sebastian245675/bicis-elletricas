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
package com.openbravo.pos.panels;

import com.openbravo.pos.forms.AppView;
import com.openbravo.pos.forms.BeanFactoryApp;
import com.openbravo.pos.forms.BeanFactoryException;
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.basic.BasicException;
import com.openbravo.data.gui.*;
import com.openbravo.data.loader.ComparatorCreator;
import com.openbravo.data.loader.Vectorer;
import com.openbravo.data.user.*;
import com.openbravo.pos.forms.*;
import com.openbravo.pos.util.ModernActionIcon;
import java.awt.BorderLayout;
import java.awt.Component;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import com.openbravo.data.loader.TableDefinition;
import java.sql.SQLException;
import java.util.logging.Level;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JSplitPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.DefaultTableModel;
import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 *
 * @author adrianromero
 */
public abstract class JPanelTable extends JPanel implements JPanelView, BeanFactoryApp {

    private static final long serialVersionUID = 1L;
    private final static Logger LOGEER = Logger.getLogger(JPanelTable.class.getName());

    protected BrowsableEditableData bd;
    protected DirtyManager dirty;
    protected AppView app;

    public JPanelTable() {
        initComponents();
    }

    @Override
    public void init(AppView app) throws BeanFactoryException {

        LOGEER.info("Init JPanelTable");
        this.app = app;
        dirty = new DirtyManager();
        bd = null;
        init();
    }

    @Override
    public Object getBean() {
        return this;
    }

    private void startNavigation() {

        if (bd == null) {
            // init browsable editable data
            bd = new BrowsableEditableData(getListProvider(), wrapSaveProvider(getSaveProvider()), getEditor(), dirty);
        }

        toolbar.removeAll();

        // Add the filter panel
        Component c = getFilter();
        if (c != null) {
            c.applyComponentOrientation(getComponentOrientation());
            add(c, BorderLayout.NORTH);
        }

        // Add the editor
        c = getEditor().getComponent();
        if (c != null) {
            c.applyComponentOrientation(getComponentOrientation());
            container.add(c, BorderLayout.CENTER);
        }

        // el panel este
        ListCellRenderer cr = getListCellRenderer();
        if (cr != null) {
            final JListNavigator nl = new JListNavigator(bd);
            nl.applyComponentOrientation(getComponentOrientation());
            nl.setCellRenderer(cr);
            
            // Style list navigator children
            modernizeListNavigator(nl);
            
            // Create a wrapper panel for the collapsible sidebar with a sleek fixed width of 200px
            final javax.swing.JPanel sidebarPanel = new javax.swing.JPanel(new java.awt.BorderLayout());
            sidebarPanel.setOpaque(false);
            sidebarPanel.setPreferredSize(new java.awt.Dimension(200, 0));
            sidebarPanel.add(nl, java.awt.BorderLayout.CENTER);
            
            // Collapse/Expand Button Panel
            javax.swing.JPanel togglePanel = new javax.swing.JPanel(new java.awt.BorderLayout());
            togglePanel.setBackground(new java.awt.Color(248, 250, 252)); // Slate 50 background
            togglePanel.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 0, 1, new java.awt.Color(226, 232, 240)));
            togglePanel.setPreferredSize(new java.awt.Dimension(18, 0));
            
            final javax.swing.JButton btnToggle = new javax.swing.JButton("<");
            btnToggle.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 11));
            btnToggle.setForeground(new java.awt.Color(100, 116, 139)); // Slate 500
            btnToggle.setBorder(null);
            btnToggle.setContentAreaFilled(false);
            btnToggle.setFocusPainted(false);
            btnToggle.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
            
            btnToggle.addActionListener(new java.awt.event.ActionListener() {
                private boolean collapsed = false;
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    collapsed = !collapsed;
                    nl.setVisible(!collapsed);
                    btnToggle.setText(collapsed ? ">" : "<");
                    sidebarPanel.setPreferredSize(new java.awt.Dimension(collapsed ? 18 : 200, 0));
                    container.revalidate();
                    container.repaint();
                }
            });
            
            togglePanel.add(btnToggle, java.awt.BorderLayout.CENTER);
            
            sidebarPanel.add(togglePanel, java.awt.BorderLayout.EAST);
            
            container.add(sidebarPanel, java.awt.BorderLayout.LINE_START);
        }

        // Split toolbar layout: leftToolbar (extras, navigation, new) and rightToolbar
        // (save, delete, history)
        toolbar.setLayout(new java.awt.BorderLayout());
        toolbar.setBackground(java.awt.Color.WHITE);

        javax.swing.JPanel leftToolbar = new javax.swing.JPanel(
                new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 0));
        leftToolbar.setBackground(java.awt.Color.WHITE);
        leftToolbar.setOpaque(true);

        javax.swing.JPanel rightToolbar = new javax.swing.JPanel(
                new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 5, 0));
        rightToolbar.setBackground(java.awt.Color.WHITE);
        rightToolbar.setOpaque(true);

        toolbar.add(leftToolbar, java.awt.BorderLayout.WEST);
        toolbar.add(rightToolbar, java.awt.BorderLayout.EAST);

        toolbar.setBorder(null);
        leftToolbar.setBorder(null);
        rightToolbar.setBorder(null);

        // add toolbar extras
        c = getToolbarExtras();
        if (c != null) {
            c.applyComponentOrientation(getComponentOrientation());
            leftToolbar.add(c);
        }

        // La Toolbar components
        c = new JLabelDirty(dirty);
        c.applyComponentOrientation(getComponentOrientation());
        leftToolbar.add(c);

        c = new JCounter(bd);
        c.applyComponentOrientation(getComponentOrientation());
        leftToolbar.add(c);

        c = new JNavigator(bd, getVectorer(), getComparatorCreator());
        c.applyComponentOrientation(getComponentOrientation());
        leftToolbar.add(c);

        // Extract JSaver buttons
        JSaver saver = new JSaver(bd);
        JButton btnNew = saver.getBtnNew();
        JButton btnDelete = saver.getBtnDelete();
        JButton btnSave = saver.getBtnSave();
        saver.removeAll(); // Clear references

        if (btnNew != null) {
            btnNew.applyComponentOrientation(getComponentOrientation());
            leftToolbar.add(btnNew);
        }

        if (btnDelete != null) {
            btnDelete.applyComponentOrientation(getComponentOrientation());
            rightToolbar.add(btnDelete);
        }

        if (btnSave != null) {
            btnSave.applyComponentOrientation(getComponentOrientation());
            rightToolbar.add(btnSave);
        }

        // Sebastian - Agregar botón Historial de Auditoría (solo si no es
        // ProductsPanel)
        if (!this.getClass().getName().contains("ProductsPanel")) {
            JButton btnHistory = new JButton("Historial");
            btnHistory.setIcon(new ModernActionIcon(
                    ModernActionIcon.Type.HISTORY, 18, java.awt.Color.WHITE));
            btnHistory.applyComponentOrientation(getComponentOrientation());
            btnHistory.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    showAuditHistoryDialog();
                }
            });
            rightToolbar.add(btnHistory);
        }
    }

    private void modernizeListNavigator(javax.swing.JComponent nl) {
        nl.setBackground(java.awt.Color.WHITE);
        nl.setOpaque(true);
        // Style children
        for (int i = 0; i < nl.getComponentCount(); i++) {
            java.awt.Component child = nl.getComponent(i);
            if (child instanceof javax.swing.JScrollPane) {
                javax.swing.JScrollPane scroll = (javax.swing.JScrollPane) child;
                scroll.setBorder(null);
                scroll.setOpaque(false);
                scroll.getViewport().setOpaque(false);
                java.awt.Component view = scroll.getViewport().getView();
                if (view instanceof javax.swing.JList) {
                    javax.swing.JList list = (javax.swing.JList) view;
                    list.setBackground(java.awt.Color.WHITE);
                    list.setSelectionBackground(new java.awt.Color(241, 245, 249)); // slate-100 selection
                    list.setSelectionForeground(new java.awt.Color(15, 23, 42)); // slate-900 text
                    list.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 13));
                    list.setFixedCellHeight(32);
                }
            }
        }
    }

    public Component getToolbarExtras() {
        return null;
    }

    public Component getFilter() {
        return null;
    }

    protected abstract void init();

    public abstract EditorRecord getEditor();

    public abstract ListProvider getListProvider();

    public abstract SaveProvider getSaveProvider();

    public Vectorer getVectorer() {
        return null;
    }

    public ComparatorCreator getComparatorCreator() {
        return null;
    }

    public ListCellRenderer getListCellRenderer() {
        return null;
    }

    @Override
    public JComponent getComponent() {
        return this;
    }

    @Override
    public void activate() throws BasicException {
        LOGEER.info("Call active");
        startNavigation();
        bd.actionLoad();
    }

    @Override
    public boolean deactivate() {

        try {
            return bd.actionClosingForm(this);
        } catch (BasicException eD) {
            MessageInf msg = new MessageInf(MessageInf.SGN_NOTICE, AppLocal.getIntString("message.CannotMove"), eD);
            msg.show(this);
            return false;
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

        container = new javax.swing.JPanel();
        toolbar = new javax.swing.JPanel();

        setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 5, 5, 5));
        setLayout(new java.awt.BorderLayout());

        container.setBackground(java.awt.Color.WHITE);
        container.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        container.setLayout(new java.awt.BorderLayout());

        toolbar.setBackground(java.awt.Color.WHITE);
        toolbar.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));
        container.add(toolbar, java.awt.BorderLayout.NORTH);

        add(container, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    protected javax.swing.JPanel container;
    protected javax.swing.JPanel toolbar;
    // End of variables declaration//GEN-END:variables

    // --- INICIO MÉTODOS DE AUDITORÍA Y HISTORIAL ---
    private SaveProvider wrapSaveProvider(final SaveProvider originalProvider) {
        if (originalProvider == null) {
            return null;
        }
        return new SaveProvider() {
            @Override
            public boolean canDelete() {
                return originalProvider.canDelete();
            }

            @Override
            public boolean canInsert() {
                return originalProvider.canInsert();
            }

            @Override
            public boolean canUpdate() {
                return originalProvider.canUpdate();
            }

            @Override
            public int deleteData(Object value) throws BasicException {
                String tableName = getTableNameForPanel();
                String entityId = getEntityId(value);
                String entityName = "";
                String entityType = getTitle();
                try {
                    if (tableName != null && !tableName.isEmpty() && !entityId.isEmpty()) {
                        java.util.Map<String, Object> oldRec = fetchRecordAsMap(tableName, entityId);
                        entityName = getEntityNameFromMap(oldRec);
                    }
                } catch (Exception e) {
                }

                int result = originalProvider.deleteData(value);
                if (result > 0) {
                    saveAuditLog("Eliminar", entityType, entityId, entityName, "Registro eliminado");
                }
                return result;
            }

            @Override
            public int insertData(Object value) throws BasicException {
                int result = originalProvider.insertData(value);
                if (result > 0) {
                    try {
                        String tableName = getTableNameForPanel();
                        String entityId = getEntityId(value);
                        if (tableName != null && !tableName.isEmpty() && !entityId.isEmpty()) {
                            java.util.Map<String, Object> newRec = fetchRecordAsMap(tableName, entityId);
                            String entityName = getEntityNameFromMap(newRec);
                            String entityType = getTitle();
                            saveAuditLog("Crear", entityType, entityId, entityName, "Registro creado");
                        }
                    } catch (Exception e) {
                        LOGEER.log(Level.WARNING, "Error saving insert audit log", e);
                    }
                }
                return result;
            }

            @Override
            public int updateData(Object value) throws BasicException {
                String tableName = getTableNameForPanel();
                String entityId = getEntityId(value);
                java.util.Map<String, Object> oldRec = null;

                try {
                    if (tableName != null && !tableName.isEmpty() && !entityId.isEmpty()) {
                        oldRec = fetchRecordAsMap(tableName, entityId);
                    }
                } catch (Exception e) {
                    LOGEER.log(Level.WARNING, "Error preparing update audit log", e);
                }

                int result = originalProvider.updateData(value);
                if (result > 0) {
                    try {
                        if (tableName != null && !tableName.isEmpty() && !entityId.isEmpty()) {
                            java.util.Map<String, Object> newRec = fetchRecordAsMap(tableName, entityId);
                            String entityName = getEntityNameFromMap(newRec.isEmpty() ? oldRec : newRec);
                            String entityType = getTitle();
                            String details = compareMaps(oldRec, newRec);
                            if (details.isEmpty()) {
                                details = "Ningún campo relevante cambió";
                            }
                            saveAuditLog("Modificar", entityType, entityId, entityName, details);
                        }
                    } catch (Exception e) {
                        LOGEER.log(Level.WARNING, "Error saving update audit log", e);
                    }
                }
                return result;
            }
        };
    }

    private String getTableNameForPanel() {
        String className = this.getClass().getName();
        if (className.contains("ProductsPanel")) {
            return "PRODUCTS";
        } else if (className.contains("CustomersPanel")) {
            return "CUSTOMERS";
        } else if (className.contains("PeoplePanel")) {
            return "PEOPLE";
        } else if (className.contains("RolesPanel")) {
            return "ROLES";
        } else if (className.contains("ResourcesPanel")) {
            return "RESOURCES";
        } else if (className.contains("CategoriesPanel")) {
            return "CATEGORIES";
        } else if (className.contains("LocationsPanel")) {
            return "LOCATIONS";
        } else if (className.contains("TaxCategoriesPanel")) {
            return "TAXCATEGORIES";
        } else if (className.contains("TaxCustCategoriesPanel")) {
            return "TAXCUSTCATEGORIES";
        } else if (className.contains("TaxPanel")) {
            return "TAXES";
        } else if (className.contains("UomPanel")) {
            return "UOM";
        } else if (className.contains("SuppliersPanel")) {
            return "SUPPLIERS";
        } else if (className.contains("BreaksPanel")) {
            return "BREAKS";
        } else if (className.contains("LeavesPanel")) {
            return "LEAVES";
        } else if (className.contains("JPanelFloors")) {
            return "FLOORS";
        } else if (className.contains("JPanelPlaces")) {
            return "PLACES";
        } else if (className.contains("VoucherPanel")) {
            return "VOUCHERS";
        } else if (className.contains("AttributesPanel")) {
            return "ATTRIBUTES";
        } else if (className.contains("AttributeUsePanel")) {
            return "ATTRIBUTEUSE";
        } else if (className.contains("AttributeValuesPanel")) {
            return "ATTRIBUTEVALUES";
        } else if (className.contains("AttributeSetsPanel")) {
            return "ATTRIBUTESET";
        } else if (className.contains("AuxiliarPanel")) {
            return "PRODUCTS_COM";
        } else if (className.contains("BundlePanel")) {
            return "PRODUCTS_BUNDLE";
        } else if (className.contains("ProductsGranelPanel")) {
            return "PRODUCTS";
        } else if (className.contains("ProductsWarehousePanel")) {
            return "PRODUCTS";
        } else if (className.contains("StockDiaryPanel")) {
            return "STOCKDIARY";
        }

        try {
            TableDefinition td = findTableDefinition();
            if (td != null) {
                if (td.getTableName().equalsIgnoreCase("RESOURCES") && !className.contains("ResourcesPanel")) {
                    // Ignore mismatched resources table
                } else {
                    return td.getTableName();
                }
            }
        } catch (Exception e) {
        }

        String title = getTitle();
        if (title != null) {
            title = title.toUpperCase();
            if (title.contains("PRODUCT"))
                return "PRODUCTS";
            if (title.contains("CLIENT") || title.contains("CUSTOMER"))
                return "CUSTOMERS";
            if (title.contains("CATEGOR"))
                return "CATEGORIES";
            if (title.contains("IMPUEST") || title.contains("TAX"))
                return "TAXES";
            if (title.contains("PROVEEDOR") || title.contains("SUPPLIER"))
                return "SUPPLIERS";
            if (title.contains("USUARIO") || title.contains("USER") || title.contains("PEOPLE")
                    || title.contains("EMPLEADO"))
                return "PEOPLE";
            if (title.contains("ROL"))
                return "ROLES";
            if (title.contains("RECURS") || title.contains("RESOURCE"))
                return "RESOURCES";
            if (title.contains("ALMACEN") || title.contains("LOCATION"))
                return "LOCATIONS";
            if (title.contains("UNIDAD") || title.contains("UOM"))
                return "UOM";
        }
        return null;
    }

    private String getEntityId(Object value) {
        if (value == null)
            return "";
        if (value instanceof Object[]) {
            Object[] rec = (Object[]) value;
            if (rec.length > 0) {
                return String.valueOf(rec[0]);
            }
        } else {
            try {
                java.lang.reflect.Method mId = value.getClass().getMethod("getId");
                return String.valueOf(mId.invoke(value));
            } catch (Exception e) {
                try {
                    java.lang.reflect.Field fId = value.getClass().getDeclaredField("id");
                    fId.setAccessible(true);
                    return String.valueOf(fId.get(value));
                } catch (Exception e2) {
                }
            }
        }
        return "";
    }

    private String getEntityNameFromMap(java.util.Map<String, Object> map) {
        if (map == null)
            return "Sin Nombre";
        if (map.containsKey("NAME") && map.get("NAME") != null) {
            return String.valueOf(map.get("NAME"));
        }
        if (map.containsKey("name") && map.get("name") != null) {
            return String.valueOf(map.get("name"));
        }
        if (map.containsKey("SEARCHKEY") && map.get("SEARCHKEY") != null) {
            return String.valueOf(map.get("SEARCHKEY"));
        }
        if (map.containsKey("searchkey") && map.get("searchkey") != null) {
            return String.valueOf(map.get("searchkey"));
        }
        if (map.containsKey("KEY") && map.get("KEY") != null) {
            return String.valueOf(map.get("KEY"));
        }
        if (map.containsKey("key") && map.get("key") != null) {
            return String.valueOf(map.get("key"));
        }
        for (java.util.Map.Entry<String, Object> entry : map.entrySet()) {
            String k = entry.getKey().toUpperCase();
            if (!k.equals("ID") && !k.equals("IMAGE") && !k.equals("ICON") && entry.getValue() != null) {
                String val = String.valueOf(entry.getValue());
                if (val.length() > 0 && val.length() < 50) {
                    return val;
                }
            }
        }
        return "Sin Nombre";
    }

    private java.util.Map<String, Object> fetchRecordAsMap(String tableName, String entityId) {
        java.util.Map<String, Object> map = new java.util.LinkedHashMap<>();
        if (tableName == null || tableName.isEmpty() || entityId == null || entityId.isEmpty()) {
            return map;
        }

        java.sql.Connection conn = null;
        java.sql.PreparedStatement pstmt = null;
        java.sql.ResultSet rs = null;
        try {
            conn = app.getSession().getConnection();
            pstmt = conn.prepareStatement("SELECT * FROM " + tableName + " WHERE ID = ?");
            pstmt.setString(1, entityId);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                java.sql.ResultSetMetaData meta = rs.getMetaData();
                int cols = meta.getColumnCount();
                for (int i = 1; i <= cols; i++) {
                    String colName = meta.getColumnName(i);
                    Object colVal = rs.getObject(i);
                    map.put(colName, colVal);
                }
            }
        } catch (Exception e) {
            LOGEER.log(Level.WARNING, "Error fetching record as map for table " + tableName + " and ID " + entityId, e);
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
        return map;
    }

    private String compareMaps(java.util.Map<String, Object> oldMap, java.util.Map<String, Object> newMap) {
        if (oldMap == null || newMap == null) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        java.util.Set<String> allKeys = new java.util.TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        allKeys.addAll(oldMap.keySet());
        allKeys.addAll(newMap.keySet());

        for (String key : allKeys) {
            String keyUpper = key.toUpperCase();
            if (keyUpper.equals("ID") || keyUpper.equals("PASSWORD") || keyUpper.equals("IMAGE")
                    || keyUpper.equals("ICON") || keyUpper.equals("MEMODATE")) {
                continue;
            }

            Object oldVal = oldMap.get(key);
            Object newVal = newMap.get(key);

            boolean changed = false;
            if (oldVal == null && newVal != null) {
                if (newVal instanceof String && ((String) newVal).trim().isEmpty()) {
                    // Ignore empty string transitions
                } else {
                    changed = true;
                }
            } else if (oldVal != null && newVal == null) {
                if (oldVal instanceof String && ((String) oldVal).trim().isEmpty()) {
                    // Ignore empty string transitions
                } else {
                    changed = true;
                }
            } else if (oldVal != null && !oldVal.equals(newVal)) {
                if (oldVal instanceof Number && newVal instanceof Number) {
                    if (((Number) oldVal).doubleValue() == ((Number) newVal).doubleValue()) {
                        continue;
                    }
                }
                if (oldVal instanceof Boolean && newVal instanceof Number) {
                    boolean oldBool = (Boolean) oldVal;
                    int newInt = ((Number) newVal).intValue();
                    if ((oldBool && newInt == 1) || (!oldBool && newInt == 0)) {
                        continue;
                    }
                }
                if (oldVal instanceof Number && newVal instanceof Boolean) {
                    int oldInt = ((Number) oldVal).intValue();
                    boolean newBool = (Boolean) newVal;
                    if ((newBool && oldInt == 1) || (!newBool && oldInt == 0)) {
                        continue;
                    }
                }
                changed = true;
            }

            if (changed) {
                if (sb.length() > 0)
                    sb.append("\n");
                String displayName = getFieldDisplayName(key);
                String oldStr = oldVal == null ? "[Vacío]" : oldVal.toString();
                String newStr = newVal == null ? "[Vacío]" : newVal.toString();

                if (oldVal instanceof Boolean) {
                    oldStr = (Boolean) oldVal ? "Sí" : "No";
                }
                if (newVal instanceof Boolean) {
                    newStr = (Boolean) newVal ? "Sí" : "No";
                }

                if (oldStr.length() > 100)
                    oldStr = oldStr.substring(0, 97) + "...";
                if (newStr.length() > 100)
                    newStr = newStr.substring(0, 97) + "...";

                sb.append(displayName).append(" cambió de '").append(oldStr).append("' a '").append(newStr).append("'");
            }
        }
        return sb.toString();
    }

    private String getFieldDisplayName(String key) {
        String k = key.toUpperCase();
        switch (k) {
            case "REFERENCE":
                return "Referencia";
            case "CODE":
                return "Código de barras";
            case "CODETYPE":
                return "Tipo de código";
            case "NAME":
                return "Nombre";
            case "PRICEBUY":
                return "Precio de compra";
            case "PRICESELL":
                return "Precio de venta";
            case "CATEGORY":
                return "Categoría";
            case "TAXCAT":
                return "Categoría de impuesto";
            case "ATTRIBUTESET_ID":
                return "Grupo de atributos";
            case "STOCKCOST":
                return "Costo de stock";
            case "STOCKVOLUME":
                return "Volumen de stock";
            case "ISCOM":
                return "Venta a granel / comisión";
            case "ISSCALE":
                return "Usa balanza";
            case "ISCONSTANT":
                return "Precio constante";
            case "PRINTKB":
                return "Imprimir en cocina/teclado";
            case "SENDSTATUS":
                return "Enviar estado";
            case "ISSERVICE":
                return "Es servicio (sin inventario)";
            case "ATTRIBUTES":
                return "Atributos adicionales";
            case "DISPLAY":
                return "Texto de pantalla";
            case "ISVPRICE":
                return "Precio variable";
            case "ISVERPATRIB":
                return "Ver atributos";
            case "TEXTTIP":
                return "Texto de ayuda / nota";
            case "WARRANTY":
                return "Tiene garantía";
            case "STOCKUNITS":
                return "Unidades de stock";
            case "PRINTTO":
                return "Imprimir en";
            case "SUPPLIER":
                return "Proveedor";
            case "UOM":
                return "Unidad de medida";
            case "ACCUMULATES_POINTS":
                return "Acumula puntos";
            case "LOTE":
                return "Lote obligatorio";
            case "MODELO":
                return "Modelo";
            case "COLOR":
                return "Color";
            case "VOLTAJE":
                return "Voltaje";
            case "NOSERIE":
                return "Número de serie";

            // Customers
            case "SEARCHKEY":
                return "Clave de búsqueda";
            case "TAXID":
                return "RUT / RFC / Tax ID";
            case "CARD":
                return "Tarjeta de cliente";
            case "DEBTMAX":
                return "Crédito máximo";
            case "DEBT":
                return "Deuda";
            case "CURDEBT":
                return "Deuda actual";
            case "CURDATE":
                return "Fecha de deuda";
            case "FIRSTNAME":
                return "Nombre";
            case "LASTNAME":
                return "Apellido";
            case "EMAIL":
                return "Correo electrónico";
            case "PHONE":
                return "Teléfono";
            case "PHONE2":
                return "Teléfono secundario";
            case "FAX":
                return "Fax";
            case "ADDRESS":
                return "Dirección";
            case "ADDRESS2":
                return "Dirección de envío";
            case "CITY":
                return "Ciudad";
            case "REGION":
                return "Región / Estado";
            case "POSTAL":
                return "Código postal";
            case "COUNTRY":
                return "País";
            case "VISIBLE":
                return "Visible / Activo";
            case "NOTES":
                return "Notas";
            case "MAXPOINTS":
                return "Puntos acumulados";

            // Taxes
            case "RATE":
                return "Tasa / Porcentaje";
            case "PARENTID":
                return "Impuesto padre";
            case "CUSTCATEGORY":
                return "Categoría de cliente";

            // Users/Employees
            case "ROLE":
                return "Rol / Permisos";
            case "APPPASSWORD":
                return "Contraseña";

            default:
                return key.substring(0, 1).toUpperCase() + key.substring(1).toLowerCase();
        }
    }

    private TableDefinition findTableDefinition() {
        TableDefinition td = findTableDefinitionInObject(this, new HashSet<Object>());
        return td;
    }

    private TableDefinition findTableDefinitionInObject(Object obj, Set<Object> visited) {
        if (obj == null || visited.contains(obj))
            return null;
        visited.add(obj);

        Class<?> clazz = obj.getClass();
        String objClassName = clazz.getName();
        if (objClassName.contains("DataLogic") || objClassName.contains("AppView")
                || objClassName.contains("Session")) {
            return null;
        }

        while (clazz != null && clazz != Object.class) {
            for (java.lang.reflect.Field field : clazz.getDeclaredFields()) {
                if (TableDefinition.class.isAssignableFrom(field.getType())) {
                    try {
                        field.setAccessible(true);
                        TableDefinition td = (TableDefinition) field.get(obj);
                        if (td != null)
                            return td;
                    } catch (Exception e) {
                    }
                }
            }
            clazz = clazz.getSuperclass();
        }

        clazz = obj.getClass();
        while (clazz != null && clazz != Object.class) {
            for (java.lang.reflect.Field field : clazz.getDeclaredFields()) {
                Class<?> t = field.getType();
                String fieldTypeName = t.getName();
                if (fieldTypeName.contains("com.openbravo")
                        && !fieldTypeName.contains("DataLogic")
                        && !fieldTypeName.contains("AppView")
                        && !fieldTypeName.contains("Session")
                        && !t.isPrimitive() && !t.isArray() && !t.isEnum()) {
                    try {
                        field.setAccessible(true);
                        Object val = field.get(obj);
                        if (val != null) {
                            TableDefinition td = findTableDefinitionInObject(val, visited);
                            if (td != null)
                                return td;
                        }
                    } catch (Exception e) {
                    }
                }
            }
            clazz = clazz.getSuperclass();
        }
        return null;
    }

    private int[] getIdIndices(TableDefinition td) {
        try {
            java.lang.reflect.Field field = TableDefinition.class.getDeclaredField("idinx");
            field.setAccessible(true);
            return (int[]) field.get(td);
        } catch (Exception e) {
            return new int[] { 0 };
        }
    }

    private String[] getFieldNames(TableDefinition td) {
        try {
            java.lang.reflect.Field field = TableDefinition.class.getDeclaredField("fieldname");
            field.setAccessible(true);
            return (String[]) field.get(td);
        } catch (Exception e) {
            return null;
        }
    }

    private String[] getFieldTranslations(TableDefinition td) {
        try {
            java.lang.reflect.Field field = TableDefinition.class.getDeclaredField("fieldtran");
            field.setAccessible(true);
            return (String[]) field.get(td);
        } catch (Exception e) {
            return null;
        }
    }

    private Object[] fetchOldRecord(String tableName, String[] fieldNames, int[] idIndices, Object[] value) {
        if (value == null || fieldNames == null || idIndices == null || idIndices.length == 0) {
            return null;
        }

        java.sql.Connection conn = null;
        java.sql.PreparedStatement pstmt = null;
        java.sql.ResultSet rs = null;
        try {
            conn = app.getSession().getConnection();
            StringBuilder sb = new StringBuilder("SELECT ");
            for (int i = 0; i < fieldNames.length; i++) {
                if (i > 0)
                    sb.append(", ");
                sb.append(fieldNames[i]);
            }
            sb.append(" FROM ").append(tableName).append(" WHERE ");
            for (int i = 0; i < idIndices.length; i++) {
                if (i > 0)
                    sb.append(" AND ");
                sb.append(fieldNames[idIndices[i]]).append(" = ?");
            }

            pstmt = conn.prepareStatement(sb.toString());
            for (int i = 0; i < idIndices.length; i++) {
                pstmt.setObject(i + 1, value[idIndices[i]]);
            }

            rs = pstmt.executeQuery();
            if (rs.next()) {
                Object[] oldRecord = new Object[fieldNames.length];
                for (int i = 0; i < fieldNames.length; i++) {
                    oldRecord[i] = rs.getObject(i + 1);
                }
                return oldRecord;
            }
        } catch (Exception e) {
            LOGEER.log(Level.WARNING, "Error fetching old record for audit log", e);
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
        return null;
    }

    private String compareRecords(String[] fieldNames, String[] fieldTrans, Object[] oldRec, Object[] newRec) {
        if (fieldNames == null || oldRec == null || newRec == null) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        int limit = Math.min(fieldNames.length, Math.min(oldRec.length, newRec.length));
        for (int i = 0; i < limit; i++) {
            Object oldVal = oldRec[i];
            Object newVal = newRec[i];

            String nameUpper = fieldNames[i].toUpperCase();
            if (nameUpper.equals("ID") || nameUpper.equals("PASSWORD") || nameUpper.equals("IMAGE")
                    || nameUpper.equals("ICON")) {
                continue;
            }

            boolean changed = false;
            if (oldVal == null && newVal != null) {
                changed = true;
            } else if (oldVal != null && !oldVal.equals(newVal)) {
                changed = true;
            }

            if (changed) {
                if (sb.length() > 0)
                    sb.append("\n");
                String displayName = (fieldTrans != null && i < fieldTrans.length) ? fieldTrans[i] : fieldNames[i];
                String oldStr = oldVal == null ? "[Vacío]" : oldVal.toString();
                String newStr = newVal == null ? "[Vacío]" : newVal.toString();

                if (oldStr.length() > 100)
                    oldStr = oldStr.substring(0, 97) + "...";
                if (newStr.length() > 100)
                    newStr = newStr.substring(0, 97) + "...";

                sb.append(displayName).append(" cambió de '").append(oldStr).append("' a '").append(newStr).append("'");
            }
        }
        return sb.toString();
    }

    private String getEntityName(String[] fieldNames, Object[] rec) {
        if (fieldNames == null || rec == null)
            return "Sin Nombre";
        for (int i = 0; i < fieldNames.length; i++) {
            if (i < rec.length && "NAME".equalsIgnoreCase(fieldNames[i])) {
                return rec[i] != null ? rec[i].toString() : "Sin Nombre";
            }
        }
        for (int i = 0; i < fieldNames.length; i++) {
            if (i < rec.length
                    && ("SEARCHKEY".equalsIgnoreCase(fieldNames[i]) || "KEY".equalsIgnoreCase(fieldNames[i]))) {
                return rec[i] != null ? rec[i].toString() : "Sin Nombre";
            }
        }
        if (rec.length > 1 && rec[1] != null) {
            return rec[1].toString();
        }
        if (rec.length > 0 && rec[0] != null) {
            return rec[0].toString();
        }
        return "Sin Nombre";
    }

    protected void saveAuditLog(String eventType, String entityType, String entityId, String entityName,
            String details) {
        String username = "Desconocido";
        try {
            if (app != null && app.getAppUserView() != null && app.getAppUserView().getUser() != null) {
                username = app.getAppUserView().getUser().getName();
            }
        } catch (Exception e) {
        }

        java.sql.Connection conn = null;
        java.sql.PreparedStatement pstmt = null;
        try {
            conn = app.getSession().getConnection();
            pstmt = conn.prepareStatement(
                    "INSERT INTO AUDIT_LOG (ID, USER_NAME, EVENT_TYPE, ENTITY_TYPE, ENTITY_ID, ENTITY_NAME, EVENT_DATE, DETAILS) VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
            pstmt.setString(1, UUID.randomUUID().toString());
            pstmt.setString(2, username);
            pstmt.setString(3, eventType);
            pstmt.setString(4, entityType);
            pstmt.setString(5, entityId);
            pstmt.setString(6, entityName);
            pstmt.setTimestamp(7, new java.sql.Timestamp(System.currentTimeMillis()));
            pstmt.setString(8, details);
            pstmt.executeUpdate();
        } catch (Exception e) {
            LOGEER.log(Level.WARNING, "Error writing to AUDIT_LOG", e);
        } finally {
            if (pstmt != null)
                try {
                    pstmt.close();
                } catch (Exception e) {
                }
        }
    }

    protected void showAuditHistoryDialog() {
        try {
            String tableName = getTableNameForPanel();
            if (tableName == null || tableName.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "No se puede mostrar el historial para este panel (sin definición de tabla).", "Historial",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            Object currentObj = null;
            int idx = bd.getIndex();
            if (idx >= 0 && idx < bd.getListModel().getSize()) {
                currentObj = bd.getListModel().getElementAt(idx);
            }
            if (currentObj == null) {
                JOptionPane.showMessageDialog(this, "Por favor seleccione un registro primero.", "Historial",
                        JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            String entityId = getEntityId(currentObj);
            if (entityId == null || entityId.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No se pudo identificar el ID del registro seleccionado.",
                        "Historial", JOptionPane.WARNING_MESSAGE);
                return;
            }

            showHistoryDialogForEntity(entityId, getTitle());

        } catch (Exception ex) {
            LOGEER.log(Level.SEVERE, "Error displaying history dialog", ex);
            JOptionPane.showMessageDialog(this, "Error al cargar el historial: " + ex.getMessage(), "Historial",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showHistoryDialogForEntity(String entityId, String entityType) {
        java.awt.Window parentWindow = javax.swing.SwingUtilities.getWindowAncestor(this);
        final javax.swing.JDialog dialog;
        if (parentWindow instanceof java.awt.Frame) {
            dialog = new javax.swing.JDialog((java.awt.Frame) parentWindow,
                    "Historial de Modificaciones - " + entityType, true);
        } else if (parentWindow instanceof java.awt.Dialog) {
            dialog = new javax.swing.JDialog((java.awt.Dialog) parentWindow,
                    "Historial de Modificaciones - " + entityType, true);
        } else {
            dialog = new javax.swing.JDialog((java.awt.Frame) null, "Historial de Modificaciones - " + entityType,
                    true);
        }

        dialog.setSize(800, 500);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new java.awt.BorderLayout(10, 10));

        final java.util.List<Object[]> dataList = new java.util.ArrayList<>();
        java.sql.Connection conn = null;
        java.sql.PreparedStatement pstmt = null;
        java.sql.ResultSet rs = null;
        try {
            conn = app.getSession().getConnection();
            pstmt = conn.prepareStatement(
                    "SELECT USER_NAME, EVENT_TYPE, EVENT_DATE, DETAILS, ENTITY_NAME FROM AUDIT_LOG WHERE ENTITY_ID = ? ORDER BY EVENT_DATE DESC");
            pstmt.setString(1, entityId);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                dataList.add(new Object[] {
                        rs.getString("USER_NAME"),
                        rs.getString("EVENT_TYPE"),
                        rs.getTimestamp("EVENT_DATE"),
                        rs.getString("DETAILS"),
                        rs.getString("ENTITY_NAME")
                });
            }
        } catch (Exception e) {
            LOGEER.log(Level.WARNING, "Error loading audit logs for entity " + entityId, e);
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

        String nameHeader = "Registro: " + entityId;
        if (!dataList.isEmpty()) {
            nameHeader = "Historial para: " + dataList.get(0)[4] + " (ID: " + entityId + ")";
        }
        javax.swing.JLabel lblHeader = new javax.swing.JLabel(nameHeader);
        lblHeader.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 14));
        lblHeader.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 5, 10));
        dialog.add(lblHeader, java.awt.BorderLayout.NORTH);

        String[] columnNames = { "Usuario / Empleado", "Acción", "Fecha y Hora" };
        javax.swing.table.DefaultTableModel tableModel = new javax.swing.table.DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        for (Object[] rowData : dataList) {
            String formattedDate = rowData[2] != null ? sdf.format((java.util.Date) rowData[2]) : "";
            tableModel.addRow(new Object[] {
                    rowData[0],
                    rowData[1],
                    formattedDate
            });
        }

        final javax.swing.JTable table = new javax.swing.JTable(tableModel);
        table.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        table.setFont(new java.awt.Font("Arial", java.awt.Font.PLAIN, 12));
        table.setRowHeight(22);

        javax.swing.JScrollPane tableScrollPane = new javax.swing.JScrollPane(table);

        final javax.swing.JTextArea txtDetails = new javax.swing.JTextArea();
        txtDetails.setEditable(false);
        txtDetails.setFont(new java.awt.Font("Consolas", java.awt.Font.PLAIN, 12));
        txtDetails.setLineWrap(true);
        txtDetails.setWrapStyleWord(true);
        txtDetails.setBackground(new java.awt.Color(245, 245, 245));
        javax.swing.JScrollPane detailScrollPane = new javax.swing.JScrollPane(txtDetails);
        detailScrollPane.setBorder(javax.swing.BorderFactory.createTitledBorder("Detalles del Cambio / Acción"));

        table.getSelectionModel().addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            @Override
            public void valueChanged(javax.swing.event.ListSelectionEvent e) {
                int selectedRow = table.getSelectedRow();
                if (selectedRow >= 0 && selectedRow < dataList.size()) {
                    txtDetails.setText(String.valueOf(dataList.get(selectedRow)[3]));
                } else {
                    txtDetails.setText("");
                }
            }
        });

        javax.swing.JSplitPane splitPane = new javax.swing.JSplitPane(javax.swing.JSplitPane.VERTICAL_SPLIT,
                tableScrollPane, detailScrollPane);
        splitPane.setDividerLocation(200);
        splitPane.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 10, 0, 10));
        dialog.add(splitPane, java.awt.BorderLayout.CENTER);

        javax.swing.JButton btnClose = new javax.swing.JButton("Cerrar");
        btnClose.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                dialog.dispose();
            }
        });
        javax.swing.JPanel buttonPanel = new javax.swing.JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));
        buttonPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 10, 10, 10));
        buttonPanel.add(btnClose);
        dialog.add(buttonPanel, java.awt.BorderLayout.SOUTH);

        if (tableModel.getRowCount() > 0) {
            table.setRowSelectionInterval(0, 0);
        } else {
            txtDetails.setText("No hay registro de cambios para este elemento.");
        }

        dialog.setVisible(true);
    }

    public void selectRecordById(String id) {
        if (id == null || id.isEmpty())
            return;
        try {
            if (bd == null || bd.getListModel() == null) {
                return;
            }
            javax.swing.ListModel listModel = bd.getListModel();
            int size = listModel.getSize();
            for (int i = 0; i < size; i++) {
                Object element = listModel.getElementAt(i);
                if (id.equals(getEntityId(element))) {
                    bd.moveTo(i);
                    break;
                }
            }
        } catch (BasicException ex) {
            LOGEER.log(Level.WARNING, "Error moving to record with ID " + id, ex);
        }
    }
    // --- FIN MÉTODOS DE AUDITORÍA Y HISTORIAL ---
}

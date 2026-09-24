package com.openbravo.pos.inventory;

import com.openbravo.pos.util.ModernActionIcon;

import com.openbravo.basic.BasicException;
import com.openbravo.data.loader.SentenceList;
import com.openbravo.data.loader.Session;
import com.openbravo.data.loader.StaticSentence;
import com.openbravo.data.loader.SerializerWriteBasic;
import com.openbravo.data.loader.Datas;
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.pos.forms.AppView;
import com.openbravo.pos.forms.BeanFactoryApp;
import com.openbravo.pos.forms.BeanFactoryException;
import com.openbravo.pos.forms.DataLogicSales;
import com.openbravo.pos.forms.JPanelView;
import com.openbravo.format.Formats;
import com.openbravo.beans.JCalendarDialog;
import com.openbravo.pos.ticket.CategoryInfo;
import com.openbravo.pos.ticket.ProductInfoExt;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

/**
 * Panel para la gestión de promociones.
 * Permite aplicar descuentos a productos o categorías completas.
 */
public class JPanelPromotions extends JPanel implements JPanelView, BeanFactoryApp {

    private static final Logger LOGGER = Logger.getLogger(JPanelPromotions.class.getName());
    private AppView m_App;
    private DataLogicSales m_dlSales;

    private JTextField m_jName;
    private JRadioButton m_rbProduct;
    private JRadioButton m_rbCategory;
    private JComboBox<CategoryInfo> m_jCategoryCombo;
    private JTextField m_jProductRef;
    private JTextField m_jDiscount;
    private JTextField m_jNotes;

    private JTextField m_jDateFrom;
    private JTextField m_jDateTo;

    private JTable m_table;
    private DefaultTableModel m_tableModel;

    private String selectedProductId = null;

    @Override
    public void init(AppView app) throws BeanFactoryException {
        m_App = app;
        m_dlSales = (DataLogicSales) m_App.getBean("com.openbravo.pos.forms.DataLogicSales");
        initComponents();
        createTableIfNotExist();
        loadCategories();
    }

    @Override
    public Object getBean() {
        return this;
    }

    @Override
    public JComponent getComponent() {
        return this;
    }

    @Override
    public String getTitle() {
        return "Gestión de Promociones";
    }

    @Override
    public void activate() throws BasicException {
        refreshTable();
    }

    @Override
    public boolean deactivate() {
        return true;
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setBackground(new Color(250, 247, 242)); // Soft Crema background

        // --- MAIN CONTENT ---
        JPanel mainContent = new JPanel(new BorderLayout(16, 16));
        mainContent.setOpaque(false);
        mainContent.setBorder(new EmptyBorder(16, 16, 16, 16));
        add(mainContent, BorderLayout.CENTER);

        // --- TOP: FORM PANEL ---
        JPanel formWrapper = new JPanel(new BorderLayout());
        formWrapper.setOpaque(false);
        
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(226, 232, 240), 1), // Slate 200 border
            new EmptyBorder(20, 24, 20, 24)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 12, 8, 12);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        Font labelFont = new Font("Segoe UI", Font.BOLD, 13);
        Color labelColor = new Color(71, 85, 105); // Slate 600
        javax.swing.border.Border inputBorder = BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(203, 213, 225), 1), // Slate 300 line
            BorderFactory.createEmptyBorder(6, 10, 6, 10)
        );
        Font inputFont = new Font("Segoe UI", Font.PLAIN, 14);

        // 1. Nombre de la Campaña
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.0;
        JLabel lblName = new JLabel("Nombre de la Campaña:");
        lblName.setFont(labelFont);
        lblName.setForeground(labelColor);
        formPanel.add(lblName, gbc);
        
        m_jName = new JTextField(25);
        m_jName.setFont(inputFont);
        m_jName.setBorder(inputBorder);
        m_jName.setToolTipText("Ejemplo: Oferta de Verano, Descuento Especial...");
        gbc.gridx = 1; gbc.weightx = 1.0;
        formPanel.add(m_jName, gbc);

        // 2. Porcentaje de Descuento
        gbc.gridx = 2; gbc.gridy = 0; gbc.weightx = 0.0;
        JLabel lblDiscount = new JLabel("Porcentaje de Descuento:");
        lblDiscount.setFont(labelFont);
        lblDiscount.setForeground(labelColor);
        formPanel.add(lblDiscount, gbc);
        
        m_jDiscount = new JTextField(8);
        m_jDiscount.setFont(new Font("Segoe UI", Font.BOLD, 14));
        m_jDiscount.setForeground(new Color(202, 159, 65)); // Modern primary gold text
        m_jDiscount.setHorizontalAlignment(JTextField.CENTER);
        m_jDiscount.setBorder(inputBorder);
        m_jDiscount.setToolTipText("Ingrese solo el número (ej: 15 para 15%)");
        gbc.gridx = 3; gbc.weightx = 1.0;
        formPanel.add(m_jDiscount, gbc);

        // 3. Aplicar Descuento a
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.0;
        JLabel lblApplyTo = new JLabel("Aplicar Descuento a:");
        lblApplyTo.setFont(labelFont);
        lblApplyTo.setForeground(labelColor);
        formPanel.add(lblApplyTo, gbc);
        
        JPanel typePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        typePanel.setOpaque(false);
        m_rbProduct = new JRadioButton("Un Producto");
        m_rbCategory = new JRadioButton("Toda una Categoría");
        m_rbProduct.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        m_rbCategory.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        m_rbProduct.setOpaque(false);
        m_rbCategory.setOpaque(false);
        m_rbProduct.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        m_rbCategory.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        ButtonGroup bg = new ButtonGroup();
        bg.add(m_rbProduct); bg.add(m_rbCategory);
        m_rbProduct.setSelected(true);
        typePanel.add(m_rbProduct); typePanel.add(m_rbCategory);
        gbc.gridx = 1; gbc.weightx = 1.0;
        formPanel.add(typePanel, gbc);

        // 4. Periodo de Validez
        gbc.gridx = 2; gbc.gridy = 1; gbc.weightx = 0.0;
        JLabel lblDates = new JLabel("Periodo de Validez:");
        lblDates.setFont(labelFont);
        lblDates.setForeground(labelColor);
        formPanel.add(lblDates, gbc);
        
        JPanel datesPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        datesPanel.setOpaque(false);
        m_jDateFrom = new JTextField(8);
        m_jDateTo = new JTextField(8);
        m_jDateFrom.setFont(inputFont);
        m_jDateTo.setFont(inputFont);
        m_jDateFrom.setBorder(inputBorder);
        m_jDateTo.setBorder(inputBorder);
        m_jDateFrom.setText(Formats.DATE.formatValue(new Date()));
        m_jDateTo.setText(Formats.DATE.formatValue(new Date()));
        m_jDateFrom.setEditable(false);
        m_jDateTo.setEditable(false);
        m_jDateFrom.setBackground(Color.WHITE);
        m_jDateTo.setBackground(Color.WHITE);
        
        datesPanel.add(new JLabel("Desde: "));
        datesPanel.add(m_jDateFrom);
        JButton btnFrom = createDateButton(m_jDateFrom);
        datesPanel.add(btnFrom);
        
        datesPanel.add(new JLabel("  Hasta: "));
        datesPanel.add(m_jDateTo);
        JButton btnTo = createDateButton(m_jDateTo);
        datesPanel.add(btnTo);
        
        gbc.gridx = 3; gbc.weightx = 1.0;
        formPanel.add(datesPanel, gbc);

        // 5. Seleccionar Destino
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.0;
        JLabel lblSelect = new JLabel("Seleccionar Destino:");
        lblSelect.setFont(labelFont);
        lblSelect.setForeground(labelColor);
        formPanel.add(lblSelect, gbc);
        
        JPanel selectionPanel = new JPanel(new GridBagLayout());
        selectionPanel.setOpaque(false);
        GridBagConstraints gbcSel = new GridBagConstraints();
        gbcSel.fill = GridBagConstraints.HORIZONTAL;
        
        m_jCategoryCombo = new JComboBox<>();
        m_jCategoryCombo.setFont(inputFont);
        m_jCategoryCombo.setBorder(BorderFactory.createLineBorder(new Color(203, 213, 225), 1));
        m_jCategoryCombo.setBackground(Color.WHITE);
        m_jCategoryCombo.setVisible(false);
        
        m_jProductRef = new JTextField(20);
        m_jProductRef.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        m_jProductRef.setEditable(false);
        m_jProductRef.setBackground(new Color(248, 250, 252));
        m_jProductRef.setBorder(inputBorder);
        m_jProductRef.setText("Haga clic en buscar...");
        
        JButton btnSearchProd = new JButton("Buscar Producto");
        btnSearchProd.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnSearchProd.setBackground(Color.WHITE);
        btnSearchProd.setForeground(new Color(202, 159, 65)); // brand gold
        btnSearchProd.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(202, 159, 65), 1),
            BorderFactory.createEmptyBorder(6, 12, 6, 12)
        ));
        btnSearchProd.setContentAreaFilled(false);
        btnSearchProd.setFocusPainted(false);
        btnSearchProd.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnSearchProd.addActionListener(e -> searchProduct());
        
        gbcSel.gridx = 0; gbcSel.weightx = 1.0;
        selectionPanel.add(m_jCategoryCombo, gbcSel);
        selectionPanel.add(m_jProductRef, gbcSel);
        gbcSel.gridx = 1; gbcSel.weightx = 0.0; gbcSel.insets = new Insets(0, 8, 0, 0);
        selectionPanel.add(btnSearchProd, gbcSel);
        
        gbc.gridx = 1; gbc.weightx = 1.0;
        formPanel.add(selectionPanel, gbc);

        m_rbProduct.addActionListener(e -> {
            m_jCategoryCombo.setVisible(false);
            m_jProductRef.setVisible(true);
            btnSearchProd.setVisible(true);
            selectionPanel.revalidate();
            selectionPanel.repaint();
        });
        m_rbCategory.addActionListener(e -> {
            m_jCategoryCombo.setVisible(true);
            m_jProductRef.setVisible(false);
            btnSearchProd.setVisible(false);
            selectionPanel.revalidate();
            selectionPanel.repaint();
        });

        // 6. Motivo
        gbc.gridx = 2; gbc.gridy = 2; gbc.weightx = 0.0;
        JLabel lblNotes = new JLabel("Motivo o Comentario:");
        lblNotes.setFont(labelFont);
        lblNotes.setForeground(labelColor);
        formPanel.add(lblNotes, gbc);
        
        m_jNotes = new JTextField(20);
        m_jNotes.setFont(inputFont);
        m_jNotes.setBorder(inputBorder);
        m_jNotes.setToolTipText("Razón de la promoción (opcional)");
        gbc.gridx = 3; gbc.weightx = 1.0;
        formPanel.add(m_jNotes, gbc);

        // 7. Botón Guardar
        gbc.gridy = 3; gbc.gridx = 3; gbc.weightx = 0.0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.EAST;
        JButton btnSave = new JButton("Crear Promoción");
        btnSave.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnSave.setBackground(new Color(202, 159, 65)); // brand gold background
        btnSave.setForeground(Color.WHITE);
        btnSave.setPreferredSize(new Dimension(180, 38));
        btnSave.setBorder(BorderFactory.createEmptyBorder(6, 16, 6, 16));
        btnSave.setFocusPainted(false);
        btnSave.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnSave.addActionListener(e -> savePromotion());
        formPanel.add(btnSave, gbc);

        formWrapper.add(formPanel, BorderLayout.CENTER);
        mainContent.add(formWrapper, BorderLayout.NORTH);

        // --- CENTER: TABLE PANEL ---
        JPanel tableCard = new JPanel(new BorderLayout(15, 15));
        tableCard.setBackground(Color.WHITE);
        tableCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        // Subheader inside the card
        JPanel tableHeaderPanel = new JPanel(new BorderLayout());
        tableHeaderPanel.setOpaque(false);

        JLabel tableTitle = new JLabel("Promociones Activas y Programadas");
        tableTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        tableTitle.setForeground(new Color(15, 23, 42)); // Slate 900
        tableHeaderPanel.add(tableTitle, BorderLayout.WEST);

        // Destructive delete button
        JButton btnDelete = new JButton("Eliminar Promoción");
        btnDelete.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnDelete.addActionListener(e -> deletePromotion());
        tableHeaderPanel.add(btnDelete, BorderLayout.EAST);

        tableCard.add(tableHeaderPanel, BorderLayout.NORTH);

        m_tableModel = new DefaultTableModel(
                new Object[] { "Campaña", "Tipo", "Destino", "Dscto %", "Vence", "Motivo", "Autor", "ID" }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        m_table = new JTable(m_tableModel);
        m_table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        m_table.setRowHeight(35);
        m_table.setShowVerticalLines(false);
        m_table.setShowHorizontalLines(true);
        m_table.setGridColor(new Color(241, 245, 249));
        m_table.setBackground(Color.WHITE);
        m_table.setSelectionBackground(new Color(239, 246, 255));
        m_table.setSelectionForeground(new Color(30, 41, 59));
        m_table.setFocusable(false);
        
        m_table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        m_table.getTableHeader().setBackground(new Color(248, 250, 252));
        m_table.getTableHeader().setForeground(new Color(71, 85, 105));
        m_table.getTableHeader().setReorderingAllowed(false);
        
        m_table.getColumnModel().getColumn(7).setMinWidth(0);
        m_table.getColumnModel().getColumn(7).setMaxWidth(0);
        m_table.getColumnModel().getColumn(7).setWidth(0);

        JScrollPane scrollPane = new JScrollPane(m_table);
        scrollPane.setBackground(Color.WHITE);
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(241, 245, 249), 1));
        
        tableCard.add(scrollPane, BorderLayout.CENTER);
        mainContent.add(tableCard, BorderLayout.CENTER);

        // Apply modern design styling recursively
        com.openbravo.pos.util.ModernLookAndFeel.estilizarComponentes(this);

        // Restyle delete button to be red
        btnDelete.setBackground(new Color(220, 38, 38));
        btnDelete.setForeground(Color.WHITE);
        btnDelete.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(185, 28, 28), 1),
            BorderFactory.createEmptyBorder(6, 16, 6, 16)
        ));

        // Reset date picker buttons to transparent icon buttons
        btnFrom.setContentAreaFilled(false);
        btnFrom.setBorderPainted(false);
        btnFrom.setOpaque(false);
        btnFrom.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));

        btnTo.setContentAreaFilled(false);
        btnTo.setBorderPainted(false);
        btnTo.setOpaque(false);
        btnTo.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));
    }

    private void searchProduct() {
        // En un entorno Swing real, aquí llamaríamos a JProductFinder
        // Como no tengo el buscador visual a mano, pediré el código de barras o referencia
        String code = JOptionPane.showInputDialog(this, "Ingrese Código de Barras o Referencia del Producto:");
        if (code != null && !code.isEmpty()) {
            try {
                ProductInfoExt prod = m_dlSales.getProductInfoByCode(code);
                if (prod == null) {
                    prod = m_dlSales.getProductInfoByReference(code);
                }
                if (prod != null) {
                    selectedProductId = prod.getID();
                    m_jProductRef.setText(prod.getName());
                } else {
                    JOptionPane.showMessageDialog(this, "Producto no encontrado.");
                }
            } catch (BasicException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
            }
        }
    }

    private void loadCategories() {
        try {
            List<CategoryInfo> cats = m_dlSales.getRootCategories();
            for (CategoryInfo c : cats) {
                m_jCategoryCombo.addItem(c);
            }
        } catch (BasicException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }

    private JButton createDateButton(final JTextField text) {
        JButton btn = new JButton();
        btn.setIcon(new ModernActionIcon(ModernActionIcon.Type.CALENDAR, 18));
        btn.addActionListener(e -> {
            Date date;
            try {
                date = (Date) Formats.DATE.parseValue(text.getText());
            } catch (BasicException ex) {
                date = new Date();
            }
            date = JCalendarDialog.showCalendarTime(this, date);
            if (date != null) {
                text.setText(Formats.DATE.formatValue(date));
            }
        });
        return btn;
    }

    private void createTableIfNotExist() {
        try (Connection conn = m_App.getSession().getConnection()) {
            DatabaseMetaData dbm = conn.getMetaData();
            ResultSet rs = dbm.getTables(null, null, "PROMOTIONS", null);
            if (!rs.next()) {
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute("CREATE TABLE PROMOTIONS (" +
                            "ID VARCHAR(255) NOT NULL, " +
                            "NAME VARCHAR(255) NOT NULL, " +
                            "TARGET_TYPE INTEGER NOT NULL, " + // 0: PROD, 1: CAT
                            "TARGET_ID VARCHAR(255) NOT NULL, " +
                            "DISCOUNT DOUBLE NOT NULL, " +
                            "DATE_START TIMESTAMP NOT NULL, " +
                            "DATE_END TIMESTAMP NOT NULL, " +
                            "REASON VARCHAR(255), " +
                            "CREATED_BY VARCHAR(255), " +
                            "PRIMARY KEY (ID))");
                    LOGGER.info("Created table PROMOTIONS");
                }
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error creating table PROMOTIONS", ex);
        }
    }

    private void savePromotion() {
        if (m_jName.getText().isEmpty() || m_jDiscount.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor complete Nombre y Descuento.");
            return;
        }

        try {
            double discount = Double.parseDouble(m_jDiscount.getText());
            String targetId = m_rbProduct.isSelected() ? selectedProductId : ((CategoryInfo) m_jCategoryCombo.getSelectedItem()).getID();
            
            if (m_rbProduct.isSelected() && targetId == null) {
                JOptionPane.showMessageDialog(this, "Debe seleccionar un producto.");
                return;
            }

            Timestamp start = new Timestamp(((Date) Formats.DATE.parseValue(m_jDateFrom.getText())).getTime());
            Date dateTo = (Date) Formats.DATE.parseValue(m_jDateTo.getText());
            // Ajustar el fin del día a las 23:59:59
            java.util.Calendar cal = java.util.Calendar.getInstance();
            cal.setTime(dateTo);
            cal.set(java.util.Calendar.HOUR_OF_DAY, 23);
            cal.set(java.util.Calendar.MINUTE, 59);
            cal.set(java.util.Calendar.SECOND, 59);
            cal.set(java.util.Calendar.MILLISECOND, 999);
            Timestamp end = new Timestamp(cal.getTimeInMillis());

            try (Connection conn = m_App.getSession().getConnection();
                 PreparedStatement ps = conn.prepareStatement("INSERT INTO PROMOTIONS (ID, NAME, TARGET_TYPE, TARGET_ID, DISCOUNT, DATE_START, DATE_END, REASON, CREATED_BY) VALUES (?,?,?,?,?,?,?,?,?)")) {
                
                ps.setString(1, UUID.randomUUID().toString());
                ps.setString(2, m_jName.getText());
                ps.setInt(3, m_rbProduct.isSelected() ? 0 : 1);
                ps.setString(4, targetId);
                ps.setDouble(5, discount);
                ps.setTimestamp(6, start);
                ps.setTimestamp(7, end);
                ps.setString(8, m_jNotes.getText());
                ps.setString(9, m_App.getAppUserView().getUser().getName());
                
                ps.executeUpdate();
                JOptionPane.showMessageDialog(this, "Promoción guardada correctamente.");
                
                // Limpiar form
                m_jName.setText("");
                m_jDiscount.setText("");
                m_jNotes.setText("");
                selectedProductId = null;
                m_jProductRef.setText("");
                
                refreshTable();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al guardar: " + ex.getMessage());
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }

    private void refreshTable() {
        m_tableModel.setRowCount(0);
        try (Connection conn = m_App.getSession().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM PROMOTIONS ORDER BY DATE_END DESC")) {
            
            while (rs.next()) {
                int type = rs.getInt("TARGET_TYPE");
                String targetName = rs.getString("TARGET_ID"); // fallback
                
                // Intentar obtener el nombre amigable
                try {
                    if (type == 0) {
                        ProductInfoExt p = m_dlSales.getProductInfo(rs.getString("TARGET_ID"));
                        if (p != null) targetName = p.getName();
                    } else {
                        CategoryInfo c = m_dlSales.getCategoryInfo(rs.getString("TARGET_ID"));
                        if (c != null) targetName = c.getName();
                    }
                } catch (Exception e) {}

                m_tableModel.addRow(new Object[]{
                    rs.getString("NAME"),
                    type == 0 ? "Producto" : "Categoría",
                    targetName,
                    rs.getDouble("DISCOUNT"),
                    Formats.DATE.formatValue(rs.getTimestamp("DATE_END")),
                    rs.getString("REASON"),
                    rs.getString("CREATED_BY"),
                    rs.getString("ID")
                });
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }

    private void deletePromotion() {
        int row = m_table.getSelectedRow();
        if (row < 0) return;
        
        String id = m_table.getValueAt(row, 7).toString();
        if (JOptionPane.showConfirmDialog(this, "¿Eliminar esta promoción?", "Confirmar", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            try (Connection conn = m_App.getSession().getConnection();
                 PreparedStatement ps = conn.prepareStatement("DELETE FROM PROMOTIONS WHERE ID = ?")) {
                ps.setString(1, id);
                ps.executeUpdate();
                refreshTable();
            } catch (SQLException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
            }
        }
    }
}

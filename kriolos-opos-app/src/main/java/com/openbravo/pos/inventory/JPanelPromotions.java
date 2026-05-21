package com.openbravo.pos.inventory;

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
        setBackground(new Color(245, 245, 245));

        // --- HEADER ---
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(46, 125, 50)); // Emerald
        header.setPreferredSize(new Dimension(0, 60));
        JLabel title = new JLabel("   GESTIÓN DE PROMOCIONES Y DESCUENTOS");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(Color.WHITE);
        header.add(title, BorderLayout.CENTER);
        
        JLabel subtitle = new JLabel("Configura ofertas temporales para productos o categorías   ");
        subtitle.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        subtitle.setForeground(new Color(200, 230, 201));
        subtitle.setHorizontalAlignment(SwingConstants.RIGHT);
        header.add(subtitle, BorderLayout.SOUTH);
        add(header, BorderLayout.NORTH);

        // --- MAIN CONTENT ---
        JPanel mainContent = new JPanel(new BorderLayout(20, 20));
        mainContent.setOpaque(false);
        mainContent.setBorder(new EmptyBorder(20, 20, 20, 20));
        add(mainContent, BorderLayout.CENTER);

        // --- TOP: FORM PANEL ---
        JPanel formWrapper = new JPanel(new BorderLayout());
        formWrapper.setOpaque(false);
        
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(javax.swing.BorderFactory.createCompoundBorder(
            new javax.swing.border.LineBorder(new Color(200, 200, 200), 1),
            new EmptyBorder(20, 20, 20, 20)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        // Estilo de fuente para etiquetas
        Font labelFont = new Font("Segoe UI", Font.BOLD, 13);
        Color labelColor = new Color(70, 70, 70);

        // 1. Nombre
        gbc.gridx = 0; gbc.gridy = 0;
        JLabel lblName = new JLabel("Nombre de la Campaña:");
        lblName.setFont(labelFont);
        lblName.setForeground(labelColor);
        formPanel.add(lblName, gbc);
        
        m_jName = new JTextField(25);
        m_jName.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        m_jName.setToolTipText("Ejemplo: Oferta de Verano, Descuento Especial...");
        gbc.gridx = 1; gbc.gridwidth = 1;
        formPanel.add(m_jName, gbc);

        // 2. Descuento
        gbc.gridx = 2; gbc.gridy = 0;
        JLabel lblDiscount = new JLabel("Porcentaje de Descuento:");
        lblDiscount.setFont(labelFont);
        lblDiscount.setForeground(labelColor);
        formPanel.add(lblDiscount, gbc);
        
        m_jDiscount = new JTextField(8);
        m_jDiscount.setFont(new Font("Segoe UI", Font.BOLD, 14));
        m_jDiscount.setForeground(new Color(183, 28, 28));
        m_jDiscount.setHorizontalAlignment(JTextField.CENTER);
        m_jDiscount.setToolTipText("Ingrese solo el número (ej: 15 para 15%)");
        gbc.gridx = 3;
        formPanel.add(m_jDiscount, gbc);

        // 3. Aplicar a (Radio Buttons)
        gbc.gridx = 0; gbc.gridy = 1;
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
        ButtonGroup bg = new ButtonGroup();
        bg.add(m_rbProduct); bg.add(m_rbCategory);
        m_rbProduct.setSelected(true);
        typePanel.add(m_rbProduct); typePanel.add(m_rbCategory);
        gbc.gridx = 1;
        formPanel.add(typePanel, gbc);

        // 4. Fechas (Desde/Hasta)
        gbc.gridx = 2; gbc.gridy = 1;
        JLabel lblDates = new JLabel("Periodo de Validez:");
        lblDates.setFont(labelFont);
        lblDates.setForeground(labelColor);
        formPanel.add(lblDates, gbc);
        
        JPanel datesPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        datesPanel.setOpaque(false);
        m_jDateFrom = new JTextField(8);
        m_jDateTo = new JTextField(8);
        m_jDateFrom.setText(Formats.DATE.formatValue(new Date()));
        m_jDateTo.setText(Formats.DATE.formatValue(new Date()));
        m_jDateFrom.setEditable(false);
        m_jDateTo.setEditable(false);
        
        datesPanel.add(new JLabel("Desde: "));
        datesPanel.add(m_jDateFrom);
        datesPanel.add(createDateButton(m_jDateFrom));
        datesPanel.add(new JLabel("  Hasta: "));
        datesPanel.add(m_jDateTo);
        datesPanel.add(createDateButton(m_jDateTo));
        gbc.gridx = 3;
        formPanel.add(datesPanel, gbc);

        // 5. Selección (Producto o Categoría)
        gbc.gridx = 0; gbc.gridy = 2;
        JLabel lblSelect = new JLabel("Seleccionar Destino:");
        lblSelect.setFont(labelFont);
        lblSelect.setForeground(labelColor);
        formPanel.add(lblSelect, gbc);
        
        JPanel selectionPanel = new JPanel(new GridBagLayout());
        selectionPanel.setOpaque(false);
        GridBagConstraints gbcSel = new GridBagConstraints();
        gbcSel.fill = GridBagConstraints.HORIZONTAL;
        
        m_jCategoryCombo = new JComboBox<>();
        m_jCategoryCombo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        m_jCategoryCombo.setVisible(false);
        
        m_jProductRef = new JTextField(20);
        m_jProductRef.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        m_jProductRef.setEditable(false);
        m_jProductRef.setText("Haga clic en buscar...");
        
        JButton btnSearchProd = new JButton("Buscar Producto");
        btnSearchProd.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnSearchProd.addActionListener(e -> searchProduct());
        
        gbcSel.gridx = 0; gbcSel.weightx = 1.0;
        selectionPanel.add(m_jCategoryCombo, gbcSel);
        selectionPanel.add(m_jProductRef, gbcSel);
        gbcSel.gridx = 1; gbcSel.weightx = 0.0; gbcSel.insets = new Insets(0, 5, 0, 0);
        selectionPanel.add(btnSearchProd, gbcSel);
        
        gbc.gridx = 1;
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
        gbc.gridx = 2; gbc.gridy = 2;
        JLabel lblNotes = new JLabel("Motivo o Comentario:");
        lblNotes.setFont(labelFont);
        lblNotes.setForeground(labelColor);
        formPanel.add(lblNotes, gbc);
        
        m_jNotes = new JTextField(20);
        m_jNotes.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        m_jNotes.setToolTipText("Razón de la promoción (opcional)");
        gbc.gridx = 3;
        formPanel.add(m_jNotes, gbc);

        // 7. Botón Guardar
        gbc.gridy = 3; gbc.gridx = 3;
        JButton btnSave = new JButton("   CREAR PROMOCIÓN   ");
        btnSave.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnSave.setBackground(new Color(46, 125, 50));
        btnSave.setForeground(Color.WHITE);
        btnSave.setPreferredSize(new Dimension(200, 40));
        btnSave.addActionListener(e -> savePromotion());
        formPanel.add(btnSave, gbc);

        formWrapper.add(formPanel, BorderLayout.CENTER);
        mainContent.add(formWrapper, BorderLayout.NORTH);

        // --- CENTER: TABLE PANEL ---
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setOpaque(false);
        tablePanel.setBorder(javax.swing.BorderFactory.createTitledBorder(
            javax.swing.BorderFactory.createEtchedBorder(), "Promociones Activas y Programadas", 
            javax.swing.border.TitledBorder.LEFT, javax.swing.border.TitledBorder.TOP, 
            new Font("Segoe UI", Font.BOLD, 12), new Color(46, 125, 50)
        ));

        m_tableModel = new DefaultTableModel(
                new Object[] { "Campaña", "Tipo", "Destino", "Dscto %", "Vence", "Motivo", "Autor", "ID" }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        m_table = new JTable(m_tableModel);
        m_table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        m_table.setRowHeight(25);
        m_table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        m_table.getTableHeader().setBackground(new Color(240, 240, 240));
        
        m_table.getColumnModel().getColumn(7).setMinWidth(0);
        m_table.getColumnModel().getColumn(7).setMaxWidth(0);
        m_table.getColumnModel().getColumn(7).setWidth(0);
        
        tablePanel.add(new JScrollPane(m_table), BorderLayout.CENTER);
        mainContent.add(tablePanel, BorderLayout.CENTER);

        // --- BOTTOM: ACTIONS ---
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footer.setBackground(new Color(230, 230, 230));
        footer.setPreferredSize(new Dimension(0, 50));
        
        JButton btnDelete = new JButton("Eliminar Promoción Seleccionada");
        btnDelete.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnDelete.setBackground(new Color(183, 28, 28)); // Dark red
        btnDelete.setForeground(Color.WHITE);
        btnDelete.setPreferredSize(new Dimension(250, 35));
        btnDelete.addActionListener(e -> deletePromotion());
        footer.add(btnDelete);
        
        add(footer, BorderLayout.SOUTH);
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
        btn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/date.png")));
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

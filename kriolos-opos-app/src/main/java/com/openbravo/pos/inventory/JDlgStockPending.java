package com.openbravo.pos.inventory;

import com.openbravo.basic.BasicException;
import com.openbravo.format.Formats;
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.pos.forms.AppView;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Date;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

/**
 * Diálogo para la gestión de stock pendiente por recibir de proveedores
 */
public class JDlgStockPending extends JDialog {

    private static final Logger LOGGER = Logger.getLogger(JDlgStockPending.class.getName());
    private AppView m_App;

    private JTextField m_jProductName;
    private JTextField m_jSupplierName;
    private JTextField m_jDocTitle;
    private JComboBox<String> m_jStatus;
    private JTextField m_jNotes;

    private JTable m_table;
    private DefaultTableModel m_tableModel;

    private JDlgStockPending(Frame parent, boolean modal) {
        super(parent, modal);
    }

    private JDlgStockPending(Dialog parent, boolean modal) {
        super(parent, modal);
    }

    private void init(AppView app) {
        m_App = app;
        initComponents();
        createTableIfNotExist();
        refreshTable();
        setSize(1000, 700);
        setVisible(true);
    }

    public static void showMessage(Component parent, AppView app) {
        Window window = getWindow(parent);
        JDlgStockPending myMsg;
        if (window instanceof Frame) {
            myMsg = new JDlgStockPending((Frame) window, true);
        } else {
            myMsg = new JDlgStockPending((Dialog) window, true);
        }
        myMsg.init(app);
    }

    private static Window getWindow(Component parent) {
        if (parent == null) {
            return new JFrame();
        } else if (parent instanceof Frame || parent instanceof Dialog) {
            return (Window) parent;
        } else {
            return getWindow(parent.getParent());
        }
    }

    private void initComponents() {
        setTitle("Stock Pendiente de Proveedores");
        setPreferredSize(new Dimension(1024, 720)); // Tamaño aumentado
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.WHITE);

        // --- HEADER ---
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(0, 121, 107)); // Teal
        header.setPreferredSize(new Dimension(0, 50));
        JLabel title = new JLabel("  PRODUCTOS PENDIENTES POR RECIBIR");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(Color.WHITE);
        header.add(title, BorderLayout.CENTER);
        add(header, BorderLayout.NORTH);

        // --- MAIN CONTENT ---
        JPanel mainContent = new JPanel(new BorderLayout(15, 15));
        mainContent.setBackground(Color.WHITE);
        mainContent.setBorder(new EmptyBorder(15, 15, 15, 15));
        add(mainContent, BorderLayout.CENTER);

        // --- CENTER: TABLE ---
        m_tableModel = new DefaultTableModel(
                new Object[] { "Fecha Registro", "Producto", "Proveedor", "Documento / Título", "Estado", "ID" }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        m_table = new JTable(m_tableModel);
        m_table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        m_table.setRowHeight(30);
        // Ocultar ID
        m_table.getColumnModel().getColumn(5).setMinWidth(0);
        m_table.getColumnModel().getColumn(5).setMaxWidth(0);
        m_table.getColumnModel().getColumn(5).setWidth(0);
        
        // Ajustar anchos de columnas visibles
        m_table.getColumnModel().getColumn(0).setPreferredWidth(140); // Fecha
        m_table.getColumnModel().getColumn(1).setPreferredWidth(200); // Producto
        m_table.getColumnModel().getColumn(2).setPreferredWidth(150); // Proveedor
        m_table.getColumnModel().getColumn(3).setPreferredWidth(250); // Documento / Título (Aumentado)
        m_table.getColumnModel().getColumn(4).setPreferredWidth(120); // Estado

        mainContent.add(new JScrollPane(m_table), BorderLayout.CENTER);

        // --- BOTTOM: FORM ---
        JPanel bottomPanel = new JPanel(new BorderLayout(10, 10));
        bottomPanel.setBackground(Color.WHITE);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(new Color(245, 245, 245));
        formPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Nuevo Seguimiento / Editar"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.EAST;

        // Label Style
        Font labelFont = new Font("Segoe UI", Font.BOLD, 12);

        gbc.gridx = 0; gbc.gridy = 0;
        gbc.weightx = 0.0;
        JLabel lblProduct = new JLabel("PRODUCTO:");
        lblProduct.setFont(labelFont);
        formPanel.add(lblProduct, gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        m_jProductName = new JTextField();
        formPanel.add(m_jProductName, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        gbc.weightx = 0.0;
        JLabel lblSupplier = new JLabel("PROVEEDOR:");
        lblSupplier.setFont(labelFont);
        formPanel.add(lblSupplier, gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        m_jSupplierName = new JTextField();
        formPanel.add(m_jSupplierName, gbc);

        gbc.gridx = 2; gbc.gridy = 0;
        gbc.weightx = 0.0;
        JLabel lblDoc = new JLabel("DOCUMENTO / TÍTULO:");
        lblDoc.setFont(labelFont);
        formPanel.add(lblDoc, gbc);
        gbc.gridx = 3;
        gbc.weightx = 1.0;
        m_jDocTitle = new JTextField();
        formPanel.add(m_jDocTitle, gbc);

        gbc.gridx = 2; gbc.gridy = 1;
        gbc.weightx = 0.0;
        JLabel lblStatus = new JLabel("ESTADO:");
        lblStatus.setFont(labelFont);
        formPanel.add(lblStatus, gbc);
        gbc.gridx = 3;
        gbc.weightx = 1.0;
        m_jStatus = new JComboBox<>(new String[] { "PENDIENTE", "EN TRÁNSITO", "PEDIDO REALIZADO", "RECIBIDO PARCIAL", "COMPLETADO", "CANCELADO" });
        formPanel.add(m_jStatus, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        gbc.weightx = 0.0;
        JLabel lblNotes = new JLabel("NOTAS:");
        lblNotes.setFont(labelFont);
        formPanel.add(lblNotes, gbc);
        gbc.gridx = 1; gbc.gridwidth = 3;
        gbc.weightx = 1.0;
        m_jNotes = new JTextField();
        formPanel.add(m_jNotes, gbc);

        // Botones
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setOpaque(false);
        
        JButton btnAdd = new JButton("GUARDAR REGISTRO");
        btnAdd.setBackground(new Color(0, 121, 107));
        btnAdd.setForeground(Color.WHITE);
        btnAdd.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnAdd.addActionListener(e -> savePendingStock());
        
        JButton btnDelete = new JButton("ELIMINAR");
        btnDelete.setBackground(new Color(198, 40, 40));
        btnDelete.setForeground(Color.WHITE);
        btnDelete.addActionListener(e -> deleteSelected());

        JButton btnClose = new JButton("CERRAR");
        btnClose.addActionListener(e -> dispose());

        buttonPanel.add(btnDelete);
        buttonPanel.add(btnAdd);
        buttonPanel.add(btnClose);

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 4;
        formPanel.add(buttonPanel, gbc);

        bottomPanel.add(formPanel, BorderLayout.CENTER);
        mainContent.add(bottomPanel, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(null);
    }

    private void createTableIfNotExist() {
        try (Connection con = m_App.getSession().getConnection();
                Statement stmt = con.createStatement()) {
            String sql = "CREATE TABLE IF NOT EXISTS STOCK_PENDING (" +
                    "ID VARCHAR(255) PRIMARY KEY, " +
                    "DATENEW TIMESTAMP, " +
                    "PRODUCT_NAME VARCHAR(255), " +
                    "SUPPLIER_NAME VARCHAR(255), " +
                    "DOC_TITLE VARCHAR(255), " +
                    "STATUS VARCHAR(50), " +
                    "NOTES VARCHAR(1000)" +
                    ")";
            stmt.executeUpdate(sql);
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error creating STOCK_PENDING table", ex);
        }
    }

    private void savePendingStock() {
        String product = m_jProductName.getText().trim();
        String supplier = m_jSupplierName.getText().trim();
        String doc = m_jDocTitle.getText().trim();
        String status = (String) m_jStatus.getSelectedItem();
        String notes = m_jNotes.getText().trim();

        if (product.isEmpty()) {
            JOptionPane.showMessageDialog(this, "El nombre del producto es obligatorio.");
            return;
        }

        try (Connection con = m_App.getSession().getConnection();
                PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO STOCK_PENDING (ID, DATENEW, PRODUCT_NAME, SUPPLIER_NAME, DOC_TITLE, STATUS, NOTES) VALUES (?, ?, ?, ?, ?, ?, ?)")) {
            ps.setString(1, UUID.randomUUID().toString());
            ps.setTimestamp(2, new Timestamp(new Date().getTime()));
            ps.setString(3, product);
            ps.setString(4, supplier);
            ps.setString(5, doc);
            ps.setString(6, status);
            ps.setString(7, notes);
            ps.executeUpdate();

            m_jProductName.setText("");
            m_jSupplierName.setText("");
            m_jDocTitle.setText("");
            m_jNotes.setText("");
            m_jProductName.requestFocus();

            refreshTable();
            JOptionPane.showMessageDialog(this, "Registro de stock pendiente guardado.");
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error saving pending stock", ex);
            JOptionPane.showMessageDialog(this, "Error al guardar registro.");
        }
    }

    private void refreshTable() {
        m_tableModel.setRowCount(0);
        try (Connection con = m_App.getSession().getConnection();
                Statement stmt = con.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT DATENEW, PRODUCT_NAME, SUPPLIER_NAME, DOC_TITLE, STATUS, ID FROM STOCK_PENDING ORDER BY DATENEW DESC")) {
            while (rs.next()) {
                m_tableModel.addRow(new Object[] {
                        Formats.TIMESTAMP.formatValue(rs.getTimestamp(1)),
                        rs.getString(2),
                        rs.getString(3),
                        rs.getString(4),
                        rs.getString(5),
                        rs.getString(6)
                });
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error refreshing table", ex);
        }
    }

    private void deleteSelected() {
        int row = m_table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Seleccione un registro para eliminar.");
            return;
        }

        String id = (String) m_tableModel.getValueAt(row, 5);
        int confirm = JOptionPane.showConfirmDialog(this, "¿Eliminar este registro de seguimiento?", "Confirmar", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        try (Connection con = m_App.getSession().getConnection();
                PreparedStatement ps = con.prepareStatement("DELETE FROM STOCK_PENDING WHERE ID = ?")) {
            ps.setString(1, id);
            ps.executeUpdate();
            refreshTable();
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error deleting record", ex);
        }
    }
}

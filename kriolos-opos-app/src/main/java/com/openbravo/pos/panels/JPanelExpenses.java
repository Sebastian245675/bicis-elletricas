package com.openbravo.pos.panels;

import com.openbravo.basic.BasicException;
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.pos.forms.AppView;
import com.openbravo.pos.forms.BeanFactoryApp;
import com.openbravo.pos.forms.BeanFactoryException;
import com.openbravo.pos.forms.JPanelView;
import com.openbravo.format.Formats;
import com.openbravo.beans.JCalendarDialog;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
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
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

/**
 * Panel para la gestión de gastos (Mantenimiento de Gastos)
 * Basado en la solicitud del usuario para registrar nombre, monto, comentario y
 * filtrar por fechas.
 */
public class JPanelExpenses extends JPanel implements JPanelView, BeanFactoryApp {

    private static final Logger LOGGER = Logger.getLogger(JPanelExpenses.class.getName());
    private AppView m_App;

    private JTextField m_jName;
    private JTextField m_jAmount;
    private JTextField m_jNotes;

    private JTextField m_jDateFrom;
    private JTextField m_jDateTo;

    private JTable m_table;
    private DefaultTableModel m_tableModel;
    private JLabel m_lblTotal;

    @Override
    public void init(AppView app) throws BeanFactoryException {
        m_App = app;
        initComponents();
        createTableIfNotExist();
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
        return "Gestión de Gastos";
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
        setBackground(Color.WHITE);

        // --- HEADER ---
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(46, 125, 50)); // Emerald
        header.setPreferredSize(new Dimension(0, 50));
        JLabel title = new JLabel("  MANTENIMIENTO DE GASTOS");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(Color.WHITE);
        header.add(title, BorderLayout.CENTER);
        add(header, BorderLayout.NORTH);

        // --- MAIN CONTENT ---
        JPanel mainContent = new JPanel(new BorderLayout(15, 15));
        mainContent.setBackground(Color.WHITE);
        mainContent.setBorder(new EmptyBorder(15, 15, 15, 15));
        add(mainContent, BorderLayout.CENTER);

        // --- TOP: FILTERS ---
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        filterPanel.setBackground(new Color(245, 245, 245));
        filterPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Filtrar por Fechas"));

        m_jDateFrom = new JTextField(10);
        m_jDateFrom.setBackground(Color.WHITE);
        m_jDateFrom.setForeground(Color.BLACK);
        m_jDateTo = new JTextField(10);
        m_jDateTo.setBackground(Color.WHITE);
        m_jDateTo.setForeground(Color.BLACK);

        Date now = new Date();
        m_jDateFrom.setText(Formats.DATE.formatValue(now));
        m_jDateTo.setText(Formats.DATE.formatValue(now));

        JButton btnFrom = createDateButton(m_jDateFrom);
        JButton btnTo = createDateButton(m_jDateTo);

        JButton btnSearch = new JButton("Buscar Gastos");
        btnSearch.setBackground(new Color(46, 125, 50));
        btnSearch.setForeground(Color.WHITE);
        btnSearch.addActionListener(e -> refreshTable());

        JLabel lblFrom = new JLabel("Desde:");
        lblFrom.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblFrom.setForeground(Color.BLACK);
        filterPanel.add(lblFrom);
        filterPanel.add(m_jDateFrom);
        filterPanel.add(btnFrom);
        
        JLabel lblTo = new JLabel("  Hasta:");
        lblTo.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTo.setForeground(Color.BLACK);
        filterPanel.add(lblTo);
        filterPanel.add(m_jDateTo);
        filterPanel.add(btnTo);
        filterPanel.add(btnSearch);

        mainContent.add(filterPanel, BorderLayout.NORTH);

        // --- CENTER: TABLE ---
        m_tableModel = new DefaultTableModel(
                new Object[] { "Fecha", "Gasto / Nombre", "Monto", "Comentario", "ID" }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        m_table = new JTable(m_tableModel);
        m_table.setFont(new Font("Segoe UI", Font.BOLD, 14)); // Negrita para visibilidad
        m_table.setForeground(Color.BLACK); // Negro intenso
        m_table.setRowHeight(30); // Un poco más alto para que respire la negrita
        // Hide ID column
        m_table.getColumnModel().getColumn(4).setMinWidth(0);
        m_table.getColumnModel().getColumn(4).setMaxWidth(0);
        m_table.getColumnModel().getColumn(4).setWidth(0);

        mainContent.add(new JScrollPane(m_table), BorderLayout.CENTER);

        // --- BOTTOM: FORM & TOTAL ---
        JPanel bottomPanel = new JPanel(new BorderLayout(10, 10));
        bottomPanel.setOpaque(false);

        // -- CONTENEDOR CENTRADO PARA EL FORMULARIO --
        JPanel centerWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
        centerWrapper.setOpaque(false);

        // Form as a "Card"
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createLineBorder(new Color(230, 230, 230), 1, true),
                new EmptyBorder(20, 30, 20, 30)));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Estilo común para etiquetas
        Font labelFont = new Font("Segoe UI", Font.BOLD, 13);
        Color labelColor = new Color(70, 70, 70);

        // -- COLUMNA 1: NOMBRE --
        gbc.gridx = 0;
        gbc.gridy = 0;
        JLabel lblName = new JLabel("¿EN QUÉ SE GASTÓ?");
        lblName.setFont(labelFont);
        lblName.setForeground(labelColor);
        formPanel.add(lblName, gbc);

        gbc.gridy = 1;
        m_jName = new JTextField(25);
        m_jName.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        m_jName.setPreferredSize(new Dimension(300, 35));
        m_jName.setBackground(Color.WHITE); // Forzar fondo blanco
        m_jName.setForeground(Color.BLACK); // Texto negro fuerte
        m_jName.setCaretColor(Color.BLACK);
        formPanel.add(m_jName, gbc);

        // -- COLUMNA 2: MONTO --
        gbc.gridx = 1;
        gbc.gridy = 0;
        JLabel lblAmount = new JLabel("¿CUÁNTO COSTÓ? ($)");
        lblAmount.setFont(labelFont);
        lblAmount.setForeground(labelColor);
        formPanel.add(lblAmount, gbc);

        gbc.gridy = 1;
        m_jAmount = new JTextField(10);
        m_jAmount.setFont(new Font("Segoe UI", Font.BOLD, 18));
        m_jAmount.setPreferredSize(new Dimension(150, 35));
        m_jAmount.setForeground(new Color(183, 28, 28)); // Rojo para dinero
        m_jAmount.setBackground(new Color(255, 255, 225)); // Restaurar el tono amarillento suave
        m_jAmount.setHorizontalAlignment(JTextField.CENTER);
        m_jAmount.setCaretColor(Color.BLACK);
        formPanel.add(m_jAmount, gbc);

        // -- COLUMNA 3: COMENTARIO --
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(15, 10, 5, 10);
        JLabel lblNotes = new JLabel("OBSERVACIÓN / COMENTARIO (OPCIONAL)");
        lblNotes.setFont(labelFont);
        lblNotes.setForeground(labelColor);
        formPanel.add(lblNotes, gbc);

        gbc.gridy = 3;
        m_jNotes = new JTextField();
        m_jNotes.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        m_jNotes.setPreferredSize(new Dimension(0, 35));
        m_jNotes.setBackground(Color.WHITE);
        m_jNotes.setForeground(Color.BLACK);
        m_jNotes.setCaretColor(Color.BLACK);
        formPanel.add(m_jNotes, gbc);

        // -- BOTÓN REGISTRAR --
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.gridheight = 4;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        gbc.insets = new Insets(5, 20, 5, 10);
        JButton btnAdd = new JButton("<html><center><b>GUARDAR</b><br>GASTO</center></html>");
        btnAdd.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnAdd.setBackground(new Color(46, 125, 50));
        btnAdd.setForeground(Color.WHITE);
        btnAdd.setPreferredSize(new Dimension(130, 110));
        btnAdd.setFocusPainted(false);
        btnAdd.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnAdd.addActionListener(e -> saveExpense());
        formPanel.add(btnAdd, gbc);

        centerWrapper.add(formPanel);
        bottomPanel.add(centerWrapper, BorderLayout.CENTER);

        // --- Grand Total ---
        JPanel totalPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        totalPanel.setBackground(new Color(46, 125, 50));
        m_lblTotal = new JLabel("TOTAL GASTOS: $ 0.00  ");
        m_lblTotal.setFont(new Font("Segoe UI", Font.BOLD, 22));
        m_lblTotal.setForeground(Color.WHITE);
        totalPanel.add(m_lblTotal);

        bottomPanel.add(totalPanel, BorderLayout.SOUTH);

        mainContent.add(bottomPanel, BorderLayout.SOUTH);

        // --- DELETE ACTION ---
        JButton btnDelete = new JButton("Eliminar Gasto Seleccionado");
        btnDelete.setBackground(new Color(211, 47, 47)); // Red
        btnDelete.setForeground(Color.WHITE);
        btnDelete.addActionListener(e -> deleteSelectedExpense());
        filterPanel.add(btnDelete);
    }

    private void deleteSelectedExpense() {
        int row = m_table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Seleccione un gasto de la tabla para eliminar.");
            return;
        }

        String id = (String) m_tableModel.getValueAt(row, 4);
        if (id == null)
            return;

        int confirm = JOptionPane.showConfirmDialog(this, "¿Está seguro de eliminar este gasto?", "Confirmar",
                JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION)
            return;

        try (Connection con = m_App.getSession().getConnection();
                PreparedStatement ps = con.prepareStatement("DELETE FROM EXPENSES WHERE ID = ?")) {
            ps.setString(1, id);
            ps.executeUpdate();
            refreshTable();
            JOptionPane.showMessageDialog(this, "Gasto eliminado.");
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error deleting expense", ex);
            JOptionPane.showMessageDialog(this, "Error al eliminar gasta.");
        }
    }

    private JButton createDateButton(JTextField field) {
        JButton btn = new JButton("...");
        btn.addActionListener(e -> {
            Date date;
            try {
                date = (Date) Formats.DATE.parseValue(field.getText());
            } catch (BasicException ex) {
                date = new Date();
            }
            date = JCalendarDialog.showCalendar(this, date);
            if (date != null) {
                field.setText(Formats.DATE.formatValue(date));
            }
        });
        return btn;
    }

    private void createTableIfNotExist() {
        try (Connection con = m_App.getSession().getConnection();
                Statement stmt = con.createStatement()) {
            // Adaptado para detectar el motor (HFSQL, MySQL, etc)
            String sql = "CREATE TABLE IF NOT EXISTS EXPENSES (" +
                    "ID VARCHAR(255) PRIMARY KEY, " +
                    "DATENEW TIMESTAMP, " +
                    "NAME VARCHAR(255), " +
                    "AMOUNT DOUBLE, " +
                    "RESPONSIBLE VARCHAR(255), " +
                    "NOTES VARCHAR(255)" +
                    ")";
            stmt.executeUpdate(sql);
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error creating EXPENSES table", ex);
        }
    }

    private void saveExpense() {
        String name = m_jName.getText().trim();
        String amountStr = m_jAmount.getText().trim();
        String notes = m_jNotes.getText().trim();

        if (name.isEmpty() || amountStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor complete nombre y monto.", "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            double amount = Double.parseDouble(amountStr.replace(",", "."));
            try (Connection con = m_App.getSession().getConnection();
                    PreparedStatement ps = con.prepareStatement(
                            "INSERT INTO EXPENSES (ID, DATENEW, NAME, AMOUNT, RESPONSIBLE, NOTES) VALUES (?, ?, ?, ?, ?, ?)")) {
                ps.setString(1, UUID.randomUUID().toString());
                ps.setTimestamp(2, new Timestamp(new Date().getTime()));
                ps.setString(3, name);
                ps.setDouble(4, amount);
                ps.setString(5, ""); // Ya no usamos responsable, dejamos vacío para compatibilidad DB
                ps.setString(6, notes);
                ps.executeUpdate();

                m_jName.setText("");
                m_jAmount.setText("");
                m_jNotes.setText("");
                m_jName.requestFocus();

                refreshTable();
                JOptionPane.showMessageDialog(this, "Gasto registrado correctamente.");
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Monto inválido.", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error saving expense", ex);
            JOptionPane.showMessageDialog(this, "Error al guardar en la base de datos.", "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void refreshTable() {
        m_tableModel.setRowCount(0);
        double total = 0;

        try {
            Date dFrom = (Date) Formats.DATE.parseValue(m_jDateFrom.getText());
            Date dTo = (Date) Formats.DATE.parseValue(m_jDateTo.getText());

            // Set end of day for dTo
            java.util.Calendar cal = java.util.Calendar.getInstance();
            cal.setTime(dTo);
            cal.set(java.util.Calendar.HOUR_OF_DAY, 23);
            cal.set(java.util.Calendar.MINUTE, 59);
            cal.set(java.util.Calendar.SECOND, 59);
            dTo = cal.getTime();

            try (Connection con = m_App.getSession().getConnection();
                    PreparedStatement ps = con.prepareStatement(
                            "SELECT DATENEW, RESPONSIBLE, NAME, AMOUNT, NOTES, ID FROM EXPENSES WHERE DATENEW >= ? AND DATENEW <= ? ORDER BY DATENEW DESC")) {
                ps.setTimestamp(1, new Timestamp(dFrom.getTime()));
                ps.setTimestamp(2, new Timestamp(dTo.getTime()));

                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    Timestamp ts = rs.getTimestamp(1);
                    String name = rs.getString(3);
                    double amount = rs.getDouble(4);
                    String notes = rs.getString(5);
                    String id = rs.getString(6);

                    total += amount;

                    m_tableModel.addRow(new Object[] {
                            Formats.TIMESTAMP.formatValue(ts),
                            name,
                            Formats.CURRENCY.formatValue(amount),
                            notes,
                            id
                    });
                }
            }
        } catch (BasicException | SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error refreshing table", ex);
        }

        m_lblTotal.setText("TOTAL GASTOS: " + Formats.CURRENCY.formatValue(total) + "  ");
    }
}

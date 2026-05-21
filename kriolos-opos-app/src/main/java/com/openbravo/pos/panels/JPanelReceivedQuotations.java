package com.openbravo.pos.panels;

import com.openbravo.basic.BasicException;
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.pos.forms.AppView;
import com.openbravo.pos.forms.BeanFactoryApp;
import com.openbravo.pos.forms.BeanFactoryException;
import com.openbravo.pos.forms.JPanelView;
import com.openbravo.format.Formats;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;
import java.io.IOException;
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
import javax.swing.JFileChooser;
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
 * Panel para la gestión de cotizaciones recibidas.
 * Permite adjuntar documentos y asignarles un título.
 */
public class JPanelReceivedQuotations extends JPanel implements JPanelView, BeanFactoryApp {

    private static final Logger LOGGER = Logger.getLogger(JPanelReceivedQuotations.class.getName());
    private AppView m_App;

    private JTextField m_jTitle;
    private JLabel m_lblFileName;
    private File m_selectedFile;

    private JTable m_table;
    private DefaultTableModel m_tableModel;

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
        return "Cotizaciones Recibidas";
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
        header.setBackground(new Color(0, 121, 107)); // Teal Dark
        header.setPreferredSize(new Dimension(0, 50));
        JLabel title = new JLabel("  COTIZACIONES RECIBIDAS (ORGANIZACIÓN)");
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
                new Object[] { "Fecha", "Título del Documento", "Nombre del Archivo", "ID" }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        m_table = new JTable(m_tableModel);
        m_table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        m_table.setRowHeight(35); // Un poco más alto
        m_table.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        
        // Abrir con doble clic
        m_table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    viewDocument();
                }
            }
        });
        
        // Hide ID column
        m_table.getColumnModel().getColumn(3).setMinWidth(0);
        m_table.getColumnModel().getColumn(3).setMaxWidth(0);
        m_table.getColumnModel().getColumn(3).setWidth(0);

        mainContent.add(new JScrollPane(m_table), BorderLayout.CENTER);

        // --- BOTTOM: FORM ---
        JPanel bottomPanel = new JPanel(new BorderLayout(10, 10));
        bottomPanel.setOpaque(false);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(new Color(245, 245, 245));
        formPanel.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                new EmptyBorder(15, 15, 15, 15)));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Título
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Título:"), gbc);
        
        gbc.gridx = 1; gbc.weightx = 1.0;
        m_jTitle = new JTextField();
        m_jTitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        formPanel.add(m_jTitle, gbc);

        // Archivo
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.0;
        formPanel.add(new JLabel("Archivo:"), gbc);

        gbc.gridx = 1; gbc.weightx = 1.0;
        JPanel fileSelectionPanel = new JPanel(new BorderLayout(5, 0));
        fileSelectionPanel.setOpaque(false);
        m_lblFileName = new JLabel("Ningún archivo seleccionado");
        m_lblFileName.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        fileSelectionPanel.add(m_lblFileName, BorderLayout.CENTER);
        
        JButton btnChoose = new JButton("Seleccionar Archivo...");
        btnChoose.addActionListener(e -> chooseFile());
        fileSelectionPanel.add(btnChoose, BorderLayout.EAST);
        formPanel.add(fileSelectionPanel, gbc);

        // Botones de Acción
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actionPanel.setOpaque(false);
        
        JButton btnAdd = new JButton("GUARDAR COTIZACIÓN");
        btnAdd.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnAdd.setBackground(new Color(0, 121, 107));
        btnAdd.setForeground(Color.WHITE);
        btnAdd.setPreferredSize(new Dimension(180, 40));
        btnAdd.addActionListener(e -> saveQuotation());
        
        JButton btnView = new JButton("ABRIR ARCHIVO");
        btnView.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnView.setBackground(new Color(25, 118, 210)); // Blue
        btnView.setForeground(Color.WHITE);
        btnView.setPreferredSize(new Dimension(150, 40));
        btnView.addActionListener(e -> viewDocument());

        JButton btnDelete = new JButton("ELIMINAR");
        btnDelete.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnDelete.setBackground(new Color(211, 47, 47));
        btnDelete.setForeground(Color.WHITE);
        btnDelete.setPreferredSize(new Dimension(100, 40));
        btnDelete.addActionListener(e -> deleteQuotation());

        actionPanel.add(btnView);
        actionPanel.add(btnDelete);
        actionPanel.add(btnAdd);

        bottomPanel.add(formPanel, BorderLayout.CENTER);
        bottomPanel.add(actionPanel, BorderLayout.SOUTH);

        mainContent.add(bottomPanel, BorderLayout.SOUTH);
    }

    private void chooseFile() {
        JFileChooser chooser = new JFileChooser();
        int returnVal = chooser.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            m_selectedFile = chooser.getSelectedFile();
            m_lblFileName.setText(m_selectedFile.getName());
            m_lblFileName.setFont(new Font("Segoe UI", Font.BOLD, 12));
        }
    }

    private void saveQuotation() {
        String title = m_jTitle.getText().trim();
        if (title.isEmpty() || m_selectedFile == null) {
            JOptionPane.showMessageDialog(this, "Por favor ingrese un título y seleccione un archivo.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try (Connection con = m_App.getSession().getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "INSERT INTO RECEIVED_QUOTATIONS (ID, DATENEW, TITLE, FILE_NAME, FILE_DATA) VALUES (?, ?, ?, ?, ?)")) {
            
            ps.setString(1, UUID.randomUUID().toString());
            ps.setTimestamp(2, new Timestamp(new Date().getTime()));
            ps.setString(3, title);
            ps.setString(4, m_selectedFile.getName());
            
            // Read file into byte array using NIO for robustness
            byte[] fileBytes = java.nio.file.Files.readAllBytes(m_selectedFile.toPath());
            ps.setBytes(5, fileBytes);
            
            ps.executeUpdate();

            m_jTitle.setText("");
            m_selectedFile = null;
            m_lblFileName.setText("Ningún archivo seleccionado");
            m_lblFileName.setFont(new Font("Segoe UI", Font.ITALIC, 12));
            
            refreshTable();
            JOptionPane.showMessageDialog(this, "Cotización guardada correctamente.");
            
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Error saving received quotation", ex);
            JOptionPane.showMessageDialog(this, "Error al guardar: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void viewDocument() {
        int row = m_table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Seleccione una cotización de la tabla.");
            return;
        }

        String id = (String) m_tableModel.getValueAt(row, 3);
        String fileName = (String) m_tableModel.getValueAt(row, 2);

        try (Connection con = m_App.getSession().getConnection();
             PreparedStatement ps = con.prepareStatement("SELECT FILE_DATA FROM RECEIVED_QUOTATIONS WHERE ID = ?")) {
            
            ps.setString(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                byte[] fileBytes = rs.getBytes(1);
                if (fileBytes == null || fileBytes.length == 0) {
                    throw new Exception("El archivo está vacío o no se encontró en la base de datos.");
                }
                
                // Create a temporary directory to avoid naming conflicts
                File tempDir = new File(System.getProperty("java.io.tmpdir"), "kriolos_attachments");
                if (!tempDir.exists()) {
                    tempDir.mkdirs();
                }
                
                // Ensure filename is safe and unique
                File tempFile = new File(tempDir, UUID.randomUUID().toString().substring(0, 8) + "_" + fileName);
                java.nio.file.Files.write(tempFile.toPath(), fileBytes);
                
                // Open file with default system application
                if (System.getProperty("os.name").toLowerCase().contains("win")) {
                    // Start process on Windows
                    Runtime.getRuntime().exec(new String[]{"cmd", "/c", "start", "", tempFile.getAbsolutePath()});
                } else if (java.awt.Desktop.isDesktopSupported()) {
                    java.awt.Desktop desktop = java.awt.Desktop.getDesktop();
                    if (desktop.isSupported(java.awt.Desktop.Action.OPEN)) {
                        desktop.open(tempFile);
                    } else {
                        JOptionPane.showMessageDialog(this, "Documento exportado a: " + tempFile.getAbsolutePath());
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "Documento exportado a: " + tempFile.getAbsolutePath());
                }
            }
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Error viewing document", ex);
            JOptionPane.showMessageDialog(this, "Error al intentar abrir el archivo.\n" +
                    "Asegúrese de tener un programa para abrir este tipo de archivo.\n" +
                    "Ruta: " + ex.getMessage(), "Error de Apertura", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteQuotation() {
        int row = m_table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Seleccione un registro para eliminar.");
            return;
        }

        String id = (String) m_tableModel.getValueAt(row, 3);
        int confirm = JOptionPane.showConfirmDialog(this, "¿Está seguro de eliminar este registro?", "Confirmar", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        try (Connection con = m_App.getSession().getConnection();
             PreparedStatement ps = con.prepareStatement("DELETE FROM RECEIVED_QUOTATIONS WHERE ID = ?")) {
            ps.setString(1, id);
            ps.executeUpdate();
            refreshTable();
            JOptionPane.showMessageDialog(this, "Registro eliminado.");
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error deleting quotation", ex);
            JOptionPane.showMessageDialog(this, "Error al eliminar.");
        }
    }

    private void createTableIfNotExist() {
        try (Connection con = m_App.getSession().getConnection();
             Statement stmt = con.createStatement()) {
            // Using LONGVARBINARY for the file data to support most DBs (Derby, MySQL, HSQL)
            String sql = "CREATE TABLE IF NOT EXISTS RECEIVED_QUOTATIONS (" +
                    "ID VARCHAR(255) PRIMARY KEY, " +
                    "DATENEW TIMESTAMP, " +
                    "TITLE VARCHAR(255), " +
                    "FILE_NAME VARCHAR(255), " +
                    "FILE_DATA LONGVARBINARY" +
                    ")";
            stmt.executeUpdate(sql);
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error creating RECEIVED_QUOTATIONS table", ex);
        }
    }

    private void refreshTable() {
        m_tableModel.setRowCount(0);
        try (Connection con = m_App.getSession().getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT DATENEW, TITLE, FILE_NAME, ID FROM RECEIVED_QUOTATIONS ORDER BY DATENEW DESC")) {
            
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                m_tableModel.addRow(new Object[] {
                        Formats.TIMESTAMP.formatValue(rs.getTimestamp(1)),
                        rs.getString(2),
                        rs.getString(3),
                        rs.getString(4)
                });
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error refreshing table", ex);
        }
    }

    @Override
    public String toString() {
        return "Cotizaciones Recibidas";
    }
}

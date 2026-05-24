package com.openbravo.pos.suppliers;

import com.openbravo.basic.BasicException;
import com.openbravo.format.Formats;
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.pos.forms.AppView;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.sql.*;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

public class JPanelSupplierDocuments extends JPanel {

    private static final Logger LOGGER = Logger.getLogger(JPanelSupplierDocuments.class.getName());
    private static final Color BLUE_COLOR = new Color(26, 115, 232);
    private static final Color RED_COLOR = new Color(234, 67, 53);
    private static final Color BG_COLOR = new Color(248, 249, 250);

    private final AppView m_App;
    private String m_supplierId;
    
    private JTable m_table;
    private DefaultTableModel m_tableModel;
    private JButton btnUpload;
    private JButton btnDownload;
    private JButton btnDelete;
    private JLabel lblStatus;

    public JPanelSupplierDocuments(AppView app) {
        this.m_App = app;
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setBackground(BG_COLOR);
        setBorder(new EmptyBorder(10, 10, 10, 10));

        // Header and Toolbar Panel
        JPanel topPanel = new JPanel(new BorderLayout(10, 0));
        topPanel.setOpaque(false);
        topPanel.setBorder(new EmptyBorder(0, 0, 10, 0));

        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setOpaque(false);

        lblStatus = new JLabel("Seleccione un proveedor para gestionar sus documentos");
        lblStatus.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblStatus.setForeground(new Color(95, 99, 104));
        titlePanel.add(lblStatus);

        JLabel lblSubtitle = new JLabel("Formatos admitidos: PDF, Imágenes, Hojas de cálculo, Documentos de texto.");
        lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSubtitle.setForeground(new Color(128, 134, 139));
        titlePanel.add(lblSubtitle);

        topPanel.add(titlePanel, BorderLayout.WEST);

        // Action Buttons Toolbar
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        toolbar.setOpaque(false);

        btnUpload = new JButton("Subir Archivo");
        btnUpload.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnUpload.setBackground(BLUE_COLOR);
        btnUpload.setForeground(Color.WHITE);
        btnUpload.setFocusPainted(false);
        btnUpload.setEnabled(false);
        btnUpload.addActionListener(e -> showUploadDialog());
        toolbar.add(btnUpload);

        btnDownload = new JButton("Descargar");
        btnDownload.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnDownload.setBackground(new Color(241, 243, 244));
        btnDownload.setForeground(new Color(60, 64, 67));
        btnDownload.setFocusPainted(false);
        btnDownload.setEnabled(false);
        btnDownload.addActionListener(e -> viewDocument());
        toolbar.add(btnDownload);

        btnDelete = new JButton("Eliminar");
        btnDelete.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnDelete.setBackground(new Color(241, 243, 244));
        btnDelete.setForeground(RED_COLOR);
        btnDelete.setFocusPainted(false);
        btnDelete.setEnabled(false);
        btnDelete.addActionListener(e -> deleteDocument());
        toolbar.add(btnDelete);

        topPanel.add(toolbar, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // Table definition
        m_tableModel = new DefaultTableModel(new Object[]{"Nombre", "Fecha de Subida", "Tamaño", "ID", "FileName"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        m_table = new JTable(m_tableModel);
        m_table.setRowHeight(48);
        m_table.setShowGrid(false);
        m_table.setIntercellSpacing(new Dimension(0, 0));
        m_table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        m_table.setForeground(new Color(60, 64, 67));
        m_table.setSelectionBackground(new Color(232, 240, 254));
        m_table.setSelectionForeground(new Color(32, 33, 36));
        m_table.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JTableHeader th = m_table.getTableHeader();
        th.setFont(new Font("Segoe UI", Font.BOLD, 13));
        th.setForeground(new Color(95, 99, 104));
        th.setBackground(Color.WHITE);
        th.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(218, 220, 224)));
        th.setPreferredSize(new Dimension(0, 36));
        ((DefaultTableCellRenderer) th.getDefaultRenderer()).setHorizontalAlignment(JLabel.LEFT);

        m_table.getColumnModel().getColumn(0).setPreferredWidth(320); // Nombre
        m_table.getColumnModel().getColumn(1).setPreferredWidth(160); // Fecha
        m_table.getColumnModel().getColumn(2).setPreferredWidth(100); // Tamaño

        // Hide ID and FileName
        m_table.getColumnModel().getColumn(3).setMinWidth(0);
        m_table.getColumnModel().getColumn(3).setMaxWidth(0);
        m_table.getColumnModel().getColumn(4).setMinWidth(0);
        m_table.getColumnModel().getColumn(4).setMaxWidth(0);

        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JPanel cell = new JPanel(new BorderLayout());
                cell.setOpaque(true);
                cell.setBackground(isSelected ? new Color(232, 240, 254) : Color.WHITE);
                cell.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(240, 240, 240)));

                if (column == 0) { // Nombre with Icon
                    JPanel leftP = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8));
                    leftP.setOpaque(false);
                    String fileName = (String) m_tableModel.getValueAt(row, 4);
                    JLabel iconLbl = new JLabel(getFileIcon(fileName));
                    leftP.add(iconLbl);

                    JPanel textP = new JPanel();
                    textP.setLayout(new BoxLayout(textP, BoxLayout.Y_AXIS));
                    textP.setOpaque(false);

                    JLabel nameLbl = new JLabel((String) value);
                    nameLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
                    nameLbl.setForeground(new Color(32, 33, 36));
                    textP.add(nameLbl);

                    JLabel fileLbl = new JLabel(fileName);
                    fileLbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                    fileLbl.setForeground(new Color(128, 134, 139));
                    textP.add(fileLbl);

                    leftP.add(textP);
                    cell.add(leftP, BorderLayout.CENTER);
                } else {
                    JLabel lbl = new JLabel(value != null ? value.toString() : "");
                    lbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                    lbl.setForeground(new Color(95, 99, 104));
                    lbl.setBorder(new EmptyBorder(0, 12, 0, 0));
                    cell.add(lbl, BorderLayout.CENTER);
                }

                return cell;
            }
        };

        for (int i = 0; i < 3; i++) {
            m_table.getColumnModel().getColumn(i).setCellRenderer(renderer);
        }

        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem itemOpen = new JMenuItem("Abrir / Descargar");
        itemOpen.addActionListener(e -> viewDocument());
        JMenuItem itemDelete = new JMenuItem("Eliminar");
        itemDelete.addActionListener(e -> deleteDocument());
        popupMenu.add(itemOpen);
        popupMenu.addSeparator();
        popupMenu.add(itemDelete);
        m_table.setComponentPopupMenu(popupMenu);

        m_table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)) {
                    viewDocument();
                }
            }
        });

        JScrollPane tableScroll = new JScrollPane(m_table);
        tableScroll.setBorder(BorderFactory.createLineBorder(new Color(218, 220, 224)));
        tableScroll.getViewport().setBackground(Color.WHITE);
        add(tableScroll, BorderLayout.CENTER);
    }

    public void setSupplierId(String supplierId) {
        this.m_supplierId = supplierId;
        if (m_supplierId == null) {
            lblStatus.setText("Seleccione un proveedor para gestionar sus documentos");
            btnUpload.setEnabled(false);
            btnDownload.setEnabled(false);
            btnDelete.setEnabled(false);
            m_tableModel.setRowCount(0);
        } else {
            lblStatus.setText("Documentos del proveedor seleccionado");
            btnUpload.setEnabled(true);
            btnDownload.setEnabled(true);
            btnDelete.setEnabled(true);
            refreshTable();
        }
    }

    private void refreshTable() {
        m_tableModel.setRowCount(0);
        if (m_supplierId == null) {
            return;
        }

        String sql = "SELECT ID, DATENEW, TITLE, FILE_NAME, OCTET_LENGTH(FILE_DATA) " +
                     "FROM SUPPLIER_DOCUMENTS WHERE SUPPLIER_ID = ? ORDER BY DATENEW DESC";

        try (Connection con = m_App.getSession().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, m_supplierId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String id = rs.getString(1);
                Timestamp date = rs.getTimestamp(2);
                String title = rs.getString(3);
                String fileName = rs.getString(4);
                long sizeBytes = rs.getLong(5);

                m_tableModel.addRow(new Object[]{
                    title,
                    Formats.TIMESTAMP.formatValue(date),
                    formatFileSize(sizeBytes),
                    id,
                    fileName
                });
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error loading supplier documents", ex);
        }
    }

    private void showUploadDialog() {
        if (m_supplierId == null) {
            return;
        }

        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), "Subir Documento", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(460, 240);
        dialog.setLocationRelativeTo(this);

        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(Color.WHITE);
        p.setBorder(new EmptyBorder(15, 20, 15, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        Font labelFont = new Font("Segoe UI", Font.BOLD, 13);
        Font inputFont = new Font("Segoe UI", Font.PLAIN, 14);

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        JLabel lblTitle = new JLabel("Título:");
        lblTitle.setFont(labelFont);
        p.add(lblTitle, gbc);
        
        gbc.gridx = 1; gbc.weightx = 1.0;
        JTextField txtTitle = new JTextField();
        txtTitle.setFont(inputFont);
        txtTitle.setPreferredSize(new Dimension(0, 32));
        p.add(txtTitle, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        JLabel lblFile = new JLabel("Archivo:");
        lblFile.setFont(labelFont);
        p.add(lblFile, gbc);
        
        gbc.gridx = 1; gbc.weightx = 1.0;
        JPanel fileP = new JPanel(new BorderLayout(10, 0));
        fileP.setOpaque(false);
        JLabel lblFileName = new JLabel("Ningún archivo seleccionado");
        lblFileName.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        fileP.add(lblFileName, BorderLayout.CENTER);

        JButton btnBrowse = new JButton("Examinar...");
        btnBrowse.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnBrowse.setBackground(new Color(241, 243, 244));
        btnBrowse.setFocusPainted(false);
        
        final File[] selectedFile = new File[1];
        btnBrowse.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            if (chooser.showOpenDialog(dialog) == JFileChooser.APPROVE_OPTION) {
                selectedFile[0] = chooser.getSelectedFile();
                lblFileName.setText(selectedFile[0].getName());
                if (txtTitle.getText().trim().isEmpty()) {
                    // Prepopulate title with filename (without extension)
                    String name = selectedFile[0].getName();
                    int lastDot = name.lastIndexOf('.');
                    if (lastDot > 0) {
                        name = name.substring(0, lastDot);
                    }
                    txtTitle.setText(name);
                }
            }
        });
        fileP.add(btnBrowse, BorderLayout.EAST);
        p.add(fileP, gbc);

        dialog.add(p, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        btnPanel.setBackground(new Color(248, 249, 250));
        btnPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(218, 220, 224)));

        JButton btnCancel = new JButton("Cancelar");
        btnCancel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnCancel.setForeground(new Color(95, 99, 104));
        btnCancel.setBackground(Color.WHITE);
        btnCancel.setFocusPainted(false);
        btnCancel.addActionListener(e -> dialog.dispose());

        JButton btnSave = new JButton("Subir");
        btnSave.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnSave.setBackground(BLUE_COLOR);
        btnSave.setForeground(Color.WHITE);
        btnSave.setFocusPainted(false);
        btnSave.addActionListener(e -> {
            String title = txtTitle.getText().trim();
            if (title.isEmpty() || selectedFile[0] == null) {
                JOptionPane.showMessageDialog(dialog, "Título y archivo requeridos.", "Subir Documento", JOptionPane.WARNING_MESSAGE);
                return;
            }
            saveDocumentToDB(title, selectedFile[0]);
            dialog.dispose();
            refreshTable();
        });

        btnPanel.add(btnCancel);
        btnPanel.add(btnSave);
        dialog.add(btnPanel, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }

    private void saveDocumentToDB(String title, File file) {
        String sql = "INSERT INTO SUPPLIER_DOCUMENTS (ID, DATENEW, SUPPLIER_ID, TITLE, FILE_NAME, FILE_DATA) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection con = m_App.getSession().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, UUID.randomUUID().toString());
            ps.setTimestamp(2, new Timestamp(new java.util.Date().getTime()));
            ps.setString(3, m_supplierId);
            ps.setString(4, title);
            ps.setString(5, file.getName());
            ps.setBytes(6, java.nio.file.Files.readAllBytes(file.toPath()));
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Documento subido correctamente.", "Documentos", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Error saving supplier document", ex);
            JOptionPane.showMessageDialog(this, "Error al guardar el archivo: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void viewDocument() {
        int row = m_table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Seleccione un documento para descargar/abrir.", "Documentos", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String id = (String) m_tableModel.getValueAt(row, 3);
        String fileName = (String) m_tableModel.getValueAt(row, 4);

        try (Connection con = m_App.getSession().getConnection();
             PreparedStatement ps = con.prepareStatement("SELECT FILE_DATA FROM SUPPLIER_DOCUMENTS WHERE ID = ?")) {
            ps.setString(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                byte[] data = rs.getBytes(1);
                File tempDir = new File(System.getProperty("java.io.tmpdir"), "kriolos_suppliers");
                tempDir.mkdirs();
                File tempFile = new File(tempDir, UUID.randomUUID().toString().substring(0, 8) + "_" + fileName);
                java.nio.file.Files.write(tempFile.toPath(), data);

                if (System.getProperty("os.name").toLowerCase().contains("win")) {
                    Runtime.getRuntime().exec(new String[]{"cmd", "/c", "start", "", tempFile.getAbsolutePath()});
                } else if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().open(tempFile);
                } else {
                    JOptionPane.showMessageDialog(this, "Archivo guardado temporalmente en:\n" + tempFile.getAbsolutePath(), "Descarga exitosa", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Error viewing document", ex);
            JOptionPane.showMessageDialog(this, "Error al descargar el archivo: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteDocument() {
        int row = m_table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Seleccione un documento para eliminar.", "Documentos", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (JOptionPane.showConfirmDialog(this, "¿Está seguro de que desea eliminar permanentemente este archivo?", "Confirmación de eliminación", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION) {
            return;
        }

        String id = (String) m_tableModel.getValueAt(row, 3);
        try (Connection con = m_App.getSession().getConnection();
             PreparedStatement ps = con.prepareStatement("DELETE FROM SUPPLIER_DOCUMENTS WHERE ID = ?")) {
            ps.setString(1, id);
            ps.executeUpdate();
            refreshTable();
            JOptionPane.showMessageDialog(this, "Documento eliminado correctamente.", "Documentos", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error deleting document", ex);
            JOptionPane.showMessageDialog(this, "Error al eliminar el archivo: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String formatFileSize(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        }
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp - 1) + "";
        return String.format("%.1f %sB", bytes / Math.pow(1024, exp), pre);
    }

    private Icon getFileIcon(String fileName) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                String ext = "";
                if (fileName != null) {
                    int idx = fileName.lastIndexOf('.');
                    if (idx > 0) {
                        ext = fileName.substring(idx + 1).toLowerCase();
                    }
                }

                Color bg = new Color(66, 133, 244); // default blue (PDF, DOCX, TXT, etc)
                if (ext.equals("pdf")) {
                    bg = RED_COLOR;
                } else if (ext.equals("xls") || ext.equals("xlsx") || ext.equals("csv")) {
                    bg = new Color(52, 168, 83); // green
                } else if (ext.equals("jpg") || ext.equals("png") || ext.equals("jpeg") || ext.equals("gif")) {
                    bg = new Color(251, 188, 5); // yellow/orange
                } else if (ext.equals("zip") || ext.equals("rar") || ext.equals("7z")) {
                    bg = new Color(156, 39, 176); // purple
                } else if (ext.equals("txt") || ext.equals("xml") || ext.equals("json") || ext.equals("html")) {
                    bg = new Color(108, 117, 125); // gray
                }

                g2.setColor(bg);
                g2.fillRoundRect(x, y, 32, 32, 6, 6);

                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
                FontMetrics fm = g2.getFontMetrics();
                String text = ext.length() > 3 ? ext.substring(0, 3).toUpperCase() : ext.toUpperCase();
                if (text.isEmpty()) {
                    text = "FILE";
                }
                int tx = x + (32 - fm.stringWidth(text)) / 2;
                int ty = y + ((32 - fm.getHeight()) / 2) + fm.getAscent();
                g2.drawString(text, tx, ty);

                // Add dog-ear fold
                g2.setColor(new Color(255, 255, 255, 100));
                Polygon fold = new Polygon();
                fold.addPoint(x + 32, y);
                fold.addPoint(x + 32 - 8, y);
                fold.addPoint(x + 32, y + 8);
                g2.fillPolygon(fold);

                g2.dispose();
            }

            @Override
            public int getIconWidth() {
                return 32;
            }

            @Override
            public int getIconHeight() {
                return 32;
            }
        };
    }
}

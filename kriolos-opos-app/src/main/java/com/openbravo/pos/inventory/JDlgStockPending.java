package com.openbravo.pos.inventory;

import com.openbravo.basic.BasicException;
import com.openbravo.format.Formats;
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.pos.forms.AppView;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
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
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;

/**
 * Diálogo para la gestión de stock pendiente por recibir de proveedores
 * Diseño premium con estilo moderno
 */
public class JDlgStockPending extends JDialog {

    private static final Logger LOGGER = Logger.getLogger(JDlgStockPending.class.getName());
    private AppView m_App;

    // --- Paleta de colores premium ---
    private static final Color BG_PRIMARY    = new Color(248, 250, 252);   // slate-50
    private static final Color BG_CARD       = Color.WHITE;
    private static final Color BORDER_COLOR  = new Color(226, 232, 240);   // slate-200
    private static final Color TEXT_PRIMARY   = new Color(15, 23, 42);     // slate-900
    private static final Color TEXT_SECONDARY = new Color(100, 116, 139);  // slate-500
    private static final Color TEXT_MUTED     = new Color(148, 163, 184);  // slate-400

    private static final Color ACCENT_TEAL     = new Color(13, 148, 136);  // teal-600
    private static final Color ACCENT_TEAL_LT  = new Color(20, 184, 166); // teal-500
    private static final Color ACCENT_TEAL_BG  = new Color(240, 253, 250);// teal-50

    private static final Color DANGER_COLOR    = new Color(220, 38, 38);   // red-600
    private static final Color DANGER_BG       = new Color(254, 242, 242); // red-50

    private static final Color TABLE_HEADER_BG = new Color(241, 245, 249); // slate-100
    private static final Color TABLE_ALT_ROW   = new Color(248, 250, 252); // slate-50
    private static final Color TABLE_HOVER     = new Color(240, 253, 250); // teal-50
    private static final Color TABLE_SELECTED  = new Color(204, 251, 241); // teal-100

    // Status colors
    private static final Color STATUS_PENDING   = new Color(245, 158, 11);  // amber-500
    private static final Color STATUS_TRANSIT    = new Color(59, 130, 246); // blue-500
    private static final Color STATUS_ORDERED    = new Color(139, 92, 246); // violet-500
    private static final Color STATUS_PARTIAL    = new Color(20, 184, 166); // teal-500
    private static final Color STATUS_COMPLETE   = new Color(34, 197, 94); // green-500
    private static final Color STATUS_CANCELLED  = new Color(156, 163, 175);// gray-400

    private JTextField m_jProductName;
    private JTextField m_jSupplierName;
    private JTextField m_jDocTitle;
    private JComboBox<String> m_jStatus;
    private JTextField m_jNotes;

    private JTable m_table;
    private DefaultTableModel m_tableModel;
    private JLabel m_lblCount;

    private JLabel m_lblFileName;
    private File m_selectedFile;
    private byte[] m_loadedFileBytes;
    private String m_loadedFileName;
    private String m_editingId = null;

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
        setSize(1050, 720);
        setLocationRelativeTo(null);
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
        setLayout(new BorderLayout());
        getContentPane().setBackground(BG_PRIMARY);

        // --- HEADER con gradiente ---
        JPanel header = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, ACCENT_TEAL, getWidth(), 0, ACCENT_TEAL_LT);
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        header.setPreferredSize(new Dimension(0, 60));
        header.setBorder(new EmptyBorder(0, 24, 0, 24));

        JLabel iconLabel = new JLabel("📦  ");
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 22));
        iconLabel.setForeground(Color.WHITE);

        JLabel title = new JLabel("Productos Pendientes por Recibir");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(Color.WHITE);

        JPanel titleRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        titleRow.setOpaque(false);
        titleRow.add(iconLabel);
        titleRow.add(title);
        header.add(titleRow, BorderLayout.WEST);

        // Badge contador
        m_lblCount = new JLabel("0 registros") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255, 255, 255, 40));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        m_lblCount.setFont(new Font("Segoe UI", Font.BOLD, 13));
        m_lblCount.setForeground(Color.WHITE);
        m_lblCount.setHorizontalAlignment(SwingConstants.CENTER);
        m_lblCount.setBorder(new EmptyBorder(6, 16, 6, 16));
        JPanel countWrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        countWrapper.setOpaque(false);
        countWrapper.setBorder(new EmptyBorder(12, 0, 12, 0));
        countWrapper.add(m_lblCount);
        header.add(countWrapper, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);

        // --- CONTENIDO PRINCIPAL ---
        JPanel mainContent = new JPanel(new BorderLayout(0, 16));
        mainContent.setBackground(BG_PRIMARY);
        mainContent.setBorder(new EmptyBorder(20, 24, 20, 24));
        add(mainContent, BorderLayout.CENTER);

        // === TABLA con card ===
        JPanel tableCard = createCardPanel();
        tableCard.setLayout(new BorderLayout());

        m_tableModel = new DefaultTableModel(
                new Object[]{"Fecha Registro", "Producto", "Proveedor", "Documento / Título", "Estado", "ID"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        m_table = new JTable(m_tableModel) {
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                if (isRowSelected(row)) {
                    c.setBackground(TABLE_SELECTED);
                    c.setForeground(TEXT_PRIMARY);
                } else if (row % 2 == 0) {
                    c.setBackground(Color.WHITE);
                    c.setForeground(TEXT_PRIMARY);
                } else {
                    c.setBackground(TABLE_ALT_ROW);
                    c.setForeground(TEXT_PRIMARY);
                }
                return c;
            }
        };
        m_table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        m_table.setRowHeight(38);
        m_table.setShowHorizontalLines(true);
        m_table.setShowVerticalLines(false);
        m_table.setGridColor(BORDER_COLOR);
        m_table.setSelectionBackground(TABLE_SELECTED);
        m_table.setSelectionForeground(TEXT_PRIMARY);
        m_table.setIntercellSpacing(new Dimension(0, 1));
        m_table.setFocusable(true);
        m_table.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);

        // Header estilizado
        JTableHeader tableHeader = m_table.getTableHeader();
        tableHeader.setFont(new Font("Segoe UI", Font.BOLD, 12));
        tableHeader.setBackground(TABLE_HEADER_BG);
        tableHeader.setForeground(TEXT_SECONDARY);
        tableHeader.setPreferredSize(new Dimension(0, 42));
        tableHeader.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, BORDER_COLOR));
        tableHeader.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                lbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
                lbl.setForeground(TEXT_SECONDARY);
                lbl.setBackground(TABLE_HEADER_BG);
                lbl.setBorder(new CompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 2, 0, BORDER_COLOR),
                    new EmptyBorder(0, 12, 0, 12)
                ));
                lbl.setHorizontalAlignment(SwingConstants.LEFT);
                return lbl;
            }
        });

        // Renderer para la columna Estado (badges de color)
        m_table.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel lbl = new JLabel(value != null ? value.toString() : "") {
                    @Override
                    protected void paintComponent(Graphics g) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        Color badgeColor = getStatusColor(getText());
                        // Fondo del badge
                        g2.setColor(new Color(badgeColor.getRed(), badgeColor.getGreen(), badgeColor.getBlue(), 25));
                        g2.fillRoundRect(4, 4, getWidth() - 8, getHeight() - 8, 16, 16);
                        g2.dispose();
                        super.paintComponent(g);
                    }
                };
                lbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
                Color statusColor = getStatusColor(value != null ? value.toString() : "");
                lbl.setForeground(statusColor);
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
                lbl.setOpaque(true);
                if (isSelected) {
                    lbl.setBackground(TABLE_SELECTED);
                } else {
                    lbl.setBackground(row % 2 == 0 ? Color.WHITE : TABLE_ALT_ROW);
                }
                return lbl;
            }
        });

        // Renderer genérico con padding
        DefaultTableCellRenderer paddedRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setBorder(new CompoundBorder(getBorder(), new EmptyBorder(0, 12, 0, 12)));
                return this;
            }
        };
        for (int i = 0; i < 4; i++) {
            m_table.getColumnModel().getColumn(i).setCellRenderer(paddedRenderer);
        }

        // Ocultar columna ID
        m_table.getColumnModel().getColumn(5).setMinWidth(0);
        m_table.getColumnModel().getColumn(5).setMaxWidth(0);
        m_table.getColumnModel().getColumn(5).setWidth(0);

        // Anchos de columnas
        m_table.getColumnModel().getColumn(0).setPreferredWidth(140);
        m_table.getColumnModel().getColumn(1).setPreferredWidth(200);
        m_table.getColumnModel().getColumn(2).setPreferredWidth(150);
        m_table.getColumnModel().getColumn(3).setPreferredWidth(250);
        m_table.getColumnModel().getColumn(4).setPreferredWidth(130);

        // Listener para doble clic (cargar en formulario)
        m_table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    loadSelectedToForm();
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(m_table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);
        tableCard.add(scrollPane, BorderLayout.CENTER);

        mainContent.add(tableCard, BorderLayout.CENTER);

        // === FORMULARIO con card ===
        JPanel formCard = createCardPanel();
        formCard.setLayout(new BorderLayout(0, 12));

        // Título del formulario
        JPanel formHeader = new JPanel(new BorderLayout());
        formHeader.setOpaque(false);
        JLabel formTitle = new JLabel("✏️  Nuevo Seguimiento / Editar");
        formTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        formTitle.setForeground(TEXT_PRIMARY);
        formHeader.add(formTitle, BorderLayout.WEST);

        JLabel formHint = new JLabel("Doble clic en la tabla para editar");
        formHint.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        formHint.setForeground(TEXT_MUTED);
        formHeader.add(formHint, BorderLayout.EAST);
        formCard.add(formHeader, BorderLayout.NORTH);

        // Campos del formulario
        JPanel fieldsPanel = new JPanel(new GridBagLayout());
        fieldsPanel.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        Font labelFont = new Font("Segoe UI", Font.BOLD, 11);
        Color labelColor = TEXT_SECONDARY;

        // Fila 1: Producto + Documento
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.0;
        JLabel lblProduct = new JLabel("PRODUCTO");
        lblProduct.setFont(labelFont);
        lblProduct.setForeground(labelColor);
        fieldsPanel.add(lblProduct, gbc);

        gbc.gridx = 1; gbc.weightx = 1.0;
        m_jProductName = createStyledTextField("Nombre del producto...");
        fieldsPanel.add(m_jProductName, gbc);

        gbc.gridx = 2; gbc.weightx = 0.0;
        JLabel lblDoc = new JLabel("DOCUMENTO / TÍTULO");
        lblDoc.setFont(labelFont);
        lblDoc.setForeground(labelColor);
        fieldsPanel.add(lblDoc, gbc);

        gbc.gridx = 3; gbc.weightx = 1.0;
        m_jDocTitle = createStyledTextField("Factura, remisión, pedido...");
        fieldsPanel.add(m_jDocTitle, gbc);

        // Fila 2: Proveedor + Estado
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.0;
        JLabel lblSupplier = new JLabel("PROVEEDOR");
        lblSupplier.setFont(labelFont);
        lblSupplier.setForeground(labelColor);
        fieldsPanel.add(lblSupplier, gbc);

        gbc.gridx = 1; gbc.weightx = 1.0;
        m_jSupplierName = createStyledTextField("Nombre del proveedor...");
        fieldsPanel.add(m_jSupplierName, gbc);

        gbc.gridx = 2; gbc.weightx = 0.0;
        JLabel lblStatus = new JLabel("ESTADO");
        lblStatus.setFont(labelFont);
        lblStatus.setForeground(labelColor);
        fieldsPanel.add(lblStatus, gbc);

        gbc.gridx = 3; gbc.weightx = 1.0;
        m_jStatus = new JComboBox<>(new String[]{
            "PENDIENTE", "EN TRÁNSITO", "PEDIDO REALIZADO",
            "RECIBIDO PARCIAL", "COMPLETADO", "CANCELADO"
        });
        m_jStatus.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        m_jStatus.setBackground(Color.WHITE);
        m_jStatus.setPreferredSize(new Dimension(0, 36));
        fieldsPanel.add(m_jStatus, gbc);

        // Fila 3: Notas (ancho completo)
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.0;
        JLabel lblNotes = new JLabel("NOTAS");
        lblNotes.setFont(labelFont);
        lblNotes.setForeground(labelColor);
        fieldsPanel.add(lblNotes, gbc);

        gbc.gridx = 1; gbc.gridwidth = 3; gbc.weightx = 1.0;
        m_jNotes = createStyledTextField("Observaciones adicionales...");
        fieldsPanel.add(m_jNotes, gbc);

        // Fila 4: Adjuntar Documento (ancho completo)
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 1; gbc.weightx = 0.0;
        JLabel lblAttachment = new JLabel("DOCUMENTO ADJUNTO");
        lblAttachment.setFont(labelFont);
        lblAttachment.setForeground(labelColor);
        fieldsPanel.add(lblAttachment, gbc);

        gbc.gridx = 1; gbc.gridwidth = 3; gbc.weightx = 1.0;
        JPanel attachmentPanel = new JPanel(new BorderLayout(8, 0));
        attachmentPanel.setOpaque(false);

        m_lblFileName = new JLabel("Ningún archivo seleccionado");
        m_lblFileName.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        m_lblFileName.setForeground(TEXT_SECONDARY);
        attachmentPanel.add(m_lblFileName, BorderLayout.CENTER);

        JPanel attachmentButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        attachmentButtons.setOpaque(false);

        JButton btnChoose = createStyledButton("Seleccionar...", ACCENT_TEAL, ACCENT_TEAL, Color.WHITE);
        btnChoose.setPreferredSize(new Dimension(120, 30));
        btnChoose.addActionListener(e -> chooseFile());

        JButton btnOpen = createStyledButton("Abrir Adjunto", new Color(59, 130, 246), new Color(59, 130, 246), Color.WHITE);
        btnOpen.setPreferredSize(new Dimension(120, 30));
        btnOpen.addActionListener(e -> viewDocument());

        JButton btnRemove = createStyledButton("Quitar", DANGER_COLOR, DANGER_COLOR, Color.WHITE);
        btnRemove.setPreferredSize(new Dimension(90, 30));
        btnRemove.addActionListener(e -> removeAttachment());

        attachmentButtons.add(btnChoose);
        attachmentButtons.add(btnOpen);
        attachmentButtons.add(btnRemove);
        attachmentPanel.add(attachmentButtons, BorderLayout.EAST);

        fieldsPanel.add(attachmentPanel, gbc);

        formCard.add(fieldsPanel, BorderLayout.CENTER);

        // Botones
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(new EmptyBorder(4, 0, 0, 0));

        JButton btnDelete = createStyledButton("Eliminar", DANGER_COLOR, DANGER_BG, Color.WHITE);
        btnDelete.addActionListener(e -> deleteSelected());

        JButton btnSave = createStyledButton("Guardar Registro", ACCENT_TEAL, ACCENT_TEAL, Color.WHITE);
        btnSave.addActionListener(e -> savePendingStock());

        JButton btnClose = createStyledButton("Cerrar", TEXT_SECONDARY, BG_PRIMARY, TEXT_PRIMARY);
        btnClose.addActionListener(e -> dispose());

        buttonPanel.add(btnDelete);
        buttonPanel.add(btnSave);
        buttonPanel.add(btnClose);
        formCard.add(buttonPanel, BorderLayout.SOUTH);

        mainContent.add(formCard, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(null);
    }

    // --- Helpers de UI ---

    private JPanel createCardPanel() {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Sombra sutil
                g2.setColor(new Color(0, 0, 0, 10));
                g2.fillRoundRect(2, 3, getWidth() - 4, getHeight() - 4, 12, 12);
                // Fondo
                g2.setColor(BG_CARD);
                g2.fillRoundRect(0, 0, getWidth() - 2, getHeight() - 3, 12, 12);
                // Borde
                g2.setColor(BORDER_COLOR);
                g2.drawRoundRect(0, 0, getWidth() - 3, getHeight() - 4, 12, 12);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(16, 16, 16, 16));
        return card;
    }

    private JTextField createStyledTextField(String placeholder) {
        JTextField field = new JTextField() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getText().isEmpty() && !hasFocus()) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                    g2.setColor(TEXT_MUTED);
                    g2.setFont(new Font("Segoe UI", Font.ITALIC, 12));
                    Insets insets = getInsets();
                    g2.drawString(placeholder, insets.left + 2, getHeight() / 2 + 4);
                    g2.dispose();
                }
            }
        };
        field.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        field.setPreferredSize(new Dimension(0, 36));
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            new EmptyBorder(4, 10, 4, 10)
        ));
        field.setBackground(Color.WHITE);
        field.setCaretColor(ACCENT_TEAL);

        // Focus effect
        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(ACCENT_TEAL, 2),
                    new EmptyBorder(3, 9, 3, 9)
                ));
                field.repaint();
            }
            @Override
            public void focusLost(FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(BORDER_COLOR, 1),
                    new EmptyBorder(4, 10, 4, 10)
                ));
                field.repaint();
            }
        });
        return field;
    }

    private JButton createStyledButton(String text, Color bgColor, Color bgAlt, Color fgColor) {
        JButton btn = new JButton(text) {
            private boolean hovered = false;
            {
                addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) { hovered = true; repaint(); }
                    @Override
                    public void mouseExited(MouseEvent e) { hovered = false; repaint(); }
                });
            }
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg = hovered ? bgColor.darker() : bgColor;
                if (bgColor.equals(bgAlt)) {
                    // Botón sólido (primary/danger)
                    g2.setColor(bg);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                } else {
                    // Botón outline/ghost
                    g2.setColor(hovered ? bgAlt : BG_CARD);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                    g2.setColor(BORDER_COLOR);
                    g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setForeground(fgColor);
        btn.setPreferredSize(new Dimension(160, 38));
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private Color getStatusColor(String status) {
        if (status == null) return TEXT_MUTED;
        switch (status.toUpperCase()) {
            case "PENDIENTE":        return STATUS_PENDING;
            case "EN TRÁNSITO":      return STATUS_TRANSIT;
            case "PEDIDO REALIZADO": return STATUS_ORDERED;
            case "RECIBIDO PARCIAL": return STATUS_PARTIAL;
            case "COMPLETADO":       return STATUS_COMPLETE;
            case "CANCELADO":        return STATUS_CANCELLED;
            default:                 return TEXT_MUTED;
        }
    }

    private void clearForm() {
        m_editingId = null;
        m_selectedFile = null;
        m_loadedFileName = null;
        m_loadedFileBytes = null;
        m_jProductName.setText("");
        m_jSupplierName.setText("");
        m_jDocTitle.setText("");
        m_jNotes.setText("");
        m_jStatus.setSelectedIndex(0);
        m_lblFileName.setText("Ningún archivo seleccionado");
        m_lblFileName.setFont(new Font("Segoe UI", Font.ITALIC, 12));
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

    private void viewDocument() {
        byte[] fileBytes = null;
        String fileName = null;

        if (m_selectedFile != null) {
            try {
                fileBytes = java.nio.file.Files.readAllBytes(m_selectedFile.toPath());
                fileName = m_selectedFile.getName();
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Error reading selected file", e);
            }
        } else {
            fileBytes = m_loadedFileBytes;
            fileName = m_loadedFileName;
        }

        if (fileBytes == null || fileBytes.length == 0 || fileName == null || fileName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No hay ningún documento adjunto para abrir.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            File tempDir = new File(System.getProperty("java.io.tmpdir"), "kriolos_attachments");
            if (!tempDir.exists()) {
                tempDir.mkdirs();
            }
            
            File tempFile = new File(tempDir, UUID.randomUUID().toString().substring(0, 8) + "_" + fileName);
            java.nio.file.Files.write(tempFile.toPath(), fileBytes);
            
            if (System.getProperty("os.name").toLowerCase().contains("win")) {
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
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Error viewing document", ex);
            JOptionPane.showMessageDialog(this, "Error al intentar abrir el archivo: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void removeAttachment() {
        m_selectedFile = null;
        m_loadedFileName = null;
        m_loadedFileBytes = null;
        m_lblFileName.setText("Ningún archivo seleccionado");
        m_lblFileName.setFont(new Font("Segoe UI", Font.ITALIC, 12));
    }

    private void loadSelectedToForm() {
        int row = m_table.getSelectedRow();
        if (row < 0) return;
        m_jProductName.setText(m_tableModel.getValueAt(row, 1) != null ? m_tableModel.getValueAt(row, 1).toString() : "");
        m_jSupplierName.setText(m_tableModel.getValueAt(row, 2) != null ? m_tableModel.getValueAt(row, 2).toString() : "");
        m_jDocTitle.setText(m_tableModel.getValueAt(row, 3) != null ? m_tableModel.getValueAt(row, 3).toString() : "");
        String status = m_tableModel.getValueAt(row, 4) != null ? m_tableModel.getValueAt(row, 4).toString() : "";
        m_jStatus.setSelectedItem(status);

        String id = (String) m_tableModel.getValueAt(row, 5);
        m_editingId = id;

        try (Connection con = m_App.getSession().getConnection();
             PreparedStatement ps = con.prepareStatement("SELECT NOTES, FILE_NAME, FILE_DATA FROM STOCK_PENDING WHERE ID = ?")) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    m_jNotes.setText(rs.getString(1) != null ? rs.getString(1) : "");
                    m_loadedFileName = rs.getString(2);
                    m_loadedFileBytes = rs.getBytes(3);
                    m_selectedFile = null;

                    if (m_loadedFileName != null && !m_loadedFileName.isEmpty()) {
                        m_lblFileName.setText(m_loadedFileName);
                        m_lblFileName.setFont(new Font("Segoe UI", Font.BOLD, 12));
                    } else {
                        m_lblFileName.setText("Ningún archivo seleccionado");
                        m_lblFileName.setFont(new Font("Segoe UI", Font.ITALIC, 12));
                    }
                }
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error loading details for selected pending stock", ex);
        }
    }

    // --- Lógica de negocio ---

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
                    "NOTES VARCHAR(1000), " +
                    "FILE_NAME VARCHAR(255), " +
                    "FILE_DATA LONGVARBINARY" +
                    ")";
            stmt.executeUpdate(sql);

            try {
                stmt.executeUpdate("ALTER TABLE STOCK_PENDING ADD COLUMN FILE_NAME VARCHAR(255)");
            } catch (SQLException e) {
                // Ignore if column already exists
            }
            try {
                stmt.executeUpdate("ALTER TABLE STOCK_PENDING ADD COLUMN FILE_DATA LONGVARBINARY");
            } catch (SQLException e) {
                // Ignore if column already exists
            }
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
            JOptionPane.showMessageDialog(this, "El nombre del producto es obligatorio.",
                "Validación", JOptionPane.WARNING_MESSAGE);
            m_jProductName.requestFocus();
            return;
        }

        byte[] fileBytes = null;
        String fileName = null;

        if (m_selectedFile != null) {
            try {
                fileBytes = java.nio.file.Files.readAllBytes(m_selectedFile.toPath());
                fileName = m_selectedFile.getName();
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Error reading selected file to save", e);
            }
        } else {
            fileBytes = m_loadedFileBytes;
            fileName = m_loadedFileName;
        }

        try (Connection con = m_App.getSession().getConnection()) {
            if (m_editingId == null) {
                // INSERT
                try (PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO STOCK_PENDING (ID, DATENEW, PRODUCT_NAME, SUPPLIER_NAME, DOC_TITLE, STATUS, NOTES, FILE_NAME, FILE_DATA) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
                    ps.setString(1, UUID.randomUUID().toString());
                    ps.setTimestamp(2, new Timestamp(new Date().getTime()));
                    ps.setString(3, product);
                    ps.setString(4, supplier);
                    ps.setString(5, doc);
                    ps.setString(6, status);
                    ps.setString(7, notes);
                    ps.setString(8, fileName);
                    if (fileBytes != null) {
                        ps.setBytes(9, fileBytes);
                    } else {
                        ps.setNull(9, java.sql.Types.LONGVARBINARY);
                    }
                    ps.executeUpdate();
                }
            } else {
                // UPDATE
                try (PreparedStatement ps = con.prepareStatement(
                        "UPDATE STOCK_PENDING SET PRODUCT_NAME = ?, SUPPLIER_NAME = ?, DOC_TITLE = ?, STATUS = ?, NOTES = ?, FILE_NAME = ?, FILE_DATA = ? WHERE ID = ?")) {
                    ps.setString(1, product);
                    ps.setString(2, supplier);
                    ps.setString(3, doc);
                    ps.setString(4, status);
                    ps.setString(5, notes);
                    ps.setString(6, fileName);
                    if (fileBytes != null) {
                        ps.setBytes(7, fileBytes);
                    } else {
                        ps.setNull(7, java.sql.Types.LONGVARBINARY);
                    }
                    ps.setString(8, m_editingId);
                    ps.executeUpdate();
                }
            }

            clearForm();
            m_jProductName.requestFocus();
            refreshTable();
            
            JOptionPane.showMessageDialog(this, "Registro de stock pendiente guardado correctamente.",
                "Éxito", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error saving pending stock", ex);
            JOptionPane.showMessageDialog(this, "Error al guardar registro.",
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void refreshTable() {
        m_tableModel.setRowCount(0);
        try (Connection con = m_App.getSession().getConnection();
                Statement stmt = con.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT DATENEW, PRODUCT_NAME, SUPPLIER_NAME, DOC_TITLE, STATUS, ID FROM STOCK_PENDING ORDER BY DATENEW DESC")) {
            while (rs.next()) {
                m_tableModel.addRow(new Object[]{
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

        // Actualizar contador
        int count = m_tableModel.getRowCount();
        m_lblCount.setText(count + (count == 1 ? " registro" : " registros"));
    }

    private void deleteSelected() {
        int row = m_table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Seleccione un registro para eliminar.",
                "Selección requerida", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String product = (String) m_tableModel.getValueAt(row, 1);
        String id = (String) m_tableModel.getValueAt(row, 5);
        int confirm = JOptionPane.showConfirmDialog(this,
                "¿Eliminar el registro de \"" + product + "\"?\nEsta acción no se puede deshacer.",
                "Confirmar eliminación", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;

        try (Connection con = m_App.getSession().getConnection();
                PreparedStatement ps = con.prepareStatement("DELETE FROM STOCK_PENDING WHERE ID = ?")) {
            ps.setString(1, id);
            ps.executeUpdate();
            clearForm();
            refreshTable();
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error deleting record", ex);
        }
    }
}

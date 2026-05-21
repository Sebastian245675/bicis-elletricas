package com.openbravo.pos.panels;

import com.openbravo.basic.BasicException;
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.pos.forms.AppView;
import com.openbravo.pos.forms.BeanFactoryApp;
import com.openbravo.pos.forms.BeanFactoryException;
import com.openbravo.pos.forms.JPanelView;
import com.openbravo.format.Formats;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.sql.*;
import java.util.UUID;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

public class JPanelDocuments extends JPanel implements JPanelView, BeanFactoryApp {

    private static final Logger LOGGER = Logger.getLogger(JPanelDocuments.class.getName());
    private AppView m_App;

    private JLayeredPane layeredPane;
    private JPanel mainContentPanel;
    
    private JPanel headerPanel;
    private JPanel foldersWrapper;
    private JPanel foldersContainer;
    private JLabel lblBreadcrumb;
    private JLabel lblHeaderSubtitle;
    
    // UI state
    private String m_currentFolder = "TODOS";
    private String m_searchQuery = "";
    
    private JTable m_table;
    private DefaultTableModel m_tableModel;
    private JTextField m_searchField;
    
    private final Color BG_COLOR = new Color(248, 249, 250); // #F8F9FA
    private final Color BLUE_COLOR = new Color(26, 115, 232); // #1A73E8

    @Override
    public void init(AppView app) throws BeanFactoryException {
        m_App = app;
        initComponents();
        createTableIfNotExist();
    }

    @Override
    public Object getBean() { return this; }
    @Override
    public JComponent getComponent() { return this; }
    @Override
    public String getTitle() { return "Drive Corporativo"; }
    @Override
    public void activate() throws BasicException { refreshAll(); }
    @Override
    public boolean deactivate() { return true; }

    private void initComponents() {
        setLayout(new BorderLayout());
        setBackground(BG_COLOR);
        
        layeredPane = new JLayeredPane();
        add(layeredPane, BorderLayout.CENTER);
        
        mainContentPanel = new JPanel(new BorderLayout(0, 20));
        mainContentPanel.setBackground(BG_COLOR);
        mainContentPanel.setBorder(new EmptyBorder(30, 40, 30, 40));
        
        // --- Header ---
        headerPanel = createHeaderPanel();
        mainContentPanel.add(headerPanel, BorderLayout.NORTH);
        
        // --- Center Content (Folders + Files) ---
        JPanel centerContent = new JPanel();
        centerContent.setLayout(new BoxLayout(centerContent, BoxLayout.Y_AXIS));
        centerContent.setBackground(BG_COLOR);
        
        // Folders Section
        foldersWrapper = new JPanel(new BorderLayout());
        foldersWrapper.setBackground(BG_COLOR);
        foldersWrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
        foldersWrapper.add(createFoldersSectionHeader(), BorderLayout.NORTH);
        
        foldersContainer = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 20));
        foldersContainer.setBackground(BG_COLOR);
        JScrollPane foldersScroll = new JScrollPane(foldersContainer);
        foldersScroll.setBorder(null);
        foldersScroll.getViewport().setBackground(BG_COLOR);
        foldersScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        foldersScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        
        // Set fixed height for folders scroll to prevent layout jumping too much
        foldersScroll.setPreferredSize(new Dimension(800, 140));
        foldersScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 140));
        
        foldersWrapper.add(foldersScroll, BorderLayout.CENTER);
        centerContent.add(foldersWrapper);
        
        centerContent.add(Box.createRigidArea(new Dimension(0, 20)));
        
        // Files Section
        JPanel filesWrapper = new JPanel(new BorderLayout());
        filesWrapper.setBackground(BG_COLOR);
        filesWrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
        filesWrapper.add(createFilesSectionHeader(), BorderLayout.NORTH);
        
        JScrollPane tableScroll = createFilesTablePanel();
        
        // Wrap table in a rounded white panel
        JPanel tableContainer = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
                g2.setColor(new Color(218, 220, 224));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
                g2.dispose();
            }
        };
        tableContainer.setOpaque(false);
        tableContainer.setBorder(new EmptyBorder(5, 5, 5, 5));
        tableContainer.add(tableScroll, BorderLayout.CENTER);
        
        filesWrapper.add(tableContainer, BorderLayout.CENTER);
        centerContent.add(filesWrapper);
        
        mainContentPanel.add(centerContent, BorderLayout.CENTER);
        
        JButton fab = createFAB();
        
        layeredPane.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent e) {
                mainContentPanel.setBounds(0, 0, layeredPane.getWidth(), layeredPane.getHeight());
                int fabSize = 64;
                fab.setBounds(layeredPane.getWidth() - fabSize - 40, layeredPane.getHeight() - fabSize - 40, fabSize, fabSize);
            }
        });
        
        layeredPane.add(mainContentPanel, Integer.valueOf(0));
        layeredPane.add(fab, Integer.valueOf(1));
    }

    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(BG_COLOR);
        header.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JPanel leftSide = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 0));
        leftSide.setBackground(BG_COLOR);
        
        // Icon
        JPanel iconPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(232, 240, 254)); // Light Blue background
                g2.fillOval(0, 0, getWidth(), getHeight());
                
                g2.setColor(BLUE_COLOR);
                int w = 26; int h = 20;
                int x = (getWidth() - w) / 2;
                int y = (getHeight() - h) / 2;
                g2.fillRoundRect(x, y + h/4, w, h*3/4, 3, 3);
                g2.fillRoundRect(x, y, w/2, h/2, 3, 3);
                g2.dispose();
            }
        };
        iconPanel.setPreferredSize(new Dimension(56, 56));
        iconPanel.setOpaque(false);
        leftSide.add(iconPanel);
        
        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setBackground(BG_COLOR);
        
        lblBreadcrumb = new JLabel("Drive Corporativo");
        lblBreadcrumb.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblBreadcrumb.setForeground(new Color(32, 33, 36));
        lblBreadcrumb.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lblBreadcrumb.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                openFolder("TODOS");
            }
            @Override
            public void mouseEntered(MouseEvent e) {
                if (!m_currentFolder.equals("TODOS")) {
                    lblBreadcrumb.setText("<html><u>Drive Corporativo</u> > " + m_currentFolder + "</html>");
                }
            }
            @Override
            public void mouseExited(MouseEvent e) {
                updateBreadcrumbUI();
            }
        });
        textPanel.add(lblBreadcrumb);
        
        lblHeaderSubtitle = new JLabel("Almacena, organiza y comparte archivos de tu empresa.");
        lblHeaderSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        lblHeaderSubtitle.setForeground(new Color(95, 99, 104));
        lblHeaderSubtitle.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));
        textPanel.add(lblHeaderSubtitle);
        
        leftSide.add(textPanel);
        header.add(leftSide, BorderLayout.WEST);
        
        // Right side (Search + View options mock)
        JPanel rightSide = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        rightSide.setBackground(BG_COLOR);
        
        // Search bar (Modern rounded pill)
        JPanel searchBox = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(241, 243, 244)); // Google Drive light gray
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 40, 40);
                g2.dispose();
            }
        };
        searchBox.setOpaque(false);
        searchBox.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        searchBox.setPreferredSize(new Dimension(300, 42));
        
        JLabel searchIcon = new JLabel("🔍 ");
        searchIcon.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        searchIcon.setForeground(new Color(95, 99, 104));
        searchBox.add(searchIcon, BorderLayout.WEST);
        
        m_searchField = new JTextField(20);
        m_searchField.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
        m_searchField.setOpaque(false);
        m_searchField.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        m_searchField.setForeground(new Color(32, 33, 36));
        m_searchField.setText(m_searchQuery);
        m_searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { doSearch(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { doSearch(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { doSearch(); }
            private void doSearch() {
                m_searchQuery = m_searchField.getText().trim();
                refreshTable();
            }
        });
        searchBox.add(m_searchField, BorderLayout.CENTER);
        
        rightSide.add(searchBox);
        
        JLabel sortLabel = new JLabel("Ordenar por: Nombre (A-Z) ▼");
        sortLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        sortLabel.setForeground(new Color(95, 99, 104));
        rightSide.add(sortLabel);
        
        header.add(rightSide, BorderLayout.EAST);
        
        return header;
    }

    private void updateBreadcrumbUI() {
        if (m_currentFolder.equals("TODOS")) {
            lblBreadcrumb.setText("Drive Corporativo");
        } else {
            lblBreadcrumb.setText("Drive Corporativo > " + m_currentFolder);
        }
    }

    private void openFolder(String folderName) {
        m_currentFolder = folderName;
        updateBreadcrumbUI();
        
        if (m_currentFolder.equals("TODOS")) {
            foldersWrapper.setVisible(true);
        } else {
            foldersWrapper.setVisible(false);
        }
        
        refreshAll();
    }

    private JPanel createFoldersSectionHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(BG_COLOR);
        p.setBorder(new EmptyBorder(0, 0, 10, 0));
        
        JLabel title = new JLabel("Carpetas");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(new Color(32, 33, 36));
        p.add(title, BorderLayout.WEST);
        
        return p;
    }

    private JPanel createFilesSectionHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(BG_COLOR);
        p.setBorder(new EmptyBorder(0, 0, 10, 0));
        
        JLabel title = new JLabel("Archivos");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(new Color(32, 33, 36));
        p.add(title, BorderLayout.WEST);
        
        return p;
    }

    private JScrollPane createFilesTablePanel() {
        m_tableModel = new DefaultTableModel(new Object[]{"Nombre", "Propietario", "Última modificación", "Tamaño", "", "ID", "FileName"}, 0) {
            public boolean isCellEditable(int row, int column) { return false; }
        };
        m_table = new JTable(m_tableModel);
        m_table.setRowHeight(56);
        m_table.setShowGrid(false);
        m_table.setIntercellSpacing(new Dimension(0, 0));
        m_table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        m_table.setForeground(new Color(60, 64, 67));
        m_table.setSelectionBackground(new Color(232, 240, 254));
        m_table.setSelectionForeground(new Color(32, 33, 36));
        m_table.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        JTableHeader th = m_table.getTableHeader();
        th.setFont(new Font("Segoe UI", Font.BOLD, 14));
        th.setForeground(new Color(95, 99, 104));
        th.setBackground(Color.WHITE);
        th.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(218, 220, 224)));
        th.setPreferredSize(new Dimension(0, 48));
        ((DefaultTableCellRenderer)th.getDefaultRenderer()).setHorizontalAlignment(JLabel.LEFT);
        
        // Adjust column widths
        m_table.getColumnModel().getColumn(0).setPreferredWidth(350); // Nombre
        m_table.getColumnModel().getColumn(1).setPreferredWidth(150); // Propietario
        m_table.getColumnModel().getColumn(2).setPreferredWidth(150); // Fecha
        m_table.getColumnModel().getColumn(3).setPreferredWidth(100); // Tamaño
        m_table.getColumnModel().getColumn(4).setPreferredWidth(50);  // Dots
        
        // Hide ID and FileName
        m_table.getColumnModel().getColumn(5).setMinWidth(0);
        m_table.getColumnModel().getColumn(5).setMaxWidth(0);
        m_table.getColumnModel().getColumn(6).setMinWidth(0);
        m_table.getColumnModel().getColumn(6).setMaxWidth(0);
        
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JPanel cell = new JPanel(new BorderLayout());
                cell.setOpaque(true);
                cell.setBackground(isSelected ? new Color(232, 240, 254) : Color.WHITE);
                cell.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(240, 240, 240)));
                
                if (column == 0) { // Nombre with Icon and Compartido text
                    JPanel leftP = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 14));
                    leftP.setOpaque(false);
                    String fileName = (String) m_tableModel.getValueAt(row, 6);
                    JLabel iconLbl = new JLabel(getFileIcon(fileName));
                    leftP.add(iconLbl);
                    
                    JPanel textP = new JPanel();
                    textP.setLayout(new BoxLayout(textP, BoxLayout.Y_AXIS));
                    textP.setOpaque(false);
                    
                    JLabel nameLbl = new JLabel((String) value);
                    nameLbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
                    nameLbl.setForeground(new Color(32, 33, 36));
                    textP.add(nameLbl);
                    
                    JLabel sharedLbl = new JLabel("👥 Compartido");
                    sharedLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                    sharedLbl.setForeground(new Color(95, 99, 104));
                    textP.add(sharedLbl);
                    
                    leftP.add(textP);
                    cell.add(leftP, BorderLayout.CENTER);
                } 
                else if (column == 1) { // Propietario with Avatar
                    JPanel leftP = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 14));
                    leftP.setOpaque(false);
                    
                    String ownerName = (String) value;
                    JLabel avatar = new JLabel() {
                        @Override
                        protected void paintComponent(Graphics g) {
                            Graphics2D g2 = (Graphics2D) g.create();
                            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                            g2.setColor(new Color(26, 115, 232)); // Blue avatar
                            g2.fillOval(0, 0, 28, 28);
                            g2.setColor(Color.WHITE);
                            g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
                            FontMetrics fm = g2.getFontMetrics();
                            String init = ownerName == null || ownerName.isEmpty() ? "A" : ownerName.substring(0, 1).toUpperCase();
                            int x = (28 - fm.stringWidth(init)) / 2;
                            int y = ((28 - fm.getHeight()) / 2) + fm.getAscent();
                            g2.drawString(init, x, y);
                            g2.dispose();
                        }
                    };
                    avatar.setPreferredSize(new Dimension(28, 28));
                    leftP.add(avatar);
                    
                    JLabel nameLbl = new JLabel(ownerName);
                    nameLbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                    nameLbl.setForeground(new Color(60, 64, 67));
                    leftP.add(nameLbl);
                    
                    cell.add(leftP, BorderLayout.CENTER);
                }
                else if (column == 4) { // Dots
                    JLabel dots = new JLabel("⋮", SwingConstants.CENTER);
                    dots.setFont(new Font("Segoe UI", Font.BOLD, 20));
                    dots.setForeground(new Color(95, 99, 104));
                    cell.add(dots, BorderLayout.CENTER);
                }
                else { // Fecha, Tamaño
                    JLabel lbl = new JLabel(value != null ? value.toString() : "");
                    lbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                    lbl.setForeground(new Color(95, 99, 104));
                    lbl.setBorder(new EmptyBorder(0, 10, 0, 0));
                    cell.add(lbl, BorderLayout.CENTER);
                }
                
                return cell;
            }
        };
        
        for (int i = 0; i < 5; i++) {
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
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)) {
                    viewDocument();
                } else if (e.getClickCount() == 1 && SwingUtilities.isLeftMouseButton(e)) {
                    int col = m_table.columnAtPoint(e.getPoint());
                    if (col == 4) {
                        m_table.setRowSelectionInterval(m_table.rowAtPoint(e.getPoint()), m_table.rowAtPoint(e.getPoint()));
                        popupMenu.show(m_table, e.getX(), e.getY());
                    }
                }
            }
        });
        
        JScrollPane sp = new JScrollPane(m_table);
        sp.setBorder(null);
        sp.getViewport().setBackground(Color.WHITE);
        return sp;
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
                    if (idx > 0) ext = fileName.substring(idx+1).toLowerCase();
                }
                
                Color bg = new Color(66, 133, 244); // default blue
                if (ext.equals("pdf")) bg = new Color(234, 67, 53); // red
                else if (ext.equals("xls") || ext.equals("xlsx") || ext.equals("csv")) bg = new Color(52, 168, 83); // green
                else if (ext.equals("jpg") || ext.equals("png") || ext.equals("jpeg")) bg = new Color(251, 188, 5); // yellow
                else if (ext.equals("mp4") || ext.equals("avi")) bg = new Color(156, 39, 176); // purple
                else if (ext.equals("doc") || ext.equals("docx")) bg = new Color(26, 115, 232); // blue
                
                g2.setColor(bg);
                g2.fillRoundRect(x, y, 32, 32, 6, 6);
                
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
                FontMetrics fm = g2.getFontMetrics();
                String text = ext.length() > 3 ? ext.substring(0,3).toUpperCase() : ext.toUpperCase();
                if (text.isEmpty()) text = "FILE";
                int tx = x + (32 - fm.stringWidth(text)) / 2;
                int ty = y + ((32 - fm.getHeight()) / 2) + fm.getAscent();
                g2.drawString(text, tx, ty);
                
                // Add a small dog-ear fold
                g2.setColor(new Color(255, 255, 255, 100));
                Polygon fold = new Polygon();
                fold.addPoint(x + 32, y);
                fold.addPoint(x + 32 - 8, y);
                fold.addPoint(x + 32, y + 8);
                g2.fillPolygon(fold);
                
                g2.dispose();
            }
            @Override
            public int getIconWidth() { return 32; }
            @Override
            public int getIconHeight() { return 32; }
        };
    }

    private JButton createFAB() {
        JButton fab = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Drop Shadow
                g2.setColor(new Color(0, 0, 0, 40));
                g2.fillOval(2, 6, getWidth() - 4, getHeight() - 4);
                
                if (getModel().isPressed()) {
                    g2.setColor(new Color(21, 101, 192));
                } else if (getModel().isRollover()) {
                    g2.setColor(new Color(30, 136, 229));
                } else {
                    g2.setColor(BLUE_COLOR);
                }
                g2.fillOval(0, 0, getWidth() - 4, getHeight() - 4);
                
                // "Upload" Icon (Up Arrow with bar)
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(3.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                int cx = (getWidth() - 4) / 2;
                int cy = (getHeight() - 4) / 2;
                
                // Vertical arrow line
                g2.drawLine(cx, cy + 6, cx, cy - 8);
                // Arrow head
                g2.drawLine(cx - 6, cy - 2, cx, cy - 8);
                g2.drawLine(cx + 6, cy - 2, cx, cy - 8);
                // Bottom bar
                g2.drawLine(cx - 8, cy + 10, cx + 8, cy + 10);
                
                g2.dispose();
            }
        };
        fab.setContentAreaFilled(false);
        fab.setBorderPainted(false);
        fab.setFocusPainted(false);
        fab.setCursor(new Cursor(Cursor.HAND_CURSOR));
        fab.setToolTipText("Subir nuevo archivo");
        fab.addActionListener(e -> showUploadDialog());
        return fab;
    }

    private JPanel createFolderCard(String name, int count, Color iconColor) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Shadow
                g2.setColor(new Color(0, 0, 0, 10));
                g2.fillRoundRect(0, 2, getWidth(), getHeight() - 2, 12, 12);
                
                // Background
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth() - 2, getHeight() - 2, 12, 12);
                
                // Border
                g2.setColor(new Color(218, 220, 224));
                g2.drawRoundRect(0, 0, getWidth() - 2, getHeight() - 2, 12, 12);
                
                g2.dispose();
            }
        };
        card.setLayout(new BorderLayout());
        card.setPreferredSize(new Dimension(260, 90));
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(15, 20, 15, 15));
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
        leftPanel.setOpaque(false);
        
        JLabel lblIcon = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(iconColor);
                int w = getWidth(); int h = getHeight();
                g2.fillRoundRect(0, h/4, w, h*3/4, 4, 4);
                g2.fillRoundRect(0, 0, w/2, h/2, 4, 4);
                g2.dispose();
            }
        };
        lblIcon.setPreferredSize(new Dimension(36, 32));
        leftPanel.add(lblIcon);
        
        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);
        
        JLabel lblName = new JLabel(name.length() > 20 ? name.substring(0, 20) + "..." : name);
        lblName.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblName.setForeground(new Color(32, 33, 36));
        textPanel.add(lblName);
        
        textPanel.add(Box.createRigidArea(new Dimension(0, 2)));
        
        JLabel lblCount = new JLabel(count + " elementos");
        lblCount.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblCount.setForeground(new Color(128, 134, 139));
        textPanel.add(lblCount);
        
        leftPanel.add(textPanel);
        card.add(leftPanel, BorderLayout.CENTER);
        
        JLabel dots = new JLabel("⋮", SwingConstants.CENTER);
        dots.setFont(new Font("Segoe UI", Font.BOLD, 22));
        dots.setForeground(new Color(95, 99, 104));
        card.add(dots, BorderLayout.EAST);
        
        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                openFolder(name);
            }
        });
        
        return card;
    }

    private void refreshAll() {
        if (m_currentFolder.equals("TODOS")) {
            refreshFolders();
        }
        refreshTable();
    }

    private void refreshFolders() {
        foldersContainer.removeAll();
        
        Color[] folderColors = {
            new Color(66, 133, 244), // Blue
            new Color(52, 168, 83),  // Green
            new Color(251, 188, 5),  // Yellow
            new Color(156, 39, 176), // Purple
            new Color(234, 67, 53),  // Red
            new Color(0, 150, 136)   // Teal
        };
        
        int colorIdx = 0;
        
        try (Connection con = m_App.getSession().getConnection();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT FOLDER_NAME, COUNT(*) FROM APP_DOCUMENTS GROUP BY FOLDER_NAME ORDER BY FOLDER_NAME")) {
            while (rs.next()) {
                String folder = rs.getString(1);
                if (folder == null || folder.isEmpty()) folder = "General";
                int count = rs.getInt(2);
                Color iconColor = folderColors[colorIdx % folderColors.length];
                colorIdx++;
                foldersContainer.add(createFolderCard(folder, count, iconColor));
            }
        } catch (SQLException ex) { LOGGER.log(Level.SEVERE, null, ex); }
        
        foldersContainer.revalidate();
        foldersContainer.repaint();
    }

    private void refreshTable() {
        m_tableModel.setRowCount(0);
        String sql;
        boolean isTodos = m_currentFolder.equals("TODOS");
        boolean isSearchActive = m_searchQuery != null && !m_searchQuery.trim().isEmpty();
        
        if (!isSearchActive) {
            if (isTodos) {
                sql = "SELECT DATENEW, FOLDER_NAME, TITLE, FILE_NAME, ID, OCTET_LENGTH(FILE_DATA) FROM APP_DOCUMENTS ORDER BY DATENEW DESC";
            } else {
                sql = "SELECT DATENEW, FOLDER_NAME, TITLE, FILE_NAME, ID, OCTET_LENGTH(FILE_DATA) FROM APP_DOCUMENTS WHERE FOLDER_NAME = ? ORDER BY DATENEW DESC";
            }
        } else {
            if (isTodos) {
                sql = "SELECT DATENEW, FOLDER_NAME, TITLE, FILE_NAME, ID, OCTET_LENGTH(FILE_DATA) FROM APP_DOCUMENTS " +
                      "WHERE UPPER(TITLE) LIKE ? OR UPPER(FILE_NAME) LIKE ? ORDER BY DATENEW DESC";
            } else {
                sql = "SELECT DATENEW, FOLDER_NAME, TITLE, FILE_NAME, ID, OCTET_LENGTH(FILE_DATA) FROM APP_DOCUMENTS " +
                      "WHERE FOLDER_NAME = ? AND (UPPER(TITLE) LIKE ? OR UPPER(FILE_NAME) LIKE ?) ORDER BY DATENEW DESC";
            }
        }
        
        try (Connection con = m_App.getSession().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            if (!isSearchActive) {
                if (!isTodos) {
                    ps.setString(1, m_currentFolder);
                }
            } else {
                String filterQuery = "%" + m_searchQuery.trim().toUpperCase() + "%";
                if (isTodos) {
                    ps.setString(1, filterQuery);
                    ps.setString(2, filterQuery);
                } else {
                    ps.setString(1, m_currentFolder);
                    ps.setString(2, filterQuery);
                    ps.setString(3, filterQuery);
                }
            }
            
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String title = rs.getString(3);
                String fileName = rs.getString(4);
                long sizeBytes = rs.getLong(6);
                String sizeStr = formatFileSize(sizeBytes);
                
                m_tableModel.addRow(new Object[] {
                        title,
                        "Admin", // Mock owner for visual consistency with Drive
                        Formats.TIMESTAMP.formatValue(rs.getTimestamp(1)),
                        sizeStr,
                        "",
                        rs.getString(5),
                        fileName
                });
            }
        } catch (SQLException ex) { LOGGER.log(Level.SEVERE, null, ex); }
    }
    
    private String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp-1) + "";
        return String.format("%.1f %sB", bytes / Math.pow(1024, exp), pre);
    }

    private void showUploadDialog() {
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), "Subir Archivo", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(480, 260);
        dialog.setLocationRelativeTo(this);
        
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(Color.WHITE);
        p.setBorder(new EmptyBorder(20, 25, 20, 25));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        Font labelFont = new Font("Segoe UI", Font.BOLD, 13);
        Font inputFont = new Font("Segoe UI", Font.PLAIN, 14);
        
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        JLabel lblFolder = new JLabel("Carpeta:"); lblFolder.setFont(labelFont);
        p.add(lblFolder, gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        JTextField txtFolder = new JTextField(m_currentFolder.equals("TODOS") ? "" : m_currentFolder);
        txtFolder.setFont(inputFont); txtFolder.setPreferredSize(new Dimension(0, 32));
        p.add(txtFolder, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        JLabel lblTitle = new JLabel("Título:"); lblTitle.setFont(labelFont);
        p.add(lblTitle, gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        JTextField txtTitle = new JTextField();
        txtTitle.setFont(inputFont); txtTitle.setPreferredSize(new Dimension(0, 32));
        p.add(txtTitle, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0;
        JLabel lblFile = new JLabel("Archivo:"); lblFile.setFont(labelFont);
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
            }
        });
        fileP.add(btnBrowse, BorderLayout.EAST);
        p.add(fileP, gbc);
        
        dialog.add(p, BorderLayout.CENTER);
        
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
        btnPanel.setBackground(new Color(248, 249, 250));
        btnPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(218, 220, 224)));
        
        JButton btnCancel = new JButton("Cancelar");
        btnCancel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnCancel.setForeground(new Color(95, 99, 104));
        btnCancel.setBackground(Color.WHITE);
        btnCancel.setFocusPainted(false);
        btnCancel.addActionListener(e -> dialog.dispose());
        
        JButton btnSave = new JButton("Subir");
        btnSave.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnSave.setBackground(BLUE_COLOR);
        btnSave.setForeground(Color.WHITE);
        btnSave.setFocusPainted(false);
        btnSave.addActionListener(e -> {
            String title = txtTitle.getText().trim();
            String folder = txtFolder.getText().trim();
            if (title.isEmpty() || selectedFile[0] == null) {
                JOptionPane.showMessageDialog(dialog, "Título y archivo requeridos.", "Drive", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (folder.isEmpty()) folder = "General";
            saveDocumentToDB(title, folder, selectedFile[0]);
            dialog.dispose();
            refreshAll();
        });
        
        btnPanel.add(btnCancel);
        btnPanel.add(btnSave);
        dialog.add(btnPanel, BorderLayout.SOUTH);
        
        dialog.setVisible(true);
    }
    
    private void saveDocumentToDB(String title, String folder, File file) {
        try (Connection con = m_App.getSession().getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "INSERT INTO APP_DOCUMENTS (ID, DATENEW, TITLE, FOLDER_NAME, FILE_NAME, FILE_DATA) VALUES (?, ?, ?, ?, ?, ?)")) {
            ps.setString(1, UUID.randomUUID().toString());
            ps.setTimestamp(2, new Timestamp(new Date().getTime()));
            ps.setString(3, title);
            ps.setString(4, folder);
            ps.setString(5, file.getName());
            ps.setBytes(6, java.nio.file.Files.readAllBytes(file.toPath()));
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Documento subido con éxito.", "Drive", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) { 
            LOGGER.log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(this, "Error al guardar: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void viewDocument() {
        int row = m_table.getSelectedRow();
        if (row < 0) return;
        String id = (String) m_tableModel.getValueAt(row, 5);
        String fileName = (String) m_tableModel.getValueAt(row, 6);
        try (Connection con = m_App.getSession().getConnection();
             PreparedStatement ps = con.prepareStatement("SELECT FILE_DATA FROM APP_DOCUMENTS WHERE ID = ?")) {
            ps.setString(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                byte[] data = rs.getBytes(1);
                File tempFile = new File(new File(System.getProperty("java.io.tmpdir"), "kriolos_drive"), UUID.randomUUID().toString().substring(0, 8) + "_" + fileName);
                tempFile.getParentFile().mkdirs();
                java.nio.file.Files.write(tempFile.toPath(), data);
                if (System.getProperty("os.name").toLowerCase().contains("win")) {
                    Runtime.getRuntime().exec(new String[]{"cmd", "/c", "start", "", tempFile.getAbsolutePath()});
                } else if (Desktop.isDesktopSupported()) { Desktop.getDesktop().open(tempFile); }
            }
        } catch (Exception ex) { LOGGER.log(Level.SEVERE, null, ex); }
    }

    private void deleteDocument() {
        int row = m_table.getSelectedRow();
        if (row < 0) return;
        if (JOptionPane.showConfirmDialog(this, "¿Eliminar permanentemente este archivo?", "Drive", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        String id = (String) m_tableModel.getValueAt(row, 5);
        try (Connection con = m_App.getSession().getConnection();
             PreparedStatement ps = con.prepareStatement("DELETE FROM APP_DOCUMENTS WHERE ID = ?")) {
            ps.setString(1, id);
            ps.executeUpdate();
            refreshAll();
        } catch (SQLException ex) { LOGGER.log(Level.SEVERE, null, ex); }
    }

    private void createTableIfNotExist() {
        try (Connection con = m_App.getSession().getConnection();
             Statement stmt = con.createStatement()) {
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS APP_DOCUMENTS (" +
                    "ID VARCHAR(255) PRIMARY KEY, DATENEW TIMESTAMP, TITLE VARCHAR(255), " +
                    "FOLDER_NAME VARCHAR(255), FILE_NAME VARCHAR(255), FILE_DATA LONGVARBINARY)");
        } catch (SQLException ex) { LOGGER.log(Level.SEVERE, null, ex); }
    }

    @Override
    public String toString() { return "Drive Corporativo"; }
}

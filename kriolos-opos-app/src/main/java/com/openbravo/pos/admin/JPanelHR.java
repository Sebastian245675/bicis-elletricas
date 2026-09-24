package com.openbravo.pos.admin;

import com.openbravo.basic.BasicException;
import com.openbravo.beans.JCalendarDialog;
import com.openbravo.format.Formats;
import com.openbravo.pos.forms.AppUser;
import com.openbravo.pos.sync.VoltiumSyncService;
import com.openbravo.pos.forms.AppView;
import com.openbravo.pos.forms.BeanFactoryApp;
import com.openbravo.pos.forms.BeanFactoryException;
import com.openbravo.pos.forms.JPanelView;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;
import java.awt.image.BufferedImage;
import javax.swing.JFileChooser;
import com.openbravo.data.loader.PreparedSentence;
import com.openbravo.data.loader.SerializerWriteString;
import com.openbravo.data.loader.SerializerReadBasic;
import com.openbravo.data.loader.SerializerWriteBasic;
import com.openbravo.data.loader.Datas;
import com.openbravo.pos.util.ModernActionIcon;

public class JPanelHR extends JPanel implements JPanelView, BeanFactoryApp {

    private static final long serialVersionUID = 1L;

    private static final Color APP_BACKGROUND = new Color(247, 248, 250);
    private static final Color CARD_BACKGROUND = Color.WHITE;
    private static final Color BORDER_COLOR = new Color(226, 232, 240);
    private static final Color TEXT_PRIMARY = new Color(30, 41, 59);
    private static final Color TEXT_SECONDARY = new Color(100, 116, 139);
    private static final Color BRAND_COLOR = new Color(37, 99, 235);
    private static final Color SUCCESS_COLOR = new Color(37, 99, 235);
    private static final Color MUTED_CARD = new Color(248, 250, 252);
    private static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 24);
    private static final Font SECTION_FONT = new Font("Segoe UI", Font.BOLD, 15);
    private static final Font BODY_FONT = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font LABEL_FONT = new Font("Segoe UI", Font.BOLD, 12);
    private static final Font METRIC_VALUE_FONT = new Font("Segoe UI", Font.BOLD, 22);
    private static final Font METRIC_LABEL_FONT = new Font("Segoe UI", Font.PLAIN, 11);
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy");
    private static final SimpleDateFormat DATE_TIME_FORMAT = new SimpleDateFormat("dd/MM/yyyy HH:mm");

    private AppView m_App;
    private DataLogicAdmin dlAdmin;
    private DataLogicHR dlHR;

    private final List<PeopleInfo> m_allEmployees = new ArrayList<>();
    private final DefaultListModel<PeopleInfo> m_employeeListModel = new DefaultListModel<>();

    private JList<PeopleInfo> m_employeeList;
    private JTextField m_txtSearch;
    private JLabel m_lblDirectoryCount;

    private JLabel m_lblSelectedName;
    private JLabel m_lblSelectedMeta;
    private JLabel m_lblSelectedStatus;
    private JLabel m_lblSelectedCode;
    private JLabel m_lblSelectedEmail;
    private JLabel m_lblSelectedHireDate;
    private JLabel m_lblAvatarInitials;
    private java.awt.image.BufferedImage m_employeeImage;
    private JPanel m_avatarPanel;
    private JLabel m_lblMetricBaseSalary;
    private JLabel m_lblMetricNetSalary;
    private JLabel m_lblMetricFrequency;
    private JLabel m_lblMetricLastPayroll;

    private JPanel m_workspaceCards;
    private CardLayout m_workspaceLayout;
    private JPanel m_detailCardsContainer;
    private CardLayout m_detailCardLayout;
    private TabManager m_tabManager;
    
    private JLabel m_lblDashTotalEmployees;
    private JLabel m_lblDashTotalPayroll;
    private JLabel m_lblDashPendingCount;
    private DefaultTableModel m_pendingModel;
    private JTable m_pendingTable;

    private JTextField m_txtEmployeeCode;
    private JTextField m_txtDepartment;
    private JTextField m_txtPositionTitle;
    private JComboBox<String> m_cmbContractType;
    private JComboBox<String> m_cmbEmployeeStatus;
    private JTextField m_txtHireDate;
    private JComboBox<String> m_cmbPayrollFrequency;
    private JTextField m_txtEmergencyContact;
    private JTextField m_txtTaxId;
    private JTextArea m_txtNotes;

    private JTextField m_txtBaseSalary;
    private JTextField m_txtCommissionRate;
    private JTextField m_txtTransportAllowance;
    private JTextField m_txtOtherAllowances;
    private JTextField m_txtBonusAmount;
    private JTextField m_txtDeductionRate;
    private JComboBox<String> m_cmbPensionType;
    private JTextField m_txtHealthInsurance;
    private JTextField m_txtSocialSecurityId;
    private JTextField m_txtBankName;
    private JTextField m_txtBankAccount;

    private JTextField m_txtPeriodLabel;
    private JTextField m_txtPeriodStart;
    private JTextField m_txtPeriodEnd;
    private JTextField m_txtPaymentDate;
    private JComboBox<String> m_cmbPaymentMethod;
    private JComboBox<String> m_cmbPayrollStatus;
    private JTextArea m_txtPayrollNotes;

    private JLabel m_lblPreviewBase;
    private JLabel m_lblPreviewCommission;
    private JLabel m_lblPreviewAllowances;
    private JLabel m_lblPreviewBonus;
    private JLabel m_lblPreviewGross;
    private JLabel m_lblPreviewDeductions;
    private JLabel m_lblPreviewNet;

    private JTable m_historyTable;
    private DefaultTableModel m_historyModel;
    private JComboBox<String> m_cmbHistoryYear;
    private JComboBox<String> m_cmbHistoryMonth;
    private JComboBox<String> m_cmbHistoryStatus;
    private JTextField m_txtHistoryCorrelative;
    private JPanel m_historyFilterPanel;
    private JTable m_errorTable;
    private DefaultTableModel m_errorModel;
    private JLabel m_lblSelectedHistoryPeriod;
    private java.util.List<Object[]> m_loadedPayrolls = new java.util.ArrayList<>();

    private JLabel m_lblCommEarnedMonth;
    private JLabel m_lblCommEarnedTotal;
    private JTable m_commTable;
    private DefaultTableModel m_commModel;

    private Date m_hireDateValue;
    private Date m_periodStartValue;
    private Date m_periodEndValue;
    private Date m_paymentDateValue;

    @Override
    public void init(AppView app) throws BeanFactoryException {
        m_App = app;
        dlAdmin = (DataLogicAdmin) app.getBean("com.openbravo.pos.admin.DataLogicAdmin");
        dlHR = (DataLogicHR) app.getBean("com.openbravo.pos.admin.DataLogicHR");
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout(0, 0));
        setBackground(APP_BACKGROUND);
        setBorder(new EmptyBorder(16, 20, 18, 20));

        // Instantiate components in the directory panel in the background
        createDirectoryPanel();

        // Add workspace panel directly (full width!)
        add(createWorkspacePanel(), BorderLayout.CENTER);

        registerLiveUpdates();
        resetPeriodDefaults();

        applyModernStyling();

        // Cargar datos iniciales antes de limpiar pantalla
        loadEmployees();
        clearScreenForNoSelection();
    }

    private void applyModernStyling() {
        // Enforce crema background on self
        this.setBackground(APP_BACKGROUND);
        estilizarRecursivo(this);
    }

    private void estilizarRecursivo(Component comp) {
        if (comp == null) return;

        if (comp instanceof JButton) {
            JButton btn = (JButton) comp;
            // Keep green for processing payroll
            if (btn.getBackground() != null && btn.getBackground().equals(SUCCESS_COLOR)) {
                btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
                btn.setFocusPainted(false);
                btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            } else {
                // Apply gold branding
                btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
                btn.setFocusPainted(false);
                btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            }
        }
        else if (comp instanceof JTextField) {
            final JTextField tf = (JTextField) comp;
            if (tf.isEditable()) {
                tf.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(224, 224, 224), 1),
                    BorderFactory.createEmptyBorder(6, 10, 6, 10)
                ));
                tf.addFocusListener(new java.awt.event.FocusAdapter() {
                    @Override
                    public void focusGained(java.awt.event.FocusEvent evt) {
                        tf.setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(BRAND_COLOR, 1),
                            BorderFactory.createEmptyBorder(6, 10, 6, 10)
                        ));
                    }
                    @Override
                    public void focusLost(java.awt.event.FocusEvent evt) {
                        tf.setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(new Color(224, 224, 224), 1),
                            BorderFactory.createEmptyBorder(6, 10, 6, 10)
                        ));
                    }
                });
            }
        }
        else if (comp instanceof JComboBox) {
            JComboBox<?> cb = (JComboBox<?>) comp;
            cb.setBorder(BorderFactory.createLineBorder(new Color(224, 224, 224), 1));
        }
        else if (comp instanceof JTable) {
            JTable t = (JTable) comp;
            t.setSelectionBackground(new Color(239, 246, 255));
            t.setSelectionForeground(TEXT_PRIMARY);
        }
        else if (comp instanceof JTabbedPane) {
            JTabbedPane tp = (JTabbedPane) comp;
            tp.setFont(new Font("Segoe UI", Font.BOLD, 13));
            tp.setBackground(Color.WHITE);
        }

        if (comp instanceof Container) {
            for (Component child : ((Container) comp).getComponents()) {
                estilizarRecursivo(child);
            }
        }
    }



    private JComponent createDirectoryPanel() {
        JPanel panel = createCardPanel();
        panel.setLayout(new BorderLayout(0, 14));
        panel.setPreferredSize(new Dimension(290, 0));

        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Directorio de personal");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(TEXT_PRIMARY);

        JLabel subtitle = new JLabel("Sellecciona a un colaborador para asig...");
        subtitle.setFont(BODY_FONT);
        subtitle.setForeground(TEXT_SECONDARY);
        subtitle.setBorder(new EmptyBorder(4, 0, 0, 0));

        header.add(title);
        header.add(subtitle);
        header.add(Box.createVerticalStrut(14));

        m_txtSearch = createTextField();
        m_txtSearch.setToolTipText("Buscar por nombre o identificador");
        m_txtSearch.putClientProperty("JTextField.placeholderText", "Buscar colaborador");
        header.add(m_txtSearch);
        
        JButton btnDash = new JButton("Limpiar búsqueda / Ver todos") {
            @Override
            protected void paintComponent(java.awt.Graphics g) {
                java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
                g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btnDash.setOpaque(false);
        btnDash.setContentAreaFilled(false);
        btnDash.setBorderPainted(false);
        btnDash.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnDash.setForeground(Color.WHITE);
        btnDash.setBackground(BRAND_COLOR);
        btnDash.setBorder(new EmptyBorder(8, 12, 8, 12));
        btnDash.setFocusPainted(false);
        btnDash.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnDash.addActionListener(e -> {
            m_txtSearch.setText("");
            m_employeeList.clearSelection();
            clearScreenForNoSelection();
        });
        header.add(Box.createVerticalStrut(8));
        header.add(btnDash);

        panel.add(header, BorderLayout.NORTH);

        m_employeeList = new JList<>(m_employeeListModel);
        m_employeeList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        m_employeeList.setCellRenderer(new EmployeeCellRenderer());
        m_employeeList.setFixedCellHeight(76);
        m_employeeList.setOpaque(false);
        m_employeeList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                loadSelectedEmployee();
            }
        });

        JScrollPane scrollPane = new JScrollPane(m_employeeList);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        scrollPane.getViewport().setBackground(CARD_BACKGROUND);
        scrollPane.getVerticalScrollBar().setUnitIncrement(18);
        panel.add(scrollPane, BorderLayout.CENTER);

        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(4, 2, 0, 2));
        m_lblDirectoryCount = new JLabel("0 empleados");
        m_lblDirectoryCount.setFont(BODY_FONT);
        m_lblDirectoryCount.setForeground(TEXT_SECONDARY);
        footer.add(m_lblDirectoryCount, BorderLayout.WEST);

        panel.add(footer, BorderLayout.SOUTH);
        return panel;
    }

    private JComponent createWorkspacePanel() {
        m_workspaceLayout = new CardLayout();
        m_workspaceCards = new JPanel(m_workspaceLayout);
        m_workspaceCards.setOpaque(false);

        m_workspaceCards.add(createDashboardPanel(), "DASHBOARD");
        m_workspaceCards.add(createEmployeeDetailPanel(), "DETAIL");

        return m_workspaceCards;
    }

    private JComponent createEmployeeDetailPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setOpaque(false);

        // Header for Detail panel
        JPanel header = new JPanel(new BorderLayout(16, 0));
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 0, 8, 0));

        JLabel title = new JLabel("Expediente y Nómina de Colaborador");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(TEXT_PRIMARY);
        header.add(title, BorderLayout.WEST);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);

        JButton saveButton = createActionButton("Guardar expediente", BRAND_COLOR);
        saveButton.setIcon(new ModernActionIcon(ModernActionIcon.Type.SAVE, 18, Color.WHITE));
        saveButton.addActionListener(e -> saveEmployeeProfile(true));

        JButton processButton = createActionButton("Procesar nomina", SUCCESS_COLOR);
        processButton.setIcon(new ModernActionIcon(ModernActionIcon.Type.MONEY, 18, Color.WHITE));
        processButton.addActionListener(e -> processPayroll());

        actions.add(saveButton);
        actions.add(processButton);
        header.add(actions, BorderLayout.EAST);

        JPanel mainContent = new JPanel(new BorderLayout(0, 8));
        mainContent.setOpaque(false);
        mainContent.add(createSpotlightPanel(), BorderLayout.NORTH);
        mainContent.add(createTabbedContent(), BorderLayout.CENTER);

        panel.add(header, BorderLayout.NORTH);
        panel.add(mainContent, BorderLayout.CENTER);
        return panel;
    }

    private JComponent createDashboardPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 16));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(0, 8, 8, 8));

        // Cabecera clara y funcional
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JPanel headerCopy = new JPanel();
        headerCopy.setLayout(new BoxLayout(headerCopy, BoxLayout.Y_AXIS));
        headerCopy.setOpaque(false);
        JLabel dashboardTitle = new JLabel("Equipo y nómina");
        dashboardTitle.setFont(new Font("Segoe UI", Font.BOLD, 25));
        dashboardTitle.setForeground(TEXT_PRIMARY);
        JLabel dashboardSubtitle = new JLabel("Resumen del personal y pagos que requieren atención");
        dashboardSubtitle.setFont(BODY_FONT);
        dashboardSubtitle.setForeground(TEXT_SECONDARY);
        headerCopy.add(dashboardTitle);
        headerCopy.add(Box.createVerticalStrut(4));
        headerCopy.add(dashboardSubtitle);
        header.add(headerCopy, BorderLayout.WEST);
        
        JPanel actionsHeader = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        actionsHeader.setOpaque(false);
        
        JButton btnRefresh = createCompactButton("Actualizar");
        btnRefresh.setIcon(new ModernActionIcon(ModernActionIcon.Type.REFRESH, 17, Color.WHITE));
        btnRefresh.setBackground(BRAND_COLOR);
        btnRefresh.setForeground(Color.WHITE);
        btnRefresh.addActionListener(e -> refreshDashboard());
        actionsHeader.add(btnRefresh);
        
        header.add(actionsHeader, BorderLayout.EAST);

        // Métricas sobrias con un único acento visual
        JPanel metricsPanel = new JPanel(new GridLayout(1, 3, 16, 0));
        metricsPanel.setOpaque(false);

        m_lblDashTotalEmployees = createMetricValueLabel();
        m_lblDashTotalPayroll = createMetricValueLabel();
        m_lblDashPendingCount = createMetricValueLabel();

        metricsPanel.add(createDashboardCard("NÓMINA DEL MES", m_lblDashTotalPayroll, BRAND_COLOR, BRAND_COLOR, "Total pagado durante el periodo", "BANKNOTE"));
        metricsPanel.add(createDashboardCard("POR PROCESAR", m_lblDashPendingCount, BRAND_COLOR, BRAND_COLOR, "Colaboradores pendientes", "CLOCK"));
        metricsPanel.add(createDashboardCard("PERSONAL ACTIVO", m_lblDashTotalEmployees, BRAND_COLOR, BRAND_COLOR, "Personas registradas", "PEOPLE"));

        // 3. Cuerpo Central: Tabla + Acciones Rapidas
        JPanel body = new JPanel(new BorderLayout(16, 0));
        body.setOpaque(false);

        // 3a. Tabla de Pendientes (Lado Izquierdo) - Card moderno con su título visible arriba
        JPanel tableCard = createCardPanel();
        tableCard.setLayout(new BorderLayout(0, 12));
        
        JLabel lblTableTitle = new JLabel("Pagos pendientes");
        lblTableTitle.setFont(SECTION_FONT);
        lblTableTitle.setForeground(TEXT_PRIMARY);
        lblTableTitle.setBorder(new EmptyBorder(0, 0, 8, 0));
        tableCard.add(lblTableTitle, BorderLayout.NORTH);
        
        m_pendingModel = new DefaultTableModel(new String[]{"ID", "Colaborador", "Departamento", "Estado", "Acción"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        m_pendingTable = new JTable(m_pendingModel);
        m_pendingTable.setFont(BODY_FONT);
        m_pendingTable.setRowHeight(48);
        m_pendingTable.setFillsViewportHeight(true);
        m_pendingTable.setShowGrid(false);
        m_pendingTable.setIntercellSpacing(new Dimension(0, 0));
        m_pendingTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Estilo de tabla moderno
        m_pendingTable.getTableHeader().setFont(LABEL_FONT);
        m_pendingTable.getTableHeader().setBackground(new Color(248, 250, 252));
        m_pendingTable.getTableHeader().setForeground(TEXT_SECONDARY);
        m_pendingTable.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR));
        
        m_pendingTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                int column = m_pendingTable.columnAtPoint(e.getPoint());
                int row = m_pendingTable.rowAtPoint(e.getPoint());
                if (row >= 0 && row < m_pendingTable.getRowCount() && column >= 0) {
                    int modelColumn = m_pendingTable.convertColumnIndexToModel(column);
                    if (modelColumn == 4) { // Action Column (Acción)
                        String id = (String) m_pendingModel.getValueAt(row, 0);
                        selectEmployeeById(id);
                    }
                }
            }
        });

        m_pendingTable.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            @Override
            public void mouseMoved(java.awt.event.MouseEvent e) {
                int column = m_pendingTable.columnAtPoint(e.getPoint());
                if (column >= 0) {
                    int modelColumn = m_pendingTable.convertColumnIndexToModel(column);
                    if (modelColumn == 4) {
                        m_pendingTable.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                        return;
                    }
                }
                m_pendingTable.setCursor(Cursor.getDefaultCursor());
            }
        });

        // Ocultar la columna ID visualmente pero dejarla disponible en el modelo
        // La columna ID es la primera (index 0)
        m_pendingTable.removeColumn(m_pendingTable.getColumnModel().getColumn(0));

        m_pendingTable.setDefaultRenderer(Object.class, new PendingTableRenderer());

        JScrollPane scroll = new JScrollPane(m_pendingTable);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        scroll.getViewport().setBackground(Color.WHITE);
        tableCard.add(scroll, BorderLayout.CENTER);

        // 3b. Panel Lateral de Acciones (Lado Derecho)
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setOpaque(false);
        rightPanel.setPreferredSize(new Dimension(280, 0));
        
        JPanel actionsCard = createSectionCard("Acciones rápidas");
        
        JPanel buttonsPanel = new JPanel(new GridLayout(3, 1, 0, 12));
        buttonsPanel.setOpaque(false);
        
        JButton btnAddEmp = createActionButton("Registrar Nuevo Colaborador", "Añadir colaborador...", "USER");
        btnAddEmp.addActionListener(e -> {
            if (m_App != null && m_App.getAppUserView() != null) {
                m_App.getAppUserView().showTask("com.openbravo.pos.admin.PeoplePanel");
            } else {
                JOptionPane.showMessageDialog(this, "Para añadir personal, usa el módulo de 'Mantenimiento de Personas'.");
            }
        });
        
        JButton btnReports = createActionButton("Reporte Mensual Detallado", "Descargar resumen ...", "REPORT");
        btnReports.setEnabled(false);
        
        JButton btnSettings = createActionButton("Configuración de Nómina Avanzada", "Ajustar porcentajes ...", "SETTINGS");
        
        buttonsPanel.add(btnAddEmp);
        buttonsPanel.add(btnReports);
        buttonsPanel.add(btnSettings);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        actionsCard.add(buttonsPanel, gbc);
        
        rightPanel.add(actionsCard);
        rightPanel.add(Box.createVerticalGlue());

        body.add(tableCard, BorderLayout.CENTER);
        body.add(rightPanel, BorderLayout.EAST);

        // Contenedor superior para agrupar cabecera y tarjetas de métricas
        JPanel topContainer = new JPanel();
        topContainer.setLayout(new BoxLayout(topContainer, BoxLayout.Y_AXIS));
        topContainer.setOpaque(false);
        topContainer.add(header);
        topContainer.add(Box.createVerticalStrut(14));
        topContainer.add(metricsPanel);

        panel.add(topContainer, BorderLayout.NORTH);
        panel.add(body, BorderLayout.CENTER);
        
        return panel;
    }

    private JButton createActionButton(String text, String subtext, String iconType) {
        JButton btn = new JButton() {
            @Override
            protected void paintComponent(java.awt.Graphics g) {
                java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
                g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth();
                int h = getHeight();
                
                if (getModel().isPressed()) {
                    g2.setColor(new Color(241, 245, 249));
                } else if (getModel().isRollover()) {
                    g2.setColor(new Color(248, 250, 252));
                } else {
                    g2.setColor(Color.WHITE);
                }
                g2.fillRoundRect(0, 0, w, h, 12, 12);
                
                g2.setColor(getModel().isRollover() ? BRAND_COLOR : new Color(226, 232, 240));
                g2.drawRoundRect(0, 0, w - 1, h - 1, 12, 12);

                // Draw Left and Right Icons
                g2.setColor(new Color(30, 41, 59)); // Slate 800
                if ("USER".equals(iconType)) {
                    // Left: Document icon
                    g2.setStroke(new java.awt.BasicStroke(1.8f));
                    g2.drawRoundRect(16, 12, 14, 18, 2, 2);
                    g2.drawLine(20, 16, 26, 16);
                    g2.drawLine(20, 20, 26, 20);
                    g2.drawLine(20, 24, 23, 24);
                    
                    // Right: User Plus icon
                    g2.setStroke(new java.awt.BasicStroke(1.8f));
                    g2.drawOval(w - 28, 12, 8, 8); // head
                    g2.drawArc(w - 33, 21, 18, 12, 0, 180); // body
                    // plus sign
                    g2.drawLine(w - 14, 13, w - 14, 19);
                    g2.drawLine(w - 17, 16, w - 11, 16);
                } else if ("REPORT".equals(iconType)) {
                    // Left: Bar chart icon
                    g2.setStroke(new java.awt.BasicStroke(1.8f));
                    g2.drawLine(14, 28, 28, 28); // base
                    g2.drawRect(16, 20, 2, 8);
                    g2.drawRect(20, 14, 2, 14);
                    g2.drawRect(24, 23, 2, 5);
                    
                    // Right: Doc with pie chart icon
                    g2.setStroke(new java.awt.BasicStroke(1.8f));
                    g2.drawRoundRect(w - 26, 11, 14, 19, 2, 2);
                    g2.drawOval(w - 22, 18, 6, 6);
                    g2.fillArc(w - 22, 18, 6, 6, 0, 90);
                } else if ("SETTINGS".equals(iconType)) {
                    // Left: Gear/Cog icon
                    g2.setStroke(new java.awt.BasicStroke(1.8f));
                    g2.drawOval(16, 16, 8, 8);
                    for (int a = 0; a < 360; a += 45) {
                        double rad = Math.toRadians(a);
                        int x1 = 20 + (int) (4 * Math.cos(rad));
                        int y1 = 20 + (int) (4 * Math.sin(rad));
                        int x2 = 20 + (int) (6 * Math.cos(rad));
                        int y2 = 20 + (int) (6 * Math.sin(rad));
                        g2.drawLine(x1, y1, x2, y2);
                    }
                    
                    // Right: Gear icon
                    g2.setStroke(new java.awt.BasicStroke(1.8f));
                    g2.drawOval(w - 24, 16, 8, 8);
                    for (int a = 0; a < 360; a += 45) {
                        double rad = Math.toRadians(a);
                        int x1 = (w - 20) + (int) (4 * Math.cos(rad));
                        int y1 = 20 + (int) (4 * Math.sin(rad));
                        int x2 = (w - 20) + (int) (6 * Math.cos(rad));
                        int y2 = 20 + (int) (6 * Math.sin(rad));
                        g2.drawLine(x1, y1, x2, y2);
                    }
                }
                
                g2.dispose();
            }
        };
        btn.setLayout(new BorderLayout(12, 0));
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(10, 42, 10, 42)); // Leave space for left and right icons
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        JLabel lblMain = new JLabel(text);
        lblMain.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblMain.setForeground(TEXT_PRIMARY);
        
        JLabel lblSub = new JLabel(subtext);
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblSub.setForeground(TEXT_SECONDARY);
        
        JPanel p = new JPanel(new GridLayout(2, 1, 0, 2));
        p.setOpaque(false);
        p.add(lblMain);
        p.add(lblSub);
        
        btn.add(p, BorderLayout.CENTER);
        
        return btn;
    }

    private JPanel createDashboardCard(String title, JLabel valueLabel, Color startColor, Color endColor, String footerText, String iconType) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(java.awt.Graphics g) {
                super.paintComponent(g);
                java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
                g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth();
                int h = getHeight();
                g2.setColor(CARD_BACKGROUND);
                g2.fillRoundRect(0, 0, w, h, 12, 12);
                g2.setColor(BORDER_COLOR);
                g2.drawRoundRect(0, 0, w - 1, h - 1, 12, 12);
                g2.setColor(BRAND_COLOR);
                g2.fillRoundRect(0, 0, 4, h, 4, 4);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createEmptyBorder(16, 18, 15, 18));

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        titleLabel.setForeground(TEXT_SECONDARY);

        valueLabel.setForeground(TEXT_PRIMARY);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        
        JLabel footer = new JLabel(footerText);
        footer.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        footer.setForeground(TEXT_SECONDARY);
        
        content.add(titleLabel);
        content.add(Box.createVerticalStrut(7));
        content.add(valueLabel);
        content.add(Box.createVerticalStrut(6));
        content.add(footer);

        card.add(content, BorderLayout.CENTER);
        
        return card;
    }

    private void refreshDashboard() {
        try {
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.DAY_OF_MONTH, 1);
            Date start = cal.getTime();
            cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
            Date end = cal.getTime();

            m_lblDashTotalEmployees.setText(String.valueOf(m_allEmployees.size()));
            m_lblDashTotalPayroll.setText(formatCurrency(dlHR.getPayrollTotalAmount(start, end)));
            
            List<Object[]> pending = dlHR.getPendingPayrollEmployees(start, end);
            m_lblDashPendingCount.setText(String.valueOf(pending.size()));

            m_pendingModel.setRowCount(0);
            for (Object[] row : pending) {
                String id = (String) row[0];
                String name = (String) row[1];
                
                String dept = "Ventas";
                if (row[2] != null && !row[2].toString().trim().isEmpty()) {
                    dept = (String) row[2];
                } else {
                    if ("empl".equalsIgnoreCase(name)) dept = "Ingenieria";
                    else if ("manager".equalsIgnoreCase(name)) dept = "RRHH";
                }

                Double baseSalary = (Double) row[3];
                String socialSec = (String) row[4];
                String emergency = (String) row[5];
                String bankAcc = (String) row[6];

                boolean hasWarnings = (baseSalary == null || baseSalary <= 0.0 || 
                                       socialSec == null || socialSec.trim().isEmpty() || 
                                       emergency == null || emergency.trim().isEmpty() || 
                                       bankAcc == null || bankAcc.trim().isEmpty());
                
                String state = hasWarnings ? "Pendiente" : "Aprobado";

                m_pendingModel.addRow(new Object[]{
                    id, name, dept, state, ""
                });
            }
        } catch (BasicException e) {
            showError("No se pudo actualizar el dashboard.", e);
        }
    }

    private void selectEmployeeById(String id) {
        for (int i = 0; i < m_employeeListModel.size(); i++) {
            if (m_employeeListModel.get(i).getID().equals(id)) {
                m_employeeList.setSelectedIndex(i);
                m_employeeList.ensureIndexIsVisible(i);
                return;
            }
        }
    }

    public void showEmployeePayroll(String employeeId) {
        if (employeeId != null && !employeeId.isEmpty()) {
            selectEmployeeById(employeeId);
        }
        if (m_workspaceLayout != null && m_workspaceCards != null) {
            m_workspaceLayout.show(m_workspaceCards, "DETAIL");
        }
        if (m_tabManager != null) {
            m_tabManager.switchToIndex(1);
        }
    }



    private JComponent createSpotlightPanel() {
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setOpaque(false);

        // Volver al Dashboard Button
        JButton btnBack = new JButton("← Volver al Dashboard") {
            @Override
            protected void paintComponent(java.awt.Graphics g) {
                java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
                g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btnBack.setOpaque(false);
        btnBack.setContentAreaFilled(false);
        btnBack.setBorderPainted(false);
        btnBack.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnBack.setForeground(Color.WHITE);
        btnBack.setBackground(BRAND_COLOR);
        btnBack.setBorder(new EmptyBorder(8, 14, 8, 14));
        btnBack.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnBack.setFocusable(false);
        btnBack.addActionListener(e -> {
            m_employeeList.clearSelection();
            clearScreenForNoSelection();
        });

        JPanel backPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        backPanel.setOpaque(false);
        backPanel.setBorder(new EmptyBorder(0, 0, 4, 0));
        backPanel.add(btnBack);
        container.add(backPanel);

        // 1. Spotlight Card (White background card)
        JPanel spotlightCard = new JPanel(new GridBagLayout());
        spotlightCard.setBackground(Color.WHITE);
        spotlightCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                new EmptyBorder(14, 18, 14, 18)));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 0, 0, 0);

        // Circular Initials Avatar
        m_avatarPanel = new JPanel() {
            @Override
            protected void paintComponent(java.awt.Graphics g) {
                java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
                g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                
                int w = getWidth();
                int h = getHeight();
                
                if (m_employeeImage != null) {
                    // Create a circular clip
                    java.awt.geom.Area clip = new java.awt.geom.Area(new java.awt.geom.Ellipse2D.Double(0, 0, w, h));
                    g2.setClip(clip);
                    
                    // Draw scaled image
                    g2.drawImage(m_employeeImage, 0, 0, w, h, null);
                } else {
                    // Fallback to blue circle
                    g2.setColor(new java.awt.Color(37, 99, 235)); // Brand blue
                    g2.fillOval(0, 0, w, h);
                }
                g2.dispose();
            }
        };
        m_avatarPanel.setPreferredSize(new Dimension(64, 64));
        m_avatarPanel.setMinimumSize(new Dimension(64, 64));
        m_avatarPanel.setOpaque(false);
        m_avatarPanel.setLayout(new BorderLayout());
        m_avatarPanel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        m_avatarPanel.setToolTipText("Haga clic para subir, cambiar o quitar la foto del empleado");
        m_avatarPanel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                chooseEmployeePhoto();
            }
        });

        m_lblAvatarInitials = new JLabel("", SwingConstants.CENTER);
        m_lblAvatarInitials.setFont(new Font("Segoe UI", Font.BOLD, 22));
        m_lblAvatarInitials.setForeground(Color.WHITE);
        m_avatarPanel.add(m_lblAvatarInitials, BorderLayout.CENTER);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridheight = 2;
        gbc.weightx = 0.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0, 0, 20);
        spotlightCard.add(m_avatarPanel, gbc);

        // Info stack for Name, Meta and Email
        JPanel infoStack = new JPanel();
        infoStack.setLayout(new BoxLayout(infoStack, BoxLayout.Y_AXIS));
        infoStack.setOpaque(false);

        m_lblSelectedName = new JLabel("Sin empleado seleccionado");
        m_lblSelectedName.setFont(new Font("Segoe UI", Font.BOLD, 20));
        m_lblSelectedName.setForeground(TEXT_PRIMARY);

        m_lblSelectedMeta = new JLabel("Selecciona un colaborador para comenzar.");
        m_lblSelectedMeta.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        m_lblSelectedMeta.setForeground(TEXT_SECONDARY);
        m_lblSelectedMeta.setBorder(new EmptyBorder(4, 0, 4, 0));

        m_lblSelectedEmail = new JLabel("");
        m_lblSelectedEmail.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        m_lblSelectedEmail.setForeground(new java.awt.Color(37, 99, 235));

        infoStack.add(m_lblSelectedName);
        infoStack.add(m_lblSelectedMeta);
        infoStack.add(m_lblSelectedEmail);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridheight = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0, 0, 0);
        spotlightCard.add(infoStack, gbc);

        // Vector Mini-Buttons Stack
        JPanel iconsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        iconsPanel.setOpaque(false);

        JButton btnStopwatch = new JButton() {
            @Override
            protected void paintComponent(java.awt.Graphics g) {
                java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
                g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(239, 246, 255));
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.setColor(new Color(37, 99, 235));
                g2.setStroke(new java.awt.BasicStroke(1.8f));
                g2.drawOval(8, 8, 16, 16);
                g2.drawLine(16, 8, 16, 5);
                g2.drawLine(16, 16, 16, 11);
                g2.drawLine(16, 16, 19, 16);
                g2.dispose();
            }
        };
        btnStopwatch.setPreferredSize(new Dimension(32, 32));
        btnStopwatch.setOpaque(false);
        btnStopwatch.setContentAreaFilled(false);
        btnStopwatch.setBorderPainted(false);
        btnStopwatch.setFocusable(false);
        btnStopwatch.setToolTipText("Control de Asistencia");

        JButton btnCalendar = new JButton() {
            @Override
            protected void paintComponent(java.awt.Graphics g) {
                java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
                g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(239, 246, 255));
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.setColor(new Color(37, 99, 235));
                g2.setStroke(new java.awt.BasicStroke(1.8f));
                g2.drawRect(8, 9, 16, 14);
                g2.drawLine(8, 14, 24, 14);
                g2.drawLine(12, 6, 12, 9);
                g2.drawLine(20, 6, 20, 9);
                g2.dispose();
            }
        };
        btnCalendar.setPreferredSize(new Dimension(32, 32));
        btnCalendar.setOpaque(false);
        btnCalendar.setContentAreaFilled(false);
        btnCalendar.setBorderPainted(false);
        btnCalendar.setFocusable(false);
        btnCalendar.setToolTipText("Horarios y Turnos");

        JButton btnOrg = new JButton() {
            @Override
            protected void paintComponent(java.awt.Graphics g) {
                java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
                g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(239, 246, 255));
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.setColor(new Color(37, 99, 235));
                g2.setStroke(new java.awt.BasicStroke(1.8f));
                g2.drawRect(13, 6, 6, 4);
                g2.drawRect(7, 16, 6, 4);
                g2.drawRect(19, 16, 6, 4);
                g2.drawLine(16, 10, 16, 13);
                g2.drawLine(10, 13, 22, 13);
                g2.drawLine(10, 13, 10, 16);
                g2.drawLine(22, 13, 22, 16);
                g2.dispose();
            }
        };
        btnOrg.setPreferredSize(new Dimension(32, 32));
        btnOrg.setOpaque(false);
        btnOrg.setContentAreaFilled(false);
        btnOrg.setBorderPainted(false);
        btnOrg.setFocusable(false);
        btnOrg.setToolTipText("Estructura Organizacional");

        iconsPanel.add(btnStopwatch);
        iconsPanel.add(btnCalendar);
        iconsPanel.add(btnOrg);

        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.gridheight = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(12, 0, 0, 0);
        spotlightCard.add(iconsPanel, gbc);

        // Right Info Stack
        JPanel rightStack = new JPanel();
        rightStack.setLayout(new BoxLayout(rightStack, BoxLayout.Y_AXIS));
        rightStack.setOpaque(false);

        JPanel statusRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        statusRow.setOpaque(false);

        m_lblSelectedStatus = new JLabel() {
            @Override
            protected void paintComponent(java.awt.Graphics g) {
                java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
                g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        m_lblSelectedStatus.setFont(new Font("Segoe UI", Font.BOLD, 12));
        m_lblSelectedStatus.setBorder(new EmptyBorder(5, 12, 5, 12));
        m_lblSelectedStatus.setHorizontalAlignment(SwingConstants.CENTER);
        m_lblSelectedStatus.setOpaque(false);

        JLabel lblLastActive = new JLabel("Último registro: —");
        lblLastActive.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblLastActive.setForeground(TEXT_SECONDARY);

        statusRow.add(m_lblSelectedStatus);
        statusRow.add(lblLastActive);

        m_lblSelectedCode = new JLabel("ID de Colaborador: -");
        m_lblSelectedCode.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        m_lblSelectedCode.setForeground(TEXT_PRIMARY);
        m_lblSelectedCode.setBorder(new EmptyBorder(8, 0, 4, 0));
        m_lblSelectedCode.setHorizontalAlignment(SwingConstants.RIGHT);
        m_lblSelectedCode.setAlignmentX(Component.RIGHT_ALIGNMENT);

        m_lblSelectedHireDate = new JLabel("Fecha de ingreso: -");
        m_lblSelectedHireDate.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        m_lblSelectedHireDate.setForeground(TEXT_PRIMARY);
        m_lblSelectedHireDate.setHorizontalAlignment(SwingConstants.RIGHT);
        m_lblSelectedHireDate.setAlignmentX(Component.RIGHT_ALIGNMENT);

        statusRow.setAlignmentX(Component.RIGHT_ALIGNMENT);
        rightStack.add(statusRow);
        rightStack.add(m_lblSelectedCode);
        rightStack.add(m_lblSelectedHireDate);

        gbc.gridx = 3;
        gbc.gridy = 0;
        gbc.gridheight = 2;
        gbc.weightx = 0.0;
        gbc.fill = GridBagConstraints.VERTICAL;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.insets = new Insets(0, 10, 0, 10);
        spotlightCard.add(rightStack, gbc);

        // Edit Button
        JButton btnEdit = new JButton("Editar expediente") {
            @Override
            protected void paintComponent(java.awt.Graphics g) {
                java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
                g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(new Color(209, 213, 219));
                g2.setStroke(new java.awt.BasicStroke(1.2f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);

                g2.setColor(new Color(31, 41, 55));
                g2.setStroke(new java.awt.BasicStroke(1.8f));
                g2.drawLine(14, 20, 20, 14);
                g2.drawLine(20, 14, 22, 16);
                g2.drawLine(22, 16, 16, 22);
                g2.drawLine(16, 22, 14, 20);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btnEdit.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnEdit.setForeground(new Color(31, 41, 55));
        btnEdit.setOpaque(false);
        btnEdit.setContentAreaFilled(false);
        btnEdit.setBorderPainted(false);
        btnEdit.setFocusPainted(false);
        btnEdit.setPreferredSize(new Dimension(160, 36));
        btnEdit.setBorder(new EmptyBorder(0, 24, 0, 0));
        btnEdit.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnEdit.addActionListener(e -> saveEmployeeProfile(true));

        gbc.gridx = 4;
        gbc.gridy = 0;
        gbc.gridheight = 2;
        gbc.weightx = 0.0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.insets = new Insets(0, 10, 0, 0);
        spotlightCard.add(btnEdit, gbc);

        // 2. Metrics Labels Initialization
        m_lblMetricBaseSalary = new JLabel(".00");
        m_lblMetricBaseSalary.setFont(new Font("Segoe UI", Font.BOLD, 20));
        m_lblMetricBaseSalary.setForeground(TEXT_PRIMARY);

        m_lblMetricNetSalary = new JLabel(".00");
        m_lblMetricNetSalary.setFont(new Font("Segoe UI", Font.BOLD, 20));
        m_lblMetricNetSalary.setForeground(SUCCESS_COLOR);

        m_lblMetricFrequency = new JLabel("Sin definir");
        m_lblMetricFrequency.setFont(new Font("Segoe UI", Font.BOLD, 20));
        m_lblMetricFrequency.setForeground(TEXT_PRIMARY);

        m_lblMetricLastPayroll = new JLabel("Sin pagos");
        m_lblMetricLastPayroll.setFont(new Font("Segoe UI", Font.BOLD, 20));
        m_lblMetricLastPayroll.setForeground(TEXT_PRIMARY);

        // 3. Neto Estimado Header Card addition
        JPanel netEstPanel = new JPanel();
        netEstPanel.setLayout(new BoxLayout(netEstPanel, BoxLayout.Y_AXIS));
        netEstPanel.setOpaque(false);
        netEstPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 1, 0, 0, BORDER_COLOR),
            BorderFactory.createEmptyBorder(0, 18, 0, 18)
        ));

        JLabel lblNetTitle = new JLabel("NETO ESTIMADO");
        lblNetTitle.setFont(new Font("Segoe UI", Font.BOLD, 9));
        lblNetTitle.setForeground(TEXT_SECONDARY);
        lblNetTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        m_lblMetricNetSalary.setFont(new Font("Segoe UI", Font.BOLD, 22));
        m_lblMetricNetSalary.setForeground(SUCCESS_COLOR);
        m_lblMetricNetSalary.setAlignmentX(Component.LEFT_ALIGNMENT);

        netEstPanel.add(lblNetTitle);
        netEstPanel.add(Box.createVerticalStrut(4));
        netEstPanel.add(m_lblMetricNetSalary);

        // Add Neto Estimado panel inside spotlightCard at gridx = 2
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.gridheight = 2;
        gbc.weightx = 0.0;
        gbc.fill = GridBagConstraints.VERTICAL;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 10, 0, 10);
        spotlightCard.add(netEstPanel, gbc);

        container.add(spotlightCard);

        return container;
    }

    private JComponent createTabbedContent() {
        JPanel mainTabPanel = new JPanel(new BorderLayout(0, 8));
        mainTabPanel.setOpaque(false);

        // Instanciar los filtros de historial aquí de forma compacta
        m_cmbHistoryYear = new JComboBox<>(new String[]{ "Todos", "2024", "2025", "2026" });
        m_cmbHistoryYear.setSelectedItem("Todos");
        m_cmbHistoryYear.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        m_cmbHistoryYear.setPreferredSize(new java.awt.Dimension(72, 26));

        m_cmbHistoryMonth = new JComboBox<>(new String[]{
            "Todos", "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
            "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
        });
        m_cmbHistoryMonth.setSelectedItem("Todos");
        m_cmbHistoryMonth.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        m_cmbHistoryMonth.setPreferredSize(new java.awt.Dimension(90, 26));

        m_cmbHistoryStatus = new JComboBox<>(new String[]{ "Todos", "Enviado", "Con errores", "Anulado" });
        m_cmbHistoryStatus.setSelectedItem("Todos");
        m_cmbHistoryStatus.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        m_cmbHistoryStatus.setPreferredSize(new java.awt.Dimension(95, 26));

        m_txtHistoryCorrelative = new JTextField();
        m_txtHistoryCorrelative.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        m_txtHistoryCorrelative.setPreferredSize(new java.awt.Dimension(80, 26));
        m_txtHistoryCorrelative.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(203, 213, 225), 1),
            BorderFactory.createEmptyBorder(2, 6, 2, 6)
        ));

        m_historyFilterPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        m_historyFilterPanel.setOpaque(false);
        m_historyFilterPanel.setBorder(new EmptyBorder(4, 0, 4, 0));

        JLabel lblYear = new JLabel("Año:");
        lblYear.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblYear.setForeground(TEXT_SECONDARY);
        m_historyFilterPanel.add(lblYear);
        m_historyFilterPanel.add(m_cmbHistoryYear);

        JLabel lblMonth = new JLabel("Mes:");
        lblMonth.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblMonth.setForeground(TEXT_SECONDARY);
        m_historyFilterPanel.add(lblMonth);
        m_historyFilterPanel.add(m_cmbHistoryMonth);

        JLabel lblStatus = new JLabel("Estado:");
        lblStatus.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblStatus.setForeground(TEXT_SECONDARY);
        m_historyFilterPanel.add(lblStatus);
        m_historyFilterPanel.add(m_cmbHistoryStatus);

        JLabel lblCorr = new JLabel("Corr.:");
        lblCorr.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblCorr.setForeground(TEXT_SECONDARY);
        m_historyFilterPanel.add(lblCorr);
        m_historyFilterPanel.add(m_txtHistoryCorrelative);

        JButton btnSearch = createCompactButton("Buscar");
        btnSearch.setBackground(new Color(37, 99, 235));
        btnSearch.setForeground(Color.WHITE);
        btnSearch.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnSearch.addActionListener(e -> filterAndDisplayPayrolls());
        m_historyFilterPanel.add(btnSearch);

        JPanel tabBar = new JPanel(new BorderLayout());
        tabBar.setBackground(Color.WHITE);
        tabBar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR),
            BorderFactory.createEmptyBorder(0, 12, 0, 12)
        ));

        JPanel tabLabelsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 24, 0));
        tabLabelsPanel.setOpaque(false);

        tabBar.add(tabLabelsPanel, BorderLayout.WEST);
        tabBar.add(m_historyFilterPanel, BorderLayout.EAST);

        m_detailCardsContainer = new JPanel(new CardLayout());
        m_detailCardsContainer.setOpaque(false);
        m_detailCardLayout = (CardLayout) m_detailCardsContainer.getLayout();

        m_detailCardsContainer.add(createProfileTab(), "PROFILE");
        m_detailCardsContainer.add(createPayrollTab(), "PAYROLL");
        m_detailCardsContainer.add(createCommissionsTab(), "COMMISSIONS");
        m_detailCardsContainer.add(createHistoryTab(), "HISTORY");

        m_tabManager = new TabManager(tabLabelsPanel, m_detailCardsContainer, m_detailCardLayout, tabBar);
        m_tabManager.setup();

        mainTabPanel.add(tabBar, BorderLayout.NORTH);
        mainTabPanel.add(m_detailCardsContainer, BorderLayout.CENTER);
        return mainTabPanel;
    }

    private JPanel createGridRow(JComponent... cells) {
        JPanel row = new JPanel(new GridLayout(1, cells.length, 8, 0));
        row.setOpaque(false);
        for (JComponent cell : cells) {
            row.add(cell);
        }
        return row;
    }

    private JPanel createGridCell(String label, JComponent input, boolean readOnly) {
        JPanel cell = new JPanel(new BorderLayout(0, 4));
        Color bg = readOnly ? new Color(248, 250, 252) : Color.WHITE;
        cell.setBackground(bg);
        cell.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(203, 213, 225), 1),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));

        JLabel lbl = new JLabel(label.toUpperCase());
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 9));
        lbl.setForeground(new Color(100, 116, 139));

        if (input instanceof JTextField) {
            ((JTextField) input).setBorder(null);
            ((JTextField) input).setBackground(bg);
        } else if (input instanceof JComboBox) {
            ((JComboBox<?>) input).setBorder(null);
            ((JComboBox<?>) input).setBackground(bg);
        } else if (input instanceof JPanel) {
            input.setOpaque(false);
            for (Component c : ((JPanel) input).getComponents()) {
                if (c instanceof JTextField) {
                    ((JTextField) c).setBorder(null);
                    ((JTextField) c).setBackground(bg);
                }
            }
        }

        cell.add(lbl, BorderLayout.NORTH);
        cell.add(input, BorderLayout.CENTER);
        return cell;
    }

    private JPanel createGridCell(String label, JComponent input) {
        return createGridCell(label, input, false);
    }

    private JComponent createProfileTab() {
        JPanel card = createCardPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        // Header Title for the unified card
        JLabel heading = new JLabel("Ficha de Expediente");
        heading.setFont(SECTION_FONT);
        heading.setForeground(TEXT_PRIMARY);
        heading.setBorder(BorderFactory.createEmptyBorder(0, 0, 18, 0));
        card.add(heading);

        // Fields Initialization
        m_txtEmployeeCode = createTextField();
        m_txtDepartment = createTextField();
        m_txtPositionTitle = createTextField();
        m_cmbContractType = createComboBox("Indefinido", "Fijo", "Temporal", "Por horas", "Comisionista");
        m_cmbEmployeeStatus = createComboBox("Activo", "En permiso", "Suspendido", "Retirado");
        m_txtHireDate = createReadOnlyField();
        m_cmbPayrollFrequency = createComboBox("Mensual", "Quincenal", "Semanal", "Por evento");
        m_txtEmergencyContact = createTextField();
        m_txtTaxId = createTextField();

        // 3 Column Grid Rows
        card.add(createGridRow(
            createGridCell("Codigo interno", m_txtEmployeeCode),
            createGridCell("Fecha de ingreso", createDateFieldGroup(m_txtHireDate, this::chooseHireDate)),
            createGridCell("Departamento", m_txtDepartment)
        ));
        card.add(Box.createVerticalStrut(12));
        card.add(createGridRow(
            createGridCell("Puesto", m_txtPositionTitle),
            createGridCell("Contrato", m_cmbContractType),
            createGridCell("Estado", m_cmbEmployeeStatus)
        ));
        card.add(Box.createVerticalStrut(12));
        card.add(createGridRow(
            createGridCell("Frecuencia de nomina", m_cmbPayrollFrequency),
            createGridCell("Identificacion fiscal", m_txtTaxId),
            createGridCell("Contacto de emergencia", m_txtEmergencyContact)
        ));
        card.add(Box.createVerticalStrut(24));

        // Add Notes text area full-width at the bottom
        JLabel notesLabel = createFieldLabel("Observaciones y trazabilidad");
        notesLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        notesLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
        card.add(notesLabel);

        m_txtNotes = createTextArea(4);
        JScrollPane scrollNotes = new JScrollPane(m_txtNotes);
        scrollNotes.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        scrollNotes.getVerticalScrollBar().setUnitIncrement(18);
        card.add(scrollNotes);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(card, BorderLayout.NORTH);

        return wrapScrollable(wrapper);
    }

    private JComponent createPayrollTab() {
        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));

        body.add(createCompensationCard2Col());
        body.add(Box.createVerticalStrut(16));
        body.add(createPayrollPeriodCard2Col());
        body.add(Box.createVerticalStrut(16));
        body.add(createPreviewCard2Col());

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(body, BorderLayout.NORTH);

        return wrapScrollable(wrapper);
    }

    private JComponent createHistoryTab() {
        JPanel mainPanel = new JPanel();
        mainPanel.setOpaque(false);
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        // 2. Center Section: Historial de Nóminas Table
        JPanel tableCard = createCardPanel();
        tableCard.setLayout(new BorderLayout(0, 12));

        JLabel lblTableTitle = new JLabel("HISTORIAL DE NÓMINAS");
        lblTableTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTableTitle.setForeground(new Color(15, 76, 129));
        tableCard.add(lblTableTitle, BorderLayout.NORTH);

        m_historyModel = new DefaultTableModel(
            new String[] { "ID", "Tipo Nómina", "Clase Nómina", "Año", "Mes", "Correlativo", "Nro. Registros", "Nro. Trabajadores", "Fecha Registro", "Monto Neto Total", "Estado", "Progreso", "Acción" },
            0) {
            private static final long serialVersionUID = 1L;
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        m_historyTable = new JTable(m_historyModel);
        m_historyTable.setFont(BODY_FONT);
        m_historyTable.setRowHeight(36);
        m_historyTable.setFillsViewportHeight(true);
        m_historyTable.setShowGrid(true);
        m_historyTable.setGridColor(BORDER_COLOR);
        m_historyTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Modern headers
        m_historyTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 11));
        m_historyTable.getTableHeader().setBackground(new Color(230, 242, 255));
        m_historyTable.getTableHeader().setForeground(new Color(15, 76, 129));
        m_historyTable.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR));

        // Hide ID column index 0
        m_historyTable.removeColumn(m_historyTable.getColumnModel().getColumn(0));

        // Align numeric/data columns
        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
        m_historyTable.getColumnModel().getColumn(8).setCellRenderer(rightRenderer); // Monto Neto Total
        
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        m_historyTable.getColumnModel().getColumn(2).setCellRenderer(centerRenderer); // Año
        m_historyTable.getColumnModel().getColumn(3).setCellRenderer(centerRenderer); // Mes
        m_historyTable.getColumnModel().getColumn(4).setCellRenderer(centerRenderer); // Correlativo
        m_historyTable.getColumnModel().getColumn(5).setCellRenderer(centerRenderer); // Nro Registros
        m_historyTable.getColumnModel().getColumn(6).setCellRenderer(centerRenderer); // Nro Trabajadores
        m_historyTable.getColumnModel().getColumn(7).setCellRenderer(centerRenderer); // Fecha Registro
        m_historyTable.getColumnModel().getColumn(9).setCellRenderer(centerRenderer); // Estado
        m_historyTable.getColumnModel().getColumn(10).setCellRenderer(centerRenderer); // Progreso

        // Render cell actions style for column 11 (Acción)
        DefaultTableCellRenderer actionRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(SwingConstants.CENTER);
                setForeground(new java.awt.Color(37, 99, 235)); // Brand blue
                setFont(new Font("Segoe UI", Font.BOLD, 11));
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                return this;
            }
        };
        m_historyTable.getColumnModel().getColumn(11).setCellRenderer(actionRenderer); // Acción (visible 11)

        // Mouse click listener to trigger PDF preview on action column
        m_historyTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                int col = m_historyTable.columnAtPoint(e.getPoint());
                int row = m_historyTable.rowAtPoint(e.getPoint());
                if (row != -1 && col != -1) {
                    int modelCol = m_historyTable.convertColumnIndexToModel(col);
                    if (modelCol == 12) { // Column index of Actions in model (ID is index 0)
                        showPayrollPreviewDialog(row);
                    }
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(m_historyTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        scrollPane.getViewport().setBackground(Color.WHITE);
        tableCard.add(scrollPane, BorderLayout.CENTER);

        // Action bar. The table already contains the complete, filterable history.
        JPanel tableBottom = new JPanel(new BorderLayout(0, 8));
        tableBottom.setOpaque(false);

        // Actions
        JPanel actionsBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        actionsBar.setOpaque(false);

        m_lblSelectedHistoryPeriod = new JLabel("Selecciona un registro de nómina de la lista");
        m_lblSelectedHistoryPeriod.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        m_lblSelectedHistoryPeriod.setForeground(TEXT_SECONDARY);

        JButton btnDeletePayroll = createCompactButton("Eliminar Nómina");
        btnDeletePayroll.setIcon(new ModernActionIcon(ModernActionIcon.Type.DELETE, 17, Color.WHITE));
        btnDeletePayroll.setBackground(new Color(239, 68, 68)); // Red color
        btnDeletePayroll.setForeground(Color.WHITE);
        btnDeletePayroll.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnDeletePayroll.setEnabled(false);
        btnDeletePayroll.setVisible(canDeletePayroll());

        JButton btnPrintReceipt = createCompactButton("Ver Recibo de Pago");
        btnPrintReceipt.setIcon(new ModernActionIcon(ModernActionIcon.Type.DOCUMENT, 17, Color.WHITE));
        btnPrintReceipt.setBackground(new Color(30, 80, 160)); // Blue color
        btnPrintReceipt.setForeground(Color.WHITE);
        btnPrintReceipt.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnPrintReceipt.setEnabled(false);

        actionsBar.add(m_lblSelectedHistoryPeriod);
        actionsBar.add(btnDeletePayroll);
        actionsBar.add(btnPrintReceipt);
        tableBottom.add(actionsBar, BorderLayout.SOUTH);
        tableCard.add(tableBottom, BorderLayout.SOUTH);

        mainPanel.add(tableCard);
        mainPanel.add(Box.createVerticalStrut(16));

        // 3. Bottom Section: Resultados de Errores Table
        JPanel errorCard = createCardPanel();
        errorCard.setLayout(new BorderLayout(0, 12));
        
        JLabel lblErrorTitle = new JLabel("RESULTADOS DE ERRORES");
        lblErrorTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblErrorTitle.setForeground(new Color(220, 38, 38)); // Warning Red
        errorCard.add(lblErrorTitle, BorderLayout.NORTH);

        m_errorModel = new DefaultTableModel(
            new String[] { "N°", "Tipo Doc.", "Nro. Doc.", "Apellidos y Nombres", "Tipo Reg. AIRHSP", "Nro. Reg. AIRHSP", "Tipo Concepto", "Codigo Concepto", "Descripción", "Fuente Fto.", "Monto", "Observaciones" },
            0) {
            private static final long serialVersionUID = 1L;
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        m_errorTable = new JTable(m_errorModel);
        m_errorTable.setFont(BODY_FONT);
        m_errorTable.setRowHeight(32);
        m_errorTable.setFillsViewportHeight(true);
        m_errorTable.setShowGrid(true);
        m_errorTable.setGridColor(BORDER_COLOR);

        m_errorTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 11));
        m_errorTable.getTableHeader().setBackground(new Color(254, 226, 226)); // Warning Red Header bg
        m_errorTable.getTableHeader().setForeground(new Color(153, 27, 27));
        m_errorTable.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR));

        // Center renderers
        for (int i = 0; i < m_errorTable.getColumnCount(); i++) {
            if (i != 3 && i != 8) {
                m_errorTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
            }
        }

        JScrollPane errorScroll = new JScrollPane(m_errorTable);
        errorScroll.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        errorScroll.getViewport().setBackground(Color.WHITE);
        errorScroll.setPreferredSize(new Dimension(0, 160));
        errorCard.add(errorScroll, BorderLayout.CENTER);

        mainPanel.add(errorCard);

        // Selections listener
        m_historyTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = m_historyTable.getSelectedRow();
                if (row != -1) {
                    int modelRow = m_historyTable.convertRowIndexToModel(row);
                    String period = (String) m_historyModel.getValueAt(modelRow, 4) + "/" + (String) m_historyModel.getValueAt(modelRow, 3);
                    String net = (String) m_historyModel.getValueAt(modelRow, 9);

                    m_lblSelectedHistoryPeriod.setText("Nómina: " + period + " (" + net + ")");
                    btnDeletePayroll.setEnabled(true);
                    btnPrintReceipt.setEnabled(true);
                } else {
                    m_lblSelectedHistoryPeriod.setText("Selecciona un registro de nómina de la lista");
                    btnDeletePayroll.setEnabled(false);
                    btnPrintReceipt.setEnabled(false);
                }
            }
        });

        // Wire actions
        btnDeletePayroll.addActionListener(ev -> {
            if (!canDeletePayroll()) {
                JOptionPane.showMessageDialog(this, "No tienes permiso para eliminar registros de nómina.",
                        "Acceso restringido", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int row = m_historyTable.getSelectedRow();
            if (row != -1) {
                int modelRow = m_historyTable.convertRowIndexToModel(row);
                String payrollId = (String) m_historyModel.getValueAt(modelRow, 0);
                int confirm = JOptionPane.showConfirmDialog(this,
                    "¿Estás seguro de que deseas eliminar este registro de nómina de forma permanente?",
                    "Eliminar Registro de Nómina", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    try {
                        new com.openbravo.data.loader.PreparedSentence<String, Object>(m_App.getSession(),
                            "DELETE FROM HR_PAYROLL WHERE ID = ?",
                            com.openbravo.data.loader.SerializerWriteString.INSTANCE).exec(payrollId);

                        // Sincronizar eliminación con la contabilidad y gastos del panel
                        VoltiumSyncService.eliminarNominaAsync(payrollId);
                        
                        PeopleInfo selected = m_employeeList.getSelectedValue();
                        if (selected != null) {
                            loadHistory(selected.getID());
                            refreshDashboard();
                        }
                        JOptionPane.showMessageDialog(this, "Nómina de pago eliminada correctamente.");
                    } catch (BasicException ex) {
                        showError("No se pudo eliminar el registro de nómina.", ex);
                    }
                }
            }
        });

        btnPrintReceipt.addActionListener(ev -> {
            int row = m_historyTable.getSelectedRow();
            if (row != -1) {
                showPayrollPreviewDialog(row);
            }
        });

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(mainPanel, BorderLayout.NORTH);

        return wrapScrollable(wrapper);
    }

    private JButton createPaginationButton(String text, boolean active) {
        JButton b = new JButton(text) {
            @Override
            protected void paintComponent(java.awt.Graphics g) {
                if (active) {
                    java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
                    g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(new Color(15, 76, 129));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 4, 4);
                    g2.dispose();
                }
                super.paintComponent(g);
            }
        };
        b.setFont(new Font("Segoe UI", active ? Font.BOLD : Font.PLAIN, 11));
        b.setForeground(active ? Color.WHITE : new Color(15, 76, 129));
        b.setBackground(active ? new Color(15, 76, 129) : Color.WHITE);
        b.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(209, 213, 219)),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        b.setContentAreaFilled(false);
        b.setOpaque(false);
        b.setFocusable(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    private JComponent createCommissionsTab() {
        JPanel body = new JPanel(new BorderLayout(0, 16));
        body.setOpaque(false);
        body.setBorder(new EmptyBorder(16, 16, 16, 16));

        JPanel summary = new JPanel(new GridLayout(1, 2, 16, 0));
        summary.setOpaque(false);

        m_lblCommEarnedMonth = createMetricValueLabel();
        m_lblCommEarnedTotal = createMetricValueLabel();

        summary.add(createCommissionMetricCard("Comision este mes", m_lblCommEarnedMonth, SUCCESS_COLOR));
        summary.add(createCommissionMetricCard("Comision historica (Total)", m_lblCommEarnedTotal, BRAND_COLOR));

        m_commModel = new DefaultTableModel(
                new Object[] { "Fecha Venta", "Ticket ID", "Monto Venta", "Comision Generada" }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        m_commTable = new JTable(m_commModel);
        m_commTable.setFont(BODY_FONT);
        m_commTable.setRowHeight(32);
        m_commTable.getTableHeader().setFont(LABEL_FONT);

        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
        m_commTable.getColumnModel().getColumn(2).setCellRenderer(rightRenderer);
        m_commTable.getColumnModel().getColumn(3).setCellRenderer(rightRenderer);

        JPanel tableContainer = createSectionCard("Desglose de ventas y comisiones");
        tableContainer.setLayout(new BorderLayout());
        tableContainer.add(new JScrollPane(m_commTable), BorderLayout.CENTER);

        body.add(summary, BorderLayout.NORTH);
        body.add(tableContainer, BorderLayout.CENTER);

        return wrapScrollable(body);
    }

    private JPanel createCommissionMetricCard(String title, JLabel valueLabel, Color accent) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                new EmptyBorder(20, 20, 20, 20)));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(LABEL_FONT);
        titleLabel.setForeground(TEXT_SECONDARY);

        valueLabel.setForeground(accent);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));

        card.add(titleLabel);
        card.add(Box.createVerticalStrut(10));
        card.add(valueLabel);
        return card;
    }



    private JPanel createCompensationCard2Col() {
        JPanel card = createCardPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        JLabel heading = new JLabel("Compensacion y Prestaciones");
        heading.setFont(SECTION_FONT);
        heading.setForeground(TEXT_PRIMARY);
        heading.setBorder(BorderFactory.createEmptyBorder(0, 0, 18, 0));
        card.add(heading);

        // Fields Initialization
        m_txtBaseSalary = createTextField();
        m_txtCommissionRate = createTextField();
        m_txtTransportAllowance = createTextField();
        m_txtOtherAllowances = createTextField();
        m_txtBonusAmount = createTextField();
        m_txtDeductionRate = createTextField();

        m_cmbPensionType = createComboBox("N/A", "AFP Integra", "AFP Prima", "AFP Profuturo", "ONP", "Privado");
        m_txtHealthInsurance = createTextField();
        m_txtSocialSecurityId = createTextField();
        m_txtBankName = createTextField();
        m_txtBankAccount = createTextField();

        // 4 Columns Grid Cells layout! Incredibly compact and beautiful!
        card.add(createGridRow(
            createGridCell("Salario base", m_txtBaseSalary, false),
            createGridCell("Comision (%)", m_txtCommissionRate, false),
            createGridCell("Auxilio transporte", m_txtTransportAllowance, false),
            createGridCell("Asignaciones extra", m_txtOtherAllowances, false)
        ));
        card.add(Box.createVerticalStrut(12));
        card.add(createGridRow(
            createGridCell("Bonificacion", m_txtBonusAmount, false),
            createGridCell("Deducciones (%)", m_txtDeductionRate, false),
            createGridCell("Pension", m_cmbPensionType, false),
            createGridCell("Seguro de salud", m_txtHealthInsurance, false)
        ));
        card.add(Box.createVerticalStrut(12));
        card.add(createGridRow(
            createGridCell("Seguridad social", m_txtSocialSecurityId, false),
            createGridCell("Banco", m_txtBankName, false),
            createGridCell("Cuenta bancaria", m_txtBankAccount, false),
            new JPanel() {{ setOpaque(false); }} // Dummy panel to fill 4th cell nicely
        ));

        return card;
    }

    private JPanel createPayrollPeriodCard2Col() {
        JPanel card = createCardPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        JLabel heading = new JLabel("Programacion del Periodo de Pago");
        heading.setFont(SECTION_FONT);
        heading.setForeground(TEXT_PRIMARY);
        heading.setBorder(BorderFactory.createEmptyBorder(0, 0, 18, 0));
        card.add(heading);

        // Fields Initialization
        m_txtPeriodLabel = createTextField();
        m_txtPeriodStart = createReadOnlyField();
        m_txtPeriodEnd = createReadOnlyField();
        m_txtPaymentDate = createReadOnlyField();
        m_cmbPaymentMethod = createComboBox("Transferencia", "Efectivo", "Cheque", "Deposito");
        m_cmbPayrollStatus = createComboBox("Pagado", "Programado", "En revision");
        m_txtPayrollNotes = createTextArea(3);

        // 3 Column Grid Rows
        card.add(createGridRow(
            createGridCell("Periodo", m_txtPeriodLabel, false),
            createGridCell("Inicio", createDateFieldGroup(m_txtPeriodStart, this::choosePeriodStart), false),
            createGridCell("Fin", createDateFieldGroup(m_txtPeriodEnd, this::choosePeriodEnd), false)
        ));
        card.add(Box.createVerticalStrut(12));
        card.add(createGridRow(
            createGridCell("Fecha de pago", createDateFieldGroup(m_txtPaymentDate, this::choosePaymentDate), false),
            createGridCell("Metodo", m_cmbPaymentMethod, false),
            createGridCell("Estado de pago", m_cmbPayrollStatus, false)
        ));
        card.add(Box.createVerticalStrut(18));

        // Period Notes full width at the bottom
        JLabel notesLabel = createFieldLabel("Notas del periodo");
        notesLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        notesLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
        card.add(notesLabel);

        JScrollPane scrollNotes = new JScrollPane(m_txtPayrollNotes);
        scrollNotes.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        scrollNotes.getVerticalScrollBar().setUnitIncrement(18);
        card.add(scrollNotes);

        return card;
    }

    private JPanel createPreviewCard2Col() {
        JPanel card = createCardPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        JLabel heading = new JLabel("Calculo y Registro de Pago");
        heading.setFont(SECTION_FONT);
        heading.setForeground(TEXT_PRIMARY);
        heading.setBorder(BorderFactory.createEmptyBorder(0, 0, 18, 0));
        card.add(heading);

        // Vista previa values
        m_lblPreviewBase = createPreviewValueLabel();
        m_lblPreviewCommission = createPreviewValueLabel();
        m_lblPreviewAllowances = createPreviewValueLabel();
        m_lblPreviewBonus = createPreviewValueLabel();
        m_lblPreviewGross = createPreviewValueLabel();
        m_lblPreviewDeductions = createPreviewValueLabel();
        m_lblPreviewNet = createPreviewValueLabel();
        m_lblPreviewNet.setFont(new Font("Segoe UI", Font.BOLD, 18));
        m_lblPreviewNet.setForeground(SUCCESS_COLOR);

        // Grid cells layout for Vista Previa
        card.add(createGridRow(
            createGridCell("Base salarial", m_lblPreviewBase, true),
            createGridCell("Comisiones", m_lblPreviewCommission, true),
            createGridCell("Asignaciones", m_lblPreviewAllowances, true),
            createGridCell("Bonificaciones", m_lblPreviewBonus, true)
        ));
        card.add(Box.createVerticalStrut(12));
        card.add(createGridRow(
            createGridCell("Ingreso bruto", m_lblPreviewGross, true),
            createGridCell("Deducciones", m_lblPreviewDeductions, true),
            createGridCell("Pago neto", m_lblPreviewNet, true),
            new JPanel() {{ setOpaque(false); }}
        ));
        card.add(Box.createVerticalStrut(16));

        // Process Button
        JButton processButton = createActionButton("Registrar pago de nomina", SUCCESS_COLOR);
        processButton.addActionListener(e -> processPayroll());
        card.add(processButton);

        return card;
    }



    private void registerLiveUpdates() {
        bindTextChange(m_txtSearch, this::filterEmployees);

        bindTextChange(m_txtEmployeeCode, this::updateSpotlightHeader);
        bindTextChange(m_txtDepartment, this::updateSpotlightHeader);
        bindTextChange(m_txtPositionTitle, this::updateSpotlightHeader);
        bindTextChange(m_txtBaseSalary, this::refreshPayrollPreview);
        bindTextChange(m_txtCommissionRate, this::refreshPayrollPreview);
        bindTextChange(m_txtTransportAllowance, this::refreshPayrollPreview);
        bindTextChange(m_txtOtherAllowances, this::refreshPayrollPreview);
        bindTextChange(m_txtBonusAmount, this::refreshPayrollPreview);
        bindTextChange(m_txtDeductionRate, this::refreshPayrollPreview);

        m_cmbEmployeeStatus.addActionListener(e -> updateSpotlightHeader());
        m_cmbContractType.addActionListener(e -> updateSpotlightHeader());
        m_cmbPayrollFrequency.addActionListener(e -> refreshPayrollPreview());
    }

    private void bindTextChange(JTextComponent component, Runnable action) {
        component.getDocument().addDocumentListener(createDocumentListener(action));
    }

    private DocumentListener createDocumentListener(Runnable action) {
        return new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                action.run();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                action.run();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                action.run();
            }
        };
    }

    private void chooseHireDate() {
        Date chosen = JCalendarDialog.showCalendar(this, m_hireDateValue == null ? new Date() : m_hireDateValue);
        if (chosen != null) {
            m_hireDateValue = chosen;
            m_txtHireDate.setText(formatDate(chosen));
        }
    }

    private void choosePeriodStart() {
        Date chosen = JCalendarDialog.showCalendar(this, m_periodStartValue == null ? new Date() : m_periodStartValue);
        if (chosen != null) {
            m_periodStartValue = chosen;
            m_txtPeriodStart.setText(formatDate(chosen));
            suggestPeriodLabelIfEmpty();
        }
    }

    private void choosePeriodEnd() {
        Date chosen = JCalendarDialog.showCalendar(this, m_periodEndValue == null ? new Date() : m_periodEndValue);
        if (chosen != null) {
            m_periodEndValue = chosen;
            m_txtPeriodEnd.setText(formatDate(chosen));
            suggestPeriodLabelIfEmpty();
        }
    }

    private void choosePaymentDate() {
        Date chosen = JCalendarDialog.showCalendar(this, m_paymentDateValue == null ? new Date() : m_paymentDateValue);
        if (chosen != null) {
            m_paymentDateValue = chosen;
            m_txtPaymentDate.setText(formatDate(chosen));
        }
    }

    private void suggestPeriodLabelIfEmpty() {
        if (!isBlank(m_txtPeriodLabel.getText())) {
            return;
        }

        if (m_periodStartValue != null && m_periodEndValue != null) {
            m_txtPeriodLabel.setText("Periodo " + formatDate(m_periodStartValue) + " - " + formatDate(m_periodEndValue));
        }
    }

    private void resetPeriodDefaults() {
        Calendar calendar = Calendar.getInstance();
        m_paymentDateValue = calendar.getTime();

        calendar.set(Calendar.DAY_OF_MONTH, 1);
        m_periodStartValue = calendar.getTime();

        calendar = Calendar.getInstance();
        m_periodEndValue = calendar.getTime();

        m_txtPeriodStart.setText(formatDate(m_periodStartValue));
        m_txtPeriodEnd.setText(formatDate(m_periodEndValue));
        m_txtPaymentDate.setText(formatDate(m_paymentDateValue));
        suggestPeriodLabelIfEmpty();
    }

    private void filterEmployees() {
        String query = normalize(m_txtSearch.getText());
        String selectedId = getSelectedEmployeeId();

        m_employeeListModel.clear();
        for (PeopleInfo person : m_allEmployees) {
            String name = normalize(person.getName());
            String id = normalize(person.getID());
            if (query.isEmpty() || name.contains(query) || id.contains(query)) {
                m_employeeListModel.addElement(person);
            }
        }

        int total = m_employeeListModel.getSize();
        m_lblDirectoryCount.setText(total + (total == 1 ? " empleado registrado" : " empleados registrados"));

        if (total == 0) {
            clearScreenForNoSelection();
            return;
        }

        if (selectedId != null) {
            for (int i = 0; i < total; i++) {
                if (selectedId.equals(m_employeeListModel.get(i).getID())) {
                    m_employeeList.setSelectedIndex(i);
                    return;
                }
            }
        }

        // Eliminamos la auto-seleccion del primer empleado para que el Dashboard sea la vista inicial
        /*
        if (m_employeeList.getSelectedIndex() < 0) {
            m_employeeList.setSelectedIndex(0);
        }
        */
    }

    private void loadSelectedEmployee() {
        PeopleInfo selected = m_employeeList.getSelectedValue();
        if (selected == null) {
            clearScreenForNoSelection();
            return;
        }

        try {
            m_workspaceLayout.show(m_workspaceCards, "DETAIL");
            Object[] profile = dlHR.getEmployeeHR(selected.getID());
            if (profile == null) {
                profile = dlHR.createDefaultEmployeeHR(selected.getID());
                profile[DataLogicHR.EMPLOYEE_CODE] = buildEmployeeCode(selected);
            }

            // Cargar foto de empleado si existe
            m_employeeImage = getEmployeeImage(selected.getID());
            if (m_employeeImage != null) {
                m_lblAvatarInitials.setVisible(false);
            } else {
                m_lblAvatarInitials.setVisible(true);
            }
            if (m_avatarPanel != null) {
                m_avatarPanel.repaint();
            }

            writeProfile(profile);
            loadHistory(selected.getID());
            loadCommissions(selected.getID());
            updateSpotlightHeader();
            refreshPayrollPreview();
        } catch (BasicException e) {
            showError("No se pudo cargar el expediente del empleado.", e);
        }
    }

    private void writeProfile(Object[] profile) {
        m_txtEmployeeCode.setText(asText(profile[DataLogicHR.EMPLOYEE_CODE]));
        m_txtDepartment.setText(asText(profile[DataLogicHR.EMPLOYEE_DEPARTMENT]));
        m_txtPositionTitle.setText(asText(profile[DataLogicHR.EMPLOYEE_POSITION_TITLE]));
        m_cmbContractType.setSelectedItem(defaultValue(profile[DataLogicHR.EMPLOYEE_CONTRACT_TYPE], "Indefinido"));
        m_cmbEmployeeStatus.setSelectedItem(defaultValue(profile[DataLogicHR.EMPLOYEE_STATUS], "Activo"));

        m_hireDateValue = (Date) profile[DataLogicHR.EMPLOYEE_HIRE_DATE];
        m_txtHireDate.setText(formatDate(m_hireDateValue));

        m_cmbPayrollFrequency.setSelectedItem(defaultValue(profile[DataLogicHR.EMPLOYEE_PAYROLL_FREQUENCY], "Mensual"));
        m_txtBaseSalary.setText(formatDecimal(profile[DataLogicHR.EMPLOYEE_BASE_SALARY]));
        m_txtCommissionRate.setText(formatDecimal(profile[DataLogicHR.EMPLOYEE_COMMISSION_RATE]));
        m_txtTransportAllowance.setText(formatDecimal(profile[DataLogicHR.EMPLOYEE_TRANSPORT_ALLOWANCE]));
        m_txtOtherAllowances.setText(formatDecimal(profile[DataLogicHR.EMPLOYEE_OTHER_ALLOWANCES]));
        m_txtBonusAmount.setText(formatDecimal(profile[DataLogicHR.EMPLOYEE_BONUS_AMOUNT]));
        m_txtDeductionRate.setText(formatDecimal(profile[DataLogicHR.EMPLOYEE_DEDUCTION_RATE]));
        m_cmbPensionType.setSelectedItem(defaultValue(profile[DataLogicHR.EMPLOYEE_PENSION_TYPE], "N/A"));
        m_txtHealthInsurance.setText(asText(profile[DataLogicHR.EMPLOYEE_HEALTH_INSURANCE]));
        m_txtSocialSecurityId.setText(asText(profile[DataLogicHR.EMPLOYEE_SOCIAL_SECURITY_ID]));
        m_txtBankName.setText(asText(profile[DataLogicHR.EMPLOYEE_BANK_NAME]));
        m_txtBankAccount.setText(asText(profile[DataLogicHR.EMPLOYEE_BANK_ACCOUNT]));
        m_txtTaxId.setText(asText(profile[DataLogicHR.EMPLOYEE_TAX_ID]));
        m_txtEmergencyContact.setText(asText(profile[DataLogicHR.EMPLOYEE_EMERGENCY_CONTACT]));
        m_txtNotes.setText(asText(profile[DataLogicHR.EMPLOYEE_NOTES]));

        if (isBlank(m_txtPeriodLabel.getText())) {
            suggestPeriodLabelIfEmpty();
        }
    }

    private void loadCommissions(String employeeId) {
        if (m_commModel == null) return;
        m_commModel.setRowCount(0);
        try {
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.DAY_OF_MONTH, 1);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            Date startOfMonth = cal.getTime();
            Date now = new Date();

            double earnedMonth = dlHR.getCommissionsEarned(employeeId, startOfMonth, now);
            m_lblCommEarnedMonth.setText(formatCurrency(earnedMonth));

            // Histórico (Desde siempre hasta ahora)
            cal.set(Calendar.YEAR, 2000);
            Date farPast = cal.getTime();
            double earnedTotal = dlHR.getCommissionsEarned(employeeId, farPast, now);
            m_lblCommEarnedTotal.setText(formatCurrency(earnedTotal));

            // Cargar desglose del mes
            List<Object[]> sales = dlHR.getCommissionSales(employeeId, startOfMonth, now);
            Object[] employee = dlHR.getEmployeeHR(employeeId);
            double rate = employee == null ? 0.0 : (Double) employee[DataLogicHR.EMPLOYEE_COMMISSION_RATE];

            for (Object[] row : sales) {
                double saleAmount = (Double) row[2];
                double commAmount = saleAmount * (rate / 100.0);
                m_commModel.addRow(new Object[] {
                        formatDateTime((Date) row[0]),
                        row[1],
                        formatCurrency(saleAmount),
                        formatCurrency(commAmount)
                });
            }
        } catch (BasicException e) {
            showError("No se pudieron cargar las comisiones.", e);
        }
    }

    private void clearScreenForNoSelection() {
        m_workspaceLayout.show(m_workspaceCards, "DASHBOARD");
        refreshDashboard();
        
        m_lblSelectedCode.setText("ID de Colaborador: -");
        m_lblSelectedStatus.setText("â—  Sin estado");
        m_lblSelectedStatus.setBackground(new Color(241, 245, 249));
        m_lblSelectedStatus.setForeground(new Color(71, 85, 105));
        m_lblSelectedName.setText("Sin empleado seleccionado");
        m_lblSelectedMeta.setText("Selecciona un colaborador para comenzar.");
        if (m_lblSelectedEmail != null) m_lblSelectedEmail.setText("");
        if (m_lblAvatarInitials != null) {
            m_lblAvatarInitials.setText("?");
            m_lblAvatarInitials.setVisible(true);
        }
        m_employeeImage = null;
        if (m_avatarPanel != null) {
            m_avatarPanel.repaint();
        }
        if (m_lblSelectedHireDate != null) m_lblSelectedHireDate.setText("Fecha de ingreso: -");
        m_lblMetricBaseSalary.setText(formatCurrency(0.0));
        m_lblMetricNetSalary.setText(formatCurrency(0.0));
        m_lblMetricFrequency.setText("Sin definir");
        m_lblMetricLastPayroll.setText("Sin pagos");

        m_txtEmployeeCode.setText("");
        m_txtDepartment.setText("");
        m_txtPositionTitle.setText("");
        m_cmbContractType.setSelectedItem("Indefinido");
        m_cmbEmployeeStatus.setSelectedItem("Activo");
        m_hireDateValue = null;
        m_txtHireDate.setText("");
        m_cmbPayrollFrequency.setSelectedItem("Mensual");
        m_txtEmergencyContact.setText("");
        m_txtTaxId.setText("");
        m_txtNotes.setText("");

        m_txtBaseSalary.setText("0");
        m_txtCommissionRate.setText("0");
        m_txtTransportAllowance.setText("0");
        m_txtOtherAllowances.setText("0");
        m_txtBonusAmount.setText("0");
        m_txtDeductionRate.setText("9");
        m_cmbPensionType.setSelectedItem("N/A");
        m_txtHealthInsurance.setText("");
        m_txtSocialSecurityId.setText("");
        m_txtBankName.setText("");
        m_txtBankAccount.setText("");

        m_txtPeriodLabel.setText("");
        m_cmbPaymentMethod.setSelectedItem("Transferencia");
        m_cmbPayrollStatus.setSelectedItem("Pagado");
        m_txtPayrollNotes.setText("");
        resetPeriodDefaults();

        if (m_historyModel != null) {
            m_historyModel.setRowCount(0);
        }

        refreshPayrollPreview();
    }

    private boolean saveEmployeeProfile(boolean showMessage) {
        PeopleInfo selected = m_employeeList.getSelectedValue();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Selecciona un empleado antes de guardar.");
            return false;
        }

        try {
            Object[] empValues = buildEmployeeProfile(selected);
            dlHR.saveEmployeeHR(empValues);
            updateSpotlightHeader();
            refreshPayrollPreview();

            // Sincronizar colaborador con ERP RRHH en panel central
            try {
                VoltiumSyncService.EmployeePayload emp = new VoltiumSyncService.EmployeePayload();
                emp.employeeId = selected.getID();
                emp.employeeCode = (String) empValues[1];
                emp.name = selected.getName();
                emp.area = (String) empValues[2];
                emp.role = (String) empValues[3];
                emp.status = (String) empValues[5];
                emp.date = (m_hireDateValue != null) ? new SimpleDateFormat("yyyy-MM-dd").format(m_hireDateValue) : null;
                emp.salary = (empValues[8] instanceof Number) ? ((Number) empValues[8]).doubleValue() : 0.0;
                VoltiumSyncService.sincronizarEmpleadoRRHHAsync(emp);
            } catch (Throwable ignored) {}

            if (showMessage) {
                JOptionPane.showMessageDialog(this, "Expediente actualizado correctamente.");
            }
            return true;
        } catch (BasicException e) {
            showError("No se pudo guardar el expediente.", e);
            return false;
        }
    }

    private void processPayroll() {
        PeopleInfo selected = m_employeeList.getSelectedValue();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Selecciona un empleado para registrar la nomina.");
            return;
        }

        if (!saveEmployeeProfile(false)) {
            return;
        }

        try {
            PayrollPreview preview = buildPayrollPreview();
            if (preview.grossAmount <= 0.0 && preview.netAmount <= 0.0) {
                JOptionPane.showMessageDialog(this, "Configura al menos un valor salarial antes de procesar la nomina.");
                return;
            }

            String payrollId = java.util.UUID.randomUUID().toString();
            String periodLabel = buildPeriodLabel();
            Date payDate = m_paymentDateValue == null ? new Date() : m_paymentDateValue;
            String payMethod = defaultValue(m_cmbPaymentMethod.getSelectedItem(), "Transferencia");
            String payStatus = defaultValue(m_cmbPayrollStatus.getSelectedItem(), "Pagado");
            String notes = cleanText(m_txtPayrollNotes.getText());
            String operator = getCurrentOperatorName();

            Object[] payroll = new Object[] {
                    payrollId,
                    selected.getID(),
                    periodLabel,
                    m_periodStartValue,
                    m_periodEndValue,
                    payDate,
                    preview.baseSalary,
                    preview.commissions,
                    preview.allowances,
                    preview.bonusAmount,
                    preview.grossAmount,
                    preview.deductions,
                    preview.netAmount,
                    payMethod,
                    payStatus,
                    notes,
                    operator
            };

            dlHR.insertPayroll(payroll);
            loadHistory(selected.getID());
            refreshPayrollPreview();

            // Sincronizar pago de nómina con la contabilidad y gastos de Voltium Sanrey en el panel central
            VoltiumSyncService.PayrollPayload payload = new VoltiumSyncService.PayrollPayload();
            payload.payrollId = payrollId;
            payload.employeeId = selected.getID();
            payload.employeeName = selected.getName();
            payload.periodLabel = periodLabel;
            payload.periodStart = (m_periodStartValue != null) ? VoltiumSyncService.formatIsoUtc(m_periodStartValue) : null;
            payload.periodEnd = (m_periodEndValue != null) ? VoltiumSyncService.formatIsoUtc(m_periodEndValue) : null;
            payload.paymentDate = VoltiumSyncService.formatIsoUtc(payDate);
            payload.baseSalary = preview.baseSalary;
            payload.commissions = preview.commissions;
            payload.allowances = preview.allowances;
            payload.bonusAmount = preview.bonusAmount;
            payload.grossAmount = preview.grossAmount;
            payload.deductions = preview.deductions;
            payload.netAmount = preview.netAmount;
            payload.paymentMethod = payMethod;
            payload.status = payStatus;
            payload.notes = notes;
            payload.processedBy = operator;

            VoltiumSyncService.sincronizarNominaAsync(payload);

            JOptionPane.showMessageDialog(this, "Nomina registrada correctamente para " + selected.getName() + ".");
        } catch (BasicException e) {
            showError("No se pudo registrar la nomina.", e);
        }
    }

    private Object[] buildEmployeeProfile(PeopleInfo selected) throws BasicException {
        return new Object[] {
                selected.getID(),
                valueOrFallback(cleanText(m_txtEmployeeCode.getText()), buildEmployeeCode(selected)),
                cleanText(m_txtDepartment.getText()),
                cleanText(m_txtPositionTitle.getText()),
                defaultValue(m_cmbContractType.getSelectedItem(), "Indefinido"),
                defaultValue(m_cmbEmployeeStatus.getSelectedItem(), "Activo"),
                m_hireDateValue,
                defaultValue(m_cmbPayrollFrequency.getSelectedItem(), "Mensual"),
                parseDouble(m_txtBaseSalary.getText()),
                parseDouble(m_txtCommissionRate.getText()),
                parseDouble(m_txtTransportAllowance.getText()),
                parseDouble(m_txtOtherAllowances.getText()),
                parseDouble(m_txtBonusAmount.getText()),
                parseDouble(m_txtDeductionRate.getText()),
                defaultValue(m_cmbPensionType.getSelectedItem(), "N/A"),
                cleanText(m_txtHealthInsurance.getText()),
                cleanText(m_txtSocialSecurityId.getText()),
                cleanText(m_txtBankName.getText()),
                cleanText(m_txtBankAccount.getText()),
                cleanText(m_txtTaxId.getText()),
                cleanText(m_txtEmergencyContact.getText()),
                cleanText(m_txtNotes.getText())
        };
    }

    private PayrollPreview buildPayrollPreview() {
        PayrollPreview preview = new PayrollPreview();
        preview.baseSalary = safeParseDouble(m_txtBaseSalary.getText());
        double commissionRate = safeParseDouble(m_txtCommissionRate.getText());
        double transport = safeParseDouble(m_txtTransportAllowance.getText());
        double extraAllowances = safeParseDouble(m_txtOtherAllowances.getText());
        preview.bonusAmount = safeParseDouble(m_txtBonusAmount.getText());
        double deductionRate = safeParseDouble(m_txtDeductionRate.getText());

        preview.commissions = preview.baseSalary * (commissionRate / 100.0);
        preview.allowances = transport + extraAllowances;
        preview.grossAmount = preview.baseSalary + preview.commissions + preview.allowances + preview.bonusAmount;
        preview.deductions = preview.grossAmount * (deductionRate / 100.0);
        preview.netAmount = preview.grossAmount - preview.deductions;
        return preview;
    }

    private void refreshPayrollPreview() {
        PayrollPreview preview = buildPayrollPreview();

        m_lblPreviewBase.setText(formatCurrency(preview.baseSalary));
        m_lblPreviewCommission.setText(formatCurrency(preview.commissions));
        m_lblPreviewAllowances.setText(formatCurrency(preview.allowances));
        m_lblPreviewBonus.setText(formatCurrency(preview.bonusAmount));
        m_lblPreviewGross.setText(formatCurrency(preview.grossAmount));
        m_lblPreviewDeductions.setText(formatCurrency(preview.deductions));
        m_lblPreviewNet.setText(formatCurrency(preview.netAmount));

        m_lblMetricBaseSalary.setText(formatCurrency(preview.baseSalary));
        m_lblMetricNetSalary.setText(formatCurrency(preview.netAmount));
        m_lblMetricFrequency.setText(defaultValue(m_cmbPayrollFrequency.getSelectedItem(), "Sin definir"));
        updateSpotlightHeader();
    }

    private void loadHistory(String employeeId) {
        m_loadedPayrolls.clear();
        try {
            m_loadedPayrolls = dlHR.getPayrollHistory(employeeId);
            filterAndDisplayPayrolls();
        } catch (BasicException e) {
            showError("No se pudo cargar el historial de pagos.", e);
        }
    }

    private void filterAndDisplayPayrolls() {
        m_historyModel.setRowCount(0);
        if (m_loadedPayrolls == null || m_loadedPayrolls.isEmpty()) {
            m_lblMetricLastPayroll.setText("Sin pagos");
            updateValidationResultsSection(); // Will update the error table with initial errors if any
            return;
        }

        String yearSel = (String) m_cmbHistoryYear.getSelectedItem();
        int monthSel = m_cmbHistoryMonth.getSelectedIndex(); 
        String statusSel = (String) m_cmbHistoryStatus.getSelectedItem();
        String correlativeSel = cleanText(m_txtHistoryCorrelative.getText());

        java.util.Calendar cal = java.util.Calendar.getInstance();
        int seq = m_loadedPayrolls.size();

        for (int i = 0; i < m_loadedPayrolls.size(); i++) {
            Object[] row = m_loadedPayrolls.get(i);
            Date paymentDate = (Date) row[DataLogicHR.PAYROLL_PAYMENT_DATE];
            cal.setTime(paymentDate);

            // Filter by Year
            if (!"Todos".equals(yearSel)) {
                int yearVal = cal.get(java.util.Calendar.YEAR);
                if (yearVal != Integer.parseInt(yearSel)) {
                    continue;
                }
            }

            // Filter by Month
            if (monthSel != 0) {
                int monthVal = cal.get(java.util.Calendar.MONTH); 
                if (monthVal != (monthSel - 1)) {
                    continue;
                }
            }

            // Filter by Status / Progress
            String progressVal = asText(row[DataLogicHR.PAYROLL_STATUS]);
            // If the employee has warnings, progress can mock to "Con errores" instead of "Enviado" (which is active "Pagado")
            boolean hasWarnings = checkIfEmployeeHasWarnings();
            String mappedProgress = "Enviado";
            if ("Programado".equalsIgnoreCase(progressVal) || "En revision".equalsIgnoreCase(progressVal)) {
                mappedProgress = "En revision";
            } else if ("Anulado".equalsIgnoreCase(progressVal)) {
                mappedProgress = "Anulado";
            } else if (hasWarnings) {
                mappedProgress = "Con errores";
            }

            if (!"Todos".equals(statusSel)) {
                if (!statusSel.equalsIgnoreCase(mappedProgress)) {
                    continue;
                }
            }

            // Filter by Correlative
            String corrVal = String.format("%04d", seq - i);
            if (!correlativeSel.isEmpty()) {
                if (!corrVal.contains(correlativeSel)) {
                    continue;
                }
            }

            String monthName = getSpanishMonthName(cal.get(java.util.Calendar.MONTH));

            m_historyModel.addRow(new Object[] {
                row[DataLogicHR.PAYROLL_ID], 
                "01-ACTIVOS", 
                "01-HABERES",
                String.valueOf(cal.get(java.util.Calendar.YEAR)),
                monthName,
                corrVal,
                "0001",
                "1",
                formatDate(paymentDate),
                formatCurrency(asDouble(row[DataLogicHR.PAYROLL_NET_AMOUNT])),
                "1",
                mappedProgress,
                "📄 PDF"
            });
        }

        Object[] latest = m_loadedPayrolls.get(0);
        String latestDate = formatDate((Date) latest[DataLogicHR.PAYROLL_PAYMENT_DATE]);
        m_lblMetricLastPayroll.setText(isBlank(latestDate) ? "Sin pagos" : latestDate);

        updateValidationResultsSection();
    }

    private String getSpanishMonthName(int m) {
        String[] months = {
            "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
            "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
        };
        if (m >= 0 && m < 12) {
            return months[m];
        }
        return "Todos";
    }

    private boolean checkIfEmployeeHasWarnings() {
        double baseSalary = safeParseDouble(m_txtBaseSalary.getText());
        String socialSec = cleanText(m_txtSocialSecurityId.getText());
        String emergency = cleanText(m_txtEmergencyContact.getText());
        String bankAcc = cleanText(m_txtBankAccount.getText());

        return (baseSalary <= 0.0 || socialSec.isEmpty() || emergency.isEmpty() || bankAcc.isEmpty());
    }

    private void updateValidationResultsSection() {
        m_errorModel.setRowCount(0);
        
        PeopleInfo selected = m_employeeList.getSelectedValue();
        if (selected == null) {
            return;
        }

        double baseSalary = safeParseDouble(m_txtBaseSalary.getText());
        String socialSec = cleanText(m_txtSocialSecurityId.getText());
        String emergency = cleanText(m_txtEmergencyContact.getText());
        String bankAcc = cleanText(m_txtBankAccount.getText());
        String taxId = cleanText(m_txtTaxId.getText());

        java.util.List<String[]> warningRows = new java.util.ArrayList<>();
        
        if (baseSalary <= 0.0) {
            warningRows.add(new String[]{ "DNI/RFC", valueOrFallback(taxId, "PENDIENTE"), "El salario base es .00. Revisa la compensación.", "DATO" });
        }
        
        if (socialSec.isEmpty()) {
            warningRows.add(new String[]{ "DNI/RFC", valueOrFallback(taxId, "PENDIENTE"), "No tiene asignado un número de seguridad social.", "SEG. SOC." });
        }

        if (emergency.isEmpty()) {
            warningRows.add(new String[]{ "DNI/RFC", valueOrFallback(taxId, "PENDIENTE"), "Falta el contacto de emergencia en la ficha.", "ADMIN" });
        }

        if (bankAcc.isEmpty()) {
            warningRows.add(new String[]{ "DNI/RFC", valueOrFallback(taxId, "PENDIENTE"), "No se ha configurado la cuenta bancaria.", "BANCO" });
        }

        int rowNum = 1;
        for (String[] warn : warningRows) {
            m_errorModel.addRow(new Object[] {
                String.valueOf(rowNum++),
                warn[0], // Tipo Doc.
                warn[1], // Nro. Doc.
                selected.getName(), // Apellidos y Nombres
                "ADVERTENCIA", // Tipo Registro AIRHSP
                "AIR-092", // Nro. Registro AIRHSP
                "Dato", // Tipo Concepto
                "SYS", // Codigo Concepto
                warn[2], // Descripción
                "RO", // Fuente Financiamiento
                "0.00", // Monto
                "Completar Ficha" // Observaciones
            });
        }
    }

    private void updateSpotlightHeader() {
        PeopleInfo selected = m_employeeList.getSelectedValue();
        if (selected == null) {
            return;
        }

        String employeeCode = valueOrFallback(cleanText(m_txtEmployeeCode.getText()), buildEmployeeCode(selected));
        String department = valueOrFallback(cleanText(m_txtDepartment.getText()), "Departamento por definir");
        String position = valueOrFallback(cleanText(m_txtPositionTitle.getText()), "Puesto por definir");
        String contract = defaultValue(m_cmbContractType.getSelectedItem(), "Indefinido");
        String status = defaultValue(m_cmbEmployeeStatus.getSelectedItem(), "Activo");

        m_lblSelectedCode.setText("ID de Colaborador: " + employeeCode);
        m_lblSelectedStatus.setText("\u25cf  " + status);
        m_lblSelectedStatus.setBackground(statusBgColor(status));
        m_lblSelectedStatus.setForeground(statusFgColor(status));
        m_lblSelectedName.setText(selected.getName());
        m_lblSelectedMeta.setText(position + "  Â·  " + department + "  Â·  " + contract);
        
        // Generate mock professional email based on name
        String cleanName = selected.getName().toLowerCase().replaceAll("[^a-z]", "");
        String mockEmail = cleanName.length() > 10 ? cleanName.substring(0, 10) + "@empresa.mx" : cleanName + "@empresa.mx";
        m_lblSelectedEmail.setText(mockEmail);

        m_lblAvatarInitials.setText(getInitials(selected.getName()));

        if (m_hireDateValue != null) {
            m_lblSelectedHireDate.setText("Fecha de ingreso: " + formatDate(m_hireDateValue));
        } else {
            m_lblSelectedHireDate.setText("Fecha de ingreso: No registrada");
        }
    }

    private Color statusBgColor(String status) {
        if ("Activo".equalsIgnoreCase(status)) return new Color(220, 252, 231); // Light Green
        if ("En permiso".equalsIgnoreCase(status)) return new Color(254, 243, 199); // Light Yellow
        if ("Suspendido".equalsIgnoreCase(status)) return new Color(254, 226, 226); // Light Red
        return new Color(241, 245, 249); // Muted slate
    }

    private Color statusFgColor(String status) {
        if ("Activo".equalsIgnoreCase(status)) return new Color(22, 163, 74); // Success Green
        if ("En permiso".equalsIgnoreCase(status)) return new Color(217, 119, 6); // Yellow/Orange
        if ("Suspendido".equalsIgnoreCase(status)) return new Color(220, 38, 38); // Red
        return new Color(71, 85, 105);
    }

    private String buildPeriodLabel() {
        String label = cleanText(m_txtPeriodLabel.getText());
        if (!isBlank(label)) {
            return label;
        }

        if (m_periodStartValue != null && m_periodEndValue != null) {
            return "Periodo " + formatDate(m_periodStartValue) + " - " + formatDate(m_periodEndValue);
        }

        return "Pago " + formatDate(new Date());
    }

    private String getSelectedEmployeeId() {
        PeopleInfo selected = m_employeeList.getSelectedValue();
        return selected == null ? null : selected.getID();
    }

    private String getCurrentOperatorName() {
        AppUser user = m_App.getAppUserView() == null ? null : m_App.getAppUserView().getUser();
        return user == null ? "Sistema" : user.getName();
    }

    private String buildEmployeeCode(PeopleInfo person) {
        String id = person.getID() == null ? "" : person.getID().replaceAll("[^A-Za-z0-9]", "").toUpperCase();
        if (id.length() >= 6) {
            return "EMP-" + id.substring(id.length() - 6);
        }

        String initials = getInitials(person.getName()).replace("?", "EMP");
        return "EMP-" + initials + (id.isEmpty() ? "001" : id);
    }

    private JPanel createCardPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(CARD_BACKGROUND);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                new EmptyBorder(18, 18, 18, 18)));
        return panel;
    }

    private JPanel createSectionCard(String title) {
        JPanel panel = createCardPanel();
        panel.setLayout(new GridBagLayout());

        JLabel heading = new JLabel(title);
        heading.setFont(SECTION_FONT);
        heading.setForeground(TEXT_PRIMARY);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0, 18, 0);
        panel.add(heading, gbc);
        return panel;
    }

    private GridBagConstraints createFormConstraints() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 0, 12, 12);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        return gbc;
    }

    private void addFormRow(JPanel panel, GridBagConstraints gbc, int rowIndex, String label, JComponent field) {
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        gbc.gridy = rowIndex + 1;
        gbc.weightx = 0.0;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(createFieldLabel(label), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(field, gbc);
    }



    private JLabel createFieldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(LABEL_FONT);
        label.setForeground(TEXT_SECONDARY);
        return label;
    }

    private JTextField createTextField() {
        JTextField field = new JTextField();
        field.setFont(BODY_FONT);
        field.setForeground(TEXT_PRIMARY);
        field.setBackground(Color.WHITE);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                new EmptyBorder(8, 10, 8, 10)));
        return field;
    }

    private JTextField createReadOnlyField() {
        JTextField field = createTextField();
        field.setEditable(false);
        field.setBackground(MUTED_CARD);
        return field;
    }

    private JTextArea createTextArea(int rows) {
        JTextArea area = new JTextArea(rows, 20);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setFont(BODY_FONT);
        area.setForeground(TEXT_PRIMARY);
        area.setBorder(new EmptyBorder(8, 10, 8, 10));
        return area;
    }

    private JComboBox<String> createComboBox(String... values) {
        JComboBox<String> combo = new JComboBox<>(values);
        combo.setFont(BODY_FONT);
        combo.setForeground(TEXT_PRIMARY);
        combo.setBackground(Color.WHITE);
        combo.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        return combo;
    }

    private JPanel createDateFieldGroup(JTextField field, Runnable action) {
        JPanel wrapper = new JPanel(new BorderLayout(8, 0));
        wrapper.setOpaque(false);

        JButton button = createCompactButton("Fecha");
        button.addActionListener(e -> action.run());

        wrapper.add(field, BorderLayout.CENTER);
        wrapper.add(button, BorderLayout.EAST);
        return wrapper;
    }

    private JButton createActionButton(String text, Color color) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(java.awt.Graphics g) {
                java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
                g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        button.setOpaque(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFont(new Font("Segoe UI", Font.BOLD, 11));
        button.setForeground(Color.WHITE);
        button.setBackground(color);
        button.setBorder(new EmptyBorder(6, 12, 6, 12));
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }

    private JButton createCompactButton(String text) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(java.awt.Graphics g) {
                java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
                g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        button.setOpaque(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFont(new Font("Segoe UI", Font.BOLD, 11));
        button.setForeground(BRAND_COLOR);
        button.setBackground(new Color(239, 246, 255));
        button.setBorder(new EmptyBorder(6, 12, 6, 12));
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }

    private boolean canDeletePayroll() {
        AppUser user = m_App == null || m_App.getAppUserView() == null
                ? null : m_App.getAppUserView().getUser();
        return user != null && user.hasPermission("hr.DeletePayroll");
    }

    private JLabel createMetricValueLabel() {
        JLabel label = new JLabel(formatCurrency(0.0));
        label.setFont(METRIC_VALUE_FONT);
        label.setForeground(Color.WHITE);
        return label;
    }

    private JLabel createPreviewValueLabel() {
        JLabel label = new JLabel(formatCurrency(0.0));
        label.setFont(new Font("Segoe UI", Font.BOLD, 16));
        label.setForeground(TEXT_PRIMARY);
        label.setHorizontalAlignment(SwingConstants.RIGHT);
        return label;
    }

    private JComponent wrapScrollable(JComponent content) {
        JScrollPane scrollPane = new JScrollPane(content);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(APP_BACKGROUND);
        scrollPane.getVerticalScrollBar().setUnitIncrement(18);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(18);
        return scrollPane;
    }

    private String formatCurrency(double value) {
        return String.valueOf(Formats.CURRENCY.formatValue(value));
    }

    private String formatDecimal(Object value) {
        return value == null ? "0" : String.valueOf(Formats.DOUBLE.formatValue(asDouble(value)));
    }

    private String formatDate(Date value) {
        return value == null ? "" : DATE_FORMAT.format(value);
    }

    private String formatDateTime(Date value) {
        return value == null ? "" : DATE_TIME_FORMAT.format(value);
    }

    private double asDouble(Object value) {
        return value instanceof Number ? ((Number) value).doubleValue() : 0.0;
    }

    private double safeParseDouble(String text) {
        try {
            return parseDouble(text);
        } catch (BasicException e) {
            return 0.0;
        }
    }

    private double parseDouble(String text) throws BasicException {
        String value = cleanText(text);
        if (isBlank(value)) {
            return 0.0;
        }

        // Limpiar símbolos de moneda y espacios
        value = value.replace("$", "").replace(" ", "").trim();
        
        // Contar ocurrencias para detectar formato
        int commaCount = value.length() - value.replace(",", "").length();
        int dotCount = value.length() - value.replace(".", "").length();

        if (commaCount > 0 && dotCount > 0) {
            // Caso mixto: 1,234,567.89 o 1.234.567,89
            if (value.lastIndexOf(",") > value.lastIndexOf(".")) {
                // Comma es decimal (Europeo/Sudamericano) -> 1.234,56
                value = value.replace(".", "").replace(",", ".");
            } else {
                // Punto es decimal (Americano/Mexicano) -> 1,234.56
                value = value.replace(",", "");
            }
        } else if (commaCount > 1) {
            // Múltiples comas -> Miles (1,234,567)
            value = value.replace(",", "");
        } else if (dotCount > 1) {
            // Múltiples puntos -> Miles (1.234.567)
            value = value.replace(".", "");
        } else if (commaCount == 1) {
            // Una sola coma. Si hay 3 dígitos después, podría ser miles.
            // Si hay 1 o 2, es probable que sea decimal.
            int commaIndex = value.indexOf(",");
            int digitsAfter = value.length() - commaIndex - 1;
            if (digitsAfter == 3 && value.length() > 3) {
                value = value.replace(",", "");
            } else {
                value = value.replace(",", ".");
            }
        }

        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            throw new BasicException("Valor numerico invalido: " + text);
        }
    }

    private String cleanText(String text) {
        return text == null ? "" : text.trim();
    }

    private boolean isBlank(String text) {
        return text == null || text.trim().isEmpty();
    }

    private String normalize(String text) {
        return text == null ? "" : text.trim().toLowerCase();
    }

    private String asText(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private String defaultValue(Object value, String fallback) {
        String text = asText(value);
        return isBlank(text) ? fallback : text;
    }

    private String valueOrFallback(String value, String fallback) {
        return isBlank(value) ? fallback : value;
    }

    private String getInitials(String name) {
        if (isBlank(name)) {
            return "?";
        }

        String[] parts = name.trim().split("\\s+");
        if (parts.length == 1) {
            return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        }

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < parts.length && builder.length() < 2; i++) {
            if (!parts[i].isEmpty()) {
                builder.append(Character.toUpperCase(parts[i].charAt(0)));
            }
        }
        return builder.length() == 0 ? "?" : builder.toString();
    }



    private void showError(String title, Exception e) {
        String message = e.getMessage();
        if (e.getCause() != null && !isBlank(e.getCause().getMessage())) {
            message = e.getCause().getMessage();
        }
        JOptionPane.showMessageDialog(this, title + "\n" + message, "RRHH", JOptionPane.ERROR_MESSAGE);
    }

    @Override
    public String getTitle() {
        return "Recursos Humanos";
    }

    @Override
    public void activate() throws BasicException {
        loadEmployees();
    }

    private void loadEmployees() {
        try {
            m_allEmployees.clear();
            m_allEmployees.addAll(dlAdmin.getPeopleList().list());

            // Auto-populate 'empl' and 'manager' with completed HR data if not setup yet
            for (PeopleInfo person : m_allEmployees) {
                if ("empl".equalsIgnoreCase(person.getName())) {
                    Object[] profile = dlHR.getEmployeeHR(person.getID());
                    if (profile == null || profile[DataLogicHR.EMPLOYEE_BASE_SALARY] == null || (Double) profile[DataLogicHR.EMPLOYEE_BASE_SALARY] <= 0.0) {
                        Object[] newProfile = dlHR.createDefaultEmployeeHR(person.getID());
                        newProfile[DataLogicHR.EMPLOYEE_CODE] = "EMP-003";
                        newProfile[DataLogicHR.EMPLOYEE_DEPARTMENT] = "Ingenieria";
                        newProfile[DataLogicHR.EMPLOYEE_POSITION_TITLE] = "Ingeniero de Software";
                        newProfile[DataLogicHR.EMPLOYEE_CONTRACT_TYPE] = "Indefinido";
                        newProfile[DataLogicHR.EMPLOYEE_STATUS] = "Activo";
                        newProfile[DataLogicHR.EMPLOYEE_PAYROLL_FREQUENCY] = "Mensual";
                        newProfile[DataLogicHR.EMPLOYEE_BASE_SALARY] = 25000.0;
                        newProfile[DataLogicHR.EMPLOYEE_COMMISSION_RATE] = 5.0;
                        newProfile[DataLogicHR.EMPLOYEE_TRANSPORT_ALLOWANCE] = 1200.0;
                        newProfile[DataLogicHR.EMPLOYEE_OTHER_ALLOWANCES] = 800.0;
                        newProfile[DataLogicHR.EMPLOYEE_BONUS_AMOUNT] = 1500.0;
                        newProfile[DataLogicHR.EMPLOYEE_DEDUCTION_RATE] = 8.5;
                        newProfile[DataLogicHR.EMPLOYEE_PENSION_TYPE] = "IMSS";
                        newProfile[DataLogicHR.EMPLOYEE_HEALTH_INSURANCE] = "Seguro MetLife";
                        newProfile[DataLogicHR.EMPLOYEE_SOCIAL_SECURITY_ID] = "NSS-192-348-12";
                        newProfile[DataLogicHR.EMPLOYEE_BANK_NAME] = "BBVA Bancomer";
                        newProfile[DataLogicHR.EMPLOYEE_BANK_ACCOUNT] = "MX-1234-5678-9012";
                        newProfile[DataLogicHR.EMPLOYEE_TAX_ID] = "RFC-EMPL950820-HA1";
                        newProfile[DataLogicHR.EMPLOYEE_EMERGENCY_CONTACT] = "Sofia Alvarez (+52 55 1234 5678)";
                        newProfile[DataLogicHR.EMPLOYEE_NOTES] = "Colaborador clave de Ingenieria. Registro automatizado de prueba.";
                        dlHR.saveEmployeeHR(newProfile);
                    }
                } else if ("manager".equalsIgnoreCase(person.getName())) {
                    Object[] profile = dlHR.getEmployeeHR(person.getID());
                    if (profile == null || profile[DataLogicHR.EMPLOYEE_BASE_SALARY] == null || (Double) profile[DataLogicHR.EMPLOYEE_BASE_SALARY] <= 0.0) {
                        Object[] newProfile = dlHR.createDefaultEmployeeHR(person.getID());
                        newProfile[DataLogicHR.EMPLOYEE_CODE] = "EMP-002";
                        newProfile[DataLogicHR.EMPLOYEE_DEPARTMENT] = "RRHH";
                        newProfile[DataLogicHR.EMPLOYEE_POSITION_TITLE] = "Gerente de RRHH";
                        newProfile[DataLogicHR.EMPLOYEE_CONTRACT_TYPE] = "Indefinido";
                        newProfile[DataLogicHR.EMPLOYEE_STATUS] = "Activo";
                        newProfile[DataLogicHR.EMPLOYEE_PAYROLL_FREQUENCY] = "Mensual";
                        newProfile[DataLogicHR.EMPLOYEE_BASE_SALARY] = 30000.0;
                        newProfile[DataLogicHR.EMPLOYEE_COMMISSION_RATE] = 0.0;
                        newProfile[DataLogicHR.EMPLOYEE_TRANSPORT_ALLOWANCE] = 1500.0;
                        newProfile[DataLogicHR.EMPLOYEE_OTHER_ALLOWANCES] = 1000.0;
                        newProfile[DataLogicHR.EMPLOYEE_BONUS_AMOUNT] = 2000.0;
                        newProfile[DataLogicHR.EMPLOYEE_DEDUCTION_RATE] = 10.0;
                        newProfile[DataLogicHR.EMPLOYEE_PENSION_TYPE] = "IMSS";
                        newProfile[DataLogicHR.EMPLOYEE_HEALTH_INSURANCE] = "Seguro GNP";
                        newProfile[DataLogicHR.EMPLOYEE_SOCIAL_SECURITY_ID] = "NSS-394-182-90";
                        newProfile[DataLogicHR.EMPLOYEE_BANK_NAME] = "Santander";
                        newProfile[DataLogicHR.EMPLOYEE_BANK_ACCOUNT] = "MX-9876-5432-1098";
                        newProfile[DataLogicHR.EMPLOYEE_TAX_ID] = "RFC-MGR900101-HB2";
                        newProfile[DataLogicHR.EMPLOYEE_EMERGENCY_CONTACT] = "Carlos Ruiz (+52 55 9876 5432)";
                        newProfile[DataLogicHR.EMPLOYEE_NOTES] = "Gerente de RRHH. Registro automatizado de prueba.";
                        dlHR.saveEmployeeHR(newProfile);
                    }
                }
            }

            filterEmployees();
        } catch (BasicException e) {
            showError("No se pudo cargar la lista de personal.", e);
        }
    }

    @Override
    public boolean deactivate() {
        return true;
    }

    public void showPayrollPreviewDialog(int row) {
        if (row == -1) return;
        try {
            int modelRow = m_historyTable.convertRowIndexToModel(row);
            String payrollId = (String) m_historyModel.getValueAt(modelRow, 0);

            PeopleInfo selected = m_employeeList.getSelectedValue();
            if (selected == null) return;
            
            Object[] payroll = null;
            for (Object[] r : m_loadedPayrolls) {
                if (payrollId.equals(r[DataLogicHR.PAYROLL_ID])) {
                    payroll = r;
                    break;
                }
            }
            if (payroll == null) return;

            Object[] empProfile = null;
            try {
                empProfile = dlHR.getEmployeeHR(selected.getID());
            } catch (BasicException e) {
                // ignore
            }
            if (empProfile == null) {
                empProfile = dlHR.createDefaultEmployeeHR(selected.getID());
            }

            String empName = selected.getName();
            String empCode = asText(empProfile[DataLogicHR.EMPLOYEE_CODE]);
            String department = asText(empProfile[DataLogicHR.EMPLOYEE_DEPARTMENT]);
            String position = asText(empProfile[DataLogicHR.EMPLOYEE_POSITION_TITLE]);
            String rfc = asText(empProfile[DataLogicHR.EMPLOYEE_TAX_ID]);
            String nss = asText(empProfile[DataLogicHR.EMPLOYEE_SOCIAL_SECURITY_ID]);
            String bankName = asText(empProfile[DataLogicHR.EMPLOYEE_BANK_NAME]);
            String bankAccount = asText(empProfile[DataLogicHR.EMPLOYEE_BANK_ACCOUNT]);

            String period = asText(payroll[DataLogicHR.PAYROLL_PERIOD_LABEL]);
            String paymentDateStr = formatDate((Date) payroll[DataLogicHR.PAYROLL_PAYMENT_DATE]);
            String payMethod = asText(payroll[DataLogicHR.PAYROLL_PAYMENT_METHOD]);
            String processedBy = asText(payroll[DataLogicHR.PAYROLL_PROCESSED_BY]);
            String notes = asText(payroll[DataLogicHR.PAYROLL_NOTES]);

            double baseVal = asDouble(payroll[DataLogicHR.PAYROLL_BASE_AMOUNT]);
            double commVal = asDouble(payroll[DataLogicHR.PAYROLL_COMMISSIONS]);
            double allowVal = asDouble(payroll[DataLogicHR.PAYROLL_ALLOWANCES]);
            double bonusVal = asDouble(payroll[DataLogicHR.PAYROLL_BONUS_AMOUNT]);
            double grossVal = asDouble(payroll[DataLogicHR.PAYROLL_GROSS_AMOUNT]);
            double dedVal = asDouble(payroll[DataLogicHR.PAYROLL_DEDUCTIONS]);
            double netVal = asDouble(payroll[DataLogicHR.PAYROLL_NET_AMOUNT]);

            String htmlContent = "<html>"
                + "<head>"
                + "<style>"
                + "body { font-family: 'Segoe UI', Arial, sans-serif; color: #1e293b; margin: 15px; font-size: 12px; }"
                + ".header-table { width: 100%; margin-bottom: 15px; border-bottom: 2px solid #0f4c81; padding-bottom: 8px; }"
                + ".company-title { font-size: 18px; font-weight: bold; color: #0f4c81; }"
                + ".doc-title { font-size: 13px; font-weight: bold; color: #64748b; text-align: right; }"
                + ".info-table { width: 100%; margin-bottom: 15px; }"
                + ".info-table td { padding: 4px; vertical-align: top; font-size: 11px; }"
                + ".section-title { font-size: 11px; font-weight: bold; color: #0f4c81; border-bottom: 1px solid #cbd5e1; padding-bottom: 3px; margin-bottom: 8px; }"
                + ".breakdown-table { width: 100%; border-collapse: collapse; margin-bottom: 15px; }"
                + ".breakdown-table th { background-color: #f1f5f9; padding: 5px 8px; font-weight: bold; text-align: left; border-bottom: 1px solid #cbd5e1; color: #475569; font-size: 11px; }"
                + ".breakdown-table td { padding: 5px 8px; border-bottom: 1px solid #e2e8f0; font-size: 11px; }"
                + ".amount-col { text-align: right; }"
                + ".total-row td { font-weight: bold; background-color: #f8fafc; border-top: 1px solid #cbd5e1; color: #1e293b; }"
                + ".net-box { background-color: #ecfdf5; border: 1px solid #a7f3d0; padding: 10px; text-align: center; }"
                + ".net-title { font-size: 11px; font-weight: bold; color: #065f46; }"
                + ".net-amount { font-size: 20px; font-weight: bold; color: #047857; margin-top: 2px; }"
                + ".signature-line { border-top: 1px solid #94a3b8; width: 180px; margin: 15px auto 0 auto; padding-top: 4px; color: #64748b; text-align: center; font-size: 10px; }"
                + "</style>"
                + "</head>"
                + "<body>"
                + "<table class='header-table'>"
                + "<tr>"
                + "<td class='company-title'>Voltium Sanrey</td>"
                + "<td class='doc-title'>RECIBO DE NÓMINA DE COLABORADOR</td>"
                + "</tr>"
                + "</table>"
                + "<table class='info-table'>"
                + "<tr>"
                + "<td style='width: 50%;'>"
                + "  <div class='section-title'>DATOS DEL COLABORADOR</div>"
                + "  <b>Nombre:</b> " + empName + "<br>"
                + "  <b>Código:</b> " + empCode + "<br>"
                + "  <b>Departamento:</b> " + department + "<br>"
                + "  <b>Puesto:</b> " + position + "<br>"
                + "  <b>RFC:</b> " + rfc + "<br>"
                + "  <b>NSS:</b> " + nss + "<br>"
                + "</td>"
                + "<td style='width: 50%;'>"
                + "  <div class='section-title'>DETALLES DEL PAGO</div>"
                + "  <b>Periodo:</b> " + period + "<br>"
                + "  <b>Fecha de Pago:</b> " + paymentDateStr + "<br>"
                + "  <b>Método de Pago:</b> " + payMethod + "<br>"
                + "  <b>Banco:</b> " + bankName + "<br>"
                + "  <b>Cuenta Bancaria:</b> " + bankAccount + "<br>"
                + "  <b>Procesado por:</b> " + processedBy + "<br>"
                + "</td>"
                + "</tr>"
                + "</table>"
                + "<div class='section-title'>DESGLOSE DE CONCEPTOS</div>"
                + "<table class='breakdown-table'>"
                + "<tr>"
                + "  <th>Concepto</th>"
                + "  <th style='width: 25%; text-align: right;'>Percepciones</th>"
                + "  <th style='width: 25%; text-align: right;'>Deducciones</th>"
                + "</tr>"
                + "<tr>"
                + "  <td>Salario Base</td>"
                + "  <td class='amount-col'>" + formatCurrency(baseVal) + "</td>"
                + "  <td class='amount-col'>-</td>"
                + "</tr>"
                + "<tr>"
                + "  <td>Comisiones por Ventas</td>"
                + "  <td class='amount-col'>" + formatCurrency(commVal) + "</td>"
                + "  <td class='amount-col'>-</td>"
                + "</tr>"
                + "<tr>"
                + "  <td>Asignaciones / Pagos de Extras</td>"
                + "  <td class='amount-col'>" + formatCurrency(allowVal) + "</td>"
                + "  <td class='amount-col'>-</td>"
                + "</tr>"
                + "<tr>"
                + "  <td>Bonificaciones y Bonos</td>"
                + "  <td class='amount-col'>" + formatCurrency(bonusVal) + "</td>"
                + "  <td class='amount-col'>-</td>"
                + "</tr>"
                + "<tr>"
                + "  <td>Deducciones de Seguridad Social / Retenciones</td>"
                + "  <td class='amount-col'>-</td>"
                + "  <td class='amount-col'>" + formatCurrency(dedVal) + "</td>"
                + "</tr>"
                + "<tr class='total-row'>"
                + "  <td>Subtotales</td>"
                + "  <td class='amount-col'>" + formatCurrency(grossVal) + "</td>"
                + "  <td class='amount-col'>" + formatCurrency(dedVal) + "</td>"
                + "</tr>"
                + "</table>";
            
            if (!isBlank(notes)) {
                htmlContent += "<div style='margin-bottom: 15px; font-size: 11px;'><b>Observaciones:</b> " + notes + "</div>";
            }
            
            htmlContent += "<table style='width:100%; margin-top: 15px;'>"
                + "<tr>"
                + "<td style='width:60%; vertical-align: middle;'>"
                + "  <div class='net-box'>"
                + "    <div class='net-title'>NETO PAGADO A RECIBIR</div>"
                + "    <div class='net-amount'>" + formatCurrency(netVal) + "</div>"
                + "  </div>"
                + "</td>"
                + "<td style='width:40%; vertical-align:middle; text-align:center;'>"
                + "  <div class='signature-line'>Firma del Colaborador</div>"
                + "</td>"
                + "</tr>"
                + "</table>"
                + "</body>"
                + "</html>";

            JDialog dialog = new JDialog((java.awt.Frame) javax.swing.SwingUtilities.getWindowAncestor(this), "Vista Previa de Recibo de Nómina", true);
            dialog.setSize(620, 680);
            dialog.setLocationRelativeTo(this);
            dialog.setLayout(new BorderLayout());

            JEditorPane editorPane = new JEditorPane();
            editorPane.setContentType("text/html");
            editorPane.setText(htmlContent);
            editorPane.setEditable(false);
            editorPane.setBackground(Color.WHITE);

            JScrollPane scroll = new JScrollPane(editorPane);
            scroll.setBorder(null);
            dialog.add(scroll, BorderLayout.CENTER);

            JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 10));
            btnPanel.setBackground(new Color(248, 250, 252));
            btnPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_COLOR));

            JButton btnPrint = createCompactButton("Imprimir / Guardar PDF");
            btnPrint.setBackground(new Color(37, 99, 235));
            btnPrint.setForeground(Color.WHITE);
            btnPrint.setFont(new Font("Segoe UI", Font.BOLD, 12));
            btnPrint.addActionListener(e -> {
                try {
                    editorPane.print(null, null, true, null, null, true);
                } catch (java.awt.print.PrinterException ex) {
                    showError("No se pudo imprimir o exportar a PDF.", ex);
                }
            });

            JButton btnClose = createCompactButton("Cerrar");
            btnClose.setBackground(new Color(100, 116, 139));
            btnClose.setForeground(Color.WHITE);
            btnClose.setFont(new Font("Segoe UI", Font.BOLD, 12));
            btnClose.addActionListener(e -> dialog.dispose());

            btnPanel.add(btnPrint);
            btnPanel.add(btnClose);
            dialog.add(btnPanel, BorderLayout.SOUTH);

            dialog.setVisible(true);
        } catch (Exception ex) {
            showError("No se pudo abrir la vista previa del recibo.", ex);
        }
    }

    @Override
    public JComponent getComponent() {
        return this;
    }

    @Override
    public Object getBean() {
        return this;
    }

    private static final class PayrollPreview {
        private double baseSalary;
        private double commissions;
        private double allowances;
        private double bonusAmount;
        private double grossAmount;
        private double deductions;
        private double netAmount;
    }

    private final class TabManager {
        private int activeIndex = 0;
        private final JPanel tabLabelsPanel;
        private final JPanel cardsContainer;
        private final CardLayout cardLayout;
        private final JPanel tabBar;

        public TabManager(JPanel tabLabelsPanel, JPanel cardsContainer, CardLayout cardLayout, JPanel tabBar) {
            this.tabLabelsPanel = tabLabelsPanel;
            this.cardsContainer = cardsContainer;
            this.cardLayout = cardLayout;
            this.tabBar = tabBar;
        }

        public void switchToIndex(int index) {
            this.activeIndex = index;
            String[] cardNames = { "PROFILE", "PAYROLL", "COMMISSIONS", "HISTORY" };
            if (index >= 0 && index < cardNames.length && cardLayout != null && cardsContainer != null) {
                cardLayout.show(cardsContainer, cardNames[index]);
            }
            setup();
        }

        public void setup() {
            tabLabelsPanel.removeAll();
            String[] titles = { "Expediente", "Nómina", "Comisiones", "Historial" };

            for (int i = 0; i < titles.length; i++) {
                final int index = i;
                final boolean isSelected = (index == activeIndex);

                TabLabel tabLabel = new TabLabel(titles[i], isSelected);
                tabLabel.addMouseListener(new java.awt.event.MouseAdapter() {
                    @Override
                    public void mousePressed(java.awt.event.MouseEvent e) {
                        switchToIndex(index);
                    }
                });
                tabLabelsPanel.add(tabLabel);
            }

            if (m_historyFilterPanel != null) {
                m_historyFilterPanel.setVisible(activeIndex == 3);
            }

            tabLabelsPanel.revalidate();
            tabLabelsPanel.repaint();
            tabBar.revalidate();
            tabBar.repaint();
        }
    }

    private static final class TabLabel extends JLabel {
        private static final long serialVersionUID = 1L;
        private final boolean isSelected;
        
        public TabLabel(String text, boolean isSelected) {
            super(text);
            this.isSelected = isSelected;
            setFont(new Font("Segoe UI", isSelected ? Font.BOLD : Font.PLAIN, 14));
            setForeground(isSelected ? new Color(37, 99, 235) : new Color(93, 111, 130)); // BRAND_COLOR vs TEXT_SECONDARY
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setBorder(new EmptyBorder(8, 6, 8, 6));
        }
        
        @Override
        protected void paintComponent(java.awt.Graphics g) {
            super.paintComponent(g);
            if (isSelected) {
                java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
                g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(37, 99, 235));
                g2.fillRect(0, getHeight() - 3, getWidth(), 3);
                g2.dispose();
            }
        }
    }

    private BufferedImage getEmployeeImage(String id) {
        try {
            Object data = new PreparedSentence<String, Object[]>(
                dlAdmin.getSession(),
                "SELECT IMAGE FROM people WHERE ID = ?",
                SerializerWriteString.INSTANCE,
                new SerializerReadBasic(new Datas[] { Datas.IMAGE })
            ).find(id);
            
            if (data != null) {
                Object[] row = (Object[]) data;
                return (BufferedImage) row[0];
            }
        } catch (Exception e) {
            System.err.println("Error al cargar la imagen del empleado: " + e.getMessage());
        }
        return null;
    }

    private void updateEmployeeImageInDb(String id, BufferedImage image) throws BasicException {
        // Guardar en la base de datos local "people"
        new PreparedSentence<Object[], Object>(
            dlAdmin.getSession(),
            "UPDATE people SET IMAGE = ? WHERE ID = ?",
            new SerializerWriteBasic(new Datas[] { Datas.IMAGE, Datas.STRING })
        ).exec(new Object[] { image, id });

        // Sincronizar cambio con Supabase si está activo
        try {
            java.util.Map<String, Object> data = new java.util.LinkedHashMap<>();
            data.put("tieneimagen", image != null);
            data.put("fechaextraccion", new Date());
            
            java.lang.reflect.Method sendToSupabaseMethod = dlAdmin.getClass().getDeclaredMethod("sendToSupabase", String.class, String.class, java.util.Map.class, String.class);
            sendToSupabaseMethod.setAccessible(true);
            sendToSupabaseMethod.invoke(dlAdmin, "PATCH", "usuarios", data, id);
        } catch (Exception ex) {
            System.err.println("Error de sincronizacion Supabase de imagen: " + ex.getMessage());
        }
    }

    private void chooseEmployeePhoto() {
        PeopleInfo selected = m_employeeList.getSelectedValue();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Selecciona un empleado antes de configurar su foto.");
            return;
        }

        String[] options = m_employeeImage == null ? new String[]{"Subir foto", "Cancelar"} : new String[]{"Cambiar foto", "Quitar foto", "Cancelar"};
        int choice = JOptionPane.showOptionDialog(this,
            "¿Qué deseas hacer con la foto de " + selected.getName() + "?",
            "Foto de Empleado",
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.INFORMATION_MESSAGE,
            null,
            options,
            options[0]);

        if (choice == 0) { // Subir / Cambiar foto
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Seleccionar Foto de Empleado");
            chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Imágenes (JPG, PNG, GIF)", "jpg", "jpeg", "png", "gif"));
            
            int returnVal = chooser.showOpenDialog(this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                java.io.File file = chooser.getSelectedFile();
                try {
                    BufferedImage img = javax.imageio.ImageIO.read(file);
                    if (img == null) {
                        JOptionPane.showMessageDialog(this, "El archivo seleccionado no es una imagen válida.");
                        return;
                    }

                    updateEmployeeImageInDb(selected.getID(), img);
                    m_employeeImage = img;
                    m_lblAvatarInitials.setVisible(false);
                    m_avatarPanel.repaint();
                    m_employeeList.repaint();
                    JOptionPane.showMessageDialog(this, "Foto de empleado actualizada correctamente.");
                } catch (Exception ex) {
                    showError("No se pudo cargar o guardar la imagen.", new BasicException(ex));
                }
            }
        } else if (choice == 1 && m_employeeImage != null) { // Quitar foto
            if (JOptionPane.showConfirmDialog(this, "¿Estás seguro de que deseas quitar la foto de este empleado?", "Quitar Foto", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                try {
                    updateEmployeeImageInDb(selected.getID(), null);
                    m_employeeImage = null;
                    m_lblAvatarInitials.setVisible(true);
                    m_avatarPanel.repaint();
                    m_employeeList.repaint();
                    JOptionPane.showMessageDialog(this, "Foto de empleado quitada correctamente.");
                } catch (Exception ex) {
                    showError("No se pudo quitar la foto.", new BasicException(ex));
                }
            }
        }
    }

    private final class EmployeeCellRenderer extends JPanel implements ListCellRenderer<PeopleInfo> {

        private static final long serialVersionUID = 1L;

        private final JLabel avatarLabel;
        private final JLabel nameLabel;
        private final JLabel detailLabel;
        private boolean isSelectedCard;

        private EmployeeCellRenderer() {
            setLayout(new BorderLayout(12, 0));
            setBorder(new EmptyBorder(12, 14, 12, 14));
            setOpaque(false);

            avatarLabel = new JLabel("", SwingConstants.CENTER) {
                @Override
                protected void paintComponent(java.awt.Graphics g) {
                    java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
                    g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(getBackground());
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            avatarLabel.setPreferredSize(new Dimension(42, 42));
            avatarLabel.setOpaque(false);
            avatarLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));

            JPanel textPanel = new JPanel(new GridLayout(2, 1, 0, 4));
            textPanel.setOpaque(false);

            nameLabel = new JLabel();
            nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));

            detailLabel = new JLabel();
            detailLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));

            textPanel.add(nameLabel);
            textPanel.add(detailLabel);

            add(avatarLabel, BorderLayout.WEST);
            add(textPanel, BorderLayout.CENTER);
        }

        @Override
        protected void paintComponent(java.awt.Graphics g) {
            java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
            g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth();
            int h = getHeight();
            
            // Draw card background
            g2.setColor(getBackground());
            g2.fillRoundRect(4, 4, w - 8, h - 8, 12, 12);
            
            // Draw card border
            if (isSelectedCard) {
                g2.setColor(BRAND_COLOR);
                g2.drawRoundRect(4, 4, w - 9, h - 9, 12, 12);
            } else {
                g2.setColor(new Color(226, 232, 240)); // Slate 200
                g2.drawRoundRect(4, 4, w - 9, h - 9, 12, 12);
            }
            g2.dispose();
            super.paintComponent(g);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends PeopleInfo> list, PeopleInfo value, int index,
                boolean isSelected, boolean cellHasFocus) {
            String name = value == null ? "" : value.getName();
            String id = value == null ? "" : value.getID();
            isSelectedCard = isSelected;

            avatarLabel.setText(getInitials(name));
            avatarLabel.setBackground(new Color(254, 243, 199)); // Gold/yellow background for initials
            avatarLabel.setForeground(BRAND_COLOR); // Gold initials text

            nameLabel.setText(name);
            detailLabel.setText("ID " + shortenId(id));

            setBackground(isSelected ? new Color(254, 243, 199) : Color.WHITE);
            nameLabel.setForeground(TEXT_PRIMARY);
            detailLabel.setForeground(TEXT_SECONDARY);

            return this;
        }

        private String shortenId(String id) {
            if (id == null) {
                return "";
            }
            String trimmed = id.trim();
            return trimmed.length() <= 10 ? trimmed : trimmed.substring(0, 10) + "...";
        }
    }

    private final class PendingTableRenderer extends javax.swing.table.DefaultTableCellRenderer {
        private final JButton btnAction;
        private final JLabel lblBadge;

        public PendingTableRenderer() {
            btnAction = new JButton("✓ Procesar") {
                @Override
                protected void paintComponent(java.awt.Graphics g) {
                    java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
                    g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(getBackground());
                    g2.fillRoundRect(2, 4, getWidth() - 4, getHeight() - 8, 8, 8);
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            btnAction.setFont(new Font("Segoe UI", Font.BOLD, 12));
            btnAction.setForeground(Color.WHITE);
            btnAction.setBackground(new Color(30, 80, 160)); // Blue color
            btnAction.setOpaque(false);
            btnAction.setContentAreaFilled(false);
            btnAction.setBorderPainted(false);
            btnAction.setFocusable(false);

            lblBadge = new JLabel() {
                @Override
                protected void paintComponent(java.awt.Graphics g) {
                    java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
                    g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(getBackground());
                    g2.fillRoundRect(2, 6, getWidth() - 4, getHeight() - 12, 12, 12);
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            lblBadge.setFont(new Font("Segoe UI", Font.BOLD, 11));
            lblBadge.setOpaque(false);
            lblBadge.setHorizontalAlignment(SwingConstants.CENTER);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            int modelColumn = table.convertColumnIndexToModel(column);

            if (modelColumn == 3) { // Estado Badge
                String val = (String) value;
                if ("Aprobado".equalsIgnoreCase(val)) {
                    lblBadge.setText("Aprobado");
                    lblBadge.setBackground(new Color(220, 252, 231)); // Light green
                    lblBadge.setForeground(new Color(22, 163, 74));  // Green text
                } else {
                    lblBadge.setText("Pendiente");
                    lblBadge.setBackground(new Color(254, 243, 199)); // Light gold
                    lblBadge.setForeground(new Color(217, 119, 6));   // Gold text
                }
                lblBadge.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
                return lblBadge;
            } else if (modelColumn == 4) { // Action Button
                return btnAction;
            }

            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (c instanceof JLabel) {
                ((JLabel) c).setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
            }
            if (!isSelected) {
                c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 250, 252));
            }
            return c;
        }
    }
}

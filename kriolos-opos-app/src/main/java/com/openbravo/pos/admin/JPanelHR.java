package com.openbravo.pos.admin;

import com.openbravo.basic.BasicException;
import com.openbravo.beans.JCalendarDialog;
import com.openbravo.format.Formats;
import com.openbravo.pos.forms.AppUser;
import com.openbravo.pos.forms.AppView;
import com.openbravo.pos.forms.BeanFactoryApp;
import com.openbravo.pos.forms.BeanFactoryException;
import com.openbravo.pos.forms.JPanelView;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
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
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
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

public class JPanelHR extends JPanel implements JPanelView, BeanFactoryApp {

    private static final long serialVersionUID = 1L;

    private static final Color APP_BACKGROUND = new Color(243, 246, 251);
    private static final Color CARD_BACKGROUND = Color.WHITE;
    private static final Color BORDER_COLOR = new Color(223, 229, 239);
    private static final Color TEXT_PRIMARY = new Color(19, 35, 52);
    private static final Color TEXT_SECONDARY = new Color(93, 111, 130);
    private static final Color BRAND_COLOR = new Color(21, 94, 156);
    private static final Color BRAND_DARK = new Color(15, 35, 64);
    private static final Color SUCCESS_COLOR = new Color(22, 163, 74);
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
    private JComboBox<String> m_cmbHistoryGovLevel;
    private JComboBox<String> m_cmbHistorySector;
    private JComboBox<String> m_cmbHistoryPliego;
    private JComboBox<String> m_cmbHistoryUnit;
    private JTextField m_txtHistoryCorrelative;
    private JTable m_errorTable;
    private DefaultTableModel m_errorModel;
    private JLabel m_lblSelectedHistoryPeriod;
    private JLabel m_lblValidationStatus;
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
        setBorder(new EmptyBorder(18, 18, 18, 18));

        add(createTopHeader(), BorderLayout.NORTH);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, createDirectoryPanel(), createWorkspacePanel());
        splitPane.setBorder(null);
        splitPane.setDividerSize(10);
        splitPane.setContinuousLayout(true);
        splitPane.setResizeWeight(0.0);
        splitPane.setDividerLocation(300);
        splitPane.setOpaque(false);
        add(splitPane, BorderLayout.CENTER);

        registerLiveUpdates();
        resetPeriodDefaults();
        
        // Cargar datos iniciales antes de limpiar pantalla
        loadEmployees();
        clearScreenForNoSelection();
    }

    private JComponent createTopHeader() {
        JPanel panel = new JPanel(new BorderLayout(16, 0));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(0, 0, 16, 0));

        JPanel textPanel = new JPanel();
        textPanel.setOpaque(false);
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Recursos humanos y nomina");
        title.setFont(TITLE_FONT);
        title.setForeground(TEXT_PRIMARY);

        JLabel subtitle = new JLabel("Expediente laboral, configuracion salarial y seguimiento de pagos en una sola vista.");
        subtitle.setFont(BODY_FONT);
        subtitle.setForeground(TEXT_SECONDARY);
        subtitle.setBorder(new EmptyBorder(4, 0, 0, 0));

        textPanel.add(title);
        textPanel.add(subtitle);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);

        JButton saveButton = createActionButton("Guardar expediente", BRAND_COLOR);
        saveButton.addActionListener(e -> saveEmployeeProfile(true));

        JButton processButton = createActionButton("Procesar nomina", SUCCESS_COLOR);
        processButton.addActionListener(e -> processPayroll());

        actions.add(saveButton);
        actions.add(processButton);

        panel.add(textPanel, BorderLayout.CENTER);
        panel.add(actions, BorderLayout.EAST);
        return panel;
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

        JLabel subtitle = new JLabel("Selecciona a un colaborador para asignarle su nomina y condiciones laborales.");
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
        
        JButton btnDash = createCompactButton("Ver Dashboard");
        btnDash.addActionListener(e -> {
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
        JPanel panel = new JPanel(new BorderLayout(0, 16));
        panel.setOpaque(false);
        panel.add(createSpotlightPanel(), BorderLayout.NORTH);
        panel.add(createTabbedContent(), BorderLayout.CENTER);
        return panel;
    }

    private JComponent createDashboardPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 32));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(0, 8, 8, 8));

        // 1. Cabecera Premium
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        
        JPanel titleGroup = new JPanel();
        titleGroup.setLayout(new BoxLayout(titleGroup, BoxLayout.Y_AXIS));
        titleGroup.setOpaque(false);
        
        JLabel title = new JLabel("Panel de Control de Nomina");
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(BRAND_DARK);
        
        JLabel subtitle = new JLabel("Visualizacion de indicadores financieros y gestion de pagos pendientes.");
        subtitle.setFont(BODY_FONT);
        subtitle.setForeground(TEXT_SECONDARY);
        
        titleGroup.add(title);
        titleGroup.add(Box.createVerticalStrut(4));
        titleGroup.add(subtitle);
        
        header.add(titleGroup, BorderLayout.WEST);
        
        JPanel actionsHeader = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        actionsHeader.setOpaque(false);
        
        JButton btnRefresh = createCompactButton("Actualizar Dashboard");
        btnRefresh.setBackground(BRAND_COLOR);
        btnRefresh.setForeground(Color.WHITE);
        btnRefresh.addActionListener(e -> refreshDashboard());
        actionsHeader.add(btnRefresh);
        
        header.add(actionsHeader, BorderLayout.EAST);

        // 2. Grid de Metricas (3 Columnas)
        JPanel metricsPanel = new JPanel(new GridLayout(1, 3, 16, 0));
        metricsPanel.setOpaque(false);

        m_lblDashTotalEmployees = createMetricValueLabel();
        m_lblDashTotalPayroll = createMetricValueLabel();
        m_lblDashPendingCount = createMetricValueLabel();

        metricsPanel.add(createDashboardCard("NOMINA PAGADA", m_lblDashTotalPayroll, SUCCESS_COLOR, "Total desembolsado este mes"));
        metricsPanel.add(createDashboardCard("PAGOS PENDIENTES", m_lblDashPendingCount, new Color(220, 38, 38), "Colaboradores por procesar"));
        metricsPanel.add(createDashboardCard("FUERZA LABORAL", m_lblDashTotalEmployees, new Color(37, 99, 235), "Personal activo registrado"));

        // 3. Cuerpo Central: Tabla + Acciones Rapidas
        JPanel body = new JPanel(new BorderLayout(24, 0));
        body.setOpaque(false);

        // 3a. Tabla de Pendientes (Lado Izquierdo)
        JPanel tableCard = createSectionCard("Detalle de Pagos por Procesar");
        tableCard.setLayout(new BorderLayout(0, 16));
        
        m_pendingModel = new DefaultTableModel(new String[]{"ID", "Colaborador", "Departamento", "Estado"}, 0) {
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
        
        m_pendingTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && m_pendingTable.getSelectedRow() != -1) {
                String id = (String) m_pendingModel.getValueAt(m_pendingTable.getSelectedRow(), 0);
                selectEmployeeById(id);
            }
        });

        JScrollPane scroll = new JScrollPane(m_pendingTable);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        scroll.getViewport().setBackground(Color.WHITE);
        tableCard.add(scroll, BorderLayout.CENTER);

        // 3b. Panel Lateral de Acciones (Lado Derecho)
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setOpaque(false);
        rightPanel.setPreferredSize(new Dimension(320, 0));
        
        JPanel actionsCard = createSectionCard("Acciones Rapidas");
        actionsCard.setLayout(new GridLayout(3, 1, 0, 12));
        
        JButton btnAddEmp = createActionButton("Registrar Nuevo Personal", "Añadir colaborador al sistema");
        btnAddEmp.addActionListener(e -> JOptionPane.showMessageDialog(this, "Para añadir personal, usa el modulo de 'Mantenimiento de Personas'."));
        
        JButton btnReports = createActionButton("Reporte Mensual", "Descargar resumen de pagos (PDF)");
        btnReports.setEnabled(false);
        
        JButton btnSettings = createActionButton("Configuracion RRHH", "Ajustar porcentajes de deduccion");
        
        actionsCard.add(btnAddEmp);
        actionsCard.add(btnReports);
        actionsCard.add(btnSettings);
        
        rightPanel.add(actionsCard);
        rightPanel.add(Box.createVerticalGlue());

        body.add(tableCard, BorderLayout.CENTER);
        body.add(rightPanel, BorderLayout.EAST);

        panel.add(header, BorderLayout.NORTH);
        panel.add(metricsPanel, BorderLayout.CENTER);
        panel.add(body, BorderLayout.SOUTH);
        
        return panel;
    }

    private JButton createActionButton(String text, String subtext) {
        JButton btn = new JButton();
        btn.setLayout(new BorderLayout(8, 0));
        btn.setBackground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                new EmptyBorder(8, 8, 8, 8)));
        
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

    private JPanel createDashboardCard(String title, JLabel valueLabel, Color accent, String footerText) {
        JPanel card = new JPanel();
        card.setLayout(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                new EmptyBorder(16, 16, 16, 16)));

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        titleLabel.setForeground(TEXT_SECONDARY);

        valueLabel.setForeground(TEXT_PRIMARY);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        
        JLabel footer = new JLabel(footerText);
        footer.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        footer.setForeground(TEXT_SECONDARY);
        
        // Indicador de color arriba
        JPanel indicator = new JPanel();
        indicator.setBackground(accent);
        indicator.setPreferredSize(new Dimension(0, 4));

        content.add(titleLabel);
        content.add(Box.createVerticalStrut(12));
        content.add(valueLabel);
        content.add(Box.createVerticalStrut(12));
        content.add(footer);

        card.add(indicator, BorderLayout.NORTH);
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
                m_pendingModel.addRow(new Object[]{
                    row[0], row[1], defaultValue(row[2], "N/A"), defaultValue(row[3], "N/A")
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

    private JPanel createSpotlightMetricCard(String title, JLabel valueLabel, Color accentColor) {
        JPanel card = new JPanel(new BorderLayout(0, 4));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                new EmptyBorder(12, 16, 12, 16)));

        JPanel topBar = new JPanel();
        topBar.setBackground(accentColor);
        topBar.setPreferredSize(new Dimension(0, 3));

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);

        JLabel lblTitle = new JLabel(title.toUpperCase());
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblTitle.setForeground(TEXT_SECONDARY);
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        valueLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        content.add(lblTitle);
        content.add(Box.createVerticalStrut(4));
        content.add(valueLabel);

        card.add(topBar, BorderLayout.NORTH);
        card.add(content, BorderLayout.CENTER);
        return card;
    }

    private JComponent createSpotlightPanel() {
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setOpaque(false);

        // 1. Spotlight Card (White background card)
        JPanel spotlightCard = new JPanel(new GridBagLayout());
        spotlightCard.setBackground(Color.WHITE);
        spotlightCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                new EmptyBorder(22, 22, 22, 22)));

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

        JLabel lblLastActive = new JLabel("\u00daltimo registro: \u2014");
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
        JPanel mainTabPanel = new JPanel(new BorderLayout(0, 16));
        mainTabPanel.setOpaque(false);

        JPanel tabBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 24, 0));
        tabBar.setBackground(Color.WHITE);
        tabBar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR),
            BorderFactory.createEmptyBorder(0, 12, 0, 12)
        ));

        JPanel cardsContainer = new JPanel(new CardLayout());
        cardsContainer.setOpaque(false);
        CardLayout cardLayout = (CardLayout) cardsContainer.getLayout();

        cardsContainer.add(createProfileTab(), "PROFILE");
        cardsContainer.add(createPayrollTab(), "PAYROLL");
        cardsContainer.add(createCommissionsTab(), "COMMISSIONS");
        cardsContainer.add(createHistoryTab(), "HISTORY");

        // Helper to manage active tab indices and draw updates
        class TabManager {
            private int activeIndex = 0;
            
            public void setup() {
                tabBar.removeAll();
                String[] titles = { "Expediente", "N\u00f3mina", "Comisiones", "Planilla Web (MCPP)" };
                String[] cardNames = { "PROFILE", "PAYROLL", "COMMISSIONS", "HISTORY" };
                
                for (int i = 0; i < titles.length; i++) {
                    final int index = i;
                    final boolean isSelected = (index == activeIndex);
                    
                    TabLabel tabLabel = new TabLabel(titles[i], isSelected);
                    tabLabel.addMouseListener(new java.awt.event.MouseAdapter() {
                        @Override
                        public void mousePressed(java.awt.event.MouseEvent e) {
                            activeIndex = index;
                            cardLayout.show(cardsContainer, cardNames[index]);
                            setup(); // Redraw tabs on switch
                        }
                    });
                    tabBar.add(tabLabel);
                }
                tabBar.revalidate();
                tabBar.repaint();
            }
        }

        TabManager manager = new TabManager();
        manager.setup();

        mainTabPanel.add(tabBar, BorderLayout.NORTH);
        mainTabPanel.add(cardsContainer, BorderLayout.CENTER);
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

        // 1. Search and Filters Bar Card
        JPanel filterCard = createCardPanel();
        filterCard.setLayout(new BoxLayout(filterCard, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Administración del Archivo de Planilla");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(TEXT_PRIMARY);
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 16, 0));
        filterCard.add(title);

        // Filter ComboBoxes
        m_cmbHistoryYear = new JComboBox<>(new String[]{ "Todos", "2024", "2025", "2026" });
        m_cmbHistoryYear.setSelectedItem("2024");
        
        m_cmbHistoryMonth = new JComboBox<>(new String[]{
            "Todos", "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
            "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
        });
        m_cmbHistoryMonth.setSelectedItem("Todos");

        m_cmbHistoryStatus = new JComboBox<>(new String[]{ "Todos", "Enviado", "Con errores", "Anulado" });
        m_cmbHistoryStatus.setSelectedItem("Todos");

        m_txtHistoryCorrelative = createTextField();

        // Beautiful horizontal search row matching our grid cell layout!
        JPanel filterRow = new JPanel(new GridBagLayout());
        filterRow.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 0, 0, 8);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;

        // Add 4 cells
        gbc.gridx = 0; gbc.weightx = 0.2; filterRow.add(createGridCell("Año", m_cmbHistoryYear, false), gbc);
        gbc.gridx = 1; gbc.weightx = 0.2; filterRow.add(createGridCell("Mes", m_cmbHistoryMonth, false), gbc);
        gbc.gridx = 2; gbc.weightx = 0.2; filterRow.add(createGridCell("Estado", m_cmbHistoryStatus, false), gbc);
        gbc.gridx = 3; gbc.weightx = 0.2; filterRow.add(createGridCell("Nro Correlativo", m_txtHistoryCorrelative, false), gbc);

        // Search Button
        JButton btnSearch = createCompactButton("Buscar");
        btnSearch.setBackground(new Color(37, 99, 235)); // Brand blue
        btnSearch.setForeground(Color.WHITE);
        btnSearch.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnSearch.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnSearch.addActionListener(e -> filterAndDisplayPayrolls());

        gbc.gridx = 4; gbc.weightx = 0.1; gbc.insets = new Insets(0, 0, 0, 0);
        filterRow.add(btnSearch, gbc);

        filterCard.add(filterRow);
        mainPanel.add(filterCard);
        mainPanel.add(Box.createVerticalStrut(16));

        // 2. Center Section: Planillas de Pago Table
        JPanel tableCard = createCardPanel();
        tableCard.setLayout(new BorderLayout(0, 12));

        JLabel lblTableTitle = new JLabel("PLANILLAS DE PAGO");
        lblTableTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTableTitle.setForeground(new Color(15, 76, 129));
        tableCard.add(lblTableTitle, BorderLayout.NORTH);

        m_historyModel = new DefaultTableModel(
            new String[] { "ID", "Tipo Planilla", "Clase Planilla", "Año", "Mes", "Correlativo", "Nro. Registros", "Nro. Trabajadores", "Fecha Importación", "Monto Neto Total", "Estado", "Progreso" },
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
        m_historyTable.getColumnModel().getColumn(7).setCellRenderer(centerRenderer); // Fecha Importación
        m_historyTable.getColumnModel().getColumn(9).setCellRenderer(centerRenderer); // Estado
        m_historyTable.getColumnModel().getColumn(10).setCellRenderer(centerRenderer); // Progreso

        JScrollPane scrollPane = new JScrollPane(m_historyTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        scrollPane.getViewport().setBackground(Color.WHITE);
        tableCard.add(scrollPane, BorderLayout.CENTER);

        // Pagination Bar + Action Buttons Panel
        JPanel tableBottom = new JPanel(new BorderLayout(0, 8));
        tableBottom.setOpaque(false);

        // Pagination
        JPanel paginationPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        paginationPanel.setOpaque(false);
        paginationPanel.add(createPaginationButton("1", true));
        paginationPanel.add(createPaginationButton("2", false));
        paginationPanel.add(createPaginationButton("3", false));
        paginationPanel.add(createPaginationButton("Siguiente", false));
        tableBottom.add(paginationPanel, BorderLayout.NORTH);

        // Actions
        JPanel actionsBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        actionsBar.setOpaque(false);

        m_lblSelectedHistoryPeriod = new JLabel("Selecciona una planilla de pago de la lista");
        m_lblSelectedHistoryPeriod.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        m_lblSelectedHistoryPeriod.setForeground(TEXT_SECONDARY);

        JButton btnDeletePayroll = createCompactButton("Eliminar Planilla");
        btnDeletePayroll.setBackground(new Color(239, 68, 68)); // Red color
        btnDeletePayroll.setForeground(Color.WHITE);
        btnDeletePayroll.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnDeletePayroll.setEnabled(false);

        JButton btnPrintReceipt = createCompactButton("Ver Recibo de Pago");
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

                    m_lblSelectedHistoryPeriod.setText("Planilla: " + period + " (" + net + ")");
                    btnDeletePayroll.setEnabled(true);
                    btnPrintReceipt.setEnabled(true);
                } else {
                    m_lblSelectedHistoryPeriod.setText("Selecciona una planilla de pago de la lista");
                    btnDeletePayroll.setEnabled(false);
                    btnPrintReceipt.setEnabled(false);
                }
            }
        });

        // Wire actions
        btnDeletePayroll.addActionListener(ev -> {
            int row = m_historyTable.getSelectedRow();
            if (row != -1) {
                int modelRow = m_historyTable.convertRowIndexToModel(row);
                String payrollId = (String) m_historyModel.getValueAt(modelRow, 0);
                int confirm = JOptionPane.showConfirmDialog(this,
                    "¿Estás seguro de que deseas eliminar esta planilla de pago de forma permanente?",
                    "Eliminar Planilla de Pago", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    try {
                        new com.openbravo.data.loader.PreparedSentence(m_App.getSession(),
                            "DELETE FROM HR_PAYROLL WHERE ID = ?",
                            com.openbravo.data.loader.SerializerWriteString.INSTANCE).exec(payrollId);
                        
                        PeopleInfo selected = m_employeeList.getSelectedValue();
                        if (selected != null) {
                            loadHistory(selected.getID());
                            refreshDashboard();
                        }
                        JOptionPane.showMessageDialog(this, "Planilla de pago eliminada correctamente.");
                    } catch (BasicException ex) {
                        showError("No se pudo eliminar la planilla de pago.", ex);
                    }
                }
            }
        });

        btnPrintReceipt.addActionListener(ev -> {
            int row = m_historyTable.getSelectedRow();
            if (row != -1) {
                int modelRow = m_historyTable.convertRowIndexToModel(row);
                String type = (String) m_historyModel.getValueAt(modelRow, 1);
                String cls = (String) m_historyModel.getValueAt(modelRow, 2);
                String year = (String) m_historyModel.getValueAt(modelRow, 3);
                String month = (String) m_historyModel.getValueAt(modelRow, 4);
                String correlative = (String) m_historyModel.getValueAt(modelRow, 5);
                String net = (String) m_historyModel.getValueAt(modelRow, 9);
                String progress = (String) m_historyModel.getValueAt(modelRow, 11);

                PeopleInfo selected = m_employeeList.getSelectedValue();
                String empName = selected == null ? "Empleado" : selected.getName();

                String msg = "<html><body style='font-family: Segoe UI; padding: 12px;'>"
                    + "<h2 style='color:#0f4c81; margin:0 0 12px 0;'>PLANILLA OFICIAL DE PAGO (MCPP)</h2>"
                    + "<hr style='border:0; border-top:1px solid #e2e8f0; margin-bottom:12px;'>"
                    + "<table style='width:100%; border-collapse:collapse;'>"
                    + "<tr><td style='padding:4px 0; color:#64748b;'><b>Colaborador:</b></td><td style='text-align:right;'>" + empName + "</td></tr>"
                    + "<tr><td style='padding:4px 0; color:#64748b;'><b>Tipo Planilla:</b></td><td style='text-align:right;'>" + type + "</td></tr>"
                    + "<tr><td style='padding:4px 0; color:#64748b;'><b>Clase Planilla:</b></td><td style='text-align:right;'>" + cls + "</td></tr>"
                    + "<tr><td style='padding:4px 0; color:#64748b;'><b>Per\u00edodo:</b></td><td style='text-align:right;'>" + month + " / " + year + " (Correlativo: " + correlative + ")</td></tr>"
                    + "<tr><td style='padding:4px 0; color:#64748b;'><b>Estado Planilla:</b></td><td style='text-align:right;'><b style='color:#16a34a;'>" + progress + "</b></td></tr>"
                    + "</table>"
                    + "<hr style='border:0; border-top:1px solid #e2e8f0; margin:12px 0;'>"
                    + "<table style='width:100%; border-collapse:collapse;'>"
                    + "<tr style='font-size:14px; font-weight:bold; border-top:1px solid #cbd5e1;'>"
                    + "<td style='padding:8px 0; color:#1e293b;'>Monto Total Liquidado:</td>"
                    + "<td style='padding:8px 0; text-align:right; color:#0f4c81;'>" + net + "</td></tr>"
                    + "</table>"
                    + "</body></html>";

                JOptionPane.showMessageDialog(this, msg, "Recibo de Pago Procesado (MEF)", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(mainPanel, BorderLayout.NORTH);

        return wrapScrollable(wrapper);
    }

    private JPanel createSidebarHeader(String text) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(12, 16, 6, 16));
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 13));
        l.setForeground(new Color(15, 76, 129));
        p.add(l, BorderLayout.CENTER);
        return p;
    }

    private JPanel createSidebarItem(String text, boolean active) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(true);
        p.setBackground(active ? new Color(219, 234, 254) : Color.WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(
            active ? BorderFactory.createMatteBorder(0, 4, 0, 0, new Color(220, 38, 38)) : BorderFactory.createEmptyBorder(0, 4, 0, 0),
            BorderFactory.createEmptyBorder(8, 20, 8, 16)
        ));
        
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", active ? Font.BOLD : Font.PLAIN, 12));
        l.setForeground(active ? new Color(30, 80, 160) : new Color(55, 65, 81));
        p.add(l, BorderLayout.CENTER);
        return p;
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

    private JPanel createLaborProfileCard() {
        JPanel card = createSectionCard("Perfil laboral");
        GridBagConstraints gbc = createFormConstraints();

        m_txtEmployeeCode = createTextField();
        m_txtDepartment = createTextField();
        m_txtPositionTitle = createTextField();
        m_cmbContractType = createComboBox("Indefinido", "Fijo", "Temporal", "Por horas", "Comisionista");
        m_cmbEmployeeStatus = createComboBox("Activo", "En permiso", "Suspendido", "Retirado");
        m_txtHireDate = createReadOnlyField();
        m_cmbPayrollFrequency = createComboBox("Mensual", "Quincenal", "Semanal", "Por evento");

        addFormRow(card, gbc, 0, "Codigo interno", m_txtEmployeeCode);
        addFormRow(card, gbc, 1, "Departamento", m_txtDepartment);
        addFormRow(card, gbc, 2, "Puesto", m_txtPositionTitle);
        addFormRow(card, gbc, 3, "Contrato", m_cmbContractType);
        addFormRow(card, gbc, 4, "Estado", m_cmbEmployeeStatus);
        addFormRow(card, gbc, 5, "Fecha de ingreso", createDateFieldGroup(m_txtHireDate, this::chooseHireDate));
        addFormRow(card, gbc, 6, "Frecuencia de nomina", m_cmbPayrollFrequency);
        return card;
    }

    private JPanel createAdministrativeCard() {
        JPanel card = createSectionCard("Control administrativo");
        GridBagConstraints gbc = createFormConstraints();

        m_txtEmergencyContact = createTextField();
        m_txtTaxId = createTextField();

        addFormRow(card, gbc, 0, "Contacto de emergencia", m_txtEmergencyContact);
        addFormRow(card, gbc, 1, "Identificacion fiscal", m_txtTaxId);

        JLabel note = new JLabel("<html>Usa esta vista para dejar definido el marco contractual del empleado antes de procesar cada nomina.</html>");
        note.setFont(BODY_FONT);
        note.setForeground(TEXT_SECONDARY);
        note.setBorder(new EmptyBorder(12, 0, 0, 0));

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        card.add(note, gbc);
        return card;
    }

    private JPanel createNotesCard() {
        JPanel card = createSectionCard("Observaciones y trazabilidad");
        card.setLayout(new BorderLayout(0, 10));

        JLabel info = new JLabel("Documenta acuerdos, responsabilidades o cualquier detalle relevante del expediente.");
        info.setFont(BODY_FONT);
        info.setForeground(TEXT_SECONDARY);

        m_txtNotes = createTextArea(6);
        JScrollPane scrollPane = new JScrollPane(m_txtNotes);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        scrollPane.getVerticalScrollBar().setUnitIncrement(18);

        card.add(info, BorderLayout.NORTH);
        card.add(scrollPane, BorderLayout.CENTER);
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
        m_lblDirectoryCount.setText(total + (total == 1 ? " empleado" : " empleados"));

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
            dlHR.saveEmployeeHR(buildEmployeeProfile(selected));
            updateSpotlightHeader();
            refreshPayrollPreview();
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

            Object[] payroll = new Object[] {
                    null,
                    selected.getID(),
                    buildPeriodLabel(),
                    m_periodStartValue,
                    m_periodEndValue,
                    m_paymentDateValue == null ? new Date() : m_paymentDateValue,
                    preview.baseSalary,
                    preview.commissions,
                    preview.allowances,
                    preview.bonusAmount,
                    preview.grossAmount,
                    preview.deductions,
                    preview.netAmount,
                    defaultValue(m_cmbPaymentMethod.getSelectedItem(), "Transferencia"),
                    defaultValue(m_cmbPayrollStatus.getSelectedItem(), "Pagado"),
                    cleanText(m_txtPayrollNotes.getText()),
                    getCurrentOperatorName()
            };

            dlHR.insertPayroll(payroll);
            loadHistory(selected.getID());
            refreshPayrollPreview();
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
                mappedProgress
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

    private void addFormRow2Col(JPanel panel, GridBagConstraints gbc, int rowIndex,
                                String labelLeft, JComponent fieldLeft,
                                String labelRight, JComponent fieldRight) {
        gbc.gridy = rowIndex + 1;
        gbc.gridwidth = 1;

        // Left Label
        gbc.gridx = 0;
        gbc.weightx = 0.0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(0, 0, 12, 12);
        panel.add(createFieldLabel(labelLeft), gbc);

        // Left Field
        gbc.gridx = 1;
        gbc.weightx = 0.5;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 12, 24); // Extra gap before next column
        panel.add(fieldLeft, gbc);

        // Right Label
        if (labelRight != null) {
            gbc.gridx = 2;
            gbc.weightx = 0.0;
            gbc.fill = GridBagConstraints.NONE;
            gbc.insets = new Insets(0, 0, 12, 12);
            panel.add(createFieldLabel(labelRight), gbc);
        }

        // Right Field
        if (fieldRight != null) {
            gbc.gridx = 3;
            gbc.weightx = 0.5;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.insets = new Insets(0, 0, 12, 0);
            panel.add(fieldRight, gbc);
        }
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
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setForeground(Color.WHITE);
        button.setBackground(color);
        button.setBorder(new EmptyBorder(10, 18, 10, 18));
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }

    private JButton createCompactButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 11));
        button.setForeground(BRAND_COLOR);
        button.setBackground(new Color(239, 246, 255));
        button.setBorder(new EmptyBorder(8, 12, 8, 12));
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }

    private JLabel createBadgeLabel(Color background, Color foreground) {
        JLabel label = new JLabel();
        label.setOpaque(true);
        label.setBackground(background);
        label.setForeground(foreground);
        label.setFont(new Font("Segoe UI", Font.BOLD, 11));
        label.setBorder(new EmptyBorder(6, 10, 6, 10));
        return label;
    }

    private JLabel createMetricValueLabel() {
        JLabel label = new JLabel(formatCurrency(0.0));
        label.setFont(METRIC_VALUE_FONT);
        label.setForeground(Color.WHITE);
        return label;
    }

    private JPanel createMetricCard(String title, JLabel valueLabel) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(new Color(28, 56, 90));
        card.setBorder(new EmptyBorder(14, 14, 14, 14));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(METRIC_LABEL_FONT);
        titleLabel.setForeground(new Color(201, 214, 230));
        titleLabel.setAlignmentX(LEFT_ALIGNMENT);
        valueLabel.setAlignmentX(LEFT_ALIGNMENT);

        card.add(titleLabel);
        card.add(Box.createVerticalStrut(10));
        card.add(valueLabel);
        return card;
    }

    private JLabel createPreviewLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(BODY_FONT);
        label.setForeground(TEXT_SECONDARY);
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

    private Color statusColor(String status) {
        String normalized = normalize(status);
        if ("activo".equals(normalized)) {
            return new Color(15, 118, 110);
        }
        if ("en permiso".equals(normalized) || "en revision".equals(normalized)) {
            return new Color(180, 83, 9);
        }
        if ("retirado".equals(normalized) || "suspendido".equals(normalized)) {
            return new Color(153, 27, 27);
        }
        return new Color(55, 65, 81);
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
            filterEmployees();
        } catch (BasicException e) {
            showError("No se pudo cargar la lista de personal.", e);
        }
    }

    @Override
    public boolean deactivate() {
        return true;
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

    private static final class TabLabel extends JLabel {
        private static final long serialVersionUID = 1L;
        private final boolean isSelected;
        
        public TabLabel(String text, boolean isSelected) {
            super(text);
            this.isSelected = isSelected;
            setFont(new Font("Segoe UI", isSelected ? Font.BOLD : Font.PLAIN, 14));
            setForeground(isSelected ? new Color(37, 99, 235) : new Color(93, 111, 130)); // BRAND_COLOR vs TEXT_SECONDARY
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setBorder(new EmptyBorder(12, 6, 12, 6));
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
            Object data = new PreparedSentence(
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
        new PreparedSentence(
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

        private EmployeeCellRenderer() {
            setLayout(new BorderLayout(12, 0));
            setBorder(new EmptyBorder(10, 12, 10, 12));

            avatarLabel = new JLabel("", SwingConstants.CENTER);
            avatarLabel.setPreferredSize(new Dimension(42, 42));
            avatarLabel.setOpaque(true);
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
        public Component getListCellRendererComponent(JList<? extends PeopleInfo> list, PeopleInfo value, int index,
                boolean isSelected, boolean cellHasFocus) {
            String name = value == null ? "" : value.getName();
            String id = value == null ? "" : value.getID();

            avatarLabel.setText(getInitials(name));
            avatarLabel.setBackground(isSelected ? BRAND_COLOR : new Color(230, 238, 248));
            avatarLabel.setForeground(isSelected ? Color.WHITE : BRAND_COLOR);

            nameLabel.setText(name);
            detailLabel.setText("ID " + shortenId(id));

            setBackground(isSelected ? new Color(239, 246, 255) : Color.WHITE);
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
}

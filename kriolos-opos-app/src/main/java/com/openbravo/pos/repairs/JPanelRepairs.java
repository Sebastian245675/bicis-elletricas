package com.openbravo.pos.repairs;

import com.openbravo.basic.BasicException;
import com.openbravo.data.gui.MessageInf;
import com.openbravo.format.Formats;
import com.openbravo.pos.customers.CustomerInfo;
import com.openbravo.pos.customers.DataLogicCustomers;
import com.openbravo.pos.customers.JCustomerFinder;
import com.openbravo.pos.forms.AppView;
import com.openbravo.pos.forms.BeanFactoryApp;
import com.openbravo.pos.forms.BeanFactoryException;
import com.openbravo.pos.forms.JPanelView;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;

public class JPanelRepairs extends JPanel implements JPanelView, BeanFactoryApp {

    private AppView m_App;
    private DataLogicRepairs dlRepairs;
    private DataLogicCustomers dlCustomers;

    private JTable m_tableRepairs;
    private DefaultTableModel m_modelRepairs;
    private List<RepairInfo> m_repairsList;

    private JTextField m_txtRepairNumber;
    private JTextField m_txtCustomer;
    private JTextField m_txtBicycle;
    private JTextArea m_txtObservations;
    private JComboBox<TechnicianInfo> m_cmbTechnician;
    private JComboBox<String> m_cmbStatus;
    private JTextField m_txtCost;
    private JLabel m_lblEntryDate;

    private String m_selectedCustomerId;
    private RepairInfo m_currentRepair;

    // Design Tokens (Matching Punto MX palette)
    private static final Color COLOR_NAVY = new Color(14, 30, 45);
    private static final Color COLOR_NAVY_SOFT = new Color(30, 59, 86);
    private static final Color COLOR_CANVAS = new Color(241, 245, 249);
    private static final Color COLOR_SURFACE = Color.WHITE;
    private static final Color COLOR_LINE = new Color(214, 222, 230);
    private static final Color COLOR_TEXT = new Color(36, 48, 63);
    private static final Color COLOR_TEXT_MUTED = new Color(110, 122, 138);
    private static final Color BRAND_EMERALD = new Color(46, 125, 50);

    private static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 18);
    private static final Font FONT_LABEL = new Font("Segoe UI", Font.BOLD, 12);
    private static final Font FONT_FIELD = new Font("Segoe UI", Font.PLAIN, 14);

    public JPanelRepairs() {
    }

    @Override
    public void init(AppView app) throws BeanFactoryException {
        m_App = app;
        dlRepairs = (DataLogicRepairs) app.getBean("com.openbravo.pos.repairs.DataLogicRepairs");
        dlCustomers = (DataLogicCustomers) app.getBean("com.openbravo.pos.customers.DataLogicCustomers");
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setBackground(COLOR_CANVAS);

        // --- HEADER ---
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(COLOR_SURFACE);
        header.setBorder(BorderFactory.createCompoundBorder(
            new MatteBorder(0, 0, 1, 0, COLOR_LINE),
            new EmptyBorder(10, 20, 10, 20)
        ));
        
        JLabel title = new JLabel("MÓDULO DE REPARACIONES");
        title.setFont(FONT_TITLE);
        title.setForeground(COLOR_NAVY);
        header.add(title, BorderLayout.WEST);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);
        
        JButton btnNew = createStyledButton("NUEVO", "/com/openbravo/images/editnew.png", BRAND_EMERALD);
        btnNew.addActionListener(e -> clearForm());
        
        JButton btnSave = createStyledButton("GUARDAR", "/com/openbravo/images/filesave.png", new Color(0, 105, 217));
        btnSave.addActionListener(e -> saveRepair());

        JButton btnDelete = createStyledButton("ELIMINAR", "/com/openbravo/images/editdelete.png", new Color(200, 35, 51));
        btnDelete.addActionListener(e -> deleteRepair());

        actions.add(btnNew);
        actions.add(btnSave);
        actions.add(btnDelete);
        header.add(actions, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);

        // --- CONTENT (SplitPane) ---
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(420);
        splitPane.setDividerSize(8);
        splitPane.setBorder(new EmptyBorder(15, 15, 15, 15));
        splitPane.setOpaque(false);

        // --- LEFT: LIST ---
        JPanel listPanel = new JPanel(new BorderLayout());
        listPanel.setBackground(COLOR_SURFACE);
        listPanel.setBorder(new LineBorder(COLOR_LINE, 1));

        m_modelRepairs = new DefaultTableModel(new String[]{"ID", "ORDEN", "CLIENTE", "ESTADO", "FECHA"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        m_tableRepairs = new JTable(m_modelRepairs);
        m_tableRepairs.setRowHeight(36);
        m_tableRepairs.setFont(FONT_FIELD);
        m_tableRepairs.setShowGrid(false);
        m_tableRepairs.setIntercellSpacing(new Dimension(0, 0));
        m_tableRepairs.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Custom Table Header
        m_tableRepairs.getTableHeader().setFont(FONT_LABEL);
        m_tableRepairs.getTableHeader().setBackground(new Color(248, 250, 252));
        m_tableRepairs.getTableHeader().setForeground(COLOR_TEXT_MUTED);
        m_tableRepairs.getTableHeader().setBorder(new MatteBorder(0, 0, 1, 0, COLOR_LINE));

        // Hide ID Column
        m_tableRepairs.removeColumn(m_tableRepairs.getColumnModel().getColumn(0));
        
        // Set Column Widths
        TableColumnModel colModel = m_tableRepairs.getColumnModel();
        colModel.getColumn(0).setPreferredWidth(60); // Nº
        colModel.getColumn(1).setPreferredWidth(180); // Cliente
        colModel.getColumn(2).setPreferredWidth(90); // Estado
        colModel.getColumn(3).setPreferredWidth(90); // Fecha

        // Renderers
        m_tableRepairs.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? COLOR_SURFACE : new Color(250, 251, 253));
                }
                setBorder(new EmptyBorder(0, 10, 0, 10));
                
                // Status highlighting
                if (column == 2) {
                    String status = (String) value;
                    if ("PENDING".equals(status)) setForeground(new Color(184, 134, 11)); // DarkGoldenRod
                    else if ("IN_PROGRESS".equals(status)) setForeground(new Color(0, 102, 204));
                    else if ("CLOSED".equals(status)) setForeground(BRAND_EMERALD);
                    else if ("CANCELLED".equals(status)) setForeground(Color.GRAY);
                } else {
                    setForeground(isSelected ? Color.WHITE : COLOR_TEXT);
                }
                return c;
            }
        });

        m_tableRepairs.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) loadSelectedRepair();
        });

        JScrollPane scroll = new JScrollPane(m_tableRepairs);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(COLOR_SURFACE);
        listPanel.add(scroll, BorderLayout.CENTER);
        splitPane.setLeftComponent(listPanel);

        // --- RIGHT: FORM ---
        JPanel formContainer = new JPanel(new BorderLayout());
        formContainer.setOpaque(false);
        formContainer.setBorder(new EmptyBorder(0, 15, 0, 0));

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(COLOR_SURFACE);
        formPanel.setBorder(new LineBorder(COLOR_LINE, 1));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 20, 8, 20);
        gbc.weightx = 1.0;

        // Group 1: Identificación
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(createSectionHeader("INFORMACIÓN DEL SERVICIO"), gbc);

        gbc.gridy++;
        formPanel.add(createLabel("Nº DE ORDEN"), gbc);
        m_txtRepairNumber = createTextField(false);
        gbc.gridy++;
        formPanel.add(m_txtRepairNumber, gbc);

        gbc.gridy++;
        formPanel.add(createLabel("CLIENTE"), gbc);
        JPanel pnlCustomer = new JPanel(new BorderLayout(8, 0));
        pnlCustomer.setOpaque(false);
        m_txtCustomer = createTextField(false);
        JButton btnFindCustomer = new JButton();
        try {
            java.net.URL imgUrl = getClass().getResource("/com/openbravo/images/search24.png");
            if (imgUrl != null) btnFindCustomer.setIcon(new ImageIcon(imgUrl));
        } catch (Exception e) {}
        btnFindCustomer.setPreferredSize(new Dimension(40, 32));
        btnFindCustomer.addActionListener(e -> findCustomer());
        pnlCustomer.add(m_txtCustomer, BorderLayout.CENTER);
        pnlCustomer.add(btnFindCustomer, BorderLayout.EAST);
        gbc.gridy++;
        formPanel.add(pnlCustomer, gbc);

        // Group 2: Detalles
        gbc.gridy++;
        gbc.insets = new Insets(20, 20, 8, 20);
        formPanel.add(createSectionHeader("DETALLES TÉCNICOS"), gbc);
        gbc.insets = new Insets(8, 20, 8, 20);

        gbc.gridy++;
        formPanel.add(createLabel("IDENTIFICACIÓN DE BICICLETA / EQUIPO"), gbc);
        m_txtBicycle = createTextField(true);
        gbc.gridy++;
        formPanel.add(m_txtBicycle, gbc);

        gbc.gridy++;
        formPanel.add(createLabel("OBSERVACIONES / DIAGNÓSTICO"), gbc);
        m_txtObservations = new JTextArea(3, 20);
        m_txtObservations.setFont(FONT_FIELD);
        m_txtObservations.setBorder(new CompoundBorder(new LineBorder(COLOR_LINE), new EmptyBorder(5, 10, 5, 10)));
        m_txtObservations.setLineWrap(true);
        m_txtObservations.setWrapStyleWord(true);
        gbc.gridy++;
        formPanel.add(new JScrollPane(m_txtObservations), gbc);

        // Group 3: Estado y Costo
        gbc.gridy++;
        gbc.insets = new Insets(20, 20, 8, 20);
        formPanel.add(createSectionHeader("ESTADO Y LIQUIDACIÓN"), gbc);
        gbc.insets = new Insets(8, 20, 8, 20);

        JPanel pnlStatusRow = new JPanel(new GridLayout(1, 2, 20, 0));
        pnlStatusRow.setOpaque(false);
        
        JPanel pnlTechCol = new JPanel(new BorderLayout(0, 5));
        pnlTechCol.setOpaque(false);
        pnlTechCol.add(createLabel("TÉCNICO ASIGNADO"), BorderLayout.NORTH);
        m_cmbTechnician = new JComboBox<>();
        m_cmbTechnician.setPreferredSize(new Dimension(0, 32));
        m_cmbTechnician.setFont(FONT_FIELD);
        pnlTechCol.add(m_cmbTechnician, BorderLayout.CENTER);
        
        JPanel pnlStatusCol = new JPanel(new BorderLayout(0, 5));
        pnlStatusCol.setOpaque(false);
        pnlStatusCol.add(createLabel("ESTADO ACTUAL"), BorderLayout.NORTH);
        m_cmbStatus = new JComboBox<>(new String[]{"PENDING", "IN_PROGRESS", "CLOSED", "CANCELLED"});
        m_cmbStatus.setPreferredSize(new Dimension(0, 32));
        m_cmbStatus.setFont(FONT_FIELD);
        pnlStatusCol.add(m_cmbStatus, BorderLayout.CENTER);
        
        pnlStatusRow.add(pnlTechCol);
        pnlStatusRow.add(pnlStatusCol);
        gbc.gridy++;
        formPanel.add(pnlStatusRow, gbc);

        gbc.gridy++;
        formPanel.add(createLabel("COSTO DEL SERVICIO ($)"), gbc);
        m_txtCost = createTextField(true);
        m_txtCost.setFont(new Font("Segoe UI", Font.BOLD, 16));
        m_txtCost.setForeground(BRAND_EMERALD);
        gbc.gridy++;
        formPanel.add(m_txtCost, gbc);

        gbc.gridy++;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        m_lblEntryDate = new JLabel("Registrado el: -", SwingConstants.RIGHT);
        m_lblEntryDate.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        m_lblEntryDate.setForeground(COLOR_TEXT_MUTED);
        formPanel.add(m_lblEntryDate, gbc);

        formContainer.add(formPanel, BorderLayout.CENTER);
        splitPane.setRightComponent(formContainer);
        add(splitPane, BorderLayout.CENTER);

        loadData();
    }

    private JLabel createSectionHeader(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(FONT_LABEL);
        lbl.setForeground(COLOR_NAVY_SOFT);
        lbl.setBorder(new MatteBorder(0, 0, 1, 0, COLOR_LINE));
        return lbl;
    }

    private JLabel createLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(FONT_LABEL);
        lbl.setForeground(COLOR_TEXT_MUTED);
        return lbl;
    }

    private JTextField createTextField(boolean editable) {
        JTextField txt = new JTextField();
        txt.setPreferredSize(new Dimension(0, 32));
        txt.setFont(FONT_FIELD);
        txt.setEditable(editable);
        txt.setBorder(new CompoundBorder(new LineBorder(COLOR_LINE), new EmptyBorder(0, 10, 0, 10)));
        if (!editable) txt.setBackground(new Color(248, 250, 252));
        return txt;
    }

    private JButton createStyledButton(String text, String iconPath, Color bg) {
        JButton btn = new JButton(text);
        try {
            java.net.URL imgUrl = getClass().getResource(iconPath);
            if (imgUrl != null) {
                btn.setIcon(new ImageIcon(imgUrl));
            }
        } catch (Exception e) { /* ignore if icon fails */ }
        btn.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(8, 16, 8, 16));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void loadData() {
        try {
            m_repairsList = dlRepairs.getRepairs();
            m_modelRepairs.setRowCount(0);
            for (RepairInfo r : m_repairsList) {
                m_modelRepairs.addRow(new Object[]{
                    r.getId(),
                    r.getRepairNumber(),
                    r.getCustomerName() != null ? r.getCustomerName() : "N/A",
                    r.getStatus(),
                    Formats.DATE.formatValue(r.getEntryDate())
                });
            }

            List<TechnicianInfo> techs = dlRepairs.getTechnicians();
            m_cmbTechnician.removeAllItems();
            for (TechnicianInfo t : techs) m_cmbTechnician.addItem(t);
        } catch (BasicException e) {
            new MessageInf(e).show(this);
        }
    }

    private void findCustomer() {
        JCustomerFinder finder = JCustomerFinder.getCustomerFinder(this, dlCustomers);
        finder.setVisible(true);
        CustomerInfo customer = finder.getSelectedCustomer();
        if (customer != null) {
            m_selectedCustomerId = customer.getId();
            m_txtCustomer.setText(customer.getName());
        }
    }

    private void loadSelectedRepair() {
        int row = m_tableRepairs.getSelectedRow();
        if (row != -1) {
            String id = (String) m_modelRepairs.getValueAt(row, 0);
            for (RepairInfo r : m_repairsList) {
                if (r.getId().equals(id)) {
                    m_currentRepair = r;
                    m_txtRepairNumber.setText(r.getRepairNumber());
                    m_txtCustomer.setText(r.getCustomerName());
                    m_selectedCustomerId = r.getCustomerId();
                    m_txtBicycle.setText(r.getBicycleDetails());
                    m_txtObservations.setText(r.getObservations());
                    m_cmbStatus.setSelectedItem(r.getStatus());
                    m_txtCost.setText(r.getTotalCost() != null ? r.getTotalCost().toString() : "0.0");
                    m_lblEntryDate.setText("Registrado el: " + Formats.TIMESTAMP.formatValue(r.getEntryDate()));
                    for (int i = 0; i < m_cmbTechnician.getItemCount(); i++) {
                        TechnicianInfo t = m_cmbTechnician.getItemAt(i);
                        if (t.getId() != null && t.getId().equals(r.getTechnicianId())) {
                            m_cmbTechnician.setSelectedIndex(i);
                            break;
                        }
                    }
                    return;
                }
            }
        }
    }

    private void clearForm() {
        m_currentRepair = null;
        m_txtRepairNumber.setText("PENDIENTE ASIGNAR");
        m_txtCustomer.setText("");
        m_selectedCustomerId = null;
        m_txtBicycle.setText("");
        m_txtObservations.setText("");
        m_cmbStatus.setSelectedIndex(0);
        m_txtCost.setText("0.0");
        m_lblEntryDate.setText("Registrado el: -");
        m_tableRepairs.clearSelection();
    }

    private void saveRepair() {
        try {
            if (m_selectedCustomerId == null) {
                JOptionPane.showMessageDialog(this, "Debe seleccionar un cliente.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            RepairInfo r = m_currentRepair != null ? m_currentRepair : new RepairInfo();
            r.setCustomerId(m_selectedCustomerId);
            r.setBicycleDetails(m_txtBicycle.getText());
            r.setObservations(m_txtObservations.getText());
            r.setStatus(m_cmbStatus.getSelectedItem().toString());
            try {
                r.setTotalCost(Double.parseDouble(m_txtCost.getText()));
            } catch (NumberFormatException e) {
                r.setTotalCost(0.0);
            }

            TechnicianInfo tech = (TechnicianInfo) m_cmbTechnician.getSelectedItem();
            if (tech != null) r.setTechnicianId(tech.getId());

            if (r.getStatus().equals("CLOSED") && r.getExitDate() == null) r.setExitDate(new java.util.Date());

            dlRepairs.saveRepair(r);
            loadData();
            clearForm();
            JOptionPane.showMessageDialog(this, "Registro procesado exitosamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
        } catch (BasicException e) {
            new MessageInf(e).show(this);
        }
    }

    private void deleteRepair() {
        if (m_currentRepair != null) {
            if (JOptionPane.showConfirmDialog(this, "¿Desea eliminar este registro de reparación?", "Confirmar Eliminación", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                try {
                    dlRepairs.deleteRepair(m_currentRepair.getId());
                    loadData();
                    clearForm();
                } catch (BasicException e) {
                    new MessageInf(e).show(this);
                }
            }
        }
    }

    @Override public JComponent getComponent() { return this; }
    @Override public String getTitle() { return "Reparaciones"; }
    @Override public void activate() throws BasicException { loadData(); }
    @Override public boolean deactivate() { return true; }
    @Override public Object getBean() { return this; }
}

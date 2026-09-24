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
import java.awt.datatransfer.StringSelection;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;

/** Recepción y seguimiento del taller de bicicletas. */
public class JPanelRepairs extends JPanel implements JPanelView, BeanFactoryApp {

    private static final String[] STATUS_CODES = {"PENDING", "IN_PROGRESS", "READY", "CLOSED", "CANCELLED"};
    private static final String[] STATUS_LABELS = {"Pendiente", "En reparación", "Lista para entregar", "Entregada", "Cancelada"};
    private static final String[] SERVICE_TEMPLATES = {
        "Seleccionar servicio frecuente...", "Afinación general", "Ajuste de frenos",
        "Ajuste de cambios", "Cambio o reparación de llanta", "Centrado de rueda",
        "Servicio de suspensión", "Mantenimiento de transmisión",
        "Armado y puesta a punto", "Diagnóstico de bicicleta eléctrica"
    };
    private static final Color NAVY = new Color(14, 30, 45);
    private static final Color NAVY_SOFT = new Color(30, 59, 86);
    private static final Color CANVAS = new Color(241, 245, 249);
    private static final Color SURFACE = Color.WHITE;
    private static final Color LINE = new Color(214, 222, 230);
    private static final Color TEXT = new Color(36, 48, 63);
    private static final Color MUTED = new Color(100, 116, 139);
    private static final Color GREEN = new Color(22, 121, 72);
    private static final Color BLUE = new Color(0, 105, 217);
    private static final Color RED = new Color(190, 38, 51);
    private static final Font TITLE = new Font("Segoe UI", Font.BOLD, 19);
    private static final Font LABEL = new Font("Segoe UI", Font.BOLD, 12);
    private static final Font FIELD = new Font("Segoe UI", Font.PLAIN, 14);

    private DataLogicRepairs dlRepairs;
    private DataLogicCustomers dlCustomers;
    private JTable table;
    private DefaultTableModel tableModel;
    private List<RepairInfo> repairs = new ArrayList<>();
    private JTextField search;
    private JComboBox<String> statusFilter;
    private JTextField repairNumber;
    private JTextField customer;
    private JTextField bicycle;
    private JTextArea observations;
    private JComboBox<String> serviceTemplate;
    private JComboBox<TechnicianInfo> technician;
    private JComboBox<String> status;
    private JTextField cost;
    private JLabel entryDate;
    private JLabel pendingCount;
    private JLabel workshopCount;
    private JLabel readyCount;
    private JLabel deliveredCount;
    private String selectedCustomerId;
    private RepairInfo currentRepair;

    @Override
    public void init(AppView app) throws BeanFactoryException {
        dlRepairs = (DataLogicRepairs) app.getBean("com.openbravo.pos.repairs.DataLogicRepairs");
        dlCustomers = (DataLogicCustomers) app.getBean("com.openbravo.pos.customers.DataLogicCustomers");
        buildUi();
    }

    private void buildUi() {
        setLayout(new BorderLayout(0, 12));
        setBackground(CANVAS);
        setBorder(new EmptyBorder(0, 0, 12, 0));
        add(createHeader(), BorderLayout.NORTH);
        JPanel body = new JPanel(new BorderLayout(0, 12));
        body.setOpaque(false);
        body.setBorder(new EmptyBorder(0, 15, 0, 15));
        body.add(createMetrics(), BorderLayout.NORTH);
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, createList(), createForm());
        split.setDividerLocation(500);
        split.setResizeWeight(.43);
        split.setDividerSize(8);
        split.setBorder(null);
        split.setOpaque(false);
        body.add(split, BorderLayout.CENTER);
        add(body, BorderLayout.CENTER);
        loadData();
        clearForm();
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout(16, 0));
        header.setBackground(SURFACE);
        header.setBorder(new CompoundBorder(new MatteBorder(0, 0, 1, 0, LINE), new EmptyBorder(12, 20, 12, 20)));
        JPanel titles = new JPanel(new GridLayout(2, 1, 0, 2));
        titles.setOpaque(false);
        JLabel title = new JLabel("TALLER DE BICICLETAS");
        title.setFont(TITLE);
        title.setForeground(NAVY);
        JLabel subtitle = new JLabel("Recepción, diagnóstico y seguimiento de servicios");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitle.setForeground(MUTED);
        titles.add(title);
        titles.add(subtitle);
        header.add(titles, BorderLayout.WEST);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        JButton newButton = button("NUEVA ORDEN", "/com/openbravo/images/editnew.png", GREEN);
        newButton.addActionListener(e -> clearForm());
        JButton copyButton = button("COPIAR FOLIO", "/com/openbravo/images/copy.png", NAVY_SOFT);
        copyButton.addActionListener(e -> copyFolio());
        JButton saveButton = button("GUARDAR", "/com/openbravo/images/filesave.png", BLUE);
        saveButton.addActionListener(e -> saveRepair());
        JButton deleteButton = button("ELIMINAR", "/com/openbravo/images/editdelete.png", RED);
        deleteButton.addActionListener(e -> deleteRepair());
        actions.add(newButton);
        actions.add(copyButton);
        actions.add(saveButton);
        actions.add(deleteButton);
        header.add(actions, BorderLayout.EAST);
        return header;
    }

    private JPanel createMetrics() {
        JPanel panel = new JPanel(new GridLayout(1, 4, 10, 0));
        panel.setOpaque(false);
        pendingCount = metric(panel, "PENDIENTES", new Color(180, 120, 20));
        workshopCount = metric(panel, "EN REPARACIÓN", BLUE);
        readyCount = metric(panel, "LISTAS PARA ENTREGAR", GREEN);
        deliveredCount = metric(panel, "ENTREGADAS", NAVY_SOFT);
        return panel;
    }

    private JLabel metric(JPanel parent, String caption, Color accent) {
        JPanel card = new JPanel(new BorderLayout(10, 0));
        card.setBackground(SURFACE);
        card.setBorder(new CompoundBorder(new MatteBorder(0, 4, 0, 0, accent), new EmptyBorder(8, 12, 8, 12)));
        JLabel title = new JLabel(caption);
        title.setFont(new Font("Segoe UI", Font.BOLD, 11));
        title.setForeground(MUTED);
        JLabel value = new JLabel("0", SwingConstants.RIGHT);
        value.setFont(new Font("Segoe UI", Font.BOLD, 22));
        value.setForeground(accent);
        card.add(title, BorderLayout.CENTER);
        card.add(value, BorderLayout.EAST);
        parent.add(card);
        return value;
    }

    private JPanel createList() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBackground(SURFACE);
        panel.setBorder(new CompoundBorder(new LineBorder(LINE), new EmptyBorder(10, 10, 10, 10)));
        JPanel filters = new JPanel(new BorderLayout(8, 0));
        filters.setOpaque(false);
        search = textField(true);
        search.setToolTipText("Buscar por folio, cliente o datos de la bicicleta");
        search.putClientProperty("JTextField.placeholderText", "Buscar orden, cliente o bicicleta...");
        search.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { filterRows(); }
            @Override public void removeUpdate(DocumentEvent e) { filterRows(); }
            @Override public void changedUpdate(DocumentEvent e) { filterRows(); }
        });
        statusFilter = new JComboBox<>(new String[]{"Todos los estados", "Pendiente", "En reparación", "Lista para entregar", "Entregada", "Cancelada"});
        statusFilter.setFont(FIELD);
        statusFilter.setPreferredSize(new Dimension(175, 34));
        statusFilter.addActionListener(e -> filterRows());
        filters.add(search, BorderLayout.CENTER);
        filters.add(statusFilter, BorderLayout.EAST);
        panel.add(filters, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(new String[]{"ID", "ORDEN", "CLIENTE", "ESTADO", "FECHA"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(tableModel);
        table.setRowHeight(36);
        table.setFont(FIELD);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setFont(LABEL);
        table.getTableHeader().setBackground(new Color(248, 250, 252));
        table.getTableHeader().setForeground(MUTED);
        table.removeColumn(table.getColumnModel().getColumn(0));
        TableColumnModel columns = table.getColumnModel();
        columns.getColumn(0).setPreferredWidth(78);
        columns.getColumn(1).setPreferredWidth(180);
        columns.getColumn(2).setPreferredWidth(125);
        columns.getColumn(3).setPreferredWidth(90);
        table.setDefaultRenderer(Object.class, new RepairRenderer());
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) loadSelectedRepair();
        });
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(null);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createForm() {
        JPanel outer = new JPanel(new BorderLayout());
        outer.setOpaque(false);
        outer.setBorder(new EmptyBorder(0, 12, 0, 0));
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(SURFACE);
        form.setBorder(new LineBorder(LINE));
        GridBagConstraints gbc = constraints();
        form.add(section("INFORMACIÓN DE LA ORDEN"), gbc);
        repairNumber = textField(false);
        addField(form, gbc, "N.º DE ORDEN", repairNumber);
        gbc.gridy++;
        form.add(label("CLIENTE *"), gbc);
        JPanel customerRow = new JPanel(new BorderLayout(8, 0));
        customerRow.setOpaque(false);
        customer = textField(false);
        JButton find = new JButton("BUSCAR");
        find.setFont(LABEL);
        find.setPreferredSize(new Dimension(90, 34));
        find.addActionListener(e -> findCustomer());
        customerRow.add(customer, BorderLayout.CENTER);
        customerRow.add(find, BorderLayout.EAST);
        gbc.gridy++;
        form.add(customerRow, gbc);

        addSection(form, gbc, "BICICLETA Y SERVICIO");
        bicycle = textField(true);
        bicycle.setToolTipText("Ejemplo: Trek Marlin 7, rodada 29, negra, serie ABC123");
        bicycle.putClientProperty("JTextField.placeholderText", "Marca, modelo, rodada, color y número de serie");
        addField(form, gbc, "IDENTIFICACIÓN DE LA BICICLETA *", bicycle);
        gbc.gridy++;
        form.add(label("SERVICIO FRECUENTE"), gbc);
        serviceTemplate = new JComboBox<>(SERVICE_TEMPLATES);
        serviceTemplate.setFont(FIELD);
        serviceTemplate.setPreferredSize(new Dimension(0, 34));
        serviceTemplate.addActionListener(e -> insertServiceTemplate());
        gbc.gridy++;
        form.add(serviceTemplate, gbc);
        gbc.gridy++;
        form.add(label("DIAGNÓSTICO, REFACCIONES Y AUTORIZACIÓN *"), gbc);
        observations = new JTextArea(4, 20);
        observations.setFont(FIELD);
        observations.setBorder(new EmptyBorder(6, 10, 6, 10));
        observations.setLineWrap(true);
        observations.setWrapStyleWord(true);
        JScrollPane observationScroll = new JScrollPane(observations);
        observationScroll.setBorder(new LineBorder(LINE));
        gbc.gridy++;
        form.add(observationScroll, gbc);

        addSection(form, gbc, "SEGUIMIENTO Y COBRO");
        JPanel statusRow = new JPanel(new GridLayout(1, 2, 14, 0));
        statusRow.setOpaque(false);
        JPanel technicianColumn = fieldColumn("TÉCNICO ASIGNADO");
        technician = new JComboBox<>();
        technician.setFont(FIELD);
        technicianColumn.add(technician, BorderLayout.CENTER);
        JPanel statusColumn = fieldColumn("ESTADO ACTUAL");
        status = new JComboBox<>(STATUS_LABELS);
        status.setFont(FIELD);
        statusColumn.add(status, BorderLayout.CENTER);
        statusRow.add(technicianColumn);
        statusRow.add(statusColumn);
        gbc.gridy++;
        form.add(statusRow, gbc);
        cost = textField(true);
        cost.setFont(new Font("Segoe UI", Font.BOLD, 16));
        cost.setForeground(GREEN);
        addField(form, gbc, "COSTO DEL SERVICIO ($) *", cost);
        gbc.gridy++;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;
        entryDate = new JLabel("Registrado el: -", SwingConstants.RIGHT);
        entryDate.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        entryDate.setForeground(MUTED);
        form.add(entryDate, gbc);
        outer.add(new JScrollPane(form, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER), BorderLayout.CENTER);
        return outer;
    }

    private GridBagConstraints constraints() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(7, 20, 7, 20);
        gbc.weightx = 1;
        return gbc;
    }

    private void addSection(JPanel form, GridBagConstraints gbc, String text) {
        gbc.gridy++;
        gbc.insets = new Insets(14, 20, 7, 20);
        form.add(section(text), gbc);
        gbc.insets = new Insets(7, 20, 7, 20);
    }

    private void addField(JPanel form, GridBagConstraints gbc, String caption, JComponent component) {
        gbc.gridy++;
        form.add(label(caption), gbc);
        gbc.gridy++;
        form.add(component, gbc);
    }

    private JPanel fieldColumn(String caption) {
        JPanel panel = new JPanel(new BorderLayout(0, 5));
        panel.setOpaque(false);
        panel.add(label(caption), BorderLayout.NORTH);
        return panel;
    }

    private JLabel section(String text) {
        JLabel result = label(text);
        result.setForeground(NAVY_SOFT);
        result.setBorder(new MatteBorder(0, 0, 1, 0, LINE));
        return result;
    }

    private JLabel label(String text) {
        JLabel result = new JLabel(text);
        result.setFont(LABEL);
        result.setForeground(MUTED);
        return result;
    }

    private JTextField textField(boolean editable) {
        JTextField result = new JTextField();
        result.setPreferredSize(new Dimension(0, 34));
        result.setFont(FIELD);
        result.setEditable(editable);
        result.setBorder(new CompoundBorder(new LineBorder(LINE), new EmptyBorder(0, 10, 0, 10)));
        if (!editable) result.setBackground(new Color(248, 250, 252));
        return result;
    }

    private JButton button(String text, String iconPath, Color background) {
        JButton result = new JButton(text);
        try {
            java.net.URL url = getClass().getResource(iconPath);
            if (url != null) result.setIcon(new ImageIcon(url));
        } catch (RuntimeException ignored) { }
        result.setFont(new Font("Segoe UI", Font.BOLD, 11));
        result.setBackground(background);
        result.setForeground(Color.WHITE);
        result.setFocusPainted(false);
        result.setBorder(new EmptyBorder(8, 13, 8, 13));
        result.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return result;
    }

    private void loadData() {
        try {
            repairs = dlRepairs.getRepairs();
            List<TechnicianInfo> technicians = dlRepairs.getTechnicians();
            technician.setModel(new DefaultComboBoxModel<>(technicians.toArray(new TechnicianInfo[0])));
            updateMetrics();
            filterRows();
        } catch (BasicException e) {
            new MessageInf(e).show(this);
        }
    }

    private void updateMetrics() {
        int pending = 0, workshop = 0, ready = 0, delivered = 0;
        for (RepairInfo repair : repairs) {
            if ("PENDING".equals(repair.getStatus())) pending++;
            else if ("IN_PROGRESS".equals(repair.getStatus())) workshop++;
            else if ("READY".equals(repair.getStatus())) ready++;
            else if ("CLOSED".equals(repair.getStatus())) delivered++;
        }
        pendingCount.setText(String.valueOf(pending));
        workshopCount.setText(String.valueOf(workshop));
        readyCount.setText(String.valueOf(ready));
        deliveredCount.setText(String.valueOf(delivered));
    }

    private void filterRows() {
        if (tableModel == null) return;
        String query = normalize(search == null ? "" : search.getText());
        int selected = statusFilter == null ? 0 : statusFilter.getSelectedIndex();
        String statusCode = selected == 0 ? null : STATUS_CODES[selected - 1];
        tableModel.setRowCount(0);
        for (RepairInfo repair : repairs) {
            String searchable = normalize(safe(repair.getRepairNumber()) + " " + safe(repair.getCustomerName()) + " " + safe(repair.getBicycleDetails()));
            if ((statusCode == null || statusCode.equals(repair.getStatus())) && (query.isEmpty() || searchable.contains(query))) {
                tableModel.addRow(new Object[]{repair.getId(), repair.getRepairNumber(), fallback(repair.getCustomerName(), "Sin cliente"), statusLabel(repair.getStatus()), Formats.DATE.formatValue(repair.getEntryDate())});
            }
        }
    }

    private void findCustomer() {
        JCustomerFinder finder = JCustomerFinder.getCustomerFinder(this, dlCustomers);
        finder.setVisible(true);
        CustomerInfo selected = finder.getSelectedCustomer();
        if (selected != null) {
            selectedCustomerId = selected.getId();
            customer.setText(selected.getName());
        }
    }

    private void loadSelectedRepair() {
        int row = table.getSelectedRow();
        if (row < 0) return;
        String id = (String) tableModel.getValueAt(table.convertRowIndexToModel(row), 0);
        for (RepairInfo repair : repairs) {
            if (repair.getId().equals(id)) {
                currentRepair = repair;
                repairNumber.setText(repair.getRepairNumber());
                customer.setText(safe(repair.getCustomerName()));
                selectedCustomerId = repair.getCustomerId();
                bicycle.setText(safe(repair.getBicycleDetails()));
                observations.setText(safe(repair.getObservations()));
                status.setSelectedIndex(statusIndex(repair.getStatus()));
                cost.setText(repair.getTotalCost() == null ? "0.00" : String.format(Locale.US, "%.2f", repair.getTotalCost()));
                entryDate.setText("Registrado el: " + Formats.TIMESTAMP.formatValue(repair.getEntryDate()));
                selectTechnician(repair.getTechnicianId());
                return;
            }
        }
    }

    private void selectTechnician(String id) {
        if (id == null) return;
        for (int i = 0; i < technician.getItemCount(); i++) {
            if (id.equals(technician.getItemAt(i).getId())) technician.setSelectedIndex(i);
        }
    }

    private void clearForm() {
        currentRepair = null;
        selectedCustomerId = null;
        repairNumber.setText("Se asignará al guardar");
        customer.setText("");
        bicycle.setText("");
        observations.setText("");
        serviceTemplate.setSelectedIndex(0);
        status.setSelectedIndex(0);
        cost.setText("0.00");
        entryDate.setText("Registrado el: -");
        table.clearSelection();
    }

    private void insertServiceTemplate() {
        int index = serviceTemplate.getSelectedIndex();
        if (index <= 0) return;
        String text = "Servicio solicitado: " + SERVICE_TEMPLATES[index]
                + "\nFalla reportada: \nRevisión realizada: \nRefacciones requeridas: \nAutorización del cliente: Pendiente";
        if (!observations.getText().trim().isEmpty()) text = observations.getText().trim() + "\n\n" + text;
        observations.setText(text);
        observations.requestFocusInWindow();
    }

    private void saveRepair() {
        if (!validateForm()) return;
        try {
            RepairInfo repair = currentRepair == null ? new RepairInfo() : currentRepair;
            repair.setCustomerId(selectedCustomerId);
            repair.setBicycleDetails(bicycle.getText().trim());
            repair.setObservations(observations.getText().trim());
            String statusCode = STATUS_CODES[status.getSelectedIndex()];
            repair.setStatus(statusCode);
            repair.setTotalCost(parseCost(cost.getText()));
            TechnicianInfo selectedTechnician = (TechnicianInfo) technician.getSelectedItem();
            repair.setTechnicianId(selectedTechnician == null ? null : selectedTechnician.getId());
            if ("CLOSED".equals(statusCode) && repair.getExitDate() == null) repair.setExitDate(new java.util.Date());
            if (!"CLOSED".equals(statusCode)) repair.setExitDate(null);
            dlRepairs.saveRepair(repair);
            loadData();
            clearForm();
            JOptionPane.showMessageDialog(this, "La orden de taller se guardó correctamente.", "Orden guardada", JOptionPane.INFORMATION_MESSAGE);
        } catch (BasicException e) {
            new MessageInf(e).show(this);
        }
    }

    private boolean validateForm() {
        if (selectedCustomerId == null) return validationError("Selecciona el cliente que entrega la bicicleta.", customer);
        if (bicycle.getText().trim().isEmpty()) return validationError("Captura marca, modelo, rodada, color o número de serie.", bicycle);
        if (observations.getText().trim().isEmpty()) return validationError("Registra la falla reportada o el diagnóstico.", observations);
        try {
            if (parseCost(cost.getText()) < 0) return validationError("El costo no puede ser negativo.", cost);
        } catch (NumberFormatException e) {
            return validationError("Ingresa un costo válido, por ejemplo 450.00.", cost);
        }
        return true;
    }

    private boolean validationError(String message, JComponent component) {
        JOptionPane.showMessageDialog(this, message, "Revisa la orden", JOptionPane.WARNING_MESSAGE);
        component.requestFocusInWindow();
        return false;
    }

    private double parseCost(String value) {
        String result = safe(value).trim().replace("$", "").replace(" ", "");
        if (result.contains(",") && result.contains(".")) result = result.replace(",", "");
        else result = result.replace(',', '.');
        return Double.parseDouble(result);
    }

    private void deleteRepair() {
        if (currentRepair == null) {
            JOptionPane.showMessageDialog(this, "Selecciona una orden para eliminarla.", "Sin selección", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String message = "¿Deseas eliminar definitivamente la orden " + currentRepair.getRepairNumber() + "?";
        if (JOptionPane.showConfirmDialog(this, message, "Eliminar orden", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION) {
            try {
                dlRepairs.deleteRepair(currentRepair.getId());
                loadData();
                clearForm();
            } catch (BasicException e) {
                new MessageInf(e).show(this);
            }
        }
    }

    private void copyFolio() {
        if (currentRepair == null) {
            JOptionPane.showMessageDialog(this, "Primero guarda o selecciona una orden.", "Folio no disponible", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String folio = repairNumber.getText().trim();
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(folio), null);
        JOptionPane.showMessageDialog(this, "Folio " + folio + " copiado.", "Folio copiado", JOptionPane.INFORMATION_MESSAGE);
    }

    private static int statusIndex(String value) {
        for (int i = 0; i < STATUS_CODES.length; i++) if (STATUS_CODES[i].equals(value)) return i;
        return 0;
    }

    private static String statusLabel(String value) { return STATUS_LABELS[statusIndex(value)]; }
    private static String safe(String value) { return value == null ? "" : value; }
    private static String fallback(String value, String fallback) { return safe(value).trim().isEmpty() ? fallback : value; }
    private static String normalize(String value) {
        return Normalizer.normalize(safe(value), Normalizer.Form.NFD).replaceAll("\\p{M}", "").toLowerCase(Locale.ROOT).trim();
    }

    private final class RepairRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable source, Object value, boolean selected, boolean focus, int row, int column) {
            Component component = super.getTableCellRendererComponent(source, value, selected, focus, row, column);
            if (!selected) component.setBackground(row % 2 == 0 ? SURFACE : new Color(250, 251, 253));
            setBorder(new EmptyBorder(0, 9, 0, 9));
            if (column == 2 && !selected) {
                String label = String.valueOf(value);
                if ("Pendiente".equals(label)) setForeground(new Color(180, 120, 20));
                else if ("En reparación".equals(label)) setForeground(BLUE);
                else if ("Lista para entregar".equals(label)) setForeground(GREEN);
                else if ("Entregada".equals(label)) setForeground(NAVY_SOFT);
                else setForeground(Color.GRAY);
                setFont(LABEL);
            } else {
                setForeground(selected ? Color.WHITE : TEXT);
                setFont(FIELD);
            }
            return component;
        }
    }

    @Override public JComponent getComponent() { return this; }
    @Override public String getTitle() { return "Taller de bicicletas"; }
    @Override public void activate() throws BasicException { loadData(); }
    @Override public boolean deactivate() { return true; }
    @Override public Object getBean() { return this; }
}

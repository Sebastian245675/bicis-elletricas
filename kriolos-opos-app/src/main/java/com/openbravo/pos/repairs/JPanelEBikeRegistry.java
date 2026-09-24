package com.openbravo.pos.repairs;

import com.openbravo.basic.BasicException;
import com.openbravo.pos.forms.AppView;
import com.openbravo.pos.forms.BeanFactoryApp;
import com.openbravo.pos.forms.BeanFactoryException;
import com.openbravo.pos.forms.DataLogicSales;
import com.openbravo.pos.forms.JPanelView;
import java.awt.*;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/** Expediente técnico individual para bicicletas eléctricas. */
public class JPanelEBikeRegistry extends JPanel implements JPanelView, BeanFactoryApp {

    private static final Color BG = new Color(247, 248, 250);
    private static final Color WHITE = Color.WHITE;
    private static final Color INK = new Color(30, 41, 59);
    private static final Color MUTED = new Color(100, 116, 139);
    private static final Color BORDER = new Color(226, 232, 240);
    private static final Color ACCENT = new Color(37, 99, 235);
    private static final Color ACCENT_SOFT = new Color(239, 246, 255);
    private static final Font BODY = new Font("Segoe UI", Font.PLAIN, 12);
    private static final Font LABEL = new Font("Segoe UI", Font.BOLD, 11);

    private DataLogicSales dlSales;
    private final List<Asset> assets = new ArrayList<>();
    private final List<Asset> visibleAssets = new ArrayList<>();
    private JTextField search;
    private DefaultTableModel model;
    private JTable table;
    private JLabel totalValue;
    private JLabel serviceValue;
    private JLabel batteryValue;
    private JLabel warrantyValue;
    private JLabel formTitle;
    private JLabel formSubtitle;
    private JComboBox<Choice> customer;
    private JComboBox<Choice> product;
    private JTextField brand;
    private JTextField bikeModel;
    private JTextField frameSerial;
    private JTextField motorBrand;
    private JTextField motorModel;
    private JTextField motorSerial;
    private JTextField batterySerial;
    private JTextField batteryVolts;
    private JTextField batteryAh;
    private JTextField batteryWh;
    private JSpinner batteryCycles;
    private JSpinner batteryHealth;
    private JTextField chargerSerial;
    private JTextField controllerSerial;
    private JTextField displaySerial;
    private JTextField odometer;
    private JTextField purchaseDate;
    private JTextField warrantyEnd;
    private JTextField nextService;
    private JComboBox<String> status;
    private JComboBox<String> location;
    private JTextArea notes;
    private String currentId;

    @Override
    public void init(AppView app) throws BeanFactoryException {
        dlSales = (DataLogicSales) app.getBean("com.openbravo.pos.forms.DataLogicSales");
        buildUi();
    }

    private void buildUi() {
        setLayout(new BorderLayout());
        setBackground(BG);
        add(createHeader(), BorderLayout.NORTH);
        JPanel body = new JPanel(new BorderLayout(14, 0));
        body.setOpaque(false);
        body.setBorder(new EmptyBorder(16, 20, 18, 20));
        body.add(createRegistry(), BorderLayout.WEST);
        body.add(createForm(), BorderLayout.CENTER);
        add(body, BorderLayout.CENTER);
    }

    private JPanel createHeader() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(WHITE);
        wrapper.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER), new EmptyBorder(17, 22, 15, 22)));
        JPanel copy = transparentBox();
        copy.add(text("FLOTILLA Y POSVENTA", 10, Font.BOLD, ACCENT));
        copy.add(Box.createVerticalStrut(3));
        copy.add(text("Expedientes E‑Bike", 24, Font.BOLD, INK));
        copy.add(Box.createVerticalStrut(3));
        copy.add(text("Una ficha por bicicleta, batería, motor y cliente.", 12, Font.PLAIN, MUTED));
        wrapper.add(copy, BorderLayout.WEST);
        JButton create = button("Nueva bicicleta", ACCENT, WHITE);
        create.addActionListener(e -> clearForm());
        wrapper.add(create, BorderLayout.EAST);
        return wrapper;
    }

    private JPanel createRegistry() {
        JPanel side = new JPanel(new BorderLayout(0, 12));
        side.setOpaque(false);
        side.setPreferredSize(new Dimension(475, 0));
        JPanel metrics = new JPanel(new GridLayout(1, 4, 7, 0));
        metrics.setOpaque(false);
        totalValue = metric(metrics, "FICHAS");
        serviceValue = metric(metrics, "SERVICIO");
        batteryValue = metric(metrics, "BATERÍA");
        warrantyValue = metric(metrics, "GARANTÍA");
        side.add(metrics, BorderLayout.NORTH);

        JPanel card = card(new BorderLayout(0, 10));
        search = field("Buscar serie, modelo o cliente");
        search.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { filter(); }
            @Override public void removeUpdate(DocumentEvent e) { filter(); }
            @Override public void changedUpdate(DocumentEvent e) { filter(); }
        });
        card.add(search, BorderLayout.NORTH);
        model = new DefaultTableModel(new String[]{"ID", "BICICLETA", "SERIE", "CLIENTE", "ESTADO"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(model);
        table.setFont(BODY);
        table.setRowHeight(42);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setShowVerticalLines(false);
        table.setGridColor(BORDER);
        table.getTableHeader().setFont(LABEL);
        table.getTableHeader().setForeground(MUTED);
        table.getTableHeader().setBackground(new Color(248, 250, 252));
        table.getTableHeader().setReorderingAllowed(false);
        table.removeColumn(table.getColumnModel().getColumn(0));
        table.getColumnModel().getColumn(0).setPreferredWidth(155);
        table.getColumnModel().getColumn(1).setPreferredWidth(90);
        table.getColumnModel().getColumn(2).setPreferredWidth(120);
        table.getColumnModel().getColumn(3).setPreferredWidth(75);
        table.setDefaultRenderer(Object.class, new RowRenderer());
        table.getSelectionModel().addListSelectionListener(e -> { if (!e.getValueIsAdjusting()) loadSelected(); });
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(new LineBorder(BORDER));
        scroll.getViewport().setBackground(WHITE);
        card.add(scroll, BorderLayout.CENTER);
        side.add(card, BorderLayout.CENTER);
        return side;
    }

    private JPanel createForm() {
        JPanel outer = card(new BorderLayout(0, 12));
        JPanel title = new JPanel(new BorderLayout());
        title.setOpaque(false);
        JPanel copy = transparentBox();
        formTitle = text("Nueva ficha", 18, Font.BOLD, INK);
        formSubtitle = text("Completa los datos de identificación y servicio.", 11, Font.PLAIN, MUTED);
        copy.add(formTitle);
        copy.add(Box.createVerticalStrut(3));
        copy.add(formSubtitle);
        title.add(copy, BorderLayout.WEST);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 7, 0));
        actions.setOpaque(false);
        JButton reset = button("Limpiar", new Color(241, 245, 249), INK);
        reset.addActionListener(e -> clearForm());
        JButton save = button("Guardar ficha", ACCENT, WHITE);
        save.addActionListener(e -> save());
        actions.add(reset);
        actions.add(save);
        title.add(actions, BorderLayout.EAST);
        outer.add(title, BorderLayout.NORTH);

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBackground(WHITE);
        customer = new JComboBox<>();
        product = new JComboBox<>();
        brand = field("Marca"); bikeModel = field("Modelo"); frameSerial = field("Obligatorio");
        motorBrand = field("Marca"); motorModel = field("Modelo"); motorSerial = field("Número de serie");
        batterySerial = field("Número de serie"); batteryVolts = field("Ej. 48"); batteryAh = field("Ej. 14"); batteryWh = field("Calculado si se deja vacío");
        batteryCycles = new JSpinner(new SpinnerNumberModel(0, 0, 100000, 1));
        batteryHealth = new JSpinner(new SpinnerNumberModel(100, 0, 100, 1));
        chargerSerial = field("Número de serie"); controllerSerial = field("Número de serie"); displaySerial = field("Número de serie");
        odometer = field("Kilómetros"); purchaseDate = field("AAAA-MM-DD"); warrantyEnd = field("AAAA-MM-DD"); nextService = field("AAAA-MM-DD");
        status = new JComboBox<>(new String[]{"Activa", "En taller", "En garantía", "En cuarentena", "Vendida", "Retirada"});
        location = new JComboBox<>(new String[]{"Cliente", "Tienda", "Taller", "Bodega", "Proveedor"});
        notes = new JTextArea(3, 20); notes.setFont(BODY); notes.setLineWrap(true); notes.setWrapStyleWord(true);

        form.add(section("IDENTIFICACIÓN", rows(
                row(pair("Cliente", customer), pair("Producto del catálogo", product)),
                row(pair("Marca", brand), pair("Modelo", bikeModel), pair("Serie del cuadro *", frameSerial)),
                row(pair("Estado", status), pair("Ubicación", location)))));
        form.add(Box.createVerticalStrut(10));
        form.add(section("SISTEMA ELÉCTRICO", rows(
                row(pair("Marca del motor", motorBrand), pair("Modelo del motor", motorModel), pair("Serie del motor", motorSerial)),
                row(pair("Serie de batería", batterySerial), pair("Voltaje", batteryVolts), pair("Capacidad Ah", batteryAh), pair("Capacidad Wh", batteryWh)),
                row(pair("Ciclos", batteryCycles), pair("Salud batería %", batteryHealth), pair("Serie cargador", chargerSerial)),
                row(pair("Serie controlador", controllerSerial), pair("Serie pantalla", displaySerial)))));
        form.add(Box.createVerticalStrut(10));
        form.add(section("GARANTÍA Y MANTENIMIENTO", rows(
                row(pair("Kilometraje", odometer), pair("Fecha de compra", purchaseDate), pair("Fin de garantía", warrantyEnd), pair("Próximo servicio", nextService)),
                row(pair("Notas técnicas", new JScrollPane(notes))))));
        JScrollPane formScroll = new JScrollPane(form);
        formScroll.setBorder(null);
        formScroll.getViewport().setBackground(WHITE);
        formScroll.getVerticalScrollBar().setUnitIncrement(18);
        outer.add(formScroll, BorderLayout.CENTER);
        return outer;
    }

    private void ensureSchema() throws Exception {
        String sql = "CREATE TABLE IF NOT EXISTS EBIKE_ASSETS ("
                + "ID VARCHAR(255) PRIMARY KEY, CUSTOMER_ID VARCHAR(255), PRODUCT_ID VARCHAR(255), BRAND VARCHAR(120), MODEL VARCHAR(120), FRAME_SERIAL VARCHAR(180) NOT NULL UNIQUE, "
                + "MOTOR_BRAND VARCHAR(120), MOTOR_MODEL VARCHAR(120), MOTOR_SERIAL VARCHAR(180), BATTERY_SERIAL VARCHAR(180), BATTERY_VOLTS DOUBLE, BATTERY_AH DOUBLE, BATTERY_WH DOUBLE, "
                + "BATTERY_CYCLES INTEGER, BATTERY_HEALTH INTEGER, CHARGER_SERIAL VARCHAR(180), CONTROLLER_SERIAL VARCHAR(180), DISPLAY_SERIAL VARCHAR(180), ODOMETER DOUBLE, "
                + "PURCHASE_DATE DATE, WARRANTY_END DATE, NEXT_SERVICE DATE, STATUS VARCHAR(40), ASSET_LOCATION VARCHAR(40), NOTES VARCHAR(2000), CREATED_AT TIMESTAMP, UPDATED_AT TIMESTAMP)";
        try (Statement st = dlSales.getSession().getConnection().createStatement()) { st.execute(sql); }
    }

    private void loadChoices() throws Exception {
        customer.removeAllItems();
        customer.addItem(new Choice(null, "Sin cliente asignado"));
        try (Statement st = dlSales.getSession().getConnection().createStatement(); ResultSet rs = st.executeQuery("SELECT ID, NAME FROM customers ORDER BY NAME")) {
            while (rs.next()) customer.addItem(new Choice(rs.getString(1), rs.getString(2)));
        }
        product.removeAllItems();
        product.addItem(new Choice(null, "Sin producto vinculado"));
        try (Statement st = dlSales.getSession().getConnection().createStatement(); ResultSet rs = st.executeQuery("SELECT ID, NAME FROM products ORDER BY NAME")) {
            while (rs.next()) product.addItem(new Choice(rs.getString(1), rs.getString(2)));
        }
    }

    private void loadAssets() {
        assets.clear();
        String sql = "SELECT e.*, c.NAME, p.NAME FROM EBIKE_ASSETS e LEFT JOIN customers c ON c.ID=e.CUSTOMER_ID LEFT JOIN products p ON p.ID=e.PRODUCT_ID ORDER BY e.UPDATED_AT DESC";
        try (Statement st = dlSales.getSession().getConnection().createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) assets.add(readAsset(rs));
            updateMetrics();
            filter();
        } catch (Exception ex) { showError("No fue posible cargar las fichas", ex); }
    }

    private Asset readAsset(ResultSet rs) throws Exception {
        Asset a = new Asset();
        a.id = rs.getString("ID"); a.customerId = rs.getString("CUSTOMER_ID"); a.productId = rs.getString("PRODUCT_ID");
        a.brand = rs.getString("BRAND"); a.model = rs.getString("MODEL"); a.frameSerial = rs.getString("FRAME_SERIAL");
        a.motorBrand = rs.getString("MOTOR_BRAND"); a.motorModel = rs.getString("MOTOR_MODEL"); a.motorSerial = rs.getString("MOTOR_SERIAL");
        a.batterySerial = rs.getString("BATTERY_SERIAL"); a.batteryVolts = rs.getDouble("BATTERY_VOLTS"); a.batteryAh = rs.getDouble("BATTERY_AH"); a.batteryWh = rs.getDouble("BATTERY_WH");
        a.batteryCycles = rs.getInt("BATTERY_CYCLES"); a.batteryHealth = rs.getInt("BATTERY_HEALTH"); a.chargerSerial = rs.getString("CHARGER_SERIAL");
        a.controllerSerial = rs.getString("CONTROLLER_SERIAL"); a.displaySerial = rs.getString("DISPLAY_SERIAL"); a.odometer = rs.getDouble("ODOMETER");
        a.purchaseDate = rs.getDate("PURCHASE_DATE"); a.warrantyEnd = rs.getDate("WARRANTY_END"); a.nextService = rs.getDate("NEXT_SERVICE");
        a.status = rs.getString("STATUS"); a.location = rs.getString("ASSET_LOCATION"); a.notes = rs.getString("NOTES");
        a.customerName = rs.getString(27); a.productName = rs.getString(28);
        return a;
    }

    private void filter() {
        if (model == null) return;
        String query = search.getText().trim().toLowerCase(Locale.ROOT);
        visibleAssets.clear(); model.setRowCount(0);
        for (Asset a : assets) {
            String text = safe(a.brand) + " " + safe(a.model) + " " + safe(a.frameSerial) + " " + safe(a.batterySerial) + " " + safe(a.customerName);
            if (!query.isEmpty() && !text.toLowerCase(Locale.ROOT).contains(query)) continue;
            visibleAssets.add(a);
            model.addRow(new Object[]{a.id, bikeName(a), safe(a.frameSerial), empty(a.customerName, "Sin asignar"), empty(a.status, "Activa")});
        }
    }

    private void updateMetrics() {
        int service = 0, lowBattery = 0, warranty = 0;
        LocalDate today = LocalDate.now();
        for (Asset a : assets) {
            if (a.nextService != null && !a.nextService.toLocalDate().isAfter(today)) service++;
            if (a.batteryHealth > 0 && a.batteryHealth < 70) lowBattery++;
            if (a.warrantyEnd != null && !a.warrantyEnd.toLocalDate().isBefore(today)) warranty++;
        }
        totalValue.setText(String.valueOf(assets.size())); serviceValue.setText(String.valueOf(service));
        batteryValue.setText(String.valueOf(lowBattery)); warrantyValue.setText(String.valueOf(warranty));
    }

    private void loadSelected() {
        int row = table.getSelectedRow();
        if (row < 0 || row >= visibleAssets.size()) return;
        Asset a = visibleAssets.get(row); currentId = a.id;
        formTitle.setText(bikeName(a)); formSubtitle.setText("Serie de cuadro: " + a.frameSerial);
        choose(customer, a.customerId); choose(product, a.productId);
        set(brand, a.brand); set(bikeModel, a.model); set(frameSerial, a.frameSerial); set(motorBrand, a.motorBrand); set(motorModel, a.motorModel); set(motorSerial, a.motorSerial);
        set(batterySerial, a.batterySerial); set(batteryVolts, number(a.batteryVolts)); set(batteryAh, number(a.batteryAh)); set(batteryWh, number(a.batteryWh));
        batteryCycles.setValue(a.batteryCycles); batteryHealth.setValue(a.batteryHealth); set(chargerSerial, a.chargerSerial); set(controllerSerial, a.controllerSerial); set(displaySerial, a.displaySerial);
        set(odometer, number(a.odometer)); set(purchaseDate, date(a.purchaseDate)); set(warrantyEnd, date(a.warrantyEnd)); set(nextService, date(a.nextService));
        status.setSelectedItem(empty(a.status, "Activa")); location.setSelectedItem(empty(a.location, "Cliente")); notes.setText(safe(a.notes));
    }

    private void clearForm() {
        currentId = null; table.clearSelection(); formTitle.setText("Nueva ficha"); formSubtitle.setText("Completa los datos de identificación y servicio.");
        customer.setSelectedIndex(0); product.setSelectedIndex(0);
        JTextField[] fields = {brand, bikeModel, frameSerial, motorBrand, motorModel, motorSerial, batterySerial, batteryVolts, batteryAh, batteryWh, chargerSerial, controllerSerial, displaySerial, odometer, purchaseDate, warrantyEnd, nextService};
        for (JTextField item : fields) item.setText("");
        batteryCycles.setValue(0); batteryHealth.setValue(100); status.setSelectedItem("Activa"); location.setSelectedItem("Cliente"); notes.setText(""); frameSerial.requestFocusInWindow();
    }

    private void save() {
        String serial = frameSerial.getText().trim();
        if (serial.isEmpty()) { warn("La serie del cuadro es obligatoria."); frameSerial.requestFocusInWindow(); return; }
        try {
            java.sql.Date purchase = parseDate(purchaseDate.getText(), "fecha de compra");
            java.sql.Date warranty = parseDate(warrantyEnd.getText(), "fin de garantía");
            java.sql.Date service = parseDate(nextService.getText(), "próximo servicio");
            double volts = decimal(batteryVolts.getText()); double ah = decimal(batteryAh.getText()); double wh = decimal(batteryWh.getText());
            if (wh <= 0 && volts > 0 && ah > 0) wh = volts * ah;
            Choice customerChoice = (Choice) customer.getSelectedItem(); Choice productChoice = (Choice) product.getSelectedItem();
            boolean insert = currentId == null;
            if (insert) currentId = UUID.randomUUID().toString();
            String sql = insert
                    ? "INSERT INTO EBIKE_ASSETS (ID,CUSTOMER_ID,PRODUCT_ID,BRAND,MODEL,FRAME_SERIAL,MOTOR_BRAND,MOTOR_MODEL,MOTOR_SERIAL,BATTERY_SERIAL,BATTERY_VOLTS,BATTERY_AH,BATTERY_WH,BATTERY_CYCLES,BATTERY_HEALTH,CHARGER_SERIAL,CONTROLLER_SERIAL,DISPLAY_SERIAL,ODOMETER,PURCHASE_DATE,WARRANTY_END,NEXT_SERVICE,STATUS,ASSET_LOCATION,NOTES,CREATED_AT,UPDATED_AT) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP)"
                    : "UPDATE EBIKE_ASSETS SET CUSTOMER_ID=?,PRODUCT_ID=?,BRAND=?,MODEL=?,FRAME_SERIAL=?,MOTOR_BRAND=?,MOTOR_MODEL=?,MOTOR_SERIAL=?,BATTERY_SERIAL=?,BATTERY_VOLTS=?,BATTERY_AH=?,BATTERY_WH=?,BATTERY_CYCLES=?,BATTERY_HEALTH=?,CHARGER_SERIAL=?,CONTROLLER_SERIAL=?,DISPLAY_SERIAL=?,ODOMETER=?,PURCHASE_DATE=?,WARRANTY_END=?,NEXT_SERVICE=?,STATUS=?,ASSET_LOCATION=?,NOTES=?,UPDATED_AT=CURRENT_TIMESTAMP WHERE ID=?";
            try (PreparedStatement ps = dlSales.getSession().getConnection().prepareStatement(sql)) {
                int i = 1; if (insert) ps.setString(i++, currentId);
                ps.setString(i++, customerChoice == null ? null : customerChoice.id); ps.setString(i++, productChoice == null ? null : productChoice.id);
                ps.setString(i++, value(brand)); ps.setString(i++, value(bikeModel)); ps.setString(i++, serial); ps.setString(i++, value(motorBrand)); ps.setString(i++, value(motorModel)); ps.setString(i++, value(motorSerial));
                ps.setString(i++, value(batterySerial)); ps.setDouble(i++, volts); ps.setDouble(i++, ah); ps.setDouble(i++, wh); ps.setInt(i++, (Integer) batteryCycles.getValue()); ps.setInt(i++, (Integer) batteryHealth.getValue());
                ps.setString(i++, value(chargerSerial)); ps.setString(i++, value(controllerSerial)); ps.setString(i++, value(displaySerial)); ps.setDouble(i++, decimal(odometer.getText()));
                ps.setDate(i++, purchase); ps.setDate(i++, warranty); ps.setDate(i++, service); ps.setString(i++, String.valueOf(status.getSelectedItem())); ps.setString(i++, String.valueOf(location.getSelectedItem())); ps.setString(i++, notes.getText().trim());
                if (!insert) ps.setString(i, currentId); ps.executeUpdate();
            }
            loadAssets(); selectById(currentId); JOptionPane.showMessageDialog(this, "Ficha guardada correctamente.", "E‑Bike", JOptionPane.INFORMATION_MESSAGE);
        } catch (DateTimeParseException ex) { warn("Usa el formato AAAA-MM-DD en " + ex.getMessage() + "."); }
        catch (NumberFormatException ex) { warn("Voltaje, capacidad, Wh y kilometraje deben ser números válidos."); }
        catch (Exception ex) { showError("No fue posible guardar la ficha. Verifica que la serie no esté repetida", ex); }
    }

    private void selectById(String id) {
        for (int i = 0; i < visibleAssets.size(); i++) if (id.equals(visibleAssets.get(i).id)) { table.setRowSelectionInterval(i, i); break; }
    }

    private static JPanel section(String title, JPanel content) {
        JPanel section = new JPanel(new BorderLayout(0, 9)); section.setBackground(WHITE);
        section.setBorder(BorderFactory.createCompoundBorder(new LineBorder(BORDER), new EmptyBorder(12, 13, 13, 13)));
        section.add(text(title, 11, Font.BOLD, ACCENT), BorderLayout.NORTH); section.add(content, BorderLayout.CENTER); return section;
    }
    private static JPanel rows(JPanel... rows) { JPanel p = new JPanel(); p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS)); p.setOpaque(false); for (int i=0;i<rows.length;i++){ if(i>0)p.add(Box.createVerticalStrut(9)); p.add(rows[i]); } return p; }
    private static JPanel row(JPanel... cells) { JPanel p = new JPanel(new GridLayout(1, cells.length, 10, 0)); p.setOpaque(false); for(JPanel c:cells)p.add(c); return p; }
    private static JPanel pair(String caption, JComponent input) { JPanel p = new JPanel(new BorderLayout(0, 4)); p.setOpaque(false); p.add(text(caption, 10, Font.BOLD, MUTED), BorderLayout.NORTH); input.setPreferredSize(new Dimension(100, 33)); if (!(input instanceof JScrollPane)) input.setFont(BODY); p.add(input, BorderLayout.CENTER); return p; }
    private static JPanel card(LayoutManager layout) { JPanel p = new JPanel(layout); p.setBackground(WHITE); p.setBorder(BorderFactory.createCompoundBorder(new LineBorder(BORDER), new EmptyBorder(13, 13, 13, 13))); return p; }
    private static JPanel transparentBox() { JPanel p = new JPanel(); p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS)); p.setOpaque(false); return p; }
    private static JTextField field(String placeholder) { JTextField f = new JTextField(); f.setFont(BODY); f.setBorder(BorderFactory.createCompoundBorder(new LineBorder(BORDER), new EmptyBorder(6, 9, 6, 9))); f.putClientProperty("JTextField.placeholderText", placeholder); return f; }
    private static JLabel metric(JPanel parent, String caption) { JPanel p = card(new BorderLayout(0, 2)); JLabel value = text("0", 20, Font.BOLD, INK); p.add(text(caption, 9, Font.BOLD, MUTED), BorderLayout.NORTH); p.add(value, BorderLayout.CENTER); parent.add(p); return value; }
    private static JLabel text(String value, int size, int style, Color color) { JLabel l = new JLabel(value); l.setFont(new Font("Segoe UI", style, size)); l.setForeground(color); return l; }
    private static JButton button(String value, Color bg, Color fg) { JButton b = new JButton(value); b.setFont(LABEL); b.setBackground(bg); b.setForeground(fg); b.setFocusPainted(false); b.setBorder(new EmptyBorder(9, 14, 9, 14)); b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); return b; }
    private static String bikeName(Asset a) { String name = (safe(a.brand) + " " + safe(a.model)).trim(); return name.isEmpty() ? empty(a.productName, "E‑Bike sin modelo") : name; }
    private static String empty(String value, String fallback) { return value == null || value.trim().isEmpty() ? fallback : value; }
    private static String safe(String value) { return value == null ? "" : value; }
    private static String value(JTextField field) { String value = field.getText().trim(); return value.isEmpty() ? null : value; }
    private static String number(double value) { return value == 0 ? "" : String.format(Locale.US, "%s", value); }
    private static String date(Date value) { return value == null ? "" : value.toLocalDate().toString(); }
    private static double decimal(String value) { return value == null || value.trim().isEmpty() ? 0 : Double.parseDouble(value.trim().replace(',', '.')); }
    private static Date parseDate(String value, String field) { if (value == null || value.trim().isEmpty()) return null; try { return Date.valueOf(LocalDate.parse(value.trim())); } catch (DateTimeParseException ex) { throw new DateTimeParseException(field, value, 0); } }
    private static void set(JTextField field, String value) { field.setText(safe(value)); }
    private static void choose(JComboBox<Choice> combo, String id) { combo.setSelectedIndex(0); if(id==null)return; for(int i=0;i<combo.getItemCount();i++) if(id.equals(combo.getItemAt(i).id)){combo.setSelectedIndex(i);break;} }
    private void warn(String message) { JOptionPane.showMessageDialog(this, message, "Revisa la ficha", JOptionPane.WARNING_MESSAGE); }
    private void showError(String message, Exception ex) { JOptionPane.showMessageDialog(this, message + ": " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE); }

    private final class RowRenderer extends DefaultTableCellRenderer {
        @Override public Component getTableCellRendererComponent(JTable t, Object value, boolean selected, boolean focus, int row, int column) {
            super.getTableCellRendererComponent(t, value, selected, focus, row, column); setBorder(new EmptyBorder(0, 7, 0, 7));
            if (!selected) { setBackground(row % 2 == 0 ? WHITE : new Color(248, 250, 252)); setForeground(column == 3 ? ACCENT : INK); }
            setFont(new Font("Segoe UI", column == 0 ? Font.BOLD : Font.PLAIN, 11)); return this;
        }
    }
    private static final class Choice { final String id, label; Choice(String id,String label){this.id=id;this.label=label;} @Override public String toString(){return label;} }
    private static final class Asset {
        String id, customerId, productId, brand, model, frameSerial, motorBrand, motorModel, motorSerial, batterySerial, chargerSerial, controllerSerial, displaySerial, status, location, notes, customerName, productName;
        double batteryVolts, batteryAh, batteryWh, odometer; int batteryCycles, batteryHealth; Date purchaseDate, warrantyEnd, nextService;
    }

    @Override public String getTitle() { return "Expedientes E‑Bike"; }
    @Override public void activate() throws BasicException { try { ensureSchema(); loadChoices(); loadAssets(); clearForm(); } catch (Exception ex) { throw new BasicException(ex); } }
    @Override public boolean deactivate() { return true; }
    @Override public JComponent getComponent() { return this; }
    @Override public Object getBean() { return this; }
}

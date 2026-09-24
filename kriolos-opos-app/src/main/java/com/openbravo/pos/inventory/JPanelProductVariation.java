package com.openbravo.pos.inventory;

import com.openbravo.basic.BasicException;
import com.openbravo.data.gui.MessageInf;
import com.openbravo.pos.forms.AppView;
import com.openbravo.pos.forms.BeanFactoryApp;
import com.openbravo.pos.forms.BeanFactoryException;
import com.openbravo.pos.forms.DataLogicSales;
import com.openbravo.pos.forms.JPanelView;
import com.openbravo.pos.ticket.ProductInfoExt;
import com.openbravo.pos.ticket.ProductPriceHistory;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/** Centro de decisiones de precio y rentabilidad para una bicicletería. */
public class JPanelProductVariation extends JPanel implements JPanelView, BeanFactoryApp {

    private static final Color BG = new Color(247, 248, 250);
    private static final Color CARD = Color.WHITE;
    private static final Color INK = new Color(31, 35, 38);
    private static final Color MUTED = new Color(105, 111, 115);
    private static final Color BORDER = new Color(222, 223, 218);
    private static final Color ORANGE = new Color(37, 99, 235);
    private static final Color ORANGE_SOFT = new Color(239, 246, 255);
    private static final Color GREEN = new Color(37, 99, 235);
    private static final Color GREEN_SOFT = new Color(239, 246, 255);
    private static final Color RED = new Color(37, 99, 235);
    private static final Color RED_SOFT = new Color(239, 246, 255);
    private static final Color AMBER = new Color(37, 99, 235);
    private static final Color AMBER_SOFT = new Color(239, 246, 255);
    private static final Locale MX = new Locale("es", "MX");
    private static final NumberFormat MONEY = NumberFormat.getCurrencyInstance(MX);

    private DataLogicSales dlSales;
    private CardLayout pages;
    private JPanel pageContainer;
    private JProductVariationPanel detailPanel;
    private JTextField searchField;
    private JComboBox<String> filterBox;
    private JComboBox<String> targetBox;
    private JLabel productsValue;
    private JLabel alertsValue;
    private JLabel inventoryValue;
    private JLabel opportunityValue;
    private JLabel resultText;
    private JLabel selectedHint;
    private DefaultTableModel tableModel;
    private JTable productTable;
    private final List<CatalogRow> catalog = new ArrayList<>();
    private final List<CatalogRow> visibleRows = new ArrayList<>();
    private double targetMargin = .30;

    @Override
    public void init(AppView app) throws BeanFactoryException {
        dlSales = (DataLogicSales) app.getBean("com.openbravo.pos.forms.DataLogicSales");
        buildUi();
    }

    private void buildUi() {
        setLayout(new BorderLayout());
        setBackground(BG);
        pages = new CardLayout();
        pageContainer = new JPanel(pages);
        pageContainer.add(createCenter(), "center");
        pageContainer.add(createDetail(), "detail");
        add(pageContainer, BorderLayout.CENTER);
    }

    private JPanel createCenter() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG);
        root.add(createHero(), BorderLayout.NORTH);
        JPanel body = new JPanel(new BorderLayout(0, 14));
        body.setOpaque(false);
        body.setBorder(new EmptyBorder(16, 22, 20, 22));
        body.add(createMetrics(), BorderLayout.NORTH);
        body.add(createWorkTable(), BorderLayout.CENTER);
        body.add(createFooterAction(), BorderLayout.SOUTH);
        root.add(body, BorderLayout.CENTER);
        return root;
    }

    private JPanel createHero() {
        JPanel hero = new JPanel(new BorderLayout(24, 8));
        hero.setBackground(INK);
        hero.setBorder(new EmptyBorder(20, 24, 18, 24));
        JPanel copy = new JPanel();
        copy.setLayout(new BoxLayout(copy, BoxLayout.Y_AXIS));
        copy.setOpaque(false);
        copy.add(label("TALLER + TIENDA", 11, Font.BOLD, new Color(147, 197, 253)));
        copy.add(Box.createVerticalStrut(3));
        copy.add(label("Centro de rentabilidad", 25, Font.BOLD, Color.WHITE));
        copy.add(Box.createVerticalStrut(4));
        copy.add(label("Decide qué precio revisar en bicicletas, componentes y servicios.", 13, Font.PLAIN, new Color(196, 201, 203)));
        hero.add(copy, BorderLayout.WEST);
        JPanel search = new JPanel(new BorderLayout(8, 0));
        search.setOpaque(false);
        search.setPreferredSize(new Dimension(410, 42));
        searchField = new JTextField();
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        searchField.setBorder(BorderFactory.createCompoundBorder(new LineBorder(new Color(75, 79, 81)), new EmptyBorder(0, 12, 0, 12)));
        searchField.putClientProperty("JTextField.placeholderText", "Buscar nombre, código o referencia");
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { applyFilter(); }
            @Override public void removeUpdate(DocumentEvent e) { applyFilter(); }
            @Override public void changedUpdate(DocumentEvent e) { applyFilter(); }
        });
        JButton refresh = button("Actualizar", ORANGE, Color.WHITE);
        refresh.addActionListener(e -> loadCatalog());
        search.add(searchField, BorderLayout.CENTER);
        search.add(refresh, BorderLayout.EAST);
        hero.add(search, BorderLayout.EAST);
        return hero;
    }

    private JPanel createMetrics() {
        JPanel grid = new JPanel(new GridLayout(1, 4, 12, 0));
        grid.setOpaque(false);
        productsValue = addMetric(grid, "CATÁLOGO", "0", "productos registrados", INK);
        alertsValue = addMetric(grid, "REQUIEREN ATENCIÓN", "0", "margen o costo por revisar", RED);
        inventoryValue = addMetric(grid, "INVERSIÓN EN STOCK", "$0", "capital al costo", ORANGE);
        opportunityValue = addMetric(grid, "VENTA POTENCIAL", "$0", "valor del inventario", GREEN);
        return grid;
    }

    private JLabel addMetric(JPanel parent, String caption, String value, String note, Color accent) {
        JPanel card = new JPanel(new BorderLayout(6, 2));
        card.setBackground(CARD);
        card.setBorder(BorderFactory.createCompoundBorder(new LineBorder(BORDER), new EmptyBorder(13, 15, 12, 15)));
        JLabel number = label(value, 23, Font.BOLD, accent);
        card.add(label(caption, 10, Font.BOLD, MUTED), BorderLayout.NORTH);
        card.add(number, BorderLayout.CENTER);
        card.add(label(note, 11, Font.PLAIN, MUTED), BorderLayout.SOUTH);
        parent.add(card);
        return number;
    }

    private JPanel createWorkTable() {
        JPanel card = new JPanel(new BorderLayout(0, 12));
        card.setBackground(CARD);
        card.setBorder(BorderFactory.createCompoundBorder(new LineBorder(BORDER), new EmptyBorder(15, 16, 10, 16)));
        JPanel toolbar = new JPanel(new BorderLayout(12, 0));
        toolbar.setOpaque(false);
        JPanel headings = new JPanel();
        headings.setLayout(new BoxLayout(headings, BoxLayout.Y_AXIS));
        headings.setOpaque(false);
        headings.add(label("PRECIOS QUE MERECEN TU ATENCIÓN", 14, Font.BOLD, INK));
        resultText = label("Cargando catálogo…", 11, Font.PLAIN, MUTED);
        headings.add(Box.createVerticalStrut(2));
        headings.add(resultText);
        toolbar.add(headings, BorderLayout.WEST);
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        controls.setOpaque(false);
        controls.add(label("Mostrar", 12, Font.PLAIN, MUTED));
        filterBox = combo(new String[]{"Todo el catálogo", "Solo atención", "Margen bajo", "Sin costo", "Con cambios"});
        filterBox.addActionListener(e -> applyFilter());
        controls.add(filterBox);
        controls.add(Box.createHorizontalStrut(8));
        controls.add(label("Margen meta", 12, Font.PLAIN, MUTED));
        targetBox = combo(new String[]{"25%", "30%", "35%", "40%", "45%"});
        targetBox.setSelectedItem("30%");
        targetBox.addActionListener(e -> {
            targetMargin = Integer.parseInt(String.valueOf(targetBox.getSelectedItem()).replace("%", "")) / 100d;
            evaluateCatalog();
            applyFilter();
        });
        controls.add(targetBox);
        toolbar.add(controls, BorderLayout.EAST);
        card.add(toolbar, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(new String[]{"ESTADO", "PRODUCTO", "TIPO", "COSTO", "VENTA", "MARGEN", "STOCK", "PRECIO META", "ÚLTIMO CAMBIO"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        productTable = new JTable(tableModel);
        productTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        productTable.setRowHeight(38);
        productTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        productTable.setShowVerticalLines(false);
        productTable.setGridColor(new Color(236, 237, 233));
        productTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 10));
        productTable.getTableHeader().setForeground(MUTED);
        productTable.getTableHeader().setBackground(new Color(249, 249, 247));
        productTable.getTableHeader().setReorderingAllowed(false);
        productTable.setDefaultRenderer(Object.class, new CatalogRenderer());
        int[] widths = {105, 235, 120, 85, 85, 75, 60, 95, 110};
        for (int i = 0; i < widths.length; i++) productTable.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        productTable.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { if (e.getClickCount() == 2) openSelectedProduct(); }
        });
        productTable.getSelectionModel().addListSelectionListener(e -> updateSelectionHint());
        JScrollPane scroll = new JScrollPane(productTable);
        scroll.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER));
        scroll.getViewport().setBackground(CARD);
        card.add(scroll, BorderLayout.CENTER);
        return card;
    }

    private JPanel createFooterAction() {
        JPanel footer = new JPanel(new BorderLayout(12, 0));
        footer.setBackground(ORANGE_SOFT);
        footer.setBorder(BorderFactory.createCompoundBorder(new LineBorder(new Color(246, 199, 177)), new EmptyBorder(10, 14, 10, 12)));
        selectedHint = label("Selecciona un producto para ver por qué necesita atención.", 12, Font.PLAIN, INK);
        JButton inspect = button("Ver análisis del producto", INK, Color.WHITE);
        inspect.addActionListener(e -> openSelectedProduct());
        footer.add(selectedHint, BorderLayout.CENTER);
        footer.add(inspect, BorderLayout.EAST);
        return footer;
    }

    private JPanel createDetail() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG);
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(INK);
        bar.setBorder(new EmptyBorder(10, 18, 10, 18));
        JButton back = button("← Volver al catálogo", new Color(57, 61, 63), Color.WHITE);
        back.addActionListener(e -> pages.show(pageContainer, "center"));
        bar.add(back, BorderLayout.WEST);
        bar.add(label("ANÁLISIS DE PRECIO", 12, Font.BOLD, Color.WHITE), BorderLayout.EAST);
        root.add(bar, BorderLayout.NORTH);
        detailPanel = new JProductVariationPanel();
        root.add(detailPanel, BorderLayout.CENTER);
        return root;
    }

    private void loadCatalog() {
        catalog.clear();
        try {
            dlSales.getProductPriceHistory("");
            Map<String, PriceTrace> traces = loadPriceTraces();
            String sql = "SELECT p.ID, p.REFERENCE, p.CODE, p.NAME, c.NAME, p.PRICEBUY, p.PRICESELL, "
                    + "COALESCE(SUM(sc.UNITS), 0) FROM products p LEFT JOIN categories c ON c.ID = p.CATEGORY "
                    + "LEFT JOIN stockcurrent sc ON sc.PRODUCT = p.ID "
                    + "GROUP BY p.ID, p.REFERENCE, p.CODE, p.NAME, c.NAME, p.PRICEBUY, p.PRICESELL ORDER BY p.NAME";
            try (Statement st = dlSales.getSession().getConnection().createStatement(); ResultSet rs = st.executeQuery(sql)) {
                while (rs.next()) {
                    CatalogRow row = new CatalogRow();
                    row.id = rs.getString(1);
                    row.reference = rs.getString(2);
                    row.code = rs.getString(3);
                    row.name = rs.getString(4);
                    row.category = rs.getString(5);
                    row.cost = rs.getDouble(6);
                    row.sale = rs.getDouble(7);
                    row.stock = rs.getDouble(8);
                    row.trace = traces.get(row.id);
                    catalog.add(row);
                }
            }
            evaluateCatalog();
            applyFilter();
        } catch (Exception ex) {
            resultText.setText("No se pudo cargar el catálogo: " + ex.getMessage());
            resultText.setForeground(RED);
        }
    }

    private Map<String, PriceTrace> loadPriceTraces() throws Exception {
        Map<String, PriceTrace> result = new HashMap<>();
        String sql = "SELECT PRODUCT_ID, PRICEBUY, PRICESELL, DATENEW FROM PRODUCT_PRICES_HISTORY ORDER BY PRODUCT_ID, DATENEW";
        try (Statement st = dlSales.getSession().getConnection().createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                String id = rs.getString(1);
                PriceTrace trace = result.computeIfAbsent(id, key -> new PriceTrace());
                trace.previousCost = trace.lastCost;
                trace.previousSale = trace.lastSale;
                trace.lastCost = rs.getDouble(2);
                trace.lastSale = rs.getDouble(3);
                trace.lastDate = rs.getTimestamp(4);
                trace.count++;
            }
        }
        return result;
    }

    private void evaluateCatalog() {
        int alerts = 0;
        double investment = 0;
        double potential = 0;
        for (CatalogRow row : catalog) {
            row.margin = row.sale > 0 ? (row.sale - row.cost) / row.sale : -1;
            row.suggested = row.cost > 0 ? roundPrice(row.cost / (1d - targetMargin)) : 0;
            boolean costRoseWithoutSale = row.trace != null && row.trace.count > 1
                    && row.trace.lastCost > row.trace.previousCost + .009 && row.trace.lastSale <= row.trace.previousSale + .009;
            if (row.cost <= 0) {
                row.state = "SIN COSTO"; row.reason = "Registra el costo de compra para saber si este producto deja utilidad."; row.priority = 4;
            } else if (row.sale <= row.cost) {
                row.state = "PÉRDIDA"; row.reason = "El precio de venta no cubre el costo. Ajusta este producto antes de venderlo."; row.priority = 5;
            } else if (costRoseWithoutSale) {
                row.state = "COSTO SUBIÓ"; row.reason = "El costo aumentó y la venta no cambió; revisa el precio para proteger el margen."; row.priority = 4;
            } else if (row.margin < targetMargin) {
                row.state = "MARGEN BAJO"; row.reason = "Está por debajo de la meta de " + Math.round(targetMargin * 100) + "%. Precio sugerido: " + MONEY.format(row.suggested) + "."; row.priority = 3;
            } else {
                row.state = "SALUDABLE"; row.reason = "El margen cumple la meta definida."; row.priority = 1;
            }
            if (row.priority > 1) alerts++;
            if (row.stock > 0) { investment += row.cost * row.stock; potential += row.sale * row.stock; }
        }
        productsValue.setText(String.valueOf(catalog.size()));
        alertsValue.setText(String.valueOf(alerts));
        inventoryValue.setText(compactMoney(investment));
        opportunityValue.setText(compactMoney(potential));
    }

    private void applyFilter() {
        if (tableModel == null) return;
        String query = searchField == null ? "" : searchField.getText().trim().toLowerCase(MX);
        String filter = filterBox == null ? "Todo el catálogo" : String.valueOf(filterBox.getSelectedItem());
        visibleRows.clear();
        for (CatalogRow row : catalog) {
            String haystack = safe(row.name) + " " + safe(row.reference) + " " + safe(row.code) + " " + safe(row.category);
            if (!query.isEmpty() && !haystack.toLowerCase(MX).contains(query)) continue;
            if ("Solo atención".equals(filter) && row.priority <= 1) continue;
            if ("Margen bajo".equals(filter) && !(row.cost > 0 && row.margin < targetMargin)) continue;
            if ("Sin costo".equals(filter) && row.cost > 0) continue;
            if ("Con cambios".equals(filter) && (row.trace == null || row.trace.count < 2)) continue;
            visibleRows.add(row);
        }
        visibleRows.sort(Comparator.comparingInt((CatalogRow r) -> r.priority).reversed().thenComparing(r -> safe(r.name)));
        tableModel.setRowCount(0);
        SimpleDateFormat date = new SimpleDateFormat("dd MMM yy", MX);
        for (CatalogRow row : visibleRows) {
            tableModel.addRow(new Object[]{row.state, safe(row.name), emptyAs(row.category, "Sin categoría"), MONEY.format(row.cost), MONEY.format(row.sale),
                row.sale > 0 ? percent(row.margin) : "—", stock(row.stock), row.suggested > 0 ? MONEY.format(row.suggested) : "—",
                row.trace != null && row.trace.lastDate != null ? date.format(row.trace.lastDate) : "Sin historial"});
        }
        resultText.setForeground(MUTED);
        resultText.setText(visibleRows.size() + " de " + catalog.size() + " productos · doble clic para abrir el análisis");
        selectedHint.setText(visibleRows.isEmpty() ? "No hay productos para este filtro." : "Selecciona un producto para ver por qué necesita atención.");
    }

    private void updateSelectionHint() {
        int index = productTable.getSelectedRow();
        if (index >= 0 && index < visibleRows.size()) selectedHint.setText(visibleRows.get(index).name + " · " + visibleRows.get(index).reason);
    }

    private void openSelectedProduct() {
        int index = productTable.getSelectedRow();
        if (index < 0 || index >= visibleRows.size()) {
            JOptionPane.showMessageDialog(this, "Selecciona primero un producto de la tabla.", "Producto", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        CatalogRow row = visibleRows.get(index);
        try {
            ProductInfoExt product = dlSales.getProductInfoByReference(row.reference);
            if (product == null) product = dlSales.getProductInfoByCode(row.code);
            if (product == null) throw new BasicException("Producto no encontrado");
            detailPanel.setHistory(dlSales.getProductPriceHistory(row.id), row.id, row.cost, row.sale, row.name, row.reference);
            detailPanel.setBusinessContext(row.category, row.stock, targetMargin, row.suggested, row.reason);
            pages.show(pageContainer, "detail");
        } catch (BasicException ex) {
            new MessageInf(ex).show(this);
        }
    }

    private static double roundPrice(double value) { return value <= 0 ? 0 : Math.ceil(value / 10d) * 10d; }
    private static String compactMoney(double value) {
        if (Math.abs(value) >= 1_000_000) return String.format(MX, "$%.1f M", value / 1_000_000d);
        if (Math.abs(value) >= 1_000) return String.format(MX, "$%.1f mil", value / 1_000d);
        return MONEY.format(value);
    }
    private static String percent(double value) { return String.format(MX, "%.1f%%", value * 100d); }
    private static String stock(double value) { return Math.abs(value - Math.rint(value)) < .001 ? String.valueOf((int) Math.rint(value)) : String.format(MX, "%.1f", value); }
    private static String safe(String value) { return value == null ? "" : value; }
    private static String emptyAs(String value, String replacement) { return value == null || value.trim().isEmpty() ? replacement : value; }

    private static JLabel label(String text, int size, int style, Color color) {
        JLabel label = new JLabel(text); label.setFont(new Font("Segoe UI", style, size)); label.setForeground(color); return label;
    }
    private static JButton button(String text, Color background, Color foreground) {
        JButton button = new JButton(text); button.setFont(new Font("Segoe UI", Font.BOLD, 12)); button.setBackground(background); button.setForeground(foreground);
        button.setFocusPainted(false); button.setBorder(new EmptyBorder(9, 15, 9, 15)); button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); return button;
    }
    private static JComboBox<String> combo(String[] items) {
        JComboBox<String> combo = new JComboBox<>(items); combo.setFont(new Font("Segoe UI", Font.PLAIN, 12)); combo.setBackground(CARD); combo.setPreferredSize(new Dimension(145, 32)); return combo;
    }

    private final class CatalogRenderer extends DefaultTableCellRenderer {
        @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean selected, boolean focus, int row, int column) {
            super.getTableCellRendererComponent(table, value, selected, focus, row, column);
            setBorder(new EmptyBorder(0, 8, 0, 8));
            setFont(new Font("Segoe UI", column == 0 || column == 5 ? Font.BOLD : Font.PLAIN, 12));
            if (!selected) {
                setBackground(row % 2 == 0 ? CARD : new Color(250, 250, 248)); setForeground(INK);
                if (column == 0) {
                    String state = String.valueOf(value);
                    if ("SALUDABLE".equals(state)) { setForeground(GREEN); setBackground(GREEN_SOFT); }
                    else if ("MARGEN BAJO".equals(state)) { setForeground(AMBER); setBackground(AMBER_SOFT); }
                    else { setForeground(RED); setBackground(RED_SOFT); }
                } else if (column == 5 && row < visibleRows.size()) setForeground(visibleRows.get(row).margin >= targetMargin ? GREEN : RED);
                else if (column == 7) setForeground(ORANGE);
            }
            setHorizontalAlignment(column >= 3 && column <= 7 ? SwingConstants.RIGHT : SwingConstants.LEFT);
            return this;
        }
    }

    private static final class CatalogRow {
        String id, reference, code, name, category, state, reason;
        double cost, sale, stock, margin, suggested;
        int priority;
        PriceTrace trace;
    }
    private static final class PriceTrace {
        double previousCost, previousSale, lastCost, lastSale;
        Date lastDate;
        int count;
    }

    @Override public String getTitle() { return "Centro de rentabilidad"; }
    @Override public void activate() throws BasicException { pages.show(pageContainer, "center"); loadCatalog(); }
    @Override public boolean deactivate() { return true; }
    @Override public JComponent getComponent() { return this; }
    @Override public Object getBean() { return this; }
}

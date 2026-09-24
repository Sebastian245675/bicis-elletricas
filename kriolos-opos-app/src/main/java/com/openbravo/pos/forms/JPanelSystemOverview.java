package com.openbravo.pos.forms;

import com.openbravo.basic.BasicException;
import com.openbravo.pos.forms.menu.Menu;
import com.openbravo.pos.scripting.ScriptEngine;
import com.openbravo.pos.scripting.ScriptException;
import com.openbravo.pos.scripting.ScriptFactory;
import com.openbravo.pos.util.StringUtils;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.JPopupMenu;
import javax.swing.JMenuItem;
import javax.swing.MenuElement;
import javax.swing.MenuSelectionManager;

public class JPanelSystemOverview extends JPanel implements JPanelView {

    public static String searchTargetField = null;

    private static final class InternalSearchEntry {
        private final String label;
        private final String path;
        private final String task;
        private final String targetField;

        private InternalSearchEntry(String label, String path, String task, String targetField) {
            this.label = label;
            this.path = path;
            this.task = task;
            this.targetField = targetField;
        }
    }

    private static final List<InternalSearchEntry> INTERNAL_SEARCHES = List.of(
            // Product Editor internal fields
            new InternalSearchEntry("Modelo (de Producto)", "Stock > Productos",
                    "com.openbravo.pos.inventory.ProductsPanel", "modelo"),
            new InternalSearchEntry("Lote (de Producto)", "Stock > Productos",
                    "com.openbravo.pos.inventory.ProductsPanel", "lote"),
            new InternalSearchEntry("Color (de Producto)", "Stock > Productos",
                    "com.openbravo.pos.inventory.ProductsPanel", "color"),
            new InternalSearchEntry("Voltaje (de Producto)", "Stock > Productos",
                    "com.openbravo.pos.inventory.ProductsPanel", "voltaje"),
            new InternalSearchEntry("No. Serie / Serie (de Producto)", "Stock > Productos",
                    "com.openbravo.pos.inventory.ProductsPanel", "noserie"),
            new InternalSearchEntry("Código de barras / Código (de Producto)", "Stock > Productos",
                    "com.openbravo.pos.inventory.ProductsPanel", "code"),
            new InternalSearchEntry("Nombre / Descripción (de Producto)", "Stock > Productos",
                    "com.openbravo.pos.inventory.ProductsPanel", "name"),
            new InternalSearchEntry("Referencia (de Producto)", "Stock > Productos",
                    "com.openbravo.pos.inventory.ProductsPanel", "ref"),
            new InternalSearchEntry("Precio Costo / Compra (de Producto)", "Stock > Productos",
                    "com.openbravo.pos.inventory.ProductsPanel", "pricebuy"),
            new InternalSearchEntry("Precio Venta (de Producto)", "Stock > Productos",
                    "com.openbravo.pos.inventory.ProductsPanel", "pricesell"),
            new InternalSearchEntry("Cantidad Mínima / Stock Mínimo (de Producto)", "Stock > Productos",
                    "com.openbravo.pos.inventory.ProductsPanel", "stockminimum"),
            new InternalSearchEntry("Cantidad Actual / Stock Actual (de Producto)", "Stock > Productos",
                    "com.openbravo.pos.inventory.ProductsPanel", "stockcurrent"),

            // Customer Editor internal fields
            new InternalSearchEntry("Nombre / Nombre Comercial (de Cliente)", "Clientes > Administrar",
                    "com.openbravo.pos.customers.CustomersPanel", "customer_name"),
            new InternalSearchEntry("RFC / TaxID (de Cliente)", "Clientes > Administrar",
                    "com.openbravo.pos.customers.CustomersPanel", "customer_taxid"),
            new InternalSearchEntry("Tarjeta / Card (de Cliente)", "Clientes > Administrar",
                    "com.openbravo.pos.customers.CustomersPanel", "customer_card"),
            new InternalSearchEntry("Teléfono (de Cliente)", "Clientes > Administrar",
                    "com.openbravo.pos.customers.CustomersPanel", "customer_phone"),
            new InternalSearchEntry("Email / Correo (de Cliente)", "Clientes > Administrar",
                    "com.openbravo.pos.customers.CustomersPanel", "customer_email"),
            new InternalSearchEntry("Dirección / Calle (de Cliente)", "Clientes > Administrar",
                    "com.openbravo.pos.customers.CustomersPanel", "customer_address"),
            new InternalSearchEntry("Código Postal / CP (de Cliente)", "Clientes > Administrar",
                    "com.openbravo.pos.customers.CustomersPanel", "customer_postal"),
            new InternalSearchEntry("Ciudad / Municipio (de Cliente)", "Clientes > Administrar",
                    "com.openbravo.pos.customers.CustomersPanel", "customer_city"),
            new InternalSearchEntry("Puntos / Puntaje (de Cliente)", "Clientes > Administrar",
                    "com.openbravo.pos.customers.CustomersPanel", "customer_points"));

    private static final Logger LOGGER = Logger.getLogger(JPanelSystemOverview.class.getName());

    private static final String MENU_ROOT_RESOURCE = "Menu.Root";
    private static final String MENU_ROOT_BUNDLE_PATH = "/com/openbravo/pos/templates/Menu.Root.bs";

    private static final String ROOT_KEY_MAIN = "Menu.Main";
    private static final String ROOT_KEY_BACKOFFICE = "Menu.Backoffice";
    private static final String ROOT_KEY_SYSTEM = "Menu.System";

    private static final String TASK_SYSTEM_OVERVIEW = "com.openbravo.pos.forms.JPanelSystemOverview";
    private static final String TASK_TICKET_SALES = "com.openbravo.pos.sales.JPanelTicketSales";
    private static final String TASK_CUSTOMER_PAYMENT = "com.openbravo.pos.customers.CustomersPayment";
    private static final String TASK_CLOSE_MONEY = "com.openbravo.pos.panels.JPanelCloseMoney";
    private static final String TASK_MENU_CUSTOMERS = "com.openbravo.pos.forms.MenuCustomers";
    private static final String TASK_MENU_SUPPLIERS = "com.openbravo.pos.forms.MenuSuppliers";
    private static final String TASK_MENU_STOCK = "com.openbravo.pos.forms.MenuStockManagement";
    private static final String TASK_MENU_SALES = "com.openbravo.pos.forms.MenuSalesManagement";
    private static final String TASK_MENU_MAINTENANCE = "com.openbravo.pos.forms.MenuMaintenance";
    private static final String TASK_CONFIGURATION = "com.openbravo.pos.config.JPanelConfiguration";
    private static final String TASK_PRINTER = "com.openbravo.pos.panels.JPanelPrinter";
    private static final String TASK_REPORTS_DASHBOARD = "com.openbravo.pos.reports.JPanelGraphics";
    private static final String TASK_BRANCHES = "com.openbravo.pos.branches.JPanelBranchesManagement";
    private static final String TASK_PAYMENTS = "com.openbravo.pos.panels.JPanelPayments";
    private static final String TASK_BREAKS = "com.openbravo.pos.epm.BreaksPanel";
    private static final String TASK_LOCATIONS = "com.openbravo.pos.inventory.LocationsPanel";
    private static final String TASK_HR = "com.openbravo.pos.admin.JPanelHR";

    private static final Color COLOR_BACKGROUND = new Color(246, 247, 243);
    private static final Color COLOR_PANEL = Color.WHITE;
    private static final Color COLOR_PANEL_SOFT = new Color(231, 236, 232);
    private static final Color COLOR_TEXT = new Color(31, 41, 38);
    private static final Color COLOR_TEXT_SOFT = new Color(73, 87, 81);
    private static final Color COLOR_TEXT_MUTED = new Color(112, 124, 118);
    private static final Color COLOR_ACCENT = new Color(214, 169, 61);
    private static final Color COLOR_ACCENT_SOFT = new Color(7, 55, 43);
    private static final Color COLOR_GREEN_SOFT = new Color(235, 243, 238);
    private static final Color COLOR_SEARCH = Color.WHITE;

    private static final String[] COLUMN_TITLES = {
            "Paneles Generales",
            "Soluciones Basicas",
            "Soluciones Avanzadas",
            "Soluciones Especiales"
    };

    private final AppUserView appUserView;
    private final DataLogicSystem dlSystem;

    private final JTextField searchField;
    private final JLabel infoLabel;
    private final JPanel columnsPanel;
    private final JPopupMenu suggestionPopup;

    private final List<OverviewRootGroup> rootGroups = new ArrayList<>();
    private boolean loaded;

    public JPanelSystemOverview(AppUserView appUserView, DataLogicSystem dlSystem) {
        this.appUserView = appUserView;
        this.dlSystem = dlSystem;

        setLayout(new BorderLayout());
        setOpaque(true);
        setBackground(COLOR_BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder());

        suggestionPopup = new JPopupMenu();
        suggestionPopup.setBackground(COLOR_PANEL);
        suggestionPopup.setBorder(BorderFactory.createLineBorder(COLOR_ACCENT, 1));

        JPanel shell = new JPanel(new BorderLayout(0, 20));
        shell.setOpaque(true);
        shell.setBackground(COLOR_BACKGROUND);
        shell.setBorder(BorderFactory.createEmptyBorder(24, 26, 22, 26));
        add(shell, BorderLayout.CENTER);

        JPanel headerPanel = new JPanel(new BorderLayout(28, 0));
        headerPanel.setOpaque(true);
        headerPanel.setBackground(COLOR_BACKGROUND);
        shell.add(headerPanel, BorderLayout.NORTH);

        headerPanel.add(createBrandPanel(), BorderLayout.WEST);

        JPanel searchBlock = new JPanel();
        searchBlock.setOpaque(false);
        searchBlock.setLayout(new BoxLayout(searchBlock, BoxLayout.Y_AXIS));
        searchBlock.setAlignmentX(LEFT_ALIGNMENT);
        searchBlock.setMaximumSize(new Dimension(1020, Integer.MAX_VALUE));

        JPanel searchRow = new JPanel(new BorderLayout(14, 0));
        searchRow.setOpaque(false);
        searchRow.setMaximumSize(new Dimension(980, 46));

        searchField = new JTextField();
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        searchField.setPreferredSize(new Dimension(720, 46));
        searchField.setMinimumSize(new Dimension(320, 46));
        searchField.setMaximumSize(new Dimension(720, 46));
        searchField.setBackground(COLOR_SEARCH);
        searchField.setForeground(COLOR_TEXT);
        searchField.setCaretColor(COLOR_ACCENT);
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(196, 207, 201), 1),
                BorderFactory.createEmptyBorder(10, 14, 10, 14)));
        searchField.putClientProperty("JTextField.roundRect", Boolean.TRUE);
        searchField.putClientProperty("JComponent.outline", COLOR_ACCENT);
        searchField.setToolTipText("Buscar modulo, reporte o proceso");

        searchField.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent e) {
                int keyCode = e.getKeyCode();
                if (suggestionPopup.isVisible()) {
                    if (keyCode == java.awt.event.KeyEvent.VK_DOWN) {
                        navigatePopup(1);
                        e.consume();
                    } else if (keyCode == java.awt.event.KeyEvent.VK_UP) {
                        navigatePopup(-1);
                        e.consume();
                    } else if (keyCode == java.awt.event.KeyEvent.VK_ENTER) {
                        triggerSelectedPopupItem();
                        e.consume();
                    } else if (keyCode == java.awt.event.KeyEvent.VK_ESCAPE) {
                        suggestionPopup.setVisible(false);
                        e.consume();
                    }
                } else {
                    if (keyCode == java.awt.event.KeyEvent.VK_DOWN || keyCode == java.awt.event.KeyEvent.VK_ENTER) {
                        searchField.transferFocus();
                        e.consume();
                    }
                }
            }
        });

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                renderOverview();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                renderOverview();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                renderOverview();
            }
        });
        searchRow.add(searchField, BorderLayout.CENTER);

        JPanel iconsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        iconsPanel.setOpaque(false);
        iconsPanel.add(
                createIconButton("🎛", "Configuración / Parámetros", () -> appUserView.showTask(TASK_CONFIGURATION)));
        iconsPanel.add(createIconButton("?", "Ayuda y Atajos de Teclado", () -> appUserView.showTask("com.openbravo.pos.forms.JPanelInstructions")));
        iconsPanel
                .add(createIconButton("▦", "Menú de Mantenimiento", () -> appUserView.showTask(TASK_MENU_MAINTENANCE)));
        iconsPanel.add(createIconButton("♥", "Ir a Ventas (F1)", () -> appUserView.showTask(TASK_TICKET_SALES)));
        iconsPanel
                .add(createIconButton("▶", "Gráficos de Reportes", () -> appUserView.showTask(TASK_REPORTS_DASHBOARD)));
        searchRow.add(iconsPanel, BorderLayout.EAST);

        searchBlock.add(searchRow);
        searchBlock.add(Box.createVerticalStrut(10));

        infoLabel = new JLabel("Cargando opciones...");
        infoLabel.setForeground(COLOR_TEXT_MUTED);
        infoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        searchBlock.add(infoLabel);

        JPanel searchShell = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        searchShell.setOpaque(false);
        searchShell.add(searchBlock);
        headerPanel.add(searchShell, BorderLayout.CENTER);
        columnsPanel = new JPanel(new GridBagLayout());
        columnsPanel.setOpaque(true);
        columnsPanel.setBackground(COLOR_BACKGROUND);

        JScrollPane scrollPane = new JScrollPane(columnsPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setViewportBorder(BorderFactory.createEmptyBorder());
        scrollPane.setOpaque(false);
        scrollPane.setBackground(COLOR_BACKGROUND);
        scrollPane.getViewport().setOpaque(true);
        scrollPane.getViewport().setBackground(COLOR_BACKGROUND);
        scrollPane.getVerticalScrollBar().setUnitIncrement(20);
        scrollPane.getVerticalScrollBar().setOpaque(false);
        scrollPane.getVerticalScrollBar().setBackground(COLOR_BACKGROUND);
        scrollPane.getVerticalScrollBar().setUI(new ArrowOnlyScrollBarUI());
        shell.add(scrollPane, BorderLayout.CENTER);
    }

    @Override
    public String getTitle() {
        return "";
    }

    @Override
    public void activate() throws BasicException {
        if (!loaded) {
            loadOverview();
            loaded = true;
        }
        renderOverview();
        SwingUtilities.invokeLater(this::requestSearchFocus);
    }

    @Override
    public boolean deactivate() {
        if (suggestionPopup != null) {
            suggestionPopup.setVisible(false);
        }
        return true;
    }

    @Override
    public JComponent getComponent() {
        return this;
    }

    public void requestSearchFocus() {
        searchField.requestFocusInWindow();
    }

    private JPanel createBrandPanel() {
        JPanel brandPanel = new JPanel();
        brandPanel.setOpaque(true);
        brandPanel.setBackground(COLOR_BACKGROUND);
        brandPanel.setLayout(new BoxLayout(brandPanel, BoxLayout.Y_AXIS));
        brandPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 8, 0));

        // Premium Title with HTML for mixed colors
        JLabel brandLabel = new JLabel(
                "<html><span style='color:#07372B'>CENTRO DE</span> <span style='color:#B58925'>GESTIÓN</span></html>");
        brandLabel.setFont(com.openbravo.pos.util.ModernLookAndFeel.getPreferredFont("Segoe UI", Font.BOLD, 27));
        brandPanel.add(brandLabel);

        brandPanel.add(Box.createVerticalStrut(2));

        JLabel subtitleLabel = new JLabel("Ventas, inventario y procesos en un solo lugar");
        subtitleLabel.setForeground(COLOR_TEXT_SOFT);
        subtitleLabel.setFont(com.openbravo.pos.util.ModernLookAndFeel.getPreferredFont("Baradig", Font.PLAIN, 14));
        brandPanel.add(subtitleLabel);

        brandPanel.add(Box.createVerticalStrut(8));

        // Separador decorativo "bonito" con degradado
        JPanel separator = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, COLOR_ACCENT, getWidth(), 0,
                        new Color(COLOR_ACCENT.getRed(), COLOR_ACCENT.getGreen(), COLOR_ACCENT.getBlue(), 0));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        separator.setPreferredSize(new Dimension(180, 3));
        separator.setMaximumSize(new Dimension(180, 3));
        separator.setAlignmentX(LEFT_ALIGNMENT);
        brandPanel.add(separator);

        brandPanel.add(Box.createVerticalStrut(4));

        JLabel helpLabel = new JLabel("Selecciona un módulo o escribe para encontrarlo");
        helpLabel.setForeground(COLOR_TEXT_MUTED);
        helpLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        brandPanel.add(helpLabel);

        return brandPanel;
    }

    private void loadOverview() {
        rootGroups.clear();
        try {
            String menuScript = resolveMenuScript();
            OverviewMenuRecorder recorder = new OverviewMenuRecorder();
            ScriptEngine engine = ScriptFactory.getScriptEngine(ScriptFactory.BEANSHELL);
            engine.put("menu", recorder);
            engine.eval(menuScript);
            rootGroups.addAll(recorder.getRootGroups());
            injectAdditionalEntries(rootGroups);
        } catch (IOException | ScriptException ex) {
            LOGGER.log(Level.SEVERE, "No fue posible construir la portada general", ex);
        }
    }

    private String resolveMenuScript() throws IOException {
        String databaseMenu = dlSystem.getResourceAsText(MENU_ROOT_RESOURCE);
        String bundledMenu = StringUtils.readResource(MENU_ROOT_BUNDLE_PATH);
        if (shouldUseBundledRootMenu(databaseMenu, bundledMenu)) {
            return bundledMenu;
        }
        return (databaseMenu == null || databaseMenu.isBlank()) ? bundledMenu : databaseMenu;
    }

    private boolean shouldUseBundledRootMenu(String databaseMenu, String bundledMenu) {
        // Sebastian - Forzar menú actualizado
        return true;
    }

    private void injectAdditionalEntries(List<OverviewRootGroup> groups) {
        injectTopNavigationEntries(groups);
        addIfMissing(groups, ROOT_KEY_MAIN, "Paneles Generales", "Paneles principales",
                "Entradas y salidas", TASK_PAYMENTS, EntryKind.SHOW_TASK);
        addIfMissing(groups, ROOT_KEY_BACKOFFICE, "Mantenimiento", "Administracion interna",
                "Administrar sucursales", TASK_BRANCHES, EntryKind.SHOW_TASK);
        addIfMissing(groups, ROOT_KEY_BACKOFFICE, "Gestion de Stock", "Gestion interna",
                "Ubicaciones", TASK_LOCATIONS, EntryKind.SHOW_TASK);
        addIfMissing(groups, ROOT_KEY_SYSTEM, "Sistema", "Soporte y salida",
                "Cerrar sesion", "system.exit", EntryKind.EXIT_TO_LOGIN);
    }

    private void injectTopNavigationEntries(List<OverviewRootGroup> groups) {
        OverviewRootGroup rootGroup = findOrCreateRootGroup(groups, ROOT_KEY_MAIN);
        OverviewCategory category = findOrCreateCategory(rootGroup, "Paneles Generales");
        OverviewSection quickAccessSection = new OverviewSection("Accesos principales");

        addQuickAccessEntry(quickAccessSection, "Inicio", TASK_SYSTEM_OVERVIEW);
        addQuickAccessEntry(quickAccessSection, "Ventas", TASK_TICKET_SALES);
        addQuickAccessEntry(quickAccessSection, "Pago de Clientes", TASK_CUSTOMER_PAYMENT);
        addQuickAccessEntry(quickAccessSection, "Cerrar Caja", TASK_CLOSE_MONEY);
        addQuickAccessEntry(quickAccessSection, "Clientes", TASK_MENU_CUSTOMERS);
        addQuickAccessEntry(quickAccessSection, "Proveedores", TASK_MENU_SUPPLIERS);
        addQuickAccessEntry(quickAccessSection, "Stock", TASK_MENU_STOCK);
        addQuickAccessEntry(quickAccessSection, "Gestion Ventas", TASK_MENU_SALES);
        addQuickAccessEntry(quickAccessSection, "Mantenimiento", TASK_MENU_MAINTENANCE);
        addQuickAccessEntry(quickAccessSection, "RRHH", TASK_HR);
        addQuickAccessEntry(quickAccessSection, "Configuracion", TASK_CONFIGURATION);
        addQuickAccessEntry(quickAccessSection, "Impresoras", TASK_PRINTER);
        addQuickAccessEntry(quickAccessSection, "Reportes", TASK_REPORTS_DASHBOARD);

        if (quickAccessSection.entries.isEmpty()) {
            return;
        }

        category.sections.add(0, quickAccessSection);
        removeDuplicatedQuickAccessEntries(category, quickAccessSection);
    }

    private void addQuickAccessEntry(OverviewSection section, String label, String task) {
        if (isTaskAllowed(task, EntryKind.SHOW_TASK)) {
            section.entries.add(new OverviewEntry(label, task, EntryKind.SHOW_TASK));
        }
    }

    private void removeDuplicatedQuickAccessEntries(OverviewCategory category, OverviewSection quickAccessSection) {
        List<String> tasksToKeepOnlyInQuickAccess = new ArrayList<>();
        for (OverviewEntry entry : quickAccessSection.entries) {
            tasksToKeepOnlyInQuickAccess.add(entry.task);
        }

        for (OverviewSection section : category.sections) {
            if (section == quickAccessSection) {
                continue;
            }
            section.entries.removeIf(entry -> tasksToKeepOnlyInQuickAccess.contains(entry.task));
        }
    }

    private void addIfMissing(List<OverviewRootGroup> groups, String rootKey, String categoryTitle,
            String sectionTitle, String label, String task, EntryKind kind) {
        if (task == null || task.isBlank() || containsTask(groups, task) || !isTaskAllowed(task, kind)) {
            return;
        }

        OverviewRootGroup rootGroup = findOrCreateRootGroup(groups, rootKey);
        OverviewCategory category = findOrCreateCategory(rootGroup, categoryTitle);
        OverviewSection section = findOrCreateSection(category, sectionTitle);
        section.entries.add(new OverviewEntry(label, task, kind));
    }

    private boolean containsTask(List<OverviewRootGroup> groups, String task) {
        for (OverviewRootGroup group : groups) {
            for (OverviewCategory category : group.categories) {
                for (OverviewSection section : category.sections) {
                    for (OverviewEntry entry : section.entries) {
                        if (task.equals(entry.task)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private boolean isTaskAllowed(String task, EntryKind kind) {
        if (kind == EntryKind.EXIT_TO_LOGIN) {
            return true;
        }
        return appUserView.getUser().hasPermission(task);
    }

    private OverviewRootGroup findOrCreateRootGroup(List<OverviewRootGroup> groups, String rootKey) {
        for (OverviewRootGroup group : groups) {
            if (equalsIgnoreNull(group.rawKey, rootKey)) {
                return group;
            }
        }
        OverviewRootGroup group = new OverviewRootGroup(rootKey, resolveRootGroupTitle(rootKey));
        groups.add(group);
        return group;
    }

    private OverviewCategory findOrCreateCategory(OverviewRootGroup rootGroup, String categoryTitle) {
        for (OverviewCategory category : rootGroup.categories) {
            if (category.displayTitle.equalsIgnoreCase(categoryTitle)) {
                return category;
            }
        }
        OverviewCategory category = new OverviewCategory(categoryTitle, null);
        rootGroup.categories.add(category);
        return category;
    }

    private OverviewSection findOrCreateSection(OverviewCategory category, String sectionTitle) {
        for (OverviewSection section : category.sections) {
            if (section.title.equalsIgnoreCase(sectionTitle)) {
                return section;
            }
        }
        OverviewSection section = new OverviewSection(sectionTitle);
        category.sections.add(section);
        return section;
    }

    private void renderOverview() {
        columnsPanel.removeAll();

        String query = normalizeQuery(searchField.getText());
        OverviewColumnData[] columns = createColumns(query);
        int totalGroups = 0;
        int totalEntries = 0;

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.weighty = 1.0;
        gbc.insets = new Insets(0, 0, 0, 18);

        for (int index = 0; index < columns.length; index++) {
            OverviewColumnData column = columns[index];
            totalGroups += column.groups.size();
            totalEntries += column.entryCount;

            gbc.gridx = index;
            gbc.weightx = 1.0;
            gbc.insets = new Insets(0, 0, 0, index == columns.length - 1 ? 0 : 18);
            columnsPanel.add(createColumnPanel(column), gbc);
        }

        infoLabel.setText(buildInfoText(totalGroups, totalEntries, query));
        columnsPanel.revalidate();
        columnsPanel.repaint();
        updateSuggestions(query);
    }

    private String buildInfoText(int totalGroups, int totalEntries, String query) {
        if (totalEntries == 0) {
            return "No hay coincidencias con el termino buscado.";
        }
        if (query.isBlank()) {
            return totalGroups + " grupos principales y " + totalEntries + " accesos visibles en esta sesion.";
        }
        return totalEntries + " coincidencias distribuidas en " + totalGroups + " grupos.";
    }

    private OverviewColumnData[] createColumns(String query) {
        OverviewColumnData[] columns = new OverviewColumnData[COLUMN_TITLES.length];
        for (int index = 0; index < COLUMN_TITLES.length; index++) {
            columns[index] = new OverviewColumnData(COLUMN_TITLES[index]);
        }

        for (OverviewRootGroup rootGroup : rootGroups) {
            for (OverviewCategory category : rootGroup.categories) {
                VisibleCategory visibleCategory = buildVisibleCategory(category, query);
                if (visibleCategory == null) {
                    continue;
                }
                int columnIndex = resolveColumnIndex(rootGroup.rawKey, category.displayTitle);
                columns[columnIndex].groups.add(visibleCategory);
                columns[columnIndex].entryCount += visibleCategory.entryCount;
            }
        }

        return columns;
    }

    private int resolveColumnIndex(String rootKey, String categoryTitle) {
        String normalized = normalizeForSearch(categoryTitle);

        if (matchesAny(normalized, "paneles generales", "sistema", "caja")) {
            return 0;
        }
        if (matchesAny(normalized, "clientes", "proveedores", "stock", "inventario")) {
            return 1;
        }
        if (matchesAny(normalized, "ventas", "mantenimiento", "reportes", "graficos", "import")) {
            return 2;
        }
        if (matchesAny(normalized, "presencia", "empleados", "recursos humanos", "reparaciones", "documentos")) {
            return 3;
        }

        if (equalsIgnoreNull(rootKey, ROOT_KEY_MAIN) || equalsIgnoreNull(rootKey, ROOT_KEY_SYSTEM)) {
            return 0;
        }
        if (equalsIgnoreNull(rootKey, ROOT_KEY_BACKOFFICE)) {
            return 2;
        }
        return 0;
    }

    private boolean matchesAny(String value, String... fragments) {
        for (String fragment : fragments) {
            if (value.contains(fragment)) {
                return true;
            }
        }
        return false;
    }

    private JPanel createColumnPanel(OverviewColumnData column) {
        JPanel panel = new SurfacePanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));

        JLabel titleLabel = new JLabel(column.title);
        titleLabel.setForeground(COLOR_TEXT);
        titleLabel.setFont(com.openbravo.pos.util.ModernLookAndFeel.getPreferredFont("Segoe UI", Font.BOLD, 20));
        titleLabel.setAlignmentX(LEFT_ALIGNMENT);
        panel.add(titleLabel);

        panel.add(Box.createVerticalStrut(6));

        JPanel underline = new JPanel();
        underline.setMaximumSize(new Dimension(54, 3));
        underline.setPreferredSize(new Dimension(54, 3));
        underline.setMinimumSize(new Dimension(54, 3));
        underline.setBackground(COLOR_ACCENT);
        underline.setAlignmentX(LEFT_ALIGNMENT);
        panel.add(underline);

        panel.add(Box.createVerticalStrut(16));

        if (column.groups.isEmpty()) {
            JLabel emptyLabel = new JLabel("Sin opciones con este filtro");
            emptyLabel.setForeground(COLOR_TEXT_MUTED);
            emptyLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            panel.add(emptyLabel);
        } else {
            for (VisibleCategory group : column.groups) {
                panel.add(createGroupPanel(group));
                panel.add(Box.createVerticalStrut(14));
            }
        }

        panel.add(Box.createVerticalGlue());

        // Dynamically compute preferred and minimum sizes to allow scrolling
        panel.setPreferredSize(new Dimension(280, panel.getPreferredSize().height));
        panel.setMinimumSize(new Dimension(230, panel.getMinimumSize().height));

        return panel;
    }

    private JPanel createGroupPanel(VisibleCategory group) {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setAlignmentX(LEFT_ALIGNMENT);

        JPanel titleRow = new JPanel(new BorderLayout(8, 0));
        titleRow.setOpaque(false);
        titleRow.setAlignmentX(LEFT_ALIGNMENT);

        JLabel titleLabel = new JLabel(group.title);
        titleLabel.setForeground(COLOR_ACCENT_SOFT);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 19));
        titleRow.add(titleLabel, BorderLayout.WEST);

        if (group.summaryTask != null) {
            JButton openButton = createMiniOpenButton();
            openButton.addActionListener(event -> appUserView.showTask(group.summaryTask));
            titleRow.add(openButton, BorderLayout.EAST);
        }

        panel.add(titleRow);
        panel.add(Box.createVerticalStrut(8));

        for (VisibleSection section : group.sections) {
            if (section.showTitle) {
                JLabel sectionLabel = new JLabel(section.title);
                sectionLabel.setForeground(COLOR_TEXT_MUTED);
                sectionLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
                sectionLabel.setAlignmentX(LEFT_ALIGNMENT);
                panel.add(sectionLabel);
                panel.add(Box.createVerticalStrut(4));
            }

            for (OverviewEntry entry : section.entries) {
                JButton entryButton = createEntryButton(entry);
                entryButton.setAlignmentX(LEFT_ALIGNMENT);
                panel.add(entryButton);
                panel.add(Box.createVerticalStrut(3));
            }

            panel.add(Box.createVerticalStrut(8));
        }

        return panel;
    }

    private JButton createMiniOpenButton() {
        JButton button = new JButton("Abrir");
        button.setFont(new Font("Segoe UI", Font.BOLD, 11));
        button.setForeground(COLOR_ACCENT_SOFT);
        button.setOpaque(true);
        button.setBackground(COLOR_PANEL);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_PANEL_SOFT, 1),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)));
        button.setFocusPainted(false);
        button.setFocusable(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.putClientProperty("JButton.buttonType", "roundRect");
        return button;
    }

    private JButton createEntryButton(OverviewEntry entry) {
        JButton button = new JButton(entry.label);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setForeground(COLOR_TEXT);
        button.setOpaque(true);
        button.setBackground(new Color(249, 250, 248));
        button.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        button.setFocusPainted(false);
        button.setContentAreaFilled(true);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.putClientProperty("JButton.buttonType", "roundRect");
        button.putClientProperty("JButton.arc", 12);
        button.addActionListener(event -> executeEntry(entry));
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setForeground(COLOR_ACCENT_SOFT);
                button.setBackground(COLOR_GREEN_SOFT);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (!button.isFocusOwner()) {
                    button.setForeground(COLOR_TEXT);
                    button.setBackground(new Color(249, 250, 248));
                }
            }
        });
        button.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                button.setForeground(COLOR_ACCENT_SOFT);
                button.setBackground(COLOR_GREEN_SOFT);
                button.scrollRectToVisible(new java.awt.Rectangle(0, 0, button.getWidth(), button.getHeight()));
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                button.setForeground(COLOR_TEXT);
                button.setBackground(new Color(249, 250, 248));
            }
        });
        button.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent e) {
                int keyCode = e.getKeyCode();
                if (keyCode == java.awt.event.KeyEvent.VK_DOWN) {
                    button.transferFocus();
                    e.consume();
                } else if (keyCode == java.awt.event.KeyEvent.VK_UP) {
                    button.transferFocusBackward();
                    e.consume();
                } else if (keyCode == java.awt.event.KeyEvent.VK_ENTER) {
                    button.doClick();
                    e.consume();
                }
            }
        });
        return button;
    }

    private void executeEntry(OverviewEntry entry) {
        switch (entry.kind) {
            case SHOW_TASK:
                appUserView.showTask(entry.task);
                break;
            case EXECUTE_TASK:
                appUserView.executeTask(entry.task);
                break;
            case EXIT_TO_LOGIN:
                appUserView.exitToLogin();
                break;
            default:
                break;
        }
    }

    private VisibleCategory buildVisibleCategory(OverviewCategory category, String query) {
        boolean categoryMatches = query.isBlank() || matches(category.displayTitle, query);
        List<VisibleSection> visibleSections = new ArrayList<>();
        int visibleEntries = 0;

        for (OverviewSection section : category.sections) {
            boolean sectionMatches = categoryMatches || matches(section.title, query);
            List<OverviewEntry> entries = new ArrayList<>();
            for (OverviewEntry entry : section.entries) {
                if (sectionMatches || matches(entry.label, query)) {
                    entries.add(entry);
                }
            }
            if (!entries.isEmpty()) {
                boolean showTitle = category.sections.size() > 1 && !isGenericSectionTitle(section.title);
                visibleSections.add(new VisibleSection(section.title, entries, showTitle));
                visibleEntries += entries.size();
            }
        }

        if (visibleSections.isEmpty()) {
            return null;
        }

        return new VisibleCategory(category.displayTitle, category.summaryTask, visibleSections, visibleEntries);
    }

    private boolean isGenericSectionTitle(String title) {
        String normalized = normalizeForSearch(title);
        return normalized.isBlank() || normalized.equals("opciones");
    }

    private boolean matches(String text, String query) {
        return query.isBlank() || normalizeForSearch(text).contains(query);
    }

    private String normalizeQuery(String text) {
        if (text == null) {
            return "";
        }
        String normalized = stripHtml(text);
        normalized = Normalizer.normalize(normalized, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .trim();
        return normalized;
    }

    private String normalizeForSearch(String text) {
        if (text == null) {
            return "";
        }
        String normalized = stripHtml(resolveText(text));
        normalized = Normalizer.normalize(normalized, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .trim();
        return normalized;
    }

    private String resolveRootGroupTitle(String key) {
        if (matchesKey(key, "Menu.Main", "Caja")) {
            return "Paneles Generales";
        }
        if (matchesKey(key, "Menu.Backoffice", "Administracion")) {
            return "Backoffice";
        }
        if (matchesKey(key, "Menu.System", "Sistema")) {
            return "Sistema";
        }
        return resolveText(key);
    }

    private String resolveDirectCategoryTitle(String rootKey, String rootTitle) {
        if (matchesKey(rootKey, ROOT_KEY_MAIN, "Caja")) {
            return "Paneles Generales";
        }
        if (matchesKey(rootKey, ROOT_KEY_SYSTEM, "Sistema")) {
            return "Sistema";
        }
        return rootTitle;
    }

    private boolean matchesKey(String value, String expectedKey, String fallbackLabel) {
        String safe = value == null ? "" : value;
        return safe.equalsIgnoreCase(expectedKey) || safe.equalsIgnoreCase(fallbackLabel);
    }

    private String resolveText(String keyOrLabel) {
        if (keyOrLabel == null) {
            return "";
        }
        String translated = AppLocal.getIntString(keyOrLabel);
        if (translated == null || translated.isBlank()) {
            return stripHtml(keyOrLabel);
        }
        return stripHtml(translated);
    }

    private String stripHtml(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replaceAll("<[^>]+>", " ")
                .replace("&nbsp;", " ")
                .replace("*", " ")
                .replace("_", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private boolean equalsIgnoreNull(String first, String second) {
        if (first == null && second == null) {
            return true;
        }
        if (first == null || second == null) {
            return false;
        }
        return first.equalsIgnoreCase(second);
    }

    private String resolveBrandTitle() {
        try {
            AppConfig config = AppConfig.getInstance();
            config.load();
            String text = stripHtml(config.getProperty("start.text"));
            if (!text.isBlank()) {
                return text.toUpperCase(Locale.ROOT);
            }
        } catch (Exception ex) {
            LOGGER.log(Level.FINE, "No se pudo leer start.text para la portada", ex);
        }
        return "WEBSY GROUP";
    }

    private final class OverviewMenuRecorder implements Menu {

        private final List<OverviewRootGroup> recorderGroups = new ArrayList<>();

        @Override
        public MenuGroup addGroup(String key) {
            OverviewRootGroup group = new OverviewRootGroup(key, resolveRootGroupTitle(key));
            recorderGroups.add(group);
            return new OverviewGroupRecorder(group);
        }

        public List<OverviewRootGroup> getRootGroups() {
            return recorderGroups;
        }
    }

    private final class OverviewGroupRecorder implements Menu.MenuGroup {

        private final OverviewRootGroup rootGroup;
        private OverviewCategory directCategory;

        private OverviewGroupRecorder(OverviewRootGroup rootGroup) {
            this.rootGroup = rootGroup;
        }

        @Override
        public void addPanel(String icon, String key, String classname) {
            if (!appUserView.getUser().hasPermission(classname)) {
                return;
            }
            ensureDirectSection("Paneles principales").entries.add(
                    new OverviewEntry(stripHtml(resolveText(key)), classname, EntryKind.SHOW_TASK));
        }

        @Override
        public void addExecution(String icon, String key, String classname) {
            if (!appUserView.getUser().hasPermission(classname)) {
                return;
            }
            ensureDirectSection("Acciones").entries.add(
                    new OverviewEntry(stripHtml(resolveText(key)), classname, EntryKind.EXECUTE_TASK));
        }

        @Override
        public Menu.Submenu addSubmenu(String icon, String key, String classname) {
            if (!appUserView.getUser().hasPermission(classname)) {
                return new HiddenSubmenuRecorder();
            }
            OverviewCategory category = new OverviewCategory(stripHtml(resolveText(key)), classname);
            rootGroup.categories.add(category);
            return new OverviewSubmenuRecorder(category);
        }

        @Override
        public void addChangePasswordAction() {
        }

        @Override
        public void addExitAction() {
            ensureDirectSection("Soporte y salida").entries.add(
                    new OverviewEntry("Cerrar sesion", "system.exit", EntryKind.EXIT_TO_LOGIN));
        }

        private OverviewSection ensureDirectSection(String title) {
            if (directCategory == null) {
                directCategory = new OverviewCategory(
                        resolveDirectCategoryTitle(rootGroup.rawKey, rootGroup.displayTitle), null);
                rootGroup.categories.add(directCategory);
            }
            for (OverviewSection section : directCategory.sections) {
                if (section.title.equalsIgnoreCase(title)) {
                    return section;
                }
            }
            OverviewSection section = new OverviewSection(title);
            directCategory.sections.add(section);
            return section;
        }
    }

    private final class OverviewSubmenuRecorder implements Menu.Submenu {

        private final OverviewCategory category;
        private OverviewSection currentSection;

        private OverviewSubmenuRecorder(OverviewCategory category) {
            this.category = category;
        }

        @Override
        public void addTitle(String key) {
            currentSection = new OverviewSection(stripHtml(resolveText(key)));
            category.sections.add(currentSection);
        }

        @Override
        public void addPanel(String icon, String key, String classname) {
            if (!appUserView.getUser().hasPermission(classname)) {
                return;
            }
            ensureSection().entries.add(new OverviewEntry(stripHtml(resolveText(key)), classname, EntryKind.SHOW_TASK));
        }

        @Override
        public void addExecution(String icon, String key, String classname) {
            if (!appUserView.getUser().hasPermission(classname)) {
                return;
            }
            ensureSection().entries
                    .add(new OverviewEntry(stripHtml(resolveText(key)), classname, EntryKind.EXECUTE_TASK));
        }

        @Override
        public Menu.Submenu addSubmenu(String icon, String key, String classname) {
            if (!appUserView.getUser().hasPermission(classname)) {
                return new HiddenSubmenuRecorder();
            }
            ensureSection().entries.add(new OverviewEntry(stripHtml(resolveText(key)), classname, EntryKind.SHOW_TASK));
            return new HiddenSubmenuRecorder();
        }

        private OverviewSection ensureSection() {
            if (currentSection == null) {
                currentSection = new OverviewSection("Opciones");
                category.sections.add(currentSection);
            }
            return currentSection;
        }
    }

    private static final class HiddenSubmenuRecorder implements Menu.Submenu {

        @Override
        public void addTitle(String key) {
        }

        @Override
        public void addPanel(String icon, String key, String classname) {
        }

        @Override
        public void addExecution(String icon, String key, String classname) {
        }

        @Override
        public Menu.Submenu addSubmenu(String icon, String key, String classname) {
            return this;
        }
    }

    private enum EntryKind {
        SHOW_TASK,
        EXECUTE_TASK,
        EXIT_TO_LOGIN
    }

    private static final class OverviewRootGroup {

        private final String rawKey;
        private final String displayTitle;
        private final List<OverviewCategory> categories = new ArrayList<>();

        private OverviewRootGroup(String rawKey, String displayTitle) {
            this.rawKey = rawKey;
            this.displayTitle = displayTitle;
        }
    }

    private static final class OverviewCategory {

        private final String displayTitle;
        private final String summaryTask;
        private final List<OverviewSection> sections = new ArrayList<>();

        private OverviewCategory(String displayTitle, String summaryTask) {
            this.displayTitle = displayTitle;
            this.summaryTask = summaryTask;
        }
    }

    private static final class OverviewSection {

        private final String title;
        private final List<OverviewEntry> entries = new ArrayList<>();

        private OverviewSection(String title) {
            this.title = title;
        }
    }

    private static final class OverviewEntry {

        private final String label;
        private final String task;
        private final EntryKind kind;

        private OverviewEntry(String label, String task, EntryKind kind) {
            this.label = label;
            this.task = task;
            this.kind = kind;
        }
    }

    private static final class VisibleSection {

        private final String title;
        private final List<OverviewEntry> entries;
        private final boolean showTitle;

        private VisibleSection(String title, List<OverviewEntry> entries, boolean showTitle) {
            this.title = title;
            this.entries = entries;
            this.showTitle = showTitle;
        }
    }

    private static final class VisibleCategory {

        private final String title;
        private final String summaryTask;
        private final List<VisibleSection> sections;
        private final int entryCount;

        private VisibleCategory(String title, String summaryTask, List<VisibleSection> sections, int entryCount) {
            this.title = title;
            this.summaryTask = summaryTask;
            this.sections = sections;
            this.entryCount = entryCount;
        }
    }

    private static final class OverviewColumnData {

        private final String title;
        private final List<VisibleCategory> groups = new ArrayList<>();
        private int entryCount;

        private OverviewColumnData(String title) {
            this.title = title;
        }
    }

    private JButton createIconButton(String iconText, String tooltip, Runnable action) {
        JButton btn = new JButton(iconText);
        btn.setFont(new Font("Segoe UI Symbol", Font.BOLD, 22));
        btn.setForeground(COLOR_TEXT_SOFT);
        btn.setBackground(COLOR_BACKGROUND);
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        btn.setFocusPainted(false);
        btn.setFocusable(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setToolTipText(tooltip);

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setForeground(COLOR_ACCENT);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btn.setForeground(COLOR_TEXT_SOFT);
            }
        });

        if (action != null) {
            btn.addActionListener(e -> action.run());
        }

        return btn;
    }

    private void showHelpDialog() {
        java.awt.Window parentWindow = javax.swing.SwingUtilities.getWindowAncestor(this);
        javax.swing.JDialog dialog = new javax.swing.JDialog(parentWindow, "Instructivo y Atajos del Sistema", java.awt.Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(850, 680);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new java.awt.BorderLayout());
        
        java.awt.Color corporateGold = new java.awt.Color(202, 159, 65);
        java.awt.Color creamBg = new java.awt.Color(250, 247, 242);
        
        javax.swing.JEditorPane editor = new javax.swing.JEditorPane();
        editor.setEditable(false);
        editor.setContentType("text/html");
        
        // HTML con estilos básicos para compatibilidad con Swing HTML3.2
        StringBuilder html = new StringBuilder();
        html.append("<html><body style='font-family: Segoe UI, sans-serif; background-color: #FAF7F2; margin: 20px; color: #334155;'>");
        
        // Encabezado
        html.append("<table width='100%' border='0' cellpadding='15' cellspacing='0' style='background-color: #CA9F41; margin-bottom: 20px;'>");
        html.append("<tr><td>");
        html.append("<h1 style='margin: 0; font-size: 24px; color: #ffffff;'>Voltium Sanrey</h1>");
        html.append("<p style='margin: 5px 0 0 0; font-size: 13px; color: #ffffff; opacity: 0.9;'>Manual de Usuario e Instructivo General del Sistema POS</p>");
        html.append("</td></tr>");
        html.append("</table>");
        
        // Atajos de teclado
        html.append("<h2 style='color: #CA9F41; border-bottom: 2px solid #CA9F41; padding-bottom: 5px;'>⌨️ Atajos de Teclado Rápidos</h2>");
        html.append("<table width='100%' border='0' cellpadding='8' cellspacing='0' style='margin-bottom: 20px;'>");
        html.append("<tr style='background-color: #E2E8F0;'>");
        html.append("<th align='left' style='padding: 8px;'>Atajo</th>");
        html.append("<th align='left' style='padding: 8px;'>Acción / Pantalla</th>");
        html.append("</tr>");
        
        html.append("<tr style='background-color: #ffffff;'>");
        html.append("<td style='padding: 8px; border-bottom: 1px solid #CBD5E1;'><b>F1</b></td>");
        html.append("<td style='padding: 8px; border-bottom: 1px solid #CBD5E1;'>Ir al panel de <b>Ventas / Facturación</b> (para cobrar o registrar tickets)</td>");
        html.append("</tr>");
        
        html.append("<tr style='background-color: #f8fafc;'>");
        html.append("<td style='padding: 8px; border-bottom: 1px solid #CBD5E1;'><b>F2</b></td>");
        html.append("<td style='padding: 8px; border-bottom: 1px solid #CBD5E1;'>Ir a <b>Cerrar Caja / Turnos</b> (para cortes de caja diarios y mensuales)</td>");
        html.append("</tr>");
        
        html.append("<tr style='background-color: #ffffff;'>");
        html.append("<td style='padding: 8px; border-bottom: 1px solid #CBD5E1;'><b>F3</b></td>");
        html.append("<td style='padding: 8px; border-bottom: 1px solid #CBD5E1;'>Ir a <b>Stock / Gestión de Inventario</b> (para productos y almacén)</td>");
        html.append("</tr>");
        
        html.append("<tr style='background-color: #f8fafc;'>");
        html.append("<td style='padding: 8px; border-bottom: 1px solid #CBD5E1;'><b>F4</b></td>");
        html.append("<td style='padding: 8px; border-bottom: 1px solid #CBD5E1;'>Ir a <b>Reportes y Gráficos</b> (para analizar ventas e ingresos)</td>");
        html.append("</tr>");
        
        html.append("<tr style='background-color: #ffffff;'>");
        html.append("<td style='padding: 8px; border-bottom: 1px solid #CBD5E1;'><b>Esc</b></td>");
        html.append("<td style='padding: 8px; border-bottom: 1px solid #CBD5E1;'>Regresar a la pantalla de <b>Inicio / Portada</b> (Índice General) desde cualquier módulo</td>");
        html.append("</tr>");
        html.append("</table>");
        
        // Explicación de vistas
        html.append("<h2 style='color: #CA9F41; border-bottom: 2px solid #CA9F41; padding-bottom: 5px;'>🖥️ Instructivo de las Vistas Clave</h2>");
        
        // Inicio
        html.append("<table width='100%' border='0' cellpadding='12' cellspacing='0' style='background-color: #ffffff; border: 1px solid #E2E8F0; margin-bottom: 15px;'>");
        html.append("<tr><td>");
        html.append("<h3 style='margin: 0 0 6px 0; color: #1E293B;'>🏠 Portada (Índice General)</h3>");
        html.append("<p style='margin: 0; font-size: 13px; line-height: 1.4; color: #475569;'>");
        html.append("Es el menú central de la aplicación. Permite ver todos los módulos agrupados. ");
        html.append("En la parte superior, dispones de una <b>barra de búsqueda inteligente</b>: al escribir el nombre de un campo (como <i>'Teléfono'</i>, <i>'RFC'</i>, o <i>'Lote'</i>), el buscador te listará los módulos que contienen ese campo para ayudarte a ubicarlos rápidamente.");
        html.append("</p></td></tr></table>");
        
        // Ventas
        html.append("<table width='100%' border='0' cellpadding='12' cellspacing='0' style='background-color: #ffffff; border: 1px solid #E2E8F0; margin-bottom: 15px;'>");
        html.append("<tr><td>");
        html.append("<h3 style='margin: 0 0 6px 0; color: #1E293B;'>🛒 Ventas y Facturación</h3>");
        html.append("<p style='margin: 0; font-size: 13px; line-height: 1.4; color: #475569;'>");
        html.append("Aquí registras las ventas del negocio. Agrega productos con un lector de código de barras o usa el buscador manual de productos. ");
        html.append("Al pagar, se despliega el menú de cobro que soporta <b>Efectivo, Tarjeta, Cheques, Vales de Despensa y Créditos</b>.");
        html.append("</p></td></tr></table>");
        
        // Puntos
        html.append("<table width='100%' border='0' cellpadding='12' cellspacing='0' style='background-color: #ffffff; border: 1px solid #E2E8F0; margin-bottom: 15px;'>");
        html.append("<tr><td>");
        html.append("<h3 style='margin: 0 0 6px 0; color: #1E293B;'>🎁 Sistema de Fidelización (Puntos)</h3>");
        html.append("<p style='margin: 0; font-size: 13px; line-height: 1.4; color: #475569;'>");
        html.append("Por cada <b>$400.00 MX de compra, el cliente acumula 10 puntos</b>. ");
        html.append("Cuando seleccionas un cliente en el panel de ventas, el saldo acumulado se muestra en la barra superior. ");
        html.append("Los clientes pueden redimir estos puntos para obtener descuentos directos al momento de pagar.");
        html.append("</p></td></tr></table>");
        
        // Cierre de caja
        html.append("<table width='100%' border='0' cellpadding='12' cellspacing='0' style='background-color: #ffffff; border: 1px solid #E2E8F0; margin-bottom: 15px;'>");
        html.append("<tr><td>");
        html.append("<h3 style='margin: 0 0 6px 0; color: #1E293B;'>📊 Cierres de Caja (Cortes y Conciliaciones)</h3>");
        html.append("<p style='margin: 0; font-size: 13px; line-height: 1.4; color: #475569;'>");
        html.append("• <b>Corte de Cajero:</b> Muestra el arqueo detallado del cajero activo en el turno actual.<br>");
        html.append("• <b>Corte del Día:</b> Consolida la suma de todos los turnos abiertos/cerrados durante el día.<br>");
        html.append("• <b>Cerrar Mes:</b> Consolida de forma masiva todos los turnos iniciados durante el mes calendario actual (devoluciones, ganancias, ventas por departamento, egresos e ingresos).<br>");
        html.append("• <b>Conciliación Física:</b> Al presionar el botón de cerrar caja, el sistema te pedirá ingresar el efectivo físico real contado en caja, calculando y reportando automáticamente si hay algún <b>sobrante o faltante</b>.");
        html.append("</p></td></tr></table>");
        
        // Reportes
        html.append("<table width='100%' border='0' cellpadding='12' cellspacing='0' style='background-color: #ffffff; border: 1px solid #E2E8F0; margin-bottom: 15px;'>");
        html.append("<tr><td>");
        html.append("<h3 style='margin: 0 0 6px 0; color: #1E293B;'>📄 Hojas Membretadas en Reportes</h3>");
        html.append("<p style='margin: 0; font-size: 13px; line-height: 1.4; color: #475569;'>");
        html.append("Los reportes del listado de clientes y listado de proveedores se generan automáticamente con un formato premium de **Hoja Membretada** que contiene el logotipo centrado, la dirección fiscal al pie de página con fondo dorado y toda la información en español.");
        html.append("</p></td></tr></table>");
        
        // Soporte
        html.append("<div style='margin-top: 25px; font-size: 11px; text-align: center; color: #94A3B8;'>");
        html.append("Soporte Técnico Websy Group &copy; 2026. Todos los derechos reservados.");
        html.append("</div>");
        
        html.append("</body></html>");
        
        editor.setText(html.toString());
        
        javax.swing.JScrollPane scroll = new javax.swing.JScrollPane(editor);
        scroll.setBorder(javax.swing.BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(creamBg);
        
        dialog.add(scroll, java.awt.BorderLayout.CENTER);
        
        javax.swing.JPanel bottomPanel = new javax.swing.JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 15, 12));
        bottomPanel.setBackground(creamBg);
        bottomPanel.setBorder(javax.swing.BorderFactory.createMatteBorder(1, 0, 0, 0, new java.awt.Color(226, 232, 240)));
        
        javax.swing.JButton closeBtn = new javax.swing.JButton("Entendido / Cerrar");
        closeBtn.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 13));
        closeBtn.setForeground(java.awt.Color.WHITE);
        closeBtn.setBackground(corporateGold);
        closeBtn.setFocusPainted(false);
        closeBtn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        
        closeBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                closeBtn.setBackground(new java.awt.Color(220, 175, 75));
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                closeBtn.setBackground(corporateGold);
            }
        });
        
        closeBtn.addActionListener(event -> dialog.dispose());
        bottomPanel.add(closeBtn);
        dialog.add(bottomPanel, java.awt.BorderLayout.SOUTH);
        
        javax.swing.SwingUtilities.invokeLater(() -> scroll.getViewport().setViewPosition(new java.awt.Point(0, 0)));
        dialog.setVisible(true);
    }

    private void updateSuggestions(String query) {
        suggestionPopup.setVisible(false);
        if (query.isBlank()) {
            return;
        }

        String normalizedQuery = normalizeForSearch(query);

        class SuggestionMatch {
            final String label;
            final String path;
            final Runnable action;

            SuggestionMatch(String label, String path, Runnable action) {
                this.label = label;
                this.path = path;
                this.action = action;
            }
        }

        List<SuggestionMatch> matches = new ArrayList<>();

        // 1. Check standard menu entries
        for (OverviewRootGroup rootGroup : rootGroups) {
            for (OverviewCategory category : rootGroup.categories) {
                for (OverviewSection section : category.sections) {
                    for (OverviewEntry entry : section.entries) {
                        if (normalizeForSearch(entry.label).contains(normalizedQuery)) {
                            matches.add(new SuggestionMatch(
                                    entry.label,
                                    category.displayTitle,
                                    () -> executeEntry(entry)));
                        }
                    }
                }
            }
        }

        // 2. Check internal search entries
        for (InternalSearchEntry internal : INTERNAL_SEARCHES) {
            if (normalizeForSearch(internal.label).contains(normalizedQuery)) {
                matches.add(new SuggestionMatch(
                        internal.label,
                        internal.path,
                        () -> {
                            searchTargetField = internal.targetField;
                            appUserView.showTask(internal.task);
                        }));
            }
        }

        if (!matches.isEmpty()) {
            suggestionPopup.removeAll();
            int limit = 10;
            int count = 0;
            for (SuggestionMatch match : matches) {
                if (count >= limit)
                    break;

                JMenuItem item = new JMenuItem();
                item.setPreferredSize(new Dimension(720, 36));
                item.setBackground(COLOR_PANEL);
                item.setForeground(COLOR_TEXT);
                item.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));

                String text = match.label;
                String category = match.path;
                String htmlText = "<html><body style='width: 680px; font-family: Segoe UI; margin: 0; padding: 0;'>"
                        + "<table width='100%' cellpadding='0' cellspacing='0'>"
                        + "<tr>"
                        + "  <td align='left' style='font-size: 13px; font-weight: bold; color: #F5F5F5;'>" + text
                        + "</td>"
                        + "  <td align='right' style='font-size: 11px; color: #AAAAAA; font-style: italic;'>" + category
                        + "</td>"
                        + "</tr>"
                        + "</table>"
                        + "</body></html>";
                item.setText(htmlText);

                item.addActionListener(event -> {
                    match.action.run();
                    suggestionPopup.setVisible(false);
                    searchField.setText("");
                });

                suggestionPopup.add(item);
                count++;
            }

            suggestionPopup.show(searchField, 0, searchField.getHeight());
            searchField.requestFocusInWindow();
        }
    }

    private void navigatePopup(int direction) {
        MenuElement[] path = MenuSelectionManager.defaultManager().getSelectedPath();
        int count = suggestionPopup.getComponentCount();
        if (count == 0)
            return;

        int nextIndex = 0;
        if (path.length > 0 && path[path.length - 1] instanceof JMenuItem) {
            JMenuItem current = (JMenuItem) path[path.length - 1];
            int currentIndex = -1;
            for (int i = 0; i < count; i++) {
                if (suggestionPopup.getComponent(i) == current) {
                    currentIndex = i;
                    break;
                }
            }
            nextIndex = currentIndex + direction;
            if (nextIndex < 0)
                nextIndex = count - 1;
            if (nextIndex >= count)
                nextIndex = 0;
        } else {
            nextIndex = direction > 0 ? 0 : count - 1;
        }

        JMenuItem nextItem = (JMenuItem) suggestionPopup.getComponent(nextIndex);
        MenuElement[] newPath = new MenuElement[] { suggestionPopup, nextItem };
        MenuSelectionManager.defaultManager().setSelectedPath(newPath);
    }

    private void triggerSelectedPopupItem() {
        MenuElement[] path = MenuSelectionManager.defaultManager().getSelectedPath();
        if (path.length > 0 && path[path.length - 1] instanceof JMenuItem) {
            JMenuItem selected = (JMenuItem) path[path.length - 1];
            selected.doClick();
        }
    }

    private static class OverviewEntryWithPath {
        final OverviewEntry entry;
        final String category;

        OverviewEntryWithPath(OverviewEntry entry, String category) {
            this.entry = entry;
            this.category = category;
        }
    }

    /** Tarjeta de superficie con borde suave para separar cada área del tablero. */
    private static class SurfacePanel extends JPanel {
        SurfacePanel() {
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(7, 55, 43, 10));
            g2.fillRoundRect(2, 4, Math.max(0, getWidth() - 4), Math.max(0, getHeight() - 5), 20, 20);
            g2.setColor(COLOR_PANEL);
            g2.fillRoundRect(1, 1, Math.max(0, getWidth() - 3), Math.max(0, getHeight() - 4), 20, 20);
            g2.setColor(COLOR_PANEL_SOFT);
            g2.drawRoundRect(1, 1, Math.max(0, getWidth() - 3), Math.max(0, getHeight() - 4), 20, 20);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private static class ArrowOnlyScrollBarUI extends javax.swing.plaf.basic.BasicScrollBarUI {
        @Override
        protected void paintTrack(java.awt.Graphics g, javax.swing.JComponent c, java.awt.Rectangle trackBounds) {
            // Do not paint the track
        }

        @Override
        protected void paintThumb(java.awt.Graphics g, javax.swing.JComponent c, java.awt.Rectangle thumbBounds) {
            // Do not paint the thumb
        }

        @Override
        protected JButton createDecreaseButton(int orientation) {
            return new ScrollArrowButton(orientation);
        }

        @Override
        protected JButton createIncreaseButton(int orientation) {
            return new ScrollArrowButton(orientation);
        }

        @Override
        public Dimension getPreferredSize(javax.swing.JComponent c) {
            return new Dimension(24, 24);
        }
    }

    private static class ScrollArrowButton extends JButton {
        private final int orientation;
        private boolean isHovered = false;

        public ScrollArrowButton(int orientation) {
            this.orientation = orientation;
            setOpaque(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusable(false);
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    isHovered = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    isHovered = false;
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Clear background
            g2.setColor(COLOR_BACKGROUND);
            g2.fillRect(0, 0, getWidth(), getHeight());

            // Draw arrow
            g2.setColor(isHovered ? COLOR_ACCENT : COLOR_TEXT_MUTED);
            int w = getWidth();
            int h = getHeight();
            int cx = w / 2;
            int cy = h / 2;

            if (orientation == SwingConstants.NORTH) {
                int size = 6;
                int[] xPoints = { cx - size, cx, cx + size };
                int[] yPoints = { cy + size / 2, cy - size / 2, cy + size / 2 };
                g2.fillPolygon(xPoints, yPoints, 3);
            } else if (orientation == SwingConstants.SOUTH) {
                int size = 6;
                int[] xPoints = { cx - size, cx, cx + size };
                int[] yPoints = { cy - size / 2, cy + size / 2, cy - size / 2 };
                g2.fillPolygon(xPoints, yPoints, 3);
            }
            g2.dispose();
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(24, 24);
        }
    }
}

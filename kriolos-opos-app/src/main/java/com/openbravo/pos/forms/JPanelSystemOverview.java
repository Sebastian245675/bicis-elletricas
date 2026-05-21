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

public class JPanelSystemOverview extends JPanel implements JPanelView {

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
    private static final String TASK_DOCUMENTS = "com.openbravo.pos.panels.JPanelDocuments";
    private static final String TASK_BRANCHES = "com.openbravo.pos.branches.JPanelBranchesManagement";
    private static final String TASK_PAYMENTS = "com.openbravo.pos.panels.JPanelPayments";
    private static final String TASK_BREAKS = "com.openbravo.pos.epm.BreaksPanel";
    private static final String TASK_LOCATIONS = "com.openbravo.pos.inventory.LocationsPanel";
    private static final String TASK_HR = "com.openbravo.pos.admin.JPanelHR";

    private static final Color COLOR_BACKGROUND = new Color(39, 39, 39);
    private static final Color COLOR_PANEL = new Color(49, 49, 49);
    private static final Color COLOR_PANEL_SOFT = new Color(58, 58, 58);
    private static final Color COLOR_TEXT = new Color(245, 245, 245);
    private static final Color COLOR_TEXT_SOFT = new Color(210, 210, 210);
    private static final Color COLOR_TEXT_MUTED = new Color(155, 155, 155);
    private static final Color COLOR_ACCENT = new Color(238, 150, 28);
    private static final Color COLOR_ACCENT_SOFT = new Color(255, 193, 94);
    private static final Color COLOR_SEARCH = new Color(68, 68, 68);

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

    private final List<OverviewRootGroup> rootGroups = new ArrayList<>();
    private boolean loaded;

    public JPanelSystemOverview(AppUserView appUserView, DataLogicSystem dlSystem) {
        this.appUserView = appUserView;
        this.dlSystem = dlSystem;

        setLayout(new BorderLayout());
        setOpaque(true);
        setBackground(COLOR_BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder());

        JPanel shell = new JPanel(new BorderLayout(0, 18));
        shell.setOpaque(true);
        shell.setBackground(COLOR_BACKGROUND);
        shell.setBorder(BorderFactory.createEmptyBorder(22, 22, 18, 22));
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
        searchField.setFont(new Font("Segoe UI", Font.BOLD, 18));
        searchField.setPreferredSize(new Dimension(720, 46));
        searchField.setMinimumSize(new Dimension(320, 46));
        searchField.setMaximumSize(new Dimension(720, 46));
        searchField.setBackground(COLOR_SEARCH);
        searchField.setForeground(COLOR_TEXT);
        searchField.setCaretColor(COLOR_ACCENT);
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_ACCENT, 2),
                BorderFactory.createEmptyBorder(10, 14, 10, 14)));
        searchField.setToolTipText("Buscar modulo, reporte o proceso");
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
        iconsPanel.add(createIconButton("🎛", "Configuración / Parámetros", () -> appUserView.showTask(TASK_CONFIGURATION)));
        iconsPanel.add(createIconButton("?", "Ayuda y Atajos de Teclado", () -> showHelpDialog()));
        iconsPanel.add(createIconButton("▦", "Menú de Mantenimiento", () -> appUserView.showTask(TASK_MENU_MAINTENANCE)));
        iconsPanel.add(createIconButton("⟳", "Mis Documentos / Historial", () -> appUserView.showTask(TASK_DOCUMENTS)));
        iconsPanel.add(createIconButton("♥", "Ir a Ventas (F1)", () -> appUserView.showTask(TASK_TICKET_SALES)));
        iconsPanel.add(createIconButton("▶", "Gráficos de Reportes", () -> appUserView.showTask(TASK_REPORTS_DASHBOARD)));
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
        JLabel brandLabel = new JLabel("<html><span style='color:#EE961C'>WEBSY</span> <span style='color:#F5F5F5'>GROUP</span></html>");
        brandLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        brandPanel.add(brandLabel);

        brandPanel.add(Box.createVerticalStrut(2));

        JLabel subtitleLabel = new JLabel("Indice general del sistema");
        subtitleLabel.setForeground(new Color(200, 200, 200));
        subtitleLabel.setFont(new Font("Segoe UI Semilight", Font.PLAIN, 14));
        brandPanel.add(subtitleLabel);

        brandPanel.add(Box.createVerticalStrut(8));

        // Separador decorativo "bonito" con degradado
        JPanel separator = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, COLOR_ACCENT, getWidth(), 0, new Color(COLOR_ACCENT.getRed(), COLOR_ACCENT.getGreen(), COLOR_ACCENT.getBlue(), 0));
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

        JLabel helpLabel = new JLabel("Accesos agrupados por vista principal");
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
        if (bundledMenu == null || bundledMenu.isBlank()) {
            return false;
        }
        if (databaseMenu == null || databaseMenu.isBlank()) {
            return true;
        }
        return (!databaseMenu.contains(TASK_HR) && bundledMenu.contains(TASK_HR))
                || databaseMenu.contains("*") && (databaseMenu.contains("Recursos Humanos") || databaseMenu.contains("humanos"));
    }

    private void injectAdditionalEntries(List<OverviewRootGroup> groups) {
        injectTopNavigationEntries(groups);
        addIfMissing(groups, ROOT_KEY_MAIN, "Paneles Generales", "Paneles principales",
                "Entradas y salidas", TASK_PAYMENTS, EntryKind.SHOW_TASK);
        addIfMissing(groups, ROOT_KEY_BACKOFFICE, "Mantenimiento", "Administracion interna",
                "Administrar sucursales", TASK_BRANCHES, EntryKind.SHOW_TASK);
        addIfMissing(groups, ROOT_KEY_BACKOFFICE, "Gestion de Presencia", "Gestion interna",
                "Descansos", TASK_BREAKS, EntryKind.SHOW_TASK);
        addIfMissing(groups, ROOT_KEY_BACKOFFICE, "Gestion de Stock", "Gestion interna",
                "Ubicaciones", TASK_LOCATIONS, EntryKind.SHOW_TASK);
        addIfMissing(groups, ROOT_KEY_SYSTEM, "Sistema", "Soporte y salida",
                "Mis documentos", TASK_DOCUMENTS, EntryKind.SHOW_TASK);
        addIfMissing(groups, ROOT_KEY_SYSTEM, "Sistema", "Soporte y salida",
                "Cerrar sesion", "system.exit", EntryKind.EXIT_TO_LOGIN);
    }

    private void injectTopNavigationEntries(List<OverviewRootGroup> groups) {
        OverviewRootGroup rootGroup = findOrCreateRootGroup(groups, ROOT_KEY_MAIN);
        OverviewCategory category = findOrCreateCategory(rootGroup, "Paneles Generales");
        OverviewSection quickAccessSection = new OverviewSection("Accesos principales");

        addQuickAccessEntry(quickAccessSection, "Inicio", TASK_SYSTEM_OVERVIEW);
        addQuickAccessEntry(quickAccessSection, "F1 Ventas", TASK_TICKET_SALES);
        addQuickAccessEntry(quickAccessSection, "Pago de Clientes", TASK_CUSTOMER_PAYMENT);
        addQuickAccessEntry(quickAccessSection, "F2 Cerrar Caja", TASK_CLOSE_MONEY);
        addQuickAccessEntry(quickAccessSection, "Clientes", TASK_MENU_CUSTOMERS);
        addQuickAccessEntry(quickAccessSection, "Proveedores", TASK_MENU_SUPPLIERS);
        addQuickAccessEntry(quickAccessSection, "F3 Stock", TASK_MENU_STOCK);
        addQuickAccessEntry(quickAccessSection, "Gestion Ventas", TASK_MENU_SALES);
        addQuickAccessEntry(quickAccessSection, "Mantenimiento", TASK_MENU_MAINTENANCE);
        addQuickAccessEntry(quickAccessSection, "RRHH", TASK_HR);
        addQuickAccessEntry(quickAccessSection, "Drive", TASK_DOCUMENTS);
        addQuickAccessEntry(quickAccessSection, "Configuracion", TASK_CONFIGURATION);
        addQuickAccessEntry(quickAccessSection, "Impresoras", TASK_PRINTER);
        addQuickAccessEntry(quickAccessSection, "F4 Reportes", TASK_REPORTS_DASHBOARD);

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
        JPanel panel = new JPanel();
        panel.setOpaque(true);
        panel.setBackground(COLOR_BACKGROUND);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setPreferredSize(new Dimension(260, 560));
        panel.setMinimumSize(new Dimension(220, 560));

        JLabel titleLabel = new JLabel(column.title);
        titleLabel.setForeground(COLOR_TEXT);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setAlignmentX(LEFT_ALIGNMENT);
        panel.add(titleLabel);

        panel.add(Box.createVerticalStrut(6));

        JPanel underline = new JPanel();
        underline.setMaximumSize(new Dimension(Integer.MAX_VALUE, 2));
        underline.setPreferredSize(new Dimension(240, 2));
        underline.setMinimumSize(new Dimension(120, 2));
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
        return panel;
    }

    private JPanel createGroupPanel(VisibleCategory group) {
        JPanel panel = new JPanel();
        panel.setOpaque(true);
        panel.setBackground(COLOR_BACKGROUND);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setAlignmentX(LEFT_ALIGNMENT);

        JPanel titleRow = new JPanel(new BorderLayout(8, 0));
        titleRow.setOpaque(false);
        titleRow.setAlignmentX(LEFT_ALIGNMENT);

        JLabel titleLabel = new JLabel(group.title);
        titleLabel.setForeground(COLOR_ACCENT_SOFT);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
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
                sectionLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
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
        button.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        button.setForeground(COLOR_TEXT);
        button.setOpaque(true);
        button.setBackground(COLOR_PANEL);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_PANEL_SOFT, 1),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    private JButton createEntryButton(OverviewEntry entry) {
        JButton button = new JButton(entry.label);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        button.setForeground(COLOR_TEXT_SOFT);
        button.setOpaque(true);
        button.setBackground(COLOR_BACKGROUND);
        button.setBorder(BorderFactory.createEmptyBorder(3, 0, 3, 0));
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.addActionListener(event -> executeEntry(entry));
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setForeground(COLOR_ACCENT);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setForeground(COLOR_TEXT_SOFT);
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
                directCategory = new OverviewCategory(resolveDirectCategoryTitle(rootGroup.rawKey, rootGroup.displayTitle), null);
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
            ensureSection().entries.add(new OverviewEntry(stripHtml(resolveText(key)), classname, EntryKind.EXECUTE_TASK));
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
        btn.setForeground(new Color(200, 200, 200));
        btn.setBackground(COLOR_BACKGROUND);
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setToolTipText(tooltip);
        
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setForeground(COLOR_ACCENT);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                btn.setForeground(new Color(200, 200, 200));
            }
        });
        
        if (action != null) {
            btn.addActionListener(e -> action.run());
        }
        
        return btn;
    }

    private void showHelpDialog() {
        javax.swing.JOptionPane.showMessageDialog(this, 
            "<html><body style='font-family: Segoe UI; font-size: 13px; color: #333333;'>" +
            "<h2 style='color: #EE961C; margin-top:0;'>Información y Atajos de Teclado</h2>" +
            "<table border='0' cellpadding='4'>" +
            "<tr><td><b>F1</b></td><td>Ventas / Facturación</td></tr>" +
            "<tr><td><b>F2</b></td><td>Cerrar Caja / Turno</td></tr>" +
            "<tr><td><b>F3</b></td><td>Control de Stock</td></tr>" +
            "<tr><td><b>F4</b></td><td>Reportes y Gráficos</td></tr>" +
            "<tr><td><b>Esc</b></td><td>Volver al Inicio / Cancelar</td></tr>" +
            "</table><br>" +
            "<hr size='1' color='#cccccc'>" +
            "<p style='color: #666666;'>KriolOS POS - Soporte Websy Group</p>" +
            "</body></html>", 
            "Ayuda del Sistema", 
            javax.swing.JOptionPane.INFORMATION_MESSAGE
        );
    }
}

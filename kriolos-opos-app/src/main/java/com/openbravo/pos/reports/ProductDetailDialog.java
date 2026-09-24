//    KriolOS POS - Product Detail Dialog
//    Shows comprehensive product details when clicking on a product in the Top 10 ranking.

package com.openbravo.pos.reports;

import com.openbravo.data.loader.Session;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 * Diálogo modal que muestra un resumen completo de un producto:
 * - Información general (nombre, código, categoría, precios)
 * - Métricas de ventas (unidades vendidas, ingreso bruto, ganancia, impuestos)
 * - Stock disponible
 * - Top vendedores del producto
 * - Últimas transacciones
 *
 * Diseño moderno con tema claro y tarjetas premium.
 */
public class ProductDetailDialog extends JDialog {

    private static final Logger LOGGER = Logger.getLogger(ProductDetailDialog.class.getName());

    // === Paleta de colores (tema claro premium) ===
    private static final Color BG_MAIN        = new Color(241, 245, 249);
    private static final Color BG_CARD        = new Color(255, 255, 255);
    private static final Color BORDER_SUBTLE  = new Color(226, 232, 240);
    private static final Color TEXT_PRIMARY    = new Color(15, 23, 42);
    private static final Color TEXT_SECONDARY  = new Color(71, 85, 105);
    private static final Color TEXT_MUTED      = new Color(148, 163, 184);

    private static final Color ACCENT_AMBER   = new Color(235, 172, 60);
    private static final Color ACCENT_GREEN   = new Color(16, 185, 129);
    private static final Color ACCENT_BLUE    = new Color(59, 130, 246);
    private static final Color ACCENT_PURPLE  = new Color(139, 92, 246);
    private static final Color ACCENT_RED     = new Color(239, 68, 68);
    private static final Color ACCENT_TEAL    = new Color(20, 184, 166);

    // Formato de moneda
    private static final NumberFormat CURRENCY_FMT = NumberFormat.getCurrencyInstance(new Locale("es", "MX"));
    private static final NumberFormat INTEGER_FMT = NumberFormat.getIntegerInstance();

    private final Session session;
    private final String productName;

    public ProductDetailDialog(Frame parent, Session session, String productName) {
        super(parent, "Detalle del Producto", true);
        this.session = session;
        this.productName = productName;

        setSize(820, 680);
        setLocationRelativeTo(parent);
        setResizable(true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        
        // Keyboard shortcut to close
        getRootPane().registerKeyboardAction(e -> dispose(),
            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

        buildUI();
    }

    private void buildUI() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(BG_MAIN);

        // === HEADER ===
        JPanel header = buildHeader();
        mainPanel.add(header, BorderLayout.NORTH);

        // === BODY (scrollable) ===
        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setOpaque(false);
        body.setBorder(new EmptyBorder(0, 24, 24, 24));

        // Load data from DB
        ProductData data = loadProductData();

        if (data == null) {
            JLabel errorLabel = new JLabel("No se encontró información del producto: " + productName);
            errorLabel.setFont(new Font("Segoe UI", Font.ITALIC, 14));
            errorLabel.setForeground(ACCENT_RED);
            errorLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            errorLabel.setBorder(new EmptyBorder(40, 0, 40, 0));
            body.add(errorLabel);
        } else {
            // Row 1: Info general + Precios
            body.add(buildInfoAndPricesRow(data));
            body.add(Box.createVerticalStrut(16));

            // Row 2: 4 KPI cards
            body.add(buildKpiRow(data));
            body.add(Box.createVerticalStrut(16));

            // Row 3: Stock + Top Vendedores
            body.add(buildStockAndSellersRow(data));
            body.add(Box.createVerticalStrut(16));

            // Row 4: Últimas transacciones
            body.add(buildRecentTransactionsCard(data));
        }

        JScrollPane scrollPane = new JScrollPane(body);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.getViewport().setBackground(BG_MAIN);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // === FOOTER ===
        JPanel footer = buildFooter();
        mainPanel.add(footer, BorderLayout.SOUTH);

        setContentPane(mainPanel);
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, ACCENT_AMBER, getWidth(), 0, new Color(245, 130, 50));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        header.setBorder(new EmptyBorder(20, 24, 20, 24));
        header.setPreferredSize(new Dimension(0, 80));

        JLabel titleLabel = new JLabel("📦  " + productName);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);
        header.add(titleLabel, BorderLayout.WEST);

        JLabel badgeLabel = new JLabel("RESUMEN COMPLETO");
        badgeLabel.setFont(new Font("Segoe UI", Font.BOLD, 10));
        badgeLabel.setForeground(new Color(255, 255, 255, 180));
        badgeLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        header.add(badgeLabel, BorderLayout.EAST);

        return header;
    }

    private JPanel buildFooter() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 12));
        footer.setBackground(BG_CARD);
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_SUBTLE));

        JButton closeBtn = new JButton("Cerrar");
        closeBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        closeBtn.setPreferredSize(new Dimension(120, 38));
        closeBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        closeBtn.setBackground(ACCENT_AMBER);
        closeBtn.setForeground(Color.WHITE);
        closeBtn.setFocusPainted(false);
        closeBtn.setBorderPainted(false);
        closeBtn.addActionListener(e -> dispose());
        footer.add(closeBtn);

        return footer;
    }

    // ==================== CARD BUILDERS ====================

    private JPanel buildInfoAndPricesRow(ProductData data) {
        JPanel row = new JPanel(new GridLayout(1, 2, 16, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 160));

        // Info general
        JPanel infoCard = createCard("📋 INFORMACIÓN GENERAL", ACCENT_BLUE);
        JPanel infoContent = new JPanel(new GridLayout(4, 1, 0, 4));
        infoContent.setOpaque(false);
        infoContent.add(createDetailRow("Referencia:", data.reference));
        infoContent.add(createDetailRow("Código:", data.code));
        infoContent.add(createDetailRow("Categoría:", data.category));
        infoContent.add(createDetailRow("Proveedor:", data.supplier != null ? data.supplier : "—"));
        infoCard.add(infoContent, BorderLayout.CENTER);
        row.add(infoCard);

        // Precios
        JPanel pricesCard = createCard("💲 PRECIOS Y MÁRGENES", ACCENT_GREEN);
        JPanel pricesContent = new JPanel(new GridLayout(4, 1, 0, 4));
        pricesContent.setOpaque(false);
        pricesContent.add(createDetailRow("Precio Compra:", CURRENCY_FMT.format(data.priceBuy)));
        pricesContent.add(createDetailRow("Precio Venta:", CURRENCY_FMT.format(data.priceSell)));
        double margin = data.priceSell - data.priceBuy;
        double marginPct = data.priceBuy > 0 ? (margin / data.priceBuy) * 100 : 0;
        pricesContent.add(createDetailRow("Margen Unitario:", CURRENCY_FMT.format(margin)));
        pricesContent.add(createDetailRow("% Margen:", String.format("%.1f%%", marginPct)));
        pricesCard.add(pricesContent, BorderLayout.CENTER);
        row.add(pricesCard);

        return row;
    }

    private JPanel buildKpiRow(ProductData data) {
        JPanel row = new JPanel(new GridLayout(1, 4, 12, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

        row.add(createKpiCard("UNIDADES VENDIDAS", String.format("%.0f", data.totalUnitsSold), ACCENT_AMBER, "📈"));
        row.add(createKpiCard("INGRESO BRUTO", CURRENCY_FMT.format(data.totalRevenue), ACCENT_GREEN, "💰"));
        row.add(createKpiCard("GANANCIA NETA", CURRENCY_FMT.format(data.totalProfit), ACCENT_BLUE, "📊"));
        row.add(createKpiCard("IMPUESTOS", CURRENCY_FMT.format(data.totalTaxes), ACCENT_PURPLE, "🏛️"));

        return row;
    }

    private JPanel buildStockAndSellersRow(ProductData data) {
        JPanel row = new JPanel(new GridLayout(1, 2, 16, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));

        // Stock
        JPanel stockCard = createCard("📦 INVENTARIO / STOCK", ACCENT_TEAL);
        JPanel stockContent = new JPanel(new GridLayout(3, 1, 0, 6));
        stockContent.setOpaque(false);
        stockContent.add(createDetailRow("Stock Disponible:", String.format("%.0f unidades", data.stockUnits)));
        stockContent.add(createDetailRow("Stock Actual (Almacén):", String.format("%.0f unidades", data.stockCurrent)));
        
        Color stockColor = data.stockCurrent > 10 ? ACCENT_GREEN : (data.stockCurrent > 0 ? ACCENT_AMBER : ACCENT_RED);
        String stockStatus = data.stockCurrent > 10 ? "✅ Disponible" : (data.stockCurrent > 0 ? "⚠️ Stock Bajo" : "❌ Agotado");
        JLabel statusLabel = new JLabel(stockStatus);
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        statusLabel.setForeground(stockColor);
        JPanel statusRow = new JPanel(new BorderLayout());
        statusRow.setOpaque(false);
        JLabel statusTitleLabel = new JLabel("Estado:");
        statusTitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        statusTitleLabel.setForeground(TEXT_MUTED);
        statusTitleLabel.setPreferredSize(new Dimension(120, 20));
        statusRow.add(statusTitleLabel, BorderLayout.WEST);
        statusRow.add(statusLabel, BorderLayout.CENTER);
        stockContent.add(statusRow);
        
        stockCard.add(stockContent, BorderLayout.CENTER);
        row.add(stockCard);

        // Top vendedores
        JPanel sellersCard = createCard("👤 TOP VENDEDORES", ACCENT_PURPLE);
        JPanel sellersContent = new JPanel();
        sellersContent.setLayout(new BoxLayout(sellersContent, BoxLayout.Y_AXIS));
        sellersContent.setOpaque(false);

        if (data.topSellers.isEmpty()) {
            JLabel noData = new JLabel("Sin datos de vendedores");
            noData.setFont(new Font("Segoe UI", Font.ITALIC, 12));
            noData.setForeground(TEXT_MUTED);
            sellersContent.add(noData);
        } else {
            double maxSellerUnits = data.topSellers.get(0).units;
            for (int i = 0; i < data.topSellers.size(); i++) {
                SellerData seller = data.topSellers.get(i);
                sellersContent.add(createSellerRow(i + 1, seller.name, seller.units, seller.revenue, maxSellerUnits));
                if (i < data.topSellers.size() - 1) {
                    sellersContent.add(Box.createVerticalStrut(6));
                }
            }
        }
        sellersCard.add(sellersContent, BorderLayout.CENTER);
        row.add(sellersCard);

        return row;
    }

    private JPanel buildRecentTransactionsCard(ProductData data) {
        JPanel card = createCard("🕐 ÚLTIMAS TRANSACCIONES", ACCENT_AMBER);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 220));

        if (data.recentTransactions.isEmpty()) {
            JLabel noData = new JLabel("Sin transacciones recientes");
            noData.setFont(new Font("Segoe UI", Font.ITALIC, 12));
            noData.setForeground(TEXT_MUTED);
            card.add(noData, BorderLayout.CENTER);
        } else {
            // Table
            String[] columns = {"Fecha", "Ticket #", "Vendedor", "Unidades", "Precio Unit.", "Total"};
            Object[][] tableData = new Object[data.recentTransactions.size()][6];
            for (int i = 0; i < data.recentTransactions.size(); i++) {
                TransactionData tx = data.recentTransactions.get(i);
                tableData[i][0] = tx.date;
                tableData[i][1] = tx.ticketId;
                tableData[i][2] = tx.sellerName;
                tableData[i][3] = String.format("%.0f", tx.units);
                tableData[i][4] = CURRENCY_FMT.format(tx.price);
                tableData[i][5] = CURRENCY_FMT.format(tx.total);
            }

            JTable table = new JTable(tableData, columns) {
                @Override
                public boolean isCellEditable(int row, int column) { return false; }
            };
            table.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            table.setRowHeight(28);
            table.setShowGrid(false);
            table.setIntercellSpacing(new Dimension(0, 0));
            table.setBackground(BG_CARD);
            table.setForeground(TEXT_PRIMARY);
            table.setSelectionBackground(new Color(235, 172, 60, 30));
            table.setSelectionForeground(TEXT_PRIMARY);
            
            // Header styling
            table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 10));
            table.getTableHeader().setBackground(BG_MAIN);
            table.getTableHeader().setForeground(TEXT_SECONDARY);
            table.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_SUBTLE));

            JScrollPane tableScroll = new JScrollPane(table);
            tableScroll.setBorder(BorderFactory.createEmptyBorder());
            tableScroll.getViewport().setBackground(BG_CARD);
            card.add(tableScroll, BorderLayout.CENTER);
        }

        return card;
    }

    // ==================== HELPER UI COMPONENTS ====================

    private JPanel createCard(String title, Color accentColor) {
        JPanel card = new JPanel(new BorderLayout(0, 10)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                // Borde lateral coloreado
                g2.setColor(accentColor);
                g2.fillRoundRect(0, 0, 4, getHeight(), 4, 4);
                // Borde exterior
                g2.setColor(BORDER_SUBTLE);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(14, 18, 14, 14));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        titleLabel.setForeground(accentColor);
        card.add(titleLabel, BorderLayout.NORTH);

        return card;
    }

    private JPanel createKpiCard(String title, String value, Color accentColor, String emoji) {
        JPanel card = new JPanel(new BorderLayout(0, 4)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(accentColor);
                g2.fillRoundRect(0, 0, 4, getHeight(), 4, 4);
                g2.setColor(BORDER_SUBTLE);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(12, 18, 12, 14));

        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);
        JLabel emojiLbl = new JLabel(emoji + "  ");
        emojiLbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        topRow.add(emojiLbl, BorderLayout.WEST);
        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 9));
        titleLbl.setForeground(TEXT_MUTED);
        topRow.add(titleLbl, BorderLayout.CENTER);
        card.add(topRow, BorderLayout.NORTH);

        JLabel valueLbl = new JLabel(value);
        valueLbl.setFont(new Font("Segoe UI", Font.BOLD, 20));
        valueLbl.setForeground(TEXT_PRIMARY);
        card.add(valueLbl, BorderLayout.CENTER);

        return card;
    }

    private JPanel createDetailRow(String label, String value) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);

        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lbl.setForeground(TEXT_MUTED);
        lbl.setPreferredSize(new Dimension(120, 18));
        row.add(lbl, BorderLayout.WEST);

        JLabel val = new JLabel(value);
        val.setFont(new Font("Segoe UI", Font.BOLD, 12));
        val.setForeground(TEXT_PRIMARY);
        row.add(val, BorderLayout.CENTER);

        return row;
    }

    private JPanel createSellerRow(int position, String name, double units, double revenue, double maxUnits) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));

        // Position badge
        JLabel posLabel = new JLabel(String.valueOf(position), SwingConstants.CENTER);
        posLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        posLabel.setForeground(position == 1 ? ACCENT_AMBER : TEXT_SECONDARY);
        posLabel.setPreferredSize(new Dimension(24, 20));
        row.add(posLabel, BorderLayout.WEST);

        // Name + mini bar
        JPanel centerPanel = new JPanel(new BorderLayout(6, 0));
        centerPanel.setOpaque(false);
        JLabel nameLabel = new JLabel(name);
        nameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        nameLabel.setForeground(TEXT_PRIMARY);
        nameLabel.setPreferredSize(new Dimension(140, 20));
        centerPanel.add(nameLabel, BorderLayout.WEST);

        // Mini progress bar
        double pct = maxUnits > 0 ? units / maxUnits : 0;
        final double finalPct = pct;
        JPanel miniBar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(226, 232, 240));
                g2.fillRoundRect(0, 2, getWidth(), getHeight() - 4, 6, 6);
                int barW = (int) (getWidth() * finalPct);
                if (barW > 0) {
                    g2.setPaint(new GradientPaint(0, 0, ACCENT_AMBER, barW, 0, new Color(245, 130, 50)));
                    g2.fillRoundRect(0, 2, barW, getHeight() - 4, 6, 6);
                }
                g2.dispose();
            }
        };
        miniBar.setOpaque(false);
        miniBar.setPreferredSize(new Dimension(80, 14));
        centerPanel.add(miniBar, BorderLayout.CENTER);
        row.add(centerPanel, BorderLayout.CENTER);

        // Value
        JLabel valueLabel = new JLabel(String.format("%.0f uds", units));
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        valueLabel.setForeground(ACCENT_AMBER);
        valueLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        valueLabel.setPreferredSize(new Dimension(60, 20));
        row.add(valueLabel, BorderLayout.EAST);

        return row;
    }

    // ==================== DATA LOADING ====================

    private ProductData loadProductData() {
        ProductData data = new ProductData();
        data.name = productName;

        try (Connection conn = session.getConnection()) {
            // 1. Información básica del producto
            loadBasicInfo(conn, data);
            if (data.productId == null) return null;

            // 2. Métricas de ventas
            loadSalesMetrics(conn, data);

            // 3. Stock actual
            loadStock(conn, data);

            // 4. Top vendedores
            loadTopSellers(conn, data);

            // 5. Últimas transacciones
            loadRecentTransactions(conn, data);

        } catch (SQLException ex) {
            LOGGER.log(Level.WARNING, "Error loading product data for: " + productName, ex);
            return null;
        }
        return data;
    }

    private void loadBasicInfo(Connection conn, ProductData data) throws SQLException {
        String sql = "SELECT p.ID, p.REFERENCE, p.CODE, p.PRICEBUY, p.PRICESELL, p.STOCKUNITS, " +
                     "p.SUPPLIER, c.NAME AS CATNAME " +
                     "FROM products p " +
                     "LEFT JOIN categories c ON p.CATEGORY = c.ID " +
                     "WHERE p.NAME = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, productName);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                data.productId = rs.getString("ID");
                data.reference = rs.getString("REFERENCE");
                data.code = rs.getString("CODE");
                data.priceBuy = rs.getDouble("PRICEBUY");
                data.priceSell = rs.getDouble("PRICESELL");
                data.stockUnits = rs.getDouble("STOCKUNITS");
                data.supplier = rs.getString("SUPPLIER");
                data.category = rs.getString("CATNAME");
                if (data.category == null) data.category = "Sin categoría";
                if (data.supplier == null) data.supplier = "—";
            }
        }
    }

    private void loadSalesMetrics(Connection conn, ProductData data) throws SQLException {
        String sql = "SELECT " +
                     "COALESCE(SUM(tl.UNITS), 0) AS TOTAL_UNITS, " +
                     "COALESCE(SUM(tl.PRICE * tl.UNITS), 0) AS TOTAL_REVENUE, " +
                     "COALESCE(SUM((tl.PRICE * tl.UNITS) * t.RATE), 0) AS TOTAL_TAXES, " +
                     "COALESCE(SUM((tl.PRICE - ?) * tl.UNITS), 0) AS TOTAL_PROFIT " +
                     "FROM ticketlines tl " +
                     "JOIN tickets tk ON tk.ID = tl.TICKET " +
                     "JOIN receipts r ON r.ID = tk.ID " +
                     "JOIN taxes t ON t.ID = tl.TAXID " +
                     "WHERE tl.PRODUCT = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, data.priceBuy);
            ps.setString(2, data.productId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                data.totalUnitsSold = rs.getDouble("TOTAL_UNITS");
                data.totalRevenue = rs.getDouble("TOTAL_REVENUE");
                data.totalTaxes = rs.getDouble("TOTAL_TAXES");
                data.totalProfit = rs.getDouble("TOTAL_PROFIT");
            }
        }
    }

    private void loadStock(Connection conn, ProductData data) throws SQLException {
        String sql = "SELECT COALESCE(SUM(sc.UNITS), 0) AS STOCK_CURRENT " +
                     "FROM stockcurrent sc WHERE sc.PRODUCT = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, data.productId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                data.stockCurrent = rs.getDouble("STOCK_CURRENT");
            }
        }
    }

    private void loadTopSellers(Connection conn, ProductData data) throws SQLException {
        String sql = "SELECT p.NAME AS SELLER_NAME, " +
                     "ROUND(SUM(tl.UNITS)) AS TOTAL_UNITS, " +
                     "ROUND(SUM(tl.PRICE * tl.UNITS)) AS TOTAL_REVENUE " +
                     "FROM ticketlines tl " +
                     "JOIN tickets tk ON tk.ID = tl.TICKET " +
                     "JOIN people p ON p.ID = tk.PERSON " +
                     "WHERE tl.PRODUCT = ? " +
                     "GROUP BY p.NAME " +
                     "ORDER BY TOTAL_UNITS DESC " +
                     "LIMIT 5";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, data.productId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                SellerData seller = new SellerData();
                seller.name = rs.getString("SELLER_NAME");
                seller.units = rs.getDouble("TOTAL_UNITS");
                seller.revenue = rs.getDouble("TOTAL_REVENUE");
                data.topSellers.add(seller);
            }
        }
    }

    private void loadRecentTransactions(Connection conn, ProductData data) throws SQLException {
        String sql = "SELECT r.DATENEW, tk.TICKETID, p.NAME AS SELLER_NAME, " +
                     "tl.UNITS, tl.PRICE, ROUND(tl.PRICE * tl.UNITS, 2) AS TOTAL " +
                     "FROM ticketlines tl " +
                     "JOIN tickets tk ON tk.ID = tl.TICKET " +
                     "JOIN receipts r ON r.ID = tk.ID " +
                     "JOIN people p ON p.ID = tk.PERSON " +
                     "WHERE tl.PRODUCT = ? " +
                     "ORDER BY r.DATENEW DESC " +
                     "LIMIT 10";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, data.productId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                TransactionData tx = new TransactionData();
                Timestamp ts = rs.getTimestamp("DATENEW");
                tx.date = ts != null ? new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm").format(ts) : "—";
                tx.ticketId = String.valueOf(rs.getInt("TICKETID"));
                tx.sellerName = rs.getString("SELLER_NAME");
                tx.units = rs.getDouble("UNITS");
                tx.price = rs.getDouble("PRICE");
                tx.total = rs.getDouble("TOTAL");
                data.recentTransactions.add(tx);
            }
        }
    }

    // ==================== DATA CLASSES ====================

    private static class ProductData {
        String productId;
        String name;
        String reference = "";
        String code = "";
        String category = "";
        String supplier = "";
        double priceBuy;
        double priceSell;
        double stockUnits;
        double stockCurrent;
        double totalUnitsSold;
        double totalRevenue;
        double totalTaxes;
        double totalProfit;
        List<SellerData> topSellers = new ArrayList<>();
        List<TransactionData> recentTransactions = new ArrayList<>();
    }

    private static class SellerData {
        String name;
        double units;
        double revenue;
    }

    private static class TransactionData {
        String date;
        String ticketId;
        String sellerName;
        double units;
        double price;
        double total;
    }
}

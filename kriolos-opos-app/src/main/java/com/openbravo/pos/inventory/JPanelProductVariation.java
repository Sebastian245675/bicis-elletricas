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
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

public class JPanelProductVariation extends JPanel implements JPanelView, BeanFactoryApp {

    private static final Color BG_PAGE = new Color(9, 10, 15);
    private static final Color BG_CARD = new Color(22, 27, 34);
    private static final Color BORDER_CARD = new Color(48, 54, 61);
    private static final Color TEXT_MAIN = new Color(255, 255, 255);
    private static final Color TEXT_SUB = new Color(139, 148, 158);
    private static final Color ACCENT_BLUE = new Color(59, 130, 246);

    private AppView m_App;
    private DataLogicSales dlSales;
    private JProductVariationPanel chart;
    private JTextField txtProduct;
    private JButton btnSearch;
    private JLabel lblStatus;
    
    private JPanel cards;
    private CardLayout cardLayout;
    
    private final JLabel lblBestMonthVal = new JLabel("Sin datos");
    private final JLabel lblWorstMonthVal = new JLabel("Sin datos");
    private final JLabel lblTotalChangesVal = new JLabel("0");
    private final JLabel lblTopProductVal = new JLabel("Ninguno");

    @Override
    public void init(AppView app) throws BeanFactoryException {
        m_App = app;
        dlSales = (DataLogicSales) m_App.getBean("com.openbravo.pos.forms.DataLogicSales");
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setBackground(BG_PAGE);
        setOpaque(true);

        // Fix parent borders that might be white
        addHierarchyListener(e -> {
            if ((e.getChangeFlags() & java.awt.event.HierarchyEvent.SHOWING_CHANGED) != 0) {
                javax.swing.SwingUtilities.invokeLater(() -> {
                    if (isShowing()) {
                        java.awt.Container parent = getParent();
                        while (parent != null) {
                            if (parent instanceof javax.swing.JComponent) {
                                parent.setBackground(BG_PAGE);
                                // Always remove border from the direct wrapper
                                if (parent == getParent() || parent.getClass().getName().contains("Container")) {
                                    ((javax.swing.JComponent) parent).setBorder(null);
                                    ((javax.swing.JComponent) parent).setOpaque(true);
                                }
                            }
                            if (parent instanceof javax.swing.JRootPane) break;
                            parent = parent.getParent();
                        }
                    } else {
                        // Restore
                        java.awt.Container parent = getParent();
                        while (parent != null) {
                            if (parent instanceof javax.swing.JComponent) {
                                parent.setBackground(javax.swing.UIManager.getColor("Panel.background"));
                                if (parent == getParent() || parent.getClass().getName().contains("Container")) {
                                    ((javax.swing.JComponent) parent).setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
                                }
                            }
                            if (parent instanceof javax.swing.JRootPane) break;
                            parent = parent.getParent();
                        }
                    }
                });
            }
        });

        cardLayout = new CardLayout();
        cards = new JPanel(cardLayout);
        cards.setOpaque(false);
        
        cards.add(buildSearchCard(), "search");
        cards.add(buildDashboardCard(), "dashboard");
        
        add(cards, BorderLayout.CENTER);
        
        btnSearch.addActionListener(e -> loadVariation());
        txtProduct.addActionListener(e -> loadVariation());
        refreshGlobalMetrics();
    }

    private JPanel buildSearchCard() {
        JPanel pnl = new JPanel(new GridBagLayout());
        pnl.setOpaque(false);
        
        JPanel centerContent = new JPanel();
        centerContent.setLayout(new BoxLayout(centerContent, BoxLayout.Y_AXIS));
        centerContent.setOpaque(false);
        
        // Logo / Icon placeholder
        JLabel lblIcon = new JLabel("📈");
        lblIcon.setFont(new Font("Segoe UI", Font.PLAIN, 64));
        lblIcon.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerContent.add(lblIcon);
        centerContent.add(Box.createVerticalStrut(20));

        JLabel title = new JLabel("Análisis de variación de precios");
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(TEXT_MAIN);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerContent.add(title);

        centerContent.add(Box.createVerticalStrut(10));

        JLabel subtitle = new JLabel("Busca un producto para visualizar cómo ha cambiado su precio.");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        subtitle.setForeground(TEXT_SUB);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerContent.add(subtitle);
        
        centerContent.add(Box.createVerticalStrut(40));

        // Search Bar
        RoundPanel card = new RoundPanel(20, BG_CARD);
        card.setLayout(new BorderLayout(0, 12));
        card.setBorder(new EmptyBorder(16, 18, 16, 18));
        card.setMaximumSize(new Dimension(600, 120));
        card.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel inputRow = new JPanel(new BorderLayout(12, 0));
        inputRow.setOpaque(false);

        RoundPanel fieldShell = new RoundPanel(10, BG_PAGE);
        fieldShell.setLayout(new BorderLayout(10, 0));
        fieldShell.setBorder(new EmptyBorder(6, 10, 6, 10));

        JLabel codeBadge = new JLabel("CÓDIGO / REF");
        codeBadge.setForeground(TEXT_SUB);
        codeBadge.setFont(new Font("Segoe UI", Font.BOLD, 11));
        fieldShell.add(codeBadge, BorderLayout.WEST);

        txtProduct = new JTextField(24);
        txtProduct.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        txtProduct.setBackground(BG_PAGE);
        txtProduct.setForeground(TEXT_MAIN);
        txtProduct.setCaretColor(TEXT_MAIN);
        txtProduct.setBorder(null);
        fieldShell.add(txtProduct, BorderLayout.CENTER);
        inputRow.add(fieldShell, BorderLayout.CENTER);

        btnSearch = new JButton("Buscar");
        btnSearch.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnSearch.setForeground(Color.WHITE);
        btnSearch.setBackground(ACCENT_BLUE);
        btnSearch.setBorder(new EmptyBorder(10, 24, 10, 24));
        btnSearch.setFocusPainted(false);
        btnSearch.setOpaque(true);
        inputRow.add(btnSearch, BorderLayout.EAST);

        card.add(inputRow, BorderLayout.CENTER);

        lblStatus = new JLabel("Estado: Esperando búsqueda...");
        lblStatus.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblStatus.setForeground(TEXT_SUB);
        card.add(lblStatus, BorderLayout.SOUTH);
        
        centerContent.add(card);
        
        // Add vertical spacing
        centerContent.add(Box.createVerticalStrut(40));
        
        // Metrics title
        JLabel lblMetricsTitle = new JLabel("Resumen de Variaciones en el Negocio");
        lblMetricsTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblMetricsTitle.setForeground(TEXT_MAIN);
        lblMetricsTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerContent.add(lblMetricsTitle);
        
        centerContent.add(Box.createVerticalStrut(15));
        
        // Metrics row
        JPanel pnlMetricsRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        pnlMetricsRow.setOpaque(false);
        
        pnlMetricsRow.add(new MetricCard("🟢", new Color(16, 185, 129, 30), "Mejor Mes Ahorro", lblBestMonthVal, "Precios de compra más bajos"));
        pnlMetricsRow.add(new MetricCard("🔴", new Color(239, 68, 68, 30), "Mes de Mayor Alza", lblWorstMonthVal, "Mayores incrementos promedio"));
        pnlMetricsRow.add(new MetricCard("🔵", new Color(59, 130, 246, 30), "Ajustes Totales", lblTotalChangesVal, "Cambios de precio registrados"));
        pnlMetricsRow.add(new MetricCard("🟡", new Color(245, 158, 11, 30), "Mayor Fluctuación", lblTopProductVal, "Producto con más variaciones"));
        
        pnlMetricsRow.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerContent.add(pnlMetricsRow);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        pnl.add(centerContent, gbc);

        return pnl;
    }

    private java.util.Date dateDesde;
    private java.util.Date dateHasta;

    private JPanel buildDashboardCard() {
        JPanel pnl = new JPanel(new BorderLayout());
        pnl.setOpaque(false);
        
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(20, 20, 10, 20));
        
        // Title area (Left)
        JPanel pnlTitleArea = new JPanel();
        pnlTitleArea.setLayout(new BoxLayout(pnlTitleArea, BoxLayout.Y_AXIS));
        pnlTitleArea.setOpaque(false);
        
        JButton btnBack = new JButton("← Volver a Búsqueda");
        btnBack.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        btnBack.setForeground(ACCENT_BLUE);
        btnBack.setContentAreaFilled(false);
        btnBack.setBorderPainted(false);
        btnBack.setFocusPainted(false);
        btnBack.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnBack.setMargin(new Insets(0, 0, 5, 0));
        btnBack.addActionListener(e -> showSearchState());
        
        JLabel lblTitle = new JLabel("Análisis de variación de precios");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(TEXT_MAIN);
        
        JLabel lblSubtitle = new JLabel("Consulta cómo ha cambiado el precio de este artículo en el tiempo.");
        lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSubtitle.setForeground(TEXT_SUB);
        
        btnBack.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblSubtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        pnlTitleArea.add(btnBack);
        pnlTitleArea.add(lblTitle);
        pnlTitleArea.add(Box.createVerticalStrut(2));
        pnlTitleArea.add(lblSubtitle);
        
        header.add(pnlTitleArea, BorderLayout.WEST);
        
        // Actions area (Right)
        JPanel pnlActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        pnlActions.setOpaque(false);
        
        ActionButton btnDateRange = new ActionButton("📅 Seleccionar fechas   v");
        btnDateRange.addActionListener(e -> {
            JPanel pnlDate = new JPanel(new GridLayout(2, 3, 5, 5));
            pnlDate.add(new JLabel("Desde:"));
            JTextField txtD = new JTextField(10);
            txtD.setEditable(false);
            if (dateDesde != null) txtD.setText(new java.text.SimpleDateFormat("dd/MM/yyyy").format(dateDesde));
            JButton btnD = new JButton("...");
            btnD.addActionListener(ev -> {
                java.util.Date d = com.openbravo.beans.JCalendarDialog.showCalendarTimeHours(this, dateDesde != null ? dateDesde : new java.util.Date());
                if (d != null) { dateDesde = d; txtD.setText(new java.text.SimpleDateFormat("dd/MM/yyyy").format(d)); }
            });
            pnlDate.add(txtD); pnlDate.add(btnD);
            
            pnlDate.add(new JLabel("Hasta:"));
            JTextField txtH = new JTextField(10);
            txtH.setEditable(false);
            if (dateHasta != null) txtH.setText(new java.text.SimpleDateFormat("dd/MM/yyyy").format(dateHasta));
            JButton btnH = new JButton("...");
            btnH.addActionListener(ev -> {
                java.util.Date d = com.openbravo.beans.JCalendarDialog.showCalendarTimeHours(this, dateHasta != null ? dateHasta : new java.util.Date());
                if (d != null) { dateHasta = d; txtH.setText(new java.text.SimpleDateFormat("dd/MM/yyyy").format(d)); }
            });
            pnlDate.add(txtH); pnlDate.add(btnH);
            
            Object[] options = {"Aplicar", "Limpiar filtros", "Cancelar"};
            int res = javax.swing.JOptionPane.showOptionDialog(this, pnlDate, "Filtrar por fechas", javax.swing.JOptionPane.YES_NO_CANCEL_OPTION, javax.swing.JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
            
            if (res == 0) { // Aplicar
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MMM yyyy");
                if (dateDesde != null && dateHasta != null) btnDateRange.setText("📅 " + sdf.format(dateDesde) + " - " + sdf.format(dateHasta) + "   v");
                else if (dateDesde != null) btnDateRange.setText("📅 Desde " + sdf.format(dateDesde) + "   v");
                else if (dateHasta != null) btnDateRange.setText("📅 Hasta " + sdf.format(dateHasta) + "   v");
                else btnDateRange.setText("📅 Seleccionar fechas   v");
                loadVariation();
            } else if (res == 1) { // Limpiar
                dateDesde = null; dateHasta = null;
                btnDateRange.setText("📅 Seleccionar fechas   v");
                loadVariation();
            }
        });
        
        ActionButton btnExport = new ActionButton("📥 Exportar");
        btnExport.addActionListener(e -> chart.exportToCSV(this));
        
        pnlActions.add(btnDateRange);
        pnlActions.add(btnExport);
        
        header.add(pnlActions, BorderLayout.EAST);
        
        chart = new JProductVariationPanel();
        pnl.add(header, BorderLayout.NORTH);
        pnl.add(chart, BorderLayout.CENTER);
        
        return pnl;
    }

    private void showSearchState() {
        txtProduct.setText("");
        lblStatus.setText("Estado: Esperando búsqueda...");
        cardLayout.show(cards, "search");
        txtProduct.requestFocus();
        chart.clearHistory();
        refreshGlobalMetrics();
    }

    private void loadVariation() {
        String ref = txtProduct.getText().trim();
        if (ref.isEmpty()) {
            lblStatus.setText("Estado: Ingresa un código o referencia");
            return;
        }

        try {
            ProductInfoExt prod = dlSales.getProductInfoByCode(ref);
            if (prod == null) {
                prod = dlSales.getProductInfoByReference(ref);
            }

            if (prod != null) {
                List<ProductPriceHistory> history = dlSales.getProductPriceHistory(prod.getID());
                
                // Filter by dates if selected
                if (dateDesde != null || dateHasta != null) {
                    history = history.stream().filter(p -> {
                        boolean pass = true;
                        if (dateDesde != null && p.getDateNew() != null && p.getDateNew().before(dateDesde)) pass = false;
                        if (dateHasta != null && p.getDateNew() != null) {
                            // Set to end of day for inclusive filtering
                            java.util.Date endOfDay = new java.util.Date(dateHasta.getTime() + 86400000L - 1);
                            if (p.getDateNew().after(endOfDay)) pass = false;
                        }
                        return pass;
                    }).collect(java.util.stream.Collectors.toList());
                }
                
                chart.setHistory(history, prod.getID(), prod.getPriceBuy(), prod.getPriceSell(), prod.getName(), prod.getReference());
                cardLayout.show(cards, "dashboard");
            } else {
                lblStatus.setText("Estado: Producto no encontrado. Intente con otro.");
                txtProduct.selectAll();
            }
        } catch (BasicException ex) {
            new MessageInf(ex).show(this);
        }
    }

    @Override
    public String getTitle() {
        return "Variación de Precios";
    }

    @Override
    public void activate() throws BasicException {
        showSearchState();
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
    private class ActionButton extends JButton {
        public ActionButton(String text) {
            super(text);
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setForeground(TEXT_MAIN);
            setFont(new Font("Segoe UI", Font.PLAIN, 14));
            setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
            setBorder(new EmptyBorder(8, 15, 8, 15));
        }
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (getModel().isRollover()) g2.setColor(new Color(30, 35, 45));
            else g2.setColor(BG_PAGE);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
            g2.setColor(BORDER_CARD);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    class RoundPanel extends JPanel {
        private int radius;
        private Color bg;
        public RoundPanel(int radius, Color bg) {
            this.radius = radius;
            this.bg = bg;
            setOpaque(false);
        }
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(bg);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
            g2.setColor(BORDER_CARD);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private class MetricCard extends RoundPanel {
        public MetricCard(String icon, Color iconBg, String title, JLabel valueLabel, String subtitle) {
            super(16, BG_CARD);
            setLayout(new BorderLayout(15, 0));
            setBorder(new EmptyBorder(15, 15, 15, 15));
            setPreferredSize(new Dimension(220, 100));
            
            // Icon
            RoundPanel pnlIcon = new RoundPanel(12, iconBg);
            pnlIcon.setLayout(new GridBagLayout());
            pnlIcon.setPreferredSize(new Dimension(48, 48));
            JLabel lblIcon = new JLabel(icon);
            lblIcon.setFont(new Font("Segoe UI", Font.PLAIN, 20));
            lblIcon.setForeground(Color.WHITE);
            pnlIcon.add(lblIcon);
            add(pnlIcon, BorderLayout.WEST);
            
            // Text panel
            JPanel pnlText = new JPanel();
            pnlText.setLayout(new BoxLayout(pnlText, BoxLayout.Y_AXIS));
            pnlText.setOpaque(false);
            
            JLabel lblTitle = new JLabel(title);
            lblTitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            lblTitle.setForeground(TEXT_SUB);
            
            valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
            valueLabel.setForeground(TEXT_MAIN);
            
            JLabel lblSub = new JLabel(subtitle);
            lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            lblSub.setForeground(TEXT_SUB);
            
            pnlText.add(lblTitle);
            pnlText.add(Box.createVerticalStrut(3));
            pnlText.add(valueLabel);
            pnlText.add(Box.createVerticalStrut(3));
            pnlText.add(lblSub);
            
            add(pnlText, BorderLayout.CENTER);
        }
    }

    private static class GlobalMetricsData {
        String totalChanges = "0";
        String topProduct = "Ninguno";
        String bestMonth = "Sin datos";
        String worstMonth = "Sin datos";
    }

    private GlobalMetricsData loadGlobalMetrics() {
        GlobalMetricsData data = new GlobalMetricsData();
        try {
            java.sql.Connection conn = dlSales.getSession().getConnection();
            
            // 1. Total Changes
            try (java.sql.Statement stmt = conn.createStatement();
                 java.sql.ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM PRODUCT_PRICES_HISTORY")) {
                if (rs.next()) {
                    data.totalChanges = String.valueOf(rs.getInt(1));
                }
            } catch (Exception e) {}
            
            // 2. Top Product with most adjustments
            try (java.sql.Statement stmt = conn.createStatement();
                 java.sql.ResultSet rs = stmt.executeQuery(
                     "SELECT p.NAME, COUNT(*) as cnt FROM PRODUCT_PRICES_HISTORY h " +
                     "JOIN products p ON h.PRODUCT_ID = p.ID " +
                     "GROUP BY p.NAME ORDER BY cnt DESC")) {
                if (rs.next()) {
                    data.topProduct = rs.getString(1);
                    if (data.topProduct.length() > 18) {
                        data.topProduct = data.topProduct.substring(0, 15) + "...";
                    }
                }
            } catch (Exception e) {}
            
            // 3. Best and Worst Month based on price trends or adjustment counts
            try (java.sql.Statement stmt = conn.createStatement();
                 java.sql.ResultSet rs = stmt.executeQuery(
                     "SELECT DATENEW, PRICEBUY FROM PRODUCT_PRICES_HISTORY ORDER BY DATENEW DESC")) {
                java.util.Map<Integer, Double> monthBuySums = new java.util.HashMap<>();
                java.util.Map<Integer, Integer> monthBuyCounts = new java.util.HashMap<>();
                
                int count = 0;
                while (rs.next() && count < 500) {
                    java.sql.Timestamp ts = rs.getTimestamp(1);
                    double buy = rs.getDouble(2);
                    if (ts != null) {
                        java.util.Calendar cal = java.util.Calendar.getInstance();
                        cal.setTimeInMillis(ts.getTime());
                        int month = cal.get(java.util.Calendar.MONTH); // 0-11
                        
                        if (buy > 0) {
                            monthBuySums.put(month, monthBuySums.getOrDefault(month, 0.0) + buy);
                            monthBuyCounts.put(month, monthBuyCounts.getOrDefault(month, 0) + 1);
                        }
                    }
                    count++;
                }
                
                String[] monthNames = {"Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", 
                                       "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"};
                
                int bestM = -1;
                double minAvg = Double.MAX_VALUE;
                for (java.util.Map.Entry<Integer, Double> entry : monthBuySums.entrySet()) {
                    int m = entry.getKey();
                    double avg = entry.getValue() / monthBuyCounts.get(m);
                    if (avg < minAvg) {
                        minAvg = avg;
                        bestM = m;
                    }
                }
                if (bestM != -1) {
                    data.bestMonth = monthNames[bestM];
                }
                
                int worstM = -1;
                double maxAvg = -1;
                for (java.util.Map.Entry<Integer, Double> entry : monthBuySums.entrySet()) {
                    int m = entry.getKey();
                    double avg = entry.getValue() / monthBuyCounts.get(m);
                    if (avg > maxAvg) {
                        maxAvg = avg;
                        worstM = m;
                    }
                }
                if (worstM != -1) {
                    data.worstMonth = monthNames[worstM];
                }
            } catch (Exception e) {}
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        return data;
    }

    private void refreshGlobalMetrics() {
        GlobalMetricsData metrics = loadGlobalMetrics();
        lblBestMonthVal.setText(metrics.bestMonth);
        lblWorstMonthVal.setText(metrics.worstMonth);
        lblTotalChangesVal.setText(metrics.totalChanges);
        lblTopProductVal.setText(metrics.topProduct);
    }
}

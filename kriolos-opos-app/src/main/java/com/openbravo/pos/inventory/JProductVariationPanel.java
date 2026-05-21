package com.openbravo.pos.inventory;

import com.openbravo.pos.ticket.ProductPriceHistory;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

public class JProductVariationPanel extends JPanel {

    private static final Color BG_PAGE = new Color(9, 10, 15);
    private static final Color BG_CARD = new Color(22, 27, 34);
    private static final Color BORDER_CARD = new Color(48, 54, 61);
    private static final Color TEXT_MAIN = new Color(255, 255, 255);
    private static final Color TEXT_SUB = new Color(139, 148, 158);
    private static final Color ACCENT_PURPLE = new Color(139, 92, 246);
    private static final Color ACCENT_GREEN = new Color(16, 185, 129);
    private static final Color ACCENT_RED = new Color(239, 68, 68);
    private static final Color ACCENT_BLUE = new Color(59, 130, 246);

    private final DecimalFormat moneyFormat = new DecimalFormat("$#,##0");
    private final DecimalFormat percentFormat = new DecimalFormat("0.00'%'");
    private final SimpleDateFormat shortDate = new SimpleDateFormat("MMM yyyy", new Locale("es", "MX"));

    private JLabel lblProdName, lblProdCat, lblProdSKU;
    private JLabel lblPriceActual, lblVarMonth, lblPriceLow, lblLowDate, lblPriceHigh, lblHighDate, lblVarYear;
    private ChartPanel chartEvolution;
    private JLabel lblBestMonth, lblBestDate, lblWorstMonth, lblWorstDate, lblAvgYearPrice, lblAvgYearDate, lblVarYearValue, lblVarYearDate;
    private JTable tableHistory;
    private DefaultTableModel tableModel;
    private ChartPanel chartCompare;
    private JLabel lblPredPrice, lblPredTrend, lblPredDetail;

    public JProductVariationPanel() {
        setLayout(new BorderLayout());
        setBackground(BG_PAGE);
        setBorder(new EmptyBorder(0, 0, 0, 0));

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(BG_PAGE);
        content.setBorder(new EmptyBorder(20, 20, 20, 20));

        content.add(createTopPanel());
        content.add(Box.createVerticalStrut(20));
        content.add(createEvolutionPanel());
        content.add(Box.createVerticalStrut(20));
        content.add(createMetricsPanel());
        content.add(Box.createVerticalStrut(20));
        content.add(createBottomPanel());

        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.getVerticalScrollBar().setBackground(BG_PAGE);
        scroll.getVerticalScrollBar().setOpaque(true);
        scroll.getHorizontalScrollBar().setBackground(BG_PAGE);
        scroll.getHorizontalScrollBar().setOpaque(true);
        scroll.setOpaque(true);
        scroll.setBackground(BG_PAGE);
        scroll.getViewport().setOpaque(true);
        scroll.getViewport().setBackground(BG_PAGE);
        add(scroll, BorderLayout.CENTER);
    }

    private JPanel createTopPanel() {
        RoundPanel pnl = new RoundPanel(20, BG_CARD);
        pnl.setLayout(new BorderLayout(20, 20));
        pnl.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Info Product
        JPanel pnlInfo = new JPanel(new BorderLayout(15, 0));
        pnlInfo.setOpaque(false);
        
        JLabel lblImg = new JLabel();
        lblImg.setPreferredSize(new Dimension(80, 80));
        lblImg.setOpaque(true);
        lblImg.setBackground(new Color(30, 30, 40));
        lblImg.setHorizontalAlignment(SwingConstants.CENTER);
        lblImg.setIcon(UIManager.getIcon("FileView.computerIcon")); // Placeholder
        pnlInfo.add(lblImg, BorderLayout.WEST);

        JPanel pnlText = new JPanel();
        pnlText.setLayout(new BoxLayout(pnlText, BoxLayout.Y_AXIS));
        pnlText.setOpaque(false);

        lblProdName = new JLabel("Seleccione un producto");
        lblProdName.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblProdName.setForeground(TEXT_MAIN);
        pnlText.add(lblProdName);
        pnlText.add(Box.createVerticalStrut(5));

        JPanel pnlBadges = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        pnlBadges.setOpaque(false);
        lblProdCat = createBadge("Categoría", ACCENT_PURPLE);
        lblProdSKU = new JLabel("SKU: ---");
        lblProdSKU.setForeground(TEXT_SUB);
        lblProdSKU.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        pnlBadges.add(lblProdCat);
        pnlBadges.add(lblProdSKU);
        pnlText.add(pnlBadges);

        pnlInfo.add(pnlText, BorderLayout.CENTER);
        pnl.add(pnlInfo, BorderLayout.WEST);

        // Stats Grid
        JPanel pnlStats = new JPanel(new GridLayout(2, 3, 20, 15));
        pnlStats.setOpaque(false);

        JPanel s1 = createStatBlock("Precio actual", lblPriceActual = createLabel("$0", 20, TEXT_MAIN), null);
        JPanel s2 = createStatBlock("Variación (último mes)", lblVarMonth = createLabel("0%", 16, TEXT_MAIN), null);
        JPanel s3 = createStatBlock("Precio más bajo", lblPriceLow = createLabel("$0", 18, ACCENT_GREEN), lblLowDate = createLabel("---", 12, TEXT_SUB));
        JPanel s4 = createStatBlock("Precio más alto", lblPriceHigh = createLabel("$0", 18, ACCENT_RED), lblHighDate = createLabel("---", 12, TEXT_SUB));
        JPanel s5 = createStatBlock("Variación anual", lblVarYear = createLabel("0%", 18, TEXT_MAIN), null);

        pnlStats.add(s1);
        pnlStats.add(s2);
        pnlStats.add(s3);
        pnlStats.add(s4);
        pnlStats.add(s5);

        pnl.add(pnlStats, BorderLayout.CENTER);

        return pnl;
    }

    private JPanel createEvolutionPanel() {
        RoundPanel pnl = new RoundPanel(20, BG_CARD);
        pnl.setLayout(new BorderLayout());
        pnl.setBorder(new EmptyBorder(20, 20, 20, 20));
        pnl.setPreferredSize(new Dimension(800, 300));

        JLabel title = new JLabel("Evolución del precio");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(TEXT_MAIN);
        pnl.add(title, BorderLayout.NORTH);

        JFreeChart chart = ChartFactory.createXYLineChart(null, null, null, null, PlotOrientation.VERTICAL, false, true, false);
        chart.setBackgroundPaint(BG_CARD);
        XYPlot plot = chart.getXYPlot();
        plot.setBackgroundPaint(BG_CARD);
        plot.setDomainGridlinePaint(BORDER_CARD);
        plot.setRangeGridlinePaint(BORDER_CARD);
        plot.setOutlinePaint(null);

        ValueAxis domainAxis = plot.getDomainAxis();
        domainAxis.setTickLabelPaint(TEXT_SUB);
        domainAxis.setAxisLinePaint(BORDER_CARD);

        ValueAxis rangeAxis = plot.getRangeAxis();
        rangeAxis.setTickLabelPaint(TEXT_SUB);
        rangeAxis.setAxisLinePaint(BORDER_CARD);

        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        renderer.setSeriesPaint(0, ACCENT_PURPLE);
        renderer.setSeriesStroke(0, new BasicStroke(3.0f));
        renderer.setSeriesShapesVisible(0, true);
        plot.setRenderer(renderer);

        chartEvolution = new ChartPanel(chart);
        chartEvolution.setOpaque(false);
        chartEvolution.setBackground(BG_CARD);
        pnl.add(chartEvolution, BorderLayout.CENTER);

        return pnl;
    }

    private JPanel createMetricsPanel() {
        JPanel pnl = new JPanel(new GridLayout(1, 4, 15, 0));
        pnl.setOpaque(false);

        pnl.add(createMetricCard("Mejor mes para comprar", lblBestMonth = createLabel("---", 20, ACCENT_GREEN), lblBestDate = createLabel("---", 12, TEXT_SUB), ACCENT_GREEN));
        pnl.add(createMetricCard("Peor mes para comprar", lblWorstMonth = createLabel("---", 20, ACCENT_RED), lblWorstDate = createLabel("---", 12, TEXT_SUB), ACCENT_RED));
        pnl.add(createMetricCard("Precio promedio anual", lblAvgYearPrice = createLabel("$0", 20, TEXT_MAIN), lblAvgYearDate = createLabel("---", 12, TEXT_SUB), ACCENT_BLUE));
        pnl.add(createMetricCard("Variación anual", lblVarYearValue = createLabel("0%", 20, TEXT_MAIN), lblVarYearDate = createLabel("---", 12, TEXT_SUB), ACCENT_PURPLE));

        return pnl;
    }

    private JPanel createBottomPanel() {
        JPanel pnl = new JPanel(new GridBagLayout());
        pnl.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;

        // Histórico de precios
        RoundPanel pnlHistory = new RoundPanel(20, BG_CARD);
        pnlHistory.setLayout(new BorderLayout());
        pnlHistory.setBorder(new EmptyBorder(20, 20, 20, 20));
        JLabel titleHistory = new JLabel("Histórico de precios");
        titleHistory.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleHistory.setForeground(TEXT_MAIN);
        pnlHistory.add(titleHistory, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(new String[]{"Mes", "Precio", "Cambio %", "Cambio $", "Estado"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tableHistory = new JTable(tableModel);
        tableHistory.setBackground(BG_CARD);
        tableHistory.setForeground(TEXT_MAIN);
        tableHistory.setGridColor(BORDER_CARD);
        tableHistory.setRowHeight(30);
        tableHistory.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        
        JTableHeader header = tableHistory.getTableHeader();
        header.setBackground(BG_CARD);
        header.setForeground(TEXT_SUB);
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_CARD));
        
        tableHistory.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (column == 4) { // Estado
                    String val = value.toString();
                    if (val.equals("Subió")) c.setForeground(ACCENT_RED);
                    else if (val.equals("Bajó")) c.setForeground(ACCENT_GREEN);
                    else c.setForeground(TEXT_SUB);
                } else if (column == 2 || column == 3) {
                    String val = value.toString();
                    if (val.startsWith("+") || val.startsWith("▲")) c.setForeground(ACCENT_RED);
                    else if (val.startsWith("-") || val.startsWith("▼")) c.setForeground(ACCENT_GREEN);
                    else c.setForeground(TEXT_SUB);
                } else {
                    c.setForeground(TEXT_MAIN);
                }
                return c;
            }
        });

        JScrollPane scrollTable = new JScrollPane(tableHistory);
        scrollTable.getViewport().setBackground(BG_CARD);
        scrollTable.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        pnlHistory.add(scrollTable, BorderLayout.CENTER);

        // Comparación de precios por año
        RoundPanel pnlCompare = new RoundPanel(20, BG_CARD);
        pnlCompare.setLayout(new BorderLayout());
        pnlCompare.setBorder(new EmptyBorder(20, 20, 20, 20));
        JLabel titleCompare = new JLabel("Comparación de precios por año");
        titleCompare.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleCompare.setForeground(TEXT_MAIN);
        pnlCompare.add(titleCompare, BorderLayout.NORTH);

        JFreeChart chartBar = ChartFactory.createBarChart(null, null, null, null, PlotOrientation.VERTICAL, false, true, false);
        chartBar.setBackgroundPaint(BG_CARD);
        CategoryPlot plotBar = chartBar.getCategoryPlot();
        plotBar.setBackgroundPaint(BG_CARD);
        plotBar.setDomainGridlinePaint(BORDER_CARD);
        plotBar.setRangeGridlinePaint(BORDER_CARD);
        plotBar.setOutlinePaint(null);
        plotBar.getDomainAxis().setTickLabelPaint(TEXT_SUB);
        plotBar.getRangeAxis().setTickLabelPaint(TEXT_SUB);
        BarRenderer barRenderer = (BarRenderer) plotBar.getRenderer();
        barRenderer.setSeriesPaint(0, ACCENT_PURPLE);
        barRenderer.setSeriesPaint(1, ACCENT_BLUE);
        chartCompare = new ChartPanel(chartBar);
        chartCompare.setOpaque(false);
        chartCompare.setBackground(BG_CARD);
        pnlCompare.add(chartCompare, BorderLayout.CENTER);

        // Predicción de precio
        RoundPanel pnlPrediction = new RoundPanel(20, BG_CARD);
        pnlPrediction.setLayout(new BoxLayout(pnlPrediction, BoxLayout.Y_AXIS));
        pnlPrediction.setBorder(new EmptyBorder(20, 20, 20, 20));
        JLabel titlePred = new JLabel("Predicción de precio");
        titlePred.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titlePred.setForeground(TEXT_MAIN);
        pnlPrediction.add(titlePred);
        pnlPrediction.add(Box.createVerticalStrut(15));
        
        lblPredPrice = createLabel("$0", 24, TEXT_MAIN);
        pnlPrediction.add(lblPredPrice);
        
        lblPredTrend = createLabel("Tendencia esperada", 14, TEXT_SUB);
        pnlPrediction.add(lblPredTrend);
        pnlPrediction.add(Box.createVerticalStrut(10));
        
        lblPredDetail = new JLabel("<html>Se espera un cambio basado en análisis histórico.</html>");
        lblPredDetail.setForeground(TEXT_SUB);
        lblPredDetail.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        pnlPrediction.add(lblPredDetail);

        // Add to bottom panel grid
        gbc.weightx = 0.4;
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridheight = 2;
        gbc.insets = new Insets(0, 0, 0, 15);
        pnl.add(pnlHistory, gbc);

        gbc.weightx = 0.6;
        gbc.gridx = 1; gbc.gridy = 0; gbc.gridheight = 1;
        gbc.insets = new Insets(0, 0, 15, 0);
        pnl.add(pnlCompare, gbc);

        gbc.gridx = 1; gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 0, 0);
        pnl.add(pnlPrediction, gbc);

        return pnl;
    }

    private JPanel createStatBlock(String title, JLabel lblVal, JLabel lblSub) {
        JPanel pnl = new JPanel();
        pnl.setLayout(new BoxLayout(pnl, BoxLayout.Y_AXIS));
        pnl.setOpaque(false);
        JLabel lblT = new JLabel(title);
        lblT.setForeground(TEXT_SUB);
        lblT.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        pnl.add(lblT);
        pnl.add(Box.createVerticalStrut(3));
        pnl.add(lblVal);
        if (lblSub != null) {
            pnl.add(Box.createVerticalStrut(2));
            pnl.add(lblSub);
        }
        return pnl;
    }

    private JPanel createMetricCard(String title, JLabel main, JLabel sub, Color accent) {
        RoundPanel pnl = new RoundPanel(20, BG_CARD);
        pnl.setLayout(new BoxLayout(pnl, BoxLayout.Y_AXIS));
        pnl.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        JLabel icon = new JLabel("●");
        icon.setForeground(accent);
        icon.setFont(new Font("Segoe UI", Font.PLAIN, 24));
        
        JLabel lblT = new JLabel(title);
        lblT.setForeground(TEXT_SUB);
        lblT.setFont(new Font("Segoe UI", Font.BOLD, 12));
        
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        top.setOpaque(false);
        top.add(icon);
        top.add(lblT);
        
        pnl.add(top);
        pnl.add(Box.createVerticalStrut(10));
        pnl.add(main);
        pnl.add(Box.createVerticalStrut(5));
        pnl.add(sub);
        
        return pnl;
    }

    private JLabel createLabel(String text, int size, Color c) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, size));
        l.setForeground(c);
        return l;
    }

    private JLabel createBadge(String text, Color bg) {
        JLabel l = new JLabel(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                super.paintComponent(g);
                g2.dispose();
            }
        };
        l.setForeground(Color.WHITE);
        l.setFont(new Font("Segoe UI", Font.BOLD, 11));
        l.setBorder(new EmptyBorder(4, 8, 4, 8));
        l.setOpaque(false);
        return l;
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

    public void setHistory(List<ProductPriceHistory> history, String productId, Double priceBuy, Double priceSell) {
        setHistory(history, productId, priceBuy, priceSell, null, null);
    }

    public void setHistory(List<ProductPriceHistory> history, String productId, Double priceBuy, Double priceSell, String name, String sku) {
        lblProdName.setText(name != null ? name : "Producto");
        lblProdSKU.setText("SKU: " + (sku != null ? sku : "N/A"));

        if (history == null || history.isEmpty()) {
            clearHistory();
            return;
        }

        // Sort ascending
        history.sort(Comparator.comparing(ProductPriceHistory::getDateNew));

        double currentPrice = priceSell != null ? priceSell : history.get(history.size() - 1).getPriceSell();
        lblPriceActual.setText(moneyFormat.format(currentPrice));

        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;
        ProductPriceHistory minItem = null;
        ProductPriceHistory maxItem = null;
        double sum = 0;

        XYSeries series = new XYSeries("Precio");
        DefaultCategoryDataset barDataset = new DefaultCategoryDataset();

        tableModel.setRowCount(0);

        for (int i = 0; i < history.size(); i++) {
            ProductPriceHistory item = history.get(i);
            double p = item.getPriceSell();
            if (p < min) { min = p; minItem = item; }
            if (p > max) { max = p; maxItem = item; }
            sum += p;

            series.add(item.getDateNew().getTime(), p);
            
            String monthLabel = shortDate.format(item.getDateNew());
            barDataset.addValue(p, "Precio", monthLabel);

            // Table entry
            double prev = i > 0 ? history.get(i - 1).getPriceSell() : p;
            double change = p - prev;
            double pct = prev > 0 ? (change / prev) * 100 : 0;
            
            String changePctStr = (change > 0 ? "▲ " : change < 0 ? "▼ " : "") + percentFormat.format(Math.abs(pct));
            String changeAmtStr = (change > 0 ? "+" : "") + moneyFormat.format(change);
            String state = change > 0 ? "Subió" : change < 0 ? "Bajó" : "Igual";
            
            tableModel.insertRow(0, new Object[]{
                monthLabel, moneyFormat.format(p), changePctStr, changeAmtStr, state
            });
        }

        lblPriceLow.setText(moneyFormat.format(min));
        if (minItem != null) lblLowDate.setText(shortDate.format(minItem.getDateNew()));
        
        lblPriceHigh.setText(moneyFormat.format(max));
        if (maxItem != null) lblHighDate.setText(shortDate.format(maxItem.getDateNew()));

        double avg = sum / history.size();
        lblAvgYearPrice.setText(moneyFormat.format(avg));
        lblAvgYearDate.setText("Promedio del periodo");

        if (history.size() > 1) {
            double prevP = history.get(history.size() - 2).getPriceSell();
            double c = currentPrice - prevP;
            double pct = prevP > 0 ? (c / prevP) * 100 : 0;
            lblVarMonth.setText((c > 0 ? "▲ " : c < 0 ? "▼ " : "") + percentFormat.format(Math.abs(pct)));
            lblVarMonth.setForeground(c > 0 ? ACCENT_RED : c < 0 ? ACCENT_GREEN : TEXT_SUB);
            
            double firstP = history.get(0).getPriceSell();
            double yC = currentPrice - firstP;
            double yPct = firstP > 0 ? (yC / firstP) * 100 : 0;
            String yPctStr = (yC > 0 ? "▲ " : yC < 0 ? "▼ " : "") + percentFormat.format(Math.abs(yPct));
            lblVarYear.setText(yPctStr);
            lblVarYear.setForeground(yC > 0 ? ACCENT_RED : yC < 0 ? ACCENT_GREEN : TEXT_SUB);
            
            lblVarYearValue.setText(yPctStr);
            lblVarYearValue.setForeground(yC > 0 ? ACCENT_RED : yC < 0 ? ACCENT_GREEN : TEXT_SUB);
            lblVarYearDate.setText("vs inicio");
        } else {
            lblVarMonth.setText("0%");
            lblVarMonth.setForeground(TEXT_SUB);
            lblVarYear.setText("0%");
            lblVarYear.setForeground(TEXT_SUB);
            lblVarYearValue.setText("0%");
            lblVarYearValue.setForeground(TEXT_SUB);
        }

        if (minItem != null) {
            lblBestMonth.setText(shortDate.format(minItem.getDateNew()));
            lblBestDate.setText("Precio: " + moneyFormat.format(min));
        }
        if (maxItem != null) {
            lblWorstMonth.setText(shortDate.format(maxItem.getDateNew()));
            lblWorstDate.setText("Precio: " + moneyFormat.format(max));
        }

        XYPlot plot = chartEvolution.getChart().getXYPlot();
        plot.setDataset(new XYSeriesCollection(series));

        CategoryPlot cPlot = chartCompare.getChart().getCategoryPlot();
        cPlot.setDataset(barDataset);

        // Prediction
        double trend = history.size() > 1 ? currentPrice - history.get(0).getPriceSell() : 0;
        double predicted = currentPrice + (trend * 0.1); // basic logic
        lblPredPrice.setText(moneyFormat.format(predicted));
        lblPredTrend.setText(trend > 0 ? "Al alza ↗" : trend < 0 ? "A la baja ↘" : "Estable →");
        lblPredTrend.setForeground(trend > 0 ? ACCENT_RED : trend < 0 ? ACCENT_GREEN : TEXT_SUB);
    }

    public void clearHistory() {
        lblProdName.setText("Sin datos");
        tableModel.setRowCount(0);
        ((XYPlot)chartEvolution.getChart().getPlot()).setDataset(null);
        ((CategoryPlot)chartCompare.getChart().getPlot()).setDataset(null);
    }
    
    public void exportToCSV(Component parent) {
        javax.swing.JFileChooser chooser = new javax.swing.JFileChooser();
        chooser.setDialogTitle("Exportar historial a CSV");
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Archivos CSV (*.csv)", "csv"));
        if (chooser.showSaveDialog(parent) == javax.swing.JFileChooser.APPROVE_OPTION) {
            java.io.File f = chooser.getSelectedFile();
            if (!f.getName().toLowerCase().endsWith(".csv")) {
                f = new java.io.File(f.getParentFile(), f.getName() + ".csv");
            }
            try (java.io.PrintWriter pw = new java.io.PrintWriter(new java.io.FileWriter(f))) {
                pw.println("Mes,Precio,Cambio %,Cambio $,Estado");
                for (int r = 0; r < tableModel.getRowCount(); r++) {
                    for (int c = 0; c < tableModel.getColumnCount(); c++) {
                        String val = String.valueOf(tableModel.getValueAt(r, c)).replace("\"", "\"\"");
                        pw.print("\"" + val + "\"");
                        if (c < tableModel.getColumnCount() - 1) pw.print(",");
                    }
                    pw.println();
                }
                javax.swing.JOptionPane.showMessageDialog(parent, "Exportación exitosa", "Éxito", javax.swing.JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                javax.swing.JOptionPane.showMessageDialog(parent, "Error al exportar: " + ex.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}

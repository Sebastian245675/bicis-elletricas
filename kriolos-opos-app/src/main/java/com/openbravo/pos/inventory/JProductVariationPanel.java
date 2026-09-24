package com.openbravo.pos.inventory;

import com.openbravo.pos.ticket.ProductPriceHistory;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

/** Vista detallada de precios de un producto y su impacto en el margen. */
public class JProductVariationPanel extends JPanel {

    private static final Color PAGE = new Color(247, 248, 250);
    private static final Color CARD = Color.WHITE;
    private static final Color BORDER = new Color(222, 223, 218);
    private static final Color NAVY = new Color(31, 35, 38);
    private static final Color TEXT = new Color(48, 51, 53);
    private static final Color MUTED = new Color(105, 111, 115);
    private static final Color BLUE = new Color(37, 99, 235);
    private static final Color GREEN = new Color(37, 99, 235);
    private static final Color RED = new Color(37, 99, 235);
    private static final Color AMBER = new Color(37, 99, 235);
    private static final Font LABEL = new Font("Segoe UI", Font.BOLD, 11);
    private static final Font BODY = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Locale MX = new Locale("es", "MX");

    private final NumberFormat money = NumberFormat.getCurrencyInstance(MX);
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", MX);
    private JLabel productName;
    private JLabel productSku;
    private JLabel saleValue;
    private JLabel costValue;
    private JLabel marginValue;
    private JLabel lowValue;
    private JLabel highValue;
    private JLabel changeValue;
    private JLabel recommendation;
    private JLabel recordCount;
    private ChartPanel priceChart;
    private DefaultTableModel historyModel;

    public JProductVariationPanel() {
        buildUi();
    }

    private void buildUi() {
        setLayout(new BorderLayout());
        setBackground(PAGE);
        JPanel content = new JPanel(new BorderLayout(0, 12));
        content.setBackground(PAGE);
        content.setBorder(new EmptyBorder(4, 20, 20, 20));
        content.add(createProductHeader(), BorderLayout.NORTH);

        JPanel middle = new JPanel(new GridBagLayout());
        middle.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = .58;
        gbc.weightx = .68;
        gbc.insets = new Insets(0, 0, 12, 12);
        middle.add(createChartCard(), gbc);
        gbc.gridx = 1;
        gbc.weightx = .32;
        gbc.insets = new Insets(0, 0, 12, 0);
        middle.add(createRecommendationCard(), gbc);
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.weightx = 1;
        gbc.weighty = .42;
        gbc.insets = new Insets(0, 0, 0, 0);
        middle.add(createHistoryCard(), gbc);
        content.add(middle, BorderLayout.CENTER);
        add(content, BorderLayout.CENTER);
    }

    private JPanel createProductHeader() {
        JPanel container = new JPanel(new BorderLayout(16, 12));
        container.setBackground(CARD);
        container.setBorder(BorderFactory.createCompoundBorder(new LineBorder(BORDER), new EmptyBorder(14, 16, 14, 16)));
        JPanel identity = new JPanel();
        identity.setLayout(new BoxLayout(identity, BoxLayout.Y_AXIS));
        identity.setOpaque(false);
        productName = new JLabel("Selecciona un producto");
        productName.setFont(new Font("Segoe UI", Font.BOLD, 19));
        productName.setForeground(NAVY);
        productSku = new JLabel("Referencia: —");
        productSku.setFont(BODY);
        productSku.setForeground(MUTED);
        identity.add(productName);
        identity.add(Box.createVerticalStrut(3));
        identity.add(productSku);
        container.add(identity, BorderLayout.WEST);

        JPanel metrics = new JPanel(new GridLayout(1, 6, 8, 0));
        metrics.setOpaque(false);
        saleValue = miniMetric(metrics, "PRECIO DE VENTA", "$0", BLUE);
        costValue = miniMetric(metrics, "COSTO", "$0", NAVY);
        marginValue = miniMetric(metrics, "MARGEN BRUTO", "0%", GREEN);
        changeValue = miniMetric(metrics, "ÚLTIMO CAMBIO", "0%", MUTED);
        lowValue = miniMetric(metrics, "STOCK ACTUAL", "0", NAVY);
        highValue = miniMetric(metrics, "PRECIO META", "$0", BLUE);
        container.add(metrics, BorderLayout.CENTER);
        return container;
    }

    private JLabel miniMetric(JPanel parent, String title, String value, Color accent) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(2, 8, 2, 8));
        JLabel caption = new JLabel(title);
        caption.setFont(new Font("Segoe UI", Font.BOLD, 10));
        caption.setForeground(MUTED);
        JLabel number = new JLabel(value);
        number.setFont(new Font("Segoe UI", Font.BOLD, 17));
        number.setForeground(accent);
        panel.add(caption);
        panel.add(Box.createVerticalStrut(5));
        panel.add(number);
        parent.add(panel);
        return number;
    }

    private JPanel createChartCard() {
        JPanel panel = card(new BorderLayout(0, 8));
        JPanel heading = new JPanel(new BorderLayout());
        heading.setOpaque(false);
        heading.add(title("EVOLUCIÓN DEL PRECIO DE VENTA"), BorderLayout.WEST);
        JLabel legend = new JLabel("Cada punto representa un ajuste registrado");
        legend.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        legend.setForeground(MUTED);
        heading.add(legend, BorderLayout.EAST);
        panel.add(heading, BorderLayout.NORTH);

        JFreeChart chart = ChartFactory.createXYLineChart(null, null, null, new XYSeriesCollection(), PlotOrientation.VERTICAL, false, true, false);
        chart.setBackgroundPaint(CARD);
        XYPlot plot = chart.getXYPlot();
        plot.setBackgroundPaint(CARD);
        plot.setOutlinePaint(null);
        plot.setDomainGridlinesVisible(false);
        plot.setRangeGridlinePaint(new Color(232, 238, 243));
        DateAxis dateAxis = new DateAxis();
        dateAxis.setDateFormatOverride(new SimpleDateFormat("MMM yy", MX));
        dateAxis.setAxisLineVisible(false);
        dateAxis.setTickMarksVisible(false);
        dateAxis.setTickLabelPaint(MUTED);
        dateAxis.setTickLabelFont(new Font("Segoe UI", Font.PLAIN, 11));
        plot.setDomainAxis(dateAxis);
        NumberAxis valueAxis = (NumberAxis) plot.getRangeAxis();
        valueAxis.setNumberFormatOverride(money);
        valueAxis.setAxisLineVisible(false);
        valueAxis.setTickMarksVisible(false);
        valueAxis.setTickLabelPaint(MUTED);
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer(true, true);
        renderer.setSeriesPaint(0, BLUE);
        renderer.setSeriesStroke(0, new BasicStroke(2.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        renderer.setSeriesShape(0, new Ellipse2D.Double(-4, -4, 8, 8));
        renderer.setSeriesShapesFilled(0, true);
        plot.setRenderer(renderer);
        priceChart = new ChartPanel(chart);
        priceChart.setBorder(null);
        priceChart.setMouseWheelEnabled(true);
        priceChart.setPreferredSize(new Dimension(650, 285));
        panel.add(priceChart, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createRecommendationCard() {
        JPanel panel = card(new BorderLayout(0, 12));
        panel.add(title("LECTURA COMERCIAL"), BorderLayout.NORTH);
        recommendation = new JLabel("<html>Selecciona un producto para evaluar su precio y margen.</html>");
        recommendation.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        recommendation.setForeground(TEXT);
        recommendation.setVerticalAlignment(SwingConstants.TOP);
        panel.add(recommendation, BorderLayout.CENTER);
        JPanel note = new JPanel(new BorderLayout());
        note.setBackground(new Color(247, 249, 251));
        note.setBorder(BorderFactory.createCompoundBorder(new LineBorder(BORDER), new EmptyBorder(9, 10, 9, 10)));
        recordCount = new JLabel("0 movimientos analizados");
        recordCount.setFont(LABEL);
        recordCount.setForeground(MUTED);
        note.add(recordCount, BorderLayout.CENTER);
        panel.add(note, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createHistoryCard() {
        JPanel panel = card(new BorderLayout(0, 8));
        panel.add(title("HISTORIAL DE CAMBIOS"), BorderLayout.NORTH);
        historyModel = new DefaultTableModel(new String[]{"FECHA", "COSTO", "VENTA", "MARGEN", "CAMBIO", "MOVIMIENTO"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable table = new JTable(historyModel);
        table.setFont(BODY);
        table.setRowHeight(32);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.getTableHeader().setFont(LABEL);
        table.getTableHeader().setForeground(MUTED);
        table.getTableHeader().setBackground(new Color(248, 250, 252));
        table.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER));
        table.setDefaultRenderer(Object.class, new HistoryRenderer());
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(CARD);
        scroll.setPreferredSize(new Dimension(800, 205));
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private JPanel card(LayoutManager layout) {
        JPanel panel = new JPanel(layout);
        panel.setBackground(CARD);
        panel.setBorder(BorderFactory.createCompoundBorder(new LineBorder(BORDER), new EmptyBorder(15, 16, 15, 16)));
        return panel;
    }

    private JLabel title(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        label.setForeground(NAVY);
        return label;
    }

    public void setHistory(List<ProductPriceHistory> source, String productId, Double priceBuy, Double priceSell) {
        setHistory(source, productId, priceBuy, priceSell, null, null);
    }

    public void setHistory(List<ProductPriceHistory> source, String productId, Double priceBuy, Double priceSell, String name, String sku) {
        productName.setText(fallback(name, "Producto"));
        productSku.setText("Referencia: " + fallback(sku, "Sin referencia"));
        double currentSale = safeNumber(priceSell);
        double currentCost = safeNumber(priceBuy);
        saleValue.setText(money.format(currentSale));
        costValue.setText(money.format(currentCost));
        double currentMargin = margin(currentCost, currentSale);
        marginValue.setText(percent(currentMargin));
        marginValue.setForeground(marginColor(currentMargin));

        List<ProductPriceHistory> history = source == null ? new ArrayList<>() : new ArrayList<>(source);
        history.removeIf(item -> item.getDateNew() == null);
        history.sort(Comparator.comparing(ProductPriceHistory::getDateNew));
        historyModel.setRowCount(0);
        XYSeries series = new XYSeries("Precio de venta");
        double minimum = currentSale;
        double maximum = currentSale;
        double latestPercent = 0;
        for (int index = 0; index < history.size(); index++) {
            ProductPriceHistory item = history.get(index);
            double sale = safeNumber(item.getPriceSell());
            double buy = safeNumber(item.getPriceBuy());
            if (index == 0 && currentSale == 0) { minimum = sale; maximum = sale; }
            minimum = Math.min(minimum, sale);
            maximum = Math.max(maximum, sale);
            double previous = index == 0 ? sale : safeNumber(history.get(index - 1).getPriceSell());
            double difference = sale - previous;
            double variation = previous > 0 ? difference / previous * 100d : 0;
            latestPercent = variation;
            series.add(item.getDateNew().getTime(), sale);
            historyModel.insertRow(0, new Object[]{dateFormat.format(item.getDateNew()), money.format(buy), money.format(sale),
                percent(margin(buy, sale)), signedPercent(variation), movementLabel(variation)});
        }
        if (history.isEmpty() && currentSale > 0) series.add(System.currentTimeMillis(), currentSale);
        lowValue.setText(money.format(minimum));
        highValue.setText(money.format(maximum));
        changeValue.setText(signedPercent(latestPercent));
        changeValue.setForeground(latestPercent > 0 ? RED : latestPercent < 0 ? GREEN : MUTED);
        priceChart.getChart().getXYPlot().setDataset(new XYSeriesCollection(series));
        recordCount.setText(history.size() + (history.size() == 1 ? " movimiento analizado" : " movimientos analizados"));
        recommendation.setText(buildRecommendation(currentMargin, latestPercent, history.size()));
    }

    /** Añade el contexto operativo que necesita la tienda para tomar una decisión. */
    public void setBusinessContext(String category, double stock, double targetMargin, double suggestedPrice, String reason) {
        productSku.setText(productSku.getText() + "   ·   " + fallback(category, "Sin categoría"));
        lowValue.setText(Math.abs(stock - Math.rint(stock)) < .001
                ? String.valueOf((int) Math.rint(stock)) : String.format(MX, "%.1f", stock));
        lowValue.setForeground(stock > 0 ? NAVY : AMBER);
        highValue.setText(suggestedPrice > 0 ? money.format(suggestedPrice) : "—");
        highValue.setForeground(BLUE);
        recommendation.setText("<html><div style='width:260px;line-height:1.55'>"
                + "<b style='color:#2563EB'>DECISIÓN SUGERIDA</b><br><br>" + reason
                + "<br><br><span style='color:#696F73'>La meta seleccionada es "
                + Math.round(targetMargin * 100) + "% de margen bruto. Valida impuestos, comisión y descuentos antes de cambiar el precio.</span>"
                + "</div></html>");
    }

    private String buildRecommendation(double margin, double variation, int records) {
        String marginMessage;
        if (margin < 0) marginMessage = "<b style='color:#BE3939'>El producto se vende por debajo de su costo.</b> Corrige el precio antes de venderlo.";
        else if (margin < 20) marginMessage = "<b style='color:#B77A16'>El margen es bajo (" + percent(margin) + ").</b> Revisa gastos, comisión y descuentos permitidos.";
        else marginMessage = "<b style='color:#1A7F54'>El margen actual es saludable (" + percent(margin) + ").</b>";
        String trendMessage;
        if (records < 2) trendMessage = "Aún faltan cambios para identificar una tendencia confiable.";
        else if (variation > 2) trendMessage = "El último precio subió " + signedPercent(variation) + ". Verifica que el precio de venta conserve la utilidad esperada.";
        else if (variation < -2) trendMessage = "El último precio bajó " + signedPercent(variation) + ". Puedes conservar margen o preparar una promoción.";
        else trendMessage = "El precio se mantiene estable en el último ajuste.";
        return "<html><div style='width:260px;line-height:1.5'>" + marginMessage + "<br><br>" + trendMessage + "</div></html>";
    }

    public void clearHistory() {
        productName.setText("Sin datos");
        productSku.setText("Referencia: —");
        saleValue.setText("$0");
        costValue.setText("$0");
        marginValue.setText("0%");
        changeValue.setText("0%");
        lowValue.setText("$0");
        highValue.setText("$0");
        historyModel.setRowCount(0);
        priceChart.getChart().getXYPlot().setDataset(new XYSeriesCollection());
        recordCount.setText("0 movimientos analizados");
        recommendation.setText("<html>Este producto todavía no cuenta con historial de cambios.</html>");
    }

    public void exportToCSV(Component parent) {
        if (historyModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(parent, "No hay movimientos para exportar.", "Sin datos", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Exportar historial de precios");
        chooser.setSelectedFile(new File("historial-precios.csv"));
        chooser.setFileFilter(new FileNameExtensionFilter("Archivo CSV (*.csv)", "csv"));
        if (chooser.showSaveDialog(parent) != JFileChooser.APPROVE_OPTION) return;
        File file = chooser.getSelectedFile();
        if (!file.getName().toLowerCase(Locale.ROOT).endsWith(".csv")) file = new File(file.getParentFile(), file.getName() + ".csv");
        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            for (int column = 0; column < historyModel.getColumnCount(); column++) {
                if (column > 0) writer.print(',');
                writer.print(csv(historyModel.getColumnName(column)));
            }
            writer.println();
            for (int row = 0; row < historyModel.getRowCount(); row++) {
                for (int column = 0; column < historyModel.getColumnCount(); column++) {
                    if (column > 0) writer.print(',');
                    writer.print(csv(String.valueOf(historyModel.getValueAt(row, column))));
                }
                writer.println();
            }
            JOptionPane.showMessageDialog(parent, "Historial exportado correctamente.", "Exportación completa", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(parent, "No se pudo exportar: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static String csv(String value) { return "\"" + value.replace("\"", "\"\"") + "\""; }
    private static String fallback(String value, String alternative) { return value == null || value.trim().isEmpty() ? alternative : value; }
    private static double safeNumber(Double value) { return value == null ? 0d : value; }
    private static double margin(double cost, double sale) { return sale <= 0 ? 0 : (sale - cost) / sale * 100d; }
    private static String percent(double value) { return String.format(MX, "%.1f%%", value); }
    private static String signedPercent(double value) { return (value > .001 ? "+" : "") + percent(value); }
    private static String movementLabel(double value) { return value > .001 ? "Aumento" : value < -.001 ? "Reducción" : "Sin cambio"; }
    private static Color marginColor(double value) { return value < 0 ? RED : value < 20 ? AMBER : GREEN; }

    private final class HistoryRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean selected, boolean focus, int row, int column) {
            Component component = super.getTableCellRendererComponent(table, value, selected, focus, row, column);
            if (!selected) component.setBackground(row % 2 == 0 ? CARD : new Color(249, 251, 253));
            setBorder(new EmptyBorder(0, 9, 0, 9));
            setFont(column >= 3 ? LABEL : BODY);
            setForeground(selected ? Color.WHITE : TEXT);
            if (!selected && (column == 4 || column == 5)) {
                String text = String.valueOf(value);
                setForeground(text.startsWith("+") || "Aumento".equals(text) ? RED : text.startsWith("-") || "Reducción".equals(text) ? GREEN : MUTED);
            }
            return component;
        }
    }
}

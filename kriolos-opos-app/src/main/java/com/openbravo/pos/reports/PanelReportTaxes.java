package com.openbravo.pos.reports;

import com.openbravo.basic.BasicException;
import com.openbravo.pos.forms.AppView;
import com.openbravo.pos.forms.BeanFactoryException;
import com.openbravo.format.Formats;
import com.openbravo.data.user.EditorCreator;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

public class PanelReportTaxes extends PanelReportBean {

    private javax.swing.JTabbedPane m_TabbedPane;
    private JPanel m_jPanelDashboard;

    private JLabel m_lblTotalBase;
    private JLabel m_lblTotalTaxes;
    private JLabel m_lblTotalCombined;
    private JLabel m_lblTaxRate;

    private ChartPanel m_barChartPanel;
    private ChartPanel m_pieChartPanel;

    @Override
    public void init(AppView app) throws BeanFactoryException {
        super.init(app);

        // Remove the standard reportviewer from BorderLayout.CENTER
        if (reportviewer != null) {
            remove(reportviewer);
        }

        // Setup JTabbedPane
        m_TabbedPane = new javax.swing.JTabbedPane();
        m_TabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 13));
        m_TabbedPane.setBackground(JPanelReport.DASH_BG);
        m_TabbedPane.setForeground(JPanelReport.DASH_TEXT_PRI);

        // Tab 1: Dashboard premium oscuro
        m_jPanelDashboard = createDashboardPanel();
        m_TabbedPane.addTab("Gráfico de Resumen", m_jPanelDashboard);

        // Tab 2: Print View
        if (reportviewer != null) {
            m_TabbedPane.addTab("Documento para Impresión", reportviewer);
        }

        add(m_TabbedPane, BorderLayout.CENTER);
    }

    private JPanel createDashboardPanel() {
        // Outer wrapper: fondo oscuro slate-900
        JPanel wrapper = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(JPanelReport.DASH_BG);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        wrapper.setOpaque(false);
        wrapper.setBorder(new EmptyBorder(20, 20, 20, 20));

        // === FILA SUPERIOR: 4 tarjetas KPI ===
        JPanel kpiRow = new JPanel(new GridLayout(1, 4, 16, 0));
        kpiRow.setOpaque(false);

        m_lblTotalBase      = new JLabel("$0.00");
        m_lblTotalTaxes     = new JLabel("$0.00");
        m_lblTotalCombined  = new JLabel("$0.00");
        m_lblTaxRate        = new JLabel("0.0%");

        JLabel titleBase     = new JLabel("BASE IMPONIBLE");
        JLabel titleTaxes    = new JLabel("TOTAL IMPUESTOS");
        JLabel titleCombined = new JLabel("RECAUDACIÓN TOTAL");
        JLabel titleRate     = new JLabel("TASA EFECTIVA PROMEDIO");

        kpiRow.add(createPremiumKpiCard(titleBase,     m_lblTotalBase,     JPanelReport.KPI_PURPLE, "🏦"));
        kpiRow.add(createPremiumKpiCard(titleTaxes,    m_lblTotalTaxes,    JPanelReport.KPI_BLUE,   "💲"));
        kpiRow.add(createPremiumKpiCard(titleCombined, m_lblTotalCombined, JPanelReport.KPI_GREEN,  "💰"));
        kpiRow.add(createPremiumKpiCard(titleRate,     m_lblTaxRate,       JPanelReport.KPI_AMBER,  "📊"));

        wrapper.add(kpiRow, BorderLayout.NORTH);

        // === FILA INFERIOR: Bar chart + Pie chart ===
        JPanel chartsRow = new JPanel(new BorderLayout(16, 0));
        chartsRow.setOpaque(false);
        chartsRow.setBorder(new EmptyBorder(16, 0, 0, 0));

        JFreeChart emptyBar = buildBarChart(new DefaultCategoryDataset(), "Impuestos por Categoría");
        m_barChartPanel = new ChartPanel(emptyBar);
        m_barChartPanel.setOpaque(false);
        styleChartPanel(m_barChartPanel);
        chartsRow.add(m_barChartPanel, BorderLayout.CENTER);

        JFreeChart emptyPie = buildPieChart(new DefaultPieDataset(), "Proporción");
        m_pieChartPanel = new ChartPanel(emptyPie);
        m_pieChartPanel.setOpaque(false);
        m_pieChartPanel.setPreferredSize(new Dimension(320, 300));
        styleChartPanel(m_pieChartPanel);
        chartsRow.add(m_pieChartPanel, BorderLayout.EAST);

        wrapper.add(chartsRow, BorderLayout.CENTER);
        return wrapper;
    }

    /** Tarjeta KPI oscura con acento de color y emoji */
    private JPanel createPremiumKpiCard(JLabel titleLabel, JLabel valueLabel, Color accentColor, String emoji) {
        JPanel card = new JPanel(new BorderLayout(0, 6)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(JPanelReport.DASH_CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(accentColor);
                g2.fillRoundRect(0, 0, 4, getHeight(), 4, 4);
                g2.setColor(JPanelReport.DASH_CARD_BORDER);
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 12, 12);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(16, 20, 16, 16));

        JLabel emojiLabel = new JLabel(emoji + "  ");
        emojiLabel.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        emojiLabel.setForeground(accentColor);

        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 10));
        titleLabel.setForeground(JPanelReport.DASH_TEXT_MUT);

        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);
        topRow.add(emojiLabel, BorderLayout.WEST);
        topRow.add(titleLabel, BorderLayout.CENTER);
        card.add(topRow, BorderLayout.NORTH);

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        valueLabel.setForeground(JPanelReport.DASH_TEXT_PRI);
        valueLabel.setHorizontalAlignment(SwingConstants.LEFT);
        card.add(valueLabel, BorderLayout.CENTER);

        return card;
    }

    @Override
    public void activate() throws BasicException {
        super.activate();
        m_TabbedPane.setSelectedIndex(0); // switch to dashboard tab
    }

    @Override
    protected void launchreport() {
        // Run standard report launch
        super.launchreport();

        // Auto-switch a la pestaña de impresión
        if (m_TabbedPane != null && m_TabbedPane.getTabCount() > 1) {
            m_TabbedPane.setSelectedIndex(1);
        }

        // Fetch values for the dashboard
        try {
            EditorCreator editor = getEditorCreator();
            Object[] composedParams = (Object[]) editor.createValue();

            Date startDate = null;
            Date endDate = null;

            if (composedParams != null && composedParams.length > 0 && composedParams[0] instanceof Object[]) {
                Object[] dateParams = (Object[]) composedParams[0];
                if (dateParams.length > 1) startDate = (Date) dateParams[1];
                if (dateParams.length > 3) endDate   = (Date) dateParams[3];
            }

            if (startDate == null) startDate = com.openbravo.beans.DateUtils.getToday();
            if (endDate   == null) endDate   = com.openbravo.beans.DateUtils.getTodayMinutes();

            fetchAndRenderTaxes(startDate, endDate);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void fetchAndRenderTaxes(Date startDate, Date endDate) {
        String sql = "SELECT taxcategories.NAME AS TAXNAME, " +
                     "SUM(taxlines.AMOUNT) AS TOTALTAXES, " +
                     "SUM(taxlines.BASE) AS TOTALBASE " +
                     "FROM receipts, taxlines, taxes, taxcategories " +
                     "WHERE receipts.ID = taxlines.RECEIPT AND taxlines.TAXID = taxes.ID AND taxes.CATEGORY = taxcategories.ID " +
                     "AND receipts.DATENEW >= ? AND receipts.DATENEW < ? " +
                     "GROUP BY taxcategories.ID, taxcategories.NAME";

        double totalBase = 0.0;
        double totalTaxes = 0.0;

        List<TaxRecord> records = new ArrayList<>();

        try (Connection conn = m_App.getSession().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setTimestamp(1, new java.sql.Timestamp(startDate.getTime()));
            ps.setTimestamp(2, new java.sql.Timestamp(endDate.getTime()));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String name = rs.getString("TAXNAME");
                    double base = rs.getDouble("TOTALBASE");
                    double taxes = rs.getDouble("TOTALTAXES");
                    totalBase += base;
                    totalTaxes += taxes;
                    records.add(new TaxRecord(name, base, taxes));
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        // Update KPIs
        m_lblTotalBase.setText(Formats.CURRENCY.formatValue(totalBase));
        m_lblTotalTaxes.setText(Formats.CURRENCY.formatValue(totalTaxes));
        m_lblTotalCombined.setText(Formats.CURRENCY.formatValue(totalBase + totalTaxes));

        double rate = totalBase > 0 ? (totalTaxes / totalBase) * 100.0 : 0.0;
        m_lblTaxRate.setText(String.format("%.2f%%", rate));

        // Render Charts
        DefaultCategoryDataset barDataset = new DefaultCategoryDataset();
        DefaultPieDataset      pieDataset = new DefaultPieDataset();

        for (TaxRecord r : records) {
            String lbl = r.name.length() > 20 ? r.name.substring(0, 18) + "..." : r.name;
            barDataset.addValue(r.taxes, "Impuestos", lbl);
            barDataset.addValue(r.base,  "Base",      lbl);
            pieDataset.setValue(lbl, r.taxes);
        }

        if (m_barChartPanel != null) {
            JFreeChart barChart = buildBarChart(barDataset, "Impuestos por Categoría");
            m_barChartPanel.setChart(barChart);
        }
        if (m_pieChartPanel != null) {
            JFreeChart pieChart = buildPieChart(pieDataset, "Distribución de Impuestos");
            m_pieChartPanel.setChart(pieChart);
        }

        if (m_jPanelDashboard != null) {
            m_jPanelDashboard.revalidate();
            m_jPanelDashboard.repaint();
        }
    }

    /** Bar chart con estilo oscuro (reutiliza paleta de JPanelReport) */
    private JFreeChart buildBarChart(DefaultCategoryDataset dataset, String chartTitle) {
        JFreeChart chart = ChartFactory.createBarChart(
            chartTitle, null, null, dataset, PlotOrientation.HORIZONTAL, true, true, false);

        chart.setBackgroundPaint(JPanelReport.DASH_CARD);
        chart.setBorderVisible(false);
        if (chart.getTitle() != null) {
            chart.getTitle().setFont(new Font("Segoe UI", Font.BOLD, 14));
            chart.getTitle().setPaint(JPanelReport.DASH_TEXT_PRI);
        }
        if (chart.getLegend() != null) {
            chart.getLegend().setBackgroundPaint(JPanelReport.DASH_CARD);
            chart.getLegend().setItemFont(new Font("Segoe UI", Font.PLAIN, 10));
            chart.getLegend().setItemPaint(JPanelReport.DASH_TEXT_MUT);
        }

        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(JPanelReport.DASH_CARD);
        plot.setOutlineVisible(false);
        plot.setDomainGridlinesVisible(false);
        plot.setRangeGridlinePaint(JPanelReport.DASH_CARD_BORDER);
        plot.getDomainAxis().setTickLabelFont(new Font("Segoe UI", Font.PLAIN, 11));
        plot.getDomainAxis().setTickLabelPaint(JPanelReport.DASH_TEXT_MUT);
        plot.getDomainAxis().setAxisLinePaint(JPanelReport.DASH_CARD_BORDER);
        plot.getRangeAxis().setTickLabelFont(new Font("Segoe UI", Font.PLAIN, 10));
        plot.getRangeAxis().setTickLabelPaint(JPanelReport.DASH_TEXT_MUT);
        plot.getRangeAxis().setAxisLinePaint(JPanelReport.DASH_CARD_BORDER);

        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setBarPainter(new StandardBarPainter());
        renderer.setShadowVisible(false);
        renderer.setMaximumBarWidth(0.5);
        renderer.setSeriesPaint(0, JPanelReport.KPI_BLUE);
        renderer.setSeriesPaint(1, JPanelReport.KPI_PURPLE);

        return chart;
    }

    /** Pie chart con estilo oscuro */
    private JFreeChart buildPieChart(DefaultPieDataset dataset, String chartTitle) {
        JFreeChart chart = ChartFactory.createPieChart(chartTitle, dataset, true, true, false);
        chart.setBackgroundPaint(JPanelReport.DASH_CARD);
        chart.setBorderVisible(false);
        if (chart.getTitle() != null) {
            chart.getTitle().setFont(new Font("Segoe UI", Font.BOLD, 14));
            chart.getTitle().setPaint(JPanelReport.DASH_TEXT_PRI);
        }
        if (chart.getLegend() != null) {
            chart.getLegend().setBackgroundPaint(JPanelReport.DASH_CARD);
            chart.getLegend().setItemFont(new Font("Segoe UI", Font.PLAIN, 10));
            chart.getLegend().setItemPaint(JPanelReport.DASH_TEXT_MUT);
        }
        PiePlot plot = (PiePlot) chart.getPlot();
        plot.setBackgroundPaint(JPanelReport.DASH_CARD);
        plot.setOutlineVisible(false);
        plot.setShadowPaint(null);
        plot.setLabelFont(new Font("Segoe UI", Font.PLAIN, 10));
        plot.setLabelPaint(JPanelReport.DASH_TEXT_MUT);
        plot.setLabelBackgroundPaint(JPanelReport.DASH_CARD);
        plot.setLabelOutlinePaint(null);
        plot.setLabelShadowPaint(null);
        int si = 0;
        for (Object key : dataset.getKeys()) {
            plot.setSectionPaint((Comparable<?>) key,
                JPanelReport.CHART_COLORS[si % JPanelReport.CHART_COLORS.length]);
            si++;
        }
        return chart;
    }

    private void styleChartPanel(ChartPanel cp) {
        cp.setBackground(JPanelReport.DASH_CARD);
        cp.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(JPanelReport.DASH_CARD_BORDER, 1),
            BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
    }

    private static class TaxRecord {
        String name;
        double base;
        double taxes;

        TaxRecord(String name, double base, double taxes) {
            this.name = name;
            this.base = base;
            this.taxes = taxes;
        }
    }
}

//    KriolOS POS
//    Copyright (c) 2019-2023 KriolOS
//
//    This program is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.
//
//    This program is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with this program.  If not, see <http://www.gnu.org/licenses/>.
package com.openbravo.pos.reports;

import com.openbravo.pos.forms.DataLogicSales;
import com.openbravo.pos.forms.AppView;
import com.openbravo.pos.forms.BeanFactoryApp;
import com.openbravo.pos.forms.BeanFactoryException;
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.basic.BasicException;
import com.openbravo.data.gui.MessageInf;
import com.openbravo.data.loader.BaseSentence;
import com.openbravo.data.loader.SentenceList;
import com.openbravo.data.loader.Session;
import com.openbravo.data.user.EditorCreator;
import com.openbravo.pos.forms.*;
import com.openbravo.pos.sales.TaxesLogic;
import com.openbravo.format.Formats;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.xml.JRXmlLoader;
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

/**
 *
 * @author JG uniCenta
 */
public abstract class JPanelReport extends JPanel implements JPanelView, BeanFactoryApp {

    private static final Logger LOGGER = Logger.getLogger(JPanelReport.class.getName());
    protected JRViewer400 reportviewer = null;
    private EditorCreator editor = null;
    protected AppView m_App;
    private Session s;
    private Connection con;
    protected SentenceList taxsent;
    protected TaxesLogic taxeslogic;

    // Sebastian - Palette de colores del Dashboard claro
    protected static final Color DASH_BG         = new Color(241, 245, 249);    // slate-100 (gris claro)
    protected static final Color DASH_CARD        = new Color(255, 255, 255);    // blanco
    protected static final Color DASH_CARD_BORDER = new Color(226, 232, 240);    // slate-200
    protected static final Color DASH_TEXT_PRI    = new Color(15, 23, 42);       // slate-900 (texto principal)
    protected static final Color DASH_TEXT_MUT    = new Color(71, 85, 105);      // slate-600 (texto secundario)
    protected static final Color KPI_PURPLE       = new Color(139, 92, 246);     // violet-500
    protected static final Color KPI_GREEN        = new Color(16, 185, 129);     // emerald-500
    protected static final Color KPI_AMBER        = new Color(245, 158, 11);     // amber-500
    protected static final Color KPI_BLUE         = new Color(59, 130, 246);     // blue-500
    protected static final Color[] CHART_COLORS   = {
        new Color(139, 92, 246), new Color(16, 185, 129), new Color(245, 158, 11),
        new Color(59, 130, 246), new Color(236, 72, 153), new Color(20, 184, 166),
        new Color(249, 115, 22)
    };

    // Sebastian - JTabbedPane y componentes del Dashboard genérico
    protected javax.swing.JTabbedPane m_TabbedPane;
    protected javax.swing.JPanel m_jPanelDashboard;
    protected javax.swing.JPanel m_chartContainer;
    protected javax.swing.JLabel m_lblKpi1;
    protected javax.swing.JLabel m_lblKpi2;
    protected javax.swing.JLabel m_lblKpi3;
    protected javax.swing.JLabel m_lblKpi4;
    protected javax.swing.JLabel m_lblKpi1Title;
    protected javax.swing.JLabel m_lblKpi2Title;
    protected javax.swing.JLabel m_lblKpi3Title;
    protected javax.swing.JLabel m_lblKpi4Title;
    private ChartPanel m_barChartPanel;
    private ChartPanel m_pieChartPanel;

    /**
     * Creates new form JPanelReport
     */
    public JPanelReport() {

        initComponents();
        
        jButton1.putClientProperty("JButton.buttonType", "accent");
        jButton1.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jToggleFilter.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    }

    /**
     *
     * @param app
     * @throws BeanFactoryException
     */
    @Override
    public void init(AppView app) throws BeanFactoryException {

        m_App = app;

        DataLogicSales dlSales = (DataLogicSales) app.getBean("com.openbravo.pos.forms.DataLogicSales");
        taxsent = dlSales.getTaxList();

        editor = getEditorCreator();
        if (editor instanceof ReportEditorCreator) {
            jPanelFilter.add(((ReportEditorCreator) editor).getComponent(), BorderLayout.CENTER);
        }

        reportviewer = new JRViewer400(null);

        // Sebastian - Cargar el dashboard interactivo si corresponde
        if (useGenericDashboard()) {
            setupGenericDashboard();
        } else {
            add(reportviewer, BorderLayout.CENTER);
        }
    }

    public static JasperReport createJasperReport(String reportFilename) throws JRException {

        JasperReport jasperReport = null;

        if (reportFilename != null) {
            String fullName = reportFilename + ".ser";

            try (InputStream in = JPanelReport.class.getResourceAsStream(fullName)) {
                if (in != null) {
                    try (ObjectInputStream oin = new ObjectInputStream(in)) {
                        jasperReport = (JasperReport) oin.readObject();
                    }
                }
            } catch (IOException | ClassNotFoundException ex) {
                LOGGER.log(Level.WARNING, "Exception load report file(.ser): " + fullName, ex);
            }
        }

        if (jasperReport == null && reportFilename != null) {
            String fullName = reportFilename + ".jrxml";
            try (InputStream in = JPanelReport.class.getResourceAsStream(fullName)) {
                if (in != null) {
                    //JasperDesign jd = JRXmlLoader.load(in);
                    jasperReport = JasperCompileManager.compileReport(in);
                }
            } catch (IOException ex) {
                LOGGER.log(Level.WARNING, "Exception load report file(.jrxml): " + fullName, ex);
            }
        }

        if (jasperReport == null) {
            LOGGER.log(Level.WARNING, "Cannot create JasperReport, because reportFilename is null");
        }

        return jasperReport;
    }

    /**
     *
     * @return
     */
    @Override
    public Object getBean() {
        return this;
    }

    /**
     *
     * @return
     */
    protected abstract String getReport();

    /**
     *
     * @return
     */
    protected abstract String getResourceBundle();

    /**
     *
     * @return
     */
    protected abstract BaseSentence getSentence();

    /**
     *
     * @return
     */
    protected abstract ReportFields getReportFields();

    /**
     *
     * @return
     */
    protected EditorCreator getEditorCreator() {
        return null;
    }

    /**
     *
     * @return
     */
    @Override
    public JComponent getComponent() {
        return this;
    }

    /**
     *
     * @throws BasicException
     */
    @Override
    public void activate() throws BasicException {

        setVisibleFilter(true);
        taxeslogic = new TaxesLogic(taxsent.list());

        // Sebastian - Reiniciar tab al activar - mostrar Dashboard por defecto
        if (useGenericDashboard() && m_TabbedPane != null) {
            m_TabbedPane.setSelectedIndex(0);
        }
    }

    /**
     *
     * @return
     */
    @Override
    public boolean deactivate() {

        reportviewer.loadJasperPrint(null);
        return true;
    }

    /**
     *
     * @param value
     */
    protected void setVisibleButtonFilter(boolean value) {
        jToggleFilter.setVisible(value);
    }

    /**
     *
     * @param value
     */
    protected void setVisibleFilter(boolean value) {
        jToggleFilter.setSelected(value);
        jToggleFilterActionPerformed(null);
    }

    protected void launchreport() {

        m_App.waitCursorBegin();

        String reportFilename = getReport();
        LOGGER.log(Level.INFO, "Launch report file: "+reportFilename);
        try {

            JasperReport jasperReport = JPanelReport.createJasperReport(reportFilename);
            if (jasperReport != null) {

                //RESOURCE FILE
                String res = getResourceBundle();

                //GET PARAMETER AND DATA
                Object params = (editor == null) ? null : editor.createValue();
                JRDataSource data = new JRDataSourceBasic(getSentence(), getReportFields(), params);

                //PARAMETERS
                Map<String, Object> reportparams = new HashMap<>();
                reportparams.put("ARG", params);
                if (res != null) {
                    reportparams.put("REPORT_RESOURCE_BUNDLE", ResourceBundle.getBundle(res));
                }
                reportparams.put("TAXESLOGIC", taxeslogic);

                JasperPrint jp = JasperFillManager.fillReport(jasperReport, reportparams, data);

                if (jp == null || jp.getPages() == null || jp.getPages().isEmpty()) {
                    MessageInf.showDialogWarn(this, "No hay ventas o datos disponibles para los criterios y filtros seleccionados.", null);
                    if (useGenericDashboard()) {
                        updateSwingDashboardFromQuery(params);
                    }
                } else {
                    reportviewer.loadJasperPrint(jp);
                    setVisibleFilter(false);

                    // Sebastian - Actualizar Dashboard y cambiar a pestaña de impresión
                    if (useGenericDashboard()) {
                        updateSwingDashboardFromQuery(params);
                        // Auto-cambiar a pestaña de impresión
                        if (m_TabbedPane != null) {
                            m_TabbedPane.setSelectedIndex(1);
                        }
                    }
                }
            }

        } catch (MissingResourceException | JRException |BasicException ex) {
            LOGGER.log(Level.SEVERE, "Exception lauch report file: "+reportFilename, ex);
            MessageInf.showDialogWarn(this, "<html>"+AppLocal.getIntString("message.cannotloadreportdata") + "<br>"+reportFilename, ex);
        } finally {
            m_App.waitCursorEnd();
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanelHeader = new javax.swing.JPanel();
        jPanelFilter = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        jToggleFilter = new javax.swing.JToggleButton();
        jButton1 = new javax.swing.JButton();

        setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        setLayout(new java.awt.BorderLayout());

        jPanelHeader.setLayout(new java.awt.BorderLayout());

        jPanelFilter.setLayout(new java.awt.BorderLayout());
        jPanelHeader.add(jPanelFilter, java.awt.BorderLayout.CENTER);

        jPanel1.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        jToggleFilter.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/1downarrow.png"))); // NOI18N
        jToggleFilter.setSelected(true);
        jToggleFilter.setToolTipText("Hide/Show Filter");
        jToggleFilter.setPreferredSize(new java.awt.Dimension(80, 45));
        jToggleFilter.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/1uparrow.png"))); // NOI18N
        jToggleFilter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleFilterActionPerformed(evt);
            }
        });
        jPanel1.add(jToggleFilter);

        jButton1.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        jButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/ok.png"))); // NOI18N
        jButton1.setText(AppLocal.getIntString("button.executereport")); // NOI18N
        jButton1.setToolTipText("Execute Report");
        jButton1.setPreferredSize(new java.awt.Dimension(150, 45));
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        jPanel1.add(jButton1);

        jPanelHeader.add(jPanel1, java.awt.BorderLayout.SOUTH);

        add(jPanelHeader, java.awt.BorderLayout.NORTH);
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed

        launchreport();

    }//GEN-LAST:event_jButton1ActionPerformed

    private void jToggleFilterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleFilterActionPerformed

        jPanelFilter.setVisible(jToggleFilter.isSelected());

    }//GEN-LAST:event_jToggleFilterActionPerformed

    // Sebastian - Declaración para obtener los campos de reportes
    public java.util.List<String> getFieldNames() {
        return null;
    }

    // Sebastian - Métodos auxiliares para el Dashboard integrado en Swing (estilo Impuestos)
    protected boolean useGenericDashboard() {
        // Habilitar dashboard genérico para todos los reportes excepto PanelReportTaxes (que ya tiene el suyo propio)
        return !this.getClass().getName().contains("PanelReportTaxes");
    }

    private void setupGenericDashboard() {
        m_TabbedPane = new javax.swing.JTabbedPane();
        m_TabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 13));
        m_TabbedPane.setBackground(DASH_BG);
        m_TabbedPane.setForeground(DASH_TEXT_PRI);

        // Tab 1: Dashboard premium oscuro
        m_jPanelDashboard = createGenericDashboardPanel();
        m_TabbedPane.addTab("Gráfico de Resumen", m_jPanelDashboard);

        // Tab 2: Visor del Reporte clásico
        m_TabbedPane.addTab("Documento para Impresión", reportviewer);

        add(m_TabbedPane, BorderLayout.CENTER);
    }

    private JPanel createGenericDashboardPanel() {
        // Outer wrapper: fondo oscuro slate-900
        JPanel wrapper = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(DASH_BG);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        wrapper.setOpaque(false);
        wrapper.setBorder(new EmptyBorder(20, 20, 20, 20));

        // === FILA SUPERIOR: 4 tarjetas KPI ===
        JPanel kpiRow = new JPanel(new GridLayout(1, 4, 16, 0));
        kpiRow.setOpaque(false);

        m_lblKpi1Title = new JLabel("VENTAS TOTALES");
        m_lblKpi1      = new JLabel("$0.00");
        m_lblKpi2Title = new JLabel("ARTÍCULOS VENDIDOS");
        m_lblKpi2      = new JLabel("0");
        m_lblKpi3Title = new JLabel("TRANSACCIONES");
        m_lblKpi3      = new JLabel("0");
        m_lblKpi4Title = new JLabel("TICKET PROMEDIO");
        m_lblKpi4      = new JLabel("$0.00");

        kpiRow.add(createPremiumKpiCard(m_lblKpi1Title, m_lblKpi1, KPI_PURPLE, "💵"));
        kpiRow.add(createPremiumKpiCard(m_lblKpi2Title, m_lblKpi2, KPI_GREEN,  "📦"));
        kpiRow.add(createPremiumKpiCard(m_lblKpi3Title, m_lblKpi3, KPI_AMBER,  "🎫"));
        kpiRow.add(createPremiumKpiCard(m_lblKpi4Title, m_lblKpi4, KPI_BLUE,   "📊"));

        wrapper.add(kpiRow, BorderLayout.NORTH);

        // === FILA INFERIOR: Bar chart + Pie chart ===
        JPanel chartsRow = new JPanel(new BorderLayout(16, 0));
        chartsRow.setOpaque(false);
        chartsRow.setBorder(new EmptyBorder(16, 0, 0, 0));

        // Bar chart placeholder (se llenará en updateSwingDashboard)
        JFreeChart emptyBar = buildBarChart(new DefaultCategoryDataset(), "Distribución de Ventas");
        m_barChartPanel = new ChartPanel(emptyBar);
        m_barChartPanel.setOpaque(false);
        styleChartPanel(m_barChartPanel);
        chartsRow.add(m_barChartPanel, BorderLayout.CENTER);

        // Pie chart placeholder
        JFreeChart emptyPie = buildPieChart(new DefaultPieDataset(), "Proporción");
        m_pieChartPanel = new ChartPanel(emptyPie);
        m_pieChartPanel.setOpaque(false);
        m_pieChartPanel.setPreferredSize(new Dimension(320, 300));
        styleChartPanel(m_pieChartPanel);
        chartsRow.add(m_pieChartPanel, BorderLayout.EAST);

        // m_chartContainer (para el texto vacío / estado inicial)
        m_chartContainer = new JPanel();
        m_chartContainer.setLayout(new BoxLayout(m_chartContainer, BoxLayout.Y_AXIS));
        m_chartContainer.setOpaque(false);

        wrapper.add(chartsRow, BorderLayout.CENTER);
        return wrapper;
    }

    /** Tarjeta KPI oscura con acento de color y emoji */
    private JPanel createPremiumKpiCard(JLabel titleLabel, JLabel valueLabel, Color accentColor, String emoji) {
        JPanel card = new JPanel(new BorderLayout(0, 6)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Fondo de la tarjeta
                g2.setColor(DASH_CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                // Borde lateral coloreado (4px)
                g2.setColor(accentColor);
                g2.fillRoundRect(0, 0, 4, getHeight(), 4, 4);
                // Borde exterior suave
                g2.setColor(DASH_CARD_BORDER);
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 12, 12);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(16, 20, 16, 16));

        // Emoji + Título
        JLabel emojiLabel = new JLabel(emoji + "  ");
        emojiLabel.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        emojiLabel.setForeground(accentColor);

        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 10));
        titleLabel.setForeground(DASH_TEXT_MUT);

        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);
        topRow.add(emojiLabel, BorderLayout.WEST);
        topRow.add(titleLabel, BorderLayout.CENTER);
        card.add(topRow, BorderLayout.NORTH);

        // Valor grande
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        valueLabel.setForeground(DASH_TEXT_PRI);
        valueLabel.setHorizontalAlignment(SwingConstants.LEFT);
        card.add(valueLabel, BorderLayout.CENTER);

        return card;
    }

    private void updateSwingDashboardFromQuery(Object params) {
        try {
            BaseSentence sentence = getSentence();
            ReportFields fields = getReportFields();
            java.util.List<String> fieldNames = getFieldNames();
            
            if (fieldNames != null && !fieldNames.isEmpty()) {
                com.openbravo.data.loader.DataResultSet SRS = sentence.openExec((Object[]) params);
                java.util.List<java.util.Map<String, Object>> records = new java.util.ArrayList<>();
                while (SRS.next()) {
                    Object record = SRS.getCurrent();
                    java.util.Map<String, Object> recordMap = new java.util.HashMap<>();
                    for (String name : fieldNames) {
                        Object val = fields.getField(record, name);
                        recordMap.put(name, val);
                    }
                    records.add(recordMap);
                }
                sentence.closeExec();
                
                updateSwingDashboard(records, fieldNames);
            }
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "Error actualizando el Dashboard de Swing: " + ex.getMessage(), ex);
        }
    }

    private void updateSwingDashboard(List<Map<String, Object>> records, List<String> fieldNames) {
        if (records == null || records.isEmpty()) {
            // Dejar gráficas vacías
            return;
        }

        // Detección de claves de campos
        String totalKey = null, qtyKey = null, labelKey = null, ticketKey = null;
        for (String key : fieldNames) {
            String uk = key.toUpperCase();
            if (totalKey == null && (uk.equals("TOTAL") || uk.equals("CATPRICE") || uk.equals("PRICE") || uk.equals("CATTOTAL") || uk.equals("TOTAL_SALES")))
                totalKey = key;
            if (qtyKey == null && (uk.equals("UNITS") || uk.equals("QTY") || uk.equals("QUANTITY")))
                qtyKey = key;
            if (labelKey == null && (uk.equals("NAME") || uk.equals("PNAME") || uk.equals("CNAME") || uk.equals("CATEGORY_NAME") || uk.equals("CATNAME")))
                labelKey = key;
            if (ticketKey == null && (uk.equals("TICKETID") || uk.equals("TICKET") || uk.equals("RECEIPT")))
                ticketKey = key;
        }
        if (totalKey == null) {
            for (String key : fieldNames) {
                if (records.get(0).get(key) instanceof Number) { totalKey = key; break; }
            }
        }
        if (labelKey == null) {
            for (String key : fieldNames) {
                if (records.get(0).get(key) instanceof String) { labelKey = key; break; }
            }
        }

        double totalSales = 0.0, totalQty = 0.0;
        Set<Object> transactions = new HashSet<>();
        Map<String, Double> labelMap = new HashMap<>();

        for (Map<String, Object> record : records) {
            double total = 0.0;
            if (totalKey != null && record.get(totalKey) instanceof Number)
                total = ((Number) record.get(totalKey)).doubleValue();
            double qty = qtyKey != null && record.get(qtyKey) instanceof Number
                ? ((Number) record.get(qtyKey)).doubleValue() : 1.0;

            totalSales += total;
            totalQty   += qty;
            if (ticketKey != null && record.get(ticketKey) != null)
                transactions.add(record.get(ticketKey));
            if (labelKey != null && record.get(labelKey) != null) {
                String lbl = record.get(labelKey).toString();
                labelMap.put(lbl, labelMap.getOrDefault(lbl, 0.0) + total);
            }
        }

        int txCount = transactions.size() > 0 ? transactions.size() : records.size();
        double avgTicket = txCount > 0 ? totalSales / txCount : 0.0;

        // Personalización KPI según tipo de reporte
        String title = getTitle() != null ? getTitle().toUpperCase() : "";
        if (title.contains("STOCK") || title.contains("INVENTARIO")) {
            m_lblKpi1Title.setText("VALOR STOCK TOTAL");
            m_lblKpi2Title.setText("UNIDADES TOTALES");
            m_lblKpi3Title.setText("PRODUCTOS ÚNICOS");
            m_lblKpi4Title.setText("VALOR PROMEDIO");
            m_lblKpi3.setText(String.valueOf(labelMap.size()));
        } else {
            m_lblKpi1Title.setText("VENTAS TOTALES");
            m_lblKpi2Title.setText("ARTÍCULOS VENDIDOS");
            m_lblKpi3Title.setText("TRANSACCIONES");
            m_lblKpi4Title.setText("TICKET PROMEDIO");
            m_lblKpi3.setText(String.valueOf(txCount));
        }
        m_lblKpi1.setText(Formats.CURRENCY.formatValue(totalSales));
        m_lblKpi2.setText(Formats.DOUBLE.formatValue(totalQty));
        m_lblKpi4.setText(Formats.CURRENCY.formatValue(avgTicket));

        // Ordenar etiquetas por valor
        List<Map.Entry<String, Double>> sorted = new ArrayList<>(labelMap.entrySet());
        Collections.sort(sorted, (a, b) -> Double.compare(b.getValue(), a.getValue()));

        // Construir datasets para las gráficas
        DefaultCategoryDataset barDataset = new DefaultCategoryDataset();
        DefaultPieDataset      pieDataset = new DefaultPieDataset();
        int idx = 0;
        double otherTotal = 0;
        for (Map.Entry<String, Double> e : sorted) {
            if (idx < 7) {
                String lbl = e.getKey().length() > 20 ? e.getKey().substring(0, 18) + "..." : e.getKey();
                barDataset.addValue(e.getValue(), "Ventas", lbl);
                pieDataset.setValue(lbl, e.getValue());
            } else {
                otherTotal += e.getValue();
            }
            idx++;
        }
        if (otherTotal > 0) {
            barDataset.addValue(otherTotal, "Ventas", "Otros");
            pieDataset.setValue("Otros", otherTotal);
        }

        // Actualizar gráficas
        if (m_barChartPanel != null) {
            JFreeChart barChart = buildBarChart(barDataset, "Distribución de Ventas");
            m_barChartPanel.setChart(barChart);
        }
        if (m_pieChartPanel != null) {
            JFreeChart pieChart = buildPieChart(pieDataset, "Proporción");
            m_pieChartPanel.setChart(pieChart);
        }

        if (m_jPanelDashboard != null) {
            m_jPanelDashboard.revalidate();
            m_jPanelDashboard.repaint();
        }
    }

    private void showEmptyDashboardMessage() {
        // No-op: con las gráficas vacías ya se ve el estado inicial limpio
    }

    /** Construye un Bar Chart horizontal con estilo oscuro */
    private JFreeChart buildBarChart(DefaultCategoryDataset dataset, String chartTitle) {
        JFreeChart chart = ChartFactory.createBarChart(
            chartTitle, null, null, dataset, PlotOrientation.HORIZONTAL, false, true, false);

        chart.setBackgroundPaint(DASH_CARD);
        chart.setBorderVisible(false);
        if (chart.getTitle() != null) {
            chart.getTitle().setFont(new Font("Segoe UI", Font.BOLD, 14));
            chart.getTitle().setPaint(DASH_TEXT_PRI);
        }

        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(DASH_CARD);
        plot.setOutlineVisible(false);
        plot.setDomainGridlinesVisible(false);
        plot.setRangeGridlinePaint(DASH_CARD_BORDER);
        plot.getDomainAxis().setTickLabelFont(new Font("Segoe UI", Font.PLAIN, 11));
        plot.getDomainAxis().setTickLabelPaint(DASH_TEXT_MUT);
        plot.getDomainAxis().setAxisLinePaint(DASH_CARD_BORDER);
        plot.getRangeAxis().setTickLabelFont(new Font("Segoe UI", Font.PLAIN, 10));
        plot.getRangeAxis().setTickLabelPaint(DASH_TEXT_MUT);
        plot.getRangeAxis().setAxisLinePaint(DASH_CARD_BORDER);

        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setBarPainter(new StandardBarPainter());
        renderer.setShadowVisible(false);
        renderer.setMaximumBarWidth(0.6);
        for (int i = 0; i < CHART_COLORS.length; i++) {
            renderer.setSeriesPaint(i, CHART_COLORS[i % CHART_COLORS.length]);
        }
        // Colores por item
        int total = dataset.getColumnCount();
        for (int i = 0; i < total; i++) {
            renderer.setSeriesPaint(0, CHART_COLORS[i % CHART_COLORS.length]);
        }
        // Multi-color: cada fila de categoría obtiene su propio color
        renderer.setItemMargin(0.1);

        return chart;
    }

    /** Construye un Donut/Pie Chart con estilo oscuro */
    private JFreeChart buildPieChart(DefaultPieDataset dataset, String chartTitle) {
        JFreeChart chart = ChartFactory.createPieChart(chartTitle, dataset, true, true, false);
        chart.setBackgroundPaint(DASH_CARD);
        chart.setBorderVisible(false);
        if (chart.getTitle() != null) {
            chart.getTitle().setFont(new Font("Segoe UI", Font.BOLD, 14));
            chart.getTitle().setPaint(DASH_TEXT_PRI);
        }
        if (chart.getLegend() != null) {
            chart.getLegend().setBackgroundPaint(DASH_CARD);
            chart.getLegend().setItemFont(new Font("Segoe UI", Font.PLAIN, 10));
            chart.getLegend().setItemPaint(DASH_TEXT_MUT);
        }

        PiePlot plot = (PiePlot) chart.getPlot();
        plot.setBackgroundPaint(DASH_CARD);
        plot.setOutlineVisible(false);
        plot.setShadowPaint(null);
        plot.setLabelFont(new Font("Segoe UI", Font.PLAIN, 10));
        plot.setLabelPaint(DASH_TEXT_MUT);
        plot.setLabelBackgroundPaint(DASH_CARD);
        plot.setLabelOutlinePaint(null);
        plot.setLabelShadowPaint(null);
        // Asignar colores a cada sector
        int si = 0;
        for (Object key : dataset.getKeys()) {
            plot.setSectionPaint((Comparable<?>) key, CHART_COLORS[si % CHART_COLORS.length]);
            si++;
        }
        return chart;
    }

    /** Aplica fondo oscuro a un ChartPanel */
    private void styleChartPanel(ChartPanel cp) {
        cp.setBackground(DASH_CARD);
        cp.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(DASH_CARD_BORDER, 1),
            BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanelFilter;
    private javax.swing.JPanel jPanelHeader;
    private javax.swing.JToggleButton jToggleFilter;
    // End of variables declaration//GEN-END:variables

}

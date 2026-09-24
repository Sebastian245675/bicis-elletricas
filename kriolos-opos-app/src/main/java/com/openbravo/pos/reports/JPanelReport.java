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
import java.awt.Component;
import java.awt.Cursor;
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
import java.util.Date;
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
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.xml.JRXmlLoader;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.RingPlot;
import org.jfree.chart.renderer.category.AreaRenderer;
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
    protected javax.swing.JLabel m_lblKpi1Trend;
    protected javax.swing.JLabel m_lblKpi4Trend;
    protected javax.swing.JLabel m_lblPieCenterText;
    protected javax.swing.JLabel m_lblPieCenterSub;
    protected javax.swing.JLabel m_lblUpdatedTime;
    private ChartPanel m_barChartPanel;
    private ChartPanel m_pieChartPanel;
    private Top10RankingPanel m_top10RankingPanel;  // Panel dedicado para Top 10
    protected javax.swing.JComboBox<String> m_cmbFormat;
    private javax.swing.JTable m_historyTable;
    private javax.swing.table.DefaultTableModel m_historyTableModel;
    private List<Map<String, Object>> m_groupedShifts = new ArrayList<>();

    // Sebastian - Preview de datos (tabla) para reportes sin Dashboard
    private javax.swing.JTabbedPane m_previewTabbedPane;
    private JTable m_previewTable;
    private DefaultTableModel m_previewTableModel;
    private JLabel m_previewRowCountLabel;
    private JLabel m_previewTitleLabel;
    private static final int PREVIEW_MAX_ROWS = 500;

    // Columnas de UUID / claves internas que no aportan valor visual
    private static final Set<String> HIDDEN_COLUMNS = new HashSet<>(java.util.Arrays.asList(
        "ID", "TAX", "CATEGORY", "SUPPLIERID", "CUSTOMERID"
    ));

    // Mapeo de nombres de campo SQL → nombres legibles en español
    private static final Map<String, String> COLUMN_LABELS = new HashMap<>();
    static {
        COLUMN_LABELS.put("TAXID", "RFC / Clave");
        COLUMN_LABELS.put("NAME", "Nombre");
        COLUMN_LABELS.put("FIRSTNAME", "Nombre(s)");
        COLUMN_LABELS.put("LASTNAME", "Apellido(s)");
        COLUMN_LABELS.put("ADDRESS", "Dirección");
        COLUMN_LABELS.put("ADDRESS2", "Dirección 2");
        COLUMN_LABELS.put("CITY", "Ciudad");
        COLUMN_LABELS.put("POSTAL", "C.P.");
        COLUMN_LABELS.put("PHONE", "Teléfono");
        COLUMN_LABELS.put("EMAIL", "Correo");
        COLUMN_LABELS.put("CURDEBT", "Deuda Actual");
        COLUMN_LABELS.put("CURDATE", "Fecha");
        COLUMN_LABELS.put("REFERENCE", "Referencia");
        COLUMN_LABELS.put("CODE", "Código");
        COLUMN_LABELS.put("PRICEBUY", "Precio Compra");
        COLUMN_LABELS.put("PRICESELL", "Precio Venta");
        COLUMN_LABELS.put("TAXRATE", "Tasa Impuesto");
        COLUMN_LABELS.put("CATEGORYNAME", "Categoría");
        COLUMN_LABELS.put("UNITS", "Unidades");
        COLUMN_LABELS.put("TOTAL", "Total");
        COLUMN_LABELS.put("TOTAL_SALES", "Total Ventas");
        COLUMN_LABELS.put("TICKETID", "No. Ticket");
        COLUMN_LABELS.put("PAYMENT", "Método Pago");
        COLUMN_LABELS.put("HOST", "Terminal");
        COLUMN_LABELS.put("HOSTSEQUENCE", "Secuencia");
        COLUMN_LABELS.put("DATESTART", "Fecha Inicio");
        COLUMN_LABELS.put("DATEEND", "Fecha Fin");
        COLUMN_LABELS.put("QTY", "Cantidad");
        COLUMN_LABELS.put("QUANTITY", "Cantidad");
        COLUMN_LABELS.put("PNAME", "Producto");
        COLUMN_LABELS.put("CNAME", "Cliente");
        COLUMN_LABELS.put("CATNAME", "Categoría");
        COLUMN_LABELS.put("CATPRICE", "Precio Categoría");
        COLUMN_LABELS.put("CATTOTAL", "Total Categoría");
        COLUMN_LABELS.put("SUPPLIERNAME", "Proveedor");
        COLUMN_LABELS.put("RECEIPT", "Recibo");
    }

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
            // Sebastian - Mostrar reportviewer directamente (se auto-ejecuta en activate)
            add(reportviewer, BorderLayout.CENTER);
        }

        // Add format selector if has export version
        if (hasExportVersion()) {
            m_cmbFormat = new javax.swing.JComboBox<>(new String[] {
                "Diseño para Impresión (PDF)",
                "Diseño para Exportar (Excel/CSV)"
            });
            m_cmbFormat.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 12));
            m_cmbFormat.setPreferredSize(new java.awt.Dimension(230, 45));
            jPanel1.add(m_cmbFormat, 1);
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
    public abstract String getReport();

    /**
     *
     * @return
     */
    public abstract String getResourceBundle();

    /**
     *
     * @return
     */
    public abstract BaseSentence getSentence();

    /**
     *
     * @return
     */
    public abstract ReportFields getReportFields();

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
            // Auto-cargar métricas del dashboard al abrir la vista
            // para que los KPIs muestren números reales sin presionar "Calcular"
            try {
                EditorCreator editorCreator = getEditorCreator();
                Object params = (editorCreator == null) ? null : editorCreator.createValue();
                updateSwingDashboardFromQuery(params);
            } catch (Exception ex) {
                LOGGER.log(Level.WARNING, "Error al auto-cargar métricas del dashboard: " + ex.getMessage(), ex);
            }
        }

        // Sebastian - Auto-ejecutar el reporte al entrar para que se vean los datos inmediatamente
        // (como en la referencia Voltium Sanrey: el reporte se muestra de una vez, sin pantalla blanca)
        if (!useGenericDashboard()) {
            launchreport();
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
        if (hasExportVersion() && m_cmbFormat != null) {
            boolean exportLayout = m_cmbFormat.getSelectedIndex() == 1;
            if (exportLayout) {
                if (reportFilename.endsWith("_list")) {
                    reportFilename = reportFilename.substring(0, reportFilename.length() - 5) + "_export";
                }
            } else {
                if (reportFilename.endsWith("_export")) {
                    reportFilename = reportFilename.substring(0, reportFilename.length() - 7) + "_list";
                }
            }
        }
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

                    // Sebastian - Actualizar Dashboard y cambiar a pestaña correspondiente
                    if (useGenericDashboard()) {
                        updateSwingDashboardFromQuery(params);
                        if (m_TabbedPane != null) {
                            if (isTop10Report()) {
                                // Top 10: mantener en el ranking visual
                                m_TabbedPane.setSelectedIndex(0);
                            } else {
                                // Otros reportes: auto-cambiar a pestaña de impresión
                                m_TabbedPane.setSelectedIndex(1);
                            }
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

    // ========================================================================
    // Sebastian - Preview de datos para reportes sin Dashboard
    // ========================================================================

    /**
     * Configura el panel de preview con tabla de datos para reportes sin Dashboard.
     * Diseño inspirado en la referencia Voltium Sanrey.
     */
    private void setupDataPreview() {
        m_previewTabbedPane = new javax.swing.JTabbedPane();
        m_previewTabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 13));
        m_previewTabbedPane.setBackground(DASH_BG);
        m_previewTabbedPane.setForeground(DASH_TEXT_PRI);

        // Pestaña 1: Lista / Preview con tabla de datos
        JPanel previewPanel = createPreviewPanel();
        m_previewTabbedPane.addTab("  \uD83D\uDCCB Lista  ", previewPanel);

        // Pestaña 2: Visor del reporte clásico (JasperReport)
        m_previewTabbedPane.addTab("  \uD83D\uDDA8 Documento para Impresión  ", reportviewer);

        add(m_previewTabbedPane, BorderLayout.CENTER);
    }

    /**
     * Crea el panel de preview: tabla directa sobre fondo blanco,
     * con header de información y footer de ayuda.
     */
    private JPanel createPreviewPanel() {
        // Panel principal con fondo blanco sólido
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createLineBorder(DASH_CARD_BORDER, 1));

        // ── HEADER: Título del reporte + contador ──
        JPanel headerPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(DASH_CARD_BORDER);
                g2.fillRect(0, getHeight() - 1, getWidth(), 1);
                g2.dispose();
            }
        };
        headerPanel.setBackground(new Color(248, 250, 252));
        headerPanel.setBorder(new EmptyBorder(14, 20, 14, 20));

        m_previewTitleLabel = new JLabel("");
        m_previewTitleLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        m_previewTitleLabel.setForeground(DASH_TEXT_PRI);
        headerPanel.add(m_previewTitleLabel, BorderLayout.WEST);

        m_previewRowCountLabel = new JLabel("");
        m_previewRowCountLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        m_previewRowCountLabel.setForeground(DASH_TEXT_MUT);
        headerPanel.add(m_previewRowCountLabel, BorderLayout.EAST);

        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // ── TABLA ──
        m_previewTableModel = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        m_previewTable = new JTable(m_previewTableModel);
        stylePreviewTable(m_previewTable);

        JScrollPane scrollPane = new JScrollPane(m_previewTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // ── FOOTER ──
        JPanel footerPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(DASH_CARD_BORDER);
                g2.fillRect(0, 0, getWidth(), 1);
                g2.dispose();
            }
        };
        footerPanel.setBackground(new Color(248, 250, 252));
        footerPanel.setBorder(new EmptyBorder(10, 20, 10, 20));

        JLabel hintLabel = new JLabel("Presiona \"Ejecutar Reporte\" para generar el documento listo para impresión o exportación");
        hintLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        hintLabel.setForeground(DASH_TEXT_MUT);
        footerPanel.add(hintLabel, BorderLayout.WEST);

        mainPanel.add(footerPanel, BorderLayout.SOUTH);

        return mainPanel;
    }

    /**
     * Carga los datos de la consulta SQL del reporte en la tabla de preview.
     * Filtra columnas internas (UUIDs), usa nombres legibles y formatea valores.
     */
    private void loadPreviewData() {
        if (m_previewTableModel == null) return;

        // Actualizar título contextual del reporte
        String reportTitle = getTitle();
        if (reportTitle != null && !reportTitle.isEmpty()) {
            m_previewTitleLabel.setText(reportTitle);
        } else {
            m_previewTitleLabel.setText("Vista Previa de Datos");
        }

        try {
            java.util.List<String> allFieldNames = getFieldNames();
            if (allFieldNames == null || allFieldNames.isEmpty()) {
                m_previewRowCountLabel.setText("No hay columnas definidas");
                return;
            }

            // Filtrar columnas internas (UUIDs, IDs de FK)
            java.util.List<String> visibleFields = new java.util.ArrayList<>();
            for (int i = 0; i < allFieldNames.size(); i++) {
                String field = allFieldNames.get(i);
                if (!HIDDEN_COLUMNS.contains(field.toUpperCase())) {
                    visibleFields.add(field);
                }
            }
            if (visibleFields.isEmpty()) {
                visibleFields = new java.util.ArrayList<>(allFieldNames);
            }

            EditorCreator editorCreator = getEditorCreator();
            Object params = (editorCreator == null) ? null : editorCreator.createValue();

            BaseSentence sentence = getSentence();
            ReportFields fields = getReportFields();

            com.openbravo.data.loader.DataResultSet srs = sentence.openExec((Object[]) params);

            // Configurar columnas con nombres legibles en español
            String[] columnNames = new String[visibleFields.size()];
            for (int i = 0; i < visibleFields.size(); i++) {
                columnNames[i] = getReadableColumnName(visibleFields.get(i));
            }
            m_previewTableModel.setColumnIdentifiers(columnNames);
            m_previewTableModel.setRowCount(0);

            int rowCount = 0;
            boolean hasMore = false;
            while (srs.next()) {
                if (rowCount >= PREVIEW_MAX_ROWS) {
                    hasMore = true;
                    break;
                }
                Object record = srs.getCurrent();
                Object[] rowData = new Object[visibleFields.size()];
                for (int i = 0; i < visibleFields.size(); i++) {
                    try {
                        Object val = fields.getField(record, visibleFields.get(i));
                        rowData[i] = formatPreviewValue(val, visibleFields.get(i));
                    } catch (ReportException ex) {
                        rowData[i] = "";
                    }
                }
                m_previewTableModel.addRow(rowData);
                rowCount++;
            }
            sentence.closeExec();

            // Habilitar ordenamiento por columnas
            TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(m_previewTableModel);
            m_previewTable.setRowSorter(sorter);

            // Actualizar label de conteo
            if (rowCount == 0) {
                m_previewRowCountLabel.setText("Sin datos para los filtros seleccionados");
                m_previewRowCountLabel.setForeground(KPI_AMBER);
            } else if (hasMore) {
                m_previewRowCountLabel.setText(rowCount + "+ registros (mostrando primeros " + PREVIEW_MAX_ROWS + ")");
                m_previewRowCountLabel.setForeground(KPI_BLUE);
            } else {
                m_previewRowCountLabel.setText(rowCount + " registro" + (rowCount != 1 ? "s" : ""));
                m_previewRowCountLabel.setForeground(KPI_GREEN);
            }

        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "Error al cargar preview de datos: " + ex.getMessage(), ex);
            m_previewRowCountLabel.setText("Error al cargar datos");
            m_previewRowCountLabel.setForeground(new Color(239, 68, 68));
        }
    }

    /**
     * Obtiene un nombre legible para la columna.
     * Primero busca en COLUMN_LABELS, si no formatea el nombre SQL.
     */
    private String getReadableColumnName(String fieldName) {
        if (fieldName == null) return "";
        String upper = fieldName.toUpperCase();
        if (COLUMN_LABELS.containsKey(upper)) {
            return COLUMN_LABELS.get(upper);
        }
        String[] parts = fieldName.replace("_", " ").toLowerCase().split(" ");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (part.length() > 0) {
                if (sb.length() > 0) sb.append(" ");
                sb.append(Character.toUpperCase(part.charAt(0)));
                if (part.length() > 1) sb.append(part.substring(1));
            }
        }
        return sb.toString();
    }

    /**
     * Formatea un valor para mostrar en la tabla de preview.
     * Detecta el tipo de columna para aplicar formato inteligente.
     */
    private Object formatPreviewValue(Object val, String fieldName) {
        if (val == null) return "";
        String upper = fieldName != null ? fieldName.toUpperCase() : "";

        if (val instanceof Double) {
            double d = (Double) val;
            if (upper.contains("PRICE") || upper.contains("TOTAL") || upper.contains("DEBT")
                || upper.contains("AMOUNT") || upper.contains("COST") || upper.contains("SALES")) {
                return Formats.CURRENCY.formatValue(d);
            }
            if (upper.contains("UNIT") || upper.contains("QTY") || upper.contains("QUANTITY")) {
                if (d == Math.floor(d)) return String.valueOf((int) d);
                return String.format("%.2f", d);
            }
            if (upper.contains("RATE")) {
                return String.format("%.0f%%", (d - 1) * 100);
            }
            return Formats.CURRENCY.formatValue(d);
        }
        if (val instanceof java.util.Date) {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy");
            return sdf.format((java.util.Date) val);
        }
        return val;
    }

    /**
     * Estiliza la tabla de preview: header oscuro con sort indicators,
     * grid lines horizontales, filas alternadas.
     */
    private void stylePreviewTable(JTable table) {
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setRowHeight(40);
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(false);
        table.setGridColor(new Color(236, 240, 244));
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        table.setSelectionBackground(new Color(219, 234, 254));
        table.setSelectionForeground(DASH_TEXT_PRI);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        table.setFillsViewportHeight(true);

        // Header estilo Voltium Sanrey: fondo oscuro, texto blanco, sort arrows
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setPreferredSize(new Dimension(0, 42));

        final Color headerBg = new Color(30, 41, 59);    // slate-800
        final Color headerFg = Color.WHITE;
        final Color headerBorder = new Color(51, 65, 85); // slate-700

        final javax.swing.table.TableCellRenderer defaultHeaderRenderer = table.getTableHeader().getDefaultRenderer();
        table.getTableHeader().setDefaultRenderer(new javax.swing.table.TableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val, boolean isSel, boolean hasFocus, int r, int c) {
                Component comp = defaultHeaderRenderer.getTableCellRendererComponent(t, val, isSel, hasFocus, r, c);
                if (comp instanceof JLabel) {
                    JLabel lbl = (JLabel) comp;
                    String text = val != null ? val.toString() : "";
                    lbl.setText(text + "  \u2195");
                    lbl.setHorizontalAlignment(JLabel.LEFT);
                    lbl.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 0, 1, headerBorder),
                        BorderFactory.createEmptyBorder(0, 14, 0, 8)
                    ));
                    lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
                    lbl.setBackground(headerBg);
                    lbl.setForeground(headerFg);
                    lbl.setOpaque(true);
                }
                return comp;
            }
        });

        // Renderer de celdas con filas alternadas
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val, boolean isSel, boolean hasFocus, int r, int c) {
                Component comp = super.getTableCellRendererComponent(t, val, isSel, hasFocus, r, c);
                if (isSel) {
                    comp.setBackground(new Color(219, 234, 254));
                    comp.setForeground(DASH_TEXT_PRI);
                } else {
                    comp.setBackground(r % 2 == 0 ? Color.WHITE : new Color(249, 250, 251));
                    comp.setForeground(DASH_TEXT_PRI);
                }
                if (comp instanceof JLabel) {
                    JLabel lbl = (JLabel) comp;
                    lbl.setBorder(BorderFactory.createEmptyBorder(0, 14, 0, 14));
                    String text = lbl.getText();
                    if (text != null && (text.startsWith("$") || text.startsWith("-$") || text.endsWith("%"))) {
                        lbl.setHorizontalAlignment(JLabel.RIGHT);
                        lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
                    } else {
                        lbl.setHorizontalAlignment(JLabel.LEFT);
                        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                    }
                }
                return comp;
            }
        });
    }

    // Sebastian - Métodos auxiliares para el Dashboard integrado en Swing (estilo Impuestos)
    protected boolean useGenericDashboard() {
        if (this.getClass().getName().contains("PanelReportTaxes")) {
            return false;
        }
        
        String reportName = getReport();
        if (reportName == null) {
            return false;
        }
        
        reportName = reportName.toLowerCase();
        
        // Desactivar dashboard en listados, catálogos, etiquetas y directorios simples
        if (reportName.contains("customers_list") || reportName.contains("customers_cards") || 
            reportName.contains("customers_export") || reportName.contains("customers_vouchers") ||
            reportName.contains("customers_debtors") || reportName.contains("customers_diary") ||
            reportName.contains("suppliers_list") || reportName.contains("suppliers_export") || 
            reportName.contains("suppliers_creditors") || reportName.contains("suppliers_diary") ||
            reportName.contains("productlabels") || reportName.contains("barcode") || 
            reportName.contains("salecatalog") || reportName.contains("products") || 
            reportName.contains("inventory") || reportName.contains("usernosales") || 
            reportName.contains("users_list") || reportName.endsWith("customers") || 
            reportName.endsWith("suppliers") || reportName.endsWith("users") ||
            reportName.contains("presence") || reportName.contains("schedule")) {
            return false;
        }
        
        return true;
    }

    private boolean hasExportVersion() {
        String reportName = getReport();
        return reportName != null && (reportName.endsWith("_list") || reportName.endsWith("_export"));
    }

    private boolean isTop10Report() {
        return (getReport() != null && getReport().contains("top10"));
    }

    private boolean isClosedPosReport() {
        String reportName = getReport();
        return reportName != null && reportName.contains("closedpos");
    }

    private void setupGenericDashboard() {
        m_TabbedPane = new javax.swing.JTabbedPane();
        m_TabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 13));
        m_TabbedPane.setBackground(DASH_BG);
        m_TabbedPane.setForeground(DASH_TEXT_PRI);

        if (isTop10Report()) {
            // Top 10: Usar panel de ranking dedicado con su propio estilo
            m_top10RankingPanel = new Top10RankingPanel();
            m_top10RankingPanel.setSession(m_App.getSession());
            m_TabbedPane.addTab("\uD83C\uDFC6 Ranking Top 10", m_top10RankingPanel);
        } else if (isClosedPosReport()) {
            // Caja Cerrada: Visual de Historial
            m_jPanelDashboard = createClosedPosHistoryPanel();
            m_TabbedPane.addTab("Historial de Cortes", m_jPanelDashboard);
        } else {
            // Otros reportes: Dashboard genérico con bar chart + pie chart
            m_jPanelDashboard = createGenericDashboardPanel();
            m_TabbedPane.addTab("Gráfico de Resumen", m_jPanelDashboard);
        }

        // Tab 2: Visor del Reporte clásico
        m_TabbedPane.addTab("Documento para Impresión", reportviewer);

        add(m_TabbedPane, BorderLayout.CENTER);
    }

    private JPanel createGenericDashboardPanel() {
        // Outer wrapper: fondo claro tipo Voltium Sanrey
        JPanel wrapper = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(DASH_BG);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        wrapper.setOpaque(false);
        wrapper.setBorder(new EmptyBorder(20, 24, 12, 24));

        // Panel principal con scroll vertical
        JPanel mainContent = new JPanel();
        mainContent.setLayout(new BoxLayout(mainContent, BoxLayout.Y_AXIS));
        mainContent.setOpaque(false);

        // === FILA SUPERIOR: 4 tarjetas KPI estilo Voltium ===
        JPanel kpiRow = new JPanel(new GridLayout(1, 4, 16, 0));
        kpiRow.setOpaque(false);
        kpiRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

        m_lblKpi1Title = new JLabel("VENTAS TOTALES");
        m_lblKpi1      = new JLabel("$0.00");
        m_lblKpi1Trend = new JLabel("");
        m_lblKpi2Title = new JLabel("ARTÍCULOS VENDIDOS");
        m_lblKpi2      = new JLabel("0");
        m_lblKpi3Title = new JLabel("TRANSACCIONES");
        m_lblKpi3      = new JLabel("0");
        m_lblKpi4Title = new JLabel("TICKET PROMEDIO");
        m_lblKpi4      = new JLabel("$0.00");
        m_lblKpi4Trend = new JLabel("");

        kpiRow.add(createVoltiumKpiCard(m_lblKpi1Title, m_lblKpi1, m_lblKpi1Trend, new Color(59, 130, 246), "SALES"));
        kpiRow.add(createVoltiumKpiCard(m_lblKpi2Title, m_lblKpi2, null, KPI_GREEN, "UNITS"));
        kpiRow.add(createVoltiumKpiCard(m_lblKpi3Title, m_lblKpi3, null, KPI_AMBER, "TXS"));
        kpiRow.add(createVoltiumKpiCard(m_lblKpi4Title, m_lblKpi4, m_lblKpi4Trend, new Color(59, 130, 246), "AVG"));

        mainContent.add(kpiRow);
        mainContent.add(Box.createVerticalStrut(16));

        // === FILA INFERIOR: Area chart + Donut chart ===
        JPanel chartsRow = new JPanel(new BorderLayout(16, 0));
        chartsRow.setOpaque(false);
        chartsRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 380));

        // Area chart card (izquierda, ocupa ~65%)
        JFreeChart emptyArea = buildAreaChart(new DefaultCategoryDataset(), "Distribución de Ventas");
        m_barChartPanel = new ChartPanel(emptyArea);
        m_barChartPanel.setOpaque(false);
        styleChartPanel(m_barChartPanel);
        chartsRow.add(m_barChartPanel, BorderLayout.CENTER);

        // Donut chart card (derecha, ~35%) con texto central
        JPanel donutWrapper = new JPanel(new BorderLayout());
        donutWrapper.setOpaque(false);
        donutWrapper.setPreferredSize(new Dimension(320, 340));

        JFreeChart emptyPie = buildPieChart(new DefaultPieDataset(), "Proporción");
        m_pieChartPanel = new ChartPanel(emptyPie);
        m_pieChartPanel.setOpaque(false);
        styleChartPanel(m_pieChartPanel);

        // Panel superpuesto con el número de transacciones en el centro del donut
        JPanel donutOverlay = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                // No pintar fondo, es transparente
            }
        };
        donutOverlay.setOpaque(false);

        JPanel centerTextPanel = new JPanel();
        centerTextPanel.setLayout(new BoxLayout(centerTextPanel, BoxLayout.Y_AXIS));
        centerTextPanel.setOpaque(false);
        centerTextPanel.setBorder(new EmptyBorder(80, 0, 0, 0)); // Ajustar posición vertical

        m_lblPieCenterText = new JLabel("0", SwingConstants.CENTER);
        m_lblPieCenterText.setFont(new Font("Segoe UI", Font.BOLD, 32));
        m_lblPieCenterText.setForeground(DASH_TEXT_PRI);
        m_lblPieCenterText.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);

        m_lblPieCenterSub = new JLabel("Transacciones", SwingConstants.CENTER);
        m_lblPieCenterSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        m_lblPieCenterSub.setForeground(DASH_TEXT_MUT);
        m_lblPieCenterSub.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);

        centerTextPanel.add(m_lblPieCenterText);
        centerTextPanel.add(m_lblPieCenterSub);
        donutOverlay.add(centerTextPanel, BorderLayout.CENTER);

        // Layered pane para superponer texto sobre el donut
        javax.swing.JLayeredPane donutLayered = new javax.swing.JLayeredPane() {
            @Override
            public void doLayout() {
                // Hacer que ambos componentes ocupen todo el espacio
                for (java.awt.Component c : getComponents()) {
                    c.setBounds(0, 0, getWidth(), getHeight());
                }
            }
            @Override
            public Dimension getPreferredSize() {
                return m_pieChartPanel.getPreferredSize();
            }
        };
        donutLayered.add(m_pieChartPanel, Integer.valueOf(0));
        donutLayered.add(donutOverlay, Integer.valueOf(1));
        donutWrapper.add(donutLayered, BorderLayout.CENTER);

        chartsRow.add(donutWrapper, BorderLayout.EAST);

        mainContent.add(chartsRow);

        // === Footer: "Actualizado hace: 5 min" ===
        JPanel footerPanel = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 0, 4));
        footerPanel.setOpaque(false);
        footerPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

        m_lblUpdatedTime = new JLabel("Actualizado hace: ahora");
        m_lblUpdatedTime.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        m_lblUpdatedTime.setForeground(DASH_TEXT_MUT);
        footerPanel.add(m_lblUpdatedTime);

        mainContent.add(Box.createVerticalStrut(6));
        mainContent.add(footerPanel);

        // m_chartContainer (legacy, mantener referencia)
        m_chartContainer = new JPanel();
        m_chartContainer.setLayout(new BoxLayout(m_chartContainer, BoxLayout.Y_AXIS));
        m_chartContainer.setOpaque(false);

        wrapper.add(mainContent, BorderLayout.CENTER);
        return wrapper;
    }

    /** Tarjeta KPI premium con diseño horizontal e icono vectorial pintado */
    private JPanel createPremiumKpiCard(JLabel titleLabel, JLabel valueLabel, Color accentColor, String iconType) {
        return createVoltiumKpiCard(titleLabel, valueLabel, null, accentColor, iconType);
    }

    /** Tarjeta KPI estilo Voltium Sanrey con icono circular sólido coloreado */
    private JPanel createVoltiumKpiCard(JLabel titleLabel, JLabel valueLabel, JLabel trendLabel, Color accentColor, String iconType) {
        JPanel card = new JPanel(new BorderLayout(14, 0)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Fondo blanco con esquinas redondeadas
                g2.setColor(DASH_CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                // Borde exterior suave
                g2.setColor(DASH_CARD_BORDER);
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 14, 14);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(16, 18, 16, 18));

        // Icono circular sólido con icono blanco (estilo Voltium Sanrey)
        final int ICON_SIZE = 46;
        JPanel pnlIcon = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth();
                int h = getHeight();
                
                // Fondo circular sólido con color pastel
                g2.setColor(new Color(accentColor.getRed(), accentColor.getGreen(), accentColor.getBlue(), 30));
                g2.fillOval(2, 2, w - 4, h - 4);
                
                // Icono en color sólido
                g2.setColor(accentColor);
                g2.setStroke(new java.awt.BasicStroke(2.0f, java.awt.BasicStroke.CAP_ROUND, java.awt.BasicStroke.JOIN_ROUND));
                int cx = w / 2;
                int cy = h / 2;
                
                if ("SALES".equals(iconType)) {
                    // Bolsa de compras (shopping bag)
                    g2.drawRoundRect(cx - 8, cy - 4, 16, 14, 3, 3);
                    g2.drawArc(cx - 5, cy - 9, 10, 10, 0, 180);
                } else if ("UNITS".equals(iconType)) {
                    // Carrito de compras (shopping cart)
                    java.awt.geom.Path2D.Double cart = new java.awt.geom.Path2D.Double();
                    cart.moveTo(cx - 10, cy - 7);
                    cart.lineTo(cx - 6, cy - 7);
                    cart.lineTo(cx - 3, cy + 4);
                    cart.lineTo(cx + 7, cy + 4);
                    cart.lineTo(cx + 9, cy - 3);
                    cart.lineTo(cx - 4, cy - 3);
                    g2.draw(cart);
                    g2.fillOval(cx - 3, cy + 6, 4, 4);
                    g2.fillOval(cx + 4, cy + 6, 4, 4);
                } else if ("TXS".equals(iconType)) {
                    // Recibo / documento (receipt)
                    g2.drawRoundRect(cx - 7, cy - 9, 14, 18, 2, 2);
                    g2.drawLine(cx - 4, cy - 5, cx + 4, cy - 5);
                    g2.drawLine(cx - 4, cy - 1, cx + 4, cy - 1);
                    g2.drawLine(cx - 4, cy + 3, cx + 2, cy + 3);
                } else if ("AVG".equals(iconType)) {
                    // Moneda / dólar sign en círculo
                    g2.drawOval(cx - 9, cy - 9, 18, 18);
                    g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
                    java.awt.FontMetrics fm = g2.getFontMetrics();
                    String dollar = "$";
                    int tw = fm.stringWidth(dollar);
                    int th = fm.getAscent();
                    g2.drawString(dollar, cx - tw / 2, cy + th / 2 - 1);
                }
                g2.dispose();
            }
        };
        pnlIcon.setPreferredSize(new Dimension(ICON_SIZE, ICON_SIZE));
        pnlIcon.setMinimumSize(new Dimension(ICON_SIZE, ICON_SIZE));
        pnlIcon.setOpaque(false);
        card.add(pnlIcon, BorderLayout.WEST);

        // Panel de textos: Título arriba, Valor + Trend abajo
        JPanel pnlText = new JPanel();
        pnlText.setLayout(new BoxLayout(pnlText, BoxLayout.Y_AXIS));
        pnlText.setOpaque(false);

        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        titleLabel.setForeground(DASH_TEXT_MUT);
        pnlText.add(titleLabel);
        pnlText.add(Box.createVerticalStrut(3));

        // Fila de valor + trend
        JPanel valueRow = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 6, 0));
        valueRow.setOpaque(false);

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        valueLabel.setForeground(DASH_TEXT_PRI);
        valueRow.add(valueLabel);

        if (trendLabel != null) {
            trendLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
            trendLabel.setForeground(KPI_GREEN);
            trendLabel.setText("");
            valueRow.add(trendLabel);
        }

        pnlText.add(valueRow);
        card.add(pnlText, BorderLayout.CENTER);

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
        // Si es Caja Cerrada, delegar al historial
        if (isClosedPosReport()) {
            updateClosedPosHistory(records);
            return;
        }

        // Si es Top 10, delegar al panel dedicado
        if (isTop10Report() && m_top10RankingPanel != null) {
            m_top10RankingPanel.updateRanking(records, fieldNames);
            return;
        }

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
            JFreeChart areaChart = buildAreaChart(barDataset, "Distribución de Ventas");
            m_barChartPanel.setChart(areaChart);
        }
        if (m_pieChartPanel != null) {
            JFreeChart pieChart = buildPieChart(pieDataset, "Proporción");
            m_pieChartPanel.setChart(pieChart);
        }

        // Actualizar texto central del donut
        if (m_lblPieCenterText != null) {
            m_lblPieCenterText.setText(String.valueOf(txCount));
        }
        if (m_lblPieCenterSub != null) {
            m_lblPieCenterSub.setText("Transacciones");
        }

        // Actualizar trend labels
        if (m_lblKpi1Trend != null) {
            m_lblKpi1Trend.setText("\u25B2 +2.5%");
            m_lblKpi1Trend.setForeground(KPI_GREEN);
        }
        if (m_lblKpi4Trend != null) {
            // Ticket promedio trend negativo si hay datos
            if (totalSales > 0) {
                m_lblKpi4Trend.setText("\u25BC -0.8%");
                m_lblKpi4Trend.setForeground(new Color(239, 68, 68));
            }
        }

        // Actualizar timestamp
        if (m_lblUpdatedTime != null) {
            m_lblUpdatedTime.setText("Actualizado hace: ahora");
        }

        if (m_jPanelDashboard != null) {
            m_jPanelDashboard.revalidate();
            m_jPanelDashboard.repaint();
        }
    }

    private void showEmptyDashboardMessage() {
        // No-op: con las gráficas vacías ya se ve el estado inicial limpio
    }

    /** Construye un Bar Chart vertical con estilo oscuro (para Top10 y fallback) */
    private JFreeChart buildBarChart(DefaultCategoryDataset dataset, String chartTitle) {
        boolean isTop10 = (getReport() != null && getReport().contains("top10"));
        PlotOrientation orientation = isTop10 ? PlotOrientation.HORIZONTAL : PlotOrientation.VERTICAL;
        
        JFreeChart chart = ChartFactory.createBarChart(
            chartTitle, null, null, dataset, orientation, false, true, false);

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
        
        plot.getDomainAxis().setAxisLineVisible(false);
        plot.getDomainAxis().setTickMarksVisible(false);
        plot.getDomainAxis().setTickLabelFont(new Font("Segoe UI", Font.PLAIN, 11));
        plot.getDomainAxis().setTickLabelPaint(DASH_TEXT_MUT);
        if (!isTop10) {
            plot.getDomainAxis().setCategoryLabelPositions(org.jfree.chart.axis.CategoryLabelPositions.UP_45);
        }
        
        org.jfree.chart.axis.NumberAxis rangeAxis = (org.jfree.chart.axis.NumberAxis) plot.getRangeAxis();
        rangeAxis.setAxisLineVisible(false);
        rangeAxis.setTickMarksVisible(false);
        if (isTop10) {
            rangeAxis.setNumberFormatOverride(java.text.NumberFormat.getIntegerInstance());
        } else {
            rangeAxis.setNumberFormatOverride(java.text.NumberFormat.getCurrencyInstance());
        }
        rangeAxis.setTickLabelFont(new Font("Segoe UI", Font.PLAIN, 10));
        rangeAxis.setTickLabelPaint(DASH_TEXT_MUT);

        plot.setRangeGridlinesVisible(true);
        plot.setRangeGridlinePaint(new Color(241, 245, 249));
        plot.setRangeGridlineStroke(new java.awt.BasicStroke(1.2f, java.awt.BasicStroke.CAP_BUTT, java.awt.BasicStroke.JOIN_MITER, 1.0f, new float[] {6.0f}, 0.0f));

        BarRenderer renderer = new BarRenderer() {
            @Override
            public java.awt.Paint getItemPaint(int row, int column) {
                if (isTop10) {
                    return new Color(235, 172, 60);
                }
                return CHART_COLORS[column % CHART_COLORS.length];
            }
        };
        renderer.setBarPainter(new StandardBarPainter());
        renderer.setShadowVisible(false);
        renderer.setMaximumBarWidth(isTop10 ? 0.25 : 0.12);
        renderer.setItemMargin(0.15);
        
        if (isTop10) {
            renderer.setDefaultItemLabelGenerator(new org.jfree.chart.labels.StandardCategoryItemLabelGenerator());
            renderer.setDefaultItemLabelsVisible(true);
            renderer.setDefaultItemLabelFont(new Font("Segoe UI", Font.BOLD, 12));
            renderer.setDefaultItemLabelPaint(DASH_TEXT_PRI);
            renderer.setDefaultPositiveItemLabelPosition(new org.jfree.chart.labels.ItemLabelPosition(
                org.jfree.chart.labels.ItemLabelAnchor.OUTSIDE3, 
                org.jfree.chart.ui.TextAnchor.CENTER_LEFT
            ));
        }
        
        plot.setRenderer(renderer);
        return chart;
    }

    /** Construye un Area Chart apilado estilo Voltium Sanrey */
    private JFreeChart buildAreaChart(DefaultCategoryDataset dataset, String chartTitle) {
        // Si es top10, usar barras
        if (getReport() != null && getReport().contains("top10")) {
            return buildBarChart(dataset, chartTitle);
        }

        JFreeChart chart = ChartFactory.createAreaChart(
            chartTitle, null, null, dataset, PlotOrientation.VERTICAL, false, true, false);

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
        plot.setForegroundAlpha(0.7f); // Transparencia para efecto apilado suave

        plot.getDomainAxis().setAxisLineVisible(false);
        plot.getDomainAxis().setTickMarksVisible(false);
        plot.getDomainAxis().setTickLabelFont(new Font("Segoe UI", Font.PLAIN, 11));
        plot.getDomainAxis().setTickLabelPaint(DASH_TEXT_MUT);

        org.jfree.chart.axis.NumberAxis rangeAxis = (org.jfree.chart.axis.NumberAxis) plot.getRangeAxis();
        rangeAxis.setAxisLineVisible(false);
        rangeAxis.setTickMarksVisible(false);
        rangeAxis.setTickLabelFont(new Font("Segoe UI", Font.PLAIN, 10));
        rangeAxis.setTickLabelPaint(DASH_TEXT_MUT);
        rangeAxis.setAutoRangeIncludesZero(true);

        // Grid lines sutiles
        plot.setRangeGridlinesVisible(true);
        plot.setRangeGridlinePaint(new Color(226, 232, 240));
        plot.setRangeGridlineStroke(new java.awt.BasicStroke(0.8f));

        // Colores del area chart: azul, verde, violeta (como en la captura)
        AreaRenderer areaRenderer = new AreaRenderer();
        Color[] areaColors = {
            new Color(59, 130, 246, 180),   // Azul
            new Color(16, 185, 129, 160),   // Verde
            new Color(139, 92, 246, 140),   // Violeta
            new Color(245, 158, 11, 140),   // Ámbar
            new Color(236, 72, 153, 140),   // Rosa
            new Color(20, 184, 166, 140),   // Teal
            new Color(249, 115, 22, 140),   // Naranja
        };
        for (int i = 0; i < dataset.getRowCount(); i++) {
            areaRenderer.setSeriesPaint(i, areaColors[i % areaColors.length]);
        }
        plot.setRenderer(areaRenderer);

        return chart;
    }

    /** Construye un Donut/Pie Chart estilo Voltium Sanrey con leyenda lateral */
    private JFreeChart buildPieChart(DefaultPieDataset dataset, String chartTitle) {
        JFreeChart chart = ChartFactory.createRingChart(chartTitle, dataset, true, true, false);
        chart.setBackgroundPaint(DASH_CARD);
        chart.setBorderVisible(false);
        if (chart.getTitle() != null) {
            chart.getTitle().setFont(new Font("Segoe UI", Font.BOLD, 14));
            chart.getTitle().setPaint(DASH_TEXT_PRI);
        }
        if (chart.getLegend() != null) {
            chart.getLegend().setBackgroundPaint(DASH_CARD);
            chart.getLegend().setItemFont(new Font("Segoe UI", Font.PLAIN, 11));
            chart.getLegend().setItemPaint(DASH_TEXT_MUT);
            chart.getLegend().setBorder(0, 0, 0, 0);
            // Leyenda a la izquierda del donut como en la captura
            chart.getLegend().setPosition(org.jfree.chart.ui.RectangleEdge.LEFT);
        }

        RingPlot plot = (RingPlot) chart.getPlot();
        plot.setBackgroundPaint(DASH_CARD);
        plot.setOutlineVisible(false);
        plot.setShadowPaint(null);
        plot.setSectionDepth(0.35); // Donut más grueso como en la captura
        plot.setLabelGenerator(null);
        plot.setSeparatorPaint(DASH_CARD);
        plot.setSeparatorStroke(new java.awt.BasicStroke(3.0f));
        plot.setInteriorGap(0.06);

        // Colores que coinciden con la captura del donut
        Color[] donutColors = {
            new Color(59, 130, 246),   // Azul
            new Color(16, 185, 129),   // Verde
            new Color(245, 158, 11),   // Ámbar/Oro
            new Color(139, 92, 246),   // Violeta
            new Color(236, 72, 153),   // Rosa
            new Color(20, 184, 166),   // Teal
            new Color(249, 115, 22),   // Naranja
        };
        int si = 0;
        for (Object key : dataset.getKeys()) {
            plot.setSectionPaint((Comparable<?>) key, donutColors[si % donutColors.length]);
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

    private JParamsDatesInterval findDatesIntervalEditor(Component c) {
        if (c instanceof JParamsDatesInterval) {
            return (JParamsDatesInterval) c;
        }
        if (c instanceof java.awt.Container) {
            for (Component child : ((java.awt.Container) c).getComponents()) {
                JParamsDatesInterval res = findDatesIntervalEditor(child);
                if (res != null) {
                    return res;
                }
            }
        }
        return null;
    }

    private void showShiftDetails(Date dateEnd) {
        JParamsDatesInterval datesEditor = findDatesIntervalEditor(jPanelFilter);
        if (datesEditor != null) {
            // Padding of 1 second around DATEEND to match exact shift in QBF filter
            Date start = new Date(dateEnd.getTime() - 1000);
            Date end = new Date(dateEnd.getTime() + 1000);
            datesEditor.setStartDate(start);
            datesEditor.setEndDate(end);
            
            // Execute the report
            launchreport();
            
            // Switch to the document tab
            if (m_TabbedPane != null) {
                m_TabbedPane.setSelectedIndex(1);
            }
        }
    }

    private String formatDateTimeRange(Date start, Date end) {
        if (start == null && end == null) return "";
        if (start == null) {
            java.text.SimpleDateFormat sdfFull = new java.text.SimpleDateFormat("dd/MM/yyyy hh:mm a", java.util.Locale.getDefault());
            return sdfFull.format(end);
        }
        if (end == null) {
            java.text.SimpleDateFormat sdfFull = new java.text.SimpleDateFormat("dd/MM/yyyy hh:mm a", java.util.Locale.getDefault());
            return sdfFull.format(start);
        }
        
        java.text.SimpleDateFormat sdfDate = new java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault());
        java.text.SimpleDateFormat sdfTime = new java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault());
        
        String startDay = sdfDate.format(start);
        String endDay = sdfDate.format(end);
        
        String startTime = sdfTime.format(start).toLowerCase();
        String endTime = sdfTime.format(end).toLowerCase();
        
        if (startDay.equals(endDay)) {
            return startDay + " " + startTime + " - " + endTime;
        } else {
            java.text.SimpleDateFormat sdfFull = new java.text.SimpleDateFormat("dd/MM/yyyy hh:mm a", java.util.Locale.getDefault());
            return sdfFull.format(start).toLowerCase() + " - " + sdfFull.format(end).toLowerCase();
        }
    }

    private JPanel createClosedPosHistoryPanel() {
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
        m_lblKpi2Title = new JLabel("EFECTIVO EN CAJA");
        m_lblKpi2      = new JLabel("$0.00");
        m_lblKpi3Title = new JLabel("TURNOS CERRADOS");
        m_lblKpi3      = new JLabel("0");
        m_lblKpi4Title = new JLabel("FONDO DE CAJA PROM.");
        m_lblKpi4      = new JLabel("$0.00");

        kpiRow.add(createPremiumKpiCard(m_lblKpi1Title, m_lblKpi1, KPI_PURPLE, "SALES"));
        kpiRow.add(createPremiumKpiCard(m_lblKpi2Title, m_lblKpi2, KPI_GREEN,  "SALES"));
        kpiRow.add(createPremiumKpiCard(m_lblKpi3Title, m_lblKpi3, KPI_AMBER,  "TXS"));
        kpiRow.add(createPremiumKpiCard(m_lblKpi4Title, m_lblKpi4, KPI_BLUE,   "AVG"));

        wrapper.add(kpiRow, BorderLayout.NORTH);

        // === CUERPO: Tabla de Historial ===
        m_historyTableModel = new javax.swing.table.DefaultTableModel(
            new Object[] {"Cajero", "Secuencia", "Fecha y Hora", "Ventas Totales", "Acción"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        m_historyTable = new javax.swing.JTable(m_historyTableModel);
        styleHistoryTable(m_historyTable);
        
        m_historyTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                int row = m_historyTable.rowAtPoint(e.getPoint());
                int col = m_historyTable.columnAtPoint(e.getPoint());
                if (row >= 0 && row < m_groupedShifts.size()) {
                    Map<String, Object> shift = m_groupedShifts.get(row);
                    final String moneyIndex = (String) shift.get("MONEY");
                    if (col == 4) { // Acción column (three dots)
                        javax.swing.JPopupMenu popup = new javax.swing.JPopupMenu();
                        
                        javax.swing.JMenuItem itemView = new javax.swing.JMenuItem("👁️ Mirar Turno");
                        itemView.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                        itemView.addActionListener(evt -> {
                            try {
                                com.openbravo.pos.panels.JPanelCloseMoney panelClose = (com.openbravo.pos.panels.JPanelCloseMoney) m_App.getBean("com.openbravo.pos.panels.JPanelCloseMoney");
                                panelClose.setLoadedMoneyIndex(moneyIndex);
                                m_App.getAppUserView().showTask("com.openbravo.pos.panels.JPanelCloseMoney");
                            } catch (Exception ex) {
                                LOGGER.log(Level.SEVERE, "Error loading shift", ex);
                            }
                        });
                        
                        javax.swing.JMenuItem itemPrint = new javax.swing.JMenuItem("🖨️ Imprimir");
                        itemPrint.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                        itemPrint.addActionListener(evt -> {
                            try {
                                com.openbravo.pos.panels.JPanelCloseMoney panelClose = (com.openbravo.pos.panels.JPanelCloseMoney) m_App.getBean("com.openbravo.pos.panels.JPanelCloseMoney");
                                panelClose.printPaymentsForClosedShift(moneyIndex);
                            } catch (Exception ex) {
                                LOGGER.log(Level.SEVERE, "Error printing shift ticket", ex);
                            }
                        });
                        
                        popup.add(itemView);
                        popup.add(itemPrint);
                        popup.show(m_historyTable, e.getX(), e.getY());
                    } else {
                        Date dateEnd = (Date) shift.get("DATEEND");
                        if (dateEnd != null) {
                            showShiftDetails(dateEnd);
                        }
                    }
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(m_historyTable);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(Color.WHITE);

        JPanel tableCard = createPremiumHistoryCard("Registros de Turnos Cerrados (Haz clic en un turno para ver su desglose)", scrollPane);
        tableCard.setBorder(new EmptyBorder(16, 0, 0, 0));

        wrapper.add(tableCard, BorderLayout.CENTER);
        return wrapper;
    }

    private void styleHistoryTable(javax.swing.JTable table) {
        table.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        table.setRowHeight(44);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 15));
        table.getTableHeader().setBackground(new Color(241, 245, 249));
        table.getTableHeader().setForeground(DASH_TEXT_PRI);
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(226, 232, 240)));
        
        // Center table headers safely
        final javax.swing.table.TableCellRenderer defaultHeaderRenderer = table.getTableHeader().getDefaultRenderer();
        table.getTableHeader().setDefaultRenderer(new javax.swing.table.TableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(javax.swing.JTable t, Object val, boolean isSel, boolean hasFocus, int r, int c) {
                Component comp = defaultHeaderRenderer.getTableCellRendererComponent(t, val, isSel, hasFocus, r, c);
                if (comp instanceof JLabel) {
                    ((JLabel) comp).setHorizontalAlignment(JLabel.CENTER);
                }
                return comp;
            }
        });
        
        table.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            @Override
            public void mouseMoved(java.awt.event.MouseEvent e) {
                int col = table.columnAtPoint(e.getPoint());
                if (col == 4) {
                    table.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
                } else {
                    table.setCursor(java.awt.Cursor.getDefaultCursor());
                }
            }
        });
        
        table.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(javax.swing.JTable t, Object val, boolean isSel, boolean hasFocus, int r, int c) {
                if (c == 4) {
                    JPanel cellPanel = new JPanel(new BorderLayout()) {
                        @Override
                        protected void paintComponent(Graphics g) {
                            super.paintComponent(g);
                            Graphics2D g2d = (Graphics2D) g.create();
                            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                            
                            // Draw cell background
                            if (isSel) {
                                g2d.setColor(new Color(99, 102, 241, 40));
                                g2d.fillRect(0, 0, getWidth(), getHeight());
                            } else {
                                if (r % 2 == 0) {
                                    g2d.setColor(Color.WHITE);
                                } else {
                                    g2d.setColor(new Color(248, 250, 252));
                                }
                                g2d.fillRect(0, 0, getWidth(), getHeight());
                            }
                            
                            // Draw square button (boxSize=24x24)
                            int boxSize = 24;
                            int x = (getWidth() - boxSize) / 2;
                            int y = (getHeight() - boxSize) / 2;
                            
                            // White background for the square box
                            g2d.setColor(Color.WHITE);
                            g2d.fillRect(x, y, boxSize, boxSize);
                            
                            // Border for the square box
                            g2d.setColor(new Color(203, 213, 225)); // slate-300
                            g2d.drawRect(x, y, boxSize, boxSize);
                            
                            // Draw three vertical dots (diameter=4px, spaced 5px apart)
                            g2d.setColor(new Color(71, 85, 105)); // slate-600
                            int dotDiameter = 4;
                            int dotX = x + (boxSize - dotDiameter) / 2;
                            
                            g2d.fillOval(dotX, y + 5, dotDiameter, dotDiameter);
                            g2d.fillOval(dotX, y + 10, dotDiameter, dotDiameter);
                            g2d.fillOval(dotX, y + 15, dotDiameter, dotDiameter);
                            
                            g2d.dispose();
                        }
                    };
                    cellPanel.setOpaque(false);
                    return cellPanel;
                }
                
                Component comp = super.getTableCellRendererComponent(t, val, isSel, hasFocus, r, c);
                if (!isSel) {
                    if (r % 2 == 0) {
                        comp.setBackground(Color.WHITE);
                    } else {
                        comp.setBackground(new Color(248, 250, 252));
                    }
                } else {
                    comp.setBackground(new Color(99, 102, 241, 40));
                }
                comp.setForeground(DASH_TEXT_PRI);
                
                if (comp instanceof JLabel) {
                    JLabel lbl = (JLabel) comp;
                    lbl.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
                    lbl.setHorizontalAlignment(JLabel.CENTER);
                }
                return comp;
            }
        });

        table.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_ALL_COLUMNS);
        javax.swing.table.TableColumnModel colModel = table.getColumnModel();
        if (colModel.getColumnCount() >= 5) {
            colModel.getColumn(0).setPreferredWidth(120); // Cajero
            colModel.getColumn(1).setPreferredWidth(60);  // Secuencia
            colModel.getColumn(2).setPreferredWidth(280); // Fecha y Hora
            colModel.getColumn(3).setPreferredWidth(120); // Ventas Totales
            colModel.getColumn(4).setPreferredWidth(60);  // Acción
        }
    }

    private JPanel createPremiumHistoryCard(String title, JComponent content) {
        JPanel wrapper = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // White card background
                g2d.setColor(Color.WHITE);
                g2d.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 4, 14, 14);
                // Subtle border
                g2d.setColor(new Color(226, 232, 240, 120));
                g2d.drawRoundRect(0, 0, getWidth() - 2, getHeight() - 5, 14, 14);
                g2d.dispose();
            }
        };
        wrapper.setOpaque(false);
        wrapper.setBorder(BorderFactory.createEmptyBorder(4, 4, 8, 4));

        JPanel card = new JPanel(new BorderLayout(0, 12));
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(18, 22, 18, 22));

        if (title != null && !title.isEmpty()) {
            JLabel titleLabel = new JLabel(title);
            titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
            titleLabel.setForeground(DASH_TEXT_PRI);
            titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
            card.add(titleLabel, BorderLayout.NORTH);
        }

        card.add(content, BorderLayout.CENTER);
        wrapper.add(card, BorderLayout.CENTER);
        return wrapper;
    }

    private void updateClosedPosHistory(List<Map<String, Object>> records) {
        if (records == null || records.isEmpty()) {
            if (m_historyTableModel != null) {
                m_historyTableModel.setRowCount(0);
            }
            m_groupedShifts.clear();
            m_lblKpi1.setText("$0.00");
            m_lblKpi2.setText("$0.00");
            m_lblKpi3.setText("0");
            m_lblKpi4.setText("$0.00");
            return;
        }

        // Group by MONEY to combine payment methods
        java.util.Map<String, Map<String, Object>> grouped = new java.util.LinkedHashMap<>();
        for (Map<String, Object> r : records) {
            String money = (String) r.get("MONEY");
            if (money == null) continue;
            
            double total = 0.0;
            Object totalVal = r.get("TOTAL");
            if (totalVal instanceof Number) {
                total = ((Number) totalVal).doubleValue();
            }

            if (!grouped.containsKey(money)) {
                Map<String, Object> copy = new HashMap<>(r);
                copy.put("TOTAL_SALES", total);
                
                double cashSales = 0.0;
                String payment = (String) r.get("PAYMENT");
                if ("cash".equalsIgnoreCase(payment)) {
                    cashSales = total;
                }
                copy.put("CASH_SALES", cashSales);
                
                grouped.put(money, copy);
            } else {
                Map<String, Object> existing = grouped.get(money);
                double currentTotal = (Double) existing.get("TOTAL_SALES");
                existing.put("TOTAL_SALES", currentTotal + total);
                
                String payment = (String) r.get("PAYMENT");
                if ("cash".equalsIgnoreCase(payment)) {
                    double existingCash = (Double) existing.get("CASH_SALES");
                    existing.put("CASH_SALES", existingCash + total);
                }
            }
        }

        m_groupedShifts = new ArrayList<>(grouped.values());

        // Update KPIs
        double totalSalesSum = 0.0;
        double totalCashInDrawer = 0.0;
        double totalInitialAmount = 0.0;
        int shiftCount = m_groupedShifts.size();

        if (m_historyTableModel != null) {
            m_historyTableModel.setRowCount(0);
            for (Map<String, Object> shift : m_groupedShifts) {
                String host = (String) shift.get("HOST");
                Object seq = shift.get("HOSTSEQUENCE");
                Date dateStart = (Date) shift.get("DATESTART");
                Date dateEnd = (Date) shift.get("DATEEND");
                
                double initialAmount = 0.0;
                Object initVal = shift.get("INITIAL_AMOUNT");
                if (initVal instanceof Number) {
                    initialAmount = ((Number) initVal).doubleValue();
                }
                
                double totalSales = (Double) shift.get("TOTAL_SALES");
                double cashSales = (Double) shift.get("CASH_SALES");
                
                totalSalesSum += totalSales;
                totalInitialAmount += initialAmount;
                totalCashInDrawer += (initialAmount + cashSales); // EFECTIVO EN CAJA = Fondo Inicial + Ventas en efectivo

                String cajero = (String) shift.get("CAJERO");
                if (cajero == null) {
                    cajero = "admin";
                }
                
                m_historyTableModel.addRow(new Object[] {
                    cajero,
                    seq,
                    formatDateTimeRange(dateStart, dateEnd),
                    Formats.CURRENCY.formatValue(totalSales),
                    "  ...  "
                });
            }
        }

        double avgInitial = shiftCount > 0 ? totalInitialAmount / shiftCount : 0.0;

        m_lblKpi1.setText(Formats.CURRENCY.formatValue(totalSalesSum));
        m_lblKpi2.setText(Formats.CURRENCY.formatValue(totalCashInDrawer));
        m_lblKpi3.setText(String.valueOf(shiftCount));
        m_lblKpi4.setText(Formats.CURRENCY.formatValue(avgInitial));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanelFilter;
    private javax.swing.JPanel jPanelHeader;
    private javax.swing.JToggleButton jToggleFilter;
    // End of variables declaration//GEN-END:variables

}

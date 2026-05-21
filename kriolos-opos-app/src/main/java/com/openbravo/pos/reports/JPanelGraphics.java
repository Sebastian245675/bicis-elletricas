package com.openbravo.pos.reports;

import com.openbravo.basic.BasicException;
import com.openbravo.data.loader.Session;
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.pos.forms.AppView;
import com.openbravo.pos.forms.JPanelView;
import com.openbravo.pos.forms.BeanFactoryApp;
import com.openbravo.pos.forms.BeanFactoryException;
import com.openbravo.format.Formats;
import org.jfree.chart.*;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.plot.*;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Panel de Gráficos - Estilo Eleventa
 * Muestra gráficos de ventas por departamento, forma de pago, y periodos
 */
public class JPanelGraphics extends JPanel implements JPanelView, BeanFactoryApp {
    
    private static final Logger LOGGER = Logger.getLogger(JPanelGraphics.class.getName());
    
    private AppView m_App;
    private Session session;
    
    // Componentes UI
    private ChartPanel chartPanelSalesProfit; // Gráfico principal de Ventas y Ganancias
    private ChartPanel chartPanelDepartment; // Gráfico de dona para ganancia por departamento
    private ChartPanel chartPanelMonthDonut; // Gráfico de dona para ventas por mes
    private ChartPanel chartPanelPayment; // Gráfico de barras: Ventas por forma de pago
    private JTable tableDepartment;
    private JTable tableDepartmentProfit; // Tabla de ganancias por departamento
    private JTable tableSalesByMonth;
    private JLabel lblTotalSales;
    private ChartPanel chartPanelHourly; // Gráfico de línea: Ventas por hora
    
    // Labels para métricas de ventas
    private JLabel lblSalesTotal;
    private JLabel lblTotalProfit;
    private JLabel lblNumberOfSales;
    private JLabel lblAverageSale;
    private JLabel lblProfitMargin;
    
    // Componentes para tabs y diseño Eleventa
    private JButton[] tabButtons;
    private int currentTabIndex = 0;
    private int currentPeriodIndex = 1;
    private JLabel lblMainTitle;
    private JLabel[] periodLinks;
    private JPanel deptListPanel; // Panel para la lista de departamentos (en lugar de tabla)
    private JPanel taxesBreakdownPanel;

    private static final Color APP_BG = new Color(239, 244, 255);
    private static final Color SURFACE_BG = Color.WHITE;
    private static final Color HEADER_DARK = new Color(16, 46, 80);
    private static final Color HEADER_LIGHT = new Color(56, 139, 253);
    private static final Color TEXT_PRIMARY = new Color(15, 23, 42);
    private static final Color TEXT_MUTED = new Color(100, 116, 139);
    private static final Color BORDER_SOFT = new Color(219, 234, 254);
    private static final Color ACCENT_BLUE = new Color(37, 99, 235);
    private static final Color ACCENT_CYAN = new Color(6, 182, 212);
    private static final Color ACCENT_GREEN = new Color(16, 185, 129);
    private static final Color ACCENT_GOLD = new Color(245, 158, 11);
    
    // Datos actuales
    private java.util.Date dateStart;
    private java.util.Date dateEnd;
    private String activeCashIndex;
    
    // Clases de datos
    public static class DepartmentData {
        public String name;
        public double sales;
        public double profit;
        public int index;
        
        public DepartmentData(String name, double sales, double profit, int index) {
            this.name = name;
            this.sales = sales;
            this.profit = profit;
            this.index = index;
        }
    }
    
    public static class PaymentData {
        public String type;
        public String displayName;
        public double amount;
        
        public PaymentData(String type, String displayName, double amount) {
            this.type = type;
            this.displayName = displayName;
            this.amount = amount;
        }
    }
    
    public JPanelGraphics() {
        initComponents();
    }
    
    @Override
    public void init(AppView app) throws BeanFactoryException {
        m_App = app;
        session = m_App.getSession();
        activeCashIndex = m_App.getActiveCashIndex();
        
        LOGGER.info("JPanelGraphics.init() llamado. CashIndex: " + activeCashIndex);
        
        // Los datos se cargarán cuando se active el panel
    }
    
    @Override
    public Object getBean() {
        return this;
    }
    
    private void initComponents() {
        setLayout(new BorderLayout(0, 0));
        setBackground(APP_BG);
        
        // ========== BARRA SUPERIOR AZUL OSCURA CON TÍTULO "REPORTES" ==========
        JPanel topBlueBar = createTopBar();
        topBlueBar.setBackground(new Color(41, 57, 80)); // Azul oscuro como Eleventa
        topBlueBar.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));
        topBlueBar.setPreferredSize(new Dimension(0, 50));
        
        // Título "REPORTES" a la izquierda
        JLabel reportsTitle = new JLabel("REPORTES");
        reportsTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        reportsTitle.setForeground(Color.WHITE);
        topBlueBar.add(reportsTitle, BorderLayout.WEST);

        JLabel headerSubtitle = new JLabel("Tendencias, rentabilidad y movimiento comercial en una sola vista");
        headerSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        headerSubtitle.setForeground(new Color(219, 234, 254));
        headerSubtitle.setBorder(BorderFactory.createEmptyBorder(6, 0, 0, 0));
        topBlueBar.add(headerSubtitle, BorderLayout.SOUTH);

        topBlueBar.setBorder(BorderFactory.createEmptyBorder(18, 26, 18, 26));
        topBlueBar.setPreferredSize(new Dimension(0, 92));
        
        // Tabs a la derecha: "Reporte de Ventas" y "Ventas por cliente"
        JPanel tabsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        tabsPanel.setOpaque(false);
        
        tabButtons = new JButton[2];
        String[] tabNames = {"Reporte de Ventas", "Ventas por cliente"};
        
        for (int i = 0; i < 2; i++) {
            final int tabIndex = i;
            tabButtons[i] = new JButton(tabNames[i]) {
                private boolean isHovered = false;
                {
                    addMouseListener(new java.awt.event.MouseAdapter() {
                        @Override
                        public void mouseEntered(java.awt.event.MouseEvent e) {
                            isHovered = true;
                            repaint();
                        }
                        @Override
                        public void mouseExited(java.awt.event.MouseEvent e) {
                            isHovered = false;
                            repaint();
                        }
                    });
                }

                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2d = (Graphics2D) g.create();
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    
                    int w = getWidth();
                    int h = getHeight();
                    
                    Color bg;
                    boolean isActive = (currentTabIndex == tabIndex);
                    
                    if (isActive) {
                        bg = isHovered ? new Color(255, 255, 255, 80) : new Color(255, 255, 255, 54);
                    } else {
                        bg = isHovered ? new Color(255, 255, 255, 40) : new Color(255, 255, 255, 20);
                    }
                    
                    if (getModel().isPressed()) {
                        bg = new Color(255, 255, 255, 100);
                    }
                    
                    g2d.setColor(bg);
                    g2d.fillRoundRect(0, 0, w, h, 8, 8);
                    g2d.dispose();
                    
                    super.paintComponent(g);
                }
            };
            tabButtons[i].setFont(new Font("Segoe UI", Font.BOLD, 13));
            tabButtons[i].setPreferredSize(new Dimension(172, 36));
            tabButtons[i].setForeground(Color.WHITE);
            tabButtons[i].setFocusPainted(false);
            tabButtons[i].setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
            tabButtons[i].setContentAreaFilled(false);
            tabButtons[i].setOpaque(false);
            
            // Estilo inicial: primer tab activo (azul claro)
            if (i == 0) {
                tabButtons[i].setBackground(new Color(255, 255, 255, 54));
            } else {
                tabButtons[i].setBackground(new Color(255, 255, 255, 20));
            }
            
            tabButtons[i].addActionListener(e -> {
                // Resetear todos los tabs
                for (int j = 0; j < tabButtons.length; j++) {
                    if (j == tabIndex) {
                        tabButtons[j].setBackground(new Color(255, 255, 255, 54));
                    } else {
                        tabButtons[j].setBackground(new Color(255, 255, 255, 20));
                    }
                }
                currentTabIndex = tabIndex;
                // TODO: Cambiar el contenido según el tab
            });
            
            tabsPanel.add(tabButtons[i]);
        }
        
        topBlueBar.add(tabsPanel, BorderLayout.EAST);
        add(topBlueBar, BorderLayout.NORTH);
        
        // ========== CONTENIDO PRINCIPAL ==========
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(APP_BG);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        
        // Crear el panel de gráficos principal con scroll
        JPanel mainChartsPanel = createMainChartsPanel();
        JScrollPane scrollPane = new JScrollPane(mainChartsPanel);
        scrollPane.setBorder(null);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        contentPanel.add(scrollPane, BorderLayout.CENTER);
        
        add(contentPanel, BorderLayout.CENTER);
    }

    private JPanel createTopBar() {
        JPanel topBar = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setPaint(new GradientPaint(0, 0, HEADER_DARK, getWidth(), getHeight(), HEADER_LIGHT));
                g2d.fillRect(0, 0, getWidth(), getHeight());
                g2d.setColor(new Color(255, 255, 255, 28));
                g2d.fillOval(getWidth() - 180, -50, 220, 220);
                g2d.setColor(new Color(255, 255, 255, 16));
                g2d.fillOval(getWidth() - 320, 10, 180, 180);
                g2d.dispose();
                super.paintComponent(g);
            }
        };
        topBar.setOpaque(false);
        return topBar;
    }

    private JPanel createMetricSummaryCard(String title, String hint, Color accent, JLabel valueLabel) {
        JPanel card = new JPanel(new BorderLayout(0, 12));
        card.setOpaque(true);
        card.setBackground(SURFACE_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
            BorderFactory.createEmptyBorder(16, 16, 16, 16)
        ));

        JPanel header = new JPanel(new BorderLayout(8, 0));
        header.setOpaque(false);

        JPanel dot = new JPanel();
        dot.setBackground(accent);
        dot.setPreferredSize(new Dimension(10, 10));
        header.add(dot, BorderLayout.WEST);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        titleLabel.setForeground(TEXT_MUTED);
        header.add(titleLabel, BorderLayout.CENTER);
        card.add(header, BorderLayout.NORTH);

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        card.add(valueLabel, BorderLayout.CENTER);

        JLabel hintLabel = new JLabel(hint);
        hintLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        hintLabel.setForeground(TEXT_MUTED);
        card.add(hintLabel, BorderLayout.SOUTH);

        return card;
    }
    
    private JPanel createMainChartsPanel() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(APP_BG);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30)); // Más padding general
        
        // ========== TÍTULO PRINCIPAL Y LINKS DE PERÍODO ==========
        JPanel titlePanel = new JPanel(new BorderLayout(15, 8));
        titlePanel.setBackground(APP_BG);
        titlePanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 2, 0, BORDER_SOFT),
            BorderFactory.createEmptyBorder(0, 0, 25, 0)
        ));
        
        // Título dinámico "Resumen de Ventas de [Mes]"
        Calendar cal = Calendar.getInstance();
        String[] monthNames = {"Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", 
                              "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"};
        String currentMonth = monthNames[cal.get(Calendar.MONTH)];
        lblMainTitle = new JLabel("Resumen de Ventas de " + currentMonth);
        lblMainTitle.setFont(new Font("Segoe UI", Font.BOLD, 30)); // Más grande
        lblMainTitle.setForeground(TEXT_PRIMARY);
        lblMainTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));
        titlePanel.add(lblMainTitle, BorderLayout.WEST);
        
        // Links de período (estilo Eleventa - como links subrayados)
        JPanel periodLinksPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 22, 0));
        periodLinksPanel.setBackground(APP_BG);
        periodLinksPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        
        periodLinks = new JLabel[5];
        String[] periodLabels = {"Semana Actual", "Mes Actual", "Mes Anterior", "Año actual", "Periodo..."};
        
        for (int i = 0; i < 5; i++) {
            final int periodIndex = i;
            periodLinks[i] = new JLabel("<html><u>" + periodLabels[i] + "</u></html>");
            periodLinks[i].setFont(new Font("Segoe UI", Font.PLAIN, 14)); // Más grande
            periodLinks[i].setForeground(new Color(59, 130, 246)); // Azul más vibrante
            periodLinks[i].setCursor(new Cursor(Cursor.HAND_CURSOR));
            periodLinks[i].setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8)); // Padding para hover
            
            // Estilo para el link seleccionado (Mes Actual por defecto)
            if (i == 1) { // Mes Actual
                periodLinks[i].setForeground(new Color(37, 99, 235)); // Azul más oscuro y sólido
                periodLinks[i].setFont(new Font("Segoe UI", Font.BOLD, 14));
                periodLinks[i].setOpaque(true);
                periodLinks[i].setBackground(new Color(239, 246, 255)); // Fondo azul claro
            }
            
            periodLinks[i].addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent evt) {
                    currentPeriodIndex = periodIndex;
                    // Resetear todos los links
                    for (int j = 0; j < periodLinks.length; j++) {
                        periodLinks[j].setForeground(new Color(59, 130, 246));
                        periodLinks[j].setFont(new Font("Segoe UI", Font.PLAIN, 14));
                        periodLinks[j].setOpaque(false);
                        periodLinks[j].setBackground(new Color(249, 250, 251));
                    }
                    // Resaltar el link seleccionado
                    periodLinks[periodIndex].setForeground(new Color(37, 99, 235));
                    periodLinks[periodIndex].setFont(new Font("Segoe UI", Font.BOLD, 14));
                    periodLinks[periodIndex].setOpaque(true);
                    periodLinks[periodIndex].setBackground(new Color(239, 246, 255));
                    
                    // Mapear índices de links a índices internos:
                    // periodIndex 0 = "Semana Actual" -> internalIndex 1
                    // periodIndex 1 = "Mes Actual" -> internalIndex 2
                    // periodIndex 2 = "Mes Anterior" -> internalIndex 5 (nuevo)
                    // periodIndex 3 = "Año actual" -> internalIndex 3
                    // periodIndex 4 = "Periodo..." -> internalIndex 4
                    int internalIndex;
                    String periodLabel = "";
                    String[] monthNames = {"Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", 
                                          "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"};
                    
                    switch (periodIndex) {
                        case 0: // Semana Actual
                            internalIndex = 1;
                            periodLabel = "Semana Actual";
                            break;
                        case 1: // Mes Actual
                            internalIndex = 2;
                            Calendar cal = Calendar.getInstance();
                            periodLabel = monthNames[cal.get(Calendar.MONTH)];
                            break;
                        case 2: // Mes Anterior
                            internalIndex = 5;
                            Calendar prevCal = Calendar.getInstance();
                            prevCal.add(Calendar.MONTH, -1);
                            periodLabel = monthNames[prevCal.get(Calendar.MONTH)];
                            break;
                        case 3: // Año actual
                            internalIndex = 3;
                            periodLabel = String.valueOf(Calendar.getInstance().get(Calendar.YEAR));
                            break;
                        case 4: // Periodo personalizado
                            showCustomPeriodDialog();
                            return;
                        default:
                            internalIndex = 2; // Por defecto Mes Actual
                            Calendar defaultCal = Calendar.getInstance();
                            periodLabel = monthNames[defaultCal.get(Calendar.MONTH)];
                            break;
                    }
                    
                    if (periodIndex != 4) {
                        // Actualizar título dinámicamente
                        if (periodIndex == 1 || periodIndex == 2) {
                            lblMainTitle.setText("Resumen de Ventas de " + periodLabel);
                        } else if (periodIndex == 3) {
                            lblMainTitle.setText("Resumen de Ventas del Año " + periodLabel);
                        } else {
                            lblMainTitle.setText("Resumen de Ventas - " + periodLabel);
                        }
                        
                        loadDataForPeriod(internalIndex);
                    }
                }
                @Override
                public void mouseEntered(java.awt.event.MouseEvent evt) {
                    if (periodIndex != currentPeriodIndex) { // Solo cambiar si no está seleccionado
                        periodLinks[periodIndex].setForeground(new Color(37, 99, 235));
                        periodLinks[periodIndex].setOpaque(true);
                        periodLinks[periodIndex].setBackground(new Color(239, 246, 255));
                    }
                }
                @Override
                public void mouseExited(java.awt.event.MouseEvent evt) {
                    if (periodIndex != currentPeriodIndex) { // No cambiar si es el seleccionado
                        periodLinks[periodIndex].setForeground(new Color(59, 130, 246));
                        periodLinks[periodIndex].setOpaque(false);
                        periodLinks[periodIndex].setBackground(new Color(249, 250, 251));
                    }
                }
            });
            
            periodLinksPanel.add(periodLinks[i]);
        }
        
        titlePanel.add(periodLinksPanel, BorderLayout.CENTER);
        mainPanel.add(titlePanel);
        
        // ========== GRÁFICO PRINCIPAL: VENTAS Y GANANCIAS (más pequeño, a la izquierda) ==========
        JPanel salesProfitContainer = new JPanel(new BorderLayout(0, 18));
        salesProfitContainer.setBackground(APP_BG);
        salesProfitContainer.setBorder(BorderFactory.createEmptyBorder(0, 0, 30, 0));
        
        // Panel izquierdo: Gráfico más pequeño
        JPanel chartLeftPanel = new JPanel(new BorderLayout());
        chartLeftPanel.setBackground(APP_BG);
        
        // Gráfico principal de Ventas y Ganancias (más pequeño y compacto)
        chartPanelSalesProfit = new ChartPanel(createEmptySalesProfitBarChart());
        chartPanelSalesProfit.setPreferredSize(new Dimension(960, 300));
        chartPanelSalesProfit.setBackground(Color.WHITE);
        chartPanelSalesProfit.setDomainZoomable(false);
        chartPanelSalesProfit.setRangeZoomable(false);
        chartPanelSalesProfit.setMouseWheelEnabled(false);
        
        // Panel con sombra sutil usando border elevado
        JPanel salesProfitWrapper = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Sombra sutil
                for (int i = 0; i < 5; i++) {
                    int alpha = 5 - i;
                    g2d.setColor(new Color(0, 0, 0, alpha));
                    g2d.fillRoundRect(5 + i, 5 + i, getWidth() - 10 - 2*i, getHeight() - 10 - 2*i, 8, 8);
                }
                
                // Panel blanco encima
                g2d.setColor(Color.WHITE);
                g2d.fillRoundRect(5, 5, getWidth() - 10, getHeight() - 10, 8, 8);
                g2d.dispose();
            }
        };
        salesProfitWrapper.setOpaque(false);
        salesProfitWrapper.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        JPanel innerPanel = new JPanel(new BorderLayout());
        innerPanel.setOpaque(false);
        innerPanel.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));
        innerPanel.add(chartPanelSalesProfit, BorderLayout.CENTER);
        salesProfitWrapper.add(innerPanel, BorderLayout.CENTER);
        
        chartLeftPanel.add(salesProfitWrapper, BorderLayout.CENTER);
        
        JPanel metricsPanel = createSalesMetricsPanelEleventa();
        
        salesProfitContainer.add(metricsPanel, BorderLayout.NORTH);
        salesProfitContainer.add(chartLeftPanel, BorderLayout.CENTER);
        
        mainPanel.add(salesProfitContainer);
        
        // ========== GRÁFICO DE BARRAS: VENTAS POR FORMA DE PAGO (más pequeño, a la izquierda) ==========
        JPanel paymentChartContainer = new JPanel(new GridLayout(1, 2, 18, 0));
        paymentChartContainer.setBackground(APP_BG);
        paymentChartContainer.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        
        // Panel izquierdo: Gráfico de barras más pequeño
        JPanel paymentLeftPanel = new JPanel(new BorderLayout());
        paymentLeftPanel.setBackground(Color.WHITE);
        
        // Título del gráfico
        JLabel paymentChartTitle = new JLabel("Ventas por forma de pago");
        paymentChartTitle.setFont(new Font("Segoe UI", Font.BOLD, 19)); // Más grande
        paymentChartTitle.setForeground(new Color(17, 24, 39)); // Gris muy oscuro
        paymentChartTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        paymentLeftPanel.add(paymentChartTitle, BorderLayout.NORTH);
        
        // Gráfico de barras más pequeño
        chartPanelPayment = new ChartPanel(createEmptyBarChart());
        chartPanelPayment.setPreferredSize(new Dimension(450, 200)); // Más pequeño como Eleventa
        chartPanelPayment.setBackground(Color.WHITE);
        chartPanelPayment.setDomainZoomable(false);
        chartPanelPayment.setRangeZoomable(false);
        chartPanelPayment.setMouseWheelEnabled(false);
        
        // Panel con sombra sutil
        JPanel barChartWrapper = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Sombra sutil
                for (int i = 0; i < 5; i++) {
                    int alpha = 5 - i;
                    g2d.setColor(new Color(0, 0, 0, alpha));
                    g2d.fillRoundRect(5 + i, 5 + i, getWidth() - 10 - 2*i, getHeight() - 10 - 2*i, 8, 8);
                }
                
                // Panel blanco encima
                g2d.setColor(Color.WHITE);
                g2d.fillRoundRect(5, 5, getWidth() - 10, getHeight() - 10, 8, 8);
                g2d.dispose();
            }
        };
        barChartWrapper.setOpaque(false);
        barChartWrapper.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        JPanel innerBarPanel = new JPanel(new BorderLayout());
        innerBarPanel.setOpaque(false);
        innerBarPanel.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
        innerBarPanel.add(chartPanelPayment, BorderLayout.CENTER);
        barChartWrapper.add(innerBarPanel, BorderLayout.CENTER);
        
        paymentLeftPanel.add(barChartWrapper, BorderLayout.CENTER);
        
        // Solo el gráfico de barras a la izquierda (sin gráfico de dona aquí)
        paymentLeftPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
            BorderFactory.createEmptyBorder(14, 14, 14, 14)
        ));
        paymentChartContainer.add(paymentLeftPanel);
        
        // Panel derecho: Gráfico de ventas por hora
        JPanel hourlyRightPanel = new JPanel(new BorderLayout());
        hourlyRightPanel.setBackground(Color.WHITE);
        
        JLabel hourlyChartTitle = new JLabel("Ventas por hora (Secuencia)");
        hourlyChartTitle.setFont(new Font("Segoe UI", Font.BOLD, 19));
        hourlyChartTitle.setForeground(new Color(17, 24, 39));
        hourlyChartTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        hourlyRightPanel.add(hourlyChartTitle, BorderLayout.NORTH);
        
        chartPanelHourly = new ChartPanel(createEmptyHourlyChart());
        chartPanelHourly.setPreferredSize(new Dimension(500, 200));
        chartPanelHourly.setBackground(Color.WHITE);
        chartPanelHourly.setDomainZoomable(false);
        chartPanelHourly.setRangeZoomable(false);
        
        JPanel hourlyChartWrapper = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                for (int i = 0; i < 5; i++) {
                    int alpha = 5 - i;
                    g2d.setColor(new Color(0, 0, 0, alpha));
                    g2d.fillRoundRect(5 + i, 5 + i, getWidth() - 10 - 2*i, getHeight() - 10 - 2*i, 8, 8);
                }
                g2d.setColor(Color.WHITE);
                g2d.fillRoundRect(5, 5, getWidth() - 10, getHeight() - 10, 8, 8);
                g2d.dispose();
            }
        };
        hourlyChartWrapper.setOpaque(false);
        hourlyChartWrapper.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        JPanel innerHourlyPanel = new JPanel(new BorderLayout());
        innerHourlyPanel.setOpaque(false);
        innerHourlyPanel.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
        innerHourlyPanel.add(chartPanelHourly, BorderLayout.CENTER);
        hourlyChartWrapper.add(innerHourlyPanel, BorderLayout.CENTER);
        
        hourlyRightPanel.add(hourlyChartWrapper, BorderLayout.CENTER);
        hourlyRightPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
            BorderFactory.createEmptyBorder(14, 14, 14, 14)
        ));
        paymentChartContainer.add(hourlyRightPanel);
        
        mainPanel.add(paymentChartContainer);
        
        // ========== SECCIÓN INFERIOR: DOS COLUMNAS ==========
        JPanel bottomSectionPanel = new JPanel(new GridLayout(1, 2, 18, 0));
        bottomSectionPanel.setBackground(APP_BG);
        bottomSectionPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        
        // ========== COLUMNA IZQUIERDA: VENTAS POR MES ==========
        JPanel leftColumnPanel = new JPanel(new BorderLayout());
        leftColumnPanel.setBackground(Color.WHITE);
        
        JLabel monthTableTitle = new JLabel("Ventas por mes");
        monthTableTitle.setFont(new Font("Segoe UI", Font.BOLD, 19)); // Más grande
        monthTableTitle.setForeground(new Color(17, 24, 39)); // Gris muy oscuro
        monthTableTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        leftColumnPanel.add(monthTableTitle, BorderLayout.NORTH);
        
        // Panel que contiene tabla y gráfico de dona lado a lado
        JPanel monthContentPanel = new JPanel(new BorderLayout(20, 0));
        monthContentPanel.setBackground(Color.WHITE);
        
        // Tabla de ventas por mes a la izquierda (más grande)
        tableSalesByMonth = createSalesByMonthTable();
        JScrollPane monthScrollPane = new JScrollPane(tableSalesByMonth);
        monthScrollPane.setBorder(BorderFactory.createLineBorder(new Color(229, 231, 235), 1));
        monthScrollPane.setPreferredSize(new Dimension(350, 300)); // Aún más grande
        monthScrollPane.getViewport().setBackground(Color.WHITE);
        
        // Agregar sombra sutil a la tabla
        JPanel monthTableWrapper = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Sombra sutil
                for (int i = 0; i < 5; i++) {
                    int alpha = 5 - i;
                    g2d.setColor(new Color(0, 0, 0, alpha));
                    g2d.fillRoundRect(5 + i, 5 + i, getWidth() - 10 - 2*i, getHeight() - 10 - 2*i, 8, 8);
                }
                
                // Panel blanco encima
                g2d.setColor(Color.WHITE);
                g2d.fillRoundRect(5, 5, getWidth() - 10, getHeight() - 10, 8, 8);
                g2d.dispose();
            }
        };
        monthTableWrapper.setOpaque(false);
        monthTableWrapper.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        monthTableWrapper.add(monthScrollPane, BorderLayout.CENTER);
        
        monthContentPanel.add(monthTableWrapper, BorderLayout.WEST);
        
        // Gráfico de dona para meses a la derecha (con leyenda)
        chartPanelMonthDonut = new ChartPanel(createEmptyPieChart());
        chartPanelMonthDonut.setPreferredSize(new Dimension(280, 250)); // Más grande para incluir leyenda
        chartPanelMonthDonut.setMinimumSize(new Dimension(250, 250));
        chartPanelMonthDonut.setMaximumSize(new Dimension(320, 280));
        chartPanelMonthDonut.setBackground(Color.WHITE);
        chartPanelMonthDonut.setDomainZoomable(false);
        chartPanelMonthDonut.setRangeZoomable(false);
        chartPanelMonthDonut.setMouseWheelEnabled(false);
        chartPanelMonthDonut.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JPanel monthChartWrapper = new JPanel(new BorderLayout());
        monthChartWrapper.setOpaque(false);
        monthChartWrapper.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        monthChartWrapper.add(chartPanelMonthDonut, BorderLayout.CENTER);
        monthContentPanel.add(monthChartWrapper, BorderLayout.CENTER);
        
        leftColumnPanel.add(monthContentPanel, BorderLayout.CENTER);
        
        // ========== COLUMNA DERECHA: VENTAS POR DEPARTAMENTO ==========
        JPanel rightColumnPanel = new JPanel(new BorderLayout());
        rightColumnPanel.setBackground(Color.WHITE);
        
        JLabel deptTableTitle = new JLabel("Ventas por Departamento");
        deptTableTitle.setFont(new Font("Segoe UI", Font.BOLD, 19)); // Más grande
        deptTableTitle.setForeground(new Color(17, 24, 39)); // Gris muy oscuro
        deptTableTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        rightColumnPanel.add(deptTableTitle, BorderLayout.NORTH);
        
        // Panel que contiene tabla y gráfico de dona lado a lado
        JPanel deptContentPanel = new JPanel(new BorderLayout(20, 0));
        deptContentPanel.setBackground(Color.WHITE);
        
        // Tabla de ventas por departamento a la izquierda (más grande)
        tableDepartment = createDepartmentTable();
        JScrollPane deptScrollPane = new JScrollPane(tableDepartment);
        deptScrollPane.setBorder(BorderFactory.createLineBorder(new Color(229, 231, 235), 1));
        deptScrollPane.setPreferredSize(new Dimension(350, 300)); // Aún más grande
        deptScrollPane.getViewport().setBackground(Color.WHITE);
        
        // Agregar sombra sutil a la tabla
        JPanel deptTableWrapper = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Sombra sutil
                for (int i = 0; i < 5; i++) {
                    int alpha = 5 - i;
                    g2d.setColor(new Color(0, 0, 0, alpha));
                    g2d.fillRoundRect(5 + i, 5 + i, getWidth() - 10 - 2*i, getHeight() - 10 - 2*i, 8, 8);
                }
                
                // Panel blanco encima
                g2d.setColor(Color.WHITE);
                g2d.fillRoundRect(5, 5, getWidth() - 10, getHeight() - 10, 8, 8);
                g2d.dispose();
            }
        };
        deptTableWrapper.setOpaque(false);
        deptTableWrapper.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        deptTableWrapper.add(deptScrollPane, BorderLayout.CENTER);
        
        deptContentPanel.add(deptTableWrapper, BorderLayout.WEST);
        
        // Gráfico de dona para ganancia por departamento a la derecha (con leyenda para ver nombres)
        chartPanelDepartment = new ChartPanel(createEmptyPieChart());
        chartPanelDepartment.setPreferredSize(new Dimension(280, 250)); // Más grande para incluir leyenda
        chartPanelDepartment.setMinimumSize(new Dimension(250, 250));
        chartPanelDepartment.setMaximumSize(new Dimension(320, 280));
        chartPanelDepartment.setBackground(Color.WHITE);
        chartPanelDepartment.setDomainZoomable(false);
        chartPanelDepartment.setRangeZoomable(false);
        chartPanelDepartment.setMouseWheelEnabled(false);
        chartPanelDepartment.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JPanel deptChartWrapper = new JPanel(new BorderLayout());
        deptChartWrapper.setOpaque(false);
        deptChartWrapper.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        deptChartWrapper.add(chartPanelDepartment, BorderLayout.CENTER);
        deptContentPanel.add(deptChartWrapper, BorderLayout.CENTER);
        
        rightColumnPanel.add(deptContentPanel, BorderLayout.CENTER);
        
        // Agregar las dos columnas lado a lado
        leftColumnPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
            BorderFactory.createEmptyBorder(14, 14, 14, 14)
        ));
        rightColumnPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
            BorderFactory.createEmptyBorder(14, 14, 14, 14)
        ));
        bottomSectionPanel.add(leftColumnPanel);
        bottomSectionPanel.add(rightColumnPanel);
        
        mainPanel.add(bottomSectionPanel);
        
        // ========== GANANCIA POR DEPARTAMENTO (Lista simple) ==========
        JPanel departmentProfitPanel = new JPanel(new BorderLayout());
        departmentProfitPanel.setBackground(Color.WHITE);
        departmentProfitPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        
        JLabel deptProfitTitle = new JLabel("Ganancia por Departamento");
        deptProfitTitle.setFont(new Font("Segoe UI", Font.BOLD, 19)); // Más grande
        deptProfitTitle.setForeground(new Color(17, 24, 39)); // Gris muy oscuro
        deptProfitTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        departmentProfitPanel.add(deptProfitTitle, BorderLayout.NORTH);
        
        // Panel para la lista de departamentos (simple, sin tabla) con sombra
        JPanel deptListWrapper = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Sombra sutil
                for (int i = 0; i < 5; i++) {
                    int alpha = 5 - i;
                    g2d.setColor(new Color(0, 0, 0, alpha));
                    g2d.fillRoundRect(5 + i, 5 + i, getWidth() - 10 - 2*i, getHeight() - 10 - 2*i, 8, 8);
                }
                
                // Panel blanco encima
                g2d.setColor(Color.WHITE);
                g2d.fillRoundRect(5, 5, getWidth() - 10, getHeight() - 10, 8, 8);
                g2d.dispose();
            }
        };
        deptListWrapper.setOpaque(false);
        deptListWrapper.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        deptListPanel = new JPanel();
        deptListPanel.setLayout(new BoxLayout(deptListPanel, BoxLayout.Y_AXIS));
        deptListPanel.setOpaque(false);
        deptListPanel.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25)); // Más padding
        
        deptListWrapper.add(deptListPanel, BorderLayout.CENTER);
        
        // Crear scroll pane para la lista de departamentos envuelta en el wrapper con sombra
        JScrollPane deptListScroll = new JScrollPane(deptListWrapper);
        deptListScroll.setBorder(null);
        deptListScroll.getViewport().setOpaque(false);
        deptListScroll.setOpaque(false);
        departmentProfitPanel.add(deptListScroll, BorderLayout.CENTER);
        
        // ========== IMPUESTOS ==========
        JPanel taxesPanel = new JPanel(new BorderLayout());
        taxesPanel.setBackground(Color.WHITE);
        
        JLabel taxesTitle = new JLabel("Impuestos");
        taxesTitle.setFont(new Font("Segoe UI", Font.BOLD, 19)); // Más grande
        taxesTitle.setForeground(new Color(17, 24, 39)); // Gris muy oscuro
        taxesTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        taxesPanel.add(taxesTitle, BorderLayout.NORTH);
        
        // Panel para mostrar impuestos (por implementar)
        taxesBreakdownPanel = new JPanel();
        taxesBreakdownPanel.setLayout(new BoxLayout(taxesBreakdownPanel, BoxLayout.Y_AXIS));
        taxesBreakdownPanel.setBackground(Color.WHITE);
        taxesBreakdownPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(229, 231, 235), 1), // Borde más suave
            BorderFactory.createEmptyBorder(20, 20, 20, 20) // Más padding
        ));
        JLabel taxesHintLabel = new JLabel("El detalle fiscal del periodo aparecera aqui.");
        taxesHintLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        taxesHintLabel.setForeground(TEXT_MUTED);
        taxesBreakdownPanel.add(taxesHintLabel);
        taxesPanel.add(taxesBreakdownPanel, BorderLayout.CENTER);
        
        JPanel bottomInsightsPanel = new JPanel(new GridLayout(1, 2, 18, 0));
        bottomInsightsPanel.setOpaque(false);
        departmentProfitPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
            BorderFactory.createEmptyBorder(14, 14, 14, 14)
        ));
        taxesPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
            BorderFactory.createEmptyBorder(14, 14, 14, 14)
        ));
        bottomInsightsPanel.add(departmentProfitPanel);
        bottomInsightsPanel.add(taxesPanel);
        
        mainPanel.add(bottomInsightsPanel);
        
        // Panel para mantener el espaciado
        mainPanel.add(Box.createVerticalGlue());
        
        return mainPanel;
    }
    
    private JPanel createChartPanel(String title, ChartPanel chartPanel) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 2),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(new Color(50, 100, 150));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(chartPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    private JFreeChart createEmptyPieChart() {
        DefaultPieDataset dataset = new DefaultPieDataset();
        // No agregar ningún valor, dejar el dataset vacío
        JFreeChart chart = ChartFactory.createRingChart(null, dataset, false, false, false);
        chart.setBackgroundPaint(new Color(0, 0, 0, 0)); // Fondo transparente
        RingPlot plot = (RingPlot) chart.getPlot();
        plot.setBackgroundPaint(new Color(0, 0, 0, 0)); // Fondo transparente
        plot.setOutlineVisible(false); // Sin borde exterior
        plot.setLabelGenerator(null); // Sin etiquetas
        plot.setShadowPaint(new Color(0, 0, 0, 0)); // Sin sombra
        plot.setInteriorGap(0.04);
        plot.setSectionDepth(0.38);
        plot.setSeparatorPaint(Color.WHITE);
        plot.setSeparatorStroke(new BasicStroke(2.0f));
        return chart;
    }
    
    private JFreeChart createEmptyBarChart() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        // Agregar un valor temporal para que el gráfico se vea
        dataset.addValue(1.0, "Esperando datos...", "Esperando datos...");
        JFreeChart chart = ChartFactory.createBarChart(null, "Forma de pago", "Ventas ($)", 
            dataset, PlotOrientation.HORIZONTAL, false, true, false);
        chart.setBackgroundPaint(SURFACE_BG);
        CategoryPlot plot = (CategoryPlot) chart.getPlot();
        plot.setBackgroundPaint(SURFACE_BG);
        plot.setOutlineVisible(false);
        plot.getDomainAxis().setLabelFont(new Font("Segoe UI", Font.PLAIN, 12));
        plot.getRangeAxis().setLabelFont(new Font("Segoe UI", Font.PLAIN, 12));
        return chart;
    }
    
    private JFreeChart createEmptySalesProfitBarChart() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        // Agregar valores temporales para que el gráfico se vea
        dataset.addValue(0.0, "Ventas", "Esperando datos...");
        dataset.addValue(0.0, "Ganancia", "Esperando datos...");
        JFreeChart chart = ChartFactory.createBarChart(null, "", "Ventas", 
            dataset, PlotOrientation.VERTICAL, true, true, false);
        chart.setBackgroundPaint(Color.WHITE);
        CategoryPlot plot = (CategoryPlot) chart.getPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setOutlineVisible(false);
        plot.getDomainAxis().setLabelFont(new Font("Segoe UI", Font.PLAIN, 11));
        plot.getRangeAxis().setLabelFont(new Font("Segoe UI", Font.PLAIN, 11));
        plot.getDomainAxis().setTickLabelFont(new Font("Segoe UI", Font.PLAIN, 10));
        plot.getRangeAxis().setTickLabelFont(new Font("Segoe UI", Font.PLAIN, 10));
        return chart;
    }
    
    private JFreeChart createEmptyHourlyChart() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(0.0, "Ventas", "00:00");
        JFreeChart chart = ChartFactory.createLineChart(null, "Hora", "Ventas", 
            dataset, PlotOrientation.VERTICAL, false, true, false);
        chart.setBackgroundPaint(Color.WHITE);
        CategoryPlot plot = (CategoryPlot) chart.getPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setOutlineVisible(false);
        return chart;
    }
    
    private void updateSalesProfitBarChart(double totalSales, double totalProfit) {
        if (chartPanelSalesProfit == null) {
            LOGGER.warning("chartPanelSalesProfit es null, no se puede actualizar el gráfico");
            return;
        }
        
        try {
            // Cargar datos agrupados por mes
            List<MonthData> monthData = loadSalesProfitByMonth();
            
            DefaultCategoryDataset dataset = new DefaultCategoryDataset();
            
            // Si hay datos por mes, mostrarlos; si no, mostrar totales
            if (monthData != null && !monthData.isEmpty()) {
                for (MonthData month : monthData) {
                    dataset.addValue(month.sales, "Ventas", month.monthName);
                    dataset.addValue(month.profit, "Ganancia", month.monthName);
                }
            } else {
                // Fallback: mostrar totales
                dataset.addValue(totalSales, "Ventas", "Total");
                dataset.addValue(totalProfit, "Ganancia", "Total");
            }
            
            JFreeChart chart = ChartFactory.createBarChart(null, "", "Ventas", 
                dataset, PlotOrientation.VERTICAL, true, true, false);
            chart.setBackgroundPaint(SURFACE_BG);
            
            CategoryPlot plot = (CategoryPlot) chart.getPlot();
            plot.setBackgroundPaint(Color.WHITE);
            plot.setOutlineVisible(false);
            plot.setRangeGridlinePaint(new Color(229, 231, 235)); // Grid más suave
            
            // Tamaño de fuente mejorado
            plot.getDomainAxis().setLabelFont(new Font("Segoe UI", Font.PLAIN, 12));
            plot.getRangeAxis().setLabelFont(new Font("Segoe UI", Font.PLAIN, 12));
            plot.getDomainAxis().setTickLabelFont(new Font("Segoe UI", Font.PLAIN, 11));
            plot.getRangeAxis().setTickLabelFont(new Font("Segoe UI", Font.PLAIN, 11));
            
            // Configurar la leyenda
            LegendTitle legend = chart.getLegend();
            if (legend != null) {
                legend.setItemFont(new Font("Segoe UI", Font.PLAIN, 12));
                legend.setBackgroundPaint(Color.WHITE);
            }
            
            // Colores similares a la imagen: Azul más oscuro para Ventas, Azul claro para Ganancia
            plot.getRenderer().setSeriesPaint(0, new Color(70, 130, 180));   // Azul para Ventas
            plot.getRenderer().setSeriesPaint(1, new Color(173, 216, 230));  // Azul claro para Ganancia
            
            chartPanelSalesProfit.setChart(chart);
            chartPanelSalesProfit.repaint();
            LOGGER.info("Gráfico de Ventas y Ganancias actualizado - Ventas: " + totalSales + ", Ganancias: " + totalProfit);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error actualizando gráfico de ventas y ganancias", e);
        }
    }
    
    // Clase para almacenar datos por mes
    private static class MonthData {
        public String monthName;
        public double sales;
        public double profit;
        
        public MonthData(String monthName, double sales, double profit) {
            this.monthName = monthName;
            this.sales = sales;
            this.profit = profit;
        }
    }
    
    private List<MonthData> loadSalesProfitByMonth() throws SQLException {
        List<MonthData> monthData = new ArrayList<>();
        
        // Sebastian - Ya no requerimos activeCashIndex para reportes
        if (session == null) {
            return monthData;
        }
        
        try {
            // Consulta SQL para agrupar por mes (compatible con HSQLDB)
            StringBuilder sqlBuilder = new StringBuilder();
            sqlBuilder.append("SELECT ");
            sqlBuilder.append("MONTH(receipts.DATENEW) as MONTH_NUM, ");
            sqlBuilder.append("SUM((ticketlines.PRICE + ticketlines.PRICE * taxes.RATE) * ticketlines.UNITS) as TOTAL_SALES, ");
            sqlBuilder.append("SUM((ticketlines.PRICE - COALESCE(products.PRICEBUY, 0)) * ticketlines.UNITS) as TOTAL_PROFIT ");
            sqlBuilder.append("FROM ticketlines ");
            sqlBuilder.append("INNER JOIN tickets ON ticketlines.TICKET = tickets.ID ");
            sqlBuilder.append("INNER JOIN receipts ON tickets.ID = receipts.ID ");
            sqlBuilder.append("INNER JOIN products ON ticketlines.PRODUCT = products.ID ");
            sqlBuilder.append("INNER JOIN taxes ON ticketlines.TAXID = taxes.ID ");
            
            // Sebastian - Para reportes, mostrar TODAS las ventas, no solo las de la caja activa del usuario actual
            // Usar solo filtros de fecha para los reportes
            boolean hasDateFilter = false;
            if (dateStart != null) {
                sqlBuilder.append(hasDateFilter ? "AND " : "WHERE ");
                sqlBuilder.append("receipts.DATENEW >= ? ");
                hasDateFilter = true;
            }
            if (dateEnd != null) {
                sqlBuilder.append(hasDateFilter ? "AND " : "WHERE ");
                sqlBuilder.append("receipts.DATENEW <= ? ");
                hasDateFilter = true;
            }
            
            sqlBuilder.append("GROUP BY MONTH(receipts.DATENEW) ");
            sqlBuilder.append("ORDER BY MONTH(receipts.DATENEW)");
            
            PreparedStatement stmt = session.getConnection().prepareStatement(sqlBuilder.toString());
            int paramIndex = 1;
            // Ya no se usa activeCashIndex para reportes
            if (dateStart != null) {
                stmt.setTimestamp(paramIndex++, new Timestamp(dateStart.getTime()));
            }
            if (dateEnd != null) {
                stmt.setTimestamp(paramIndex++, new Timestamp(dateEnd.getTime()));
            }
            
            ResultSet rs = stmt.executeQuery();
            String[] monthNames = {"", "Ene", "Feb", "Mar", "Abr", "May", "Jun", "Jul", "Ago", "Sep", "Oct", "Nov", "Dic"};
            while (rs.next()) {
                int monthNum = rs.getInt("MONTH_NUM");
                double sales = rs.getDouble("TOTAL_SALES");
                double profit = rs.getDouble("TOTAL_PROFIT");
                
                if (sales > 0 || profit > 0) {
                    String monthName = (monthNum >= 1 && monthNum <= 12) ? monthNames[monthNum] : "N/A";
                    monthData.add(new MonthData(monthName, sales, profit));
                }
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error cargando datos por mes", e);
        }
        
        return monthData;
    }
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    private void updatePieChart(List<DepartmentData> departments) {
        if (chartPanelDepartment == null) {
            LOGGER.warning("chartPanelDepartment es null, no se puede actualizar el gráfico");
            return;
        }
        
        if (departments == null || departments.isEmpty()) {
            LOGGER.info("No hay datos de departamentos para mostrar");
            // Ocultar el gráfico completamente cuando no hay datos
            chartPanelDepartment.setVisible(false);
            return;
        }
        
        DefaultPieDataset dataset = new DefaultPieDataset();
        
        // Ordenar por ganancia descendente
        departments.sort((a, b) -> Double.compare(b.profit, a.profit));
        
        // Agregar los primeros 6 departamentos más grandes
        int count = 0;
        double othersProfit = 0.0;
        Color[] colors = {
            new Color(70, 130, 180),   // Azul acero
            new Color(60, 179, 113),   // Verde mar
            new Color(255, 140, 0),    // Naranja oscuro
            new Color(220, 20, 60),    // Rojo carmesí (pero solo si hay múltiples segmentos)
            new Color(138, 43, 226),   // Violeta azul
            new Color(255, 215, 0),    // Oro
            new Color(102, 204, 204),  // Turquesa claro
            new Color(51, 153, 204)    // Azul medio
        };
        
        for (DepartmentData dept : departments) {
            if (count < 6 && dept.profit > 0) {
                String label = dept.index > 0 ? dept.index + "." + dept.name : dept.name;
                dataset.setValue(label, dept.profit);
                count++;
            } else if (dept.profit > 0) {
                othersProfit += dept.profit;
            }
        }
        
        if (othersProfit > 0) {
            dataset.setValue("Otros...", othersProfit);
        }
        
        if (dataset.getItemCount() == 0) {
            LOGGER.info("No hay datos con ganancia positiva para mostrar");
            // Ocultar el gráfico completamente cuando no hay datos
            chartPanelDepartment.setVisible(false);
            return;
        }
        
        // Mostrar el gráfico cuando hay datos
        chartPanelDepartment.setVisible(true);
        
        // Crear gráfico CON leyenda para mostrar los nombres de los departamentos
        JFreeChart chart = ChartFactory.createRingChart(null, dataset, true, true, false);
        chart.setBackgroundPaint(Color.WHITE);
        
        RingPlot plot = (RingPlot) chart.getPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setOutlineVisible(false);
        plot.setLabelGenerator(null); // Sin etiquetas en el gráfico mismo
        plot.setShadowPaint(new Color(0, 0, 0, 0));
        plot.setInteriorGap(0.04);
        plot.setSectionDepth(0.38);
        plot.setSeparatorPaint(Color.WHITE);
        plot.setSeparatorStroke(new BasicStroke(2.0f));
        
        // Configurar la leyenda para que sea legible
        LegendTitle legend = chart.getLegend();
        if (legend != null) {
            legend.setItemFont(new Font("Segoe UI", Font.PLAIN, 11));
            legend.setBackgroundPaint(SURFACE_BG);
        }
        
        // Aplicar colores variados
        int colorIndex = 0;
        for (Object key : dataset.getKeys()) {
            if (colorIndex < colors.length) {
                plot.setSectionPaint((Comparable) key, colors[colorIndex]);
            } else {
                // Si hay más elementos que colores, generar colores alternativos
                float hue = (colorIndex * 0.1f) % 1.0f;
                Color newColor = Color.getHSBColor(hue, 0.6f, 0.9f);
                plot.setSectionPaint((Comparable) key, newColor);
            }
            colorIndex++;
        }
        
        chartPanelDepartment.setChart(chart);
        chartPanelDepartment.repaint();
        LOGGER.info("Gráfico circular actualizado con " + dataset.getItemCount() + " elementos");
    }
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    private void updateMonthDonutChart(List<MonthData> monthData) {
        if (chartPanelMonthDonut == null) {
            LOGGER.warning("chartPanelMonthDonut es null, no se puede actualizar el gráfico");
            return;
        }
        
        if (monthData == null || monthData.isEmpty()) {
            LOGGER.info("No hay datos de meses para mostrar");
            chartPanelMonthDonut.setVisible(false);
            return;
        }
        
        DefaultPieDataset dataset = new DefaultPieDataset();
        
        // Agregar todos los meses con ventas
        for (MonthData month : monthData) {
            if (month.sales > 0) {
                dataset.setValue(month.monthName, month.sales);
            }
        }
        
        if (dataset.getItemCount() == 0) {
            LOGGER.info("No hay meses con ventas para mostrar");
            chartPanelMonthDonut.setVisible(false);
            return;
        }
        
        chartPanelMonthDonut.setVisible(true);
        
        // Crear gráfico CON leyenda para mostrar los nombres de los meses
        JFreeChart chart = ChartFactory.createRingChart(null, dataset, true, true, false);
        chart.setBackgroundPaint(Color.WHITE);
        
        RingPlot plot = (RingPlot) chart.getPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setOutlineVisible(false);
        plot.setLabelGenerator(null); // Sin etiquetas en el gráfico mismo
        plot.setShadowPaint(new Color(0, 0, 0, 0));
        plot.setInteriorGap(0.04);
        plot.setSectionDepth(0.38);
        plot.setSeparatorPaint(Color.WHITE);
        plot.setSeparatorStroke(new BasicStroke(2.0f));
        
        // Configurar la leyenda para que sea legible
        LegendTitle legend = chart.getLegend();
        if (legend != null) {
            legend.setItemFont(new Font("Segoe UI", Font.PLAIN, 11));
            legend.setBackgroundPaint(Color.WHITE);
        }
        
        // Aplicar colores variados para meses
        Color[] colors = {
            new Color(70, 130, 180),   // Azul acero
            new Color(60, 179, 113),   // Verde mar
            new Color(255, 140, 0),    // Naranja oscuro
            new Color(220, 20, 60),    // Rojo carmesí
            new Color(138, 43, 226),   // Violeta azul
            new Color(255, 215, 0),    // Oro
            new Color(102, 204, 204),  // Turquesa claro
            new Color(51, 153, 204),   // Azul medio
            new Color(255, 192, 203),  // Rosa
            new Color(144, 238, 144),  // Verde claro
            new Color(255, 165, 0),    // Naranja
            new Color(186, 85, 211)    // Orquídea
        };
        
        int colorIndex = 0;
        for (Object key : dataset.getKeys()) {
            if (colorIndex < colors.length) {
                plot.setSectionPaint((Comparable) key, colors[colorIndex % colors.length]);
            } else {
                float hue = (colorIndex * 0.1f) % 1.0f;
                Color newColor = Color.getHSBColor(hue, 0.6f, 0.9f);
                plot.setSectionPaint((Comparable) key, newColor);
            }
            colorIndex++;
        }
        
        chartPanelMonthDonut.setChart(chart);
        chartPanelMonthDonut.repaint();
        LOGGER.info("Gráfico de meses actualizado con " + dataset.getItemCount() + " elementos");
    }
    
    private void updateBarChart(List<PaymentData> payments) {
        if (chartPanelPayment == null) {
            LOGGER.warning("chartPanelPayment es null, no se puede actualizar el gráfico");
            return;
        }
        
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        
        if (payments == null || payments.isEmpty()) {
            LOGGER.info("No hay datos de pagos para mostrar");
            // Mostrar gráfico vacío
            JFreeChart chart = ChartFactory.createBarChart(null, "Forma de pago", "Ventas", 
                dataset, PlotOrientation.HORIZONTAL, false, true, false);
            chart.setBackgroundPaint(SURFACE_BG);
            chartPanelPayment.setChart(chart);
            return;
        }
        
        // Ordenar por monto descendente
        payments.sort((a, b) -> Double.compare(b.amount, a.amount));
        
        for (PaymentData payment : payments) {
            if (payment.amount > 0) {
                dataset.addValue(payment.amount, payment.displayName, payment.displayName);
            }
        }
        
        if (dataset.getRowCount() == 0) {
            LOGGER.info("No hay pagos con monto positivo para mostrar");
        }
        
        // Cambiar a barras verticales como en Eleventa, con leyenda
        JFreeChart chart = ChartFactory.createBarChart(null, null, "Ventas ($)", 
            dataset, PlotOrientation.HORIZONTAL, true, true, false);
        chart.setBackgroundPaint(Color.WHITE);
        CategoryPlot plot = (CategoryPlot) chart.getPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setOutlineVisible(false);
        plot.setRangeGridlinePaint(new Color(229, 231, 235)); // Grid más suave
        
        // Mejorar la apariencia del gráfico
        plot.getDomainAxis().setLabelFont(new Font("Segoe UI", Font.PLAIN, 12));
        plot.getRangeAxis().setLabelFont(new Font("Segoe UI", Font.PLAIN, 12));
        plot.getDomainAxis().setTickLabelFont(new Font("Segoe UI", Font.PLAIN, 11));
        plot.getRangeAxis().setTickLabelFont(new Font("Segoe UI", Font.PLAIN, 11));
        
        // Configurar la leyenda
        LegendTitle legend = chart.getLegend();
        if (legend != null) {
            legend.setItemFont(new Font("Segoe UI", Font.PLAIN, 12));
            legend.setBackgroundPaint(Color.WHITE);
        }
        
        // Colores como en Eleventa: Efectivo (azul claro), Vales (azul oscuro), Tarjeta (rojo), Crédito (naranja)
        Map<String, Color> paymentColors = new HashMap<>();
        paymentColors.put("Efectivo", new Color(173, 216, 230));  // Azul claro
        paymentColors.put("Vales", new Color(70, 130, 180));      // Azul acero
        paymentColors.put("Tarjeta", new Color(220, 20, 60));     // Rojo
        paymentColors.put("Crédito", new Color(255, 140, 0));     // Naranja
        
        if (dataset.getRowCount() > 0) {
            for (int i = 0; i < dataset.getRowCount(); i++) {
                String category = (String) dataset.getRowKey(i);
                Color color = paymentColors.getOrDefault(category, new Color(70, 130, 180));
                plot.getRenderer().setSeriesPaint(i, color);
            }
        }

        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setDefaultItemLabelGenerator(new StandardCategoryItemLabelGenerator());
        renderer.setDefaultItemLabelsVisible(true);
        renderer.setMaximumBarWidth(0.18);
        renderer.setShadowVisible(false);
        plot.getDomainAxis().setTickLabelPaint(TEXT_MUTED);
        plot.getRangeAxis().setTickLabelPaint(TEXT_MUTED);
        
        chartPanelPayment.setChart(chart);
        chartPanelPayment.repaint();
        LOGGER.info("Gráfico de barras actualizado con " + dataset.getRowCount() + " formas de pago");
    }
    
    private void updateHourlyChart() {
        if (chartPanelHourly == null) return;
        
        try {
            List<HourlyData> hourlyData = loadSalesByHour();
            DefaultCategoryDataset dataset = new DefaultCategoryDataset();
            
            if (hourlyData.isEmpty()) {
                dataset.addValue(0.0, "Ventas", "Sin datos");
            } else {
                for (HourlyData data : hourlyData) {
                    dataset.addValue(data.amount, "Ventas", data.hour + ":00");
                }
            }
            
            JFreeChart chart = ChartFactory.createLineChart(null, null, "Ventas ($)", 
                dataset, PlotOrientation.VERTICAL, false, true, false);
            chart.setBackgroundPaint(Color.WHITE);
            
            CategoryPlot plot = (CategoryPlot) chart.getPlot();
            plot.setBackgroundPaint(new Color(248, 250, 252));
            plot.setOutlineVisible(false);
            plot.setRangeGridlinePaint(new Color(229, 231, 235));
            
            // Estilo de línea
            LineAndShapeRenderer renderer = new LineAndShapeRenderer();
            renderer.setSeriesPaint(0, ACCENT_BLUE);
            renderer.setSeriesStroke(0, new BasicStroke(3.0f));
            renderer.setDefaultShapesVisible(true);
            renderer.setDefaultShapesFilled(true);
            renderer.setUseFillPaint(true);
            renderer.setSeriesFillPaint(0, ACCENT_CYAN);
            plot.setRenderer(renderer);
            plot.getDomainAxis().setTickLabelPaint(TEXT_MUTED);
            plot.getRangeAxis().setTickLabelPaint(TEXT_MUTED);
            
            chartPanelHourly.setChart(chart);
            chartPanelHourly.repaint();
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error actualizando gráfico por hora", e);
        }
    }
    
    private static class HourlyData {
        public int hour;
        public double amount;
        public HourlyData(int hour, double amount) {
            this.hour = hour;
            this.amount = amount;
        }
    }

    private static class TaxData {
        public String name;
        public double base;
        public double totalTax;

        public TaxData(String name, double base, double totalTax) {
            this.name = name;
            this.base = base;
            this.totalTax = totalTax;
        }
    }
    
    private List<HourlyData> loadSalesByHour() throws SQLException {
        List<HourlyData> data = new ArrayList<>();
        if (session == null) return data;
        
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT HOUR(receipts.DATENEW) as HOUR_NUM, SUM(ticketlines.PRICE * ticketlines.UNITS) as TOTAL ");
        sql.append("FROM ticketlines ");
        sql.append("INNER JOIN tickets ON ticketlines.TICKET = tickets.ID ");
        sql.append("INNER JOIN receipts ON tickets.ID = receipts.ID ");
        if (dateStart != null) sql.append("WHERE receipts.DATENEW >= ? ");
        if (dateEnd != null) sql.append((dateStart != null ? "AND " : "WHERE ") + "receipts.DATENEW <= ? ");
        sql.append("GROUP BY HOUR(receipts.DATENEW) ");
        sql.append("ORDER BY HOUR(receipts.DATENEW)");
        
        PreparedStatement stmt = session.getConnection().prepareStatement(sql.toString());
        int idx = 1;
        if (dateStart != null) stmt.setTimestamp(idx++, new Timestamp(dateStart.getTime()));
        if (dateEnd != null) stmt.setTimestamp(idx++, new Timestamp(dateEnd.getTime()));
        
        ResultSet rs = stmt.executeQuery();
        while (rs.next()) {
            data.add(new HourlyData(rs.getInt("HOUR_NUM"), rs.getDouble("TOTAL")));
        }
        rs.close();
        stmt.close();
        return data;
    }

    private List<TaxData> loadTaxData() throws SQLException {
        List<TaxData> taxes = new ArrayList<>();
        if (session == null) return taxes;

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT taxes.NAME as TAX_NAME, ");
        sql.append("SUM(ticketlines.PRICE * ticketlines.UNITS) as BASE_AMOUNT, ");
        sql.append("SUM((ticketlines.PRICE * taxes.RATE) * ticketlines.UNITS) as TAX_AMOUNT ");
        sql.append("FROM ticketlines ");
        sql.append("INNER JOIN tickets ON ticketlines.TICKET = tickets.ID ");
        sql.append("INNER JOIN receipts ON tickets.ID = receipts.ID ");
        sql.append("INNER JOIN taxes ON ticketlines.TAXID = taxes.ID ");
        if (dateStart != null) sql.append("WHERE receipts.DATENEW >= ? ");
        if (dateEnd != null) sql.append((dateStart != null ? "AND " : "WHERE ") + "receipts.DATENEW <= ? ");
        sql.append("GROUP BY taxes.NAME ");
        sql.append("ORDER BY TAX_AMOUNT DESC");

        PreparedStatement stmt = session.getConnection().prepareStatement(sql.toString());
        int idx = 1;
        if (dateStart != null) stmt.setTimestamp(idx++, new Timestamp(dateStart.getTime()));
        if (dateEnd != null) stmt.setTimestamp(idx++, new Timestamp(dateEnd.getTime()));

        ResultSet rs = stmt.executeQuery();
        while (rs.next()) {
            taxes.add(new TaxData(
                rs.getString("TAX_NAME"),
                rs.getDouble("BASE_AMOUNT"),
                rs.getDouble("TAX_AMOUNT")
            ));
        }
        rs.close();
        stmt.close();
        return taxes;
    }

    private void updateTaxBreakdown(List<TaxData> taxes) {
        if (taxesBreakdownPanel == null) {
            return;
        }

        taxesBreakdownPanel.removeAll();

        if (taxes == null || taxes.isEmpty()) {
            JLabel empty = new JLabel("No hay impuestos registrados para este periodo.");
            empty.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            empty.setForeground(TEXT_MUTED);
            taxesBreakdownPanel.add(empty);
        } else {
            double totalTaxes = 0.0;
            for (TaxData tax : taxes) {
                totalTaxes += tax.totalTax;

                JPanel taxRow = new JPanel(new BorderLayout(18, 0));
                taxRow.setBackground(Color.WHITE);
                taxRow.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(241, 245, 249)),
                    BorderFactory.createEmptyBorder(12, 0, 12, 0)
                ));

                JPanel taxMeta = new JPanel();
                taxMeta.setOpaque(false);
                taxMeta.setLayout(new BoxLayout(taxMeta, BoxLayout.Y_AXIS));

                JLabel taxName = new JLabel(tax.name != null ? tax.name : "Impuesto");
                taxName.setFont(new Font("Segoe UI", Font.BOLD, 15));
                taxName.setForeground(TEXT_PRIMARY);
                taxMeta.add(taxName);

                JLabel taxBase = new JLabel("Base gravable " + Formats.CURRENCY.formatValue(tax.base));
                taxBase.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                taxBase.setForeground(TEXT_MUTED);
                taxBase.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));
                taxMeta.add(taxBase);

                JLabel taxAmount = new JLabel(Formats.CURRENCY.formatValue(tax.totalTax));
                taxAmount.setFont(new Font("Segoe UI", Font.BOLD, 16));
                taxAmount.setForeground(ACCENT_GOLD);

                taxRow.add(taxMeta, BorderLayout.WEST);
                taxRow.add(taxAmount, BorderLayout.EAST);
                taxesBreakdownPanel.add(taxRow);
            }

            JPanel totalRow = new JPanel(new BorderLayout());
            totalRow.setBackground(new Color(248, 250, 252));
            totalRow.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

            JLabel totalLabel = new JLabel("Total de impuestos del periodo");
            totalLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
            totalLabel.setForeground(TEXT_PRIMARY);

            JLabel totalValue = new JLabel(Formats.CURRENCY.formatValue(totalTaxes));
            totalValue.setFont(new Font("Segoe UI", Font.BOLD, 18));
            totalValue.setForeground(ACCENT_GOLD);

            totalRow.add(totalLabel, BorderLayout.WEST);
            totalRow.add(totalValue, BorderLayout.EAST);
            taxesBreakdownPanel.add(Box.createVerticalStrut(10));
            taxesBreakdownPanel.add(totalRow);
        }

        taxesBreakdownPanel.revalidate();
        taxesBreakdownPanel.repaint();
    }
    
    private JTable createSalesByMonthTable() {
        String[] columns = {"Mes", "Monto"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        JTable table = new JTable(model);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14)); // Más grande
        table.setRowHeight(38); // Más alto
        table.setShowGrid(true);
        table.setGridColor(new Color(243, 244, 246));
        table.setBackground(Color.WHITE);
        table.setSelectionBackground(new Color(239, 246, 255));
        table.setSelectionForeground(new Color(30, 64, 175));
        table.setIntercellSpacing(new Dimension(1, 1));
        
        // Alternar colores de filas para mejor legibilidad
        table.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(249, 250, 251));
                }
                return c;
            }
        });
        
        // Header moderno estilo Eleventa
        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 14)); // Más grande
        header.setBackground(new Color(249, 250, 251));
        header.setForeground(new Color(17, 24, 39)); // Más oscuro
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(229, 231, 235)));
        header.setPreferredSize(new Dimension(0, 42)); // Más alto
        
        return table;
    }
    
    private void updateSalesByMonthTable(List<MonthData> monthData) {
        DefaultTableModel model = (DefaultTableModel) tableSalesByMonth.getModel();
        model.setRowCount(0);
        
        if (monthData != null && !monthData.isEmpty()) {
            for (MonthData month : monthData) {
                String monthName = month.monthName;
                String amount = Formats.CURRENCY.formatValue(month.sales);
                model.addRow(new Object[]{monthName, amount});
            }
        }
    }
    
    private JTable createDepartmentTable() {
        String[] columns = {"Departamento", "Monto"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        JTable table = new JTable(model);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14)); // Más grande
        table.setRowHeight(38); // Más alto
        table.setShowGrid(true);
        table.setGridColor(new Color(243, 244, 246));
        table.setBackground(Color.WHITE);
        table.setSelectionBackground(new Color(239, 246, 255));
        table.setSelectionForeground(new Color(30, 64, 175));
        table.setIntercellSpacing(new Dimension(1, 1));
        
        // Alternar colores de filas para mejor legibilidad
        table.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(249, 250, 251));
                }
                return c;
            }
        });
        
        // Header moderno estilo Eleventa
        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 14)); // Más grande
        header.setBackground(new Color(249, 250, 251));
        header.setForeground(new Color(17, 24, 39)); // Más oscuro
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(229, 231, 235)));
        header.setPreferredSize(new Dimension(0, 42)); // Más alto
        
        return table;
    }
    
    private JTable createDepartmentProfitTable() {
        String[] columns = {"Departamento", "Ganancia"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        JTable table = new JTable(model);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setRowHeight(30);
        table.setShowGrid(true);
        table.setGridColor(new Color(230, 230, 230));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.getTableHeader().setBackground(new Color(70, 130, 180));
        table.getTableHeader().setForeground(Color.WHITE);
        
        return table;
    }
    
    private void updateDepartmentProfitList(List<DepartmentData> departments) {
        if (deptListPanel == null) {
            LOGGER.warning("deptListPanel es null, no se puede actualizar la lista");
            return;
        }
        
        // Limpiar el panel
        deptListPanel.removeAll();
        
        if (departments != null && !departments.isEmpty()) {
            // Ordenar por ganancia descendente
            List<DepartmentData> sortedDepts = new ArrayList<>(departments);
            sortedDepts.sort((a, b) -> Double.compare(b.profit, a.profit));
            
            for (DepartmentData dept : sortedDepts) {
                if (dept.profit > 0) {
                    // Crear un panel para cada departamento con su ganancia
                    JPanel deptItemPanel = new JPanel(new BorderLayout(25, 0));
                    deptItemPanel.setOpaque(false);
                    deptItemPanel.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(229, 231, 235)), // Línea divisoria más visible
                        BorderFactory.createEmptyBorder(14, 0, 14, 0) // Más padding vertical
                    ));
                    
                    // Nombre del departamento
                    JLabel deptNameLabel = new JLabel(dept.name);
                    deptNameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 15)); // Más grande
                    deptNameLabel.setForeground(new Color(75, 85, 99)); // Gris más oscuro para mejor legibilidad
                    deptItemPanel.add(deptNameLabel, BorderLayout.WEST);
                    
                    // Ganancia
                    String profit = Formats.CURRENCY.formatValue(dept.profit);
                    JLabel profitLabel = new JLabel(profit);
                    profitLabel.setFont(new Font("Segoe UI", Font.BOLD, 16)); // Más grande
                    profitLabel.setForeground(new Color(34, 197, 94)); // Verde para ganancia positiva
                    profitLabel.setHorizontalAlignment(SwingConstants.RIGHT);
                    deptItemPanel.add(profitLabel, BorderLayout.EAST);
                    
                    deptListPanel.add(deptItemPanel);
                }
            }
            
            // Agregar "Otros..." si hay más departamentos con ganancia 0 o negativa
            long othersCount = sortedDepts.stream().filter(d -> d.profit <= 0).count();
            if (othersCount > 0) {
                JPanel othersPanel = new JPanel(new BorderLayout(10, 0));
                othersPanel.setBackground(Color.WHITE);
                othersPanel.setBorder(BorderFactory.createEmptyBorder(8, 5, 8, 5));
                
                JLabel othersLabel = new JLabel("Otros...");
                othersLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                othersLabel.setForeground(new Color(100, 100, 100));
                othersPanel.add(othersLabel, BorderLayout.WEST);
                
                JLabel othersProfitLabel = new JLabel("$0.00");
                othersProfitLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
                othersProfitLabel.setForeground(new Color(100, 100, 100));
                othersProfitLabel.setHorizontalAlignment(SwingConstants.RIGHT);
                othersPanel.add(othersProfitLabel, BorderLayout.EAST);
                
                deptListPanel.add(othersPanel);
            }
        } else {
            // Mostrar mensaje cuando no hay datos
            JLabel noDataLabel = new JLabel("No hay datos disponibles");
            noDataLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            noDataLabel.setForeground(new Color(150, 150, 150));
            noDataLabel.setHorizontalAlignment(SwingConstants.CENTER);
            noDataLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
            deptListPanel.add(noDataLabel);
        }
        
        deptListPanel.revalidate();
        deptListPanel.repaint();
    }
    
    private void updateDepartmentTable(List<DepartmentData> departments) {
        DefaultTableModel model = (DefaultTableModel) tableDepartment.getModel();
        model.setRowCount(0);
        
        // Ordenar por ventas descendente
        departments.sort((a, b) -> Double.compare(b.sales, a.sales));
        
        for (DepartmentData dept : departments) {
            String label = dept.index > 0 ? dept.index + "." + dept.name : dept.name;
            String amount = Formats.CURRENCY.formatValue(dept.sales);
            model.addRow(new Object[]{label, amount});
        }
    }
    
    private JPanel createSummaryPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 20, 0));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));
        
        // Panel de Ventas
        JPanel salesPanel = new JPanel(new BorderLayout());
        salesPanel.setBackground(new Color(230, 240, 250));
        salesPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(70, 130, 180), 2),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        
        JLabel salesLabel = new JLabel("Ventas");
        salesLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        salesLabel.setForeground(new Color(70, 130, 180));
        salesPanel.add(salesLabel, BorderLayout.NORTH);
        
        lblTotalSales = new JLabel("$0.00");
        lblTotalSales.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblTotalSales.setForeground(new Color(50, 50, 50));
        lblTotalSales.setHorizontalAlignment(SwingConstants.CENTER);
        salesPanel.add(lblTotalSales, BorderLayout.CENTER);
        
        // Panel de Ganancia
        JPanel profitPanel = new JPanel(new BorderLayout());
        profitPanel.setBackground(new Color(230, 250, 240));
        profitPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(76, 175, 80), 2),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        
        JLabel profitLabel = new JLabel("Ganancia");
        profitLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        profitLabel.setForeground(new Color(76, 175, 80));
        profitPanel.add(profitLabel, BorderLayout.NORTH);
        
        lblTotalProfit = new JLabel("$0.00");
        lblTotalProfit.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblTotalProfit.setForeground(new Color(50, 50, 50));
        lblTotalProfit.setHorizontalAlignment(SwingConstants.CENTER);
        profitPanel.add(lblTotalProfit, BorderLayout.CENTER);
        
        panel.add(salesPanel);
        panel.add(profitPanel);
        
        return panel;
    }
    
    private JPanel createSalesMetricsPanelEleventa() {
        JPanel mainPanel = new JPanel(new GridLayout(1, 5, 14, 0));
        mainPanel.setOpaque(false);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));

        lblSalesTotal = new JLabel("$0.00");
        lblSalesTotal.setForeground(TEXT_PRIMARY);
        mainPanel.add(createMetricSummaryCard("Ventas Totales", "Ingreso bruto del periodo", ACCENT_BLUE, lblSalesTotal));

        lblNumberOfSales = new JLabel("0");
        lblNumberOfSales.setForeground(TEXT_PRIMARY);
        mainPanel.add(createMetricSummaryCard("Número de Ventas", "Tickets registrados", ACCENT_CYAN, lblNumberOfSales));

        lblTotalProfit = new JLabel("$0.00");
        lblTotalProfit.setForeground(ACCENT_GREEN);
        mainPanel.add(createMetricSummaryCard("Ganancia", "Utilidad acumulada", ACCENT_GREEN, lblTotalProfit));

        lblAverageSale = new JLabel("$0.00");
        lblAverageSale.setForeground(TEXT_PRIMARY);
        mainPanel.add(createMetricSummaryCard("Venta Promedio", "Valor medio por ticket", ACCENT_GOLD, lblAverageSale));

        lblProfitMargin = new JLabel("0.00%");
        lblProfitMargin.setForeground(ACCENT_BLUE);
        mainPanel.add(createMetricSummaryCard("Margen", "Rentabilidad porcentual", ACCENT_BLUE, lblProfitMargin));

        return mainPanel;
    }
    
    private JPanel createMetricRowEleventa(String labelText) {
        JPanel panel = new JPanel(new BorderLayout(25, 0));
        panel.setBackground(new Color(249, 250, 251));
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(229, 231, 235)), // Línea divisoria más visible
            BorderFactory.createEmptyBorder(16, 0, 16, 0) // Más padding vertical
        ));
        
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 15)); // Más grande
        label.setForeground(new Color(75, 85, 99)); // Gris más oscuro para mejor contraste
        panel.add(label, BorderLayout.WEST);
        
        return panel;
    }
    
    private JPanel createSalesMetricsPanel() {
        // Panel principal con dos filas
        JPanel mainPanel = new JPanel(new BorderLayout(0, 10));
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        // Primera fila: Ventas Totales | Ganancia
        JPanel firstRow = new JPanel(new GridLayout(1, 2, 10, 0));
        firstRow.setBackground(Color.WHITE);
        
        // Métrica 1: Ventas Totales
        JPanel salesTotalPanel = createMetricCell("Ventas Totales");
        if (lblSalesTotal == null) {
        lblSalesTotal = new JLabel("$0.00");
        lblSalesTotal.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblSalesTotal.setForeground(new Color(50, 50, 50));
        }
        salesTotalPanel.add(lblSalesTotal, BorderLayout.CENTER);
        firstRow.add(salesTotalPanel);
        
        // Métrica 2: Ganancia
        JPanel profitPanel = createMetricCell("Ganancia");
        if (lblTotalProfit == null) {
        lblTotalProfit = new JLabel("$0.00");
        lblTotalProfit.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTotalProfit.setForeground(new Color(50, 50, 50));
        }
        profitPanel.add(lblTotalProfit, BorderLayout.CENTER);
        firstRow.add(profitPanel);
        
        // Segunda fila: Número de Ventas | Venta Promedio | Margen de utilidad promedio
        JPanel secondRow = new JPanel(new GridLayout(1, 3, 10, 0));
        secondRow.setBackground(Color.WHITE);
        
        // Métrica 3: Número de Ventas
        JPanel numberSalesPanel = createMetricCell("Número de Ventas");
        if (lblNumberOfSales == null) {
        lblNumberOfSales = new JLabel("0");
        lblNumberOfSales.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblNumberOfSales.setForeground(new Color(50, 50, 50));
        }
        numberSalesPanel.add(lblNumberOfSales, BorderLayout.CENTER);
        secondRow.add(numberSalesPanel);
        
        // Métrica 4: Venta Promedio
        JPanel averageSalePanel = createMetricCell("Venta Promedio");
        if (lblAverageSale == null) {
        lblAverageSale = new JLabel("$0.00");
        lblAverageSale.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblAverageSale.setForeground(new Color(50, 50, 50));
        }
        averageSalePanel.add(lblAverageSale, BorderLayout.CENTER);
        secondRow.add(averageSalePanel);
        
        // Métrica 5: Margen de utilidad promedio
        JPanel profitMarginPanel = createMetricCell("Margen de utilidad promedio");
        if (lblProfitMargin == null) {
        lblProfitMargin = new JLabel("0.00%");
        lblProfitMargin.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblProfitMargin.setForeground(new Color(50, 50, 50));
        }
        profitMarginPanel.add(lblProfitMargin, BorderLayout.CENTER);
        secondRow.add(profitMarginPanel);
        
        mainPanel.add(firstRow, BorderLayout.NORTH);
        mainPanel.add(secondRow, BorderLayout.SOUTH);
        
        return mainPanel;
    }
    
    private JPanel createMetricCell(String labelText) {
        JPanel panel = new JPanel(new BorderLayout(0, 5));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        label.setForeground(new Color(100, 100, 100));
        panel.add(label, BorderLayout.NORTH);
        
        return panel;
    }
    
    private String getTabName(int index) {
        switch (index) {
            case 0: return "📅 Día Actual";
            case 1: return "📅 Semana Actual";
            case 2: return "📅 Mes Actual";
            case 3: return "📅 Año Actual";
            case 4: return "📅 Periodo...";
            default: return "📅 Periodo";
        }
    }
    
    private void loadCurrentMonthData() {
        // Cargar mes actual por defecto (internalIndex 2)
        loadDataForPeriod(2);
    }
    
    private void loadDataForPeriod(int periodIndex) {
        SwingUtilities.invokeLater(() -> {
            try {
                // Sebastian - Para reportes, NO usamos la fecha de inicio de caja del usuario actual
                // Mostramos TODAS las ventas del período seleccionado, sin importar quién las hizo
                Calendar cal = Calendar.getInstance();
                java.util.Date endDate = new java.util.Date();
                
                switch (periodIndex) {
                    case 0: // Día Actual - desde inicio del día de hoy
                        Calendar todayCal = Calendar.getInstance();
                        todayCal.set(Calendar.HOUR_OF_DAY, 0);
                        todayCal.set(Calendar.MINUTE, 0);
                        todayCal.set(Calendar.SECOND, 0);
                        todayCal.set(Calendar.MILLISECOND, 0);
                        dateStart = todayCal.getTime(); // Siempre desde inicio del día
                        dateEnd = endDate; // Hasta ahora
                        break;
                    case 1: // Semana Actual - desde lunes de esta semana
                        dateStart = getWeekStart(); // Siempre desde inicio de la semana
                        dateEnd = endDate;
                        break;
                    case 2: // Mes Actual - desde inicio del mes
                        dateStart = getMonthStart(); // Siempre desde inicio del mes
                        dateEnd = endDate;
                        break;
                    case 3: // Año Actual - desde inicio del año
                        dateStart = getYearStart(); // Siempre desde inicio del año
                        dateEnd = endDate;
                        break;
                    case 4: // Periodo personalizado
                        showCustomPeriodDialog();
                        return;
                    case 5: // Mes Anterior - todo el mes anterior completo
                        Calendar prevMonthCal = Calendar.getInstance();
                        prevMonthCal.add(Calendar.MONTH, -1);
                        prevMonthCal.set(Calendar.DAY_OF_MONTH, 1);
                        prevMonthCal.set(Calendar.HOUR_OF_DAY, 0);
                        prevMonthCal.set(Calendar.MINUTE, 0);
                        prevMonthCal.set(Calendar.SECOND, 0);
                        prevMonthCal.set(Calendar.MILLISECOND, 0);
                        dateStart = prevMonthCal.getTime();
                        
                        // Último día del mes anterior
                        Calendar prevMonthEnd = Calendar.getInstance();
                        prevMonthEnd.set(Calendar.DAY_OF_MONTH, 1);
                        prevMonthEnd.add(Calendar.DAY_OF_MONTH, -1);
                        prevMonthEnd.set(Calendar.HOUR_OF_DAY, 23);
                        prevMonthEnd.set(Calendar.MINUTE, 59);
                        prevMonthEnd.set(Calendar.SECOND, 59);
                        prevMonthEnd.set(Calendar.MILLISECOND, 999);
                        dateEnd = prevMonthEnd.getTime();
                        break;
                }
                
                LOGGER.info("Periodo seleccionado: " + periodIndex + ", Fecha inicio: " + dateStart + ", Fecha fin: " + dateEnd);
                loadDataFromDatabase();
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error cargando datos del periodo", e);
                JOptionPane.showMessageDialog(this, 
                    "Error al cargar los datos: " + e.getMessage(),
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        });
    }
    
    private java.util.Date getCashStartDate() {
        try {
            if (session == null || activeCashIndex == null) {
                return null;
            }
            
            PreparedStatement stmt = session.getConnection().prepareStatement(
                "SELECT DATESTART FROM closedcash WHERE MONEY = ? AND DATEEND IS NULL");
            stmt.setString(1, activeCashIndex);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Timestamp ts = rs.getTimestamp("DATESTART");
                rs.close();
                stmt.close();
                if (ts != null) {
                    LOGGER.info("Fecha de inicio de caja: " + ts);
                    return new java.util.Date(ts.getTime());
                }
            }
            rs.close();
            stmt.close();
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error obteniendo fecha de inicio de caja", e);
        }
        return null;
    }
    
    private java.util.Date getWeekStart() {
        Calendar cal = Calendar.getInstance();
        // Retroceder hasta el lunes más reciente
        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        int daysFromMonday = (dayOfWeek == Calendar.SUNDAY) ? 6 : dayOfWeek - Calendar.MONDAY;
        cal.add(Calendar.DAY_OF_MONTH, -daysFromMonday);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }
    
    private java.util.Date getMonthStart() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        return cal.getTime();
    }
    
    private java.util.Date getYearStart() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_YEAR, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        return cal.getTime();
    }
    
    private void showCustomPeriodDialog() {
        // TODO: Implementar selector de fechas
        LOGGER.info("Mostrando diálogo de periodo personalizado");
        loadDataForPeriod(1); // Por ahora carga mes actual
    }
    
    private int loadNumberOfSales() {
        int count = 0;
        try {
            // Sebastian - Ya no requerimos activeCashIndex para reportes
            if (session == null) {
                return 0;
            }
            
            StringBuilder sqlBuilder = new StringBuilder();
            sqlBuilder.append("SELECT COUNT(DISTINCT receipts.ID) as TOTAL_COUNT ");
            sqlBuilder.append("FROM receipts ");
            sqlBuilder.append("INNER JOIN tickets ON receipts.ID = tickets.ID ");
            
            // Sebastian - Para reportes, mostrar TODAS las ventas, no solo las de la caja activa del usuario actual
            // Usar solo filtros de fecha para los reportes
            boolean hasDateFilter = false;
            if (dateStart != null) {
                sqlBuilder.append(hasDateFilter ? "AND " : "WHERE ");
                sqlBuilder.append("receipts.DATENEW >= ? ");
                hasDateFilter = true;
            }
            if (dateEnd != null) {
                sqlBuilder.append(hasDateFilter ? "AND " : "WHERE ");
                sqlBuilder.append("receipts.DATENEW <= ? ");
                hasDateFilter = true;
            }
            
            PreparedStatement stmt = session.getConnection().prepareStatement(sqlBuilder.toString());
            int paramIndex = 1;
            // Ya no se usa activeCashIndex para reportes
            if (dateStart != null) {
                stmt.setTimestamp(paramIndex++, new Timestamp(dateStart.getTime()));
            }
            if (dateEnd != null) {
                stmt.setTimestamp(paramIndex++, new Timestamp(dateEnd.getTime()));
            }
            
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                count = rs.getInt("TOTAL_COUNT");
            }
            rs.close();
            stmt.close();
            
            LOGGER.info("Número de ventas encontradas: " + count);
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error obteniendo número de ventas", e);
        }
        
        return count;
    }
    
    private void loadDataFromDatabase() throws BasicException {
        // Sebastian - Para reportes, ya no requerimos activeCashIndex porque mostramos TODAS las ventas
        if (session == null) {
            LOGGER.warning("Session es null. Session: " + (session != null));
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(this, 
                    "Error: No se pudo obtener la sesión de base de datos.",
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
            });
            return;
        }
        
        LOGGER.info("═══════════════════════════════════════════════");
        LOGGER.info("Cargando datos desde base de datos.");
        LOGGER.info("CashIndex: " + activeCashIndex);
        LOGGER.info("Fecha inicio: " + (dateStart != null ? dateStart : "NULL"));
        LOGGER.info("Fecha fin: " + (dateEnd != null ? dateEnd : "NULL"));
        
        try {
            // 0. Verificar si hay recepciones (receipts) en la base de datos
            verifyReceiptsExist();
            
            // 1. Obtener ventas por departamento con ganancias
            List<DepartmentData> departments = loadDepartmentData();
            LOGGER.info("Departamentos cargados: " + (departments != null ? departments.size() : 0));
            if (departments != null && !departments.isEmpty()) {
                for (DepartmentData dept : departments) {
                    LOGGER.info("  - " + dept.name + ": Ventas=" + dept.sales + ", Ganancia=" + dept.profit);
                }
            } else {
                LOGGER.warning("⚠️ No se encontraron departamentos con ventas en el periodo seleccionado");
            }
            
            // 2. Obtener ventas por forma de pago
            List<PaymentData> payments = loadPaymentData();
            LOGGER.info("Formas de pago cargadas: " + (payments != null ? payments.size() : 0));
            
            // 3. Obtener número de ventas y ventas por mes
            int numberOfSales = loadNumberOfSales();
            List<MonthData> monthData = loadSalesProfitByMonth();
            List<TaxData> taxData = loadTaxData();
            
            // 4. Calcular totales
            final List<DepartmentData> finalDepartments = departments != null ? departments : new ArrayList<>();
            final List<PaymentData> finalPayments = payments != null ? payments : new ArrayList<>();
            final List<MonthData> finalMonthData = monthData != null ? monthData : new ArrayList<>();
            final List<TaxData> finalTaxData = taxData != null ? taxData : new ArrayList<>();
            
            double totalSalesCalc = 0.0;
            double totalProfitCalc = 0.0;
            for (DepartmentData dept : finalDepartments) {
                totalSalesCalc += dept.sales;
                totalProfitCalc += dept.profit;
            }
            final double totalSales = totalSalesCalc;
            final double totalProfit = totalProfitCalc;
            final int finalNumberOfSales = numberOfSales;
            final double averageSale = (numberOfSales > 0) ? (totalSales / numberOfSales) : 0.0;
            
            LOGGER.info("Totales calculados - Ventas: " + totalSales + ", Ganancia: " + totalProfit + ", Número de ventas: " + numberOfSales);
            LOGGER.info("═══════════════════════════════════════════════");
            
            // 5. Actualizar UI en el hilo de eventos
            SwingUtilities.invokeLater(() -> {
                // PRIMERO: Actualizar el gráfico principal de Ventas y Ganancias
                updateSalesProfitBarChart(totalSales, totalProfit);
                // Luego los otros gráficos
                updatePieChart(finalDepartments); // Gráfico de dona para ganancia por departamento
                updateMonthDonutChart(finalMonthData); // Gráfico de dona para ventas por mes
                updateBarChart(finalPayments);
                // Actualizar tablas
                updateSalesByMonthTable(finalMonthData); // Tabla de ventas por mes
                updateHourlyChart(); // Gráfico de secuencia por hora
                updateDepartmentTable(finalDepartments); // Tabla de ventas por departamento
                updateDepartmentProfitList(finalDepartments); // Lista de ganancia por departamento
                updateTaxBreakdown(finalTaxData);
                
                // Actualizar métricas de ventas
                if (lblSalesTotal != null) {
                    lblSalesTotal.setText(Formats.CURRENCY.formatValue(totalSales));
                }
                if (lblTotalProfit != null) {
                    lblTotalProfit.setText(Formats.CURRENCY.formatValue(totalProfit));
                }
                if (lblNumberOfSales != null) {
                    lblNumberOfSales.setText(String.valueOf(finalNumberOfSales));
                }
                if (lblAverageSale != null) {
                    lblAverageSale.setText(Formats.CURRENCY.formatValue(averageSale));
                }
                if (lblProfitMargin != null) {
                    double profitMargin = (totalSales > 0) ? ((totalProfit / totalSales) * 100.0) : 0.0;
                    lblProfitMargin.setText(String.format("%.2f%%", profitMargin));
                }
                
                // Forzar repaint
                if (chartPanelSalesProfit != null) {
                    chartPanelSalesProfit.repaint();
                }
                if (chartPanelDepartment != null) {
                    chartPanelDepartment.repaint();
                }
                repaint();
            });
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error SQL cargando datos", e);
            e.printStackTrace();
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(this, 
                    "Error al cargar datos: " + e.getMessage(),
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
            });
            throw new BasicException(e);
        }
    }
    
    private void verifyReceiptsExist() {
        try {
            StringBuilder sqlBuilder = new StringBuilder();
            sqlBuilder.append("SELECT COUNT(*) as TOTAL_RECEIPTS, ");
            sqlBuilder.append("MIN(receipts.DATENEW) as MIN_DATE, ");
            sqlBuilder.append("MAX(receipts.DATENEW) as MAX_DATE ");
            sqlBuilder.append("FROM receipts ");
            
            // Sebastian - Para reportes, mostrar TODAS las ventas, no solo las de la caja activa del usuario actual
            // Usar solo filtros de fecha para los reportes
            boolean hasDateFilter = false;
            if (dateStart != null) {
                sqlBuilder.append(hasDateFilter ? "AND " : "WHERE ");
                sqlBuilder.append("receipts.DATENEW >= ? ");
                hasDateFilter = true;
            }
            if (dateEnd != null) {
                sqlBuilder.append(hasDateFilter ? "AND " : "WHERE ");
                sqlBuilder.append("receipts.DATENEW <= ? ");
                hasDateFilter = true;
            }
            
            PreparedStatement stmt = session.getConnection().prepareStatement(sqlBuilder.toString());
            int paramIndex = 1;
            // Ya no se usa activeCashIndex para reportes
            if (dateStart != null) {
                stmt.setTimestamp(paramIndex++, new Timestamp(dateStart.getTime()));
            }
            if (dateEnd != null) {
                stmt.setTimestamp(paramIndex++, new Timestamp(dateEnd.getTime()));
            }
            
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int totalReceipts = rs.getInt("TOTAL_RECEIPTS");
                Timestamp minDate = rs.getTimestamp("MIN_DATE");
                Timestamp maxDate = rs.getTimestamp("MAX_DATE");
                LOGGER.info("📊 Verificación: Total de recepciones encontradas: " + totalReceipts);
                if (minDate != null) {
                    LOGGER.info("  - Primera recepción: " + minDate);
                }
                if (maxDate != null) {
                    LOGGER.info("  - Última recepción: " + maxDate);
                }
                if (totalReceipts == 0) {
                    LOGGER.warning("⚠️ No se encontraron recepciones en la base de datos para el periodo seleccionado");
                }
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error verificando recepciones", e);
        }
    }
    
    private List<DepartmentData> loadDepartmentData() throws SQLException {
        List<DepartmentData> departments = new ArrayList<>();
        
        try {
            // Construir la consulta SQL dinámicamente
            StringBuilder sqlBuilder = new StringBuilder();
            sqlBuilder.append("SELECT COALESCE(categories.NAME, 'Sin Departamento') as CATEGORY_NAME, ");
            sqlBuilder.append("COALESCE(categories.ID, '') as CATEGORY_ID, ");
            sqlBuilder.append("SUM((ticketlines.PRICE + ticketlines.PRICE * taxes.RATE) * ticketlines.UNITS) as TOTAL_SALES, ");
            sqlBuilder.append("SUM((ticketlines.PRICE - COALESCE(products.PRICEBUY, 0)) * ticketlines.UNITS) as TOTAL_PROFIT ");
            sqlBuilder.append("FROM ticketlines ");
            sqlBuilder.append("INNER JOIN tickets ON ticketlines.TICKET = tickets.ID ");
            sqlBuilder.append("INNER JOIN receipts ON tickets.ID = receipts.ID ");
            sqlBuilder.append("INNER JOIN products ON ticketlines.PRODUCT = products.ID ");
            sqlBuilder.append("LEFT JOIN categories ON products.CATEGORY = categories.ID ");
            sqlBuilder.append("INNER JOIN taxes ON ticketlines.TAXID = taxes.ID ");
            
            // Sebastian - Para reportes, mostrar TODAS las ventas, no solo las de la caja activa del usuario actual
            // Usar solo filtros de fecha para los reportes
            boolean hasDateFilter = false;
            if (dateStart != null) {
                sqlBuilder.append(hasDateFilter ? "AND " : "WHERE ");
                sqlBuilder.append("receipts.DATENEW >= ? ");
                hasDateFilter = true;
            }
            if (dateEnd != null) {
                sqlBuilder.append(hasDateFilter ? "AND " : "WHERE ");
                sqlBuilder.append("receipts.DATENEW <= ? ");
                hasDateFilter = true;
            }
            
            sqlBuilder.append("GROUP BY COALESCE(categories.NAME, 'Sin Departamento'), COALESCE(categories.ID, '') ");
            sqlBuilder.append("ORDER BY TOTAL_PROFIT DESC");
            
            String sql = sqlBuilder.toString();
            LOGGER.info("Ejecutando consulta de departamentos");
            LOGGER.info("Parámetros: (TODAS las ventas, sin filtrar por caja)");
            if (dateStart != null) {
                LOGGER.info("  - Filtro fecha inicio: " + dateStart);
            } else {
                LOGGER.info("  - Sin filtro de fecha inicio (todas las ventas)");
            }
            if (dateEnd != null) {
                LOGGER.info("  - Filtro fecha fin: " + dateEnd);
            } else {
                LOGGER.info("  - Sin filtro de fecha fin (todas las ventas)");
            }
            
            PreparedStatement stmt = session.getConnection().prepareStatement(sql);
            int paramIndex = 1;
            // Ya no se usa activeCashIndex para reportes
            if (dateStart != null) {
                Timestamp tsStart = new Timestamp(dateStart.getTime());
                stmt.setTimestamp(paramIndex++, tsStart);
                LOGGER.info("  - Parámetro fecha inicio establecido: " + tsStart);
            }
            if (dateEnd != null) {
                Timestamp tsEnd = new Timestamp(dateEnd.getTime());
                stmt.setTimestamp(paramIndex++, tsEnd);
                LOGGER.info("  - Parámetro fecha fin establecido: " + tsEnd);
            }
            
            ResultSet rs = stmt.executeQuery();
            int index = 1;
            int rowCount = 0;
            while (rs.next()) {
                rowCount++;
                String name = rs.getString("CATEGORY_NAME");
                double sales = rs.getDouble("TOTAL_SALES");
                double profit = rs.getDouble("TOTAL_PROFIT");
                
                LOGGER.info("Fila " + rowCount + ": " + name + " - Ventas: " + sales + ", Ganancia: " + profit);
                
                // Solo agregar si hay ventas
                if (sales > 0 || profit > 0) {
                    // Intentar obtener el índice de la categoría si existe
                    String catId = rs.getString("CATEGORY_ID");
                    int deptIndex = index;
                    try {
                        if (catId != null && !catId.isEmpty()) {
                            PreparedStatement catStmt = session.getConnection().prepareStatement(
                                "SELECT ORDERNUM FROM categories WHERE ID = ?");
                            catStmt.setString(1, catId);
                            ResultSet catRs = catStmt.executeQuery();
                            if (catRs.next()) {
                                int orderNum = catRs.getInt("ORDERNUM");
                                if (orderNum > 0) {
                                    deptIndex = orderNum;
                                }
                            }
                            catRs.close();
                            catStmt.close();
                        }
                    } catch (Exception e) {
                        LOGGER.log(Level.FINE, "No se pudo obtener ORDERNUM para categoría " + catId, e);
                    }
                    
                    departments.add(new DepartmentData(name, sales, profit, deptIndex));
                    index++;
                }
            }
            rs.close();
            stmt.close();
            
            LOGGER.info("Total filas procesadas: " + rowCount + ", Departamentos agregados: " + departments.size());
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error SQL cargando departamentos: " + e.getMessage(), e);
            e.printStackTrace();
            throw e;
        }
        
        return departments;
    }
    
    private List<PaymentData> loadPaymentData() throws SQLException, BasicException {
        List<PaymentData> payments = new ArrayList<>();
        
        try {
            Map<String, String> paymentNames = new HashMap<>();
            paymentNames.put("cash", "Efectivo");
            paymentNames.put("card", "Tarjeta");
            paymentNames.put("magcard", "Tarjeta");
            paymentNames.put("debt", "Crédito");
            paymentNames.put("voucher", "Vales");
            paymentNames.put("transfer", "Transferencia");
            paymentNames.put("cheque", "Cheque");
            
            StringBuilder sqlBuilder = new StringBuilder();
            sqlBuilder.append("SELECT payments.PAYMENT, SUM(payments.TOTAL) as TOTAL ");
            sqlBuilder.append("FROM payments ");
            sqlBuilder.append("INNER JOIN receipts ON payments.RECEIPT = receipts.ID ");
            
            // Sebastian - Para reportes, mostrar TODAS las ventas, no solo las de la caja activa del usuario actual
            // Usar solo filtros de fecha para los reportes
            boolean hasDateFilter = false;
            if (dateStart != null) {
                sqlBuilder.append(hasDateFilter ? "AND " : "WHERE ");
                sqlBuilder.append("receipts.DATENEW >= ? ");
                hasDateFilter = true;
            }
            if (dateEnd != null) {
                sqlBuilder.append(hasDateFilter ? "AND " : "WHERE ");
                sqlBuilder.append("receipts.DATENEW <= ? ");
                hasDateFilter = true;
            }
            sqlBuilder.append("GROUP BY payments.PAYMENT");
            
            String sql = sqlBuilder.toString();
            LOGGER.info("Ejecutando consulta de formas de pago: " + sql);
            LOGGER.info("Parámetros: (TODAS las ventas, sin filtrar por caja)");
            
            PreparedStatement stmt = session.getConnection().prepareStatement(sql);
            int paramIndex = 1;
            // Ya no se usa activeCashIndex para reportes
            if (dateStart != null) {
                stmt.setTimestamp(paramIndex++, new Timestamp(dateStart.getTime()));
            }
            if (dateEnd != null) {
                stmt.setTimestamp(paramIndex++, new Timestamp(dateEnd.getTime()));
            }
            
            ResultSet rs = stmt.executeQuery();
            int rowCount = 0;
            while (rs.next()) {
                rowCount++;
                String type = rs.getString("PAYMENT");
                double amount = Math.abs(rs.getDouble("TOTAL")); // Usar valor absoluto
                LOGGER.info("Forma de pago encontrada: " + type + " = " + amount);
                if (amount > 0) {
                    String displayName = paymentNames.getOrDefault(type, type);
                    payments.add(new PaymentData(type, displayName, amount));
                }
            }
            rs.close();
            stmt.close();
            
            LOGGER.info("Total formas de pago encontradas: " + rowCount + ", Agregadas: " + payments.size());
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error SQL cargando formas de pago: " + e.getMessage(), e);
            e.printStackTrace();
            throw e;
        }
        
        return payments;
    }
    
    @Override
    public JComponent getComponent() {
        return this;
    }
    
    @Override
    public String getTitle() {
        return AppLocal.getIntString("Menu.Graphics");
    }
    
    @Override
    public void activate() throws BasicException {
        // Asegurar que m_App esté inicializado
        if (m_App == null) {
            LOGGER.severe("m_App es null, no se puede activar el panel de gráficos");
            return;
        }
        
        // Asegurar que session esté disponible
        if (session == null) {
            session = m_App.getSession();
        }
        
        // Sebastian - Ya no requerimos activeCashIndex para reportes, mostramos TODAS las ventas
        // Obtener activeCashIndex solo para logging, pero no es necesario para cargar datos
        if (activeCashIndex == null) {
            activeCashIndex = m_App.getActiveCashIndex();
        }
        
        LOGGER.info("Panel de gráficos activado. CashIndex: " + activeCashIndex + " (no usado para reportes), Session: " + (session != null));
        
        if (session == null) {
            LOGGER.warning("⚠️ Session es null - los datos no se cargarán.");
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(this, 
                    "Advertencia: No se pudo obtener la sesión de base de datos.",
                    "Error de sesión", 
                    JOptionPane.WARNING_MESSAGE);
            });
            return;
        }
        
        // Cargar datos del mes actual por defecto (como está seleccionado en los links)
        SwingUtilities.invokeLater(() -> {
            try {
                loadDataForPeriod(2); // Mes Actual (internalIndex 2)
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error recargando datos al activar panel", e);
            }
        });
    }
    
    @Override
    public boolean deactivate() {
        return true;
    }
}


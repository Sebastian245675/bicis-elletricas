package com.openbravo.pos.sales;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import com.openbravo.basic.BasicException;
import com.openbravo.data.gui.MessageInf;
import com.openbravo.format.Formats;
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.pos.forms.AppView;
import com.openbravo.pos.forms.BeanFactoryApp;
import com.openbravo.pos.forms.BeanFactoryException;
import com.openbravo.pos.forms.DataLogicSales;
import com.openbravo.pos.forms.JPanelView;
import com.openbravo.pos.ticket.TicketInfo;
import com.openbravo.pos.ticket.TicketLineInfo;
import com.openbravo.beans.JCalendarDialog;
import com.openbravo.pos.forms.DataLogicSystem;
import com.openbravo.pos.printer.TicketParser;
import com.openbravo.pos.scripting.ScriptEngine;
import com.openbravo.pos.scripting.ScriptFactory;
import com.openbravo.pos.sales.TaxesLogic;
import com.openbravo.pos.util.ModernActionIcon;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Panel rediseñado de Facturación Electrónica.
 * Implementa exactamente el diseño premium de Voltium Sanrey.
 * 
 * @author Sebastian / Antigravity
 */
public class JPanelFacturadas extends JPanel implements JPanelView, BeanFactoryApp {

    private static final Logger LOGGER = Logger.getLogger(JPanelFacturadas.class.getName());

    private static final long serialVersionUID = 1L;

    private AppView m_App;
    private DataLogicSales m_dlSales;

    // Componentes principales
    private JTable tablaFacturas;
    private DefaultTableModel modeloTabla;
    private JTextField txtRangoFechas;
    private JLabel lblTotalPeriodo;
    private JLabel lblFacturasGeneradas;
    private JLabel lblFacturasCanceladas;
    private JLabel lblRegistrosCount;

    private PillButton btnHoy;
    private PillButton btnSemana;
    private PillButton btnMes;
    private SearchButton btnBuscar;

    // Rango de fechas seleccionado en memoria
    private Date dateDesde;
    private Date dateHasta;

    // Datos en memoria
    private List<Object[]> listFacturas;

    // Colores de la paleta premium
    private static final Color DASH_BG = new Color(241, 245, 249);         // slate-100 (fondo general)
    private static final Color DASH_CARD_BORDER = new Color(226, 232, 240); // slate-200
    private static final Color DASH_TEXT_PRI = new Color(15, 23, 42);       // slate-900
    private static final Color DASH_TEXT_MUT = new Color(71, 85, 105);      // slate-600
    private static final Color DASH_TEAL_NUM = new Color(15, 118, 110);     // teal-700 (números KPI)

    // Sebastian - Iconos personalizados de assets
    private static ImageIcon iconPdfCustom = null;
    private static ImageIcon iconExcelCustom = null;
    static {
        try {
            ImageIcon pdfRaw = new ImageIcon(JPanelFacturadas.class.getResource("/com/openbravo/images/toolbar-pdf.png"));
            if (pdfRaw.getIconWidth() > 0) {
                Image img = pdfRaw.getImage().getScaledInstance(22, 22, Image.SCALE_SMOOTH);
                iconPdfCustom = new ImageIcon(img);
            }
            
            ImageIcon excelRaw = new ImageIcon(JPanelFacturadas.class.getResource("/com/openbravo/images/toolbar-excel.png"));
            if (excelRaw.getIconWidth() > 0) {
                Image img = excelRaw.getImage().getScaledInstance(22, 22, Image.SCALE_SMOOTH);
                iconExcelCustom = new ImageIcon(img);
            }
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "Error al cargar los iconos personalizados: " + ex.getMessage(), ex);
        }
    }

    public JPanelFacturadas() {
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setBackground(DASH_BG);
        setBorder(new EmptyBorder(24, 24, 24, 24));

        // ==========================================
        // 1. PANEL SUPERIOR: Título y Buscador de Fechas
        // ==========================================
        JPanel topPanel = new JPanel(new BorderLayout(20, 20));
        topPanel.setOpaque(false);

        // Título del Dashboard
        JLabel lblTitle = new JLabel("Facturación Electrónica - Historial y Totales");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitle.setForeground(DASH_TEXT_PRI);
        topPanel.add(lblTitle, BorderLayout.WEST);

        // Contenedor del Buscador / Selector de fechas (arriba a la derecha)
        JPanel dateCard = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(248, 250, 252)); // Gris muy claro (slate-50)
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.setColor(DASH_CARD_BORDER);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
                g2.dispose();
            }
        };
        dateCard.setOpaque(false);
        dateCard.setBorder(new EmptyBorder(12, 16, 12, 16));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Fila 1: Display del rango y botón de calendario
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 3;
        gbc.weightx = 1.0;
        txtRangoFechas = new JTextField("Selecciona rango...");
        txtRangoFechas.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtRangoFechas.setEditable(false);
        txtRangoFechas.setBackground(Color.WHITE);
        txtRangoFechas.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(DASH_CARD_BORDER, 1),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        txtRangoFechas.setCursor(new Cursor(Cursor.HAND_CURSOR));
        txtRangoFechas.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                seleccionarRangoFechasCustom();
            }
        });
        dateCard.add(txtRangoFechas, gbc);

        gbc.gridx = 3;
        gbc.gridwidth = 1;
        gbc.weightx = 0.0;
        JButton btnCalendario = new JButton(new ImageIcon(getClass().getResource("/com/openbravo/images/date.png")));
        btnCalendario.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCalendario.setContentAreaFilled(false);
        btnCalendario.setBorderPainted(false);
        btnCalendario.setFocusPainted(false);
        btnCalendario.addActionListener(e -> seleccionarRangoFechasCustom());
        dateCard.add(btnCalendario, gbc);

        // Fila 2: Botones rápidos y Buscar
        gbc.gridy = 1;
        gbc.gridx = 0;
        gbc.weightx = 0.25;
        btnHoy = new PillButton("Hoy");
        btnHoy.addActionListener(e -> seleccionarPeriodoRapido("HOY"));
        dateCard.add(btnHoy, gbc);

        gbc.gridx = 1;
        btnSemana = new PillButton("Esta Semana");
        btnSemana.addActionListener(e -> seleccionarPeriodoRapido("SEMANA"));
        dateCard.add(btnSemana, gbc);

        gbc.gridx = 2;
        btnMes = new PillButton("Este Mes");
        btnMes.addActionListener(e -> seleccionarPeriodoRapido("MES"));
        dateCard.add(btnMes, gbc);

        gbc.gridx = 3;
        btnBuscar = new SearchButton("Buscar");
        btnBuscar.addActionListener(e -> ejecutarConsulta(dateDesde, dateHasta));
        dateCard.add(btnBuscar, gbc);

        topPanel.add(dateCard, BorderLayout.EAST);

        // ==========================================
        // 2. PANEL CENTRAL: KPI Cards y Tabla
        // ==========================================
        JPanel centerPanel = new JPanel(new BorderLayout(20, 20));
        centerPanel.setOpaque(false);

        // Fila de 3 KPIs
        JPanel kpiRow = new JPanel(new GridLayout(1, 3, 20, 0));
        kpiRow.setOpaque(false);

        lblTotalPeriodo = new JLabel("$0.00");
        lblFacturasGeneradas = new JLabel("0");
        lblFacturasCanceladas = new JLabel("0");

        kpiRow.add(createKpiCard("Total del Período", lblTotalPeriodo, "$", new Color(15, 118, 110)));
        kpiRow.add(createKpiCard("Facturas Generadas", lblFacturasGeneradas, "\uD83D\uDCB5", new Color(15, 118, 110)));
        kpiRow.add(createKpiCard("Canceladas", lblFacturasCanceladas, "\u274C", new Color(220, 38, 38)));

        centerPanel.add(kpiRow, BorderLayout.NORTH);

        // Panel de Tabla (Estilo tarjeta)
        JPanel tableContainer = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(Color.WHITE);
                g2d.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
                g2d.setColor(DASH_CARD_BORDER);
                g2d.drawRoundRect(0, 0, getWidth() - 2, getHeight() - 2, 14, 14);
                g2d.dispose();
            }
        };
        tableContainer.setOpaque(false);
        tableContainer.setBorder(new EmptyBorder(1, 1, 1, 1));

        // Inicializar tabla
        modeloTabla = new DefaultTableModel(new String[]{"ID", "Ticket", "Fecha / Hora", "Estatus", "Total", "Acciones"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tablaFacturas = new JTable(modeloTabla);
        styleTable(tablaFacturas);

        // Listener de clicks para columna Acciones
        tablaFacturas.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = tablaFacturas.rowAtPoint(e.getPoint());
                int col = tablaFacturas.columnAtPoint(e.getPoint());
                if (row >= 0 && col == 5) { // Columna Acciones
                    row = tablaFacturas.convertRowIndexToModel(row);
                    int cellWidth = tablaFacturas.getColumnModel().getColumn(5).getWidth();
                    int clickX = e.getX() - tablaFacturas.getCellRect(row, col, false).x;
                    
                    int actionIdx = clickX / (cellWidth / 3);
                    if (actionIdx == 0) {
                        doViewFactura(row);
                    } else if (actionIdx == 1) {
                        doDownloadPdf(row);
                    } else {
                        doDownloadXml(row);
                    }
                }
            }
        });

        JScrollPane scrollTable = new JScrollPane(tablaFacturas);
        scrollTable.setBorder(BorderFactory.createEmptyBorder());
        scrollTable.getViewport().setBackground(Color.WHITE);
        scrollTable.getVerticalScrollBar().setUnitIncrement(16);

        tableContainer.add(scrollTable, BorderLayout.CENTER);

        // Footer del contenedor de tabla (Paginación + conteo)
        JPanel tableFooter = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(DASH_CARD_BORDER);
                g2.fillRect(0, 0, getWidth(), 1);
                g2.dispose();
            }
        };
        tableFooter.setBackground(new Color(248, 250, 252));
        tableFooter.setBorder(new EmptyBorder(12, 20, 12, 20));

        // Controles de Paginación de mockup
        JPanel paginationPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
        paginationPanel.setOpaque(false);
        paginationPanel.add(createPageButton("<<", false));
        paginationPanel.add(createPageButton("1", true));
        paginationPanel.add(createPageButton("2", false));
        paginationPanel.add(createPageButton("...", false));
        paginationPanel.add(createPageButton(">>", false));
        tableFooter.add(paginationPanel, BorderLayout.WEST);

        lblRegistrosCount = new JLabel("Total: 0 registros");
        lblRegistrosCount.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblRegistrosCount.setForeground(DASH_TEXT_PRI);
        tableFooter.add(lblRegistrosCount, BorderLayout.EAST);

        tableContainer.add(tableFooter, BorderLayout.SOUTH);

        centerPanel.add(tableContainer, BorderLayout.CENTER);

        // Añadir todo al layout principal
        add(topPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
    }

    private void styleTable(JTable table) {
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setRowHeight(44);
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(false);
        table.setGridColor(new Color(236, 240, 244));
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setSelectionBackground(new Color(219, 234, 254)); // blue-100
        table.setSelectionForeground(DASH_TEXT_PRI);
        table.setFillsViewportHeight(true);

        // Header
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setPreferredSize(new Dimension(0, 42));

        final Color headerBg = new Color(241, 245, 249); // slate-100
        final Color headerBorder = DASH_CARD_BORDER;

        final javax.swing.table.TableCellRenderer defaultHeaderRenderer = table.getTableHeader().getDefaultRenderer();
        table.getTableHeader().setDefaultRenderer(new javax.swing.table.TableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val, boolean isSel, boolean hasFocus, int r, int c) {
                Component comp = defaultHeaderRenderer.getTableCellRendererComponent(t, val, isSel, hasFocus, r, c);
                if (comp instanceof JLabel) {
                    JLabel lbl = (JLabel) comp;
                    String text = val != null ? val.toString() : "";
                    if (!"Acciones".equals(text)) {
                        lbl.setText(text + "  \u2195");
                    }
                    lbl.setHorizontalAlignment(JLabel.LEFT);
                    lbl.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 1, headerBorder),
                        BorderFactory.createEmptyBorder(0, 14, 0, 8)
                    ));
                    lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
                    lbl.setBackground(headerBg);
                    lbl.setForeground(DASH_TEXT_MUT);
                    lbl.setOpaque(true);
                }
                return comp;
            }
        });

        // Configuración de anchos
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        table.getColumnModel().getColumn(0).setPreferredWidth(60);  // ID
        table.getColumnModel().getColumn(1).setPreferredWidth(80);  // Ticket
        table.getColumnModel().getColumn(2).setPreferredWidth(200); // Fecha / Hora
        table.getColumnModel().getColumn(3).setPreferredWidth(120); // Estatus
        table.getColumnModel().getColumn(4).setPreferredWidth(100); // Total
        table.getColumnModel().getColumn(5).setPreferredWidth(155); // Acciones

        // Renderizadores personalizados
        table.getColumnModel().getColumn(3).setCellRenderer(new StatusBadgeRenderer());
        table.getColumnModel().getColumn(5).setCellRenderer(new ActionsRenderer());

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
                    if (c == 4) {
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

    private JPanel createKpiCard(String title, JLabel valueLabel, String watermark, Color numberColor) {
        JPanel card = new JPanel(new BorderLayout(15, 6)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Fondo y borde
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
                g2.setColor(DASH_CARD_BORDER);
                g2.drawRoundRect(0, 0, getWidth() - 2, getHeight() - 2, 16, 16);

                // Marca de agua difuminada
                g2.setColor(new Color(15, 118, 110, 16));
                g2.setFont(new Font("Segoe UI", Font.BOLD, 56));
                FontMetrics fm = g2.getFontMetrics();
                int x = getWidth() - fm.stringWidth(watermark) - 24;
                int y = getHeight() / 2 + fm.getAscent() / 3;
                g2.drawString(watermark, x, y);

                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(16, 20, 16, 20));
        card.setPreferredSize(new Dimension(200, 95));

        // Subcontenedor de textos
        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        valueLabel.setForeground(numberColor);
        textPanel.add(valueLabel);
        
        textPanel.add(Box.createVerticalStrut(4));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        titleLabel.setForeground(DASH_TEXT_MUT);
        textPanel.add(titleLabel);

        card.add(textPanel, BorderLayout.CENTER);

        return card;
    }

    private JButton createPageButton(String text, boolean active) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (active) {
                    g2.setColor(new Color(15, 118, 110)); // teal-700
                    setForeground(Color.WHITE);
                } else {
                    g2.setColor(Color.WHITE);
                    setForeground(DASH_TEXT_MUT);
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                g2.setColor(DASH_CARD_BORDER);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 6, 6);
                super.paintComponent(g2);
                g2.dispose();
            }
        };
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(34, 30));
        btn.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    // ==========================================
    // 3. LOGICA Y CARGA DE DATOS
    // ==========================================

    @Override
    public void init(AppView app) throws BeanFactoryException {
        m_App = app;
        m_dlSales = (DataLogicSales) m_App.getBean("com.openbravo.pos.forms.DataLogicSales");
    }

    @Override
    public Object getBean() {
        return this;
    }

    @Override
    public JComponent getComponent() {
        return this;
    }

    @Override
    public String getTitle() {
        return "Facturas Electrónicas";
    }

    @Override
    public void activate() throws BasicException {
        seleccionarPeriodoRapido("SEMANA");
    }

    @Override
    public boolean deactivate() {
        return true;
    }

    private void seleccionarPeriodoRapido(String periodo) {
        btnHoy.setSelected("HOY".equals(periodo));
        btnSemana.setSelected("SEMANA".equals(periodo));
        btnMes.setSelected("MES".equals(periodo));

        Calendar cal = Calendar.getInstance();
        Date end = new Date();
        cal.setTime(end);
        cal.add(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        end = cal.getTime();

        Date start = new Date();
        cal.setTime(new Date());
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        if ("HOY".equals(periodo)) {
            start = cal.getTime();
        } else if ("SEMANA".equals(periodo)) {
            cal.add(Calendar.DAY_OF_MONTH, -7);
            start = cal.getTime();
        } else if ("MES".equals(periodo)) {
            cal.add(Calendar.MONTH, -1);
            start = cal.getTime();
        }

        dateDesde = start;
        dateHasta = end;

        // Actualizar visualización del rango
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        txtRangoFechas.setText(sdf.format(start) + " - " + sdf.format(new Date()));

        ejecutarConsulta(start, end);
    }

    private void seleccionarRangoFechasCustom() {
        Date start = dateDesde != null ? dateDesde : new Date();
        start = JCalendarDialog.showCalendarTimeHours(this, start);
        if (start != null) {
            Date end = dateHasta != null ? dateHasta : new Date();
            end = JCalendarDialog.showCalendarTimeHours(this, end);
            if (end != null) {
                dateDesde = start;
                dateHasta = end;
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                txtRangoFechas.setText(sdf.format(start) + " - " + sdf.format(end));
                btnHoy.setSelected(false);
                btnSemana.setSelected(false);
                btnMes.setSelected(false);
                ejecutarConsulta(start, end);
            }
        }
    }

    private void ejecutarConsulta(Date start, Date end) {
        try {
            listFacturas = m_dlSales.getFacturasElectronicas(start, end);
            modeloTabla.setRowCount(0);

            double totalSuma = 0.0;
            int countTimbradas = 0;
            int countCanceladas = 0;

            int rowId = 1;
            for (Object[] fac : listFacturas) {
                String uuid = (String) fac[0];
                int ticketid = (Integer) fac[1];
                Date fecha = (Date) fac[2];
                double total = (Double) fac[3];
                String estatus = (String) fac[4];

                if ("TIMBRADA".equalsIgnoreCase(estatus)) {
                    totalSuma += total;
                    countTimbradas++;
                } else if ("CANCELADA".equalsIgnoreCase(estatus)) {
                    countCanceladas++;
                }

                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy, hh:mm:ss a");
                modeloTabla.addRow(new Object[]{
                    rowId++,
                    ticketid,
                    sdf.format(fecha),
                    estatus,
                    Formats.CURRENCY.formatValue(total),
                    "👁 📄 XML"
                });
            }

            lblTotalPeriodo.setText(Formats.CURRENCY.formatValue(totalSuma));
            lblFacturasGeneradas.setText(String.valueOf(countTimbradas));
            lblFacturasCanceladas.setText(String.valueOf(countCanceladas));
            lblRegistrosCount.setText("Total: " + listFacturas.size() + " registros");

            TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(modeloTabla);
            tablaFacturas.setRowSorter(sorter);

        } catch (BasicException ex) {
            MessageInf msg = new MessageInf(MessageInf.SGN_WARNING, "Error al cargar las facturas", ex);
            msg.show(this);
        }
    }

    // ==========================================
    // 4. ACCIONES DE FILA (View, PDF, XML)
    // ==========================================

    private void doViewFactura(int row) {
        if (listFacturas == null || row >= listFacturas.size()) return;
        Object[] fac = listFacturas.get(row);
        int ticketid = (Integer) fac[1];
        String uuid = (String) fac[0];
        Date fecha = (Date) fac[2];
        double total = (Double) fac[3];
        String estatus = (String) fac[4];

        try {
            TicketInfo ticket = m_dlSales.loadTicket(0, ticketid);
            
            // Diálogo personalizado para mostrar el ticket detallado
            JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Detalle de Facturación - Ticket #" + ticketid, true);
            dlg.setLayout(new BorderLayout());
            dlg.setBackground(Color.WHITE);

            JPanel pnlInfo = new JPanel(new GridBagLayout());
            pnlInfo.setBackground(Color.WHITE);
            pnlInfo.setBorder(new EmptyBorder(16, 16, 16, 16));
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.insets = new Insets(6, 6, 6, 6);

            Font labelFont = new Font("Segoe UI", Font.BOLD, 13);
            Font valFont = new Font("Segoe UI", Font.PLAIN, 13);

            // Campos CFDI
            gbc.gridx = 0; gbc.gridy = 0;
            JLabel l1 = new JLabel("Folio Fiscal (UUID):"); l1.setFont(labelFont); pnlInfo.add(l1, gbc);
            gbc.gridx = 1;
            JTextField txtUuid = new JTextField(uuid); txtUuid.setEditable(false); txtUuid.setFont(new Font("Consolas", Font.BOLD, 12)); pnlInfo.add(txtUuid, gbc);

            gbc.gridx = 0; gbc.gridy = 1;
            JLabel l2 = new JLabel("Estatus:"); l2.setFont(labelFont); pnlInfo.add(l2, gbc);
            gbc.gridx = 1;
            JLabel valStatus = new JLabel(estatus); valStatus.setFont(labelFont);
            valStatus.setForeground("TIMBRADA".equals(estatus) ? new Color(22, 163, 74) : new Color(220, 38, 38));
            pnlInfo.add(valStatus, gbc);

            gbc.gridx = 0; gbc.gridy = 2;
            JLabel l3 = new JLabel("Fecha / Hora Emisión:"); l3.setFont(labelFont); pnlInfo.add(l3, gbc);
            gbc.gridx = 1;
            JLabel valFecha = new JLabel(new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(fecha)); valFecha.setFont(valFont); pnlInfo.add(valFecha, gbc);

            gbc.gridx = 0; gbc.gridy = 3;
            JLabel l4 = new JLabel("Cliente / RFC:"); l4.setFont(labelFont); pnlInfo.add(l4, gbc);
            gbc.gridx = 1;
            String clienteStr = ticket != null && ticket.getCustomer() != null ? ticket.getCustomer().getName() + " (" + ticket.getCustomer().getTaxid() + ")" : "PUBLICO EN GENERAL";
            JLabel valCliente = new JLabel(clienteStr); valCliente.setFont(valFont); pnlInfo.add(valCliente, gbc);

            gbc.gridx = 0; gbc.gridy = 4;
            JLabel l5 = new JLabel("Total Facturado:"); l5.setFont(labelFont); pnlInfo.add(l5, gbc);
            gbc.gridx = 1;
            JLabel valTotal = new JLabel(Formats.CURRENCY.formatValue(total)); valTotal.setFont(new Font("Segoe UI", Font.BOLD, 15)); valTotal.setForeground(DASH_TEAL_NUM); pnlInfo.add(valTotal, gbc);

            dlg.add(pnlInfo, BorderLayout.NORTH);

            // Detalle de productos
            if (ticket != null) {
                DefaultTableModel modelItems = new DefaultTableModel(new String[]{"Producto", "Cant.", "Precio Unit.", "Total"}, 0);
                for (int i = 0; i < ticket.getLinesCount(); i++) {
                    TicketLineInfo line = ticket.getLine(i);
                    modelItems.addRow(new Object[]{
                        line.getProductName(),
                        line.getMultiply(),
                        Formats.CURRENCY.formatValue(line.getPrice()),
                        Formats.CURRENCY.formatValue(line.getValue())
                    });
                }
                JTable tblItems = new JTable(modelItems);
                tblItems.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                tblItems.setRowHeight(28);
                tblItems.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
                JScrollPane scrollItems = new JScrollPane(tblItems);
                scrollItems.setBorder(BorderFactory.createTitledBorder("Conceptos del Ticket"));
                dlg.add(scrollItems, BorderLayout.CENTER);
            }

            // Botones de acción
            JPanel pnlBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
            pnlBtns.setBackground(new Color(248, 250, 252));
            pnlBtns.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, DASH_CARD_BORDER));

            JButton btnCerrar = new JButton("Cerrar");
            btnCerrar.setFont(labelFont);
            btnCerrar.addActionListener(e -> dlg.dispose());
            pnlBtns.add(btnCerrar);

            if (ticket != null) {
                JButton btnImprimir = new JButton("🖨️ Reimprimir Ticket");
                btnImprimir.setFont(labelFont);
                btnImprimir.setBackground(new Color(30, 41, 59));
                btnImprimir.setForeground(Color.WHITE);
                btnImprimir.setFocusPainted(false);
                btnImprimir.addActionListener(e -> {
                    try {
                        DataLogicSystem dlSystem = (DataLogicSystem) m_App.getBean("com.openbravo.pos.forms.DataLogicSystem");
                        String sresource = dlSystem.getResourceAsXML("Printer.Ticket");
                        if (sresource == null) {
                            sresource = dlSystem.getResourceAsXML("Printer.TicketPreview");
                        }
                        if (sresource == null) {
                            JOptionPane.showMessageDialog(dlg, "No se encontró plantilla de impresión.", "Error", JOptionPane.ERROR_MESSAGE);
                            return;
                        }

                        // Calcular impuestos del ticket
                        TaxesLogic taxeslogic = new TaxesLogic(m_dlSales.getTaxList().list());
                        taxeslogic.calculateTaxes(ticket);

                        TicketParser parser = new TicketParser(m_App.getDeviceTicket(), dlSystem);
                        ScriptEngine script = ScriptFactory.getScriptEngine(ScriptFactory.VELOCITY);
                        script.put("taxes", new com.openbravo.data.gui.ListKeyed(new java.util.ArrayList()));
                        script.put("taxeslogic", taxeslogic);
                        script.put("ticket", ticket);
                        script.put("place", null);

                        // Evitar que fallen variables de puntos no definidas
                        script.put("customerPoints", "");
                        script.put("customerPointsAfter", "");
                        script.put("puntosPorCompra", "");
                        script.put("limiteAlcanzado", false);

                        String evaluated = script.eval(sresource).toString();
                        parser.printTicket(evaluated, ticket);
                        
                        JOptionPane.showMessageDialog(dlg, "Ticket enviado a la impresora del sistema.", "Impresión", JOptionPane.INFORMATION_MESSAGE);
                    } catch (Exception ex) {
                        LOGGER.log(Level.SEVERE, "Error al imprimir ticket", ex);
                        JOptionPane.showMessageDialog(dlg, "Error al imprimir ticket: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                });
                pnlBtns.add(btnImprimir);
            }

            dlg.add(pnlBtns, BorderLayout.SOUTH);
            dlg.setSize(600, 520);
            dlg.setLocationRelativeTo(this);
            dlg.setVisible(true);

        } catch (BasicException ex) {
            JOptionPane.showMessageDialog(this, "No se pudo cargar el ticket asociado.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void doDownloadPdf(int row) {
        if (listFacturas == null || row < 0 || row >= listFacturas.size()) return;
        Object[] invoice = listFacturas.get(row);
        String providerId = invoice.length > 6 ? (String) invoice[6] : null;
        if (!validateOfficialDocument(providerId)) return;

        String uuid = (String) invoice[0];
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Guardar factura PDF");
        chooser.setSelectedFile(new File("Factura-" + safeFileName(uuid) + ".pdf"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;

        File target = ensureExtension(chooser.getSelectedFile(), ".pdf");
        downloadOfficialDocument(providerId, "pdf", bytes -> {
            try {
                Files.write(target.toPath(), bytes);
                JOptionPane.showMessageDialog(this,
                        "PDF guardado correctamente en:\n" + target.getAbsolutePath(),
                        "Factura descargada", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                showDocumentError("No fue posible guardar el PDF", ex);
            }
        });
    }

    private void doDownloadXml(int row) {
        if (listFacturas == null || row < 0 || row >= listFacturas.size()) return;
        Object[] invoice = listFacturas.get(row);
        String providerId = invoice.length > 6 ? (String) invoice[6] : null;
        if (!validateOfficialDocument(providerId)) return;
        downloadOfficialDocument(providerId, "xml",
                bytes -> showOfficialXml((String) invoice[0], bytes));
    }

    private boolean validateOfficialDocument(String providerId) {
        if (providerId == null || providerId.isBlank()) {
            JOptionPane.showMessageDialog(this,
                    "Esta factura fue registrada con una versión anterior y no conserva el ID del proveedor.\n"
                    + "No se generará un documento simulado. Consulta el CFDI en Facturama usando su UUID.",
                    "Documento oficial no disponible", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        if (getSetting("facturama.user", "FACTURAMA_USER").isBlank()
                || getSetting("facturama.password", "FACTURAMA_PASSWORD").isBlank()) {
            JOptionPane.showMessageDialog(this,
                    "Configura el usuario y la contraseña de Facturama en Configuración > Empresa.",
                    "Falta configuración fiscal", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }

    private void downloadOfficialDocument(String providerId, String format,
            java.util.function.Consumer<byte[]> onSuccess) {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        new SwingWorker<byte[], Void>() {
            @Override
            protected byte[] doInBackground() throws Exception {
                String username = getSetting("facturama.user", "FACTURAMA_USER");
                String password = getSetting("facturama.password", "FACTURAMA_PASSWORD");
                String endpoint = getFacturamaBaseUrl() + "/api/Cfdi/" + format + "/issued/"
                        + URLEncoder.encode(providerId, StandardCharsets.UTF_8);
                String authorization = Base64.getEncoder().encodeToString(
                        (username + ":" + password).getBytes(StandardCharsets.UTF_8));
                HttpRequest request = HttpRequest.newBuilder(URI.create(endpoint))
                        .timeout(Duration.ofSeconds(45))
                        .header("Authorization", "Basic " + authorization)
                        .header("Accept", "application/json")
                        .GET().build();
                HttpResponse<String> response = HttpClient.newBuilder()
                        .connectTimeout(Duration.ofSeconds(15)).build()
                        .send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
                if (response.statusCode() < 200 || response.statusCode() >= 300) {
                    throw new IOException("Facturama respondió HTTP " + response.statusCode()
                            + ": " + compactApiError(response.body()));
                }
                JsonObject body = JsonParser.parseString(response.body()).getAsJsonObject();
                if (!body.has("Content") || body.get("Content").isJsonNull()) {
                    throw new IOException("La respuesta del proveedor no contiene el archivo solicitado.");
                }
                return Base64.getDecoder().decode(body.get("Content").getAsString());
            }

            @Override
            protected void done() {
                setCursor(Cursor.getDefaultCursor());
                try {
                    onSuccess.accept(get());
                } catch (Exception ex) {
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                    showDocumentError("No fue posible descargar el " + format.toUpperCase(), cause);
                }
            }
        }.execute();
    }

    private void showOfficialXml(String uuid, byte[] bytes) {
        String xml = new String(bytes, StandardCharsets.UTF_8);
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "CFDI XML - " + uuid, true);
        dialog.setLayout(new BorderLayout());
        JTextArea content = new JTextArea(xml);
        content.setEditable(false);
        content.setFont(new Font("Consolas", Font.PLAIN, 12));
        content.setCaretPosition(0);
        dialog.add(new JScrollPane(content), BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        JButton save = new JButton("Guardar XML", new ModernActionIcon(ModernActionIcon.Type.SAVE, 18));
        save.addActionListener(event -> saveXmlFile(uuid, bytes, dialog));
        JButton copy = new JButton("Copiar", new ModernActionIcon(ModernActionIcon.Type.COPY, 18));
        copy.addActionListener(event -> {
            java.awt.datatransfer.StringSelection selection = new java.awt.datatransfer.StringSelection(xml);
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, selection);
        });
        JButton close = new JButton("Cerrar");
        close.addActionListener(event -> dialog.dispose());
        actions.add(save);
        actions.add(copy);
        actions.add(close);
        dialog.add(actions, BorderLayout.SOUTH);
        dialog.setSize(760, 560);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void saveXmlFile(String uuid, byte[] bytes, Component parent) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Guardar factura XML");
        chooser.setSelectedFile(new File("Factura-" + safeFileName(uuid) + ".xml"));
        if (chooser.showSaveDialog(parent) != JFileChooser.APPROVE_OPTION) return;
        File target = ensureExtension(chooser.getSelectedFile(), ".xml");
        try {
            Files.write(target.toPath(), bytes);
            JOptionPane.showMessageDialog(parent, "XML guardado correctamente.",
                    "Factura descargada", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            showDocumentError("No fue posible guardar el XML", ex);
        }
    }

    private String getSetting(String propertyName, String environmentName) {
        String value = m_App.getProperties().getProperty(propertyName);
        if (value == null || value.isBlank()) value = System.getenv(environmentName);
        return value == null ? "" : value.trim();
    }

    private String getFacturamaBaseUrl() {
        String configured = getSetting("facturama.url", "FACTURAMA_URL");
        if (configured.isBlank()) return "https://apisandbox.facturama.mx";
        URI uri = URI.create(configured);
        if (uri.getScheme() == null || uri.getAuthority() == null) {
            throw new IllegalArgumentException("La URL configurada para Facturama no es válida.");
        }
        return uri.getScheme() + "://" + uri.getAuthority();
    }

    private static File ensureExtension(File file, String extension) {
        return file.getName().toLowerCase().endsWith(extension) ? file
                : new File(file.getParentFile(), file.getName() + extension);
    }

    private static String safeFileName(String value) {
        return value == null ? "sin-uuid" : value.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    private static String compactApiError(String response) {
        if (response == null) return "sin detalle";
        String compact = response.replaceAll("\\s+", " ").trim();
        return compact.length() > 300 ? compact.substring(0, 300) + "…" : compact;
    }

    private void showDocumentError(String title, Throwable error) {
        LOGGER.log(Level.WARNING, title, error);
        JOptionPane.showMessageDialog(this, title + ":\n" + error.getMessage(),
                "Error de facturación", JOptionPane.ERROR_MESSAGE);
    }

    /** Vista anterior conservada solo como referencia interna; no genera documentos fiscales. */
    private void doLegacyPdfPreview(int row) {
        if (listFacturas == null || row >= listFacturas.size()) return;
        Object[] fac = listFacturas.get(row);
        int ticketid = (Integer) fac[1];
        String dbUuid = (String) fac[0];
        String uuid = getOrGenerateUuid(dbUuid);
        Date fecha = (Date) fac[2];
        double total = (Double) fac[3];

        try {
            TicketInfo ticket = m_dlSales.loadTicket(0, ticketid);
            String rfcReceptor = "XAXX010101000";
            String nombreReceptor = "PUBLICO EN GENERAL";
            String cpReceptor = "26015";
            String usoCfdi = "S01 - Sin efectos fiscales";
            String regimenReceptor = "616 - Sin obligaciones fiscales";

            if (ticket != null && ticket.getCustomer() != null) {
                String taxId = ticket.getCustomer().getTaxid();
                if (taxId != null && !taxId.isEmpty()) rfcReceptor = taxId;
                String name = ticket.getCustomer().getName();
                if (name != null && !name.isEmpty()) nombreReceptor = name;
                String postal = ticket.getCustomer().getPostal();
                if (postal != null && !postal.isEmpty()) cpReceptor = postal;
            }

            JDialog pdfDlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Representación Impresa Digital - CFDI " + uuid, true);
            pdfDlg.setLayout(new BorderLayout());
            pdfDlg.getContentPane().setBackground(DASH_BG);

            // --- TOOLBAR SUPERIOR ---
            JPanel pnlToolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
            pnlToolbar.setBackground(new Color(30, 41, 59)); // slate-800
            
            JButton btnImprimir = new JButton("🖨️ Imprimir Factura");
            btnImprimir.setFont(new Font("Segoe UI", Font.BOLD, 12));
            btnImprimir.setBackground(new Color(15, 118, 110)); // teal-700
            btnImprimir.setForeground(Color.WHITE);
            btnImprimir.setFocusPainted(false);
            btnImprimir.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btnImprimir.addActionListener(e -> {
                JOptionPane.showMessageDialog(pdfDlg, "Imprimiendo copia de la factura...", "Impresión", JOptionPane.INFORMATION_MESSAGE);
            });

            JButton btnGuardar = new JButton("💾 Guardar PDF");
            btnGuardar.setFont(new Font("Segoe UI", Font.BOLD, 12));
            btnGuardar.setBackground(new Color(15, 118, 110));
            btnGuardar.setForeground(Color.WHITE);
            btnGuardar.setFocusPainted(false);
            btnGuardar.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btnGuardar.addActionListener(e -> {
                JOptionPane.showMessageDialog(pdfDlg, "Factura PDF guardada exitosamente en la carpeta de descargas.", "Guardar", JOptionPane.INFORMATION_MESSAGE);
            });

            pnlToolbar.add(btnImprimir);
            pnlToolbar.add(btnGuardar);
            pdfDlg.add(pnlToolbar, BorderLayout.NORTH);

            // --- HOJA DE REPRESENTACIÓN IMPRESA (CFDI) ---
            JPanel hoja = new JPanel();
            hoja.setLayout(new BoxLayout(hoja, BoxLayout.Y_AXIS));
            hoja.setBackground(Color.WHITE);
            hoja.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(20, 20, 20, 20),
                BorderFactory.createLineBorder(new Color(203, 213, 225), 1)
            ));

            // 1. Emisor y Datos del Comprobante (Dos columnas)
            JPanel pnlHeader = new JPanel(new GridLayout(1, 2, 20, 0));
            pnlHeader.setOpaque(false);

            // Emisor info
            JPanel pnlEmisor = new JPanel();
            pnlEmisor.setLayout(new BoxLayout(pnlEmisor, BoxLayout.Y_AXIS));
            pnlEmisor.setOpaque(false);
            
            JLabel lblEmpresa = new JLabel("VOLTIUM SANREY SA DE CV");
            lblEmpresa.setFont(new Font("Segoe UI", Font.BOLD, 16));
            lblEmpresa.setForeground(DASH_TEXT_PRI);
            pnlEmisor.add(lblEmpresa);
            pnlEmisor.add(Box.createVerticalStrut(4));
            
            pnlEmisor.add(new JLabel("RFC: VOSA900909AA1"));
            pnlEmisor.add(new JLabel("Régimen Fiscal: 601 - General de Ley Personas Morales"));
            pnlEmisor.add(new JLabel("Domicilio: Av. Reforma 1234, Col. Centro, CP 26015"));
            pnlEmisor.add(new JLabel("Piedras Negras, Coahuila, México"));

            // Datos del Comprobante
            JPanel pnlFacturaData = new JPanel();
            pnlFacturaData.setLayout(new BoxLayout(pnlFacturaData, BoxLayout.Y_AXIS));
            pnlFacturaData.setOpaque(false);

            JLabel lblFacturaTitle = new JLabel("FACTURA ELECTRÓNICA (CFDI 4.0)");
            lblFacturaTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
            lblFacturaTitle.setForeground(new Color(15, 118, 110));
            pnlFacturaData.add(lblFacturaTitle);
            pnlFacturaData.add(Box.createVerticalStrut(4));
            
            pnlFacturaData.add(new JLabel("Folio Fiscal (UUID): " + uuid));
            pnlFacturaData.add(new JLabel("No. Certificado: 00001000000504465028"));
            pnlFacturaData.add(new JLabel("Fecha y Hora de Certificación: " + new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(fecha)));
            pnlFacturaData.add(new JLabel("Tipo de Comprobante: I - Ingreso"));

            pnlHeader.add(pnlEmisor);
            pnlHeader.add(pnlFacturaData);
            hoja.add(pnlHeader);
            hoja.add(Box.createVerticalStrut(15));

            // Línea divisoria
            hoja.add(new JSeparator(JSeparator.HORIZONTAL));
            hoja.add(Box.createVerticalStrut(10));

            // 2. Datos del Receptor
            JPanel pnlReceptor = new JPanel(new GridLayout(1, 2, 20, 0));
            pnlReceptor.setOpaque(false);

            JPanel pnlRecLeft = new JPanel();
            pnlRecLeft.setLayout(new BoxLayout(pnlRecLeft, BoxLayout.Y_AXIS));
            pnlRecLeft.setOpaque(false);
            pnlRecLeft.add(new JLabel("RECEPTOR:"));
            JLabel lblRecNombre = new JLabel(nombreReceptor);
            lblRecNombre.setFont(new Font("Segoe UI", Font.BOLD, 13));
            pnlRecLeft.add(lblRecNombre);
            pnlRecLeft.add(new JLabel("RFC: " + rfcReceptor));

            JPanel pnlRecRight = new JPanel();
            pnlRecRight.setLayout(new BoxLayout(pnlRecRight, BoxLayout.Y_AXIS));
            pnlRecRight.setOpaque(false);
            pnlRecRight.add(new JLabel("Domicilio Fiscal Receptor: " + cpReceptor));
            pnlRecRight.add(new JLabel("Régimen Fiscal Receptor: " + regimenReceptor));
            pnlRecRight.add(new JLabel("Uso CFDI: " + usoCfdi));

            pnlReceptor.add(pnlRecLeft);
            pnlReceptor.add(pnlRecRight);
            hoja.add(pnlReceptor);
            hoja.add(Box.createVerticalStrut(15));

            // 3. Tabla de Conceptos
            DefaultTableModel modelItems = new DefaultTableModel(new String[]{"Clave ProdServ", "Cant.", "Unidad", "Descripción", "Valor Unitario", "Importe"}, 0);
            if (ticket != null) {
                for (int i = 0; i < ticket.getLinesCount(); i++) {
                    TicketLineInfo line = ticket.getLine(i);
                    modelItems.addRow(new Object[]{
                        "01010101",
                        line.getMultiply(),
                        "H87 - Pz",
                        line.getProductName(),
                        Formats.CURRENCY.formatValue(line.getPrice()),
                        Formats.CURRENCY.formatValue(line.getValue())
                    });
                }
            } else {
                modelItems.addRow(new Object[]{
                    "01010101",
                    "1.0",
                    "H87 - Pz",
                    "Venta general ticket #" + ticketid,
                    Formats.CURRENCY.formatValue(total / 1.16),
                    Formats.CURRENCY.formatValue(total)
                });
            }

            JTable tblItems = new JTable(modelItems);
            tblItems.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            tblItems.setRowHeight(25);
            tblItems.setShowHorizontalLines(true);
            tblItems.setShowVerticalLines(false);
            tblItems.setGridColor(new Color(241, 245, 249));
            tblItems.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
            tblItems.getTableHeader().setBackground(new Color(248, 250, 252));
            tblItems.getTableHeader().setPreferredSize(new Dimension(0, 30));

            JScrollPane scrollItems = new JScrollPane(tblItems);
            scrollItems.setPreferredSize(new Dimension(0, 150));
            scrollItems.setBorder(BorderFactory.createLineBorder(DASH_CARD_BORDER, 1));
            hoja.add(scrollItems);
            hoja.add(Box.createVerticalStrut(15));

            // 4. Totales y SAT QR Code (Dos columnas)
            JPanel pnlFooter = new JPanel(new GridBagLayout());
            pnlFooter.setOpaque(false);
            GridBagConstraints c = new GridBagConstraints();
            c.fill = GridBagConstraints.BOTH;
            c.insets = new Insets(4, 4, 4, 4);

            // Lado izquierdo: QR Code
            c.gridx = 0; c.gridy = 0;
            c.weightx = 0.35; c.weighty = 1.0;
            QrCodePanel pnlQr = new QrCodePanel();
            pnlQr.setPreferredSize(new Dimension(120, 120));
            pnlFooter.add(pnlQr, c);

            // Lado derecho: Desglose e Impuestos y Totales
            c.gridx = 1;
            c.weightx = 0.65;
            JPanel pnlTotales = new JPanel(new GridLayout(4, 2, 10, 4));
            pnlTotales.setOpaque(false);
            
            pnlTotales.add(new JLabel("Subtotal:", SwingConstants.RIGHT));
            pnlTotales.add(new JLabel(Formats.CURRENCY.formatValue(total / 1.16), SwingConstants.RIGHT));

            pnlTotales.add(new JLabel("Descuento:", SwingConstants.RIGHT));
            pnlTotales.add(new JLabel("$0.00", SwingConstants.RIGHT));

            pnlTotales.add(new JLabel("IVA Trasladado (16.00%):", SwingConstants.RIGHT));
            pnlTotales.add(new JLabel(Formats.CURRENCY.formatValue(total - (total / 1.16)), SwingConstants.RIGHT));

            JLabel lblTotalLabel = new JLabel("TOTAL:", SwingConstants.RIGHT);
            lblTotalLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
            pnlTotales.add(lblTotalLabel);
            JLabel lblTotalVal = new JLabel(Formats.CURRENCY.formatValue(total), SwingConstants.RIGHT);
            lblTotalVal.setFont(new Font("Segoe UI", Font.BOLD, 14));
            lblTotalVal.setForeground(DASH_TEAL_NUM);
            pnlTotales.add(lblTotalVal);

            pnlFooter.add(pnlTotales, c);
            hoja.add(pnlFooter);
            hoja.add(Box.createVerticalStrut(15));

            hoja.add(new JSeparator(JSeparator.HORIZONTAL));
            hoja.add(Box.createVerticalStrut(10));

            // 5. Cadena Original y Sello Digital
            JPanel pnlSello = new JPanel();
            pnlSello.setLayout(new BoxLayout(pnlSello, BoxLayout.Y_AXIS));
            pnlSello.setOpaque(false);

            JLabel lblSelloSat = new JLabel("Sello Digital del SAT:");
            lblSelloSat.setFont(new Font("Segoe UI", Font.BOLD, 10));
            pnlSello.add(lblSelloSat);
            JTextArea taSello = new JTextArea("MIIEPgIBAAKCAQEA0f9+Y1J6d26B6z0S/7lI9...ficticio...x2nL");
            taSello.setFont(new Font("Consolas", Font.PLAIN, 9));
            taSello.setLineWrap(true);
            taSello.setEditable(false);
            pnlSello.add(taSello);
            
            pnlSello.add(Box.createVerticalStrut(6));

            JLabel lblCadena = new JLabel("Cadena Original del Complemento de Certificación Digital del SAT:");
            lblCadena.setFont(new Font("Segoe UI", Font.BOLD, 10));
            pnlSello.add(lblCadena);
            JTextArea taCadena = new JTextArea("||1.1|" + uuid + "|" + new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(fecha) + "|VOSA900909AA1|...");
            taCadena.setFont(new Font("Consolas", Font.PLAIN, 9));
            taCadena.setLineWrap(true);
            taCadena.setEditable(false);
            pnlSello.add(taCadena);

            hoja.add(pnlSello);

            JScrollPane scrollHoja = new JScrollPane(hoja);
            scrollHoja.getVerticalScrollBar().setUnitIncrement(16);
            pdfDlg.add(scrollHoja, BorderLayout.CENTER);

            pdfDlg.setSize(750, 700);
            pdfDlg.setLocationRelativeTo(this);
            pdfDlg.setVisible(true);

        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Error al cargar la visualización de la factura", ex);
            JOptionPane.showMessageDialog(this, "No se pudo cargar la representación de la factura.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void doLegacyXmlPreview(int row) {
        if (listFacturas == null || row >= listFacturas.size()) return;
        Object[] fac = listFacturas.get(row);
        String uuid = (String) fac[0];
        double total = (Double) fac[3];
        Date fecha = (Date) fac[2];
        int ticketid = (Integer) fac[1];

        // Crear una representación simplificada de un CFDI 4.0 XML para que el usuario la vea/copie
        String xmlContent = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
            "<cfdi:Comprobante xmlns:cfdi=\"http://www.sat.gob.mx/cfd/4\" \n" +
            "    Version=\"4.0\" \n" +
            "    UUID=\"" + uuid + "\"\n" +
            "    Fecha=\"" + new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(fecha) + "\"\n" +
            "    SubTotal=\"" + String.format("%.2f", total / 1.16) + "\"\n" +
            "    Total=\"" + String.format("%.2f", total) + "\"\n" +
            "    Moneda=\"MXN\" \n" +
            "    TipoDeComprobante=\"I\">\n" +
            "  <cfdi:Emisor Rfc=\"VOSA900909AA1\" Nombre=\"VOLTIUM SANREY SA DE CV\" RegimenFiscal=\"601\"/>\n" +
            "  <cfdi:Receptor Rfc=\"XAXX010101000\" Nombre=\"PUBLICO EN GENERAL\" UsoCFDI=\"S01\" RegimenFiscalReceptor=\"616\" DomicilioFiscalReceptor=\"26015\"/>\n" +
            "  <cfdi:Conceptos>\n" +
            "    <cfdi:Concepto ClaveProdServ=\"01010101\" Cantidad=\"1\" ClaveUnidad=\"H87\" Unidad=\"Pieza\" Descripcion=\"Venta de ticket #" + ticketid + "\" ValorUnitario=\"" + String.format("%.2f", total / 1.16) + "\" Importe=\"" + String.format("%.2f", total / 1.16) + "\" ObjetoImp=\"02\">\n" +
            "      <cfdi:Impuestos>\n" +
            "        <cfdi:Traslados>\n" +
            "          <cfdi:Traslado Base=\"" + String.format("%.2f", total / 1.16) + "\" Impuesto=\"002\" TipoFactor=\"Tasa\" TasaOCuota=\"0.160000\" Importe=\"" + String.format("%.2f", total - (total / 1.16)) + "\"/>\n" +
            "        </cfdi:Traslados>\n" +
            "      </cfdi:Impuestos>\n" +
            "    </cfdi:Concepto>\n" +
            "  </cfdi:Conceptos>\n" +
            "</cfdi:Comprobante>";

        JDialog xmlDlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "CFDI XML - " + uuid, true);
        xmlDlg.setLayout(new BorderLayout());

        JTextArea ta = new JTextArea(xmlContent);
        ta.setFont(new Font("Consolas", Font.PLAIN, 12));
        ta.setEditable(false);
        JScrollPane scroll = new JScrollPane(ta);
        scroll.setBorder(new EmptyBorder(10, 10, 10, 10));
        xmlDlg.add(scroll, BorderLayout.CENTER);

        JPanel pnlBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        pnlBtns.setBackground(new Color(248, 250, 252));
        
        JButton btnCopy = new JButton("Copiar XML");
        btnCopy.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnCopy.addActionListener(e -> {
            java.awt.datatransfer.StringSelection selection = new java.awt.datatransfer.StringSelection(xmlContent);
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, selection);
            JOptionPane.showMessageDialog(xmlDlg, "XML copiado al portapapeles.", "Copiar", JOptionPane.INFORMATION_MESSAGE);
        });
        pnlBtns.add(btnCopy);

        JButton btnClose = new JButton("Cerrar");
        btnClose.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnClose.addActionListener(e -> xmlDlg.dispose());
        pnlBtns.add(btnClose);

        xmlDlg.add(pnlBtns, BorderLayout.SOUTH);
        xmlDlg.setSize(650, 480);
        xmlDlg.setLocationRelativeTo(this);
        xmlDlg.setVisible(true);
    }

    // ==========================================
    // 5. COMPONENTES GRÁFICOS PERSONALIZADOS
    // ==========================================

    /**
     * Botón con estilo píldora/cápsula redondeada.
     */
    private static class PillButton extends JButton {
        private static final long serialVersionUID = 1L;
        private boolean selected = false;
        private final Color normalBg = new Color(241, 245, 249);
        private final Color selectedBg = new Color(15, 118, 110); // teal-700
        private final Color normalFg = new Color(71, 85, 105);
        private final Color selectedFg = Color.WHITE;

        public PillButton(String text) {
            super(text);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setFont(new Font("Segoe UI", Font.BOLD, 12));
            setCursor(new Cursor(Cursor.HAND_CURSOR));
        }

        public void setSelected(boolean sel) {
            this.selected = sel;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (selected) {
                g2.setColor(selectedBg);
                setForeground(selectedFg);
            } else {
                g2.setColor(normalBg);
                setForeground(normalFg);
            }
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
            super.paintComponent(g2);
            g2.dispose();
        }
    }

    /**
     * Botón de búsqueda de color teal y con icono circular.
     */
    private static class SearchButton extends JButton {
        private static final long serialVersionUID = 1L;
        public SearchButton(String text) {
            super(text);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setFont(new Font("Segoe UI", Font.BOLD, 13));
            setForeground(Color.WHITE);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(15, 118, 110)); // teal-700
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());

            // Lupa blanca
            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(2.0f));
            int cx = 18, cy = getHeight() / 2 - 1;
            g2.drawOval(cx - 5, cy - 5, 9, 9);
            g2.drawLine(cx + 2, cy + 2, cx + 6, cy + 6);

            super.paintComponent(g2);
            g2.dispose();
        }
    }

    /**
     * Renderizador de Estatus con badges cápsula coloreados (TIMBRADA en verde, CANCELADA en rojo).
     */
    private static class StatusBadgeRenderer extends DefaultTableCellRenderer {
        private static final long serialVersionUID = 1L;
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JPanel cell = new JPanel(new GridBagLayout()) {
                private static final long serialVersionUID = 1L;
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    if (value == null) return;
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    
                    String val = value.toString();
                    if ("TIMBRADA".equalsIgnoreCase(val)) {
                        g2.setColor(new Color(22, 163, 74)); // Green-600
                    } else {
                        g2.setColor(new Color(220, 38, 38)); // Red-600
                    }
                    int w = 90, h = 24;
                    int x = (getWidth() - w) / 2;
                    int y = (getHeight() - h) / 2;
                    g2.fillRoundRect(x, y, w, h, h, h);
                    g2.dispose();
                }
            };
            cell.setOpaque(true);
            if (isSelected) {
                cell.setBackground(table.getSelectionBackground());
            } else {
                cell.setBackground(row % 2 == 0 ? Color.WHITE : new Color(249, 250, 251));
            }

            JLabel label = new JLabel(value != null ? value.toString() : "");
            label.setFont(new Font("Segoe UI", Font.BOLD, 11));
            label.setForeground(Color.WHITE);
            cell.add(label);
            return cell;
        }
    }

    /**
     * Renderizador de botones/iconos de Acciones (👁️, 📄, XML).
     */
    private static class ActionsRenderer extends DefaultTableCellRenderer {
        private static final long serialVersionUID = 1L;
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JPanel cell = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 8));
            cell.setOpaque(true);
            if (isSelected) {
                cell.setBackground(table.getSelectionBackground());
            } else {
                cell.setBackground(row % 2 == 0 ? Color.WHITE : new Color(249, 250, 251));
            }

            JLabel lblView = new JLabel(new ModernActionIcon(ModernActionIcon.Type.VIEW, 20));
            lblView.setToolTipText("Ver detalle");
            JLabel lblPdf = new JLabel(new ModernActionIcon(ModernActionIcon.Type.DOCUMENT, 20));
            lblPdf.setToolTipText("Descargar PDF oficial");
            JLabel compExcel = new JLabel("XML", new ModernActionIcon(ModernActionIcon.Type.DOCUMENT, 18), SwingConstants.LEFT);
            compExcel.setFont(new Font("Segoe UI", Font.BOLD, 9));
            compExcel.setToolTipText("Ver y guardar XML oficial");

            cell.add(lblView);
            cell.add(lblPdf);
            cell.add(compExcel);
            return cell;
        }
    }

    private String getOrGenerateUuid(String dbUuid) {
        if (dbUuid == null || dbUuid.trim().isEmpty() || "N/A".equalsIgnoreCase(dbUuid)) {
            return java.util.UUID.randomUUID().toString();
        }
        return dbUuid;
    }

    // Panel auxiliar para dibujar el código QR del SAT
    private static class QrCodePanel extends JPanel {
        private static final long serialVersionUID = 1L;
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Color.BLACK);
            int size = Math.min(getWidth(), getHeight()) - 10;
            int blocks = 21;
            int blockSize = size / blocks;
            int offset = 5;
            java.util.Random rand = new java.util.Random(12345);
            for (int r = 0; r < blocks; r++) {
                for (int c = 0; c < blocks; c++) {
                    if ((r < 7 && c < 7) || (r < 7 && c >= blocks - 7) || (r >= blocks - 7 && c < 7)) {
                        if (r == 0 || r == 6 || c == 0 || c == 6 || (r >= 2 && r <= 4 && c >= 2 && c <= 4)) {
                            g2.fillRect(offset + c * blockSize, offset + r * blockSize, blockSize, blockSize);
                        }
                    } else {
                        if (rand.nextBoolean()) {
                            g2.fillRect(offset + c * blockSize, offset + r * blockSize, blockSize, blockSize);
                        }
                    }
                }
            }
            g2.dispose();
        }
    }
}

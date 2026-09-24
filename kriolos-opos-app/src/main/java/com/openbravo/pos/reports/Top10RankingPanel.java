//    KriolOS POS - Top 10 Ranking Panel
//    Custom panel that displays a ranked list of top-selling products
//    with horizontal progress bars, inspired by infographic-style rankings.

package com.openbravo.pos.reports;

import com.openbravo.data.loader.Session;
import java.awt.*;
import java.util.List;
import java.util.Map;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 * Panel personalizado que muestra un ranking estilo infografía para
 * los 10 productos más vendidos. Diseño inspirado en rankings de barras
 * horizontales con posiciones numeradas, nombres y valores.
 * 
 * Este panel tiene su propio estilo dedicado, independiente del dashboard genérico.
 */
public class Top10RankingPanel extends JPanel {

    // === Paleta de colores dedicada para Top 10 (tema claro) ===
    private static final Color BG_DARK        = new Color(241, 245, 249);    // Fondo principal gris claro
    private static final Color BG_CARD        = new Color(255, 255, 255);    // Fondo de tarjeta blanco
    private static final Color BG_CARD_HOVER  = new Color(248, 250, 252);    // Hover de fila
    private static final Color BORDER_SUBTLE  = new Color(226, 232, 240);    // Borde sutil
    private static final Color TEXT_PRIMARY    = new Color(15, 23, 42);      // Texto principal oscuro
    private static final Color TEXT_SECONDARY  = new Color(71, 85, 105);     // Texto secundario
    private static final Color TEXT_MUTED      = new Color(148, 163, 184);   // Texto apagado
    
    // Colores para las barras de progreso (gradiente cálido)
    private static final Color BAR_START       = new Color(235, 172, 60);    // Dorado/ámbar
    private static final Color BAR_END         = new Color(245, 130, 50);    // Naranja cálido
    private static final Color BAR_BG          = new Color(226, 232, 240);   // Fondo de barra gris claro
    
    // Colores para las medallas de posición
    private static final Color MEDAL_GOLD      = new Color(217, 150, 20);    // Dorado más visible en fondo claro
    private static final Color MEDAL_SILVER    = new Color(120, 130, 145);   // Plateado
    private static final Color MEDAL_BRONZE    = new Color(180, 120, 50);    // Bronce
    private static final Color MEDAL_DEFAULT   = new Color(148, 163, 184);   // Gris
    
    // Colores KPI dedicados
    private static final Color KPI_ACCENT_1    = new Color(235, 172, 60);    // Ámbar
    private static final Color KPI_ACCENT_2    = new Color(16, 185, 129);    // Verde esmeralda
    private static final Color KPI_ACCENT_3    = new Color(59, 130, 246);    // Azul
    private static final Color KPI_ACCENT_4    = new Color(139, 92, 246);    // Púrpura

    private JPanel rankingListPanel;
    private JLabel lblTotalProducts;
    private JLabel lblTotalUnits;
    private JLabel lblTotalRevenue;
    private JLabel lblAvgUnits;
    private Session dbSession;  // Conexión a BD para el diálogo de detalle

    public Top10RankingPanel() {
        setLayout(new BorderLayout());
        setBackground(BG_DARK);
        setBorder(new EmptyBorder(20, 24, 20, 24));
        buildUI();
    }

    /** Establece la sesión de BD para habilitar la vista de detalle al hacer clic */
    public void setSession(Session session) {
        this.dbSession = session;
    }

    private void buildUI() {
        // === HEADER: Título principal ===
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(new EmptyBorder(0, 0, 16, 0));

        JLabel titleLabel = new JLabel("TOP 10 PRODUCTOS MÁS VENDIDOS");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(TEXT_PRIMARY);
        headerPanel.add(titleLabel, BorderLayout.WEST);

        JLabel subtitleLabel = new JLabel("Haz clic en un producto para ver su detalle completo");
        subtitleLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        subtitleLabel.setForeground(TEXT_MUTED);
        subtitleLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        headerPanel.add(subtitleLabel, BorderLayout.EAST);

        // === KPI ROW: 4 tarjetas de métricas ===
        JPanel kpiRow = new JPanel(new GridLayout(1, 4, 12, 0));
        kpiRow.setOpaque(false);
        kpiRow.setBorder(new EmptyBorder(0, 0, 20, 0));

        lblTotalProducts = new JLabel("0");
        lblTotalUnits = new JLabel("0");
        lblTotalRevenue = new JLabel("$0.00");
        lblAvgUnits = new JLabel("0");

        kpiRow.add(createKpiCard("PRODUCTOS", lblTotalProducts, KPI_ACCENT_1, "🏆"));
        kpiRow.add(createKpiCard("UNIDADES TOTALES", lblTotalUnits, KPI_ACCENT_2, "📦"));
        kpiRow.add(createKpiCard("INGRESOS TOTALES", lblTotalRevenue, KPI_ACCENT_3, "💰"));
        kpiRow.add(createKpiCard("PROMEDIO UNID.", lblAvgUnits, KPI_ACCENT_4, "📊"));

        // === TOP SECTION: Header + KPIs ===
        JPanel topSection = new JPanel();
        topSection.setLayout(new BoxLayout(topSection, BoxLayout.Y_AXIS));
        topSection.setOpaque(false);
        topSection.add(headerPanel);
        topSection.add(kpiRow);

        add(topSection, BorderLayout.NORTH);

        // === RANKING LIST ===
        rankingListPanel = new JPanel();
        rankingListPanel.setLayout(new BoxLayout(rankingListPanel, BoxLayout.Y_AXIS));
        rankingListPanel.setOpaque(false);

        // Columna de encabezados
        JPanel headerRow = createHeaderRow();
        rankingListPanel.add(headerRow);
        rankingListPanel.add(Box.createVerticalStrut(8));

        // Mensaje vacío inicial
        JLabel emptyLabel = new JLabel("Ejecute el reporte para ver el ranking de productos más vendidos");
        emptyLabel.setFont(new Font("Segoe UI", Font.ITALIC, 14));
        emptyLabel.setForeground(TEXT_MUTED);
        emptyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        emptyLabel.setBorder(new EmptyBorder(40, 0, 40, 0));
        rankingListPanel.add(emptyLabel);

        JScrollPane scrollPane = new JScrollPane(rankingListPanel);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.getViewport().setBackground(BG_DARK);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        
        add(scrollPane, BorderLayout.CENTER);
    }

    private JPanel createHeaderRow() {
        JPanel row = new JPanel(new BorderLayout(12, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BORDER_SUBTLE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
            }
        };
        row.setOpaque(false);
        row.setBorder(new EmptyBorder(10, 16, 10, 16));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        JLabel posHeader = new JLabel("#");
        posHeader.setFont(new Font("Segoe UI", Font.BOLD, 11));
        posHeader.setForeground(TEXT_MUTED);
        posHeader.setPreferredSize(new Dimension(36, 20));
        row.add(posHeader, BorderLayout.WEST);

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setOpaque(false);
        
        JLabel nameHeader = new JLabel("PRODUCTO");
        nameHeader.setFont(new Font("Segoe UI", Font.BOLD, 11));
        nameHeader.setForeground(TEXT_MUTED);
        centerPanel.add(nameHeader, BorderLayout.WEST);
        
        row.add(centerPanel, BorderLayout.CENTER);

        JLabel valueHeader = new JLabel("UNIDADES");
        valueHeader.setFont(new Font("Segoe UI", Font.BOLD, 11));
        valueHeader.setForeground(TEXT_MUTED);
        valueHeader.setHorizontalAlignment(SwingConstants.RIGHT);
        valueHeader.setPreferredSize(new Dimension(80, 20));
        row.add(valueHeader, BorderLayout.EAST);

        return row;
    }

    /**
     * Actualiza el panel de ranking con datos frescos.
     * @param records Lista de registros con datos de productos vendidos
     * @param fieldNames Lista de nombres de campos
     */
    public void updateRanking(List<Map<String, Object>> records, List<String> fieldNames) {
        rankingListPanel.removeAll();
        
        // Agregar encabezado de columnas
        rankingListPanel.add(createHeaderRow());
        rankingListPanel.add(Box.createVerticalStrut(6));

        if (records == null || records.isEmpty()) {
            JLabel emptyLabel = new JLabel("No se encontraron ventas para el período seleccionado");
            emptyLabel.setFont(new Font("Segoe UI", Font.ITALIC, 14));
            emptyLabel.setForeground(TEXT_MUTED);
            emptyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            emptyLabel.setBorder(new EmptyBorder(40, 0, 40, 0));
            rankingListPanel.add(emptyLabel);
            
            lblTotalProducts.setText("0");
            lblTotalUnits.setText("0");
            lblTotalRevenue.setText("$0.00");
            lblAvgUnits.setText("0");
            
            rankingListPanel.revalidate();
            rankingListPanel.repaint();
            return;
        }

        // Detectar claves de campos
        String nameKey = null, unitsKey = null, totalKey = null;
        for (String key : fieldNames) {
            String uk = key.toUpperCase();
            if (nameKey == null && (uk.equals("NAME") || uk.equals("PNAME"))) nameKey = key;
            if (unitsKey == null && (uk.equals("UNITS") || uk.equals("QTY") || uk.equals("QUANTITY"))) unitsKey = key;
            if (totalKey == null && (uk.equals("GROSSTOTAL") || uk.equals("TOTAL") || uk.equals("SUBTOTAL"))) totalKey = key;
        }

        // Fallback: buscar por tipo
        if (nameKey == null) {
            for (String key : fieldNames) {
                if (records.get(0).get(key) instanceof String) { nameKey = key; break; }
            }
        }
        if (unitsKey == null) {
            for (String key : fieldNames) {
                if (records.get(0).get(key) instanceof Number && !key.equalsIgnoreCase(totalKey)) { 
                    unitsKey = key; break; 
                }
            }
        }

        // Calcular el valor máximo para escalar las barras
        double maxUnits = 0;
        double totalUnitsVal = 0;
        double totalRevenueVal = 0;
        
        for (Map<String, Object> record : records) {
            double units = 0;
            if (unitsKey != null && record.get(unitsKey) instanceof Number) {
                units = ((Number) record.get(unitsKey)).doubleValue();
            }
            if (units > maxUnits) maxUnits = units;
            totalUnitsVal += units;
            
            if (totalKey != null && record.get(totalKey) instanceof Number) {
                totalRevenueVal += ((Number) record.get(totalKey)).doubleValue();
            }
        }

        // Actualizar KPIs
        int productCount = Math.min(records.size(), 10);
        lblTotalProducts.setText(String.valueOf(productCount));
        lblTotalUnits.setText(String.format("%.0f", totalUnitsVal));
        lblTotalRevenue.setText(String.format("$%,.2f", totalRevenueVal));
        double avgUnits = productCount > 0 ? totalUnitsVal / productCount : 0;
        lblAvgUnits.setText(String.format("%.1f", avgUnits));

        // Construir las filas del ranking
        int position = 1;
        for (Map<String, Object> record : records) {
            if (position > 10) break;

            String name = nameKey != null && record.get(nameKey) != null 
                ? record.get(nameKey).toString() : "Producto " + position;
            double units = 0;
            if (unitsKey != null && record.get(unitsKey) instanceof Number) {
                units = ((Number) record.get(unitsKey)).doubleValue();
            }
            double revenue = 0;
            if (totalKey != null && record.get(totalKey) instanceof Number) {
                revenue = ((Number) record.get(totalKey)).doubleValue();
            }

            double barPercent = maxUnits > 0 ? (units / maxUnits) : 0;
            JPanel rowPanel = createRankingRow(position, name, units, revenue, barPercent);
            rankingListPanel.add(rowPanel);
            rankingListPanel.add(Box.createVerticalStrut(4));
            position++;
        }

        rankingListPanel.revalidate();
        rankingListPanel.repaint();
    }

    private JPanel createRankingRow(int position, String productName, double units, double revenue, double barPercent) {
        final Color rowBg = (position % 2 == 0) ? BG_CARD : new Color(248, 250, 252);
        
        JPanel row = new JPanel(new BorderLayout(12, 0)) {
            Color currentBg = rowBg;
            
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(currentBg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                // Línea de acento izquierda para top 3
                if (position <= 3) {
                    g2.setColor(getMedalColor(position));
                    g2.fillRoundRect(0, 0, 4, getHeight(), 4, 4);
                }
                g2.dispose();
            }
        };
        row.setOpaque(false);
        row.setBorder(new EmptyBorder(12, 16, 12, 16));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        row.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Hover effect + Click to open product detail
        row.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                row.setBackground(BG_CARD_HOVER);
                row.repaint();
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                row.setBackground(rowBg);
                row.repaint();
            }
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (dbSession != null) {
                    Frame parentFrame = (Frame) SwingUtilities.getWindowAncestor(Top10RankingPanel.this);
                    ProductDetailDialog dialog = new ProductDetailDialog(parentFrame, dbSession, productName);
                    dialog.setVisible(true);
                }
            }
        });

        // === LEFT: Posición con medalla ===
        JPanel posPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color medalColor = getMedalColor(position);
                g2.setColor(new Color(medalColor.getRed(), medalColor.getGreen(), medalColor.getBlue(), 30));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
            }
        };
        posPanel.setOpaque(false);
        posPanel.setPreferredSize(new Dimension(36, 36));
        
        JLabel posLabel = new JLabel(String.valueOf(position), SwingConstants.CENTER);
        posLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        posLabel.setForeground(getMedalColor(position));
        posPanel.add(posLabel, BorderLayout.CENTER);
        row.add(posPanel, BorderLayout.WEST);

        // === CENTER: Nombre del producto + barra de progreso ===
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setOpaque(false);
        centerPanel.setBorder(new EmptyBorder(0, 8, 0, 8));

        JLabel nameLabel = new JLabel(productName);
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        nameLabel.setForeground(TEXT_PRIMARY);
        nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        centerPanel.add(nameLabel);
        centerPanel.add(Box.createVerticalStrut(4));

        // Barra de progreso personalizada
        final double finalBarPercent = barPercent;
        JPanel barContainer = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                int w = getWidth();
                int h = getHeight();
                
                // Fondo de la barra
                g2.setColor(BAR_BG);
                g2.fillRoundRect(0, 0, w, h, h, h);
                
                // Barra de progreso con gradiente
                int barWidth = (int) (w * finalBarPercent);
                if (barWidth > 0) {
                    GradientPaint gradient = new GradientPaint(0, 0, BAR_START, barWidth, 0, BAR_END);
                    g2.setPaint(gradient);
                    g2.fillRoundRect(0, 0, barWidth, h, h, h);
                    
                    // Brillo sutil en la parte superior de la barra
                    g2.setColor(new Color(255, 255, 255, 40));
                    g2.fillRoundRect(0, 0, barWidth, h / 2, h, h);
                }
                
                g2.dispose();
            }
        };
        barContainer.setOpaque(false);
        barContainer.setPreferredSize(new Dimension(0, 10));
        barContainer.setMaximumSize(new Dimension(Integer.MAX_VALUE, 10));
        barContainer.setAlignmentX(Component.LEFT_ALIGNMENT);
        centerPanel.add(barContainer);

        row.add(centerPanel, BorderLayout.CENTER);

        // === RIGHT: Valor de unidades + revenue ===
        JPanel valuePanel = new JPanel();
        valuePanel.setLayout(new BoxLayout(valuePanel, BoxLayout.Y_AXIS));
        valuePanel.setOpaque(false);
        valuePanel.setPreferredSize(new Dimension(100, 36));

        JLabel unitsLabel = new JLabel(String.format("%.0f uds", units));
        unitsLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        unitsLabel.setForeground(BAR_START);
        unitsLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);
        unitsLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        valuePanel.add(unitsLabel);

        JLabel revenueLabel = new JLabel(String.format("$%,.2f", revenue));
        revenueLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        revenueLabel.setForeground(TEXT_MUTED);
        revenueLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);
        revenueLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        valuePanel.add(revenueLabel);

        row.add(valuePanel, BorderLayout.EAST);

        return row;
    }

    private JPanel createKpiCard(String title, JLabel valueLabel, Color accentColor, String emoji) {
        JPanel card = new JPanel(new BorderLayout(0, 6)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Fondo de la tarjeta
                g2.setColor(BG_CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                // Borde lateral coloreado (4px)
                g2.setColor(accentColor);
                g2.fillRoundRect(0, 0, 4, getHeight(), 4, 4);
                // Borde exterior suave
                g2.setColor(BORDER_SUBTLE);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(12, 18, 12, 14));

        // Emoji + Título
        JLabel emojiLabel = new JLabel(emoji + "  ");
        emojiLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        emojiLabel.setForeground(accentColor);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 9));
        titleLabel.setForeground(TEXT_MUTED);

        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);
        topRow.add(emojiLabel, BorderLayout.WEST);
        topRow.add(titleLabel, BorderLayout.CENTER);
        card.add(topRow, BorderLayout.NORTH);

        // Valor grande
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        valueLabel.setForeground(TEXT_PRIMARY);
        valueLabel.setHorizontalAlignment(SwingConstants.LEFT);
        card.add(valueLabel, BorderLayout.CENTER);

        return card;
    }

    private static Color getMedalColor(int position) {
        switch (position) {
            case 1: return MEDAL_GOLD;
            case 2: return MEDAL_SILVER;
            case 3: return MEDAL_BRONZE;
            default: return MEDAL_DEFAULT;
        }
    }
}

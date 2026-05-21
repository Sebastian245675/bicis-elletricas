package com.openbravo.pos.sales;

import com.openbravo.basic.BasicException;
import com.openbravo.data.gui.MessageInf;
import com.openbravo.format.Formats;
import com.openbravo.pos.forms.AppView;
import com.openbravo.pos.forms.BeanFactoryApp;
import com.openbravo.pos.forms.BeanFactoryException;
import com.openbravo.pos.forms.JPanelView;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import java.awt.Frame;
import javax.swing.JDialog;
import javax.swing.SwingUtilities;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JTable;
import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;
import java.util.Calendar;
import com.openbravo.data.loader.SerializerRead;
import com.openbravo.data.loader.DataRead;
import com.openbravo.data.loader.PreparedSentence;
import com.openbravo.data.loader.SerializerWriteParams;
import com.openbravo.pos.forms.DataLogicSales;

/**
 * Vista de Gestión de Apartados con Diseño de Tarjetas Moderno
 */
public class JPanelApartados extends JPanel implements JPanelView, BeanFactoryApp {

    private AppView m_App;
    private DataLogicApartados m_dlApartados;
    private List<ApartadoInfo> m_apartadosList;
    
    private JPanel m_cardsContainer;
    private JTextField m_txtSearch;
    private JButton m_btnRefresh;

    public JPanelApartados() {
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(20, 20, 20, 20));
        setBackground(Color.WHITE); // Restaurado a Blanco Institucional

        // --- HEADER PANEL ---
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(new EmptyBorder(0, 0, 20, 0));
        
        JLabel title = new JLabel("Mis Apartados");
        title.setFont(new Font("Segoe UI", Font.BOLD, 32));
        title.setForeground(new Color(33, 37, 41));
        headerPanel.add(title, BorderLayout.NORTH);

        // --- SEARCH & ACTIONS ---
        JPanel actionsPanel = new JPanel(new BorderLayout(15, 0));
        actionsPanel.setOpaque(false);
        actionsPanel.setBorder(new EmptyBorder(10, 0, 0, 0));
        
        m_txtSearch = new JTextField() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getText().isEmpty()) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                    g2.setColor(Color.GRAY);
                    g2.setFont(getFont().deriveFont(Font.ITALIC));
                    g2.drawString("Buscar por cliente o ticket...", 10, 25);
                    g2.dispose();
                }
            }
        };
        m_txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        m_txtSearch.setPreferredSize(new Dimension(400, 45));
        m_txtSearch.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(206, 212, 218), 1, true),
            BorderFactory.createEmptyBorder(5, 15, 5, 15)
        ));
        m_txtSearch.addActionListener(e -> filterData());
        actionsPanel.add(m_txtSearch, BorderLayout.CENTER);
        
        m_btnRefresh = new JButton("Refrescar Lista");
        styleButton(m_btnRefresh, new Color(46, 125, 50)); // Verde Esmeralda Corporativo
        m_btnRefresh.addActionListener(e -> loadData());
        actionsPanel.add(m_btnRefresh, BorderLayout.EAST);

        headerPanel.add(actionsPanel, BorderLayout.CENTER);
        add(headerPanel, BorderLayout.NORTH);

        // --- CARDS CONTAINER ---
        m_cardsContainer = new JPanel(new WrapLayout(FlowLayout.LEFT, 20, 20));
        m_cardsContainer.setOpaque(false);
        
        JScrollPane scrollPane = new JScrollPane(m_cardsContainer);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);
        
        // --- FOOTER ---
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(15, 0, 0, 0));
        JLabel hint = new JLabel("Toca una tarjeta para ver el detalle de los pagos y artículos.");
        hint.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        hint.setForeground(new Color(108, 117, 125));
        footer.add(hint, BorderLayout.WEST);
        add(footer, BorderLayout.SOUTH);
    }

    private void styleButton(JButton btn, Color bg) {
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(150, 45));
        btn.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
    }

    @Override
    public void init(AppView app) throws BeanFactoryException {
        m_App = app;
        m_dlApartados = (DataLogicApartados) m_App.getBean("com.openbravo.pos.sales.DataLogicApartados");
    }

    @Override
    public Object getBean() { return this; }

    @Override
    public String getTitle() { return "Gestión de Apartados"; }

    @Override
    public void activate() throws BasicException {
        loadData();
    }

    @Override
    public boolean deactivate() { return true; }

    @Override
    public JComponent getComponent() { return this; }

    private void loadData() {
        try {
            m_apartadosList = m_dlApartados.getApartadosList();
            renderCards(m_apartadosList);
        } catch (BasicException ex) {
            new MessageInf(ex).show(this);
        }
    }

    private void filterData() {
        String filter = m_txtSearch.getText().toLowerCase();
        List<ApartadoInfo> filtered = new ArrayList<>();
        for (ApartadoInfo info : m_apartadosList) {
            String name = info.getCustomerName() != null ? info.getCustomerName().toLowerCase() : "";
            String ticketId = info.getTicketId() != null ? info.getTicketId().toLowerCase() : "";
            if (name.contains(filter) || ticketId.contains(filter)) {
                filtered.add(info);
            }
        }
        renderCards(filtered);
    }

    private void renderCards(List<ApartadoInfo> list) {
        m_cardsContainer.removeAll();
        for (ApartadoInfo info : list) {
            m_cardsContainer.add(new ApartadoCard(info));
        }
        m_cardsContainer.revalidate();
        m_cardsContainer.repaint();
    }

    /**
     * Componente de Tarjeta para cada Apartado
     */
    private class ApartadoCard extends JPanel {
        private ApartadoInfo info;
        
        public ApartadoCard(ApartadoInfo info) {
            this.info = info;
            setLayout(new BorderLayout());
            setPreferredSize(new Dimension(280, 360));
            setBackground(Color.WHITE);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            
            // Image Section
            JLabel imageLabel = new JLabel();
            imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
            imageLabel.setPreferredSize(new Dimension(280, 180));
            if (info.getImage() != null) {
                imageLabel.setIcon(rescaleImage(info.getImage(), 260, 160));
            } else {
                imageLabel.setText("No Image");
                imageLabel.setForeground(Color.LIGHT_GRAY);
                imageLabel.setBorder(BorderFactory.createLineBorder(new Color(240, 240, 240)));
            }
            add(imageLabel, BorderLayout.NORTH);

            // Info Section
            JPanel infoPanel = new JPanel();
            infoPanel.setLayout(new GridLayout(0, 1, 5, 5));
            infoPanel.setOpaque(false);
            infoPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

            JLabel lblCustomer = new JLabel(info.getCustomerName());
            lblCustomer.setFont(new Font("Segoe UI", Font.BOLD, 18));
            lblCustomer.setForeground(new Color(33, 37, 41));
            infoPanel.add(lblCustomer);

            JLabel lblTicket = new JLabel("# " + info.getTicketId() + " - " + Formats.DATE.formatValue(info.getDate()));
            lblTicket.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            lblTicket.setForeground(new Color(108, 117, 125));
            infoPanel.add(lblTicket);

            JLabel lblArticles = new JLabel(info.getProducts());
            lblArticles.setFont(new Font("Segoe UI", Font.ITALIC, 13));
            lblArticles.setForeground(new Color(73, 80, 87));
            infoPanel.add(lblArticles);

            // Badge Status & Months
            JPanel badges = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
            badges.setOpaque(false);

            JLabel lblStatus = new JLabel(info.getStatus());
            lblStatus.setOpaque(true);
            lblStatus.setHorizontalAlignment(SwingConstants.CENTER);
            lblStatus.setFont(new Font("Segoe UI", Font.BOLD, 11));
            lblStatus.setPreferredSize(new Dimension(80, 22));
            if (info.getCurDebt() <= 0) {
                lblStatus.setBackground(new Color(25, 135, 84));
                lblStatus.setForeground(Color.WHITE);
            } else {
                lblStatus.setBackground(new Color(255, 193, 7));
                lblStatus.setForeground(Color.BLACK);
            }
            badges.add(lblStatus);

            if (info.getMonths() > 0) {
                JLabel lblMonths = new JLabel(info.getMonths() + " Meses");
                lblMonths.setOpaque(true);
                lblMonths.setBackground(new Color(233, 236, 239));
                lblMonths.setForeground(new Color(73, 80, 87));
                lblMonths.setHorizontalAlignment(SwingConstants.CENTER);
                lblMonths.setFont(new Font("Segoe UI", Font.BOLD, 11));
                lblMonths.setPreferredSize(new Dimension(70, 22));
                badges.add(lblMonths);
            }
            
            if (info.isOverdue()) {
                JLabel lblOverdue = new JLabel("RETRASADO");
                lblOverdue.setOpaque(true);
                lblOverdue.setBackground(new Color(220, 53, 69));
                lblOverdue.setForeground(Color.WHITE);
                lblOverdue.setHorizontalAlignment(SwingConstants.CENTER);
                lblOverdue.setFont(new Font("Segoe UI", Font.BOLD, 11));
                lblOverdue.setPreferredSize(new Dimension(85, 22));
                badges.add(lblOverdue);
            }
            
            infoPanel.add(badges);

            // Pricing
            JPanel pricePanel = new JPanel(new GridLayout(1, 2));
            pricePanel.setOpaque(false);
            
            JLabel lblTotal = new JLabel("Total: " + Formats.CURRENCY.formatValue(info.getTotal()));
            lblTotal.setFont(new Font("Segoe UI", Font.BOLD, 14));
            pricePanel.add(lblTotal);

            JLabel lblDebt = new JLabel("Deuda: " + Formats.CURRENCY.formatValue(info.getCurDebt()));
            lblDebt.setFont(new Font("Segoe UI", Font.BOLD, 14));
            lblDebt.setForeground(new Color(220, 53, 69));
            lblDebt.setHorizontalAlignment(SwingConstants.RIGHT);
            pricePanel.add(lblDebt);

            infoPanel.add(pricePanel);

            add(infoPanel, BorderLayout.CENTER);

            // Add Click effect
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    setBorder(BorderFactory.createLineBorder(new Color(13, 110, 253), 2));
                }
                @Override
                public void mouseExited(MouseEvent e) {
                    setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230), 1));
                }
                @Override
                public void mouseClicked(MouseEvent e) {
                    showDetails(info);
                }
            });
            setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230), 1));
        }

        private ImageIcon rescaleImage(byte[] data, int w, int h) {
            try {
                BufferedImage img = javax.imageio.ImageIO.read(new java.io.ByteArrayInputStream(data));
                if (img == null) return null;
                return new ImageIcon(img.getScaledInstance(w, h, Image.SCALE_SMOOTH));
            } catch (Exception e) {
                return null;
            }
        }
    }

    private void showDetails(ApartadoInfo info) {
        try {
            // Cargar pagos reales
            List<Object[]> payments = m_dlApartados.getApartadoPayments(info.getId());
            
            JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Detalles del Apartado", true);
            dialog.setLayout(new BorderLayout());
            dialog.setSize(550, 650);
            dialog.setLocationRelativeTo(this);

            JPanel root = new JPanel();
            root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
            root.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));
            root.setBackground(Color.WHITE);

            // Header con Imagen y Título
            JPanel header = new JPanel(new BorderLayout(20, 0));
            header.setOpaque(false);
            header.setAlignmentX(Component.LEFT_ALIGNMENT);
            
            if (info.getImage() != null) {
                try {
                    BufferedImage img = ImageIO.read(new ByteArrayInputStream(info.getImage()));
                    JLabel lblImg = new JLabel(new ImageIcon(img.getScaledInstance(120, 120, Image.SCALE_SMOOTH)));
                    lblImg.setBorder(BorderFactory.createLineBorder(new Color(233, 236, 239)));
                    header.add(lblImg, BorderLayout.WEST);
                } catch (Exception ex) {}
            }
            
            JPanel titlePanel = new JPanel(new GridLayout(3, 1));
            titlePanel.setOpaque(false);
            
            JLabel lblProd = new JLabel(info.getProducts());
            lblProd.setFont(new Font("Segoe UI", Font.BOLD, 22));
            titlePanel.add(lblProd);
            
            JLabel lblTicket = new JLabel("Ticket #" + info.getTicketId());
            lblTicket.setFont(new Font("Segoe UI", Font.PLAIN, 16));
            lblTicket.setForeground(Color.GRAY);
            titlePanel.add(lblTicket);

            JLabel lblStatus = new JLabel(info.getStatus());
            lblStatus.setFont(new Font("Segoe UI", Font.BOLD, 14));
            lblStatus.setForeground(info.getCurDebt() <= 0 ? new Color(25, 135, 84) : new Color(255, 193, 7));
            titlePanel.add(lblStatus);
            
            header.add(titlePanel, BorderLayout.CENTER);
            root.add(header);
            root.add(Box.createVerticalStrut(25));

            // Info de Cliente y Tiempo
            JPanel grid = new JPanel(new GridLayout(2, 2, 10, 10));
            grid.setOpaque(false);
            grid.setAlignmentX(Component.LEFT_ALIGNMENT);
            grid.add(createDetailItem("CLIENTE", info.getCustomerName()));
            grid.add(createDetailItem("TELÉFONO", info.getCustomerPhone() != null ? info.getCustomerPhone() : "No registrado"));
            grid.add(createDetailItem("FECHA INICIO", Formats.DATE.formatValue(info.getDate())));
            
            String deadlineStr = info.getDeadline() != null ? Formats.DATE.formatValue(info.getDeadline()) : "No definida";
            JLabel lblDeadline = createDetailItem("FECHA LÍMITE", deadlineStr);
            if (info.isOverdue()) {
                lblDeadline.setForeground(Color.RED);
                lblDeadline.setText(deadlineStr + " (RETRASADO)");
            }
            grid.add(lblDeadline);
            
            root.add(grid);
            root.add(Box.createVerticalStrut(25));

            // Resumen de Pagos
            JPanel summary = new JPanel(new GridLayout(1, 3, 10, 0));
            summary.setOpaque(true);
            summary.setBackground(new Color(248, 249, 250));
            summary.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(233, 236, 239)),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
            ));
            summary.setAlignmentX(Component.LEFT_ALIGNMENT);
            summary.add(createSummaryItem("TOTAL", Formats.CURRENCY.formatValue(info.getTotal())));
            summary.add(createSummaryItem("ABONADO", Formats.CURRENCY.formatValue(info.getPaid())));
            summary.add(createSummaryItem("DEUDA", Formats.CURRENCY.formatValue(info.getCurDebt())));
            root.add(summary);
            root.add(Box.createVerticalStrut(25));

            // Tabla de Historial
            JLabel lblHist = new JLabel("HISTORIAL DE PAGOS");
            lblHist.setFont(new Font("Segoe UI", Font.BOLD, 12));
            lblHist.setForeground(new Color(108, 117, 125));
            lblHist.setAlignmentX(Component.LEFT_ALIGNMENT);
            root.add(lblHist);
            root.add(Box.createVerticalStrut(10));

            String[] columns = {"Fecha", "Método", "Monto"};
            Object[][] data = new Object[payments.size()][3];
            for (int i = 0; i < payments.size(); i++) {
                Object[] p = payments.get(i);
                data[i][0] = Formats.TIMESTAMP.formatValue((java.util.Date) p[0]);
                data[i][1] = p[1];
                data[i][2] = Formats.CURRENCY.formatValue((java.lang.Double) p[2]);
            }
            
            JTable table = new JTable(data, columns);
            table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            table.setRowHeight(30);
            table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
            table.getTableHeader().setBackground(new Color(248, 249, 250));
            
            JScrollPane scroll = new JScrollPane(table);
            scroll.setPreferredSize(new Dimension(500, 150));
            scroll.setBorder(BorderFactory.createLineBorder(new Color(233, 236, 239)));
            scroll.setAlignmentX(Component.LEFT_ALIGNMENT);
            root.add(scroll);

            dialog.add(root, BorderLayout.CENTER);
            
            JButton btnClose = new JButton("Cerrar");
            btnClose.setFocusPainted(false);
            btnClose.setBackground(new Color(13, 110, 253));
            btnClose.setForeground(Color.WHITE);
            btnClose.setFont(new Font("Segoe UI", Font.BOLD, 14));
            btnClose.addActionListener(e -> dialog.dispose());
            
            JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            footer.setBackground(Color.WHITE);
            footer.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 20));
            footer.add(btnClose);
            dialog.add(footer, BorderLayout.SOUTH);

            dialog.setVisible(true);
        } catch (BasicException e) {
            MessageInf msg = new MessageInf(MessageInf.SGN_WARNING, "Error al cargar detalles", e);
            msg.show(this);
        }
    }

    private JLabel createDetailItem(String title, String value) {
        JLabel label = new JLabel("<html><font color='#6c757d' size='2'>" + title + "</font><br><font color='#212529' size='4'>" + (value == null ? "-" : value) + "</font></html>");
        label.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        return label;
    }

    private JPanel createSummaryItem(String title, String value) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        JLabel lblT = new JLabel(title);
        lblT.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblT.setForeground(new Color(108, 117, 125));
        JLabel lblV = new JLabel(value);
        lblV.setFont(new Font("Segoe UI", Font.BOLD, 16));
        p.add(lblT, BorderLayout.NORTH);
        p.add(lblV, BorderLayout.CENTER);
        return p;
    }

    /**
     * Un layout simple que envuelve los componentes (como FlowLayout pero respeta el ancho del contenedor)
     */
    class WrapLayout extends FlowLayout {
        public WrapLayout() { super(); }
        public WrapLayout(int align) { super(align); }
        public WrapLayout(int align, int hgap, int vgap) { super(align, hgap, vgap); }

        @Override
        public Dimension preferredLayoutSize(java.awt.Container target) {
            return layoutSize(target, true);
        }

        @Override
        public Dimension minimumLayoutSize(java.awt.Container target) {
            Dimension minimum = layoutSize(target, false);
            minimum.width -= (getHgap() + 1);
            return minimum;
        }

        private Dimension layoutSize(java.awt.Container target, boolean preferred) {
            synchronized (target.getTreeLock()) {
                int targetWidth = target.getSize().width;
                if (targetWidth == 0) targetWidth = Integer.MAX_VALUE;

                int hgap = getHgap();
                int vgap = getVgap();
                java.awt.Insets insets = target.getInsets();
                int horizontalInsetsAndGap = insets.left + insets.right + (hgap * 2);
                int maxWidth = targetWidth - horizontalInsetsAndGap;

                Dimension dim = new Dimension(0, 0);
                int rowWidth = 0;
                int rowHeight = 0;

                int nmembers = target.getComponentCount();
                for (int i = 0; i < nmembers; i++) {
                    java.awt.Component m = target.getComponent(i);
                    if (m.isVisible()) {
                        Dimension d = preferred ? m.getPreferredSize() : m.getMinimumSize();
                        if (rowWidth + d.width > maxWidth) {
                            dim.width = Math.max(dim.width, rowWidth);
                            dim.height += rowHeight + vgap;
                            rowWidth = 0;
                            rowHeight = 0;
                        }
                        if (rowWidth != 0) rowWidth += hgap;
                        rowWidth += d.width;
                        rowHeight = Math.max(rowHeight, d.height);
                    }
                }
                dim.width = Math.max(dim.width, rowWidth);
                dim.height += rowHeight;
                dim.width += horizontalInsetsAndGap;
                dim.height += insets.top + insets.bottom + vgap * 2;
                return dim;
            }
        }
    }
}

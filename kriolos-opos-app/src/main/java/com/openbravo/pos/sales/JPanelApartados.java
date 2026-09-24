package com.openbravo.pos.sales;

import com.openbravo.basic.BasicException;
import com.openbravo.data.gui.MessageInf;
import com.openbravo.format.Formats;
import com.openbravo.pos.forms.AppView;
import com.openbravo.pos.forms.BeanFactoryApp;
import com.openbravo.pos.forms.BeanFactoryException;
import com.openbravo.pos.forms.JPanelView;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;
import com.openbravo.pos.forms.DataLogicSales;

/**
 * Vista de Gestión de Apartados - Réplica exacta del diseño solicitado
 */
public class JPanelApartados extends JPanel implements JPanelView, BeanFactoryApp {

    private AppView m_App;
    private DataLogicApartados m_dlApartados;
    private List<ApartadoInfo> m_apartadosList;

    private JPanel m_cardsContainer;
    private JTextField m_txtSearch;
    private JButton m_btnRefresh;
    private JLabel m_lblSubtitle;

    // Colores del diseño de la captura
    private static final Color BG_PAGE = new Color(245, 246, 248);
    private static final Color CARD_BG = Color.WHITE;
    private static final Color TEXT_DARK = new Color(33, 37, 41);
    private static final Color TEXT_MUTED = new Color(108, 117, 125);
    private static final Color BORDER_COLOR = new Color(222, 226, 230);
    
    // Colores de botones y badges
    private static final Color BTN_GREEN = new Color(40, 167, 69);
    private static final Color BTN_GOLD = new Color(193, 154, 62);
    private static final Color BTN_RED = new Color(220, 53, 69);
    private static final Color TEXT_DEBT = new Color(185, 28, 28);

    public JPanelApartados() {
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(24, 24, 24, 24));
        setBackground(BG_PAGE);

        // ===== HEADER PANEL =====
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setOpaque(false);
        headerPanel.setBorder(new EmptyBorder(0, 0, 16, 0));

        JLabel title = new JLabel("Mis Apartados");
        title.setFont(new Font("Segoe UI", Font.BOLD, 30));
        title.setForeground(TEXT_DARK);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        headerPanel.add(title);

        m_lblSubtitle = new JLabel("Mostrando 0 apartados activos");
        m_lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        m_lblSubtitle.setForeground(TEXT_MUTED);
        m_lblSubtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        headerPanel.add(m_lblSubtitle);
        headerPanel.add(Box.createVerticalStrut(16));

        // ===== SEARCH ROW =====
        JPanel searchRow = new JPanel(new BorderLayout(15, 0));
        searchRow.setOpaque(false);
        searchRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        searchRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        m_txtSearch = new JTextField() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getText().isEmpty()) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                    g2.setColor(new Color(180, 180, 180));
                    g2.setFont(getFont().deriveFont(Font.ITALIC));
                    g2.drawString("Buscar por cliente o ticket...", 12, getHeight() / 2 + 5);
                    g2.dispose();
                }
            }
        };
        m_txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        m_txtSearch.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(6, 12, 6, 12)
        ));
        m_txtSearch.addActionListener(e -> filterData());
        m_txtSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { filterData(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { filterData(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { filterData(); }
        });
        searchRow.add(m_txtSearch, BorderLayout.CENTER);

        m_btnRefresh = new JButton("Actualizar");
        m_btnRefresh.setIcon(new com.openbravo.pos.util.ModernActionIcon(
                com.openbravo.pos.util.ModernActionIcon.Type.REFRESH, 18, Color.WHITE));
        m_btnRefresh.setFont(new Font("Segoe UI", Font.BOLD, 13));
        m_btnRefresh.setBackground(new Color(40, 110, 60));
        m_btnRefresh.setForeground(Color.WHITE);
        m_btnRefresh.setFocusPainted(false);
        m_btnRefresh.setCursor(new Cursor(Cursor.HAND_CURSOR));
        m_btnRefresh.setPreferredSize(new Dimension(150, 44));
        m_btnRefresh.setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));
        m_btnRefresh.setOpaque(true);
        m_btnRefresh.addActionListener(e -> loadData());
        searchRow.add(m_btnRefresh, BorderLayout.EAST);

        headerPanel.add(searchRow);
        add(headerPanel, BorderLayout.NORTH);

        // ===== GRID CONTAINER =====
        m_cardsContainer = new JPanel(new GridLayout(0, 2, 20, 20));
        m_cardsContainer.setOpaque(false);
        m_cardsContainer.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                GridLayout layout = (GridLayout) m_cardsContainer.getLayout();
                int columns = m_cardsContainer.getWidth() < 1050 ? 1 : 2;
                if (layout.getColumns() != columns) {
                    layout.setColumns(columns);
                    renderCards(m_apartadosList == null ? new ArrayList<>() : filteredApartados());
                }
            }
        });

        JScrollPane scroll = new JScrollPane(m_cardsContainer);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);

        // ===== FOOTER =====
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(12, 0, 0, 0));
        JLabel hint = new JLabel("Toca una tarjeta para ver el detalle de los pagos y artículos.");
        hint.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        hint.setForeground(TEXT_MUTED);
        footer.add(hint, BorderLayout.WEST);
        add(footer, BorderLayout.SOUTH);
    }

    @Override
    public void init(AppView app) throws BeanFactoryException {
        m_App = app;
        m_dlApartados = (DataLogicApartados) m_App.getBean("com.openbravo.pos.sales.DataLogicApartados");
    }

    @Override public Object getBean() { return this; }
    @Override public String getTitle() { return "Gestión de Apartados"; }
    @Override public void activate() throws BasicException { loadData(); }
    @Override public boolean deactivate() { return true; }
    @Override public JComponent getComponent() { return this; }

    private void loadData() {
        try {
            m_apartadosList = m_dlApartados.getApartadosList();
            renderCards(m_apartadosList);
        } catch (BasicException ex) {
            new MessageInf(ex).show(this);
        }
    }

    private void filterData() {
        renderCards(filteredApartados());
    }

    private List<ApartadoInfo> filteredApartados() {
        String filter = m_txtSearch.getText().trim().toLowerCase();
        List<ApartadoInfo> filtered = new ArrayList<>();
        if (m_apartadosList != null) {
            for (ApartadoInfo info : m_apartadosList) {
                String name = info.getCustomerName() != null ? info.getCustomerName().toLowerCase() : "";
                String ticketId = info.getTicketId() != null ? info.getTicketId().toLowerCase() : "";
                if (name.contains(filter) || ticketId.contains(filter)) {
                    filtered.add(info);
                }
            }
        }
        return filtered;
    }

    private void renderCards(List<ApartadoInfo> list) {
        m_cardsContainer.removeAll();
        m_lblSubtitle.setText("Mostrando " + list.size() + " apartados activos");
        if (list.isEmpty()) {
            JPanel emptyState = new JPanel(new GridBagLayout());
            emptyState.setOpaque(false);
            String message = m_txtSearch.getText().trim().isEmpty()
                    ? "No hay apartados activos por cobrar."
                    : "No se encontraron apartados con esa búsqueda.";
            JLabel emptyLabel = new JLabel(message);
            emptyLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
            emptyLabel.setForeground(TEXT_MUTED);
            emptyLabel.setBorder(new EmptyBorder(48, 20, 48, 20));
            emptyState.add(emptyLabel);
            m_cardsContainer.add(emptyState);
        }
        for (ApartadoInfo info : list) {
            m_cardsContainer.add(new ApartadoCard(info));
        }
        GridLayout layout = (GridLayout) m_cardsContainer.getLayout();
        if (!list.isEmpty() && layout.getColumns() == 2 && list.size() % 2 != 0) {
            JPanel filler = new JPanel();
            filler.setOpaque(false);
            m_cardsContainer.add(filler);
        }
        m_cardsContainer.revalidate();
        m_cardsContainer.repaint();
    }

    // =========================================================================
    //  TARJETA DE APARTADO (DISEÑO HORIZONTAL DE LA CAPTURA)
    // =========================================================================

    private class ApartadoCard extends JPanel {
        private final ApartadoInfo info;

        public ApartadoCard(ApartadoInfo info) {
            this.info = info;

            setLayout(new BorderLayout());
            setBackground(CARD_BG);
            setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));

            // ----- PANEL IZQUIERDO: Imagen -----
            JPanel leftPanel = new JPanel(new GridBagLayout());
            leftPanel.setOpaque(false);
            leftPanel.setBorder(new EmptyBorder(15, 15, 15, 10));

            JLabel imageLabel = new JLabel();
            imageLabel.setPreferredSize(new Dimension(110, 110));
            imageLabel.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
            imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
            
            if (info.getImage() != null) {
                imageLabel.setIcon(rescaleImage(info.getImage(), 108, 108));
            } else {
                // Placeholder gris
                imageLabel.setText("Sin img");
                imageLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                imageLabel.setForeground(TEXT_MUTED);
                imageLabel.setBackground(new Color(245, 245, 245));
                imageLabel.setOpaque(true);
            }
            leftPanel.add(imageLabel);
            add(leftPanel, BorderLayout.WEST);

            // ----- PANEL CENTRAL: Datos e Información -----
            JPanel centerPanel = new JPanel();
            centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
            centerPanel.setOpaque(false);
            centerPanel.setBorder(new EmptyBorder(15, 5, 15, 5));

            // Nombre Cliente
            JLabel lblCustomer = new JLabel(info.getCustomerName() != null ? info.getCustomerName() : "Cliente General");
            lblCustomer.setFont(new Font("Segoe UI", Font.BOLD, 17));
            lblCustomer.setForeground(TEXT_DARK);
            lblCustomer.setAlignmentX(Component.LEFT_ALIGNMENT);
            centerPanel.add(lblCustomer);

            // Ticket y Fecha
            String dateStr = info.getDate() != null ? Formats.DATE.formatValue(info.getDate()) : "";
            JLabel lblTicketInfo = new JLabel("#" + (info.getTicketId() != null ? info.getTicketId() : "?") + " - " + dateStr);
            lblTicketInfo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            lblTicketInfo.setForeground(TEXT_MUTED);
            lblTicketInfo.setAlignmentX(Component.LEFT_ALIGNMENT);
            centerPanel.add(lblTicketInfo);
            centerPanel.add(Box.createVerticalStrut(4));

            // Descripción de Producto
            JLabel lblProdName = new JLabel(info.getProducts() != null ? info.getProducts() : "Sin producto");
            lblProdName.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            lblProdName.setForeground(TEXT_DARK);
            lblProdName.setAlignmentX(Component.LEFT_ALIGNMENT);
            centerPanel.add(lblProdName);
            centerPanel.add(Box.createVerticalStrut(8));

            // Fila de Badges
            JPanel badgesRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
            badgesRow.setOpaque(false);
            badgesRow.setAlignmentX(Component.LEFT_ALIGNMENT);

            // Badge de Color
            badgesRow.add(createSolidBadge(getColorBadgeLabel(info), getColorBadgeBackground(info)));

            // Badge de Estado con Icono
            badgesRow.add(createStatusBadge(info));

            centerPanel.add(badgesRow);
            centerPanel.add(Box.createVerticalGlue());

            // Fila de Botones
            JPanel buttonsRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
            buttonsRow.setOpaque(false);
            buttonsRow.setAlignmentX(Component.LEFT_ALIGNMENT);

            JButton btnDetail = new JButton("\u2630 Ver Detalle");
            btnDetail.setText("Ver detalle");
            btnDetail.setIcon(new com.openbravo.pos.util.ModernActionIcon(
                    com.openbravo.pos.util.ModernActionIcon.Type.VIEW, 17, new Color(7, 55, 43)));
            styleButtonOutline(btnDetail);
            btnDetail.addActionListener(e -> showDetails(info));
            buttonsRow.add(btnDetail);

            JButton btnAction = new JButton(getActionButtonText(info));
            styleButtonSolid(btnAction, getActionButtonColor(info));
            boolean canAct = info.getCurDebt() <= 0
                    ? m_App.hasPermission("layaway.Deliver")
                    : m_App.hasPermission("layaway.TakePayment")
                            && m_App.hasPermission("com.openbravo.pos.customers.CustomersPayment");
            btnAction.setEnabled(canAct);
            if (!canAct) {
                btnAction.setToolTipText("Tu rol no tiene permiso para realizar esta acción");
            }
            btnAction.addActionListener(e -> handleLayawayAction(info));
            buttonsRow.add(btnAction);

            centerPanel.add(buttonsRow);
            add(centerPanel, BorderLayout.CENTER);

            // ----- PANEL DERECHO: Financieros y Duración -----
            JPanel rightPanel = new JPanel();
            rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
            rightPanel.setOpaque(false);
            rightPanel.setBorder(new EmptyBorder(15, 5, 15, 15));
            rightPanel.setPreferredSize(new Dimension(170, 0));

            // Total
            JLabel lblTotal = new JLabel("Total: " + Formats.CURRENCY.formatValue(info.getTotal()));
            lblTotal.setFont(new Font("Segoe UI", Font.BOLD, 14));
            lblTotal.setForeground(TEXT_DARK);
            lblTotal.setAlignmentX(Component.RIGHT_ALIGNMENT);
            rightPanel.add(lblTotal);

            // Abonado
            JLabel lblPaid = new JLabel("Abonado: " + Formats.CURRENCY.formatValue(info.getPaid()));
            lblPaid.setFont(new Font("Segoe UI", Font.BOLD, 13));
            lblPaid.setForeground(TEXT_DARK);
            lblPaid.setAlignmentX(Component.RIGHT_ALIGNMENT);
            rightPanel.add(lblPaid);

            // Deuda
            JLabel lblDebt = new JLabel("Deuda: " + Formats.CURRENCY.formatValue(info.getCurDebt()));
            lblDebt.setFont(new Font("Segoe UI", Font.BOLD, 14));
            lblDebt.setForeground(info.getCurDebt() <= 0 ? new Color(40, 167, 69) : TEXT_DEBT);
            lblDebt.setAlignmentX(Component.RIGHT_ALIGNMENT);
            rightPanel.add(lblDebt);

            rightPanel.add(Box.createVerticalGlue());

            // Meses
            if (info.getMonths() > 0) {
                JLabel lblMonths = new JLabel(info.getMonths() + " Meses");
                lblMonths.setOpaque(true);
                lblMonths.setBackground(new Color(222, 226, 230));
                lblMonths.setForeground(new Color(73, 80, 87));
                lblMonths.setFont(new Font("Segoe UI", Font.BOLD, 12));
                lblMonths.setBorder(BorderFactory.createEmptyBorder(3, 8, 3, 8));
                lblMonths.setAlignmentX(Component.RIGHT_ALIGNMENT);
                rightPanel.add(lblMonths);
            }

            add(rightPanel, BorderLayout.EAST);
        }

        // --- Helpers de Tarjeta ---

        private String getColorBadgeLabel(ApartadoInfo info) {
            if (info.getCurDebt() <= 0) return "Listo para entregar";
            if (info.isOverdue()) {
                if (isDeeplyOverdue(info)) return "Vencido +30 días";
                return "Plazo vencido";
            }
            return "Dentro del plazo";
        }

        private Color getColorBadgeBackground(ApartadoInfo info) {
            if (info.getCurDebt() <= 0) return new Color(40, 167, 69);
            if (info.isOverdue()) {
                if (isDeeplyOverdue(info)) return new Color(139, 0, 0);
                return new Color(230, 81, 0);
            }
            return new Color(245, 140, 0);
        }

        private JLabel createSolidBadge(String text, Color bg) {
            JLabel label = new JLabel(text);
            label.setOpaque(true);
            label.setBackground(bg);
            label.setForeground(Color.WHITE);
            label.setFont(new Font("Segoe UI", Font.BOLD, 11));
            label.setBorder(BorderFactory.createEmptyBorder(3, 8, 3, 8));
            return label;
        }

        private JLabel createStatusBadge(ApartadoInfo info) {
            String text = "";
            Color fg = Color.BLACK;
            Color bg = new Color(240, 240, 240);

            if (info.getCurDebt() <= 0) {
                text = "\u2713 Pagado";
                fg = new Color(40, 167, 69);
                bg = new Color(223, 240, 216);
            } else if (info.isOverdue()) {
                if (isDeeplyOverdue(info)) {
                    text = "\u2717 Vencido";
                    fg = new Color(185, 28, 28);
                    bg = new Color(242, 222, 222);
                } else {
                    text = "\u26A0 Retrasado";
                    fg = new Color(230, 81, 0);
                    bg = new Color(252, 248, 227);
                }
            } else {
                text = "\uD83D\uDD12 Abonado Parcial";
                fg = new Color(180, 120, 10);
                bg = new Color(252, 248, 227);
            }

            JLabel label = new JLabel(text);
            label.setOpaque(true);
            label.setBackground(bg);
            label.setForeground(fg);
            label.setFont(new Font("Segoe UI", Font.BOLD, 11));
            label.setBorder(BorderFactory.createEmptyBorder(3, 8, 3, 8));
            return label;
        }

        private String getActionButtonText(ApartadoInfo info) {
            if (info.getCurDebt() <= 0) return "Entregar Producto";
            return "Realizar Pago";
        }

        private Color getActionButtonColor(ApartadoInfo info) {
            if (info.getCurDebt() <= 0) return BTN_GOLD;
            return BTN_GREEN;
        }

        private boolean isDeeplyOverdue(ApartadoInfo info) {
            if (!info.isOverdue() || info.getDeadline() == null) return false;
            long diff = System.currentTimeMillis() - info.getDeadline().getTime();
            return diff > 30L * 24 * 60 * 60 * 1000;
        }

        private void styleButtonOutline(JButton btn) {
            btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
            btn.setBackground(Color.WHITE);
            btn.setForeground(TEXT_DARK);
            btn.setFocusPainted(false);
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btn.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(BORDER_COLOR, 1),
                    BorderFactory.createEmptyBorder(6, 12, 6, 12)
            ));
        }

        private void styleButtonSolid(JButton btn, Color bg) {
            btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
            btn.setBackground(bg);
            btn.setForeground(Color.WHITE);
            btn.setFocusPainted(false);
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btn.setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));
            btn.setOpaque(true);
        }

        private ImageIcon rescaleImage(byte[] data, int w, int h) {
            try {
                BufferedImage img = ImageIO.read(new ByteArrayInputStream(data));
                if (img == null) return null;
                return new ImageIcon(img.getScaledInstance(w, h, Image.SCALE_SMOOTH));
            } catch (Exception e) {
                return null;
            }
        }
    }

    private void handleLayawayAction(ApartadoInfo info) {
        if (info.getCurDebt() <= 0) {
            if (!m_App.hasPermission("layaway.Deliver")) {
                showAccessDenied("entregar apartados liquidados");
                return;
            }
            int answer = JOptionPane.showConfirmDialog(this,
                    "Confirma que el producto del ticket #" + info.getTicketId()
                            + " fue entregado al cliente " + info.getCustomerName() + ".",
                    "Confirmar entrega", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (answer == JOptionPane.YES_OPTION) {
                try {
                    m_dlApartados.markAsDelivered(info.getId());
                    loadData();
                    JOptionPane.showMessageDialog(this, "Entrega registrada correctamente.",
                            "Apartado entregado", JOptionPane.INFORMATION_MESSAGE);
                } catch (BasicException ex) {
                    new MessageInf(MessageInf.SGN_WARNING, "No se pudo registrar la entrega", ex).show(this);
                }
            }
            return;
        }

        if (!m_App.hasPermission("layaway.TakePayment")
                || !m_App.hasPermission("com.openbravo.pos.customers.CustomersPayment")) {
            showAccessDenied("recibir abonos de apartados");
            return;
        }

        m_App.getAppUserView().showTask("com.openbravo.pos.customers.CustomersPayment");
        SwingUtilities.invokeLater(() -> {
            try {
                Object view = m_App.getBean("com.openbravo.pos.customers.CustomersPayment");
                if (view instanceof com.openbravo.pos.customers.CustomersPayment) {
                    ((com.openbravo.pos.customers.CustomersPayment) view)
                            .selectLayaway(info.getCustomerId(), info.getId(), info.getCurDebt());
                }
            } catch (Exception ex) {
                // La pantalla queda abierta para permitir búsqueda manual si el bean no
                // puede recuperarse en esta instalación.
            }
        });
    }

    private void showAccessDenied(String action) {
        JOptionPane.showMessageDialog(this,
                "Tu usuario no tiene permiso para " + action + ".\nSolicítalo a un administrador.",
                "Acceso restringido", JOptionPane.WARNING_MESSAGE);
    }

    // =========================================================================
    //  DIALOGO DE DETALLES (MANTIENE LA FUNCIONALIDAD ACTUAL)
    // =========================================================================

    private void showDetails(ApartadoInfo info) {
        try {
            List<Object[]> payments = m_dlApartados.getApartadoPayments(info.getId());

            JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Detalles del Apartado", true);
            dialog.setLayout(new BorderLayout());
            dialog.setSize(550, 650);
            dialog.setLocationRelativeTo(this);

            JPanel root = new JPanel();
            root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
            root.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));
            root.setBackground(Color.WHITE);

            // Header
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
            lblStatus.setForeground(info.getCurDebt() <= 0 ? new Color(40, 167, 69) : TEXT_DEBT);
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
            new MessageInf(MessageInf.SGN_WARNING, "Error al cargar detalles", e).show(this);
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
        lblT.setForeground(TEXT_MUTED);
        JLabel lblV = new JLabel(value);
        lblV.setFont(new Font("Segoe UI", Font.BOLD, 16));
        p.add(lblT, BorderLayout.NORTH);
        p.add(lblV, BorderLayout.CENTER);
        return p;
    }
}

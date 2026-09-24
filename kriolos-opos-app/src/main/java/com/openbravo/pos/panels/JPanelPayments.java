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

package com.openbravo.pos.panels;

import com.openbravo.basic.BasicException;
import com.openbravo.data.user.EditorRecord;
import com.openbravo.data.user.ListProvider;
import com.openbravo.data.user.DefaultSaveProvider;
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.pos.forms.DataLogicSales;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.table.*;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFDataFormat;

/**
 * Premium Entradas y Salidas Panel — dark dashboard cards, pill badges, Excel-style table.
 * @author adrianromero, antigravity
 */
public class JPanelPayments extends JPanelTable {

    private PaymentsEditor jeditor;
    private DataLogicSales m_dlSales = null;

    private boolean m_initialized = false;
    private CardLayout m_cardLayout;
    private JPanel m_cardsPanel;
    private JPanel m_listPanel;
    private JButton btnEditor;
    private JButton btnList;

    // List view widgets
    private JLabel lblMonthTitle;
    private JComboBox<MonthYearOption> comboMonths;
    private DefaultTableModel tableModel;
    private JTable movementsTable;
    private JButton btnDeleteMovement;

    // Summary card labels
    private JLabel lblBalanceAmount;
    private JLabel lblBalanceDelta;
    private JLabel lblEntradasAmount;
    private JLabel lblSalidasAmount;

    private List<CashMovement> allMovements = new ArrayList<>();
    private List<CashMovement> filteredMovements = new ArrayList<>();

    // ── Palette ──
    private static final Color CARD_DARK    = new Color(30, 41, 59);    // slate-800
    private static final Color CARD_DARK2   = new Color(36, 48, 68);    // slightly lighter
    private static final Color GREEN        = new Color(34, 197, 94);   // green-500
    private static final Color GREEN_DIM    = new Color(22, 163, 74);   // green-600
    private static final Color RED          = new Color(239, 68, 68);   // red-500
    private static final Color RED_DIM      = new Color(220, 38, 38);   // red-600
    private static final Color SLATE_50     = new Color(248, 250, 252);
    private static final Color SLATE_100    = new Color(241, 245, 249);
    private static final Color SLATE_200    = new Color(226, 232, 240);
    private static final Color SLATE_400    = new Color(148, 163, 184);
    private static final Color SLATE_500    = new Color(100, 116, 139);
    private static final Color SLATE_600    = new Color(71, 85, 105);
    private static final Color SLATE_800    = new Color(30, 41, 59);
    private static final Color BLUE_SEL     = new Color(219, 234, 254);
    private static final DecimalFormat DF   = new DecimalFormat("$#,##0.00");
    private static final int CARD_RADIUS    = 14;

    public JPanelPayments() {}

    @Override
    protected void init() {
        m_dlSales = (DataLogicSales) app.getBean("com.openbravo.pos.forms.DataLogicSales");
        jeditor = new PaymentsEditor(app, dirty);
    }

    @Override
    public void activate() throws BasicException {
        super.activate();
        
        toolbar.setVisible(false); // Hide standard JSaver toolbar
        container.remove(jeditor.getComponent()); // Remove standard editor from JPanelTable layout
        
        if (!m_initialized) {
            createListPanel();
            m_initialized = true;
        }
        
        container.add(m_listPanel, BorderLayout.CENTER);
        loadMovementsList();
        
        this.revalidate();
        this.repaint();
    }

    @Override
    public boolean deactivate() { return super.deactivate(); }

    // =====================================================================
    // List Panel — premium dashboard
    // =====================================================================
    private void createListPanel() {
        m_listPanel = new JPanel(new BorderLayout(0, 0));
        m_listPanel.setBackground(new Color(243, 244, 246)); // gray-100 background
        m_listPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        JPanel north = new JPanel();
        north.setLayout(new BoxLayout(north, BoxLayout.Y_AXIS));
        north.setOpaque(false);

        // ── 1. Summary cards row ──
        JPanel cardsRow = new JPanel(new GridLayout(1, 3, 14, 0));
        cardsRow.setOpaque(false);
        cardsRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 95));
        cardsRow.setPreferredSize(new Dimension(800, 95));
        cardsRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        cardsRow.add(buildBalanceCard());
        cardsRow.add(buildEntradasCard());
        cardsRow.add(buildSalidasCard());
        north.add(cardsRow);
        north.add(Box.createVerticalStrut(12));

        // ── 2. Filter row ──
        JPanel filterRow = new JPanel(new BorderLayout());
        filterRow.setOpaque(false);
        filterRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        filterRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        lblMonthTitle = new JLabel("Mes de —");
        lblMonthTitle.setFont(new Font("Segoe UI", Font.BOLD, 19));
        lblMonthTitle.setForeground(SLATE_800);
        filterRow.add(lblMonthTitle, BorderLayout.WEST);

        JPanel filterRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        filterRight.setOpaque(false);

        JButton btnNewMovement = new JButton("+ Nuevo Movimiento");
        btnNewMovement.setText("Nuevo movimiento");
        btnNewMovement.setIcon(new com.openbravo.pos.util.ModernActionIcon(
                com.openbravo.pos.util.ModernActionIcon.Type.ADD, 18, Color.WHITE));
        btnNewMovement.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnNewMovement.setBackground(new Color(37, 99, 235)); // Accent Blue
        btnNewMovement.setForeground(Color.WHITE);
        btnNewMovement.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(37, 99, 235), 1),
            BorderFactory.createEmptyBorder(5, 16, 5, 16)));
        btnNewMovement.setFocusPainted(false);
        btnNewMovement.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnNewMovement.addActionListener(e -> showNewMovementDialog());
        filterRight.add(btnNewMovement);

        comboMonths = new JComboBox<>();
        comboMonths.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        comboMonths.setPreferredSize(new Dimension(170, 30));
        comboMonths.setBackground(Color.WHITE);
        comboMonths.addActionListener(e -> filterMovementsBySelectedMonth());
        filterRight.add(comboMonths);

        JButton btnExport = new JButton("Exportar Excel");
        btnExport.setIcon(new com.openbravo.pos.util.ModernActionIcon(
                com.openbravo.pos.util.ModernActionIcon.Type.EXPORT, 18, Color.WHITE));
        btnExport.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnExport.setBackground(new Color(16, 185, 129)); // Excel green
        btnExport.setForeground(Color.WHITE);
        btnExport.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(16, 185, 129), 1),
            BorderFactory.createEmptyBorder(5, 14, 5, 14)));
        btnExport.setFocusPainted(false);
        btnExport.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnExport.addActionListener(e -> exportToExcel());
        filterRight.add(btnExport);

        JButton btnRefresh = new JButton("Actualizar");
        btnRefresh.setIcon(new com.openbravo.pos.util.ModernActionIcon(
                com.openbravo.pos.util.ModernActionIcon.Type.REFRESH, 18, new Color(7, 55, 43)));
        btnRefresh.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnRefresh.setBackground(Color.WHITE);
        btnRefresh.setForeground(SLATE_600);
        btnRefresh.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(SLATE_200, 1),
            BorderFactory.createEmptyBorder(5, 14, 5, 14)));
        btnRefresh.setFocusPainted(false);
        btnRefresh.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnRefresh.addActionListener(e -> loadMovementsList());
        filterRight.add(btnRefresh);
        filterRow.add(filterRight, BorderLayout.EAST);

        north.add(filterRow);
        north.add(Box.createVerticalStrut(10));
        m_listPanel.add(north, BorderLayout.NORTH);

        // ── 3. Table ──
        tableModel = new DefaultTableModel(new Object[]{"Fecha", "Tipo", "Motivo / Notas", "Total", ""}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        movementsTable = new JTable(tableModel);
        movementsTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        movementsTable.setRowHeight(38);
        movementsTable.setShowHorizontalLines(true);
        movementsTable.setShowVerticalLines(false);
        movementsTable.setGridColor(SLATE_200);
        movementsTable.setSelectionBackground(BLUE_SEL);
        movementsTable.setSelectionForeground(SLATE_800);
        movementsTable.setFillsViewportHeight(true);
        movementsTable.setIntercellSpacing(new Dimension(0, 1));
        movementsTable.setBackground(Color.WHITE);

        movementsTable.setTableHeader(null);

        setupRenderers();

        movementsTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int row = movementsTable.rowAtPoint(e.getPoint());
                int col = movementsTable.columnAtPoint(e.getPoint());
                if (row >= 0 && col == 4) {
                    showRowActionsMenu(row, e.getX(), e.getY());
                }
            }
        });

        JScrollPane scroll = new JScrollPane(movementsTable);
        scroll.setBorder(BorderFactory.createLineBorder(SLATE_200));
        scroll.getViewport().setBackground(Color.WHITE);

        // Wrap table in a rounded white panel
        RoundedPanel tableWrapper = new RoundedPanel(12, Color.WHITE);
        tableWrapper.setLayout(new BorderLayout());
        tableWrapper.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        tableWrapper.add(scroll, BorderLayout.CENTER);
        m_listPanel.add(tableWrapper, BorderLayout.CENTER);

        // Bottom delete panel is removed, row-level options used instead.
    }

    private void showNewMovementDialog() {
        try {
            bd.actionInsert();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        Window parentWindow = SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(parentWindow, "Nuevo Movimiento de Caja", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setLayout(new BorderLayout());
        dialog.setBackground(Color.WHITE);

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 24, 10, 24));
        
        JLabel headerTitle = new JLabel("Registrar Movimiento");
        headerTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        headerTitle.setForeground(SLATE_800);
        headerPanel.add(headerTitle, BorderLayout.WEST);
        dialog.add(headerPanel, BorderLayout.NORTH);

        Component editorComp = jeditor.getComponent();
        dialog.add(editorComp, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        btnPanel.setBackground(Color.WHITE);
        btnPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, SLATE_200));

        JButton btnCancel = new JButton("Cancelar");
        btnCancel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnCancel.setBackground(SLATE_50);
        btnCancel.setForeground(SLATE_600);
        btnCancel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(SLATE_200, 1),
            BorderFactory.createEmptyBorder(8, 18, 8, 18)
        ));
        btnCancel.setFocusPainted(false);
        btnCancel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnCancel.addActionListener(e -> {
            try {
                bd.actionLoad();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            dialog.dispose();
        });

        JButton btnSave = new JButton("Guardar Movimiento");
        btnSave.setIcon(new com.openbravo.pos.util.ModernActionIcon(
                com.openbravo.pos.util.ModernActionIcon.Type.SAVE, 18, Color.WHITE));
        btnSave.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnSave.setBackground(new Color(30, 41, 59));
        btnSave.setForeground(Color.WHITE);
        btnSave.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(30, 41, 59), 1),
            BorderFactory.createEmptyBorder(8, 18, 8, 18)
        ));
        btnSave.setFocusPainted(false);
        btnSave.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnSave.addActionListener(e -> {
            try {
                Double val = null;
                Object valueObj = jeditor.createValue();
                if (valueObj instanceof Object[]) {
                    val = (Double) ((Object[]) valueObj)[5];
                }
                if (val == null || val == 0.0) {
                    JOptionPane.showMessageDialog(dialog, "Por favor, introduzca una cantidad válida.", "Validación", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                
                bd.saveData();
                try {
                    if (valueObj instanceof Object[]) {
                        Object[] payment = (Object[]) valueObj;
                        String payId = (String) payment[3];
                        String rKey = (String) payment[4];
                        Double totalSigned = (Double) payment[5];
                        String notes = (String) payment[6];
                        boolean isCashIn = "cashin".equalsIgnoreCase(rKey);

                        com.openbravo.pos.sync.VoltiumSyncService.ExpensePayload exp = new com.openbravo.pos.sync.VoltiumSyncService.ExpensePayload();
                        exp.id = "cash_" + payId;
                        exp.agencyId = com.openbravo.pos.sync.VoltiumSyncService.getAgencyId();
                        exp.fecha = com.openbravo.pos.sync.VoltiumSyncService.formatIsoUtc(new Date());
                        exp.monto = Math.abs(totalSigned != null ? totalSigned : 0.0);
                        exp.tipo = isCashIn ? "ingreso" : "egreso";
                        exp.categoria = "Caja Menor";
                        exp.concepto = (isCashIn ? "Entrada de caja: " : "Salida de caja: ") + (notes != null && !notes.trim().isEmpty() ? notes : (isCashIn ? "Entrada Efectivo" : "Salida Efectivo"));
                        exp.metodoPago = "Efectivo";
                        exp.responsable = app.getAppUserView().getUser().getName();
                        exp.notas = notes;
                        exp.referencia = "CASH-" + payId;
                        com.openbravo.pos.sync.VoltiumSyncService.sincronizarGastoAsync(exp);
                    }
                } catch (Exception ignored) {}
                dialog.dispose();
                loadMovementsList();
            } catch (BasicException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(dialog, "Error al guardar el movimiento: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnPanel.add(btnCancel);
        btnPanel.add(btnSave);
        dialog.add(btnPanel, BorderLayout.SOUTH);

        dialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                try {
                    bd.actionLoad();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        dialog.pack();
        dialog.setResizable(false);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    // =====================================================================
    // Summary Cards (Rounded, dark)
    // =====================================================================
    private JPanel buildBalanceCard() {
        RoundedPanel card = new RoundedPanel(CARD_RADIUS, CARD_DARK);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));

        JLabel title = makeCardTitle("Saldo Mensual");
        card.add(title);
        card.add(Box.createVerticalStrut(4));

        lblBalanceAmount = new JLabel("$0.00");
        lblBalanceAmount.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblBalanceAmount.setForeground(Color.WHITE);
        lblBalanceAmount.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(lblBalanceAmount);
        card.add(Box.createVerticalStrut(4));

        lblBalanceDelta = new JLabel("");
        lblBalanceDelta.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblBalanceDelta.setForeground(GREEN);
        lblBalanceDelta.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(lblBalanceDelta);

        return card;
    }

    private JPanel buildEntradasCard() {
        RoundedPanel card = new RoundedPanel(CARD_RADIUS, CARD_DARK2);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));

        card.add(makeCardTitle("Entradas Totales"));
        card.add(Box.createVerticalStrut(4));

        lblEntradasAmount = new JLabel("$0.00");
        lblEntradasAmount.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblEntradasAmount.setForeground(GREEN);
        lblEntradasAmount.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(lblEntradasAmount);

        return card;
    }

    private JPanel buildSalidasCard() {
        RoundedPanel card = new RoundedPanel(CARD_RADIUS, CARD_DARK2);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));

        card.add(makeCardTitle("Salidas Totales"));
        card.add(Box.createVerticalStrut(4));

        lblSalidasAmount = new JLabel("$0.00");
        lblSalidasAmount.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblSalidasAmount.setForeground(RED);
        lblSalidasAmount.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(lblSalidasAmount);

        return card;
    }

    private JLabel makeCardTitle(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lbl.setForeground(SLATE_400);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    private JLabel makeBreakdownLine(String concept, String amount) {
        JLabel lbl = new JLabel("\u2022 " + concept + "  " + amount);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lbl.setForeground(SLATE_400);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    // =====================================================================
    // Table Renderers
    // =====================================================================
    private void setupRenderers() {
        final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");

        // Column 0 — Fecha
        movementsTable.getColumnModel().getColumn(0).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                setHorizontalAlignment(CENTER);
                setFont(new Font("Segoe UI", Font.PLAIN, 13));
                setForeground(SLATE_800);
                if (v instanceof Date) setText(sdf.format((Date) v));
                if (!sel) setBackground(r % 2 == 0 ? Color.WHITE : new Color(250, 251, 252));
                setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
                return this;
            }
        });

        // Column 1 — Tipo (pill badge)
        movementsTable.getColumnModel().getColumn(1).setCellRenderer(new PillBadgeRenderer());

        // Column 2 — Motivo / Notas
        movementsTable.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                setHorizontalAlignment(LEFT);
                setFont(new Font("Segoe UI", Font.PLAIN, 13));
                setForeground(SLATE_600);
                if (!sel) {
                    setBackground(r % 2 == 0 ? Color.WHITE : new Color(250, 251, 252));
                }
                setBorder(BorderFactory.createEmptyBorder(0, 14, 0, 10));
                return this;
            }
        });

        // Column 3 — Total
        movementsTable.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                setHorizontalAlignment(RIGHT);
                setFont(new Font("Segoe UI", Font.BOLD, 13));
                if (!sel) {
                    setBackground(r % 2 == 0 ? Color.WHITE : new Color(250, 251, 252));
                }

                if (v instanceof Double) {
                    double d = (Double) v;
                    setText(DF.format(Math.abs(d)));
                    setForeground(d >= 0 ? GREEN_DIM : RED_DIM);
                } else if (v instanceof String) {
                    setForeground(SLATE_800);
                }
                setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 16));
                return this;
            }
        });

        movementsTable.getColumnModel().getColumn(0).setPreferredWidth(145);
        movementsTable.getColumnModel().getColumn(1).setPreferredWidth(105);
        movementsTable.getColumnModel().getColumn(2).setPreferredWidth(350);
        movementsTable.getColumnModel().getColumn(3).setPreferredWidth(140);
        movementsTable.getColumnModel().getColumn(4).setPreferredWidth(50);
        movementsTable.getColumnModel().getColumn(4).setCellRenderer(new ActionMenuRenderer());
    }

    /** Pill-shaped badge renderer for Entrada / Salida */
    private class PillBadgeRenderer extends JPanel implements TableCellRenderer {
        private String text = "";
        private Color badgeColor = GREEN;

        PillBadgeRenderer() {
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean sel, boolean foc, int row, int col) {
            text = value != null ? value.toString() : "";

            if (sel) {
                setBackground(BLUE_SEL);
            } else {
                setBackground(row % 2 == 0 ? Color.WHITE : new Color(250, 251, 252));
            }

            if ("Entrada".equals(text)) badgeColor = GREEN;
            else if ("Salida".equals(text)) badgeColor = RED;

            return this;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (text.isEmpty()) return;

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            Font font = new Font("Segoe UI", Font.BOLD, 11);
            g2.setFont(font);
            FontMetrics fm = g2.getFontMetrics();
            int tw = fm.stringWidth(text);
            int th = fm.getHeight();

            int pw = tw + 22;  // pill width
            int ph = th + 6;   // pill height
            int px = (getWidth() - pw) / 2;
            int py = (getHeight() - ph) / 2;

            // Draw pill background
            g2.setColor(badgeColor);
            g2.fillRoundRect(px, py, pw, ph, ph, ph);

            // Draw text
            g2.setColor(Color.WHITE);
            int tx = px + (pw - tw) / 2;
            int ty = py + fm.getAscent() + (ph - th) / 2;
            g2.drawString(text, tx, ty);

            g2.dispose();
        }
    }

    // =====================================================================
    // Data Loading
    // =====================================================================
    private List<CashMovement> loadMovementsFromDb() {
        List<CashMovement> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = app.getSession().getConnection();
            ps = conn.prepareStatement(
                "SELECT p.ID as PAYMENT_ID, r.ID as RECEIPT_ID, r.DATENEW, p.PAYMENT, p.TOTAL, p.NOTES "
              + "FROM payments p JOIN receipts r ON p.RECEIPT = r.ID "
              + "WHERE p.PAYMENT IN ('cashin','cashout') ORDER BY r.DATENEW DESC");
            rs = ps.executeQuery();
            while (rs.next()) {
                CashMovement m = new CashMovement();
                m.paymentId = rs.getString("PAYMENT_ID");
                m.receiptId = rs.getString("RECEIPT_ID");
                m.date      = rs.getTimestamp("DATENEW");
                m.type      = rs.getString("PAYMENT");
                m.total     = rs.getDouble("TOTAL");
                m.notes     = rs.getString("NOTES");
                list.add(m);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error al cargar movimientos: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            if (rs != null) try { rs.close(); } catch (Exception e) {}
            if (ps != null) try { ps.close(); } catch (Exception e) {}
        }
        return list;
    }

    private void loadMovementsList() {
        MonthYearOption prev = (MonthYearOption) comboMonths.getSelectedItem();
        allMovements = loadMovementsFromDb();

        DefaultComboBoxModel<MonthYearOption> model = new DefaultComboBoxModel<>();
        Set<String> keys = new HashSet<>();
        List<MonthYearOption> opts = new ArrayList<>();

        Calendar cur = Calendar.getInstance();
        SimpleDateFormat fmt = new SimpleDateFormat("MMMM yyyy", new Locale("es", "ES"));
        String cl = fmt.format(cur.getTime());
        cl = cl.substring(0, 1).toUpperCase() + cl.substring(1);
        MonthYearOption curOpt = new MonthYearOption(cur.get(Calendar.MONTH), cur.get(Calendar.YEAR), cl);
        opts.add(curOpt);
        keys.add(cur.get(Calendar.YEAR) + "-" + cur.get(Calendar.MONTH));

        for (CashMovement m : allMovements) {
            if (m.date == null) continue;
            Calendar c = Calendar.getInstance();
            c.setTime(m.date);
            String k = c.get(Calendar.YEAR) + "-" + c.get(Calendar.MONTH);
            if (!keys.contains(k)) {
                keys.add(k);
                String l = fmt.format(m.date);
                l = l.substring(0, 1).toUpperCase() + l.substring(1);
                opts.add(new MonthYearOption(c.get(Calendar.MONTH), c.get(Calendar.YEAR), l));
            }
        }
        Collections.sort(opts, (a, b) -> a.year != b.year ? b.year - a.year : b.month - a.month);
        for (MonthYearOption o : opts) model.addElement(o);

        ActionListener[] ls = comboMonths.getActionListeners();
        for (ActionListener l : ls) comboMonths.removeActionListener(l);
        comboMonths.setModel(model);
        if (prev != null && opts.contains(prev)) comboMonths.setSelectedItem(prev);
        else comboMonths.setSelectedItem(curOpt);
        for (ActionListener l : ls) comboMonths.addActionListener(l);

        filterMovementsBySelectedMonth();
    }

    private void filterMovementsBySelectedMonth() {
        MonthYearOption sel = (MonthYearOption) comboMonths.getSelectedItem();
        if (sel == null) return;

        lblMonthTitle.setText("Mes de " + sel.label);
        tableModel.setRowCount(0);
        filteredMovements.clear();

        double totalIn = 0, totalOut = 0;

        for (CashMovement m : allMovements) {
            if (m.date == null) continue;
            Calendar cal = Calendar.getInstance();
            cal.setTime(m.date);
            if (cal.get(Calendar.MONTH) == sel.month && cal.get(Calendar.YEAR) == sel.year) {
                filteredMovements.add(m);
                String typeStr = "cashin".equals(m.type) ? "Entrada" : "Salida";
                double abs = Math.abs(m.total);
                tableModel.addRow(new Object[]{ m.date, typeStr, m.notes == null ? "" : m.notes, m.total, "" });

                if ("cashin".equals(m.type)) { totalIn += abs; }
                else { totalOut += abs; }
            }
        }


        // ── Update summary cards ──
        double balance = totalIn - totalOut;
        lblBalanceAmount.setText(DF.format(Math.abs(balance)));
        if (balance >= 0) {
            lblBalanceDelta.setText("\u25B2  +" + DF.format(balance));
            lblBalanceDelta.setForeground(GREEN);
        } else {
            lblBalanceDelta.setText("\u25BC  -" + DF.format(Math.abs(balance)));
            lblBalanceDelta.setForeground(RED);
        }

        lblEntradasAmount.setText(DF.format(totalIn));
        lblSalidasAmount.setText(DF.format(totalOut));
    }

    // =====================================================================
    // Delete
    // =====================================================================
    private void deleteSelectedMovement() {
        int row = movementsTable.getSelectedRow();
        if (row < 0 || row >= filteredMovements.size()) {
            JOptionPane.showMessageDialog(this, "Seleccione un movimiento de la lista.", "Sin selección", JOptionPane.WARNING_MESSAGE);
            return;
        }
        row = movementsTable.convertRowIndexToModel(row);
        final CashMovement m = filteredMovements.get(row);
        int res = JOptionPane.showConfirmDialog(this,
            "¿Eliminar este movimiento?\nEsta acción no se puede deshacer.", "Confirmar", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (res == JOptionPane.YES_OPTION) {
            Connection conn = null;
            PreparedStatement p1 = null, p2 = null;
            try {
                conn = app.getSession().getConnection();
                conn.setAutoCommit(false);
                p1 = conn.prepareStatement("DELETE FROM payments WHERE ID = ?");
                p1.setString(1, m.paymentId); p1.executeUpdate();
                p2 = conn.prepareStatement("DELETE FROM receipts WHERE ID = ?");
                p2.setString(1, m.receiptId); p2.executeUpdate();
                conn.commit();
                String tn = "cashin".equals(m.type) ? "Entrada" : "Salida";
                saveAuditLog("Eliminar", "Pagos", m.paymentId, "Movimiento",
                    "Tipo: " + tn + ", Concepto: " + (m.notes != null ? m.notes : "") + ", Total: " + m.total);
                JOptionPane.showMessageDialog(this, "Movimiento eliminado.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                loadMovementsList();
            } catch (SQLException ex) {
                if (conn != null) try { conn.rollback(); } catch (SQLException x) {}
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            } finally {
                if (p1 != null) try { p1.close(); } catch (Exception e) {}
                if (p2 != null) try { p2.close(); } catch (Exception e) {}
            }
        }
    }

    // =====================================================================
    // JPanelTable overrides
    // =====================================================================
    @Override public ListProvider getListProvider() { return null; }
    @Override public DefaultSaveProvider getSaveProvider() {
        return new DefaultSaveProvider(null, m_dlSales.getPaymentMovementInsert(), m_dlSales.getPaymentMovementDelete());
    }
    @Override public EditorRecord getEditor() { return jeditor; }
    @Override public String getTitle() { return AppLocal.getIntString("Menu.Payments"); }

    // =====================================================================
    // Rounded Panel component
    // =====================================================================
    private static class RoundedPanel extends JPanel {
        private final int radius;
        private Color bg;

        RoundedPanel(int radius, Color bg) {
            super();
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
            g2.dispose();
            super.paintComponent(g);
        }
    }

    // =====================================================================
    private class ActionMenuRenderer extends JPanel implements TableCellRenderer {
        ActionMenuRenderer() {
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
            if (sel) {
                setBackground(BLUE_SEL);
            } else {
                setBackground(r % 2 == 0 ? Color.WHITE : new Color(250, 251, 252));
            }
            return this;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            g2.setColor(SLATE_500);
            int cx = getWidth() / 2;
            int cy = getHeight() / 2;
            int dotSize = 4;
            
            g2.fillOval(cx - 2, cy - 8, dotSize, dotSize);
            g2.fillOval(cx - 2, cy - 2, dotSize, dotSize);
            g2.fillOval(cx - 2, cy + 4, dotSize, dotSize);
            
            g2.dispose();
        }
    }

    private void showRowActionsMenu(final int row, int x, int y) {
        int modelRow = movementsTable.convertRowIndexToModel(row);
        if (modelRow < 0 || modelRow >= filteredMovements.size()) return;
        final CashMovement m = filteredMovements.get(modelRow);

        JPopupMenu menu = new JPopupMenu();
        menu.setBackground(Color.WHITE);
        menu.setBorder(BorderFactory.createLineBorder(SLATE_200, 1));

        JMenuItem itemEdit = new JMenuItem("Editar Movimiento");
        itemEdit.setFont(new Font("Segoe UI", Font.BOLD, 12));
        itemEdit.setForeground(SLATE_800);
        itemEdit.setBackground(Color.WHITE);
        itemEdit.addActionListener(e -> showEditMovementDialog(m));
        
        JMenuItem itemDelete = new JMenuItem("Eliminar Movimiento");
        itemDelete.setFont(new Font("Segoe UI", Font.BOLD, 12));
        itemDelete.setForeground(RED_DIM);
        itemDelete.setBackground(Color.WHITE);
        itemDelete.addActionListener(e -> deleteMovement(m));

        menu.add(itemEdit);
        menu.add(itemDelete);
        
        menu.show(movementsTable, x, y);
    }

    private void deleteMovement(final CashMovement m) {
        int res = JOptionPane.showConfirmDialog(this,
            "¿Eliminar este movimiento?\nEsta acción no se puede deshacer.", "Confirmar", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (res == JOptionPane.YES_OPTION) {
            Connection conn = null;
            PreparedStatement p1 = null, p2 = null;
            try {
                conn = app.getSession().getConnection();
                conn.setAutoCommit(false);
                p1 = conn.prepareStatement("DELETE FROM payments WHERE ID = ?");
                p1.setString(1, m.paymentId); p1.executeUpdate();
                p2 = conn.prepareStatement("DELETE FROM receipts WHERE ID = ?");
                p2.setString(1, m.receiptId); p2.executeUpdate();
                conn.commit();
                try {
                    com.openbravo.pos.sync.VoltiumSyncService.eliminarGastoRemotoAsync("cash_" + m.paymentId);
                } catch (Exception ignored) {}
                String tn = "cashin".equals(m.type) ? "Entrada" : "Salida";
                saveAuditLog("Eliminar", "Pagos", m.paymentId, "Movimiento",
                    "Tipo: " + tn + ", Concepto: " + (m.notes != null ? m.notes : "") + ", Total: " + m.total);
                JOptionPane.showMessageDialog(this, "Movimiento eliminado.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                loadMovementsList();
            } catch (SQLException ex) {
                if (conn != null) try { conn.rollback(); } catch (SQLException x) {}
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            } finally {
                if (p1 != null) try { p1.close(); } catch (Exception e) {}
                if (p2 != null) try { p2.close(); } catch (Exception e) {}
            }
        }
    }

    private void showEditMovementDialog(final CashMovement m) {
        Window parentWindow = SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(parentWindow, "Editar Movimiento de Caja", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setLayout(new BorderLayout());
        dialog.setBackground(Color.WHITE);

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 24, 10, 24));
        
        JLabel headerTitle = new JLabel("Editar Movimiento");
        headerTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        headerTitle.setForeground(SLATE_800);
        headerPanel.add(headerTitle, BorderLayout.WEST);
        dialog.add(headerPanel, BorderLayout.NORTH);

        jeditor.setMovementData(m.type, m.total, m.notes);
        jeditor.setFieldsEnabled(true);

        Component editorComp = jeditor.getComponent();
        dialog.add(editorComp, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        btnPanel.setBackground(Color.WHITE);
        btnPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, SLATE_200));

        JButton btnCancel = new JButton("Cancelar");
        btnCancel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnCancel.setBackground(SLATE_50);
        btnCancel.setForeground(SLATE_600);
        btnCancel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(SLATE_200, 1),
            BorderFactory.createEmptyBorder(8, 18, 8, 18)
        ));
        btnCancel.setFocusPainted(false);
        btnCancel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnCancel.addActionListener(e -> {
            try {
                bd.actionLoad();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            dialog.dispose();
        });

        JButton btnSave = new JButton("Guardar Cambios");
        btnSave.setIcon(new com.openbravo.pos.util.ModernActionIcon(
                com.openbravo.pos.util.ModernActionIcon.Type.SAVE, 18, Color.WHITE));
        btnSave.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnSave.setBackground(new Color(30, 41, 59));
        btnSave.setForeground(Color.WHITE);
        btnSave.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(30, 41, 59), 1),
            BorderFactory.createEmptyBorder(8, 18, 8, 18)
        ));
        btnSave.setFocusPainted(false);
        btnSave.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnSave.addActionListener(e -> {
            Double val = jeditor.getAmountValue();
            if (val == null || val == 0.0) {
                JOptionPane.showMessageDialog(dialog, "Por favor, introduzca una cantidad válida.", "Validación", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            Connection conn = null;
            PreparedStatement ps = null;
            try {
                conn = app.getSession().getConnection();
                ps = conn.prepareStatement("UPDATE payments SET PAYMENT = ?, TOTAL = ?, NOTES = ? WHERE ID = ?");
                ps.setString(1, jeditor.getReasonKey());
                ps.setDouble(2, jeditor.getSignedTotal());
                ps.setString(3, jeditor.getNotes());
                ps.setString(4, m.paymentId);
                ps.executeUpdate();
                
                try {
                    boolean isCashIn = "cashin".equalsIgnoreCase(jeditor.getReasonKey());
                    Double amt = jeditor.getSignedTotal();
                    String notes = jeditor.getNotes();
                    com.openbravo.pos.sync.VoltiumSyncService.ExpensePayload exp = new com.openbravo.pos.sync.VoltiumSyncService.ExpensePayload();
                    exp.id = "cash_" + m.paymentId;
                    exp.agencyId = com.openbravo.pos.sync.VoltiumSyncService.getAgencyId();
                    exp.fecha = com.openbravo.pos.sync.VoltiumSyncService.formatIsoUtc(new Date());
                    exp.monto = Math.abs(amt != null ? amt : 0.0);
                    exp.tipo = isCashIn ? "ingreso" : "egreso";
                    exp.categoria = "Caja Menor";
                    exp.concepto = (isCashIn ? "Entrada de caja: " : "Salida de caja: ") + (notes != null && !notes.trim().isEmpty() ? notes : (isCashIn ? "Entrada Efectivo" : "Salida Efectivo"));
                    exp.metodoPago = "Efectivo";
                    exp.responsable = app.getAppUserView().getUser().getName();
                    exp.notas = notes;
                    exp.referencia = "CASH-" + m.paymentId;
                    com.openbravo.pos.sync.VoltiumSyncService.sincronizarGastoAsync(exp);
                } catch (Exception ignored) {}

                String tn = "cashin".equals(jeditor.getReasonKey()) ? "Entrada" : "Salida";
                saveAuditLog("Editar", "Pagos", m.paymentId, "Movimiento",
                    "Tipo: " + tn + ", Concepto: " + (jeditor.getNotes() != null ? jeditor.getNotes() : "") + ", Total: " + jeditor.getSignedTotal());
                
                bd.actionLoad();
                dialog.dispose();
                loadMovementsList();
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(dialog, "Error al guardar los cambios: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            } finally {
                if (ps != null) try { ps.close(); } catch (Exception ex) {}
            }
        });

        btnPanel.add(btnCancel);
        btnPanel.add(btnSave);
        dialog.add(btnPanel, BorderLayout.SOUTH);

        dialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                try {
                    bd.actionLoad();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        dialog.pack();
        dialog.setResizable(false);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void exportToExcel() {
        if (filteredMovements == null || filteredMovements.isEmpty()) {
            javax.swing.JOptionPane.showMessageDialog(this, "No hay datos para exportar.", "Exportar Excel", javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }

        javax.swing.JFileChooser fileChooser = new javax.swing.JFileChooser();
        fileChooser.setDialogTitle("Guardar como Excel");
        
        MonthYearOption sel = (MonthYearOption) comboMonths.getSelectedItem();
        String filename = "movimientos_caja";
        if (sel != null) {
            filename = "movimientos_" + sel.label.replace(" ", "_").toLowerCase();
        }
        fileChooser.setSelectedFile(new java.io.File(filename + ".xls"));
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Archivos de Excel (*.xls)", "xls"));

        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection == javax.swing.JFileChooser.APPROVE_OPTION) {
            java.io.File fileToSave = fileChooser.getSelectedFile();
            String path = fileToSave.getAbsolutePath();
            if (!path.toLowerCase().endsWith(".xls")) {
                path += ".xls";
                fileToSave = new java.io.File(path);
            }

            try (org.apache.poi.hssf.usermodel.HSSFWorkbook workbook = new org.apache.poi.hssf.usermodel.HSSFWorkbook()) {
                org.apache.poi.hssf.usermodel.HSSFSheet sheet = workbook.createSheet("Movimientos");
                org.apache.poi.hssf.usermodel.HSSFCellStyle headerStyle = workbook.createCellStyle();
                org.apache.poi.hssf.usermodel.HSSFFont font = workbook.createFont();
                font.setBold(true);
                headerStyle.setFont(font);

                org.apache.poi.hssf.usermodel.HSSFCellStyle dateStyle = workbook.createCellStyle();
                org.apache.poi.hssf.usermodel.HSSFDataFormat format = workbook.createDataFormat();
                dateStyle.setDataFormat(format.getFormat("dd/mm/yyyy hh:mm"));

                org.apache.poi.hssf.usermodel.HSSFCellStyle currencyStyle = workbook.createCellStyle();
                currencyStyle.setDataFormat(format.getFormat("$#,##0.00"));

                String[] headers = {
                    "Fecha", "Tipo", "Motivo / Notas", "Monto"
                };

                org.apache.poi.hssf.usermodel.HSSFRow headerRow = sheet.createRow(0);
                for (int i = 0; i < headers.length; i++) {
                    org.apache.poi.hssf.usermodel.HSSFCell cell = headerRow.createCell(i);
                    cell.setCellValue(headers[i]);
                    cell.setCellStyle(headerStyle);
                }

                for (int i = 0; i < filteredMovements.size(); i++) {
                    CashMovement m = filteredMovements.get(i);
                    org.apache.poi.hssf.usermodel.HSSFRow row = sheet.createRow(i + 1);

                    org.apache.poi.hssf.usermodel.HSSFCell dateCell = row.createCell(0);
                    if (m.date != null) {
                        dateCell.setCellValue(m.date);
                        dateCell.setCellStyle(dateStyle);
                    } else {
                        dateCell.setCellValue("");
                    }

                    String typeStr = "cashin".equals(m.type) ? "Entrada" : "Salida";
                    row.createCell(1).setCellValue(typeStr);

                    row.createCell(2).setCellValue(m.notes == null ? "" : m.notes);

                    org.apache.poi.hssf.usermodel.HSSFCell totalCell = row.createCell(3);
                    totalCell.setCellValue(m.total);
                    totalCell.setCellStyle(currencyStyle);
                }

                for (int i = 0; i < headers.length; i++) {
                    sheet.autoSizeColumn(i);
                }

                try (java.io.FileOutputStream fileOut = new java.io.FileOutputStream(fileToSave)) {
                    workbook.write(fileOut);
                }

                javax.swing.JOptionPane.showMessageDialog(this, "Movimientos exportados con éxito.", "Exportar Excel", javax.swing.JOptionPane.INFORMATION_MESSAGE);

            } catch (Exception e) {
                java.util.logging.Logger.getLogger(JPanelPayments.class.getName()).log(java.util.logging.Level.SEVERE, "Error exporting movements to Excel", e);
                javax.swing.JOptionPane.showMessageDialog(this, "Error al exportar a Excel: " + e.getMessage(), "Exportar Excel", javax.swing.JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // Inner data classes
    // =====================================================================
    private static class MonthYearOption {
        int month, year; String label;
        MonthYearOption(int m, int y, String l) { month = m; year = y; label = l; }
        @Override public String toString() { return label; }
        @Override public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof MonthYearOption)) return false;
            MonthYearOption t = (MonthYearOption) o;
            return month == t.month && year == t.year;
        }
        @Override public int hashCode() { return Objects.hash(month, year); }
    }

    private static class CashMovement {
        String paymentId, receiptId, type, notes;
        Date date;
        double total;
    }
}

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
package com.openbravo.pos.forms;

import com.openbravo.pos.menu.JRootMenu;
import com.openbravo.basic.BasicException;
import com.openbravo.data.gui.JMessageDialog;
import com.openbravo.data.gui.MessageInf;
import com.openbravo.format.Formats;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;

/**
 *
 * @author adrianromero
 */
public class JPrincipalApp extends JPanel implements AppUserView {

    private static final Logger LOGGER = Logger.getLogger(JPrincipalApp.class.getName());
    private static final long serialVersionUID = 1L;
    private static final String TASK_SYSTEM_OVERVIEW = "com.openbravo.pos.forms.JPanelSystemOverview";
    private static final java.awt.Color COLOR_NAVY = new java.awt.Color(14, 30, 45);
    private static final java.awt.Color COLOR_NAVY_SOFT = new java.awt.Color(30, 59, 86);
    private static final java.awt.Color COLOR_CANVAS = new java.awt.Color(241, 245, 249);
    private static final java.awt.Color COLOR_OVERVIEW_CANVAS = new java.awt.Color(39, 39, 39);
    private static final java.awt.Color COLOR_HEADER_BLACK = new java.awt.Color(8, 8, 8);
    private static final java.awt.Color COLOR_BRAND_ORANGE = new java.awt.Color(202, 159, 65); // Hex #CA9F41
    private static final java.awt.Color COLOR_BRAND_ORANGE_DARK = new java.awt.Color(176, 137, 51); // Hex #B08933
    private static final java.awt.Color COLOR_SURFACE = java.awt.Color.WHITE;
    private static final java.awt.Color COLOR_SURFACE_SOFT = new java.awt.Color(247, 249, 251);
    private static final java.awt.Color COLOR_LINE = new java.awt.Color(214, 222, 230);
    private static final java.awt.Color COLOR_TEXT = new java.awt.Color(36, 48, 63);
    private static final java.awt.Color COLOR_TEXT_MUTED = new java.awt.Color(110, 122, 138);
    private static final java.awt.Color COLOR_SHORTCUT = new java.awt.Color(231, 238, 246);
    private static final java.awt.Color COLOR_DANGER = new java.awt.Color(178, 34, 52);

    private final JRootApp m_appview;
    private final AppUser m_appuser;
    private final DataLogicSystem m_dlSystem;
    private final JLabel m_principalnotificator;

    private Icon menu_open;
    private Icon menu_close;

    private final JRootMenu rMenu;

    // Referencias a botones principales para atajos de teclado
    private javax.swing.JButton btnVentasRef;
    private javax.swing.JButton btnCierreRef;
    private javax.swing.JButton btnInventarioRef;
    private javax.swing.JButton btnReportesRef;

    private javax.swing.JPanel m_jPanelNotificationSidebar;
    private javax.swing.JPanel m_notificationsListPanel;
    private javax.swing.JLabel m_lblNotificationTitle;
    private javax.swing.JButton m_btnNotification;
    private int m_notificationCount = 0;
    private javax.swing.JPanel profilePanelRef;
    private javax.swing.JPanel overviewRibbonPanel;
    private final Map<String, javax.swing.JButton> navigationButtons = new LinkedHashMap<>();
    private javax.swing.JButton activeNavigationButton;

    /**
     * Creates a JPanel
     *
     * @param appview
     * @param appuser
     */
    public JPrincipalApp(JRootApp appview, AppUser appuser) {

        m_appview = appview;
        m_appuser = appuser;

        m_dlSystem = (DataLogicSystem) m_appview.getBean("com.openbravo.pos.forms.DataLogicSystem");

        // IMPORTANTE: Inicializar roles predeterminados ANTES de cargar permisos del
        // usuario
        try {
            System.out.println("=== INICIALIZANDO ROLES AL INICIO DE LA APP ===");
            com.openbravo.pos.admin.DataLogicAdmin dlAdmin = (com.openbravo.pos.admin.DataLogicAdmin) m_appview
                    .getBean("com.openbravo.pos.admin.DataLogicAdmin");
            com.openbravo.pos.admin.DefaultRolesInitializer.initializeDefaultRoles(dlAdmin.getSession());
            System.out.println("=== ROLES INICIALIZADOS CORRECTAMENTE ===");
        } catch (Exception ex) {
            System.err.println("ERROR al inicializar roles: " + ex.getMessage());
            ex.printStackTrace();
        }

        AppUserPermissionsLoader aupLoader = new AppUserPermissionsLoader(m_dlSystem);

        // Convertir ID de rol a nombre de rol para compatibilidad
        String roleName = mapRoleIdToName(m_appuser.getRole());
        System.out.println(
                "Usuario: " + m_appuser.getName() + " | Rol ID: " + m_appuser.getRole() + " | Rol Nombre: " + roleName);

        Set<String> userPermissions = aupLoader.getPermissionsForRole(roleName);

        // Sebastian - TEMPORAL: Agregar permiso de gráficos manualmente hasta que se
        // arregle el BLOB
        if ("ADMIN".equals(roleName) || "1".equals(m_appuser.getRole())) {
            userPermissions.add("com.openbravo.pos.reports.JPanelGraphics");
        }
        userPermissions.add(TASK_SYSTEM_OVERVIEW);

        m_appuser.fillPermissions(userPermissions);

        initComponents();
        applyComponentOrientation(m_appview.getComponentOrientation());

        m_principalnotificator = new JLabel();
        m_principalnotificator.applyComponentOrientation(getComponentOrientation());
        m_principalnotificator.setText(m_appuser.getName());
        // Sebastian - Sin icono en el perfil, solo texto
        m_principalnotificator.setIcon(null);

        // Sebastian - Configurar estilo del perfil para el panel superior (reducido
        // para vertical)
        m_principalnotificator.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12));
        m_principalnotificator.setForeground(COLOR_SURFACE);
        m_principalnotificator.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        m_principalnotificator.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);

        // Agregar el perfil al panel superior (se inicializa en initComponents)
        javax.swing.SwingUtilities.invokeLater(() -> {
            if (profilePanelRef != null) {
                profilePanelRef.removeAll();
                profilePanelRef.add(m_principalnotificator);
                profilePanelRef.revalidate();
                profilePanelRef.repaint();
            }
        });

        // MENU SIDE
        colapseHPanel.add(Box.createVerticalStrut(50), 0);
        m_jPanelMenu.getVerticalScrollBar().setPreferredSize(new Dimension(35, 35));
        rMenu = new JRootMenu(this, this);
        rMenu.setRootMenu(m_jPanelMenu, m_dlSystem);
        rMenu.getViewManager().getPreparedViews().put(TASK_SYSTEM_OVERVIEW, new JPanelSystemOverview(this, m_dlSystem));
        setMenuIcon();
        assignMenuButtonIcon();

        // MAIN
        m_jPanelTitle.setVisible(false);
        m_jPanelTitle.setPreferredSize(new java.awt.Dimension(0, 0)); // Sin tamaño cuando está oculto
        m_jPanelTitle.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, 0)); // Sin altura cuando está oculto
        addView(new JPanel(), "<NULL>");
        showView("<NULL>");

        // Configurar atajos de teclado globales después de inicializar todo
        setupGlobalKeyboardShortcuts();

    }

    private void setMenuIcon() {
        if (colapseButton.getComponentOrientation().isLeftToRight()) {
            menu_open = new ImageIcon(getClass().getResource(
                    "/com/openbravo/images/menu-right.png"));
            menu_close = new ImageIcon(getClass().getResource(
                    "/com/openbravo/images/menu-left.png"));
        } else {
            menu_open = new ImageIcon(getClass().getResource(
                    "/com/openbravo/images/menu-left.png"));
            menu_close = new ImageIcon(getClass().getResource(
                    "/com/openbravo/images/menu-right.png"));
        }
    }

    private void assignMenuButtonIcon() {
        colapseButton.setIcon(m_jPanelMenu.isVisible() ? menu_close : menu_open);
    }

    private void setMenuVisible(boolean value) {

        m_jPanelMenu.setVisible(value);
        assignMenuButtonIcon();
        revalidate();
    }

    public JComponent getNotificator() {
        return m_principalnotificator;
    }

    public void activate() {

        // Sebastian - Mantener el menú lateral siempre oculto para diseño tipo eleventa
        setMenuVisible(false);
        showTask(TASK_SYSTEM_OVERVIEW);
        refreshNotifications();

        // Sebastian - Refrescar el logo cuando se active el panel (por si cambió en
        // configuración)
        try {
            // Buscar el logoPanel en el componente
            javax.swing.JPanel artisticPanel = findArtisticTopPanel(this);
            if (artisticPanel != null) {
                for (java.awt.Component comp : artisticPanel.getComponents()) {
                    if (comp instanceof javax.swing.JPanel) {
                        javax.swing.JPanel logoPanel = (javax.swing.JPanel) comp;
                        @SuppressWarnings("unchecked")
                        java.util.function.Consumer<String> updateLogo = (java.util.function.Consumer<String>) logoPanel
                                .getClientProperty("updateLogo");
                        if (updateLogo != null) {
                            // Recargar la configuración y actualizar el logo
                            com.openbravo.pos.forms.AppConfig appConfig = com.openbravo.pos.forms.AppConfig
                                    .getInstance();
                            appConfig.load();
                            String logoPath = appConfig.getProperty("start.logo");
                            updateLogo.accept(logoPath);
                            logoPanel.revalidate();
                            logoPanel.repaint();
                            break;
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error al refrescar el logo", e);
        }
    }

    // Sebastian - Método helper para encontrar el artisticTopPanel
    private javax.swing.JPanel findArtisticTopPanel(java.awt.Container container) {
        for (java.awt.Component comp : container.getComponents()) {
            if (comp instanceof javax.swing.JPanel) {
                javax.swing.JPanel panel = (javax.swing.JPanel) comp;
                // Verificar si es el artisticTopPanel buscando el logoPanel dentro
                for (java.awt.Component child : panel.getComponents()) {
                    if (child instanceof javax.swing.JPanel) {
                        javax.swing.JPanel childPanel = (javax.swing.JPanel) child;
                        if (childPanel.getClientProperty("logoLabel") != null) {
                            return panel; // Este es el artisticTopPanel
                        }
                    }
                }
                // Buscar recursivamente
                javax.swing.JPanel found = findArtisticTopPanel(panel);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    public boolean deactivate() {
        if (rMenu.getViewManager().deactivateLastView()) {
            showView("<NULL>");
            return true;
        } else {
            return false;
        }

    }

    @Override
    public void exitToLogin() {
        // Sebastian - Nuevo diálogo de opciones de salida estilo eleventa
        java.awt.Window parentWindow = SwingUtilities.getWindowAncestor(this);
        java.awt.Frame parentFrame = null;
        if (parentWindow instanceof java.awt.Frame) {
            parentFrame = (java.awt.Frame) parentWindow;
        } else if (parentWindow instanceof java.awt.Dialog) {
            parentFrame = (java.awt.Frame) ((java.awt.Dialog) parentWindow).getParent();
        }

        // Si hay turno abierto, mostrar opciones personalizadas
        if (m_appview.getActiveCashDateEnd() == null && m_appview.getActiveCashIndex() != null) {
            JDialogExitOptions exitOptions = new JDialogExitOptions(parentFrame, (JRootApp) m_appview);
            exitOptions.setVisible(true);

            if (exitOptions.isCloseShiftRequested()) {
                // El usuario eligió cerrar turno
                JDialogCloseShift dialog = new JDialogCloseShift(parentFrame, m_appview);
                dialog.setVisible(true);

                if (dialog.isClosed() && dialog.shouldCloseShift()) {
                    // Turno cerrado exitosamente, ahora salir
                    performFinalExit();
                }
            } else if (exitOptions.isExitOnlyRequested()) {
                // El usuario eligió salir con turno abierto
                performFinalExit();
            }
            // Si canceló (isCloseShiftRequested e isExitOnlyRequested son false), no hacer
            // nada
        } else {
            // No hay turno abierto, permitir salir normalmente
            performFinalExit();
        }
    }

    private void performFinalExit() {
        if (m_appview.closeAppView()) {
            ((JRootApp) m_appview).showLoginPanelPublic();
        }
    }

    private void addView(JComponent component, String sView) {
        m_jPanelContainer.add(component, sView);
    }

    private void showView(String sView) {
        CardLayout cl = (CardLayout) (m_jPanelContainer.getLayout());
        cl.show(m_jPanelContainer, sView);
    }

    @Override
    public AppUser getUser() {
        return m_appuser;
    }

    @Override
    public void showTask(String sTaskClass) {

        LOGGER.info("Show View for class: " + sTaskClass);
        try {
            m_appview.waitCursorBegin();

            if (m_appuser.hasPermission(sTaskClass)) {

                JPanelView viewPanel = rMenu.getViewManager().getCreatedViews().get(sTaskClass);
                if (viewPanel == null) {

                    viewPanel = rMenu.getViewManager().getPreparedViews().get(sTaskClass);

                    if (viewPanel == null) {

                        try {
                            viewPanel = (JPanelView) m_appview.getBean(sTaskClass);
                        } catch (BeanFactoryException e) {
                            LOGGER.log(Level.SEVERE, "Exception on get a JPanelView Bean for class: " + sTaskClass, e);
                            viewPanel = new JPanelNull(m_appview, e);
                        }
                    }

                    rMenu.getViewManager().getCreatedViews().put(sTaskClass, viewPanel);
                }

                if (!rMenu.getViewManager().checkIfLastView(viewPanel)) {

                    if (rMenu.getViewManager().getLastView() != null) {
                        LOGGER.info("Call 'deactivate' on class: "
                                + rMenu.getViewManager().getLastView().getClass().getName());
                        rMenu.getViewManager().getLastView().deactivate();
                    }

                    viewPanel.getComponent().applyComponentOrientation(getComponentOrientation());
                    addView(viewPanel.getComponent(), sTaskClass);

                    LOGGER.info("Call 'activate' on class: " + sTaskClass);
                    viewPanel.activate();

                    rMenu.getViewManager().setLastView(viewPanel);

                    // Sebastian - Mantener el menú lateral siempre oculto
                    setMenuVisible(false);

                    showView(sTaskClass);
                    applyContentSurface(sTaskClass);
                    setActiveNavigationTask(sTaskClass);
                    String sTitle = viewPanel.getTitle();
                    applyHeaderState(sTaskClass, sTitle);
                    if (sTitle != null && !sTitle.isBlank()) {
                        m_jPanelTitle.setVisible(true);
                        m_jPanelTitle.setPreferredSize(null); // Restaurar tamaño normal
                        m_jPanelTitle.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE)); // Restaurar
                                                                                                                    // tamaño
                                                                                                                    // máximo
                        updateBreadcrumbs(sTaskClass, sTitle);
                    } else {
                        m_jPanelTitle.setVisible(false);
                        m_jPanelTitle.setPreferredSize(new java.awt.Dimension(0, 0)); // Sin tamaño cuando está oculto
                        m_jPanelTitle.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, 0)); // Sin altura cuando
                                                                                                    // está oculto
                        m_jTitle.setText("");
                    }

                    // Sebastian - Si es la vista de ventas, asegurar que el campo de búsqueda tenga
                    // el foco
                    if (sTaskClass != null && sTaskClass.contains("JPanelTicketSales")) {
                        final JPanelView finalViewPanel = viewPanel;
                        javax.swing.SwingUtilities.invokeLater(() -> {
                            try {
                                if (finalViewPanel.getComponent() instanceof com.openbravo.pos.sales.JPanelTicket) {
                                    ((com.openbravo.pos.sales.JPanelTicket) finalViewPanel.getComponent())
                                            .setSearchFieldFocus();
                                }
                            } catch (Exception ex) {
                                LOGGER.log(Level.WARNING, "Error al establecer foco en campo de búsqueda", ex);
                            }
                        });
                    } else if (TASK_SYSTEM_OVERVIEW.equals(sTaskClass) && viewPanel instanceof JPanelSystemOverview) {
                        javax.swing.SwingUtilities.invokeLater(((JPanelSystemOverview) viewPanel)::requestSearchFocus);
                    }
                } else {
                    LOGGER.log(Level.INFO, "Already open: " + sTaskClass + ", Instance: " + viewPanel);
                    applyContentSurface(sTaskClass);
                    applyHeaderState(sTaskClass, viewPanel.getTitle());
                    setActiveNavigationTask(sTaskClass);
                    // Sebastian - Incluso si ya está abierto, asegurar que el campo tenga el foco
                    if (sTaskClass != null && sTaskClass.contains("JPanelTicketSales")) {
                        final JPanelView finalViewPanel = viewPanel;
                        javax.swing.SwingUtilities.invokeLater(() -> {
                            try {
                                if (finalViewPanel.getComponent() instanceof com.openbravo.pos.sales.JPanelTicket) {
                                    ((com.openbravo.pos.sales.JPanelTicket) finalViewPanel.getComponent())
                                            .setSearchFieldFocus();
                                }
                            } catch (Exception ex) {
                                LOGGER.log(Level.WARNING, "Error al establecer foco en campo de búsqueda", ex);
                            }
                        });
                    } else if (TASK_SYSTEM_OVERVIEW.equals(sTaskClass) && viewPanel instanceof JPanelSystemOverview) {
                        javax.swing.SwingUtilities.invokeLater(((JPanelSystemOverview) viewPanel)::requestSearchFocus);
                    }
                }
            } else {

                LOGGER.log(Level.INFO, "NO PERMISSION on call class: : " + sTaskClass);
                JMessageDialog.showMessage(this,
                        new MessageInf(MessageInf.SGN_WARNING,
                                AppLocal.getIntString("message.notpermissions"), "<html>" + sTaskClass));
            }
            m_appview.waitCursorEnd();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Exception on show class: " + sTaskClass, e);
            JMessageDialog.showMessage(this,
                    new MessageInf(MessageInf.SGN_WARNING,
                            AppLocal.getIntString("message.notactive"), e));
        }
    }

    private void applyContentSurface(String taskClass) {
        boolean overviewMode = TASK_SYSTEM_OVERVIEW.equals(taskClass);
        boolean isConfig = "com.openbravo.pos.config.JPanelConfiguration".equals(taskClass);

        boolean isMenu = false;
        if (rMenu != null && rMenu.getViewManager() != null && rMenu.getViewManager().getCreatedViews() != null) {
            JPanelView viewPanel = rMenu.getViewManager().getCreatedViews().get(taskClass);
            if (viewPanel instanceof com.openbravo.pos.menu.JPanelMenu) {
                isMenu = true;
            }
        }

        if (overviewMode || isMenu || isConfig) {
            m_jPanelContainer.setBackground(COLOR_OVERVIEW_CANVAS);
            m_jPanelContainer.setBorder(javax.swing.BorderFactory.createEmptyBorder());
        } else if (taskClass != null && taskClass.contains("JPanelTicketSales")) {
            m_jPanelContainer.setBackground(COLOR_CANVAS);
            m_jPanelContainer.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        } else if (taskClass != null && taskClass.contains("JPanelGraphics")) {
            m_jPanelContainer.setBackground(COLOR_CANVAS);
            m_jPanelContainer.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 18, 18, 18));
        } else {
            m_jPanelContainer.setBackground(COLOR_CANVAS);
            m_jPanelContainer.setBorder(javax.swing.BorderFactory.createEmptyBorder(18, 18, 18, 18));
        }
        m_jPanelContainer.revalidate();
        m_jPanelContainer.repaint();
    }

    private void applyHeaderState(String taskClass, String title) {
        boolean overviewMode = TASK_SYSTEM_OVERVIEW.equals(taskClass);

        if (overviewRibbonPanel != null) {
            overviewRibbonPanel.setVisible(overviewMode);
        }

        if (overviewMode) {
            m_jPanelTitle.setVisible(false);
            m_jPanelTitle.setPreferredSize(new java.awt.Dimension(0, 0));
            m_jPanelTitle.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, 0));
            m_jTitle.setText("");
        } else if (title != null && !title.isBlank()) {
            m_jPanelTitle.setVisible(true);
            m_jPanelTitle.setPreferredSize(null);
            m_jPanelTitle.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
            updateBreadcrumbs(taskClass, title);
        } else {
            m_jPanelTitle.setVisible(false);
            m_jPanelTitle.setPreferredSize(new java.awt.Dimension(0, 0));
            m_jPanelTitle.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, 0));
            m_jTitle.setText("");
        }

        if (overviewRibbonPanel != null) {
            overviewRibbonPanel.revalidate();
            overviewRibbonPanel.repaint();
        }
        m_jPanelTitle.revalidate();
        m_jPanelTitle.repaint();
    }

    @Override
    public void executeTask(String sTaskClass) {

        m_appview.waitCursorBegin();

        if (m_appuser.hasPermission(sTaskClass)) {
            try {
                ProcessAction myProcess = (ProcessAction) m_appview.getBean(sTaskClass);

                try {
                    MessageInf m = myProcess.execute();
                    if (m != null) {
                        JMessageDialog.showMessage(JPrincipalApp.this, m);
                    }
                } catch (BasicException eb) {
                    JMessageDialog.showMessage(JPrincipalApp.this, new MessageInf(eb));
                }
            } catch (BeanFactoryException e) {
                JMessageDialog.showMessage(JPrincipalApp.this,
                        new MessageInf(MessageInf.SGN_WARNING,
                                AppLocal.getIntString("label.LoadError"), e));
            }
        } else {
            JMessageDialog.showMessage(JPrincipalApp.this,
                    new MessageInf(MessageInf.SGN_WARNING,
                            AppLocal.getIntString("message.notpermissions")));
        }
        m_appview.waitCursorEnd();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated
    // Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        m_jPanelLefSide = new javax.swing.JPanel();
        m_jPanelMenu = new javax.swing.JScrollPane();
        colapseHPanel = new javax.swing.JPanel();
        colapseButton = new javax.swing.JButton();
        m_jPanelRightSide = new javax.swing.JPanel();
        m_jPanelTitle = new javax.swing.JPanel();
        m_jTitle = new javax.swing.JLabel();
        m_jPanelContainer = new javax.swing.JPanel();

        setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N - Tamaño aumentado
        setLayout(new java.awt.BorderLayout());

        m_jPanelLefSide.setLayout(new java.awt.BorderLayout());

        m_jPanelMenu.setBackground(new java.awt.Color(102, 102, 102));
        m_jPanelMenu.setBorder(null);
        m_jPanelMenu.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N - Tamaño aumentado
        m_jPanelMenu.setPreferredSize(new java.awt.Dimension(250, 2));
        m_jPanelLefSide.add(m_jPanelMenu, java.awt.BorderLayout.LINE_START);

        colapseHPanel.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        colapseHPanel.setPreferredSize(new java.awt.Dimension(45, 45));

        colapseButton.setToolTipText(AppLocal.getIntString("tooltip.menu")); // NOI18N
        colapseButton.setFocusPainted(false);
        colapseButton.setFocusable(false);
        colapseButton.setIconTextGap(0);
        colapseButton.setMargin(new java.awt.Insets(10, 2, 10, 2));
        colapseButton.setMaximumSize(new java.awt.Dimension(45, 32224661));
        colapseButton.setMinimumSize(new java.awt.Dimension(32, 32));
        colapseButton.setPreferredSize(new java.awt.Dimension(36, 45));
        colapseButton.setRequestFocusEnabled(false);
        colapseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                colapseButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout colapseHPanelLayout = new javax.swing.GroupLayout(colapseHPanel);
        colapseHPanel.setLayout(colapseHPanelLayout);
        colapseHPanelLayout.setHorizontalGroup(
                colapseHPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, colapseHPanelLayout
                                .createSequentialGroup()
                                .addGap(0, 0, 0)
                                .addComponent(colapseButton, javax.swing.GroupLayout.PREFERRED_SIZE,
                                        javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap()));
        colapseHPanelLayout.setVerticalGroup(
                colapseHPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(colapseHPanelLayout.createSequentialGroup()
                                .addContainerGap(88, Short.MAX_VALUE)
                                .addComponent(colapseButton, javax.swing.GroupLayout.DEFAULT_SIZE, 33, Short.MAX_VALUE)
                                .addContainerGap(188, Short.MAX_VALUE)));

        m_jPanelLefSide.add(colapseHPanel, java.awt.BorderLayout.LINE_END);

        // Sebastian - Ocultar la barra lateral del menú para diseño tipo eleventa
        m_jPanelLefSide.setVisible(false);
        // add(m_jPanelLefSide, java.awt.BorderLayout.LINE_START);

        // No forzar tamaño pequeño: dejar que el contenido use el espacio disponible
        // (evita vistas cortadas)
        m_jPanelRightSide.setLayout(new java.awt.BorderLayout());

        // Sebastian - Crear barra horizontal superior con TODOS los botones del menú
        // (estilo eleventa)
        javax.swing.JPanel topMenuBar = new javax.swing.JPanel(new java.awt.BorderLayout(5, 0));
        topMenuBar.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createLineBorder(COLOR_LINE, 1),
                javax.swing.BorderFactory.createEmptyBorder(10, 16, 10, 16)));
        topMenuBar.setBackground(COLOR_SURFACE);
        topMenuBar.setMinimumSize(new java.awt.Dimension(0, 56));
        topMenuBar.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, 56));
        topMenuBar.setPreferredSize(new java.awt.Dimension(Integer.MAX_VALUE, 56));

        // Panel izquierdo con todos los botones del menú
        javax.swing.JPanel leftMenuPanel = new javax.swing.JPanel();
        leftMenuPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 8, 2));
        leftMenuPanel.setBackground(COLOR_SURFACE);
        leftMenuPanel.setOpaque(false);

        // Panel derecho con puntos del cliente y botón cerrar
        javax.swing.JPanel rightPanel = new javax.swing.JPanel();
        rightPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 10, 2));
        rightPanel.setBackground(COLOR_SURFACE);
        rightPanel.setOpaque(false);

        // ========== MENU.MAIN - Elementos principales ==========
        // Botón Ventas (Menu.Ticket)
        javax.swing.JButton btnInicio = createMenuButton(
                null,
                "Inicio",
                TASK_SYSTEM_OVERVIEW);
        leftMenuPanel.add(btnInicio);

        if (m_appuser.hasPermission("com.openbravo.pos.sales.JPanelTicketSales")) {
            btnVentasRef = createMenuButton(
                    "/com/openbravo/images/sale.png",
                    AppLocal.getIntString("Menu.Ticket"),
                    "com.openbravo.pos.sales.JPanelTicketSales",
                    new java.awt.Color(202, 159, 65));
            leftMenuPanel.add(btnVentasRef);
        }

        // Botón Pagos de Clientes
        if (m_appuser.hasPermission("com.openbravo.pos.customers.CustomersPayment")) {
            javax.swing.JButton btnPagosClientes = createMenuButton(
                    "/com/openbravo/images/customerpay.png",
                    AppLocal.getIntString("Menu.CustomersPayment"),
                    "com.openbravo.pos.customers.CustomersPayment");
            leftMenuPanel.add(btnPagosClientes);
        }

        // Botón Cierre de Caja
        if (m_appuser.hasPermission("com.openbravo.pos.panels.JPanelCloseMoney")) {
            btnCierreRef = createMenuButton(
                    "/com/openbravo/images/calculator.png",
                    AppLocal.getIntString("Menu.CloseTPV"),
                    "com.openbravo.pos.panels.JPanelCloseMoney",
                    new java.awt.Color(202, 159, 65));
            leftMenuPanel.add(btnCierreRef);
        }

        // ========== MENU.BACKOFFICE - Submenús ==========
        // Botón Clientes
        if (m_appuser.hasPermission("com.openbravo.pos.forms.MenuCustomers")) {
            javax.swing.JButton btnClientes = createMenuButton(
                    "/com/openbravo/images/customer.png",
                    AppLocal.getIntString("Menu.Customers"),
                    "com.openbravo.pos.forms.MenuCustomers");
            leftMenuPanel.add(btnClientes);
        }

        // Botón Reparaciones
        // Botón Proveedores
        if (m_appuser.hasPermission("com.openbravo.pos.forms.MenuSuppliers")) {
            javax.swing.JButton btnProveedores = createMenuButton(
                    "/com/openbravo/images/stockmaint.png",
                    AppLocal.getIntString("Menu.Suppliers"),
                    "com.openbravo.pos.forms.MenuSuppliers");
            leftMenuPanel.add(btnProveedores);
        }

        // Botón Gestión de Inventario
        if (m_appuser.hasPermission("com.openbravo.pos.forms.MenuStockManagement")) {
            btnInventarioRef = createMenuButton(
                    "/com/openbravo/images/products.png",
                    AppLocal.getIntString("Menu.StockManagement"),
                    "com.openbravo.pos.forms.MenuStockManagement",
                    new java.awt.Color(202, 159, 65));
            leftMenuPanel.add(btnInventarioRef);
        }

        // Botón Gestión de Ventas
        if (m_appuser.hasPermission("com.openbravo.pos.forms.MenuSalesManagement")) {
            javax.swing.JButton btnVentasManagement = createMenuButton(
                    "/com/openbravo/images/sales.png",
                    "Gestión Ventas",
                    "com.openbravo.pos.forms.MenuSalesManagement");
            leftMenuPanel.add(btnVentasManagement);
        }

        // Botón Mantenimiento
        if (m_appuser.hasPermission("com.openbravo.pos.forms.MenuMaintenance")) {
            javax.swing.JButton btnMantenimiento = createMenuButton(
                    "/com/openbravo/images/maintain.png",
                    AppLocal.getIntString("Menu.Maintenance"),
                    "com.openbravo.pos.forms.MenuMaintenance");
            leftMenuPanel.add(btnMantenimiento);
        }

        // Botón RRHH
        if (m_appuser.hasPermission("com.openbravo.pos.admin.JPanelHR")) {
            javax.swing.JButton btnRecursosHumanos = createMenuButton(
                    "/com/openbravo/images/leaves.png",
                    "RRHH",
                    "com.openbravo.pos.admin.JPanelHR");
            leftMenuPanel.add(btnRecursosHumanos);
        }



        // Botón Configuración
        if (m_appuser.hasPermission("com.openbravo.pos.config.JPanelConfiguration")) {
            javax.swing.JButton btnConfig = createMenuButton(
                    "/com/openbravo/images/configuration.png",
                    AppLocal.getIntString("Menu.Configuration"),
                    "com.openbravo.pos.config.JPanelConfiguration");
            leftMenuPanel.add(btnConfig);
        }

        // Botón Impresora
        if (m_appuser.hasPermission("com.openbravo.pos.panels.JPanelPrinter")) {
            javax.swing.JButton btnImpresora = createMenuButton(
                    "/com/openbravo/images/printer.png",
                    AppLocal.getIntString("Menu.Printer"),
                    "com.openbravo.pos.panels.JPanelPrinter");
            leftMenuPanel.add(btnImpresora);
        }

        // Botón Reportes
        if (m_appuser.hasPermission("com.openbravo.pos.reports.JPanelGraphics")) {
            btnReportesRef = createMenuButton(
                    null,
                    AppLocal.getIntString("Menu.Reports"),
                    "com.openbravo.pos.reports.JPanelGraphics",
                    new java.awt.Color(202, 159, 65));
            leftMenuPanel.add(btnReportesRef);
        }

        // Sebastian - Label de puntos del cliente (al lado del botón Salir)
        m_jCustomerPoints = new javax.swing.JLabel();
        m_jCustomerPoints.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 15));
        m_jCustomerPoints.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        m_jCustomerPoints.setText("");
        m_jCustomerPoints.setToolTipText("Puntos del cliente");
        m_jCustomerPoints.setVerticalAlignment(javax.swing.SwingConstants.CENTER);
        m_jCustomerPoints.setOpaque(false); // Sin fondo
        m_jCustomerPoints.setPreferredSize(new java.awt.Dimension(280, 32));
        m_jCustomerPoints.setForeground(COLOR_NAVY_SOFT);
        m_jCustomerPoints.setVisible(false); // Inicialmente oculto
        rightPanel.add(m_jCustomerPoints);
        // Sebastian - Label de puntos del cliente (al lado del botón Salir)

        // Agregar paneles al topMenuBar principal
        topMenuBar.add(leftMenuPanel, java.awt.BorderLayout.WEST);
        topMenuBar.add(rightPanel, java.awt.BorderLayout.EAST);

        // Altura fija para una sola línea con botones compactos
        topMenuBar.setPreferredSize(new java.awt.Dimension(Integer.MAX_VALUE, 30)); // Altura fija para una línea

        m_jPanelTitle.setLayout(new java.awt.BorderLayout());
        m_jPanelTitle.setOpaque(true);
        m_jPanelTitle.setBackground(COLOR_HEADER_BLACK);
        m_jPanelTitle.setBorder(javax.swing.BorderFactory.createEmptyBorder());
        m_jPanelTitle.setPreferredSize(new java.awt.Dimension(0, 0)); // Sin tamaño cuando está oculto
        m_jPanelTitle.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, 0)); // Sin altura cuando está oculto

        m_jTitle.setFont(new java.awt.Font("Arial", 1, 22)); // NOI18N - Tamaño aumentado
        m_jTitle.setForeground(new java.awt.Color(202, 159, 65));
        m_jTitle.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, java.awt.Color.darkGray),
                javax.swing.BorderFactory.createEmptyBorder(2, 10, 2, 10))); // Reducir padding vertical (2px en lugar
                                                                             // de 10px)
        m_jTitle.setMaximumSize(new java.awt.Dimension(100, 28)); // Reducir altura máxima aún más
        m_jTitle.setMinimumSize(new java.awt.Dimension(30, 24));
        m_jTitle.setPreferredSize(new java.awt.Dimension(100, 28)); // Reducir altura preferida (28px)
        m_jTitle.setFont(new java.awt.Font("Segoe UI", 1, 24));
        m_jTitle.setForeground(COLOR_NAVY);
        m_jTitle.setOpaque(true);
        m_jTitle.setBackground(COLOR_SURFACE);
        m_jTitle.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createLineBorder(COLOR_LINE, 1),
                javax.swing.BorderFactory.createEmptyBorder(14, 18, 14, 18)));
        m_jTitle.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, 56));
        m_jTitle.setMinimumSize(new java.awt.Dimension(30, 48));
        m_jTitle.setPreferredSize(new java.awt.Dimension(100, 52));
        m_jPanelTitle.add(m_jTitle, java.awt.BorderLayout.NORTH);
        m_jTitle.setFont(com.openbravo.pos.util.ModernLookAndFeel.getPreferredFont("Baradig", java.awt.Font.BOLD, 19));
        m_jTitle.setForeground(new java.awt.Color(236, 236, 236));
        m_jTitle.setBackground(COLOR_HEADER_BLACK);
        m_jTitle.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, new java.awt.Color(28, 28, 28)),
                javax.swing.BorderFactory.createEmptyBorder(12, 34, 12, 24)));
        m_jTitle.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, 44));
        m_jTitle.setMinimumSize(new java.awt.Dimension(30, 44));
        m_jTitle.setPreferredSize(new java.awt.Dimension(100, 44));

        overviewRibbonPanel = new javax.swing.JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 16, 0));
        overviewRibbonPanel.setOpaque(true);
        overviewRibbonPanel.setBackground(COLOR_HEADER_BLACK);
        overviewRibbonPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(6, 18, 0, 18));
        overviewRibbonPanel.setPreferredSize(new java.awt.Dimension(0, 44));
        overviewRibbonPanel.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, 44));
        overviewRibbonPanel.setVisible(false);

        javax.swing.JLabel overviewBackLabel = new javax.swing.JLabel("\u2039");
        overviewBackLabel.setFont(new java.awt.Font("Segoe UI Symbol", java.awt.Font.PLAIN, 24));
        overviewBackLabel.setForeground(new java.awt.Color(175, 175, 175));
        overviewRibbonPanel.add(overviewBackLabel);

        javax.swing.JPanel homeTabPanel = new javax.swing.JPanel();
        homeTabPanel.setOpaque(false);
        homeTabPanel.setLayout(new javax.swing.BoxLayout(homeTabPanel, javax.swing.BoxLayout.Y_AXIS));

        javax.swing.JLabel homeTabLabel = new javax.swing.JLabel("Home");
        homeTabLabel.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 14));
        homeTabLabel.setForeground(new java.awt.Color(230, 230, 230));
        homeTabLabel.setAlignmentX(javax.swing.JComponent.LEFT_ALIGNMENT);
        homeTabPanel.add(homeTabLabel);
        homeTabPanel.add(javax.swing.Box.createVerticalStrut(8));

        javax.swing.JPanel homeUnderline = new javax.swing.JPanel();
        homeUnderline.setBackground(COLOR_BRAND_ORANGE);
        homeUnderline.setPreferredSize(new java.awt.Dimension(150, 2));
        homeUnderline.setMaximumSize(new java.awt.Dimension(150, 2));
        homeUnderline.setMinimumSize(new java.awt.Dimension(150, 2));
        homeUnderline.setAlignmentX(javax.swing.JComponent.LEFT_ALIGNMENT);
        homeTabPanel.add(homeUnderline);

        overviewRibbonPanel.add(homeTabPanel);

        // Sebastian - Panel artístico superior con fondo difuminado tipo Eleventa
        javax.swing.JPanel artisticTopPanel = new javax.swing.JPanel() {
            @Override
            protected void paintComponent(java.awt.Graphics g) {
                super.paintComponent(g);
                java.awt.Graphics2D g2d = (java.awt.Graphics2D) g.create();
                g2d.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING,
                        java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setRenderingHint(java.awt.RenderingHints.KEY_RENDERING,
                        java.awt.RenderingHints.VALUE_RENDER_QUALITY);

                int width = getWidth();
                int height = getHeight();

                // 1. Draw outer 3D frame (bevel)
                // Top/Left highlights
                g2d.setColor(new java.awt.Color(230, 205, 150));
                g2d.fillRect(0, 0, width, 3);
                g2d.fillRect(0, 0, 3, height);

                // Bottom/Right shadows
                g2d.setColor(new java.awt.Color(115, 85, 35));
                g2d.fillRect(0, height - 3, width, 3);
                g2d.fillRect(width - 3, 0, 3, height);

                // Inner dark groove (gives the inset look)
                g2d.setColor(new java.awt.Color(85, 60, 25));
                g2d.drawRect(3, 3, width - 7, height - 7);

                // 2. Main content background fill (smooth metallic gradient)
                java.awt.LinearGradientPaint mainGrad = new java.awt.LinearGradientPaint(
                    0, 4, 0, height - 4,
                    new float[]{0.0f, 0.25f, 0.75f, 1.0f},
                    new java.awt.Color[]{
                        new java.awt.Color(190, 150, 75), // Upper gold
                        new java.awt.Color(205, 168, 92), // Central shine
                        new java.awt.Color(180, 140, 65), // Lower slope
                        new java.awt.Color(150, 115, 48)  // Bottom dark
                    }
                );
                g2d.setPaint(mainGrad);
                g2d.fillRect(4, 4, width - 8, height - 8);

                // 3. Highlight gloss at the top of main content
                g2d.setPaint(new java.awt.GradientPaint(
                    0, 4, new java.awt.Color(255, 255, 255, 55),
                    0, 15, new java.awt.Color(255, 255, 255, 0)
                ));
                g2d.fillRect(4, 4, width - 8, 11);

                g2d.dispose();
            }
        };
        artisticTopPanel.setLayout(new java.awt.BorderLayout());
        artisticTopPanel.setPreferredSize(new java.awt.Dimension(0, 82));
        artisticTopPanel.setOpaque(false);

        // Panel para el logo en la parte izquierda con medidas exactas
        javax.swing.JPanel logoPanel = new javax.swing.JPanel();
        logoPanel.setLayout(new java.awt.BorderLayout());
        logoPanel.setOpaque(false);
        logoPanel.setPreferredSize(new java.awt.Dimension(400, 82));
        logoPanel.setMaximumSize(new java.awt.Dimension(400, 82));
        logoPanel.setMinimumSize(new java.awt.Dimension(400, 82));
        logoPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(18, 24, 6, 20)); // Adjusted padding to drop everything

        // Sebastian - Cargar imagen del logo desde configuraciones (propiedad "start.logo")
        javax.swing.JLabel logoLabel = new javax.swing.JLabel();
        logoLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        logoLabel.setVerticalAlignment(javax.swing.SwingConstants.CENTER);
        logoLabel.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        logoLabel.setVerticalTextPosition(javax.swing.SwingConstants.CENTER);
        logoLabel.setIconTextGap(14); // Perfect margin between scooter icon and text

        // Beautiful cursive font fallback resolver
        java.awt.Font cursiveFont = null;
        for (String fName : new String[]{"Gabriola", "Segoe Script", "Monotype Corsiva", "Lucida Handwriting"}) {
            cursiveFont = new java.awt.Font(fName, java.awt.Font.ITALIC, 38); // Increased size to 38
            if (cursiveFont.getFamily().equalsIgnoreCase(fName)) {
                break;
            }
        }
        if (cursiveFont == null) {
            cursiveFont = new java.awt.Font("Serif", java.awt.Font.ITALIC, 32); // Increased fallback size to 32
        }
        final java.awt.Font fCursive = cursiveFont;

        // Método para actualizar el logo
        java.util.function.Consumer<String> updateLogo = (logoPath) -> {
            try {
                logoLabel.setIcon(null); // Limpiar icono anterior
                logoLabel.setText(""); // Limpiar texto anterior

                java.io.File logoFile = logoPath != null && !logoPath.trim().isEmpty()
                        ? new java.io.File(logoPath.trim()) : null;
                java.awt.Image originalImage = logoFile != null && logoFile.isFile()
                        ? javax.imageio.ImageIO.read(logoFile)
                        : javax.imageio.ImageIO.read(getClass().getResource("/com/openbravo/images/logo-voltium-header.png"));
                if (originalImage != null) {
                    if (originalImage != null) {
                        int originalWidth = originalImage.getWidth(null);
                        int originalHeight = originalImage.getHeight(null);
                        if (originalWidth > 0 && originalHeight > 0) {
                            // Escalar la imagen para que quepa en el panel (máximo 180x50px manteniendo proporción)
                            int maxWidth = 180;
                            int maxHeight = 50; // Adjusted max height to match padding

                            double widthRatio = (double) maxWidth / originalWidth;
                            double heightRatio = (double) maxHeight / originalHeight;
                            double ratio = Math.min(widthRatio, heightRatio);

                            int scaledWidth = (int) (originalWidth * ratio);
                            int scaledHeight = (int) (originalHeight * ratio);

                            java.awt.Image scaledImage = originalImage.getScaledInstance(
                                    scaledWidth, scaledHeight, java.awt.Image.SCALE_SMOOTH);

                            java.awt.image.BufferedImage bufferedImage = new java.awt.image.BufferedImage(
                                    scaledWidth, scaledHeight, java.awt.image.BufferedImage.TYPE_INT_ARGB);
                            java.awt.Graphics2D g2d = bufferedImage.createGraphics();
                            g2d.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION,
                                    java.awt.RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                            g2d.setRenderingHint(java.awt.RenderingHints.KEY_RENDERING,
                                    java.awt.RenderingHints.VALUE_RENDER_QUALITY);
                            g2d.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING,
                                    java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                            g2d.drawImage(scaledImage, 0, 0, null);
                            g2d.dispose();

                            logoLabel.setIcon(new javax.swing.ImageIcon(bufferedImage));
                            logoLabel.setText("voltium sanrey"); // Cursive brand text next to icon
                            logoLabel.setFont(fCursive);
                            logoLabel.setForeground(new java.awt.Color(15, 23, 42)); // Slate 900
                            logoPanel.revalidate();
                            logoPanel.repaint();
                            return;
                        }
                    }
                }

                // Si no hay ruta válida o el archivo no existe, mostrar texto "voltium sanrey"
                logoLabel.setIcon(null);
                logoLabel.setText("voltium sanrey");
                logoLabel.setFont(fCursive);
                logoLabel.setForeground(new java.awt.Color(15, 23, 42));
                logoPanel.revalidate();
                logoPanel.repaint();
            } catch (Exception e) {
                // En caso de error, mostrar texto
                logoLabel.setIcon(null);
                logoLabel.setText("voltium sanrey");
                logoLabel.setFont(fCursive);
                logoLabel.setForeground(new java.awt.Color(15, 23, 42));
                LOGGER.log(Level.WARNING, "Error al cargar el logo", e);
                logoPanel.revalidate();
                logoPanel.repaint();
            }
        };

        // Cargar el logo inicialmente
        try {
            com.openbravo.pos.forms.AppConfig appConfig = com.openbravo.pos.forms.AppConfig.getInstance();
            appConfig.load();
            String logoPath = appConfig.getProperty("start.logo"); // Sebastian - Propiedad correcta desde Configuración
                                                                   // > General > Logo
            LOGGER.log(Level.INFO, "Ruta del logo desde configuraciones: " + logoPath);
            if (logoPath != null && !logoPath.trim().isEmpty()) {
                LOGGER.log(Level.INFO, "Intentando cargar logo desde: " + logoPath);
            }
            updateLogo.accept(logoPath);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error al cargar configuración del logo", e);
            updateLogo.accept(null);
        }

        logoPanel.add(logoLabel, java.awt.BorderLayout.CENTER);

        // Sebastian - Guardar referencia al logoLabel para poder actualizarlo
        // dinámicamente
        // (se puede usar más adelante para refrescar cuando cambie la configuración)
        logoPanel.putClientProperty("logoLabel", logoLabel);
        logoPanel.putClientProperty("updateLogo", updateLogo);

        artisticTopPanel.add(logoPanel, java.awt.BorderLayout.WEST);

        // Panel derecho con campana de notificaciones y perfil (estilo Voltium Sanrey)
        javax.swing.JPanel rightTopPanel = new javax.swing.JPanel();
        rightTopPanel.setLayout(new java.awt.GridBagLayout()); // GridBag para centrar verticalmente
        rightTopPanel.setOpaque(false);
        rightTopPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 18, 0, 24));

        // Panel contenedor horizontal: [campana] [avatar] [nombre ˅]
        javax.swing.JPanel atendidoPanel = new javax.swing.JPanel();
        atendidoPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 12, 0));
        atendidoPanel.setOpaque(false);
        atendidoPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        atendidoPanel.setAlignmentX(javax.swing.JComponent.RIGHT_ALIGNMENT);

        // --- Icono de campana de notificaciones ---
        m_btnNotification = new javax.swing.JButton() {
            @Override
            protected void paintComponent(java.awt.Graphics g) {
                java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
                g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING,
                        java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth();
                int h = getHeight();

                // Hover effect
                if (getModel().isRollover()) {
                    g2.setColor(new java.awt.Color(255, 255, 255, 40));
                    g2.fillRoundRect(2, 2, w - 4, h - 4, 8, 8);
                }

                int cx = w / 2;
                int cy = h / 2;

                // Campana (bell icon) - color gris suave
                g2.setColor(new java.awt.Color(100, 116, 139)); // slate-500
                g2.setStroke(new java.awt.BasicStroke(1.8f, java.awt.BasicStroke.CAP_ROUND, java.awt.BasicStroke.JOIN_ROUND));

                // Cuerpo de la campana
                java.awt.geom.Path2D.Double bell = new java.awt.geom.Path2D.Double();
                bell.moveTo(cx - 8, cy + 3);
                bell.curveTo(cx - 8, cy - 6, cx - 6, cy - 10, cx, cy - 10);
                bell.curveTo(cx + 6, cy - 10, cx + 8, cy - 6, cx + 8, cy + 3);
                bell.lineTo(cx + 10, cy + 5);
                bell.lineTo(cx - 10, cy + 5);
                bell.closePath();
                g2.draw(bell);

                // Badajo (clapper)
                g2.drawLine(cx - 3, cy + 5, cx + 3, cy + 5);
                g2.drawArc(cx - 2, cy + 5, 4, 3, 180, 180);

                // Punto rojo de notificación si hay alertas pendientes
                if (m_notificationCount > 0) {
                    g2.setColor(new java.awt.Color(239, 68, 68));
                    g2.fillOval(cx + 4, cy - 10, 6, 6);
                }

                g2.dispose();
            }
        };
        m_btnNotification.setOpaque(false);
        m_btnNotification.setContentAreaFilled(false);
        m_btnNotification.setBorderPainted(false);
        m_btnNotification.setFocusPainted(false);
        m_btnNotification.setFocusable(false);
        m_btnNotification.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        m_btnNotification.setToolTipText("Notificaciones");
        m_btnNotification.setPreferredSize(new java.awt.Dimension(36, 36));
        m_btnNotification.setMinimumSize(new java.awt.Dimension(36, 36));
        m_btnNotification.setMaximumSize(new java.awt.Dimension(36, 36));
        m_btnNotification.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                toggleNotificationSidebar();
            }
        });

        // --- Icono de perfil circular con foto real ---
        final java.awt.image.BufferedImage[] profileImageRef = new java.awt.image.BufferedImage[1];
        try {
            com.openbravo.pos.forms.AppConfig appConfig = com.openbravo.pos.forms.AppConfig.getInstance();
            appConfig.load();
            String profileImgPath = appConfig.getProperty("profile.image");
            java.io.File profileFile = profileImgPath != null && !profileImgPath.trim().isEmpty()
                    ? new java.io.File(profileImgPath.trim()) : null;
            if (profileFile != null && profileFile.isFile()) {
                profileImageRef[0] = javax.imageio.ImageIO.read(profileFile);
            } else {
                java.net.URL defaultProfile = getClass().getResource("/com/openbravo/images/profile-default.png");
                if (defaultProfile != null) profileImageRef[0] = javax.imageio.ImageIO.read(defaultProfile);
            }
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "Error al cargar imagen de perfil", ex);
        }

        javax.swing.JPanel profileIconPanel = new javax.swing.JPanel() {
            @Override
            protected void paintComponent(java.awt.Graphics g) {
                super.paintComponent(g);
                java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
                g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING,
                        java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION,
                        java.awt.RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                int w = getWidth();
                int h = getHeight();
                int sz = Math.min(w, h) - 2;

                // Clip circular
                java.awt.geom.Ellipse2D.Double circle = new java.awt.geom.Ellipse2D.Double(
                        (w - sz) / 2.0, (h - sz) / 2.0, sz, sz);

                if (profileImageRef[0] != null) {
                    // Dibujar imagen recortada en circulo
                    g2.setClip(circle);
                    g2.drawImage(profileImageRef[0],
                            (w - sz) / 2, (h - sz) / 2, sz, sz, null);
                    g2.setClip(null);
                } else {
                    // Fallback: silueta genérica
                    g2.setColor(new java.awt.Color(203, 213, 225)); // slate-300
                    g2.fill(circle);
                    g2.setColor(new java.awt.Color(148, 163, 184)); // slate-400
                    int headR = (int) (sz * 0.15f);
                    int headCX = w / 2;
                    int headCY = (int) (h * 0.35f);
                    g2.fillOval(headCX - headR, headCY - headR, headR * 2, headR * 2);
                    int bodyW = (int) (sz * 0.5f);
                    int bodyH = (int) (sz * 0.35f);
                    int bodyX = (w - bodyW) / 2;
                    int bodyY = (int) (h * 0.55f);
                    g2.setClip(circle);
                    g2.fillArc(bodyX, bodyY, bodyW, bodyH, 0, 180);
                    g2.setClip(null);
                }

                // Borde circular suave
                g2.setColor(new java.awt.Color(226, 232, 240)); // slate-200
                g2.setStroke(new java.awt.BasicStroke(1.5f));
                g2.draw(circle);

                g2.dispose();
            }
        };
        profileIconPanel.setOpaque(false);
        profileIconPanel.setPreferredSize(new java.awt.Dimension(38, 38));
        profileIconPanel.setMinimumSize(new java.awt.Dimension(38, 38));
        profileIconPanel.setMaximumSize(new java.awt.Dimension(38, 38));

        // --- Nombre del usuario + flecha dropdown (horizontal) ---
        String userName = m_appuser.getName();
        if (userName == null || userName.isEmpty()) userName = "admin";
        javax.swing.JLabel lblUserName = new javax.swing.JLabel(userName + "  \u25BE");
        lblUserName.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 13));
        lblUserName.setForeground(new java.awt.Color(15, 23, 42)); // slate-900

        // Perfil del usuario - se agregará después de inicializar m_principalnotificator
        profilePanelRef = new javax.swing.JPanel();
        profilePanelRef.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 0, 0));
        profilePanelRef.setOpaque(false);

        // Panel agrupador horizontal: [avatar] [nombre ˅]
        javax.swing.JPanel profileGroupPanel = new javax.swing.JPanel() {
            @Override
            protected void paintComponent(java.awt.Graphics g) {
                Boolean hovered = (Boolean) getClientProperty("hovered");
                if (hovered != null && hovered) {
                    java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
                    g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING,
                            java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(new java.awt.Color(0, 0, 0, 15));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                    g2.dispose();
                }
                super.paintComponent(g);
            }
        };
        profileGroupPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 8, 4));
        profileGroupPanel.setOpaque(false);
        profileGroupPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(2, 6, 2, 6));

        profileGroupPanel.add(profileIconPanel);
        profileGroupPanel.add(lblUserName);

        // Al hacer clic en el perfil, mostrar popup de opciones
        profileGroupPanel.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        profileGroupPanel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                profileGroupPanel.putClientProperty("hovered", Boolean.TRUE);
                profileGroupPanel.repaint();
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                profileGroupPanel.putClientProperty("hovered", Boolean.FALSE);
                profileGroupPanel.repaint();
            }

            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                // Mostrar popup menu de opciones del sistema
                JPopupMenu profileMenu = new JPopupMenu();
                profileMenu.setBackground(new java.awt.Color(30, 41, 59));
                profileMenu.setBorder(BorderFactory.createLineBorder(new java.awt.Color(202, 159, 65), 1));

                JMenuItem itemPerfil = new JMenuItem("Mi Perfil");
                styleMenuItem(itemPerfil);
                itemPerfil.addActionListener(e -> {
                    if (m_appuser.hasPermission("com.openbravo.pos.config.JPanelConfiguration")) {
                        showTask("com.openbravo.pos.config.JPanelConfiguration");
                        javax.swing.SwingUtilities.invokeLater(() -> {
                            try {
                                JPanelView viewPanel = rMenu.getViewManager().getCreatedViews()
                                        .get("com.openbravo.pos.config.JPanelConfiguration");
                                if (viewPanel instanceof com.openbravo.pos.config.JPanelConfiguration) {
                                    ((com.openbravo.pos.config.JPanelConfiguration) viewPanel).selectProfileTab();
                                }
                            } catch (Exception ex2) {
                                LOGGER.log(Level.WARNING, "Error al seleccionar pestaña de perfil", ex2);
                            }
                        });
                    }
                });
                profileMenu.add(itemPerfil);

                JMenuItem itemConfig2 = new JMenuItem("Configuración");
                styleMenuItem(itemConfig2);
                itemConfig2.addActionListener(e -> {
                    if (m_appuser.hasPermission("com.openbravo.pos.config.JPanelConfiguration")) {
                        showTask("com.openbravo.pos.config.JPanelConfiguration");
                    }
                });
                profileMenu.add(itemConfig2);

                JMenuItem itemSalir2 = new JMenuItem("Salir / Apagar");
                styleMenuItem(itemSalir2);
                itemSalir2.addMouseListener(new java.awt.event.MouseAdapter() {
                    @Override
                    public void mouseEntered(java.awt.event.MouseEvent e2) {
                        itemSalir2.setBackground(new java.awt.Color(190, 18, 60));
                        itemSalir2.setForeground(java.awt.Color.WHITE);
                    }
                    @Override
                    public void mouseExited(java.awt.event.MouseEvent e2) {
                        itemSalir2.setBackground(new java.awt.Color(30, 41, 59));
                        itemSalir2.setForeground(new java.awt.Color(241, 245, 249));
                    }
                });
                itemSalir2.addActionListener(e -> m_appview.tryToClose());
                profileMenu.add(itemSalir2);

                profileMenu.show(profileGroupPanel,
                        profileGroupPanel.getWidth() - profileMenu.getPreferredSize().width,
                        profileGroupPanel.getHeight() + 4);
            }
        });

        atendidoPanel.removeAll();
        atendidoPanel.add(m_btnNotification);
        atendidoPanel.add(profileGroupPanel);

        java.awt.GridBagConstraints gbcCentred = new java.awt.GridBagConstraints();
        gbcCentred.gridx = 0;
        gbcCentred.gridy = 0;
        gbcCentred.anchor = java.awt.GridBagConstraints.CENTER;
        rightTopPanel.add(atendidoPanel, gbcCentred);

        artisticTopPanel.add(rightTopPanel, java.awt.BorderLayout.EAST);
        // Sebastian - Agregar barra de menú horizontal arriba del título (con múltiples
        // filas automáticas)
        // Usar BoxLayout vertical para permitir que el panel de botones se expanda
        // correctamente
        javax.swing.JPanel topContainer = new javax.swing.JPanel();
        topContainer.setLayout(new java.awt.BorderLayout());
        topContainer.setOpaque(true);
        topContainer.setBackground(COLOR_HEADER_BLACK);

        // Agregar panel artístico arriba
        topContainer.add(artisticTopPanel, java.awt.BorderLayout.NORTH);

        // Panel para la barra de menú y título
        javax.swing.JPanel menuContainer = new javax.swing.JPanel();
        menuContainer.setLayout(new javax.swing.BoxLayout(menuContainer, javax.swing.BoxLayout.Y_AXIS));
        menuContainer.setOpaque(true);
        menuContainer.setBackground(COLOR_HEADER_BLACK);
        menuContainer.setBorder(javax.swing.BorderFactory.createEmptyBorder());
        topMenuBar.setPreferredSize(new java.awt.Dimension(Integer.MAX_VALUE, 56));
        topMenuBar.setAlignmentX(javax.swing.JComponent.LEFT_ALIGNMENT);
        overviewRibbonPanel.setAlignmentX(javax.swing.JComponent.LEFT_ALIGNMENT);
        m_jPanelTitle.setAlignmentX(javax.swing.JComponent.LEFT_ALIGNMENT);
        menuContainer.add(overviewRibbonPanel);
        menuContainer.add(m_jPanelTitle); // Sin espacio entre barra y título
        // Asegurar que el contenedor respete el tamaño preferido del panel de botones
        menuContainer.setAlignmentX(javax.swing.JComponent.LEFT_ALIGNMENT);
        // No forzar tamaño preferido para evitar espacio cuando m_jPanelTitle está
        // oculto
        // El tamaño se calculará automáticamente basado en los componentes visibles

        topContainer.add(menuContainer, java.awt.BorderLayout.CENTER);

        m_jPanelRightSide.setOpaque(true);
        m_jPanelRightSide.setBackground(COLOR_CANVAS);
        m_jPanelRightSide.add(topContainer, java.awt.BorderLayout.NORTH);

        m_jPanelContainer.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N - Tamaño aumentado
        m_jPanelContainer.setLayout(new java.awt.CardLayout());
        m_jPanelContainer.setOpaque(true);
        m_jPanelContainer.setBackground(COLOR_CANVAS);
        m_jPanelContainer.setBorder(javax.swing.BorderFactory.createEmptyBorder(18, 18, 18, 18));
        m_jPanelRightSide.add(m_jPanelContainer, java.awt.BorderLayout.CENTER);

        add(m_jPanelRightSide, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void colapseButtonActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_colapseButtonActionPerformed

        setMenuVisible(!m_jPanelMenu.isVisible());

    }// GEN-LAST:event_colapseButtonActionPerformed

    /**
     * Sebastian - Aplica el estilo moderno sin ícono a un JMenuItem del menú de
     * cabecera
     */
    private void styleMenuItem(JMenuItem item) {
        item.setFont(com.openbravo.pos.util.ModernLookAndFeel.getPreferredFont("Baradig", java.awt.Font.BOLD, 13));
        item.setBackground(new java.awt.Color(30, 41, 59)); // Gris pizarra
        item.setForeground(new java.awt.Color(241, 245, 249)); // Off-white
        item.setOpaque(true);
        item.setIcon(null); // Sin ícono
        item.setBorder(BorderFactory.createEmptyBorder(10, 24, 10, 24));
        item.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        item.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                item.setBackground(new java.awt.Color(202, 159, 65)); // Oro corporativo
                item.setForeground(java.awt.Color.WHITE);
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                item.setBackground(new java.awt.Color(30, 41, 59));
                item.setForeground(new java.awt.Color(241, 245, 249));
            }
        });
    }

    /**
     * Sebastian - Agrega una opción de texto sin ícono al menú hamburguesa
     */
    private void addMenuItem(JPopupMenu menu, String text, String taskClass) {
        JMenuItem item = new JMenuItem(text);
        item.setFont(com.openbravo.pos.util.ModernLookAndFeel.getPreferredFont("Baradig", java.awt.Font.BOLD, 13));
        item.setBackground(new java.awt.Color(30, 41, 59)); // Gris pizarra
        item.setForeground(new java.awt.Color(241, 245, 249)); // Off-white
        item.setOpaque(true);
        item.setIcon(null); // Sin ícono
        item.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        item.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        item.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                item.setBackground(new java.awt.Color(202, 159, 65)); // Oro corporativo al hacer hover
                item.setForeground(java.awt.Color.WHITE);
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                item.setBackground(new java.awt.Color(30, 41, 59));
                item.setForeground(new java.awt.Color(241, 245, 249));
            }
        });

        item.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showTask(taskClass);
            }
        });

        menu.add(item);
    }

    /**
     * Método helper para crear botones del menú de forma consistente
     */
    private javax.swing.JButton createMenuButton(String iconPath, String text, String taskClass) {
        return createMenuButton(iconPath, text, taskClass, null);
    }

    /**
     * Método helper para crear botones del menú con color personalizado
     */
    private javax.swing.JButton createMenuButton(String iconPath, String text, String taskClass,
            java.awt.Color backgroundColor) {
        javax.swing.JButton button = new javax.swing.JButton();
        // Iconos removidos para diseño compacto como eleventa
        button.setText(text);

        // Calcular ancho basado en la longitud del texto para que se lea bien
        int textLength = text.length();
        int buttonWidth = Math.max(60, Math.min(130, 30 + (textLength * 6))); // Mínimo 60, máximo 130, más proporcional
                                                                              // al texto

        // Tamaño compacto y proporcional al texto
        button.setPreferredSize(new java.awt.Dimension(buttonWidth, 25));
        button.setMinimumSize(new java.awt.Dimension(60, 25));
        button.setMaximumSize(new java.awt.Dimension(130, 25));
        button.setFont(new java.awt.Font("Arial", java.awt.Font.PLAIN, 15)); // Tamaño de fuente aumentado para mejor
                                                                             // legibilidad

        // Color de fondo personalizado o blanco por defecto
        if (backgroundColor != null) {
            button.setBackground(backgroundColor);
            // Texto blanco si el fondo es oscuro, negro si es claro
            int brightness = (backgroundColor.getRed() + backgroundColor.getGreen() + backgroundColor.getBlue()) / 3;
            button.setForeground(brightness < 128 ? java.awt.Color.WHITE : java.awt.Color.BLACK);
        } else {
            button.setBackground(java.awt.Color.WHITE);
            button.setForeground(java.awt.Color.BLACK);
        }

        button.setOpaque(true);
        button.setFocusPainted(false);
        button.setBorderPainted(true);
        button.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createLineBorder(new java.awt.Color(200, 200, 200), 1),
                javax.swing.BorderFactory.createEmptyBorder(2, 5, 2, 5)));
        button.setPreferredSize(new java.awt.Dimension(Math.max(90, Math.min(180, 54 + (textLength * 7))), 34));
        button.setMinimumSize(new java.awt.Dimension(90, 34));
        button.setMaximumSize(new java.awt.Dimension(180, 34));
        button.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 13));
        button.setFocusable(false);
        button.putClientProperty("nav.emphasis", Boolean.valueOf(backgroundColor != null));
        navigationButtons.put(taskClass, button);
        refreshNavigationButtonStyle(button, false);
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                refreshNavigationButtonStyle(button, true);
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                refreshNavigationButtonStyle(button, false);
            }
        });

        button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showTask(taskClass);
            }
        });
        return button;
    }

    private void setActiveNavigationTask(String taskClass) {
        javax.swing.JButton nextActive = navigationButtons.get(taskClass);
        if (nextActive == null) {
            return;
        }

        activeNavigationButton = nextActive;
        for (javax.swing.JButton button : navigationButtons.values()) {
            refreshNavigationButtonStyle(button, false);
        }
    }

    private void refreshNavigationButtonStyle(javax.swing.JButton button, boolean hovered) {
        boolean emphasized = Boolean.TRUE.equals(button.getClientProperty("nav.emphasis"));
        boolean active = button == activeNavigationButton;

        java.awt.Color background;
        java.awt.Color foreground;
        java.awt.Color border;

        if (active) {
            background = COLOR_NAVY;
            foreground = COLOR_SURFACE;
            border = COLOR_NAVY;
        } else if (emphasized) {
            background = hovered ? new java.awt.Color(221, 231, 241) : COLOR_SHORTCUT;
            foreground = COLOR_NAVY;
            border = new java.awt.Color(188, 203, 218);
        } else {
            background = hovered ? new java.awt.Color(245, 248, 251) : COLOR_SURFACE;
            foreground = COLOR_TEXT;
            border = COLOR_LINE;
        }

        button.setBackground(background);
        button.setForeground(foreground);
        button.setOpaque(true);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(true);
        button.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        button.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createLineBorder(border, 1),
                javax.swing.BorderFactory.createEmptyBorder(7, 12, 7, 12)));
    }

    /**
     * Configura los atajos de teclado globales para los botones principales
     * F1: Ventas
     * F2: Cerrar Caja
     * F3: Stock (Gestión de Inventario)
     * F4: Reportes
     */
    private void setupGlobalKeyboardShortcuts() {
        javax.swing.InputMap inputMap = this.getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW);
        javax.swing.ActionMap actionMap = this.getActionMap();
        
        // F1: Ventas
        if (btnVentasRef != null) {
            inputMap.put(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F1, 0), "shortcutVentas");
            actionMap.put("shortcutVentas", new javax.swing.AbstractAction() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    if (btnVentasRef != null && btnVentasRef.isEnabled()) {
                        btnVentasRef.doClick();
                    }
                }
            });
        }
        
        // F2: Cerrar Caja
        if (btnCierreRef != null) {
            inputMap.put(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F2, 0), "shortcutCierre");
            actionMap.put("shortcutCierre", new javax.swing.AbstractAction() {

    @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    if (btnCierreRef != null && btnCierreRef.isEnabled()) {
                        btnCierreRef.doClick();
                    }
                }
            });
        }
        
        // F3: Stock (Gestión de Inventario)
        if (btnInventarioRef != null) {
            inputMap.put(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F3, 0), "shortcutInventario");
            actionMap.put("shortcutInventario", new javax.swing.AbstractAction() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    if (btnInventarioRef != null && btnInventarioRef.isEnabled()) {
                        btnInventarioRef.doClick();
                    }
                }
            });
        }

    // F4: Reportes
    if(btnReportesRef!=null)

    {
        inputMap.put(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F4, 0), "shortcutReportes");
        actionMap.put("shortcutReportes", new javax.swing.AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                if (btnReportesRef != null && btnReportesRef.isEnabled()) {
                    btnReportesRef.doClick();
                }
            }
        });
    }

    LOGGER.log(Level.INFO,"✅ Atajos de teclado globales configurados: F1=Ventas, F2=Cerrar Caja, F3=Stock, F4=Reportes");
    }

    /**
     * Convierte el ID del rol (0, 1, 2, 3...) al nombre del rol (ADMIN, MANAGER,
     * Employee)
     * para compatibilidad con el sistema de permisos
     */
    private String mapRoleIdToName(String roleId) {
        if (roleId == null || roleId.trim().isEmpty()) {
            return "Employee"; // Por defecto
        }
        switch (roleId.trim()) {
            case "0":
            case "1": // rol = 1 son admins según la tabla usuarios
                return "ADMIN";
            case "2":
                return "MANAGER";
            case "3":
                return "Employee";
            default:
                // Si ya es un nombre de rol, devolverlo tal cual
                if (roleId.equalsIgnoreCase("ADMIN") || roleId.equalsIgnoreCase("MANAGER")
                        || roleId.equalsIgnoreCase("Employee")) {
                    return roleId.substring(0, 1).toUpperCase() + roleId.substring(1).toLowerCase();
                }
                return roleId; // Rol personalizado
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton colapseButton;
    private javax.swing.JPanel colapseHPanel;
    private javax.swing.JPanel m_jPanelContainer;
    private javax.swing.JPanel m_jPanelLefSide;
    private javax.swing.JScrollPane m_jPanelMenu;
    private javax.swing.JPanel m_jPanelRightSide;
    private javax.swing.JPanel m_jPanelTitle;
    private javax.swing.JLabel m_jTitle;
    // Sebastian - Label de puntos del cliente en la barra superior
    private javax.swing.JLabel m_jCustomerPoints;
    // End of variables declaration//GEN-END:variables

    private String[] findTaskBreadcrumb(String sTaskClass) {
        if (sTaskClass == null) {
            return null;
        }
        if ("com.openbravo.pos.sales.JPanelTicketSales".equals(sTaskClass)) {
            return new String[]{ AppLocal.getIntString("Menu.Ticket"), "com.openbravo.pos.sales.JPanelTicketSales" };
        }
        if ("com.openbravo.pos.config.JPanelConfiguration".equals(sTaskClass)) {
            return new String[]{ "Configuración", "com.openbravo.pos.config.JPanelConfiguration" };
        }
        if ("com.openbravo.pos.admin.JPanelHR".equals(sTaskClass)) {
            return new String[]{ "Recursos Humanos", "com.openbravo.pos.admin.JPanelHR" };
        }
        if ("com.openbravo.pos.inventory.JPanelProductVariation".equals(sTaskClass)) {
            return new String[]{ "Inventario", "Menu.Inventory", "Variación de Precios", sTaskClass };
        }
        
        java.util.Map<String, JPanelView> allViews = new java.util.HashMap<>();
        if (rMenu != null && rMenu.getViewManager() != null) {
            java.util.Map<String, JPanelView> prepared = rMenu.getViewManager().getPreparedViews();
            java.util.Map<String, JPanelView> created = rMenu.getViewManager().getCreatedViews();
            if (prepared != null) {
                allViews.putAll(prepared);
            }
            if (created != null) {
                allViews.putAll(created);
            }
        }
        
        // If sTaskClass is actually a submenu class itself, return path [ "SubmenuTitle", sTaskClass ]
        JPanelView targetView = allViews.get(sTaskClass);
        if (targetView instanceof com.openbravo.pos.menu.JPanelMenu) {
            return new String[]{ targetView.getTitle(), sTaskClass };
        }
        
        for (java.util.Map.Entry<String, JPanelView> entry : allViews.entrySet()) {
            if (entry.getValue() instanceof com.openbravo.pos.menu.JPanelMenu) {
                com.openbravo.pos.menu.JPanelMenu menu = (com.openbravo.pos.menu.JPanelMenu) entry.getValue();
                String[] path = menu.findTaskPath(sTaskClass);
                if (path != null) {
                    return new String[]{ menu.getTitle(), path[0], path[1], entry.getKey() };
                }
            }
        }
        return null;
    }

    private javax.swing.JPanel m_jPanelBreadcrumbs = null;

    private void updateBreadcrumbs(String sTaskClass, String sTitle) {
        if (m_jPanelTitle == null) {
            return;
        }
        m_jPanelTitle.removeAll();
        
        String[] path = findTaskBreadcrumb(sTaskClass);
        if (path == null) {
            if (sTaskClass != null && (sTaskClass.contains("reports") || sTaskClass.contains("Report") || sTaskClass.endsWith(".bs"))) {
                path = new String[]{ "Ventas y Reportes", "Reportes", sTitle, "com.openbravo.pos.forms.MenuSalesManagement" };
            } else {
                path = new String[]{ "Inicio", "General", sTitle, "com.openbravo.pos.forms.JPanelSystemOverview" };
            }
        }
        
        if (path != null) {
            if (m_jPanelBreadcrumbs == null) {
                m_jPanelBreadcrumbs = new javax.swing.JPanel();
                m_jPanelBreadcrumbs.setOpaque(true);
                m_jPanelBreadcrumbs.setBackground(COLOR_HEADER_BLACK);
                m_jPanelBreadcrumbs.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 6, 0));
                m_jPanelBreadcrumbs.setBorder(javax.swing.BorderFactory.createEmptyBorder(14, 24, 14, 24));
            } else {
                m_jPanelBreadcrumbs.removeAll();
            }
            
            if (path.length == 2) {
                // It is a submenu page (e.g. Stock)
                final String submenuTitle = path[0];
                final String submenuClass = path[1];
                
                // Add "Inicio" link
                javax.swing.JLabel homeLink = createBreadcrumbLink("Inicio", () -> {
                    showTask(TASK_SYSTEM_OVERVIEW);
                });
                m_jPanelBreadcrumbs.add(homeLink);
                
                // Add Separator
                m_jPanelBreadcrumbs.add(createBreadcrumbSeparator());
                
                // Add Item name (active, bold white)
                javax.swing.JLabel activeLabel = new javax.swing.JLabel(stripHtml(submenuTitle));
                activeLabel.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 15));
                activeLabel.setForeground(new java.awt.Color(255, 255, 255));
                m_jPanelBreadcrumbs.add(activeLabel);
                
                m_jPanelTitle.add(m_jPanelBreadcrumbs, java.awt.BorderLayout.CENTER);
            } else if (path.length == 4) {
                // It is a task page under a submenu (e.g. Departamentos)
                final String submenuTitle = path[0];
                final String sectionName = path[1];
                final String itemName = path[2];
                final String submenuClass = path[3];
                
                // Add "Inicio" link
                javax.swing.JLabel homeLink = createBreadcrumbLink("Inicio", () -> {
                    showTask(TASK_SYSTEM_OVERVIEW);
                });
                m_jPanelBreadcrumbs.add(homeLink);
                
                // Add Separator
                m_jPanelBreadcrumbs.add(createBreadcrumbSeparator());
                
                // Add Submenu link
                javax.swing.JLabel subLink = createBreadcrumbLink(submenuTitle, () -> {
                    showTask(submenuClass);
                });
                m_jPanelBreadcrumbs.add(subLink);
                
                // Add Separator
                m_jPanelBreadcrumbs.add(createBreadcrumbSeparator());
                
                // Add Section name (non-clickable, muted)
                javax.swing.JLabel secLabel = new javax.swing.JLabel(stripHtml(sectionName));
                secLabel.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 15));
                secLabel.setForeground(new java.awt.Color(160, 160, 160));
                m_jPanelBreadcrumbs.add(secLabel);
                
                // Add Separator
                m_jPanelBreadcrumbs.add(createBreadcrumbSeparator());
                
                // Add Item name (active, bold white)
                javax.swing.JLabel activeLabel = new javax.swing.JLabel(stripHtml(itemName));
                activeLabel.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 15));
                activeLabel.setForeground(new java.awt.Color(255, 255, 255));
                m_jPanelBreadcrumbs.add(activeLabel);
                
                m_jPanelTitle.add(m_jPanelBreadcrumbs, java.awt.BorderLayout.CENTER);
            }
        } else {
            // Fallback to normal title label
            m_jTitle.setText(stripHtml(sTitle));
            m_jPanelTitle.add(m_jTitle, java.awt.BorderLayout.CENTER);
        }
        
        m_jPanelTitle.revalidate();
        m_jPanelTitle.repaint();
    }

    private javax.swing.JLabel createBreadcrumbLink(String text, final Runnable action) {
        final javax.swing.JLabel link = new javax.swing.JLabel(stripHtml(text));
        link.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 15));
        link.setForeground(new java.awt.Color(200, 200, 200));
        link.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        
        link.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                link.setForeground(COLOR_BRAND_ORANGE);
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                link.setForeground(new java.awt.Color(200, 200, 200));
            }
            
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                action.run();
            }
        });
        
        return link;
    }

    private javax.swing.JLabel createBreadcrumbSeparator() {
        javax.swing.JLabel sep = new javax.swing.JLabel("\u203a");
        sep.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 15));
        sep.setForeground(new java.awt.Color(110, 110, 110));
        return sep;
    }

    private String stripHtml(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replaceAll("<[^>]+>", " ")
                .replace("&nbsp;", " ")
                .replace("*", " ")
                .replace("_", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    /**
     * Sebastian - Método público para actualizar el label de puntos del cliente
     */
    public void updateCustomerPointsDisplay(String text, boolean visible) {
        if (m_jCustomerPoints != null) {
            m_jCustomerPoints.setText(text);
            m_jCustomerPoints.setVisible(visible);
        }
    }

    private void createNotificationSidebar() {
        m_jPanelNotificationSidebar = new javax.swing.JPanel(new java.awt.BorderLayout()) {
            @Override
            protected void paintComponent(java.awt.Graphics g) {
                super.paintComponent(g);
                java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
                g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                // Borde izquierdo sutil
                g2.setColor(COLOR_LINE);
                g2.drawLine(0, 0, 0, getHeight());
                g2.dispose();
            }
        };
        m_jPanelNotificationSidebar.setOpaque(true);
        m_jPanelNotificationSidebar.setBackground(java.awt.Color.WHITE);
        m_jPanelNotificationSidebar.setPreferredSize(new java.awt.Dimension(320, 200));

        // Header de la barra lateral con altura cómoda y fija
        javax.swing.JPanel headerPanel = new javax.swing.JPanel(new java.awt.BorderLayout());
        headerPanel.setOpaque(true);
        headerPanel.setBackground(new java.awt.Color(248, 250, 252)); // Slate-50
        headerPanel.setPreferredSize(new java.awt.Dimension(320, 52));
        headerPanel.setMinimumSize(new java.awt.Dimension(320, 52));
        headerPanel.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, 52));
        headerPanel.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, COLOR_LINE),
                javax.swing.BorderFactory.createEmptyBorder(0, 16, 0, 10)
        ));

        m_lblNotificationTitle = new javax.swing.JLabel("Alertas y Notificaciones");
        m_lblNotificationTitle.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 14));
        m_lblNotificationTitle.setForeground(COLOR_TEXT);
        headerPanel.add(m_lblNotificationTitle, java.awt.BorderLayout.CENTER);

        // Botón Cerrar (X) con renderizado vectorial perfecto centrado y respuesta instantánea
        javax.swing.JButton btnClose = new javax.swing.JButton() {
            @Override
            protected void paintComponent(java.awt.Graphics g) {
                java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
                g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth();
                int h = getHeight();

                int boxSize = 30;
                int bx = (w - boxSize) / 2;
                int by = (h - boxSize) / 2;

                if (getModel().isPressed()) {
                    g2.setColor(new java.awt.Color(226, 232, 240)); // slate-200
                    g2.fillRoundRect(bx, by, boxSize, boxSize, 8, 8);
                } else if (getModel().isRollover()) {
                    g2.setColor(new java.awt.Color(241, 245, 249)); // slate-100
                    g2.fillRoundRect(bx, by, boxSize, boxSize, 8, 8);
                }

                int cx = w / 2;
                int cy = h / 2;
                int arm = 5;
                g2.setColor(getModel().isRollover() ? new java.awt.Color(15, 23, 42) : new java.awt.Color(100, 116, 139)); // slate-900 en hover, slate-500 normal
                g2.setStroke(new java.awt.BasicStroke(2.2f, java.awt.BasicStroke.CAP_ROUND, java.awt.BasicStroke.JOIN_ROUND));
                g2.drawLine(cx - arm, cy - arm, cx + arm, cy + arm);
                g2.drawLine(cx + arm, cy - arm, cx - arm, cy + arm);

                g2.dispose();
            }
        };
        btnClose.setPreferredSize(new java.awt.Dimension(36, 36));
        btnClose.setMinimumSize(new java.awt.Dimension(36, 36));
        btnClose.setMaximumSize(new java.awt.Dimension(36, 36));
        btnClose.setOpaque(false);
        btnClose.setContentAreaFilled(false);
        btnClose.setBorderPainted(false);
        btnClose.setFocusPainted(false);
        btnClose.setFocusable(false);
        btnClose.setRolloverEnabled(true);
        btnClose.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnClose.setToolTipText("Cerrar notificaciones (Esc)");

        // Cerrar de forma instantánea al presionar el ratón o por evento de botón (idempotente)
        btnClose.addActionListener(e -> closeNotificationSidebar());
        btnClose.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                if (javax.swing.SwingUtilities.isLeftMouseButton(e)) {
                    closeNotificationSidebar();
                }
            }
        });

        javax.swing.JPanel btnWrapper = new javax.swing.JPanel(new java.awt.GridBagLayout());
        btnWrapper.setOpaque(false);
        btnWrapper.add(btnClose);
        headerPanel.add(btnWrapper, java.awt.BorderLayout.EAST);

        // Atajo ESC para cerrar la barra de notificaciones
        m_jPanelNotificationSidebar.getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ESCAPE, 0), "closeNotificationSidebar");
        m_jPanelNotificationSidebar.getActionMap().put("closeNotificationSidebar", new javax.swing.AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                closeNotificationSidebar();
            }
        });

        m_jPanelNotificationSidebar.add(headerPanel, java.awt.BorderLayout.NORTH);

        // Panel de lista scrollable
        m_notificationsListPanel = new javax.swing.JPanel();
        m_notificationsListPanel.setLayout(new javax.swing.BoxLayout(m_notificationsListPanel, javax.swing.BoxLayout.Y_AXIS));
        m_notificationsListPanel.setOpaque(true);
        m_notificationsListPanel.setBackground(java.awt.Color.WHITE);
        m_notificationsListPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(12, 12, 12, 12));

        javax.swing.JScrollPane scrollPane = new javax.swing.JScrollPane(m_notificationsListPanel);
        scrollPane.setBorder(null);
        scrollPane.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setPreferredSize(new java.awt.Dimension(8, 8));
        
        m_jPanelNotificationSidebar.add(scrollPane, java.awt.BorderLayout.CENTER);

        m_jPanelRightSide.add(m_jPanelNotificationSidebar, java.awt.BorderLayout.EAST);
        m_jPanelNotificationSidebar.setVisible(false);
    }

    public void toggleNotificationSidebar() {
        if (m_jPanelNotificationSidebar == null) {
            createNotificationSidebar();
        }
        boolean isVisible = !m_jPanelNotificationSidebar.isVisible();
        m_jPanelNotificationSidebar.setVisible(isVisible);
        if (isVisible) {
            refreshNotifications();
        }
        revalidate();
        repaint();
    }

    public void closeNotificationSidebar() {
        if (m_jPanelNotificationSidebar != null && m_jPanelNotificationSidebar.isVisible()) {
            m_jPanelNotificationSidebar.setVisible(false);
            revalidate();
            repaint();
        }
    }

    private static class NotificationData {
        java.util.List<com.openbravo.pos.inventory.LowStockProduct> lowStockProducts;
        java.util.List<com.openbravo.pos.admin.PayrollPendingAlert> payrollAlerts;
    }

    private void refreshNotifications() {
        if (m_notificationsListPanel == null) return;
        m_notificationsListPanel.removeAll();

        final boolean isAdminOrHR = m_appuser != null && (
            m_appuser.hasPermission("com.openbravo.pos.admin.JPanelHR") ||
            "admin".equalsIgnoreCase(m_appuser.getName()) ||
            "Administrator".equalsIgnoreCase(m_appuser.getRole())
        );

        new javax.swing.SwingWorker<NotificationData, Void>() {
            @Override
            protected NotificationData doInBackground() throws Exception {
                NotificationData data = new NotificationData();
                try {
                    com.openbravo.pos.forms.DataLogicSales dlSales = (com.openbravo.pos.forms.DataLogicSales) m_appview.getBean("com.openbravo.pos.forms.DataLogicSales");
                    if (dlSales != null) {
                        data.lowStockProducts = dlSales.getLowStockProducts();
                    }
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "Error al obtener stock bajo para notificaciones", e);
                }

                if (isAdminOrHR) {
                    try {
                        com.openbravo.pos.admin.DataLogicHR dlHR = (com.openbravo.pos.admin.DataLogicHR) m_appview.getBean("com.openbravo.pos.admin.DataLogicHR");
                        if (dlHR != null) {
                            data.payrollAlerts = dlHR.getPendingPayrollAlerts(new java.util.Date());
                        }
                    } catch (Exception e) {
                        LOGGER.log(Level.WARNING, "Error al obtener nóminas pendientes para notificaciones", e);
                    }
                }
                return data;
            }

            @Override
            protected void done() {
                try {
                    NotificationData data = get();
                    int stockCount = (data != null && data.lowStockProducts != null) ? data.lowStockProducts.size() : 0;
                    int payrollCount = (data != null && data.payrollAlerts != null) ? data.payrollAlerts.size() : 0;
                    int totalAlerts = stockCount + payrollCount;

                    m_notificationCount = totalAlerts;
                    if (m_lblNotificationTitle != null) {
                        m_lblNotificationTitle.setText("Alertas y Notificaciones" + (totalAlerts > 0 ? " (" + totalAlerts + ")" : ""));
                    }
                    if (m_btnNotification != null) {
                        m_btnNotification.repaint();
                    }

                    if (totalAlerts == 0) {
                        showEmptyState();
                    } else {
                        // 1. Mostrar alertas de nómina para el administrador
                        if (payrollCount > 0) {
                            if (stockCount > 0) {
                                addNotificationSectionHeader("Nóminas pendientes (" + payrollCount + ")");
                            }
                            for (com.openbravo.pos.admin.PayrollPendingAlert alert : data.payrollAlerts) {
                                addPayrollNotificationItem(alert);
                            }
                        }

                        // 2. Mostrar alertas de inventario bajo
                        if (stockCount > 0) {
                            if (payrollCount > 0) {
                                addNotificationSectionHeader("Inventario bajo (" + stockCount + ")");
                            }
                            for (com.openbravo.pos.inventory.LowStockProduct p : data.lowStockProducts) {
                                addNotificationItem(p);
                            }
                        }
                    }
                } catch (Exception ex) {
                    showErrorState(ex.getMessage());
                }
                m_notificationsListPanel.revalidate();
                m_notificationsListPanel.repaint();
            }
        }.execute();
    }

    private void addNotificationSectionHeader(String title) {
        javax.swing.JLabel lbl = new javax.swing.JLabel(title);
        lbl.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 11));
        lbl.setForeground(new java.awt.Color(71, 85, 105)); // Slate-600
        lbl.setBorder(javax.swing.BorderFactory.createEmptyBorder(6, 4, 6, 4));
        m_notificationsListPanel.add(lbl);
    }

    private void showEmptyState() {
        javax.swing.JPanel emptyPanel = new javax.swing.JPanel();
        emptyPanel.setLayout(new javax.swing.BoxLayout(emptyPanel, javax.swing.BoxLayout.Y_AXIS));
        emptyPanel.setOpaque(false);
        emptyPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(40, 20, 40, 20));

        javax.swing.JLabel checkLabel = new javax.swing.JLabel("✓");
        checkLabel.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 48));
        checkLabel.setForeground(new java.awt.Color(34, 197, 94)); // verde-500
        checkLabel.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);

        javax.swing.JLabel text1 = new javax.swing.JLabel("Todo en orden por el momento");
        text1.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 14));
        text1.setForeground(COLOR_TEXT_MUTED);
        text1.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);

        emptyPanel.add(checkLabel);
        emptyPanel.add(javax.swing.Box.createVerticalStrut(16));
        emptyPanel.add(text1);

        m_notificationsListPanel.add(emptyPanel);
    }

    private void showErrorState(String errorMsg) {
        javax.swing.JLabel errLbl = new javax.swing.JLabel("Error al cargar alertas: " + errorMsg);
        errLbl.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 12));
        errLbl.setForeground(COLOR_DANGER);
        m_notificationsListPanel.add(errLbl);
    }

    private void addPayrollNotificationItem(com.openbravo.pos.admin.PayrollPendingAlert p) {
        javax.swing.JPanel item = new javax.swing.JPanel(new java.awt.BorderLayout(12, 0)) {
            @Override
            protected void paintComponent(java.awt.Graphics g) {
                super.paintComponent(g);
                java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
                g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                
                boolean hover = Boolean.TRUE.equals(getClientProperty("hover"));
                if (hover) {
                    g2.setColor(new java.awt.Color(226, 232, 240)); // slate-200
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                    g2.setColor(new java.awt.Color(203, 213, 225)); // slate-300
                } else {
                    g2.setColor(new java.awt.Color(241, 245, 249)); // slate-100
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                    g2.setColor(new java.awt.Color(226, 232, 240)); // slate-200
                }
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                g2.dispose();
            }
        };
        item.setOpaque(false);
        item.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 12, 10, 12));
        item.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, 68));
        item.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        item.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                item.putClientProperty("hover", Boolean.TRUE);
                item.repaint();
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                item.putClientProperty("hover", Boolean.FALSE);
                item.repaint();
            }
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                closeNotificationSidebar();
                openHRPayroll(p.getEmployeeId());
            }
        });

        // Icono de nómina ($): Rojo si está vencida, Azul si es hoy/programada
        final boolean isOverdue = p.isOverdue();
        javax.swing.JPanel iconPanel = new javax.swing.JPanel() {
            @Override
            protected void paintComponent(java.awt.Graphics g) {
                java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
                g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth();
                int h = getHeight();
                
                if (isOverdue) {
                    g2.setColor(new java.awt.Color(239, 68, 68)); // red-500
                } else {
                    g2.setColor(new java.awt.Color(37, 99, 235)); // blue-600
                }
                g2.fillOval(0, 0, w, h);

                g2.setColor(java.awt.Color.WHITE);
                g2.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 14));
                java.awt.FontMetrics fm = g2.getFontMetrics();
                String text = "$";
                int tx = (w - fm.stringWidth(text)) / 2;
                int ty = (h - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString(text, tx, ty);
                
                g2.dispose();
            }
        };
        iconPanel.setOpaque(false);
        iconPanel.setPreferredSize(new java.awt.Dimension(28, 28));
        iconPanel.setMinimumSize(new java.awt.Dimension(28, 28));
        iconPanel.setMaximumSize(new java.awt.Dimension(28, 28));

        item.add(iconPanel, java.awt.BorderLayout.WEST);

        // Textos de la nómina
        javax.swing.JPanel textPanel = new javax.swing.JPanel();
        textPanel.setLayout(new javax.swing.BoxLayout(textPanel, javax.swing.BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);

        String empName = p.getEmployeeName();
        if (empName != null && empName.length() > 24) {
            empName = empName.substring(0, 22) + "...";
        }
        javax.swing.JLabel nameLabel = new javax.swing.JLabel("Nómina: " + (empName == null ? "Colaborador" : empName));
        nameLabel.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12));
        nameLabel.setForeground(COLOR_TEXT);

        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy");
        String dateStr = p.getPaymentDate() != null ? sdf.format(p.getPaymentDate()) : "Hoy";
        String amountStr = Formats.CURRENCY.formatValue(p.getAmount());

        String statusStr = p.isOverdue() ? "Vencida" : p.getStatus();
        javax.swing.JLabel detailLabel = new javax.swing.JLabel(
            p.getPeriodLabel() + " • " + statusStr
        );
        detailLabel.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 11));
        detailLabel.setForeground(p.isOverdue() ? new java.awt.Color(220, 38, 38) : new java.awt.Color(71, 85, 105));

        javax.swing.JLabel payLabel = new javax.swing.JLabel(
            "Pago: " + dateStr + " • " + amountStr
        );
        payLabel.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 11));
        payLabel.setForeground(new java.awt.Color(37, 99, 235));

        textPanel.add(nameLabel);
        textPanel.add(javax.swing.Box.createVerticalStrut(2));
        textPanel.add(detailLabel);
        textPanel.add(javax.swing.Box.createVerticalStrut(1));
        textPanel.add(payLabel);

        item.add(textPanel, java.awt.BorderLayout.CENTER);

        m_notificationsListPanel.add(item);
        m_notificationsListPanel.add(javax.swing.Box.createVerticalStrut(8));
    }

    private void addNotificationItem(com.openbravo.pos.inventory.LowStockProduct p) {
        javax.swing.JPanel item = new javax.swing.JPanel(new java.awt.BorderLayout(12, 0)) {
            @Override
            protected void paintComponent(java.awt.Graphics g) {
                super.paintComponent(g);
                java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
                g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                
                boolean hover = Boolean.TRUE.equals(getClientProperty("hover"));
                if (hover) {
                    g2.setColor(new java.awt.Color(226, 232, 240)); // slate-200
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                    g2.setColor(new java.awt.Color(203, 213, 225)); // slate-300
                } else {
                    g2.setColor(new java.awt.Color(241, 245, 249)); // slate-100
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                    g2.setColor(new java.awt.Color(226, 232, 240)); // slate-200
                }
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                g2.dispose();
            }
        };
        item.setOpaque(false);
        item.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 12, 10, 12));
        item.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, 64));
        item.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        item.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                item.putClientProperty("hover", Boolean.TRUE);
                item.repaint();
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                item.putClientProperty("hover", Boolean.FALSE);
                item.repaint();
            }
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                closeNotificationSidebar();
                openProductDetails(p.getProductId());
            }
        });

        // Icono de advertencia
        javax.swing.JPanel iconPanel = new javax.swing.JPanel() {
            @Override
            protected void paintComponent(java.awt.Graphics g) {
                java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
                g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth();
                int h = getHeight();
                
                // Círculo ámbar
                g2.setColor(new java.awt.Color(251, 191, 36)); // amber-400
                g2.fillOval(0, 0, w, h);

                // Exclamación blanca
                g2.setColor(java.awt.Color.WHITE);
                g2.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 14));
                java.awt.FontMetrics fm = g2.getFontMetrics();
                String text = "!";
                int tx = (w - fm.stringWidth(text)) / 2;
                int ty = (h - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString(text, tx, ty);
                
                g2.dispose();
            }
        };
        iconPanel.setOpaque(false);
        iconPanel.setPreferredSize(new java.awt.Dimension(28, 28));
        iconPanel.setMinimumSize(new java.awt.Dimension(28, 28));
        iconPanel.setMaximumSize(new java.awt.Dimension(28, 28));

        item.add(iconPanel, java.awt.BorderLayout.WEST);

        // Textos del producto
        javax.swing.JPanel textPanel = new javax.swing.JPanel();
        textPanel.setLayout(new javax.swing.BoxLayout(textPanel, javax.swing.BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);

        String prodName = p.getProductName();
        if (prodName.length() > 28) {
            prodName = prodName.substring(0, 26) + "...";
        }
        javax.swing.JLabel nameLabel = new javax.swing.JLabel(prodName);
        nameLabel.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12));
        nameLabel.setForeground(COLOR_TEXT);
        
        javax.swing.JLabel stockLabel = new javax.swing.JLabel(
            "Stock: " + Formats.DOUBLE.formatValue(p.getUnits()) + " (Mín: " + Formats.DOUBLE.formatValue(p.getMinimum()) + ")"
        );
        stockLabel.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 11));
        stockLabel.setForeground(new java.awt.Color(239, 68, 68)); // Rojo suave para resaltar stock crítico

        textPanel.add(nameLabel);
        textPanel.add(javax.swing.Box.createVerticalStrut(2));
        textPanel.add(stockLabel);

        item.add(textPanel, java.awt.BorderLayout.CENTER);

        m_notificationsListPanel.add(item);
        m_notificationsListPanel.add(javax.swing.Box.createVerticalStrut(8));
    }

    private void openProductDetails(String productId) {
        String taskClass = "com.openbravo.pos.inventory.ProductsPanel";
        showTask(taskClass);
        
        JPanelView viewPanel = rMenu.getViewManager().getCreatedViews().get(taskClass);
        if (viewPanel instanceof com.openbravo.pos.inventory.ProductsPanel) {
            com.openbravo.pos.inventory.ProductsPanel productsPanel = (com.openbravo.pos.inventory.ProductsPanel) viewPanel;
            productsPanel.showProductById(productId);
        }
    }

    private void openHRPayroll(String employeeId) {
        String taskClass = "com.openbravo.pos.admin.JPanelHR";
        showTask(taskClass);
        
        JPanelView viewPanel = rMenu.getViewManager().getCreatedViews().get(taskClass);
        if (viewPanel instanceof com.openbravo.pos.admin.JPanelHR) {
            com.openbravo.pos.admin.JPanelHR hrPanel = (com.openbravo.pos.admin.JPanelHR) viewPanel;
            hrPanel.showEmployeePayroll(employeeId);
        }
    }
}

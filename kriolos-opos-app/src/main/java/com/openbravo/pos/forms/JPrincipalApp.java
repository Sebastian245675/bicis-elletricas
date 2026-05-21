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
    private static final java.awt.Color COLOR_BRAND_ORANGE = new java.awt.Color(243, 153, 18);
    private static final java.awt.Color COLOR_BRAND_ORANGE_DARK = new java.awt.Color(222, 129, 10);
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
    
    // Referencia al panel de perfil en el panel superior
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
        
        // Sebastian - Configurar estilo del perfil para el panel superior
        m_principalnotificator.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 16));
        m_principalnotificator.setForeground(COLOR_SURFACE);
        m_principalnotificator.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        
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
        
        // Sebastian - Refrescar el logo cuando se active el panel (por si cambió en configuración)
        try {
            // Buscar el logoPanel en el componente
            javax.swing.JPanel artisticPanel = findArtisticTopPanel(this);
            if (artisticPanel != null) {
                for (java.awt.Component comp : artisticPanel.getComponents()) {
                    if (comp instanceof javax.swing.JPanel) {
                        javax.swing.JPanel logoPanel = (javax.swing.JPanel) comp;
                        @SuppressWarnings("unchecked")
                        java.util.function.Consumer<String> updateLogo = (java.util.function.Consumer<String>) logoPanel.getClientProperty("updateLogo");
                        if (updateLogo != null) {
                            // Recargar la configuración y actualizar el logo
                            com.openbravo.pos.forms.AppConfig appConfig = com.openbravo.pos.forms.AppConfig.getInstance();
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
            // Si canceló (isCloseShiftRequested e isExitOnlyRequested son false), no hacer nada
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
                    
                    // Sebastian - Si es la vista de ventas, asegurar que el campo de búsqueda tenga el foco
                    if (sTaskClass != null && sTaskClass.contains("JPanelTicketSales")) {
                        final JPanelView finalViewPanel = viewPanel;
                        javax.swing.SwingUtilities.invokeLater(() -> {
                            try {
                                if (finalViewPanel.getComponent() instanceof com.openbravo.pos.sales.JPanelTicket) {
                                    ((com.openbravo.pos.sales.JPanelTicket) finalViewPanel.getComponent()).setSearchFieldFocus();
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
                                    ((com.openbravo.pos.sales.JPanelTicket) finalViewPanel.getComponent()).setSearchFieldFocus();
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

        // No forzar tamaño pequeño: dejar que el contenido use el espacio disponible (evita vistas cortadas)
        m_jPanelRightSide.setLayout(new java.awt.BorderLayout());

        // Sebastian - Crear barra horizontal superior con TODOS los botones del menú (estilo eleventa)
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
                    "F1 " + AppLocal.getIntString("Menu.Ticket"),
                    "com.openbravo.pos.sales.JPanelTicketSales",
                    new java.awt.Color(21, 94, 156));
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
                    "F2 " + AppLocal.getIntString("Menu.CloseTPV"),
                    "com.openbravo.pos.panels.JPanelCloseMoney",
                    new java.awt.Color(21, 94, 156));
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
                    "F3 " + AppLocal.getIntString("Menu.StockManagement"),
                    "com.openbravo.pos.forms.MenuStockManagement",
                    new java.awt.Color(21, 94, 156));
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

        // Botón Drive
        if (m_appuser.hasPermission("com.openbravo.pos.panels.JPanelDocuments")) {
            javax.swing.JButton btnDocumentos = createMenuButton(
                    "/com/openbravo/images/fileopen.png",
                    "Drive",
                    "com.openbravo.pos.panels.JPanelDocuments");
            leftMenuPanel.add(btnDocumentos);
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
                    "F4 " + AppLocal.getIntString("Menu.Reports"),
                    "com.openbravo.pos.reports.JPanelGraphics",
                    new java.awt.Color(21, 94, 156));
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
        m_jTitle.setForeground(new java.awt.Color(21, 94, 156));
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
        m_jTitle.setFont(new java.awt.Font("Segoe UI", 1, 19));
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
                g2d.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setRenderingHint(java.awt.RenderingHints.KEY_RENDERING, java.awt.RenderingHints.VALUE_RENDER_QUALITY);
                
                int width = getWidth();
                int height = getHeight();
                
                // Gradiente base suave tipo Eleventa - mejorado con más tonos
                java.awt.GradientPaint baseGradient = new java.awt.GradientPaint(
                    0, 0, new java.awt.Color(250, 252, 255),
                    0, height, new java.awt.Color(238, 242, 247)
                );
                g2d.setPaint(baseGradient);
                g2d.fillRect(0, 0, width, height);
                
                // Efecto de luz difuminada superior izquierda (para logo) - más suave
                java.awt.RadialGradientPaint lightEffect1 = new java.awt.RadialGradientPaint(
                    150, 40, 250,
                    new float[]{0f, 0.5f, 0.8f, 1f},
                    new java.awt.Color[]{
                        new java.awt.Color(140, 195, 240, 50),
                        new java.awt.Color(140, 195, 240, 25),
                        new java.awt.Color(140, 195, 240, 10),
                        new java.awt.Color(140, 195, 240, 0)
                    }
                );
                g2d.setPaint(lightEffect1);
                g2d.fillOval(-50, -30, 500, 200);
                
                // Efecto de luz difuminada superior derecha - más suave
                java.awt.RadialGradientPaint lightEffect2 = new java.awt.RadialGradientPaint(
                    width - 150, 40, 220,
                    new float[]{0f, 0.6f, 0.9f, 1f},
                    new java.awt.Color[]{
                        new java.awt.Color(110, 170, 230, 35),
                        new java.awt.Color(110, 170, 230, 15),
                        new java.awt.Color(110, 170, 230, 5),
                        new java.awt.Color(110, 170, 230, 0)
                    }
                );
                g2d.setPaint(lightEffect2);
                g2d.fillOval(width - 440, -20, 440, 180);
                
                // Efecto adicional central para más profundidad
                java.awt.RadialGradientPaint lightEffect3 = new java.awt.RadialGradientPaint(
                    width / 2, height / 3, 300,
                    new float[]{0f, 0.7f, 1f},
                    new java.awt.Color[]{
                        new java.awt.Color(120, 180, 225, 20),
                        new java.awt.Color(120, 180, 225, 5),
                        new java.awt.Color(120, 180, 225, 0)
                    }
                );
                g2d.setPaint(lightEffect3);
                g2d.fillOval(width / 2 - 300, -50, 600, 200);
                
                // Línea sutil inferior con gradiente más suave
                java.awt.MultipleGradientPaint.CycleMethod cycleMethod = java.awt.MultipleGradientPaint.CycleMethod.NO_CYCLE;
                java.awt.Color[] lineColors = {
                    new java.awt.Color(200, 210, 220, 80),
                    new java.awt.Color(220, 220, 220, 40),
                    new java.awt.Color(240, 240, 240, 0)
                };
                float[] lineFractions = {0.0f, 0.5f, 1.0f};
                java.awt.LinearGradientPaint lineGradient = new java.awt.LinearGradientPaint(
                    0, height - 1, width, height - 1,
                    lineFractions, lineColors, cycleMethod
                );
                g2d.setPaint(lineGradient);
                g2d.fillRect(0, height - 2, width, 2);
                
                // Sombra sutil superior para profundidad
                java.awt.GradientPaint shadowGradient = new java.awt.GradientPaint(
                    0, 0, new java.awt.Color(0, 0, 0, 5),
                    0, 10, new java.awt.Color(0, 0, 0, 0)
                );
                g2d.setPaint(shadowGradient);
                g2d.fillRect(0, 0, width, 10);

                java.awt.GradientPaint enterpriseOverlay = new java.awt.GradientPaint(
                    0, 0, new java.awt.Color(14, 30, 45, 210),
                    width, height, new java.awt.Color(30, 59, 86, 205)
                );
                g2d.setPaint(enterpriseOverlay);
                g2d.fillRect(0, 0, width, height);

                java.awt.RadialGradientPaint premiumGlowLeft = new java.awt.RadialGradientPaint(
                    120, 20, 220,
                    new float[]{0f, 0.65f, 1f},
                    new java.awt.Color[]{
                        new java.awt.Color(125, 177, 220, 75),
                        new java.awt.Color(125, 177, 220, 20),
                        new java.awt.Color(125, 177, 220, 0)
                    }
                );
                g2d.setPaint(premiumGlowLeft);
                g2d.fillOval(-60, -100, 360, 260);

                java.awt.RadialGradientPaint premiumGlowRight = new java.awt.RadialGradientPaint(
                    width - 140, 15, 240,
                    new float[]{0f, 0.55f, 1f},
                    new java.awt.Color[]{
                        new java.awt.Color(255, 255, 255, 40),
                        new java.awt.Color(255, 255, 255, 12),
                        new java.awt.Color(255, 255, 255, 0)
                    }
                );
                g2d.setPaint(premiumGlowRight);
                g2d.fillOval(width - 380, -120, 420, 260);

                g2d.setColor(new java.awt.Color(255, 255, 255, 24));
                g2d.fillRoundRect(18, 12, width - 36, height - 24, 24, 24);

                g2d.setPaint(new java.awt.GradientPaint(
                    0, height - 2, new java.awt.Color(255, 255, 255, 0),
                    width, height - 2, new java.awt.Color(255, 255, 255, 90)
                ));
                g2d.fillRect(22, height - 3, width - 44, 2);

                g2d.setPaint(new java.awt.GradientPaint(
                        0, 0, COLOR_BRAND_ORANGE,
                        0, height, COLOR_BRAND_ORANGE_DARK));
                g2d.fillRect(0, 0, width, height);

                g2d.setPaint(new java.awt.GradientPaint(
                        0, 0, new java.awt.Color(255, 255, 255, 42),
                        width, 0, new java.awt.Color(255, 255, 255, 0)));
                g2d.fillRect(0, 0, width, 10);

                g2d.setColor(new java.awt.Color(255, 222, 163, 55));
                g2d.fillRoundRect(16, 10, Math.max(220, width / 5), height - 20, 24, 24);

                g2d.setPaint(new java.awt.GradientPaint(
                        0, height - 2, new java.awt.Color(67, 33, 0, 170),
                        width, height - 2, new java.awt.Color(38, 17, 0, 220)));
                g2d.fillRect(0, height - 2, width, 2);
                
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
        logoPanel.setPreferredSize(new java.awt.Dimension(320, 82));
        logoPanel.setMaximumSize(new java.awt.Dimension(320, 82));
        logoPanel.setMinimumSize(new java.awt.Dimension(320, 82));
        logoPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(12, 24, 12, 20));
        
        // Sebastian - Cargar imagen del logo desde configuraciones (propiedad "start.logo")
        javax.swing.JLabel logoLabel = new javax.swing.JLabel();
        logoLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        logoLabel.setVerticalAlignment(javax.swing.SwingConstants.CENTER);
        
        // Método para actualizar el logo
        java.util.function.Consumer<String> updateLogo = (logoPath) -> {
            try {
                logoLabel.setIcon(null); // Limpiar icono anterior
                logoLabel.setText(""); // Limpiar texto anterior
                
                if (logoPath != null && !logoPath.trim().isEmpty()) {
                    java.io.File logoFile = new java.io.File(logoPath);
                    LOGGER.log(Level.INFO, "Verificando archivo de logo: " + logoPath + " - Existe: " + logoFile.exists() + " - Es archivo: " + logoFile.isFile());
                    
                    if (logoFile.exists() && logoFile.isFile()) {
                        // Cargar la imagen del logo usando ImageIO para mejor manejo
                        try {
                            java.awt.Image originalImage = javax.imageio.ImageIO.read(logoFile);
                            
                            // Verificar que la imagen se cargó correctamente
                            if (originalImage != null) {
                                int originalWidth = originalImage.getWidth(null);
                                int originalHeight = originalImage.getHeight(null);
                                LOGGER.log(Level.INFO, "Imagen cargada: " + originalWidth + "x" + originalHeight);
                                
                                if (originalWidth > 0 && originalHeight > 0) {
                                    // Escalar la imagen para que quepa en el panel (máximo 180x60px manteniendo proporción)
                                    int maxWidth = 180;
                                    int maxHeight = 60;
                                    
                                    // Calcular dimensiones manteniendo proporción
                                    double widthRatio = (double) maxWidth / originalWidth;
                                    double heightRatio = (double) maxHeight / originalHeight;
                                    double ratio = Math.min(widthRatio, heightRatio);
                                    
                                    int scaledWidth = (int) (originalWidth * ratio);
                                    int scaledHeight = (int) (originalHeight * ratio);
                                    
                                    LOGGER.log(Level.INFO, "Escalando imagen a: " + scaledWidth + "x" + scaledHeight);
                                    
                                    // Escalar la imagen con mejor calidad usando Graphics2D
                                    java.awt.Image scaledImage = originalImage.getScaledInstance(
                                        scaledWidth, scaledHeight, java.awt.Image.SCALE_SMOOTH
                                    );
                                    
                                    // Usar BufferedImage para mejor renderizado
                                    java.awt.image.BufferedImage bufferedImage = new java.awt.image.BufferedImage(
                                        scaledWidth, scaledHeight, java.awt.image.BufferedImage.TYPE_INT_ARGB
                                    );
                                    java.awt.Graphics2D g2d = bufferedImage.createGraphics();
                                    g2d.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION, java.awt.RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                                    g2d.setRenderingHint(java.awt.RenderingHints.KEY_RENDERING, java.awt.RenderingHints.VALUE_RENDER_QUALITY);
                                    g2d.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                                    g2d.drawImage(scaledImage, 0, 0, null);
                                    g2d.dispose();
                                    
                                    logoLabel.setIcon(new javax.swing.ImageIcon(bufferedImage));
                                    logoLabel.setText(""); // Sin texto, solo imagen
                                    LOGGER.log(Level.INFO, "✓ Logo cargado y mostrado exitosamente desde: " + logoPath);
                                    logoPanel.revalidate();
                                    logoPanel.repaint();
                                    return;
                                }
                            }
                        } catch (javax.imageio.IIOException e) {
                            LOGGER.log(Level.WARNING, "Error al leer imagen del logo (formato no soportado?): " + logoPath, e);
                        }
                    } else {
                        LOGGER.log(Level.WARNING, "El archivo de logo no existe o no es un archivo válido: " + logoPath);
                    }
                } else {
                    LOGGER.log(Level.INFO, "No hay ruta de logo configurada en 'start.logo'");
                }
                
                // Si no hay ruta válida o el archivo no existe, mostrar texto "LOGO"
                logoLabel.setIcon(null);
                logoLabel.setText("<html><span style='color:#EE961C'>WEBSY</span> <span style='color:#313131'>GROUP</span></html>");
                logoLabel.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 24));
                logoLabel.setForeground(new java.awt.Color(36, 22, 7));
                logoPanel.revalidate();
                logoPanel.repaint();
            } catch (Exception e) {
                // En caso de error, mostrar texto "LOGO"
                logoLabel.setIcon(null);
                logoLabel.setText("<html><span style='color:#EE961C'>WEBSY</span> <span style='color:#313131'>GROUP</span></html>");
                logoLabel.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 24));
                logoLabel.setForeground(new java.awt.Color(36, 22, 7));
                LOGGER.log(Level.WARNING, "Error al cargar el logo desde: " + logoPath, e);
                logoPanel.revalidate();
                logoPanel.repaint();
            }
        };
        
        // Cargar el logo inicialmente
        try {
            com.openbravo.pos.forms.AppConfig appConfig = com.openbravo.pos.forms.AppConfig.getInstance();
            appConfig.load();
            String logoPath = appConfig.getProperty("start.logo"); // Sebastian - Propiedad correcta desde Configuración > General > Logo
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
        
        // Sebastian - Guardar referencia al logoLabel para poder actualizarlo dinámicamente
        // (se puede usar más adelante para refrescar cuando cambie la configuración)
        logoPanel.putClientProperty("logoLabel", logoLabel);
        logoPanel.putClientProperty("updateLogo", updateLogo);
        
        artisticTopPanel.add(logoPanel, java.awt.BorderLayout.WEST);
        
        // Panel derecho con "Le atiende: [perfil]"
        javax.swing.JPanel rightTopPanel = new javax.swing.JPanel();
        rightTopPanel.setLayout(new java.awt.BorderLayout());
        rightTopPanel.setOpaque(false);
        rightTopPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 18, 10, 24));
        
        // Panel contenedor para el texto y el perfil (horizontal)
        javax.swing.JPanel atendidoPanel = new javax.swing.JPanel();
        atendidoPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 14, 0));
        atendidoPanel.setOpaque(false);
        atendidoPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        atendidoPanel.setAlignmentX(javax.swing.JComponent.RIGHT_ALIGNMENT);
        
        // --- Icono de perfil circular ---
        javax.swing.JPanel profileIconPanel = new javax.swing.JPanel() {
            @Override
            protected void paintComponent(java.awt.Graphics g) {
                super.paintComponent(g);
                java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
                g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(); int h = getHeight();
                // Fondo circular blanco semitransparente
                g2.setColor(new java.awt.Color(255, 255, 255, 60));
                g2.fillOval(0, 0, w - 1, h - 1);
                g2.setColor(new java.awt.Color(255, 255, 255, 120));
                g2.setStroke(new java.awt.BasicStroke(1.5f));
                g2.drawOval(0, 0, w - 1, h - 1);
                // Cabeza
                int headR = w / 4;
                int headCX = w / 2; int headCY = h * 5 / 16;
                g2.setColor(new java.awt.Color(255, 255, 255, 230));
                g2.fillOval(headCX - headR, headCY - headR, headR * 2, headR * 2);
                // Cuerpo (semielipse inferior)
                int bodyW = w * 10 / 16; int bodyH = h * 6 / 16;
                int bodyX = (w - bodyW) / 2; int bodyY = h * 9 / 16;
                g2.setColor(new java.awt.Color(255, 255, 255, 200));
                // Clip to circle for body
                java.awt.geom.Ellipse2D.Double clipCircle = new java.awt.geom.Ellipse2D.Double(0, 0, w, h);
                g2.setClip(clipCircle);
                g2.fillArc(bodyX, bodyY, bodyW, bodyH, 0, 180);
                g2.dispose();
            }
        };
        profileIconPanel.setOpaque(false);
        profileIconPanel.setPreferredSize(new java.awt.Dimension(32, 32));
        profileIconPanel.setMinimumSize(new java.awt.Dimension(32, 32));
        profileIconPanel.setMaximumSize(new java.awt.Dimension(32, 32));
        
        // Label "Le atiende:"
        javax.swing.JLabel lblLeAtiende = new javax.swing.JLabel("Le atiende:");
        lblLeAtiende.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 13));
        lblLeAtiende.setForeground(new java.awt.Color(201, 213, 225));
        
        // Perfil del usuario - se agregarÃ¡ despuÃ©s de inicializar m_principalnotificator
        profilePanelRef = new javax.swing.JPanel();
        profilePanelRef.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 0));
        profilePanelRef.setOpaque(false);
        
        // Panel agrupador de perfil (icono + textos)
        javax.swing.JPanel profileGroupPanel = new javax.swing.JPanel();
        profileGroupPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 8, 0));
        profileGroupPanel.setOpaque(false);
        profileGroupPanel.add(profileIconPanel);
        profileGroupPanel.add(lblLeAtiende);
        profileGroupPanel.add(profilePanelRef);
        
        // --- Bandera de Argentina ---
        javax.swing.JPanel flagArPanel = new javax.swing.JPanel() {
            @Override
            protected void paintComponent(java.awt.Graphics g) {
                super.paintComponent(g);
                java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
                g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(); int h = getHeight();
                int stripe = h / 3;
                java.awt.Color lightBlue = new java.awt.Color(116, 172, 220); // celeste argentina
                java.awt.Color white = java.awt.Color.WHITE;
                // Franja superior celeste
                g2.setColor(lightBlue);
                g2.fillRect(0, 0, w, stripe);
                // Franja media blanca
                g2.setColor(white);
                g2.fillRect(0, stripe, w, stripe);
                // Franja inferior celeste
                g2.setColor(lightBlue);
                g2.fillRect(0, stripe * 2, w, h - stripe * 2);
                // Sol de mayo (centro)
                int cx = w / 2; int cy = h / 2;
                int sunR = stripe / 2 - 1;
                g2.setColor(new java.awt.Color(252, 191, 73)); // dorado
                // Rayos del sol
                g2.setStroke(new java.awt.BasicStroke(1.0f));
                int rays = 16;
                for (int i = 0; i < rays; i++) {
                    double angle = Math.toRadians(i * (360.0 / rays));
                    int x1 = cx + (int)((sunR + 1) * Math.cos(angle));
                    int y1 = cy + (int)((sunR + 1) * Math.sin(angle));
                    int x2 = cx + (int)((sunR + 3) * Math.cos(angle));
                    int y2 = cy + (int)((sunR + 3) * Math.sin(angle));
                    g2.drawLine(x1, y1, x2, y2);
                }
                // CÃ­rculo del sol
                g2.fillOval(cx - sunR, cy - sunR, sunR * 2, sunR * 2);
                // Contorno sutil de la bandera
                g2.setColor(new java.awt.Color(0, 0, 0, 60));
                g2.setStroke(new java.awt.BasicStroke(1f));
                g2.drawRoundRect(0, 0, w - 1, h - 1, 3, 3);
                g2.dispose();
            }
        };
        flagArPanel.setOpaque(false);
        flagArPanel.setPreferredSize(new java.awt.Dimension(36, 24));
        flagArPanel.setMinimumSize(new java.awt.Dimension(36, 24));
        flagArPanel.setMaximumSize(new java.awt.Dimension(36, 24));
        flagArPanel.setToolTipText("Argentina");
        
        // Wrapper para centrar verticalmente la bandera
        javax.swing.JPanel flagWrapper = new javax.swing.JPanel();
        flagWrapper.setLayout(new java.awt.GridBagLayout());
        flagWrapper.setOpaque(false);
        flagWrapper.setPreferredSize(new java.awt.Dimension(36, 32));
        flagWrapper.add(flagArPanel);
        
        // BotÃ³n de Apagar / Cerrar Programa (con sÃ­mbolo power vectorizado en rojo)
        javax.swing.JButton btnCerrar = new javax.swing.JButton() {
            @Override
            protected void paintComponent(java.awt.Graphics g) {
                java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
                g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(); int h = getHeight();
                
                // Fondo circular rojo
                g2.setColor(COLOR_DANGER);
                if (getModel().isRollover()) {
                    g2.setColor(COLOR_DANGER.brighter());
                }
                g2.fillOval(2, 2, w - 5, h - 5);
                
                // SÃ­mbolo de power blanco
                g2.setColor(java.awt.Color.WHITE);
                g2.setStroke(new java.awt.BasicStroke(2.2f));
                // Arco abierto arriba
                g2.drawArc(8, 8, w - 17, h - 17, 120, 300);
                // LÃ­nea vertical central
                g2.drawLine(w / 2, 6, w / 2, h / 2 - 2);
                g2.dispose();
            }
        };
        btnCerrar.setOpaque(false);
        btnCerrar.setContentAreaFilled(false);
        btnCerrar.setBorderPainted(false);
        btnCerrar.setFocusPainted(false);
        btnCerrar.setFocusable(false);
        btnCerrar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnCerrar.setToolTipText("Apagar / Salir del Sistema");
        btnCerrar.setPreferredSize(new java.awt.Dimension(32, 32));
        btnCerrar.setMinimumSize(new java.awt.Dimension(32, 32));
        btnCerrar.setMaximumSize(new java.awt.Dimension(32, 32));
        btnCerrar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_appview.tryToClose();
            }
        });
        
        // BotÃ³n de ConfiguraciÃ³n (con sÃ­mbolo de engranaje vectorizado grande)
        javax.swing.JButton btnConfig = new javax.swing.JButton() {
            @Override
            protected void paintComponent(java.awt.Graphics g) {
                java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
                g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(); int h = getHeight();
                
                int cx = w / 2; int cy = h / 2;
                int rOuter = 11; // Dientes del engranaje mÃ¡s grandes
                int rInner = 6;
                
                g2.setColor(new java.awt.Color(200, 200, 200));
                if (getModel().isRollover()) {
                    g2.setColor(COLOR_BRAND_ORANGE);
                }
                
                // Dibujar 8 dientes del engranaje
                g2.setStroke(new java.awt.BasicStroke(3.5f));
                for (int i = 0; i < 8; i++) {
                    double angle = Math.toRadians(i * 45);
                    int x1 = cx + (int)(rInner * Math.cos(angle));
                    int y1 = cy + (int)(rInner * Math.sin(angle));
                    int x2 = cx + (int)(rOuter * Math.cos(angle));
                    int y2 = cy + (int)(rOuter * Math.sin(angle));
                    g2.drawLine(x1, y1, x2, y2);
                }
                
                // Dibujar anillo exterior
                g2.setStroke(new java.awt.BasicStroke(2.5f));
                g2.drawOval(cx - rInner, cy - rInner, rInner * 2, rInner * 2);
                
                // Agujero central
                g2.setColor(new java.awt.Color(32, 32, 32)); // Color de fondo oscuro sutil
                g2.fillOval(cx - 3, cy - 3, 6, 6);
                g2.dispose();
            }
        };
        btnConfig.setOpaque(false);
        btnConfig.setContentAreaFilled(false);
        btnConfig.setBorderPainted(false);
        btnConfig.setFocusPainted(false);
        btnConfig.setFocusable(false);
        btnConfig.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnConfig.setToolTipText("ConfiguraciÃ³n del Sistema");
        btnConfig.setPreferredSize(new java.awt.Dimension(36, 36));
        btnConfig.setMinimumSize(new java.awt.Dimension(36, 36));
        btnConfig.setMaximumSize(new java.awt.Dimension(36, 36));
        btnConfig.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                if (m_appuser.hasPermission("com.openbravo.pos.config.JPanelConfiguration")) {
                    showTask("com.openbravo.pos.config.JPanelConfiguration");
                } else {
                    javax.swing.JOptionPane.showMessageDialog(null, 
                        "No tienes permiso para acceder a la configuraciÃ³n.", 
                        "Acceso Denegado", 
                        javax.swing.JOptionPane.WARNING_MESSAGE);
                }
            }
        });
        
        // Agregar componentes al panel en el orden solicitado:
        // BotÃ³n apagar, Bandera, Perfil, ConfiguraciÃ³n
        atendidoPanel.add(btnConfig);
        atendidoPanel.add(profileGroupPanel);
        atendidoPanel.add(flagWrapper);
        atendidoPanel.add(btnCerrar);
        
        rightTopPanel.add(atendidoPanel, java.awt.BorderLayout.CENTER);
        
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
     * Método helper para crear botones del menú de forma consistente
     */
    private javax.swing.JButton createMenuButton(String iconPath, String text, String taskClass) {
        return createMenuButton(iconPath, text, taskClass, null);
    }
    
    /**
     * Método helper para crear botones del menú con color personalizado
     */
    private javax.swing.JButton createMenuButton(String iconPath, String text, String taskClass, java.awt.Color backgroundColor) {
        javax.swing.JButton button = new javax.swing.JButton();
        // Iconos removidos para diseño compacto como eleventa
        button.setText(text);
        
        // Calcular ancho basado en la longitud del texto para que se lea bien
        int textLength = text.length();
        int buttonWidth = Math.max(60, Math.min(130, 30 + (textLength * 6))); // Mínimo 60, máximo 130, más proporcional al texto
        
        // Tamaño compacto y proporcional al texto
        button.setPreferredSize(new java.awt.Dimension(buttonWidth, 25));
        button.setMinimumSize(new java.awt.Dimension(60, 25));
        button.setMaximumSize(new java.awt.Dimension(130, 25));
        button.setFont(new java.awt.Font("Arial", java.awt.Font.PLAIN, 15)); // Tamaño de fuente aumentado para mejor legibilidad
        
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
        if (btnReportesRef != null) {
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
        
        LOGGER.log(Level.INFO, "✅ Atajos de teclado globales configurados: F1=Ventas, F2=Cerrar Caja, F3=Stock, F4=Reportes");
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
        if ("com.openbravo.pos.config.JPanelConfiguration".equals(sTaskClass)) {
            return new String[]{ "ConfiguraciÃ³n", "com.openbravo.pos.config.JPanelConfiguration" };
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
                link.setForeground(new java.awt.Color(243, 153, 18)); // COLOR_BRAND_ORANGE
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

}

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

package com.openbravo.pos.admin;

import com.openbravo.basic.BasicException;
import com.openbravo.beans.JPasswordDialog;
import com.openbravo.data.gui.ComboBoxValModel;
import com.openbravo.data.loader.SentenceList;
import com.openbravo.data.user.*;
import com.openbravo.format.Formats;
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.pos.util.StringUtils;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.image.BufferedImage;
import java.io.StringReader;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Vista de edición de usuarios con diseño mejorado
 *
 * @author adrianromero
 * @author Sebastian (mejoras UI)
 */
public class PeopleView extends JPanel implements EditorRecord<Object> {

        private static final long serialVersionUID = 1L;

        private String m_oId;
        private String m_sPassword;
        private String m_currentRoleId; // Track what role this user currently has

        private final DirtyManager m_Dirty;
        private final DataLogicAdmin dlAdmin;

        private final SentenceList<RoleInfo> m_sentrole;
        private ComboBoxValModel<RoleInfo> m_RoleModel;

        private final ComboBoxValModel<String> m_ReasonModel;

        // Panel de permisos con tabs
        private JTabbedPane permissionsTabbedPane;
        private final Map<String, JCheckBox> permissionCheckboxes = new HashMap<>();

        // Componentes UI principales
        private JTabbedPane jTabbedPane1;
        private JPanel generalPanel;
        private JPanel imagePanel;
        private JLabel jLabel1;
        private JTextField m_jName;
        private JCheckBox m_jVisible;
        private JLabel jLabel3;
        private JButton jButton1;
        private JLabel jLblCardID;
        private JTextField m_jcard;
        private JComboBox webCBSecurity;
        private JLabel jLabel6;
        private com.openbravo.data.gui.JImageEditor m_jImage;

        private JLabel jLblFirstName;
        private JTextField m_jFirstName;
        private JLabel jLblLastName;
        private JTextField m_jLastName;
        private JLabel jLblAge;
        private JTextField m_jAge;
        private JLabel jLblDocument;
        private JTextField m_jDocument;
        private JButton btn2FA;

        // Colores
        private static final Color PRIMARY_COLOR = new Color(41, 128, 185);
        private static final Color HEADER_BG = new Color(245, 245, 245);

        public PeopleView(DataLogicAdmin dlAdmin, DirtyManager dirty) {
                this.dlAdmin = dlAdmin;
                this.m_Dirty = dirty;

                m_sentrole = dlAdmin.getRolesList();
                m_RoleModel = new ComboBoxValModel<>();

                m_ReasonModel = new ComboBoxValModel<>();
                m_ReasonModel.add(AppLocal.getIntString("cboption.generate"));
                m_ReasonModel.add(AppLocal.getIntString("cboption.clear"));

                initComponents();
                createPermissionsTabs();

                cleanFields();
                disableFields();
        }

        private void initComponents() {
                jTabbedPane1 = new JTabbedPane();
                generalPanel = new JPanel();
                imagePanel = new JPanel();

                jLabel1 = new JLabel();
                m_jName = new JTextField();
                m_jVisible = new JCheckBox();
                jLabel3 = new JLabel();
                jButton1 = new JButton();
                jLblCardID = new JLabel();
                m_jcard = new JTextField();
                webCBSecurity = new JComboBox();
                jLabel6 = new JLabel();
                m_jImage = new com.openbravo.data.gui.JImageEditor();

                jLblFirstName = new JLabel();
                m_jFirstName = new JTextField();
                jLblLastName = new JLabel();
                m_jLastName = new JTextField();
                jLblAge = new JLabel();
                m_jAge = new JTextField();
                jLblDocument = new JLabel();
                m_jDocument = new JTextField();

                setFont(new Font("Arial", Font.PLAIN, 12));
                setPreferredSize(new Dimension(800, 600));

                jTabbedPane1.setMinimumSize(new Dimension(750, 550));
                jTabbedPane1.setPreferredSize(new Dimension(780, 580));

                // General Panel Layout
                generalPanel.setLayout(new BorderLayout(0, 15));
                generalPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
                generalPanel.setBackground(Color.WHITE);

                // Header Title
                JLabel headerTitle = new JLabel("GESTIÓN DE USUARIO");
                headerTitle.setFont(new Font("Arial", Font.BOLD, 22));
                headerTitle.setForeground(PRIMARY_COLOR);
                headerTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

                JPanel northContainer = new JPanel(new BorderLayout());
                northContainer.setBackground(Color.WHITE);
                northContainer.add(headerTitle, BorderLayout.NORTH);

                JPanel basicInfoPanel = createBasicInfoPanel();
                northContainer.add(basicInfoPanel, BorderLayout.CENTER);

                generalPanel.add(northContainer, BorderLayout.NORTH);

                // Permissions Tabs
                permissionsTabbedPane = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
                permissionsTabbedPane.setFont(new Font("Arial", Font.BOLD, 12));
                permissionsTabbedPane.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));

                generalPanel.add(permissionsTabbedPane, BorderLayout.CENTER);

                jTabbedPane1.addTab(AppLocal.getIntString("label.general"), generalPanel);

                // ─── IMAGE TAB: Rediseño estilo tarjeta de perfil moderna ───
                m_jImage.setFont(new Font("Arial", Font.PLAIN, 12));
                m_jImage.setPreferredSize(new Dimension(220, 220));
                m_jImage.addPropertyChangeListener("image", m_Dirty);

                // Fondo del tab con degradado claro
                imagePanel.setLayout(new BorderLayout());
                imagePanel.setBackground(new Color(241, 245, 249)); // slate-100 en lugar del oscuro
                imagePanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20)); // Padding alrededor de la tarjeta

                // Tarjeta central blanca con sombra simulada
                JPanel card = new JPanel(new BorderLayout(0, 0)) {
                    @Override protected void paintComponent(java.awt.Graphics g) {
                        java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
                        g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                        // Sombra
                        g2.setColor(new Color(0, 0, 0, 40));
                        g2.fillRoundRect(6, 6, getWidth() - 12, getHeight() - 12, 20, 20);
                        // Fondo blanco
                        g2.setColor(Color.WHITE);
                        g2.fillRoundRect(0, 0, getWidth() - 12, getHeight() - 12, 20, 20);
                        g2.dispose();
                    }
                };
                card.setOpaque(false);
                // Removemos el preferredSize fijo para que se expanda en el BorderLayout

                // ── Header de la tarjeta (banda de color) ──
                JPanel cardHeader = new JPanel(new BorderLayout()) {
                    @Override protected void paintComponent(java.awt.Graphics g) {
                        java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
                        g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                        java.awt.GradientPaint gp = new java.awt.GradientPaint(
                            0, 0, new Color(234, 120, 12),  // naranja
                            getWidth(), 0, new Color(251, 146, 60) // naranja claro
                        );
                        g2.setPaint(gp);
                        g2.fillRoundRect(0, 0, getWidth(), getHeight() + 20, 20, 20);
                        g2.dispose();
                    }
                };
                cardHeader.setOpaque(false);
                cardHeader.setPreferredSize(new Dimension(750, 70));
                cardHeader.setBorder(BorderFactory.createEmptyBorder(15, 25, 15, 25));

                JLabel headerLabel = new JLabel("Perfil de Usuario");
                headerLabel.setFont(new Font("Arial", Font.BOLD, 18));
                headerLabel.setForeground(Color.WHITE);
                JLabel subLabel = new JLabel("Fotografía e información personal");
                subLabel.setFont(new Font("Arial", Font.PLAIN, 12));
                subLabel.setForeground(new Color(255, 255, 255, 200));
                JPanel headerText = new JPanel();
                headerText.setOpaque(false);
                headerText.setLayout(new javax.swing.BoxLayout(headerText, javax.swing.BoxLayout.Y_AXIS));
                headerText.add(headerLabel);
                headerText.add(subLabel);
                cardHeader.add(headerText, BorderLayout.CENTER);

                // ── Cuerpo de la tarjeta ──
                JPanel cardBody = new JPanel(new BorderLayout(30, 0));
                cardBody.setOpaque(false);
                cardBody.setBorder(BorderFactory.createEmptyBorder(25, 30, 25, 30));

                // ── Lado izquierdo: foto ──
                JPanel leftPanel = new JPanel();
                leftPanel.setOpaque(false);
                leftPanel.setLayout(new javax.swing.BoxLayout(leftPanel, javax.swing.BoxLayout.Y_AXIS));
                leftPanel.setPreferredSize(new Dimension(250, 300));

                // Marco estilizado para la foto
                JPanel photoFrame = new JPanel(new BorderLayout()) {
                    @Override protected void paintComponent(java.awt.Graphics g) {
                        java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
                        g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                        g2.setColor(new Color(241, 245, 249)); // slate-100
                        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                        g2.setColor(new Color(226, 232, 240)); // slate-200
                        g2.setStroke(new java.awt.BasicStroke(1.5f));
                        g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 16, 16);
                        g2.dispose();
                    }
                };
                photoFrame.setOpaque(false);
                photoFrame.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
                photoFrame.add(m_jImage, BorderLayout.CENTER);

                JLabel photoLabel = new JLabel("Foto de Perfil", JLabel.CENTER);
                photoLabel.setFont(new Font("Arial", Font.BOLD, 12));
                photoLabel.setForeground(new Color(100, 116, 139)); // slate-500
                photoLabel.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));
                photoLabel.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);
                photoFrame.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);

                leftPanel.add(photoFrame);
                leftPanel.add(photoLabel);

                // ── Lado derecho: datos personales ──
                JPanel rightPanel = new JPanel(new GridBagLayout());
                rightPanel.setOpaque(false);

                // Helper para crear un campo estilo Material
                java.util.function.BiFunction<String, JTextField, JPanel> makeField = (labelText, field) -> {
                    JPanel fp = new JPanel(new BorderLayout(0, 4));
                    fp.setOpaque(false);

                    JLabel lbl = new JLabel(labelText.toUpperCase());
                    lbl.setFont(new Font("Arial", Font.BOLD, 10));
                    lbl.setForeground(new Color(100, 116, 139)); // slate-500

                    field.setFont(new Font("Arial", Font.PLAIN, 15));
                    field.setForeground(new Color(30, 41, 59));
                    field.setBackground(Color.WHITE);
                    field.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(234, 120, 12)),
                        BorderFactory.createEmptyBorder(6, 4, 4, 4)
                    ));
                    field.setPreferredSize(new Dimension(200, 36));
                    field.setOpaque(true);

                    fp.add(lbl, BorderLayout.NORTH);
                    fp.add(field, BorderLayout.CENTER);
                    return fp;
                };

                // Configurar campos
                jLblFirstName.setText("Nombres:");
                m_jFirstName.getDocument().addDocumentListener(m_Dirty);
                jLblLastName.setText("Apellidos:");
                m_jLastName.getDocument().addDocumentListener(m_Dirty);
                jLblAge.setText("Edad:");
                m_jAge.setPreferredSize(new Dimension(100, 36));
                m_jAge.getDocument().addDocumentListener(m_Dirty);
                jLblDocument.setText("Identificaci\u00f3n:");
                m_jDocument.getDocument().addDocumentListener(m_Dirty);

                // Título sección
                JLabel sectionTitle = new JLabel("Información Personal");
                sectionTitle.setFont(new Font("Arial", Font.BOLD, 16));
                sectionTitle.setForeground(new Color(30, 41, 59));

                JSeparator sep = new JSeparator();
                sep.setForeground(new Color(226, 232, 240));
                sep.setBackground(new Color(226, 232, 240));

                GridBagConstraints rc = new GridBagConstraints();
                rc.fill = GridBagConstraints.HORIZONTAL;
                rc.weightx = 1.0;
                rc.insets = new Insets(0, 0, 0, 0);

                rc.gridx = 0; rc.gridy = 0; rc.gridwidth = 2; rc.insets = new Insets(0, 0, 6, 0);
                rightPanel.add(sectionTitle, rc);
                rc.gridy = 1; rc.insets = new Insets(0, 0, 20, 0);
                rightPanel.add(sep, rc);
                rc.gridwidth = 1;

                // Nombres - fila 2 col 0
                rc.gridx = 0; rc.gridy = 2; rc.insets = new Insets(0, 0, 16, 12);
                rc.weightx = 0.5;
                rightPanel.add(makeField.apply("Nombres", m_jFirstName), rc);

                // Apellidos - fila 2 col 1
                rc.gridx = 1; rc.gridy = 2;
                rightPanel.add(makeField.apply("Apellidos", m_jLastName), rc);

                // Edad - fila 3 col 0
                rc.gridx = 0; rc.gridy = 3; rc.insets = new Insets(0, 0, 16, 12);
                rc.weightx = 0.3;
                JPanel agePanel = makeField.apply("Edad", m_jAge);
                m_jAge.setPreferredSize(new Dimension(100, 36));
                rightPanel.add(agePanel, rc);

                // Identificación - fila 3 col 1
                rc.gridx = 1; rc.gridy = 3; rc.weightx = 0.7;
                rightPanel.add(makeField.apply("No. Identificaci\u00f3n", m_jDocument), rc);

                // Filler
                rc.gridx = 0; rc.gridy = 4; rc.gridwidth = 2; rc.weighty = 1.0;
                rc.fill = GridBagConstraints.BOTH;
                rc.insets = new Insets(0, 0, 0, 0);
                rightPanel.add(new JPanel() {{ setOpaque(false); }}, rc);

                cardBody.add(leftPanel, BorderLayout.WEST);
                cardBody.add(rightPanel, BorderLayout.CENTER);

                card.add(cardHeader, BorderLayout.NORTH);
                card.add(cardBody, BorderLayout.CENTER);

                imagePanel.add(card);

                jTabbedPane1.addTab(AppLocal.getIntString("label.peopleimage"), imagePanel);


                setLayout(new BorderLayout());
                add(jTabbedPane1, BorderLayout.CENTER);
        }

        private JPanel createBasicInfoPanel() {
                JPanel panel = new JPanel(new GridBagLayout());
                panel.setBackground(HEADER_BG);
                panel.setBorder(BorderFactory.createCompoundBorder(
                                BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
                                BorderFactory.createEmptyBorder(15, 20, 15, 20)));

                GridBagConstraints gbc = new GridBagConstraints();
                gbc.insets = new Insets(8, 10, 8, 10);
                gbc.anchor = GridBagConstraints.WEST;
                gbc.fill = GridBagConstraints.NONE; // FIX: No stretch

                // Row 1: Usuario & Clave
                jLabel1.setFont(new Font("Arial", Font.BOLD, 14));
                jLabel1.setForeground(new Color(80, 80, 80));
                jLabel1.setText(AppLocal.getIntString("label.peoplenamem")); // Usuario
                jLabel1.addMouseListener(new java.awt.event.MouseAdapter() {
                        public void mouseClicked(java.awt.event.MouseEvent evt) {
                                jLabel1MouseClicked(evt);
                        }
                });

                m_jName.setFont(new Font("Arial", Font.PLAIN, 14));
                m_jName.setPreferredSize(new Dimension(220, 30));
                m_jName.getDocument().addDocumentListener(m_Dirty);

                jLabel6.setFont(new Font("Arial", Font.BOLD, 14));
                jLabel6.setForeground(new Color(80, 80, 80));
                jLabel6.setText(AppLocal.getIntString("label.Password"));

                jButton1.setFont(new Font("Arial", Font.PLAIN, 12));
                jButton1.setIcon(new ImageIcon(getClass().getResource("/com/openbravo/images/password.png")));
                jButton1.setText(AppLocal.getIntString("button.peoplepassword"));
                jButton1.setPreferredSize(new Dimension(160, 30));
                jButton1.addActionListener(evt -> jButton1ActionPerformed(evt));

                gbc.gridx = 0;
                gbc.gridy = 0;
                gbc.weightx = 0;
                panel.add(jLabel1, gbc);

                gbc.gridx = 1;
                gbc.gridy = 0;
                gbc.weightx = 0; // FIX: Weight 0
                panel.add(m_jName, gbc);

                gbc.gridx = 2;
                gbc.gridy = 0;
                gbc.weightx = 0;
                gbc.insets = new Insets(8, 30, 8, 10);
                panel.add(jLabel6, gbc);

                gbc.gridx = 3;
                gbc.gridy = 0;
                gbc.weightx = 0; // FIX: Weight 0
                gbc.insets = new Insets(8, 10, 8, 10);
                panel.add(jButton1, gbc);

                // Filler for Row 1 to push content left
                GridBagConstraints gbcFiller = new GridBagConstraints();
                gbcFiller.gridx = 4;
                gbcFiller.gridy = 0;
                gbcFiller.weightx = 1.0;
                gbcFiller.fill = GridBagConstraints.HORIZONTAL;
                panel.add(new JPanel() {
                        {
                                setOpaque(false);
                        }
                }, gbcFiller);

                // Row 2: Visible & Tarjeta
                jLabel3.setFont(new Font("Arial", Font.BOLD, 14));
                jLabel3.setForeground(new Color(80, 80, 80));
                jLabel3.setText(AppLocal.getIntString("label.peoplevisible"));

                m_jVisible.setFont(new Font("Arial", Font.PLAIN, 12));
                m_jVisible.setBackground(HEADER_BG);
                m_jVisible.addActionListener(m_Dirty);

                jLblCardID.setFont(new Font("Arial", Font.BOLD, 14));
                jLblCardID.setForeground(new Color(80, 80, 80));
                jLblCardID.setText(AppLocal.getIntString("label.card"));

                m_jcard.setFont(new Font("Arial", Font.PLAIN, 14));
                m_jcard.setPreferredSize(new Dimension(150, 30));
                m_jcard.getDocument().addDocumentListener(m_Dirty);

                webCBSecurity.setFont(new Font("Arial", Font.PLAIN, 12));
                webCBSecurity.setModel(m_ReasonModel);
                webCBSecurity.setPreferredSize(new Dimension(100, 30));
                webCBSecurity.addActionListener(evt -> webCBSecurityActionPerformed(evt));

                gbc.gridx = 0;
                gbc.gridy = 1;
                gbc.weightx = 0;
                gbc.insets = new Insets(8, 10, 8, 10);
                panel.add(jLabel3, gbc);

                gbc.gridx = 1;
                gbc.gridy = 1;
                gbc.weightx = 0; // FIX: Weight 0
                panel.add(m_jVisible, gbc);

                gbc.gridx = 2;
                gbc.gridy = 1;
                gbc.weightx = 0;
                gbc.insets = new Insets(8, 30, 8, 10);
                panel.add(jLblCardID, gbc);

                gbc.gridx = 3;
                gbc.gridy = 1;
                gbc.weightx = 0; // FIX: Weight 0
                gbc.insets = new Insets(8, 10, 8, 10);
                JPanel cardPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
                cardPanel.setBackground(HEADER_BG);
                cardPanel.add(m_jcard);
                cardPanel.add(webCBSecurity);
                panel.add(cardPanel, gbc);

                btn2FA = new JButton("Configurar 2FA");
                btn2FA.setFont(new Font("Arial", Font.PLAIN, 12));
                btn2FA.addActionListener(evt -> configure2FA());
                
                gbc.gridx = 4;
                gbc.gridy = 1;
                gbc.weightx = 1.0;
                gbc.anchor = GridBagConstraints.WEST;
                panel.add(btn2FA, gbc);

                return panel;
        }

        private void createPermissionsTabs() {
                Map<String, List<PermissionInfo>> allPermissions = PermissionsCatalog.getAllPermissions();

                for (Map.Entry<String, List<PermissionInfo>> entry : allPermissions.entrySet()) {
                        String category = entry.getKey();
                        List<PermissionInfo> permissions = entry.getValue();

                        // Panel para la pestaña
                        JPanel tabContent = new JPanel(new BorderLayout());
                        tabContent.setBackground(Color.WHITE);
                        tabContent.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

                        // Opciones de acción masiva
                        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
                        actionsPanel.setBackground(Color.WHITE);

                        JButton selectAllBtn = new JButton("Seleccionar Todo");
                        selectAllBtn.setFont(new Font("Arial", Font.PLAIN, 11));
                        selectAllBtn.setBackground(new Color(230, 240, 250));
                        selectAllBtn.addActionListener(e -> {
                                for (PermissionInfo perm : permissions) {
                                        JCheckBox cb = permissionCheckboxes.get(perm.getClassName());
                                        if (cb != null)
                                                cb.setSelected(true);
                                }
                                m_Dirty.setDirty(true);
                        });

                        JButton deselectAllBtn = new JButton("Ninguno");
                        deselectAllBtn.setFont(new Font("Arial", Font.PLAIN, 11));
                        deselectAllBtn.setBackground(new Color(250, 230, 230));
                        deselectAllBtn.addActionListener(e -> {
                                for (PermissionInfo perm : permissions) {
                                        JCheckBox cb = permissionCheckboxes.get(perm.getClassName());
                                        if (cb != null)
                                                cb.setSelected(false);
                                }
                                m_Dirty.setDirty(true);
                        });

                        actionsPanel.add(selectAllBtn);
                        actionsPanel.add(deselectAllBtn);
                        tabContent.add(actionsPanel, BorderLayout.NORTH);

                        // Checkboxes en Grid
                        JPanel checksPanel = new JPanel(new GridLayout(0, 2, 10, 5)); // 2 columnas
                        checksPanel.setBackground(Color.WHITE);

                        for (PermissionInfo perm : permissions) {
                                JCheckBox checkBox = new JCheckBox(perm.getDisplayName());
                                checkBox.setFont(new Font("Arial", Font.PLAIN, 13));
                                checkBox.setBackground(Color.WHITE);
                                checkBox.setCursor(new Cursor(Cursor.HAND_CURSOR));
                                checkBox.addActionListener(e -> m_Dirty.setDirty(true));

                                permissionCheckboxes.put(perm.getClassName(), checkBox);
                                checksPanel.add(checkBox);
                        }

                        // Wrapper para alinear arriba
                        JPanel checksWrapper = new JPanel(new BorderLayout());
                        checksWrapper.setBackground(Color.WHITE);
                        checksWrapper.add(checksPanel, BorderLayout.NORTH);

                        JScrollPane scrollPane = new JScrollPane(checksWrapper);
                        scrollPane.setBorder(null);
                        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

                        tabContent.add(scrollPane, BorderLayout.CENTER);

                        permissionsTabbedPane.addTab(category, tabContent);
                }
        }

        private void cleanFields() {
                m_oId = null;
                m_sPassword = null;
                m_currentRoleId = null;
                m_jName.setText(null);
                m_jVisible.setSelected(false);
                m_jcard.setText(null);
                m_jImage.setImage(null);
                m_jFirstName.setText(null);
                m_jLastName.setText(null);
                m_jAge.setText(null);
                m_jDocument.setText(null);

                for (JCheckBox cb : permissionCheckboxes.values()) {
                        cb.setSelected(false);
                }

                if (permissionsTabbedPane.getTabCount() > 0) {
                        permissionsTabbedPane.setSelectedIndex(0);
                }
        }

        private void disableFields() {
                m_jName.setEnabled(false);
                m_jVisible.setEnabled(false);
                m_jcard.setEnabled(false);
                m_jImage.setEnabled(false);
                jButton1.setEnabled(false);
                webCBSecurity.setEnabled(false);
                m_jFirstName.setEnabled(false);
                m_jLastName.setEnabled(false);
                m_jAge.setEnabled(false);
                m_jDocument.setEnabled(false);
                if (btn2FA != null) btn2FA.setEnabled(false);
                setPermissionsEnabled(false);
                permissionsTabbedPane.setEnabled(false);
        }

        private void enableFields() {
                m_jName.setEnabled(true);
                m_jVisible.setEnabled(true);
                m_jcard.setEnabled(true);
                m_jImage.setEnabled(true);
                jButton1.setEnabled(true);
                webCBSecurity.setEnabled(true);
                m_jFirstName.setEnabled(true);
                m_jLastName.setEnabled(true);
                m_jAge.setEnabled(true);
                m_jDocument.setEnabled(true);
                if (btn2FA != null) btn2FA.setEnabled(true);
                setPermissionsEnabled(true);
                permissionsTabbedPane.setEnabled(true);
        }

        private void setPermissionsEnabled(boolean enabled) {
                for (JCheckBox cb : permissionCheckboxes.values()) {
                        cb.setEnabled(enabled);
                }
        }

        @Override
        public void writeValueEOF() {
                cleanFields();
                disableFields();
        }

        @Override
        public void writeValueInsert() {
                cleanFields();
                m_oId = UUID.randomUUID().toString();
                m_jVisible.setSelected(true);
                enableFields();
        }

        @Override
        public void writeValueDelete(Object value) {
                Object[] people = (Object[]) value;
                m_oId = (String) people[0];
                m_jName.setText(Formats.STRING.formatValue((String) people[1]));
                m_sPassword = Formats.STRING.formatValue((String) people[2]);
                m_jVisible.setSelected(((Boolean) people[4]));
                m_jcard.setText(Formats.STRING.formatValue((String) people[5]));
                m_jImage.setImage((BufferedImage) people[6]);

                m_jFirstName.setText(people.length > 11 && people[11] != null ? Formats.STRING.formatValue((String) people[11]) : "");
                m_jLastName.setText(people.length > 12 && people[12] != null ? Formats.STRING.formatValue((String) people[12]) : "");
                m_jAge.setText(people.length > 13 && people[13] != null ? Formats.INT.formatValue((Integer) people[13]) : "");
                m_jDocument.setText(people.length > 14 && people[14] != null ? Formats.STRING.formatValue((String) people[14]) : "");

                m_currentRoleId = (String) people[3];
                loadPermissionsFromRole(m_currentRoleId);

                disableFields();
        }

        @Override
        public void writeValueEdit(Object value) {
                Object[] people = (Object[]) value;
                m_oId = (String) people[0];
                m_jName.setText(Formats.STRING.formatValue((String) people[1]));
                m_sPassword = Formats.STRING.formatValue((String) people[2]);
                m_jVisible.setSelected(((Boolean) people[4]));
                m_jcard.setText(Formats.STRING.formatValue((String) people[5]));
                m_jImage.setImage((BufferedImage) people[6]);

                m_jFirstName.setText(people.length > 11 && people[11] != null ? Formats.STRING.formatValue((String) people[11]) : "");
                m_jLastName.setText(people.length > 12 && people[12] != null ? Formats.STRING.formatValue((String) people[12]) : "");
                m_jAge.setText(people.length > 13 && people[13] != null ? Formats.INT.formatValue((Integer) people[13]) : "");
                m_jDocument.setText(people.length > 14 && people[14] != null ? Formats.STRING.formatValue((String) people[14]) : "");

                if (m_jcard.getText() != null && m_jcard.getText().length() == 16) {
                        jLblCardID.setText(AppLocal.getIntString("label.ibutton"));
                } else {
                        jLblCardID.setText(AppLocal.getIntString("label.card"));
                }

                m_currentRoleId = (String) people[3];
                loadPermissionsFromRole(m_currentRoleId);

                enableFields();
        }

        private void loadPermissionsFromRole(String roleId) {
                if (roleId == null) {
                        return;
                }

                try {
                        Object roleData = new com.openbravo.data.loader.StaticSentence(
                                        dlAdmin.getSession(),
                                        "SELECT PERMISSIONS FROM ROLES WHERE ID = ?",
                                        new com.openbravo.data.loader.SerializerWriteBasic(
                                                        new com.openbravo.data.loader.Datas[] {
                                                                        com.openbravo.data.loader.Datas.STRING }),
                                        new com.openbravo.data.loader.SerializerReadBasic(
                                                        new com.openbravo.data.loader.Datas[] {
                                                                        com.openbravo.data.loader.Datas.BYTES }))
                                        .find(roleId);

                        if (roleData != null) {
                                Object[] record = (Object[]) roleData;
                                if (record[0] != null) {
                                        String xml = Formats.BYTEA.formatValue((byte[]) record[0]);
                                        loadPermissionsFromXML(xml);
                                }
                        }
                } catch (BasicException e) {
                        System.err.println("Error al cargar permisos del rol: " + e.getMessage());
                }
        }

        private void loadPermissionsFromXML(String xml) {
                if (xml == null || xml.isEmpty()) {
                        return;
                }

                for (JCheckBox cb : permissionCheckboxes.values()) {
                        cb.setSelected(false);
                }

                try {
                        SAXParserFactory factory = SAXParserFactory.newInstance();
                        SAXParser saxParser = factory.newSAXParser();

                        DefaultHandler handler = new DefaultHandler() {
                                @Override
                                public void startElement(String uri, String localName, String qName,
                                                Attributes attributes) {
                                        if ("class".equals(qName)) {
                                                String className = attributes.getValue("name");
                                                if (className != null) {
                                                        JCheckBox cb = permissionCheckboxes.get(className);
                                                        if (cb != null) {
                                                                cb.setSelected(true);
                                                        }
                                                }
                                        }
                                }
                        };

                        saxParser.parse(new InputSource(new StringReader(xml)), handler);
                } catch (Exception e) {
                        System.err.println("Error al parsear XML de permisos: " + e.getMessage());
                }
        }

        private String generatePermissionsXML() {
                StringBuilder xml = new StringBuilder();
                xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
                xml.append("<permissions>\n");

                for (Map.Entry<String, JCheckBox> entry : permissionCheckboxes.entrySet()) {
                        if (entry.getValue().isSelected()) {
                                xml.append("  <class name=\"").append(entry.getKey()).append("\"/>\n");
                        }
                }

                xml.append("</permissions>");
                return xml.toString();
        }

        @Override
        public Object createValue() throws BasicException {
                Object[] people = new Object[15];
                String cardText = m_jcard.getText();
                boolean hasCard = cardText != null && cardText.length() > 0;
                String computedId = hasCard ? cardText : (m_oId == null ? UUID.randomUUID().toString() : m_oId);

                // Guardar permisos en un rol personalizado para este usuario
                String roleId = savePermissionsToCustomRole(computedId, m_jName.getText());

                people[0] = computedId;
                people[1] = Formats.STRING.parseValue(m_jName.getText());
                people[2] = Formats.STRING.parseValue(m_sPassword);
                people[3] = roleId; // Rol personalizado con los permisos seleccionados
                people[4] = m_jVisible.isSelected();
                people[5] = Formats.STRING.parseValue(cardText);
                people[6] = m_jImage.getImage();
                people[7] = null;
                people[8] = null;
                people[9] = null;
                people[10] = null;
                
                people[11] = Formats.STRING.parseValue(m_jFirstName.getText());
                people[12] = Formats.STRING.parseValue(m_jLastName.getText());
                people[13] = Formats.INT.parseValue(m_jAge.getText());
                people[14] = Formats.STRING.parseValue(m_jDocument.getText());

                return people;
        }

        /**
         * Guarda los permisos seleccionados en un rol personalizado para este usuario.
         * Si el usuario ya tiene un rol custom, lo actualiza. Si no, crea uno nuevo.
         * Si tiene un rol estándar (1=ADMIN, 2=MANAGER, 3=Employee), crea uno nuevo.
         */
        private String savePermissionsToCustomRole(String userId, String userName) throws BasicException {
                String permXml = generatePermissionsXML();
                // We keep the rest of this method unchanged, just adding configure2FA below it

                byte[] permBytes = permXml.getBytes(java.nio.charset.StandardCharsets.UTF_8);

                // Determinar el roleId a usar
                String roleId = m_currentRoleId;
                boolean isStandardRole = roleId == null || "1".equals(roleId) || "2".equals(roleId)
                                || "3".equals(roleId);

                if (isStandardRole) {
                        // Crear un rol personalizado con ID único para este usuario
                        roleId = "custom_" + userId;
                }

                String roleName = "Custom_" + (userName != null ? userName : userId);

                // Verificar si el rol ya existe
                Object existing = new com.openbravo.data.loader.StaticSentence(
                                dlAdmin.getSession(),
                                "SELECT ID FROM roles WHERE ID = ?",
                                new com.openbravo.data.loader.SerializerWriteBasic(
                                                new com.openbravo.data.loader.Datas[] {
                                                                com.openbravo.data.loader.Datas.STRING }),
                                new com.openbravo.data.loader.SerializerReadBasic(
                                                new com.openbravo.data.loader.Datas[] {
                                                                com.openbravo.data.loader.Datas.STRING }))
                                .find(roleId);

                if (existing != null) {
                        // Actualizar el rol existente con los nuevos permisos
                        new com.openbravo.data.loader.PreparedSentence(
                                        dlAdmin.getSession(),
                                        "UPDATE roles SET NAME = ?, PERMISSIONS = ? WHERE ID = ?",
                                        new com.openbravo.data.loader.SerializerWriteBasic(
                                                        new com.openbravo.data.loader.Datas[] {
                                                                        com.openbravo.data.loader.Datas.STRING,
                                                                        com.openbravo.data.loader.Datas.BYTES,
                                                                        com.openbravo.data.loader.Datas.STRING }))
                                        .exec(new Object[] { roleName, permBytes, roleId });
                } else {
                        // Insertar nuevo rol
                        new com.openbravo.data.loader.PreparedSentence(
                                        dlAdmin.getSession(),
                                        "INSERT INTO roles (ID, NAME, PERMISSIONS) VALUES (?, ?, ?)",
                                        new com.openbravo.data.loader.SerializerWriteBasic(
                                                        new com.openbravo.data.loader.Datas[] {
                                                                        com.openbravo.data.loader.Datas.STRING,
                                                                        com.openbravo.data.loader.Datas.STRING,
                                                                        com.openbravo.data.loader.Datas.BYTES }))
                                        .exec(new Object[] { roleId, roleName, permBytes });
                }

                m_currentRoleId = roleId;
                return roleId;
        }

        @Override
        public Component getComponent() {
                return this;
        }

        public void activate() throws BasicException {
                m_RoleModel = new ComboBoxValModel<>(m_sentrole.list());
        }

        @Override
        public void refresh() {
        }

        private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {
                String sNewPassword = JPasswordDialog.changePassword(this);
                if (sNewPassword != null) {
                        m_sPassword = sNewPassword;
                        m_Dirty.setDirty(true);
                }
        }

        private void webCBSecurityActionPerformed(java.awt.event.ActionEvent evt) {
                if (webCBSecurity.getSelectedIndex() == 0) {
                        if (JOptionPane.showConfirmDialog(this,
                                        AppLocal.getIntString("message.cardnew"),
                                        AppLocal.getIntString("title.editor"),
                                        JOptionPane.YES_NO_OPTION,
                                        JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
                                m_jcard.setText("C" + StringUtils.getCardNumber());
                                m_Dirty.setDirty(true);
                        }
                }

                if (webCBSecurity.getSelectedIndex() == 1) {
                        if (JOptionPane.showConfirmDialog(this,
                                        AppLocal.getIntString("message.cardremove"),
                                        AppLocal.getIntString("title.editor"),
                                        JOptionPane.YES_NO_OPTION,
                                        JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
                                m_jcard.setText(null);
                                m_Dirty.setDirty(true);
                        }
                }
        }

        private void jLabel1MouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                        String uuidString = m_oId.toString();
                        StringSelection stringSelection = new StringSelection(uuidString);
                        Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
                        clpbrd.setContents(stringSelection, null);

                        JOptionPane.showMessageDialog(null,
                                        AppLocal.getIntString("message.uuidcopy"));
                }
        }
        private void configure2FA() {
                if (m_oId == null) {
                        JOptionPane.showMessageDialog(this, "Debe guardar el usuario primero antes de configurar 2FA.");
                        return;
                }
                try {
                        Object rowData = new com.openbravo.data.loader.StaticSentence(dlAdmin.getSession(),
                                        "SELECT TOTP_SECRET FROM PEOPLE WHERE ID = ?",
                                        com.openbravo.data.loader.SerializerWriteString.INSTANCE,
                                        new com.openbravo.data.loader.SerializerReadBasic(
                                                        new com.openbravo.data.loader.Datas[] { com.openbravo.data.loader.Datas.STRING }))
                                        .find(m_oId);

                        String secret = null;
                        if (rowData != null) {
                                Object[] row = (Object[]) rowData;
                                if (row[0] != null) {
                                        secret = (String) row[0];
                                }
                        }

                        if (secret != null && !secret.isEmpty()) {
                                int res = JOptionPane.showConfirmDialog(this,
                                                "El usuario ya tiene 2FA configurado. ¿Desea desactivarlo?", "2FA",
                                                JOptionPane.YES_NO_OPTION);
                                if (res == JOptionPane.YES_OPTION) {
                                        new com.openbravo.data.loader.StaticSentence(dlAdmin.getSession(),
                                                        "UPDATE PEOPLE SET TOTP_SECRET = NULL WHERE ID = ?",
                                                        com.openbravo.data.loader.SerializerWriteString.INSTANCE).exec(m_oId);
                                        JOptionPane.showMessageDialog(this, "2FA desactivado exitosamente.");
                                }
                                return;
                        }

                        com.warrenstrange.googleauth.GoogleAuthenticator gAuth = new com.warrenstrange.googleauth.GoogleAuthenticator();
                        final com.warrenstrange.googleauth.GoogleAuthenticatorKey key = gAuth.createCredentials();
                        String newSecret = key.getKey();

                        String appName = "KriolOS";
                        String userName = m_jName.getText() != null ? m_jName.getText().replaceAll(" ", "") : "User";
                        String otpAuthUrl = String.format("otpauth://totp/%s:%s?secret=%s&issuer=%s", appName, userName,
                                        newSecret, appName);

                        com.google.zxing.qrcode.QRCodeWriter qrCodeWriter = new com.google.zxing.qrcode.QRCodeWriter();
                        com.google.zxing.common.BitMatrix bitMatrix = qrCodeWriter.encode(otpAuthUrl,
                                        com.google.zxing.BarcodeFormat.QR_CODE, 200, 200);
                        BufferedImage qrImage = com.google.zxing.client.j2se.MatrixToImageWriter.toBufferedImage(bitMatrix);

                        JPanel panel = new JPanel(new BorderLayout(10, 10));
                        JLabel lblQr = new JLabel(new ImageIcon(qrImage));
                        lblQr.setHorizontalAlignment(SwingConstants.CENTER);
                        panel.add(lblQr, BorderLayout.CENTER);

                        JPanel bottom = new JPanel(new BorderLayout(5, 5));
                        bottom.add(new JLabel("Escanee el código y escriba el PIN de 6 dígitos:"), BorderLayout.NORTH);
                        JTextField txtCode = new JTextField();
                        txtCode.setFont(new Font("Arial", Font.BOLD, 18));
                        txtCode.setHorizontalAlignment(JTextField.CENTER);
                        bottom.add(txtCode, BorderLayout.CENTER);
                        panel.add(bottom, BorderLayout.SOUTH);

                        int option = JOptionPane.showConfirmDialog(this, panel, "Configurar Google Authenticator",
                                        JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
                        if (option == JOptionPane.OK_OPTION) {
                                String code = txtCode.getText().trim();
                                try {
                                        int pin = Integer.parseInt(code);
                                        if (gAuth.authorize(newSecret, pin)) {
                                                new com.openbravo.data.loader.StaticSentence(dlAdmin.getSession(),
                                                                "UPDATE PEOPLE SET TOTP_SECRET = ? WHERE ID = ?",
                                                                new com.openbravo.data.loader.SerializerWriteBasic(
                                                                                new com.openbravo.data.loader.Datas[] {
                                                                                                com.openbravo.data.loader.Datas.STRING,
                                                                                                com.openbravo.data.loader.Datas.STRING }))
                                                                .exec(new Object[] { newSecret, m_oId });
                                                JOptionPane.showMessageDialog(this, "2FA Activado correctamente.");
                                        } else {
                                                JOptionPane.showMessageDialog(this, "Código incorrecto. No se activó el 2FA.",
                                                                "Error", JOptionPane.ERROR_MESSAGE);
                                        }
                                } catch (Exception ex) {
                                        JOptionPane.showMessageDialog(this, "Código inválido.", "Error",
                                                        JOptionPane.ERROR_MESSAGE);
                                }
                        }
                } catch (Exception e) {
                        e.printStackTrace();
                        JOptionPane.showMessageDialog(this, "Error al configurar 2FA: " + e.getMessage(), "Error",
                                        JOptionPane.ERROR_MESSAGE);
                }
        }
}

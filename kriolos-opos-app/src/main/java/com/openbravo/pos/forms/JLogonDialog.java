/*
 * JLogonDialog.java
 * Diálogo de inicio de sesión personalizado
 */
package com.openbravo.pos.forms;

import com.openbravo.basic.BasicException;
import com.openbravo.data.gui.MessageInf;
import com.openbravo.data.loader.Session;
import com.openbravo.data.loader.StaticSentence;
import com.openbravo.data.loader.SerializerWriteBasic;
import com.openbravo.data.loader.SerializerReadBasic;
import com.openbravo.data.loader.Datas;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import com.openbravo.pos.util.Hashcypher;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import java.io.File;

/**
 * Diálogo de login con diseño premium
 * 
 * @author Sebastian
 */
public class JLogonDialog extends JDialog {

    private static final Logger LOGGER = Logger.getLogger(JLogonDialog.class.getName());

    // Colores corporativos (Restaurando Tema Blanco Institucional)
    private static final Color BG_DEEP_GREEN = new Color(51, 98, 140);    // Azul Institucional (reemplaza Verde profundo)
    private static final Color BRAND_SOFT_GREEN = Color.WHITE;             // Fondo Blanco (reemplaza Verde Menta)
    private static final Color BRAND_ACCENT_GREEN = new Color(46, 125, 50); // Verde Esmeralda (Mantener para acentos sutiles)
    private static final Color PANEL_BG = BRAND_SOFT_GREEN; 
    private static final Color TEXT_DARK = new Color(33, 33, 33);
    private static final Color BORDER_COLOR = BRAND_ACCENT_GREEN;
    private static final Color BACKGROUND_DARK = new Color(250, 247, 242);

    private final DataLogicSystem m_dlSystem;
    private final Session m_session;

    private JComboBox<String> cmbUsername;
    private JPasswordField txtPassword;
    private JButton btnLogin;
    private JButton btnExit;
    private AppUser loggedUser = null;

    public JLogonDialog(java.awt.Frame parent, DataLogicSystem dlSystem, Session session) {
        super(parent, "Comenzar Nuevo Turno", true);
        this.m_dlSystem = dlSystem;
        this.m_session = session;

        initComponents();
        setupDialog();
        loadRecentUsers();
    }

    private void initComponents() {
        // Usar BorderLayout para el diálogo
        setLayout(new BorderLayout());
        JPanel mainPanel = new JPanel();
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setLayout(new GridBagLayout());

        // Contenedor del login (El cuadro luminoso)
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBackground(PANEL_BG); 
        contentPanel.setBorder(BorderFactory.createEmptyBorder(15, 60, 15, 60));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 0, 0);

        // LOGO: Cargar el logo PNG y escalarlo proporcionalmente
        JLabel lblLogo = new JLabel();
        lblLogo.setHorizontalAlignment(SwingConstants.CENTER);
        try {
            java.net.URL logoResource = getClass().getResource("/com/openbravo/images/logo-voltium.png");
            if (logoResource != null) {
                Image img = javax.imageio.ImageIO.read(logoResource);
                int imgW = img.getWidth(null);
                int imgH = img.getHeight(null);
                int targetWidth = 350;
                int targetHeight = (imgW > 0) ? (imgH * targetWidth / imgW) : 120;
                lblLogo.setIcon(new ImageIcon(img.getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH)));
            } else {
                // Fallback a la imagen de logo incluida en la aplicación
                ImageIcon logoIcon = new ImageIcon(getClass().getResource("/com/openbravo/images/logo.png"));
                if (logoIcon != null) {
                    lblLogo.setIcon(new ImageIcon(logoIcon.getImage().getScaledInstance(250, -1, Image.SCALE_SMOOTH)));
                }
            }
        } catch (Exception e) {
            LOGGER.warning("No se pudo cargar el logo PNG: " + e.getMessage());
        }
        
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridheight = 1;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(0, 0, 10, 0);
        contentPanel.add(lblLogo, gbc);

        // SUBTITULO
        JLabel lblSubTitle = new JLabel("IDENTIFÍCATE PARA CONTINUAR", SwingConstants.CENTER);
        lblSubTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblSubTitle.setForeground(BG_DEEP_GREEN); // Texto oscuro
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 15, 0);
        contentPanel.add(lblSubTitle, gbc);

        // CAMPO: Usuario
        JLabel lblUser = new JLabel("USUARIO:");
        lblUser.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblUser.setForeground(TEXT_DARK);
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(10, 0, 5, 0);
        contentPanel.add(lblUser, gbc);

        // COMBO USUARIO
        cmbUsername = new JComboBox<>();
        cmbUsername.setEditable(true);
        cmbUsername.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        cmbUsername.setPreferredSize(new Dimension(400, 45));
        cmbUsername.putClientProperty("JComponent.roundRect", true);
        cmbUsername.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(25, BORDER_COLOR),
            BorderFactory.createEmptyBorder(0, 10, 0, 10)
        ));
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.insets = new Insets(0, 0, 20, 0);
        contentPanel.add(cmbUsername, gbc);

        // CAMPO: Contraseña
        JLabel lblPass = new JLabel("CONTRASEÑA:");
        lblPass.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblPass.setForeground(TEXT_DARK);
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.insets = new Insets(15, 0, 8, 0);
        contentPanel.add(lblPass, gbc);

        // PANEL CONTRASEÑA + LINK
        JPanel passPanel = new JPanel(new BorderLayout(15, 0));
        passPanel.setBackground(PANEL_BG);

        txtPassword = new JPasswordField();
        txtPassword.setFont(new Font("Segoe UI", Font.PLAIN, 20));
        txtPassword.setPreferredSize(new Dimension(400, 50)); // Más ancho como se solicitó
        txtPassword.putClientProperty("JTextField.showClearButton", true);
        txtPassword.putClientProperty("JTextField.arc", 999);
        txtPassword.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(25, BORDER_COLOR),
            BorderFactory.createEmptyBorder(0, 15, 0, 15)
        ));
        txtPassword.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    performLogin();
                }
            }
        });
        passPanel.add(txtPassword, BorderLayout.CENTER);

        // LINK: Olvidé mi contraseña
        JLabel lblForgot = new JLabel("<html><u>Olvide mi contraseña</u></html>");
        lblForgot.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblForgot.setForeground(BRAND_ACCENT_GREEN);
        lblForgot.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lblForgot.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                showForgotPassword();
            }
        });
        passPanel.add(lblForgot, BorderLayout.EAST);

        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.insets = new Insets(0, 0, 20, 0);
        contentPanel.add(passPanel, gbc);

        // PANEL DE BOTONES
        JPanel buttonPanel = new JPanel(new GridBagLayout());
        buttonPanel.setBackground(BACKGROUND_DARK);
        GridBagConstraints bGbc = new GridBagConstraints();
        bGbc.fill = GridBagConstraints.BOTH;
        bGbc.weightx = 0.5;
        bGbc.insets = new Insets(0, 0, 0, 0);

        // BOTÓN ACCEDER
        btnLogin = new JButton("Acceder");
        btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 18));
        btnLogin.setBackground(BRAND_ACCENT_GREEN);
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setFocusPainted(false);
        btnLogin.setBorder(new RoundedBorder(25, BRAND_ACCENT_GREEN));
        btnLogin.addActionListener(e -> performLogin());
        
        gbc.gridx = 0;
        gbc.insets = new Insets(0, 0, 0, 10);
        buttonPanel.add(btnLogin, bGbc);

        // BOTÓN SALIR
        btnExit = new JButton("Salir");
        btnExit.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        btnExit.setBackground(new Color(45, 45, 45));
        btnExit.setForeground(Color.WHITE);
        btnExit.setFont(new Font("Segoe UI", Font.BOLD, 18));
        btnExit.setFocusPainted(false);
        btnExit.setBorder(BorderFactory.createEmptyBorder(12, 0, 12, 0));
        btnExit.putClientProperty("JButton.buttonType", "roundRect");
        btnExit.setBorder(new RoundedBorder(25, BRAND_ACCENT_GREEN));
        btnExit.addActionListener(e -> System.exit(0));
        bGbc.gridx = 1;
        bGbc.insets = new Insets(0, 0, 0, 0);
        buttonPanel.add(btnExit, bGbc);

        gbc.gridx = 0;
        gbc.gridy = 6;
        contentPanel.add(buttonPanel, gbc);

        // Wrapper to center contentPanel horizontally and vertically inside the full-width area
        JPanel wrapperPanel = new JPanel(new GridBagLayout());
        wrapperPanel.setBackground(Color.WHITE);
        wrapperPanel.add(contentPanel, new GridBagConstraints());
        add(wrapperPanel, BorderLayout.CENTER);

        // Footer image containing collaboration brands
        JPanel footerPanel = new JPanel(new BorderLayout()) {
            private Image img = null;
            {
                try {
                    String imagePath = "C:\\Users\\USUARIO\\Downloads\\bicis_mx\\bici\\punto-mx\\assets\\image.png";
                    File imgFile = new File(imagePath);
                    if (imgFile.exists()) {
                        img = javax.imageio.ImageIO.read(imgFile);
                    } else {
                        LOGGER.warning("No se encontro la imagen de marcas en: " + imagePath);
                    }
                } catch (Exception e) {
                    LOGGER.warning("Error al cargar la imagen de marcas: " + e.getMessage());
                }
                setPreferredSize(new Dimension(795, 185));
            }

            @Override
            protected void paintComponent(java.awt.Graphics g) {
                super.paintComponent(g);
                if (img != null) {
                    g.drawImage(img, 0, 0, getWidth(), getHeight(), this);
                } else {
                    g.setColor(new Color(245, 245, 240));
                    g.fillRect(0, 0, getWidth(), getHeight());
                }
            }
        };
        add(footerPanel, BorderLayout.SOUTH);
    }

    private void setupDialog() {
        setUndecorated(true);
        
        // Make the undecorated dialog draggable
        final java.awt.Point[] dragPoint = new java.awt.Point[1];
        addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                dragPoint[0] = e.getPoint();
            }
        });
        addMouseMotionListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseDragged(java.awt.event.MouseEvent e) {
                java.awt.Point current = e.getLocationOnScreen();
                setLocation(current.x - dragPoint[0].x, current.y - dragPoint[0].y);
            }
        });

        pack();
        setResizable(false);
        setLocationRelativeTo(getParent());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        // Round window corners (25px arc) and support transparent window corners
        try {
            setBackground(new Color(0, 0, 0, 0));
            setShape(new java.awt.geom.RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 25, 25));
        } catch (Exception e) {
            LOGGER.warning("No se pudieron redondear las esquinas de la ventana: " + e.getMessage());
        }
    }

    private void loadRecentUsers() {
        try {
            com.openbravo.pos.forms.AppConfig config = com.openbravo.pos.forms.AppConfig.getInstance();
            config.load();
            String usersStr = config.getProperty("login.recent.users");
            if (usersStr != null && !usersStr.isEmpty()) {
                String[] users = usersStr.split(",");
                for (String user : users) {
                    if (user != null && !user.trim().isEmpty()) {
                        cmbUsername.addItem(user.trim());
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error al cargar usuarios recientes", e);
        }
    }

    private void saveUserToHistory(String username) {
        if (username == null || username.trim().isEmpty())
            return;
        try {
            com.openbravo.pos.forms.AppConfig config = com.openbravo.pos.forms.AppConfig.getInstance();
            config.load();
            String currentUsers = config.getProperty("login.recent.users");
            java.util.List<String> userList = new java.util.ArrayList<>();
            if (currentUsers != null && !currentUsers.isEmpty()) {
                for (String u : currentUsers.split(",")) {
                    if (!u.trim().equalsIgnoreCase(username))
                        userList.add(u.trim());
                }
            }
            userList.add(0, username.trim());
            if (userList.size() > 5)
                userList = userList.subList(0, 5);
            config.setProperty("login.recent.users", String.join(",", userList));
            config.save();
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error al guardar historial", e);
        }
    }

    private void performLogin() {
        String username = cmbUsername.getEditor().getItem().toString().trim();
        String password = new String(txtPassword.getPassword());

        if (username.isEmpty()) {
            new MessageInf(MessageInf.SGN_WARNING, "Por favor ingrese un usuario").show(this);
            cmbUsername.requestFocus();
            return;
        }

        try {
            // Reutilizar lógica de búsqueda de JAuthPanel
            AppUser user = m_dlSystem.findPeopleByName(username);

            // Búsqueda case-insensitive si falla la exacta
            if (user == null && m_session != null) {
                Object[] found = (Object[]) new StaticSentence(m_session,
                        "SELECT NAME FROM PEOPLE WHERE UPPER(NAME) = UPPER(?)",
                        new SerializerWriteBasic(new Datas[] { Datas.STRING }),
                        new SerializerReadBasic(new Datas[] { Datas.STRING }))
                        .find(username);
                if (found != null) {
                    user = m_dlSystem.findPeopleByName((String) found[0]);
                }
            }

            if (user == null) {
                new MessageInf(MessageInf.SGN_WARNING, "Usuario no encontrado.").show(this);
                return;
            }

            if (user.authenticate(password)) {
                if (!check2FA(user)) {
                    return;
                }
                loggedUser = user;
                saveUserToHistory(user.getName());
                dispose();
            } else {
                new MessageInf(MessageInf.SGN_WARNING, AppLocal.getIntString("message.BadPassword")).show(this);
                txtPassword.requestFocus();
                txtPassword.selectAll();
            }
        } catch (BasicException ex) {
            LOGGER.log(Level.SEVERE, "Error en login", ex);
            new MessageInf(MessageInf.SGN_DANGER, "Error al iniciar sesión: " + ex.getMessage()).show(this);
        }
    }

    private void showForgotPassword() {
        JDialogForgotPassword dialog = new JDialogForgotPassword(null, m_dlSystem);
        dialog.setVisible(true);
    }

    public AppUser getLoggedUser() {
        return loggedUser;
    }

    private boolean check2FA(AppUser user) {
        if (!com.openbravo.pos.forms.AppConfig.getInstance().getBoolean("system.enable2fa")) {
            return true; // 2FA is globally disabled in system settings
        }
        String secret = user.getTotpSecret();
        if (secret == null || secret.isEmpty()) {
            // Primer inicio de sesión con 2FA activado: forzar configuración
            try {
                com.warrenstrange.googleauth.GoogleAuthenticator gAuth = new com.warrenstrange.googleauth.GoogleAuthenticator();
                final com.warrenstrange.googleauth.GoogleAuthenticatorKey key = gAuth.createCredentials();
                String newSecret = key.getKey();

                String appName = "KriolOS";
                String userName = user.getName() != null ? user.getName().replaceAll(" ", "") : "User";
                String otpAuthUrl = String.format("otpauth://totp/%s:%s?secret=%s&issuer=%s", appName, userName, newSecret, appName);

                com.google.zxing.qrcode.QRCodeWriter qrCodeWriter = new com.google.zxing.qrcode.QRCodeWriter();
                com.google.zxing.common.BitMatrix bitMatrix = qrCodeWriter.encode(otpAuthUrl, com.google.zxing.BarcodeFormat.QR_CODE, 200, 200);
                java.awt.image.BufferedImage qrImage = com.google.zxing.client.j2se.MatrixToImageWriter.toBufferedImage(bitMatrix);

                javax.swing.JPanel panel = new javax.swing.JPanel(new java.awt.BorderLayout(10, 10));
                javax.swing.JLabel lblQr = new javax.swing.JLabel(new javax.swing.ImageIcon(qrImage));
                lblQr.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
                panel.add(lblQr, java.awt.BorderLayout.CENTER);

                javax.swing.JPanel bottom = new javax.swing.JPanel(new java.awt.BorderLayout(5, 5));
                bottom.add(new javax.swing.JLabel("<html>Para continuar, configure la autenticación de dos factores.<br>Escanee el código y escriba el PIN de 6 dígitos:</html>"), java.awt.BorderLayout.NORTH);
                javax.swing.JTextField txtCode = new javax.swing.JTextField();
                txtCode.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 18));
                txtCode.setHorizontalAlignment(javax.swing.JTextField.CENTER);
                bottom.add(txtCode, java.awt.BorderLayout.CENTER);
                panel.add(bottom, java.awt.BorderLayout.SOUTH);

                int option = javax.swing.JOptionPane.showConfirmDialog(this, panel, "Configurar Autenticación 2FA", javax.swing.JOptionPane.OK_CANCEL_OPTION, javax.swing.JOptionPane.PLAIN_MESSAGE);
                if (option == javax.swing.JOptionPane.OK_OPTION) {
                    String codeStr = txtCode.getText().trim();
                    try {
                        int pin = Integer.parseInt(codeStr);
                        if (gAuth.authorize(newSecret, pin)) {
                            // Guardar en la base de datos con commit forzado
                            try {
                                java.sql.Connection conn = m_session.getConnection();
                                boolean autoCommit = conn.getAutoCommit();
                                if (autoCommit) {
                                    conn.setAutoCommit(false);
                                }
                                new com.openbravo.data.loader.StaticSentence(m_session,
                                        "UPDATE people SET TOTP_SECRET = ? WHERE ID = ?",
                                        new com.openbravo.data.loader.SerializerWriteBasic(
                                                new com.openbravo.data.loader.Datas[] {
                                                        com.openbravo.data.loader.Datas.STRING,
                                                        com.openbravo.data.loader.Datas.STRING }))
                                        .exec(new Object[] { newSecret, user.getId() });
                                conn.commit();
                                if (autoCommit) {
                                    conn.setAutoCommit(true);
                                }
                            } catch (Exception sqlEx) {
                                LOGGER.log(java.util.logging.Level.SEVERE, "Error forzando commit de 2FA", sqlEx);
                            }
                            
                            user.setTotpSecret(newSecret);
                            javax.swing.JOptionPane.showMessageDialog(this, "2FA configurado exitosamente. Puede continuar.");
                            return true;
                        } else {
                            new MessageInf(MessageInf.SGN_WARNING, "Código PIN incorrecto. No se configuró el 2FA.").show(this);
                            return false;
                        }
                    } catch (NumberFormatException e) {
                        new MessageInf(MessageInf.SGN_WARNING, "Formato de código inválido.").show(this);
                        return false;
                    }
                } else {
                    return false; // El usuario canceló
                }
            } catch (Exception e) {
                LOGGER.log(java.util.logging.Level.WARNING, "Error configurando 2FA durante login", e);
                new MessageInf(MessageInf.SGN_WARNING, "Error al generar código QR para 2FA.").show(this);
                return false;
            }
        }
        
        String code = javax.swing.JOptionPane.showInputDialog(this, "Ingrese el código de Google Authenticator de 6 dígitos:", "Autenticación 2FA", javax.swing.JOptionPane.QUESTION_MESSAGE);
        if (code == null || code.trim().isEmpty()) {
            return false;
        }
        try {
            int pin = Integer.parseInt(code.trim());
            com.warrenstrange.googleauth.GoogleAuthenticator gAuth = new com.warrenstrange.googleauth.GoogleAuthenticator();
            boolean isCodeValid = gAuth.authorize(secret, pin);
            if (!isCodeValid) {
                new MessageInf(MessageInf.SGN_WARNING, "Código 2FA incorrecto").show(this);
            }
            return isCodeValid;
        } catch (Exception e) {
            new MessageInf(MessageInf.SGN_WARNING, "Código 2FA inválido").show(this);
            return false;
        }
    }

    // Clase auxiliar para bordes redondeados manuales
    private static class RoundedBorder implements javax.swing.border.Border {
        private final int radius;
        private final Color color;

        RoundedBorder(int radius, Color color) {
            this.radius = radius;
            this.color = color;
        }

        @Override
        public java.awt.Insets getBorderInsets(java.awt.Component c) {
            return new java.awt.Insets(this.radius / 3, this.radius / 2, this.radius / 3, this.radius / 2);
        }

        @Override
        public boolean isBorderOpaque() {
            return false;
        }

        @Override
        public void paintBorder(java.awt.Component c, java.awt.Graphics g, int x, int y, int width, int height) {
            java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
            g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.drawRoundRect(x, y, width - 1, height - 1, radius, radius);
            g2.dispose();
        }
    }
}

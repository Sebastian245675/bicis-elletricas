/*
 * ModernLookAndFeel.java
 * 
 * Utilidad para aplicar estilos modernos a la aplicación
 */
package com.openbravo.pos.util;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.Method;
import com.formdev.flatlaf.FlatLightLaf;

/**
 * Clase para aplicar estilos modernos a la aplicación
 */
public class ModernLookAndFeel {
    
    /**
     * Aplica un Look and Feel moderno a la aplicación
     */
    public static void aplicarEstiloModerno() {
        try {
            // Preferir FlatLightLaf para el tema Blanco Institucional
            try {
                // Paleta empresarial del PDV: neutros claros, verde profundo y acento dorado.
                Color creamBg = new Color(246, 247, 243);
                Color surface = Color.WHITE;
                Color brandGreen = new Color(7, 55, 43);
                Color brandGreenHover = new Color(12, 79, 61);
                Color corporateGold = new Color(214, 169, 61);
                Color text = new Color(31, 41, 38);
                Color muted = new Color(104, 116, 111);
                Color line = new Color(218, 224, 220);
                
                UIManager.put("control", creamBg);
                UIManager.put("Panel.background", creamBg);
                UIManager.put("Button.background", brandGreen);
                UIManager.put("Button.foreground", Color.WHITE);
                UIManager.put("Button.hoverBackground", brandGreenHover);
                UIManager.put("Button.pressedBackground", new Color(5, 43, 34));
                UIManager.put("Button.focusedBorderColor", corporateGold);
                UIManager.put("Button.focusWidth", 2);
                UIManager.put("Label.foreground", text);
                UIManager.put("Component.accentColor", corporateGold);
                UIManager.put("Component.focusColor", corporateGold);
                UIManager.put("Component.borderColor", line);
                UIManager.put("TextField.background", surface);
                UIManager.put("TextField.foreground", text);
                UIManager.put("TextField.placeholderForeground", muted);
                UIManager.put("Table.background", surface);
                UIManager.put("Table.alternateRowColor", new Color(249, 250, 248));
                UIManager.put("Table.gridColor", line);
                UIManager.put("Table.selectionBackground", new Color(220, 239, 231));
                UIManager.put("Table.selectionForeground", brandGreen);
                UIManager.put("TableHeader.background", new Color(239, 243, 240));
                UIManager.put("TableHeader.foreground", brandGreen);
                UIManager.put("List.selectionBackground", new Color(220, 239, 231));
                UIManager.put("List.selectionForeground", brandGreen);
                UIManager.put("TabbedPane.selectedBackground", creamBg);
                UIManager.put("TabbedPane.selectedForeground", brandGreen);
                UIManager.put("TabbedPane.underlineColor", corporateGold);
                UIManager.put("TabbedPane.showTabSeparators", Boolean.FALSE);
                UIManager.put("ScrollPane.background", creamBg);
                UIManager.put("Viewport.background", surface);
                
                // Configurar FlatLightLaf
                FlatLightLaf.setup();
                
                aplicarPropiedadesModernas();
                aplicarFuenteGlobal(getPreferredFont("Segoe UI", Font.PLAIN, 14));
                return;
            } catch (Throwable t) {
                // Fallback si FlatLaf no está disponible
            }

            // Buscar y aplicar Nimbus si FlatLaf no está disponible
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());

                    // Personalizaciones adicionales para Nimbus
                    personalizarNimbus();

                    // Aplicar propiedades globales modernas
                    aplicarPropiedadesModernas();
                    aplicarFuenteGlobal(getPreferredFont("Segoe UI", Font.PLAIN, 14));
                    return; // Éxito, salir
                }
            }

            // Si no se encuentra Nimbus, aplicar propiedades básicas al LAF actual
            aplicarPropiedadesModernas();
            aplicarFuenteGlobal(getPreferredFont("Segoe UI", Font.PLAIN, 14));
            
        } catch (Exception e) {
            System.err.println("Error aplicando Look and Feel moderno: " + e.getMessage());
        }
    }

    /**
     * Aplica un tema con color primario y fuente base personalizados en tiempo de ejecución.
     */
    public static void aplicarTemaPersonalizado(Color primary, Font baseFont) {
        if (primary != null) {
            UIManager.put("nimbusBase", primary);
            UIManager.put("Button.background", primary);
            UIManager.put("nimbusSelectionBackground", primary);
        }
        if (baseFont != null) {
            aplicarFuenteGlobal(baseFont);
        }
        aplicarPropiedadesModernas();
    }
    
    /**
     * Personaliza el tema Nimbus con colores modernos
     */
    private static void personalizarNimbus() {
        // Respaldo Nimbus con la misma identidad visual del tema principal.
        UIManager.put("control", new Color(246, 247, 243));
        UIManager.put("nimbusBase", new Color(7, 55, 43));
        UIManager.put("nimbusBlueGrey", new Color(218, 224, 220));
        UIManager.put("nimbusFocus", new Color(214, 169, 61));
        UIManager.put("nimbusSelectedText", Color.WHITE);                      // Texto seleccionado
        UIManager.put("nimbusSelectionBackground", new Color(12, 79, 61));
        
        // Botones más modernos
        UIManager.put("Button.background", new Color(7, 55, 43));
        UIManager.put("Button.foreground", Color.WHITE);
        UIManager.put("Button[Default].backgroundPainter", new Color(7, 55, 43));
        
        // Campos de texto más limpios
        UIManager.put("TextField.background", Color.WHITE);
        UIManager.put("TextField.border", BorderFactory.createLineBorder(new Color(224, 224, 224), 1));
        
        // Paneles más modernos
        UIManager.put("Panel.background", new Color(246, 247, 243));
        
        // Tablas más elegantes
        UIManager.put("Table.background", Color.WHITE);
        UIManager.put("Table.alternateRowColor", new Color(248, 248, 248));
        UIManager.put("Table.gridColor", new Color(224, 224, 224));
    }
    
    /**
     * Configura los botones de JOptionPane en español
     */
    private static void configurarBotonesEspanol() {
        // Configurar botones de JOptionPane en español
        UIManager.put("OptionPane.yesButtonText", "Sí");
        UIManager.put("OptionPane.noButtonText", "No");
        UIManager.put("OptionPane.cancelButtonText", "Cancelar");
        UIManager.put("OptionPane.okButtonText", "Aceptar");
    }
    
    /**
     * Aplica propiedades modernas globales
     */
    private static void aplicarPropiedadesModernas() {
        // Configurar botones en español
        configurarBotonesEspanol();
        
        // Habilitar anti-aliasing para texto más suave
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");
        
        // Renderizado de texto más suave
        System.setProperty("swing.plaf.metal.controlFont", "Arial");
        System.setProperty("swing.plaf.metal.userFont", "Arial");
        
        // Mejores transiciones y animaciones
        UIManager.put("swing.boldMetal", Boolean.FALSE);
        
        // Bordes más modernos para todos los componentes
        UIManager.put("TitledBorder.font", getPreferredFont("Segoe UI", Font.BOLD, 14));
        UIManager.put("TitledBorder.titleColor", new Color(7, 55, 43));
        
        // Tooltips más elegantes
        UIManager.put("ToolTip.background", new Color(7, 55, 43));
        UIManager.put("ToolTip.foreground", Color.WHITE);
        UIManager.put("ToolTip.border", BorderFactory.createLineBorder(new Color(128, 128, 128), 1));
        
        // Scrollbars más modernos
        UIManager.put("ScrollBar.background", new Color(240, 240, 240));
        UIManager.put("ScrollBar.thumb", new Color(180, 180, 180));
        UIManager.put("ScrollBar.track", new Color(245, 245, 245));

        // Bordes redondeados y apariencia general para FlatLaf/Nimbus
        UIManager.put("Button.arc", 14);
        UIManager.put("Component.arc", 12);
        UIManager.put("TextComponent.arc", 12);
        UIManager.put("ProgressBar.arc", 12);
        UIManager.put("ScrollBar.thumbArc", 999);
        UIManager.put("ScrollBar.width", 10);
        UIManager.put("TabbedPane.contentBorderInsets", new Insets(8, 8, 8, 8));

        // Mejoras adicionales para tablas, menús y barras de herramientas
        UIManager.put("Table.rowHeight", 34);
        UIManager.put("Table.showGrid", Boolean.FALSE);
        UIManager.put("Table.selectionBackground", new Color(220, 239, 231));
        UIManager.put("Table.selectionForeground", new Color(7, 55, 43));
        UIManager.put("TableHeader.background", new Color(239, 243, 240));
        UIManager.put("TableHeader.foreground", new Color(7, 55, 43));
        UIManager.put("TableHeader.font", getPreferredFont("Segoe UI", Font.BOLD, 13));

        UIManager.put("ToolBar.background", new Color(246, 247, 243));
        UIManager.put("Menu.background", Color.WHITE);
        UIManager.put("Menu.selectionBackground", new Color(231, 241, 236));

        // Mejorar aspecto de popups y tooltips
        UIManager.put("PopupMenu.border", BorderFactory.createLineBorder(new Color(200, 200, 200)));
        UIManager.put("ScrollPane.viewportBorder", BorderFactory.createEmptyBorder(8, 8, 8, 8));

        // --- PERSONALIZACIÓN PARA JOPTIONPANE Y BOTONES DEL DIÁLOGO (SEBASTIAN) ---
        Color brandGreen = new Color(7, 55, 43);
        Color hoverGreen = new Color(12, 79, 61);
        Color corporateGold = new Color(214, 169, 61);
        
        // Configuración para el Botón por Defecto (Default Button)
        UIManager.put("Button.default.background", brandGreen);
        UIManager.put("Button.default.foreground", Color.WHITE);
        UIManager.put("Button.default.hoverBackground", hoverGreen);
        UIManager.put("Button.default.hoverForeground", Color.WHITE);
        UIManager.put("Button.default.focusedBackground", brandGreen);
        UIManager.put("Button.default.focusedForeground", Color.WHITE);
        UIManager.put("Button.default.pressedBackground", new Color(5, 43, 34));
        UIManager.put("Button.default.pressedForeground", Color.WHITE);
        UIManager.put("Button.default.focusColor", corporateGold);
        UIManager.put("Button.default.focusedBorderColor", corporateGold);
        UIManager.put("Button.default.focusWidth", 2);

        // Configuración para los Botones Regulares (Regular Buttons)
        UIManager.put("Button.hoverBackground", hoverGreen);
        UIManager.put("Button.hoverForeground", Color.WHITE);
        UIManager.put("Button.focusedBackground", brandGreen);
        UIManager.put("Button.focusedForeground", Color.WHITE);
        UIManager.put("Button.pressedBackground", new Color(5, 43, 34));
        UIManager.put("Button.pressedForeground", Color.WHITE);
        UIManager.put("Button.focusedBorderColor", corporateGold);
        UIManager.put("Button.focusWidth", 2);

        UIManager.put("OptionPane.background", Color.WHITE);
        UIManager.put("OptionPane.messageForeground", new Color(51, 65, 85)); // Slate 700
        UIManager.put("OptionPane.messageFont", getPreferredFont("Segoe UI", Font.PLAIN, 14));
        UIManager.put("OptionPane.buttonFont", getPreferredFont("Segoe UI", Font.BOLD, 13));
        UIManager.put("OptionPane.border", BorderFactory.createEmptyBorder(24, 24, 24, 24));
        UIManager.put("OptionPane.messageAreaBorder", BorderFactory.createEmptyBorder(0, 0, 16, 0));
        UIManager.put("OptionPane.buttonAreaBorder", BorderFactory.createEmptyBorder(8, 0, 0, 0));

        // Iconos vectoriales modernos
        UIManager.put("OptionPane.informationIcon", new ModernVectorIcon(ModernVectorIcon.INFO, 42));
        UIManager.put("OptionPane.questionIcon", new ModernVectorIcon(ModernVectorIcon.QUESTION, 42));
        UIManager.put("OptionPane.warningIcon", new ModernVectorIcon(ModernVectorIcon.WARNING, 42));
        UIManager.put("OptionPane.errorIcon", new ModernVectorIcon(ModernVectorIcon.ERROR, 42));
    }

    /**
     * Retorna la mejor fuente geométrica disponible en el sistema.
     * Busca "Baradig", "Century Gothic", "Segoe UI" en ese orden.
     */
    public static Font getPreferredFont(String name, int style, int size) {
        try {
            String[] families = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
            for (String f : families) {
                if (f.equalsIgnoreCase(name)) {
                    return new Font(f, style, size);
                }
            }
            // Fallbacks
            String[] fallbacks = {"Century Gothic", "Segoe UI", "Arial"};
            for (String fb : fallbacks) {
                for (String f : families) {
                    if (f.equalsIgnoreCase(fb)) {
                        return new Font(f, style, size);
                    }
                }
            }
        } catch (Exception e) {
            // Ignorar y retornar fuente por defecto
        }
        return new Font("Dialog", style, size);
    }

    /**
     * Aplica una fuente por defecto a todos los componentes Swing.
     */
    private static void aplicarFuenteGlobal(Font font) {
        if (font == null) return;
        UIDefaults defaults = UIManager.getDefaults();
        for (Object key : defaults.keySet()) {
            if (key != null && key.toString().toLowerCase().contains("font")) {
                try {
                    UIManager.put(key, font);
                } catch (Exception e) {
                    // ignorar claves que no aceptan Font
                }
            }
        }

        // Force some common keys as well
        UIManager.put("defaultFont", font);
        UIManager.put("Button.font", font.deriveFont(Font.BOLD, 14f));
        UIManager.put("Label.font", font.deriveFont(14f));
        UIManager.put("TextField.font", font.deriveFont(14f));
        UIManager.put("TextArea.font", font.deriveFont(14f));
        UIManager.put("Table.font", font.deriveFont(14f));
        UIManager.put("TableHeader.font", font.deriveFont(Font.BOLD, 13f));

        // Improve option panes and dialogs
        UIManager.put("OptionPane.messageFont", font.deriveFont(14f));
        UIManager.put("OptionPane.buttonFont", font.deriveFont(Font.BOLD, 13f));
    }
    
    /**
     * Aplica estilo moderno a un JDialog específico
     */
    public static void aplicarEstiloModernoADialogo(JDialog dialog) {
        if (dialog == null) return;
        
        // Fondo moderno
        dialog.getContentPane().setBackground(new Color(246, 247, 243));
        
        // Aplicar estilos a todos los componentes del diálogo
        aplicarEstilosRecursivamente(dialog.getContentPane());
        
        // Sombra moderna (si es posible en el sistema)
        try {
            dialog.getRootPane().setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
            ));
        } catch (Exception e) {
            // Ignorar si no se puede aplicar
        }
    }
    
    /**
     * Aplica estilos modernos recursivamente a todos los componentes
     */
    private static void aplicarEstilosRecursivamente(Container container) {
        for (Component comp : container.getComponents()) {
            
            if (comp instanceof JButton) {
                aplicarEstiloBotonModerno((JButton) comp);
            }
            else if (comp instanceof JTextField) {
                aplicarEstiloCampoModerno((JTextField) comp);
            }
            else if (comp instanceof JLabel) {
                aplicarEstiloEtiquetaModerna((JLabel) comp);
            }
            else if (comp instanceof JPanel) {
                aplicarEstiloPanelModerno((JPanel) comp);
            }
            else if (comp instanceof JToggleButton) {
                aplicarEstiloToggleModerno((JToggleButton) comp);
            }
            
            // Aplicar recursivamente a contenedores
            if (comp instanceof Container) {
                aplicarEstilosRecursivamente((Container) comp);
            }
        }
    }

    /**
     * Public helper to apply modern styles recursively to any container.
     * Useful for modules that want to restyle existing UI components at runtime.
     */
    public static void estilizarComponentes(Container container) {
        if (container == null) return;
        aplicarEstilosRecursivamente(container);
    }
    
    /**
     * Aplica estilo moderno a un botón
     */
    private static void aplicarEstiloBotonModerno(JButton button) {
        button.setFocusPainted(false);
        button.setBorderPainted(true);
        button.setBackground(new Color(7, 55, 43));
        button.setForeground(Color.WHITE);
        button.setFont(getPreferredFont("Segoe UI", Font.BOLD, 14));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.putClientProperty("JButton.buttonType", "roundRect");
        
        // Bordes redondeados (efecto visual)
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(7, 55, 43), 1),
            BorderFactory.createEmptyBorder(7, 14, 7, 14)
        ));
        
        // Efectos hover si es posible
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            Color colorOriginal = button.getBackground();
            
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(12, 79, 61));
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(colorOriginal);
            }
        });
    }
    
    /**
     * Aplica estilo moderno a un campo de texto
     */
    private static void aplicarEstiloCampoModerno(JTextField field) {
        field.setFont(getPreferredFont("Segoe UI", Font.PLAIN, 14));
        field.putClientProperty("JTextField.roundRect", Boolean.TRUE);
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(218, 224, 220), 1),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        field.setBackground(Color.WHITE);
        
        // Efecto focus
        field.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent evt) {
                field.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(214, 169, 61), 1),
                    BorderFactory.createEmptyBorder(6, 10, 6, 10)
                ));
            }
            
            @Override
            public void focusLost(java.awt.event.FocusEvent evt) {
                field.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(218, 224, 220), 1),
                    BorderFactory.createEmptyBorder(6, 10, 6, 10)
                ));
            }
        });
    }
    
    /**
     * Aplica estilo moderno a una etiqueta
     */
    private static void aplicarEstiloEtiquetaModerna(JLabel label) {
        label.setFont(getPreferredFont("Segoe UI", Font.PLAIN, 14));
        label.setForeground(new Color(31, 41, 38));
    }
    
    /**
     * Aplica estilo moderno a un panel
     */
    private static void aplicarEstiloPanelModerno(JPanel panel) {
        panel.setBackground(new Color(246, 247, 243));
        
        // Si tiene borde de título, modernizarlo
        if (panel.getBorder() instanceof javax.swing.border.TitledBorder) {
            javax.swing.border.TitledBorder titleBorder = 
                (javax.swing.border.TitledBorder) panel.getBorder();
            titleBorder.setTitleFont(getPreferredFont("Segoe UI", Font.BOLD, 14));
            titleBorder.setTitleColor(new Color(7, 55, 43));
        }
    }
    
    /**
     * Aplica estilo moderno a un toggle button
     */
    private static void aplicarEstiloToggleModerno(JToggleButton toggle) {
        toggle.setFocusPainted(false);
        toggle.setBorderPainted(true);
        toggle.setFont(getPreferredFont("Segoe UI", Font.BOLD, 14));
        toggle.setCursor(new Cursor(Cursor.HAND_CURSOR));
        toggle.putClientProperty("JButton.buttonType", "roundRect");
        
        Color colorNormal = new Color(239, 243, 240);
        Color colorSeleccionado = new Color(7, 55, 43);
        
        toggle.setBackground(toggle.isSelected() ? colorSeleccionado : colorNormal);
        toggle.setForeground(toggle.isSelected() ? Color.WHITE : new Color(31, 41, 38));
        
        // Cambiar colores al seleccionar/deseleccionar
        toggle.addActionListener(e -> {
            toggle.setBackground(toggle.isSelected() ? colorSeleccionado : colorNormal);
            toggle.setForeground(toggle.isSelected() ? Color.WHITE : new Color(31, 41, 38));
        });
    }

    /**
     * Iconos vectoriales personalizados para los cuadros de diálogo y alertas
     */
    public static class ModernVectorIcon implements Icon {
        public static final int INFO = 0;
        public static final int QUESTION = 1;
        public static final int WARNING = 2;
        public static final int ERROR = 3;
        
        private final int type;
        private final int size;
        
        public ModernVectorIcon(int type, int size) {
            this.type = type;
            this.size = size;
        }
        
        @Override
        public int getIconWidth() { return size; }
        
        @Override
        public int getIconHeight() { return size; }
        
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
            
            Color circleColor;
            String symbol;
            Color symbolColor = Color.WHITE;
            
            switch (type) {
                case INFO:
                    circleColor = new Color(59, 130, 246); // Blue #3B82F6
                    symbol = "i";
                    break;
                case QUESTION:
                    circleColor = new Color(202, 159, 65); // Oro corporativo #CA9F41
                    symbol = "?";
                    break;
                case WARNING:
                    circleColor = new Color(245, 158, 11); // Amber/Orange #F59E0B
                    symbol = "!";
                    break;
                case ERROR:
                default:
                    circleColor = new Color(239, 68, 68); // Red #EF4444
                    symbol = "✕";
                    break;
            }
            
            // Draw circle background
            g2d.setColor(circleColor);
            g2d.fillOval(x, y, size, size);
            
            // Draw symbol inside the circle
            g2d.setColor(symbolColor);
            
            if (type == ERROR) {
                // Draw ✕ using smooth vector lines
                int stroke = Math.max(2, size / 10);
                g2d.setStroke(new BasicStroke(stroke, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                int padding = size / 3;
                g2d.drawLine(x + padding, y + padding, x + size - padding, y + size - padding);
                g2d.drawLine(x + size - padding, y + padding, x + padding, y + size - padding);
            } else if (type == INFO) {
                // Draw 'i' symbol manually to look perfect
                int stroke = Math.max(2, size / 12);
                g2d.setStroke(new BasicStroke(stroke, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                // Dot
                g2d.fillOval(x + size / 2 - stroke / 2, y + size / 4, stroke, stroke);
                // Body
                g2d.drawLine(x + size / 2, y + size / 4 + stroke * 2, x + size / 2, y + size * 3 / 4);
                // Base & top flag serif
                g2d.drawLine(x + size / 2 - stroke, y + size * 3 / 4, x + size / 2 + stroke, y + size * 3 / 4);
                g2d.drawLine(x + size / 2 - stroke, y + size / 4 + stroke * 2, x + size / 2, y + size / 4 + stroke * 2);
            } else if (type == WARNING) {
                // Draw '!' symbol manually
                int stroke = Math.max(2, size / 10);
                // Dot
                g2d.fillOval(x + size / 2 - stroke / 2, y + size * 3 / 4 - stroke / 2, stroke, stroke);
                // Stem
                g2d.setStroke(new BasicStroke(stroke, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2d.drawLine(x + size / 2, y + size / 4, x + size / 2, y + size * 3 / 4 - stroke * 2);
            } else {
                // For QUESTION (?) use font text rendering
                g2d.setFont(new Font("Segoe UI", Font.BOLD, (int) (size * 0.65)));
                FontMetrics fm = g2d.getFontMetrics();
                int sx = x + (size - fm.stringWidth(symbol)) / 2;
                int sy = y + fm.getAscent() + (size - fm.getHeight()) / 2 - 1;
                g2d.drawString(symbol, sx, sy);
            }
            
            g2d.dispose();
        }
    }
}

package com.openbravo.pos.config;

import com.openbravo.pos.forms.AppConfig;
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.data.user.DirtyManager;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

public class JPanelConfigSecurity extends JPanel implements PanelConfig {

    private JCheckBox jchkEnable2FA;
    private DirtyManager dirty;

    public JPanelConfigSecurity() {
        dirty = new DirtyManager();
        initComponents();
    }

    private void initComponents() {
        this.setLayout(new FlowLayout(FlowLayout.LEFT, 20, 20));
        this.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel innerPanel = new JPanel();
        innerPanel.setLayout(new javax.swing.BoxLayout(innerPanel, javax.swing.BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Configuración de Seguridad");
        title.setFont(new Font("Arial", Font.BOLD, 18));
        title.setBorder(new EmptyBorder(0, 0, 15, 0));
        innerPanel.add(title);

        jchkEnable2FA = new JCheckBox("Habilitar Verificación en Dos Pasos (2FA)");
        jchkEnable2FA.setFont(new Font("Arial", Font.PLAIN, 14));
        jchkEnable2FA.addActionListener(dirty);
        innerPanel.add(jchkEnable2FA);
        
        JLabel hint = new JLabel("<html><i>(Requiere reiniciar la aplicación tras guardar los cambios)</i></html>");
        hint.setFont(new Font("Arial", Font.PLAIN, 12));
        hint.setBorder(new EmptyBorder(5, 25, 0, 0));
        innerPanel.add(hint);

        this.add(innerPanel);
    }

    @Override
    public boolean hasChanged() {
        return dirty.isDirty();
    }

    @Override
    public Component getConfigComponent() {
        return this;
    }

    @Override
    public void loadProperties(AppConfig config) {
        String enable2FA = config.getProperty("system.enable2fa");
        if (enable2FA == null) {
            config.setProperty("system.enable2fa", "false");
        }
        jchkEnable2FA.setSelected(Boolean.parseBoolean(config.getProperty("system.enable2fa")));
        dirty.setDirty(false);
    }

    @Override
    public void saveProperties(AppConfig config) {
        config.setProperty("system.enable2fa", Boolean.toString(jchkEnable2FA.isSelected()));
        dirty.setDirty(false);
    }
}

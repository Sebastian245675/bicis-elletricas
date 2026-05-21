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

package com.openbravo.pos.reports;

import com.openbravo.basic.BasicException;
import com.openbravo.data.gui.ComboBoxValModel;
import com.openbravo.data.loader.*;
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.pos.forms.AppView;
import com.openbravo.pos.forms.DataLogicSales;
import java.awt.Component;
import java.awt.event.ActionListener;
import java.util.List;

/**
 *
 * @author adrianromero
 */
public class JParamsLocation extends javax.swing.JPanel implements ReportEditorCreator {
    
    private SentenceList m_sentlocations;
    private ComboBoxValModel m_LocationsModel;    
    
    private DataLogicSales m_dlSales;
    
    /** Creates new form JParamsLocation */
    public JParamsLocation() {
        initComponents();     
    }

    /**
     *
     * @param app
     */
    @Override
    public void init(AppView app) {
         
        m_dlSales = (DataLogicSales) app.getBean("com.openbravo.pos.forms.DataLogicSales");
        
        // El modelo de locales
        m_sentlocations = m_dlSales.getLocationsList();
        m_LocationsModel = new ComboBoxValModel();
        
        m_jBtnAdd.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jBtnAddActionPerformed(evt);
            }
        });
    }
        
    /**
     *
     * @throws BasicException
     */
    @Override
    public void activate() throws BasicException {
        List a = m_sentlocations.list();
        addFirst(a);
        m_LocationsModel = new ComboBoxValModel(a);
        m_LocationsModel.setSelectedFirst();
        m_jLocation.setModel(m_LocationsModel); // refresh model   
    }
    
    /**
     *
     * @return
     */
    @Override
    public SerializerWrite getSerializerWrite() {
        return new SerializerWriteBasic(new Datas[] {Datas.OBJECT, Datas.STRING});
    }

    /**
     *
     * @return
     */
    @Override
    public Component getComponent() {
        return this;
    }

    /**
     *
     * @param a
     */
    protected void addFirst(List a) {
        a.add(0, null);
    }
    
    /**
     *
     * @param l
     */
    public void addActionListener(ActionListener l) {
        m_jLocation.addActionListener(l);
    }
    
    /**
     *
     * @param l
     */
    public void removeActionListener(ActionListener l) {
        m_jLocation.removeActionListener(l);
    }
    
    /**
     *
     * @return
     * @throws BasicException
     */
    @Override
    public Object createValue() throws BasicException {
        
        return new Object[] {
            m_LocationsModel.getSelectedKey() == null 
                ? QBFCompareEnum.COMP_NONE 
                : QBFCompareEnum.COMP_EQUALS, 
            m_LocationsModel.getSelectedKey()
        };
    }    
    private void m_jBtnAddActionPerformed(java.awt.event.ActionEvent evt) {
        final javax.swing.JDialog dialog = new javax.swing.JDialog(javax.swing.SwingUtilities.getWindowAncestor(this), "Gestión de Sucursales", java.awt.Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setLayout(new java.awt.BorderLayout());
        dialog.setResizable(false);

        // --- Header Premium ---
        javax.swing.JPanel headerPanel = new javax.swing.JPanel(new java.awt.BorderLayout());
        headerPanel.setBackground(new java.awt.Color(33, 37, 41)); // Dark modern background
        headerPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(18, 22, 18, 22));
        
        javax.swing.JLabel titleLabel = new javax.swing.JLabel("Nueva Sucursal");
        titleLabel.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 22));
        titleLabel.setForeground(new java.awt.Color(248, 249, 250));
        
        javax.swing.JLabel subTitleLabel = new javax.swing.JLabel("Registre un nuevo punto de operación en su red");
        subTitleLabel.setFont(new java.awt.Font("Segoe UI", java.awt.Font.ITALIC, 12));
        subTitleLabel.setForeground(new java.awt.Color(173, 181, 189));
        
        javax.swing.JPanel titleTextPanel = new javax.swing.JPanel(new java.awt.GridLayout(2, 1, 0, 5));
        titleTextPanel.setOpaque(false);
        titleTextPanel.add(titleLabel);
        titleTextPanel.add(subTitleLabel);
        
        headerPanel.add(titleTextPanel, java.awt.BorderLayout.CENTER);
        dialog.add(headerPanel, java.awt.BorderLayout.NORTH);

        // --- Formulario Moderno ---
        javax.swing.JPanel formPanel = new javax.swing.JPanel(new java.awt.GridBagLayout());
        formPanel.setBackground(java.awt.Color.WHITE);
        formPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(25, 30, 25, 30));
        java.awt.GridBagConstraints gbc = new java.awt.GridBagConstraints();
        gbc.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gbc.insets = new java.awt.Insets(12, 0, 4, 0);
        gbc.weightx = 1.0;

        java.awt.Font labelFont = new java.awt.Font("Segoe UI Semibold", java.awt.Font.PLAIN, 14);
        java.awt.Font inputFont = new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 15);
        
        javax.swing.JTextField nameField = createStyledTextField(inputFont);
        javax.swing.JTextField addressField = createStyledTextField(inputFont);
        javax.swing.JTextField phoneField = createStyledTextField(inputFont);
        
        int row = 0;
        gbc.gridy = row++;
        formPanel.add(createStyledLabel("NOMBRE DE SUCURSAL *", labelFont), gbc);
        gbc.gridy = row++;
        gbc.insets = new java.awt.Insets(0, 0, 15, 0);
        formPanel.add(nameField, gbc);
        
        gbc.gridy = row++;
        gbc.insets = new java.awt.Insets(5, 0, 4, 0);
        formPanel.add(createStyledLabel("DIRECCIÓN / UBICACIÓN", labelFont), gbc);
        gbc.gridy = row++;
        gbc.insets = new java.awt.Insets(0, 0, 15, 0);
        formPanel.add(addressField, gbc);
        
        gbc.gridy = row++;
        gbc.insets = new java.awt.Insets(5, 0, 4, 0);
        formPanel.add(createStyledLabel("TELÉFONO DE CONTACTO", labelFont), gbc);
        gbc.gridy = row++;
        gbc.insets = new java.awt.Insets(0, 0, 10, 0);
        formPanel.add(phoneField, gbc);

        dialog.add(formPanel, java.awt.BorderLayout.CENTER);

        // --- Footer con Botones Premium ---
        javax.swing.JPanel buttonPanel = new javax.swing.JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 15, 15));
        buttonPanel.setBackground(new java.awt.Color(248, 249, 250));
        buttonPanel.setBorder(javax.swing.BorderFactory.createMatteBorder(1, 0, 0, 0, new java.awt.Color(222, 226, 230)));
        
        javax.swing.JButton btnCancel = new javax.swing.JButton("CANCELAR");
        btnCancel.setFont(new java.awt.Font("Segoe UI Bold", java.awt.Font.PLAIN, 12));
        btnCancel.setFocusPainted(false);
        btnCancel.setPreferredSize(new java.awt.Dimension(110, 38));
        
        javax.swing.JButton btnSave = new javax.swing.JButton("REGISTRAR");
        btnSave.setFont(new java.awt.Font("Segoe UI Bold", java.awt.Font.PLAIN, 12));
        btnSave.setBackground(new java.awt.Color(0, 123, 255));
        btnSave.setForeground(java.awt.Color.WHITE);
        btnSave.setFocusPainted(false);
        btnSave.setPreferredSize(new java.awt.Dimension(130, 38));

        btnCancel.addActionListener(e -> dialog.dispose());
        btnSave.addActionListener(e -> {
            String name = nameField.getText().trim();
            if (name.isEmpty()) {
                javax.swing.JOptionPane.showMessageDialog(dialog, "El nombre comercial es obligatorio.", "Validación", javax.swing.JOptionPane.WARNING_MESSAGE);
                return;
            }
            try {
                String id = java.util.UUID.randomUUID().toString();
                Object[] newLocation = new Object[] {
                    id, name, addressField.getText().trim(), phoneField.getText().trim()
                };
                m_dlSales.getTableLocations().getInsertSentence().exec(newLocation);
                activate();
                m_LocationsModel.setSelectedKey(id);
                dialog.dispose();
            } catch (BasicException ex) {
                javax.swing.JOptionPane.showMessageDialog(dialog, "Error al guardar: " + ex.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
            }
        });

        buttonPanel.add(btnCancel);
        buttonPanel.add(btnSave);
        dialog.add(buttonPanel, java.awt.BorderLayout.SOUTH);

        dialog.pack();
        dialog.setLocationRelativeTo(javax.swing.SwingUtilities.getWindowAncestor(this));
        dialog.setVisible(true);
    }

    private javax.swing.JTextField createStyledTextField(java.awt.Font font) {
        javax.swing.JTextField field = new javax.swing.JTextField();
        field.setFont(font);
        field.setPreferredSize(new java.awt.Dimension(250, 35));
        field.setBorder(javax.swing.BorderFactory.createCompoundBorder(
            javax.swing.BorderFactory.createLineBorder(new java.awt.Color(206, 212, 218)),
            javax.swing.BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        return field;
    }

    private javax.swing.JLabel createStyledLabel(String text, java.awt.Font font) {
        javax.swing.JLabel label = new javax.swing.JLabel(text);
        label.setFont(font);
        label.setForeground(new java.awt.Color(108, 117, 125));
        return label;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        m_jLocation = new javax.swing.JComboBox();
        jLabel8 = new javax.swing.JLabel();
        m_jBtnAdd = new javax.swing.JButton();

        setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        setMaximumSize(new java.awt.Dimension(300, 42));
        setMinimumSize(new java.awt.Dimension(300, 42));
        setPreferredSize(new java.awt.Dimension(300, 42));

        m_jLocation.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        m_jLocation.setMaximumSize(new java.awt.Dimension(150, 30));
        m_jLocation.setMinimumSize(new java.awt.Dimension(150, 30));
        m_jLocation.setPreferredSize(new java.awt.Dimension(150, 30));

        jLabel8.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel8.setText(AppLocal.getIntString("label.warehouse")); // NOI18N
        jLabel8.setMaximumSize(new java.awt.Dimension(100, 30));
        jLabel8.setMinimumSize(new java.awt.Dimension(100, 30));
        jLabel8.setPreferredSize(new java.awt.Dimension(100, 30));

        m_jBtnAdd.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/editnew.png"))); // NOI18N
        m_jBtnAdd.setToolTipText("Añadir Nueva Sucursal");
        m_jBtnAdd.setFocusable(false);
        m_jBtnAdd.setPreferredSize(new java.awt.Dimension(36, 30));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(m_jLocation, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(m_jBtnAdd, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(m_jLocation, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(m_jBtnAdd, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel8;
    private javax.swing.JComboBox m_jLocation;
    private javax.swing.JButton m_jBtnAdd;
    // End of variables declaration//GEN-END:variables
    
}

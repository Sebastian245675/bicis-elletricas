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
import com.openbravo.beans.JCalendarDialog;
import com.openbravo.data.loader.Datas;
import com.openbravo.data.loader.QBFCompareEnum;
import com.openbravo.data.loader.SerializerWrite;
import com.openbravo.data.loader.SerializerWriteBasic;
import com.openbravo.format.Formats;
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.pos.forms.AppView;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.util.Date;
import javax.swing.BorderFactory;
import javax.swing.border.EmptyBorder;

/**
 *
 * @author JG uniCenta
 */
public class JParamsDatesInterval extends javax.swing.JPanel implements ReportEditorCreator {

    /** Creates new form JParamsClosedPos */
    public JParamsDatesInterval() {
        initComponents();
        styleComponents();
    }
    
    private void styleComponents() {
        setOpaque(false);
        setBorder(new EmptyBorder(5, 5, 5, 5));
        
        Font fontLabel = new Font("Segoe UI", Font.BOLD, 13);
        Font fontText = new Font("Segoe UI", Font.PLAIN, 14);
        Color colorLabel = new Color(70, 80, 90);

        jLabel1.setFont(fontLabel);
        jLabel1.setForeground(colorLabel);
        jLabel2.setFont(fontLabel);
        jLabel2.setForeground(colorLabel);

        jTxtStartDate.setFont(fontText);
        jTxtEndDate.setFont(fontText);
        
        jTxtStartDate.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(210, 220, 230)),
            BorderFactory.createEmptyBorder(2, 8, 2, 8)
        ));
        jTxtEndDate.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(210, 220, 230)),
            BorderFactory.createEmptyBorder(2, 8, 2, 8)
        ));
        
        btnDateStart.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnDateEnd.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    }
    
    /**
     *
     * @param d
     */
    public void setStartDate(Date d) {
        jTxtStartDate.setText(Formats.TIMESTAMP.formatValue(d));
    }
    
    /**
     *
     * @param d
     */
    public void setEndDate(Date d) {
        jTxtEndDate.setText(Formats.TIMESTAMP.formatValue(d));
    }

    /**
     *
     * @param app
     */
    @Override
    public void init(AppView app) {
    }

    /**
     *
     * @throws BasicException
     */
    @Override
    public void activate() throws BasicException {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        
        // Start date: hoy a las 12:00:00 a.m. (00:00:00)
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
        cal.set(java.util.Calendar.MINUTE, 0);
        cal.set(java.util.Calendar.SECOND, 0);
        cal.set(java.util.Calendar.MILLISECOND, 0);
        setStartDate(cal.getTime());
        
        // End date: hoy a las 11:59:59 p.m. (23:59:59)
        cal.set(java.util.Calendar.HOUR_OF_DAY, 23);
        cal.set(java.util.Calendar.MINUTE, 59);
        cal.set(java.util.Calendar.SECOND, 59);
        cal.set(java.util.Calendar.MILLISECOND, 999);
        setEndDate(cal.getTime());
    }
    
    /**
     *
     * @return
     */
    @Override
    public SerializerWrite getSerializerWrite() {
        return new SerializerWriteBasic(new Datas[] {Datas.OBJECT, Datas.TIMESTAMP, Datas.OBJECT, Datas.TIMESTAMP});
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
     * @return
     * @throws BasicException
     */
    @Override
    public Object createValue() throws BasicException {
        Object startdate = Formats.TIMESTAMP.parseValue(jTxtStartDate.getText());
        Object enddate = Formats.TIMESTAMP.parseValue(jTxtEndDate.getText());  

        return new Object[] {
            startdate == null ? QBFCompareEnum.COMP_NONE : QBFCompareEnum.COMP_GREATEROREQUALS,
            startdate,
            enddate == null ? QBFCompareEnum.COMP_NONE : QBFCompareEnum.COMP_LESS,
            enddate
        };
    }    
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jTxtStartDate = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jTxtEndDate = new javax.swing.JTextField();
        btnDateStart = new javax.swing.JButton();
        btnDateEnd = new javax.swing.JButton();

        setMaximumSize(new java.awt.Dimension(850, 48));
        setMinimumSize(new java.awt.Dimension(850, 48));
        setPreferredSize(new java.awt.Dimension(850, 48));

        jLabel1.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel1.setText(AppLocal.getIntString("label.StartDate")); // NOI18N
        jLabel1.setPreferredSize(new java.awt.Dimension(90, 32));

        jTxtStartDate.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jTxtStartDate.setPreferredSize(new java.awt.Dimension(210, 34));

        jLabel2.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel2.setText(AppLocal.getIntString("label.EndDate")); // NOI18N
        jLabel2.setPreferredSize(new java.awt.Dimension(90, 32));

        jTxtEndDate.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jTxtEndDate.setPreferredSize(new java.awt.Dimension(210, 34));

        btnDateStart.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/date.png"))); // NOI18N
        btnDateStart.setToolTipText("Open Calendar");
        btnDateStart.setPreferredSize(new java.awt.Dimension(44, 34));
        btnDateStart.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDateStartActionPerformed(evt);
            }
        });

        btnDateEnd.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/date.png"))); // NOI18N
        btnDateEnd.setToolTipText("Open Calendar");
        btnDateEnd.setPreferredSize(new java.awt.Dimension(44, 34));
        btnDateEnd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDateEndActionPerformed(evt);
            }
        });

        setLayout(new java.awt.GridBagLayout());
        java.awt.GridBagConstraints gbc = new java.awt.GridBagConstraints();
        gbc.fill = java.awt.GridBagConstraints.BOTH;
        gbc.insets = new java.awt.Insets(0, 5, 0, 5);
        gbc.weighty = 1.0;

        // Col 0: Start Date Label
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.0;
        add(jLabel1, gbc);

        // Col 1: Start Date Text Field
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 0.0;
        add(jTxtStartDate, gbc);

        // Col 2: Start Date Button
        gbc.gridx = 2; gbc.gridy = 0; gbc.weightx = 0.0;
        add(btnDateStart, gbc);

        // Spacer between groups
        javax.swing.JPanel spacer = new javax.swing.JPanel();
        spacer.setOpaque(false);
        spacer.setPreferredSize(new java.awt.Dimension(30, 10));
        gbc.gridx = 3; gbc.gridy = 0; gbc.weightx = 0.0;
        add(spacer, gbc);

        // Col 4: End Date Label
        gbc.gridx = 4; gbc.gridy = 0; gbc.weightx = 0.0;
        add(jLabel2, gbc);

        // Col 5: End Date Text Field
        gbc.gridx = 5; gbc.gridy = 0; gbc.weightx = 0.0;
        add(jTxtEndDate, gbc);

        // Col 6: End Date Button
        gbc.gridx = 6; gbc.gridy = 0; gbc.weightx = 0.0;
        add(btnDateEnd, gbc);

        // Trailing Spacer to absorb remaining horizontal space and push everything to the left
        javax.swing.JPanel trailingSpacer = new javax.swing.JPanel();
        trailingSpacer.setOpaque(false);
        gbc.gridx = 7; gbc.gridy = 0; gbc.weightx = 1.0;
        add(trailingSpacer, gbc);
    }// </editor-fold>//GEN-END:initComponents

    private void btnDateStartActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDateStartActionPerformed

        Date date;
        try {
            date = (Date) Formats.TIMESTAMP.parseValue(jTxtStartDate.getText());
        } catch (BasicException e) {
            date = null;
        }        
        date = JCalendarDialog.showCalendarTimeHours(this, date);
        if (date != null) {
            jTxtStartDate.setText(Formats.TIMESTAMP.formatValue(date));
        }             
    }//GEN-LAST:event_btnDateStartActionPerformed

    private void btnDateEndActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDateEndActionPerformed

        Date date;
        try {
            date = (Date) Formats.TIMESTAMP.parseValue(jTxtEndDate.getText());
        } catch (BasicException e) {
            date = null;
        }        
        date = JCalendarDialog.showCalendarTimeHours(this, date);
        if (date != null) {
            jTxtEndDate.setText(Formats.TIMESTAMP.formatValue(date));
        }          
    }//GEN-LAST:event_btnDateEndActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnDateEnd;
    private javax.swing.JButton btnDateStart;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JTextField jTxtEndDate;
    private javax.swing.JTextField jTxtStartDate;
    // End of variables declaration//GEN-END:variables
    
}


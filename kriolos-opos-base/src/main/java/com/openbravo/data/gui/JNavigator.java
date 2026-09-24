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

package com.openbravo.data.gui;

import java.util.*;
import com.openbravo.basic.BasicException;
import com.openbravo.data.loader.ComparatorCreator;
import com.openbravo.data.loader.LocalRes;
import com.openbravo.data.loader.Vectorer;
import com.openbravo.data.user.BrowseListener;
import com.openbravo.data.user.BrowsableEditableData;
import com.openbravo.data.user.StateListener;
import com.openbravo.pos.util.ModernActionIcon;
import java.awt.Color;

/**
 *
 * @author JG uniCenta
 */
public class JNavigator extends javax.swing.JPanel implements BrowseListener, StateListener {

    public final static int BUTTONS_ALL = 0;
    public final static int BUTTONS_NONAVIGATE = 1;

    protected BrowsableEditableData m_bd;
    protected ComparatorCreator m_cc;
    protected FindInfo m_LastFindInfo;  

    private javax.swing.JButton jbtnFind = null;
    private javax.swing.JButton jbtnSort = null;
    private javax.swing.JButton jbtnFirst = null;
    private javax.swing.JButton jbtnLast = null;
    private javax.swing.JButton jbtnRefresh = null;
    private javax.swing.JButton jbtnReload = null;    
    
    /** Creates new form JNavigator
     * @param bd
     * @param vec
     * @param cc
     * @param iButtons */
    public JNavigator(BrowsableEditableData bd, Vectorer vec, ComparatorCreator cc, int iButtons) {

        initComponents();
        

        if (bd.canLoadData()) {
            jbtnReload = new javax.swing.JButton();
            jbtnReload.setPreferredSize(new java.awt.Dimension(38, 38));
            jbtnReload.setIcon(new ModernActionIcon(ModernActionIcon.Type.REFRESH, 21, Color.WHITE));
            jbtnReload.setToolTipText("Actualizar");
            jbtnReload.setBackground(new Color(7, 55, 43));
            jbtnReload.setForeground(Color.WHITE);
            jbtnReload.putClientProperty("JButton.buttonType", "roundRect");
            jbtnReload.setMargin(new java.awt.Insets(7, 7, 7, 7));
            jbtnReload.setFocusPainted(false);
            jbtnReload.setFocusable(false);
            jbtnReload.setRequestFocusEnabled(false);
            jbtnReload.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    jbtnReloadActionPerformed(evt);
                }
            });
            add(jbtnReload);

            add(new javax.swing.JSeparator());
        }
        
        
        m_cc = cc;
        
        m_bd = bd;
        bd.addBrowseListener(this);
        bd.addStateListener(this);
    }

    public JNavigator(BrowsableEditableData bd) {
        this(bd, null, null, BUTTONS_ALL);
    }

    public JNavigator(BrowsableEditableData bd, Vectorer vec, ComparatorCreator cc) {
        this(bd, vec, cc, BUTTONS_ALL);
    }

    @Override
    public void updateState(int iState) {
        if (iState == BrowsableEditableData.ST_INSERT || iState == BrowsableEditableData.ST_DELETE) {
             // Insert o Delete
            if (jbtnFirst != null) jbtnFirst.setEnabled(false);
            // if (jbtnPrev != null) jbtnPrev.setEnabled(false);
            // if (jbtnNext != null) jbtnNext.setEnabled(false);
            if (jbtnLast != null) jbtnLast.setEnabled(false);
            if (jbtnRefresh != null) jbtnRefresh.setEnabled(true);
        }
    }

    @Override
    public void updateIndex(int iIndex, int iCounter) {
        
        if (iIndex >= 0 && iIndex < iCounter) {
            if (jbtnFirst != null) jbtnFirst.setEnabled(iIndex > 0);
            // if (jbtnPrev != null) jbtnPrev.setEnabled(iIndex > 0);
            // if (jbtnNext != null) jbtnNext.setEnabled(iIndex < iCounter - 1);
            if (jbtnLast != null) jbtnLast.setEnabled(iIndex < iCounter - 1);
            if (jbtnRefresh != null) jbtnRefresh.setEnabled(true);
        } else {
            // EOF
            if (jbtnFirst != null) jbtnFirst.setEnabled(false);
            // if (jbtnPrev != null) jbtnPrev.setEnabled(false);
            // if (jbtnNext != null) jbtnNext.setEnabled(false);
            if (jbtnLast != null) jbtnLast.setEnabled(false);
            if (jbtnRefresh != null) jbtnRefresh.setEnabled(false);
        }
    }   
    
    private void jbtnSortActionPerformed(java.awt.event.ActionEvent evt) {                                         
        try {
            Comparator c = JSort.showMessage(this, m_cc);
            if (c != null) {
                m_bd.sort(c);
            }
        } catch (BasicException eD) {
            MessageInf msg = new MessageInf(MessageInf.SGN_NOTICE, LocalRes.getIntString("message.nolistdata"), eD);
            msg.show(this);
        }  
    }
    
    private void jbtnFindActionPerformed(java.awt.event.ActionEvent evt) {                                         
        
        try {
            FindInfo newFindInfo = JFind.showMessage(this, m_LastFindInfo);
            if (newFindInfo != null) {
                m_LastFindInfo = newFindInfo;
                
                int index = m_bd.findNext(newFindInfo);
                if (index < 0) {
                    MessageInf msg = new MessageInf(MessageInf.SGN_NOTICE, LocalRes.getIntString("message.norecord"));
                    msg.show(this);
                } else {
                    m_bd.moveTo(index);
                }
            }
        } catch (BasicException eD) {
            MessageInf msg = new MessageInf(MessageInf.SGN_NOTICE, LocalRes.getIntString("message.nolistdata"), eD);
            msg.show(this);
        }           
    }                                        

    private void jbtnRefreshActionPerformed(java.awt.event.ActionEvent evt) {                                            
       
        m_bd.actionReloadCurrent(this);       
    }                                           

    private void jbtnReloadActionPerformed(java.awt.event.ActionEvent evt) {                                           

        try {
            m_bd.actionLoad();
        } catch (BasicException eD) {
            MessageInf msg = new MessageInf(MessageInf.SGN_NOTICE, LocalRes.getIntString("message.noreload"), eD);
            msg.show(this);
        }
    }                                          

    private void jbtnLastActionPerformed(java.awt.event.ActionEvent evt) {                                         

        try {
            m_bd.moveLast();
        } catch (BasicException eD) {
            MessageInf msg = new MessageInf(MessageInf.SGN_NOTICE, LocalRes.getIntString("message.nomove"), eD);
            msg.show(this);
        }
    }                                        

    private void jbtnFirstActionPerformed(java.awt.event.ActionEvent evt) {                                          

        try{
            m_bd.moveFirst();
        } catch (BasicException eD) {
            MessageInf msg = new MessageInf(MessageInf.SGN_NOTICE, LocalRes.getIntString("message.nomove"), eD);
            msg.show(this);
        }
    }                                         

    private void jbtnPrevActionPerformed(java.awt.event.ActionEvent evt) {                                         
        try {
            m_bd.movePrev();
        } catch (BasicException eD) {
            MessageInf msg = new MessageInf(MessageInf.SGN_NOTICE, LocalRes.getIntString("message.nomove"), eD);
            msg.show(this);
        }       
    }                                        

    private void jbtnNextActionPerformed(java.awt.event.ActionEvent evt) {
        try {
            m_bd.moveNext();
        } catch (BasicException eD) {
            MessageInf msg = new MessageInf(MessageInf.SGN_NOTICE, LocalRes.getIntString("message.nomove"), eD);
            msg.show(this);
        }     
    }                                        
       
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
    }// </editor-fold>//GEN-END:initComponents
   
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
    
}

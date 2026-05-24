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

import com.openbravo.data.gui.ListCellRendererBasic;
import com.openbravo.data.loader.ComparatorCreator;
import com.openbravo.data.loader.TableDefinition;
import com.openbravo.data.loader.Vectorer;
import com.openbravo.data.user.EditorRecord;
import com.openbravo.data.user.ListProvider;
import com.openbravo.data.user.ListProviderCreator;
import com.openbravo.data.user.DefaultSaveProvider;
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.pos.forms.DataLogicSystem;
import com.openbravo.pos.panels.JPanelTable;
import javax.swing.ListCellRenderer;

/**
 *
 * @author adrianromero
 */
public class ResourcesPanel extends JPanelTable {

    private static final long serialVersionUID = 1L;

    private TableDefinition<ResourceInfo> tresources;
    private ResourcesView jeditor;

    public ResourcesPanel() {}

    @Override
    protected void init() {
        DataLogicAdmin dlAdmin = (DataLogicAdmin) app.getBean("com.openbravo.pos.admin.DataLogicAdmin"); 
        DataLogicSystem dlSystem = (DataLogicSystem) app.getBean("com.openbravo.pos.forms.DataLogicSystem");
        tresources = dlSystem.getTableResources();         
        jeditor = new ResourcesView(dirty);           
    }

    @Override
    public boolean deactivate() {
        return super.deactivate();    
    }

    @Override
    public ListProvider<ResourceInfo> getListProvider() {
        return new ListProviderCreator(tresources);
    }

    @Override
    public DefaultSaveProvider getSaveProvider() {
        return new DefaultSaveProvider(tresources);        
    }

    @Override
    public Vectorer getVectorer() {
        return tresources.getVectorerBasic(new int[] {1});
    }

    @Override
    public ComparatorCreator getComparatorCreator() {
        return tresources.getComparatorCreator(new int[] {1, 2});
    }

    @Override
    public ListCellRenderer getListCellRenderer() {
        return new ListCellRendererBasic(tresources.getRenderStringBasic(new int[] {1}));
    }

    public EditorRecord getEditor() {
        return jeditor;
    }
 
    @Override
    public String getTitle() {
        return AppLocal.getIntString("Menu.Resources");
    }

    @Override
    public void activate() throws com.openbravo.basic.BasicException {
        javax.swing.JPanel panel = new javax.swing.JPanel(new java.awt.BorderLayout(5, 5));
        javax.swing.JLabel label = new javax.swing.JLabel("Ingrese la clave de seguridad para administrar recursos:");
        javax.swing.JPasswordField pf = new javax.swing.JPasswordField();
        
        label.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 12));
        pf.setFont(new java.awt.Font("Arial", java.awt.Font.PLAIN, 14));
        
        pf.addActionListener(e -> {
            javax.swing.JComponent comp = (javax.swing.JComponent) e.getSource();
            javax.swing.JOptionPane optionPane = (javax.swing.JOptionPane) javax.swing.SwingUtilities.getAncestorOfClass(javax.swing.JOptionPane.class, comp);
            if (optionPane != null) {
                optionPane.setValue(javax.swing.JOptionPane.OK_OPTION);
            }
        });
        
        panel.add(label, java.awt.BorderLayout.NORTH);
        panel.add(pf, java.awt.BorderLayout.CENTER);
        
        javax.swing.SwingUtilities.invokeLater(() -> pf.requestFocusInWindow());

        int ok = javax.swing.JOptionPane.showConfirmDialog(
            this, 
            panel, 
            "Acceso Protegido", 
            javax.swing.JOptionPane.OK_CANCEL_OPTION, 
            javax.swing.JOptionPane.QUESTION_MESSAGE
        );
        
        if (ok == javax.swing.JOptionPane.OK_OPTION) {
            String password = new String(pf.getPassword());
            if ("123456".equals(password)) {
                super.activate();
                return;
            }
        }
        
        javax.swing.JOptionPane.showMessageDialog(
            this, 
            "Clave incorrecta. Acceso denegado.", 
            "Error de Autenticación", 
            javax.swing.JOptionPane.ERROR_MESSAGE
        );
        throw new com.openbravo.basic.BasicException("Acceso no autorizado.");
    }
}

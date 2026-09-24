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
package com.openbravo.pos.catalog;

import java.awt.*;
import java.awt.event.ActionListener;
import javax.swing.*;

/**
 * Panel de catálogo modernizado con mejor distribución de productos
 * @author Sebastian - Versión mejorada
 */
public class JCatalogTab extends javax.swing.JPanel {

    private static final long serialVersionUID = 1L;
    
    private static final int CATALOG_BUTTON_WITH = 170; // Aumentado para mejor apariencia
    private static final int CATALOG_BUTTON_HEIGHT = 200; // Aumentado para mejor proporción

    private final JFlowPanel flowpanel;

    public JCatalogTab() {
        initComponents();

        flowpanel = new JFlowPanel();
        
        JScrollPane scroll = new JScrollPane(flowpanel);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroll.getVerticalScrollBar().setPreferredSize(new Dimension(12, 12)); // Scrollbar más delgado
        scroll.setBorder(null); // Sin borde para apariencia limpia
        scroll.getViewport().setBackground(new Color(250, 247, 242));
        scroll.getVerticalScrollBar().setUnitIncrement(16); // Desplazamiento más suave

        add(scroll, BorderLayout.CENTER);
    }

    @Override
    public void setEnabled(boolean value) {
        flowpanel.setEnabled(value);
        super.setEnabled(value);
    }

    public void addButton(Icon icon, ActionListener actionListener, String text, String textTip) {
        
        CatalogItem item = new CatalogItem(text);
        item.setTextTip(textTip);
        // Convertir Icon a Image si es necesario
        if (icon instanceof ImageIcon) {
            item.setImage(((ImageIcon)icon).getImage());
        }
        ProductCardV3 prodCard = new ProductCardV3(item, actionListener);
        
        flowpanel.add(prodCard);
    }
    
    public void addCatalogItem(CatalogItem catalogItem, ActionListener actionListener) {
        ProductCardV3 prodCard = new ProductCardV3(catalogItem, actionListener);
        flowpanel.add(prodCard);
    }
    
    public void showEmptyState(ActionListener onShowAll) {
        flowpanel.removeAll();
        
        JPanel emptyPanel = new JPanel();
        emptyPanel.setLayout(new BoxLayout(emptyPanel, BoxLayout.Y_AXIS));
        emptyPanel.setOpaque(false);
        emptyPanel.setBorder(BorderFactory.createEmptyBorder(60, 20, 40, 20));

        JLabel iconLabel = new JLabel("📦");
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 56));
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        emptyPanel.add(iconLabel);

        emptyPanel.add(Box.createVerticalStrut(15));

        JLabel titleLabel = new JLabel("No hay productos en esta categoría");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(new Color(55, 65, 81));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        emptyPanel.add(titleLabel);

        emptyPanel.add(Box.createVerticalStrut(8));

        JLabel subLabel = new JLabel("Puedes registrar o asociar productos en Administración > Productos");
        subLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subLabel.setForeground(new Color(107, 114, 128));
        subLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        emptyPanel.add(subLabel);

        if (onShowAll != null) {
            emptyPanel.add(Box.createVerticalStrut(20));
            JButton btnAll = new JButton("⭐ Ver todos los productos");
            btnAll.setFont(new Font("Segoe UI", Font.BOLD, 13));
            btnAll.setBackground(new Color(202, 159, 65));
            btnAll.setForeground(Color.WHITE);
            btnAll.setFocusPainted(false);
            btnAll.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btnAll.setBorder(BorderFactory.createEmptyBorder(10, 22, 10, 22));
            btnAll.setAlignmentX(Component.CENTER_ALIGNMENT);
            btnAll.addActionListener(onShowAll);
            emptyPanel.add(btnAll);
        }

        flowpanel.setLayout(new BorderLayout());
        flowpanel.add(emptyPanel, BorderLayout.CENTER);
        flowpanel.revalidate();
        flowpanel.repaint();
    }
    public void clearItems() {
        flowpanel.removeAll();
        flowpanel.resetLayout();
        flowpanel.revalidate();
        flowpanel.repaint();
    }

    public void showSearchEmptyState(String query) {
        flowpanel.removeAll();
        
        JPanel emptyPanel = new JPanel();
        emptyPanel.setLayout(new BoxLayout(emptyPanel, BoxLayout.Y_AXIS));
        emptyPanel.setOpaque(false);
        emptyPanel.setBorder(BorderFactory.createEmptyBorder(60, 20, 40, 20));

        JLabel iconLabel = new JLabel("🔍");
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 56));
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        emptyPanel.add(iconLabel);

        emptyPanel.add(Box.createVerticalStrut(15));

        JLabel titleLabel = new JLabel("No se encontraron productos para \"" + (query != null ? query : "") + "\"");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(new Color(55, 65, 81));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        emptyPanel.add(titleLabel);

        emptyPanel.add(Box.createVerticalStrut(8));

        JLabel subLabel = new JLabel("Intenta buscar por nombre, referencia o código de barras");
        subLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subLabel.setForeground(new Color(107, 114, 128));
        subLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        emptyPanel.add(subLabel);

        flowpanel.setLayout(new BorderLayout());
        flowpanel.add(emptyPanel, BorderLayout.CENTER);
        flowpanel.revalidate();
        flowpanel.repaint();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        setLayout(new java.awt.BorderLayout());
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}

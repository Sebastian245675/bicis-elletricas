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

package com.openbravo.pos.menu;

import com.openbravo.basic.BasicException;
import com.openbravo.pos.forms.JPanelView;
import java.awt.Component;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JLabel;

/**
 *
 * @author adrianromero
 */
public class JPanelMenu extends JPanel implements JPanelView {
    
    private final MenuDefinition m_menu;
    private boolean created = false;
    
    private static class Section {
        String name;
        java.util.List<javax.swing.Action> items = new java.util.ArrayList<>();
        
        Section(String name) {
            this.name = name;
        }
    }
    
    private final java.util.List<Section> sections = new java.util.ArrayList<>();
    private Section currentSection = null;
    
    public JPanelMenu(MenuDefinition menu) {
        m_menu = menu;
        created = false;
       
        initComponents();  
        setBackground(new java.awt.Color(39, 39, 39));
        menucontainer.setBackground(new java.awt.Color(39, 39, 39));
    }
    
    /**
     *
     * @return
     */
    @Override
    public JComponent getComponent() {
        return this;
    }
    
    /**
     *
     * @return
     */
    @Override
    public String getTitle() {
        return m_menu.getTitle();
    }

    /**
     *
     * @throws BasicException
     */
    @Override
    public void activate() throws BasicException {
        if (created == false) {
            for(int i = 0; i < m_menu.countMenuElements(); i++) {
                MenuElement menuitem = m_menu.getMenuElement(i);
                menuitem.addComponent(this);
            }            
            created = true;
        }
        
        renderListLayout();
    }

    /**
     *
     * @return
     */
    @Override
    public boolean deactivate() {  
        return true;
    }
    
    /**
     *
     * @param title
     */
    public void addTitle(Component title) {
        String name = "";
        if (title instanceof javax.swing.JLabel) {
            name = ((javax.swing.JLabel) title).getText();
        }
        currentSection = new Section(name);
        sections.add(currentSection);
        
        currententrypanel = null;
        JPanel titlepanel = new JPanel();
        titlepanel.setBackground(new java.awt.Color(39, 39, 39));
        titlepanel.setLayout(new java.awt.BorderLayout());
        titlepanel.add(title, java.awt.BorderLayout.CENTER);     
        titlepanel.applyComponentOrientation(getComponentOrientation());
        menucontainer.add(titlepanel);
    }
    
    /**
     *
     * @param entry
     */
    public void addEntry(Component entry) {
        if (entry instanceof javax.swing.JButton) {
            javax.swing.JButton btn = (javax.swing.JButton) entry;
            javax.swing.Action act = btn.getAction();
            if (act != null) {
                if (currentSection == null) {
                    currentSection = new Section("Opciones");
                    sections.add(currentSection);
                }
                currentSection.items.add(act);
            }
        }
        
        if (currententrypanel == null) {
            currententrypanel = new JPanel();                    
            currententrypanel.setBackground(new java.awt.Color(39, 39, 39));
            currententrypanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 0, 20, 0));
            currententrypanel.setLayout(new java.awt.GridLayout(0, 6, 5, 5));            
            menucontainer.add(currententrypanel);
        }
        currententrypanel.add(entry);
        currententrypanel.applyComponentOrientation(getComponentOrientation());
    }
    
    private void renderListLayout() {
        this.removeAll();
        this.setLayout(new java.awt.BorderLayout());
        this.setBackground(new java.awt.Color(39, 39, 39));
        this.setBorder(javax.swing.BorderFactory.createEmptyBorder(24, 32, 24, 32));
        
        // Header
        JPanel headerPanel = new JPanel();
        headerPanel.setOpaque(false);
        headerPanel.setLayout(new javax.swing.BoxLayout(headerPanel, javax.swing.BoxLayout.Y_AXIS));
        
        JLabel titleLabel = new JLabel(stripHtml(getTitle()));
        titleLabel.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 28));
        titleLabel.setForeground(new java.awt.Color(245, 245, 245));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        headerPanel.add(titleLabel);
        
        headerPanel.add(javax.swing.Box.createVerticalStrut(8));
        
        JPanel underline = new JPanel() {
            @Override
            protected void paintComponent(java.awt.Graphics g) {
                java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
                g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                java.awt.GradientPaint gp = new java.awt.GradientPaint(
                    0, 0, new java.awt.Color(238, 150, 28),
                    180, 0, new java.awt.Color(238, 150, 28, 0)
                );
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        underline.setPreferredSize(new java.awt.Dimension(180, 3));
        underline.setMaximumSize(new java.awt.Dimension(180, 3));
        underline.setAlignmentX(Component.LEFT_ALIGNMENT);
        headerPanel.add(underline);
        
        headerPanel.add(javax.swing.Box.createVerticalStrut(24));
        this.add(headerPanel, java.awt.BorderLayout.NORTH);
        
        // List items container
        JPanel listContainer = new JPanel();
        listContainer.setOpaque(false);
        listContainer.setLayout(new javax.swing.BoxLayout(listContainer, javax.swing.BoxLayout.Y_AXIS));
        
        for (Section section : sections) {
            for (final javax.swing.Action act : section.items) {
                JPanel rowPanel = new JPanel();
                rowPanel.setOpaque(true);
                rowPanel.setBackground(new java.awt.Color(39, 39, 39));
                rowPanel.setLayout(new java.awt.BorderLayout());
                rowPanel.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                    javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, new java.awt.Color(55, 55, 55)),
                    javax.swing.BorderFactory.createEmptyBorder(12, 16, 12, 16)
                ));
                
                // Dot Panel
                final String currentSectionName = section.name;
                JPanel dotPanel = new JPanel() {
                    private final java.awt.Color dotColor = resolveDotColor(currentSectionName);
                    @Override
                    protected void paintComponent(java.awt.Graphics g) {
                        super.paintComponent(g);
                        java.awt.Graphics2D g2d = (java.awt.Graphics2D) g.create();
                        g2d.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                        g2d.setColor(dotColor);
                        int size = 10;
                        int x = (getWidth() - size) / 2;
                        int y = (getHeight() - size) / 2;
                        g2d.fillOval(x, y, size, size);
                        g2d.dispose();
                    }
                };
                dotPanel.setOpaque(false);
                dotPanel.setPreferredSize(new java.awt.Dimension(24, 24));
                
                // Title and Path Labels
                final String titleText = (String) act.getValue(javax.swing.Action.NAME);
                final JLabel rowTitleLabel = new JLabel(stripHtml(titleText));
                rowTitleLabel.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 15));
                rowTitleLabel.setForeground(new java.awt.Color(230, 230, 230));
                
                String mainCat = getTitle();
                String cleanMain = stripHtml(mainCat);
                String cleanSec = stripHtml(section.name);
                String cleanItem = stripHtml(titleText);
                String pathText = cleanMain + "  \u2192  " + cleanSec + "  \u2192  " + cleanItem;
                
                JLabel pathLabel = new JLabel(pathText);
                pathLabel.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 12));
                pathLabel.setForeground(new java.awt.Color(150, 150, 150));
                
                // Stack layout
                JPanel contentPanel = new JPanel();
                contentPanel.setOpaque(false);
                contentPanel.setLayout(new javax.swing.BoxLayout(contentPanel, javax.swing.BoxLayout.Y_AXIS));
                
                JPanel topLine = new JPanel(new java.awt.BorderLayout(10, 0));
                topLine.setOpaque(false);
                topLine.add(dotPanel, java.awt.BorderLayout.WEST);
                topLine.add(rowTitleLabel, java.awt.BorderLayout.CENTER);
                contentPanel.add(topLine);
                
                contentPanel.add(javax.swing.Box.createVerticalStrut(4));
                
                JPanel bottomLine = new JPanel(new java.awt.BorderLayout());
                bottomLine.setOpaque(false);
                JPanel indentPanel = new JPanel();
                indentPanel.setOpaque(false);
                indentPanel.setPreferredSize(new java.awt.Dimension(34, 1));
                bottomLine.add(indentPanel, java.awt.BorderLayout.WEST);
                bottomLine.add(pathLabel, java.awt.BorderLayout.CENTER);
                contentPanel.add(bottomLine);
                
                rowPanel.add(contentPanel, java.awt.BorderLayout.CENTER);
                
                // Interactivity
                final JPanel finalRowPanel = rowPanel;
                rowPanel.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
                rowPanel.addMouseListener(new java.awt.event.MouseAdapter() {
                    @Override
                    public void mouseEntered(java.awt.event.MouseEvent e) {
                        finalRowPanel.setBackground(new java.awt.Color(49, 49, 49));
                        rowTitleLabel.setForeground(new java.awt.Color(238, 150, 28));
                    }
                    
                    @Override
                    public void mouseExited(java.awt.event.MouseEvent e) {
                        finalRowPanel.setBackground(new java.awt.Color(39, 39, 39));
                        rowTitleLabel.setForeground(new java.awt.Color(230, 230, 230));
                    }
                    
                    @Override
                    public void mousePressed(java.awt.event.MouseEvent e) {
                        act.actionPerformed(new java.awt.event.ActionEvent(finalRowPanel, java.awt.event.ActionEvent.ACTION_PERFORMED, ""));
                    }
                });
                
                listContainer.add(rowPanel);
                listContainer.add(javax.swing.Box.createVerticalStrut(1));
            }
        }
        
        javax.swing.JScrollPane scrollPane = new javax.swing.JScrollPane(listContainer);
        scrollPane.setBorder(javax.swing.BorderFactory.createEmptyBorder());
        scrollPane.setViewportBorder(javax.swing.BorderFactory.createEmptyBorder());
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        
        this.add(scrollPane, java.awt.BorderLayout.CENTER);
        
        this.revalidate();
        this.repaint();
    }
    
    private java.awt.Color resolveDotColor(String sectionName) {
        String normalized = sectionName == null ? "" : sectionName.toLowerCase();
        if (normalized.contains("reportes") || normalized.contains("reports") || normalized.contains("graficos") || normalized.contains("charts")) {
            return new java.awt.Color(33, 150, 243);
        } else if (normalized.contains("import") || normalized.contains("herramientas") || normalized.contains("mantenimiento")) {
            return new java.awt.Color(46, 204, 113);
        } else {
            return new java.awt.Color(238, 150, 28);
        }
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
    
    public String[] findTaskPath(String taskClass) {
        if (taskClass == null) {
            return null;
        }
        for (Section sec : sections) {
            for (javax.swing.Action act : sec.items) {
                String actTask = (String) act.getValue(com.openbravo.pos.forms.AppUserView.ACTION_TASKNAME);
                if (taskClass.equals(actTask)) {
                    return new String[]{ sec.name, (String) act.getValue(javax.swing.Action.NAME) };
                }
            }
        }
        return null;
    }
    
    private JPanel currententrypanel = null;
    
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        menucontainer = new javax.swing.JPanel();

        setBorder(javax.swing.BorderFactory.createEmptyBorder(20, 20, 20, 20));
        setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        setLayout(new java.awt.BorderLayout());

        menucontainer.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        menucontainer.setLayout(new javax.swing.BoxLayout(menucontainer, javax.swing.BoxLayout.Y_AXIS));
        add(menucontainer, java.awt.BorderLayout.NORTH);
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel menucontainer;
    // End of variables declaration//GEN-END:variables
    
}

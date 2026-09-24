package com.openbravo.pos.catalog;

import java.awt.*;
import javax.swing.*;

/**
 * Panel de flujo personalizado para productos con mejor distribución
 */
public class JFlowPanel extends JPanel implements Scrollable {
    
    private static final long serialVersionUID = 1L;
    
    private static final int DEFAULT_HGAP = 20;
    private static final int DEFAULT_VGAP = 20;
    
    public JFlowPanel() {
        super();
        // Usar GridBagLayout para mejor control
        setLayout(new ModernFlowLayout(FlowLayout.LEFT, DEFAULT_HGAP, DEFAULT_VGAP));
        setOpaque(true);
        setBackground(new Color(250, 247, 242));
    }

    public void resetLayout() {
        setLayout(new ModernFlowLayout(FlowLayout.LEFT, DEFAULT_HGAP, DEFAULT_VGAP));
    }

    @Override
    public Dimension getPreferredScrollableViewportSize() {
        return getPreferredSize();
    }

    @Override
    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
        return 24;
    }

    @Override
    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
        return 72;
    }

    @Override
    public boolean getScrollableTracksViewportWidth() {
        return true;
    }

    @Override
    public boolean getScrollableTracksViewportHeight() {
        return false;
    }
    
    /**
     * Layout personalizado que distribuye mejor los elementos
     */
    private static class ModernFlowLayout extends FlowLayout {
        
        public ModernFlowLayout(int align, int hgap, int vgap) {
            super(align, hgap, vgap);
        }

        @Override
        public Dimension preferredLayoutSize(Container target) {
            return computeLayoutSize(target, true);
        }

        @Override
        public Dimension minimumLayoutSize(Container target) {
            return computeLayoutSize(target, false);
        }

        private Dimension computeLayoutSize(Container target, boolean preferred) {
            synchronized (target.getTreeLock()) {
                int targetWidth = target.getWidth();
                if (targetWidth <= 0 && target.getParent() != null) {
                    targetWidth = target.getParent().getWidth();
                }
                if (targetWidth <= 0) {
                    targetWidth = 800;
                }

                Insets insets = target.getInsets();
                int maxWidth = targetWidth - (insets.left + insets.right + getHgap() * 2);
                if (maxWidth <= 0) {
                    maxWidth = Integer.MAX_VALUE;
                }

                int nmembers = target.getComponentCount();
                int x = 0;
                int y = insets.top + getVgap();
                int rowh = 0;
                int maxRowWidth = 0;

                for (int i = 0; i < nmembers; i++) {
                    Component m = target.getComponent(i);
                    if (m.isVisible()) {
                        Dimension d = preferred ? m.getPreferredSize() : m.getMinimumSize();
                        if (x > 0 && (x + d.width) > maxWidth) {
                            y += getVgap() + rowh;
                            x = 0;
                            rowh = 0;
                        }
                        if (x > 0) {
                            x += getHgap();
                        }
                        x += d.width;
                        rowh = Math.max(rowh, d.height);
                        maxRowWidth = Math.max(maxRowWidth, x);
                    }
                }
                y += rowh + getVgap() + insets.bottom;
                return new Dimension(Math.max(maxRowWidth + insets.left + insets.right + getHgap() * 2, targetWidth), y);
            }
        }
        
        @Override
        public void layoutContainer(Container target) {
            synchronized (target.getTreeLock()) {
                Insets insets = target.getInsets();
                int maxWidth = target.getWidth() - (insets.left + insets.right + getHgap() * 2);
                int nmembers = target.getComponentCount();
                int x = insets.left + getHgap();
                int y = insets.top + getVgap();
                int rowh = 0;
                int start = 0;
                
                boolean ltr = target.getComponentOrientation().isLeftToRight();
                
                for (int i = 0; i < nmembers; i++) {
                    Component m = target.getComponent(i);
                    if (m.isVisible()) {
                        Dimension d = m.getPreferredSize();
                        m.setSize(d.width, d.height);
                        
                        if ((x == insets.left + getHgap()) || ((x + d.width) <= maxWidth)) {
                            if (x > insets.left + getHgap()) {
                                x += getHgap();
                            }
                            x += d.width;
                            rowh = Math.max(rowh, d.height);
                        } else {
                            rowh = moveComponents(target, insets.left + getHgap(), y, 
                                                maxWidth - x, rowh, start, i, ltr);
                            y += getVgap() + rowh;
                            rowh = d.height;
                            x = insets.left + getHgap() + d.width;
                            start = i;
                        }
                    }
                }
                moveComponents(target, insets.left + getHgap(), y, maxWidth - x, rowh, start, nmembers, ltr);
            }
        }
        
        private int moveComponents(Container target, int x, int y, int width, int height,
                                 int rowStart, int rowEnd, boolean ltr) {
            switch (getAlignment()) {
                case LEFT:
                    x += ltr ? 0 : width;
                    break;
                case CENTER:
                    x += width / 2;
                    break;
                case RIGHT:
                    x += ltr ? width : 0;
                    break;
                case LEADING:
                    break;
                case TRAILING:
                    x += width;
                    break;
            }
            
            for (int i = rowStart; i < rowEnd; i++) {
                Component m = target.getComponent(i);
                if (m.isVisible()) {
                    if (ltr) {
                        m.setLocation(x, y + (height - m.getHeight()) / 2);
                    } else {
                        m.setLocation(target.getWidth() - x - m.getWidth(), 
                                    y + (height - m.getHeight()) / 2);
                    }
                    x += m.getWidth() + getHgap();
                }
            }
            return height;
        }
    }
}
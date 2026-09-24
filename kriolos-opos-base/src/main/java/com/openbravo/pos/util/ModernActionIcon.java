package com.openbravo.pos.util;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Arc2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import javax.swing.Icon;

/** Crisp, resolution-independent icons used by the shared POS toolbars. */
public final class ModernActionIcon implements Icon {

    public enum Type { REFRESH, ADD, DELETE, SAVE, HISTORY, EXPORT, CALENDAR, DOCUMENT, PRINT, VIEW, COPY,
        SEARCH, EDIT, BOX, CALCULATOR, MONEY, INFO }

    private final Type type;
    private final int size;
    private final Color color;

    public ModernActionIcon(Type type, int size) {
        this(type, size, new Color(51, 65, 85));
    }

    public ModernActionIcon(Type type, int size, Color color) {
        this.type = type;
        this.size = size;
        this.color = color;
    }

    @Override
    public int getIconWidth() { return size; }

    @Override
    public int getIconHeight() { return size; }

    @Override
    public void paintIcon(Component component, Graphics graphics, int x, int y) {
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        Color paint = component != null && !component.isEnabled()
                ? new Color(color.getRed(), color.getGreen(), color.getBlue(), 105) : color;
        g.setColor(paint);
        float u = size / 24f;
        g.setStroke(new BasicStroke(Math.max(1.8f, 2f * u), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.translate(x, y);
        switch (type) {
            case REFRESH -> paintRefresh(g, u);
            case ADD -> paintAdd(g, u);
            case DELETE -> paintDelete(g, u);
            case SAVE -> paintSave(g, u);
            case HISTORY -> paintHistory(g, u);
            case EXPORT -> paintExport(g, u);
            case CALENDAR -> paintCalendar(g, u);
            case DOCUMENT -> paintDocument(g, u);
            case PRINT -> paintPrint(g, u);
            case VIEW -> paintView(g, u);
            case COPY -> paintCopy(g, u);
            case SEARCH -> paintSearch(g, u);
            case EDIT -> paintEdit(g, u);
            case BOX -> paintBox(g, u);
            case CALCULATOR -> paintCalculator(g, u);
            case MONEY -> paintMoney(g, u);
            case INFO -> paintInfo(g, u);
        }
        g.dispose();
    }

    private void paintSearch(Graphics2D g, float u) {
        g.drawOval(Math.round(4*u), Math.round(4*u), Math.round(11*u), Math.round(11*u));
        g.drawLine(Math.round(14*u), Math.round(14*u), Math.round(20*u), Math.round(20*u));
    }

    private void paintEdit(Graphics2D g, float u) {
        Path2D p = new Path2D.Float();
        p.moveTo(5*u, 16*u); p.lineTo(4*u, 20*u); p.lineTo(8*u, 19*u);
        p.lineTo(19*u, 8*u); p.lineTo(16*u, 5*u); p.closePath();
        g.draw(p);
        g.drawLine(Math.round(14*u), Math.round(7*u), Math.round(18*u), Math.round(11*u));
    }

    private void paintBox(Graphics2D g, float u) {
        Path2D p = new Path2D.Float();
        p.moveTo(3*u, 7*u); p.lineTo(12*u, 3*u); p.lineTo(21*u, 7*u);
        p.lineTo(21*u, 17*u); p.lineTo(12*u, 21*u); p.lineTo(3*u, 17*u); p.closePath();
        g.draw(p);
        g.drawLine(Math.round(3*u), Math.round(7*u), Math.round(12*u), Math.round(11*u));
        g.drawLine(Math.round(21*u), Math.round(7*u), Math.round(12*u), Math.round(11*u));
        g.drawLine(Math.round(12*u), Math.round(11*u), Math.round(12*u), Math.round(21*u));
    }

    private void paintCalculator(Graphics2D g, float u) {
        g.draw(new RoundRectangle2D.Float(5*u, 2*u, 14*u, 20*u, 2*u, 2*u));
        g.draw(new RoundRectangle2D.Float(8*u, 5*u, 8*u, 4*u, u, u));
        for (int row = 0; row < 2; row++) {
            for (int col = 0; col < 3; col++) {
                g.fillOval(Math.round((8 + col*4)*u), Math.round((13 + row*4)*u), Math.max(2, Math.round(2*u)), Math.max(2, Math.round(2*u)));
            }
        }
    }

    private void paintMoney(Graphics2D g, float u) {
        g.draw(new RoundRectangle2D.Float(2*u, 5*u, 20*u, 14*u, 3*u, 3*u));
        g.drawOval(Math.round(9*u), Math.round(8*u), Math.round(6*u), Math.round(8*u));
        g.drawLine(Math.round(5*u), Math.round(9*u), Math.round(5*u), Math.round(15*u));
        g.drawLine(Math.round(19*u), Math.round(9*u), Math.round(19*u), Math.round(15*u));
    }

    private void paintInfo(Graphics2D g, float u) {
        g.drawOval(Math.round(3*u), Math.round(3*u), Math.round(18*u), Math.round(18*u));
        g.fillOval(Math.round(11*u), Math.round(7*u), Math.max(2, Math.round(2*u)), Math.max(2, Math.round(2*u)));
        g.drawLine(Math.round(12*u), Math.round(12*u), Math.round(12*u), Math.round(17*u));
    }

    private void paintRefresh(Graphics2D g, float u) {
        g.draw(new Arc2D.Float(4*u, 4*u, 16*u, 16*u, 35, 285, Arc2D.OPEN));
        Path2D p = new Path2D.Float();
        p.moveTo(17.5*u, 3.5*u); p.lineTo(20.5*u, 8.2*u); p.lineTo(15.1*u, 8.3*u); p.closePath();
        g.fill(p);
    }

    private void paintAdd(Graphics2D g, float u) {
        g.drawOval(Math.round(3*u), Math.round(3*u), Math.round(18*u), Math.round(18*u));
        g.drawLine(px(12*u), px(7.5f*u), px(12*u), px(16.5f*u));
        g.drawLine(px(7.5f*u), px(12*u), px(16.5f*u), px(12*u));
    }

    private void paintDelete(Graphics2D g, float u) {
        g.draw(new RoundRectangle2D.Float(6*u, 7*u, 12*u, 13*u, 2*u, 2*u));
        g.drawLine(px(4.5f*u), px(6*u), px(19.5f*u), px(6*u));
        g.drawLine(px(9*u), px(3.5f*u), px(15*u), px(3.5f*u));
        g.drawLine(px(10*u), px(10*u), px(10*u), px(17*u));
        g.drawLine(px(14*u), px(10*u), px(14*u), px(17*u));
    }

    private void paintSave(Graphics2D g, float u) {
        g.draw(new RoundRectangle2D.Float(4*u, 3*u, 16*u, 18*u, 2*u, 2*u));
        g.drawRect(px(8*u), px(3*u), px(8*u), px(6*u));
        g.draw(new RoundRectangle2D.Float(7*u, 13*u, 10*u, 8*u, 1.5f*u, 1.5f*u));
    }

    private void paintHistory(Graphics2D g, float u) {
        g.draw(new Arc2D.Float(4*u, 4*u, 16*u, 16*u, 48, 292, Arc2D.OPEN));
        Path2D p = new Path2D.Float();
        p.moveTo(4.2*u, 4.5*u); p.lineTo(4.4*u, 10*u); p.lineTo(9*u, 6.7*u); p.closePath();
        g.fill(p);
        g.drawLine(px(12*u), px(8*u), px(12*u), px(12.5f*u));
        g.drawLine(px(12*u), px(12.5f*u), px(15.5f*u), px(14.5f*u));
    }

    private void paintExport(Graphics2D g, float u) {
        g.draw(new RoundRectangle2D.Float(4*u, 13*u, 16*u, 7*u, 2*u, 2*u));
        g.drawLine(px(12*u), px(4*u), px(12*u), px(15*u));
        g.drawLine(px(7.5f*u), px(10.5f*u), px(12*u), px(15*u));
        g.drawLine(px(16.5f*u), px(10.5f*u), px(12*u), px(15*u));
    }

    private void paintCalendar(Graphics2D g, float u) {
        g.draw(new RoundRectangle2D.Float(3*u, 5*u, 18*u, 16*u, 2*u, 2*u));
        g.drawLine(px(3*u), px(10*u), px(21*u), px(10*u));
        g.drawLine(px(8*u), px(3*u), px(8*u), px(7*u));
        g.drawLine(px(16*u), px(3*u), px(16*u), px(7*u));
    }

    private void paintDocument(Graphics2D g, float u) {
        Path2D p = new Path2D.Float();
        p.moveTo(6*u, 3*u); p.lineTo(15*u, 3*u); p.lineTo(20*u, 8*u);
        p.lineTo(20*u, 21*u); p.lineTo(6*u, 21*u); p.closePath();
        g.draw(p);
        g.drawLine(px(15*u), px(3*u), px(15*u), px(8*u));
        g.drawLine(px(15*u), px(8*u), px(20*u), px(8*u));
        g.drawLine(px(9*u), px(13*u), px(17*u), px(13*u));
        g.drawLine(px(9*u), px(17*u), px(17*u), px(17*u));
    }

    private void paintPrint(Graphics2D g, float u) {
        g.drawRect(px(7*u), px(3*u), px(10*u), px(6*u));
        g.draw(new RoundRectangle2D.Float(3*u, 8*u, 18*u, 9*u, 2*u, 2*u));
        g.drawRect(px(7*u), px(14*u), px(10*u), px(7*u));
    }

    private void paintView(Graphics2D g, float u) {
        Path2D p = new Path2D.Float();
        p.moveTo(2*u, 12*u); p.curveTo(6*u, 5*u, 18*u, 5*u, 22*u, 12*u);
        p.curveTo(18*u, 19*u, 6*u, 19*u, 2*u, 12*u); p.closePath();
        g.draw(p); g.drawOval(px(9*u), px(9*u), px(6*u), px(6*u));
    }

    private void paintCopy(Graphics2D g, float u) {
        g.draw(new RoundRectangle2D.Float(8*u, 7*u, 12*u, 14*u, 2*u, 2*u));
        g.draw(new RoundRectangle2D.Float(4*u, 3*u, 12*u, 14*u, 2*u, 2*u));
    }

    private int px(float value) { return Math.round(value); }
}

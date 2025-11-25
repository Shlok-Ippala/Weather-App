package com.aurora.climatesync.view.dialog;

import com.aurora.climatesync.util.EventColorUtil;

import javax.swing.*;
import java.awt.*;

public class ColorListCellRenderer extends DefaultListCellRenderer {
    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        
        if (value instanceof String) {
            String colorName = (String) value;
            // Find index of this name in EventColorUtil.COLOR_NAMES
            int colorIndex = -1;
            for (int i = 0; i < EventColorUtil.COLOR_NAMES.length; i++) {
                if (EventColorUtil.COLOR_NAMES[i].equals(colorName)) {
                    colorIndex = i;
                    break;
                }
            }
            
            if (colorIndex != -1) {
                String colorId = EventColorUtil.COLOR_IDS[colorIndex];
                Color color = EventColorUtil.getEventColor(colorId);
                setIcon(new ColorIcon(color, 12, 12));
            }
        }
        
        return this;
    }
    
    private static class ColorIcon implements Icon {
        private final Color color;
        private final int width;
        private final int height;
        
        public ColorIcon(Color color, int width, int height) {
            this.color = color;
            this.width = width;
            this.height = height;
        }
        
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setColor(color);
            g2d.fillOval(x, y, width, height);
            g2d.dispose();
        }
        
        @Override
        public int getIconWidth() {
            return width;
        }
        
        @Override
        public int getIconHeight() {
            return height;
        }
    }
}

package com.luis.p.durao.AutoActionsDBAnalyser.GUI;

import com.luis.p.durao.AutoActionsDBAnalyser.Main;

import javax.swing.*;
import java.awt.Component;
import java.awt.event.ActionEvent;

public class AnalyseGUI {

    private JPanel mainPanel;
    private JButton button1;
    private JPanel panel;

    public AnalyseGUI() {
        panel.setLayout(new BoxLayout(panel,BoxLayout.Y_AXIS));
        button1.setAction(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JLabel label = new JLabel("Table " + panel.getComponentCount()/2);
                label.setAlignmentX(Component.CENTER_ALIGNMENT);
                String[] columnNames = {"C1", "C2"};
                Object[][] data = {{"a","b"},{"a","b"},{"a","b"},{"a","b"},{"a","b"},{"a","b"}};
                JTable table = new JTable(data,columnNames);
                table.setVisible(true);
                SwingUtilities.invokeLater(() -> {
                    panel.add(label);
                    panel.add(table);
                    panel.setVisible(true);
                    Main.frame.revalidate();
                    Main.frame.repaint();
                    Main.frame.pack();
                });
            }
        });
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }
}

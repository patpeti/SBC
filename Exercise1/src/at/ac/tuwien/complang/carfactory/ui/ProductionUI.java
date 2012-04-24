package at.ac.tuwien.complang.carfactory.ui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class ProductionUI extends JFrame {
    public ProductionUI() {
        showUI();
    }

    private void showUI() {
        JButton createPainterButton = new JButton("Create Painter");
        createPainterButton.setActionCommand("wheelfactory");
        JButton createAssemblerButton = new JButton("Create Assembler");
        createPainterButton.setActionCommand("assemble");
        JButton createLogisticanButton = new JButton("Create Logistican");
        createPainterButton.setActionCommand("logistican");
        
        JPanel panel = new JPanel();
        panel.add(createPainterButton);
        panel.add(createAssemblerButton);
        panel.add(createLogisticanButton);
        this.add(panel, BorderLayout.CENTER);
        
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.pack();
        this.setVisible(true);
    }
    
    class CreationListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            e.getActionCommand();
        }
        
    }
}

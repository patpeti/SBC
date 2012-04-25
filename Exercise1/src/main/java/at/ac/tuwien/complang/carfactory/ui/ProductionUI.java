package at.ac.tuwien.complang.carfactory.ui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSpinner;

public class ProductionUI extends JFrame {
	private static final long serialVersionUID = -6151830798597607052L;

	public ProductionUI() {
        showUI();
    }

    private void showUI() {
    	buildCreationPanel();
        
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.pack();
        this.setVisible(true);
    }

	private void buildCreationPanel() {
		CreationListener listener = new CreationListener();
    	
        JButton createBodyFactoryButton = new JButton("Create Body Factory");
        createBodyFactoryButton.setActionCommand("body");
        createBodyFactoryButton.addActionListener(listener);
        JButton createWheelFactoryButton = new JButton("Create Wheel Factory");
        createWheelFactoryButton.setActionCommand("wheel");
        createWheelFactoryButton.addActionListener(listener);
        JButton createMotorFactoryButton = new JButton("Create Motor Factory");
        createMotorFactoryButton.setActionCommand("motor");
        createMotorFactoryButton.addActionListener(listener);
        
        JSpinner bodyCountSpinner = new JSpinner();
        bodyCountSpinner.setValue(50);
        JSpinner wheelCountSpinner = new JSpinner();
        wheelCountSpinner.setValue(50);
        JSpinner motorCountSpinner = new JSpinner();
        motorCountSpinner.setValue(50);

        JPanel producerPanel = new JPanel(new GridLayout(3,2));
        producerPanel.add(createBodyFactoryButton);
        producerPanel.add(bodyCountSpinner);
        producerPanel.add(createWheelFactoryButton);
        producerPanel.add(wheelCountSpinner);
        producerPanel.add(createMotorFactoryButton);
        producerPanel.add(motorCountSpinner);
        this.add(producerPanel, BorderLayout.CENTER);
	}
    
    class CreationListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            System.out.println(e.getActionCommand());
        }
        
    }
}

package at.ac.tuwien.complang.carfactory.ui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;

import org.apache.commons.logging.impl.Jdk13LumberjackLogger;

import ch.qos.logback.core.pattern.SpacePadder;

import at.ac.tuwien.complang.carfactory.application.FactoryFacade;
import at.ac.tuwien.complang.carfactory.application.ProducerType;

public class ProductionUI extends JFrame {
	private static final long serialVersionUID = -6151830798597607052L;
	
	//Fields
	JSpinner bodyCountSpinner, wheelCountSpinner, motorCountSpinner;

	public ProductionUI() {
        showUI();
    }

    private void showUI() {
    	buildCreationPanel();
    	buildSpaceTable();
    	
        
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.pack();
        this.setVisible(true);
    }
    
    /**
     * Show the Table for the content of the space
     */
    private void buildSpaceTable() {
    	JPanel container = new JPanel();
    	JPanel spaceTablePanel = new JPanel();
    	BoxLayout layout = new BoxLayout(spaceTablePanel, BoxLayout.PAGE_AXIS);
    	spaceTablePanel.setLayout(layout);
    	JLabel label = new JLabel("Current Content of the Space");
    	label.setAlignmentX(CENTER_ALIGNMENT);
    	String[] columns = {"ID", "PartName", "PID"};
    	Object[][] data = {{1, "test", "PID"}, {2, "test2", "PID"}};
    	JTable table = new JTable(data, columns);
    	JScrollPane scrollPane = new JScrollPane(table);
    	table.setFillsViewportHeight(true);
    	spaceTablePanel.add(label);
    	spaceTablePanel.add(scrollPane);
    	container.add(spaceTablePanel);
    	this.add(container, BorderLayout.SOUTH);
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
        
        bodyCountSpinner = new JSpinner();
        bodyCountSpinner.setValue(50);
        wheelCountSpinner = new JSpinner();
        wheelCountSpinner.setValue(50);
        motorCountSpinner = new JSpinner();
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
            String command = e.getActionCommand();
            if(command.equals("body")) {
            	int value = (Integer) bodyCountSpinner.getValue();
            	FactoryFacade bodyFactory = FactoryFacade.getInstance(ProducerType.BODY);
            	bodyFactory.init(value);
            	bodyFactory.start();
            } else if(command.equals("wheel")) {
            	int value = (Integer) wheelCountSpinner.getValue();
            	FactoryFacade wheelFactory = FactoryFacade.getInstance(ProducerType.WHEEL);
            	wheelFactory.init(value);
            	wheelFactory.start();
            } else if(command.equals("motor")) {
            	int value = (Integer) motorCountSpinner.getValue();
            	FactoryFacade motorFactory = FactoryFacade.getInstance(ProducerType.MOTOR);
            	motorFactory.init(value);
            	motorFactory.start();
            	
            }
            
        }
        
    }
}

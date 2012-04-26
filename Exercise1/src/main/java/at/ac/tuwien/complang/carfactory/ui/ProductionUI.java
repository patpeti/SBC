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
import org.mozartspaces.core.Capi;
import org.mozartspaces.core.ContainerReference;

import ch.qos.logback.core.pattern.SpacePadder;

import at.ac.tuwien.complang.carfactory.application.FactoryFacade;
import at.ac.tuwien.complang.carfactory.application.enums.ProducerType;

public class ProductionUI extends JFrame {
	private static final long serialVersionUID = -6151830798597607052L;
	
	//Fields
	JSpinner bodyCountSpinner, wheelCountSpinner, motorCountSpinner;
	JPanel tableContainer;
	Capi capi;
	ContainerReference cref;

	public ProductionUI(Capi capi, ContainerReference cref) {
		this.capi = capi;
		this.cref = cref;
		tableContainer = new JPanel();
        showUI();
    }

    private void showUI() {
    	buildCreationPanel();
    	buildTables();
    	
        
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.pack();
        this.setVisible(true);
    }
    
    private void buildTables() {
    	JPanel spaceTable = buildSpaceTable();
    	JPanel finishedGoodsTable = buildFinishedGoodsTable();
    	tableContainer.add(spaceTable);
    	tableContainer.add(finishedGoodsTable);
    	this.add(tableContainer, BorderLayout.SOUTH);
    }
    
    /**
     * Show the Table for the content of the space
     */
    private JPanel buildSpaceTable() {
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
    	return container;
    }

    private JPanel buildFinishedGoodsTable() {
    	JPanel container = new JPanel();
    	JPanel spaceTablePanel = new JPanel();
    	BoxLayout layout = new BoxLayout(spaceTablePanel, BoxLayout.PAGE_AXIS);
    	spaceTablePanel.setLayout(layout);
    	JLabel label = new JLabel("Finished Goods");
    	label.setAlignmentX(CENTER_ALIGNMENT);
    	String[] columns = {"Car ID", "Motor ID", "PID", "..."};
    	Object[][] data = {{1, "test", "PID", "..."}, {2, "test2", "PID", "..."}};
    	JTable table = new JTable(data, columns);
    	JScrollPane scrollPane = new JScrollPane(table);
    	table.setFillsViewportHeight(true);
    	spaceTablePanel.add(label);
    	spaceTablePanel.add(scrollPane);
    	container.add(spaceTablePanel);
    	return container;
    }
    
	private void buildCreationPanel() {
		CreationListener listener = new CreationListener();
    	JPanel container = new JPanel();
    	BoxLayout layout = new BoxLayout(container, BoxLayout.PAGE_AXIS);
    	container.setLayout(layout);
    	JPanel padding = new JPanel();
		
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
        
        JLabel label = new JLabel("Create a worker");
        label.setAlignmentX(CENTER_ALIGNMENT);

        JPanel producerPanel = new JPanel(new GridLayout(3,2));
        producerPanel.add(createBodyFactoryButton);
        producerPanel.add(bodyCountSpinner);
        producerPanel.add(createWheelFactoryButton);
        producerPanel.add(wheelCountSpinner);
        producerPanel.add(createMotorFactoryButton);
        producerPanel.add(motorCountSpinner);
        container.add(label);
        container.add(producerPanel);
        padding.add(container);
        this.add(padding, BorderLayout.CENTER);
	}
    
    class CreationListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            String command = e.getActionCommand();
            if(command.equals("body")) {
            	int value = (Integer) bodyCountSpinner.getValue();
            	FactoryFacade bodyFactory = FactoryFacade.getInstance(ProducerType.BODY, capi, cref);
            	bodyFactory.init(value);
            	bodyFactory.start();
            } else if(command.equals("wheel")) {
            	int value = (Integer) wheelCountSpinner.getValue();
            	FactoryFacade wheelFactory = FactoryFacade.getInstance(ProducerType.WHEEL, capi, cref);
            	wheelFactory.init(value);
            	wheelFactory.start();
            } else if(command.equals("motor")) {
            	int value = (Integer) motorCountSpinner.getValue();
            	FactoryFacade motorFactory = FactoryFacade.getInstance(ProducerType.MOTOR, capi, cref);
            	motorFactory.init(value);
            	motorFactory.start();
            }
            
        }
        
    }
}

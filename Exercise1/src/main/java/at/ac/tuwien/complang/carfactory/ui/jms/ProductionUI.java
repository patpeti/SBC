package at.ac.tuwien.complang.carfactory.ui.jms;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.Session;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;

import org.apache.activemq.ActiveMQConnectionFactory;

import at.ac.tuwien.complang.carfactory.application.enums.ProducerType;
import at.ac.tuwien.complang.carfactory.application.enums.SpaceChangeType;
import at.ac.tuwien.complang.carfactory.application.jms.JmsFactoryFacade;
import at.ac.tuwien.complang.carfactory.application.jms.enums.QueueChangeType;
import at.ac.tuwien.complang.carfactory.domain.Car;
import at.ac.tuwien.complang.carfactory.domain.ICarPart;
import at.ac.tuwien.complang.carfactory.ui.jms.listener.IQueueListener;
import at.ac.tuwien.complang.carfactory.ui.jms.listener.IQueueObserver;
import at.ac.tuwien.complang.carfactory.ui.tableModels.FinishedGoodsTableModel;
import at.ac.tuwien.complang.carfactory.ui.tableModels.SpaceDataTableModel;

public class ProductionUI extends JFrame implements IQueueObserver {
	
	//Static Fields
	private static final long serialVersionUID = -6151830798597607052L;

	//Fields
	private JSpinner bodyCountSpinner, wheelCountSpinner, motorCountSpinner;
	private JPanel tableContainer;

	private IQueueListener listener;
	private JTable spaceTable, finishedGoodsTable;
	private SpaceDataTableModel spaceDataTableModel;
	private FinishedGoodsTableModel finishedGoodsTableModel;

	public ProductionUI(IQueueListener listener) {
		tableContainer = new JPanel(new GridLayout(2, 1));
		this.listener = listener;
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
    	JPanel spaceMargin = new JPanel();
    	spaceMargin.add(spaceTable);
    	tableContainer.add(spaceMargin);
    	tableContainer.add(finishedGoodsTable);
    	this.add(tableContainer, BorderLayout.SOUTH);
    }
    
    /**
     * Show the Table for the content of the space
     */
    private JPanel buildSpaceTable() {
    	JPanel spaceTablePanel = new JPanel();
    	BoxLayout layout = new BoxLayout(spaceTablePanel, BoxLayout.PAGE_AXIS);
    	spaceTablePanel.setLayout(layout);
    	JLabel label = new JLabel("Current Content of the Space");
    	label.setAlignmentX(CENTER_ALIGNMENT);
    	spaceDataTableModel = new SpaceDataTableModel();
    	spaceTable = new JTable(spaceDataTableModel);
    	JScrollPane scrollPane = new JScrollPane(spaceTable);
    	spaceTable.setFillsViewportHeight(true);
    	spaceTablePanel.add(label);
    	spaceTablePanel.add(scrollPane);
    	return spaceTablePanel;
    }

    private JPanel buildFinishedGoodsTable() {
    	JPanel spaceTablePanel = new JPanel();
    	BoxLayout layout = new BoxLayout(spaceTablePanel, BoxLayout.PAGE_AXIS);
    	spaceTablePanel.setLayout(layout);
    	JLabel label = new JLabel("Finished Goods");
    	label.setAlignmentX(CENTER_ALIGNMENT);
    	finishedGoodsTableModel = new FinishedGoodsTableModel();
    	finishedGoodsTable = new JTable(finishedGoodsTableModel);
    	JScrollPane scrollPane = new JScrollPane(finishedGoodsTable);
    	finishedGoodsTable.setFillsViewportHeight(true);
    	spaceTablePanel.add(label);
    	spaceTablePanel.add(scrollPane);
    	return spaceTablePanel;
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
        bodyCountSpinner.setValue(40);
        wheelCountSpinner = new JSpinner();
        wheelCountSpinner.setValue(100);
        motorCountSpinner = new JSpinner();
        motorCountSpinner.setValue(40);
        
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

	public void addPart(ICarPart carPart, SpaceChangeType type) {
		System.out.println("#GUI# : CarPart " + carPart.getId() + " is created");
		spaceDataTableModel.addRow(carPart.getObjectData());
		spaceTable.validate();
	}
	
	public void updatePart(ICarPart part) {
		spaceDataTableModel.updateRow(part.getObjectData());
		spaceTable.validate();
	}

	public void removePart(ICarPart carPart) {
		System.out.println("#GUI# : CarPart " + carPart.getId() + " taken from space");
		spaceDataTableModel.deleteRow(carPart.getObjectData());
		spaceTable.validate();
	}

	public void addCar(Car car) {
		System.out.println("#GUI# : Car " + car.getId() + " added to space");
		finishedGoodsTableModel.addRow(car.getObjectData());
		finishedGoodsTable.validate();
	}

	public void removeCar(Car car) {
		System.out.println("#GUI# : Car " + car.getId() + " removed from space");
		finishedGoodsTableModel.removeRow(car.getObjectData());
		finishedGoodsTable.validate();
	}
	
	public void addOrUpdateCar(Car car) {
		finishedGoodsTableModel.addOrUpdateRow(car.getObjectData());
		finishedGoodsTable.validate();
	}

    class CreationListener implements ActionListener {
		@Override
        public void actionPerformed(ActionEvent e) {
            String command = e.getActionCommand();
            if(command.equals("body")) {
            	int value = (Integer) bodyCountSpinner.getValue();
            	JmsFactoryFacade bodyFactory = JmsFactoryFacade.getInstance(ProducerType.BODY, listener);
            	if(!bodyFactory.isRunning()) {
	            	bodyFactory.init(value);
	            	bodyFactory.start();
            	}

            } else if(command.equals("wheel")) {
            	int value = (Integer) wheelCountSpinner.getValue();
            	JmsFactoryFacade wheelFactory = JmsFactoryFacade.getInstance(ProducerType.WHEEL, listener);
            	if(!wheelFactory.isRunning()) {
            		wheelFactory.init(value);
            		wheelFactory.start();
            	}
            } else if(command.equals("motor")) {
            	int value = (Integer) motorCountSpinner.getValue();
            	JmsFactoryFacade motorFactory = JmsFactoryFacade.getInstance(ProducerType.MOTOR, listener);
            	if(!motorFactory.isRunning()) {
	            	motorFactory.init(value);
	            	motorFactory.start();
            	}
            }
        }
    }

	@Override
	public void onQueueChange(ICarPart carpart, QueueChangeType changeType) {
		System.out.println("#GUI.onQueueChange# : CarPart is created");
		spaceDataTableModel.addRow(carpart.getObjectData());
	}
}

package at.ac.tuwien.complang.carfactory.ui.xvsm;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;

import org.mozartspaces.core.Entry;
import org.mozartspaces.notifications.Notification;
import org.mozartspaces.notifications.NotificationListener;
import org.mozartspaces.notifications.Operation;

import at.ac.tuwien.complang.carfactory.application.IFacade;
import at.ac.tuwien.complang.carfactory.application.IFactory;
import at.ac.tuwien.complang.carfactory.application.enums.ProducerType;
import at.ac.tuwien.complang.carfactory.domain.Car;
import at.ac.tuwien.complang.carfactory.domain.ICarPart;
import at.ac.tuwien.complang.carfactory.ui.IFactoryData;
import at.ac.tuwien.complang.carfactory.ui.tableModels.FinishedGoodsTableModel;
import at.ac.tuwien.complang.carfactory.ui.tableModels.SpaceDataTableModel;

public class ProductionUI extends JFrame implements IFactoryData, NotificationListener {

	//Static Fields
	private static final long serialVersionUID = -6151830798597607052L;

	//Fields
	private JSpinner bodyCountSpinner, wheelCountSpinner, motorCountSpinner;
	private JPanel tableContainer;
	
	private IFacade factoryFacade;
	private JTable spaceTable, finishedGoodsTable;
	private SpaceDataTableModel spaceDataTableModel;
	private FinishedGoodsTableModel finishedGoodsTableModel;

	public ProductionUI(IFacade factoryFacade) {
		this.factoryFacade = factoryFacade;
		tableContainer = new JPanel(new GridLayout(2, 1));
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
    	JLabel label = new JLabel("(Semi-)Finished Goods");
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


	@Override
	public void addPart(ICarPart carPart) {
		System.out.println("#GUI# : CarPart " + carPart.getId() + " is created");
		spaceDataTableModel.addRow(carPart.getObjectData());
	}
	
	@Override
	public void removePart(ICarPart carPart) {
		System.out.println("#GUI# : CarPart " + carPart.getId() + " taken from space");
		spaceDataTableModel.deleteRow(carPart.getObjectData());
	}

	@Override
	public void updatePart(ICarPart carPart) {
		spaceDataTableModel.updateRow(carPart.getObjectData());
	}

	@Override
	public void addCar(Car car) {
		System.out.println("#GUI# : Car " + car.getId() + " added to space");
		finishedGoodsTableModel.addRow(car.getObjectData());
	}
	
	@Override
	public void removeCar(Car car) {
		System.out.println("#GUI# : Car " + car.getId() + " removed from space");
		finishedGoodsTableModel.removeRow(car.getObjectData());
	}
	
	@Override
	public void updateCar(Car car) {
		finishedGoodsTableModel.updateRow(car.getObjectData());
	}
	
	@Override
	public void addOrUpdateCar(Car car) {
		finishedGoodsTableModel.addOrUpdateRow(car.getObjectData());
	}

	class CreationListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			String command = e.getActionCommand();
			if(command.equals("body")) {
				int value = (Integer) bodyCountSpinner.getValue();
				IFactory bodyFactory = factoryFacade.getInstance(ProducerType.BODY);
				if(!bodyFactory.isRunning()) {
					bodyFactory.init(value);
					bodyFactory.start();
				}
			} else if(command.equals("wheel")) {
				int value = (Integer) wheelCountSpinner.getValue();
				IFactory wheelFactory = factoryFacade.getInstance(ProducerType.WHEEL);
				if(!wheelFactory.isRunning()) {
					wheelFactory.init(value);
					wheelFactory.start();
				}
			} else if(command.equals("motor")) {
				int value = (Integer) motorCountSpinner.getValue();
				IFactory motorFactory = factoryFacade.getInstance(ProducerType.MOTOR);
				if(!motorFactory.isRunning()) {
					motorFactory.init(value);
					motorFactory.start();
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void entryOperationFinished(
		Notification source,
		Operation operation,
		List<? extends Serializable> entries)
	{
		System.out.println("[XVSM Notification: " + operation.name() + "]");
		if(operation.name().equals("WRITE")) {
			for(Entry entry : (List<Entry>) entries) {
				if (entry.getValue() instanceof Car) {
					System.out.println("[GUI_Notification] New Car written");
					Car car = (Car) entry.getValue();
					addOrUpdateCar(car);
				} else if (entry.getValue() instanceof ICarPart) { //its not a car but still a carpart
					ICarPart part = (ICarPart) entry.getValue();
					spaceDataTableModel.addRow(part.getObjectData());
				}
			}
		} else if(operation.name().equals("TAKE")) {
			for(ICarPart part: (List<ICarPart>) entries) {
				if(!(part instanceof Car)) {
					removePart(part);
				}
			}
		}
	}
}
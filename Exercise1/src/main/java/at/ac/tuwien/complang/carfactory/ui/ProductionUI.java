package at.ac.tuwien.complang.carfactory.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Observable;
import java.util.Observer;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;

import at.ac.tuwien.complang.carfactory.application.IFacade;
import at.ac.tuwien.complang.carfactory.application.IFactory;
import at.ac.tuwien.complang.carfactory.application.enums.ProducerType;
import at.ac.tuwien.complang.carfactory.domain.Car;
import at.ac.tuwien.complang.carfactory.domain.ICarPart;
import at.ac.tuwien.complang.carfactory.ui.panels.StatusLight;
import at.ac.tuwien.complang.carfactory.ui.tableModels.FinishedGoodsTableModel;
import at.ac.tuwien.complang.carfactory.ui.tableModels.SpaceDataTableModel;
import javax.swing.SpinnerNumberModel;

public class ProductionUI extends JFrame implements IFactoryData, Observer {

	//Static Fields
	private static final long serialVersionUID = -6151830798597607052L;

	//Fields
	private JSpinner bodyCountSpinner, bodyErrorRateSpinner,
		wheelCountSpinner, wheelErrorRateSpinner,
		motorCountSpinner, motorErrorRateSpinner;
	private JPanel tableContainer;
	
	private IFacade factoryFacade;
	private JTable partsTable, finishedGoodsTable;
	private SpaceDataTableModel partsDataTableModel;
	private FinishedGoodsTableModel finishedGoodsTableModel;
	private StatusLight bodyFactoryStatus,wheelFactoryStatus,motorFactoryStatus;

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
    	JPanel partsTable = buildPartsTable();
    	JPanel finishedGoodsTable = buildFinishedGoodsTable();
    	tableContainer.add(partsTable);
    	tableContainer.add(finishedGoodsTable);
    	getContentPane().add(tableContainer, BorderLayout.CENTER);
    }
    
    /**
     * Show the Table for the content of the space
     */
    private JPanel buildPartsTable() {
    	JPanel spaceTablePanel = new JPanel();
    	JLabel label = new JLabel("Current Content of the Space");
    	label.setAlignmentX(CENTER_ALIGNMENT);
    	partsDataTableModel = new SpaceDataTableModel();
    	partsTable = new JTable(partsDataTableModel);
    	partsTable.setAutoResizeMode(HEIGHT);
    	JScrollPane scrollPane = new JScrollPane(partsTable);
    	partsTable.setFillsViewportHeight(true);
    	spaceTablePanel.setLayout(new BoxLayout(spaceTablePanel, BoxLayout.Y_AXIS));
    	spaceTablePanel.add(label);
    	spaceTablePanel.add(scrollPane);
    	return spaceTablePanel;
    }

    private JPanel buildFinishedGoodsTable() {
    	JPanel finishedGoodsTablePanel = new JPanel();
    	JLabel label = new JLabel("(Semi-)Finished Goods");
    	label.setAlignmentX(CENTER_ALIGNMENT);
    	finishedGoodsTableModel = new FinishedGoodsTableModel();
    	finishedGoodsTable = new JTable(finishedGoodsTableModel);
    	finishedGoodsTable.setAutoResizeMode(HEIGHT);
    	JScrollPane scrollPane = new JScrollPane(finishedGoodsTable);
    	finishedGoodsTable.setFillsViewportHeight(true);
    	finishedGoodsTablePanel.setLayout(new BoxLayout(finishedGoodsTablePanel, BoxLayout.Y_AXIS));
    	finishedGoodsTablePanel.add(label);
    	finishedGoodsTablePanel.add(scrollPane);
    	return finishedGoodsTablePanel;
    }

	private void buildCreationPanel() {
		CreationListener listener = new CreationListener();
    	JPanel container = new JPanel();
    	BoxLayout layout = new BoxLayout(container, BoxLayout.PAGE_AXIS);
    	container.setLayout(layout);
    	JPanel padding = new JPanel();
        
        JLabel label = new JLabel("Create a worker");
        label.setAlignmentX(CENTER_ALIGNMENT);

        JPanel producerPanel = new JPanel();
        GridBagLayout gbl_producerPanel = new GridBagLayout();
        gbl_producerPanel.columnWidths = new int[]{39, 216, 100, 100, 0};
        gbl_producerPanel.rowHeights = new int[]{34, 34, 34, 0};
        gbl_producerPanel.columnWeights = new double[]{1.0, 1.0, 1.0, 0.0, Double.MIN_VALUE};
        gbl_producerPanel.rowWeights = new double[]{1.0, 1.0, 1.0, Double.MIN_VALUE};
        producerPanel.setLayout(gbl_producerPanel);
        JButton createMotorFactoryButton = new JButton("Create Motor Factory");
        createMotorFactoryButton.setActionCommand("motor");
        createMotorFactoryButton.addActionListener(listener);
        
        JButton createBodyFactoryButton = new JButton("Create Body Factory");
        createBodyFactoryButton.setActionCommand("body");
        createBodyFactoryButton.addActionListener(listener);
        
        bodyFactoryStatus = new StatusLight();
        GridBagConstraints gbc_bodyFactoryStatus = new GridBagConstraints();
        gbc_bodyFactoryStatus.insets = new Insets(5, 5, 5, 5);
        gbc_bodyFactoryStatus.fill = GridBagConstraints.BOTH;
        gbc_bodyFactoryStatus.gridx = 0;
        gbc_bodyFactoryStatus.gridy = 0;
        producerPanel.add(bodyFactoryStatus, gbc_bodyFactoryStatus);
        FlowLayout fl_bodyFactoryStatus = new FlowLayout(FlowLayout.CENTER, 0, 0);
        bodyFactoryStatus.setLayout(fl_bodyFactoryStatus);
        GridBagConstraints gbc_createBodyFactoryButton = new GridBagConstraints();
        gbc_createBodyFactoryButton.fill = GridBagConstraints.BOTH;
        gbc_createBodyFactoryButton.insets = new Insets(5, 5, 5, 5);
        gbc_createBodyFactoryButton.gridx = 1;
        gbc_createBodyFactoryButton.gridy = 0;
        producerPanel.add(createBodyFactoryButton, gbc_createBodyFactoryButton);
        
        bodyCountSpinner = new JSpinner();
        bodyCountSpinner.setValue(40);
        GridBagConstraints gbc_bodyCountSpinner = new GridBagConstraints();
        gbc_bodyCountSpinner.fill = GridBagConstraints.BOTH;
        gbc_bodyCountSpinner.insets = new Insets(5, 5, 5, 5);
        gbc_bodyCountSpinner.gridx = 2;
        gbc_bodyCountSpinner.gridy = 0;
        producerPanel.add(bodyCountSpinner, gbc_bodyCountSpinner);
        JButton createWheelFactoryButton = new JButton("Create Wheel Factory");
        createWheelFactoryButton.setActionCommand("wheel");
        createWheelFactoryButton.addActionListener(listener);
        
        bodyErrorRateSpinner = new JSpinner();
        bodyErrorRateSpinner.setModel(new SpinnerNumberModel(new Double(0), new Double(0), new Double(1), new Double(0.1)));
        GridBagConstraints gbc_bodyErrorRateSpinner = new GridBagConstraints();
        gbc_bodyErrorRateSpinner.fill = GridBagConstraints.BOTH;
        gbc_bodyErrorRateSpinner.insets = new Insets(5, 5, 5, 5);
        gbc_bodyErrorRateSpinner.gridx = 3;
        gbc_bodyErrorRateSpinner.gridy = 0;
        producerPanel.add(bodyErrorRateSpinner, gbc_bodyErrorRateSpinner);
        
        wheelFactoryStatus = new StatusLight();
        GridBagConstraints gbc_wheelFactoryStatus = new GridBagConstraints();
        gbc_wheelFactoryStatus.insets = new Insets(5, 5, 5, 5);
        gbc_wheelFactoryStatus.fill = GridBagConstraints.BOTH;
        gbc_wheelFactoryStatus.gridx = 0;
        gbc_wheelFactoryStatus.gridy = 1;
        producerPanel.add(wheelFactoryStatus, gbc_wheelFactoryStatus);
        wheelFactoryStatus.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
        GridBagConstraints gbc_createWheelFactoryButton = new GridBagConstraints();
        gbc_createWheelFactoryButton.fill = GridBagConstraints.BOTH;
        gbc_createWheelFactoryButton.insets = new Insets(5, 5, 5, 5);
        gbc_createWheelFactoryButton.gridx = 1;
        gbc_createWheelFactoryButton.gridy = 1;
        producerPanel.add(createWheelFactoryButton, gbc_createWheelFactoryButton);
        wheelCountSpinner = new JSpinner();
        wheelCountSpinner.setValue(100);
        GridBagConstraints gbc_wheelCountSpinner = new GridBagConstraints();
        gbc_wheelCountSpinner.fill = GridBagConstraints.BOTH;
        gbc_wheelCountSpinner.insets = new Insets(5, 5, 5, 5);
        gbc_wheelCountSpinner.gridx = 2;
        gbc_wheelCountSpinner.gridy = 1;
        producerPanel.add(wheelCountSpinner, gbc_wheelCountSpinner);
        
        wheelErrorRateSpinner = new JSpinner();
        wheelErrorRateSpinner.setModel(new SpinnerNumberModel(new Double(0), new Double(0), new Double(1), new Double(0.1)));
        GridBagConstraints gbc_wheelErrorRateSpinner = new GridBagConstraints();
        gbc_wheelErrorRateSpinner.fill = GridBagConstraints.BOTH;
        gbc_wheelErrorRateSpinner.insets = new Insets(5, 5, 5, 5);
        gbc_wheelErrorRateSpinner.gridx = 3;
        gbc_wheelErrorRateSpinner.gridy = 1;
        producerPanel.add(wheelErrorRateSpinner, gbc_wheelErrorRateSpinner);
        
        motorFactoryStatus = new StatusLight();
        GridBagConstraints gbc_motorFactoryStatus = new GridBagConstraints();
        gbc_motorFactoryStatus.insets = new Insets(5, 5, 5, 5);
        gbc_motorFactoryStatus.fill = GridBagConstraints.BOTH;
        gbc_motorFactoryStatus.gridx = 0;
        gbc_motorFactoryStatus.gridy = 2;
        producerPanel.add(motorFactoryStatus, gbc_motorFactoryStatus);
        motorFactoryStatus.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
        GridBagConstraints gbc_createMotorFactoryButton = new GridBagConstraints();
        gbc_createMotorFactoryButton.fill = GridBagConstraints.BOTH;
        gbc_createMotorFactoryButton.insets = new Insets(5, 5, 5, 5);
        gbc_createMotorFactoryButton.gridx = 1;
        gbc_createMotorFactoryButton.gridy = 2;
        producerPanel.add(createMotorFactoryButton, gbc_createMotorFactoryButton);
        motorCountSpinner = new JSpinner();
        motorCountSpinner.setValue(40);
        GridBagConstraints gbc_motorCountSpinner = new GridBagConstraints();
        gbc_motorCountSpinner.insets = new Insets(5, 5, 5, 5);
        gbc_motorCountSpinner.fill = GridBagConstraints.BOTH;
        gbc_motorCountSpinner.gridx = 2;
        gbc_motorCountSpinner.gridy = 2;
        producerPanel.add(motorCountSpinner, gbc_motorCountSpinner);
        container.add(label);
        container.add(producerPanel);
        
        motorErrorRateSpinner = new JSpinner();
        motorErrorRateSpinner.setModel(new SpinnerNumberModel(new Double(0), new Double(0), new Double(1), new Double(0.1)));
        GridBagConstraints gbc_motorErrorRateSpinner = new GridBagConstraints();
        gbc_motorErrorRateSpinner.insets = new Insets(5, 5, 5, 5);
        gbc_motorErrorRateSpinner.fill = GridBagConstraints.BOTH;
        gbc_motorErrorRateSpinner.gridx = 3;
        gbc_motorErrorRateSpinner.gridy = 2;
        producerPanel.add(motorErrorRateSpinner, gbc_motorErrorRateSpinner);
        padding.add(container);
        getContentPane().add(padding, BorderLayout.NORTH);
	}


	@Override
	public boolean addPart(ICarPart carPart) {
		System.out.println("#GUI# : CarPart " + carPart.getId() + " is created");
		return partsDataTableModel.addRow(carPart.getObjectData());
	}
	
	@Override
	public boolean updatePart(ICarPart carPart) {
		return partsDataTableModel.updateRow(carPart.getObjectData());
	}
	
	@Override
	public boolean removePart(ICarPart carPart) {
		System.out.println("#GUI# : CarPart " + carPart.getId() + " removed from warehouse");
		return partsDataTableModel.deleteRow(carPart.getObjectData());
	}

	@Override
	public boolean addCar(Car car) {
		System.out.println("#GUI# : Car " + car.getId() + " added to warehouse");
		return finishedGoodsTableModel.addRow(car.getObjectData());
	}
	
	@Override
	public boolean removeCar(Car car) {
		System.out.println("#GUI# : Car " + car.getId() + " removed from warehouse");
		return finishedGoodsTableModel.removeRow(car.getObjectData());
	}
	
	@Override
	public boolean updateCar(Car car) {
		return finishedGoodsTableModel.updateRow(car.getObjectData());
	}

	class CreationListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			String command = e.getActionCommand();
			if(command.equals("body")) {
				int value = (Integer) bodyCountSpinner.getValue();
				double errorRate = (Double) bodyErrorRateSpinner.getValue();
				IFactory bodyFactory = factoryFacade.getInstance(ProducerType.BODY);
				if(!bodyFactory.isRunning()) {
					bodyFactory.init(value, errorRate);
					bodyFactory.start();
					bodyFactory.addObserver(ProductionUI.this);
					bodyFactoryStatus.setActive();
				}
			} else if(command.equals("wheel")) {
				int amount = (Integer) wheelCountSpinner.getValue();
				double errorRate = (Double) wheelErrorRateSpinner.getValue();
				IFactory wheelFactory = factoryFacade.getInstance(ProducerType.WHEEL);
				if(!wheelFactory.isRunning()) {
					wheelFactory.init(amount, errorRate);
					wheelFactory.start();
					wheelFactory.addObserver(ProductionUI.this);
					wheelFactoryStatus.setActive();
				}
			} else if(command.equals("motor")) {
				int amount = (Integer) motorCountSpinner.getValue();
				double errorRate = (Double) motorErrorRateSpinner.getValue();
				IFactory motorFactory = factoryFacade.getInstance(ProducerType.MOTOR);
				if(!motorFactory.isRunning()) {
					motorFactory.init(amount, errorRate);
					motorFactory.start();
					motorFactory.addObserver(ProductionUI.this);
					motorFactoryStatus.setActive();
				}
			}
		}
	}

	@Override
	public void update(Observable o, Object string) {
		if(string.equals("BODY")) {
			bodyFactoryStatus.setInactive();
		} else if(string.equals("MOTOR")) {
			motorFactoryStatus.setInactive();
		} else if(string.equals("WHEEL")) {
			wheelFactoryStatus.setInactive();
		}
	}
}

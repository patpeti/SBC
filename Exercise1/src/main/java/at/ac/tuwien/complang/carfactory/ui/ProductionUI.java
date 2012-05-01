package at.ac.tuwien.complang.carfactory.ui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;

import org.mozartspaces.core.Capi;
import org.mozartspaces.core.ContainerReference;
import org.mozartspaces.core.Entry;
import org.mozartspaces.core.MzsCoreException;
import org.mozartspaces.notifications.Notification;
import org.mozartspaces.notifications.NotificationListener;
import org.mozartspaces.notifications.NotificationManager;
import org.mozartspaces.notifications.Operation;

import at.ac.tuwien.complang.carfactory.application.FactoryFacade;
import at.ac.tuwien.complang.carfactory.application.enums.ProducerType;
import at.ac.tuwien.complang.carfactory.application.enums.SpaceChangeType;
import at.ac.tuwien.complang.carfactory.domain.Car;
import at.ac.tuwien.complang.carfactory.domain.ICarPart;
import at.ac.tuwien.complang.carfactory.ui.tableModels.FinishedGoodsTableModel;
import at.ac.tuwien.complang.carfactory.ui.tableModels.SpaceDataTableModel;





public class ProductionUI extends JFrame implements ISpaceObserver, NotificationListener{
	private static final long serialVersionUID = -6151830798597607052L;
	private static final String[] FINISHED_GOODS_COLUMNS = {"Car ID", "PID",
		"Body ID", "Body PID",
		"Motor ID", "Motor PID",
		"WHEEL 1 ID",
		"WHEEL 1 PID",
		"WHEEL 2 ID",
		"WHEEL 2 PID",
		"WHEEL 3 ID",
		"WHEEL 3 PID",
		"WHEEL 4 ID",
		"WHEEL 4 PID"};
	private static final String[] SPACE_CONTENT_COLUMNS = {"ID", "PartName", "PID"};
	
	//Fields
	private JSpinner bodyCountSpinner, wheelCountSpinner, motorCountSpinner;
	private JPanel tableContainer;
	private Capi capi;
	private ContainerReference cref;
	private ISpaceListener listener;
	private JTable spaceTable, finishedGoodsTable;
	private SpaceDataTableModel spaceDataTableModel;
	private FinishedGoodsTableModel finishedGoodsTableModel;
	private NotificationManager notifMgr;

	public ProductionUI(Capi capi, ContainerReference cref, ISpaceListener listener, NotificationManager notifMgr) {
		this.capi = capi;
		this.cref = cref;
		this.notifMgr = notifMgr;
		Set<Operation> operations = new HashSet<Operation>();
	    operations.add(Operation.DELETE);
	    operations.add(Operation.TAKE);
	    operations.add(Operation.WRITE);

	    try {
			notifMgr.createNotification(cref, this, operations, null, null);
		} catch (MzsCoreException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		tableContainer = new JPanel(new GridLayout(1, 2));
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
    	tableContainer.add(spaceTable);
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
    	Object[][] data = {{1, "test", "PID"}, {2, "test2", "PID"}};
    	spaceDataTableModel = new SpaceDataTableModel(data);
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
    	Object[][] data = {{1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1}};
    	finishedGoodsTableModel = new FinishedGoodsTableModel(data);
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
	
	 
	
	public void onSpaceChange (ICarPart carPart, SpaceChangeType type){
		System.out.println("#GUI# : CarPart is created");
		spaceDataTableModel.addRow(carPart.getObjectData());
	}
	

    
    class CreationListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            String command = e.getActionCommand();
            if(command.equals("body")) {
            	int value = (Integer) bodyCountSpinner.getValue();
            	FactoryFacade bodyFactory = FactoryFacade.getInstance(ProducerType.BODY, capi, cref, listener);
            	if(!bodyFactory.isRunning()) {
	            	bodyFactory.init(value);
	            	bodyFactory.start();
            	}

            } else if(command.equals("wheel")) {
            	int value = (Integer) wheelCountSpinner.getValue();
            	FactoryFacade wheelFactory = FactoryFacade.getInstance(ProducerType.WHEEL, capi, cref, listener);
            	if(!wheelFactory.isRunning()) {
            		wheelFactory.init(value);
            		wheelFactory.start();
            	}
            } else if(command.equals("motor")) {
            	int value = (Integer) motorCountSpinner.getValue();
            	FactoryFacade motorFactory = FactoryFacade.getInstance(ProducerType.MOTOR, capi, cref, listener);
            	if(!motorFactory.isRunning()) {
	            	motorFactory.init(value);
	            	motorFactory.start();
            	}

            }
            
        }
        
    }

	@Override
	public void entryOperationFinished(Notification source,
			Operation operation, List<? extends Serializable> entries) {
		System.out.println("[GUI_Notification]#######################################################");
		System.out.println("opname: "+ operation.name());
		if(operation.name().equals("WRITE")){
			
			for(Entry e : (List<Entry>) entries){
				if (e.getValue() instanceof Car){
					System.out.println("[GUI_Notification] New Car written");
					Car c = (Car) e.getValue();
					if(c.isComplete()){
						//TODO show this car in the gui table.
						finishedGoodsTableModel.addRow(c.getDetails());
					}
				}
			}
		}
		
	}

	


}

package at.ac.tuwien.complang.carfactory.application.workers.xvsm;

import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.mozartspaces.capi3.AnyCoordinator;
import org.mozartspaces.capi3.CoordinationData;
import org.mozartspaces.capi3.Coordinator;
import org.mozartspaces.capi3.FifoCoordinator;
import org.mozartspaces.capi3.KeyCoordinator;
import org.mozartspaces.capi3.LabelCoordinator;
import org.mozartspaces.capi3.Property;
import org.mozartspaces.capi3.Query;
import org.mozartspaces.capi3.QueryCoordinator;
import org.mozartspaces.capi3.Selector;
import org.mozartspaces.core.Capi;
import org.mozartspaces.core.CapiUtil;
import org.mozartspaces.core.ContainerReference;
import org.mozartspaces.core.DefaultMzsCore;
import org.mozartspaces.core.Entry;
import org.mozartspaces.core.MzsCore;
import org.mozartspaces.core.MzsCoreException;
import org.mozartspaces.core.MzsTimeoutException;
import org.mozartspaces.core.TransactionException;
import org.mozartspaces.core.TransactionReference;
import org.mozartspaces.notifications.Notification;
import org.mozartspaces.notifications.NotificationListener;
import org.mozartspaces.notifications.NotificationManager;
import org.mozartspaces.notifications.Operation;

import at.ac.tuwien.complang.carfactory.application.enums.CarPartType;
import at.ac.tuwien.complang.carfactory.domain.Car;
import at.ac.tuwien.complang.carfactory.domain.TesterType;
import at.ac.tuwien.complang.carfactory.ui.constants.SpaceConstants;
import at.ac.tuwien.complang.carfactory.ui.constants.SpaceLabels;
import at.ac.tuwien.complang.carfactory.ui.constants.SpaceTimeout;


public class Tester {
	
	private Capi capi;
	private ContainerReference container, signalContainer;
	//private ContainerReference defectContainer;
	private TransactionReference tx;
	private TesterType type;
	private long tid;
	private boolean running = false;
	private boolean waitForSignal = false;

	public Tester(TesterType type, long id, boolean waitForSignal) {
		initSpace();
		this.type = type;
		this.tid = id;
		this.waitForSignal = waitForSignal;
	}
	
	public void start() {
		if(waitForSignal) {
			waitForStartSignal();
		} else {
			running = true;
		}
		if(type == TesterType.COMPLETETESTER) {
			//do completetest loop
			while(running) {
				doCompletenessTest();
			}
		}else if(type == TesterType.DEFECTTESTER) {
			while(running){
				doDefectTest();
			}
		}else{
			System.err.println("This type of tester is not implemented");
			System.exit(1);
		}
		System.out.println("[Tester] Received a stop signal and is terminating...");
	}

	private void doDefectTest() {
		try {
			tx = capi.createTransaction(SpaceTimeout.INFINITE, new URI(SpaceConstants.CONTAINER_URI));
		} catch (MzsCoreException e1) {
			e1.printStackTrace();
		} catch (URISyntaxException e1) {
			e1.printStackTrace();
		}
		List<Selector> selectors = new ArrayList<Selector>();
		
		//add queryCoordinator so that we get cars that are not tested yet
		Query query = null;
		Property prop = null;
		prop = Property.forName("isDefectTested_pid");
		query = new Query().filter(prop.equalTo(new Long(-1)));
		selectors.add(QueryCoordinator.newSelector(query));
		selectors.add(FifoCoordinator.newSelector(1));
		List<Car> cars = new ArrayList<Car>();
		try {
			cars = capi.take(container, selectors, SpaceTimeout.INFINITE, tx);
		} catch (MzsTimeoutException e) {
			return;
		} catch (TransactionException e) {
			try {
				capi.rollbackTransaction(tx);
			} catch (MzsCoreException e1) {
				e1.printStackTrace();
			}
		} catch (MzsCoreException e) {
			e.printStackTrace();
		}
		if(cars.size() == 0) return;
		Car car = cars.get(0);
		//TODO semantic WTF
		if(testDefect(car)){ 
			car.setDefect(tid, false);
		}else{
			car.setDefect(tid, true);
		}
		writeCarIntoSpace(car);
	}

	private void doCompletenessTest() {
		try {
			tx = capi.createTransaction(SpaceTimeout.INFINITE, new URI(SpaceConstants.CONTAINER_URI));
		} catch (MzsCoreException e1) {
			e1.printStackTrace();
		} catch (URISyntaxException e1) {
			e1.printStackTrace();
		}
		List<Selector> selectors = new ArrayList<Selector>();
		
		
		// add queryCoordinator so that we get cars that are not tested yet
		
		Query query = null;
		Property prop = null;
		prop = Property.forName("isComplete_pid");
		query = new Query().filter(prop.equalTo(new Long(-1)));
		selectors.add(QueryCoordinator.newSelector(query));
		selectors.add(FifoCoordinator.newSelector(1));
		
		List<Car> cars = new ArrayList<Car>();
		try {
			cars = capi.take(container, selectors, SpaceTimeout.INFINITE, tx);
		} catch (MzsTimeoutException e) {
			return;
		} catch (TransactionException e) {
			try {
				capi.rollbackTransaction(tx);
			} catch (MzsCoreException e1) {
				e1.printStackTrace();
			}
		} catch (MzsCoreException e) {
			e.printStackTrace();
		}
		if(cars.size() == 0) return;
		Car car = cars.get(0);
		if(testCompleteness(car)){
			car.setComplete(tid, true);
		}else{
			car.setComplete(tid, false);
		}
		writeCarIntoSpace(car);
		
	}
	
	private boolean testCompleteness(Car c) {
		//all parts are set and body is painted
		boolean testOk = true;
		if(c.getBody() == null) testOk = false;
		else if(c.getBody().getColor() == null && c.getBody().getPainterId() != -1) testOk = false;
		if(c.getMotor() == null) testOk = false;
		if(c.getWheels()[0] == null) testOk = false;
		if(c.getWheels()[1] == null) testOk = false;
		if(c.getWheels()[2] == null) testOk = false;
		if(c.getWheels()[3] == null) testOk = false;
		
		return testOk;
	}

	private boolean testDefect(Car car) {
		//none of the parts is defected
		boolean testOk = true;
		if(car.getBody().isDefect()) testOk = false;
		if(car.getMotor().isDefect()) testOk = false;
		if(car.getWheels()[0].isDefect()) testOk = false;
		if(car.getWheels()[1].isDefect()) testOk = false;
		if(car.getWheels()[2].isDefect()) testOk = false;
		if(car.getWheels()[3].isDefect()) testOk = false;
		
		return testOk;
	}
	
	private void writeCarIntoSpace(Car car) {

		//WRITE CAR ALWAYS IN THE CARCONTAINER AFTER TEST... DEFECTCONTAINER IS USED BY SUPERVISOR
		try {
			List<CoordinationData> cordinator = new ArrayList<CoordinationData>();
			String label =  CarPartType.CAR.toString();
			if(car.getBody().getColor() != null) label = SpaceLabels.PAINTEDCAR;
			cordinator.add(LabelCoordinator.newCoordinationData(label));
			cordinator.add(KeyCoordinator.newCoordinationData(""+car.getId()));
			cordinator.add(FifoCoordinator.newCoordinationData());
			capi.write(new Entry(car,cordinator),container,SpaceTimeout.TENSEC, tx );
			capi.commitTransaction(tx);
			System.out.println("[Tester]*Car " + car.getId() + " tested: "+this.type+" Defect: " + car.isDefect());
		} catch (MzsCoreException e) {
			e.printStackTrace();
		}

	}

	private void initSpace(){
		MzsCore core = DefaultMzsCore.newInstance(0);
		this.capi = new Capi(core);
		this.container = null;
		NotificationManager notificationManager = new NotificationManager(core);
		try {
			List<Coordinator> coords = new ArrayList<Coordinator>();
			coords.add(new LabelCoordinator());
			coords.add(new KeyCoordinator());
			coords.add(new FifoCoordinator());	
			coords.add(new QueryCoordinator());
			List<Coordinator> signalCoordinators = new ArrayList<Coordinator>();
			signalCoordinators.add(new AnyCoordinator());
			signalCoordinators.add(new FifoCoordinator());
			try {
				this.container = CapiUtil.lookupOrCreateContainer(SpaceConstants.CARCONTAINER_NAME, new URI(SpaceConstants.CONTAINER_URI), coords, null, capi);
				this.signalContainer = CapiUtil.lookupOrCreateContainer(SpaceConstants.SIGNALCONTAINER_NAME, new URI(SpaceConstants.CONTAINER_URI), signalCoordinators, null, capi);
				
			} catch (URISyntaxException e) {
				System.out.println("Error: Invalid container name");
				e.printStackTrace();
			}
			
			Set<Operation> operations = new HashSet<Operation>();
			operations.add(Operation.WRITE);
			
			notificationManager.createNotification(signalContainer, new SignalContainerListener(), operations, null, null);
			
			
		} catch (MzsCoreException e) {
			System.out.println("Error: Could not initialize Space");
			e.printStackTrace();
		} catch (Exception e){
			e.printStackTrace();
		}
		System.out.println("[SpaceUtil]: Space initiated ");
		
	}
	
	private void waitForStartSignal() {
		List<Selector> selectors = new ArrayList<Selector>();
		selectors.add(FifoCoordinator.newSelector(1));
		selectors.add(LabelCoordinator.newSelector("START"));
		List<String> entries;
		try {
			entries = capi.read(signalContainer, selectors, SpaceTimeout.INFINITE, null);
			String signal = (String) entries.get(0);
			if(signal.equalsIgnoreCase("START")) {
				running = true;
			}
		} catch (MzsCoreException e) {
			e.printStackTrace();
		}
	}

	class SignalContainerListener implements NotificationListener {
		@Override
		public void entryOperationFinished(Notification source,
			Operation operation, List<? extends Serializable> entries)
		{
			System.out.println("IM STOPPING");
			if(operation.name().equals("WRITE")) {
				for(Entry entry : (List<Entry>) entries) {
					String signal = (String) entry.getValue();
					if(signal.equalsIgnoreCase("STOP")) {
						Tester.this.running = false;
					}
				}
			}
		}
	}
}

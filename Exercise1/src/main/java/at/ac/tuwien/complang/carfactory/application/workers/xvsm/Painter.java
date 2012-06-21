package at.ac.tuwien.complang.carfactory.application.workers.xvsm;

import java.awt.Color;
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.tools.ant.taskdefs.Sleep;
import org.mozartspaces.capi3.AnyCoordinator;
import org.mozartspaces.capi3.CoordinationData;
import org.mozartspaces.capi3.Coordinator;
import org.mozartspaces.capi3.CountNotMetException;
import org.mozartspaces.capi3.FifoCoordinator;
import org.mozartspaces.capi3.KeyCoordinator;
import org.mozartspaces.capi3.LabelCoordinator;
import org.mozartspaces.capi3.Matchmaker;
import org.mozartspaces.capi3.Matchmakers;
import org.mozartspaces.capi3.Property;
import org.mozartspaces.capi3.Query;
import org.mozartspaces.capi3.QueryCoordinator;
import org.mozartspaces.capi3.Selector;
import org.mozartspaces.core.Capi;
import org.mozartspaces.core.CapiUtil;
import org.mozartspaces.core.ContainerReference;
import org.mozartspaces.core.DefaultMzsCore;
import org.mozartspaces.core.Entry;
import org.mozartspaces.core.MzsConstants;
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
import at.ac.tuwien.complang.carfactory.application.workers.xvsm.Tester.SignalContainerListener;
import at.ac.tuwien.complang.carfactory.domain.Body;
import at.ac.tuwien.complang.carfactory.domain.Car;
import at.ac.tuwien.complang.carfactory.domain.ICarPart;
import at.ac.tuwien.complang.carfactory.domain.Task;
import at.ac.tuwien.complang.carfactory.ui.constants.SpaceConstants;
import at.ac.tuwien.complang.carfactory.ui.constants.SpaceLabels;
import at.ac.tuwien.complang.carfactory.ui.constants.SpaceTimeout;

public class Painter {

	/**
	 * Workflow:
	 * 1. Connect to the Mozard space
	 * 2. Load a Body (which is not yet painted) or an
	 *    assembled car (which is not yet painted)
	 * 3. Paint the Body or the Body associated with the car object
	 * 4. Save the painted part back into the space 
	 */
	
	/** Half the time is used to take a part and the other half is used to paint it.
	 * We use this to relax the update intervals of the UI, so that there is no
	 * flickering, which happens, when a part is taken and written back immediately. */
	public static final long TIME_TO_PAINT = 1400L; //time in milliseconds
	private boolean running = false;
	private long pid = 0;
	private Color color; //the color which this painter uses to paint an object. It is set on creation of the painter.

	private Capi capi;
	private TransactionReference tx;
	private ContainerReference carContainer, bodyContainer, taskContainer, signalContainer;
	
	public Painter(long id, Color color) {
		super();
		pid = id;
		this.color = color;
		initSpace();
	}
	
	public void start() {
		waitForStartSignal();
		while(running){
			doPaint();
		}
		System.out.println("[Painter] Received a stop signal and is terminating...");
	}

	private void doPaint() {
		try {
			Thread.sleep(TIME_TO_PAINT/2);
		} catch (InterruptedException e) { }
		try {
			tx = capi.createTransaction(SpaceTimeout.TENSEC, new URI(SpaceConstants.CONTAINER_URI));
			//1. Take the Task object from the space
			Task task = getTask();
			if(task == null) {
				//normal paint loop
				//paint a car which has taskId -1
				//or paint a body
				doNormalPaintLoop();
			} else {
				System.out.println("[Painter] Task ID: " + task.getId());
				//select car from space where taskId = Task.id
				//if it is not painted paint it
				doTaskPaintLoop(task);
			}
		} catch (MzsTimeoutException e) { //do nothing, rollback is automatic
		} catch (MzsCoreException e1) {
			try {
				capi.rollbackTransaction(tx);
			} catch (MzsCoreException e) { }
		} catch (URISyntaxException e1) {
			e1.printStackTrace();
		}
	}

	private void doTaskPaintLoop(Task task) throws MzsCoreException {
		List<Selector> selectors = new ArrayList<Selector>();
		selectors.add(LabelCoordinator.newSelector(CarPartType.CAR.toString(), 1)); //select one object
		Query query = null;
		Property prop = null;
		prop = Property.forName("taskId");
		query = new Query().filter(prop.equalTo(new Long(task.getId())));
		selectors.add(QueryCoordinator.newSelector(query));
		System.out.println("[Painter] Getting car:");
		List<ICarPart> parts = null;
		try {
				parts = capi.take(carContainer, selectors, SpaceTimeout.ZERO, tx);
		} catch (CountNotMetException ex) {
			//TODO read and paint body
			doPaintCarOrBody();
		}
		if(parts != null){
			System.out.println("[Painter] Painting Ordered car:");
			Car car = (Car) parts.get(0);
			car.getBody().setColor(pid, this.color);
			this.updateTask(task); //own tx
			writeCarIntoSpace(car); //commits tx
		}
	}

	private void doNormalPaintLoop() throws MzsCoreException {
		List<Selector> selectors = new ArrayList<Selector>();
		selectors.add(LabelCoordinator.newSelector(CarPartType.CAR.toString(), 1)); //select one object
		Query query = null;
		Property prop = null;
		prop = Property.forName("taskId");
		query = new Query().filter(prop.equalTo(new Long(-1)));
		selectors.add(QueryCoordinator.newSelector(query));

		List<ICarPart> parts = null;
		try {
				parts = capi.take(carContainer, selectors, SpaceTimeout.ZERO, tx);
		} catch (CountNotMetException ex) {
			parts = null;
		}
		if(parts != null){
			Car car = (Car) parts.get(0);
			car.getBody().setColor(pid, this.color);
			writeCarIntoSpace(car); //commits tx
		}else{
			//take body which is not painted
			
			List<Selector> selectors2 = new ArrayList<Selector>();
			selectors2.add(LabelCoordinator.newSelector(CarPartType.BODY.toString(), 1)); //select one object
			parts = null;
			try {
					parts = capi.take(bodyContainer, selectors2, SpaceTimeout.ZERO, tx);
			}catch (CountNotMetException e) {
				capi.rollbackTransaction(tx);
				return;
			}
			
			if(parts == null) {
				capi.rollbackTransaction(tx);
				return;
			}else{
				Body b = (Body) parts.get(0);
				b.setColor(pid, this.color);
				writeBodyIntoSpace(b); //commits
			}
			
		}
	}

	private void doPaintCarOrBody() throws MzsCoreException {
		List<Selector> selectors = new ArrayList<Selector>();
		selectors.add(LabelCoordinator.newSelector(CarPartType.CAR.toString(), 1)); //select one object
		Query query = null;
		Property prop = null;
		prop = Property.forName("taskId");
		query = new Query().filter(prop.equalTo(new Long(-1)));
		selectors.add(QueryCoordinator.newSelector(query));

		List<ICarPart> parts = null;
		try {
				parts = capi.take(carContainer, selectors, SpaceTimeout.ZERO, tx);
		} catch (CountNotMetException ex) {
			parts = null;
		}
		
		
		if(parts != null){
			Car car = (Car) parts.get(0);
			car.getBody().setColor(pid, this.color);
			writeCarIntoSpace(car); //commits tx
		}else{
			//take body which is not painted
			
			List<Selector> selectors2 = new ArrayList<Selector>();
			selectors2.add(LabelCoordinator.newSelector(CarPartType.BODY.toString(), 1)); //select one object
			parts = null;
			try {
					parts = capi.take(bodyContainer, selectors2, SpaceTimeout.ZERO, tx);
			}catch (CountNotMetException e) {
				capi.rollbackTransaction(tx);
				return;
			}
			
			if(parts == null) {
				capi.rollbackTransaction(tx);
				return;
			}else{
				Body b = (Body) parts.get(0);
				b.setColor(pid, this.color);
				writeBodyIntoSpace(b); //commits
			}
			
		}
		
	}

	private void updateTask(Task task) {
		TransactionReference tx2 = null;
		try {
			tx2 =  capi.createTransaction(SpaceTimeout.TENSEC, new URI(SpaceConstants.CONTAINER_URI));
		
			//take the same task
			//update
			//write back
			List<Selector> selectors = new ArrayList<Selector>();
			Query query = null;
			Property prop = null;
			prop = Property.forName("id");
			query = new Query().filter(prop.equalTo(new Long(task.getId())));
			selectors.add(QueryCoordinator.newSelector(query));

			List<Task> takenTaskList = null;
			takenTaskList = capi.take(taskContainer, selectors, SpaceTimeout.TENSEC, tx2);

			Task  takenTask = takenTaskList.get(0);
			takenTask.increasePaintAmount(1);
			takenTask.setAmountCompleted(takenTask.getAmountCompleted()+1);
			
			if(takenTask.isFinished()){
				System.out.println("[Painter]*Task finshed...write in finishedtasks");
				List<CoordinationData> ftaskCoords = new ArrayList<CoordinationData>();
				ftaskCoords.add(KeyCoordinator.newCoordinationData(""+takenTask.getId()));
				ftaskCoords.add(FifoCoordinator.newCoordinationData());
				ftaskCoords.add(LabelCoordinator.newCoordinationData("finishedTask"));
				capi.write(taskContainer, SpaceTimeout.TENSEC, tx2, new Entry(takenTask,ftaskCoords));
				
				List<Selector> fSelList = new ArrayList<Selector>();
				fSelList.add(KeyCoordinator.newSelector(""+takenTask.getId()));
				capi.delete(taskContainer, fSelList, SpaceTimeout.TENSEC, tx2);
				
				System.out.println("[PreferredAssembler]*Task deleted");
				
				capi.commitTransaction(tx2);
				return;
			} else {
				List<CoordinationData> taskCoords = new ArrayList<CoordinationData>();
				taskCoords.add(KeyCoordinator.newCoordinationData(""+takenTask.getId()));
				taskCoords.add(FifoCoordinator.newCoordinationData());
				taskCoords.add(LabelCoordinator.newCoordinationData(takenTask.getColor().toString()));
				capi.write(taskContainer, SpaceTimeout.TENSEC, tx2, new Entry(takenTask, taskCoords));
				System.out.println("[Painter]*Write Task");
				capi.commitTransaction(tx2);
			}
		} catch (MzsCoreException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}

	private Task getTask() {
		List<Selector> selectors = new ArrayList<Selector>();
		selectors.add(LabelCoordinator.newSelector(color.toString()));
		selectors.add(FifoCoordinator.newSelector(1));
		List<ICarPart> entities = null;
		try {
			entities = capi.read(taskContainer, selectors, MzsConstants.RequestTimeout.TRY_ONCE, null);
			if(entities != null) return (Task) entities.get(0);
		} catch (CountNotMetException ex) {
			return null;
		} catch (MzsTimeoutException e) {
			return null;
		} catch (TransactionException e) {
			return null;
		} catch (MzsCoreException e) {
			try {
				capi.rollbackTransaction(tx);
			} catch (MzsCoreException e1) { }
		}
		return null;
	}
	
	

	public List<ICarPart> takeCarPart(String selectorLabel, long timeout, TransactionReference tx) {
		List<Selector> selectors = new ArrayList<Selector>();
		selectors.add(LabelCoordinator.newSelector(selectorLabel, 1)); //select one object
		List<ICarPart> parts = null;
		try {
			if(selectorLabel.equals(CarPartType.BODY.toString())) {
				parts = capi.take(bodyContainer, selectors, timeout, tx);
			} else {
				parts = capi.take(carContainer, selectors, timeout, tx);
			}
		} catch (CountNotMetException ex) {
			return null;
		} catch (MzsTimeoutException e) {
			return null;
		} catch (TransactionException e) {
			return null;
		} catch (MzsCoreException e) {
			return null;
		}
		if (parts != null && !parts.isEmpty()) {
			return parts;
		} else {
			return null;
		}
	}

	private void initSpace() {
		MzsCore core = DefaultMzsCore.newInstance(0);
		this.capi = new Capi(core);
		try {
			List<Coordinator> coords = new ArrayList<Coordinator>();
			coords.add(new AnyCoordinator());
			coords.add(new LabelCoordinator());
			coords.add(new KeyCoordinator());
			coords.add(new QueryCoordinator());
			List<Coordinator> taskCoordinators = new ArrayList<Coordinator>();
			taskCoordinators.add(new AnyCoordinator());
			taskCoordinators.add(new KeyCoordinator());
			taskCoordinators.add(new FifoCoordinator());
			
			List<Coordinator> signalCoordinators = new ArrayList<Coordinator>();
			signalCoordinators.add(new AnyCoordinator());
			signalCoordinators.add(new FifoCoordinator());
			try {
				this.carContainer = CapiUtil.lookupOrCreateContainer(SpaceConstants.CARCONTAINER_NAME, new URI(SpaceConstants.CONTAINER_URI), coords, null, capi);
				this.bodyContainer = CapiUtil.lookupOrCreateContainer(SpaceConstants.BODYCONTAINER_NAME, new URI(SpaceConstants.CONTAINER_URI), coords, null, capi);
				this.taskContainer = CapiUtil.lookupOrCreateContainer(SpaceConstants.TASKCONTAINER_NAME, new URI(SpaceConstants.CONTAINER_URI), coords, null, capi);
				this.signalContainer = CapiUtil.lookupOrCreateContainer(SpaceConstants.SIGNALCONTAINER_NAME, new URI(SpaceConstants.CONTAINER_URI), signalCoordinators, null, capi);
			} catch (URISyntaxException e) {
				System.out.println("Error: Invalid container name");
				e.printStackTrace();
			}
			NotificationManager notificationManager = new NotificationManager(core);			
			Set<Operation> operations = new HashSet<Operation>();
			operations.add(Operation.WRITE);
			
			notificationManager.createNotification(signalContainer, new SignalContainerListener(), operations, null, null);
			

			
		} catch (MzsCoreException e) {
			System.out.println("Error: Could not initialize Space");
			e.printStackTrace();
		} catch (Exception e){
			e.printStackTrace();
		}
		System.out.println("[Painter]: Space initiated ");
	}

	private void writeBodyIntoSpace(Body body) throws MzsCoreException {
		List<CoordinationData> cordinator = new ArrayList<CoordinationData>();
		cordinator.add(LabelCoordinator.newCoordinationData(SpaceLabels.PAINTEDBODY));
		cordinator.add(KeyCoordinator.newCoordinationData("" + body.getId()));
		capi.write(new Entry(body,cordinator), bodyContainer, SpaceTimeout.INFINITE, tx);
		capi.commitTransaction(tx);
		System.out.println("[Painter] Body " + body.getId() + " painted and written in space");
	}

	private void writeCarIntoSpace(Car car) throws MzsCoreException {
		List<CoordinationData> cordinator = new ArrayList<CoordinationData>();
		cordinator.add(LabelCoordinator.newCoordinationData(SpaceLabels.PAINTEDCAR));
		cordinator.add(KeyCoordinator.newCoordinationData("" + car.getId()));
		cordinator.add(FifoCoordinator.newCoordinationData());
		capi.write( new Entry(car,cordinator), carContainer, SpaceTimeout.TENSEC, tx);
		capi.commitTransaction(tx);
		System.out.println("[Painter] Car " + car.getId() + " painted and written in space");
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
						Painter.this.running = false;
					}
				}
			}
		}
	}
}

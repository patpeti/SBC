package at.ac.tuwien.complang.carfactory.businesslogic.xvsm;

import java.awt.Color;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.mozartspaces.capi3.AnyCoordinator;
import org.mozartspaces.capi3.CoordinationData;
import org.mozartspaces.capi3.Coordinator;
import org.mozartspaces.capi3.CountNotMetException;
import org.mozartspaces.capi3.FifoCoordinator;
import org.mozartspaces.capi3.KeyCoordinator;
import org.mozartspaces.capi3.LabelCoordinator;
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

import at.ac.tuwien.complang.carfactory.application.enums.CarPartType;
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
	private long pid = 0;
	private Color color; //the color which this painter uses to paint an object. It is set on creation of the painter.

	private Capi capi;
	private TransactionReference tx;
	private ContainerReference carContainer, bodyContainer, taskContainer;
	
	public Painter(long id, Color color) {
		super(); //1
		pid = id;
		this.color = color;
		initSpace();
		while(true){
			doPaint();
		}
	}

	private void doPaint() {
		try {
			Thread.sleep(TIME_TO_PAINT/2);
		} catch (InterruptedException e) { }
		try {
			tx = capi.createTransaction(SpaceTimeout.TENSEC, new URI(SpaceConstants.CONTAINER_URI));
			//1. Take the Task object from the space
			//Task task = getTask();
			Task task = null;
			//2a. Take a car from the space
			List<ICarPart> carparts =  takeCarPart(CarPartType.CAR.toString(), SpaceTimeout.ZERO, null);
			if(carparts != null ) {
				//paint car body and write it to space
				try {
					Thread.sleep(TIME_TO_PAINT/2);
				} catch (InterruptedException e) { }
				Car car = (Car) carparts.get(0);
				car.getBody().setColor(pid, this.color);
				writeCarIntoSpace(car);
				if(task != null) {
					task.increasePaintAmount(1);
				}
			} else {
				//2b. if it is still null take a body from space
				List<ICarPart> parts = takeCarPart(CarPartType.BODY.toString(), SpaceTimeout.ZERO, null);
				//get body paint it write it
				if(parts != null) {
					try {
						Thread.sleep(TIME_TO_PAINT/2);
					} catch (InterruptedException e) { }
					Body b = (Body) parts.get(0);
					b.setColor(pid, this.color);
					writeBodyIntoSpace(b);
					if(task != null) {
						task.increasePaintAmount(1);
					}
				}
			}
//			capi.commitTransaction(tx);
		} catch (MzsCoreException e1) {
			try {
				capi.rollbackTransaction(tx);
			} catch (MzsCoreException e) {
				e.printStackTrace();
			}
			e1.printStackTrace();
		} catch (URISyntaxException e1) {
			e1.printStackTrace();
		}
	}

	private Task getTask() {
		List<Selector> selectors = new ArrayList<Selector>();
		selectors.add(LabelCoordinator.newSelector(color.toString()));
		selectors.add(FifoCoordinator.newSelector(1));
		List<ICarPart> entities = null;
		try {
			entities = capi.take(taskContainer, selectors, 0, tx);
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
			} catch (MzsCoreException e1) {
				e1.printStackTrace();
			}
			e.printStackTrace();
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
//			try {
//				capi.rollbackTransaction(tx);
//			} catch (MzsCoreException e1) {
//				e1.printStackTrace();
//			}
//			e.printStackTrace();
		}
		if (parts != null && !parts.isEmpty()) {
			return parts;
		} else {
			return null;
		}
	}

	private void initSpace(){
		MzsCore core = DefaultMzsCore.newInstance(0);
		this.capi = new Capi(core);
		try {
			List<Coordinator> coords = new ArrayList<Coordinator>();
			coords.add(new AnyCoordinator());
			coords.add(new LabelCoordinator());
			coords.add(new KeyCoordinator());
			List<Coordinator> taskCoordinators = new ArrayList<Coordinator>();
			taskCoordinators.add(new AnyCoordinator());
			taskCoordinators.add(new KeyCoordinator());
			taskCoordinators.add(new FifoCoordinator());
			try {
				this.carContainer = CapiUtil.lookupOrCreateContainer(SpaceConstants.CARCONTAINER_NAME, new URI(SpaceConstants.CONTAINER_URI), coords, null, capi);
				this.bodyContainer = CapiUtil.lookupOrCreateContainer(SpaceConstants.BODYCONTAINER_NAME, new URI(SpaceConstants.CONTAINER_URI), coords, null, capi);
				this.taskContainer = CapiUtil.lookupOrCreateContainer(SpaceConstants.TASKCONTAINER_NAME, new URI(SpaceConstants.CONTAINER_URI), coords, null, capi);
			} catch (URISyntaxException e) {
				System.out.println("Error: Invalid container name");
				e.printStackTrace();
			}
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
}

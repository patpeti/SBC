package at.ac.tuwien.complang.carfactory.businesslogic.xvsm;

import java.awt.Color;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.mozartspaces.capi3.AnyCoordinator;
import org.mozartspaces.capi3.CoordinationData;
import org.mozartspaces.capi3.Coordinator;
import org.mozartspaces.capi3.CountNotMetException;
import org.mozartspaces.capi3.FifoCoordinator;
import org.mozartspaces.capi3.KeyCoordinator;
import org.mozartspaces.capi3.KeyCoordinator.KeySelector;
import org.mozartspaces.capi3.LabelCoordinator;
import org.mozartspaces.capi3.LifoCoordinator;
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
import org.mozartspaces.core.MzsConstants.RequestTimeout;
import org.mozartspaces.core.MzsCore;
import org.mozartspaces.core.MzsCoreException;
import org.mozartspaces.core.MzsTimeoutException;
import org.mozartspaces.core.TransactionException;
import org.mozartspaces.core.TransactionReference;
import org.mozartspaces.notifications.Operation;

import at.ac.tuwien.complang.carfactory.application.enums.CarPartType;
import at.ac.tuwien.complang.carfactory.application.enums.PaintState;
import at.ac.tuwien.complang.carfactory.domain.Body;
import at.ac.tuwien.complang.carfactory.domain.Car;
import at.ac.tuwien.complang.carfactory.domain.CarId;
import at.ac.tuwien.complang.carfactory.domain.ICarPart;
import at.ac.tuwien.complang.carfactory.domain.Motor;
import at.ac.tuwien.complang.carfactory.domain.MotorType;
import at.ac.tuwien.complang.carfactory.domain.Task;
import at.ac.tuwien.complang.carfactory.domain.Wheel;
import at.ac.tuwien.complang.carfactory.ui.constants.SpaceConstants;
import at.ac.tuwien.complang.carfactory.ui.constants.SpaceLabels;
import at.ac.tuwien.complang.carfactory.ui.constants.SpaceTimeout;


public class Assembler{
	
	private Capi capi;
	private ContainerReference CarContainer;
	private ContainerReference MotorContainer;
	private ContainerReference WheelContainer;
	private ContainerReference BodyContainer;
	private ContainerReference CarIdContainer;
	private ContainerReference TaskContainer;
	
	public static long pid = 0;
	//public static long carId = 100000;

	private Body body;
	private Wheel[] fourWheels = new Wheel[4];
	private Motor motor;
	private TransactionReference tx;
	

	public Assembler(long id){
		/**
		 * Workflow:
		 * 1. Connect to the space
		 * 2. take a body
		 * 3. take 4 wheels
		 * 4. take a motor
		 * 5. assemble them into a car object (create a new car object and set the parts)
		 * 6. save the car object back into the space
		 */
		
		pid = id;
		//1
		initSpace();
		System.out.println("Space initialized");
	}
	
	public void doAssemble(){
		System.out.println("[Assembler] New loop");
		
		//read Task

		Task task = getTaskFromSpace();
		if(task != null){
			//if there is task set preferations (body, motor)
			preferredLoop(task);
			//if preferation is set and car written -> update Task counters (write task into space)
		}else{
			//if no task set ..do normal loop:
			defaultWork();
		}
	}
	
	private void preferredLoop(Task t) {
		if(t == null) return; //leave loop if no other Task can be found
		try{
		TransactionReference tx1 = capi.createTransaction(SpaceTimeout.TENSEC, new URI(SpaceConstants.CONTAINER_URI));
		Motor m = getPreferredMotor(t.getMotortype(),tx1);
		Body b = getPreferredBody(t.getColor(),tx1);
		Wheel[] wheels =  getFourWheels(tx1);
		assemblePreferredCar(m,b, wheels,t,tx1); //assemble car and update Task
		//if one element missing -> TransactionException is thrown
		}catch(TransactionException e){
			//rollback - automatically?
			preferredLoop(readNextTask(t));
		}catch(MzsCoreException e){
			System.err.println("Dear future me, in the past you hoped that you will never see this text coming");
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		
			
	}

	private Task readNextTask(Task t) {
		// assume Tasks are in FIFO order and bigger key value means older Task
		// get task with bigger key then the key of the input task
		//if nothing found return null -> terminates preferredLoop
		
		List<Selector> taskSelectors = new ArrayList<Selector>();
		taskSelectors.add(AnyCoordinator.newSelector());
		List<Task> tListe = new ArrayList<Task>();
		TransactionReference tx2 = null;
		try {
			tx2 = capi.createTransaction(SpaceTimeout.TENSEC, new URI(SpaceConstants.CONTAINER_URI));
		} catch (MzsCoreException e1) {
			e1.printStackTrace();
		} catch (URISyntaxException e1) {
			e1.printStackTrace();
		}
		try {
			tListe = capi.read(TaskContainer, taskSelectors, SpaceTimeout.TENSEC, tx2);
		} catch (MzsCoreException e) {
			return null;
		}
		
		if(!tListe.isEmpty() ){
			//assume elements are in FIFO order in the beginning old Tasks with low ids /in the end young Tasks with high ids
			for(Task iTask : tListe){
				if(iTask.getId()>t.getId()) return iTask;
			}
			return null;
		}else{
			return null;
		}

	}

	private void assemblePreferredCar(Motor m, Body b, Wheel[] wheels, Task t, TransactionReference tx1) throws MzsCoreException, TransactionException{
		//create the car
		Car c = new Car(pid, b, m, wheels);
		
		//write car into space (which labels?, coordinators?)
	
		//get ID from space
		long carId = 0;
		//TODO read next car ID
		List<Selector> idselectors = new ArrayList<Selector>();
		idselectors.add(LifoCoordinator.newSelector());
		List<CarId> spaceId = capi.take(CarIdContainer,idselectors,SpaceTimeout.ZERO,tx1);
		if(spaceId.size() != 0)	carId = spaceId.get(0).getCarID() + 1; //increase ID
		
		c.setId(carId);
		
		//write increase ID back into space
		
		List<CoordinationData> idcoords = new ArrayList<CoordinationData>();
		idcoords.add(LifoCoordinator.newCoordinationData());
		CarId id = new CarId();
		id.setCarID(carId);
		
		capi.write(new Entry(id,idcoords), CarIdContainer, SpaceTimeout.INFINITE, tx);
	
		List<CoordinationData> cordinator = new ArrayList<CoordinationData>();
		String label =  CarPartType.CAR.toString();
		if(this.body.getColor() != null) label = SpaceLabels.PAINTEDCAR;
		cordinator.add(LabelCoordinator.newCoordinationData(label));
		cordinator.add(KeyCoordinator.newCoordinationData(""+c.getId()));
		cordinator.add(FifoCoordinator.newCoordinationData());
		capi.write(new Entry(c,cordinator),CarContainer,SpaceTimeout.TENSEC,tx1 );
		System.out.println("[PreferredAssembler]*Car " + c.getId() + " created");
		
		//update task
		
		//take task
		List<Selector> sel = new ArrayList<Selector>();
		sel.add(KeyCoordinator.newSelector(""+t.getId()));
		Task takenTask = (Task) capi.take(TaskContainer, sel, SpaceTimeout.TENSEC, tx1).get(0);
		takenTask.increaseCarAmount(1);
		
		//write task
		List<CoordinationData> taskCoords = new ArrayList<CoordinationData>();
		taskCoords.add(KeyCoordinator.newCoordinationData(""+c.getId()));
		taskCoords.add(FifoCoordinator.newCoordinationData());
		capi.write(TaskContainer, SpaceTimeout.TENSEC, tx1, new Entry(takenTask,taskCoords));
		
		capi.commitTransaction(tx1);
	}

	private void defaultWork() {
		if(this.motor == null){
			System.out.println("[Assembler] motor was null");
			getOneMotor();
		}
		if(this.fourWheels[0] == null || this.fourWheels[1] == null || this.fourWheels[2] == null || this.fourWheels[3] == null){
			System.out.println("[Assembler] wheel was null");
			getFourWheels();
			
		}
		if(this.body == null){
			System.out.println("[Assembler] body was null");
			getOneBody();
			
		}
		if(this.motor != null && this.fourWheels[0] != null && this.body != null){
			System.out.println("[Assembler] all field set");
			//create Car
			createCar();
		}
		
		
	}

	private Task getTaskFromSpace() {
		List<Selector> selectors = new ArrayList<Selector>();
		selectors.add(FifoCoordinator.newSelector(1));
		Task t = null;
		try {
			t = (Task) capi.read(TaskContainer, selectors, SpaceTimeout.ZERO, null).get(0);
		} catch (MzsCoreException e) {
			System.err.println("Error while trying to get the first task from the space");
			return null;
		}
		return t;
	}
	

	private void createCar() {
		Car c = new Car(pid,this.body,this.motor,this.fourWheels);
		long carId = 0;
		//TODO read next car ID
		List<Selector> idselectors = new ArrayList<Selector>();
		idselectors.add(LifoCoordinator.newSelector());
		try{
		List<CarId> spaceId = capi.take(CarIdContainer,idselectors,SpaceTimeout.ZERO,tx);
		if(spaceId.size() != 0)	carId = spaceId.get(0).getCarID() + 1; //increase ID
	
		
		c.setId(carId);
		
		//write increase ID back into space
		
		List<CoordinationData> idcoords = new ArrayList<CoordinationData>();
		idcoords.add(LifoCoordinator.newCoordinationData());
		CarId id = new CarId();
		id.setCarID(carId);
		
			capi.write(new Entry(id,idcoords), CarIdContainer, SpaceTimeout.INFINITE, tx);
		} catch (MzsCoreException e1) {
			try {
				capi.rollbackTransaction(tx);
			} catch (MzsCoreException e) {
				e.printStackTrace();
			}
			e1.printStackTrace();
		}
		
		
		
		try {
			List<CoordinationData> cordinator = new ArrayList<CoordinationData>();
			String label =  CarPartType.CAR.toString();
			if(this.body.getColor() != null) label = SpaceLabels.PAINTEDCAR;
			cordinator.add(LabelCoordinator.newCoordinationData(label));
			cordinator.add(KeyCoordinator.newCoordinationData(""+c.getId()));
			cordinator.add(FifoCoordinator.newCoordinationData());
			capi.write(CarContainer, new Entry(c,cordinator));
			System.out.println("[Assembler]*Car " + c.getId() + " created");
		} catch (MzsCoreException e) {
			e.printStackTrace();
		}
	}
	
	private Motor getPreferredMotor(MotorType motortype, TransactionReference tx1) throws MzsCoreException,TransactionException {
		Motor tempMotor = null;
		
		Query query = null;
		Property prop = null;
		prop = Property.forName("*", "power");
		query = new Query().filter(prop.equalTo(motortype));
		List<Selector> selectors = new ArrayList<Selector>();
		selectors.add(QueryCoordinator.newSelector(query));
		selectors.add(AnyCoordinator.newSelector(1));
		
		tempMotor = (Motor) capi.take(MotorContainer, selectors, SpaceTimeout.TENSEC, tx1).get(0);
		
		return tempMotor;
	}

	
	private Body getPreferredBody(Color color, TransactionReference tx1) throws MzsCoreException,TransactionException {
		Body tempBody = null;
		Query query = null;
		Property prop = null;
		prop = Property.forName("*", "color");
		query = new Query().filter(prop.equalTo(color));
		List<Selector> selectors = new ArrayList<Selector>();
		selectors.add(QueryCoordinator.newSelector(query));
		selectors.add(AnyCoordinator.newSelector(1));
		
		try{
		tempBody = (Body) capi.take(BodyContainer, selectors, SpaceTimeout.ZERO, null).get(0);
		}catch(CountNotMetException e){
			System.out.println("[PreferredAssembler] Painted Body Not found");
		}
		
		if(tempBody == null){
			selectors.clear();
			selectors.add(AnyCoordinator.newSelector(1));
			tempBody = (Body) capi.take(BodyContainer, selectors, SpaceTimeout.TENSEC, tx1).get(0);
		}
		
		return tempBody;
	}
	
	public void getOneMotor() {
		
		List<ICarPart> motors = this.takeCarPart(CarPartType.MOTOR, new Integer(1), SpaceTimeout.INFINITE, tx);
		//set body
		if(motors != null)
			this.motor = (Motor) motors.get(0);
		else
			this.motor = null;	
		try {
			Set<Operation> operations = new HashSet<Operation>();
			operations.add(Operation.DELETE);
			operations.add(Operation.TAKE);
			operations.add(Operation.WRITE);
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("[Assembler] *Motor taken");
	}
	
	private Wheel[] getFourWheels(TransactionReference tx1) throws MzsCoreException, TransactionException {
		Wheel[] tempWheels = new Wheel[4];
		List<Selector> selectors = new ArrayList<Selector>();
		selectors.add(AnyCoordinator.newSelector(4));
		List<ICarPart> wheels = capi.take(WheelContainer, selectors, SpaceTimeout.TENSEC, tx1);
		int i = 0;
		for(ICarPart w : wheels){
			tempWheels[i] = (Wheel) w;
		}
		return tempWheels;
	}
	
	public void getFourWheels(){
	
		
		List<ICarPart> wheels = this.takeCarPart(CarPartType.WHEEL, new Integer(4), SpaceTimeout.INFINITE, tx);
		//set wheels
		int i = 0;
		for(ICarPart w : wheels){
			fourWheels[i] = (Wheel) w;
			i++;
		}
		try {
			Set<Operation> operations = new HashSet<Operation>();
			operations.add(Operation.DELETE);
			operations.add(Operation.TAKE);
			operations.add(Operation.WRITE);
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("[Assembler] *Four Wheels taken");
	}
	
	public void getOneBody() {
	
		try {
			
			//List<ICarPart> bodies = this.takeCarPart(CarPartType.BODY, new Integer(1), SpaceTimeout.ZERO_WITHEXCEPTION, tx);
			List<Selector> selectors = new ArrayList<Selector>();
			//selectors.add(LabelCoordinator.newSelector(CarPartType.BODY.toString(), MzsConstants.Selecting.COUNT_MAX));
			//selectors.add(AnyCoordinator.newSelector(1));
			Query query = null;
			Property prop = null;
			List<Matchmaker> matchmakers = new ArrayList<Matchmaker>();
			Matchmaker[] array = new Matchmaker[2];
			prop = Property.forName("*", "paintState");
			matchmakers.add(prop.equalTo(PaintState.PAINTED));
			matchmakers.add(prop.equalTo(PaintState.UNPAINTED));
			query = new Query().filter(Matchmakers.and(    (Matchmakers.or(matchmakers.toArray(array))),   Property.forName("type").equalTo(CarPartType.BODY)   ));
			selectors.add(QueryCoordinator.newSelector(query));
			List<ICarPart> bodies = null;
			bodies = capi.take(BodyContainer, selectors, RequestTimeout.INFINITE, tx);
			//set body
			if(bodies != null)
				this.body = (Body) bodies.get(0);
			else
				this.body = null;
			Set<Operation> operations = new HashSet<Operation>();
			operations.add(Operation.DELETE);
			operations.add(Operation.TAKE);
			operations.add(Operation.WRITE);
			//notifMgr.createNotification(container, this, operations, null, null);
			System.out.println("[Assembler] *Body taken");
		} catch (MzsCoreException e1) {
			e1.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void initSpace(){
		MzsCore core = DefaultMzsCore.newInstance(0);
		this.capi = new Capi(core);
		//notifMgr = new NotificationManager(core);		
		
		try {
			List<Coordinator> coords = new ArrayList<Coordinator>();
			coords.add(new AnyCoordinator());
			coords.add(new LabelCoordinator());
			coords.add(new QueryCoordinator());
			coords.add(new KeyCoordinator());
			coords.add(new FifoCoordinator());
			List<Coordinator> carIdCoords = new ArrayList<Coordinator>();
			carIdCoords.add(new LifoCoordinator());
			List<Coordinator> taskCoords = new ArrayList<Coordinator>();
			taskCoords.add(new FifoCoordinator());
			taskCoords.add(new QueryCoordinator());
			try {
				this.CarContainer = CapiUtil.lookupOrCreateContainer(SpaceConstants.CARCONTAINER_NAME, new URI(SpaceConstants.CONTAINER_URI), coords, null, capi);
				this.BodyContainer = CapiUtil.lookupOrCreateContainer(SpaceConstants.BODYCONTAINER_NAME, new URI(SpaceConstants.CONTAINER_URI), coords, null, capi);
				this.MotorContainer = CapiUtil.lookupOrCreateContainer(SpaceConstants.MOTORCONTAINER_NAME, new URI(SpaceConstants.CONTAINER_URI), coords, null, capi);
				this.WheelContainer = CapiUtil.lookupOrCreateContainer(SpaceConstants.WHEELCONTAINER_NAME, new URI(SpaceConstants.CONTAINER_URI), coords, null, capi);
				this.CarIdContainer = CapiUtil.lookupOrCreateContainer(SpaceConstants.CARIDCAONTAINER_NAME, new URI(SpaceConstants.CONTAINER_URI), carIdCoords, null, capi);
				this.TaskContainer = CapiUtil.lookupOrCreateContainer(SpaceConstants.TASKCONTAINER_NAME, new URI(SpaceConstants.CONTAINER_URI), taskCoords, null, capi);
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
	}

	private List<ICarPart> takeCarPart(CarPartType type, Integer amount, long timeout, TransactionReference tx){
		List<Selector> selectors = new ArrayList<Selector>();
		selectors.add(LabelCoordinator.newSelector(type.toString(), MzsConstants.Selecting.COUNT_MAX));
		selectors.add(AnyCoordinator.newSelector(amount));
		
		List<ICarPart> parts = null;
		ContainerReference container = null;
		switch (type) {
		case WHEEL:
			container = WheelContainer;
			break;
		case BODY:
			container = BodyContainer;
			break;
		case CAR:
			container = CarContainer;
			break;
		case MOTOR:
			container = MotorContainer;
			break;
		default:
			break;
		}
		
		try {
			if (timeout == 0) {
				try {
					parts = capi.take(container, selectors, RequestTimeout.ZERO, tx);
				} catch (CountNotMetException ex) {
					return null;
				}
			} else if (timeout == SpaceTimeout.ZERO_WITHEXCEPTION) {
				parts = capi.take(container, selectors, RequestTimeout.ZERO, tx);
			} else if (timeout == SpaceTimeout.INFINITE) {
				parts = capi.take(container, selectors, RequestTimeout.INFINITE, tx);
			} else {
				parts = capi.take(container, selectors, timeout, tx);
			}
		} catch (CountNotMetException ex) {
			System.err.println("Not enough object found");
			try {
				capi.rollbackTransaction(tx);
			} catch (MzsCoreException e) {
				e.printStackTrace();
			}
		} catch (MzsTimeoutException e) {
			e.printStackTrace();
		} catch (TransactionException e) {
			e.printStackTrace();
		} catch (MzsCoreException e) {
			e.printStackTrace();
		}
		if(parts == null) return null;
		if (!parts.isEmpty()) {
			return parts;
		} else {
			return null;
		}
	}

	

	public void doWork() {
		while(true){
			//System.out.println("[Assembler] New loop");
			
			try {
				tx = capi.createTransaction(MzsConstants.RequestTimeout.INFINITE, new URI(SpaceConstants.CONTAINER_URI));
			
			
			if(this.motor == null){
				//System.out.println("[Assembler] motor was null");
				getOneMotor();
			}
			if(this.fourWheels[0] == null || this.fourWheels[1] == null || this.fourWheels[2] == null || this.fourWheels[3] == null){
				//System.out.println("[Assembler] wheel was null");
				getFourWheels();
			}
			if(this.body == null){
				//System.out.println("[Assembler] body was null");
				getOneBody();
			}
			if(this.motor != null && this.fourWheels[0] != null && this.body != null) {
				System.out.println("[Assembler] all field set, creating car");
				// Create a new Car
				createCar();
				this.body = null;
				this.motor = null;
				this.fourWheels = new Wheel[4];
				
				capi.commitTransaction(tx);
			}
			
			} catch (MzsCoreException e) {
				e.printStackTrace();
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
		}
	}
}

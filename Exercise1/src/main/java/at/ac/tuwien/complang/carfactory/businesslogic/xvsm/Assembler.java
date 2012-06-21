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
	private ContainerReference carContainer;
	private ContainerReference motorContainer;
	private ContainerReference wheelContainer;
	private ContainerReference bodyContainer;
	private ContainerReference carIdContainer;
	private ContainerReference taskContainer;

	public static long pid = 0;

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
		initSpace();
		System.out.println("Space initialized");
	}

	public void doAssemble(){
		System.out.println("[Assembler] New loop");
		//read Task
		Task task = getTaskFromSpace();
		if(task != null){
			//if there is a task set preferences (body, motor)
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
		}catch(MzsTimeoutException e){
			//rollback - automatically?
			preferredLoop(readNextTask(t));
		}catch(MzsCoreException e){
			System.err.println("Dear future me, in the past you hoped that you will never see this text coming");
			e.printStackTrace();
			return;
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		
		
//		while(task != null) {
//			try{
//				TransactionReference tx1 = capi.createTransaction(SpaceTimeout.INFINITE, new URI(SpaceConstants.CONTAINER_URI));
//				Motor motor = getPreferredMotor(task.getMotortype(),tx1);
//				Body body = getPreferredBody(task.getColor(),tx1);
//				Wheel[] wheels =  getFourWheels(tx1);
//				assemblePreferredCar(motor, body, wheels, task, tx1); //assemble car and update Task
//				//if one element missing -> TransactionException is thrown
//			} catch(TransactionException e) {
//				//rollback - automatically?
//				task = readNextTask(task);
//			} catch(MzsCoreException e) {
//				System.err.println("Dear future me, in the past you hoped that you will never see this text coming");
//				e.printStackTrace();
//			} catch (URISyntaxException e) {
//				e.printStackTrace();
//			}
//		}
	}

	/**
	 * assume Tasks are in FIFO order and bigger key value means older Task
	 * get task with bigger key then the key of the input task
	 * if nothing found return null -> terminates preferredLoop
	 * 
	 * @param task for which the next task should be found
	 * @return The next task for the given task object
	 */
	private Task readNextTask(Task task) {
		System.out.println("readNextTask method");
		List<Selector> taskSelectors = new ArrayList<Selector>();
		taskSelectors.add(AnyCoordinator.newSelector());
		List<Task> tListe = new ArrayList<Task>();
		TransactionReference tx2 = null;
		try {
			tx2 = capi.createTransaction(SpaceTimeout.ONESEC, new URI(SpaceConstants.CONTAINER_URI));
			tListe = capi.read(taskContainer, taskSelectors, SpaceTimeout.ZERO, tx2);
		}catch(MzsTimeoutException e){
			return null;
		}catch (MzsCoreException e) {
			System.out.println("readNextTask method MzsCoreException");
			return null;
		} catch (URISyntaxException e) {
			e.printStackTrace();
			return null;
		}
		System.out.println("[Assembler] Task list is empty:" + (tListe.isEmpty() ? "YES" : "NO"));
		if(!tListe.isEmpty()) {
			//assume elements are in FIFO order in the beginning old Tasks with low ids /in the end young Tasks with high ids
			for(Task iTask : tListe){
				if(iTask.getId()>task.getId()) {
					System.out.println("readNextTask method return task with id: "+ iTask.getId());
					return iTask;
				}
			}
			//TODO before return do one normaloop because the task is blocked and there is no other task to do
			defaultWork();
			return null;
		} else {
			return null;
		}
	}

	private void assemblePreferredCar(Motor motor, Body body, Wheel[] wheels, Task t, TransactionReference tx1)
		throws MzsCoreException, TransactionException
	{
		//create the car
		Car car = new Car(pid, body, motor, wheels);
		car.setTaskId(t.getId());
		//write car into space (which labels?, coordinators?)
		//get ID from space, increase it and assign it to car, then write it back into the space
		setGlobalCarId(tx1, car);
		List<CoordinationData> coordinators = new ArrayList<CoordinationData>();
		String label =  CarPartType.CAR.toString();
		if(body.getColor() != null) label = SpaceLabels.PAINTEDCAR;
		coordinators.add(LabelCoordinator.newCoordinationData(label));
		coordinators.add(KeyCoordinator.newCoordinationData(""+car.getId()));
		coordinators.add(FifoCoordinator.newCoordinationData());
		capi.write(carContainer, SpaceTimeout.TENSEC,tx1, new Entry(car, coordinators));
		System.out.println("[PreferredAssembler]*Car " + car.getId() + " created");

		//update task
		
		//take task
		List<Selector> sel = new ArrayList<Selector>();
		sel.add(KeyCoordinator.newSelector(""+t.getId()));
		Task takenTask = (Task) capi.take(taskContainer, sel, SpaceTimeout.TENSEC, tx1).get(0);
		takenTask.increaseCarAmount(1);
		if(car.getBody().hasColor()){
			takenTask.increasePaintAmount(1);
			takenTask.setAmountCompleted(takenTask.getAmountCompleted()+1);
		}
		System.out.println("[PreferredAssembler]*Task taken");
		
		if(takenTask.isFinished()){
			System.out.println("[PreferredAssembler]*Task finshed...delete task");
			List<CoordinationData> ftaskCoords = new ArrayList<CoordinationData>();
			ftaskCoords.add(KeyCoordinator.newCoordinationData(""+takenTask.getId()));
			ftaskCoords.add(FifoCoordinator.newCoordinationData());
			ftaskCoords.add(LabelCoordinator.newCoordinationData("finishedTask"));
			capi.write(taskContainer, SpaceTimeout.TENSEC, tx1, new Entry(takenTask,ftaskCoords));
			
			List<Selector> fSelList = new ArrayList<Selector>();
			fSelList.add(KeyCoordinator.newSelector(""+takenTask.getId()));
			capi.delete(taskContainer, fSelList, SpaceTimeout.TENSEC, tx1);
			
			System.out.println("[PreferredAssembler]*Task deleted");
			capi.commitTransaction(tx1);
			return;
			
		
		}
		//write task
		List<CoordinationData> taskCoords = new ArrayList<CoordinationData>();
		taskCoords.add(KeyCoordinator.newCoordinationData(""+takenTask.getId()));
		taskCoords.add(FifoCoordinator.newCoordinationData());
		taskCoords.add(LabelCoordinator.newCoordinationData(takenTask.getColor().toString()));
		capi.write(taskContainer, SpaceTimeout.TENSEC, tx1, new Entry(takenTask, taskCoords));
		System.out.println("[PreferredAssembler]*Write Task");
		capi.commitTransaction(tx1);
	}

	private void setGlobalCarId(TransactionReference tx1, Car car)
			throws MzsCoreException {
		long carId = 0;
		//TODO read next car ID
		List<Selector> idselectors = new ArrayList<Selector>();
		idselectors.add(LifoCoordinator.newSelector());
		List<CarId> spaceId = capi.take(carIdContainer, idselectors, SpaceTimeout.ZERO, tx1);
		if(spaceId.size() != 0) {
			carId = spaceId.get(0).getCarID() + 1; //increase ID
		}
		System.out.println("[PreferredAssembler]*CarID taken");
		car.setId(carId);
		//write increase ID back into space
		List<CoordinationData> idcoords = new ArrayList<CoordinationData>();
		idcoords.add(LifoCoordinator.newCoordinationData());
		CarId id = new CarId();
		id.setCarID(carId);
		capi.write(new Entry(id,idcoords), carIdContainer, SpaceTimeout.TENSEC, tx1);
		System.out.println("[PreferredAssembler]*CarID increased");
	}

	private void defaultWork() {
		try {
			tx = capi.createTransaction(SpaceTimeout.ONESEC, new URI(SpaceConstants.CONTAINER_URI));
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
		}catch(MzsTimeoutException e){
			this.body = null;
			this.motor = null;
			this.fourWheels = new Wheel[4];
			return;
		}catch (MzsCoreException e) {
			return;
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}

	private Task getTaskFromSpace() {
		List<Selector> selectors = new ArrayList<Selector>();
		selectors.add(FifoCoordinator.newSelector(1));
		Task task = null;
		try {
			//Reading a task must be instantaneous
			task = (Task) capi.read(taskContainer, selectors, SpaceTimeout.ZERO, null).get(0);
		} catch (MzsCoreException e) {
			return null;
		}
		
		//if assembler finished with the Task but painter not yet read the next task... and this task should be closed by painter
		if(task.getAmount() <= task.getCarAmount()) {
			System.out.println("Assembler is finished with this task, reading the next one if any");
			Task nextTask = readNextTask(task);
			if(nextTask != null)
				System.out.println(nextTask.getId()); 
			else
				System.out.println("Task is null");
			return nextTask;
		}
		
		return task;
	}


	private void createCar() {
		Car car = new Car(pid,this.body,this.motor,this.fourWheels);
		long carId = 0;
		// read next car ID
		List<Selector> idselectors = new ArrayList<Selector>();
		idselectors.add(LifoCoordinator.newSelector());
		try {
			List<CarId> spaceId = capi.take(carIdContainer,idselectors,SpaceTimeout.TENSEC,tx);
			if(spaceId.size() != 0) {
				carId = spaceId.get(0).getCarID() + 1; //increase ID
			}
			car.setId(carId);
			//write increased ID back into space
			List<CoordinationData> idcoords = new ArrayList<CoordinationData>();
			idcoords.add(LifoCoordinator.newCoordinationData());
			CarId id = new CarId();
			id.setCarID(carId);
			capi.write(new Entry(id,idcoords), carIdContainer, SpaceTimeout.INFINITE, tx);
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
			cordinator.add(KeyCoordinator.newCoordinationData(""+car.getId()));
			cordinator.add(FifoCoordinator.newCoordinationData());
			capi.write(carContainer, new Entry(car,cordinator));
			System.out.println("[Assembler]*Car " + car.getId() + " created");
		} catch (MzsCoreException e) {
			e.printStackTrace();
		}
	}

	private Motor getPreferredMotor(MotorType motortype, TransactionReference tx1) throws MzsCoreException,MzsTimeoutException {
		Motor tempMotor = null;
		Query query = null;
		Property prop = null;
		prop = Property.forName("*", "power");
		query = new Query().filter(prop.equalTo(motortype));
		List<Selector> selectors = new ArrayList<Selector>();
		selectors.add(QueryCoordinator.newSelector(query));
		selectors.add(AnyCoordinator.newSelector(1));
		tempMotor = (Motor) capi.take(motorContainer, selectors, SpaceTimeout.INFINITE, tx1).get(0);
		System.out.println("[PreferredAssembler] Motor taken");
		return tempMotor;
	}


	private Body getPreferredBody(Color color, TransactionReference tx1) throws MzsCoreException,MzsTimeoutException {
		Body tempBody = null;
		Query query = null;
		Property prop = null;
		prop = Property.forName("*", "color");
		query = new Query().filter(prop.equalTo(color));
		List<Selector> selectors = new ArrayList<Selector>();
		selectors.add(QueryCoordinator.newSelector(query));
		selectors.add(AnyCoordinator.newSelector(1));
		try{
			tempBody = (Body) capi.take(bodyContainer, selectors, SpaceTimeout.ZERO, null).get(0);
		}catch(CountNotMetException e){
			System.out.println("[PreferredAssembler] Painted Body Not found");
		}
		if(tempBody == null){
			selectors.clear();
			selectors.add(AnyCoordinator.newSelector(1));
			tempBody = (Body) capi.take(bodyContainer, selectors, SpaceTimeout.INFINITE, tx1).get(0);
		}
		System.out.println("[PreferredAssembler] Body taken");
		return tempBody;
	}

	public void getOneMotor() throws MzsCoreException {
		List<Selector> selectors = new ArrayList<Selector>();
		selectors.add(AnyCoordinator.newSelector(1));
		List<ICarPart> motors = capi.take(motorContainer, selectors, SpaceTimeout.INFINITE, tx);
		//set body
		if(motors != null)
			this.motor = (Motor) motors.get(0);
		else
			this.motor = null;	
		System.out.println("[Assembler] *Motor taken");
	}

	private Wheel[] getFourWheels(TransactionReference tx1) throws MzsCoreException, MzsTimeoutException {
		Wheel[] tempWheels = new Wheel[4];
		List<Selector> selectors = new ArrayList<Selector>();
		selectors.add(AnyCoordinator.newSelector(4));
		List<Wheel> wheels = capi.take(wheelContainer, selectors, SpaceTimeout.INFINITE, tx1);
		int i = 0;
		for(Wheel wheel : wheels) {
			tempWheels[i] = wheel;
			i++;
		}
		System.out.println("[PreferredAssembler] *4Wheel taken");
		return tempWheels;
	}

	public void getFourWheels() throws MzsCoreException {
		List<Selector> selectors = new ArrayList<Selector>();
		selectors.add(AnyCoordinator.newSelector(4));
		List<ICarPart> wheels = capi.take(wheelContainer, selectors, SpaceTimeout.INFINITE, tx);
		int i = 0;
		for(ICarPart w : wheels){
			fourWheels[i] = (Wheel) w;
			i++;
		}
		System.out.println("[Assembler] *Four Wheels taken");
	}

	public void getOneBody() {
		try {
			List<Selector> selectors = new ArrayList<Selector>();
			Query query = null;
			Property prop = null;
			List<Matchmaker> matchmakers = new ArrayList<Matchmaker>();
			Matchmaker[] array = new Matchmaker[2];
			prop = Property.forName("*", "paintState");
			matchmakers.add(prop.equalTo(PaintState.PAINTED));
			matchmakers.add(prop.equalTo(PaintState.UNPAINTED));
			query = new Query().filter(
				Matchmakers.and(
					Matchmakers.or(matchmakers.toArray(array)),
					Property.forName("type").equalTo(CarPartType.BODY)
				)
			);
			selectors.add(QueryCoordinator.newSelector(query));
			List<ICarPart> bodies = null;
			bodies = capi.take(bodyContainer, selectors, RequestTimeout.INFINITE, tx);
			//set body
			if(bodies != null) {
				this.body = (Body) bodies.get(0);
			} else {
				this.body = null;
			}
			System.out.println("[Assembler] *Body taken");
		} catch (MzsTimeoutException e) { //do nothing, roll back is automatic
		} catch (MzsCoreException e1) {
			e1.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void initSpace(){
		MzsCore core = DefaultMzsCore.newInstance(0);
		this.capi = new Capi(core);
		try {
			List<Coordinator> coords = new ArrayList<Coordinator>();
			coords.add(new AnyCoordinator());
			coords.add(new LabelCoordinator());
			coords.add(new QueryCoordinator());
			coords.add(new FifoCoordinator());
			List<Coordinator> carIdCoords = new ArrayList<Coordinator>();
			carIdCoords.add(new LifoCoordinator());
			List<Coordinator> taskCoords = new ArrayList<Coordinator>();
			taskCoords.add(new FifoCoordinator());
			taskCoords.add(new QueryCoordinator());
			taskCoords.add(new LabelCoordinator());
			try {
				this.carContainer = CapiUtil.lookupOrCreateContainer(SpaceConstants.CARCONTAINER_NAME, new URI(SpaceConstants.CONTAINER_URI), coords, null, capi);
				this.bodyContainer = CapiUtil.lookupOrCreateContainer(SpaceConstants.BODYCONTAINER_NAME, new URI(SpaceConstants.CONTAINER_URI), coords, null, capi);
				this.motorContainer = CapiUtil.lookupOrCreateContainer(SpaceConstants.MOTORCONTAINER_NAME, new URI(SpaceConstants.CONTAINER_URI), coords, null, capi);
				this.wheelContainer = CapiUtil.lookupOrCreateContainer(SpaceConstants.WHEELCONTAINER_NAME, new URI(SpaceConstants.CONTAINER_URI), coords, null, capi);
				this.carIdContainer = CapiUtil.lookupOrCreateContainer(SpaceConstants.CARIDCAONTAINER_NAME, new URI(SpaceConstants.CONTAINER_URI), carIdCoords, null, capi);
				this.taskContainer = CapiUtil.lookupOrCreateContainer(SpaceConstants.TASKCONTAINER_NAME, new URI(SpaceConstants.CONTAINER_URI), taskCoords, null, capi);
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
}

package at.ac.tuwien.complang.carfactory.businesslogic.xvsm;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.mozartspaces.capi3.AnyCoordinator;
import org.mozartspaces.capi3.CoordinationData;
import org.mozartspaces.capi3.Coordinator;
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
import org.mozartspaces.core.MzsConstants.Container;
import org.mozartspaces.core.MzsCore;
import org.mozartspaces.core.MzsCoreException;
import org.mozartspaces.core.MzsTimeoutException;
import org.mozartspaces.core.TransactionException;
import org.mozartspaces.core.TransactionReference;

import at.ac.tuwien.complang.carfactory.application.enums.CarPartType;
import at.ac.tuwien.complang.carfactory.domain.Body;
import at.ac.tuwien.complang.carfactory.domain.Car;
import at.ac.tuwien.complang.carfactory.domain.ICarPart;
import at.ac.tuwien.complang.carfactory.domain.Motor;
import at.ac.tuwien.complang.carfactory.domain.Wheel;
import at.ac.tuwien.complang.carfactory.ui.constants.SpaceConstants;
import at.ac.tuwien.complang.carfactory.ui.constants.SpaceLabels;
import at.ac.tuwien.complang.carfactory.ui.constants.SpaceTimeout;

public class Supervisor{

	/**
	 * Workflow:
	 * 1. Connect to the Mozart space
	 * 2. Take a car from the Mozart space and verify its completed
	 * 3. Set the complete flag for the car
	 * 4. write it back into the space 		
	 */
	
	private Capi capi;
	private ContainerReference carContainer;
	private ContainerReference bodyContainer;
	private ContainerReference motorContainer;
	private ContainerReference wheelContainer;
	private ContainerReference defectContainer;
	private TransactionReference tx;
	private long pid = 0;
	
	
	public Supervisor(long id) {
		this.pid = id;
		initSpace();
		while(true){
			readTestedCar();
//			readPaintedCar();
//			recylceDefectedCar();
		}
	}
	
	private void readTestedCar() {
		try {
			tx = capi.createTransaction(SpaceTimeout.TENSEC, new URI(SpaceConstants.CONTAINER_URI));
		} catch (MzsCoreException e1) {
			e1.printStackTrace();
		} catch (URISyntaxException e1) {
			e1.printStackTrace();
		}
		
		List<Selector> selectors = new ArrayList<Selector>();
		selectors.add(FifoCoordinator.newSelector());
		selectors.add(LabelCoordinator.newSelector(SpaceLabels.PAINTEDCAR, MzsConstants.Selecting.COUNT_MAX));
		selectors.add(AnyCoordinator.newSelector(1));
		
		Query query = null;
		Property prop = null;
		Property prop2 = null;
		List<Matchmaker> matchmakers = new ArrayList<Matchmaker>();
		Matchmaker[] array = new Matchmaker[2];
		prop = Property.forName("isComplete_pid");
		prop2 = Property.forName("isDefectTested_pid");
		matchmakers.add(prop.notEqualTo(new Long(-1)));
		matchmakers.add(prop2.notEqualTo(new Long(-1)));
		query = new Query().filter((Matchmakers.and(matchmakers.toArray(array)))  );
		
		selectors.add(QueryCoordinator.newSelector(query));
		
		List<ICarPart> parts = null;
		
		try {
			parts = capi.take(carContainer, selectors, SpaceTimeout.INFINITE, tx);
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
		
		if(parts != null){
			Car c = (Car) parts.get(0);
			c.setFinished(pid, true);
			if(c.isDefect() || !c.isComplete()){
				writeDefectedCar(c);
				recycleCar(c);
				System.out.println("Car defected and recycled " + c.getId());
			}else{
				writeCar(c);
				System.out.println("Supervised car " + c.getId());
			}
			
		}
	}

	private void writeDefectedCar(Car c) {
		List<CoordinationData> cordinator = new ArrayList<CoordinationData>();
		cordinator.add(FifoCoordinator.newCoordinationData());
		cordinator.add(KeyCoordinator.newCoordinationData(""+c.getId()));
		try {
			// Write the finished car back to the space
			capi.write(new Entry(c,cordinator), defectContainer,SpaceTimeout.INFINITE, tx );
			//capi.commitTransaction(tx);
		} catch (MzsCoreException e) {
			try {
				capi.rollbackTransaction(tx);
			} catch (MzsCoreException e1) {
				e1.printStackTrace();
			}
			e.printStackTrace();
		}
		
	}

	private void recycleCar(Car c) {
				
		Body b = c.getBody();
		Motor m = c.getMotor();
		Wheel w1,w2,w3,w4;
		w1 = c.getWheels()[0];
		w2 = c.getWheels()[1];
		w3 = c.getWheels()[2];
		w4 = c.getWheels()[3];
		
		if(!b.isDefect()) writePartBackInSpace(b,tx);
		if(!m.isDefect()) writePartBackInSpace(m,tx);
		if(!w1.isDefect()) writePartBackInSpace(w1,tx);
		if(!w2.isDefect()) writePartBackInSpace(w2,tx);
		if(!w3.isDefect()) writePartBackInSpace(w3,tx);
		if(!w4.isDefect()) writePartBackInSpace(w4,tx);
		System.out.println("Car with id: " + c.getId() + " is recycled");
		
		try {
			capi.commitTransaction(tx);
		} catch (MzsCoreException e) {
			e.printStackTrace();
		}
	}


	private void writePartBackInSpace(ICarPart part, TransactionReference tx2) {
		ContainerReference c = null;
		List<CoordinationData> cordinator = new ArrayList<CoordinationData>();
		cordinator.add(KeyCoordinator.newCoordinationData(""+part.getId()));
		if(part instanceof Body){
			c = bodyContainer;
			if(((Body)part).getColor() == null){
				cordinator.add(LabelCoordinator.newCoordinationData(CarPartType.BODY.toString()));
			}
			else{
				cordinator.add(LabelCoordinator.newCoordinationData(SpaceLabels.PAINTEDBODY));
			}
			
		}else if(part instanceof Motor){
			cordinator.add(LabelCoordinator.newCoordinationData(CarPartType.MOTOR.toString()));
			c = motorContainer;
		}else if(part instanceof Wheel){
			cordinator.add(LabelCoordinator.newCoordinationData(CarPartType.WHEEL.toString()));
			c = wheelContainer;
		}
		try {
			capi.write(c,SpaceTimeout.TENSEC,tx2,new Entry(part, cordinator));
		} catch (MzsCoreException e) {
			e.printStackTrace();
		}
		
	}


	private void writeCar(Car c) {
		List<CoordinationData> cordinator = new ArrayList<CoordinationData>();
		cordinator.add(LabelCoordinator.newCoordinationData(SpaceLabels.FINISHEDCAR));
		cordinator.add(KeyCoordinator.newCoordinationData(""+c.getId()));
		try {
			// Write the finished car back to the space
			capi.write(new Entry(c,cordinator), carContainer,SpaceTimeout.INFINITE, tx );
			capi.commitTransaction(tx);
		} catch (MzsCoreException e) {
			try {
				capi.rollbackTransaction(tx);
			} catch (MzsCoreException e1) {
				e1.printStackTrace();
			}
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
			List<Coordinator> defectCoords = new ArrayList<Coordinator>();
			defectCoords.add(new FifoCoordinator());
			defectCoords.add(new KeyCoordinator());
			
			try {
				this.carContainer = CapiUtil.lookupOrCreateContainer(SpaceConstants.CARCONTAINER_NAME, new URI(SpaceConstants.CONTAINER_URI), coords, null, capi);
				this.bodyContainer = CapiUtil.lookupOrCreateContainer(SpaceConstants.BODYCONTAINER_NAME, new URI(SpaceConstants.CONTAINER_URI), coords, null, capi);
				this.motorContainer = CapiUtil.lookupOrCreateContainer(SpaceConstants.MOTORCONTAINER_NAME, new URI(SpaceConstants.CONTAINER_URI), coords, null, capi);
				this.wheelContainer = CapiUtil.lookupOrCreateContainer(SpaceConstants.WHEELCONTAINER_NAME, new URI(SpaceConstants.CONTAINER_URI), coords, null, capi);
				this.defectContainer = CapiUtil.lookupOrCreateContainer(SpaceConstants.DEFECTCONTAINER_NAME, new URI(SpaceConstants.CONTAINER_URI), defectCoords, null, capi);
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

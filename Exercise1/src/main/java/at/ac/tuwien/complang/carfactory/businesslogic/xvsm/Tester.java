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

import at.ac.tuwien.complang.carfactory.application.enums.CarPartType;
import at.ac.tuwien.complang.carfactory.domain.Car;
import at.ac.tuwien.complang.carfactory.domain.TesterType;
import at.ac.tuwien.complang.carfactory.ui.constants.SpaceConstants;
import at.ac.tuwien.complang.carfactory.ui.constants.SpaceLabels;
import at.ac.tuwien.complang.carfactory.ui.constants.SpaceTimeout;


//TODO: Needs to extend the abstract worker class!!!
public class Tester {
	
	private Capi capi;
	private ContainerReference container;
	//private ContainerReference defectContainer;
	private TransactionReference tx;
	private TesterType type;

	public Tester(TesterType type) {
		initSpace();
		this.type = type;
		if(type == TesterType.COMPLETETESTER) {
			//do completetestloop
			while(true) {
				doCompletenessTest();
			}
		}else if(type == TesterType.DEFECTTESTER) {
			while(true){
				doDefectTest();
			}
		}else{
			System.err.println("Better programmer needed");
			System.exit(1);
		}
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
		selectors.add(FifoCoordinator.newSelector(1));
		//add queryCoordinator so that we get cars that are not tested yet
		Query query = null;
		Property prop = null;
		prop = Property.forName("*", "defectTested");
		query = new Query().filter(prop.equalTo(false));
		selectors.add(QueryCoordinator.newSelector(query));
		
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
		//TODO: I really dont understand whats happening in the next few lines, need refectoring and fixing ~Sebastian
		/*if(testDefect(car)){
			
			if(car.isDefect()) ;//do nothing -> car still defect
			//else car.setDefect(false);
			
		}else{
			car.setDefectTested(true);
			car.setDefect(true);
		}*/
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
		selectors.add(FifoCoordinator.newSelector(1));
		
		// add queryCoordinator so that we get cars that are not tested yet
		
		Query query = null;
		Property prop = null;
		prop = Property.forName("*", "completenessTested");
		query = new Query().filter(prop.equalTo(false));
		selectors.add(QueryCoordinator.newSelector(query));
		
		
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
		//TODO: Refactor, clean up? I dont understand whats going on ~Sebastian
		/*if(testCompleteness(car)){
			if(car.isDefect()) ;//do nothing -> car still defect
			else car.setDefect(this.pid, false);
		}else{
			car.setDefect(true);
		}*/
		writeCarIntoSpace(car);
		
	}
	
	private boolean testCompleteness(Car c) {
		//all parts are set and body is painted
		boolean testOk = true;
		if(c.getBody() == null) testOk = false;
		else if(c.getBody().getColor() == null) testOk = false;
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
//		if(car.isCompletenessTested() && car.isDefectTested() && car.isDefect()){
//			//write car into defectlager.. NO 
//		try{	
//			List<CoordinationData> cordinator = new ArrayList<CoordinationData>();
//			cordinator.add(KeyCoordinator.newCoordinationData(""+car.getId()));
//			cordinator.add(FifoCoordinator.newCoordinationData());
//			capi.write(new Entry(car,cordinator),defectContainer,SpaceTimeout.TENSEC, tx );
//			capi.commitTransaction(tx);
//			System.out.println("[Tester]*Car " + car.getId() + " written in defectlager");
//		} catch (MzsCoreException e) {
//			e.printStackTrace();
//		}
//		}else{
		
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
//		}
	}

	private void initSpace(){
		MzsCore core = DefaultMzsCore.newInstance(0);
		this.capi = new Capi(core);
	
		this.container = null;
		try {
			List<Coordinator> coords = new ArrayList<Coordinator>();
//			coords.add(new AnyCoordinator());
			coords.add(new LabelCoordinator());
			coords.add(new KeyCoordinator());
			coords.add(new FifoCoordinator());	
			coords.add(new QueryCoordinator());
//			List<Coordinator> defcoords = new ArrayList<Coordinator>();
//			defcoords.add(new FifoCoordinator());	
//			defcoords.add(new KeyCoordinator());
			try {
				this.container = CapiUtil.lookupOrCreateContainer(SpaceConstants.CARCONTAINER_NAME, new URI(SpaceConstants.CONTAINER_URI), coords, null, capi);
				//this.defectContainer = CapiUtil.lookupOrCreateContainer(SpaceConstants.DEFECTCONTAINER_NAME, new URI(SpaceConstants.CONTAINER_URI), defcoords, null, capi);
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
		System.out.println("[SpaceUtil]: Space initiated ");
		
	}
	
}

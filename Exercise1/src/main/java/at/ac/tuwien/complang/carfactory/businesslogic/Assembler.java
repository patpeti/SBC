package at.ac.tuwien.complang.carfactory.businesslogic;

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
import org.mozartspaces.capi3.CountNotMetException;
import org.mozartspaces.capi3.KeyCoordinator;
import org.mozartspaces.capi3.LabelCoordinator;
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
import org.mozartspaces.notifications.Notification;
import org.mozartspaces.notifications.NotificationListener;
import org.mozartspaces.notifications.NotificationManager;
import org.mozartspaces.notifications.Operation;

import at.ac.tuwien.complang.carfactory.application.enums.CarPartType;
import at.ac.tuwien.complang.carfactory.domain.Body;
import at.ac.tuwien.complang.carfactory.domain.Car;
import at.ac.tuwien.complang.carfactory.domain.ICarPart;
import at.ac.tuwien.complang.carfactory.domain.Motor;
import at.ac.tuwien.complang.carfactory.domain.Wheel;
import at.ac.tuwien.complang.carfactory.ui.constants.SpaceConstants;
import at.ac.tuwien.complang.carfactory.ui.constants.SpaceTimeout;


public class Assembler implements NotificationListener, Runnable{
	
	private Capi capi;
	private ContainerReference container;
	private NotificationManager notifMgr;
	public static long pid = 0;
	public static long carId = 100000;
	
	
	private Body body;
	private Wheel[] fourWheels = new Wheel[4];
	private Motor motor;
	
	public Assembler(){
		
		
		/**
		 * TODO:
		 * 1. Connect to the space
		 * 2. load a body
		 * 3. load 4 wheels
		 * 4. load a motor
		 * 5. assemble them into a car object (create a new car object and set the parts)
		 * 7. mark the body, wheels and motor as already used 
		 *    (or alternatively remove them from the space)
		 *    FIXME: decide if objects should remain in the space -->all infos still available inside the car objects no need to have the original objects in the space
		 * 6. save the car object back into the space
		 */
		
		pid++;
		//1
		initSpace();
		System.out.println("Space initialized");
		
		
		
		
		
				
	
	}
	public void doAssemble(){
		
			
				System.out.println("[Assembler] New loop");
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
					deleteParts();
					
				}
			
	
	}
	
	private void deleteParts() {
		try {
			TransactionReference tx = capi.createTransaction(SpaceTimeout.ZERO_WITHEXCEPTION, new URI(SpaceConstants.CONTAINER_URI));
			List<Selector> bodySelector = new ArrayList<Selector>();
			bodySelector.add(KeyCoordinator.newSelector(""+this.body.getId(), 1));

		     			
			capi.delete(container, bodySelector, SpaceTimeout.ZERO_WITHEXCEPTION, tx);
			
			List<Selector> motorSelector = new ArrayList<Selector>();
			motorSelector.add(KeyCoordinator.newSelector(""+this.motor.getId(), 1));

			
		     			
			capi.delete(container, motorSelector, SpaceTimeout.ZERO_WITHEXCEPTION, tx);
			
			for(int i = 1; i < 4 ; i++){
				List<Selector> wheelSelector = new ArrayList<Selector>();
				wheelSelector.add(KeyCoordinator.newSelector(""+this.fourWheels[i].getId(), 1));
				capi.delete(container,wheelSelector,SpaceTimeout.ZERO_WITHEXCEPTION,tx);
			}
			this.body = null;
			this.motor = null;
			this.fourWheels = new Wheel[4];
			capi.commitTransaction(tx);
			
		
			
		} catch (MzsCoreException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		
		
		
		
		
	}
	private void createCar() {
		Car c = new Car(pid,this.body,this.motor,this.fourWheels);
		c.setId(carId);
		carId++;
		try {
			List<CoordinationData> cordinator = new ArrayList<CoordinationData>();
			cordinator.add(LabelCoordinator.newCoordinationData(CarPartType.CAR.toString()));
			cordinator.add(KeyCoordinator.newCoordinationData(""+c.getId()));
			capi.write(container, new Entry(c,cordinator));
			System.out.println("*************Car Created");
			notifMgr.createNotification(container, this, Operation.WRITE);
		} catch (MzsCoreException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
	}
	public void getOneMotor() {
		//4
				TransactionReference tx = null;
				try {
					tx = capi.createTransaction(100000, new URI(SpaceConstants.CONTAINER_URI));
				} catch (MzsCoreException e1) {
					e1.printStackTrace();
				} catch (URISyntaxException e1) {
					e1.printStackTrace();
				}
				List<ICarPart> motors = this.takeCarPart(CarPartType.MOTOR, new Integer(1), SpaceTimeout.ZERO_WITHEXCEPTION, tx);
				System.out.println(motors.size() + "Motor retrieved: " );
				System.out.println("id:" + motors.get(0).getId() );
				//set body
				this.motor = (Motor) motors.get(0);
				   try {
					   Set<Operation> operations = new HashSet<Operation>();
				        operations.add(Operation.DELETE);
				        operations.add(Operation.TAKE);
				        operations.add(Operation.WRITE);
			            notifMgr.createNotification(container, this, operations, null, null);
			        } catch (Exception e) {
			            e.printStackTrace();
			        }
				   System.out.println("*************Motor taken");
		
	}
	
	public void getFourWheels(){
		//3
		TransactionReference tx = null;
		try {
			tx = capi.createTransaction(SpaceTimeout.INFINITE, new URI(SpaceConstants.CONTAINER_URI));
		} catch (MzsCoreException e1) {
			e1.printStackTrace();
		} catch (URISyntaxException e1) {
			e1.printStackTrace();
		}
		List<ICarPart> wheels = this.takeCarPart(CarPartType.WHEEL, new Integer(4), SpaceTimeout.INFINITE, tx);
		System.out.println(wheels.size() + "Wheels retrieved: " );
		System.out.println("id:" + wheels.get(0).getId() );
		System.out.println("id:" + wheels.get(1).getId() );
		System.out.println("id:" + wheels.get(2).getId() );
		System.out.println("id:" + wheels.get(3).getId() );
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
	            notifMgr.createNotification(container, this, operations, null, null);
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
		   
		   System.out.println("*************Four Wheel taken");
	}
	
	public void getOneBody() {
		//2
				TransactionReference tx = null;
				try {
					tx = capi.createTransaction(100000, new URI(SpaceConstants.CONTAINER_URI));
				} catch (MzsCoreException e1) {
					e1.printStackTrace();
				} catch (URISyntaxException e1) {
					e1.printStackTrace();
				}
				List<ICarPart> bodies = this.takeCarPart(CarPartType.BODY, new Integer(1), SpaceTimeout.ZERO_WITHEXCEPTION, tx);
				System.out.println(bodies.size() + "Body retrieved: " );
				System.out.println("id:" + bodies.get(0).getId() );
				//set body
				this.body = (Body) bodies.get(0);
				   try {
					   Set<Operation> operations = new HashSet<Operation>();
				        operations.add(Operation.DELETE);
				        operations.add(Operation.TAKE);
				        operations.add(Operation.WRITE);
			            notifMgr.createNotification(container, this, operations, null, null);
			        } catch (Exception e) {
			            e.printStackTrace();
			        }
				   System.out.println("*************Body taken");
	}

	private void initSpace(){
		MzsCore core = DefaultMzsCore.newInstance(0);
		this.capi = new Capi(core);
		notifMgr = new NotificationManager(core);		
	
		this.container = null;
		try {
			List<Coordinator> coords = new ArrayList<Coordinator>();
			coords.add(new AnyCoordinator());
			coords.add(new LabelCoordinator());
			coords.add(new KeyCoordinator());
			try {
				this.container = CapiUtil.lookupOrCreateContainer(SpaceConstants.CONTAINER_NAME, new URI(SpaceConstants.CONTAINER_URI), coords, null, capi);
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

	        if (!parts.isEmpty()) {
	            return parts;
	        } else {
	            return null;
	        }

	}


	@Override
	public void entryOperationFinished(Notification source,
			Operation operation, List<? extends Serializable> entries) {


		System.out.println("[Notification]");
	
	}
	@Override
	public void run() {
		while(true){
			System.out.println("[Assembler] New loop");
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
				this.body = null;
				this.motor = null;
				this.fourWheels = new Wheel[4];
				//deleteParts();
				
			}
		}
	}


	 
	
}

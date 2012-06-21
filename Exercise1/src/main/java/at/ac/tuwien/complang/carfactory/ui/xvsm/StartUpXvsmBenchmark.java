package at.ac.tuwien.complang.carfactory.ui.xvsm;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

import org.mozartspaces.capi3.AnyCoordinator;
import org.mozartspaces.capi3.Coordinator;
import org.mozartspaces.capi3.FifoCoordinator;
import org.mozartspaces.capi3.KeyCoordinator;
import org.mozartspaces.capi3.LabelCoordinator;
import org.mozartspaces.capi3.LifoCoordinator;
import org.mozartspaces.capi3.QueryCoordinator;
import org.mozartspaces.core.Capi;
import org.mozartspaces.core.ContainerReference;
import org.mozartspaces.core.DefaultMzsCore;
import org.mozartspaces.core.Entry;
import org.mozartspaces.core.MzsCore;
import org.mozartspaces.core.MzsCoreException;
import org.mozartspaces.core.MzsConstants.Container;
import org.mozartspaces.notifications.NotificationManager;

import at.ac.tuwien.complang.carfactory.application.enums.ProducerType;
import at.ac.tuwien.complang.carfactory.application.producers.IFacade;
import at.ac.tuwien.complang.carfactory.application.producers.IFactory;
import at.ac.tuwien.complang.carfactory.application.producers.xvsm.FactoryFacade;
import at.ac.tuwien.complang.carfactory.domain.CarId;
import at.ac.tuwien.complang.carfactory.ui.constants.SpaceConstants;

public class StartUpXvsmBenchmark {
	ContainerReference motorContainer = null;
	ContainerReference wheelContainer = null;
	ContainerReference carContainer = null;
	ContainerReference bodyContainer = null;
	ContainerReference taskContainer = null;
	ContainerReference carIdContainer = null;
	ContainerReference defectContainer = null;
	ContainerReference signalContainer = null;
	//1. Create an embedded instance of Mozart spaces
	MzsCore core = DefaultMzsCore.newInstance(SpaceConstants.SPACE_PORT);
	final Capi capi = new Capi(core);
	
	public static void main(String[] args) {
		/**
		 * Workflow:
		 * - Start FactoryFacade
		 * - Start Producers
		 * - Produce 2000 Bodies, 2000 Motors, 2000 Wheels
		 * - Send start signal to workers
		 */
		new StartUpXvsmBenchmark().go();
	}
	
	public void go() {
		NotificationManager notificationManager = new NotificationManager(core);
		//2. Initialise a container for each type of data we store in the space (e.g Car, Body, Motor, Wheel, Task) 

		try {
			List<Coordinator> coords = new ArrayList<Coordinator>();
			coords.add(new AnyCoordinator());
			coords.add(new LabelCoordinator());
			coords.add(new KeyCoordinator());
			coords.add(new QueryCoordinator());
			List<Coordinator> taskCoordinators = new ArrayList<Coordinator>();
			taskCoordinators.add(new AnyCoordinator());
			taskCoordinators.add(new KeyCoordinator());
			taskCoordinators.add(new LabelCoordinator());
			taskCoordinators.add(new FifoCoordinator());
			taskCoordinators.add(new QueryCoordinator());
			List<Coordinator> optionalCoords = new ArrayList<Coordinator>();
			optionalCoords.add(new FifoCoordinator());
			List<Coordinator> carIdCoords = new ArrayList<Coordinator>();
			carIdCoords.add(new LifoCoordinator());
			List<Coordinator> defectCoords = new ArrayList<Coordinator>();
			defectCoords.add(new FifoCoordinator());
			defectCoords.add(new KeyCoordinator());
			
			List<Coordinator> signalCoordinators = new ArrayList<Coordinator>();
			signalCoordinators.add(new AnyCoordinator());
			try {
				motorContainer = capi.createContainer(SpaceConstants.MOTORCONTAINER_NAME, new URI(SpaceConstants.CONTAINER_URI),Container.UNBOUNDED,  coords, optionalCoords, null);
				wheelContainer = capi.createContainer(SpaceConstants.WHEELCONTAINER_NAME, new URI(SpaceConstants.CONTAINER_URI),Container.UNBOUNDED,  coords, optionalCoords, null);
				carContainer = capi.createContainer(SpaceConstants.CARCONTAINER_NAME, new URI(SpaceConstants.CONTAINER_URI),Container.UNBOUNDED,  coords, optionalCoords, null);
				bodyContainer = capi.createContainer(SpaceConstants.BODYCONTAINER_NAME, new URI(SpaceConstants.CONTAINER_URI),Container.UNBOUNDED,  coords, optionalCoords, null);
				taskContainer = capi.createContainer(SpaceConstants.TASKCONTAINER_NAME, new URI(SpaceConstants.CONTAINER_URI),Container.UNBOUNDED,  taskCoordinators, null, null);
				carIdContainer = capi.createContainer(SpaceConstants.CARIDCAONTAINER_NAME, new URI(SpaceConstants.CONTAINER_URI), Container.UNBOUNDED,carIdCoords, null, null);
				defectContainer = capi.createContainer(SpaceConstants.DEFECTCONTAINER_NAME, new URI(SpaceConstants.CONTAINER_URI), Container.UNBOUNDED,defectCoords, null, null);
				signalContainer = capi.createContainer(SpaceConstants.SIGNALCONTAINER_NAME, new URI(SpaceConstants.CONTAINER_URI), Container.UNBOUNDED, signalCoordinators, null, null);
				//init first id
				CarId firstId = new CarId();
				firstId.setCarID(100000);
				capi.write(carIdContainer,new Entry(firstId));
				
			} catch (URISyntaxException e) {
				System.out.println("Error: Invalid container name");
				System.exit(1);
			}
		} catch (MzsCoreException e) {
			System.out.println("Error: Could not initialize Space");
			System.exit(1);
		}
		List<ContainerReference> containers = new ArrayList<ContainerReference>();
		containers.add(bodyContainer);
		containers.add(carContainer);
		containers.add(motorContainer);
		containers.add(wheelContainer);

		IFacade factoryFacade = FactoryFacade.getInstance(capi, containers);
		IFactory bodyFactory = factoryFacade.getInstance(ProducerType.BODY);
		bodyFactory.init(2000, 0.0);
		bodyFactory.start();
		IFactory motorFactory = factoryFacade.getInstance(ProducerType.MOTOR);
		motorFactory.init(2000, 0.0);
		bodyFactory.start();
		IFactory wheelFactory = factoryFacade.getInstance(ProducerType.WHEEL);
		wheelFactory.init(8000, 0.0);
		bodyFactory.start();
		while(bodyFactory.isRunning() || motorFactory.isRunning() || wheelFactory.isRunning()) { 
			//busyloop to wait until the factories are finished
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		System.out.println("Production is finished. Press ENTER to send signal to workers.");
		Scanner scanner = new Scanner(System.in);
		scanner.nextLine();
		scanner.close();
		try {
			capi.write(signalContainer, new Entry(new String("START")));
		} catch (MzsCoreException e) {
		}
		System.out.println("Workers are starting...");
		Timer timer = new Timer();
		TimerTask task = new TimerTask() {
			@Override
			public void run() {
				try {
					stop();
					System.out.println("Workers are stopping...");
				} catch (MzsCoreException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};
		timer.schedule(task, 60000);
	}
	
	public void stop() throws MzsCoreException {
		capi.write(signalContainer, new Entry(new String("STOP")));
	}
}

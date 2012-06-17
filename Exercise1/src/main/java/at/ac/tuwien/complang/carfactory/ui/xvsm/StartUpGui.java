package at.ac.tuwien.complang.carfactory.ui.xvsm;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
import org.mozartspaces.core.MzsConstants.Container;
import org.mozartspaces.core.MzsCore;
import org.mozartspaces.core.MzsCoreException;
import org.mozartspaces.notifications.NotificationManager;
import org.mozartspaces.notifications.Operation;

import at.ac.tuwien.complang.carfactory.application.xvsm.FactoryFacade;
import at.ac.tuwien.complang.carfactory.application.xvsm.TaskController;
import at.ac.tuwien.complang.carfactory.domain.CarId;
import at.ac.tuwien.complang.carfactory.ui.ProductionUI;
import at.ac.tuwien.complang.carfactory.ui.constants.SpaceConstants;

public class StartUpGui {

	public static void main(String[] args) {
		
		//1. Create an embedded instance of Mozart spaces
		MzsCore core = DefaultMzsCore.newInstance(SpaceConstants.SPACE_PORT);
		Capi capi = new Capi(core);
		NotificationManager notificationManager = new NotificationManager(core);
		//2. Initialise a container for each type of data we store in the space (e.g Car, Body, Motor, Wheel, Task) 
		ContainerReference motorContainer = null;
		ContainerReference wheelContainer = null;
		ContainerReference carContainer = null;
		ContainerReference bodyContainer = null;
		ContainerReference taskContainer = null;
		ContainerReference carIdContainer = null;
		ContainerReference defectContainer = null;
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
			List<Coordinator> optionalCoords = new ArrayList<Coordinator>();
			optionalCoords.add(new FifoCoordinator());
			List<Coordinator> carIdCoords = new ArrayList<Coordinator>();
			carIdCoords.add(new LifoCoordinator());
			List<Coordinator> defectCoords = new ArrayList<Coordinator>();
			defectCoords.add(new FifoCoordinator());
			defectCoords.add(new KeyCoordinator());
			try {
				motorContainer = capi.createContainer(SpaceConstants.MOTORCONTAINER_NAME, new URI(SpaceConstants.CONTAINER_URI),Container.UNBOUNDED,  coords, optionalCoords, null);
				wheelContainer = capi.createContainer(SpaceConstants.WHEELCONTAINER_NAME, new URI(SpaceConstants.CONTAINER_URI),Container.UNBOUNDED,  coords, optionalCoords, null);
				carContainer = capi.createContainer(SpaceConstants.CARCONTAINER_NAME, new URI(SpaceConstants.CONTAINER_URI),Container.UNBOUNDED,  coords, optionalCoords, null);
				bodyContainer = capi.createContainer(SpaceConstants.BODYCONTAINER_NAME, new URI(SpaceConstants.CONTAINER_URI),Container.UNBOUNDED,  coords, optionalCoords, null);
				taskContainer = capi.createContainer(SpaceConstants.TASKCONTAINER_NAME, new URI(SpaceConstants.CONTAINER_URI),Container.UNBOUNDED,  taskCoordinators, null, null);
				carIdContainer = capi.createContainer(SpaceConstants.CARIDCAONTAINER_NAME, new URI(SpaceConstants.CONTAINER_URI), Container.UNBOUNDED,carIdCoords, null, null);
				defectContainer = capi.createContainer(SpaceConstants.DEFECTCONTAINER_NAME, new URI(SpaceConstants.CONTAINER_URI), Container.UNBOUNDED,defectCoords, null, null);
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
		//1. Start the User interface
		TaskController taskController = new TaskController(capi, taskContainer);
		ProductionUI gui = new ProductionUI(FactoryFacade.getInstance(capi, containers), taskController);
		Set<Operation> operations = new HashSet<Operation>();
		operations.add(Operation.DELETE);
		operations.add(Operation.TAKE);
		operations.add(Operation.WRITE);
		SpaceListener listener = new SpaceListener(gui);
		try {
			notificationManager.createNotification(containers.get(0), listener, operations, null, null);
			notificationManager.createNotification(containers.get(1), listener, operations, null, null);
			notificationManager.createNotification(containers.get(2), listener, operations, null, null);
			notificationManager.createNotification(containers.get(3), listener, operations, null, null);
			notificationManager.createNotification(taskContainer, listener, operations, null, null);
			notificationManager.createNotification(carIdContainer, listener, operations, null, null);
			notificationManager.createNotification(defectContainer, listener, operations, null, null);
		
		} catch (MzsCoreException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	
}

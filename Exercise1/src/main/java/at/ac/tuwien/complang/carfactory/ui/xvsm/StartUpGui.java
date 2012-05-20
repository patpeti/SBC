package at.ac.tuwien.complang.carfactory.ui.xvsm;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.mozartspaces.capi3.AnyCoordinator;
import org.mozartspaces.capi3.Coordinator;
import org.mozartspaces.capi3.FifoCoordinator;
import org.mozartspaces.capi3.KeyCoordinator;
import org.mozartspaces.capi3.LabelCoordinator;
import org.mozartspaces.capi3.QueryCoordinator;
import org.mozartspaces.core.Capi;
import org.mozartspaces.core.ContainerReference;
import org.mozartspaces.core.DefaultMzsCore;
import org.mozartspaces.core.MzsCore;
import org.mozartspaces.core.MzsCoreException;
import org.mozartspaces.core.MzsConstants.Container;
import org.mozartspaces.notifications.NotificationManager;
import org.mozartspaces.notifications.Operation;

import at.ac.tuwien.complang.carfactory.application.xvsm.FactoryFacade;
import at.ac.tuwien.complang.carfactory.ui.constants.SpaceConstants;

public class StartUpGui {
	
	
	public static void main(String[] args) {
		
		//2. Create an embedded instance of mozart spaces and initialize a container on port 9876
		MzsCore core = DefaultMzsCore.newInstance(SpaceConstants.SPACE_PORT);
		Capi capi = new Capi(core);
		NotificationManager notifMgr = new NotificationManager(core);
		ContainerReference motorContainer = null;
		ContainerReference wheelContainer = null;
		ContainerReference carContainer = null;
		ContainerReference bodyContainer = null;
		try {
			//List<AnyCoordinator> coords = Arrays.asList(new AnyCoordinator());
			List<Coordinator> coords = new ArrayList<Coordinator>();
			coords.add(new AnyCoordinator());
			coords.add(new LabelCoordinator());
			coords.add(new KeyCoordinator());
			coords.add(new QueryCoordinator());
			List<Coordinator> optionalCoords = new ArrayList<Coordinator>();
			optionalCoords.add(new FifoCoordinator());
			try {
				motorContainer = capi.createContainer(SpaceConstants.MOTORCONTAINER_NAME, new URI(SpaceConstants.CONTAINER_URI),Container.UNBOUNDED,  coords, optionalCoords, null);
				wheelContainer = capi.createContainer(SpaceConstants.WHEELCONTAINER_NAME, new URI(SpaceConstants.CONTAINER_URI),Container.UNBOUNDED,  coords, optionalCoords, null);
				carContainer = capi.createContainer(SpaceConstants.CARCONTAINER_NAME, new URI(SpaceConstants.CONTAINER_URI),Container.UNBOUNDED,  coords, optionalCoords, null);
				bodyContainer = capi.createContainer(SpaceConstants.BODYCONTAINER_NAME, new URI(SpaceConstants.CONTAINER_URI),Container.UNBOUNDED,  coords, optionalCoords, null);
			} catch (URISyntaxException e) {
				System.out.println("Error: Invalid container name");
				System.exit(1);
			}
		} catch (MzsCoreException e) {
			System.out.println("Error: Could not initialize Space");
			System.exit(1);
		}

		//1. Start the User interface
		List<ContainerReference> containers = new ArrayList<ContainerReference>();
		containers.add(bodyContainer);
		containers.add(carContainer);
		containers.add(motorContainer);
		containers.add(wheelContainer);
		ProductionUI gui = new ProductionUI(notifMgr, new FactoryFacade(capi, containers));
		Set<Operation> operations = new HashSet<Operation>();
		operations.add(Operation.DELETE);
		operations.add(Operation.TAKE);
		operations.add(Operation.WRITE);

		try {
			notifMgr.createNotification(containers.get(0), gui, operations, null, null);
			notifMgr.createNotification(containers.get(1), gui, operations, null, null);
			notifMgr.createNotification(containers.get(2), gui, operations, null, null);
			notifMgr.createNotification(containers.get(3), gui, operations, null, null);
			
		} catch (MzsCoreException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}

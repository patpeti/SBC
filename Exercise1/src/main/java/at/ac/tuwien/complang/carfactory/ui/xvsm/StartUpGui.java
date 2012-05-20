package at.ac.tuwien.complang.carfactory.ui.xvsm;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.mozartspaces.capi3.AnyCoordinator;
import org.mozartspaces.capi3.Coordinator;
import org.mozartspaces.capi3.FifoCoordinator;
import org.mozartspaces.capi3.KeyCoordinator;
import org.mozartspaces.capi3.LabelCoordinator;
import org.mozartspaces.capi3.QueryCoordinator;
import org.mozartspaces.core.Capi;
import org.mozartspaces.core.CapiUtil;
import org.mozartspaces.core.ContainerReference;
import org.mozartspaces.core.DefaultMzsCore;
import org.mozartspaces.core.MzsCore;
import org.mozartspaces.core.MzsCoreException;
import org.mozartspaces.core.MzsConstants.Container;
import org.mozartspaces.notifications.NotificationManager;

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
		
		//insatnciate globale Listener
		ISpaceListener listener = new SpaceListenerImpl();
		//1. Start the User interface
		List<ContainerReference> containers = new ArrayList<ContainerReference>();
		containers.add(bodyContainer);
		containers.add(carContainer);
		containers.add(motorContainer);
		containers.add(wheelContainer);
		ProductionUI gui = new ProductionUI(capi, containers, listener, notifMgr);
		listener.setSpaceObserver(gui);
	}
}

package at.ac.tuwien.complang.carfactory.ui;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.mozartspaces.capi3.AnyCoordinator;
import org.mozartspaces.capi3.Coordinator;
import org.mozartspaces.capi3.LabelCoordinator;
import org.mozartspaces.core.Capi;
import org.mozartspaces.core.CapiUtil;
import org.mozartspaces.core.ContainerReference;
import org.mozartspaces.core.DefaultMzsCore;
import org.mozartspaces.core.MzsCore;
import org.mozartspaces.core.MzsCoreException;
import org.mozartspaces.notifications.NotificationManager;

import at.ac.tuwien.complang.carfactory.ui.constants.SpaceConstants;

public class StartUpGui {
	
	
	public static void main(String[] args) {
		ContainerReference container;
		//2. Create an embedded instance of mozard spaces and initialize a container on port 9876
		MzsCore core = DefaultMzsCore.newInstance(SpaceConstants.SPACE_PORT);
		Capi capi = new Capi(core);
		NotificationManager notifMgr = new NotificationManager(core);
		container = null;
		try {
			//List<AnyCoordinator> coords = Arrays.asList(new AnyCoordinator());
			List<Coordinator> coords = new ArrayList<Coordinator>();
			coords.add(new AnyCoordinator());
			coords.add(new LabelCoordinator());
			try {
				container = CapiUtil.lookupOrCreateContainer(SpaceConstants.CONTAINER_NAME, new URI(SpaceConstants.CONTAINER_URI), coords, null, capi);
			} catch (URISyntaxException e) {
				System.out.println("Error: Invalid container name");
				System.exit(1);
			}
			//container = capi.createContainer();
		} catch (MzsCoreException e) {
			System.out.println("Error: Could not initialize Space");
			System.exit(1);
		}
		
		//insatnciate globale Listener
		ISpaceListener listener = new SpaceListenerImpl();
		//1. Start the User interface
		ProductionUI gui = new ProductionUI(capi, container, listener, notifMgr);
		listener.setSpaceObserver(gui);
	}
}

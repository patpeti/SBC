package at.ac.tuwien.complang.carfactory.businesslogic;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.mozartspaces.capi3.AnyCoordinator;
import org.mozartspaces.capi3.Coordinator;
import org.mozartspaces.capi3.FifoCoordinator;
import org.mozartspaces.capi3.KeyCoordinator;
import org.mozartspaces.capi3.LabelCoordinator;
import org.mozartspaces.core.Capi;
import org.mozartspaces.core.CapiUtil;
import org.mozartspaces.core.ContainerReference;
import org.mozartspaces.core.DefaultMzsCore;
import org.mozartspaces.core.MzsCore;
import org.mozartspaces.core.MzsCoreException;
import org.mozartspaces.notifications.NotificationManager;

import at.ac.tuwien.complang.carfactory.ui.constants.SpaceConstants;

public class SuperVisor {

	
	/**
	 * TODO:
	 * 1. Connect to the mozard space
	 * 2. Load a car from the mozardspace and verify its completed
	 * 3. Set the complete flag for the car
	 * 4. write it back into the space 		
	 */
	
	private Capi capi;
	private ContainerReference container;
	private NotificationManager notifMgr;
	
	public SuperVisor() {
		initSpace();
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
			coords.add(new FifoCoordinator());						
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
		System.out.println("[SpaceUtil]: Space initiated ");
		
	}
	
}

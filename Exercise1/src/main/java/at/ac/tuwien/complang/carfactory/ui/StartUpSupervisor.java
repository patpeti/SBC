package at.ac.tuwien.complang.carfactory.ui;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

import org.mozartspaces.capi3.AnyCoordinator;
import org.mozartspaces.core.Capi;
import org.mozartspaces.core.CapiUtil;
import org.mozartspaces.core.ContainerReference;
import org.mozartspaces.core.DefaultMzsCore;
import org.mozartspaces.core.MzsCore;
import org.mozartspaces.core.MzsCoreException;

import at.ac.tuwien.complang.carfactory.ui.constants.SpaceConstants;

public class StartUpSupervisor {
	public static void main(String[] args) {
		/**
		 * TODO:
		 * 1. Connect to the mozard space
		 * 2. Load a car from the mozardspace and verify its completed
		 * 3. Set the complete flag for the car
		 * 4. write it back into the space 		
		 */
		MzsCore core = DefaultMzsCore.newInstance(0);
		Capi capi = new Capi(core);
		ContainerReference container;
		try {
			List<AnyCoordinator> coords = Arrays.asList(new AnyCoordinator());
			try {
				container = CapiUtil.lookupOrCreateContainer(SpaceConstants.CONTAINER_NAME, new URI(SpaceConstants.CONTAINER_URI), coords, null, capi);
			} catch (URISyntaxException e) {
				System.out.println("Error: Invalid container name");
				e.printStackTrace();
			}
		} catch (MzsCoreException e) {
			System.out.println("Error: Could not initialize Space");
			e.printStackTrace();
		}
		try {
			System.in.read();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

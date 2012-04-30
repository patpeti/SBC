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

public class StartUpAssembler {
	public static void main(String[] args) {
		/**
		 * TODO:
		 * 1. Connect to the space
		 * 2. load a body
		 * 3. load 4 wheels
		 * 4. load a motor
		 * 5. assemble them into a car object (create a new car object and set the parts)
		 * 7. mark the body, wheels and motor as already used 
		 *    (or alternatively remove them from the space)
		 *    FIXME: decide if objects should remain in the space
		 * 6. save the car object back into the space
		 */
		
		MzsCore core = DefaultMzsCore.newInstance(0);
		Capi capi = new Capi(core);
		ContainerReference container;
		try {
			List<AnyCoordinator> coords = Arrays.asList(new AnyCoordinator());
			try {
				container = CapiUtil.lookupOrCreateContainer(StartUpGui.CONTAINER_NAME, new URI(StartUpGui.CONTAINER_URI), coords, null, capi);
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

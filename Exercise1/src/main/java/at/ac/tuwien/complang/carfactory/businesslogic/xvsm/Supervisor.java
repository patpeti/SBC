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
import org.mozartspaces.capi3.Selector;
import org.mozartspaces.core.Capi;
import org.mozartspaces.core.CapiUtil;
import org.mozartspaces.core.ContainerReference;
import org.mozartspaces.core.DefaultMzsCore;
import org.mozartspaces.core.Entry;
import org.mozartspaces.core.MzsConstants;
import org.mozartspaces.core.MzsCore;
import org.mozartspaces.core.MzsCoreException;
import org.mozartspaces.core.MzsTimeoutException;
import org.mozartspaces.core.TransactionException;
import org.mozartspaces.core.TransactionReference;

import at.ac.tuwien.complang.carfactory.domain.Car;
import at.ac.tuwien.complang.carfactory.domain.ICarPart;
import at.ac.tuwien.complang.carfactory.ui.constants.SpaceConstants;
import at.ac.tuwien.complang.carfactory.ui.constants.SpaceLabels;
import at.ac.tuwien.complang.carfactory.ui.constants.SpaceTimeout;

public class Supervisor{

	/**
	 * Workflow:
	 * 1. Connect to the Mozard space
	 * 2. Take a car from the Mozard space and verify its completed
	 * 3. Set the complete flag for the car
	 * 4. write it back into the space 		
	 */
	
	private Capi capi;
	private ContainerReference container;
	private TransactionReference tx;
	private long pid = 0;
	
	public Supervisor(long id) {
		this.pid = id;
		initSpace();
		while(true){
			readPaintedCar();
		}
	}
	
	private void readPaintedCar(){
		
		try {
			tx = capi.createTransaction(SpaceTimeout.TENSEC, new URI(SpaceConstants.CONTAINER_URI));
		} catch (MzsCoreException e1) {
			e1.printStackTrace();
		} catch (URISyntaxException e1) {
			e1.printStackTrace();
		}
		
		List<Selector> selectors = new ArrayList<Selector>();
		selectors.add(FifoCoordinator.newSelector());
		selectors.add(LabelCoordinator.newSelector(SpaceLabels.PAINTEDCAR, MzsConstants.Selecting.COUNT_MAX));
		selectors.add(AnyCoordinator.newSelector(1));
		List<ICarPart> parts = null;
		
		try {
			parts = capi.take(container, selectors, SpaceTimeout.INFINITE, tx);
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
		
		if(parts != null){
			Car c = (Car) parts.get(0);
			c.setComplete(pid, true);
			writeCar(c);
			System.out.println("Supervised car " + c.getId());
		}
	}

	private void writeCar(Car c) {
		List<CoordinationData> cordinator = new ArrayList<CoordinationData>();
		cordinator.add(LabelCoordinator.newCoordinationData(SpaceLabels.FINISHEDCAR));
		cordinator.add(KeyCoordinator.newCoordinationData(""+c.getId()));
		try {
			// Write the finished car back to the space
			capi.write(new Entry(c,cordinator), container,SpaceTimeout.INFINITE, tx );
			capi.commitTransaction(tx);
		} catch (MzsCoreException e) {
			try {
				capi.rollbackTransaction(tx);
			} catch (MzsCoreException e1) {
				e1.printStackTrace();
			}
			e.printStackTrace();
		}
	}

	private void initSpace(){
		MzsCore core = DefaultMzsCore.newInstance(0);
		this.capi = new Capi(core);
	
		this.container = null;
		try {
			List<Coordinator> coords = new ArrayList<Coordinator>();
			coords.add(new AnyCoordinator());
			coords.add(new LabelCoordinator());
			coords.add(new KeyCoordinator());
			coords.add(new FifoCoordinator());						
			try {
				this.container = CapiUtil.lookupOrCreateContainer(SpaceConstants.CARCONTAINER_NAME, new URI(SpaceConstants.CONTAINER_URI), coords, null, capi);
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

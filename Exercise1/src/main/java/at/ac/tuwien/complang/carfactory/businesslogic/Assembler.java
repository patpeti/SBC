package at.ac.tuwien.complang.carfactory.businesslogic;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.mozartspaces.capi3.AnyCoordinator;
import org.mozartspaces.capi3.Coordinator;
import org.mozartspaces.capi3.CountNotMetException;
import org.mozartspaces.capi3.LabelCoordinator;
import org.mozartspaces.capi3.Selector;
import org.mozartspaces.core.Capi;
import org.mozartspaces.core.CapiUtil;
import org.mozartspaces.core.ContainerReference;
import org.mozartspaces.core.DefaultMzsCore;
import org.mozartspaces.core.MzsConstants;
import org.mozartspaces.core.MzsConstants.RequestTimeout;
import org.mozartspaces.core.MzsCore;
import org.mozartspaces.core.MzsCoreException;
import org.mozartspaces.core.MzsTimeoutException;
import org.mozartspaces.core.TransactionException;
import org.mozartspaces.core.TransactionReference;

import at.ac.tuwien.complang.carfactory.application.enums.CarPartType;
import at.ac.tuwien.complang.carfactory.domain.ICarPart;
import at.ac.tuwien.complang.carfactory.ui.constants.SpaceConstants;
import at.ac.tuwien.complang.carfactory.ui.constants.SpaceTimeout;

public class Assembler {
	
	private Capi capi;
	private ContainerReference container;

	public Assembler() {
		
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
		
		//1
		initSpace();
		System.out.println("Space initialized");
		//2
		TransactionReference tx = null;
		try {
			tx = capi.createTransaction(100000, new URI(SpaceConstants.CONTAINER_URI));
		} catch (MzsCoreException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (URISyntaxException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		List<ICarPart> body = this.takeCarPart(CarPartType.BODY, new Integer(1), SpaceTimeout.ZERO_WITHEXCEPTION, tx);
		System.out.println(body.size() + "Body retrieved: " );
		System.out.println("id:" + body.get(0).getId() );
		
		
				
		try {
			System.in.read();
		} catch (IOException e) {
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

	
	
}

package at.ac.tuwien.complang.carfactory.ui;

import org.mozartspaces.core.Capi;

import at.ac.tuwien.complang.carfactory.businesslogic.Assembler;

public class StartUpAssembler {
	
	private static Capi capi; 
	
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
		
		Assembler assembler = new Assembler();
		
		
	}
	
	
	
}

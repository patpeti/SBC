package at.ac.tuwien.complang.carfactory.ui.xvsm;

import at.ac.tuwien.complang.carfactory.businesslogic.xvsm.Assembler;
import at.ac.tuwien.complang.carfactory.ui.jms.StartUpJmsAssembler;

public class StartUpAssembler {
	
	//Static Fields 
	private static long id;
	
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

		parseArguments(args);
		Assembler assembler = new Assembler(id);
		assembler.doWork();
	}

	private static void parseArguments(String[] args) {
		String usage = "[Usage] " + StartUpJmsAssembler.class.getName() + " --id=<id>";
		if(args.length != 1) {
			System.out.println(usage);
			System.exit(1);
		}
		try {
			if(!args[0].startsWith("--id=")) {
				System.out.println(usage);
				System.exit(1);
			}
			String substring = args[0].substring(5);
			id = Integer.parseInt(substring);
		} catch(NumberFormatException e) {
			System.out.println(usage);
			System.exit(1);
		}
	}
	
}

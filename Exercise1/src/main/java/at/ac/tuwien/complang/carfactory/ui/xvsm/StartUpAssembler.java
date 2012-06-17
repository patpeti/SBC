package at.ac.tuwien.complang.carfactory.ui.xvsm;

import at.ac.tuwien.complang.carfactory.businesslogic.xvsm.Assembler;
import at.ac.tuwien.complang.carfactory.ui.jms.StartUpJmsAssembler;

public class StartUpAssembler {
	
	//Static Fields 
	private static long id;
	
	public static void main(String[] args) {
		/**
		 * Workflow:
		 * 1. Connect to the space
		 * 2. take a body
		 * 3. take 4 wheels
		 * 4. take a motor
		 * 5. assemble them into a car object (create a new car object and set the parts)
		 * 6. save the car object back into the space
		 */

		parseArguments(args);
		Assembler assembler = new Assembler(id);
		while(true) assembler.doAssemble();
		//assembler.doWork();
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

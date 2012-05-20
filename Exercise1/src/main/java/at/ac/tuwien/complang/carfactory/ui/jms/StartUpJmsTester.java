package at.ac.tuwien.complang.carfactory.ui.jms;

import at.ac.tuwien.complang.carfactory.ui.xvsm.StartUpSupervisor;

public class StartUpJmsTester {
	
	//Static Fields
	private static long id;
	
	public static void main(String[] args) {
		/**
		 * Workflow:
		 * 1. Connect to the Mozard space
		 * 2. Load a car from the Mozard space (in a transaction) and verify if it contains defect parts
		 * 3. Set the defect flag for the car if necessary
		 * 4. write it back into the space
		 */
		parseArguments(args);
		//Initialize tester: Supervisor s = new Supervisor(id);
	}

	private static void parseArguments(String[] args) {
		String usage = "[Usage] " + StartUpSupervisor.class.getName() + " --id=<id>";
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

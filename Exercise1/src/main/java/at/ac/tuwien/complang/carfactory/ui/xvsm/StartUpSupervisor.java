package at.ac.tuwien.complang.carfactory.ui.xvsm;

import at.ac.tuwien.complang.carfactory.application.workers.xvsm.Supervisor;

public class StartUpSupervisor {
	
	//Static Fields
	private static long id;
	
	public static void main(String[] args) {
		/**
		 * Workflow:
		 * 1. Connect to the Mozard space
		 * 2. Load a car from the Mozard space and verify its completed
		 * 3. Set the complete flag for the car
		 * 4. write it back into the space 		
		 */
		parseArguments(args);
		new Supervisor(id);
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

package at.ac.tuwien.complang.carfactory.ui.jms;

import at.ac.tuwien.complang.carfactory.businesslogic.jms.JmsSupervisor;

public class StartUpJmsSupervisor {

	//Static Fields
	static int id;
	
	public static void main(String[] args) {
		parseArguments(args);
		JmsSupervisor supervisor = new JmsSupervisor(id);
		supervisor.initialize();
		supervisor.startWorkLoop();
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

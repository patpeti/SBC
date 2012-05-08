package at.ac.tuwien.complang.carfactory.ui.jms;

import at.ac.tuwien.complang.carfactory.businesslogic.jms.AltAssembler;

public class StartUpAltAssembler {

	static int id;
	
	public static void main(String[] args) {
		parseArguments(args);
		AltAssembler assembler = new AltAssembler(id);
		assembler.initialize();
		assembler.startAssemblyLoop();
	}

	private static void parseArguments(String[] args) {
		String usage = "[Usage] " + StartUpAltAssembler.class.getName() + " --id=<id>";
		if(args.length != 1) {
			System.out.println(usage);
			return;
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

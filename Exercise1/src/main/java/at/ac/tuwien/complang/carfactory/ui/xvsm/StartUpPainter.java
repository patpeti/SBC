package at.ac.tuwien.complang.carfactory.ui.xvsm;

import java.awt.Color;

import at.ac.tuwien.complang.carfactory.application.workers.xvsm.Painter;
import at.ac.tuwien.complang.carfactory.ui.jms.StartUpJmsAssembler;

public class StartUpPainter {
	
	//Static Fields
	private static long id;
	private static Color color;
	
	public static void main(String[] args) {
		/**
		 * Workflow:
		 * 1. Connect to the Mozard space
		 * 2. Load a Body (which is not yet painted) or an
		 *    assembled car (which is not yet painted)
		 * 3. Paint the Body or the Body associated with the car object
		 * 4. Save the painted part back into the space 
		 */

		parseArguments(args);
		new Painter(id, color);
	}

	private static void parseArguments(String[] args) {
		String usage = "[Usage] " + StartUpJmsAssembler.class.getName() + " --id=<id> --color=<color>\nValid colors are: [RED, GREEN, BLUE]";
		if(args.length != 2) {
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
		if(!args[1].startsWith("--color=")) {
			System.out.println(usage);
			System.exit(1);
		}
		String substring = args[1].substring(8).toUpperCase();
		if(substring.equals("RED")) {
			color = Color.RED;
		} else if (substring.equals("BLUE")) {
			color = Color.BLUE;
		} else if (substring.equals("GREEN")) {
			color = Color.green;
		} else {
			System.out.println(usage);
			System.exit(1);
		}
	}
}

package at.ac.tuwien.complang.carfactory.ui.jms;

import java.awt.Color;

import at.ac.tuwien.complang.carfactory.businesslogic.jms.JmsPainter;

public class StartUpJmsPainter {
	static int id;
	static Color color;
	
	public static void main(String[] args) {
		parseArguments(args);
		JmsPainter painter = new JmsPainter(id);
		painter.initialize();
		painter.startAssemblyLoop(); //currently the loop needs to be terminated by pressing CTRL+C
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
		String substring = args[1].substring(9).toUpperCase();
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

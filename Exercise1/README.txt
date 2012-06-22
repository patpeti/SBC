README

Names:
 * Sebastian Geiger
 * Peter Patonai

Group: 2

* The whole source is tracked with Git.

* The source has an attached Maven pom.xml, that can be used to import 
  the project into Eclipse and to download the necessary dependencies.
  Additionally there is also a build.xml available, but it has been created
  with Eclipse, therefore it is probably not platform independent.

There are two possible options to start the programs:

Option 1 (Recommended):
   Import the project in Eclipse and run them from there. All main classes
   follow a clear naming schema. They space classes are in:
       - at.ac.tuwien.complang.carfactory.ui.xvsm
   and the alternative implementation classes are in:
       - at.ac.tuwien.complang.carfactory.ui.jms
   each runnable class has StartUp*** in its name and is easy to recognise.
   When you start the class for the first time it will tell you the necessary
   console options and format.

Option 2:
   You can also use the included ant script. But some modifications might
   be necessary. There are ant targets for all relevant classes.

Start sequence:
XVSM:
   The XVSM implementation uses an embedded space which runs inside the GUI.
   You will therefore need to start the XVSM GUI first and then the worker
   classes.

JMS:
   With JMS it is the other way around. First start the MessageQueue with the
   Maven goal: mvn activemq:run (or via Eclipse). Then start each of the workers
   so they can register their durable topic subscriptions. Finally, start the
   JMS GUI.

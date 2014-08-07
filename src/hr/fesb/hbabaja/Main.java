package hr.fesb.hbabaja;

import java.io.IOException;
import java.util.Map;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

public class Main {
	
	
	
		public static void main(String[] args) throws IOException {
			
			Parser parser;
			NetworkTrain training;
			NetworkTest testing;
			System.loadLibrary(Core.NATIVE_LIBRARY_NAME );
				
			if (args.length >= 2) {
				System.out.println("You entered " + args.length + " arguments\n");
				
				parser = new Parser(args);
				
				for (Map.Entry<String, Boolean> entry : Settings.flags.entrySet()) {
					System.out.println(entry.getKey() + "  -  " + entry.getValue());
				}
				
				for (Map.Entry<String, String> entry : Settings.paths.entrySet()) {
					System.out.println(entry.getKey() + "  -  " + entry.getValue());
				}
			}
			else {
				System.out.println("Not enough program arguments.");
				System.exit(1);
			}
			
			if (Settings.flags.get("-train")) {
				training = new NetworkTrain(Settings.paths.get("-train"));		
				training.makeRPropNeuralNetwork();
			}

			if (Settings.flags.get("-test")) {			
				testing = new NetworkTest(Settings.paths.get("-test")); 
				testing.predictOutput();
				testing.printResults();

			}
			
			if (Settings.flags.containsKey("-pic") && Settings.flags.containsKey("-net")) {
//				pokreni metodu za prolaz jedne slike kroz mrezu, dobije se slika kao output
			}
	}
}

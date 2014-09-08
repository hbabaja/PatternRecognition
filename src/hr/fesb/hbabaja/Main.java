package hr.fesb.hbabaja;

import java.io.IOException;
import java.util.Map;

import org.opencv.core.Core;

public class Main {
	
	
	
		public static void main(String[] args) throws IOException {
			
			Parser parser;
			TrainMode training;
			TestMode testing;
			NormalMode normal;
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
				//Preprocessor preproc = new Preprocessor(Settings.paths.get("-train"),2);
				
				training = new TrainMode(Settings.paths.get("-train"), Settings.trainingSampleSize);		
				training.makeRPropNeuralNetwork();
			}

			if (Settings.flags.get("-test")) {	
				//Preprocessor preproc = new Preprocessor(Settings.paths.get("-test"),2);
				
				testing = new TestMode(Settings.paths.get("-test"), Settings.testingSampleSize); 
				testing.predictOutput();
				testing.printResults();
			}
			
			if (Settings.flags.get("-pic")) {
				normal = new NormalMode(Settings.paths.get("-pic"));		
			}
			
//			if (Settings.flags.containsKey("-pic") && Settings.flags.containsKey("-net")) {
//				pokreni metodu za prolaz jedne slike kroz mrezu, dobije se slika kao output
//			}
	}
}
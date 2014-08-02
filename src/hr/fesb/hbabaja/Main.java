package hr.fesb.hbabaja;

import java.io.IOException;
import java.util.Map;

import org.opencv.core.Core;

public class Main {
	
	
	
		public static void main(String[] args) throws IOException {
			
			Parser parser;
			Preprocessor preprocessor;
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
			
			if (Settings.flags.containsKey("-train")) {
				preprocessor = new Preprocessor(Settings.paths.get("-train"));
				preprocessor.generateCSV();
				
			}
			
			if (Settings.flags.containsKey("-test") && Settings.flags.containsKey("-net")) {
				Settings.networkFile = Settings.paths.get("-net");
				
				if (Settings.flags.containsKey("-out")) {
					Settings.testOutputFile = Settings.paths.get("-out");
					System.out.println("Test results saved in " + Settings.testOutputFile);
//					networkTest = new NetworkTest(Settings.paths.get("-test"), Settings.testOutputFile);
				} else {
					System.out.println("Test results saved to default path");
//					networkTest = new NetworkTest(Settings.paths.get("-test"), Settings.testOutputFile);
				}
			}
			
			if (Settings.flags.containsKey("-pic") && Settings.flags.containsKey("-net")) {
//				pokreni metodu za prolaz jedne slike kroz mrezu, dobije se slika kao output
			}
	}
}

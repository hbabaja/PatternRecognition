package hr.fesb.hbabaja;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;

public class Parser {
	
	private String[] arguments;
	
	public Parser (String[] args) {
		arguments = args;
		
		initializeSettings();
		if(!updateSettings()) {
			System.out.println("Parsing has failed");
		}
		else {	
			if(Settings.flags.containsKey("-all")) {
				String allPath = Settings.paths.get("-all");
				initializeSettings();
				if (updateSettingsFromFile(allPath)) {
					System.out.println("Settings updated from parameter file");
				}
				else {
					System.out.println("Errors in parameter file");
				}
			}
		}
	}
	
	private void initializeSettings() {
		for(String flag : Settings.allFlags) {
			Settings.flags.put(flag, false);
			Settings.paths.put(flag, "");
		}
	}
	
	private Boolean updateSettings() {
		int counter = 1;
		String lastFlag = "";
		for(String arg: arguments) {
			if (counter % 2 == 1) {
				if(Settings.flags.containsKey(arg)){
					Settings.flags.put(arg, true);
					lastFlag = arg;
					counter++;
				}
				else {
					System.out.println("Flag doesn't exsist, open help for more information");
					return false;
				}
			}
			else {
				Settings.paths.put(lastFlag,arg);
				counter++;
			}
		}
		return true;
	}
	
	private Boolean updateSettingsFromFile(String path) {
		
		ArrayList<String> records = new ArrayList<String>();
		
		try {
			BufferedReader reader = new BufferedReader(new FileReader(path));
			String line;
			while ((line = reader.readLine()) != null) {
				String[] params = line.split(" ");
				for(String param : params) {
					records.add(param);
				}
			}
			reader.close();
			arguments = records.toArray(new String[0]);
		}
		catch (Exception e){
			System.err.format("Failed to read '%s'", path);
			e.printStackTrace();
			return false;
		}
		
		updateSettings();
		return true;
	}
}

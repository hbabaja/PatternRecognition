package hr.fesb.hbabaja;

import java.util.HashMap;
import java.util.Map;

public class Settings {
	
	public static String[] allFlags = {"-all", "-train", "-test", "-net", "-out", "-pic"};
	
	public static Map<String, Boolean> flags = new HashMap<String, Boolean>();
	public static Map<String, String> paths = new HashMap<String, String>();
	
	public static String networkFile = "stored_network.xml";
	public static int ATTRIBUTES = 30;
	public static int CLASSES = 11;
	
}

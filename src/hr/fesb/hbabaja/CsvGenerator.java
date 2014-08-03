package hr.fesb.hbabaja;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.opencv.core.Mat;
import org.opencv.highgui.Highgui;

public class CsvGenerator {
	
	private String path;
	
	public CsvGenerator(String folderPath) {
		path = folderPath;
		generateCSV();
	}
	
	private void generateCSV() {
		File folder = new File(path);
		if (!folder.isDirectory()) {
			generateCSVfromPicture(folder);
		} else {
			File[] listOfFiles = folder.listFiles();
			for (File file : listOfFiles) {
				generateCSVfromPicture(file);
			}
		}
	}
		
	private void generateCSVfromPicture(File pictureFile){
		String picPath = pictureFile.getAbsolutePath();
		
		Mat pic = Highgui.imread(picPath);
		int rows = pic.rows();
		int cols = pic.cols();
		
		String csvPath = pictureFile.getAbsolutePath() + "\\..\\..\\csvClasses\\" + pictureFile.getName().replaceFirst(".bmp", ".csv");
		
		FileWriter writer;
		try {
			writer = new FileWriter(csvPath);
			
			for (int x = 0; x < rows; x++) {
				for (int y = 0; y < cols; y++) {
					writer.append(String.valueOf(classNumber((int)pic.get(x,y)[0])));
					writer.append(',');
				}
				writer.append('\n');
			}
			
			writer.flush();
			writer.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	

	
	private int classNumber(int classIntensity) {
		int classN = -1;
		
		switch (classIntensity)
		{
			case 23: classN = 0; break;
			case 46: classN = 1; break;
			case 69: classN = 2; break;
			case 92: classN = 3; break;
			case 115: classN = 4; break;
			case 138: classN = 5; break;
			case 161: classN = 6; break;
			case 184: classN = 7; break;
			case 207: classN = 8; break;
			case 230: classN = 9; break;
			case 253: classN = 10; break;
		}
		
		return classN;
	}
	
	
/*	generate picture from generated csv, to check if csv output is good, original and new picture
should be equal */
	
//	private void generatePicFromCsv(int rows, int cols, int type) {
//		Mat pic = new Mat(rows, cols, type);
//		String csvPath = "C:\\prepTrain\\bisevo1.csv";
//		String line ="";
//		BufferedReader br = null;
//		double[] pixel = new double[3];
//		
//		try {
//			br = new BufferedReader(new FileReader(csvPath));
//			int x = 0;
//			int y = 0;
//			while ((line = br.readLine()) != null) {
//				String[] row = line.split(",");
//				y=0;
//				for(String value : row) {
//					pixel[0] = pixel[1] = pixel[2] = getIntensityValue(Integer.valueOf(value));
//					pic.put(x,y,pixel);
//					y++;
//				}
//				x++;
//			}
//			
//			br.close();
//		} catch (FileNotFoundException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		
//		Highgui.imwrite("C:\\prepTrain\\bisevoBackwards.bmp", pic);
//	}
	
//	private double getIntensityValue(int classN) {
//		double intensityValue = 0;
//		
//		switch (classN)
//		{
//			case 0: intensityValue = 23; break;
//			case 1: intensityValue = 46; break;
//			case 2: intensityValue = 69; break;
//			case 3: intensityValue = 92; break;
//			case 4: intensityValue = 115; break;
//			case 5: intensityValue = 138; break;
//			case 6: intensityValue = 161; break;
//			case 7: intensityValue = 184; break;
//			case 8: intensityValue = 207; break;
//			case 9: intensityValue = 230; break;
//			case 10: intensityValue = 253; break;
//		}
//		
//		return intensityValue;
//	}
}

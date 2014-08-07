package hr.fesb.hbabaja;

import java.io.File;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.TermCriteria;
import org.opencv.highgui.Highgui;
import org.opencv.ml.CvANN_MLP;
import org.opencv.ml.CvANN_MLP_TrainParams;

public class NetworkTrain {
	
	private String path;
	protected Mat networkInput = new Mat(0, Settings.ATTRIBUTES, CvType.CV_32F);
	protected Mat networkOutput = new Mat(0, Settings.CLASSES, CvType.CV_32F);
	
	public NetworkTrain(String folderPath) {
		path = folderPath;
		getTrainPictures();
	}
	
	private void getTrainPictures() {
		String picPath = path + "\\pictures\\";
		String gtPath = path + "\\gt_pictures\\";
		
		File folder = new File(picPath);
		
		if (folder.isDirectory()) {
			File[] listOfFiles = folder.listFiles();
			for (File file : listOfFiles) {
				String tempPath = gtPath + file.getName().replaceAll(".jpg|.JPG|.jpeg|.JPEG", ".bmp");
				File gtFile = new File(tempPath);
				if (gtFile.exists()) {
					generateTrainSample(file.getAbsolutePath(),gtFile.getAbsolutePath());
					System.out.println(file.getName() + " added.");
				}
			}
		}
		
	}
	
	private void generateTrainSample(String picPath, String gtPath) {
		Mat pic = Highgui.imread(picPath);
		Mat gtPic = Highgui.imread(gtPath);
		
		int rows = pic.rows();
		int cols = pic.cols();
		
		for (int x=1; x<rows-1; x++) {
			for(int y=1; y<cols-1; y++) {
				int classN = classNumber((int)gtPic.get(x, y)[0]);
				if (classN != -1) {
					addTrainSample(pic, x, y, classN);
				}
			}
		}
	}
	

	private void addTrainSample(Mat pic, int x, int y, int classN) {
		Mat inputElement = Mat.zeros(1, Settings.ATTRIBUTES, CvType.CV_32F);
		Mat outputElement = Mat.zeros(1, Settings.CLASSES, CvType.CV_32F);
		
		outputElement.put(0,classN,1);
		networkOutput.push_back(outputElement);
		
		inputElement.put(0,0, pic.get(x-1,y-1));
		inputElement.put(0,3, pic.get(x-1,y));
		inputElement.put(0,6, pic.get(x,y-1));
		inputElement.put(0,9, pic.get(x,y));
		inputElement.put(0,12, pic.get(x+1,y+1));
		inputElement.put(0,15, pic.get(x,y+1));
		inputElement.put(0,18, pic.get(x+1,y));
		inputElement.put(0,21, pic.get(x-1,y+1));
		inputElement.put(0,24, pic.get(x+1,y-1));
		networkInput.push_back(inputElement);
	}
	
	public void makeBPropNeuralNetwork() {
		Mat layers = new Mat(3, 1, CvType.CV_32S);
		layers.put(0, 0, Settings.ATTRIBUTES);
		layers.put(1, 0, 12);
		layers.put(2, 0, Settings.CLASSES);
		
		CvANN_MLP network = new CvANN_MLP(layers, CvANN_MLP.SIGMOID_SYM, 1, 1);
		
		TermCriteria criteria = new TermCriteria(TermCriteria.COUNT + TermCriteria.EPS, 20000, 0.0000001);
		CvANN_MLP_TrainParams params = new CvANN_MLP_TrainParams();
		params.set_term_crit(criteria);
		params.set_train_method(CvANN_MLP_TrainParams.BACKPROP);
		params.set_bp_dw_scale(0.1);
		params.set_bp_moment_scale(0.1);
		
		System.out.println(networkInput.rows());
		System.out.println(networkOutput.rows());
		
		System.out.println("Starting network train");
		int iters = network.train(networkInput, networkOutput, new Mat(), new Mat(), params, CvANN_MLP.NO_OUTPUT_SCALE);
		System.out.println("Training iterations = " + iters);
		
		Mat tSample = networkInput.row(100);
		Mat classificationResult = Mat.zeros(1, Settings.CLASSES, CvType.CV_32F);
		
		System.out.println("Predicting on:");
		System.out.println(tSample.dump());
		
		network.predict(tSample, classificationResult);
		
		System.out.println("\nResult is:");
		System.out.println(classificationResult.dump());

		network.save(Settings.networkFile);
	}
	
	public void makeRPropNeuralNetwork() {
		Mat layers = new Mat(3, 1, CvType.CV_32S);
		layers.put(0, 0, Settings.ATTRIBUTES);
		layers.put(1, 0, 12);
		layers.put(2, 0, Settings.CLASSES);
		
		CvANN_MLP network = new CvANN_MLP(layers, CvANN_MLP.SIGMOID_SYM, 1, 1);
		
		
		TermCriteria criteria = new TermCriteria(TermCriteria.COUNT + TermCriteria.EPS, 1000, 0.00001);
		CvANN_MLP_TrainParams params = new CvANN_MLP_TrainParams();
		params.set_term_crit(criteria);
		params.set_train_method(CvANN_MLP_TrainParams.RPROP);
		params.set_rp_dw0(0.1);
		params.set_rp_dw_plus(1.2);
		params.set_rp_dw_minus(0.5);
		params.set_rp_dw_max(50);
		
		System.out.println(networkInput.rows());
		System.out.println(networkOutput.rows());
		
		System.out.println("Starting network train");
		int iters = network.train(networkInput, networkOutput, new Mat(), new Mat(), params, CvANN_MLP.NO_OUTPUT_SCALE);
		System.out.println("Training iterations = " + iters);
		
		Mat tSample = networkInput.row(100);
		Mat classificationResult = Mat.zeros(1, Settings.CLASSES, CvType.CV_32F);
		
		System.out.println("Predicting on:");
		System.out.println(tSample.dump());
		
		network.predict(tSample, classificationResult);
		
		System.out.println("\nResult is:");
		System.out.println(classificationResult.dump());

		network.save(Settings.networkFile);
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
	
//
//	private void generateCSV() {
//		File folder = new File(path);
//		if (!folder.isDirectory()) {
//			generateCSVfromPicture(folder);
//		} else {
//			File[] listOfFiles = folder.listFiles();
//			for (File file : listOfFiles) {
//				generateCSVfromPicture(file);
//			}
//		}
//	}
//		
//	private void generateCSVfromPicture(File pictureFile){
//		String picPath = pictureFile.getAbsolutePath();
//		
//		Mat pic = Highgui.imread(picPath);
//		int rows = pic.rows();
//		int cols = pic.cols();
//		
//		String csvPath = pictureFile.getAbsolutePath() + "\\..\\..\\csvClasses\\" + pictureFile.getName().replaceFirst(".bmp", ".csv");
//		
//		FileWriter writer;
//		try {
//			writer = new FileWriter(csvPath);
//			
//			for (int x = 0; x < rows; x++) {
//				for (int y = 0; y < cols; y++) {
//					writer.append(String.valueOf(classNumber((int)pic.get(x,y)[0])));
//					writer.append(',');
//				}
//				writer.append('\n');
//			}
//			
//			writer.flush();
//			writer.close();
//			
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}
//	
	
/*	generate picture from generated csv, to check if csv output is good, original and new picture
should be equal */
//	
//	public static void generatePicFromCsv() {
//		String picPath = "C:\\Diplomski\\slike\\train\\gt_slike\\img_00464.bmp";
//		Mat pic1 = Highgui.imread(picPath);
//		Mat pic = new Mat(pic1.rows(),pic1.cols(),pic1.type());
//		String csvPath = "C:\\Diplomski\\slike\\train\\csvClasses\\img_00464.csv";
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
//		Highgui.imwrite("C:\\img_00464_backwards.bmp", pic);
//	}
//	
//	public static double getIntensityValue(int classN) {
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

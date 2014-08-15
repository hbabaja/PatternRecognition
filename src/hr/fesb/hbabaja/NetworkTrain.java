package hr.fesb.hbabaja;

import java.io.File;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.TermCriteria;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;
import org.opencv.ml.CvANN_MLP;
import org.opencv.ml.CvANN_MLP_TrainParams;

public class NetworkTrain {
	
	private String path;
	
	private int sampleSize;
	private int[] classes = new int[Settings.CLASSES];
	
	protected Mat networkInput = new Mat(0, Settings.ATTRIBUTES, CvType.CV_32F);
	protected Mat networkOutput = new Mat(0, Settings.CLASSES, CvType.CV_32F);
	
	public NetworkTrain(String folderPath, int samples) {
		path = folderPath;
		sampleSize = samples;
		getTrainPictures();
	}
	
	private void getTrainPictures() {
		String picPath = path + "\\pictures\\";
		String gtPath = path + "\\gt_pictures\\";
		String stdevPath = path + "\\stdev_pictures\\";
		
		File folder = new File(picPath);
		
		if (folder.isDirectory()) {
			File[] listOfFiles = folder.listFiles();
			int numberOfFiles = listOfFiles.length;
			
			while (!isGenerated()){
				
				int fileNumber = (int)(Math.random()*(numberOfFiles));
				String tempGtPath = gtPath + listOfFiles[fileNumber].getName().replaceAll(".jpg|.JPG|.jpeg|.JPEG", ".bmp");
				String tempStdevPath = stdevPath + listOfFiles[fileNumber].getName().replaceAll(".jpg|.JPG|.jpeg|.JPEG", ".bmp");
				File gtFile = new File(tempGtPath);
				File stdevFile = new File(tempStdevPath);
				if (gtFile.exists() && stdevFile.exists()) {
					generateRandomTrainSample(listOfFiles[fileNumber].getAbsolutePath(),gtFile.getAbsolutePath(), stdevFile.getAbsolutePath());
					//System.out.println(listOfFiles[fileNumber].getName() + " added");
				}
			}
		}
	}
	
	private void generateRandomTrainSample(String picPath, String gtPath, String stdevPath) {
		Mat pic = Highgui.imread(picPath);
		Mat gtPic = Highgui.imread(gtPath);
		Mat stdevPic = Highgui.imread(stdevPath);
		Mat hsvPic = new Mat();
		Mat labPic = new Mat();
		
		pic.convertTo(labPic, Imgproc.COLOR_BGR2Lab);
		pic.convertTo(hsvPic, Imgproc.COLOR_BGR2HSV);
		
		int sample = 0;
		
		int rows = pic.rows();
		int cols = pic.cols();
		//System.out.println("x = " + rows + "  y = " + cols);
		
		while (sample < 200) {
			int x = (int)(Math.random()*(rows-6));
			int y = (int)(Math.random()*(cols-6));
			
			int classUpperLeft = classNumber((int)gtPic.get(x, y)[0]);
			int classLowerRight = classNumber((int)gtPic.get(x+6, y+6)[0]);
			int classLowerLeft = classNumber((int)gtPic.get(x+6, y)[0]);
			int classUpperRight = classNumber((int)gtPic.get(x, y+6)[0]);
			
			int isSameClass = classUpperLeft + classLowerRight + classLowerLeft + classUpperRight - 4*classUpperLeft;
			
			if ((classUpperLeft != -1) && (isSameClass == 0) && classes[classUpperLeft] <= sampleSize) {
				addTrainSample(pic, hsvPic, labPic, stdevPic, x, y, classUpperLeft);
				classes[classUpperLeft]++;
				}
			sample++;
		}
	}
	

	private void addTrainSample(Mat pic, Mat hsvPic, Mat labPic, Mat stdevPic, int x, int y, int classN) {
		Mat inputElement = Mat.zeros(1, Settings.ATTRIBUTES, CvType.CV_32F);
		Mat outputElement = Mat.zeros(1, Settings.CLASSES, CvType.CV_32F);
		
		outputElement.put(0,classN,1);
		networkOutput.push_back(outputElement);
		
		int n = 0;
		int picChoice = 0;
		for (int i = 0; i < 7; i++) {
			for (int j = 0; j < 7; j++) {
				if (((i%2 == 0) && (j%2 == 0)) || ((i%2 == 1) && (j%2 == 1))) {
					switch (picChoice) {
					case 0:
						inputElement.put(0, n, pic.get(x+i, y+j));
						picChoice = 1;
						break;
					case 1:
						inputElement.put(0, n, hsvPic.get(x+i, y+j));
						picChoice = 2;
						break;
					case 2:
						inputElement.put(0, n, labPic.get(x+i,y+j));
					}
					n = n+3;
				} else {
					inputElement.put(0, n, stdevPic.get(x+i, y+j));
					n = n+1;
				}
			}
		}
		
		networkInput.push_back(inputElement);
	}
	
	private boolean isGenerated() {
		boolean check = true;
		
		for (int i = 0; i < Settings.CLASSES; i++) {
			if (classes[i] < (int)(sampleSize*0.8)) {
				check = false;
			}
		}
		
		return check;	
	}
	
	
	public void makeRPropNeuralNetwork() {
		Mat layers = new Mat(3, 1, CvType.CV_32S);
		layers.put(0, 0, Settings.ATTRIBUTES);
		layers.put(1, 0, 150);
		layers.put(2, 0, Settings.CLASSES);
		
		CvANN_MLP network = new CvANN_MLP(layers, CvANN_MLP.SIGMOID_SYM, 1, 1);
		
		TermCriteria criteria = new TermCriteria(TermCriteria.COUNT + TermCriteria.EPS, 4000, 0.000001);
		//TermCriteria criteria = new TermCriteria(TermCriteria.COUNT, 1000, 0.000001);
		CvANN_MLP_TrainParams params = new CvANN_MLP_TrainParams();
		params.set_term_crit(criteria);
		params.set_train_method(CvANN_MLP_TrainParams.RPROP);
		params.set_rp_dw0(0.1);
		params.set_rp_dw_plus(1.2);
		params.set_rp_dw_minus(0.5);
		params.set_rp_dw_max(50);
		
		System.out.println(networkInput.rows());
		System.out.println(networkOutput.rows());
		
		showObtainedSamples();
		
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
	
	private void showObtainedSamples() {
		for (int i = 0; i < Settings.CLASSES; i++) {
			System.out.println("Class " + i + " = " + classes[i] + " samples");
		}
	}
	
//	private void generateTrainSample(String picPath, String gtPath) {
//		Mat pic = Highgui.imread(picPath);
//		Mat gtPic = Highgui.imread(gtPath);
//		Mat hsvPic = new Mat();
//		pic.convertTo(hsvPic, Imgproc.COLOR_BGR2HSV);
//		
//		int rows = pic.rows();
//		int cols = pic.cols();
//		
//		for (int x=1; x<rows-1; x++) {
//			for(int y=1; y<cols-1; y++) {
//				int classN = classNumber((int)gtPic.get(x, y)[0]);
//				if (classN != -1) {
//					addTrainSample(pic, hsvPic, x, y, classN);
//				}
//			}
//		}
//	}
//
//	public void makeBPropNeuralNetwork() {
//		Mat layers = new Mat(3, 1, CvType.CV_32S);
//		layers.put(0, 0, Settings.ATTRIBUTES);
//		layers.put(1, 0, 54);
//		layers.put(2, 0, Settings.CLASSES);
//		
//		CvANN_MLP network = new CvANN_MLP(layers, CvANN_MLP.SIGMOID_SYM, 0.6, 1);
//		
//		TermCriteria criteria = new TermCriteria(TermCriteria.COUNT + TermCriteria.EPS, 1000, 0.0000001);
//		CvANN_MLP_TrainParams params = new CvANN_MLP_TrainParams();
//		params.set_term_crit(criteria);
//		params.set_train_method(CvANN_MLP_TrainParams.BACKPROP);
//		params.set_bp_dw_scale(0.05);
//		params.set_bp_moment_scale(0.05);
//		
//		showObtainedSamples();
//		
//		System.out.println("Starting network train");
//		int iters = network.train(networkInput, networkOutput, new Mat(), new Mat(), params, CvANN_MLP.NO_OUTPUT_SCALE);
//		System.out.println("Training iterations = " + iters);
//		
//		Mat tSample = networkInput.row(100);
//		Mat classificationResult = Mat.zeros(1, Settings.CLASSES, CvType.CV_32F);
//		
//		System.out.println("Predicting on:");
//		System.out.println(tSample.dump());
//		
//		network.predict(tSample, classificationResult);
//		
//		System.out.println("\nResult is:");
//		System.out.println(classificationResult.dump());
//
//		network.save(Settings.networkFile);
//	}
//	
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
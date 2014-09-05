package hr.fesb.hbabaja;

import java.io.File;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;
import org.opencv.ml.CvANN_MLP;

public class NormalMode extends TrainMode {
	
	private String path = "";
	private String outputPath = "";
	private CvANN_MLP network = new CvANN_MLP();
	
	public NormalMode(String folderPath) {
		super();
		
		path = folderPath;

		if(Settings.flags.get("-net")) {
			Settings.networkFile = Settings.paths.get("-net");
		}
		network.load(Settings.networkFile);
		getPictures();
	}
	
	private void getPictures() {
		File folder = new File(path);
		
		if (folder.isDirectory()) {
			File[] listOfFiles = folder.listFiles();
			
			for (File file : listOfFiles) {
				if(Settings.flags.get("-out")) {
					outputPath = Settings.paths.get("-out");
				}
				else {
					outputPath = folder.getAbsolutePath();
				}
				runPicThroughNetwork(file.getAbsolutePath(), file.getName());
			}
		}
		else {
			if(Settings.flags.get("-out")) {
				outputPath = Settings.paths.get("-out");
			}
			else {
				outputPath = folder.getParent();
			}
			runPicThroughNetwork(folder.getAbsolutePath(), folder.getName());
		}
	}
	
	private void runPicThroughNetwork(String picPath, String picName) {
		
		int[] foundClasses = new int[Settings.CLASSES];
		
		Mat pic = Highgui.imread(picPath);
		Mat outputPic = pic;
		
		Preprocessor preprocessor = new Preprocessor();
		Mat stdevPic = preprocessor.makeStDevImage(picPath, "", false);
		Mat clusterPic = preprocessor.makeClusteredImage(picPath, "", false);
		
		Mat hsvPic = new Mat();
		Mat labPic = new Mat();
		Imgproc.cvtColor(pic, hsvPic, Imgproc.COLOR_BGR2HSV);
		Imgproc.cvtColor(pic, labPic, Imgproc.COLOR_BGR2Lab);
		
		
		int rows = pic.rows();
		int cols = pic.cols();
		
		for (int x = 0; x < rows - 7; x = x + 7) {
			for (int y = 0; y < cols - 7; y = y + 7) {
				Mat inputElement = Mat.zeros(1, Settings.ATTRIBUTES, CvType.CV_32F);
				inputElement = super.getTrainSampleInput(pic, hsvPic, labPic, stdevPic, clusterPic, x, y);
				
				int sampleClass = predictOnSample(inputElement);
				foundClasses[sampleClass]++;

				makeOutputPicture(outputPic, sampleClass, x, y);
			}
		}
		
		printFoundClasses(picPath, foundClasses);
		saveOutputPicture(outputPic, picName);
		
	}
	
	private int predictOnSample(Mat sample) {
		Mat classificationResult = new Mat(1,Settings.CLASSES,CvType.CV_32F);
		network.predict(sample, classificationResult);
		
		int maxIndex = 0;
		double value = 0;
		double maxValue = classificationResult.get(0, 0)[0];
		
		for (int index = 1; index < Settings.CLASSES; index++) {
			value = classificationResult.get(0, index)[0];
			
			if (value > maxValue) {
				maxValue = value;
				maxIndex = index;
			}
		}
		
		return maxIndex;
	}

	private void makeOutputPicture(Mat outputPic, int classN, int x, int y) {
		double[] pixel = new double[3];
		double intensity = super.getIntensityValue(classN);
		System.out.println(intensity);
		
		pixel[0] = intensity;
		pixel[1] = intensity;
		pixel[2] = intensity;
		
		for (int i = x; i<x+7; i++) {
			for (int j = y; j<y+7; j++) {
				outputPic.put(i, j, pixel);
			}
		}
	}
	
	private void printFoundClasses(String picPath, int[] classes) {
		System.out.println("Found classes for picture: " + picPath);
		
		for(int i = 0; i < Settings.CLASSES; i++) {
			System.out.println("Class " + i + ": " + classes[i]);
		}
	}
	
	private void saveOutputPicture(Mat outputPic, String picName) {
		String name = picName.replaceAll(".jpg|.JPG|.jpeg|.JPEG|.png|.PNG|.bmp|.BMP", "");
		String savePath = outputPath + "\\" + name + "_output.bmp";

		System.out.println("Saving output picture to: " + savePath);	
		Highgui.imwrite(savePath, outputPic);
	}
}















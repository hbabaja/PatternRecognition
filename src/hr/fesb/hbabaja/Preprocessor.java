package hr.fesb.hbabaja;

import java.io.File;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.core.TermCriteria;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

public class Preprocessor {
	
	private String path;
	private int preprocChoice = 0; // 1- stdev, 2- kmeans clustering
	
	public Preprocessor() {
		
	}
	
	public Preprocessor(String tPath, int procChoice) {
		path = tPath;
		preprocChoice = procChoice;
		getImages();
	}
	
	private void getImages() {
		String picPath = path + "\\pictures\\";
		String saveFolder = "";
		
		if (preprocChoice == 1) {
			saveFolder = "\\stdev_pictures\\";
		}
		if (preprocChoice == 2) {
			saveFolder = "\\clustered_pictures\\";
		}
		
		File folder = new File(picPath);
		
		if(folder.isDirectory()) {
			File[] listOfFiles = folder.listFiles();
			
			for (File file : listOfFiles) {
				String stdevPath = path + saveFolder + file.getName().replaceAll(".jpg|.JPG|.jpeg|.JPEG", ".bmp");
				if (preprocChoice == 1) {
					makeStDevImage(file.getAbsolutePath(), stdevPath, true);
				}
				if (preprocChoice == 2) {
					makeClusteredImage(file.getAbsolutePath(), stdevPath, true);
				}
				System.out.println(file.getName() + " added");
			}
		} 
		else {
			if (preprocChoice == 1) {
				makeStDevImage(folder.getAbsolutePath(), "", false);
			}
			if (preprocChoice == 2) {
				makeClusteredImage(folder.getAbsolutePath(), "", false);
			}
		}
	}
	
	public Mat makeStDevImage(String picPath, String stdevPath, boolean saving) {
		Mat pic = Highgui.imread(picPath, 0);
		
		Mat image32f = new Mat();
		pic.convertTo(image32f, CvType.CV_32F);
		
		Mat mu = new Mat();
		Imgproc.blur(image32f, mu, new Size(7,7));
		
		Mat mu2 = new Mat();
		Imgproc.blur(image32f.mul(image32f), mu2, new Size(7,7));
		
		Mat sigma = new Mat();
		Mat result = new Mat();
		
		Core.subtract(mu2, mu.mul(mu), result);
		Core.sqrt(result, sigma);
		Core.normalize(sigma, sigma, 0, 255, Core.NORM_MINMAX);
		
		if (saving == true) {
			Highgui.imwrite(stdevPath, sigma);
		}
		
		return sigma;
	}
	
	public Mat makeClusteredImage(String picPath, String clusterPath, boolean saving) {
		Mat pic = Highgui.imread(picPath);
		
		Mat samples = new Mat(pic.rows()*pic.cols(), 3, CvType.CV_32F);
		
		for (int y = 0; y < pic.rows(); y++)
			for (int x = 0; x < pic.cols(); x++)
				for (int z = 0; z < 3; z++) {
					samples.put(y + x*pic.rows(), z, pic.get(y, x)[z]);
				}
		
		int clusterCount = 11;
		Mat labels = new Mat();
		int attempts = 1;
		Mat centers = new Mat();
		
		Core.kmeans(samples, clusterCount, labels, new TermCriteria(TermCriteria.MAX_ITER + TermCriteria.EPS, 500, 0.01), attempts, Core.KMEANS_PP_CENTERS, centers);
		
		Mat newPic = new Mat(pic.size(), pic.type());
		
		for (int y = 0; y < pic.rows(); y++) {
			for (int x = 0; x < pic.cols(); x++) {
				int cluster_idx = (int)labels.get(y + x*pic.rows(), 0)[0];
				double[] pixel = new double[3];
				pixel[0] = centers.get(cluster_idx, 0)[0];
				pixel[1] = centers.get(cluster_idx, 1)[0];
				pixel[2] = centers.get(cluster_idx, 2)[0];
				newPic.put(y, x, pixel);
			}
		}
		
		if (saving == true) {
			Highgui.imwrite(clusterPath, newPic);
		}
		return newPic;
	}
	
//	public static void main(String[] args) {	
//		System.loadLibrary(Core.NATIVE_LIBRARY_NAME );
//				
//		String path = "C:\\Diplomski\\trying\\train\\pictures\\img_00168.jpg";
//		
//		Mat src = Highgui.imread(path);
//		
//		Mat samples = new Mat(src.rows()*src.cols(), 3, CvType.CV_32F);
//		
//		for (int y = 0; y < src.rows(); y++)
//			for (int x = 0; x < src.cols(); x++)
//				for (int z = 0; z < 3; z++) {
//					samples.put(y + x*src.rows(), z, src.get(y, x)[z]);
//				}
//		
//		int clusterCount = 10;
//		Mat labels = new Mat();
//		int attempts = 1;
//		Mat centers = new Mat();
//		
//		Core.kmeans(samples, clusterCount, labels, new TermCriteria(TermCriteria.MAX_ITER + TermCriteria.EPS, 1000, 0.01), attempts, Core.KMEANS_PP_CENTERS, centers);
//		
//		Mat new_image = new Mat(src.size(), src.type());
//		
//		for (int y = 0; y < src.rows(); y++)
//			for (int x = 0; x < src.cols(); x++) {
//				int cluster_idx = (int)labels.get(y + x*src.rows(), 0)[0];
//				double[] mm = new double[3];
//				mm[0] = centers.get(cluster_idx, 0)[0];
//				mm[1] = centers.get(cluster_idx, 1)[0];
//				mm[2] = centers.get(cluster_idx, 2)[0];
//				new_image.put(y, x, mm);
//			}
//		Mat grayImg = new Mat();
//		
//		Imgproc.cvtColor(new_image, grayImg, Imgproc.COLOR_BGR2GRAY);
//		
//		Highgui.imwrite("C:\\Diplomski\\test.jpg", grayImg);
//		System.out.println("Done");
//	}
}
package hr.fesb.hbabaja;

import java.io.File;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

public class Preprocessor {
	
	private String path;
	
	public Preprocessor(String tPath) {
		path = tPath;
		getStDevImages();
	}
	
	private void getStDevImages() {
		String picPath = path + "\\pictures\\";
		
		File folder = new File(picPath);
		
		if(folder.isDirectory()) {
			File[] listOfFiles = folder.listFiles();
			
			for (File file : listOfFiles) {
				String stdevPath = path + "\\stdev_pictures\\" + file.getName().replaceAll(".jpg|.JPG|.jpeg|.JPEG", ".bmp");
				makeStDevImage(file.getAbsolutePath(), stdevPath);
				System.out.println(file.getName() + " added");
			}
		}
	}
	
	public void makeStDevImage(String picPath, String stdevPath) {
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
		
		Highgui.imwrite(stdevPath, sigma);	
	}
}

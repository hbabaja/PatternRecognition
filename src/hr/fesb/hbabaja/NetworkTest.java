// classes
// [0] -- smoke
// [1] -- clouds or fog
// [2] -- sun or light effects
// [3] -- sky
// [4] -- sea
// [5] -- distant landscape
// [6] -- rocks
// [7] -- distant vegetation
// [8] -- close vegetation
// [9] -- low vegetation and agricultural areas
// [10] -- buildings and artificial objects

package hr.fesb.hbabaja;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.ml.CvANN_MLP;

public class NetworkTest extends NetworkTrain{
	
	private int[][] results = new int[11][2];
	private CvANN_MLP network = new CvANN_MLP();

	public NetworkTest(String folderPath, int sampleSize) {
		super(folderPath, sampleSize);
	}
	
	public void predictOutput() {	
		network.load(Settings.networkFile);
		
		for (int ntestSample = 0; ntestSample < networkInput.rows(); ntestSample++) {
			Mat testSample = networkInput.row(ntestSample);
			Mat classificationResult = new Mat(1,Settings.CLASSES,CvType.CV_32F);
			
			//System.out.println(testSample.dump());
			
			network.predict(testSample, classificationResult);
			
			//System.out.println(classificationResult.dump());
			
			int maxIndex = 0;
			double value = 0;
			double maxValue= classificationResult.get(0, 0)[0];
			
			for (int index = 1; index < Settings.CLASSES; index++) {
				value = classificationResult.get(0, index)[0];
				
				if (value > maxValue) {
					maxValue = value;
					maxIndex = index;
				}
			}

			if (networkOutput.get(ntestSample, maxIndex)[0] != 1) {
				
				for (int classIndex = 0; classIndex < Settings.CLASSES; classIndex++) {
					if (networkOutput.get(ntestSample, classIndex)[0] == 1) {
						results[classIndex][1]++;
					}
				}
			} else {
				results[maxIndex][0]++;
				results[maxIndex][1]++;
			}
		}
	}
	
	public void printResults() {
		double sumCorrect=1;
		double sumAll = 1;
		for (int i = 0; i < Settings.CLASSES; i++) {
			System.out.println("Class " + i + ", correct = " + results[i][0] + "  , total = " + results[i][1]);
			sumCorrect += results[i][0];
			sumAll += results[i][1];
		}
		System.out.println(100*(sumCorrect/sumAll) + "%");
	}
	

}
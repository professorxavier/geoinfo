package br.com.fernandoxavier.processing;


import weka.classifiers.Evaluation;
import weka.classifiers.trees.M5P;
import weka.core.Instances;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader ;
import java.io.ObjectInputStream;

/**
 * Apply specific model in a test dataset
 * Used in all arff files
 * Remember to generate arff and model files first (should see the ConvertUtils class)
 * @author fernando
 *
 */
public class ReapplyModel {

	public static final String DATA = "C:\\Users\\fernando\\Desktop\\rbc\\datasets\\arff\\";
	public static final String MODELS = "C:\\Users\\fernando\\Desktop\\rbc\\datasets\\models\\";
	public static final String HOME = "C:\\Users\\fernando\\Desktop\\rbc\\";

	/**
	 * Read all arff files and apply corresponding ML model
	 * Output station name and correlation coefficient to console
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args)  throws Exception {
		// training
		BufferedReader reader = null;
		BufferedReader br = null;
		br = new BufferedReader(new FileReader(HOME+"stations.csv"));
		String line = br.readLine();
		
		while (line != null ) {
			line = br.readLine();
			String[] campos = line.split(",");
			String datafile = campos[0]+".arff";
			String modelfileName = getModelFile(campos[1].toLowerCase());
			
			File f = new File(DATA + datafile);
			if (!f.exists()) {
				System.out.println(campos[0]+"," + 0);
				continue;
			}
			
			reader = new BufferedReader(new FileReader(DATA + datafile));
			Instances teste = new Instances (reader);
			teste.setClassIndex(2);     
			reader.close();
			File modelfile = new File(MODELS + modelfileName);
			double corr = getCorr(teste, modelfile);
			System.out.println(campos[0]+"," + corr);
		}
		
	}

	/**
	 * According to file configuration, returns the specific model file
	 * @param refer
	 * @return model filename
	 */
	private static String getModelFile(String refer) {
		String name = "";
		
		switch (refer) {
		case "manaus":
			name = "am_manaus.arff.model";
			break;
		case "paracatu":
			name = "mg_paracatu.arff.model";
			break;
		case "natal":
			name = "rn_natal.arff.model";
			break;
		case "caceres":
			name = "mt_caceres.arff.model";
			break;
		case "petrolina":
			name = "pe_petrolina.arff.model";
			break;
		case "portoalegre":
			name = "rs_portoalegre.arff.model";
			break;

		default:
			break;
		}
		
		return name;
	}

	/**
	 * Apply model in instances set
	 * @param testSet Instances for testing
	 * @param modelFile Model generated from reference station
	 * @return Correlation coefficient
	 * @throws Exception
	 */
	private static double getCorr(Instances testSet, File modelFile) throws Exception {

		ObjectInputStream ois = new ObjectInputStream(new FileInputStream(modelFile));
		M5P m5p =  (M5P) ois.readObject();
		ois.close();

		Evaluation eval = new Evaluation(testSet);
		eval.evaluateModel(m5p, testSet);

		double corr = eval.correlationCoefficient();
		return corr;
	}
	
}

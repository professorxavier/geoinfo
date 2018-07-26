package br.com.fernandoxavier.utils;

import weka.classifiers.Evaluation;
import weka.classifiers.trees.M5P;
import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.core.converters.CSVLoader;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Random;

/**
 * Utils methods
 * @author fernando
 *
 */

public class ConvertUtils {

	public static final String ARFF_FILES = "C:\\Users\\fernando\\Desktop\\rbc\\datasets\\arff\\";

	public static void main(String[] args) throws Exception {

	}


	private static void generateModels() throws Exception {

		File folder = new File(ARFF_FILES);
		File[] listOfFiles = folder.listFiles();

		for (File file : listOfFiles) {
			if (file.isFile()) {
				String str = file.getAbsolutePath();
				System.out.println(str);
				String name = file.getName();
				System.out.println(name);


				// training
				BufferedReader reader = null;
				reader=new BufferedReader(new FileReader(str));
				Instances train = new Instances (reader);
				train.setClassIndex(2);     
				reader.close();

				M5P m5p = new M5P();
				m5p.buildClassifier(train);
				Evaluation eval = new Evaluation(train);
				eval.crossValidateModel(m5p, train, 10 , new Random(1));
				weka.core.SerializationHelper.write(name+".model", m5p);

				System.out.println(eval.toSummaryString("\n Results \n=====\n",true));
			}
		}



	}

	/**
	 * Transform csv in arff file
	 * Addictionaly, can be exclude some attributes
	 * @param origin complete path ot csv file
	 * @param name Name for output file
	 * @param exclude Array of indices for excluding from dataset
	 * @throws Exception
	 */
	public static void generateArff(String origin, String name, int[] exclude) throws Exception {

		CSVLoader loader = new CSVLoader();
		loader.setSource(new File(origin));
		Instances data = loader.getDataSet();

		int[] indices = {4,5,7,9,10,12,15,16,17,18}; //TODO REMOVE ME

		Remove removeFilter = new Remove();
		removeFilter.setAttributeIndicesArray(exclude);
		removeFilter.setInvertSelection(true);
		removeFilter.setInputFormat(data);
		Instances newData = Filter.useFilter(data, removeFilter);

		String filename = name+".arff";

		ArffSaver saver = new ArffSaver();
		String dest = ARFF_FILES + filename;
		saver.setInstances(newData);
		File f = new File(dest);
		saver.setFile(f);
		saver.writeBatch();

	}

}

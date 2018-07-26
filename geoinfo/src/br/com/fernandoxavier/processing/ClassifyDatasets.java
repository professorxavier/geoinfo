package br.com.fernandoxavier.processing;

import java.io.File;
import java.util.ArrayList;
import java.util.Random;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.trees.M5P;
import weka.classifiers.trees.m5.RuleNode;
import weka.core.Instances;
import weka.core.converters.CSVLoader;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;

/**
 * Class used only for local testing
 * @author fernando
 *
 */
public class ClassifyDatasets {


	static String attributesIndexes = "5,6,7,8,9,10,11,12,13,14,15,16,17,18,19";
	static ArrayList<String> stationsIgnored = new ArrayList<String>();

	// For local testing
	public static void main(String[] args) throws Exception {

		stationsIgnored.add("ba_feiradesantana.csv");
		stationsIgnored.add("df_roncador.csv");
		stationsIgnored.add("go_goiania.csv");
		stationsIgnored.add("mg_bomdespacho.csv");
		stationsIgnored.add("mg_caldaspdecaldas.csv");
		stationsIgnored.add("mg_carbonita.csv");
		stationsIgnored.add("mg_ibirite.csv");
		stationsIgnored.add("mg_juramento.csv");
		stationsIgnored.add("mg_lambari.csv");
		stationsIgnored.add("mg_saosdoparaiso.csv");
		stationsIgnored.add("ms_nhumirimnhecolandia.csv");
		stationsIgnored.add("rj_riodejaneiro.csv");
		stationsIgnored.add("rs_bentogoncalves.csv");
		stationsIgnored.add("sc_urussanga.csv");
		stationsIgnored.add("sp_sorocaba.csv");		

		String header = "Estacao,Data,Hora,DirecaoVento,VelocidadeVentoMedia,VelocidadeVentoMaximaMedia,EvaporacaoPiche,EvapoBHPotencial,EvapoBHReal,InsolacaoTotal,NebulosidadeMedia,NumDiasPrecipitacao,PrecipitacaoTotal,PressaoNivelMarMedia,PressaoMedia,TempMaximaMedia,TempCompensadaMedia,TempMinimaMedia,UmidadeRelativaMedia,VisibilidadeMedia,nullcolumn";

		Instances       instNew;
		File f = new File("./data/cleaned/am_manaus.csv");
		CSVLoader cnv = new CSVLoader();
		cnv.setSource(f);
		Instances inst = cnv.getDataSet();

		Remove remove = new Remove();
		remove.setAttributeIndices(attributesIndexes);
		remove.setInvertSelection(true); 
		remove.setInputFormat(inst);
		instNew = Filter.useFilter(inst, remove);

		instNew.setClassIndex(3); 

		M5P tree = new M5P();        		
		tree.buildClassifier(instNew);  
		Evaluation eval = new Evaluation(instNew);
		eval.crossValidateModel(tree, instNew, 10, new Random(1));

		RuleNode rules = tree.getM5RootNode();
		rules.printAllModels();

		System.out.println("Correlation is " + eval.correlationCoefficient());

		weka.core.SerializationHelper.write("./data/models/cruzeiroM5P.model", tree);

		File dir = new File("./data/cleaned");

		for (String file: dir.list()) {

			if (!stationsIgnored.contains(file)) {
				try {
					applyModel(file);
					break;
				} catch (Exception e) {
					System.out.println("erro em " + file);
				}
			}

		}

	}

	/**
	 * Apply model in a test dataset and output results
	 * @param filename file with test dataset
	 * @throws Exception
	 */
	private static void applyModel(String filename) throws Exception {

		Instances instances = getInstances(filename);
		Classifier cls = (Classifier) weka.core.SerializationHelper.read("./data/models/cruzeiroM5P.model");
		cls.buildClassifier(instances);
		Evaluation eval = new Evaluation(instances);
		eval.crossValidateModel(cls, instances, 10, new Random(1));
		RuleNode rules = ((M5P)cls).getM5RootNode();
		rules.printAllModels();
		System.out.println(filename + ";" + eval.correlationCoefficient());
		
	}

	/**
	 * From csv file, filter attributes
	 * @param filename
	 * @return instances set
	 * @throws Exception
	 */
	private static Instances getInstances(String filename) throws Exception {
		Instances       instNew;
		File f = new File("./data/cleaned/" + filename);
		CSVLoader cnv = new CSVLoader();
		cnv.setSource(f);
		Instances inst = cnv.getDataSet();

		//removing attributes
		Remove remove = new Remove();
		remove.setAttributeIndices(attributesIndexes);
		remove.setInvertSelection(true); 
		remove.setInputFormat(inst);
		instNew = Filter.useFilter(inst, remove);

		instNew.setClassIndex(3); 
		return instNew;
	}

}

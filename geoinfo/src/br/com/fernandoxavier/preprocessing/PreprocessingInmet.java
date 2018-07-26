package br.com.fernandoxavier.preprocessing;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;

import br.com.fernandoxavier.model.StationVO;

/**
 * Class used for preprocessing tasks 
 * Adapted for INMET dataset
 * @author fernando
 *
 */
public class PreprocessingInmet {

	public static int[] remove = {};
	public static final String DATASET_FOLDER="./bin/data";

	public static void main(String[] args) {

		//local test
		loadFile(DATASET_FOLDER, "82024.txt.csv");
	}

	/**
	 * Read raw data file and generate a stationVO
	 * @param path Location of csv file
	 * @param filename
	 * @return VO for station
	 */
	public static StationVO loadFile(String path, String filename) {

		BufferedReader br = null;
		String line;
		StationVO station = new StationVO();
		
		try {
			br = new BufferedReader(new FileReader(path+"\\"+filename));
			String[] fileNameParts = filename.split("\\.");
			String wmoCode = fileNameParts[0];
			station.setStationName(filename.replace(".csv",""));
			
			//adding metadata to VO
			for (int i=1;i<17;i++) {
				line = br.readLine();
				if (i==3) {
					String[] campos = line.split(":");
					campos = campos[1].split(" - ");
					String[] estado = campos[1].split("\\(");
					station.setStationName((estado[0].trim().toLowerCase()+"_"+campos[0].trim().replace(" ", "").toLowerCase()).trim());
				}
				
				if (i==16) {
					String[] columns = line.split(";",-1);
					for (int indexRemove: remove) {
						columns = removeElement(columns,indexRemove);
					}
					station.setHeader(getCleanedArray(columns));
				}
			}
			
			//adding instances to VO
			while ((line = br.readLine()) != null) {
				if (!line.trim().equals("")) {
					if (line.startsWith(wmoCode)) {
						String[] columns = line.split(";",-1);
						for (int indexRemove: remove) {
							columns = removeElement(columns, indexRemove);
						}
						station.addInstance(getCleanedArray(columns));
					}
				}

			}
			saveStationData(station);

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		return station;
	}

	/**
	 * Store metadata station from csv file
	 * @param path Location of csv file
	 * @param filename
	 * @return VO for station
	 */
	public static StationVO collectMeta(String path, String filename) {

		BufferedReader br = null;
		String line;
		StationVO station = new StationVO();
		try {
			br = new BufferedReader(new FileReader(path+"\\"+filename));
			station.setStationDataset(filename.replace(".csv",""));
			for (int i=1;i<17;i++) {
				line = br.readLine();
				if (i==3) {
					String[] campos = line.split(":");
					campos = campos[1].split(" - ");
					String[] estado = campos[1].split("\\(");
					station.setStationName(campos[0].trim());
					String[] code = line.split(":");
					station.setOMMCode(code[code.length-1].trim().replace(")",""));
					station.setStationDataset((estado[0].trim().toLowerCase()+"_"+campos[0].trim().replace(" ", "").toLowerCase()).trim());
				}
				if (i==4) {
					station.setLatitude(Double.parseDouble(line.replace("Latitude  (graus) : ", "")));

				}
				if (i==5) {
					station.setLongitude(Double.parseDouble(line.replace("Longitude (graus) : ", "")));

				}
				if (i==6) {
					station.setAltitude(Double.parseDouble(line.replace("Altitude  (metros): ", "")));

				}
				if (i==16) {
					break;
				}

			}
			saveStationMetadata(station);

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		return station;
	}

	/**
	 * Clean data array
	 * @param columns
	 * @return cleaned array
	 */
	public static String getCleanedArray(String[] columns) {
		String[] array = columns;
		String arrayStr = Arrays.toString(array).replaceAll("]", "").replaceAll("\\[", "");
		arrayStr = arrayStr.substring(0, arrayStr.length()-2);
		return arrayStr;
	}

	/**
	 * Output station data in a file
	 * @param station data
	 * @throws FileNotFoundException
	 * @throws UnsupportedEncodingException
	 */
	private static void saveStationData(StationVO station) throws FileNotFoundException, UnsupportedEncodingException {

		File directory = new File("./data/cleaned/");
		if (!directory.exists()) {
			if (directory.mkdirs()) {
				System.out.println("Directory is created!");
			} else {
				System.out.println("Failed to create directory!");

			}
		}
		File file = new File(directory.getPath() + "/" + station.getStationName()+".csv");

		PrintWriter writer = new PrintWriter(file, "UTF-8");
		writer.println(station.getHeader().replace(";", ","));
		for(String line: station.getInstances()) {
			writer.println(line.replace(";", ",")); //weka only reads comma
		}
		writer.close();

	}

	/**
	 * Output station metadata in a file
	 * @param station data
	 * @throws FileNotFoundException
	 * @throws UnsupportedEncodingException
	 */
	private static void saveStationMetadata(StationVO station) throws IOException {

		File directory = new File("./data/meta");
		if (!directory.exists()) {
			if (directory.mkdirs()) {
				System.out.println("Directory is created!");
			} else {
				System.out.println("Failed to create directory!");

			}
		}

		String meta=station.getOMMcode() + "," + station.getStationDataset() + "," + station.getStationName()+","
				+station.getLatitude()+","+station.getLongitude()+","+
				station.getAltitude();
		
		String insertMeta = "INSERT INTO stations (id, dataset, description, lat, long, alt) VALUES (" +
				station.getOMMcode() + ",'" + station.getStationDataset() + "','" + station.getStationName()+"',"
				+station.getLatitude()+","+station.getLongitude()+","+
				station.getAltitude()+ ")";
		System.out.println(insertMeta);

		FileWriter fw = new FileWriter(directory.getPath() + "/stationMeta.csv", true);
		BufferedWriter bw = new BufferedWriter(fw);
		PrintWriter out = new PrintWriter(bw);
		out.println(meta);
		out.close();
	}

	/**
	 * Remove attribute from instance
	 * @param original data array
	 * @param element index to remove
	 * @return cleaned array
	 */
	public static String[] removeElement(String[] original, int element){

		String[] n = new String[original.length - 1];
		System.arraycopy(original, 0, n, 0, element );
		System.arraycopy(original, element+1, n, element, original.length - element-1);
		return n;
	}

}

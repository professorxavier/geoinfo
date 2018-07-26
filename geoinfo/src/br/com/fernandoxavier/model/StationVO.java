package br.com.fernandoxavier.model;

import java.util.Date;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 * VO class for station
 * @author fernando
 *
 */
public class StationVO {

	String stationName = "";
	String stationDataset = "";
	String OMMcode;
	String city = "";
	String state = "";
	double latitude;
	double longitude;
	double altitude;
	String header="";
	double coefficient=0;
	ArrayList<String> instances= new ArrayList<String>();
	private double mae;
	private double rmse;
	private String numInstances;
	
	public String getOMMcode() {
		return OMMcode;
	}

	public void setOMMCode(String oMMcode) {
		OMMcode = oMMcode;
	}

	public String getStationDataset() {
		return stationDataset;
	}

	public void setStationDataset(String stationDataset) {
		this.stationDataset = stationDataset;
	}



	public double getCoefficient() {
		return coefficient;
	}

	public void setCoefficient(double coefficient) {
		this.coefficient = coefficient;
	}

	public String getStationName() {
		return stationName;
	}

	public void setStationName(String stationName) {
		this.stationName = stationName;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public double getAltitude() {
		return altitude;
	}

	public void setAltitude(double altitude) {
		this.altitude = altitude;
	}

	public String getHeader() {
		return header;
	}

	public void setHeader(String header) {
		this.header = header;
	}

	public void addInstance (String instance) {
		this.instances.add(instance);
	}

	public ArrayList<String> getInstances() {
		return instances;
	}

	public void setInstances(ArrayList<String> instances) {
		this.instances = instances;
	}

	@Override
	public String toString() {
		return getOMMcode() + " - " + getStationName() + " - " + getState() + " - "
				+ getCity() + " - " + getLatitude() + " - "
				+ getLongitude() + " - " + getAltitude();
	}

	public void setMae(double mae) {
		this.mae = mae;
	}

	public void setRmse(double rmse) {
		this.rmse = rmse;
	}

	public double getMae() {
		return mae;
	}

	public double getRmse() {
		return rmse;
	}

	public String getNumInstances() {
		return numInstances;
	}

	/**
	 * generate SQL insert from station data and output in a file
	 * @param sqlFile name
	 * @throws ParseException
	 */
	public void generateInserts(File sqlFile) throws ParseException {

		for (String instance: this.instances) {
			String[] data = instance.split(",");
			String sql = "INSERT INTO data (station,dateMeasure,timeMeasure,DirecaoVento,"
					+ "VelocidadeVentoMedia,VelocidadeVentoMaximaMedia,EvaporacaoPiche,EvapoBHPotencial,"
					+ "EvapoBHReal,InsolacaoTotal,NebulosidadeMedia,NumDiasPrecipitacao,PrecipitacaoTotal,"
					+ "PressaoNivelMarMedia,PressaoMedia,TempMaximaMedia,TempCompensadaMedia,TempMinimaMedia,"
					+ "UmidadeRelativaMedia,VisibilidadeMedia) VALUES ("
					+ "'" + data[0] + "',"
					+ "'" + convertDate(data[1]) + "',"
					+ "'" + data[2] + "',";

			for (int i=3; i<data.length; i++) {
				String value = (!data[i].trim().equals(""))?data[i]:"NULL";
				sql += value + ",";
			}

			sql = sql + ")";
			sql = sql.replace(",)", ");\n");
			writeFile(sql, sqlFile);
		}
	}

	/**
	 * Output string in file
	 * @param text to output
	 * @param filename file
	 */
	private void writeFile(String text, File filename) {
		FileWriter fr = null;
		try {
			fr = new FileWriter(filename,true);
			fr.append(text);
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			try {
				fr.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Transform date to yyy-MM-dd format
	 * @param date original date in brazilian format
	 * @return Formatted date
	 * @throws ParseException
	 */
	private String convertDate(String date) throws ParseException {

		SimpleDateFormat sf = new SimpleDateFormat("dd/MM/yyyy");
		SimpleDateFormat sfDB = new SimpleDateFormat("yyyy-MM-dd");
		Date newDate = sf.parse(date);

		return sfDB.format(newDate);
	}

}

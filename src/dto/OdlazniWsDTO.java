package dto;

import java.util.ArrayList;

import model.TipWs;

public class OdlazniWsDTO {

	private String ime; 
	
	private ArrayList<Object> objekti = new ArrayList<>();
	
	private TipWs tip;

	public OdlazniWsDTO() {
		super();
	}

	public String getIme() {
		return ime;
	}

	public void setIme(String ime) {
		this.ime = ime;
	}

	public ArrayList<Object> getObjekti() {
		return objekti;
	}

	public void setObjekti(ArrayList<Object> objekti) {
		this.objekti = objekti;
	}

	public TipWs getTip() {
		return tip;
	}

	public void setTip(TipWs tip) {
		this.tip = tip;
	} 
	
	
	
}

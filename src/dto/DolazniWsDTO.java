package dto;

import model.TipWs;

public class DolazniWsDTO {

	private TipWs tip;
	
	private Object object;
	
	private String naziv;
	
	private String tipAgenta;

	public DolazniWsDTO() {
		super();
	}

	public TipWs getTip() {
		return tip;
	}

	public void setTip(TipWs tip) {
		this.tip = tip;
	}

	public Object getObject() {
		return object;
	}

	public void setObject(Object object) {
		this.object = object;
	}

	public String getNaziv() {
		return naziv;
	}

	public void setNaziv(String naziv) {
		this.naziv = naziv;
	}

	public String getTipAgenta() {
		return tipAgenta;
	}

	public void setTipAgenta(String tipAgenta) {
		this.tipAgenta = tipAgenta;
	}
	
	
	
}

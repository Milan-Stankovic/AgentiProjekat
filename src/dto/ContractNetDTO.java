package dto;

import java.util.HashMap;

import model.AID;

public class ContractNetDTO {

	private HashMap<AID, Integer>  ponude = new HashMap<>();

	public HashMap<AID, Integer> getPonude() {
		return ponude;
	}

	public void setPonude(HashMap<AID, Integer> ponude) {
		this.ponude = ponude;
	}

	public ContractNetDTO() {
		super();
	}
	
	
	
	
}

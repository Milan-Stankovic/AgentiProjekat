package model;

import java.util.ArrayList;
import java.util.HashMap;

import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.ejb.Startup;

@ConcurrencyManagement(ConcurrencyManagementType.CONTAINER)
@Startup
@Singleton
public class Baza {
	
	private ArrayList<AgentType> tipovi = new ArrayList<>();
	
	private HashMap<AID, Agent> agenti = new HashMap<>();
	
	private ArrayList<AgentskiCentar> agentskiCentri = new ArrayList<>();
	
	private AgentskiCentar lokalniCentar; // Grba tu mi postavi na pocetku trenutni centar
	
	private String masterIp = ""; // Kada budemo testirali 
	
	public ArrayList<AgentType> getTipovi() {
		return tipovi;
	}

	public void setTipovi(ArrayList<AgentType> tipovi) {
		this.tipovi = tipovi;
	}

	public HashMap<AID, Agent> getAgenti() {
		return agenti;
	}

	public void setAgenti(HashMap<AID, Agent> agenti) {
		this.agenti = agenti;
	}

	public ArrayList<AgentskiCentar> getAgentskiCentri() {
		return agentskiCentri;
	}

	public void setAgentskiCentri(ArrayList<AgentskiCentar> agentskiCentri) {
		this.agentskiCentri = agentskiCentri;
	}

	public AgentskiCentar getLokalniCentar() {
		return lokalniCentar;
	}

	public void setLokalniCentar(AgentskiCentar lokalniCentar) {
		this.lokalniCentar = lokalniCentar;
	}

	public String getMasterIp() {
		return masterIp;
	}

	public void setMasterIp(String masterIp) {
		this.masterIp = masterIp;
	}
	
	@Lock(LockType.WRITE)
	public Boolean addAgent(Agent agent) {

		AID aid = agent.getAid();
		for (AID a : agenti.keySet()) {
			if (aid.getName().equals(a.getName())) {
				return false;
			}
		}

		agenti.put(aid, agent);
		return true;
	}

	
	
	
	

}

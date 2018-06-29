package model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.websocket.Session;

@ConcurrencyManagement(ConcurrencyManagementType.CONTAINER)
@Startup
@Singleton
public class Baza {
	
	private ArrayList<AgentType> tipovi = new ArrayList<>();
	
	private ArrayList<Session> sesije = new ArrayList<>();
	
	private HashMap<AID, Agent> agenti = new HashMap<>();
	
	private ArrayList<AgentskiCentar> agentskiCentri = new ArrayList<>();
	
	private AgentskiCentar lokalniCentar; // Grba tu mi postavi na pocetku trenutni centar
	
	private String masterIp = ""; // Kada budemo testirali 
	
	public AgentskiCentar getAgentWithAlias(String alias) {
		for(AgentskiCentar c:agentskiCentri) {
			if(c.getAlias().equals(alias)) {
				return c;
			}
		}
		return null;
	}
	
	public AgentskiCentar removeAgentWithAlias(AgentskiCentar acentar) {
		for(AgentskiCentar c:agentskiCentri) {
			if(c.getAlias().equals(acentar.getAlias())) {
				agentskiCentri.remove(c);
				return acentar;
			}
		}
		return null;
	}
	
	public void removeAgentsOnHost(AgentskiCentar acentar) {
		ArrayList<AID> temp = new ArrayList<AID>();

		for (AID a : agenti.keySet()) {
			if (a.getHost().getAlias().equals(acentar.getAlias())) {
				temp.add(a);
			}
		}

		for (AID a : temp) {
			agenti.remove(a);
		}
	}
	
	public void updateAgentTypes(List<AgentType> noviZaUpdate) {
		for(AgentType a:noviZaUpdate) {
			insertAgentType(a);
		}
	}
	
	public AgentType insertAgentType(AgentType noviZaUpdate) { 
		for(AgentType tip: tipovi) {
			if(noviZaUpdate.getName().equals(tip.getName())&&noviZaUpdate.getModule().equals(tip.getModule())) {
				return null;
			}
		}
		tipovi.add(noviZaUpdate);
		return noviZaUpdate;
	}
	
	
	public void updateCenters(List<AgentskiCentar> noviZaUpdate) {
		for (AgentskiCentar a : agentskiCentri) {
			insertAgentskiCentar(a);
		}
	}
	
	public AgentskiCentar insertAgentskiCentar(AgentskiCentar noviZaUpdate) { 
		for (AgentskiCentar a : agentskiCentri) {
			if (a.getAlias().equals(noviZaUpdate.getAlias())) {
				return null;
			}
		}
		agentskiCentri.add(noviZaUpdate);
		return noviZaUpdate;
	}
	
	public void addAllAgents(List<AgentInterface> agenti) {
		for(AgentInterface a:agenti) {
			addAgent((Agent) a);
		}
	}

	
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
	
	public ArrayList<Session> getSesije() {
		return sesije;
	}

	public void setSesije(ArrayList<Session> sesije) {
		this.sesije = sesije;
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

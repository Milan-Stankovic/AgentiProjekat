package model;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.websocket.EncodeException;
import javax.websocket.Session;

import org.omg.Messaging.SyncScopeHelper;

import com.fasterxml.jackson.databind.ObjectMapper;

import dto.DolazniWsDTO;
import dto.OdlazniWsDTO;

@ConcurrencyManagement(ConcurrencyManagementType.CONTAINER)
@Startup
@Singleton
public class Baza implements Serializable{
	
	private ArrayList<AgentType> tipovi = new ArrayList<>();
	
	private ArrayList<Session> sesije = new ArrayList<>();
	
	private HashMap<AID, AgentInterface> agenti = new HashMap<>();
	
	private ArrayList<AgentskiCentar> agentskiCentri = new ArrayList<>();
	
	private AgentskiCentar lokalniCentar; // Grba tu mi postavi na pocetku trenutni centar
	
	private String masterIp = ""; // Kada budemo testirali 

	public void sendActiveToSocket() throws IOException, EncodeException{
		// napravi listu aid-a umesto remote interfejsa
		DolazniWsDTO d = new DolazniWsDTO();
		d.setTip(TipWs.ACTIVE);

		// prodji kroz sve sesije i posalji listu agenata
		for (Session s : sesije) {
			s.getBasicRemote().sendObject(d);
		}	
}
	
	public ArrayList<AID> getAgentByType(String type) {
		ArrayList<AID> retVal = new ArrayList<AID>();
		for (AID a : agenti.keySet()) {
			if (a.getType().getName().equals(type)) {
				retVal.add(a);
			}
		}
		return retVal;
	}
	
	@Lock(LockType.READ)
	public AgentInterface getAgentWithAID(AID aid) {
		System.out.println("Trazim agente i na serveru: "+getLokalniCentar().getAddress()+"imam sledece agente: ");
		System.out.println("Moram da poredim sa ovim aidom : " +aid);
		for(AID a:agenti.keySet()) {
			System.out.print(a);
			if(a.getHost().getAlias().equals(aid.getHost().getAlias())&&
					a.getHost().getAddress().equals(aid.getHost().getAddress())&&
					a.getName().equals(aid.getName())&&
					a.getType().getName().equals(aid.getType().getName())/*&&
					a.getType().getModule().equals(aid.getType().getModule())*/) {
				System.out.println("TRUE!!!!!!!");
				return agenti.get(a);
			}else {
				System.out.println();
			}
		}
		return null;
	}
	
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
		for (AgentskiCentar a : noviZaUpdate) {
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

	public HashMap<AID, AgentInterface> getAgenti() {
		return agenti;
	}

	public void setAgenti(HashMap<AID, AgentInterface> agenti) {
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
	public Boolean addAgent(AgentInterface agent) {

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

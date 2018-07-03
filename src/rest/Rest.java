package rest;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;

import agenti.ai.Generator;
import jms.JMSQueue;
import model.ACLPoruka;
import model.AID;
import model.Agent;
import model.AgentInterface;
import model.AgentType;
import model.AgentskiCentar;
import model.Baza;
import model.Performative;

@LocalBean
@Path("/agentskiCentar")
@Stateless
public class Rest implements RestRemote {
	
	@EJB
	Baza baza;
	
	@GET
	@Path("/test")
	@Produces(MediaType.TEXT_PLAIN)
	public String test() {
		System.out.println("UPAO JE U TEST WTF ???");
		
		return "RADIII !!!";}
	
	
	@GET
	@Path("/agents/classes")
	@Produces(MediaType.APPLICATION_JSON)
	public List<AgentType> getTipovi() {
		System.out.println("TIPOVI---"+baza.getTipovi());
		return baza.getTipovi();
	}
	
	@POST
	@Path("/agents/classes")
	@Produces(MediaType.APPLICATION_JSON)
	public void postTipovi(List<AgentType> noviAgenti) {
		baza.updateAgentTypes(noviAgenti);
	}
	
	@POST
	@Path("/agents/running")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public void postAgents(List<Agent> agents) {
		System.out.println("USAO SAM !");
		
		ArrayList<AgentInterface> ai = (ArrayList<AgentInterface>) getInterfaces((ArrayList) agents);
		baza.addAllAgents(ai);
		try {
			baza.sendActiveToSocket();
		} catch (Exception e) {
			System.out.println("Puko ws.");
			e.printStackTrace();
		}
	}
	
	
	public List<AgentInterface> getInterfaces(List<Agent> noviAgenti){
		ArrayList<AgentInterface> retVal = new ArrayList<AgentInterface>();
		for (Agent a : noviAgenti) {
			retVal.add(a);	
		}
		return retVal;
	}
	
	@GET
	@Path("/agents/types")
	@Produces(MediaType.APPLICATION_JSON)
	public ArrayList<AgentType> getAgents2() {return baza.getTipovi();}
	
	
	@GET
	@Path("/agents/running")
	@Produces(MediaType.APPLICATION_JSON)
	public ArrayList<AgentInterface> getAgents() {return new ArrayList<>(baza.getAgenti().values());}
	
	@PUT
	@Path("/agents/running/{type}/{name}")
	@Produces(MediaType.APPLICATION_JSON)
	public void startAgent(@PathParam("type") String type, @PathParam("name") String name) {
		
		try {
			Context context = new InitialContext();
			
			//Object obj223 = context.lookup("java:module/Pong"); // Totova madjija mada on nesto kao radi preko interfejsa
			
			AgentInterface agent = (AgentInterface) context.lookup("java:module/"+type); // Totova madjija mada on nesto kao radi preko interfejsa
			agent.init(new AID(name, baza.getLokalniCentar(), new AgentType(type, null)));

			
			baza.addAgent(agent);
			context.close();

			for (AgentskiCentar tempA : baza.getAgentskiCentri()) {
				
				System.out.println("http://" + tempA.getAddress() + ":8096/AgentiProjekat/rest/agentskiCentar/agents/running");
				
				ResteasyClient client = new ResteasyClientBuilder().build();
				if (!tempA.getAddress().equals(baza.getLokalniCentar().getAddress())) {
			
					
					System.out.println("http://" + tempA.getAddress() + ":8096/AgentiProjekat/rest/agentskiCentar/agents/running");
					
					ResteasyWebTarget target = client
							.target("http://" + tempA.getAddress() + ":8096/AgentiProjekat/rest/agentskiCentar/agents/running");
					
					ArrayList<AgentInterface> lista = new ArrayList<AgentInterface>();
					lista.add(agent);
					
					target.request(MediaType.APPLICATION_JSON).post(Entity.entity(lista,MediaType.APPLICATION_JSON));
				}
			}

		} catch (NamingException e) {
			e.printStackTrace();
		}
	}
	
	
	@DELETE
	@Path("/agents/running/{aid}/{hostName}")
	public void stopAgent(@PathParam("aid") String aid, @PathParam("hostName") String hostName) {

		for (AgentskiCentar ac : baza.getAgentskiCentri()) {
			if (ac.getAddress().equals(baza.getLokalniCentar().getAddress())) {
				
				AID temp = new AID();
				temp.setHost(baza.getLokalniCentar());
				temp.setName(aid);
				baza.getAgenti().remove(temp); 
				
				// Da li ovde trebam proci korz sve AgenteskeCentre i obrisati i u njima ako postoji ? Ne znam da li ima sinhronizacija izmedju Centara
				
			} else {
				ResteasyClient client = new ResteasyClientBuilder().build();
				ResteasyWebTarget target = client.target("http://" + ac.getAddress()
						+ ":8096/AgentiProjekat/rest/agentskiCentar/agents/running/remove/" + aid + "/" + hostName);
				target.request().delete();
			}
		}

	}
	
	@POST
	@Path("/messages")
	@Consumes(MediaType.APPLICATION_JSON)
	public void sendMessage(ACLPoruka poruka) {
		new JMSQueue(poruka);
	}
	
	@GET
	@Path("/messagesAI")
	@Consumes(MediaType.APPLICATION_JSON)
	public void sendMessageAI() {
		hardCodeAI();
		new JMSQueue(baza.getAiTest());
	}
	
	/*@GET
	@Path("/testJMS")
	@Consumes(MediaType.APPLICATION_JSON)
	public void sendMessagejms() {
		new JMSQueue(new ACLPoruka());
	}*/
	
	@GET
	@Path("/message")
	@Produces(MediaType.APPLICATION_JSON)
	public List<Performative> getPerformative() {
		
		ArrayList<Performative> temp = new ArrayList<Performative>();
		for (Performative p : Performative.values()) {
			temp.add(p);
		}
		return temp;
	}
	
	private void hardCodeAI() {
		startAgent("Generator", "AIGEN");

		AID gen = baza.getAgentByType("Generator").get(0);
		
		ACLPoruka nova = new ACLPoruka();
		nova.setContentObj(1);
		nova.setReceivers(new AID[] {gen});
		nova.setConversationID("ConvoIdAI");
		HashMap<String, Object> temp = new HashMap<>();
		
		/* "cmd.exe", "/c", "cd \"C:\\Users\\ilija\\AiTest\" && generator.py"
		 * 
			        "cd AiTest",
			        "-c",
			        "echo password | python script.py '" + packet.toString() + "'"
			        */
		String s1 = "cmd.exe,"+
					"/c,"+
					"cd \"C:\\Users\\milan\\AiTest\" &&"+
					"discriminator.py ";
		String s2 = "cmd.exe,"+
					"/c,"+
					"cd \"C:\\Users\\ilija\\AiTest\" &&"+
					"generator.py ";
		
		String sav1 = "C:\\Users\\ilija\\AiTest\\input.txt";
		String sav2 = "C:\\Users\\milan\\AiTest\\input.txt";
		String sav3 = "C:\\Users\\ilija\\AiTest\\output.txt";
		String sav4 = "C:\\Users\\milan\\AiTest\\output.txt";
		temp.put("DIS_LOC", s1);
		temp.put("DIS_RES_LOC", sav2);
		temp.put("DIS_SAVE_LOC", sav4);
		temp.put("GEN_LOC", s2);
		temp.put("GEN_RES_LOC", sav1);
		temp.put("GEN_SAVE_LOC", sav3);
		
		nova.setUserArgs(temp);
		nova.setPerformative(Performative.STARTAI);
		
		
		baza.setAiTest(nova);
	}
	
	
	
	


}

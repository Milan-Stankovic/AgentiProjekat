package rest;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
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

import model.ACLPoruka;
import model.AgentType;
import model.AgentskiCentar;
import model.Baza;

@LocalBean
@Path("/node")
@Stateless
public class NodeRest implements NodeRestRemote{
	
	@EJB 
	private Baza db;
	
	@GET
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	public AgentskiCentar getNode() {
		return db.getLokalniCentar();
	}
	
	@POST
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public List<AgentskiCentar> registerNode(AgentskiCentar novCenatar) {
		System.out.println("Poceta registracija na Master node");
		try {
			if(db.getMasterIp().equals(db.getLokalniCentar().getAddress())) {
				System.out.println("Saljem zahtev da se na novi cvor obavesti o tipovima agenata na: "+"http://" + novCenatar.getAddress() + ":8096/AgentiProjekat/rest/agentskiCentar/agents/classes");
				ResteasyClient client = new ResteasyClientBuilder().build();
				ResteasyWebTarget target = client.target("http://" + novCenatar.getAddress() + ":8096/AgentiProjekat/rest/agentskiCentar/agents/classes");
				
				System.out.println("Target je: "+target.getUri());
				
				Response response = target.request(MediaType.APPLICATION_JSON).get();
				System.out.println("Response je: "+response.getStatus()+response.getEntity());
				ArrayList<AgentType> podrzavaniAgenti = (ArrayList<AgentType>) response.readEntity(new GenericType<List<AgentType>>() {});
				System.out.println("Dal eradi ono sto mislimo da ne radi: "+podrzavaniAgenti);
				db.updateAgentTypes(podrzavaniAgenti);
				
				System.out.println("Saljem novi node na ostale ne master nodeove");
				for (AgentskiCentar nodeovi : db.getAgentskiCentri()) {
					if (!nodeovi.getAddress().equals(db.getMasterIp())) {
						target = client.target("http://" + nodeovi.getAddress() + ":8096/AgentiProjekat/rest/node");
						response = target.request().post(Entity.entity(novCenatar, MediaType.APPLICATION_JSON));
					}
				}
				db.insertAgentskiCentar(novCenatar);

				System.out.println("Saljem tipove agenata na ostale cvorove");
				for (AgentskiCentar nodeovi : db.getAgentskiCentri()) {
					if (!nodeovi.getAddress().equals(db.getMasterIp())) {
						target = client.target("http://" + nodeovi.getAddress() + ":8096/AgentiProjekat/rest/agentskiCentar/agents/classes");
						response = target.request().post(Entity.entity(db.getTipovi(), MediaType.APPLICATION_JSON));
					}
				}
				
				System.out.println("Saljem na novi node agente koji radi");
				target = client.target("http://" + novCenatar.getAddress() + ":8096/AgentiProjekat/rest/agentskiCentar/agents/running");
				response = target.request().post(Entity.entity(db.getAgenti(), MediaType.APPLICATION_JSON));
				
				return db.getAgentskiCentri();
			}else {
				db.insertAgentskiCentar(novCenatar);
			}
		}catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return null;
	}
	
	
	
	@DELETE
	@Path("/{alias}")
	@Produces(MediaType.APPLICATION_JSON)
	public AgentskiCentar deleteNode(@PathParam("alias") String alias) {
		AgentskiCentar c = db.getAgentWithAlias(alias);
		db.removeAgentsOnHost(c);
		//TODO obrisati tipove agenata
		db.removeAgentWithAlias(c);

		if (db.getMasterIp().equals(c.getAddress())) {
			for (AgentskiCentar a : db.getAgentskiCentri()) {
				if (!a.getAlias().equals(db.getLokalniCentar().getAlias())) {
					ResteasyClient client = new ResteasyClientBuilder().build();
					ResteasyWebTarget target = client.target("http://" + a.getAddress() + ":8096/AgentiProjekat/rest/node/" + alias);
					target.request().delete();
				}
			}
		}
		return c;
	}
	

}

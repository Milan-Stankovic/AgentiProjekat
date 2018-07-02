package main;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.EJB;
import javax.ejb.Schedule;
import javax.ejb.Schedules;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;

import model.AgentType;
import model.AgentskiCentar;
import model.Baza;

@Startup
@Singleton
public class StartServer {

	@EJB
	private Baza db;
	
	private String masterIp;
	
	private String currentIp;
	
	private String curentHostname;
	
	private String alias;
	
	@PostConstruct
	public void handshake() {
		
		
		//ucitavanje IP i kreiranje agentskog centra
		if(loadIp()) {
			db.setMasterIp(masterIp);
			
			AgentskiCentar a = new AgentskiCentar(curentHostname, currentIp);
			db.setLokalniCentar(a);
			db.insertAgentskiCentar(a);
			
			
			//ako je kreirani cvor bas master dodam samo par tipova agenata
			//System.out.println("POREDIM SLEDECA 2:"+currentIp+"---"+db.getMasterIp()+"---"+currentIp.equals(db.getMasterIp()));
			if(currentIp.equals(db.getMasterIp())) {
				AgentType tip = new AgentType();
				tip.setModule("EJB");
				tip.setName("Ping");
				AgentType tip1 = new AgentType();
				tip1.setModule("EJB");
				tip1.setName("Pong");
				ArrayList<AgentType> tipovi = new ArrayList<>();
				tipovi.add(tip);
				tipovi.add(tip1);
				db.setTipovi(tipovi);

				
				System.out.println("Master node initiated. Prepare to be amazed.");
			}
			else {
				
				//TODO dodaj tip agenta koji centar podrzava
				AgentType tip = new AgentType();
				tip.setModule("EJB");
				tip.setName("Initiator");
				ArrayList<AgentType> tipovi = new ArrayList<>();
				tipovi.add(tip);
				
				AgentType tip1 = new AgentType();
				tip1.setModule("EJB");
				tip1.setName("Participant");
				tipovi.add(tip1);
				
				db.setTipovi(tipovi);
				Thread t = new Thread() {
		            @Override
		            public void run() {
		            	System.out.println("Slave node trying to connect to cluster. Hope master is nice.");
						if (!tryHandshake()){
							deleteNode(db.getLokalniCentar());
							if (!tryHandshake()){
								deleteNode(db.getLokalniCentar());
							}
						}
						
						System.out.println("Thanks master. I'm alive.");
		            }
		        };
		        System.out.println("Starting new thread");
		        t.start();
			}
		}
	}
	
	@PreDestroy
	public void preDestroy(){
		
		AgentskiCentar agentskiCentar = new AgentskiCentar(alias, currentIp);
		deleteNode(agentskiCentar);
	}	
	
	public Boolean tryHandshake(){
		
		try {
			ResteasyClient client1 = new ResteasyClientBuilder().build();
			ResteasyWebTarget target1 = client1.target("http://" + masterIp + ":8096/AgentiProjekat/rest/agentskiCentar/test");
			System.out.println("Target je: "+target1.getUri());

			Response response2 = target1.request(MediaType.TEXT_PLAIN).get();
			System.out.println("Response je: "+response2.getStatus()+response2.getEntity());
			
			
			System.out.println("Protocol code 'Handshake' initiated. Hope everything doesnt burn down in flames.");
			ResteasyClient client = new ResteasyClientBuilder().build();
			ResteasyWebTarget target = client.target("http://" + masterIp + ":8096/AgentiProjekat/rest/node/");
			Response response = target.request(MediaType.APPLICATION_JSON).post(Entity.entity(db.getLokalniCentar(), MediaType.APPLICATION_JSON));
			List<AgentskiCentar> agentskiCentri = response.readEntity(new GenericType<List<AgentskiCentar>>(){});
			System.out.println("Dobio sam listu cvorova: "+agentskiCentri);
			db.updateCenters(agentskiCentri);
			System.out.println("Stavio sam listu cvorova: "+db.getAgentskiCentri());
			System.out.println("Protocol code 'Handshake' is succesfull");
					
		} catch (Exception e){
			System.out.println("Protocol code 'Handshake' failed");
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
		
	public void deleteNode(AgentskiCentar ac){
		
		ResteasyClient client = new ResteasyClientBuilder().build();
		ResteasyWebTarget target = client.target("http://" + masterIp +  ":8096/AgentiProjekat/rest/node/" + ac.getAlias());
		target.request().delete();
			
	}
	
	@Schedules({
		@Schedule(hour = "*", minute = "*", second = "*/60", info = "svaki minut"),
		})
	public void checkHeartbeats(){
		
		try{
			AgentskiCentar agentskiCentar = db.getLokalniCentar();
			for (AgentskiCentar ac : db.getAgentskiCentri()){
				if (!ac.getAddress().equals(agentskiCentar.getAddress())){
					
					AgentskiCentar checked = heartbeat(ac);
					if (checked == null){
						checked = heartbeat(ac);
						if (checked == null){
							deleteNode(ac);
						}
					}
					System.out.println("Agentski centar " + ac.getAlias() + " is alive");
				}
				
			}
		} catch (Exception e){
			System.out.println("Exception while checking hearthbeat from center with IP: "+db.getLokalniCentar().getAddress()+" and alias: "+db.getLokalniCentar().getAlias());
			e.printStackTrace();
			
		}
		
	} 
	
	public AgentskiCentar heartbeat(AgentskiCentar ac){
		
		try {
			ResteasyClient client = new ResteasyClientBuilder().build();
			ResteasyWebTarget target = client.target("http://" + ac.getAddress() + ":8096/AgentiProjekat/rest/node/");
			Response response = target.request(MediaType.APPLICATION_JSON).get();
			
			return response.readEntity(AgentskiCentar.class);
		} catch (Exception e){
			System.out.println("Exception while checking hearthbeat on center with IP: "+ac.getAddress()+" and alias: "+ac.getAlias());
			return null;
		}
		
}
	
	private boolean loadIp() {
		BufferedReader br = null;
		try {
			
			java.nio.file.Path p = Paths.get(".").toAbsolutePath().normalize();
			br = new BufferedReader(new FileReader(p.toString()+"\\config.txt"));
			System.out.println("Loading IP address from configuration file with path: "+p.toString()+"\\config.txt");
			
		    StringBuilder sb = new StringBuilder();
		    String line = br.readLine();

		    while (line != null) {
		        sb.append(line);
		        sb.append(System.lineSeparator());
		        line = br.readLine();
		    }
		    masterIp = sb.toString();
		    masterIp = masterIp.substring(0, masterIp.length()-2);
		    System.out.println("IP length loaded is: "+masterIp.length());
		  
		    
		    URL whatismyip = new URL("http://checkip.amazonaws.com");
		    BufferedReader in = new BufferedReader(new InputStreamReader(
		                    whatismyip.openStream()));

		    String ip = in.readLine();
		    
		    currentIp = getLocalHostLANAddress().toString();
		    String[] split = currentIp.split("/");
		    currentIp=split[1];
		    String[] split2 = currentIp.split("/n");
		    currentIp=split2[0];
		    curentHostname = InetAddress.getLocalHost().getHostName();
		    
		    System.out.println("I am registring myself.\n Master IP is: "+masterIp+"My IP(global) is: "+ip+"\nMy IP(local) is: " + currentIp);
		    return true;
		} catch (Exception e) {
			System.out.println("Error while loading config.txt or Error while loading IP adress.");
			return false;
		}
	}
	private static InetAddress getLocalHostLANAddress() throws UnknownHostException {
	    try {
	        InetAddress candidateAddress = null;
	        // Iterate all NICs (network interface cards)...
	        for (Enumeration ifaces = NetworkInterface.getNetworkInterfaces(); ifaces.hasMoreElements();) {
	            NetworkInterface iface = (NetworkInterface) ifaces.nextElement();
	            // Iterate all IP addresses assigned to each card...
	            for (Enumeration inetAddrs = iface.getInetAddresses(); inetAddrs.hasMoreElements();) {
	                InetAddress inetAddr = (InetAddress) inetAddrs.nextElement();
	                if (!inetAddr.isLoopbackAddress()) {

	                    if (inetAddr.isSiteLocalAddress()) {
	                        // Found non-loopback site-local address. Return it immediately...
	                        return inetAddr;
	                    }
	                    else if (candidateAddress == null) {
	                        // Found non-loopback address, but not necessarily site-local.
	                        // Store it as a candidate to be returned if site-local address is not subsequently found...
	                        candidateAddress = inetAddr;
	                        // Note that we don't repeatedly assign non-loopback non-site-local addresses as candidates,
	                        // only the first. For subsequent iterations, candidate will be non-null.
	                    }
	                }
	            }
	        }
	        if (candidateAddress != null) {
	            // We did not find a site-local address, but we found some other non-loopback address.
	            // Server might have a non-site-local address assigned to its NIC (or it might be running
	            // IPv6 which deprecates the "site-local" concept).
	            // Return this non-loopback candidate address...
	            return candidateAddress;
	        }
	        // At this point, we did not find a non-loopback address.
	        // Fall back to returning whatever InetAddress.getLocalHost() returns...
	        InetAddress jdkSuppliedAddress = InetAddress.getLocalHost();
	        if (jdkSuppliedAddress == null) {
	            throw new UnknownHostException("The JDK InetAddress.getLocalHost() method unexpectedly returned null.");
	        }
	        return jdkSuppliedAddress;
	    }
	    catch (Exception e) {
	        UnknownHostException unknownHostException = new UnknownHostException("Failed to determine LAN address: " + e);
	        unknownHostException.initCause(e);
	        throw unknownHostException;
	    }
}
}

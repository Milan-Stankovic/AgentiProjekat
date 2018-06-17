package rest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateful;
import javax.websocket.EncodeException;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import dto.DolazniWsDTO;
import dto.OdlazniWsDTO;
import encoderDecoder.DolazniWsDTODecoder;
import encoderDecoder.OdlazniWsDTOEncoder;
import model.ACLPoruka;
import model.Agent;
import model.AgentType;
import model.Baza;
import model.Performative;
import model.TipWs;


@Stateful
@ServerEndpoint(
		
	    value = "/ws",
        decoders = {DolazniWsDTODecoder.class},
	    encoders = {OdlazniWsDTOEncoder.class}
		
		)

public class WebSocket implements WebSocketRemote {


	List<Session> lokalneSesije = new ArrayList<>();
	
	@EJB
	Rest agentskiCentar;
	
	@EJB
	Baza baza;
	

	@OnMessage
	public void message(Session session, DolazniWsDTO ws) {
		
		if (session.isOpen()) {
			
			for (Session s : lokalneSesije) {
					
				if (s.getId().equals(session.getId())) {
						
					switch (ws.getTip()) {
						case PERFORMATIVE: 
								
							OdlazniWsDTO odlazni = new OdlazniWsDTO();
								
							odlazni.setTip(TipWs.PERFORMATIVE);
								
							List<Performative> performative = agentskiCentar.getPerformative();

							for(Performative p : performative) {
								odlazni.getObjekti().add(p);
							}
								
						try {
							s.getBasicRemote().sendObject(odlazni);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (EncodeException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
							break;
							
							
						case TYPE: 
								
							OdlazniWsDTO odlazni2 = new OdlazniWsDTO();
								
							odlazni2.setTip(TipWs.TYPE);
								
							List<AgentType> tipovi = agentskiCentar.getTipovi();

							for(AgentType temp : tipovi){
								odlazni2.getObjekti().add(temp);
							}
								
								
						try {
							s.getBasicRemote().sendObject(odlazni2);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (EncodeException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
							break;
							
						case ACTIVE: 
								
							OdlazniWsDTO odlazni3 = new OdlazniWsDTO();
								
							odlazni3.setTip(TipWs.ACTIVE);
								
								
							ArrayList<Agent> agenti = agentskiCentar.getAgents();

							for(Agent temp : agenti){
								odlazni3.getObjekti().add(temp.getAid());
							}		
								
								
						try {
							s.getBasicRemote().sendObject(odlazni3);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (EncodeException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
							break;
							
							
						case UKLJUCI: 
								
							agentskiCentar.startAgent(ws.getTipAgenta(), ws.getNaziv());						
							break;
							
						case ISKLJUCI: 
							
							agentskiCentar.stopAgent(ws.getTipAgenta(), ws.getNaziv());
							break;
							
						case PORUKA: 
								
							ACLPoruka poruka = (ACLPoruka) ws.getObject();
							agentskiCentar.sendMessage(poruka);
							break;
							
						default: 
							System.out.println("KAKO SI TU STIGAO ?");
		                    break;
					}
				}
			}
		}
	} 
			

	@OnOpen
	public void open(Session session) {
		if (!lokalneSesije.contains(session)) {
			
			System.out.println("NOVA SESIJA");
			lokalneSesije.add(session);
			baza.getSesije().add(session);
		}
	}
	
	@OnClose
	public void close(Session session) {
		lokalneSesije.remove(session);
		baza.getSesije().remove(session);
	}
	
	@OnError
	public void error(Session session, Throwable t) {
		System.out.println("PRSAO");
	}
	
}

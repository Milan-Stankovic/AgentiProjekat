package rest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.ejb.LocalBean;
import javax.ejb.Stateful;
import javax.inject.Inject;
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
import encoderDecoder.DolazniWsDTOEncoder;
import encoderDecoder.OdlazniWsDTODecoder;
import encoderDecoder.OdlazniWsDTOEncoder;
import model.ACLPoruka;
import model.Agent;
import model.AgentInterface;
import model.AgentType;
import model.Baza;
import model.Performative;
import model.TipWs;

@LocalBean
@Stateful
@ServerEndpoint(
		
	    value = "/ws",
        decoders = {DolazniWsDTODecoder.class, OdlazniWsDTODecoder.class},
	    encoders = {OdlazniWsDTOEncoder.class, DolazniWsDTOEncoder.class}
		
		)
public class WebSocket implements WebSocketRemote {


	List<Session> lokalneSesije = new ArrayList<>();
	
	@Inject
	Rest agentskiCentar;
	
	@Inject
	Baza baza;
	

	@OnMessage
	public void message(Session session, DolazniWsDTO ws) {
		
		System.out.println("EVO ME U WEBSOCKETU");
		System.out.println(ws.getTip());
		System.out.println(ws);
		
		if (session.isOpen()) {
			Session s = session;
			
		//	for (Session s : lokalneSesije) {
					
			//	if (s.getId().equals(session.getId())) {
						
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
								 
							System.out.println("UPAO U AKTIVNI WS");
							
							OdlazniWsDTO odlazni3 = new OdlazniWsDTO();
								
							odlazni3.setTip(TipWs.ACTIVE);
							odlazni3.setIme("CHOO CHOO");
								
								
							ArrayList<AgentInterface> agenti = agentskiCentar.getAgents();

							for(AgentInterface temp : agenti){
								System.out.println("Agenti su : " + temp);
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
								
						//	agentskiCentar.startAgent(ws.getTipAgenta(), ws.getNaziv());						
							break;
							
						case ISKLJUCI: 
							
							agentskiCentar.stopAgent(ws.getTipAgenta(), ws.getNaziv());
							break;
							
						case PORUKA: 
								
							ACLPoruka poruka = (ACLPoruka) ws.getObject();
							//agentskiCentar.sendMessage(poruka);
							
							OdlazniWsDTO odlaznaPoruka = new OdlazniWsDTO();
							odlaznaPoruka.setTip(TipWs.PORUKA);
							odlaznaPoruka.setIme(poruka.toString());
							
						try {
							s.getBasicRemote().sendObject(odlaznaPoruka);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (EncodeException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
							break;
							
						default: 
							System.out.println("KAKO SI TU STIGAO ?");
		                    break;
					}
				}
			//}
		//}
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

package agenti.contractNet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.ejb.Remote;
import javax.ejb.Stateful;
import javax.inject.Inject;

import dto.ContractNetDTO;
import jms.JMSQueue;
import model.ACLPoruka;
import model.AID;
import model.Agent;
import model.AgentInterface;
import model.Baza;
import model.Performative;

@Remote(AgentInterface.class)
@Stateful
public class Initiator extends Agent {

	
	private HashMap<String, ContractNetDTO> mapa = new HashMap<String, ContractNetDTO>();
	
	private long pocetak;
	
	private long kraj = 3000;
	
	@Inject
	Baza baza;

	@Override
	public void stop() {
		System.out.println("Initiator iz contract Neta stao");
	}

	@Override
	public void handleMessage(ACLPoruka poruka) {
		
		if (poruka.getPerformative().equals(Performative.REQUEST)) {
			
			System.out.println("REQUEST");
			
			pocetak = System.currentTimeMillis();

			
			ACLPoruka temp = new ACLPoruka();
			
			temp.setContent("CONTRACT NET, POTREBNO VREME ?");
			temp.setSender(this.getAid());
			
			
			HashMap<AID, AgentInterface> agenti = baza.getAgenti();
			
			ArrayList<AID> receivers = new ArrayList<>();
			
			for(Map.Entry<AID, AgentInterface> entry : agenti.entrySet()) {
				
				AID key = entry.getKey();
			    
				AgentInterface value = entry.getValue();

				if(key.getType().getName().equals("Participant"))
					receivers.add(key);
			 
			}
			
			AID[] r = new AID[receivers.size()];
			r = receivers.toArray(r);
			
			temp.setReceivers(r); /// OVDEEEEEE
			
			
			temp.setConversationID(poruka.getConversationID());
			temp.setPerformative(Performative.CONTRACTNET);
			
			ContractNetDTO cdto = new ContractNetDTO();
			
			mapa.put(poruka.getConversationID(), cdto);
			
			// GRBA SALJI PORUKU
			new JMSQueue(temp);
			//evo saljem
			
			
			final ACLPoruka next = new ACLPoruka();
			next.setSender(this.aid);
			next.setReceivers(new AID[] { this.aid });
			next.setPerformative(Performative.NEXT);

			//Thread.sleep(kraj); // Mislim da ne smem ovako, jer nece moci da primi, ima li neko drugo odlaganje ? :D
			
			System.out.println("DALJE");
			
			
			Thread t = new Thread() {
	            @Override
	            public void run() {
	            	
	            	 try {
						Thread.sleep(10000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	            	
	            	new JMSQueue(next);
					
	            }
	        };

	        t.start();
			
			
			//GRBA SALJI PORUKU
			//new JMSQueue(next);

		} else if (poruka.getPerformative().equals(Performative.REFUSE)) {
			
			System.out.println("ODBIO ME");

		} else if (poruka.getPerformative().equals(Performative.PROPOSE)) {

			boolean b = false;
			
			long sada = System.currentTimeMillis();
			
			if (sada - pocetak < kraj) { // Odbij one sto kasne :D
				
				System.out.println("PRIHVATIO");
				
				String tekst = poruka.getContent().split(",")[1];
				int vreme = Integer.parseInt(tekst);
				
				
				mapa.get(poruka.getConversationID()).getPonude().put(poruka.getSender(), vreme);
				
				b=true;

	
				
				
			} else {

				System.out.println("ODBIJ GA");


			}
			
			
			ACLPoruka temp = new ACLPoruka();
			temp.setSender(this.getAid());
			temp.setConversationID(poruka.getConversationID());
			if(b)
				temp.setPerformative(Performative.OK);
			else
				temp.setPerformative(Performative.REJECT);
			
			
			
			
			temp.setReceivers(new AID[] { poruka.getSender() });
			
			//GRBA SALJI PORUKU
			//new JMSQueue(temp);
			//evo saljem
			

		} else if (poruka.getPerformative().equals(Performative.NEXT)) {

			
			System.out.println("USAO JE U NEXT");
			
			int min =101;
			AID minAID = null;
			
			HashMap<AID, Integer> ponude = mapa.get(poruka.getConversationID()).getPonude();
			
			for(Map.Entry<AID, Integer> entry : ponude.entrySet()) {
				
				AID key = entry.getKey();
			    
				int value = entry.getValue();

				if(value < min) {
					min=value;
					minAID = key;
				}
				
			 
			}
			
			

			ACLPoruka temp = new ACLPoruka();
			temp.setSender(this.getAid());
			temp.setConversationID(poruka.getConversationID());

			for(Map.Entry<AID, Integer> entry : ponude.entrySet()) {
				
				System.out.println("UPAO JE U FOR");

				AID key = entry.getKey();
				
				if (key.equals(minAID)) {
					temp.setPerformative(Performative.ACCEPT);

				} else {
					temp.setPerformative(Performative.REJECT);
				
				}
				
				AID[] niz = new AID[1];
				niz[0]=key;
				
				temp.setReceivers(niz);

				
				//GRBA SALJI PORUKU
				new JMSQueue(temp);
				//evo saljem
			
			}
		} else if (poruka.getPerformative().equals(Performative.DONE)) {
			
			ACLPoruka temp = new ACLPoruka();
			temp.setContent("ODRADIO CONTRACTNET USPESNO : " + poruka.getSender().getName());
			//GRBA SALJI PORUKU
			new JMSQueue(temp);
			//evo saljem
			
		
		} else if (poruka.getPerformative().equals(Performative.FAILED)) {
			
			ACLPoruka aclMsg = new ACLPoruka();
			aclMsg.setContent("ODRADIO CONTRACTNET NEUSPESNO :" + poruka.getSender().getName());
			
			
			//GRBA SALJI PORUKU
			new JMSQueue(aclMsg);
			//evo saljem
			
		}
	}


	
	
}

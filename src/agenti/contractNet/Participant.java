package agenti.contractNet;

import java.util.ArrayList;
import java.util.Random;

import javax.ejb.EJB;
import javax.ejb.Remote;
import javax.ejb.Stateful;

import model.ACLPoruka;
import model.AID;
import model.Agent;
import model.AgentInterface;
import model.Baza;
import model.Performative;

@Remote(AgentInterface.class)
@Stateful
public class Participant extends Agent {
	
	
	@EJB
	Baza baza;
	
	@Override 
	public void stop(){
		System.out.println("Participant iz contract Neta stao");
	}
	
	@Override
	public void handleMessage(ACLPoruka poruka){ // Dobije ili zapocni ili da je prihvacen ili da je odbijen
		
		if (poruka.getPerformative().equals(Performative.CONTRACTNET)){
		
			System.out.println("POCEO CONTRACT NET");
			
			ACLPoruka temp = new ACLPoruka();
			temp.setSender(this.getAid());
			
			AID[] niz = new AID[1];
			niz[0]=poruka.getSender();
			
			temp.setReceivers(niz);
			temp.setConversationID(poruka.getConversationID());
			
			Random r = new Random();
			int n = r.nextInt(100);
			
			
			if (n<50){
				
				System.out.println("PRIMIO");
				
				temp.setPerformative(Performative.PROPOSE);
				
				int vreme = r.nextInt(100);
				temp.setContent("VREME," + vreme );
				
			} else {
				
				System.out.println("ODBIO");
				
				temp.setPerformative(Performative.REFUSE);
				
				temp.setContent("NO");
			}

			// SALJI PORUKU GRBA
			
		} else if (poruka.getPerformative().equals(Performative.REJECT)){
			
			System.out.println("ODBIJEN");
			
		}else if (poruka.getPerformative().equals(Performative.OK)){
			
			System.out.println("INITIATOR DOBIO NA VREME");
			
		} else if (poruka.getPerformative().equals(Performative.ACCEPT)){
			
			System.out.println("PRIMLJEN");
			
			ACLPoruka temp = new ACLPoruka();
			temp.setSender(this.getAid());
			
			AID[] niz = new AID[1];
			niz[0]=poruka.getSender();
			
			temp.setReceivers(niz);
			temp.setConversationID(poruka.getConversationID());
			
			Random r = new Random();
			
			int n = r.nextInt(100);
			
			if (n<50){
				
				System.out.println("USPEO");
				
				temp.setPerformative(Performative.DONE);
				
				temp.setContent("DONE");
				
			} else {
				
				System.out.println("FAILOVAO");
				
				temp.setPerformative(Performative.FAILED);
				
				temp.setContent("FAILED");
			}
			
			//SALJI PORUKU GRBA
		
			
		}
	}

	

}

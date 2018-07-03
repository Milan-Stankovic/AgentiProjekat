package agenti.ai;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.ejb.EJB;
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

@Stateful
public class Discriminator extends Agent{

private int broj_generacija=-1;
	
	private AID generator;
	
	private String saveLoc = "";
	
	private int broj_max = -1;
	

	@Inject
	private Baza baza;

	
	@Override
	public void stop() {
		System.out.println("GAN STAO");
	}


	@Override
	public void handleMessage(ACLPoruka poruka) {
		boolean b = true;
		
		if (poruka.getPerformative().equals(Performative.ENDGAN)) {
			System.out.println("END GAN");
		}else if (poruka.getPerformative().equals(Performative.RETURNRESULTGENERATOR)) {
		
			File f = new File(saveLoc);
			try {
				Files.deleteIfExists(f.toPath());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
			
			File actualFile = new File ((File)poruka.getContentObj(), saveLoc);
			
			System.out.println("SACUVAO");
			
			ACLPoruka next = new ACLPoruka();
			next.setSender(this.aid);
			next.setReceivers(new AID[] { this.aid });
			next.setPerformative(Performative.RETURNRESULTDISCRIMINATOR);
			next.setOntology(poruka.getOntology());
			next.setLanguage(poruka.getLanguage());
	
			
			new JMSQueue(next);
			
			

		}else if (poruka.getPerformative().equals(Performative.STARTGAN)) {
			
			System.out.println("STARTUJE GAN U GENERATORU");
		
			
		/*	temp.setProtocol((String)poruka.getUserArgs().get("DIS_LOC")); // OVDE CE BITI STRING ZA LOKACIJU PYa
			temp.setConversationID(poruka.getConversationID());
			temp.setPerformative(Performative.STARTGAN); // TODO DA LI JE STARTGAN ILI STARTAI
			temp.setOntology((String)poruka.getUserArgs().get("DIS_RES_LOC"));
			temp.setLanguage((String)poruka.getUserArgs().get("DIS_SAVE_LOC"));
			*/
			generator=poruka.getSender();
			
			broj_generacija=(int)poruka.getContentObj();
			saveLoc=poruka.getLanguage();
			broj_max=broj_generacija;
			
		
		
			ACLPoruka next = new ACLPoruka();
			next.setSender(this.aid);
			next.setReceivers(new AID[] { this.aid });
			next.setPerformative(Performative.RETURNRESULTGENERATOR);
			next.setOntology(poruka.getOntology());
			next.setLanguage(poruka.getLanguage());
	
			System.out.println("POCEO GAN");
			
			
		}else if (poruka.getPerformative().equals(Performative.RETURNRESULTDISCRIMINATOR)) {
			
			if(broj_max==broj_generacija) {
				String[] cmd = poruka.getProtocol().split(",");
						/*{
				        "/bin/bash",
				        "-c",
				        "echo password | python script.py '" + packet.toString() + "'"
				    };*/
				try {
					Runtime.getRuntime().exec(cmd);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			if(broj_generacija==0) {
				
				ACLPoruka next = new ACLPoruka();
				next.setSender(this.aid);
				next.setReceivers(new AID[] { this.aid });
				next.setPerformative(Performative.ENDGAN);
				
				
				ACLPoruka next2 = new ACLPoruka();
				next2.setSender(this.aid);
				next2.setReceivers(new AID[] { generator });
				next2.setPerformative(Performative.ENDGAN);
				System.out.println("KRAJ");
				
				
				//GRBA SALJI PORUKE
				new JMSQueue(next);
				new JMSQueue(next2);
			
			}
			
			else {
			
				final ACLPoruka temp = poruka;
				
				final AID thisAid = this.aid;
				
				Thread t = new Thread() {
		            @Override
		            public void run() {
		            	try {
		            	
		            	WatchService watcher = FileSystems.getDefault().newWatchService();
						Path dir = Paths.get(temp.getOntology());
						WatchKey key = dir.register(watcher, StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_CREATE);
		            	
		            	boolean next = false;
		            	
		            	while(true) {
							
							if(next)
								break;
							
							for(WatchEvent<?> event : key.pollEvents()) {
								
								if(event.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
									System.out.println("TEK SE SADA KREIRAO FAJL");
								}else {
									

									ACLPoruka next2 = new ACLPoruka();
									next2.setSender(thisAid);
									next2.setReceivers(new AID[] { generator });
									next2.setPerformative(Performative.RETURNRESULTDISCRIMINATOR);
									next2.setContentObj(new File(temp.getOntology())); // CEO FAJL DOBIJAS AKO OVO NE RADI, JEDAN SKIP
									System.out.println("SALJEM DALJE");
									broj_generacija--;
									//GRBA SALJI PORUKU
									new JMSQueue(next2);
									next= true; 
									break;
									
								}
								
							}
							
						}
		            	
		            	} catch (IOException e) {
		    				// TODO Auto-generated catch block
		    				e.printStackTrace();
		    			}
		            	
		            }
		        };

		        t.start();
			
		}
			
				
			
		}
		
	}
	
}

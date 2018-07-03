package agenti.ai;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
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
	
	private String ontology="";

	

	@Inject
	private Baza baza;

	
	@Override
	public void stop() {
		System.out.println("GAN STAO");
	}


	@Override
	public void handleMessage(ACLPoruka poruka) {
		
		if (poruka.getPerformative().equals(Performative.ENDGAN)) {
			System.out.println("END GAN");
		}else if (poruka.getPerformative().equals(Performative.RETURNRESULTGENERATOR)) {
			
			System.out.println("*****USAO U DISKRIMINATOR RETURNRESULTGENERATOR*****");
			
			System.out.println(saveLoc);
			
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
			next.setOntology(ontology);
			next.setLanguage(saveLoc);
			next.setConversationID(poruka.getConversationID());
	
			
			new JMSQueue(next);
			
			

		}else if (poruka.getPerformative().equals(Performative.STARTGAN)) {
			
			System.out.println("STARTUJE GAN U GENERATORU");

			generator=poruka.getSender();
			ontology=poruka.getOntology();
			broj_generacija=(int)poruka.getContentObj();
			saveLoc=poruka.getLanguage();
			broj_max=broj_generacija;

	
			System.out.println("POCEO GAN");
			
			
		}else if (poruka.getPerformative().equals(Performative.RETURNRESULTDISCRIMINATOR)) {
			
			if(broj_max==broj_generacija) {
				final String[] cmd = poruka.getProtocol().split(",");
						/*{
				        "/bin/bash",
				        "-c",
				        "echo password | python script.py '" + packet.toString() + "'"
				    };*/
			Thread t1 = new Thread() {
	            @Override
	            public void run() {
					try {
						System.out.println("INVOKE COMAND: "+cmd);
						ProcessBuilder builder = new ProcessBuilder(cmd);
				        builder.redirectErrorStream(true);
				        Process p = builder.start();
				        BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
				        String line;
				        while (true) {
				            line = r.readLine();
				            if (line == null) { break; }
				            System.out.println("CMD-SHIT---"+line);
				        }
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
			};
			t1.start();
			if(broj_generacija==0) {
				
				ACLPoruka next = new ACLPoruka();
				next.setSender(this.aid);
				next.setReceivers(new AID[] { this.aid });
				next.setPerformative(Performative.ENDGAN);
				next.setConversationID(poruka.getConversationID());
				
				
				ACLPoruka next2 = new ACLPoruka();
				next2.setSender(this.aid);
				next2.setReceivers(new AID[] { generator });
				next2.setPerformative(Performative.ENDGAN);
				next2.setConversationID(poruka.getConversationID());
				System.out.println("KRAJ");
				
				
				//GRBA SALJI PORUKE
				new JMSQueue(next);
				new JMSQueue(next2);
			
			}
			
			else {
			
				final ACLPoruka temp = poruka;
				
				final AID thisAid = this.aid;
				
				final String ont = ontology;
				
				Thread t = new Thread() {
		            @Override
		            public void run() {
		            	try {
		            	
		            	WatchService watcher = FileSystems.getDefault().newWatchService();
						Path dir = Paths.get(ont);
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
									next2.setConversationID(temp.getConversationID());
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
}

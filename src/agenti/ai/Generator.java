package agenti.ai;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
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
import javax.swing.plaf.synth.SynthSpinnerUI;

import dto.ContractNetDTO;
import jms.JMSQueue;
import model.ACLPoruka;
import model.AID;
import model.Agent;
import model.AgentInterface;
import model.Baza;
import model.Performative;

@Stateful
public class Generator extends Agent {

	private int broj_generacija=-1;
	
	
	private AID diskriminator;
	
	private String saveLoc = "";
	

	@Inject
	private Baza baza;

	
	@Override
	public void stop() {
		System.out.println("GAN STAO");
	}


	@Override
	public void handleMessage(ACLPoruka poruka) {
		boolean b = true;
		
		if(poruka.getPerformative().equals(Performative.STARTAI)) {
			
			broj_generacija=(int)poruka.getContentObj();
			
			
			
			System.out.println("START GAN");
			
			ACLPoruka temp = new ACLPoruka();
			
			
			temp.setSender(this.getAid());
			
			Baza bBB = baza;
			if(bBB==null) System.out.println("BAZA JE NULL A NE AGENTI");
			HashMap<AID, AgentInterface> agenti = bBB.getAgenti();
			
			System.out.println("Prosla baza u gene");
			
			ArrayList<AID> receivers = new ArrayList<>();
			
			for(Map.Entry<AID, AgentInterface> entry : agenti.entrySet()) {
				
				System.out.println("--- Nasao agenta : " + entry.getKey());
				
				AID key = entry.getKey();
			    
		

				if(key.getType().getName().equals("Discriminator")) {
					receivers.add(key);
					System.out.println("!!!NASAO DISKRIMINATORA!!!");
				}
					
			}
			
			if(receivers.size()==0) {
				System.out.println("NEMA Diskriminatora");
				b=false;
			}
			
			
			if(b) {
				
				System.out.println("USAO U IF !");
				
				AID[] r = new AID[1];
				r[0] = receivers.get(0);
				diskriminator=receivers.get(0);
				
				temp.setReceivers(r);
				temp.setEncoding((String)poruka.getUserArgs().get("DIS_SAVE_LOC"));
				System.out.println("SYSOOOO ENCODING: "+temp.getEncoding());
				temp.setProtocol((String)poruka.getUserArgs().get("DIS_LOC")); // OVDE CE BITI STRING ZA LOKACIJU PYa
				temp.setConversationID(poruka.getConversationID());
				temp.setPerformative(Performative.STARTGAN); // TODO DA LI JE STARTGAN ILI STARTAI
				temp.setOntology((String)poruka.getUserArgs().get("DIS_RES_LOC"));
				temp.setContentObj(broj_generacija);
				temp.setConversationID(poruka.getConversationID());
				temp.setContent((String)poruka.getUserArgs().get("DIS_RES_LOC"));
				//DODAJ AKO NESTO FALI
				new JMSQueue(temp);

				ACLPoruka next = new ACLPoruka();
				next.setSender(this.aid);
				next.setReceivers(new AID[] { this.aid });
				next.setPerformative(Performative.STARTGAN);
				next.setConversationID(poruka.getConversationID());
				next.setProtocol((String)poruka.getUserArgs().get("GEN_LOC"));// OVDE CE BITI STRING ZA LOKACIJU PY-a
				next.setOntology((String)poruka.getUserArgs().get("GEN_RES_LOC")); // LOKACIJA TEXT FILE-a
				next.setLanguage((String)poruka.getUserArgs().get("GEN_SAVE_LOC")); // LOKACIJA FILE IZ KOJE PY CITA
				saveLoc= (String)poruka.getUserArgs().get("GEN_SAVE_LOC");
				
				System.out.println("DALJE GAN");
				
				new JMSQueue(next);
				
				//da li sam trebao obe da saljem?
		
			}
			
			
		}else if (poruka.getPerformative().equals(Performative.ENDGAN)) {
			System.out.println("END GAN");
		}else if (poruka.getPerformative().equals(Performative.RETURNRESULTDISCRIMINATOR)) {
		
			File f = new File(saveLoc);
			try {
				Files.deleteIfExists(f.toPath());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
			
			String preneseno = poruka.getEncoding();
			writeFile(preneseno, saveLoc+"\\input.txt");
			
			ACLPoruka next = new ACLPoruka();
			next.setSender(this.aid);
			next.setReceivers(new AID[] { this.aid });
			next.setPerformative(Performative.RETURNRESULTGENERATOR);
			next.setOntology(poruka.getOntology());
			next.setLanguage(poruka.getLanguage());
			next.setConversationID(poruka.getConversationID());
			
			new JMSQueue(next);
			
			
			System.out.println("SACUVAO");
			

		}else if (poruka.getPerformative().equals(Performative.STARTGAN)) {
			
			System.out.println("STARTUJE GAN U GENERATORU");
		
			
			final String[] cmd = poruka.getProtocol().split(",");
					
					/*{
			        "/bin/bash",
			        "-c",
			        "echo password | python script.py '" + packet.toString() + "'"
			    };*/
			Thread t = new Thread() {
	            @Override
	            public void run() {
					try {
						//Runtime.getRuntime().exec({"cmd.exe", "cd AiTest"});
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
			t.start();
		
			ACLPoruka next = new ACLPoruka();
			next.setSender(this.aid);
			next.setReceivers(new AID[] { this.aid });
			next.setPerformative(Performative.RETURNRESULTGENERATOR);
			next.setOntology(poruka.getOntology());
			next.setLanguage(poruka.getLanguage());
			next.setConversationID(poruka.getConversationID());
	
			
			new JMSQueue(next);
			
			System.out.println("POCEO GAN");
			
			
		}else if (poruka.getPerformative().equals(Performative.RETURNRESULTGENERATOR)) {
			
			System.out.println("RETURNRESULTGENERATOR U GENERATORU ");
			
			if(broj_generacija==0) {
				
				ACLPoruka next = new ACLPoruka();
				next.setSender(this.aid);
				next.setReceivers(new AID[] { this.aid });
				next.setPerformative(Performative.ENDGAN);
				next.setConversationID(poruka.getConversationID());
				
				ACLPoruka next2 = new ACLPoruka();
				next2.setSender(this.aid);
				next2.setReceivers(new AID[] { diskriminator });
				next2.setPerformative(Performative.ENDGAN);
				next2.setConversationID(poruka.getConversationID());
				System.out.println("KRAJ");
				
				
				//GRBA SALJI PORUKE
				new JMSQueue(next);
				new JMSQueue(next2);
			
			}
			
			else {
			
				System.out.println("RETURNRESULTGENERATOR U GENERATORU U ELSE DELU ");
				
				final ACLPoruka temp = poruka;
				
				final AID thisAid = this.aid;
				
				Thread t = new Thread() {
		            @Override
		            public void run() {
		            	try {
		            	
		            	System.out.println("PRAVI WATCH SERVICE U GENERATORU ");
		            		
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
									
									System.out.println("GENERATOR WATCH SERVICE ");
									ACLPoruka next2 = new ACLPoruka();
									next2.setSender(thisAid);
									next2.setReceivers(new AID[] { diskriminator });
									next2.setPerformative(Performative.RETURNRESULTGENERATOR);
									next2.setEncoding(readFile(temp.getOntology()+"\\output.txt")); // CEO FAJL DOBIJAS
									System.out.println((String)next2.getEncoding());
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
	
	private String readFile(String path) {
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader(path));
		    StringBuilder sb = new StringBuilder();
		    String line = br.readLine();

		    while (line != null) {
		        sb.append(line);
		        sb.append(System.lineSeparator());
		        line = br.readLine();
		    }
		    String everything = sb.toString();
		    br.close();
		    return sb.toString();
		}catch (Exception e) {
			System.out.println("Error while loading file on loacion: "+path);
			return null;
		}
		
		

		    
	}
	
	private void writeFile(String write, String path) {
		BufferedWriter writer = null;
        try {
            //create a temporary file
            File logFile = new File(path);

            // This will output the full path where the file will be written to...
            System.out.println(logFile.getCanonicalPath());

            writer = new BufferedWriter(new FileWriter(logFile));
            writer.write(write);
        } catch (Exception e) {
        	System.out.println("Error while writing file on loacion: "+path);
            e.printStackTrace();
        } finally {
            try {
                // Close the writer regardless of what happens...
                writer.close();
            } catch (Exception e) {
            }
        }
	}

	
}

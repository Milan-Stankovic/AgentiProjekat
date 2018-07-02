package model;

public class Agent implements AgentInterface {
	
protected AID id;
	
	public Agent(AID id){
		this.id = id;
	}
	
	public Agent(){
		id = null;
	}
	
	public void init(AID aid) {
		this.id = aid;
	}
	

	@Override
	public String toString() {
		return "Agent [id=" + id + "]";
	}
	
	@Override
	public void setAid(AID aid){
		this.id = aid;
	}
	
	@Override
	public AID getAid(){
		return id;
	}

	@Override
	public void stop() {
		// TODO Auto-generated method stub
		System.out.println("AGENT STOP");
	}

	@Override
	public void handleMessage(ACLPoruka poruka) {
		// TODO Auto-generated method stub
		System.out.println("AGENT HADNLE MESSAGE");
	}

}

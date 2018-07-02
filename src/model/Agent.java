package model;

public class Agent implements AgentInterface {
	
protected AID aid;
	
	public Agent(AID id){
		this.aid = id;
	}
	
	public Agent(){
		aid = null;
	}
	
	public void init(AID aid) {
		this.aid = aid;
	}
	

	@Override
	public String toString() {
		return "Agent [id=" + aid + "]";
	}
	
	@Override
	public void setAid(AID aid){
		this.aid = aid;
	}
	
	@Override
	public AID getAid(){
		return aid;
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

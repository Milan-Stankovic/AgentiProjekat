package model;

import java.io.Serializable;

public interface AgentInterface extends Serializable {
	
	void init(AID aid);
	void stop();
	void handleMessage(ACLPoruka poruka);
	void setAid(AID aid);
	AID getAid();
}

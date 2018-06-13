package model;

import java.io.Serializable;

public class AID implements Serializable {

	private String name;
	private AgentskiCentar host;
	private AgentType type;
	
	
	public AID() {
		super();
	}

	public AID(String name, AgentskiCentar host, AgentType type) {
		super();
		this.name = name;
		this.host = host;
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public AgentskiCentar getHost() {
		return host;
	}

	public void setHost(AgentskiCentar host) {
		this.host = host;
	}

	public AgentType getType() {
		return type;
	}

	public void setType(AgentType type) {
		this.type = type;
	}

	@Override
	public String toString() {
		return "AID [name=" + name + ", host=" + host + ", type=" + type + "]";
	}

	
}

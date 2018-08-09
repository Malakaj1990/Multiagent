package MSCPro.actions;

import jade.content.AgentAction;

public class AmbulanceAccidentAction  implements AgentAction {
	
	public static final String AMBULANCE_ACCIDENT = "AmbulanceAccidentAction";
	 public static final String AMBULANCE_ACCIDENT_CASUALTIES_COUNT = "casualtiesCount";
	 public static final String AMBULANCE_ACCIDENT_LOCATION = "location";
	 public static final String AMBULANCE_ACCIDENT_SEVERITY = "severity";
	 public static final String AMBULANCE_ACCIDENT_AGENT_NAME = "agentName";
	
	 private int casualtiesCount;
	 private String location;
	 private String severity;
	 private String agentName;
	 
	 public String getAgentName()
	 {
		 return this.agentName;
	 }
	 
	 public void setAgentName(String agentName)
	 {
		 this.agentName = agentName;
	 }
	 
	 public int getCasualtiesCount()
	 {
		 return casualtiesCount;
	 }
	 
	 public String getLocation()
	 {
		 return location;
	 }
	 
	 public String getSeverity()
	 {
		 return severity;
	 }
	
	 public void setCasualtiesCount(int casualtiesCount)
	 {
		 this.casualtiesCount = casualtiesCount;
	 }
	 
	 public void setLocation(String location)
	 {
		 this.location = location;
	 }
	 
	 public void setSeverity(String severity)
	 {
		 this.severity = severity;
	 }
	
	
}

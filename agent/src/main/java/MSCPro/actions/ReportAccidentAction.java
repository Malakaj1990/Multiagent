package MSCPro.actions;

import jade.content.AgentAction;

public class ReportAccidentAction implements AgentAction {

	 public static final String REPORTING_ACCIDENT = "ReportAccidentAction";
	 public static final String REPORTING_ACCIDENT_CASUALTIES_COUNT = "casualtiesCount";
	 public static final String REPORTING_ACCIDENT_LOCATION = "location";
	 public static final String REPORTING_ACCIDENT_SEVERITY = "severity";
	
	 private int casualtiesCount;
	 private String location;
	 private String severity;
	 
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

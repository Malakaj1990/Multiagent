package MSCPro.actions;

import jade.content.AgentAction;

public class IncidentCompletionAction implements AgentAction{
	
	public static final String INCIDENT_COMPLETE_ACTION = "IncidentCompletionAction";
	 public static final String INCIDENT_COMPLETE_CONVERSATION_ID = "conversationID";
	 String conversationID;
	 public void setconversationID(String conversationID)
	 {
		 this.conversationID = conversationID;
	 }
	 
	 public String getconversationID()
	 {
		 return this.conversationID;
	 }
}

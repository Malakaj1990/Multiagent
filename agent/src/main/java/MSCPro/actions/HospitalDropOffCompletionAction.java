package MSCPro.actions;

import jade.content.AgentAction;

public class HospitalDropOffCompletionAction  implements AgentAction {

	 public static final String HOSPITAL_COMPLETION_DROPOFF_ACTION = "HospitalDropOffCompletionAction";
	 public static final String HOSPITAL_COMPLETION_DROPOFF_COUNT = "dropOffCount";
	 private int dropOffCount;
	 
	 public void setdropOffCount(int dropOffCount)
	 {
		 this.dropOffCount = dropOffCount;
	 }
	 
	 public int getdropOffCount()
	 {
		 return this.dropOffCount;
	 }
	
}

package MSCPro.agents;

import MSCPro.actions.AmbulanceAccidentAction;
import MSCPro.actions.AmbulanceNotSentAction;
import MSCPro.actions.IncidentCompletionAction;
import MSCPro.ontology.DisasterManagement;
import jade.content.Concept;
import jade.content.ContentElement;
import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.Ontology;
import jade.content.onto.basic.Action;
import jade.core.Agent;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;


class PoliceAgentBehaviour extends SimpleBehaviour
{
	public PoliceAgentBehaviour(PoliceAgent agent,String location,String conversationID) {
		this.agent = agent;
		this.location = location;
		this.conversationID = conversationID;
	}
	
	@Override
	public void onStart()
	{
		agent.logMessage("Police Agent is dispatched to "+ location);
	}


	@Override
	public void action() {
	
		//MessageTemplate mt = MessageTemplate.MatchConversationId(conversationID);
				ACLMessage msg = agent.receive();
				if(msg == null)
				{
					return;
				}
			
				ContentElement content = null;
				try {
						content = agent.getContentManager().extractContent(msg);
				} catch (Exception e) {
					e.printStackTrace();
				}
				Concept action = ((Action)content).getAction();
				if(action instanceof IncidentCompletionAction)
				{
					agent.logMessage("Inicident Completed Agent Terminated");
					agent.doDelete();
				}
				else if (action instanceof AmbulanceNotSentAction )
				{
					agent.logMessage("No Ambulance Sent Handle Casualties Transport to the hospital");
				}
					
				
				block();
		
	}

	@Override
	public boolean done() {
		// TODO Auto-generated method stub
		return false;
	}
	
	PoliceAgent agent;
	String location;
	String conversationID;
	
}

public class PoliceAgent extends Agent{

	@Override
	protected void setup() 
    { 
		 getContentManager().registerLanguage(codec);
	     getContentManager().registerOntology(ontology);
		
		Object[] args = getArguments();
		if (args != null) {
			location = (String) args[0];
			conversationID = (String) args[1];
		}
		
		addBehaviour(new PoliceAgentBehaviour(this,location,conversationID));
    }
	
	@Override
	protected void takeDown() 
    {
       
    }
	
	public void logMessage(String logMessage)
	{
		System.out.println(getLocalName()+"  ************************");
		System.out.println(logMessage);
		System.out.println("***************************");
		
	}
	
	String location;
	String conversationID;
	
	public Codec codec = new SLCodec();
	public Ontology ontology = DisasterManagement.getInstance();
}

package MSCPro.agents;

import java.util.HashSet;
import java.util.Set;

import MSCPro.actions.AmbulanceAccidentAction;
import MSCPro.actions.AmbulanceNotSentAction;
import MSCPro.actions.HospitalDropOffCompletionAction;
import MSCPro.actions.IncidentCompletionAction;
import MSCPro.actions.ReportAccidentAction;
import MSCPro.ontology.DisasterManagement;
import jade.content.Concept;
import jade.content.ContentElement;
import jade.content.lang.Codec;
import jade.content.lang.Codec.CodecException;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.Ontology;
import jade.content.onto.OntologyException;
import jade.content.onto.basic.Action;
import jade.core.AID;
import jade.core.Agent;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;
import jade.core.behaviours.SimpleBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

class IncidentHandlingBehaviuor extends SimpleBehaviour
{
	public IncidentHandlingBehaviuor(HospitalManagementAgent agent,String conversationID,ReportAccidentAction incident)
	{
		this.conversationID = conversationID;
		this.agent = agent;
		this.currentIncident = incident;
		this.ambulances = new HashSet<String>();
	
	}
	
	@Override
	public void onStart()
	{
		agent.logAction("New incident started = " + this.conversationID);
		if(currentIncident.getSeverity().equalsIgnoreCase("LOW") == true)
		{
			agent.logAction("Low Severity Incident Reported no ambulance will be dispatched");
			sendNoAmbulanceSentMessage();
		}
		else
		{
			createNewAmbulance();
		}
	}
	
	@Override
	public void action() {
		
		MessageTemplate mt = MessageTemplate.MatchConversationId(conversationID);
		ACLMessage msg = agent.receive(mt);
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
		if(action instanceof HospitalDropOffCompletionAction)
		{
			onDropOffCompletion((HospitalDropOffCompletionAction)action);
		}
		else if(action instanceof AmbulanceAccidentAction)
		{
			onAmbulanceAccident((AmbulanceAccidentAction)action);
		}
		else 
		{
		}
		
		block();
		
	}

	@Override
	public boolean done() {
		// TODO Auto-generated method stub
		return false;
	}
	
	private void onAmbulanceAccident(AmbulanceAccidentAction action)
	{
		agent.logAction("Ambulance " + action.getAgentName() + "has met with an accident "
				+ "new ambulance to dispatch");
	}
	
	private void createNewAmbulance()
	{
		AgentContainer c = agent.getContainerController();
		++initiatedambulance_count;
		String agentID = "AMB_" +conversationID+"_"+initiatedambulance_count; 
		try {
			Object[] args = new Object[2];
			args[0] = currentIncident.getLocation();
			args[1] = conversationID;
			AgentController ambulanceAgent = c.createNewAgent(agentID,"MSCPro.agents.AmbulanceAgent", args);
			ambulanceAgent.start();
			ambulances.add(agentID);
		} catch (StaleProxyException e) {
			
			e.printStackTrace();
		}
	}
	
	private void onDropOffCompletion(HospitalDropOffCompletionAction action)
	{
		
		agent.logAction("On DropOff Completed incidentID = " + this.conversationID 
				+ " dropoff Count = " + action.getdropOffCount());
		
		int remainingCasualtiesCount = currentIncident.getCasualtiesCount() - action.getdropOffCount();
		if(remainingCasualtiesCount < 0)
		{
			remainingCasualtiesCount = 0;
		}
		currentIncident.setCasualtiesCount(remainingCasualtiesCount);
		
		if(remainingCasualtiesCount == 0)
		{
			agent.logAction("Incident ID = " + conversationID +" is completed");
			if(ambulances.size() ==0 )
			{
				return;
			}
				
			ACLMessage replyMsg = new ACLMessage(ACLMessage.INFORM);
			replyMsg.setLanguage(agent.codec.getName());
			replyMsg.setOntology(agent.ontology.getName());
			replyMsg.setConversationId(conversationID);
			for(String ambulanceIDs : ambulances)
			{
				System.out.println("Send Terminaction Message to  " + ambulanceIDs);
				replyMsg.addReceiver(new AID(ambulanceIDs,AID.ISLOCALNAME));
			}
			IncidentCompletionAction completionAction = new IncidentCompletionAction();
			completionAction.setconversationID(conversationID);
			try {
				agent.getContentManager().fillContent(replyMsg, new Action(agent.getAID(),completionAction) );
			} catch (Exception e) {
				e.printStackTrace();
			} 
			agent.send(replyMsg);
		}
		
	}
	
	private void sendNoAmbulanceSentMessage()
	{
		ACLMessage replyMsg = new ACLMessage(ACLMessage.INFORM);
		replyMsg.setLanguage(agent.codec.getName());
		replyMsg.setOntology(agent.ontology.getName());
		replyMsg.setConversationId(conversationID);
		replyMsg.addReceiver(new AID("PoliceManagementAgent",AID.ISLOCALNAME));
		AmbulanceNotSentAction action = new AmbulanceNotSentAction();
		try {
			agent.getContentManager().fillContent(replyMsg, new Action(agent.getAID(),action) );
		} catch (Exception e) {
			e.printStackTrace();
		} 
		agent.send(replyMsg);
		
	}
	
	String conversationID;
	HospitalManagementAgent agent;
	ReportAccidentAction currentIncident;
	int initiatedambulance_count = 0;
	Set<String> ambulances;
	
	
	
}


class HospitalManagementBehaviour extends SimpleBehaviour
{
	public HospitalManagementBehaviour(HospitalManagementAgent a) {
		super(a);
		this.agent = a;
	}

	@Override
	public void action() {
		
		//MessageTemplate mt = MessageTemplate.MatchSender(new AID("ReportingAgent",AID.ISLOCALNAME));
				
		ACLMessage msg = agent.receive();
		if(msg == null)
		{
			return;
		}
		
		 switch(msg.getPerformative())
	     {
	        case (ACLMessage.INFORM):
	        	ContentElement content = null;
				try {
						content = agent.getContentManager().extractContent(msg);
				} catch (Exception e) {
					e.printStackTrace();
				}
				Concept action = ((Action)content).getAction();
				if(action instanceof ReportAccidentAction)
				{
					agent.addBehaviour(new IncidentHandlingBehaviuor(agent,msg.getConversationId(),(ReportAccidentAction)action));
					
				}
				else 
				{
					System.out.println("Unknown Message Type Recived");
				}
	        	break;
	        default:
	        	System.out.println("Invlid Request Type Recived");
	      }
		
	}

	@Override
	public boolean done() {
		// TODO Auto-generated method stub
		return false;
	}
	
	HospitalManagementAgent agent;
	
}


public class HospitalManagementAgent extends Agent 
{
	
	@Override
	protected void setup() 
    { 

        getContentManager().registerLanguage(codec);
        getContentManager().registerOntology(ontology);
		
		 DFAgentDescription dfd = new DFAgentDescription();
	     dfd.setName( getAID() );
	     ServiceDescription sd  = new ServiceDescription();
	     sd.setType( "HospitalManagementAgent" );
	     sd.setName( getLocalName() );
	     dfd.addServices(sd);
	     
	     try {  
	            DFService.register(this, dfd );  
	        }
	        catch (FIPAException fe) { fe.printStackTrace(); }
		
        System.out.println("Hello World. ");
        System.out.println("My name is "+ getLocalName()); 
        addBehaviour(new HospitalManagementBehaviour(this));
    }
	
	public void logAction(String logMessage)
	{
		System.out.println(getLocalName()+"  ************************");
		System.out.println(logMessage);
		System.out.println("***************************");
		
	}
	
	@Override
	protected void takeDown() 
    {
       try { DFService.deregister(this); }
       catch (Exception e) {}
    }
	
	public Codec codec = new SLCodec();
	public Ontology ontology = DisasterManagement.getInstance();
}

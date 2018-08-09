package MSCPro.agents;

import javax.crypto.NullCipher;

import MSCPro.actions.AmbulanceAccidentAction;
import MSCPro.actions.HospitalDropOffCompletionAction;
import MSCPro.actions.IncidentCompletionAction;
import MSCPro.actions.ReportAccidentAction;
import MSCPro.ontology.DisasterManagement;
import jade.content.Concept;
import jade.content.ContentElement;
import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.Ontology;
import jade.content.onto.basic.Action;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.SimpleBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;


class AmbulanceAgentBehaviour extends SimpleBehaviour
{
	public AmbulanceAgentBehaviour(AmbulanceAgent agent,String location, String conversationID)
	{
		this.agent = agent;
		this.location = location;
		this.conversationID = conversationID;
		
	}
	
	@Override
	public void onStart()
	{
		agent.logMessage("Ambulance is dispatched to "+ location);
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
		else if (action instanceof AmbulanceAccidentAction )
		{
			onAmbulanceAccident((AmbulanceAccidentAction)action);
		}
			
		
		block();
	
	}

	
	@Override
	public boolean done() {
		return false;
	}
	
	void onAmbulanceAccident(AmbulanceAccidentAction action)
	{

		ACLMessage replyToHospitalMsg = new ACLMessage(ACLMessage.INFORM);
		action.setAgentName(agent.getLocalName());
		replyToHospitalMsg.setConversationId(this.conversationID);
		replyToHospitalMsg.setLanguage(agent.codec.getName());
		replyToHospitalMsg.setOntology(agent.ontology.getName());
		replyToHospitalMsg.addReceiver(new AID("HospitalManagementAgent",AID.ISLOCALNAME));
		try {
			agent.getContentManager().fillContent(replyToHospitalMsg, new Action(agent.getAID(),action) );
		} catch (Exception e) {
			e.printStackTrace();
		} 
		agent.send(replyToHospitalMsg);
		
		ACLMessage reportIncidentMsg = new ACLMessage(ACLMessage.INFORM);
		ReportAccidentAction reportAction = new ReportAccidentAction();
		reportAction.setCasualtiesCount(action.getCasualtiesCount());
		reportAction.setLocation(action.getLocation());
		reportAction.setSeverity(action.getSeverity());

		reportIncidentMsg.setLanguage(agent.codec.getName());
		reportIncidentMsg.setOntology(agent.ontology.getName());
		reportIncidentMsg.addReceiver(new AID("ReportingAgent",AID.ISLOCALNAME));
		try {
			agent.getContentManager().fillContent(reportIncidentMsg, new Action(agent.getAID(),reportAction) );
		} catch (Exception e) {
			e.printStackTrace();
		} 
		
		agent.send(reportIncidentMsg);
		
		agent.logMessage("Met with an accident ambulance terminating");
		agent.doDelete();
	}
	
	
	String location;
	String conversationID;
	AmbulanceAgent agent;
	
}


public class AmbulanceAgent  extends Agent{

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
		
		addBehaviour(new AmbulanceAgentBehaviour(this,location,conversationID));
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

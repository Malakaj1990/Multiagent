package MSCPro.agents;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.LongSummaryStatistics;
import java.util.Properties;
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
import jade.core.behaviours.ParallelBehaviour;
import jade.core.behaviours.SequentialBehaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

class IncidentHandlingBehaviuor extends SimpleBehaviour
{
	public IncidentHandlingBehaviuor(HospitalManagementAgent agent,String conversationID,ReportAccidentAction incident,
			HospitalManagementBehaviour parentBehaviour)
	{
		this.conversationID = conversationID;
		this.agent = agent;
		this.currentIncident = incident;
		this.ambulances = new HashSet<String>();
		this.parentBehaviour = parentBehaviour;
	
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
			parentBehaviour.assignAmbulance(this);
		
			if(ambulances.size() == 0)
			{
				sendNoAmbulanceSentMessage();
			}
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
		System.out.println("IncidentHandlingBehaviuor Message Recived");
		ContentElement content = null;
		try {
				content = agent.getContentManager().extractContent(msg);
		} catch (Exception e) {
			e.printStackTrace();
			return;
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
			System.out.println("IncidentHandlingBehaviuor Unknown Message type recived = " + msg.getConversationId());
		}
		
	
		
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
		ambulances.remove(action.getAgentName());
		parentBehaviour.assignAmbulance(this);
	}
	
	public void createNewAmbulance()
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
	
	public void removeOneAmbulance()
	{
		
		if(ambulances.size() <= 0 )
		{
			System.out.println("Invalid removal Size");
		}
		ACLMessage replyMsg = new ACLMessage(ACLMessage.INFORM);
		replyMsg.setLanguage(agent.codec.getName());
		replyMsg.setOntology(agent.ontology.getName());
		replyMsg.setConversationId(conversationID);
		
		String ambulanceID = ambulances.iterator().next();
		replyMsg.addReceiver(new AID(ambulanceID,AID.ISLOCALNAME));
		ambulances.remove(ambulanceID);
		
		IncidentCompletionAction completionAction = new IncidentCompletionAction();
		completionAction.setconversationID(conversationID);
		try {
			agent.getContentManager().fillContent(replyMsg, new Action(agent.getAID(),completionAction) );
		} catch (Exception e) {
			e.printStackTrace();
		} 
		agent.send(replyMsg);
		
		if(ambulances.size() == 0)
		{
			sendNoAmbulanceSentMessage();
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
				parentBehaviour.onReleaseAmbulance();
			}
			IncidentCompletionAction completionAction = new IncidentCompletionAction();
			completionAction.setconversationID(conversationID);
			try {
				agent.getContentManager().fillContent(replyMsg, new Action(agent.getAID(),completionAction) );
			} catch (Exception e) {
				e.printStackTrace();
			} 
			agent.send(replyMsg);
			isActive = false;
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
	
	public int getCurrentCost()
	{
		return getCost(ambulances.size());
	}
	
	public int getCostAfterUpdate(int updateCount)
	{
		return getCost((ambulances.size() + updateCount));
		
	}
	
	public int getCost(int ambulanceCount)
	{
		
		String severity = currentIncident.getSeverity();
		
		if(ambulanceCount < 0 )
		{
			return 10000;
		}
		
		if(severity.equalsIgnoreCase("LOW"))
		{
			return 0;
		}
		
		if(ambulanceCount == 0)
		{
			if(severity.equalsIgnoreCase("MEDIUM"))
			{
				return 500;
			}
			else if(severity.equalsIgnoreCase("HIGH"))
			{
				return 1000;
			}
			else 
			{
				return 10000;
			}
					
		}
	
		if(currentIncident.getCasualtiesCount()/ambulanceCount < 1)
		{
			return 1000;
		}
		
		if(severity.equalsIgnoreCase("MEDIUM"))
		{
			return 500/(ambulanceCount+1);
		}
		
		if(severity.equalsIgnoreCase("HIGH"))
		{
			return 1000/(ambulanceCount+1);
		}
		return 10000;
		
	}
	
	int getAmbulanceSize()
	{
		return ambulances.size();
	}
	
	boolean isActive()
	{
		return isActive;
	}
	
	String conversationID;
	HospitalManagementAgent agent;
	ReportAccidentAction currentIncident;
	int initiatedambulance_count = 0;
	Set<String> ambulances;
	HospitalManagementBehaviour parentBehaviour;
	
	boolean isActive = true;
	
	
	
}


class HospitalManagementBehaviour extends SimpleBehaviour
{
	public HospitalManagementBehaviour(HospitalManagementAgent a) {
		super(a);
		this.agent = a;
		incidentsList = new ArrayList<IncidentHandlingBehaviuor>();
	}

	
	@Override
	public void onStart()
	{
		
		Properties prop = new Properties();
		InputStream input = null;
		try {	
			input = new FileInputStream("src/main/resources/HospitalManagement.properties");	
			prop.load(input);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		ambulancesCount = Integer.parseInt(prop.getProperty("ambulanceCount"));
		agent.logAction("Ambulance Count = " + ambulancesCount);
	
	}
	
	@Override
	public void action() {
		
		//MessageTemplate mt = MessageTemplate.MatchSender(new AID("ReportingAgent",AID.ISLOCALNAME));
				
		ACLMessage msg = agent.receive();
		if(msg == null)
		{
			return;
		}
		if(msg.getConversationId() == null)
		{
			agent.logAction("Conversation ID not set");
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
					IncidentHandlingBehaviuor newIncident = new IncidentHandlingBehaviuor(agent,msg.getConversationId(),(ReportAccidentAction)action,
							this);
					incidentsList.add(newIncident);
					agent.parallelBehaviour.addSubBehaviour(newIncident);
					
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
	
	
	void assignAmbulance(IncidentHandlingBehaviuor requestedBehaviour)
	{
		while(true)
		{
			int currentCost = requestedBehaviour.getCurrentCost();
			int addedCost = requestedBehaviour.getCostAfterUpdate(1);
			if(addedCost >= currentCost)
			{
				break;
			}
			
			if(ambulancesCount > 0 )
			{
				requestedBehaviour.createNewAmbulance();
				--ambulancesCount;
			}
			else
			{
				break;
			}
		}
		
		while (true)
		{
			int gain = requestedBehaviour.getCurrentCost() - requestedBehaviour.getCostAfterUpdate(1);
			int loss = gain;
			
			for(IncidentHandlingBehaviuor behaviour : incidentsList)
			{
				if(behaviour == requestedBehaviour)
				{
					continue;
				}
				if(behaviour.getAmbulanceSize() == 0 )
				{
					continue;
				}
				if(behaviour.isActive() == false)
				{
					continue;
				}
				
				loss =  behaviour.getCostAfterUpdate(-1) - behaviour.getCurrentCost();
				if(gain > loss)
				{
					behaviour.removeOneAmbulance();
					requestedBehaviour.createNewAmbulance();
					break;
				}
			}
			
			if(gain <= loss)
			{
				break;
			}
		}
		
		
	}
	
	void onReleaseAmbulance()
	{
		++ambulancesCount;
	}
	
	HospitalManagementAgent agent;
	List<IncidentHandlingBehaviuor> incidentsList;
	int ambulancesCount =0;
	
	
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
        SequentialBehaviour seq = new SequentialBehaviour();
        addBehaviour( seq );
        seq.addSubBehaviour(parallelBehaviour);
        //addBehaviour(new HospitalManagementBehaviour(this));
        parallelBehaviour.addSubBehaviour(new HospitalManagementBehaviour(this));
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
	
	public ParallelBehaviour parallelBehaviour = new ParallelBehaviour();
}

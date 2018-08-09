package MSCPro.agents;

import MSCPro.actions.AmbulanceNotSentAction;
import MSCPro.actions.HospitalDropOffCompletionAction;
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
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;

class PoliceIncidentHandlingBehaviuor extends SimpleBehaviour
{

	public PoliceIncidentHandlingBehaviuor(PoliceManagementAgent agent,String conversationID,ReportAccidentAction action) {
		this.agent = agent;
		this.conversationID = conversationID;
		this.currentIncident = action;
	}
	@Override
	public void onStart() {
		createNewPoliceAgent();
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
		if(action instanceof AmbulanceNotSentAction)
		{
			ACLMessage replyMsg = new ACLMessage(ACLMessage.INFORM);
			replyMsg.setLanguage(agent.codec.getName());
			replyMsg.setOntology(agent.ontology.getName());
			replyMsg.setConversationId(conversationID);
			replyMsg.addReceiver(new AID(policeAgentID,AID.ISLOCALNAME));
			try {
				agent.getContentManager().fillContent(replyMsg, content);
				agent.send(replyMsg);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			
			
		}
		
	}

	@Override
	public boolean done() {
		
		return false;
	}
	
	void createNewPoliceAgent()
	{
		AgentContainer c = agent.getContainerController();
		
		String agentID = "POL_" +conversationID;
		this.policeAgentID = agentID;
		try {
			Object[] args = new Object[2];
			args[0] = currentIncident.getLocation();
			args[1] = conversationID;
			AgentController policeAgent = c.createNewAgent(agentID,"MSCPro.agents.PoliceAgent", args);
			policeAgent.start();
		} catch (StaleProxyException e) {
			
			e.printStackTrace();
		}
	}
	
	PoliceManagementAgent agent;
	String conversationID;
	ReportAccidentAction currentIncident;
	String policeAgentID;
}




class PoliceManagementBehaviour extends SimpleBehaviour
{
	public PoliceManagementBehaviour(PoliceManagementAgent a) {
		super(a);
		this.agent = a;
	}

	@Override
	public void action() {

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
					agent.addBehaviour(new PoliceIncidentHandlingBehaviuor(agent,msg.getConversationId(),(ReportAccidentAction)action));
					
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
	
	PoliceManagementAgent agent;
}



public class PoliceManagementAgent extends Agent{
	@Override
	protected void setup() 
    { 
		
		getContentManager().registerLanguage(codec);
        getContentManager().registerOntology(ontology);
		
		 DFAgentDescription dfd = new DFAgentDescription();
	     dfd.setName( getAID() );
	     ServiceDescription sd  = new ServiceDescription();
	     sd.setType( "PoliceManagementAgent" );
	     sd.setName( getLocalName() );
	     dfd.addServices(sd);
	     
	     try {  
	            DFService.register(this, dfd );  
	        }
	        catch (FIPAException fe) { fe.printStackTrace(); }
		
        System.out.println("Hello World. ");
        System.out.println("My name is "+ getLocalName()); 
        addBehaviour(new PoliceManagementBehaviour(this));
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

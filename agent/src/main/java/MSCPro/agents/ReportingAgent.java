package MSCPro.agents;


import java.io.IOException;

import MSCPro.actions.ReportAccidentAction;
import MSCPro.ontology.DisasterManagement;
import jade.content.Concept;
import jade.content.ContentElement;
import jade.content.lang.Codec;
import jade.content.lang.Codec.CodecException;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.Ontology;
import jade.content.onto.OntologyException;
import jade.content.onto.UngroundedException;
import jade.content.onto.basic.Action;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.SimpleBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import jade.tools.testagent.ReceiveCyclicBehaviour;
import jade.util.Logger;



class ReportingBehaviour extends SimpleBehaviour
{

	String name;
	ReportingAgent agent;
	int incidentID = 0;

	public ReportingBehaviour(ReportingAgent agent ,String name) {
		super(agent);
		this.name = name;
		this.agent = agent;
	}
	
	
	String generateIncidentID()
	{
		++incidentID;
		String incidentIDStr = "ACC";
		incidentIDStr += incidentID;
		return incidentIDStr;
	}
	
	AID getService(String serviceType)
	{

        DFAgentDescription dfd = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType( serviceType );
        dfd.addServices(sd);
        try
        {
            DFAgentDescription[] result = DFService.search(agent, dfd);
            if (result.length>0)
                return result[0].getName() ;
        }
        catch (FIPAException fe) { fe.printStackTrace(); }
        return null;
	}

	@Override
	public void action() {
		
		
		ACLMessage msg = agent.receive();
		if(msg == null)
		{
			return;
		}
		agent.logger.log(jade.util.Logger.INFO,"Reporting behaviour action");
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
				
				System.out.println("Accident Report Recived");
				ACLMessage replyMsg = new ACLMessage(ACLMessage.INFORM);
				replyMsg.setLanguage(agent.codec.getName());
				replyMsg.setOntology(agent.ontology.getName());
				replyMsg.addReceiver(getService("HospitalManagementAgent"));
				replyMsg.addReceiver(getService("PoliceManagementAgent"));
				replyMsg.setConversationId(generateIncidentID());
				try {
					agent.getContentManager().fillContent(replyMsg, content);
					agent.send(replyMsg);
				} catch (Exception e) {
					e.printStackTrace();
				}
				
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
		return false;
	}
	
	
}

public class ReportingAgent extends Agent
{
	protected void setup() 
    { 
		
	     
        System.out.println("Hello World12. ");
        logger.log(jade.util.Logger.INFO, "Reporting agent on Start"); 
        System.out.println("My name is "+ getLocalName()); 
        addBehaviour(new ReportingBehaviour(this,"Instance1"));
        
        getContentManager().registerLanguage(codec);
        getContentManager().registerOntology(ontology);
           
    }
	
	public Codec codec = new SLCodec();
	public Ontology ontology = DisasterManagement.getInstance();
	
	public Logger logger = jade.util.Logger.getMyLogger(this.getClass().getName());

	
}

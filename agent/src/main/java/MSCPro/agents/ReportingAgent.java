package MSCPro.agents;


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
/**
 * Hello world!
 *
 */
import jade.core.behaviours.SimpleBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.tools.testagent.ReceiveCyclicBehaviour;

class ReportingBehaviour extends SimpleBehaviour
{

	String name;
	Agent a;

	public ReportingBehaviour(Agent agent ,String name) {
		super(agent);
		this.name = name;
		this.a = agent;
	}
	
	
	AID getService(String serviceType)
	{

        DFAgentDescription dfd = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType( serviceType );
        dfd.addServices(sd);
        try
        {
            DFAgentDescription[] result = DFService.search(a, dfd);
            if (result.length>0)
                return result[0].getName() ;
        }
        catch (FIPAException fe) { fe.printStackTrace(); }
        return null;
	}

	@Override
	public void action() {
		
		ACLMessage msg = a.receive();
		/*if(msg != null)
		{
			System.out.println("="
					+ a.getLocalName() + "<-" +
					msg.getContent());
			ACLMessage replyMsg = new ACLMessage(ACLMessage.INFORM);
			replyMsg.setContent("Something bad happend");
			replyMsg.addReceiver(getService("HospitalManagementAgent"));
			a.send(replyMsg);
			block();
		}*/
		
		if(msg != null)
		{
		ContentElement content = null;
		try {
			content = a.getContentManager().extractContent(msg);
		} catch (Exception e) {
			e.printStackTrace();
		
		}
        Concept action = ((Action)content).getAction();
		if(action instanceof ReportAccidentAction)
		{
			System.out.println("Accident Report Recived");
			
		}
		else 
		{
			System.out.println("Unknown");
		}
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
        System.out.println("My name is "+ getLocalName()); 
        addBehaviour(new ReportingBehaviour(this,"Instance1"));
        
        getContentManager().registerLanguage(codec);
        getContentManager().registerOntology(ontology);
        
        ReportAccidentAction reportAccident = new ReportAccidentAction();
    	int i = 15;
    	float amount = (float)10.1;
    	reportAccident.setSeverity("LOW");
    	reportAccident.setCasualtiesCount(100);
    	reportAccident.setLocation("Location1");
    	
    	
    	ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
    	msg.setLanguage(codec.getName());
    	msg.setOntology(ontology.getName());
    	try {
			getContentManager().fillContent(msg, new Action( new AID( "ReportingAgent", AID.ISLOCALNAME ), reportAccident));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	System.out.println(msg.getLanguage()+" " + msg.getOntology() + " " );
    	System.out.println(msg.getContent());
    	
    	
        
    }
	
	public Codec codec = new SLCodec();
	public Ontology ontology = DisasterManagement.getInstance();
	

	
}

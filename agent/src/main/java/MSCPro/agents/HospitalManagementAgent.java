package MSCPro.agents;

import jade.core.Agent;
import jade.core.behaviours.SimpleBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;

class HospitalManagementBehaviour extends SimpleBehaviour
{
	public HospitalManagementBehaviour(Agent a) {
		super(a);
		this.agent = a;
	}

	@Override
	public void action() {
		ACLMessage msg = agent.receive();
		if(msg != null)
		{
			System.out.println("="
					+ agent.getLocalName() + "<-" +
					msg.getContent());
			block();
		}
		
	}

	@Override
	public boolean done() {
		// TODO Auto-generated method stub
		return false;
	}
	
	Agent agent;
}


public class HospitalManagementAgent extends Agent 
{
	
	@Override
	protected void setup() 
    { 
		
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
	
	@Override
	protected void takeDown() 
    {
       try { DFService.deregister(this); }
       catch (Exception e) {}
    }
}

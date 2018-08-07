package MSCPro.ontology;

import jade.content.onto.*;
import jade.content.schema.*;
import MSCPro.actions.*;

public class DisasterManagement extends Ontology {

	public static final String ONTOLOGY_NAME = "Disaster-Management";
	
	private static Ontology instance = new DisasterManagement();

	public static Ontology getInstance() { return instance; }
	
	private DisasterManagement()
	{
		super(ONTOLOGY_NAME,BasicOntology.getInstance());
		System.out.println("Ontology initiated");
		
		try {
		
			System.out.println("Get Schema " + getSchema(Integer.class));
			AgentActionSchema reportingAS = new AgentActionSchema(ReportAccidentAction.REPORTING_ACCIDENT);
			add(reportingAS, ReportAccidentAction.class);
			reportingAS.add(ReportAccidentAction.REPORTING_ACCIDENT_CASUALTIES_COUNT, (PrimitiveSchema) getSchema(BasicOntology.INTEGER), ObjectSchema.MANDATORY);
			reportingAS.add(ReportAccidentAction.REPORTING_ACCIDENT_LOCATION, (PrimitiveSchema) getSchema(BasicOntology.STRING), ObjectSchema.MANDATORY);
			reportingAS.add(ReportAccidentAction.REPORTING_ACCIDENT_SEVERITY, (PrimitiveSchema) getSchema(BasicOntology.STRING), ObjectSchema.MANDATORY);
			
			AgentActionSchema IncidentComAS = new AgentActionSchema(IncidentCompletionAction.INCIDENT_COMPLETE_ACTION);
			add(IncidentComAS,IncidentCompletionAction.class);
			IncidentComAS.add(IncidentCompletionAction.INCIDENT_COMPLETE_CONVERSATION_ID,  (PrimitiveSchema) getSchema(BasicOntology.STRING),ObjectSchema.MANDATORY);
			
			AgentActionSchema dropOffCompAS = new AgentActionSchema(HospitalDropOffCompletionAction.HOSPITAL_COMPLETION_DROPOFF_ACTION);
			add(dropOffCompAS,HospitalDropOffCompletionAction.class);
			dropOffCompAS.add(HospitalDropOffCompletionAction.HOSPITAL_COMPLETION_DROPOFF_COUNT, (PrimitiveSchema) getSchema(BasicOntology.INTEGER),ObjectSchema.MANDATORY);
		}
		catch(OntologyException oe) {
			oe.printStackTrace();
		}
	}
}

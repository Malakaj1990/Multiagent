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
			AgentActionSchema as = new AgentActionSchema(ReportAccidentAction.REPORTING_ACCIDENT);
			add(as, ReportAccidentAction.class);
			as.add(ReportAccidentAction.REPORTING_ACCIDENT_CASUALTIES_COUNT, (PrimitiveSchema) getSchema(BasicOntology.INTEGER), ObjectSchema.MANDATORY);
			as.add(ReportAccidentAction.REPORTING_ACCIDENT_LOCATION, (PrimitiveSchema) getSchema(BasicOntology.STRING), ObjectSchema.MANDATORY);
			as.add(ReportAccidentAction.REPORTING_ACCIDENT_SEVERITY, (PrimitiveSchema) getSchema(BasicOntology.STRING), ObjectSchema.MANDATORY);
			
		}
		catch(OntologyException oe) {
			oe.printStackTrace();
		}
	}
}

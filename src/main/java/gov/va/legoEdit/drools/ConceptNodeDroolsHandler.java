package gov.va.legoEdit.drools;

import gov.va.legoEdit.drools.actions.DroolsLegoAction;
import gov.va.legoEdit.drools.definitions.IsKindOfEvaluatorDefinition;
import gov.va.legoEdit.drools.facts.AssertionFact;
import gov.va.legoEdit.drools.facts.ConceptFact;
import gov.va.legoEdit.drools.facts.Context;
import gov.va.legoEdit.drools.facts.PendingConceptFact;
import gov.va.legoEdit.drools.manager.DroolsExceptionHandler;
import gov.va.legoEdit.gui.legoTreeView.ConceptUsageType;
import gov.va.legoEdit.model.schemaModel.Concept;
import gov.va.legoEdit.model.schemaModel.Relation;
import gov.va.legoEdit.storage.wb.WBDataStore;
import gov.va.legoEdit.storage.wb.WBUtility;
import gov.va.legoEdit.util.Utility;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseConfiguration;
import org.drools.KnowledgeBaseFactory;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderConfiguration;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.builder.conf.EvaluatorOption;
import org.drools.conf.ConsequenceExceptionHandlerOption;
import org.drools.io.Resource;
import org.drools.io.ResourceFactory;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.rule.ConsequenceExceptionHandler;
import org.ihtsdo.bdb.BdbTerminologyStore;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.coordinate.StandardViewCoordinates;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Much of this class is cribbed from TK DroolsExecutionManager
 * 
 * @author jefron
 * @author darmbrust
 */
public class ConceptNodeDroolsHandler
{
	public static String drools_dialect_java_compiler;
	public static File droolsRootFolder = new File("drools-rules");
	
	private static Boolean initFailed = null;
	private static volatile ConceptNodeDroolsHandler instance_;
	
	private KnowledgeBase kbase;
	private KnowledgeBaseConfiguration kBaseConfig;
	private static Logger logger = LoggerFactory.getLogger(ConceptNodeDroolsHandler.class);

	/**
	 * Check for null from this return - returns null if init fails
	 */
	public static ConceptNodeDroolsHandler getInstance()
	{
		if (instance_ == null)
		{
			if (initFailed != null && initFailed)
			{
				return null;
			}
			synchronized (ConceptNodeDroolsHandler.class)
			{
				try
				{
					if (instance_ == null)
					{
						instance_ = new ConceptNodeDroolsHandler();
						initFailed = false;
					}
				}
				catch (Exception e)
				{
					logger.error("Drools Init failed!", e);
					initFailed = true;
				}
			}
		}
		return instance_;
	}

	private ConceptNodeDroolsHandler()
	{
		logger.debug("Initializing Drools Knowledgebase");
		
		if (!(WBDataStore.Ts() instanceof BdbTerminologyStore))
		{
			throw new RuntimeException("Drools is only supported with local datastores");
		}
		
		HashMap<Resource, ResourceType> resources = new HashMap<Resource, ResourceType>();
		if (droolsRootFolder.exists())
		{
			for (File f : droolsRootFolder.listFiles())
			{
				if (f.isFile() && f.getName().toLowerCase().endsWith(".drl"))
				{
					logger.debug("Loading Rule " + f.getAbsolutePath());
					resources.put(ResourceFactory.newFileResource(f), ResourceType.DRL);
				}
			}
		}
		else
		{
			logger.error("The drools-rules folder is missing!  Was looking for " + droolsRootFolder.getAbsolutePath());
		}

		Properties props = new Properties();
		if (drools_dialect_java_compiler != null)
		{
			props.setProperty("drools.dialect.java.compiler", drools_dialect_java_compiler);
		}
		KnowledgeBuilderConfiguration builderConfig = KnowledgeBuilderFactory.newKnowledgeBuilderConfiguration(props, (ClassLoader[]) null);

		builderConfig.setOption(EvaluatorOption.get(IsKindOfEvaluatorDefinition.IS_KIND_OF.getOperatorString(), new IsKindOfEvaluatorDefinition()));

		kbase = KnowledgeBaseFactory.newKnowledgeBase();
		KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder(kbase, builderConfig);
		for (Resource resource : resources.keySet())
		{
			kbuilder.add(resource, resources.get(resource));
		}
		if (kbuilder.hasErrors())
		{
			throw new RuntimeException(kbuilder.getErrors().toString());
		}

		kBaseConfig = KnowledgeBaseFactory.newKnowledgeBaseConfiguration();

		Class<? extends ConsequenceExceptionHandler> exHandlerClass = DroolsExceptionHandler.class;

		ConsequenceExceptionHandlerOption cehOption = ConsequenceExceptionHandlerOption.get(exHandlerClass);

		kBaseConfig.setOption(cehOption);
		if (drools_dialect_java_compiler != null)
		{
			kBaseConfig.setProperty("drools.dialect.java.compiler", drools_dialect_java_compiler);
		}

		kbase = KnowledgeBaseFactory.newKnowledgeBase(kBaseConfig);
		kbase.addKnowledgePackages(kbuilder.getKnowledgePackages());
		
		logger.debug("Drools Initialization Complete");
	}

	public List<DroolsLegoAction> processConceptNodeRules(ConceptVersionBI concept, ConceptUsageType usageType) throws IOException
	{
		ArrayList<DroolsLegoAction> actions = new ArrayList<>();

		StatefulKnowledgeSession ksession = kbase.newStatefulKnowledgeSession();
		try
		{
			ksession.setGlobal("actions", actions);
			ksession.insert(new ConceptFact(Context.DROP_OBJECT, concept, StandardViewCoordinates.getSnomedLatest()));
			ksession.insert(new AssertionFact(usageType));
			ksession.fireAllRules();

			return actions;
		}
		finally
		{
			ksession.dispose();
		}
	}

	public List<DroolsLegoAction> processConceptNodeRelationshipRules(ConceptVersionBI cv, Relation r) throws IOException
	{
		ArrayList<DroolsLegoAction> actions = new ArrayList<>();

		StatefulKnowledgeSession ksession = kbase.newStatefulKnowledgeSession();
		try
		{
			if (r == null || r.getType() == null || r.getType().getConcept() == null || Utility.isEmpty(r.getType().getConcept().getUuid()) 
					|| r.getDestination() == null || r.getDestination().getExpression() == null || r.getDestination().getExpression().getConcept() == null 
					|| Utility.isEmpty(r.getDestination().getExpression().getConcept().getUuid()))
			{
				//Not enough info yet to run the drools validator. 
				return actions;
			}

			Concept type = r.getType().getConcept();
			Concept dest = r.getDestination().getExpression().getConcept();

			ConceptVersionBI typeConcept = WBUtility.lookupSnomedIdentifierAsCV(type.getUuid());
			ConceptVersionBI destConcept = WBUtility.lookupSnomedIdentifierAsCV(dest.getUuid());

			ksession.insert(new ConceptFact(Context.SOURCE_CONCEPT, cv, StandardViewCoordinates.getSnomedLatest()));

			if (typeConcept != null)
			{
				ksession.insert(new ConceptFact(Context.TYPE_CONCEPT, typeConcept, StandardViewCoordinates.getSnomedLatest()));
			}
			else
			{
				ksession.insert(new PendingConceptFact(Context.TYPE_CONCEPT, type));
			}
			if (destConcept != null)
			{
				ksession.insert(new ConceptFact(Context.DESTINATION_CONCEPT, destConcept, StandardViewCoordinates.getSnomedLatest()));
			}
			else
			{
				ksession.insert(new PendingConceptFact(Context.DESTINATION_CONCEPT, dest));
			}

			ksession.setGlobal("actions", actions);
			ksession.fireAllRules();

			return actions;
		}
		finally
		{
			ksession.dispose();
		}
	}
}

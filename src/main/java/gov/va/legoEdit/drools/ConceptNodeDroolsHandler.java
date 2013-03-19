package gov.va.legoEdit.drools;

import gov.va.legoEdit.drools.actions.DroolsLegoAction;
import gov.va.legoEdit.drools.definitions.IsKindOfEvaluatorDefinition;
import gov.va.legoEdit.drools.facts.AssertionFact;
import gov.va.legoEdit.drools.facts.ConceptFact;
import gov.va.legoEdit.drools.facts.Context;
import gov.va.legoEdit.drools.manager.DroolsExceptionHandler;
import gov.va.legoEdit.gui.legoTreeView.ConceptUsageType;
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
	private static volatile ConceptNodeDroolsHandler instance_;
	public static String drools_dialect_java_compiler;
	public static File droolsRootFolder = new File("drools-rules");
	private KnowledgeBase kbase;
	private KnowledgeBaseConfiguration kBaseConfig;
	private static Logger logger = LoggerFactory.getLogger(ConceptNodeDroolsHandler.class);

	public static ConceptNodeDroolsHandler getInstance()
	{
		if (instance_ == null)
		{
			synchronized (ConceptNodeDroolsHandler.class)
			{
				if (instance_ == null)
				{
					instance_ = new ConceptNodeDroolsHandler();
				}
			}
		}
		return instance_;
	}

	private ConceptNodeDroolsHandler()
	{
		logger.debug("Initializing Drools Knowledgebase");
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
}

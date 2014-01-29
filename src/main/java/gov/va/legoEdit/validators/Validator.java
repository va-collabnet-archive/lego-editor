package gov.va.legoEdit.validators;

import gov.va.legoEdit.drools.ConceptNodeDroolsHandler;
import gov.va.legoEdit.drools.actions.DroolsLegoAction;
import gov.va.legoEdit.gui.legoTreeView.ConceptUsageType;
import gov.va.legoEdit.gui.legoTreeView.LegoTreeItem;
import gov.va.legoEdit.model.schemaModel.Assertion;
import gov.va.legoEdit.model.schemaModel.AssertionComponent;
import gov.va.legoEdit.model.schemaModel.Bound;
import gov.va.legoEdit.model.schemaModel.Concept;
import gov.va.legoEdit.model.schemaModel.Destination;
import gov.va.legoEdit.model.schemaModel.Discernible;
import gov.va.legoEdit.model.schemaModel.Expression;
import gov.va.legoEdit.model.schemaModel.Measurement;
import gov.va.legoEdit.model.schemaModel.Point;
import gov.va.legoEdit.model.schemaModel.PointDouble;
import gov.va.legoEdit.model.schemaModel.PointLong;
import gov.va.legoEdit.model.schemaModel.PointMeasurementConstant;
import gov.va.legoEdit.model.schemaModel.Qualifier;
import gov.va.legoEdit.model.schemaModel.Relation;
import gov.va.legoEdit.model.schemaModel.RelationGroup;
import gov.va.legoEdit.model.schemaModel.Value;
import gov.va.legoEdit.storage.wb.WBUtility;
import java.util.List;
import java.util.UUID;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handle the validation of various aspects of the Lego.  The LegoTreeView uses this to display error markers, and reasons.
 * Additional validators should be registered with this class (apis to be determined) - these validators are automatically called
 * when content changes as necessary.
 * 
 * Validators should return null if there is no problem.  They should return a user-friendly string error message if something is wrong.
 * The error message will be displayed in the Lego tree.
 * 
 * Validators run in a background (non GUI) thread.
 * 
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 * Copyright 2013
 */
public class Validator
{
	private static Logger logger = LoggerFactory.getLogger(Validator.class);
	public static final String UPPER_BOUND = "upper bound";
	public static final String LOWER_BOUND = "lower bound";
	
	public static String isValid(Object o, LegoTreeItem lti)
	{
		if (o instanceof AssertionComponent)
		{
			return validate((AssertionComponent) o, lti);
		}
		else if (o instanceof Value)
		{
			return validate((Value) o, lti);
		}
		else if (o instanceof Destination)
		{
			return validate((Destination) o, lti);
		}
		else if (o instanceof Concept)
		{
			return validate((Concept) o, lti);
		}
		else if (o instanceof Measurement)
		{
			return validate((Measurement) o, lti);
		}
		else if (o instanceof Assertion)
		{
			return validate((Assertion)o, lti);
		}
		else if (o instanceof Expression)
		{
			return validate((Expression)o, lti);
		}
		else if (o instanceof Relation)
		{
			return validate((Relation)o, lti);
		}
		else if (o instanceof Discernible)
		{
			return validate((Discernible)o, lti);
		}
		else if (o instanceof Qualifier)
		{
			return validate((Qualifier)o, lti);
		}
		else if (o instanceof RelationGroup)
		{
			return validate((RelationGroup)o, lti);
		}
		
		else
		{
			logger.error("Validation requested on unsuported type " + o);
			return null;
		}
	}

	public static String validate(AssertionComponent ac, LegoTreeItem lti)
	{
		try
		{
			UUID.fromString(ac.getAssertionUUID());
			
			//Also validate the type, since there is no longer a special node for type.
			if (ac.getType() == null)
			{
				return "Assertion Components are required to have a type";
			}
			
			return validate(ac.getType().getConcept(), lti);
		}
		catch (Exception e)
		{
			return "The provided value is not a valid UUID";
		}
		
	}

	public static String validate(Value value, LegoTreeItem lti)
	{
		if (value.getText() != null)
		{
			if (value.getText().length() == 0)
			{
				return "A non-empty string value must be supplied";
			}
			else
			{
				return null;
			}
		}
		else if (value.isBoolean() != null)
		{
			// hard to get a boolean wrong...
			return null;
		}
		else if (value.getExpression() != null || value.getMeasurement() != null)
		{
			return null;
		}

		return "Value cannot be empty";
	}

	public static String validate(Destination destination, LegoTreeItem lti)
	{
		if (destination.getText() != null)
		{
			if (destination.getText().length() == 0)
			{
				return "A non-empty string value must be supplied";
			}
			else
			{
				return null;
			}
		}
		else if (destination.isBoolean() != null)
		{
			// hard to get a boolean wrong...
			return null;
		}
		else if (destination.getExpression() != null || destination.getMeasurement() != null)
		{
			return null;
		}
		return "Destination cannot be empty";
	}

	public static String validate(Concept concept, LegoTreeItem lti)
	{
		if (concept == null)
		{
			return "The concept must be specified";
		}
		// better validation is still handled on the GUI side - but when it fails lookup, at least one of these gets set to null
		if (concept.getSctid() == null && concept.getUuid() == null)
		{
			return "The concept is not properly specified";
		}
		else
		{
			try
			{
				ConceptVersionBI cv = WBUtility.lookupSnomedIdentifierAsCV(concept.getUuid());
				if (cv != null)
				{
					ConceptNodeDroolsHandler cndh = ConceptNodeDroolsHandler.getInstance();
					if (cndh != null)
					{
						List<DroolsLegoAction> actions = cndh.processConceptNodeRules(cv, lti.getConceptUsageType());
						if (actions.size() > 0)
						{
							return actions.get(0).getFailureReason();
						}
						
						if ((lti.getConceptUsageType() == ConceptUsageType.DISCERNIBLE 
								|| lti.getConceptUsageType() == ConceptUsageType.QUALIFIER
								|| lti.getConceptUsageType() == ConceptUsageType.VALUE 
								|| lti.getConceptUsageType() == ConceptUsageType.REL_DESTINATION)
								&& lti.getExtraData() != null && lti.getExtraData() instanceof Expression)
						{
							Expression exp = (Expression) lti.getExtraData();
							//Note - this implementation doesn't yet handle concept rel groups 
							for (Relation rel : exp.getRelation())
							{
								actions = cndh.processConceptNodeRelationshipRules(cv, rel);
								if (actions.size() > 0)
								{
									return actions.get(0).getFailureReason();
								}
							}
						}
					}
					else
					{
						logger.warn("Drools validation is not available due to previous failures");
					}
				} 
				else 
				{
					logger.debug("Can't run drools validation on pending concept");
				}
			}
			catch (Exception e)
			{
				logger.error("Drools validation failed!", e);
			}
			return null;
		}
	}

	public static String validate(Measurement measurement, LegoTreeItem lti)
	{
		if (hasValue(measurement.getPoint()))
		{
			return null;
		}
		else if (measurement.getBound() != null)
		{
			// At least one should have a value
			if (!hasValue(measurement.getBound().getLowerPoint()) && !hasValue(measurement.getBound().getUpperPoint()))
			{
				return "At least one value must be specified";
			}

			// lower should be less than upper
			return validateBoundLeftLessThanRight(measurement.getBound(), "bound");
		}
		else if (measurement.getInterval() != null)
		{
			if ((measurement.getInterval().getLowerBound() == null || 
					!hasValue(measurement.getInterval().getLowerBound().getLowerPoint()) && !hasValue(measurement.getInterval().getLowerBound().getUpperPoint()))
					&& (measurement.getInterval().getUpperBound() == null || 
					!hasValue(measurement.getInterval().getUpperBound().getLowerPoint()) && !hasValue(measurement.getInterval().getUpperBound().getUpperPoint())))
			{
				return "At least one value must be specified";
			}
				
			// lower should be less than upper, on each of the lower bound and the upper bound.
			String boundError = validateBoundLeftLessThanRight(measurement.getInterval().getLowerBound(), LOWER_BOUND);
			if (boundError != null)
			{
				return boundError;
			}
			
			boundError = validateBoundLeftLessThanRight(measurement.getInterval().getUpperBound(), UPPER_BOUND);
			if (boundError != null)
			{
				return boundError;
			}
			
			if ((isNumber(measurement.getInterval().getLowerBound().getLowerPoint()) || isNumber(measurement.getInterval().getLowerBound().getUpperPoint()))
					&& (isNumber(measurement.getInterval().getUpperBound().getLowerPoint()) || isNumber(measurement.getInterval().getUpperBound().getUpperPoint())))
			{
				double lower = (isNumber(measurement.getInterval().getLowerBound().getUpperPoint()) ? 
						getNumber(measurement.getInterval().getLowerBound().getUpperPoint()).doubleValue() : 
							getNumber(measurement.getInterval().getLowerBound().getLowerPoint()).doubleValue());
				double upper = (isNumber(measurement.getInterval().getUpperBound().getLowerPoint()) ? 
						getNumber(measurement.getInterval().getUpperBound().getLowerPoint()).doubleValue() : 
							getNumber(measurement.getInterval().getUpperBound().getUpperPoint()).doubleValue());
				if (upper <= lower)
				{
					return "The lower interval must be less than the upper interval";
				}
			}
			return null;
		}
		else
		{
			return "A measurement value is required";
		}
	}
	
	private static String validateBoundLeftLessThanRight(Bound b, String type)
	{
		if (isNumber(b.getLowerPoint()) && isNumber(b.getUpperPoint()))
		{
			if (getNumber(b.getLowerPoint()).doubleValue() >= 
					getNumber(b.getUpperPoint()).doubleValue())
			{
				return "The " + type + " left number must be less than the " + type + " right number";
			}
		}
		return null;
	}

	public static String validate(Expression e, LegoTreeItem lti)
	{
		if (e == null)
		{
			return "The Expression cannot be null";
		}
		if (e.getConcept() == null && e.getExpression().size() == 0)
		{
			return "An expression must contain 1 or more concepts";
		}
		if (e.getConcept() != null && e.getExpression().size() == 0)
		{
			//The concept is displayed on the expression node in these cases.
			//also need to validate the concept
			return validate(e.getConcept(), lti);
		}

		return null;
	}

	public static String validate(Relation r, LegoTreeItem lti)
	{
		if (r.getType() == null)
		{
			return "A Relationship must have a type";
		}
		if (r.getDestination() == null 
				|| (r.getDestination().getExpression() == null && r.getDestination().getMeasurement() == null 
					&& r.getDestination().getText() == null && r.getDestination().isBoolean() == null))
		{
			return "A Relationship must have a Destination";
		}
		//also validate the type concept, because it doesn't have its own node anymore.
		return validate(r.getType().getConcept(), lti);
	}
	
	public static String validate(RelationGroup rg, LegoTreeItem lti)
	{
		if (rg.getRelation().size() == 0)
		{
			return "A Relationship Group must have at least one relationship";
		}
		return null;
	}
	
	public static String validate(Assertion a, LegoTreeItem lti)
	{
		if (a.getValue() == null)
		{
			return "An assertion must have a value";
		}
		if (a.getDiscernible() == null)
		{
			return "An assertion must have a Discernible";
		}
		if (a.getQualifier() == null)
		{
			return "An assertion must have a Qualifier";
		}
		
		if (a.getTiming() != null)
		{
			//TODO check for timing units, of right type - need the snomed concept first.
		}
		return null;
	}
	
	public static String validate(Discernible d, LegoTreeItem lti)
	{
		if (d == null)
		{
			return "A Discernible is required";
		}
		return null;
	}
	
	public static String validate(Qualifier q, LegoTreeItem lti)
	{
		if (q == null)
		{
			return "A Qualifier is required";
		}
		return null;
	}

	private static boolean hasValue(Point p)
	{
		if (p == null)
		{
			return false;
		}
		if (p instanceof PointLong)
		{
			return true;
		}
		else if (p instanceof PointDouble)
		{
			return true;
		}
		else if (p instanceof PointMeasurementConstant)
		{
			return ((PointMeasurementConstant) p).getValue() != null;
		}
		return false;
	}

	private static boolean isNumber(Point p)
	{
		if (p instanceof PointLong)
		{
			return true;
		}
		else if (p instanceof PointDouble)
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	private static Number getNumber(Point p)
	{
		if (p instanceof PointLong)
		{
			return ((PointLong) p).getValue();
		}
		else if (p instanceof PointDouble)
		{
			return ((PointDouble) p).getValue();
		}
		else
		{
			return null;
		}
	}
}

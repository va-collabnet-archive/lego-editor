package gov.va.legoEdit.gui.legoTreeView;

import gov.va.legoEdit.gui.util.LegoTreeItemComparator;
import gov.va.legoEdit.model.LegoListByReference;
import gov.va.legoEdit.model.LegoReference;
import gov.va.legoEdit.model.schemaModel.Assertion;
import gov.va.legoEdit.model.schemaModel.AssertionComponent;
import gov.va.legoEdit.model.schemaModel.Concept;
import gov.va.legoEdit.model.schemaModel.Destination;
import gov.va.legoEdit.model.schemaModel.Discernible;
import gov.va.legoEdit.model.schemaModel.Expression;
import gov.va.legoEdit.model.schemaModel.Lego;
import gov.va.legoEdit.model.schemaModel.Measurement;
import gov.va.legoEdit.model.schemaModel.Qualifier;
import gov.va.legoEdit.model.schemaModel.Relation;
import gov.va.legoEdit.model.schemaModel.RelationGroup;
import gov.va.legoEdit.model.schemaModel.Stamp;
import gov.va.legoEdit.model.schemaModel.Type;
import gov.va.legoEdit.model.schemaModel.Value;
import gov.va.legoEdit.util.Utility;
import gov.va.legoEdit.validators.Validator;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map.Entry;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.scene.control.TreeItem;

/**
 * LegoTreeItem The actual data item for each node in the tree
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a> 
 * Copyright 2013
 * 
 */
public class LegoTreeItem extends TreeItem<String>
{
	private LegoTreeNodeType ltnt_ = null;
	private ConceptUsageType cut_ = null; //This is a cache
	private Object extraData_ = null;
	private Boolean isValid = null;
	private Boolean areChildrenValid = null;
	private String invalidReason_ = null;
	private LegoTreeNodeGraphic treeNodeGraphic = null;
	private long validationTimestamp = -1;
	
	public LegoTreeItem getLegoParent()
	{
		return (LegoTreeItem)getParent();
	}

	public LegoTreeItem()
	{
		setValue("Hidden Root");
	}

	public LegoTreeItem(String value)
	{
		setValue(value);
	}

	public LegoTreeItem(LegoTreeNodeType tct)
	{
		this.ltnt_ = tct;
		setValue(null);
	}

	public LegoTreeItem(String value, LegoTreeNodeType tct)
	{
		this.ltnt_ = tct;
		setValue(value);
	}
	
	public LegoTreeItem(String value, LegoTreeNodeType tct, Object extraData)
	{
		this.ltnt_ = tct;
		setValue(value);
		this.extraData_ = extraData;
	}

	public LegoTreeItem(Stamp value, LegoTreeNodeType tct)
	{
		this.ltnt_ = tct;
		extraData_ = value;
		setValue(null);
	}

	public LegoTreeItem(String label, String value, LegoTreeNodeType tct)
	{
		this.ltnt_ = tct;
		setValue(value);
		extraData_ = label;
	}

	public LegoTreeItem(LegoListByReference llbr)
	{
		setValue(llbr.getGroupName());
		this.extraData_ = llbr;
		this.ltnt_ = LegoTreeNodeType.legoListByReference;

		// Going to reorganize these under the LEGO list by introducing a PNCS NAME / value hierarchy in-between the
		// LegoList and the individual legos.

		buildPNCSChildren();
	}

	public void buildPNCSChildren()
	{
		if (!(getExtraData() instanceof LegoListByReference))
		{
			throw new IllegalArgumentException();
		}
		LegoListByReference llbr = (LegoListByReference) getExtraData();
		Hashtable<String, Hashtable<String, List<LegoReference>>> pncsHier = new Hashtable<>();

		for (LegoReference lr : llbr.getLegoReference())
		{
			String pncsName = lr.getPncs().getName();
			Hashtable<String, List<LegoReference>> pncsValueTable = pncsHier.get(pncsName);
			if (pncsValueTable == null)
			{
				pncsValueTable = new Hashtable<String, List<LegoReference>>();
				pncsHier.put(pncsName, pncsValueTable);
			}
			String pncsValue = lr.getPncs().getValue();
			List<LegoReference> legoList = pncsValueTable.get(pncsValue);
			if (legoList == null)
			{
				legoList = new ArrayList<LegoReference>();
				pncsValueTable.put(pncsValue, legoList);
			}
			legoList.add(lr);
		}

		for (Entry<String, Hashtable<String, List<LegoReference>>> items : pncsHier.entrySet())
		{
			Integer pncsId = null;
			if (items.getValue().values().size() > 0)
			{
				pncsId = items.getValue().values().iterator().next().get(0).getPncs().getId();
			}
			
			LegoTreeItem pncsNameTI = new LegoTreeItem(items.getKey(), LegoTreeNodeType.pncsName, pncsId);
			getChildren().add(pncsNameTI);
			for (Entry<String, List<LegoReference>> nestedItems : items.getValue().entrySet())
			{
				LegoTreeItem pncsValueTI = new LegoTreeItem(nestedItems.getKey(), LegoTreeNodeType.pncsValue);
				pncsNameTI.getChildren().add(pncsValueTI);
				for (LegoReference lr : nestedItems.getValue())
				{
					pncsValueTI.getChildren().add(new LegoTreeItem(lr));
				}
			}
		}
		FXCollections.sort(getChildren(), new LegoTreeItemComparator(true));
		for (TreeItem<String> item : getChildren())
		{
			FXCollections.sort(item.getChildren(), new LegoTreeItemComparator(true));
		}
	}

	public LegoTreeItem(LegoReference lr)
	{
		setValue("Lego");
		extraData_ = lr;
		ltnt_ = LegoTreeNodeType.legoReference;
	}

	public LegoTreeItem(Lego l, LegoTreeNodeType ltnt)
	{
		if (ltnt == LegoTreeNodeType.comment)
		{
			setValue("Comment");
		}
		else
		{
			throw new IllegalArgumentException();
		}
		extraData_ = l;
		ltnt_ = ltnt;
	}

	public LegoTreeItem(Assertion a)
	{
		setValue("Assertion");
		ltnt_ = LegoTreeNodeType.assertion;
		extraData_ = a;

		for (AssertionComponent ac : a.getAssertionComponent())
		{
			getChildren().add(new LegoTreeItem(ac));
		}
		if (a.getDiscernible() == null)
		{
			a.setDiscernible(new Discernible());
		}

		Expression de = a.getDiscernible().getExpression();
		if (de == null)
		{
			de = new Expression();
			a.getDiscernible().setExpression(de);
		}
		getChildren().add(new LegoTreeItem(de, LegoTreeNodeType.expressionDiscernible));

		if (a.getQualifier() == null)
		{
			a.setQualifier(new Qualifier());
		}

		Expression qe = a.getQualifier().getExpression();
		if (qe == null)
		{
			qe = new Expression();
			a.getQualifier().setExpression(qe);
		}
		getChildren().add(new LegoTreeItem(qe, LegoTreeNodeType.expressionQualifier));

		if (a.getTiming() != null)
		{
			getChildren().add(new LegoTreeItem(a.getTiming(), "Timing"));
		}
		if (a.getValue() == null)
		{
			a.setValue(new Value());
		}
		getChildren().add(new LegoTreeItem(a.getValue()));
	}

	public LegoTreeItem(Value value)
	{
		setValue("Value");
		ltnt_ = LegoTreeNodeType.value;
		extraData_ = value;

		if (value.getExpression() != null)
		{
			getChildren().add(new LegoTreeItem(value.getExpression(), LegoTreeNodeType.expressionValue));
		}
		else if (value.getMeasurement() != null)
		{
			getChildren().add(new LegoTreeItem(value.getMeasurement(), "Measurement"));
		}
		else if (value.getText() != null)
		{
			getChildren().add(new LegoTreeItem(value, LegoTreeNodeType.text));
		}
		else if (value.isBoolean() != null)
		{
			getChildren().add(new LegoTreeItem(value, LegoTreeNodeType.bool));
		}
	}

	public LegoTreeItem(Measurement measurement, String label)
	{
		extraData_ = measurement;
		setValue(label);  //Expected to be Timing or Measurement
		
		if (measurement.getPoint() != null)
		{
			ltnt_ = LegoTreeNodeType.measurementPoint;
		}
		else if (measurement.getInterval() != null)
		{
			ltnt_ = LegoTreeNodeType.measurementInterval;
		}
		else if (measurement.getBound() != null)
		{
			ltnt_ = LegoTreeNodeType.measurementBound;
		}
		else
		{
			ltnt_ = LegoTreeNodeType.measurementEmpty;
		}
	}

	public LegoTreeItem(AssertionComponent ac)
	{
		setValue("Assertion Component");
		ltnt_ = LegoTreeNodeType.assertionComponent;

		getChildren().add(new LegoTreeItem(ac.getAssertionUUID(), LegoTreeNodeType.assertionUUID));

		Type t = ac.getType();
		if (t == null)
		{
			t = new Type();
			ac.setType(t);
		}

		Concept c = t.getConcept();
		if (c == null)
		{
			c = new Concept();
			t.setConcept(c);
		}

		//no node for type, it is rendered on the asssertion component line
		extraData_ = ac;
	}

	public LegoTreeItem(Concept concept, LegoTreeNodeType tct)
	{
		extraData_ = concept;
		this.ltnt_ = tct;
	}

	public LegoTreeItem(Expression expression, LegoTreeNodeType tct)
	{
		extraData_ = expression;
		this.ltnt_ = tct;
		setValue("Expression");

		if (expression.getConcept() == null && expression.getExpression().size() == 0)
		{
			Concept c = new Concept();
			expression.setConcept(c);
		}
		
		//no node for single concept, that is rendered on the expression line

		if (expression.getExpression().size() > 0)
		{
			while (expression.getExpression().size() < 2)
			{
				expression.getExpression().add(new Expression());
			}

			for (Expression e : expression.getExpression())
			{
				// If we are building a conjunction, and the type is Discernible or qualifier - the expressions become optional
				getChildren().add(
						new LegoTreeItem(e,
								(tct == LegoTreeNodeType.expressionDiscernible || tct == LegoTreeNodeType.expressionQualifier) ? LegoTreeNodeType.expressionOptional
										: tct));
			}
		}

		if (expression.getRelation() != null)
		{
			for (Relation r : expression.getRelation())
			{
				getChildren().add(new LegoTreeItem(r));
			}
		}
		if (expression.getRelationGroup() != null)
		{
			for (RelationGroup rg : expression.getRelationGroup())
			{
				LegoTreeItem rgti = new LegoTreeItem(rg);
				getChildren().add(rgti);
			}
		}
	}

	public LegoTreeItem(RelationGroup relationGroup)
	{
		extraData_ = relationGroup;
		ltnt_ = LegoTreeNodeType.relationshipGroup;
		setValue("Relation Group");
		if (relationGroup.getRelation().size() == 0)
		{
			relationGroup.getRelation().add(new Relation());
		}
		for (Relation r : relationGroup.getRelation())
		{
			getChildren().add(new LegoTreeItem(r));
		}
	}

	public LegoTreeItem(Relation r)
	{
		setValue("Relation");
		ltnt_ = LegoTreeNodeType.relation;
		extraData_ = r;

		Type t = r.getType();
		if (t == null)
		{
			t = new Type();
			r.setType(t);
		}
		Concept c = t.getConcept();
		if (c == null)
		{
			c = new Concept();
			t.setConcept(c);
		}

		//No node for type, it is rendered on the Relation line
		
		Destination d = r.getDestination();
		if (d == null)
		{
			d = new Destination();
			r.setDestination(d);
		}

		if (d.getExpression() != null)
		{
			getChildren().add(new LegoTreeItem(d.getExpression(), LegoTreeNodeType.expressionDestination));
		}
		else if (d.getMeasurement() != null)
		{
			getChildren().add(new LegoTreeItem(d.getMeasurement(), "Measurement"));
		}
		else if (d.getText() != null)
		{
			getChildren().add(new LegoTreeItem(d, LegoTreeNodeType.text));
		}
		else if (d.isBoolean() != null)
		{
			getChildren().add(new LegoTreeItem(d, LegoTreeNodeType.bool));
		}
	}

	public LegoTreeItem(Value value, LegoTreeNodeType type)
	{
		setValue("");
		ltnt_ = type;
		extraData_ = value;
	}

	public LegoTreeItem(Destination destination, LegoTreeNodeType type)
	{
		setValue("");
		ltnt_ = type;
		extraData_ = destination;
	}

	public LegoTreeNodeType getNodeType()
	{
		return ltnt_;
	}

	/**
	 * use caution changing this... only here for measurement support.
	 */
	protected void setNodeType(LegoTreeNodeType type)
	{
		ltnt_ = type;
		cut_ = null;
	}
	
	public Object getExtraData()
	{
		return extraData_;
	}

	public int getSortOrder()
	{
		if (ltnt_ != null)
		{
			return ltnt_.getSortOrder();
		}
		return 0;
	}
	
	/**
	 * Callers responsibility to ensure the validator has run before calling this to get current results.
	 */
	protected String getInvalidReason()
	{
		return invalidReason_;
	}
	
	public void isValidThreaded(final BooleanCallback callback)
	{
		Utility.tpe.execute(new Runnable()
		{
			@Override
			public void run()
			{
				callback.sendResult(isValid());
			}
		});
	}
	
	protected long getValidationTimestamp()
	{
		return validationTimestamp;
	}
	
	private boolean isValid()
	{
		if (isValid == null)
		{
			//prevent multiple threads from duplicating work
			synchronized (this)
			{
				if (isValid == null)
				{
					validate();
				}
			}
		}
		return isValid;
	}
	
	private boolean areChildrenValid()
	{
		if (areChildrenValid == null)
		{
			synchronized (this)
			{
				//prevent multiple threads from duplicating work
				if (areChildrenValid == null)
				{
					validateChildren();
				}
			}
		}
		return areChildrenValid;
	}
	
	private void validate()
	{
		if (LegoTreeNodeType.pncsName  == ltnt_ || LegoTreeNodeType.pncsValue == ltnt_ || LegoTreeNodeType.legoListByReference == ltnt_
				|| LegoTreeNodeType.blankLegoEndNode == ltnt_ || LegoTreeNodeType.blankLegoListEndNode == ltnt_ 
				|| LegoTreeNodeType.legoReference == ltnt_ || LegoTreeNodeType.status == ltnt_ || LegoTreeNodeType.comment == ltnt_)
		{
			invalidReason_ = null;
		}
		else if (extraData_ == null)
		{
			invalidReason_ = null;
		}
		else
		{
			//This may be slow.  Turn on the progress indicator
			Platform.runLater(new Runnable()
			{
				@Override
				public void run()
				{
					if (treeNodeGraphic != null)
					{
						treeNodeGraphic.showProgress(true);
					}
				}
			});
			invalidReason_ = Validator.isValid(extraData_, this);
			//This may be slow.  Turn on the progress indicator
			Platform.runLater(new Runnable()
			{
				@Override
				public void run()
				{
					if (treeNodeGraphic != null)
					{
						treeNodeGraphic.showProgress(false);
					}
				}
			});
		}
		isValid = invalidReason_ == null;
		validationTimestamp = System.currentTimeMillis();
	}

	private void validateChildren()
	{
		boolean newValue = true;
		for (TreeItem<String> ti : getChildren())
		{
			LegoTreeItem lti = (LegoTreeItem)ti;
			if (!lti.isValid() || !lti.areChildrenValid())
			{
				newValue = false;
				break;
			}
		}
		
		areChildrenValid = newValue;
	}
	
	protected void setTreeNodeGraphic(LegoTreeNodeGraphic node)
	{
		this.treeNodeGraphic = node;
	}
	
	public void revalidateToRootThreaded()
	{
		Utility.tpe.execute(new Runnable()
		{
			@Override
			public void run()
			{
				revalidateToRoot();
			}
		});
	}
	
	/**
	 * Runs in current thread.
	 */
	public void revalidateToRoot()
	{
		validate();
		validateChildren();
		updateValidityImage();
		
		LegoTreeItem parent = getLegoParent();
		if (parent != null)
		{
			parent.revalidateToRoot();
		}
	}
	
	protected void updateValidityImageThreaded()
	{
		Utility.tpe.execute(new Runnable()
		{
			@Override
			public void run()
			{
				updateValidityImage();
			}
		});
	}
	
	private void updateValidityImage()
	{
		final LegoTreeNodeGraphic localTreeNodeGraphic = treeNodeGraphic;
		
		if (localTreeNodeGraphic != null && LegoTreeNodeType.concept != ltnt_ && LegoTreeNodeType.assertionUUID != ltnt_ && LegoTreeNodeType.text != ltnt_)
		{
			//run both of these in the thread that called us (probably a background thread)
			final boolean isValid = isValid(); 
			final boolean areChildrenValid = areChildrenValid();
	
			Platform.runLater(new Runnable()
			{
				@Override
				public void run()
				{
					localTreeNodeGraphic.showProgress(false);
					if (isValid && areChildrenValid)
		 			{
						localTreeNodeGraphic.showInvalid(false);
					}
					else
					{
						localTreeNodeGraphic.showInvalid(true);
						localTreeNodeGraphic.setInvalidReason((isValid ? "Error in child" : invalidReason_));
					}
				}
			});
		}
	}
	
	public ConceptUsageType getConceptUsageType()
	{
		if (cut_ != null)
		{
			return cut_;
		}
		if (LegoTreeNodeType.expressionDestination == ltnt_)
		{
			cut_ = ConceptUsageType.REL_DESTINATION;
		}
		else if (extraData_ != null)
		{
			if (extraData_ instanceof Relation || extraData_ instanceof AssertionComponent)
			{
				cut_ = ConceptUsageType.TYPE;
			}
			else if (extraData_ instanceof Measurement)
			{
				cut_ = ConceptUsageType.UNITS;
			}
			else if (extraData_ instanceof Expression)
			{
				if (LegoTreeNodeType.expressionDiscernible == ltnt_)
				{
					cut_ = ConceptUsageType.DISCERNIBLE;
				}
				else if (LegoTreeNodeType.expressionQualifier == ltnt_)
				{
					cut_ = ConceptUsageType.QUALIFIER;
				}
			}
			else if (extraData_ instanceof Value)
			{
				cut_ = ConceptUsageType.VALUE;
			}
		}
		if (cut_ == null)
		{
			// Didn't find it... recurse...
			LegoTreeItem parent = getLegoParent();
			if (parent != null)
			{
				cut_ = parent.getConceptUsageType();
			}
		}
		return cut_;
	}
}

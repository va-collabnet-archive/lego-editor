package gov.va.legoEdit.gui.legoTreeView;

/**
 * 
 * LegoTreeNodeType
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a> 
 * Copyright 2013
 */
public enum LegoTreeNodeType
{
	legoListByReference(1),
	legoReference(0),
	pncsValue(0), 
	pncsName(0),
	
	comment(0),
	status(2),
	assertion(3),
	expressionDiscernible(0),
	expressionQualifier(1),
	measurementEmpty(2), measurementPoint(2), measurementInterval(2), measurementBound(2), 
	value(3),
	assertionComponent(4),
	
	concept(0),
	
	expressionValue(2), expressionDestination(2), expressionOptional(2), 
	relation(3),
	relationshipGroup(4),

	assertionUUID(0),
	
	text(0), bool(0),

	blankLegoEndNode(50),
	blankLegoListEndNode(50);
	
	
	private int sortOrder_;
	
	private LegoTreeNodeType(int sortOrder)
	{
		sortOrder_ = sortOrder;
	}
	
	public int getSortOrder()
	{
		return sortOrder_;
	}
}

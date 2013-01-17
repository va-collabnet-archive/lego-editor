package gov.va.legoEdit.gui.legoTreeView;

public enum LegoTreeNodeType
{
	legoListByReference(1),
	legoReference(0),
	pncsValue(0), 
	pncsName(0),
	
	status(0),
	comment(1),
	assertion(2),
	expressionDiscernible(0),
	expressionQualifier(1),
	timingMeasurement(2),
	value(3),
	assertionComponent(4),
	
	concept(0), conceptOptional(1),
	
	expressionValue(2), expressionDestination(2), expressionOptional(2), 
	relation(3),
	relationshipGroup(4),
	
	measurement(0),  interval(0), 
	upper(0), lower(0), assertionUUID(0),  point(0),
	
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

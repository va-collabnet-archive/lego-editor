package gov.va.legoEdit.gui.legoTreeView;

public enum LegoTreeNodeType
{
	legoListByReference(1),
	legoReference(0),
	pncsValue(0), 
	pncsName(0),
	
	status(0),
	assertion(1),
	expressionDiscernible(0),
	expressionQualifier(1),
	timing(2),
	value(3),
	assertionComponents(4),
	
	expressionValue(0), expressionDestination(0), expressionOptional(0), 
	relation(1),
	relationshipGroup(2),
	
	concept(0), conceptOptional(0),
	
	labeledUneditableString(0),assertionComponent(0),
	measurement(0),  interval(0), 
	upper(0), lower(0), assertionUUID(0),  point(0),
    
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

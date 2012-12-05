package gov.va.legoEdit.gui.legoTreeView;

public enum LegoTreeNodeType
{
    addLegoListPlaceholder(1),
	legoList(2),
	legoListLego(0),
	pncsValue(0), 
	pncsName(0),
	
	status(1),
	assertion(2),
	discernible(0),
	qualifier(1),
	timing(2),
	value(3),
	assertionComponents(4),
	
	labeledUneditableString(0),assertionComponent(0), destinationConcept(0),  
	typeConcept(0), unitsConcept(0), measurement(0),  interval(0), relation(0), 
	upper(0), lower(0), assertionUUID(0), assertionTypeConcept(0), addAssertionPlaceholder(0), relConcept(0), valueConcept(0), point(0);
	
	
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

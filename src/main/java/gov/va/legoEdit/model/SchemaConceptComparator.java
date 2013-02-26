package gov.va.legoEdit.model;

import gov.va.legoEdit.gui.util.AlphanumComparator;
import gov.va.legoEdit.model.schemaModel.Concept;
import java.util.Comparator;

/**
 * SchemaConceptComparator
 * @author Dan Armbrust 
 * Copyright 2013
 * 
 */
public class SchemaConceptComparator implements Comparator<Concept>
{
	private AlphanumComparator ac = new AlphanumComparator(true);

	@Override
	public int compare(Concept o1, Concept o2)
	{
		return ac.compare(o1.getDesc(),  o2.getDesc());
	}

}

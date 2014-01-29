package gov.va.legoEdit.storage.templates;

import gov.va.legoEdit.gui.util.AlphanumComparator;
import java.util.Comparator;

/**
 * 
 * LegoTemplateComparator
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a> 
 * Copyright 2013
 */
public class LegoTemplateComparator implements Comparator<LegoTemplate>
{
    private AlphanumComparator ac = new AlphanumComparator(true);
    
    @Override
    public int compare(LegoTemplate o1, LegoTemplate o2)
    {
        return ac.compare(o1.getTemplate().getClass().getSimpleName() + o1.getDescription(),
                o2.getTemplate().getClass().getSimpleName() + o2.getDescription());
    }
}

package gov.va.legoEdit.gui.legoTreeView;

import gov.va.legoEdit.util.Utility;


public class ComboBoxConcept
{
    private String description_, id_;
    
    public ComboBoxConcept(String description, String id)
    {
        description_ = description;
        id_ = id;
    }
    
    public ComboBoxConcept(String id)
    {
        description_ = null;
        id_ = id;
    }
    
    public String getDescription()
    {
        return description_;
    }
    
    public String getId()
    {
        return id_;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == null)
        {
            return false;
        }
        if (obj instanceof ComboBoxConcept)
        {
            ComboBoxConcept other = (ComboBoxConcept)obj;
            //I really only care about the ID field.  Some hacking happens with the description field in the ConceptNode class, so ignore it.
            return Utility.isEqual(id_, other.id_);
        }
        return false;
    }
    
    
}
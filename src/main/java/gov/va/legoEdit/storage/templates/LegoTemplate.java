package gov.va.legoEdit.storage.templates;

/**
 * 
 * LegoTemplate
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a> 
 * Copyright 2013
 */
public class LegoTemplate
{
    private Object template;
    private String description;
    
    public LegoTemplate(String description, Object template)
    {
        this.template = template;
        this.description = description;
    }
    
    public String getDescription()
    {
        return description;
    }
    
    public Object getTemplate()
    {
        return template;
    }
}

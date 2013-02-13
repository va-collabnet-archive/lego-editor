package gov.va.legoEdit.storage.templates;

import gov.va.legoEdit.formats.LegoXMLUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LegoTemplateManager implements Observable
{
    private static volatile LegoTemplateManager instance_;
    protected static File templatesPath_ = new File("Templates");
    static Logger logger = LoggerFactory.getLogger(LegoTemplateManager.class);
    
    private HashMap<String, LegoTemplate> templates_ = new HashMap<>();
    private ArrayList<InvalidationListener> listeners_ = new ArrayList<>();
    
    public static LegoTemplateManager getInstance()
    {
        if (instance_ == null)
        {
            synchronized (LegoTemplateManager.class)
            {
                if (instance_ == null)
                {
                    instance_ = new LegoTemplateManager();
                }
            }
        }
        return instance_;
    }
    
    private LegoTemplateManager()
    {
        templatesPath_.mkdir();
        if (!templatesPath_.isDirectory())
        {
            logger.error("Cannot create directory '" +templatesPath_.getAbsolutePath() + "' for Template storage.  Templates will not be stored!");
        }
        else
        {
            for (File f : templatesPath_.listFiles())
            {
                if (f.isFile() && f.getName().endsWith(".xml"))
                {
                    String name = f.getName().substring(0, f.getName().indexOf(".xml"));
                    if (templates_.containsKey(name.toLowerCase()))
                    {
                        logger.warn("Skipping '" + f.getName() + "' because the name is not unique when ignoring case");
                        continue;
                    }
                    try
                    {
                        templates_.put(name.toLowerCase(), new LegoTemplate(name, LegoXMLUtils.read(new FileInputStream(f))));
                    }
                    catch (Exception e)
                    {
                        logger.warn("Could not read template '" + f.getName() + "'.", e);
                    }
                }
                else
                {
                    logger.warn("Skipping unexpected item '" + f.getName() + "' in the templates folder");
                }
            }
        }
    }
    
    public boolean isNameInUse(String name)
    {
        return templates_.containsKey(name.toLowerCase());
    }
    
    public void storeTemplate(String name, Object object) throws Exception
    {
        if (isNameInUse(name))
        {
            throw new Exception("A template with the name '" + name + "' already exists");
        }
        try
        {
            File f = new File(templatesPath_, name + ".xml");
            FileOutputStream fos = new FileOutputStream(f);
            LegoXMLUtils.writeXML(object, fos);
            fos.close();
            templates_.put(name.toLowerCase(), new LegoTemplate(name, object));
            notifyListeners();
        }
        catch (Exception e)
        {
            logger.error("Unexpected error storing template", e);
            throw new Exception("Template store request failed", e);
        }
    }
    
    public LegoTemplate deleteTemplate(String name)
    {
        File f = new File(templatesPath_, name + ".xml");
        if (f.exists())
        {
            if (!f.delete())
            {
                logger.warn("Delete of template file " + f.getAbsolutePath() + "' from disk failed");
            }
        }
        LegoTemplate t = templates_.remove(name);
        notifyListeners();
        return t;
    }
    
    public Collection<LegoTemplate> getTemplates()
    {
        return templates_.values();
    }
    
    private void notifyListeners()
    {
        for (InvalidationListener il : listeners_)
        {
            il.invalidated(this);
        }
    }

    @Override
    public void addListener(InvalidationListener arg0)
    {
        listeners_.add(arg0);
    }

    @Override
    public void removeListener(InvalidationListener arg0)
    {
       listeners_.remove(arg0);
    }
}

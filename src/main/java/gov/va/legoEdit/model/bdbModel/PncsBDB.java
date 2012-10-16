package gov.va.legoEdit.model.bdbModel;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;
import com.sleepycat.persist.model.Relationship;
import com.sleepycat.persist.model.SecondaryKey;
import gov.va.legoEdit.model.schemaModel.Pncs;

@Entity
public class PncsBDB {

    @PrimaryKey
    private String uniqueId;
    
    @SecondaryKey(relate= Relationship.MANY_TO_ONE)
    protected int id;
    protected String name;
    protected String value;
    
    private PncsBDB()
    {
        //required by BDB
    }
    
    public PncsBDB(Pncs pncs)
    {
        id = pncs.getId();
        name = pncs.getName();
        value = pncs.getValue();
        uniqueId = makeUniqueId(id, value);
    }
    
    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }
    
    public String getUniqueId()
    {
        return this.uniqueId;
    }
    
    public static String makeUniqueId(int id, String value)
    {
        return id + ":" + value;
    }
    
    public Pncs toSchemaPncs()
    {
        Pncs pncs = new Pncs();
        pncs.setId(id);
        pncs.setName(name);
        pncs.setValue(value);
        return pncs;
    }
}

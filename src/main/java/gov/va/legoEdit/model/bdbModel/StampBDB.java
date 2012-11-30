package gov.va.legoEdit.model.bdbModel;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;
import gov.va.legoEdit.model.schemaModel.Stamp;
import gov.va.legoEdit.util.TimeConvert;
import java.util.UUID;

@Entity
public class StampBDB
{
    @SuppressWarnings("unused")
	private StampBDB()
    {
        //required by BDB
    }
    
    /**
     * This should only be called by LegoBDB, otherwise there will be issues with the generating consistent unique IDs for the stamp.
     * Since nothing about the stamp itself can be used to create a unique ID.
     * @param stamp 
     */
    protected StampBDB(Stamp stamp)
    {
        status = stamp.getStatus();
        time =  TimeConvert.convert(stamp.getTime());
        author = stamp.getAuthor();
        module = stamp.getModule();
        path = stamp.getPath();
        stampId = stamp.getUuid();
        if (stampId == null || stampId.length() == 0)
        {
            stampId = UUID.randomUUID().toString();
        }
    }

    @PrimaryKey
    private String stampId;
    protected String status;
    protected long time;
    protected String author;
    protected String module;
    protected String path;

    public String getStatus()
    {
        return status;
    }

    public long getTime()
    {
        return time;
    }

    public String getAuthor()
    {
        return author;
    }

    public String getModule()
    {
        return module;
    }

    public String getPath()
    {
        return path;
    }

    public String getStampId()
    {
        return this.stampId;
    }

    public Stamp toSchemaStamp()
    {
        Stamp stamp = new Stamp();
        stamp.setAuthor(author);
        stamp.setModule(module);
        stamp.setPath(path);
        stamp.setStatus(status);
        stamp.setTime(TimeConvert.convert(time));
        stamp.setUuid(stampId);
        return stamp;
    }
}

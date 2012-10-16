package gov.va.legoEdit.storage.util;

import com.sleepycat.persist.EntityCursor;
import gov.va.legoEdit.model.bdbModel.LegoListBDB;
import gov.va.legoEdit.model.schemaModel.LegoList;
import gov.va.legoEdit.storage.CloseableIterator;
import java.util.concurrent.ScheduledExecutorService;

/**
 *
 * @author darmbrust
 */
public class LegoListBDBConvertingIterator implements CloseableIterator<LegoList>
{

    private BDBIterator<LegoListBDB> iter;

    public LegoListBDBConvertingIterator(ScheduledExecutorService sec, EntityCursor<LegoListBDB> c)
    {
        iter = new BDBIterator(sec, c);
    }

    @Override
    public LegoList next()
    {
        return iter.next().toSchemaLegoList();
    }

    @Override
    public boolean hasNext()
    {
        return iter.hasNext();
    }

    @Override
    public void remove()
    {
        iter.remove();
    }

    @Override
    public void close()
    {
        iter.close();
    }
}

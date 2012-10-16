package gov.va.legoEdit.storage.util;

import com.sleepycat.persist.EntityCursor;
import gov.va.legoEdit.model.bdbModel.LegoBDB;
import gov.va.legoEdit.model.schemaModel.Lego;
import gov.va.legoEdit.storage.CloseableIterator;
import java.util.concurrent.ScheduledExecutorService;

/**
 *
 * @author darmbrust
 */
public class LegoBDBConvertingIterator implements CloseableIterator<Lego>
{

    private BDBIterator<LegoBDB> iter;

    public LegoBDBConvertingIterator(ScheduledExecutorService sec, EntityCursor<LegoBDB> c)
    {
        iter = new BDBIterator<>(sec, c);
    }

    @Override
    public Lego next()
    {
        return iter.next().toSchemaLego();
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

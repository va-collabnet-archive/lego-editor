package gov.va.legoEdit.storage.wb;

import gov.va.legoEdit.model.schemaModel.Concept;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.ihtsdo.fxmodel.concept.FxConcept;
import org.ihtsdo.fxmodel.concept.component.description.FxDescriptionChronicle;
import org.ihtsdo.fxmodel.concept.component.description.FxDescriptionVersion;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.api.description.DescriptionChronicleBI;
import org.ihtsdo.tk.api.description.DescriptionVersionBI;
import org.ihtsdo.tk.api.id.IdBI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Utility
{
    //Should be the first, but isn't... not sure why.
    //TODO file bug
    //private static UUID snomedIdType = UUID.fromString("0418a591-f75b-39ad-be2c-3ab849326da9");  //SNOMED integer id
    private static UUID snomedIdType = UUID.fromString("f7495b58-6630-3499-a44e-2052b5fcf06c");  //user
    private static Integer snomedIdTypeNid = null;
    private static UUID FSN_UUID = UUID.fromString("00791270-77c9-32b6-b34f-d932569bd2bf");
    private static Integer FSNTypeNid = null;
    
    
    private static Logger logger = LoggerFactory.getLogger(Utility.class);
    
    
    private static BlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<Runnable>();
    private static ThreadPoolExecutor tpe = new ThreadPoolExecutor(5, 5, 60, TimeUnit.SECONDS, workQueue, new ThreadFactory()
    {
        @Override
        public Thread newThread(Runnable r)
        {
            Thread t = Executors.defaultThreadFactory().newThread(r);
            t.setDaemon(true);
            return t;
        }
    });
    
    
    public static void lookupSnomedIdentifier(final String identifier, final ConceptLookupCallback callback)
    {
        Runnable r = new Runnable()
        {
            @Override
            public void run()
            {
                Concept c = lookupSnomedIdentifier(identifier);
                callback.lookupComplete(c);
            }
        };
        tpe.execute(r);
    }
    
    
    public static Concept lookupSnomedIdentifier(String identifier)
    {
        if (identifier == null)
        {
            return null;
        }
        Concept c = null;
        ConceptChronicleBI result = null;
        try
        {
            UUID uuid = UUID.fromString(identifier);
            result = WBDataStore.Ts().getConcept(uuid);
        }
        catch (IllegalArgumentException | IOException e)
        {
            //try looking up by ID 
            try
            {
                result = WBDataStore.Ts().getConceptFromAlternateId(snomedIdType, identifier);
            }
            catch (IOException e1)
            {
                // noop
            }
        }
        if (result != null && result.getUUIDs().size() > 0)
        {
            c = new Concept();
            c.setDesc(Utility.getFSN(result));
            c.setUuid(result.getUUIDs().get(0).toString());
            try
            {
                for (IdBI x : result.getAdditionalIds())
                {
                    //TODO this will likely get the wrong value, because the DB is returning multiple authorities all with the same type... something hosed.
                    if (x.getAuthorityNid() == getSnomedIdTypeNid())
                    {
                        c.setSctid(Long.parseLong(x.getDenotation().toString()));
                        break;
                    }
                }
            }
            catch (Exception e)
            {
                // noop
            }
        }
        return c;
    }
    
    private static int getSnomedIdTypeNid()
    {
        if (snomedIdTypeNid == null)
        {
            try
            {
                snomedIdTypeNid = WBDataStore.Ts().getNidForUuids(snomedIdType);
            }
            catch (IOException e)
            {
                logger.error("Couldn't find nid for snomed id UUID", e);
                snomedIdTypeNid = -1;
            }
        }
        return snomedIdTypeNid;
    }
    
    private static int getFSNTypeNid()
    {
        if (FSNTypeNid == null)
        {
            try
            {
                FSNTypeNid = WBDataStore.Ts().getNidForUuids(FSN_UUID);
            }
            catch (IOException e)
            {
                logger.error("Couldn't find nid for FSN UUID", e);
                FSNTypeNid = -1;
            }
        }
        return FSNTypeNid;
    }
    
    public static String getFSN(ConceptChronicleBI concept)
    {
        try
        {
            for (DescriptionChronicleBI desc : concept.getDescs())
            {
                for (DescriptionVersionBI<?> descVer : desc.getVersions())
                {
                    if (descVer.getTypeNid() == getFSNTypeNid())
                    {
                        return descVer.getText();
                    }
                }
            }
        }
        catch (IOException e)
        {
            //noop
        }
        return concept.toUserString();
    }
    
    public static String getFSN(FxConcept concept)
    {
        //Go hunting for a FSN
        if (concept.getDescriptions() == null)
        {
            return concept.getConceptReference().getText();
        }
        for (FxDescriptionChronicle d : concept.getDescriptions())
        {
            FxDescriptionVersion dv = d.getVersions().get(d.getVersions().size() - 1);
            if (dv.getTypeReference().getUuid().equals(FSN_UUID))
            {
                return dv.getText();
            }
        }
        return concept.getConceptReference().getText();
    }
}

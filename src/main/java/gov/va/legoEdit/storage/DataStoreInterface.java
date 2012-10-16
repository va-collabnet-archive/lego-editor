package gov.va.legoEdit.storage;

import gov.va.legoEdit.model.schemaModel.Lego;
import gov.va.legoEdit.model.schemaModel.LegoList;
import gov.va.legoEdit.model.schemaModel.Pncs;
import gov.va.legoEdit.model.schemaModel.Stamp;
import java.util.List;

/**
 *
 * @author darmbrust
 */
public interface DataStoreInterface
{

    /**
     * Get the legoList object with the specified name.
     */
    public LegoList getLegoListByName(String legoListGroupName) throws DataStoreException;

    /**
     * Get the legoList object with the specified uuid.
     */
    public LegoList getLegoListByID(String legoListUUID) throws DataStoreException;

    /**
     * Get an iterator that traverses all stored legoLists.
     */
    public CloseableIterator<LegoList> getLegoLists() throws DataStoreException;

    /**
     * Get all LEGO (versions) in the DB with the provided UUID.
     */
    public List<Lego> getLegos(String legoUUID) throws DataStoreException;

    /**
     * Get all LEGOs which contain (define) the assertion with the specified assertion UUID.
     * (There may be more than one stamped lego with the same UUID - up to the caller to decide which stamp they want)
     * Note - this does not return legos that simply link to the named assertion via an assertionComponent
     */
    public List<Lego> getLegosContainingAssertion(String assertionUUID);
    
    /**
     * Get all LEGOs which use the assertion with the specified assertion UUID as part of an assertionComponent
     */
    public List<Lego> getLegosUsingAssertion(String assertionUUID);
    
    /**
     * Get all LEGOs which contain the specified snomed concept (as any child).
     */
    public List<Lego> getLegosContainingConcept(int sctId) throws DataStoreException;

    /**
     * Get all LEGOs which contain the specified snomed concept (as any child).
     */
    public List<Lego> getLegosContainingConcept(String uuid) throws DataStoreException;

    /**
     * Get an iterator that traverses all stored LEGO objects.
     */
    public CloseableIterator<Lego> getLegos() throws DataStoreException;

    /**
     * Get all LEGOs which reference the specified PNCS.
     */
    public List<Lego> getLegosForPncs(int id, String value) throws DataStoreException;

    /**
     * Get all LEGOs which reference the specified PNCS.
     */
    public List<Lego> getLegosForPncs(int id) throws DataStoreException;
    
    /**
     * Get all of the PNCS values currently in the system.
     */
    public CloseableIterator<Pncs> getPncs() throws DataStoreException;

    /**
     * Get the UUID of the legoList(s) which contains the specified LEGO.
     *
     * @return null if no legoList contains the specified LEGO
     */
    public List<String> getLegoListByLego(String legoUUID) throws DataStoreException;

    /**
     * Create and store a new (empty) LegoList.
     *
     * @throws WriteException if the operation fails
     */
    public LegoList createLegoList(String groupName, String groupDescription) throws WriteException;

    /**
     * Store the specified legoList. Note - this method cannot be used to replace an existing LegoList.
     *
     * This method does not make any changes to the STAMP values of any contained LEGO objects.
     *
     * @throws WriteException if a legoList of the specified name already exists or if the store operation fails
     */
    public void importLegoList(LegoList legoList) throws WriteException;

    /**
     * Delete the specified legoList (and all LEGO children contained within).
     *
     * @throws WriteException if the operation fails
     */
    public void deleteLegoList(String legoListUUID) throws WriteException;

    /**
     * This method always adds a new LEGO to the specified legoList - even if a LEGO with the same UUID already exists in the legoList.
     *
     * The new LEGO will be differentiated from other LEGOs with the same UUID by the STAMP value (status, time, author, module, path)
     *
     * The status field of the STAMP may be set by the caller, prior to sending in the LEGO. All of the other fields will be set automatically during
     * the commit process. Any values other than status provided by the caller will be ignored. If the STAMP object is missing, or the status field is
     * not specified by the caller, the default status will be used during the commit process.
     *
     * The updated STAMP will be returned to the caller.
     *
     * @return The STAMP given to the LEGO as committed.
     *
     * @throws WriteException if the specified legoList does not exist or if the write operation fails for any reason.
     */
    public Stamp commitLego(Lego lego, String legoListUUID) throws WriteException;
    
    /**
     * Call to notify the backend to shutdown cleanly.
     */
    public void shutdown();
}

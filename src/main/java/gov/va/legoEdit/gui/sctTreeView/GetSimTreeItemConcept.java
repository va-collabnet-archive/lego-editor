package gov.va.legoEdit.gui.sctTreeView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.Callable;
import javafx.application.Platform;
import org.ihtsdo.otf.tcc.api.coordinate.StandardViewCoordinates;
import org.ihtsdo.otf.tcc.ddo.ComponentReference;
import org.ihtsdo.otf.tcc.ddo.TaxonomyReferenceWithConcept;
import org.ihtsdo.otf.tcc.ddo.concept.ConceptChronicleDdo;
import org.ihtsdo.otf.tcc.ddo.concept.component.relationship.RelationshipChronicleDdo;
import org.ihtsdo.otf.tcc.ddo.concept.component.relationship.RelationshipVersionDdo;
import org.ihtsdo.otf.tcc.ddo.fetchpolicy.RefexPolicy;
import org.ihtsdo.otf.tcc.ddo.fetchpolicy.RelationshipPolicy;
import org.ihtsdo.otf.tcc.ddo.fetchpolicy.VersionPolicy;
import org.ihtsdo.otf.tcc.ddo.store.FxTerminologyStoreDI;
 
/**
 *
 * @author kec
 */
class GetSimTreeItemConcept implements Callable<Boolean> {
   ArrayList<SimTreeItem> childrenToAdd      = new ArrayList<>();
   boolean                addChildren        = true;
   VersionPolicy          versionPolicy      = VersionPolicy.ACTIVE_VERSIONS;
   RelationshipPolicy     relationshipPolicy =
      RelationshipPolicy.ORIGINATING_AND_DESTINATION_TAXONOMY_RELATIONSHIPS;
   RefexPolicy refexPolicy = RefexPolicy.ANNOTATION_MEMBERS;
   ConceptChronicleDdo   concept;
   SimTreeItem treeItem;
   FxTerminologyStoreDI ts;

   //~--- constructors --------------------------------------------------------

   public GetSimTreeItemConcept(SimTreeItem treeItem, FxTerminologyStoreDI ts) {
      this.treeItem = treeItem;
      this.ts = ts;
   }

   public GetSimTreeItemConcept(SimTreeItem treeItem, boolean addChildren, FxTerminologyStoreDI ts) {
      this.treeItem    = treeItem;
      this.addChildren = addChildren;
      this.ts = ts;
   }

   public GetSimTreeItemConcept(SimTreeItem treeItem, VersionPolicy versionPolicy, RefexPolicy refexPolicy,
                                RelationshipPolicy relationshipPolicy, FxTerminologyStoreDI ts) {
      this.treeItem           = treeItem;
      this.versionPolicy      = versionPolicy;
      this.refexPolicy        = refexPolicy;
      this.relationshipPolicy = relationshipPolicy;
      this.ts = ts;
   }

   //~--- methods -------------------------------------------------------------

   @Override
   public Boolean call() throws Exception {
      ComponentReference reference;

      if (addChildren) {
         reference = treeItem.getValue().getRelationshipVersion().getOriginReference();
      } else {
         reference = treeItem.getValue().getRelationshipVersion().getDestinationReference();
      }

      if (SimTreeView.shutdownRequested)
      {
          return false;
      }
      
      concept = ts.getFxConcept(reference,
              StandardViewCoordinates.getSnomedStatedLatest(), versionPolicy, refexPolicy,
              relationshipPolicy);

      if ((concept.getConceptAttributes() == null) || concept.getConceptAttributes().getVersions().isEmpty()
              || concept.getConceptAttributes().getVersions().get(0).isDefined()) {
         treeItem.setDefined(true);
      }

      if (concept.getOriginRelationships().size() > 1) {
         treeItem.setMultiParent(true);
      }

      if (addChildren) {
         for (RelationshipChronicleDdo fxrc : concept.getDestinationRelationships()) {
             if (SimTreeView.shutdownRequested)
             {
                 return false;
             }
             for (RelationshipVersionDdo rv : fxrc.getVersions()) {
               TaxonomyReferenceWithConcept fxtrc     = new TaxonomyReferenceWithConcept(rv);
               SimTreeItem                    childItem = new SimTreeItem(fxtrc, ts);

               childrenToAdd.add(childItem);
            }
         }
      }
      if (SimTreeView.shutdownRequested)
      {
          return false;
      }

      Collections.sort(childrenToAdd);
      Platform.runLater(new Runnable() {
         @Override
         public void run() {
            TaxonomyReferenceWithConcept itemValue = treeItem.getValue();

            treeItem.setValue(null);
            treeItem.getChildren().clear();
            treeItem.computeGraphic();
            treeItem.getChildren().addAll(childrenToAdd);
            treeItem.setValue(itemValue);
            treeItem.getValue().conceptProperty().set(concept);
         }
      });

      return true;
   }
}

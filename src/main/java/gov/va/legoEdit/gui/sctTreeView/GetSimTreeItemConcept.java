package gov.va.legoEdit.gui.sctTreeView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.Callable;
import javafx.application.Platform;
import org.ihtsdo.fxmodel.FxComponentReference;
import org.ihtsdo.fxmodel.FxTaxonomyReferenceWithConcept;
import org.ihtsdo.fxmodel.concept.FxConcept;
import org.ihtsdo.fxmodel.concept.component.relationship.FxRelationshipChronicle;
import org.ihtsdo.fxmodel.concept.component.relationship.FxRelationshipVersion;
import org.ihtsdo.fxmodel.fetchpolicy.RefexPolicy;
import org.ihtsdo.fxmodel.fetchpolicy.RelationshipPolicy;
import org.ihtsdo.fxmodel.fetchpolicy.VersionPolicy;
import org.ihtsdo.tk.api.TerminologyStoreDI;
import org.ihtsdo.tk.api.coordinate.StandardViewCoordinates;
 
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
   FxConcept   concept;
   SimTreeItem treeItem;
   TerminologyStoreDI ts;

   //~--- constructors --------------------------------------------------------

   public GetSimTreeItemConcept(SimTreeItem treeItem, TerminologyStoreDI ts) {
      this.treeItem = treeItem;
      this.ts = ts;
   }

   public GetSimTreeItemConcept(SimTreeItem treeItem, boolean addChildren, TerminologyStoreDI ts) {
      this.treeItem    = treeItem;
      this.addChildren = addChildren;
      this.ts = ts;
   }

   public GetSimTreeItemConcept(SimTreeItem treeItem, VersionPolicy versionPolicy, RefexPolicy refexPolicy,
                                RelationshipPolicy relationshipPolicy, TerminologyStoreDI ts) {
      this.treeItem           = treeItem;
      this.versionPolicy      = versionPolicy;
      this.refexPolicy        = refexPolicy;
      this.relationshipPolicy = relationshipPolicy;
      this.ts = ts;
   }

   //~--- methods -------------------------------------------------------------

   @Override
   public Boolean call() throws Exception {
      FxComponentReference reference;

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
              StandardViewCoordinates.getSnomedLatest(), versionPolicy, refexPolicy,
              relationshipPolicy);

      if ((concept.getConceptAttributes() == null) || concept.getConceptAttributes().getVersions().isEmpty()
              || concept.getConceptAttributes().getVersions().get(0).isDefined()) {
         treeItem.setDefined(true);
      }

      if (concept.getOriginRelationships().size() > 1) {
         treeItem.setMultiParent(true);
      }

      if (addChildren) {
         for (FxRelationshipChronicle fxrc : concept.getDestinationRelationships()) {
             if (SimTreeView.shutdownRequested)
             {
                 return false;
             }
             for (FxRelationshipVersion rv : fxrc.getVersions()) {
               FxTaxonomyReferenceWithConcept fxtrc     = new FxTaxonomyReferenceWithConcept(rv);
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
            FxTaxonomyReferenceWithConcept itemValue = treeItem.getValue();

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

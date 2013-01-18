package gov.va.legoEdit.gui.legoTreeView;

import gov.va.legoEdit.gui.util.Images;
import gov.va.legoEdit.gui.util.Utility;
import gov.va.legoEdit.model.schemaModel.Measurement;
import gov.va.legoEdit.model.schemaModel.MeasurementConstant;
import gov.va.legoEdit.model.schemaModel.Point;
import gov.va.legoEdit.model.schemaModel.PointDouble;
import gov.va.legoEdit.model.schemaModel.PointLong;
import gov.va.legoEdit.model.schemaModel.PointMeasurementConstant;
import java.util.Observable;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PointNode extends Observable
{
    public enum PointNodeType {point, boundLow, boundHigh, intervalLowBoundLow, intervalLowBoundHigh, intervalHighBoundLow, intervalHighBoundHigh};
    Logger logger = LoggerFactory.getLogger(PointNode.class);
    
    private StackPane sp_;
    private ComboBox<String> cb_;
    private LegoTreeView ltv_;
    private Measurement measurement_;
    private PointNodeType type_;
    private StringProperty localInvalidMessage_ = new SimpleStringProperty();
    private StringProperty groupInvalidMessage_ = new SimpleStringProperty();
    private Tooltip tooltip_;
    private ImageView invalidImage_;
    
    public PointNode(Measurement m, PointNodeType type, LegoTreeView legoTreeView)
    {
        ltv_ = legoTreeView;
        measurement_ = m;
        type_ = type;
        cb_ = new ComboBox<>();
        cb_.setEditable(true);
        for (MeasurementConstant s : MeasurementConstant.values())
        {
            cb_.getItems().add(s.value());
        }
        cb_.setPromptText("Number or select item");
        cb_.setVisibleRowCount(MeasurementConstant.values().length + 2);
        cb_.setMaxWidth(Double.MAX_VALUE);
        cb_.setMinWidth(200.0);
        cb_.setPrefWidth(200.0);
        
        cb_.valueProperty().addListener(new ChangeListener<String>()
        {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue)
            {
                try
                {
                    if (newValue.contains("."))
                    {
                        PointDouble p = new PointDouble();
                        p.setValue(Double.parseDouble(newValue.trim()));
                        setPoint(p);
                    }
                    else
                    {
                        PointLong p = new PointLong();
                        p.setValue(Long.parseLong(newValue.trim()));
                        setPoint(p);
                    }
                    localInvalidMessage_.set(null);
                }
                catch (NumberFormatException e)
                {
                    try
                    {
                        MeasurementConstant ms = MeasurementConstant.fromValue(newValue);
                        PointMeasurementConstant p = new PointMeasurementConstant();
                        p.setValue(ms);
                        setPoint(p);
                        localInvalidMessage_.set(null);
                    }
                    catch (IllegalArgumentException ex)
                    {
                        setPoint(null);
                        if (newValue.length() > 0)
                        {
                            localInvalidMessage_.set("The value must be a number, or an item from the drop down");
                        }
                        else
                        {
                            //blank might be ok, but we can't tell without more info - needs to happen in group validation
                            localInvalidMessage_.set(null);
                        }
                    }
                }
                PointNode.this.setChanged();
                PointNode.this.notifyObservers();
                PointNode.this.clearChanged();
            }
        });
        
        BooleanBinding valid = new BooleanBinding()
        {
            {
                bind(localInvalidMessage_, groupInvalidMessage_);
            }
            @Override
            protected boolean computeValue()
            {
                return (localInvalidMessage_.get() == null || localInvalidMessage_.get().length() == 0) 
                        && (groupInvalidMessage_.get() == null || groupInvalidMessage_.get().length() == 0);
            }
        };
        valid.addListener(new ChangeListener<Boolean>()
        {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue)
            {
                if (newValue)
                {
                    cb_.setEffect(null);
                    tooltip_.setText("");
                }
                else
                {
                    cb_.setEffect(Utility.redDropShadow);
                    tooltip_.setText((localInvalidMessage_.get() != null && localInvalidMessage_.get().length() > 0) 
                            ? localInvalidMessage_.get() : groupInvalidMessage_.get());
                }
            }
        });
        
        Point p = getPoint();
        
        if (p instanceof PointMeasurementConstant)
        {
            PointMeasurementConstant pmc = (PointMeasurementConstant)p;
            if (pmc.getValue() != null)
            {
                cb_.getSelectionModel().select(pmc.getValue().value());
            }
        }
        else if (p instanceof PointDouble)
        {
            cb_.setValue(((PointDouble)p).getValue() + "");
        }
        else if (p instanceof PointLong)
        {
            cb_.setValue(((PointLong)p).getValue() + "");
        }
        
        invalidImage_ = Images.EXCLAMATION.createImageView();
        invalidImage_.visibleProperty().bind(valid.not());
        tooltip_ = new Tooltip();
        Tooltip.install(invalidImage_, tooltip_);
        valid.invalidate();
        
        sp_ = new StackPane();
        sp_.getChildren().add(cb_);
        sp_.getChildren().add(invalidImage_);
        StackPane.setAlignment(invalidImage_, Pos.CENTER_RIGHT);
        StackPane.setMargin(invalidImage_, new Insets(0.0, 20.0, 0.0, 0.0));
    }
    
    public Node getNode()
    {
        return sp_;
    }
    
    protected Point getPoint()
    {
        try
        {
            switch (type_)
            {
                case point:
                    return measurement_.getPoint();
                case boundHigh: 
                    return measurement_.getBound().getUpperPoint();
                case boundLow: 
                    return measurement_.getBound().getLowerPoint();
                case intervalLowBoundLow:
                    return measurement_.getInterval().getLowerBound().getLowerPoint();
                case intervalLowBoundHigh:
                    return measurement_.getInterval().getLowerBound().getUpperPoint();
                case intervalHighBoundLow:
                    return measurement_.getInterval().getUpperBound().getLowerPoint();
                case intervalHighBoundHigh:
                    return measurement_.getInterval().getUpperBound().getUpperPoint();
                default:
                    return null;
            }
        }
        catch (NullPointerException e)
        {
            return null;
        }
    }
    
    protected void setPoint(Point p)
    {
        switch (type_)
        {
            case point:
                measurement_.setPoint(p);
                measurement_.setInterval(null);
                measurement_.setBound(null);
                break;
            case boundHigh: 
                measurement_.getBound().setUpperPoint(p);
                measurement_.setPoint(null);
                measurement_.setInterval(null);
                break;
            case boundLow: 
                measurement_.getBound().setLowerPoint(p);
                measurement_.setPoint(null);
                measurement_.setInterval(null);
                break;
            case intervalLowBoundLow:
                measurement_.getInterval().getLowerBound().setLowerPoint(p);
                measurement_.setPoint(null);
                measurement_.setBound(null);
                break;
            case intervalLowBoundHigh:
                measurement_.getInterval().getLowerBound().setUpperPoint(p);
                measurement_.setPoint(null);
                measurement_.setBound(null);
                break;
            case intervalHighBoundLow:
                measurement_.getInterval().getUpperBound().setLowerPoint(p);
                measurement_.setPoint(null);
                measurement_.setBound(null);
                break;
            case intervalHighBoundHigh:
                measurement_.getInterval().getUpperBound().setUpperPoint(p);
                measurement_.setPoint(null);
                measurement_.setBound(null);
                break;
            default:
                logger.error("bad type?  Save not processed");
        }
        ltv_.contentChanged();
    }
    
    protected void setGroupInvalidMessage(String message)
    {
        groupInvalidMessage_.set(message);
    }
}

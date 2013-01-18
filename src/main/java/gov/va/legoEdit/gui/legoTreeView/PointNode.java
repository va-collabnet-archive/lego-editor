package gov.va.legoEdit.gui.legoTreeView;

import gov.va.legoEdit.gui.util.Utility;
import gov.va.legoEdit.model.schemaModel.Measurement;
import gov.va.legoEdit.model.schemaModel.MeasurementConstant;
import gov.va.legoEdit.model.schemaModel.Point;
import gov.va.legoEdit.model.schemaModel.PointDouble;
import gov.va.legoEdit.model.schemaModel.PointLong;
import gov.va.legoEdit.model.schemaModel.PointMeasurementConstant;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PointNode
{
    public enum PointNodeType {point, boundLow, boundHigh, intervalLowBoundLow, intervalLowBoundHigh, intervalHighBoundLow, intervalHighBoundHigh};
    Logger logger = LoggerFactory.getLogger(PointNode.class);
    
    private ComboBox<String> cb_;
    private LegoTreeView ltv_;
    
    public PointNode(final Measurement m, final PointNodeType type, LegoTreeView legoTreeView)
    {
        ltv_ = legoTreeView;
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
        
        Point p = getPoint(m, type);
        if (p == null)
        {
            //just leave cb_ blank
        }
        else
        {
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
        }

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
                        p.setValue(Double.parseDouble(newValue));
                        setPoint(m, p, type);
                    }
                    else
                    {
                        PointLong p = new PointLong();
                        p.setValue(Long.parseLong(newValue));
                        setPoint(m, p, type);
                    }
                    cb_.setEffect(null);
                }
                catch (NumberFormatException e)
                {
                    try
                    {
                        MeasurementConstant ms = MeasurementConstant.fromValue(newValue);
                        PointMeasurementConstant p = new PointMeasurementConstant();
                        p.setValue(ms);
                        setPoint(m, p, type);
                        cb_.setEffect(null);
                    }
                    catch (IllegalArgumentException ex)
                    {
                        //TODO completely redo validation on the pointnodes - needs to be done at a higher level.
                        cb_.setEffect(Utility.redDropShadow);
                        setPoint(m, null, type);
                    }
                }
                ltv_.contentChanged();
            }
        });
    }
    
    public Node getNode()
    {
        return cb_;
    }
    
    private Point getPoint(Measurement m, PointNodeType type)
    {
        try
        {
            switch (type)
            {
                case point:
                    return m.getPoint();
                case boundHigh: 
                    return m.getBound().getUpperPoint();
                case boundLow: 
                    return m.getBound().getLowerPoint();
                case intervalLowBoundLow:
                    return m.getInterval().getLowerBound().getLowerPoint();
                case intervalLowBoundHigh:
                    return m.getInterval().getLowerBound().getUpperPoint();
                case intervalHighBoundLow:
                    return m.getInterval().getUpperBound().getLowerPoint();
                case intervalHighBoundHigh:
                    return m.getInterval().getUpperBound().getUpperPoint();
                default:
                    return null;
            }
        }
        catch (NullPointerException e)
        {
            return null;
        }
    }
    
    private void setPoint(Measurement m, Point p, PointNodeType type)
    {
        switch (type)
        {
            case point:
                m.setPoint(p);
                m.setInterval(null);
                m.setBound(null);
                break;
            case boundHigh: 
                m.getBound().setUpperPoint(p);
                m.setPoint(null);
                m.setInterval(null);
                break;
            case boundLow: 
                m.getBound().setLowerPoint(p);
                m.setPoint(null);
                m.setInterval(null);
                break;
            case intervalLowBoundLow:
                m.getInterval().getLowerBound().setLowerPoint(p);
                m.setPoint(null);
                m.setBound(null);
                break;
            case intervalLowBoundHigh:
                m.getInterval().getLowerBound().setUpperPoint(p);
                m.setPoint(null);
                m.setBound(null);
                break;
            case intervalHighBoundLow:
                m.getInterval().getUpperBound().setLowerPoint(p);
                m.setPoint(null);
                m.setBound(null);
                break;
            case intervalHighBoundHigh:
                m.getInterval().getUpperBound().setUpperPoint(p);
                m.setPoint(null);
                m.setBound(null);
                break;
            default:
                logger.error("bad type?  Save not processed");
        }
    }
}

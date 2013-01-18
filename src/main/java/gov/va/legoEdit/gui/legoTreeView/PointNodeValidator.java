package gov.va.legoEdit.gui.legoTreeView;

import gov.va.legoEdit.model.schemaModel.PointDouble;
import gov.va.legoEdit.model.schemaModel.PointLong;
import gov.va.legoEdit.model.schemaModel.PointMeasurementConstant;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

public class PointNodeValidator implements Observer
{
    private ArrayList<PointNode> pointNodes_ = new ArrayList<>();

    /**
     * Expecting 1, 2, or 4 PointNode objects, which will be validated together. 1 for standalone point, 2 for a bound,
     * 4 for an interval. Should be passed in in order for bound and interval (low to high)
     */
    protected void addPointNode(PointNode pn)
    {
        pointNodes_.add(pn);
        pn.addObserver(this);
    }

    @Override
    public void update(Observable o, Object arg)
    {
        check();
    }
    
    protected void check()
    {
        if (pointNodes_.size() == 1)
        {
            if (hasValue(pointNodes_.get(0)))
            {
                pointNodes_.get(0).setGroupInvalidMessage(null);
            }
            else
            {
                pointNodes_.get(0).setGroupInvalidMessage("A value must be specified");
            }
        }
        else if (pointNodes_.size() == 2)
        {
            String[] messages = new String[4];
            //At least one should have a value
            if (!hasValue(pointNodes_.get(0)) && !hasValue(pointNodes_.get(1))) 
            {
                for (int i = 0; i <= 1; i++)
                {
                    messages[i] = "At least one value must be specified";
                }
            }
            
            // lower should be less than upper
            if (isNumber(pointNodes_.get(0)) && isNumber(pointNodes_.get(1)))
            {
                if (getNumber(pointNodes_.get(0)).doubleValue() >= getNumber(pointNodes_.get(1)).doubleValue())
                {
                    for (int i = 0; i <= 1; i++)
                    {
                        if (messages[i] == null)
                        {
                            messages[i] = "The left number must be lower than the right number";
                        }
                    }
                }
            }
            
            for (int i = 0; i <= 1; i++)
            {
                pointNodes_.get(i).setGroupInvalidMessage(messages[i]);
            }
        }
        else if (pointNodes_.size() == 4)
        {
            String[] messages = new String[4];
            
            //At least one should have a value
            if (!hasValue(pointNodes_.get(0)) && !hasValue(pointNodes_.get(1)) &&
                    !hasValue(pointNodes_.get(2)) && !hasValue(pointNodes_.get(3))) 
            {
                for (int i = 0; i < 4; i++)
                {
                    messages[i] = "At least one value must be specified";
                }
            }
            
            // lower should be less than upper, on each of the lower bound and the upper bound.
            if (isNumber(pointNodes_.get(0)) && isNumber(pointNodes_.get(1)))
            {
                if (getNumber(pointNodes_.get(0)).doubleValue() >= getNumber(pointNodes_.get(1)).doubleValue())
                {
                    for (int i = 0; i <= 1; i++)
                    {
                        if (messages[i] == null)
                        {
                            messages[i] = "The left number must be lower than the right number";
                        }
                    }
                }
            }
            
            if (isNumber(pointNodes_.get(2)) && isNumber(pointNodes_.get(3)))
            {
                if (getNumber(pointNodes_.get(2)).doubleValue() >= getNumber(pointNodes_.get(3)).doubleValue())
                {
                    for (int i = 2; i <= 3; i++)
                    {
                        if (messages[i] == null)
                        {
                            messages[i] = "The left number must be lower than the right number";
                        }
                    }
                }
            }
            
            //And finally, the largest of the lower bound should be less than the smallest of the upper bound
            if ((isNumber(pointNodes_.get(0)) || isNumber(pointNodes_.get(1))) &&
                    (isNumber(pointNodes_.get(2)) || isNumber(pointNodes_.get(3))))
            {
                double lower = (isNumber(pointNodes_.get(1)) ? getNumber(pointNodes_.get(1)).doubleValue() : getNumber(pointNodes_.get(0)).doubleValue());
                double upper = (isNumber(pointNodes_.get(2)) ? getNumber(pointNodes_.get(2)).doubleValue() : getNumber(pointNodes_.get(3)).doubleValue());
                if (upper <= lower)
                {
                    for (int i = 0; i < 4; i++)
                    {
                        if (messages[i] == null)
                        {
                            messages[i] = "The lower bound must be lower than the upper bound";
                        }
                    }
                }
            }
            
            for (int i = 0; i < 4; i++)
            {
                pointNodes_.get(i).setGroupInvalidMessage(messages[i]);
            }
        }
        else
        {
            throw new IllegalArgumentException("Must have 1, 2 or 4 point nodes");
        }
    }
    
    private boolean hasValue(PointNode pn)
    {
        if (pn == null)
        {
            return false;
        }
        if (pn.getPoint() instanceof PointLong)
        {
            return true;
        }
        else if (pn.getPoint() instanceof PointDouble)
        {
            return true;
        }
        else if (pn.getPoint() instanceof PointMeasurementConstant)
        {
            return ((PointMeasurementConstant)pn.getPoint()).getValue() != null;
        }
        return false;
    }
    
    private boolean isNumber(PointNode pn)
    {
        if (pn.getPoint() instanceof PointLong)
        {
            return true;
        }
        else if (pn.getPoint() instanceof PointDouble)
        {
            return true;
        }
        else
        {
            return false;
        }
    }
    
    private Number getNumber(PointNode pn)
    {
        if (pn.getPoint() instanceof PointLong)
        {
            return ((PointLong)pn.getPoint()).getValue();
        }
        else if (pn.getPoint() instanceof PointDouble)
        {
            return ((PointDouble)pn.getPoint()).getValue();
        }
        else
        {
            return null;
        }
    }

}

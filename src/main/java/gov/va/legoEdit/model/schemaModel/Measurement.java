//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2012.10.16 at 08:28:41 AM CDT 
//


package gov.va.legoEdit.model.schemaModel;

import com.sleepycat.persist.model.Persistent;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{}units" minOccurs="0"/>
 *         &lt;choice>
 *           &lt;element name="stringValue" type="{}measurementString"/>
 *           &lt;element name="numericValue" type="{http://www.w3.org/2001/XMLSchema}float"/>
 *           &lt;element ref="{}measuredRange"/>
 *         &lt;/choice>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "units",
    "stringValue",
    "numericValue",
    "measuredRange"
})
@XmlRootElement(name = "measurement")
@Persistent
public class Measurement {

    protected Units units;
    protected MeasurementString stringValue;
    protected Float numericValue;
    protected MeasuredRange measuredRange;

    /**
     * Gets the value of the units property.
     * 
     * @return
     *     possible object is
     *     {@link Units }
     *     
     */
    public Units getUnits() {
        return units;
    }

    /**
     * Sets the value of the units property.
     * 
     * @param value
     *     allowed object is
     *     {@link Units }
     *     
     */
    public void setUnits(Units value) {
        this.units = value;
    }

    /**
     * Gets the value of the stringValue property.
     * 
     * @return
     *     possible object is
     *     {@link MeasurementString }
     *     
     */
    public MeasurementString getStringValue() {
        return stringValue;
    }

    /**
     * Sets the value of the stringValue property.
     * 
     * @param value
     *     allowed object is
     *     {@link MeasurementString }
     *     
     */
    public void setStringValue(MeasurementString value) {
        this.stringValue = value;
    }

    /**
     * Gets the value of the numericValue property.
     * 
     * @return
     *     possible object is
     *     {@link Float }
     *     
     */
    public Float getNumericValue() {
        return numericValue;
    }

    /**
     * Sets the value of the numericValue property.
     * 
     * @param value
     *     allowed object is
     *     {@link Float }
     *     
     */
    public void setNumericValue(Float value) {
        this.numericValue = value;
    }

    /**
     * Gets the value of the measuredRange property.
     * 
     * @return
     *     possible object is
     *     {@link MeasuredRange }
     *     
     */
    public MeasuredRange getMeasuredRange() {
        return measuredRange;
    }

    /**
     * Sets the value of the measuredRange property.
     * 
     * @param value
     *     allowed object is
     *     {@link MeasuredRange }
     *     
     */
    public void setMeasuredRange(MeasuredRange value) {
        this.measuredRange = value;
    }

}

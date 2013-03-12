package gov.va.legoEdit.model.userPrefs;

import gov.va.legoEdit.LegoGUI;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
@XmlRootElement(name = "userPreferences")
public class UserPreferences
{
	@XmlAttribute(name = "author") protected String author;
	@XmlAttribute(name = "module") protected String module;
	@XmlAttribute(name = "path") protected String path;
	@XmlAttribute(name = "showSummary") protected boolean showSummary = true;
	@XmlAttribute(name = "useFSN") protected boolean useFSN = true;

	/**
	 * Gets the value of the author property.
	 * 
	 * @return
	 *         possible object is {@link String }
	 * 
	 */
	public String getAuthor()
	{
		return author;
	}

	/**
	 * Sets the value of the author property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setAuthor(String value)
	{
		this.author = value;
	}

	/**
	 * Gets the value of the module property.
	 * 
	 * @return
	 *         possible object is {@link String }
	 * 
	 */
	public String getModule()
	{
		return module;
	}

	/**
	 * Sets the value of the module property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setModule(String value)
	{
		this.module = value;
	}

	/**
	 * Gets the value of the path property.
	 * 
	 * @return
	 *         possible object is {@link String }
	 * 
	 */
	public String getPath()
	{
		return path;
	}

	/**
	 * Sets the value of the path property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setPath(String value)
	{
		this.path = value;
	}
	

	public boolean getShowSummary()
	{
		return showSummary;
	}

	public void setShowSummary(boolean showSummary)
	{
		boolean old = this.showSummary;
		this.showSummary = showSummary;
		if (old != this.showSummary)
		{
			LegoGUI.getInstance().getLegoGUIController().showLegoSummaryPrefChanged();
		}
	}
	
	public boolean getUseFSN()
	{
		return useFSN;
	}

	public void setUseFSN(boolean useFSN)
	{
		boolean old = this.useFSN;
		this.useFSN = useFSN;
		if (old !=  this.useFSN)
		{
			LegoGUI.getInstance().getLegoGUIController().rebuildSCTTree();
		}
	}
}

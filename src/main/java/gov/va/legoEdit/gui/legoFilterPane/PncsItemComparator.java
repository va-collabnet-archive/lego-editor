package gov.va.legoEdit.gui.legoFilterPane;

import gov.va.legoEdit.gui.util.AlphanumComparator;
import java.util.Comparator;

public class PncsItemComparator implements Comparator<PncsItem>
{
	private AlphanumComparator ac = new AlphanumComparator(true);

	@Override
	public int compare(PncsItem o1, PncsItem o2)
	{
		if (o1.getName().equals(LegoFilterPaneController.ANY))
		{
			return -1;
		}
		else if (o2.getName().equals(LegoFilterPaneController.ANY))
		{
			return 1;
		}
		else
		{
			return ac.compare(o1.toString(), o2.toString());
		}
	}

}

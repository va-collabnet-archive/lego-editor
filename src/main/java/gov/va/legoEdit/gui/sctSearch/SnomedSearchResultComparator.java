package gov.va.legoEdit.gui.sctSearch;

import java.util.Comparator;

public class SnomedSearchResultComparator implements Comparator<SnomedSearchResult>
{
	@Override
	public int compare(SnomedSearchResult o1, SnomedSearchResult o2)
	{
		return o1.getSortOrder() - o2.getSortOrder();
	}
}

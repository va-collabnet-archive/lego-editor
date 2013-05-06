package gov.va.legoEdit.gui.sctSearch;

import java.util.Comparator;

public class SnomedSearchResultComparator implements Comparator<SnomedSearchResult>
{
	@Override
	/**
	 * Note, this sorts in reverse, so it goes highest to lowest
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	public int compare(SnomedSearchResult o1, SnomedSearchResult o2)
	{
		if (o1.getBestScore() < o2.getBestScore())
		{
			return 1;
		}
		else if (o1.getBestScore() > o2.getBestScore())
		{
			return -1;
		}
		return 0;
	}
}

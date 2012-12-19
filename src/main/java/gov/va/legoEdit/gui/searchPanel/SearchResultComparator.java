package gov.va.legoEdit.gui.searchPanel;

import java.util.Comparator;

public class SearchResultComparator implements Comparator<SearchResult>
{
    @Override
    public int compare(SearchResult o1, SearchResult o2)
    {
        return o1.getSortOrder() - o2.getSortOrder();
    }
}

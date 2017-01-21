package com.zagorapps.utilities_suite.state.models;

import java.util.List;

/**
 * Created by eyssy on 21/01/2017.
 */

public class ClipboardDataList extends CustomAbstractListBase<ClipboardData>
{
    public ClipboardDataList()
    {
        super(null);
    }

    public ClipboardDataList(List<ClipboardData> item)
    {
        super(item);
    }
}

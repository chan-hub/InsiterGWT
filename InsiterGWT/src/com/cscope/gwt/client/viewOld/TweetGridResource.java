package com.cscope.gwt.client.viewOld;

import com.google.gwt.user.cellview.client.DataGrid;

public interface TweetGridResource extends DataGrid.Resources {

  @Source({ DataGrid.Style.DEFAULT_CSS, "TweetGrid.css" })
  TweetGridStyle dataGridStyle();

}


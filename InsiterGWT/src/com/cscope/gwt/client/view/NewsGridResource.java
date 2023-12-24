package com.cscope.gwt.client.view;

import com.google.gwt.user.cellview.client.DataGrid;

public interface NewsGridResource extends DataGrid.Resources {

  @Source({ DataGrid.Style.DEFAULT_CSS, "NewsListGrid.css" })
  NewsGridStyle dataGridStyle();
}


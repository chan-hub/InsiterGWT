package com.cscope.gwt.client.view;

import com.cscope.gwt.client.presenter.NewsPresenter;
import com.cscope.gwt.client.view.NewsListGrid;
import com.cscope.gwt.client.view.cell.ProfileIdxCell;
import com.cscope.gwt.client.view.cell.TweetBodyComposite;
import com.cscope.gwt.client.view.newscell.OldNewsBodyComposite;
//import com.cscope.gwt.shared.NewsContents;
import com.cscope.gwt.client.view.newscell._NewsCompositeCell;
import com.cscope.gwt.client.view.newscell.NewsCompositeCell;
import com.cscope.gwt.client.view.newscell.NewsPhotoCell;
import com.google.gwt.cell.client.CompositeCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.dom.client.ScrollEvent;
import com.google.gwt.event.dom.client.ScrollHandler;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DecoratorPanel;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SimpleLayoutPanel;
import com.google.gwt.user.client.ui.HTMLTable;
import com.google.gwt.user.client.ui.HeaderPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.CellPreviewEvent;
import com.google.web.bindery.event.shared.EventBus;

import insiter.ina.news.service.web.NewsContents;

import java.util.ArrayList;
import java.util.List;

//public class TweetsView extends Composite implements TweetsPresenter.Display {
public class NewsListView implements NewsPresenter.Display {
  
  private SimpleLayoutPanel slp;
  private NewsListGrid<NewsContents> newsListGrid;

  private HTML emptyListWidget;
  
  @SuppressWarnings("deprecation")
  public NewsListView(EventBus eBus) {
    initEmptyMsg();

    // Instance NewsListGrid to support scroll position control
    int pageSize = 200;
    this.newsListGrid = this.createNewsListGridWithColumn(pageSize, eBus);

    // Populate layouts
    slp = new SimpleLayoutPanel();

    // Add newsListGrid to our panel
    slp.add(this.newsListGrid);

    //RootLayoutPanel.get().add(slp);
  }

  private void initEmptyMsg() {
	  emptyListWidget=new HTML("No news found!");
	  Style style = emptyListWidget.getElement().getStyle();
	  style.setPaddingTop(20,Unit.PX);
	  style.setFontSize(16,Unit.PX);
  }

  public SimpleLayoutPanel getPanel() {
    return this.slp;
  }

  /**
   * Get ScrollPanel out of existing SimpleLayoutPanel containing DataGrid
   */
  public ScrollPanel getScrollPanel() {
    return this.newsListGrid.getScrollPanel();
  }

  /**
   * Rendering NewsList data with scroll position adjustment
   */
  public void setData(List<NewsContents> newsList) {
    // Set index to NewsContents
    for (int idx=0; idx<newsList.size(); ++idx) {
      NewsContents nct = newsList.get(idx);
      nct.index = (idx+1) + "";
    }
    this.newsListGrid.setRowCount(newsList.size(), true);
    // Push the data into the widget
    this.newsListGrid.setRowData(0, newsList);

    // Use Scheduler to adjust the scroll position after rendering
    Scheduler.get().scheduleDeferred(new ScheduledCommand() {
      @Override
      public void execute() {
        newsListGrid.restoreScrollBarPosition();
        //view.getContainerPanel().getElement().setScrollTop(2000);
      }
    });
  }

  static native void clickAnchor(String url)
  /*-{
      var a = document.createElement('a');
      document.body.appendChild(a);
      a.href = url;
      a.click();
  }-*/;

  static private void addClickHandler(final DataGrid<NewsContents> dataGrid) {
    // Add click handler to forward to the News source
    dataGrid.addCellPreviewHandler(
        new CellPreviewEvent.Handler<NewsContents>() {
          @Override public void onCellPreview(
            CellPreviewEvent<NewsContents> event) {
              if ("click".equals(event.getNativeEvent().getType())) {
                int newsIndex = dataGrid.getKeyboardSelectedRow();
                NewsContents newsSelected = dataGrid.getVisibleItem(newsIndex);
                Window.alert("News selected:" + newsSelected.srcTitle);
        // Save the scroll position
       
                clickAnchor(newsSelected.urlFull);
              }
           }
        });
    dataGrid.sinkEvents(Event.ONCLICK);
  }

  /*
   * How to get the screen size ?
   * 
 You want Window.addResizeHandler

Window.addResizeHandler(new ResizeHandler() {

    @Override
    public void onResize(ResizeEvent event) {
        yourCustomLayoutAdjustmentMethod(event.getHeight(), event.getWidth());
    }
});

   ==>No need to resize !
      You may want to review your layout solution.
      There are very few cases when you need to use a ResizeHandler.
      Typically, you can achieve a desired layout either by using a LayoutPanel
     (or Horizontal/Vertical panels) that automatically resize with the window,
      or by using CSS. Then your layout will respond well to any changes in
      a browser window, and you don't need to write any code for that to happen.
    EDIT:
      In order for a widget to resize automatically, the parent widget must implement
      ProvidesResize, and the child widget must implement Resizable.
      FlowPanel does not implement either.
      Once you use it, the chain of resizing events is broken.

      Typically, I use a LayoutPanel for my outmost container.
      It occupies the entire browser window when added to the RootPanel, and it
      adjusts with the Window. Vertical and Horizontal panels are similar.
   */

  static public boolean isMobile() {
    String usrAgnt = Window.Navigator.getUserAgent();
    return usrAgnt.indexOf("Android")>=0 ||
           usrAgnt.indexOf("iPhone")>=0 ||
           usrAgnt.indexOf("Windows Phone")>=0;
  }
  /**
   * Create a NewsListGrid with saving and restoring its scroll position
   * 
   * @param pageSize
   * @param eBus
   * @return
   */
  private NewsListGrid<NewsContents> createNewsListGridWithColumn(int pageSize, EventBus eBus) {
    // Create a News DataGrid with custom DataGrid style
    NewsGridResource newsGridResource = GWT.create(NewsGridResource.class);
    NewsListGrid<NewsContents> newsListGrid = new NewsListGrid<NewsContents>(pageSize, eBus, newsGridResource);
    newsListGrid.setEmptyTableWidget(emptyListWidget);

    // Do not refresh the headers upon data update
    newsListGrid.setAutoHeaderRefreshDisabled(true);

    // News Body containing the title, article-entry, name, counter, time
    NewsCompositeCell newsCell = new NewsCompositeCell(eBus);
    //NewsCompositeCell newsCell = new NewsCompositeCell(eBus);
    newsListGrid.setNewsCompositeCell(newsCell);
    Column<NewsContents, NewsContents> newsBodyColumn =
                              new Column<NewsContents, NewsContents>(newsCell.getCompositeCell()) {
//    Column<News2Show, News2Show> newsBodyColumn =
//        new Column<News2Show, News2Show>(NewsBodyComposite.makeNewsBody(eBus)) {

      @Override
      public NewsContents getValue(NewsContents tweet) {
        return tweet;
      }
    };
    // News photo column
    Column<NewsContents, String> newsPhotoColumn = new Column<NewsContents, String>(new NewsPhotoCell()) {
      @Override
      public String getValue(NewsContents newsConts) {
        if (newsConts.thumbImage!=null && !newsConts.thumbImage.isEmpty()) {
          return newsConts.thumbImage;
        } else {
          return "";
        }
      }
    };
    // Adjust again as horizontal still being cut in Galaxy 720 pixel
    newsListGrid.setColumnWidth(newsBodyColumn, 210, Unit.PX);
    newsListGrid.setColumnWidth(newsPhotoColumn, 92, Unit.PX);
    // Now newsBody goes first - adjusted to fit into 720 pixel Galaxy, June 2023
    newsListGrid.setColumnWidth(newsBodyColumn, 238, Unit.PX);
    newsListGrid.setColumnWidth(newsPhotoColumn, 90, Unit.PX);
    // Adjusted trying to fit 720 pixel Galaxy
    //newsListGrid.setColumnWidth(newsBodyColumn, 256, Unit.PX);
    //newsListGrid.setColumnWidth(newsPhotoColumn, 94, Unit.PX);
// Setting used for long time
    //newsListGrid.setColumnWidth(newsPhotoColumn, 108, Unit.PX);
    //newsListGrid.setColumnWidth(newsBodyColumn, 288, Unit.PX);
//  newsListGrid.setColumnWidth(newsPhotoColumn, 110, Unit.PX);
//  newsListGrid.setColumnWidth(newsBodyColumn, 290, Unit.PX);

    // Add columns w/o the header - body first
    newsListGrid.addColumn(newsBodyColumn);
    newsListGrid.addColumn(newsPhotoColumn);

    return newsListGrid;
  }

//  private NewsListGrid<NewsContents> _createNewsListGridWithColumn(int pageSize, EventBus eBus) {
//    // Create a News DataGrid with custom DataGrid style
//    NewsGridResource newsGridResource = GWT.create(NewsGridResource.class);
//    NewsListGrid<NewsContents> newsListGrid = new NewsListGrid<NewsContents>(pageSize, eBus, newsGridResource);
//
//    newsListGrid.setEmptyTableWidget(emptyListWidget);
//
//    // Do not refresh the headers upon data update
//    newsListGrid.setAutoHeaderRefreshDisabled(true);
//
//    // News photo column
//    Column<NewsContents, String> newsPhotoColumn = new Column<NewsContents, String>(new NewsPhotoCell()) {
//      @Override
//      public String getValue(NewsContents newsConts) {
//        if (newsConts.thumbImage!=null && !newsConts.thumbImage.isEmpty()) {
//          return newsConts.thumbImage;
//        } else {
//          return "";
//        }
//      }
//    };
//
//    // News Body containing the title, article-entry, name, counter, time
//    NewsCompositeCell newsCell = new NewsCompositeCell(eBus);
//    newsListGrid.setNewsCompositeCell(newsCell);
//    Column<NewsContents, NewsContents> newsBodyColumn =
//                              new Column<NewsContents, NewsContents>(newsCell.getCompositeCell()) {
////    Column<News2Show, News2Show> newsBodyColumn =
////        new Column<News2Show, News2Show>(NewsBodyComposite.makeNewsBody(eBus)) {
//
//      @Override
//      public NewsContents getValue(NewsContents tweet) {
//        return tweet;
//      }
//    };
//
//    newsListGrid.setColumnWidth(newsPhotoColumn, 94, Unit.PX);
//    newsListGrid.setColumnWidth(newsBodyColumn, 256, Unit.PX);
//    //newsListGrid.setColumnWidth(newsPhotoColumn, 108, Unit.PX);
//    //newsListGrid.setColumnWidth(newsBodyColumn, 288, Unit.PX);
////  newsListGrid.setColumnWidth(newsPhotoColumn, 110, Unit.PX);
////  newsListGrid.setColumnWidth(newsBodyColumn, 290, Unit.PX);
//
//    // Add columns w/o the header
//    newsListGrid.addColumn(newsPhotoColumn);
//    newsListGrid.addColumn(newsBodyColumn);
//    
//    return newsListGrid;
//  }
}

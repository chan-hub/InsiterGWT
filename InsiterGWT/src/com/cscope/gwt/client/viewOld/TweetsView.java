package com.cscope.gwt.client.viewOld;

import com.cscope.gwt.client.presenter.NewsPresenter;
import com.cscope.gwt.client.view.cell.ProfileIdxCell;
import com.cscope.gwt.client.view.cell.TweetBodyComposite;
import com.cscope.gwt.client.view.newscell.OldNewsBodyComposite;
//import com.cscope.gwt.shared.NewsContents;
import com.cscope.gwt.client.view.newscell.NewsPhotoCell;
import com.google.gwt.core.client.GWT;
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
public class TweetsView implements NewsPresenter.Display {
//  private final Button addButton;
  
  private SimpleLayoutPanel slp;
  private DataGrid<NewsContents> tweetGrid;
  private ScrollPanel scrollPanel;
  private int scrollPosition = 0;
  private HTML emptyListWidget;
  
  @SuppressWarnings("deprecation")
  public TweetsView(EventBus eBus) {
    int pageSize = 200;
    initEmptyMsg();
    this.tweetGrid = this.createNewsGridWithColumn(pageSize, eBus);
//    this.tweetGrid = this.createGridWithColumn(pageSize, eBus);

    // Populate layouts
    //this.scrollPanel = new ScrollPanel();
    slp = new SimpleLayoutPanel();

    // Add tweetGrid to our panel
    //this.scrollPanel.add(this.tweetGrid);
    slp.add(this.tweetGrid);
    //RootLayoutPanel.get().add(slp);

    // Get ScrollPanel to control the scroll position on display
    //this.setScrollPanel();
  }

  private void initEmptyMsg() {
	  emptyListWidget=new HTML("No news found!");
	  Style style = emptyListWidget.getElement().getStyle();
	  style.setPaddingTop(20,Unit.PX);
	  style.setFontSize(16,Unit.PX);
  }

//  public HasClickHandlers getAddButton() {
//    return addButton;
//  }
//

  public ScrollPanel getScrollPanel() {
    return this.scrollPanel;
  }

  public SimpleLayoutPanel getPanel() {
    return this.slp;
  }

  /**
   * Get & set ScrollPanel out of existing SimpleLayoutPanel containing DataGrid
   */
  private void setScrollPanel() {
    // Get the ScrollPanel of this DataGrid
    HeaderPanel headerPanel = (HeaderPanel) slp.getWidget();
    this.scrollPanel = (ScrollPanel) headerPanel.getContentWidget();

//    // Add ScrollHandler
//    this.scrollPanel.addScrollHandler(new ScrollHandler() {
//      @Override
//      public void onScroll(ScrollEvent event) {
//        scrollPosition = scrollPanel.getVerticalScrollPosition();
//      }
//    });
  }

  public void setData(List<NewsContents> newsList) {
    this.tweetGrid.setRowCount(newsList.size(), true);
    // Push the data into the widget.
    this.tweetGrid.setRowData(0, newsList);

//    if (this.scrollPanel.isVisible())
//      Window.alert("TweetsView.setData():ScrollPanel is visible");
//    else
//      Window.alert("TweetsView.setData():ScrollPanel is IN-visible");
//      
    // Set the previous scroll position
//    if (this.scrollPosition > 0)
//      this.scrollPanel.setVerticalScrollPosition(this.scrollPosition);
  }

  /**
   * Rendering Tweet view
   * @param pageSize
   * @param eBus
   * @return
   */
  private DataGrid<NewsContents> createGridWithColumn(int pageSize, EventBus eBus) {

    // Create a DataGrid with custom DataGrid style
    TweetGridResource tweetGridResource = GWT.create(TweetGridResource.class);
    DataGrid<NewsContents> grid = new DataGrid<NewsContents>(pageSize, tweetGridResource);
    
    grid.setEmptyTableWidget(emptyListWidget);

    // Do not refresh the headers upon data update
    grid.setAutoHeaderRefreshDisabled(true);

    // Profile photo & id (@screenName)
    Column<NewsContents, String> photoColumn = new Column<NewsContents, String>(new ProfileIdxCell()) {
      @Override
      public String getValue(NewsContents tweet) {
        return tweet.largeImage + ',' + tweet.index;
      }
    };

    // TweetBody containing name, logo, txt-msg, counter, time, URL and category
    Column<NewsContents, NewsContents> tweetBodyColumn =
        new Column<NewsContents, NewsContents>(TweetBodyComposite.makeTweetBody(eBus)) {

      @Override
      public NewsContents getValue(NewsContents tweet) {
        return tweet;
      }
    };

    // Increase txtColumn width by 24 as the index column is gone
//    grid.setColumnWidth(photoColumn, 60, Unit.PX);
//    grid.setColumnWidth(tweetBodyColumn, 324, Unit.PX);

//    grid.setColumnWidth(photoColumn, 100, Unit.PX);
//    grid.setColumnWidth(tweetBodyColumn, 700, Unit.PX);

//    grid.setColumnWidth(photoColumn, 60, Unit.PX);
//    grid.setColumnWidth(tweetBodyColumn, 420, Unit.PX);

//    grid.setColumnWidth(photoColumn, 40, Unit.PX);
//    grid.setColumnWidth(tweetBodyColumn, 280, Unit.PX);

//    grid.setColumnWidth(photoColumn, 50, Unit.PX);
//    grid.setColumnWidth(tweetBodyColumn, 350, Unit.PX);

    // Re-size to fit into viewport of 400 PX - the photo is 48px by 48px fixed size
    //grid.setColumnWidth(photoColumn, 60, Unit.PX);
    //grid.setColumnWidth(tweetBodyColumn, 340, Unit.PX);
    grid.setColumnWidth(photoColumn, 50, Unit.PX);
    grid.setColumnWidth(tweetBodyColumn, 280, Unit.PX);

    // Add columns w/o the header
    grid.addColumn(photoColumn);
    grid.addColumn(tweetBodyColumn);
    //grid.addColumn(dateColumn);
    
    return grid;
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
                clickAnchor(newsSelected.urlFull);
              }
           }
        });
    dataGrid.sinkEvents(Event.ONCLICK);
  }

  /**
   * Rendering News view
   * @param pageSize
   * @param eBus
   * @return
   */
  private DataGrid<NewsContents> createNewsGridWithColumn(int pageSize, EventBus eBus) {

    // Create a News DataGrid with custom DataGrid style
    TweetGridResource tweetGridResource = GWT.create(TweetGridResource.class);
    DataGrid<NewsContents> grid = new DataGrid<NewsContents>(pageSize, tweetGridResource);
    
    grid.setEmptyTableWidget(emptyListWidget);

    // Do not refresh the headers upon data update
    grid.setAutoHeaderRefreshDisabled(true);

    // Never worked, but then caused side effect to disable menu click !
    // Add click handler
    //addClickHandler(grid);

    // News photo column including the index
    Column<NewsContents, String> newsPhotoColumn = new Column<NewsContents, String>(new NewsPhotoCell()) {
      @Override
      public String getValue(NewsContents tweet) {
        if (tweet.thumbImage!=null && !tweet.thumbImage.isEmpty()) {
          return tweet.thumbImage;
//          return tweet.srcImage + ',' + tweet.index;
        } else {
          return "";
          //return tweet.photo;
//          return tweet.photo + ',' + tweet.index;
        }
      }
    };
// Comment out to remove NewsBodyComposite - Oct 25 2019
//    // News Body containing the title, article-entry, name, counter, time
//    Column<News2Show, News2Show> newsBodyColumn =
//        new Column<News2Show, News2Show>(NewsBodyComposite.makeNewsBody(eBus)) {
//
//      @Override
//      public News2Show getValue(News2Show tweet) {
//        return tweet;
//      }
//    };

    // Increase txtColumn width by 24 as the index column is gone
//    grid.setColumnWidth(photoColumn, 60, Unit.PX);
//    grid.setColumnWidth(tweetBodyColumn, 324, Unit.PX);

//    grid.setColumnWidth(photoColumn, 100, Unit.PX);
//    grid.setColumnWidth(tweetBodyColumn, 700, Unit.PX);

//    grid.setColumnWidth(photoColumn, 60, Unit.PX);
//    grid.setColumnWidth(tweetBodyColumn, 420, Unit.PX);

//    grid.setColumnWidth(photoColumn, 40, Unit.PX);
//    grid.setColumnWidth(tweetBodyColumn, 280, Unit.PX);

//    grid.setColumnWidth(photoColumn, 50, Unit.PX);
//    grid.setColumnWidth(tweetBodyColumn, 350, Unit.PX);

    // Re-size to fit into viewport of 400 PX - the photo is 48px by 48px fixed size
    //grid.setColumnWidth(photoColumn, 60, Unit.PX);
    //grid.setColumnWidth(tweetBodyColumn, 340, Unit.PX);

//    grid.setColumnWidth(newsPhotoColumn, 110, Unit.PX);
//    grid.setColumnWidth(newsBodyColumn, 290, Unit.PX);

    grid.setColumnWidth(newsPhotoColumn, 108, Unit.PX);
    //grid.setColumnWidth(newsBodyColumn, 288, Unit.PX);

    // Add columns w/o the header
    grid.addColumn(newsPhotoColumn);
    //grid.addColumn(newsBodyColumn);
    
    return grid;
  }
}

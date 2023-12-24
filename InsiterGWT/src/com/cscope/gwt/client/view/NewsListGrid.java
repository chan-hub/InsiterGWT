package com.cscope.gwt.client.view;

import com.cscope.gwt.client.event.NewsSrcClickEvent;
import com.cscope.gwt.client.event.NewsSrcClickEventHandler;
import com.cscope.gwt.client.presenters.AppPresenter;
import com.cscope.gwt.client.view.newscell._NewsCompositeCell;
import com.cscope.gwt.client.view.newscell.NewsCompositeCell;
//import com.cscope.gwt.shared.NewsContents;
import com.google.gwt.dom.client.BrowserEvents;
//import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.CustomScrollPanel;
import com.google.gwt.user.client.ui.HeaderPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.view.client.CellPreviewEvent;
import com.google.web.bindery.event.shared.EventBus;

import insiter.ina.news.service.web.NewsContents;

public class NewsListGrid<T> extends DataGrid<NewsContents> {

  private EventBus eBus;
  private NewsCompositeCell newsCell = null;

  public NewsListGrid(int pageSize, NewsGridResource newsGridResource) {
    super(pageSize, newsGridResource);
  }

  public NewsListGrid(int pageSize, EventBus eb, NewsGridResource newsGridResource) {
    super(pageSize, newsGridResource);
    this.eBus = eb;
    this.bind();
  }

  public void setEventBus(EventBus eb) {
    this.eBus = eb;
  }

  public NewsCompositeCell getNewsCompositeCell() {
    return this.newsCell;
  }
  public void setNewsCompositeCell(NewsCompositeCell newsCell) {
    this.newsCell = newsCell;
  }

//  public NewsCompositeCell getNewsCompositeCell() {
//    return this.newsCell;
//  }
//  public void setNewsCompositeCell(NewsCompositeCell newsCell) {
//    this.newsCell = newsCell;
//  }

  public ScrollPanel getScrollPanel() {
    HeaderPanel header = (HeaderPanel) this.getWidget();
    return (ScrollPanel) header.getContentWidget();
  }

  public void saveScrollBarPosition() {
    final HeaderPanel panel = (HeaderPanel) getWidget();
    final CustomScrollPanel bodyContent = (CustomScrollPanel) panel.getContentWidget();
    AppPresenter.putScrollLocH(bodyContent.getHorizontalScrollPosition());
    AppPresenter.putScrollLocV(bodyContent.getVerticalScrollPosition());
  }

  /**
   * Restore the scroll position saved previously
   */
  public void restoreScrollBarPosition() {
    final HeaderPanel panel = (HeaderPanel) getWidget();
    final CustomScrollPanel bodyContent = (CustomScrollPanel) panel.getContentWidget();

    // Get the latest scroll position saved if any
    //Window.alert("Restoring Scroll Position");
    int positionH = AppPresenter.getScrollLocH();
    int positionV = AppPresenter.getScrollLocV();

    if (positionV > 0) {
      //Window.alert("Restore scrollV=" + positionV);
      bodyContent.setVerticalScrollPosition(positionV);
      // Reset to default location, 0
      AppPresenter.putScrollLocV(0);
    }

    if (positionH > 0) {
      //Window.alert("Restored scrollH=" + positionH);
      bodyContent.setHorizontalScrollPosition(positionH);
      // Reset to default location, 0
      AppPresenter.putScrollLocH(0);
    }
  }

  /**
   * Bind a NewsSrcClick handler to the event bus - to save the scroll position
   */
  private void bind() {
    this.eBus.addHandler(NewsSrcClickEvent.TYPE, new NewsSrcClickEventHandler() {
      @Override
      public void onNewsSrcClick(NewsSrcClickEvent event) {
        // Save the current scroll position prior to jumping to the link location
        HeaderPanel panel = (HeaderPanel) getWidget();
        CustomScrollPanel bodyContent = (CustomScrollPanel) panel.getContentWidget();
        AppPresenter.putScrollLocH(bodyContent.getHorizontalScrollPosition());
        AppPresenter.putScrollLocV(bodyContent.getVerticalScrollPosition());
        
    //Window.alert("Saved scrollH=" + AppPresenter.getScrollLocH());
    //Window.alert("Saved scrollV=" + AppPresenter.getScrollLocV());
        //this.saveScrollBarPosition();
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
}

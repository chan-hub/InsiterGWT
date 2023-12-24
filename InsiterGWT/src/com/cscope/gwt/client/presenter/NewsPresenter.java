package com.cscope.gwt.client.presenter;

//import com.cscope.gwt.shared.NewsClass;
import insiter.ina.news.build.NewsClass;
import insiter.ina.news.service.web.NewsContents;

import com.cscope.gwt.client.InsiterRPCAsync;
import com.cscope.gwt.client.view.NewsListView;
//import com.cscope.gwt.shared.NewsContents;
import com.cscope.gwt.shared.NewsList2Show;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.IncompatibleRemoteServiceException;
import com.google.gwt.user.client.rpc.InvocationException;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SimpleLayoutPanel;
import com.google.web.bindery.event.shared.EventBus;

import java.util.List;

public class NewsPresenter implements Presenter {  

  private List<NewsContents> newsList;

  /**
   * This is what the NewsListView will support
   */
  public interface Display {
//    HasClickHandlers getAddButton();
//    HasClickHandlers getDeleteButton();
//    HasClickHandlers getList();
    void setData(List<NewsContents> newsList);
//    int getClickedRow(ClickEvent event);
//    List<Integer> getSelectedRows();
//    Widget asWidget();
    SimpleLayoutPanel getPanel();
    ScrollPanel getScrollPanel();
  }
  
  private final InsiterRPCAsync rpcService;
  private final EventBus eventBus;
  private final Display display;

  // NewsClass of news to be displayed
  private NewsClass newsClass;
  
  public NewsPresenter(InsiterRPCAsync rpcService, EventBus eventBus2,
                       NewsListView newsListView, NewsClass nClass) {
    this.rpcService = rpcService;
    this.eventBus = eventBus2;
    this.display = newsListView;
    this.newsClass = nClass;
  }

  public void bind() {
/*
    display.getAddButton().addClickHandler(new ClickHandler() {   
      public void onClick(ClickEvent event) {
        eventBus.fireEvent(new ChangeNewsEvent());
      }
    });

    display.getDeleteButton().addClickHandler(new ClickHandler() {   
      public void onClick(ClickEvent event) {
        deleteSelectedContacts();
      }
    });
*/
//    display.getList().addClickHandler(new ClickHandler() {
//      public void onClick(ClickEvent event) {
//        int selectedRow = display.getClickedRow(event);
//        
//        if (selectedRow >= 0) {
//          //String id = tweetList.get(selectedRow).getId();
//          //eventBus.fireEvent(new ShowTweetEvent(id));
//        }
//      }
//    });
  }

  /**
   * Add the NewsView to the container (RootPanel) then fetch News to display
   */
  public void go(final HasWidgets container) {
    bind();
    container.clear();
    container.add(display.getPanel());
    // Get the News to show
    this.fetchNews();
  }

  public void setNewsList(List<NewsContents> newsList) {
    this.newsList = newsList;
  }
  
  public NewsContents getNews(int index) {
    return newsList.get(index);
  }

  static int defMaxCnt = 150;
  //static int defMaxCnt = 100;
  static int[] maxCounts = { 100, 200, 300 };

  static int defNumHours = 24;
  enum TimeUnit {
    hour, day, week
  }

  enum TimeWindow {
    Yesterday, Today, ThisWeek, ThisMonth
  }

  static String defOrder = SeqOrder.Trend.toString();
  enum SeqOrder {
    Trend, Time
  }

  // Predefined windows
  static TimeWindow currentWinow = TimeWindow.Today;      // Default Window is today
  static TimeWindow yesterWindow = TimeWindow.Yesterday;

  /**
   * Fetching desired News list specified in the 4 args:
   * 1. Max number of News
   * 2. Max time period
   * 3. Sorting order
   * 4. News class
   * 
   * When the News class specifies a certain class, the time period
   * is extended to 120 hours to cover 5 days period. This is a temporary
   * as the NumHours will be set by the top line menu to be implemented
   */
  private void fetchNews() {
    // Extend the max time period if the tweetClass specifies non-default
    int maxNumHours = defNumHours;
    String listOrder = defOrder;  // "Trend"
    if (!newsClass.isDefault()) {
      //listOrder = "Time";  // "Time"
      maxNumHours = 5 * 24;  // 5 days in hour
    }
    // Making RPC call with Async callback method
    if (Window.Location.getParameter("deepdebug") != null) 
	Window.alert("NewsPresenter ("+this+") - fetchNews - Making REQUEST: " + defMaxCnt +
			     "," + maxNumHours + "," + defOrder + "," + newsClass );
    // Make RPC to get NewsList
    rpcService.getNewsList(defMaxCnt, maxNumHours, listOrder, newsClass,
                                       new AsyncCallback<NewsList2Show>() {
      public void onSuccess(NewsList2Show result) {
        // Log if necessary
        if (Window.Location.getParameter("deepdebug") != null) 
    	  Window.alert("NewsPresenter - fetchNews -got RESPONSE: " +  (result == null ? " NULL " : " len: "+result.size() ));
        if (result == null)
          GWT.log("Received News: NONE ");
    	else
          GWT.log("Received News: " + result.size());
    	// Extract list of News2Show
        List<NewsContents> newsList = result.getNewsList();
        System.out.println("NewsPresenter.fetchNews():received:" + newsList.size() + " News");
        display.setData(newsList);
      }
      
      public void onFailure(Throwable caught) {
        // Find out which exception was thrown
        try {
          throw caught;
        } catch (IncompatibleRemoteServiceException e) {
          Window.alert("RPC failed:IncompatibleRemoteService:" + e.getMessage());
          // this client is not compatible with the server; cleanup and refresh the browser
        } catch (InvocationException e) {
          Window.alert("RPC failed:InvocationException:" + e.getMessage());
          // the call didn't complete cleanly
        } catch (Throwable e) {
          // last resort -- a very unexpected exception
          Window.alert("RPC failed:Unexpected exception:" + e.getMessage());
        }
      }
    });
  }
}

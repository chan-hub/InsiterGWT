package com.cscope.gwt.client;

import insiter.ina.news.build.NewsClass;
import com.cscope.gwt.client.event.ChangeNewsEvent;
import com.cscope.gwt.client.event.ChangeNewsEventHandler;
import com.cscope.gwt.client.presenter.*;
import com.cscope.gwt.client.view.*;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.ui.HasWidgets;

public class AppController implements Presenter, ValueChangeHandler<String> {
  private final EventBus eventBus;
  private final InsiterRPCAsync rpcService; 
  private HasWidgets container;
  
  public AppController(InsiterRPCAsync rpcService, EventBus eventBus) {
    this.eventBus = eventBus;
    this.rpcService = rpcService;
    bind();
  }

  private void bind() {
    History.addValueChangeHandler(this);
    this.eventBus.addHandler(ChangeNewsEvent.TYPE,
        new ChangeNewsEventHandler() {
          public void onChangeNews(ChangeNewsEvent event) {
            doChangeNews(event.getNewsClass());
          }
        });  
  }

  private void doChangeNews(NewsClass tClass) {
    boolean issueEvent = false;
    History.newItem("list", issueEvent);
    Presenter presenter = new NewsPresenter(rpcService, eventBus, new NewsListView(eventBus), tClass);
    presenter.go(container);
  }

  /**
   * We have an event to go for the container.
   * Either push "list" OR fire the current state
   */
  public void go(final HasWidgets container) {
    this.container = container;

    if ("".equals(History.getToken())) {
      History.newItem("list");
    }
    else {
      History.fireCurrentHistoryState();
    }
  }

  // Set Default Query Class
  static public final NewsClass defQueryClass = NewsClass.getDefQueryClass();

  public void onValueChange(ValueChangeEvent<String> event) {
    String token = event.getValue();
    if (token != null) {
      Presenter presenter = null;
      if (token.equals("list")) {
        presenter = new NewsPresenter(rpcService, eventBus, new NewsListView(eventBus), defQueryClass);
      }
      if (presenter != null) {
        presenter.go(container);
      }
    }
  } 
}

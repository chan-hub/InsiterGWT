package com.cscope.gwt.client.models;

//import com.cscope.gwt.shared.NewsClass;
import insiter.ina.news.build.NewsClass;

import com.cscope.gwt.client.InsiterRPCAsync;
import com.cscope.gwt.shared.NewsList2Show;
import com.google.gwt.user.client.rpc.AsyncCallback;
//
public interface AppModel extends CategoryMenuModel {
  public void getNewsList(NewsClass newsClass, AsyncCallback<NewsList2Show> callback);
  public InsiterRPCAsync getRPCService();
}

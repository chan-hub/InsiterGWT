package com.cscope.gwt.client;

import insiter.ina.news.build.NewsClass;
//import com.cscope.gwt.shared.NewsClass;

import com.cscope.gwt.shared.News2ShowExt;
//import com.cscope.gwt.shared.NewsMenu;
import com.cscope.gwt.shared.NewsList2Show;
import com.google.gwt.user.client.rpc.AsyncCallback;

import insiter.ina.news.service.web.NewsMenu;

public interface InsiterRPCAsync {
  public void getPageTagLine(AsyncCallback<String> callback);
  public void getNewsMenu(int maxCnt, int numHours, String seqOrder, String classStr,
                                              AsyncCallback<NewsMenu> callback);
  public void getNews(String id, String seqOrder, AsyncCallback<News2ShowExt> callback);
  public void putNews(String id, NewsClass[] tClasses, String rtCat, String adminId, AsyncCallback<String> callback);
  public void getNewsList(int maxCnt, int numHours, String seqOrder, NewsClass tClass,
                                              AsyncCallback<NewsList2Show> callback);
  // User input
  public void putFeedback(String feedback, AsyncCallback<String> callback);

  // Admin stuffs
  public void adminLogin(String id, String passwd, AsyncCallback<String> callback);
  public void adminLogout(String id, AsyncCallback<String> callback);
  public void addAdmin(String id, String passwd, String emailAddr, AsyncCallback<String> callback);
  public void rmAdmin(String id, AsyncCallback<String> callback);
}

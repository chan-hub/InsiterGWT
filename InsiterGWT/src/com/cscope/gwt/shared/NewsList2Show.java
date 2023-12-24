package com.cscope.gwt.shared;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import insiter.ina.news.build.NewsClass;
import insiter.ina.news.service.web.NewsContents;

import com.google.gwt.user.client.rpc.IsSerializable;

public class NewsList2Show implements IsSerializable, Serializable {
  static private final long serialVersionUID = 100L;

  public long timeCreate;
  NewsClass queryClass;
  List<NewsContents> newsList = null;
  //JSONArray newsListJSON = null;  // List of NewsJSON list (to support Cluster)

  static public NewsList2Show EmptyListInstance = new NewsList2Show(new ArrayList<NewsContents>());

  public NewsList2Show() {}

  public NewsList2Show(ArrayList<NewsContents> newsList) {
    this.newsList = newsList;
  }

  public void setTimeCreate() {
    this.timeCreate = System.currentTimeMillis();
  }

  public boolean isFilledUp() {
    return this.newsList != null;
  }

  public int size() {
    return this.newsList.size();
  }

  public NewsClass getQueryClass() {
    return this.queryClass;
  }

  public void setQueryClass(NewsClass queryClass) {
    this.queryClass = queryClass;
  }

  /**
   * This method will take JSON structure received from the host, then extract
   * the JSON to instance a list of NewsContents to be retrieved
   * 
   * @return
   */
  public List<NewsContents> getNewsListNew() {
	    return this.newsList;
  }

  public List<NewsContents> getNewsList() {
    return this.newsList;
  }

  public void setNewsList(List<NewsContents> newsList) {
    this.newsList = newsList;
  }
}

package com.cscope.gwt.client;

import insiter.ina.news.build.NewsClass;
import insiter.ina.news.service.web.NewsMenu;

import com.cscope.gwt.shared.News2ShowExt;
import com.cscope.gwt.shared.NewsList2Show;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * The client-side stub for the RPC service.
 */
@RemoteServiceRelativePath("InsiterService")
//@RemoteServiceRelativePath("TektweetService")
public interface InsiterRPC extends RemoteService {
  public String getPageTagLine();
  public NewsMenu getNewsMenu(int maxCnt, int numHours, String seqOrder, String classStr);
  public News2ShowExt getNews(String id, String seqOrder);
  public String putNews(String id, NewsClass[] tClasses, String rtCat, String adminId);
  public NewsList2Show getNewsList(int maxCnt, int numHours, String seqOrder, NewsClass tClass);

  // User input
  public String putFeedback(String feedback);

  // Admin stuffs
  public String adminLogin(String id, String passwd);
  public String adminLogout(String id);
  public String addAdmin(String id, String passwd, String emailAddr);
  public String rmAdmin(String id);
}
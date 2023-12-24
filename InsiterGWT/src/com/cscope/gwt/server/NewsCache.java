package com.cscope.gwt.server;

import java.util.HashMap;
import java.util.Map;

//import com.cscope.gwt.shared.NewsMenu;
import com.cscope.gwt.shared.NewsList2Show;

import insiter.ina.news.service.web.NewsMenu;

public class NewsCache {

  static final long maxMenuCacheTimeMsec  = 10 * 60 * 1000;   // Menu  is valid for 10 minutes
  static final long maxCacheValidTimeMsec = 10 * 60 * 1000;   // Cache is valid for 10 minutes
 
  static private Map<String, Object> newsListAndMenuTable = new HashMap<String, Object>();

  static void putNewsMenu(String key, NewsMenu menu) {
    menu.setTimeCreate();
    newsListAndMenuTable.put(key, menu);
    System.out.println("=>NewsCache.putNewsMenu():Cached a NewsMenu with key=" + key);
  }

  static NewsMenu getNewsMenuCache(String key) {
    Object menuObj = newsListAndMenuTable.get(key);
    if (menuObj != null) {
      if (menuObj.getClass().getSimpleName().equals("NewsMenu")) {
        return(NewsMenu)menuObj;
      }
    }
    return null;
  }

  /**
   * Called by InsiterRPCImpl.getNewsMenu(), either take cached NewsMenu or
   * null, so that RPCImpl make a call to get new NewsMenu contents
   * @param key
   * @return
   */
  static NewsMenu getNewsMenu(String key) {
    Object menuObj = NewsCache.newsListAndMenuTable.get(key);
    if (menuObj != null) {
      if (menuObj instanceof NewsMenu) {
        NewsMenu menuCache = (NewsMenu)menuObj;
        if ((System.currentTimeMillis() - menuCache.timeCreate) < maxMenuCacheTimeMsec) {
          System.out.println("NewsCache.getNewsMenu():Found NewsMenu cache with key=" + key);
          return menuCache;
        } else {
          //newsTable.remove(key);
          System.out.println("NewsCache.getNewsMenu():Found expired NewsMenu with key=" + key);
        }
      }
    } else {
      System.out.println("NewsCache.getNewsMenu():No NewsMenu cache with key=" + key);
    }
    return null;
  }

  static void putNewsList(String key, NewsList2Show newsList) {
    newsList.setTimeCreate();
    NewsCache.newsListAndMenuTable.put(key, newsList);
    System.out.println("=>NewsCache.putNewsList():Cached a NewsList with key=" + key);
  }

  static NewsList2Show getNewsListCache(String key) {
    Object listObj = NewsCache.newsListAndMenuTable.get(key);
    if (listObj != null) {
      if (listObj instanceof NewsList2Show) {
        return (NewsList2Show)listObj;
      }
    }
    return null;
  }

  static NewsList2Show getNewsList(String key) {
    Object listObj = NewsCache.newsListAndMenuTable.get(key);
    if (listObj != null) {
      if (listObj instanceof NewsList2Show) {
        NewsList2Show listCache = (NewsList2Show)listObj;
        if ((System.currentTimeMillis() - listCache.timeCreate) < maxCacheValidTimeMsec) {
          System.out.println("NewsCache.getNewsList():Found NewsList cache=" + key);
          return listCache;
        } else {
          //newsTable.remove(key);
          System.out.println("NewsCache.getNewsList():Found expired NewsList with key=" + key);
        }
      }
    } else {
      System.out.println("NewsCache.getNewsList():No NewsList cache for key=" + key);
    }
    return null;
  }
}

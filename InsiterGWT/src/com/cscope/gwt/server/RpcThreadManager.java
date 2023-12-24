package com.cscope.gwt.server;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import insiter.ina.news.service.web.NewsMenu;
import insiter.ina.util.Util;
//import com.cscope.gwt.shared.NewsMenu;
import com.cscope.gwt.shared.NewsList2Show;

public class RpcThreadManager {
  static long maxGetMenuTime = 24 * 1000;
  static long maxGetListTime = 24 * 1000;
//  static long maxGetMenuTime = 16 * 1000;
//  static long maxGetListTime = 16 * 1000;
  static Map<String, Set<Long>> waitThreadTable = new HashMap<String, Set<Long>>();

  static public void wait2GetNewsMenu(String key, NewsMenu newsMenu) {
    // Get the set of Thread Ids waiting for this NewsMenu
    Set<Long> threadIds = waitThreadTable.get(key);
    if (threadIds == null) {
      threadIds = new HashSet<Long>();
      waitThreadTable.put(key, threadIds);
    }
    // Add this thread's Id to the IdSet
    Long id = Long.valueOf(Thread.currentThread().getId());
    threadIds.add(id);
    // Record the time
    long timeTaken = System.currentTimeMillis();

    // Synchronized check & wait on this NewsMenu object
    if (newsMenu.isFilledUp()) {
      System.out.println("RpcThreadManager.wait2GetNewsMenu():Thread:" + id + " takes NewsMenu " +
                         "just responded at:" + Util.getCurrentTime());
    } else {
      synchronized (newsMenu) {
        try {
          newsMenu.wait(maxGetMenuTime);
        } catch (Exception e) {
          System.out.println("RpcThreadManager.wait2GetNewsMenu():exception:" + e.getMessage());
        }
      }
    }
    // Calculate timeTaken
    timeTaken = System.currentTimeMillis() - timeTaken;

    // Now we get the NewsMenu or timed out
    if (newsMenu.isFilledUp()) {
      System.out.println("RpcThreadManager.wait2GetNewsMenu():Thread:" + id + " takes " + timeTaken +
                         " msec to get new NewsMenu at:" + Util.getCurrentTime());
    } else {
      System.out.println("RpcThreadManager.wait2GetNewsMenu():Thread:" + id + " takes " + timeTaken +
                         " msec to get timed out at:" + Util.getCurrentTime());
    }
    // Remove its id to delete the Id Set && ReqRec when the set is empty
    threadIds.remove(id);
    if (threadIds.isEmpty()) {
      waitThreadTable.remove(key);
      JmsReqHandler.delReqNewsList(key);
      System.out.println("RpcThreadManager.wait2GetNewsMenu():Removed threadId set && reqRecord");
    }
  }

  static public void wait2GetNewsList(String key, NewsList2Show list) {
    // Get the set of Thread Ids waiting for this NewsList
    Set<Long> threadIds = waitThreadTable.get(key);
    if (threadIds == null) {
      threadIds = new HashSet<Long>();
      waitThreadTable.put(key, threadIds);
    }
    // Add this thread's Id to the IdSet
    Long id = Long.valueOf(Thread.currentThread().getId());
    threadIds.add(id);
    // Record the time
    long timeTaken = System.currentTimeMillis();

    // Synchronized check & wait on this NewsList object
    synchronized (list) {
      // No need to wait if the NewsList has been filled up
      if (list.isFilledUp()) {
        System.out.println("RpcThreadManager.wait2GetNewsList():Thread:" + id + " takes NewsList " +
                           "just responded at:" + Util.getCurrentTime());
      }
      else {
        try {
          System.out.println("RpcThreadManager.wait2GetNewsList():Thread:" + id +
                             " wait at:" + Util.getCurrentTime());
          list.wait(maxGetListTime);  // Must wait 'til respond OR timeout
        } catch (Exception e) {
          System.out.println("RpcThreadManager.wait2GetNewsList():exception:" + e.getMessage());
        }
      }
    } // synchronized (list)

    // Calculate timeTaken
    timeTaken = System.currentTimeMillis() - timeTaken;
    // Now we get the NewsList or timed out
    if (list.isFilledUp()) {
      System.out.println("RpcThreadManager.wait2GetNewsList():Thread:" + id + " takes " + timeTaken +
                         " msec to get new NewsList at:" + Util.getCurrentTime());
    } else {
      System.out.println("RpcThreadManager.wait2GetNewsList():Thread:" + id + " takes " + timeTaken +
                         " msec to get timed out at:" + Util.getCurrentTime());
    }
    // Remove its id to delete the ReqRec when the set is empty
    threadIds.remove(id);
    if (threadIds.isEmpty()) {
      waitThreadTable.remove(key);
      JmsReqHandler.delReqNewsList(key);
      System.out.println("RpcThreadManager.wait2GetNewsList():Removed threadId Set && reqRecord");
    }
  }
}

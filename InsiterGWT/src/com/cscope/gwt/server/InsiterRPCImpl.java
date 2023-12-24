package com.cscope.gwt.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import org.apache.activemq.transport.stomp.StompConnection;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import com.cscope.gwt.client.InsiterRPC;
import com.cscope.gwt.shared.News2ShowExt;
import com.cscope.gwt.shared.NewsList2Show;

import com.google.gson.Gson;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import insiter.ina.news.build.NewsClass;
//import com.cscope.gwt.shared.NewsClass;
import insiter.ina.news.service.web.NewsMenu;
import insiter.ina.util.Util;

public class InsiterRPCImpl extends RemoteServiceServlet implements InsiterRPC {
  static final long serialVersionUID = 100L;

  static {
    // Initialize the JMS handlers
    JmsReqHandler.init();
    JmsRespHandler.init();
    // Start TomcatClient to receive NewsList object
    TomcatClient.startTomcatClient();
  }

  // Commands to cscope NewsServer via JMS
  public enum JmsReqCmd {
    GetMenu, GetNews, PutNews, GetNewsList,
    PutFeedback,
    AdminLogin, AdminLogout, AddAdmin, RmAdmin;

    static public JmsReqCmd getCmd(String type) {
      JmsReqCmd cmd = null;
      try {
        cmd = JmsReqCmd.valueOf(type);
      } catch (Exception e) {
        
      }
      return cmd;
    }
  }

  static String pageTagLine = "Top Technology News";
  static Properties props = null;

  /**
   * Make a NewsList request JSON cmd to send over the Stomp connection
   * @param cmd
   * @param args[0]:tweetId, tweetClass
   * @param args[1]
   * @param args[2]
   * @return
   */
  static JSONObject MakeCmdJSON(JmsReqCmd cmd, String ... args) {
    JSONObject jsonCmd = new JSONObject();
    // Put the command first
    jsonCmd.put("cmd", cmd+"");

    switch (cmd) {
      case GetMenu:
        // Nothing else necessary
        break;

      case GetNews:
        // first arg is the Tweet Id
        jsonCmd.put("tweetId", args[0]);
        jsonCmd.put("seqOrder", args[1]);
        break;

      case PutNews:
        jsonCmd.put("tweetId", args[0]);
        jsonCmd.put("tweetClasses", '[' + args[1] + ']');
        jsonCmd.put("rtCat", args[2]);
        jsonCmd.put("adminId", args[3]);
        break;

      case GetNewsList:
        jsonCmd.put("maxTweetCnt", args[0]);
        jsonCmd.put("hourPeriod", args[1]);
        jsonCmd.put("seqOrder", args[2]);
        jsonCmd.put("tweetClass", args[3]);
        break;

      case PutFeedback:
        jsonCmd.put("feedback", args[0]);
        break;

      case AdminLogin:
        jsonCmd.put("id", args[0]);
        jsonCmd.put("passwd", args[1]);
        break;

      case AdminLogout:
        jsonCmd.put("id", args[0]);
        //jsonCmd.put("passwd", args[1]);
        break;

      case AddAdmin:
        jsonCmd.put("id", args[0]);
        jsonCmd.put("passwd", args[1]);
        jsonCmd.put("emailAddr", args[2]);

      case RmAdmin:
        jsonCmd.put("id", args[0]);
        break;

      default:
        System.err.println("InsiterRPCImpl.makeJsonCmd():invalid cmd:" + cmd);
        break;
    }
    return jsonCmd;
  }

  /**
   * The TagLine of Home Page defined in the property file
   */
  public String getPageTagLine() {
//    String title = "";
//    if (InsiterRPCImpl.props != null) {
//      title = props.getProperty("pageTitle");
//      System.out.println("==>Send back title:" + title);
//    }
    return InsiterRPCImpl.pageTagLine;
  }

  // To keep the id of the latest TweetData2Annotate request
  static String latestId = "";

//  static private boolean isServerBusy = false;
//  static private final long updatePeriod = 10000;   // 10 sec * 1000 msec/sec
  static private final long timeLimitMsec = 16 * 1000;    // 16 secs

  // Boolean flags to control responding to RPC requests
  static boolean newsReady = false;
  static boolean newsListReady = false;
  static boolean newsMenuReady = false;

  /**
   * RPC method implementation
   */
  /**
   * GetNews is to fetch extended News info including the media URL
   * and other related News. This info will be displayed on full screen
   * with optional editing window if it's requested by an admin client
   */
  static private News2ShowExt news2ShowExt = null;
  public News2ShowExt getNews(String id, String seqOrder) {
    // Reset resp string
    InsiterRPCImpl.news2ShowExt = null;
    // Build JSON command
    JSONObject cmdJson = InsiterRPCImpl.MakeCmdJSON(JmsReqCmd.GetNews, id, seqOrder);
    // Send out JSON command
    //CsStomp.sendMsg(hotweet2cscope, CsStomp.hotweet2cscopeQ, cmdJson.toString());
    //System.out.println("InsiterRPCImpl.getTweet():sent GetTweet request to cScope:" + url);

    // Now wait 'til we have a Tweet2ShowExt delivered OR the timeLimit is reached
    long waitCnt = 0;
    // Get the current time
    long timeNow = System.currentTimeMillis();

    // Busy wait 'til get the respond OR time out
    while (InsiterRPCImpl.news2ShowExt == null) {
      if ((++waitCnt % 50000000L) == 0L) {
        long timeSpent = System.currentTimeMillis() - timeNow;
        System.out.println("InsiterRPCImpl.getNews():waiting -> timeSpent=" + (timeSpent/1000) + "sec");
        if (timeSpent >= timeLimitMsec) {
          System.out.println("InsiterRPCImpl.getNews():time out reached");
          break;
        }
      }
    }
    return InsiterRPCImpl.news2ShowExt;
  }

  /**
   * PutTweet
   */
  static private String putNewsResp;
  public String putNews(String id, NewsClass[] tClasses, String rtCat, String adminId) {
    // Reset resp string
    InsiterRPCImpl.putNewsResp = null;

    // TweetClass str
    String classStr = "";
    if (tClasses != null && tClasses.length > 0) {
      StringBuilder strBuilder = new StringBuilder();
      for (NewsClass tClass : tClasses) {
        strBuilder.append(tClass.toStrPair());
        strBuilder.append(',');
      }
      strBuilder.deleteCharAt(strBuilder.length() - 1);
      classStr = strBuilder.toString();
    }

    // RtCat
    if (rtCat==null) rtCat = "";
 
    // Build JSON command
    JSONObject cmdJson = InsiterRPCImpl.MakeCmdJSON(JmsReqCmd.PutNews, id, classStr, rtCat, adminId);

    // Send out JSON command
    //CsStomp.sendMsg(hotweet2cscope, CsStomp.hotweet2cscopeQ, cmdJson.toString());
    //System.out.println("InsiterRPCImpl.putTweetClass():sent PutTweetClass request to cScope:" + url);

    // Now wait 'til we have the TweetMenu delivered OR the timeLimit is reached
    long waitCnt = 0;
    // Get the current time
    long timeNow = System.currentTimeMillis();

    // Busy wait 'til get the respond OR time out
    while (InsiterRPCImpl.putNewsResp == null) {
      if ((++waitCnt % 50000000L) == 0L) {
        long timeSpent = System.currentTimeMillis() - timeNow;
        System.out.println("InsiterRPCImpl.putNews():waiting -> timeSpent=" + (timeSpent/1000) + "sec");
        if (timeSpent >= timeLimitMsec) {
          System.out.println("InsiterRPCImpl.putNews():time out reached");
          break;
        }
      }
    }
    return InsiterRPCImpl.putNewsResp;
  }

  /**
   * GetMenu
   */
  public NewsMenu getNewsMenu(int maxCnt, int hourPeriod, String seqOrder, String classStr) {
    System.out.println("\n=>InsiterNewsRPCImpl.getNewsMenu():received a NewsMenu request " + Util.getCurrentTime());
    
    // Get the Menu cache if available
    String key = JmsReqHandler.getKey4NewsMenu(maxCnt, hourPeriod, seqOrder, classStr);
    NewsMenu menu = NewsCache.getNewsMenu(key);
    if (menu != null) {
      System.out.println("==>InsiterNewsRPCImpl.getNewsMenu():returning a cached NewsMenu " + Util.getCurrentTime());
      return menu;
    }
    // Call NewsListsQuery to get a fresh NewsMenu
    menu = NewsListsQuery.getNewsMenu();

    // Return it
    System.out.println("==>InsiterNewsRPCImpl.getNewsMenu():returning a NewsMenu " + Util.getCurrentTime());
    return menu;
  }

  public NewsMenu _getNewsMenu(int maxCnt, int hourPeriod, String seqOrder, String classStr) {
    System.out.println("=>InsiterNewsRPCImpl.getNewsMenu():received a NewsMenu request");
    // Get the Menu cache if available
    String key = JmsReqHandler.getKey4NewsMenu(maxCnt, hourPeriod, seqOrder, classStr);
    NewsMenu menu = NewsCache.getNewsMenu(key);
    if (menu != null) {
      System.out.println("==>InsiterNewsRPCImpl.getNewsMenu():returning a cached NewsMenu");
      return menu;
    }
    // Call JmsReqHandler to get the menu obj, on which we'll wait
    menu = JmsReqHandler.reqNewsMenu(key, maxCnt, hourPeriod, seqOrder, classStr);
    
    // Then wait 'til the menu is responded
    RpcThreadManager.wait2GetNewsMenu(key, menu);

    // Use cached one if no menu was delivered in time
    if (!menu.isFilledUp()) {
      menu = NewsCache.getNewsMenuCache(key);
    }
    // Return it
    System.out.println("==>InsiterNewsRPCImpl.getNewsMenu():returning a NewsMenu");
    return menu;
  }

  static public String getDefNewsMenuKey() {
    // Make sure a Menu - default menu parameters as set in RPCDataModel.java
    int maxCnt = 300;
    int numHours = 24 * 7;
    String seqOrder = "Time";
    String classStr = "All:All";
    String key = JmsReqHandler.getKey4NewsMenu(maxCnt, numHours, seqOrder, classStr);
    return key;
  }

  /**
   * GetNewsList
   */
  public NewsList2Show getNewsList(int maxCnt, int hourPeriod, String seqOrder, NewsClass tClass) {
System.out.println("\nInsiterRPCImpl.getNewsList():Request " + maxCnt + " News of NewsClass:" + tClass.toToken());
    // Make sure Menu exists - default menu parameters as set in RPCDataModel.java
    //int menuMaxCnt = 300; //XXX
    //int numHours = 24 * 7;
    //String menuSeqOrder = "Time";
    //NewsMenu menu = getNewsMenu(menuMaxCnt, numHours, menuSeqOrder, "All:All");

    // Get the List cache if available
    String key = JmsReqHandler.getKey4NewsList(maxCnt, hourPeriod, seqOrder, tClass.toToken());
    NewsList2Show list = NewsCache.getNewsList(key);
    if (list != null) {
      return list;
    }
    // Call NewsListsQuery to get NewsList2Shpw containing the whole list of News
    list = NewsListsQuery.getNewsList2Show(key, maxCnt, hourPeriod, seqOrder, tClass.toToken());

    // Cache it then return it
    NewsCache.putNewsList(key, list);
    return list;
  }

  // DEBUG - For request with a Subject, check if the NewsList of Entity:All is in the cache.
  //         If the NewsList exists, then extract a sublist of the NewsList for the Subject,
  //         rather than making a new JMS request
  
  public NewsList2Show _getNewsList(int maxCnt, int hourPeriod, String seqOrder, NewsClass tClass) {
    
    // Make sure Menu exists - default menu parameters as set in RPCDataModel.java
    int menuMaxCnt = 300; //XXX
    int numHours = 24 * 7;
    String menuSeqOrder = "Time";
    NewsMenu menu = getNewsMenu(menuMaxCnt, numHours, menuSeqOrder, "All:All");

    // Get the List cache if available
    String key = JmsReqHandler.getKey4NewsList(maxCnt, hourPeriod, seqOrder, tClass.toToken());
    NewsList2Show list = NewsCache.getNewsList(key);
    if (list != null) {
      return list;
    }
    // Make a JMS request to get the list obj to wait on
    list = JmsReqHandler.reqNewsList(key, maxCnt, hourPeriod, seqOrder, tClass.toToken());
    
    // Wait 'til the list is responded && filled up
    RpcThreadManager.wait2GetNewsList(key, list);

    // Use expired-cache if no list was delivered in time
    if (!list.isFilledUp()) {
      list = NewsCache.getNewsListCache(key);
    }

// // Test run of new getNewsList() method
// InsiterRPCImpl.getNewsList2(maxCnt, hourPeriod, seqOrder, tClass);

    // Return it
    return list;
  }

  /**
   * PutFeedback
   */
  static private String putFeedbackResp;
  public String putFeedback(String feedback) {
    // Reset resp string
    InsiterRPCImpl.putFeedbackResp = null;
    // Build JSON command
    JSONObject cmdJson = InsiterRPCImpl.MakeCmdJSON(JmsReqCmd.PutFeedback, feedback);
    // Send out JSON command
    //CsStomp.sendMsg(hotweet2cscope, CsStomp.hotweet2cscopeQ, cmdJson.toString());
    //System.out.println("InsiterRPCImpl.putFeedback():sent PutFeedback request to cScope:" + url);

    // Now wait 'til we have the PutFeedback respond delivered OR the timeLimit is reached
    long waitCnt = 0;
    // Get the current time
    long timeNow = System.currentTimeMillis();

    // Busy wait 'til get the respond OR time out
    while (InsiterRPCImpl.putFeedbackResp == null) {
      if ((++waitCnt % 50000000L) == 0L) {
        long timeSpent = System.currentTimeMillis() - timeNow;
        System.out.println("InsiterRPCImpl.putFeedback():waiting -> timeSpent=" + (timeSpent/1000) + "sec");
        if (timeSpent >= timeLimitMsec) {
          System.out.println("InsiterRPCImpl.putFeedback():time out reached");
          break;
        }
      }
    }
    // putFeedbackResp may be empty if no respond found
    return InsiterRPCImpl.putFeedbackResp;
  }

  /**
   * AdminLogin
   */
  static private String adminLoginResp;
  public String adminLogin(String id, String passwd) {
    // Reset resp string
    InsiterRPCImpl.adminLoginResp = null;
 
    // Build a JSON command
    JSONObject cmdJson = InsiterRPCImpl.MakeCmdJSON(JmsReqCmd.AdminLogin, id, passwd);
    // Send out JSON command
    //CsStomp.sendMsg(hotweet2cscope, CsStomp.hotweet2cscopeQ, cmdJson.toString());
    //System.out.println("InsiterRPCImpl.adminLogin():sent AdminLogin request to cScope:" + url);

    // Now wait 'til we have the AdminLogin respond delivered OR the timeLimit is reached
    long waitCnt = 0;
    // Get the current time
    long timeNow = System.currentTimeMillis();

    // Busy wait 'til get the respond OR time out
    while (InsiterRPCImpl.adminLoginResp == null) {
      if ((++waitCnt % 50000000L) == 0L) {
        long timeSpent = System.currentTimeMillis() - timeNow;
        System.out.println("InsiterRPCImpl.adminLogin():waiting -> timeSpent=" + (timeSpent/1000) + "sec");
        if (timeSpent >= timeLimitMsec) {
          System.out.println("InsiterRPCImpl.adminLogin():time out reached");
          break;
        }
      }
    }
    // adminLoginResp may be empty if no respond found
    return InsiterRPCImpl.adminLoginResp;
  }

  /**
   * AdminLogout
   */
  static private String adminLogoutResp;
  public String adminLogout(String id) {
    // Reset resp string
    InsiterRPCImpl.adminLogoutResp = null;
 
    // Build a JSON command
    JSONObject cmdJson = InsiterRPCImpl.MakeCmdJSON(JmsReqCmd.AdminLogout, id);
    // Send out JSON command
    //CsStomp.sendMsg(hotweet2cscope, CsStomp.hotweet2cscopeQ, cmdJson.toString());
    //System.out.println("InsiterRPCImpl.adminLogout():sent AdminLogout request to cScope:" + url);

    // Now wait 'til we have the AdminLogout respond delivered OR the timeLimit is reached
    long waitCnt = 0;
    // Get the current time
    long timeNow = System.currentTimeMillis();

    // Busy wait 'til get the respond OR time out
    while (InsiterRPCImpl.adminLogoutResp == null) {
      if ((++waitCnt % 50000000L) == 0L) {
        long timeSpent = System.currentTimeMillis() - timeNow;
        System.out.println("InsiterRPCImpl.adminLogout():waiting -> timeSpent=" + (timeSpent/1000) + "sec");
        if (timeSpent >= timeLimitMsec) {
          System.out.println("InsiterRPCImpl.adminLogout():time out reached");
          break;
        }
      }
    }
    // adminLogoutResp may be empty if no respond found
    return InsiterRPCImpl.adminLogoutResp;
  }

  /**
   * AddAdmin
   */
  static private String addAdminResp;
  public String addAdmin(String id, String passwd, String emailAddr) {
    // Reset resp string
    InsiterRPCImpl.addAdminResp = null;
 
    // Build a JSON command
    JSONObject cmdJson = InsiterRPCImpl.MakeCmdJSON(JmsReqCmd.AddAdmin, id, passwd, emailAddr);
    // Send out JSON command
    //CsStomp.sendMsg(hotweet2cscope, CsStomp.hotweet2cscopeQ, cmdJson.toString());
    //System.out.println("InsiterRPCImpl.addAdmin():sent AddAdmin request to cScope:" + url);

    // Now wait 'til we have the AddAdmin respond delivered OR the timeLimit is reached
    long waitCnt = 0;
    // Get the current time
    long timeNow = System.currentTimeMillis();

    // Busy wait 'til get the respond OR time out
    while (InsiterRPCImpl.addAdminResp == null) {
      if ((++waitCnt % 50000000L) == 0L) {
        long timeSpent = System.currentTimeMillis() - timeNow;
        System.out.println("InsiterRPCImpl.addAdmin():waiting -> timeSpent=" + (timeSpent/1000) + "sec");
        if (timeSpent >= timeLimitMsec) {
          System.out.println("InsiterRPCImpl.addAdmin():time out reached");
          break;
        }
      }
    }
    // addAdminResp may be empty if no respond received
    return InsiterRPCImpl.addAdminResp;
  }

  /**
   * RmAdmin
   */
  static private String rmAdminResp;
  public String rmAdmin(String id) {
    // Reset resp string
    InsiterRPCImpl.rmAdminResp = null;
 
    // Build a JSON command
    JSONObject cmdJson = InsiterRPCImpl.MakeCmdJSON(JmsReqCmd.RmAdmin, id);
    // Send out JSON command
    //CsStomp.sendMsg(hotweet2cscope, CsStomp.hotweet2cscopeQ, cmdJson.toString());
    //System.out.println("InsiterRPCImpl.rmAdmin():sent RmAdmin request to cScope:" + url);

    // Now wait 'til we have the RmAdmin respond delivered OR the timeLimit is reached
    long waitCnt = 0;
    // Get the current time
    long timeNow = System.currentTimeMillis();

    // Busy wait 'til get the respond OR time out
    while (InsiterRPCImpl.rmAdminResp == null) {
      if ((++waitCnt % 50000000L) == 0L) {
        long timeSpent = System.currentTimeMillis() - timeNow;
        System.out.println("InsiterRPCImpl.rmAdmin():waiting -> timeSpent=" + (timeSpent/1000) + "sec");
        if (timeSpent >= timeLimitMsec) {
          System.out.println("InsiterRPCImpl.rmAdmin():time out reached");
          break;
        }
      }
    }
    // rmAdminResp may be empty if no respond received
    return InsiterRPCImpl.rmAdminResp;
  }
}
package com.cscope.gwt.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.activemq.transport.stomp.StompConnection;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import com.cscope.gwt.client.InsiterRPC;
import com.cscope.gwt.shared.News2ShowExt;
import com.cscope.gwt.shared.News2ShowExt.RtCategory;

import com.cscope.gwt.shared.NewsList2Show;
import com.google.gson.Gson;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import insiter.ina.news.build.NewsClass;
import insiter.ina.news.service.web.NewsMenu;
import insiter.ina.util.Util;

public class JmsReqHandler {
  static final long serialVersionUID = 100L;

  // Commands to cscope server
  public enum JmsCmd {
    GetMenu, GetTweet, PutTweet, GetTweetList,
    PutFeedback,
    AdminLogin, AdminLogout, AddAdmin, RmAdmin;

    static public JmsCmd getCmd(String type) {
      JmsCmd cmd = null;
      try {
        cmd = JmsCmd.valueOf(type);
      } catch (Exception e) {
        
      }
      return cmd;
    }
  }

  // Two StompConnections for sending & receiving Stomp msgs
  static protected StompConnection cscope2hotweet;
  static protected StompConnection tomcat2cscope;

  // Set up Stomp connection and start TweetData receiving thread
  static String url = null;
  static String pageTagLine = "Top Technology News";

  /**
   * Set up JMS channels for JmsReqHandler && JmsRecvHandler
   */
  static void init() {
    // Either local or remote Stomp connection to transport commands & responds
    //url = "10.0.0.41";
    //url = "107.209.155.138";
    url = "68.121.160.161";
    int stompPort = 61613;
    System.out.println("\n=== JmsReqHandler:start init() ===");
/*
 * Comment this out to do development mode testing from laptop
 */
    // Get the URL from Property
    String urlProp = Property.getServerUrl();
    if (urlProp != null) {
      url = urlProp;
    }
    // Get the home page title too, to respond upon client request
    String title = Property.getPageTitle();
    if (title != null) {
      pageTagLine = title;
    }
/*
 */
    // Open up a Stomp channel for sending commands
    boolean isReceiver = false;
    tomcat2cscope = openTomcat2CScope(url, stompPort,  CsStomp.tomcat2cscopeQ, isReceiver);
    if (tomcat2cscope != null) {
      System.out.println("JmsReqHandler:Built StompConnection sending to:" + url);
    } else {
      System.err.println("! JmsReqHandler:FAIL to build StompConnection to send");
    }

    // Open up a Stomp channel for receiving responds
    isReceiver = true;
    cscope2hotweet = openTomcat2CScope(url, stompPort, CsStomp.cscope2tomcatQ, isReceiver);
    if (cscope2hotweet != null) {
      System.out.println("JmsReqHandler:Built StompConnection receiving from:" + url);
    } else {
      System.err.println("! JmsReqHandler:FAIL to build StompConnection to receive");
    }
  }

  // Calendar support
  final static String profileFormStr = "MMM dd HH:mm:ss";
  final static SimpleDateFormat profileFormat  = new SimpleDateFormat(profileFormStr);
  static public String getCurrentTime() {
    Date dateNow = new Date(System.currentTimeMillis());
    return profileFormat.format(dateNow);
  }
  static public String getTimeStrOfMsec(long timeMsec) {
    Date dateNow = new Date(timeMsec);
    return profileFormat.format(dateNow);
  }

  /**
   * Open a Stomp connection to cScope agent
   * @param url
   * @param port
   * @param queueName
   * @param receiver
   * @return
   */
  static StompConnection openTomcat2CScope(String url, int port,
                                           String queueName, boolean receiver) {
    String usrName = "system";  // default user name
    String passwd = "manager";  // default passwd
    StompConnection c = CsStomp.openConnection(receiver, url, port, usrName, passwd, queueName);
    return c;
  }

  /**
   * reqRecTable keeps the currently outstanding JMS requests.
   * When a request is outstanding, the table provides the final res object
   * to be filled and to be delivered. Threads will wait on this object
   * instead of issuing redundant JMS requests, to be notified with nofifyAll()
   */
  static Map<String, Object> reqRecTable = new HashMap<String, Object>();

  static public String reqMenu = "ReqMenu";
  static void putReqNewsMenu(String key, NewsMenu menu) {
    reqRecTable.put(key, menu);
  }
  static void delReqNewsMenu(String key) {
    reqRecTable.remove(key);
  }
  static NewsMenu getReqNewsMenu(String key) {
    Object menuObj = null;
    //synchronized(reqRecTable) {
      menuObj = reqRecTable.get(key);
    //}
    if (menuObj != null) {
      String className = menuObj.getClass().getSimpleName();
      if ("NewsMenu".equals(className)) {
        return (NewsMenu) menuObj;
      } else {
        System.out.println("!!! JmsReqHandler.getReqNewsMenu():invalid className:" + className);
      }
    }
    return null;
  }

  static void putReqNewsList(String key, NewsList2Show list) {
    reqRecTable.put(key, list);
  }
  static void delReqNewsList(String key) {
    reqRecTable.remove(key);
  }
  static NewsList2Show getReqNewsList(String key) {
    Object listObj = null;
    //synchronized(reqRecTable) {
      listObj = reqRecTable.get(key);
    //}
    if (listObj != null) {
      String className = listObj.getClass().getSimpleName();
      if ("NewsList2Show".equals(className)) {
        return (NewsList2Show) listObj;
      } else {
        System.out.println("!!! JmsReqHandler.getReqNewsList():invalid className:" + className);
      }
    }
    return null;
  }

  /**
   * JMS request handlers
   */
  /**
   * GetTweet is to fetch extended Tweet info including the media URL
   * and other related Tweets. This info will be displayed on full screen
   * with optional editing window if it's requested by an admin client
   */
  static private News2ShowExt tweet2ShowExt = null;
  public News2ShowExt getTweet(String id, String seqOrder) {
    // Reset resp string
    JmsReqHandler.tweet2ShowExt = null;
    // Build JSON command
    JSONObject cmdJson = JmsReqHandler.MakeCmdJSON(JmsCmd.GetTweet, id, seqOrder);
    // Send out JSON command
    CsStomp.sendMsg(tomcat2cscope, CsStomp.tomcat2cscopeQ, cmdJson.toString());
    System.out.println("JmsReqHandler.getTweet():sent GetTweet request to cScope:" + url);

    // Now wait 'til we have a Tweet2ShowExt delivered OR the timeLimit is reached
    long waitCnt = 0;
    // Get the current time
    long timeNow = System.currentTimeMillis();

    // Busy wait 'til get the respond OR time out
    while (JmsReqHandler.tweet2ShowExt == null) {
      if ((++waitCnt % 50000000L) == 0L) {
        long timeSpent = System.currentTimeMillis() - timeNow;
        System.out.println("JmsReqHandler.getTweet():waiting -> timeSpent=" + (timeSpent/1000) + "sec");
//        if (timeSpent >= timeLimitMsec) {
//          System.out.println("JmsReqHandler.getTweet():time out reached");
//          break;
//        }
      }
    }
    return JmsReqHandler.tweet2ShowExt;
  }

  /**
   * PutTweet
   */
  static private String putTweetResp;
  public String putTweet(String id, NewsClass[] tClasses, String rtCat, String adminId) {
    // Reset resp string
    JmsReqHandler.putTweetResp = null;

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
    JSONObject cmdJson = JmsReqHandler.MakeCmdJSON(JmsCmd.PutTweet, id, classStr, rtCat, adminId);

    // Send out JSON command
    CsStomp.sendMsg(tomcat2cscope, CsStomp.tomcat2cscopeQ, cmdJson.toString());
    System.out.println("JmsReqHandler.putTweetClass():sent PutTweetClass request to cScope:" + url);

    // Now wait 'til we have the TweetMenu delivered OR the timeLimit is reached
    long waitCnt = 0;
    // Get the current time
    long timeNow = System.currentTimeMillis();

    // Busy wait 'til get the respond OR time out
    while (JmsReqHandler.putTweetResp == null) {
      if ((++waitCnt % 50000000L) == 0L) {
        long timeSpent = System.currentTimeMillis() - timeNow;
        System.out.println("JmsReqHandler.putTweetClass():waiting -> timeSpent=" + (timeSpent/1000) + "sec");
//        if (timeSpent >= timeLimitMsec) {
//          System.out.println("JmsReqHandler.putTweetClass():time out reached");
//          break;
//        }
      }
    }
    return JmsReqHandler.putTweetResp;
  }

  /**
   * GetMenu
   */
  static String getKey4NewsMenu(int maxCnt, int numHours, String seqOrder, String classPair) {
    StringBuilder builder = new StringBuilder();
    builder.append("NewsMenu:");
    builder.append(maxCnt); builder.append(':');
    builder.append(numHours); builder.append(':');
    builder.append(seqOrder); builder.append('@');
    builder.append(classPair);
    return builder.toString();
  }

  static public synchronized NewsMenu reqNewsMenu(String key, int maxCnt, int hourPeriod,
                                                  String seqOrder, String classPair) {
    NewsMenu menu = JmsReqHandler.getReqNewsMenu(key);
    if (menu != null) {
      return menu;
    }
    // No existing req - instance Menu obj to fill upon respond arrival
    menu = new NewsMenu();
    // Put this to the request record
    JmsReqHandler.putReqNewsMenu(key, menu);
    // Build a JSON command to send to the server
    JSONObject cmdJson = MakeCmdJSON(JmsCmd.GetMenu,
                                     maxCnt+"", hourPeriod+"", seqOrder, classPair, key);
    // Send out JSON command
    CsStomp.sendMsg(tomcat2cscope, CsStomp.tomcat2cscopeQ, cmdJson.toString());
    System.out.println("=>JmsReqHandler.reqNewsMenu():sent Menu request to:" + url+
                       " at:" + Util.getCurrentTime());
    return menu;
  }

  /**
   * GetNewsList
   */
  static String getKey4NewsList(int maxCnt, int numHours, String seqOrder, String classPair) {
    StringBuilder builder = new StringBuilder();
    builder.append("NewsList:");
    builder.append(maxCnt); builder.append(':');
    builder.append(numHours); builder.append(':');
    builder.append(seqOrder); builder.append('@');
    builder.append(classPair);
    return builder.toString();
  }

  static public synchronized NewsList2Show reqNewsList(String key, int maxCnt, int hourPeriod,
                                                        String seqOrder, String classStr) {
    NewsList2Show list = JmsReqHandler.getReqNewsList(key);
    if (list != null) {
      return list;
    }
    // No existing req - instance a List obj to fill upon respond arrival
    list = new NewsList2Show();
    // Put this to our request record
    JmsReqHandler.putReqNewsList(key, list);
    // Build a JSON command object to send
    JSONObject cmdJson = MakeCmdJSON(JmsCmd.GetTweetList,
                                     maxCnt+"", hourPeriod+"", seqOrder, classStr, key);
    // Send JSON command to JMS server
    CsStomp.sendMsg(tomcat2cscope, CsStomp.tomcat2cscopeQ, cmdJson.toString());
    System.out.println("=>JmsReqHandler.reqNewsList():sent NewsList req:'" + key + "' to:" + url+
                       " at:" + Util.getCurrentTime());
    return list;
  }

  /**
   * PutFeedback
   */
  static private String putFeedbackResp;
  public String putFeedback(String feedback) {
    // Reset resp string
    JmsReqHandler.putFeedbackResp = null;
    // Build JSON command
    JSONObject cmdJson = JmsReqHandler.MakeCmdJSON(JmsCmd.PutFeedback, feedback);
    // Send out JSON command
    CsStomp.sendMsg(tomcat2cscope, CsStomp.tomcat2cscopeQ, cmdJson.toString());
    System.out.println("JmsReqHandler.putFeedback():sent PutFeedback request to cScope:" + url);

    // Now wait 'til we have the PutFeedback respond delivered OR the timeLimit is reached
    long waitCnt = 0;
    // Get the current time
    long timeNow = System.currentTimeMillis();

    // Busy wait 'til get the respond OR time out
    while (JmsReqHandler.putFeedbackResp == null) {
      if ((++waitCnt % 50000000L) == 0L) {
        long timeSpent = System.currentTimeMillis() - timeNow;
        System.out.println("JmsReqHandler.putFeedback():waiting -> timeSpent=" + (timeSpent/1000) + "sec");
//        if (timeSpent >= timeLimitMsec) {
//          System.out.println("JmsReqHandler.putFeedback():time out reached");
//          break;
//        }
      }
    }
    // putFeedbackResp may be empty if no respond found
    return JmsReqHandler.putFeedbackResp;
  }

  /**
   * AdminLogin
   */
  static private String adminLoginResp;
  public String adminLogin(String id, String passwd) {
    // Reset resp string
    JmsReqHandler.adminLoginResp = null;
 
    // Build a JSON command
    JSONObject cmdJson = JmsReqHandler.MakeCmdJSON(JmsCmd.AdminLogin, id, passwd);
    // Send out JSON command
    CsStomp.sendMsg(tomcat2cscope, CsStomp.tomcat2cscopeQ, cmdJson.toString());
    System.out.println("JmsReqHandler.adminLogin():sent AdminLogin request to cScope:" + url);

    // Now wait 'til we have the AdminLogin respond delivered OR the timeLimit is reached
    long waitCnt = 0;
    // Get the current time
    long timeNow = System.currentTimeMillis();

    // Busy wait 'til get the respond OR time out
    while (JmsReqHandler.adminLoginResp == null) {
      if ((++waitCnt % 50000000L) == 0L) {
        long timeSpent = System.currentTimeMillis() - timeNow;
        System.out.println("JmsReqHandler.adminLogin():waiting -> timeSpent=" + (timeSpent/1000) + "sec");
//        if (timeSpent >= timeLimitMsec) {
//          System.out.println("JmsReqHandler.adminLogin():time out reached");
//          break;
//        }
      }
    }
    // adminLoginResp may be empty if no respond found
    return JmsReqHandler.adminLoginResp;
  }

  /**
   * AdminLogout
   */
  static private String adminLogoutResp;
  public String adminLogout(String id) {
    // Reset resp string
    JmsReqHandler.adminLogoutResp = null;
 
    // Build a JSON command
    JSONObject cmdJson = JmsReqHandler.MakeCmdJSON(JmsCmd.AdminLogout, id);
    // Send out JSON command
    CsStomp.sendMsg(tomcat2cscope, CsStomp.tomcat2cscopeQ, cmdJson.toString());
    System.out.println("JmsReqHandler.adminLogout():sent AdminLogout request to cScope:" + url);

    // Now wait 'til we have the AdminLogout respond delivered OR the timeLimit is reached
    long waitCnt = 0;
    // Get the current time
    long timeNow = System.currentTimeMillis();

    // Busy wait 'til get the respond OR time out
    while (JmsReqHandler.adminLogoutResp == null) {
      if ((++waitCnt % 50000000L) == 0L) {
        long timeSpent = System.currentTimeMillis() - timeNow;
        System.out.println("JmsReqHandler.adminLogout():waiting -> timeSpent=" + (timeSpent/1000) + "sec");
//        if (timeSpent >= timeLimitMsec) {
//          System.out.println("JmsReqHandler.adminLogout():time out reached");
//          break;
//        }
      }
    }
    // adminLogoutResp may be empty if no respond found
    return JmsReqHandler.adminLogoutResp;
  }

  /**
   * AddAdmin
   */
  static private String addAdminResp;
  public String addAdmin(String id, String passwd, String emailAddr) {
    // Reset resp string
    JmsReqHandler.addAdminResp = null;
 
    // Build a JSON command
    JSONObject cmdJson = JmsReqHandler.MakeCmdJSON(JmsCmd.AddAdmin, id, passwd, emailAddr);
    // Send out JSON command
    CsStomp.sendMsg(tomcat2cscope, CsStomp.tomcat2cscopeQ, cmdJson.toString());
    System.out.println("JmsReqHandler.addAdmin():sent AddAdmin request to cScope:" + url);

    // Now wait 'til we have the AddAdmin respond delivered OR the timeLimit is reached
    long waitCnt = 0;
    // Get the current time
    long timeNow = System.currentTimeMillis();

    // Busy wait 'til get the respond OR time out
    while (JmsReqHandler.addAdminResp == null) {
      if ((++waitCnt % 50000000L) == 0L) {
        long timeSpent = System.currentTimeMillis() - timeNow;
        System.out.println("JmsReqHandler.addAdmin():waiting -> timeSpent=" + (timeSpent/1000) + "sec");
//        if (timeSpent >= timeLimitMsec) {
//          System.out.println("JmsReqHandler.addAdmin():time out reached");
//          break;
//        }
      }
    }
    // addAdminResp may be empty if no respond received
    return JmsReqHandler.addAdminResp;
  }

  /**
   * RmAdmin
   */
  static private String rmAdminResp;
  public String rmAdmin(String id) {
    // Reset resp string
    JmsReqHandler.rmAdminResp = null;
 
    // Build a JSON command
    JSONObject cmdJson = JmsReqHandler.MakeCmdJSON(JmsCmd.RmAdmin, id);
    // Send out JSON command
    CsStomp.sendMsg(tomcat2cscope, CsStomp.tomcat2cscopeQ, cmdJson.toString());
    System.out.println("JmsReqHandler.rmAdmin():sent RmAdmin request to cScope:" + url);

    // Now wait 'til we have the RmAdmin respond delivered OR the timeLimit is reached
    long waitCnt = 0;
    // Get the current time
    long timeNow = System.currentTimeMillis();

    // Busy wait 'til get the respond OR time out
    while (JmsReqHandler.rmAdminResp == null) {
      if ((++waitCnt % 50000000L) == 0L) {
        long timeSpent = System.currentTimeMillis() - timeNow;
        System.out.println("JmsReqHandler.rmAdmin():waiting -> timeSpent=" + (timeSpent/1000) + "sec");
//        if (timeSpent >= timeLimitMsec) {
//          System.out.println("JmsReqHandler.rmAdmin():time out reached");
//          break;
//        }
      }
    }
    // rmAdminResp may be empty if no respond received
    return JmsReqHandler.rmAdminResp;
  }

  /**
   * Make a TweetList request JSON cmd to send over the Stomp connection
   * @param cmd
   * @param args[0]:tweetId, tweetClass
   * @param args[1]:tweetClass
   * @param args[2]:RtCat
   * @return
   */
  static JSONObject MakeCmdJSON(JmsCmd cmd, String ... args) {
    JSONObject jsonCmd = new JSONObject();
    // Put the command first
    jsonCmd.put("cmd", cmd+"");

    switch (cmd) {
      case GetMenu:
        jsonCmd.put("maxTweetCnt", args[0]);
        jsonCmd.put("hourPeriod", args[1]);
        jsonCmd.put("seqOrder", args[2]);
        jsonCmd.put("tweetClass", args[3]);
        jsonCmd.put("cacheKey", args[4]);
        break;

      case GetTweet:
        // first arg is the Tweet Id
        jsonCmd.put("tweetId", args[0]);
        jsonCmd.put("seqOrder", args[1]);
        break;

      case PutTweet:
        jsonCmd.put("tweetId", args[0]);
        jsonCmd.put("tweetClasses", '[' + args[1] + ']');
        jsonCmd.put("rtCat", args[2]);
        jsonCmd.put("adminId", args[3]);
        break;

      case GetTweetList:
        jsonCmd.put("maxTweetCnt", args[0]);
        jsonCmd.put("hourPeriod", args[1]);
        jsonCmd.put("seqOrder", args[2]);
        jsonCmd.put("tweetClass", args[3]);
        jsonCmd.put("cacheKey", args[4]);
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
        System.err.println("JmsReqHandler.makeJsonCmd():invalid cmd:" + cmd);
        break;
    }
    return jsonCmd;
  }

}
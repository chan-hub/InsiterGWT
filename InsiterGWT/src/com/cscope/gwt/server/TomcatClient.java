package com.cscope.gwt.server;

import java.io.File;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import insiter.ina.news.NewsIndex;
import insiter.ina.news.cluster.NewsCluster;
import insiter.ina.news.curate.NewsFilter;
import insiter.ina.news.service.NewsDelivery;
import insiter.ina.news.service.web.NewsJSON;
import insiter.ina.news.service.web.NewsList4Menu;
import insiter.ina.news.service.web.NewsListBuilder;
import insiter.ina.news.service.web.NewsMenuConfig;
import insiter.ina.news.table.NewsLists;
import insiter.ina.news.build.NewsClass.Menu;
import insiter.ina.tweet.processor.TweetProcessor;
import insiter.ina.util.constant.Constant.RunMode;
import insiter.ina.util.IO;
import insiter.ina.util.Util;
import insiter.ina.util.constant.Resource;
import insiter.ina.util.constant.Resource.Track;

/**
 * TomcatClient is to open up a connection & streams to the NewsListServer,
 * to listen the ObjectInputStream continuously for getting the latest
 * NewsList object update. Once a new NewsList object is received, it's
 * immediately transferred to the latest NewsList to make it effective
 */
public class TomcatClient {
  // From insiter.ina.util.constant.Network.java:
  static final int    ProdMainProcNewsPort = 63650;
  static final String ProdMainProcNewsAddr = "10.0.10.5";
  static int serverPort = ProdMainProcNewsPort;
  static String serverAddr = ProdMainProcNewsAddr;

  static public boolean debug = false;
  // Run mode - one of Prod, Devl or Test
  static RunMode runMode = null;
  static Socket clientSocket;
  static NewsDelivery newsDelivery = null;
  static ObjectInputStream objInputStream;
  static ObjectOutputStream objOutputStream;

  static public NewsJSON getNewsJSON(Long id) {
    if (TomcatClient.newsDelivery == null) {
      System.out.println("TomcatClient.getNewsJSON():Can't get NewsJSON as its NewsDelivery is null");
      return null;
    }
    return TomcatClient.newsDelivery.getNewsJSON(id);
  }

  /**
   * Start TomcatClient
   */
  static public void startTomcatClient() {
    // Take server addr from Property if specified
    String urlProp = Property.getServerUrl();
    if (urlProp != null) {
      TomcatClient.serverAddr = urlProp;
      System.out.println("\nTomcatClient.startTomcatClient():Take Url Property:" + urlProp);
    }
    // Take server port as well
    int port = Property.getServerPort();
    if (port > 0) {
      serverPort = port;
    }
    // Set its run mode
    if (Property.isProdRun()) {
      TomcatClient.runMode = RunMode.Prod;
    }
    else if (Property.isDevlRun()) {
      TomcatClient.runMode = RunMode.Devl;
    }
    else if (Property.isTestRun()) {
      TomcatClient.runMode = RunMode.Test;
    }
    else {
      // Can't proceed w/o concrete run mode
      System.err.println("\n*** TomcatClient.startTomcatClient():Cannot proceed w/o setting RuunMode ***");
      System.exit(0);
    }
    // Init its News Curator
    NewsFilter.init();

    // Load NewsMenu in NewsMenuConfig
    TomcatClient.initNewsMenu();

    // Enable this client Thread
    TomcatClient.runNewsListClient = true;
 
    // Start running the Thread
    TomcatClient.TomcatClientThread.start();
  }

  static public void stopTomcatClient() {
  }

  static private boolean makeSocketConnection() {
    // Send connection request to the News server to open a client socket
    System.out.println("\nTomcatClient.makeServerConnection():Connect NewsServer:" +
                          serverAddr + ':' + serverPort);
    TomcatClient.clientSocket = IO.openClientSocket(serverAddr, serverPort);
    if (TomcatClient.clientSocket == null) {
      System.out.println("TomcatClient.makeServerConnection():Can't connect NewsServer on:[" +
                          serverAddr + ":" + serverPort + "]");
      return false;
    }
    // OK - established connection and client socket is opened
    System.out.println("TomcatClient.makeServerConnection():Connected NewsServer on [localPort:remotePort]=[" +
                        clientSocket.getLocalPort() + ":" + clientSocket.getPort() + "]");
    return true;
  }

  static private boolean isSocketConnected() {
    return TomcatClient.clientSocket!=null &&
           TomcatClient.clientSocket.isConnected() &&
           !TomcatClient.clientSocket.isClosed();
  }

  /**
   * Open both ObjectInput & ObjectOutput streams on the opened socket
   * @return
   */
  static private boolean openObjStreams(Object the1stObj) {
    // Make sure our Socket is connected && open
    if (!TomcatClient.isSocketConnected()) {
      System.out.println("TomcatClient.openObjStreams():can't open as Socket is NOT connected");
      return false;
    }

    // Open object streams on the socket - open output stream first to send out the1stObj
    if (!TomcatClient.openObjOutputStream(the1stObj)) {
      System.out.println("TomcatClient.openObjStreams():can't open ObjectOutputStream, abort socket");
      return false;
    }
    System.out.println("TomcatClient.openObjStreams():Opened ObjectOutputStream");

    if (!openObjInputStream()) {
      System.out.println("TomcatClient.openObjStreams():can't open ObjectInputStream, abort socket");
      return false;
    }
    System.out.println("TomcatClient.openObjStreams():Opened ObjectInputStream");
    return true;
  }

  /**
   * NewsList4Menu encapsulates all of the News to be presented
   * organized into a tree structure reflecting user-configured
   * News Menu contents
   */
  static protected NewsList4Menu latestNewsList4Menu = null;

  /**
   * NewsList object being updated by the receiver thread, then
   * retrieved by Tomcat server methods to support client requests
   */
  static protected NewsLists latestNewsLists = null;

  /**
   * Flag to control newListClientThread execution
   */
  static private boolean runNewsListClient = false;

  /**
   * Thread responsible for making connection and receiving the latest
   * NewsList updates from NewsList server
   */
  static private final long sleepMsecInc = 1000L;
  static private Thread TomcatClientThread = new Thread() {
    public void run() {
      int failCnt = 0;
      long sleepTime = 0L;
      long sleepTimeBound = 60000L;  // Incremental sleep time to maximum of 60sec
      String the1stObj = Property.getClientId();  // The 1stObj to send is its Id
      if (the1stObj == null) {
      	the1stObj = "Unknown-Client";
      }
      // Open up connection then start listening its InputStream to get NewsList updates
      while (runNewsListClient) {
        // First open socket connection to NewsList server
        boolean opened = TomcatClient.openUpSocketAndStreams(the1stObj);
        if (opened) {
          System.out.println("TomcatClientThread.run():Opened Socket & Streams");
          // Start running NewsList object receiving loop - should not return 'til service stop
          TomcatClient.runNewsListRxLoop();
          // Session terminated - close socket & stream prior to re-opening
          TomcatClient.closeConnection();
        }
        else {
          // Failed to open - sleep before re-try for time being incremented up to the timeBound
          if (sleepTime <= sleepTimeBound) {
            sleepTime += TomcatClient.sleepMsecInc;
          }
          // Sleep here
          try {
            Thread.sleep(sleepTime);
          } catch (Exception e) {
            System.out.println("TomcatClientThread.run():"+e.getClass().getSimpleName() +
                               " while sleeping for:" + sleepTime/1000L + " sec");
          }
          // Show failCnt every 100 failures
          if (failCnt % 100 == 0) {
            failCnt++;
            System.out.println("TomcatClientThread.run():Failed " + failCnt +
                               " times, to open Socket & Streams");
          }
        }
      }
    }
  };

  /**
   * 
   * @param the1stObj
   * @return
   */
  static private boolean openUpSocketAndStreams(Object the1stObj) {
    // Show where we are
    System.out.println("TomcatClient.openUpSocketAndStreams():Start opening socket & stream");

    // Close down if any of its socket & streams are left open
    TomcatClient.closeConnection();

    // First open socket connection to NewsList server
    boolean sktConnected = TomcatClient.makeSocketConnection();
    if (!sktConnected) {
      System.out.println("TomcatClient.openUpSocketAndStreams():Can't make socket connection");
      return false;
    }
    System.out.println("TomcatClient.openUpSocketAndStreams():Opened socket connection to the server");

    // OK - now open Streams on the socket connection opened
    boolean streamOpened = TomcatClient.openObjStreams(the1stObj);
    if (!streamOpened) {
      System.out.println("TomcatClient.openUpSocketAndStreams():Can't open ObjectStreams");
      return false;
    }
    System.out.println("TomcatClient.openUpSocketAndStreams():Opened ObjectStreams over the socket");
    return true;
  }

  /**
   * Get the latest NewsIndex table delivered w/ NewsLists
   * @return
   */
  static public Map<Long,NewsIndex> getNewsIndexTable() {
  	if (TomcatClient.newsDelivery == null) {
  		System.out.println("!TomcatClient.getNewsIndexTable():TomcatClient.newsDelivery is null");
  		return new HashMap<>();
  	}
  	Map<Long,NewsIndex> indexTable = TomcatClient.newsDelivery.indexTable;
  	if (indexTable != null) {
      System.out.println("TomcatClient.getNewsIndexTable():Returning TomcatClient.newsDelivery's " +
  	                     "NewsIndex table w/ "+ indexTable.size() + " entries");
  	  return indexTable;
  	}
  	System.out.println("Error:TomcatClient.getNewsIndexTable():NewsDelivery's NewsIndexTable is null");
    return new HashMap<>();
  }

  /**
   * Get the latest NewsCluster table delivered w/ NewsLists
   * @return
   */
  static public Map<Long,NewsCluster> getNewsClusterTable() {
  	if (TomcatClient.newsDelivery == null) {
  		System.out.println("!TomcatClient.getNewsClusterTable():TomcatClient.newsDelivery is null");
  		return new HashMap<>();
  	}
  	Map<Long,NewsCluster> clusterTable = TomcatClient.newsDelivery.clusterTable;
  	if (clusterTable != null) {
      System.out.println("TomcatClient.getNewsIndexTable():Returning TomcatClient.newsDelivery's " +
                         "NewsCluster table w/ "+ clusterTable.size() + " entries");
  	  return clusterTable;
  	}
  	System.out.println("Error:TomcatClient.getNewsClusterTable():NewsDelivery's NewsCluster is null");
    return new HashMap<>();
  }

  /**
   * Initialize per-Tomcat server NewsMenu
   */
  static private void initNewsMenu() {
  	// Update NewsMenu
  	String menuName = NewsMenuConfig.getMenuName(Track.tech);
  	File menuFile = Property.getNewsMenuFile(menuName);
  	if (menuFile != null) {
  	  NewsMenuConfig.initNewsMenu(menuName, Track.tech, menuFile);
  	} else {
  		System.out.println("\n===>!TomcatClient.initNewsMenu():Can't get File instance from '" +
  	    Property.getConfigDirPath() + '/' + Property.getNewsMenuDir());
  	}
  }

  /**
   * Update NewsMenu at regular interval
   */
  static private void updateNewsMenu() {
  	// Update NewsMenu
  	String menuName = NewsMenuConfig.getMenuName(Track.tech);
  	File menuFile = Property.getNewsMenuFile(menuName);
  	if (menuFile != null) {
  	  NewsMenuConfig.updateMenuTable(menuName, Track.tech, menuFile);
  	} else {
  		System.out.println("\n===>TomcatClient.runNewsListRxLoop():Can't get File instancefrom '" +
  	    Property.getConfigDirPath() + '/' + Property.getNewsMenuDir());
  	}
  }

  /**
   * Run an infinite loop to receive NewsList updates from the News server.
   *
   * When this method returns, the current RX loop terminates and to start
   * a new session with a new Socket connection and new ObjectStreams.
   * Return true means normal termination (by the boolean control flag).
   * Return false means an abnormal termination, for re-connect attempt
   */
  static private boolean runNewsListRxLoop() {
    // Run an infinite loop to receive NewsList updates
    while (TomcatClient.runNewsListClient) {
      Object o = null;
      try {
        o = TomcatClient.objInputStream.readUnshared();
        // o = TomcatClient.objInputStream.readObject();
      } catch (SocketException se) {
        if (se.getMessage().equals("Connection timed out")) {
          // Network instability caused the connection broken. Reconnect it
          System.out.println(
              "TomcatClient.runNewsListRxLoop():Connection timed out - must reconnect");
          return false;
        } else {
          System.out.println("TomcatClient.runNewsListRxLoop():SocketException:'" +
                              se.getMessage() + "', reconnect");
          return false;
        }
      } catch (Exception e) {
        System.out.println("TomcatClient.runNewsListRxLoop():Can't read ObjectInputStream:" + e
            .getMessage() + ", reconnect");
        return false;
      }
      // Check the received object
      if (o == null) {
        System.out.println("TomcatClient.runNewsListRxLoop():Received object is NULL");
        continue;
      }
      // Take the received object
      if (o instanceof NewsLists) {
      	// It's a ready-built NewsLists - take it if this Tomcat is configured for the old
      	NewsLists newsLists = (NewsLists)o;
        int size = newsLists.newsList2Go.getAllNewsList().size();
      	if (!Property.isNewNewsList()) {
          NewsListsQuery.setNewsLists(newsLists);
          TomcatClient.latestNewsLists = newsLists;
          System.out.println("\n===>TomcatClient.runNewsListRxLoop():Received and stored " +
                             " NewsLists of " + size + " News at " + Util.getCurrentTime());
      	} else {
          System.out.println("\n===>TomcatClient.runNewsListRxLoop():Received but dropped" +
                             " NewsLists of " + size + " News at " + Util.getCurrentTime());
      	}
      }
      else if (o instanceof NewsDelivery) {
    	  // Now Property is unified by Tektweet.property in the ~/conf directory
//        // Update Property-based controls
//        if (Resource.isPropUpdated()) {
//          Resource.updateCscopeProp();
//        }
      	// Update NewsMenu if necessary
      	TomcatClient.updateNewsMenu();

        // It's a NewsDelivery - take it to produce a NewsLists
      	NewsDelivery newsDlvry = (NewsDelivery) o;
        int newsSize = newsDlvry.getNewsTable().size();
        int indexSize = newsDlvry.getNewsIndexTable().size();

        // Bind the delivered News tables to each of their class' static table
        newsDlvry.setNewsTables();

// Show NewsCluster table entries if requested
if (debug) {
  for (Long cid : NewsCluster.getNewsClusterTable().keySet()) {
	NewsCluster nc = NewsCluster.getNewsClusterTable().get(cid);
	if (nc != null) {
		Long headId = nc.getHeadId();
		System.out.println("TomcatClient.runNewsListRxLoop():ClusterId=" + cid + " mapps Cluster w/ head="+headId +
				 " with members=" + Util.toString(nc.getMemberIdList()));
	} else {
		System.out.println("TomcatClient.runNewsListRxLoop():ClusterId=" + cid + " has NO Newscluster");
	}
  }
}
        // Update its debug flag w/ Property setting OR NewsDelivery cmd
        TomcatClient.debug = Property.getDebugFlag() || newsDlvry.debug;
        NewsList4Menu.setDebug(TomcatClient.debug);
        // Update run mode flag
        boolean runMode = Property.updateIsProdRun();
        String runModeStr = (runMode ? " ProductionRun" : "DevelopmentRun");
        System.out.println("\n===>TomcatClient.runNewsListRxLoop():Received NewsDelivery of " + newsSize + " News & " +
            indexSize + " NewsIndex in '" + runMode + "' at " + Util.getCurrentTime() + ", with Debug='" + TomcatClient.debug + '\'');

        // Take the main platform's state information to share if it's a new NewsList
        if (Property.isNewNewsList()) {
          // Keep this NewsDelivery
          TomcatClient.newsDelivery = newsDlvry;
          // Take the NewsDelivery's newsPeriod, or override it w/ Property value if exists
          long maxNewsPeriod = newsDlvry.newsPeriod;
          long maxNewsProp = Property.getMaxPeriodMsec();
          if (maxNewsProp != 0L) {
            maxNewsPeriod = maxNewsProp;
          }
//          // Set TweetProcessor's current Time
//          TweetProcessor.setCurrentTime(newsDlvry.tweetArrvTime);
          // Get the NewsDelivery's size
          int delSize = newsDlvry.indexTable.size();
          
          // Discard this Delivery if it's empty
          if (delSize == 0) {
            System.out.println("TomcatClient.runNewsListRxLoop():Discard an empty " +
 	                 "NewsDelivery with " + delSize + " NewsIndex objs");
            continue;
          }
          System.out.println("TomcatClient.runNewsListRxLoop():Received " +
	                 "NewsDelivery with " + delSize + " NewsIndex objs");
          /********************************************************************
           * Build a new fresh NewsList4Menu out of the latest NewsDelivery
           ********************************************************************/
          NewsList4Menu rootNewsList = null;
          try {
        	  rootNewsList = NewsListBuilder.buildNewsList4Menu(newsDlvry, maxNewsPeriod, TomcatClient.runMode, TomcatClient.debug);
          }
          catch (Exception e) {
          	System.out.println("===>NewsListBuilder.buildNewsList4Menu():Discard NewsDelivery due to "+
                               " Error/Exception:" + e.getMessage());
            e.printStackTrace();
            continue;
          }
          // Check the NewsList freshly built
          NewsList4Menu topList4Menu = rootNewsList.findTopNewsList();
          if (topList4Menu == null) {
            System.out.println("===>TomcatClient.runNewsListRxLoop():Can't fine TopNewsList from rootNewsList");
            continue;
          }
          // Take if this latest NewsList is in good shape
          if (topList4Menu.isNewsListBuilt()) {
            TomcatClient.latestNewsList4Menu = rootNewsList;
            // NewsListQuery will utilize this NewsList4Menu
            NewsListsQuery.setNewsList4Menu(TomcatClient.latestNewsList4Menu);
            System.out.println("===>TomcatClient.runNewsListRxLoop():Created a NewsLists=" + newsSize +
                               " out of raw NewsDelivery received at " + Util.getCurrentTime());
          } else {
    TomcatClient.latestNewsList4Menu = rootNewsList;
    NewsListsQuery.setNewsList4Menu(TomcatClient.latestNewsList4Menu);
            System.out.println("===>TomcatClient.runNewsListRxLoop():Failed, but taking NewsList4Menu " +
                               " size=" + newsSize + " out of raw NewsDelivery received at " + Util.getCurrentTime());
          }
        } else {
          System.out.println("===>TomcatClient.runNewsListRxLoop():Dropped raw NewsDelivery=" +
                              newsSize +" News received at " + Util.getCurrentTime());
        } 
      } else {
        System.out.println("TomcatClient.runNewsListRxLoop():Received is *NOT* NewsLists, but " +
                            o.getClass().getSimpleName()+ " at " + Util.getCurrentTime());
      }
    }
    return true;
  }

  /**
   * Open an ObjectInputStream on the clientSocket
   * @return
   */
  static private boolean openObjInputStream() {
    // Open an ObjectInputStream to get ArtMatchResp objects
    int timeoutSec = 8;
    String portStr = IO.getPortStr(TomcatClient.clientSocket);
    ObjectInputStream objReader = IO.openObjInputStream(clientSocket, timeoutSec);
    if (objReader == null) {
      System.out.println("TomcatClient.openObjInputStream():can't open ObjectInputStream on:" + portStr +
      		               " with " + timeoutSec + " sec tiimeout");
      try {
        TomcatClient.clientSocket.close();
      } catch (Exception e) {
        System.out.println("TomcatClient.openObjInputStream():can't close socket on:" + portStr);
        return false;
      }
      System.out.println("TomcatClient.openObjInputStream():Closed unusable client socket on:" + portStr);
      return false;
    }
    TomcatClient.objInputStream = objReader;
    System.out.println("TomcatClient.openObjInputStream():ObjectInputStream is opened on:" + portStr);
    return true;
  }

  /**
   * Open an ObjectOutputStream on the clientSocket to send out 'the1stObj' 
   * @return
   */
  static private boolean openObjOutputStream(Object the1stObj) {
    // Open an object output stream to send ArtMatchReq to the server
    ObjectOutputStream objWriter = null;
    try {
      OutputStream outStream = TomcatClient.clientSocket.getOutputStream();
      objWriter = new ObjectOutputStream(outStream);
    } catch (Exception e) {
      System.err.println("TomcatClient.openObjOutputStream():Can't open ObjectOutputStream:" + e.getMessage());
      return false;
    }
    // Do a dummy write/flush to help opening ObjectInputStream by the server
    try {
    	objWriter.writeUnshared(the1stObj);
      //objWriter.writeUnshared(new String("Dummy String Object"));
      objWriter.flush();
    } catch (Exception e) {
      System.err.println("TomcatClient.openObjOutputStream():Can't flush ObjStream:" + e.getMessage());
    }
    TomcatClient.objOutputStream = objWriter;
    return true;
  }

  /**
   * Close out ALL client's connections
   */
  static private void closeConnection() {
    if (TomcatClient.objInputStream != null)
      try {
        TomcatClient.objInputStream.close();
      } catch (Exception e) {
        System.out.println("TomcatClient.closeConnection():objInputStream Exception:" + e.getMessage());
      }
    if (TomcatClient.objOutputStream != null)
      try {
        TomcatClient.objOutputStream.close();
      } catch (Exception e) {
        System.out.println("TomcatClient.closeConnection():objOutputStream Exception:" + e.getMessage());
      }
    if (TomcatClient.clientSocket != null && !TomcatClient.clientSocket.isClosed())
      try {
        TomcatClient.clientSocket.close();
      } catch (Exception e) {
        System.out.println("TomcatClient.closeConnection():clientSocket Exception:" + e.getMessage());
      }
  }
}

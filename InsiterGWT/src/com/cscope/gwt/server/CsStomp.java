package com.cscope.gwt.server;

import org.apache.activemq.transport.stomp.StompConnection;
import org.apache.activemq.transport.stomp.StompFrame;
import org.apache.activemq.transport.stomp.Stomp.Headers.Subscribe;

public class CsStomp {

  // 2 unidirectional queues for hotweet web server & cScope tweet processor
  static public final String tomcat2cscopeQ = "/queue/json/tomcat2cscope";
  static public final String cscope2tomcatQ = "/queue/json/cscope2tomcat";

  static public StompConnection openConnection(boolean receiver, String url, int port,
                                       String usrName, String passwd, String queueName) {
    // Get a new StompConnection instance
    StompConnection connection = new StompConnection();
    try {
      connection.open(url, port);
    } catch (Exception e1) {
      System.err.println("Stomp.openConnection():can't open:" + e1.getMessage());
      return null;
    }
    try {
      connection.connect(usrName, passwd);
    } catch (Exception e2) {
      System.err.println("Stomp.openConnection():can't connect:" + e2.getMessage());
      return null;
    }
    // Only the receiver side needs to set its AckMode
    if (receiver) {
        //connection.subscribe("/queue/test", Subscribe.AckModeValues.CLIENT);
        //connection.subscribe("/queue/test", Subscribe.AckModeValues.INDIVIDUAL);
        //connection.subscribe("/queue/tweet/tech", Subscribe.AckModeValues.AUTO);
      try {
        connection.subscribe(queueName, Subscribe.AckModeValues.AUTO);
      } catch (Exception e3) {
        System.err.println("Stomp.openConnection():can't subscribe:" + e3.getMessage());
        return null;
      }
      // Drain any pending messages
      int oldMsgCnt = 0;
      long recvTimeout = 1000L;
      while (true) {
        try {
          if (connection.receive(recvTimeout) != null) {
            oldMsgCnt++;
          }
        } catch (Exception e4) {
          // TimeOutException
          if (oldMsgCnt > 0)
            System.out.println("=>Stomp.openConnection():drained " + oldMsgCnt + " old pending msg");
          else
            System.out.println("=>Stomp.openConnection():NO pending msg found");
          break;
        }
      }
    }
    System.out.println("Stomp.openConnection():Opened STOMP channel of:" + queueName);
    return connection;
  }

  static public boolean closeConnection(StompConnection connection) {
    if (connection == null) {
      return false;
    }
    try {
      connection.disconnect();
      connection.close();
    } catch (Exception e) {
      System.err.println("Stomp.closeConnection():can't close:" + e.getMessage());
      return false;
    }
    return true;
  }

  static public String recvMsg(StompConnection connection, boolean doLog) {
    StompFrame frame = null;
    try {
      frame = connection.receive(0);
    } catch (Exception e) {
      if (doLog) {
        System.err.println("CsStomp.recvMsg():receive() Exception:" + e.getMessage());
        try {
          Thread.currentThread().wait(1000);
        } catch (Exception ee) {
          System.err.println("CsStomp.recvMsg():wait() Exception:" + e.getMessage());
        }
      }
      return null;
    }
    if (frame == null) {
      if (doLog) {
        System.err.println("CsStomp.recvMsg():frame is NULL");
      }
      return null;
    }
    // OK - we have a Stomp frame, get the message body to return
    String message = frame.getBody();
    return message;
  }

  static public boolean sendMsg(StompConnection connection, String destQ, String msg) {
    try {
      connection.send(destQ,  msg);
    } catch (Exception e) {
      System.err.println("Stomp.recvMsg():can't send:" + e.getMessage());
      return false;
    }
    return true;
  }
}

package com.cscope.gwt.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

import insiter.ina.news.service.web.NewsJsonBuilder;
import insiter.ina.util.StrUtil;
import insiter.ina.util.constant.Constant;
import insiter.ina.util.constant.Resource;
import insiter.ina.util.constant.TimeKeeper;

public class Property {

  static Properties props = null;

  static {
    System.out.println("\n=== Property init ===");
    Property.loadTomcatProperty();
  }

  static public boolean loadTomcatProperty() {
    File configDir = new File(System.getProperty("catalina.base"), "conf");
	File propertyFile = new File(configDir, "Tektweet.properties");
	if (!propertyFile.exists()) {
      System.out.println("Property.loadTomcatProperty():Can't find Property file:" +
	                      configDir.getAbsolutePath() + "/Tektweet.properties");
      return false;
	}
	try {
	  InputStream stream = new FileInputStream(propertyFile);
	  Property.props = new Properties();
	  Property.props.load(stream);
	  System.out.println("Property.loadTomcatProperty():Loaded Property from its propertyFile:" +
	                         propertyFile.getAbsolutePath());   
	  return true;
	} catch (Exception e) {
	  System.err.println("Property.loadTomcatProperty():can't read the propertyFile:" +
	                     "Tektweet.properties in the conf dir");
	  return false;
	}
  }

  /**
   * Retrieve 'ProductionRun' Property line from the property file ten
   * update the run mode flag in those interested sub systems (i.e. NewsListJSON)
   * @return
   */
  static protected boolean updateIsProdRun() {
	  boolean isProdRun = true;
    if (!Property.loadTomcatProperty()) {
      System.out.println("Property.updateIsProdRun():Can't load Tomcat Property - assume Production Run");
	  }
    else {
      // Get the actual Property
	    String isProd = Property.props.getProperty("ProductionRun", "true");
	    if (isProd.equals("true")) {
		    isProdRun = true;
	    } else {
		    isProdRun = false;
	    }
	  }
    NewsJsonBuilder.setProdRunMode(isProdRun);
    return isProdRun;
  }

  static protected boolean isProdRun() {
    if (Property.props == null) {
      System.err.println("Property.isProdRun{}:Property is not found OR not initialized");
      return false;
    }
    String runMode = Property.props.getProperty("ProductionRun");
    return (runMode!=null && runMode.equals("true"));
  }
  static protected boolean isDevlRun() {
    if (Property.props == null) {
      System.err.println("Property.isDevlRun{}:Property is not found OR not initialized");
      return false;
    }
    String runMode = Property.props.getProperty("DevelopmentRun");
    return (runMode!=null && runMode.equals("true"));
  }
  static protected boolean isTestRun() {
    if (Property.props == null) {
      System.err.println("Property.isTestRun{}:Property is not found OR not initialized");
      return false;
    }
    String runMode = Property.props.getProperty("TestRun");
    return (runMode!=null && runMode.equals("true"));
  }

  /**
   * Retrieve the 'debug' flag from freshly loaded Property, to update the flag
   * @return
   */
  static public boolean getDebugFlag() {
	if (!Property.loadTomcatProperty()) {
	  System.out.println("Property.getDebugFlag():Can't load Tomcat Property - assume disabled");
	  return false;
	}
    String debugFlagStr = "false";
    if (Property.props == null) {
  	  System.out.println("Property.getDebugFlag():Can't build Property object - assume disabled");
  	  return false;
    } else {
      debugFlagStr = Property.props.getProperty("debug");
      if (debugFlagStr == null) {
        System.out.println("Property.getDebugFlag():Can't find 'debug' in Property, assume false");
        return false;
      }
    }
    debugFlagStr = debugFlagStr.toLowerCase();
    System.out.println("Property.getDebugFlag():Got debugFlag:" + debugFlagStr);
    return debugFlagStr.equals("yes") || debugFlagStr.equals("true");
  }

  /**
   * Return true if this Tomcat client is configured for the new NewsList
   * @return
   */
  static protected boolean isNewNewsList() {
  	if (Property.props == null) {
      System.err.println("Property.isNewNewsList{}:Property is not found OR not initialized");
      return false;
  	}
  	String newsListMode = Property.props.getProperty("NewsListMode");
  	return (newsListMode!=null && newsListMode.equals("new"));
  }

  /**
   * Get maxNewsPeriod property specified in hours, converted to msec
   * Return zero if no such property is found
   * @return
   */
  static protected long getMaxPeriodMsec() {
    if (Property.props == null) {
	  System.err.println("Property.isNewNewsList{}:Property is not found OR not initialized");
	  return 0L;
	}
	String maxHourStr = Property.props.getProperty("MaxNewsPeriodHours");
	if (maxHourStr!=null && !maxHourStr.isEmpty() && StrUtil.isNumber(maxHourStr)) {
      int maxHours = Integer.parseInt(maxHourStr);
      return (long)maxHours * TimeKeeper.timeMsec_1Hour;
	}
	return 0L;
  }

  static protected int getServerPort() {
  	if (Property.props == null) {
      System.err.println("Property.static{}:can't read the propertyFile:" +
          "Tektweet.properties in the conf dir");
  	}
    int port = 0;
    String portStr = Property.props.getProperty("serverPort");
    if (portStr != null && StrUtil.isNumber(portStr)) {
      port = Integer.parseInt(portStr);
    }
	System.out.println("Property.getServerPort():Get server port:" + portStr);
  	return port;
  }

  static protected String getServerUrl() {
    String url = null;
    if (Property.props != null) {
      url = Property.props.getProperty("serverURL");
    }
    System.out.println("Property.getServerUrl():Get server URL:" + url);
    return url;
  }

  static protected String getPageTitle() {
    String title = null;
    if (Property.props != null) {
      title = Property.props.getProperty("pageTitle");
    }
    System.out.println("Property.getServerUrl():Get Page Title:" + title);
    return title;
  }

  static protected String getClientId() {
    String id = null;
    if (Property.props != null) {
      id = Property.props.getProperty("clientId");
    }
    System.out.println("Property.getServerUrl():Get Client ID:" + id);
    return id;
  }

  static public File getConfigDirFile() {
    File configDir = new File(System.getProperty("catalina.base"), "conf");
    return configDir;
  }

  static public String getConfigDirPath() {
    File configDir = Property.getConfigDirFile();
    String path = configDir.getAbsolutePath();
    return path;
  }

  static public String getNewsMenuDir() {
    String dir = null;
    if (Property.props != null) {
      dir = Property.props.getProperty("NewsMenu");
    }
    System.out.println("Property.getNewsMenuDir():Get News Menu directory:" + dir);
    return dir;
  }

  static public File getNewsMenuFile(String menuName) {
    File confDir = Property.getConfigDirFile();
    File menuDir = new File(confDir, Property.getNewsMenuDir());
    File menuFile = new File(menuDir, menuName);
    System.out.println("Property.getNewsMenuFile():Get NewsMenu file from:" + menuFile.getAbsolutePath());
    return menuFile;
  }
}
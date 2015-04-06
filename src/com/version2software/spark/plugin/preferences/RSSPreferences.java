/**
 * Copyright (C) 2006 Version 2 Software, LLC. All rights reserved.
 * 
 * This software is published under the terms of the GNU Public License (GPL).
 */

package com.version2software.spark.plugin.preferences;

import java.util.ArrayList;

public class RSSPreferences {
   private String refreshInterval;
   private boolean useProxy;
   private String proxyHost;
   private String proxyPort;
   
   private ArrayList<String> urls = new ArrayList<String>();
   
   public String getRefreshInterval() {
      return refreshInterval;
   }

   public void setRefreshInterval(String refreshInterval) {
      this.refreshInterval = refreshInterval;
   }

   public boolean isUseProxy() {
      return useProxy;
   }

   public void setUseProxy(boolean useProxy) {
      this.useProxy = useProxy;
   }

   public String getProxyHost() {
      return proxyHost;
   }

   public void setProxyHost(String proxyHost) {
      this.proxyHost = proxyHost;
   }

   public String getProxyPort() {
      return proxyPort;
   }

   public void setProxyPort(String proxyPort) {
      this.proxyPort = proxyPort;
   }
   
   public ArrayList<String> getUrls() {
      return urls;
   }

   public void setUrls(ArrayList<String> feeds) {
      this.urls = feeds;
   }
}

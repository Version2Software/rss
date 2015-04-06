/**
 * Copyright (C) 2006 Version 2 Software, LLC. All rights reserved.
 * 
 * This software is published under the terms of the GNU Public License (GPL).
 */

package com.version2software.spark.plugin.preferences;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.net.URL;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;

import org.jivesoftware.Spark;
import org.jivesoftware.spark.preference.Preference;
import org.jivesoftware.spark.util.log.Log;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

public class RSSPreference implements Preference {
   private RSSPreferenceUI ui = new RSSPreferenceUI();;
   private RSSPreferences preferences = new RSSPreferences();

   private XStream xstream;

   public RSSPreference() {
      xstream = new XStream(new DomDriver());

      load();
   }
   
   public RSSPreferences getPreferences() {
       return preferences;
   }

   public String getTitle() {
      return "RSS Preferences";
   }

   public Icon getIcon() {
      String image = "feed-icon32x32.png";
      
      URL imgURL = RSSPreference.class.getResource(image);
      if (imgURL != null) {
         return new ImageIcon(imgURL);
      } else {
         Log.warning("Unable to find image: " + image);
         return null;
      }
   }

   public String getTooltip() {
      return "RSS Preferences";
   }

   public String getListName() {
      return "RSS";
   }

   public String getNamespace() {
      return "RSS";
   }

   public JComponent getGUI() {
      return ui;
   }

   public void load() {
      try {
         FileReader reader = new FileReader(getPreferencesFile());
         preferences = (RSSPreferences) xstream.fromXML(reader);
         
         ui.setRefreshInterval(preferences.getRefreshInterval());
         
         ui.setUseProxy(preferences.isUseProxy());
         ui.setProxyHost(preferences.getProxyHost());
         ui.setProxyPort(preferences.getProxyPort());
      } catch (Exception e) {
         Log.error(e);
      }
   }

   public void commit() {
      try {
         preferences.setRefreshInterval(ui.getRefreshInterval());
         
         preferences.setUseProxy(ui.useProxy());
         preferences.setProxyHost(ui.getProxyHost());
         preferences.setProxyPort(ui.getProxyPort());
         
         FileWriter writer = new FileWriter(getPreferencesFile());
         xstream.toXML(preferences, writer);
      } catch (Exception e) {
         Log.error(e);
      }
   }

   public boolean isDataValid() {
      return true;
   }

   public String getErrorMessage() {
      return null;
   }

   public Object getData() {
      return null;
   }

   public void shutdown() {
      commit();
   }

   private File getPreferencesFile() {
      File file = new File(Spark.getUserHome(), "Spark");
      if (!file.exists()) {
         file.mkdirs();
      }
      return new File(file, "rss-preferences.xml");
   }
}
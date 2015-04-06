/**
 * Copyright (C) 2006 Version 2 Software, LLC. All rights reserved.
 * 
 * This software is published under the terms of the GNU Public License (GPL).
 */

package com.version2software.spark.plugin.preferences;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.jivesoftware.spark.component.VerticalFlowLayout;

public class RSSPreferenceUI extends JPanel {
   private JTextField refreshIntervalField = new JTextField("5");
   
   private JCheckBox useProxyChkBox  = new JCheckBox("Use Proxy");
   private JTextField proxyHostField = new JTextField();
   private JTextField proxyPortField = new JTextField();

   public RSSPreferenceUI() {
      setLayout(new VerticalFlowLayout());
      add(buildRSSPanel());
      add(buildProxyPanel());     
   }

   private JPanel buildRSSPanel() {
      JPanel panel = new JPanel();
      panel.setBorder(BorderFactory.createTitledBorder("Feed Information"));
      panel.setLayout(new GridBagLayout());
      
      panel.add(new JLabel("Refresh interval (in minutes):"), new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
      panel.add(refreshIntervalField, new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 100, 0));
      
      return panel;
   }
   
   private JPanel buildProxyPanel() {
      JPanel panel = new JPanel();
      panel.setBorder(BorderFactory.createTitledBorder("Proxy Information"));
      panel.setLayout(new GridBagLayout());
      
      panel.add(useProxyChkBox, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
      
      panel.add(new JLabel("Proxy Host:"), new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
      panel.add(proxyHostField, new GridBagConstraints(1, 1, 1, 1, 1.0, 0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 200, 0));
      
      panel.add(new JLabel("Proxy Port:"), new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
      panel.add(proxyPortField, new GridBagConstraints(1, 2, 1, 1, 1.0, 0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 200, 0));
      
      return panel;
   }
   
   public void setRefreshInterval(String interval) {
      refreshIntervalField.setText(interval);
   }

   public String getRefreshInterval() {
      return refreshIntervalField.getText();
   }

   public boolean useProxy() {
      return useProxyChkBox.isSelected();
   }

   public void setUseProxy(boolean selected) {
      useProxyChkBox.setSelected(selected);
   }

   public void setProxyHost(String host) {
      proxyHostField.setText(host);
   }

   public String getProxyHost() {
      return proxyHostField.getText();
   }

   public void setProxyPort(String port) {
      proxyPortField.setText(port);
   }

   public String getProxyPort() {
      return proxyPortField.getText();
   }
}

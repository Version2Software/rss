/**
 * Copyright (C) 2006 Version 2 Software, LLC. All rights reserved.
 * 
 * This software is published under the terms of the GNU Public License (GPL).
 */

package com.version2software.spark.plugin;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Properties;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import org.jivesoftware.spark.SparkManager;
import org.jivesoftware.spark.component.VerticalFlowLayout;
import org.jivesoftware.spark.component.panes.CollapsiblePane;
import org.jivesoftware.spark.plugin.Plugin;
import org.jivesoftware.spark.util.BrowserLauncher;
import org.jivesoftware.spark.util.ModelUtil;
import org.jivesoftware.spark.util.log.Log;

import com.sun.syndication.feed.synd.SyndContentImpl;
import com.sun.syndication.feed.synd.SyndEntryImpl;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;
import com.version2software.spark.plugin.preferences.RSSPreference;
import com.version2software.spark.plugin.preferences.RSSPreferences;

public class RSSSparkplug implements Plugin {
   private static int refreshRate = 5;
   
   private RSSPreference preference = new RSSPreference();
   
   private RSSPaneContainer rssPane = new RSSPaneContainer();
   private JEditorPane htmlPane = new JEditorPane();
   private ScheduledExecutorService executor = new ScheduledThreadPoolExecutor(1);
   
   public void initialize() {
      configureProxy();
      configureExecutor();
      
      SparkManager.getPreferenceManager().addPreference(preference);
      SparkManager.getWorkspace().getWorkspacePane().addTab("RSS Feeds", getIcon(), buildRSSPanel());
   }
   
   public void shutdown() {
      preference = null;
      rssPane = null;
      htmlPane = null;
      
      if (executor != null) {
         executor.shutdown();
         executor = null;
     }
   }

   public boolean canShutDown() {
      return true;
   }

   public void uninstall() {
   }
   
   private void configureProxy() {
      RSSPreferences prefs = preference.getPreferences();
      if (prefs.isUseProxy()) {
         Properties systemSettings = System.getProperties();
         systemSettings.put("proxySet", "true");
         systemSettings.put("proxyHost", prefs.getProxyHost());
         systemSettings.put("proxyPort", prefs.getProxyPort());
         System.setProperties(systemSettings);
      }
   }
   
   private void configureExecutor() {
      try {
         int interval = Integer.valueOf(preference.getPreferences().getRefreshInterval());
         if (interval > 0 && interval != refreshRate) {
            refreshRate = interval;
            
            executor.shutdown();
            executor = null;
            executor = new ScheduledThreadPoolExecutor(1);
            executor.scheduleWithFixedDelay(new RSSMonitor(), 0, (refreshRate * 60), TimeUnit.SECONDS);
         }
      } catch (NumberFormatException nfe) {
         Log.error(nfe);
      }
   }
   
   private JPanel buildRSSPanel() {
      JPanel panel = new JPanel();
      
      panel.setLayout(new BorderLayout());
      panel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY));
      
      final JPopupMenu menu = new JPopupMenu();
      JMenuItem add = new JMenuItem("Add feed");
      
      //TODO add a dialog that allows the user to test the url
      add.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            String newURL = JOptionPane.showInputDialog(rssPane, "Enter new url:", "Edit feed", JOptionPane.QUESTION_MESSAGE);
            if (ModelUtil.hasLength(newURL)) {
                try {
                   SyndFeedInput input = new SyndFeedInput();
                   input.build(new XmlReader(new URL(newURL)));
                   
                   rssPane.add(new RSSTaskPane(new URL(newURL)));
                   rssPane.refresh();
               } catch (Exception e1) {
                  JOptionPane.showMessageDialog(rssPane, "Unable to verify the URL", "Error", JOptionPane.ERROR_MESSAGE);
               }
            }
         }
      });
      
      menu.add(add);
      rssPane.add(menu);
      
      rssPane.addMouseListener(new MouseAdapter() {
         public void mousePressed(MouseEvent e) {
            if (e.getButton() == MouseEvent.BUTTON3) {
               menu.show(rssPane, e.getX(), e.getY());
            }
         }

         public void mouseReleased(MouseEvent e) {
            //ignore
         }
      });
      
      JScrollPane rssScrollPane = new JScrollPane(rssPane, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
      JScrollPane htmlScrollPane = new JScrollPane(htmlPane, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
      htmlPane.setEditable(false);
      htmlPane.setContentType("text/html");
      htmlPane.addHyperlinkListener(new RSSHyperlinkListener());
      
      JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
      splitPane.setBorder(null);
      splitPane.setResizeWeight(1.0);
      splitPane.setDividerSize(2);
      splitPane.setTopComponent(rssScrollPane);
      splitPane.setBottomComponent(htmlScrollPane);

      Dimension minimumSize = new Dimension(100, 50);
      rssScrollPane.setMinimumSize(minimumSize);
      htmlScrollPane.setMinimumSize(minimumSize);
      splitPane.setDividerLocation(100);

      panel.add(splitPane);
      
      return panel;
   }
   
   private Icon getIcon() {
      String image = "feed-icon13x13.gif";
      
      URL imgURL = RSSSparkplug.class.getResource(image);
      if (imgURL != null) {
         return new ImageIcon(imgURL);
      } else {
         Log.warning("Unable to find image: " + image);
         return null;
      }
   }
   
   private void displayURL(FeedInfo feedInfo) {      
      htmlPane.setText(feedInfo.toHTML());
      htmlPane.setCaretPosition(0);
   }

   private class RSSTaskPane extends CollapsiblePane {
      private DefaultListModel model = new DefaultListModel();
      private JList feedList = new JList(model);
      private URL url;
      private JPopupMenu menu = new JPopupMenu();
      
      public RSSTaskPane(URL url) {
         super();
         
         this.url = url;
         
         setContentPane(feedList);
         feedList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
         feedList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
               FeedInfo fi = (FeedInfo) feedList.getSelectedValue();
               displayURL(fi);
            }
         });
         
         getTitlePane().addMouseListener(new RSSTaskPaneMouseListener());
                  
         JMenuItem deleteItem = new JMenuItem("Delete");
         deleteItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
               int selection = JOptionPane.showConfirmDialog(RSSTaskPane.this,
                     "Are you sure you want to delete this feed?",
                     "Delete?",
                     JOptionPane.YES_NO_OPTION);
               
               if (selection == JOptionPane.YES_OPTION) {
                  rssPane.remove(RSSTaskPane.this);
                  rssPane.validate();
                  rssPane.repaint();
               }
            }
         });
         
         menu.add(deleteItem);
         
         refresh();
      }
      
      //TODO improve logic to only refresh if the contents of the feeds have changed
      public void refresh() {
         try {
            SyndFeedInput input = new SyndFeedInput();
            SyndFeed feed = input.build(new XmlReader(url));

            setTitle(feed.getTitle());
            setCollapsed(isCollapsed());

            model.clear();

            Iterator entryIterator = feed.getEntries().iterator();
            while (entryIterator.hasNext()) {
               SyndEntryImpl element = (SyndEntryImpl) entryIterator.next();

               Iterator contentIterator = element.getContents().iterator();
               while (contentIterator.hasNext()) {
                  SyndContentImpl content = (SyndContentImpl) contentIterator.next();
                  FeedInfo fi = new FeedInfo(element.getTitle(), element.getLink(), content.getValue());
                  model.addElement(fi);
               }
            }
         } catch (Exception e) {
            Log.error(e);
         }
      }
      
      private class RSSTaskPaneMouseListener extends MouseAdapter {
         public void mousePressed(MouseEvent e) {
            if (e.getButton() == MouseEvent.BUTTON3) {
               menu.show(RSSTaskPane.this, e.getX(), e.getY());
            }
         }

         public void mouseReleased(MouseEvent e) {
            //ignore
         }
      }
   }
   
   private class RSSPaneContainer extends JPanel {
      public RSSPaneContainer() {
         setBackground(Color.white);
         setLayout(new VerticalFlowLayout(VerticalFlowLayout.TOP, 0, 0, true, false));
         
         for (String url : preference.getPreferences().getUrls()) {
            try {
               add(new RSSTaskPane(new URL(url)));
            } catch (Exception e){
               Log.error(e);
            }
         }
      }
      
      public void refresh() {
         ArrayList<String> urls = new ArrayList<String>();
         
         Component c[] = this.getComponents();
         for (int i = 0; i < c.length; i++) {
            Component component = c[i];
            if (component instanceof RSSTaskPane) {
               RSSTaskPane pane = (RSSTaskPane) component;
               pane.refresh();
               urls.add(pane.url.toExternalForm());
            }
         }
         
         preference.getPreferences().setUrls(urls);
         preference.commit();
      }
   }
   
   private class FeedInfo {
      public String title;
      public String url;
      public String content;

      public FeedInfo(String title, String url, String content) {
         this.title = title;
         this.url = url;
         this.content = content;
      }

      //TODO need to improve the handling of images, links, etc.
      public String toHTML() {
         return "<html><body><a href=\"" + url + "\">" + title + "</a><p>" + content + "</p></html></body>";
      }
      
      public String toString() {
         return title;
      }
   }
   
   private class RSSHyperlinkListener implements HyperlinkListener {
      public void hyperlinkUpdate(HyperlinkEvent e) {
         if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
            try {
               BrowserLauncher.openURL(e.getDescription());
            } catch (IOException ioe) {
               Log.error(ioe);
            }
         }
      }
   }
   
   private class RSSMonitor implements Runnable {
      public void run() {
         SwingUtilities.invokeLater(new Runnable() {
            public void run() {
               configureProxy();
               
               rssPane.refresh();
               rssPane.validate();
               rssPane.repaint();
               
               configureExecutor();
            }
         });
      }
   }
   
   private static void createAndShowGUI() {
      JFrame frame = new JFrame("RSS Demo");
      frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

      RSSSparkplug newContentPane = new RSSSparkplug();
      frame.setContentPane(newContentPane.buildRSSPanel());

      frame.pack();
      frame.setVisible(true);
   }

   public static void main(String[] args) {
      SwingUtilities.invokeLater(new Runnable() {
         public void run() {
            createAndShowGUI();
         }
      });
   }
}

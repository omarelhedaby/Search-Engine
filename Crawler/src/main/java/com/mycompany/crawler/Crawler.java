/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.crawler;
import com.mycompany.crawler.*;
import java.io.FileWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.net.URI;

/**
 *
 * @author mohab
 */
import com.google.gson.*;
import com.mycompany.crawler.WebPage.PageState;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Crawler {
    private int threadCount;
    private int pageCount;
    private transient int pageId;
    private int pagesBeforeStore;
    private SeedSet ss;
    private static String filename = "crawler.json";
    private static  String dirname = "downloads";
    private static String useragent = "KumokoBot/1.0";
    private transient RobotsStore rs; 
    private transient Object sslock;
    Crawler()
    {
        ss = new SeedSet();
        CreateDownloadsDir();
        rs = new RobotsStore(this);
        sslock = new Object();
    }
    
    private void CreateDownloadsDir()
    {
        File directory = new File(dirname);
        if (! directory.exists()){
            directory.mkdir();
        }
    }
    
    public void SetThreadCount(int th_cnt)
    {
        threadCount = th_cnt;
    }
    
    public int GetThreadCount()
    {
        return threadCount;
    }
    static Crawler LoadConfig() throws IOException
    {  
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        String json = new String(Files.readAllBytes(Paths.get(filename)));
        Crawler crawler = gson.fromJson(json, Crawler.class);
        return crawler;
    }
    
    void StartCrawling()
    {
        pageId = 0;//force pageId to reset,to recrawl failed/crawling pages
        ArrayList<Thread> threadList = new ArrayList<Thread>();
        for (int i = 0; i < threadCount; i++) {
            threadList.add(new Thread(new CrawlerThread(i, this)));
        }
        threadList.forEach((t) -> t.start());
        threadList.forEach((t) -> {
            try {
                t.join();
            } catch (InterruptedException ex) {
                //ignore for now ?
            }
        });
    }
    
    
    WebPage GetNextPage()
    {
        synchronized (sslock)
        {
            if (pageId % pagesBeforeStore == 0) {
                StoreConfig();
            }
            
            while(true)
            {
                System.out.println("Crawler getnextpage " + pageId);
                WebPage page = ss.GetPageAt(pageId);
                
                if(page == null)
                {
                    if (ss.Size() <= pageCount) {
                        try {
                            sslock.wait();
                        } catch (InterruptedException ex) {
                        }
                        continue;
                    }
                    else
                        return page;
                }
                pageId++;
                if(page.GetState() != PageState.DONE && page.GetState() != PageState.UNWANTED)
                    return page;
            }
        }
    }
    
    boolean NeedsURLs() {
        synchronized (sslock)
        {
            return ss.Size() <= pageCount;
        }
    }
    
    void AddURL(String URL)
    {        
        synchronized(sslock)
        {
            if (ss.PageExists(URL)) {
                return;
            }
        }
        URI normuri;
        try {
            normuri = new URI(URL);
        } catch (URISyntaxException ex) {
            System.out.println("Failed to parse url: " + URL);
            return;
        }
        
        
        StringBuilder base = new StringBuilder();

        {
            String scheme = normuri.getScheme();
            if(scheme == null)
                base.append("http://");
            else
                base.append(scheme+"://");
            String auth = normuri.getAuthority();
            if(auth == null)
                return;
            else
                base.append(auth);
        }
        String bstr = base.toString();
        String roboturl = base + "/robots.txt";
        Robots r;
        boolean shouldDownload = false;
        synchronized(rs)
        {
            r= rs.Exists(bstr);
            if(r==null)
            {
                shouldDownload = true;
            }
        }
        
        if (shouldDownload) {
            System.out.println("robots AddSite: " + bstr);
            Robots r2 = rs.DownloadAndParseSite(bstr, roboturl);
            
            if (r2 != null) {
                
                synchronized(rs)
                {
                    r= rs.Exists(bstr);
                    if (r==null) {
                       r=rs.AddRobots(r2);
                    }
                }
            }
        }
        
        if(r!=null && r.Allows(URL))
        {
            synchronized(sslock)
            {
                if (ss.PageExists(URL)) {
                    return;
                }
                ss.AddPage(URL);
                sslock.notifyAll();
            }
        }
    }
    
    boolean StoreConfig()
    {
        try {
            FileWriter myWriter = new FileWriter(filename, false);
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String json = gson.toJson(this);
            myWriter.write(json);
            myWriter.close();
            System.out.println("Crawler config write success.");
            return true;
        } catch (IOException e) {
            System.out.println("Crawler config write error:");
            e.printStackTrace();
            return false;
        }
    }
    
    public String GetUserAgent()
    {
        return useragent;
    }
    
    public String GetDownloadsDir()
    {
        return dirname;
    }
}

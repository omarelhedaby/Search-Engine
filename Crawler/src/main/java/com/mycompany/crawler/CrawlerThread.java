/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.crawler;
import com.mycompany.crawler.*;
import java.io.IOException;
import java.io.File;
import java.io.FileWriter;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.lang.Character;
import java.net.URISyntaxException;
import java.util.Optional;

/**
 *
 * @author mohab
 */
public class CrawlerThread implements Runnable{
    private int threadId;
    private Crawler crawler;
    public CrawlerThread(int num, Crawler pc)
    {
        threadId = num;
        crawler = pc;
    }
    
    private String NormalizeURL(String url)
    {
        StringBuilder burl = new StringBuilder(url);
        URINormalizer.Normalize(burl);
        
        return burl.toString();
        
    }
    
    private void ParsePage(String html, String url)
    {
            Document doc = Jsoup.parse(html, url);
            Elements links = doc.select("a[href]");
            for (Element link : links) {
                
                if(crawler.NeedsURLs())
                {
                    crawler.AddURL(NormalizeURL(link.attr("abs:href")));
                }
            }

    }
    
    
    private void CrawlPage(WebPage page)
    {
        HttpClient client = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.ALWAYS).build();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(page.GetURL()))
                .setHeader("User-Agent", crawler.GetUserAgent())
                .build();
        HttpResponse<String> response;
        try {
            response = client.send(request, BodyHandlers.ofString());
        } catch (IOException ex) {
            System.out.println("CrawlPage Thread " + threadId + " page " + page.GetURL() + " IO error:");
            ex.printStackTrace();
            page.SetState(WebPage.PageState.FAILED);
            return;
        } catch (InterruptedException ex) {
            System.out.println("CrawlPage Thread " + threadId + " page " + page.GetURL() + " interrupted error:");
            ex.printStackTrace();
            page.SetState(WebPage.PageState.FAILED);
            return;
        }

        
        if (response.statusCode() != 200) {
            System.out.println("CrawlPage Thread " + threadId + " page " + page.GetURL() + " error got response code: " + response.statusCode());
            page.SetState(WebPage.PageState.FAILED);
            return;
        }
        else
        {
            System.out.println("CrawlPage Thread " + threadId + " page " + page.GetURL() + " request sucess");
        }
        Optional<String> content_type = response.headers().firstValue("Content-Type");//text/html
        if (content_type.isEmpty()) {
            System.out.println("CrawlPage Thread " + threadId + " page " + page.GetURL() + " empty content_type");
            page.SetState(WebPage.PageState.UNWANTED);
            return;
        }
        if (!content_type.get().contains("text/html")) {
            System.out.println("CrawlPage Thread " + threadId + " page " + page.GetURL() + " unwanted content_type");
            page.SetState(WebPage.PageState.UNWANTED);
            return;
        }
        
        String body = response.body();
        ParsePage(body, page.GetURL());
        File dir = new File(crawler.GetDownloadsDir());
        File f;
        try {
            f = File.createTempFile("crawlout", ".html", dir);
            FileWriter myWriter = new FileWriter(f, false);
            myWriter.write(body);
            myWriter.close();
            page.SetState(WebPage.PageState.DONE);
            
            page.SetFileName(f.getName());
            System.out.println("CrawlPage Thread " + threadId + " page " + page.GetURL() + " crawled successfully.");
        } catch (IOException ex) {
            System.out.println("CrawlPage Thread " + threadId + " page " + page.GetURL() + " file save error:");
            ex.printStackTrace();
            page.SetState(WebPage.PageState.FAILED);
            return;
        }
        
        
    }
    
    public void run()
    {
        System.out.println("Thread " + threadId + " starting.");
        WebPage page;
        while((page = crawler.GetNextPage()) != null)
        {
            CrawlPage(page);
            page = null;
        }
        System.out.println("Thread " + threadId + " finished.");
        
    }
    
    public int GetID()
    {
        return threadId;
    }
}

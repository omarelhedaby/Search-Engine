/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.crawler;
import com.mycompany.crawler.Robots;
import java.io.IOException;
import java.util.ArrayList;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.net.URI;
import java.net.http.HttpClient.Redirect;
import java.time.Duration;
/**
 *
 * @author mohab
 */
public class RobotsStore {
    private ArrayList<Robots> sites;
    private Crawler crawler;
    
    RobotsStore(Crawler cr)
    {
        crawler = cr;
        sites = new ArrayList<Robots>();
    }
    
    private Robots Add(String url, String robotfile)
    {
        String lines[] = robotfile.split("\\r?\\n");
        int index = -1;
        for (int i = 0; i < lines.length; i++) {
            if(lines[i].startsWith("User-agent: *"))
            {
                index = i;
                break;
            }
        }
        
        
        Robots entry = new Robots(url);
        
        if(index != -1)
        {
            for (int i = index+1; i < lines.length; i++) {
                String rule = lines[i];
                String[] split = rule.split(" ");
                if (split.length < 2) {
                    continue;
                }
                if (split[0].equals("User-agent:")) {
                    break;
                }
                else if(split[0].equals("Allow:"))
                {
                    entry.AddAllows(split[1]);
                }
                else if(split[0].equals("Disallow:"))
                {
                    entry.AddDisallows(split[1]);
                }
            }
        }
        System.out.println("Add robots: " + url + " Allows: " + entry.NumAllows() + " Disallows: " + entry.NumDisallows());
        return entry;
    }
    
    Robots Exists(String site)
    {
        for (int i = 0; i < sites.size(); i++) {
            Robots r = sites.get(i);
            if (r.SiteMatches(site)) {
                return r;
            }
        }
        return null;
    }
    
    Robots DownloadAndParseSite(String url, String robotsurl)
    {
        HttpClient client = HttpClient.newBuilder().followRedirects(Redirect.ALWAYS).build();
        HttpRequest request = HttpRequest.newBuilder()
                .timeout(Duration.ofMillis(3000))
                .uri(URI.create(robotsurl))
                .setHeader("User-Agent", crawler.GetUserAgent())
                
                .build();
        HttpResponse<String> response;
        try {
            response = client.send(request, BodyHandlers.ofString());
        } catch (IOException ex) {
            System.out.println("RobotsStore page " + url + " IO error:");
            ex.printStackTrace();
            return null;
        } catch (InterruptedException ex) {
            System.out.println("RobotsStore page " + url + " interrupted error:");
            ex.printStackTrace();
            return null;
        }

        
        if (response.statusCode() != 200) {
            System.out.println("RobotsStore page " + url + " error got response code: " + response.statusCode());
            return Add(url, "");//ignore error
        }
        else
        {           
            System.out.println("RobotsStore page " + url + " got request success.");
        }
        String body = response.body();
        
        return Add(url, body);
    }
    
    Robots AddRobots(Robots r)
    {
        sites.add(r);
        return r;
    }
    
}

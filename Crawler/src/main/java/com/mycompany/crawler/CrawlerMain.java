/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.crawler;
import com.mycompany.crawler.*;
import java.io.IOException;

/**
 *
 * @author mohab
 */
public class CrawlerMain {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
        
            
        Crawler crawler;
        try
        {
            crawler = Crawler.LoadConfig();
            System.out.println("Crawler config read success.");
        }
        catch (IOException e)
        {
            System.out.println("Crawler config read error:");
            e.printStackTrace();
            return;
        }
        crawler.StartCrawling();
        crawler.StoreConfig();
    }

    private static void TestNormalizer() {
        StringBuilder url = new StringBuilder("http://example.com/foo%2a");
        System.out.print("Url before: " + url.toString());
        boolean status = URINormalizer.Normalize(url);
        System.out.print(" url after: " + url.toString());
        System.out.println(" status: " + status);

        url = new StringBuilder("HTTP://User@Example.COM/Foo");
        System.out.print("Url before: " + url.toString());
        status = URINormalizer.Normalize(url);
        System.out.print(" url after: " + url.toString());
        System.out.println(" status: " + status);

        url = new StringBuilder("http://example.com/%7Efoo");
        System.out.print("Url before: " + url.toString());
        status = URINormalizer.Normalize(url);
        System.out.print(" url after: " + url.toString());
        System.out.println(" status: " + status);

        url = new StringBuilder("http://example.com/foo/./bar/baz/../qux");
        System.out.print("Url before: " + url.toString());
        status = URINormalizer.Normalize(url);
        System.out.print(" url after: " + url.toString());
        System.out.println(" status: " + status);

        url = new StringBuilder("http://example.com");
        System.out.print("Url before: " + url.toString());
        status = URINormalizer.Normalize(url);
        System.out.print(" url after: " + url.toString());
        System.out.println(" status: " + status);

        url = new StringBuilder("http://example.com:80/");
        System.out.print("Url before: " + url.toString());
        status = URINormalizer.Normalize(url);
        System.out.print(" url after: " + url.toString());
        System.out.println(" status: " + status);

    }
    
}

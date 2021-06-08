/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.crawler;
import com.mycompany.crawler.WebPage;
import java.util.ArrayList;
/**
 *
 * @author mohab
 */
public class SeedSet {
    private ArrayList<WebPage> weblist;
    SeedSet()
    {
        weblist = new ArrayList<WebPage>();
    }
    
    public void AddPage(String url)
    {
        WebPage page = new WebPage(url);
        //todo: check robots or already exists somewhere.
        weblist.add(page);
    }
    
    public boolean PageExists(String url)
    {
        for (int i = 0; i < weblist.size(); i++) {
            WebPage page = weblist.get(i);
            if (page.GetURL().equals(url)) {
                return true;
            }
        }
        return false;
    }
    public WebPage GetPageAt(int id)
    {
        if(id < weblist.size())
            return weblist.get(id);
        else
            return null;
    }
    
    public int Size()
    {
        return weblist.size();
    }
    
}

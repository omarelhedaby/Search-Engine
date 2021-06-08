/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.crawler;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

/**
 *
 * @author mohab
 */
public class Robots {
    private String site;
    private ArrayList<String> Allows;
    private ArrayList<String> Disallows;
    
    Robots(String st)
    {
        site = st;
        Allows = new ArrayList<String>();
        Disallows = new ArrayList<String>();
    }
    
    void AddAllows(String a)
    {
        if (!Allows.contains(a)) {
            Allows.add(a);
        }
    }
    
    
    void AddDisallows(String d)
    {
        if (!Disallows.contains(d)) {
            Disallows.add(d);
        }
    }
    
    boolean Allows(String a)
    {
        URI normuri;
        try {
            normuri = new URI(a);
            normuri.normalize();
        } catch (URISyntaxException ex) {
            return false;
        }
        
        String path = normuri.getPath();
        if(path == null)
            path = "/";
        
        for (int i = 0; i < Disallows.size(); i++) {
            if (path.startsWith(Disallows.get(i))) {
                return false;
            }
        }
        
        return true;
    }
    
    boolean Disallows(String d)
    {
        //Todo
        return false;
    }
    
    boolean SiteMatches(String url)
    {
        return url.equalsIgnoreCase(site);
    }
    
    int NumAllows()
    {
        return Allows.size();
    }
    
    int NumDisallows()
    {
        return Disallows.size();
    }
}

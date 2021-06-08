/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.crawler;

import java.net.URI;
import java.net.URISyntaxException;

/**
 *
 * @author mohab
 */
public class URINormalizer {
    
    private static void TripletsToUpper(StringBuilder str)
    {
        for (int i = 0; i <= str.length()-3; i++) {
            if (str.charAt(i) == '%') {
                char n1 = Character.toUpperCase(str.charAt(i+1));
                char n2 = Character.toUpperCase(str.charAt(i+2));
                str.setCharAt(i+1, n1);
                str.setCharAt(i+2, n2);
                i=i+2;
            }
        }
    }
    
    private static void DecodeTriplets(StringBuilder str)
    {
        
        for (int i = 0; i <= str.length()-3; i++) {
            if (str.charAt(i) == '%') {
                String hex = str.substring(i+1, i+3);
                int value = Integer.parseInt(hex,16);
                if ((value >= 'a' && value <= 'z' )
                        || (value >= 'A' && value <= 'Z' )
                        || (value >= '0' && value <= '9')
                        || value == '-'
                        || value == '.'
                        || value == '_'
                        || value == '~')
                {
                    str.replace(i, i+3, Character.toString((char)value));
                    continue;
                }
                
                i=i+2;
            }
        }
    }
    
    private static void RemoveDots(StringBuilder str)
    {
        //https://datatracker.ietf.org/doc/html/rfc3986#section-5.2.4
        StringBuilder input = new StringBuilder(str);
        StringBuilder output = new StringBuilder();
        while(!input.isEmpty())
        {
            String in = input.toString();
            String out = output.toString();
            if (in.startsWith("../")) {
                input.delete(0, 3);
                continue;
            }
            
            if (in.startsWith("./")) {
                input.delete(0, 2);
                continue;
            }
            
            if (in.startsWith("/./")) {
                input.replace(0, 3, "/");
                continue;
            }
            
            if (in.equals("/.")) {
                input.replace(0, 2, "/");
                continue;
            }
            
            if (in.startsWith("/../")) {
                input.replace(0, 4, "/");
                int i = out.lastIndexOf("/");
                if (i == -1) {
                    output.setLength(0);
                    output.append("/");
                }
                else
                {
                    output.delete(i, out.length());
                }
                continue;
            }
            
            if (in.startsWith("/..")) {
                input.replace(0, 3, "/");
                int i = out.lastIndexOf("/");
                if (i == -1) {
                    output.setLength(0);
                    output.append("/");
                }
                else
                {
                    output.delete(i, out.length());
                }
                continue;
            }
            
            if (in.equals(".") || in.equals("..")) {
                input.setLength(0);
                continue;
            }
            
            int i = in.indexOf("/", 1);
            
            if (i == -1) {
                //only 1 path segment, move from input to output
                output.append(in);
                input.setLength(0);
            }
            else
            {
                //move path segment input[0...i-1] to output
                output.append(in.substring(0, i));
                input.delete(0, i);
            }
        }
        
        str.setLength(0);
        str.append(output.toString());
    }
    public static boolean Normalize(StringBuilder url)
    {
        URI normuri;
        try {
            normuri = new URI(url.toString());
            normuri = normuri.normalize();
        } catch (URISyntaxException ex) {
            //System.out.println("CrawlPage Thread " + threadId + " normalize failed:  " + url);
            return false;
        }
        
        String npath = normuri.getPath();
        StringBuilder path = new StringBuilder(npath == null ? "" : npath);
        
        String nquery = normuri.getQuery();
        StringBuilder query = new StringBuilder(nquery == null ? "" : nquery);
        
        String nfragment = normuri.getFragment();
        StringBuilder fragment = new StringBuilder(nfragment == null ? "" : nfragment);
        
        String nscheme = normuri.getScheme();
        StringBuilder scheme = new StringBuilder(nscheme == null ? "" : nscheme);
        
        String nhost = normuri.getHost();
        StringBuilder host = new StringBuilder(nhost == null ? "" : nhost);
        
        String nuserinfo = normuri.getUserInfo();
        StringBuilder userinfo = new StringBuilder(nuserinfo == null ? "" : nuserinfo);
        
        int port = normuri.getPort();
        
        //System.out.println("CrawlPage Thread " + threadId + " normalize " + url);
        
        //1: replace lower case %2a to upper case %2A
        //path:
        TripletsToUpper(path);
        //query:
        TripletsToUpper(query);
        //fragment:
        TripletsToUpper(fragment);
        
        //2: scheme and host to lowercase
        scheme = new StringBuilder(scheme.toString().toLowerCase());
        host = new StringBuilder(host.toString().toLowerCase());
        
        //3: decode triplets
        //already happens inside uri internally
        //path:
        //DecodeTriplets(path);
        //query:
        //DecodeTriplets(query);
        //fragment:
        //DecodeTriplets(fragment);

        //4: dot segments
        RemoveDots(path);
        //already done in normuri.normalize();
        
        //5: convert empty path to "/"
        
        if(path.isEmpty())
            path.append("/");
        
        //6: remove default port
        //already happens when building new uri)
        
        StringBuilder tmp = new StringBuilder();
        
        if (!scheme.isEmpty()) {
            tmp.append(scheme.toString());
            tmp.append("://");
        }
        
        if (!userinfo.isEmpty()) {
            tmp.append(userinfo.toString());
            tmp.append("@");
        }
        
        tmp.append(host.toString());
        
        if (port >= 0 && ((scheme.toString().equals("http") && port != 80) || (scheme.toString().equals("https") && port != 443))) {
            tmp.append(":");
            tmp.append(Integer.toString(port));
        }
        
        if(!path.isEmpty())
        {
            tmp.append(path.toString());
        }
        
        if(!query.isEmpty())
        {
            tmp.append("?");
            tmp.append(query.toString());
        }
        
        //ignore fragment for now
        /*if(!fragment.isEmpty())
        {
            tmp.append("#");
            tmp.append(fragment.toString());
        }*/
        
        url.setLength(0);
        url.append(tmp.toString());        
        return true;
    }
}

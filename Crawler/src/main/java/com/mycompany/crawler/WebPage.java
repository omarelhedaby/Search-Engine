/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.crawler;

/**
 *
 * @author mohab
 */
public class WebPage {
    enum PageState
    {
      QUEUED,
      CRAWLING,
      DONE,
      FAILED,
      UNWANTED
    };
    
    private String URL;
    private PageState state;
    private String filename;
    
    WebPage(String sUrl)
    {
        URL = sUrl;
        filename = "";
        SetState(PageState.QUEUED);
    }
    
    PageState GetState()
    {
        return state;
    }
    
    void SetState(PageState st)
    {
        state = st;
    }
    
    String GetURL()
    {
        return URL;
    }
    
    void SetFileName(String name)
    {
        filename = name;
    }
}

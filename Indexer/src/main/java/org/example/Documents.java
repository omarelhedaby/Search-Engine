package org.example;

public class Documents {
    String url ;
    int IDF;
    String Importance;

    Documents(String url, int IDF,String importance)//, String Importance)
    {
        this.IDF =IDF;
        this.url = url;
        this.Importance = Importance;
    }

}

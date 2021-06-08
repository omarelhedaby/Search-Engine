import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import opennlp.tools.stemmer.snowball.SnowballStemmer;
import org.bson.Document;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import javax.print.Doc;
import javax.servlet.*;
import javax.servlet.*;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.in;
import static opennlp.tools.stemmer.snowball.SnowballStemmer.ALGORITHM.ENGLISH;

public class SearchHandler extends HttpServlet {

    public String getRespose(String search,int page)
    {
        MongoClient mongoClient;
        MongoDatabase database;
        MongoCollection<Document> collection;
        mongoClient = new MongoClient( "localhost" , 27017 );
        database = mongoClient.getDatabase("search-engine");
        collection = database.getCollection("indexer");

        SnowballStemmer stemmer = new SnowballStemmer(ENGLISH);


        search = search.replaceAll(",|\\.|!|\\?|:|;|\\)|\\(|\\[|]|\\*&\\^%\\$|\"|\'", "");
        search = search.replaceAll("/|\\\\|-", "");
        search=search.toLowerCase();


        String wordArr[];


        wordArr = search.split(" ");

        // IndexerModel indexerModel;

        //finding freq of all word in a doc
        HashMap<String,String> hashmap = new HashMap<String,String>();

        //use for loop to pull the elements of array to hashmap's key
        for (int j = 0; j < wordArr.length; j++) {
            if(!StopppingWords.containsStoppingWords(wordArr[j]))
                hashmap.put(wordArr[j], "");
        }
        // use hashmap.keySet() for printing all keys of hashmap using it's keySet() method
        HashMap<String, Document> links= new HashMap<String ,Document>();
        System.out.println(hashmap.keySet());
        for (String word : hashmap.keySet())
        {
            String stemmed = (String)stemmer.stem(word);
            Document doc = collection.find(eq("stem",stemmed)).first();
            if(doc!=null) {
                ArrayList<Document> arrDoc=(ArrayList<Document>)doc.get("documents");
                for(int i=0;i<arrDoc.size();i++)
                {
                    links.put(arrDoc.get(i).get("url").toString(),arrDoc.get(i));
                }
            }
        }
        int availableLinksSize=links.values().size();
        String JSON="{\"total\":"+availableLinksSize+",\"results\":[";
        if((page-1)*10>availableLinksSize)
        {
            mongoClient.close();
            return JSON+"]}";
        }

        System.out.println("Available Links are "+links.values().size());
        int startIndex=(page-1)*10;
        int endIndex=startIndex+((availableLinksSize-((page-1)*10)>10)?9:availableLinksSize-((page-1)*10)-1);
        for(int index = startIndex ;index<=endIndex;index++)
        {
            Document doc = (Document)links.values().toArray()[index];
            JSON+="{";
            JSON+="\"title\":\""+doc.get("title").toString().replaceAll("\n","").replaceAll("\"","\'")+"\",";
            JSON+="\"link\":\""+doc.get("url").toString()+"\",";
            JSON+="\"description\":\""+doc.get("description").toString()+"\"";
            JSON+="}";
            if(index<endIndex)
            {
                JSON += ",";
            }
        }
        JSON+="]"+"}";
        mongoClient.close();
        return JSON;
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        String search = request.getParameter("Search");
        int page = Integer.parseInt(request.getParameter("Page"));
        response.setContentType("application/json");
        response.getWriter().println(getRespose(search,page));

    }
}

package org.example;



import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import opennlp.tools.stemmer.snowball.SnowballStemmer;
import static opennlp.tools.stemmer.snowball.SnowballStemmer.ALGORITHM.ENGLISH;


import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import com.mongodb.client.model.Updates;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.jsoup.Connection;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.bson.Document;


import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import com.google.gson.*;


import static java.util.Arrays.asList;
import static org.example.CrawlerData.PageState.DONE;

public class App {
    //private String Words[];
    public static void main(String[] args) throws IOException {

        long start = System.currentTimeMillis();


        System.out.println("Hello MongoDB!");
        MongoClient mongoClient = MongoClients.create("mongodb://localhost");

        MongoDatabase indexerDB = mongoClient.getDatabase("IndexerDB");
        //gonna drop db for a while for testing
        indexerDB.drop();
        MongoCollection<Document> indexerCollection = indexerDB.getCollection("Indexer");
        Document index;


        ArrayList<String> adhtmls = new ArrayList<String>();
        ArrayList<String> aurls = new ArrayList<String>();

        getCrawlerData(adhtmls,aurls);

        String[] documentHTMLs =  adhtmls.toArray(String[]::new);
        String[] urls = aurls.toArray(String[]::new);


        int docsize =0;
        String description = "";

        for (int currDoc = 0; currDoc < documentHTMLs.length; currDoc++) {



        try {

            String str = String.format("C:\\Users\\teray\\Desktop\\crawler\\downloads\\%s", documentHTMLs[currDoc]);

            File input = new File(str);

            org.jsoup.nodes.Document docyy = Jsoup.parse(input, "UTF-8");
            System.out.println(currDoc);


            // using simple for-loop
            for (int i = 0; i < 200; i++) {

                if ((docyy.body().text().length() <= i))
                    break;
                description = description + docyy.body().text().charAt(i);
            }
            // System.out.print(description);


            String htmlWords = docyy.body().text();


            SnowballStemmer stemmer = new SnowballStemmer(ENGLISH);

            htmlWords = htmlWords.replaceAll(",|\\.|!|\\?|:|;|\\)|\\(|\\[|]|\\*&\\^%\\$|\"|\'", "");
            htmlWords = htmlWords.replaceAll("/|\\\\|-", "");


            String wordArr[];


            wordArr = htmlWords.split(" ");

            docsize = docsize + htmlWords.length();

            if(htmlWords.length()<=1)
                continue;


            // IndexerModel indexerModel;
            long[] freqNum = new long[htmlWords.length()];

            //finding freq of all word in a doc
            var freqOfWords = Arrays.stream(wordArr)
                    .collect(Collectors.groupingBy(String::toLowerCase, Collectors.counting()));

            String[] newArrWord = new String[freqOfWords.size()];

            int newSize = 0;

            for (Map.Entry<String, Long> pair : freqOfWords.entrySet()) {
                if (!StopppingWords.containsStoppingWords(pair.getKey())) {
                    freqNum[newSize] = pair.getValue();
                    newArrWord[newSize] = pair.getKey();
                    newSize++;
                }
            }

//        Documents docty = new Documents(urls[currDoc], 0);
//
//        List<Documents> docsy = new ArrayList<Documents>();
//        docsy.add(docty);
            Double TF = 0d;
            Double IDF = 0d;

            long count = 0;

            for (int i = 0; i < newSize; i++) {
                //  int idfCount=1;
                IDF = Math.log(documentHTMLs.length);
                if (currDoc != 0) {
                    count = indexerCollection.count(new Document("index", newArrWord[i]));

                }
                if (count > 0 && currDoc != 0) {
                    TF = (double) freqNum[i] / wordArr.length;
                    Document found = indexerCollection.find(new Document("index", newArrWord[i])).first();
                    Document found_got = indexerCollection.find(new Document("index", newArrWord[i])).first();
                    Document newE = new Document("url", urls[currDoc]).append("TF", TF)
                            .append("title", docyy.head().text()).append("description", description);
                    ArrayList<Document> arrDoc = (ArrayList<Document>) found.get("documents");
                    arrDoc.add(newE);
                    Bson data = new Document("documents", arrDoc);
                    Bson query = new Document("$set", data);
                    indexerCollection.updateOne(found_got, query);
                    count = 0;

                    found = indexerCollection.find(new Document("index", newArrWord[i])).first();
                    IDF = Math.log((double) documentHTMLs.length / arrDoc.size());

                    data = new Document("IDF", IDF).append("stem", (String) stemmer.stem(newArrWord[i]));
                    query = new Document("$set", data);

                    indexerCollection.updateOne(found, query);
                } else {
                    TF = (double) freqNum[i] / wordArr.length;


                    index = new Document("_id", new ObjectId());
                    index.append("index", newArrWord[i])
                            .append("indexId", i)
                            .append("IDF", IDF)
                            .append("stem", (String) stemmer.stem(newArrWord[i]))
                            .append("documents", asList(new Document("url", urls[currDoc])
                                    .append("TF", TF).append("title", docyy.head().text())
                                    .append("description", description)
                            ));

                    indexerCollection.insertOne(index);
                }

            }
            description = "";

        }
        catch (IOException e)
        {
            continue;
        }
    }

        long end = System.currentTimeMillis();
        long elapsedTime = end - start;
        System.out.println(elapsedTime/1000.0);

    }

    public static void getCrawlerData(ArrayList<String> htmls, ArrayList<String> urls)
    {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        String json = null;
        try {
            json = new String(Files.readAllBytes(Paths.get("C:\\Users\\teray\\Desktop\\crawler\\crawler.json")));
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        CrawlerOut crawler = gson.fromJson(json, CrawlerOut.class);
        for(var x : crawler.ss.weblist)
        {
            if(x.state == DONE)
            {
                urls.add(x.URL);
                htmls.add(x.filename);
            }
        }
    }
}

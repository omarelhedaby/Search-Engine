import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.util.ArrayList;
import java.util.Comparator;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Updates.set;

public class AutoComplete {

    MongoClient mongoClient;
    MongoDatabase database;
    MongoCollection<Document> collection;
    AutoComplete()
    {
        mongoClient = new MongoClient( "localhost" , 27017 );
        database = mongoClient.getDatabase("search-engine");
        collection = database.getCollection("suggestions");
    }

    public void postSuggestion(String suggestion)
    {

        suggestion=suggestion.toLowerCase();
        Document doc = collection.find(eq("suggestion",suggestion)).first();
        if(doc!=null)
        {
            int score = Integer.parseInt(doc.get("score").toString());
            score++;
            collection.updateOne(eq("suggestion",suggestion),set("score",score));
        }
        else
        {
            collection.insertOne(new Document("suggestion",suggestion).append("score",1));
        }
        mongoClient.close();
    }

    public String getSuggestions(String search,int maxSuggestions)
    {
        String JSON = "{\"suggestions\":[";
        if(search.length()==0)
        {
            mongoClient.close();
            return JSON+="]}";
        }
        MongoClient mongoClient;
        MongoDatabase database;
        MongoCollection<Document> collection;
        mongoClient = new MongoClient( "localhost" , 27017 );
        database = mongoClient.getDatabase("search-engine");
        collection = database.getCollection("suggestions");
        MongoCursor<Document> cursor = collection.find().iterator();
        ArrayList<Suggestion> sugArr = new ArrayList<Suggestion>();
        while(cursor.hasNext())
        {
            Document doc = cursor.next();
            String sug = doc.get("suggestion").toString();
            if(sug.length()>=search.length())
            {
                if (sug.substring(0, search.length()).equals(search))
                {
                    sugArr.add(new Suggestion(sug,doc.getInteger("score")));
                }
            }
        }
        if(sugArr.isEmpty())
        {
            mongoClient.close();
            return JSON+="]}";
        }

        sugArr.sort(Comparator.comparing(Suggestion::getScore));
        int endIndex=((sugArr.size()<=maxSuggestions)?0:sugArr.size()-maxSuggestions);
        for(int i=sugArr.size()-1;i>=endIndex;i--)
        {
            JSON+="{";
            JSON+="\"suggestion\":\""+sugArr.get(i).getText()+"\",";
            JSON+="\"score\":"+sugArr.get(i).getScore();
            JSON+="}";
            if(i!=endIndex)
            {
                JSON+=",";
            }
        }
        mongoClient.close();
        return JSON+="]}";
    }
}
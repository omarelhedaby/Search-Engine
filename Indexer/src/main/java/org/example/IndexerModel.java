package org.example;

public class IndexerModel {

    private String indexerWord;
    private long IF;
    private Documents[] doc;

    public IndexerModel(String indexerWord, long IF,Documents[] doc)
    {
        this.indexerWord = indexerWord;
        this.doc = doc;
        this.IF=IF;
    }

}

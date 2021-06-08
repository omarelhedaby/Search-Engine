package org.example;


class CrawlerData {
        enum PageState
        {
            QUEUED,
            CRAWLING,
            DONE,
            FAILED,
            UNWANTED
        };

        public String URL;
        public PageState state;
        public String filename;


}

Distributed web crawler

author: Haoyun Qiu

== Mercator-based design. Master/slave structure. 

== Worker: 
    Components:
    ==Http Fetcher: For fetching files from web, can handle:
        various of exceptions, Robots cache, timeout, content-length, content-type
        content-encoding(gzip, deflate), redirect and max-depth, cookie manager.
    ==Host Splitter: Each url will be hashed according to its host, if this url is not belongs to this worker,
        it will batch them for others. 
    ==URL Frontier: Contain URLs to be crawled, handle politeness and url priority. Include: 
            ==HostHeap: Maintain politeness. 
            ==Frontend queue: 
                Similarity based url queue, which maintain two queues, a hot queue for hot pages and another normal-queue. 
                When some backend queue becomes empty, it will first look for new url from hot queue, 
                then look for it in normal queue when hot queue is empty.
            A hot page: when url and its title contains anchor words, or its content contains over 10 anchor words, then we define it
            a hot page. Then all its sub-links will have a 20% chance to become a hot page and enqueue to hot queue. 
            ==Backend queue: Each active host map a backend queue for crawling urls. 
            ==Host-Queue Map: A mapping between a host and its backend queue. 
    ==Link Extractor:
        Handle various of corner cases: relative or absolute path,  /, ///, http:///, javascript:void(0), mailto:
    ==URL Filter:
        cgi-bin and other links we don’t want
    ==Content-seen: for removing duplicated-content web page(Rabin footprint algorithm for reducing memory footprint) 
    ==DUE: for removing duplicated urls(also use Rabin hash) 
    ==Servlet for nodes' communication

Extra credits:
    ==Duplicated content checked:
        ==Content-seen component: use Rabin footprint algorithm to hash the page content and cache it for duplicated content checked. 
        ==Canonical tag: check html's cannonical tag to locate the original page, drop it if canonical crawled before and only store the canonical url if new. 
        (From our over 1 million crawled pages, about half of pages could be removed by that.) 

    ==Similarity Based Algorithm: 
        Remain another hot url queue for high-correlation web-page as mentioned before.
        Reference: Junghoo Cho, Hector Garcia-Molina, Lawrence Page, Efficient Crawling Through URL Ordering

Source Structure:
    Crawler worker: ./crawler/src/com/myapp/
        ==worker: each crawler worker's components
        ==utils: some utility model
        ==servlet: servlet for node communication
        ==db: BDB component
        ==test: JUnit test
    Crawler master: ./crawler_master/src/com/myapp/
        ==master: all component
        ==test: JUnit test

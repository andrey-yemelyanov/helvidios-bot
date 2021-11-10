![example workflow](https://github.com/andrey-yemelyanov/helvidios-bot/actions/workflows/maven.yml/badge.svg)

# helvidios-bot

A more sophisticated multithreaded Web crawler with proper use of concurrent features in Java. Supports rate limiting and retries for HTTP requests. Number of threads is ten times the number of cores on the host machine CPU. Ten threads per core provide a good balance between parallel CPU-intensive parsing of HTML pages for URLs and highly IO-intensive downloading of web pages. Running concurrently on a single core, 9/10 threads will be downloading web pages while the single remaining thread will be parsing page content, thus fully utilizing the core resources.

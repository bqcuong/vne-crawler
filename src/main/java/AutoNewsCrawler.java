import cn.edu.hfut.dmic.webcollector.model.CrawlDatums;
import cn.edu.hfut.dmic.webcollector.model.Page;
import cn.edu.hfut.dmic.webcollector.net.HttpRequest;
import cn.edu.hfut.dmic.webcollector.net.HttpResponse;
import cn.edu.hfut.dmic.webcollector.plugin.berkeley.BreadthCrawler;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AutoNewsCrawler extends BreadthCrawler {

    private static Pattern TIME_PATTERN = Pattern.compile("\\d+/\\d+/\\d+\\s\\|\\s\\d+:\\d+");
    private static Pattern ARTICLE_ID_PATTERN = Pattern.compile("\"article_id\":\"(\\d+)\"");
    private static Pattern ARTICLE_TYPE_PATTERN = Pattern.compile("\"article_type\":\"(\\d+)\"");
    private static Pattern TOTAL_ITEM_PATTERN = Pattern.compile("\"totalitem\":(\\d+)"); // num of comments

    private static DateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy | hh:mm");
    private static final String GET_COMMENT_API =
            "https://usi-saas.vnexpress.net/index/get?offset=0&limit=1&&objectid=%s&objecttype=%s&siteid=1000000";

    private String categoryUrl;


    public AutoNewsCrawler(String categoryUrl, String crawlPath, boolean autoParse) {
        super(crawlPath, autoParse);
        this.categoryUrl = categoryUrl;

        // start page
        this.addSeed(categoryUrl + "/page/1.html");

        // fetch url like
        this.addRegex(categoryUrl + "/.*\\.html");

        // do not fetch jpg|png|gif & url contains #
        this.addRegex("-.*\\.(jpg|png|gif).*");
        this.addRegex("-.*#.*");

    }

    @Override
    public void visit(Page page, CrawlDatums next) {
        String url = page.url();
        // if page is news page
        if (page.matchUrl(categoryUrl + "/.*\\.html") && !page.matchUrl(categoryUrl + "/page/\\d+\\.html")) {
            try {
                // extract data
                String timeStr = page.selectText("span.time.left");
                Date time = getTime(timeStr);
                String title = page.selectText("h1");
                String description = page.selectText("h2");
                List<String> tags = page.select("a.tag_item").eachText();
                String commentBox = page.select("div#box_comment_vne").attr("data-component-input");
                Integer commentCount = getCommentCount(commentBox);

                // TODO: get number of likes
                // TODO: save to database or json

                System.out.println("url: " + url);
                System.out.println("time: " + time);
                System.out.println("title: " + title);
                System.out.println("description: " + description);
                System.out.println("tags: " + tags);
                System.out.println("comment count: " + commentCount);

            }
            catch (ParseException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static Date getTime(String rawText) throws ParseException {
        Matcher matcher = TIME_PATTERN.matcher(rawText);
        return matcher.find() ? DATE_FORMAT.parse(matcher.group()) : null;
    }

    private static Integer getCommentCount(String commentParams) throws Exception {
        // get objectId & objectType from "data-component-input" attribute of comment box
        Matcher idMatcher = ARTICLE_ID_PATTERN.matcher(commentParams);
        Matcher typeMatcher = ARTICLE_TYPE_PATTERN.matcher(commentParams);

        String objectId = null;
        String objectType = null;
        if (idMatcher.find()) objectId = idMatcher.group(1);
        if (typeMatcher.find()) objectType = typeMatcher.group(1);
        if (objectId == null || objectType == null) return 0;

        // request to api and get result
        HttpRequest httpRequest = new HttpRequest(String.format(GET_COMMENT_API, objectId, objectType));
        HttpResponse response = httpRequest.response();
        String textResponse = response.decode("utf-8");
        Matcher totalItemMatcher = TOTAL_ITEM_PATTERN.matcher(textResponse);
        return totalItemMatcher.find() ? Integer.parseInt(totalItemMatcher.group(1)) : 0;
    }

    public static void main(String[] args) throws Exception {
        String[] categoryUrls = {
                "https://vnexpress.net/tin-tuc/thoi-su", "https://vnexpress.net/tin-tuc/the-gioi",
                "https://giaitri.vnexpress.net", "https://thethao.vnexpress.net",
                "https://vnexpress.net/tin-tuc/phap-luat"
        };

        AutoNewsCrawler crawler = new AutoNewsCrawler(categoryUrls[0], "crawl-data", true);

        crawler.start(5);
        crawler.setThreads(50);
        crawler.getConf().setTopN(100);
        crawler.getConf().setWaitThreadEndTime(1000);


//        System.out.println(getTime("Chủ nhật, 5/11/2017 | 00:39 GMT+7"));
//        System.out.println(getCommentCount("{\"type\":\"comment\",\"article_id\":\"3670230\",\"article_type\":\"1\",\"site_id\":\"1000000\",\"category_id\":\"1001005\",\"sign\":\"8eb9e17d7f8f608ac726d7d00c72280d\",\"limit\":24}"));
    }

}
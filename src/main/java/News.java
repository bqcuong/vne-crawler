import java.util.List;

/**
 * Created by duongtd on 15/11/2017.
 */
public class News {
    public String url;
    public String time;
    public String title;
    public String description;
    public List<String> tags;
    public int comment_count;

    public int fb_reaction_count;
    public int fb_comment_count;
    public int fb_share_count;

    public News(){}

    public News(String url, String time, String title, String description, List<String> tags, int comment_count, int fb_reaction_count, int fb_comment_count, int fb_share_count) {
        this.url = url;
        this.time = time;
        this.title = title;
        this.description = description;
        this.tags = tags;
        this.comment_count = comment_count;
        this.fb_reaction_count = fb_reaction_count;
        this.fb_comment_count = fb_comment_count;
        this.fb_share_count = fb_share_count;
    }
}

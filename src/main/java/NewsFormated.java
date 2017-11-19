import java.util.List;

public class NewsFormated {
    public String url;
    public String time;
    public String title;
    public String description;
    public List<String> tags;

    public int like_count;
    public int comment_count;
    public int share_count;

    public List<String> tokenized_tags;
    public String tokenized_content;
    public NewsFormated() {}

    public NewsFormated(String url, String time, String title, String description,
                        List<String> tags, int reaction_count, int comment_count,
                        int share_count, List<String> tokenized_tags, String tokenized_content) {
        this.url = url;
        this.time = time;
        this.title = title;
        this.description = description;
        this.tags = tags;
        this.like_count = reaction_count;
        this.comment_count = comment_count;
        this.share_count = share_count;
        this.tokenized_content = tokenized_content;
        this.tokenized_tags = tokenized_tags;
    }
}

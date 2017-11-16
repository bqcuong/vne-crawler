import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class VnExpressConvert {
    public static void main(String[] args) throws Exception {
        VnExpressConvert("phapluat_4.json", "phapluat_formated_4.json");
    }

    public static void VnExpressConvert(String pathIn, String pathOut) throws Exception{
        byte[] jsonData = Files.readAllBytes(Paths.get(pathIn));

        //create ObjectMapper instance
        ObjectMapper objectMapper = new ObjectMapper();

        //convert json string to object
        List<News> news =
                objectMapper.readValue(jsonData, new TypeReference<List<News>>(){});
        List<NewsFormated> re = new ArrayList<NewsFormated>();
        for (News news_1 : news){
            String[] tokenizeds = NewsContentExtractor.tokenizeAndGetKeywords(
                    news_1.title + ". " + news_1.description);
            List<String> listTokenizes = Arrays.asList(tokenizeds);
//            listTokenizes.remove(0);
            NewsFormated newsFormated = new NewsFormated(
                    news_1.url,
                    news_1.time,
                    news_1.title,
                    news_1.description,
                    news_1.tags,
                    news_1.fb_reaction_count,
                    news_1.fb_comment_count,
                    news_1.fb_share_count,
                    listTokenizes.subList(1, listTokenizes.size()),
                    tokenizeds[0]

            );
            re.add(newsFormated);
        }
        final OutputStream out = new ByteArrayOutputStream();
        final ObjectMapper mapper = new ObjectMapper();

        mapper.writeValue(out, re);

        Files.write(new File(pathOut).toPath(), out.toString().getBytes());

//        List<News> myObjects = objectMapper.readValue(jsonInput, new TypeReference<List<MyClass>>(){});
    }
}

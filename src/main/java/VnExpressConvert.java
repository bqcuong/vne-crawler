import org.apache.avro.data.Json;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.type.TypeReference;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.*;
import java.lang.reflect.Array;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion.NON_NULL;

public class VnExpressConvert {
    public static void main(String[] args) throws Exception {
//        VnExpressConvert("hieu_tuoitrevn.json", "tuoitrevn_formated.json");
//        NormalConvert("test24h.json",
//                "test24h_formated.json");
//        AddArr("dantri_news.json");
        convertWithEmptyTags("dantri_news.json", "dantri_news_formated.json");
    }


    public static void AddArr(String path) throws Exception {
        File f1 = new File(path);
        String line;
        List<String> lines = new ArrayList<>();
        FileReader fr = new FileReader(f1);
        BufferedReader br = new BufferedReader(fr);
        while ((line = br.readLine()) != null) {
//            if (line.contains("\"tags"))
            line = convertTag1(line);
            lines.add(line);
        }
        fr.close();
        br.close();

        FileWriter fw = new FileWriter(f1);
        BufferedWriter out = new BufferedWriter(fw);
        for(String s : lines)
            out.write(s);
        out.flush();
    }
    //{"description": "MC ", "comment_count": 44, "time": "14/11/2017 13:22", "like_count": 4399,
    // "tags": "r\u01a1i m\u00e1y bay Mi171, mc phan anh", "url": "http://vietnamnet.vn/vn/thoi-su/clip-nong/mc-phan-anh-trao-tay-gia-cho-chien-si-song-sot-vu-roi-may-bay-mi171-410834.html", "title": "MC Phan Anh trao tay gi\u1ea3 cho chi\u1ebfn s\u0129 s\u1ed1ng s\u00f3t v\u1ee5 r\u01a1i m\u00e1y bay Mi171", "share_count": 25},


    public static String convertTag1(String input) {
        String pattern1 = "\"tags\":";
//        String pattern2 = "\"like_count\":";
        String pattern2 = "\"comment_count\":";

        Pattern p = Pattern.compile(Pattern.quote(pattern1) + "(.*?)" + Pattern.quote(pattern2));
        Matcher m = p.matcher(input);
        while (m.find()) {
//            System.out.println(m.group());
            String content = m.group(1).trim();
            content = content.replaceAll("^\\s*\"", "").replaceAll("\",$", "");
            String[] strs = content.split(",");
            List<String> tags = new ArrayList<String>();
            for(String str : strs) {
                tags.add("\"" + str.trim() + "\"");
            }
            String replace = "\"tags\":" + tags + "," + pattern2;
            input = input.replace(m.group(), replace);
        }
        return input;
    }

    public static void VnExpressConvert(String pathIn, String pathOut) throws Exception{
        byte[] jsonData = Files.readAllBytes(Paths.get(pathIn));

        //create ObjectMapper instance
        ObjectMapper objectMapper = new ObjectMapper();
//        objectMapper.setSerializationInclusion(JsonSerialize.Inclusion.NON_EMPTY);
//        objectMapper.configure(DeserializationConfig.Feature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
        objectMapper.getDeserializationConfig().with(DeserializationConfig.Feature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);

        //convert json string to object
        List<News> news =
                objectMapper.readValue(jsonData, new TypeReference<List<News>>(){});
        List<NewsFormated> re = new ArrayList<NewsFormated>();
        for (News news_1 : news){
            DateFormat df = new SimpleDateFormat("EEE MMM dd kk:mm:ss z yyyy", Locale.ENGLISH);
            Date result =  df.parse(news_1.time);
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");

            String[] tokenizeds = NewsContentExtractor.tokenizeAndGetKeywords(
                    news_1.title + ". " + news_1.description);
            List<String> listTokenizes = Arrays.asList(tokenizeds);
//            listTokenizes.remove(0);
            NewsFormated newsFormated = new NewsFormated(
                    news_1.url,
                    dateFormat.format(result),
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

    public static void convertWithEmptyTags(String pathIn, String pathOut) throws Exception {
        List<NewsFormated> re = new ArrayList<>();
        String content = new String(Files.readAllBytes(Paths.get(pathIn)));
        JSONParser parser = new JSONParser();
        JSONArray arrJson = (JSONArray)parser.parse(content);
        for (int i = 0; i < arrJson.size(); i++) {
            if (i % 100 == 0)
                System.out.println(i + "\n");
            JSONObject jsonObjectRow = (JSONObject) arrJson.get(i);
            String url = (String) jsonObjectRow.get("url");
            String time = (String) jsonObjectRow.get("time");
            String title = (String) jsonObjectRow.get("title");

            NewsFormated temp = new NewsFormated(url, time);
            if (re.contains(temp))
                continue;
            String description = (String) jsonObjectRow.get("description");
            String tags = (String) jsonObjectRow.get("tags");
            long like_count = (long) jsonObjectRow.get("like_count");
            long comment_count = (long) jsonObjectRow.get("comment_count");
            long share_count = (long) jsonObjectRow.get("share_count");
            String[] tagsNew = tags.split(",");

            String[] tokenizeds = NewsContentExtractor.tokenizeAndGetKeywords(
                    title + ". " + description);
            if (tokenizeds.length < 2)
                continue;
            List<String> listTokenizes = Arrays.asList(tokenizeds);
            NewsFormated newsFormated = new NewsFormated(
                    url,
                    time,
                    title,
                    description,
                    Arrays.asList(tagsNew),
                    (int)like_count,
                    (int)comment_count,
                    (int)share_count,
                    listTokenizes.subList(1, listTokenizes.size()),
                    tokenizeds[0]
            );
            re.add(newsFormated);
        }

//        return re;

        final OutputStream out = new ByteArrayOutputStream();
        final ObjectMapper mapper = new ObjectMapper();

        mapper.writeValue(out, re);

        Files.write(new File(pathOut).toPath(), out.toString().getBytes());
    }

    public static void NormalConvert(String pathIn, String pathOut) throws Exception{
        byte[] jsonData = Files.readAllBytes(Paths.get(pathIn));

        //create ObjectMapper instance
        ObjectMapper objectMapper = new ObjectMapper();

        //convert json string to object
        List<NewsFormated> news =
                objectMapper.readValue(jsonData, new TypeReference<List<NewsFormated>>(){});
        List<NewsFormated> re = new ArrayList<NewsFormated>();
        for (NewsFormated news_1 : news){
            String[] tokenizeds = NewsContentExtractor.tokenizeAndGetKeywords(
                    news_1.title + ". " + news_1.description);
            if (tokenizeds.length < 2)
                continue;
            List<String> listTokenizes = Arrays.asList(tokenizeds);
//            listTokenizes.remove(0);
            NewsFormated newsFormated = new NewsFormated(
                    news_1.url,
                    news_1.time,
                    news_1.title,
                    news_1.description,
                    news_1.tags,
                    news_1.like_count,
                    news_1.comment_count,
                    news_1.share_count,
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

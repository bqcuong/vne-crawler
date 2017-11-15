import vn.hus.nlp.tagger.VietnameseMaxentTagger;
import vn.hus.nlp.tokenizer.VietTokenizer;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NewsContentExtractor {

    private static final VietnameseMaxentTagger vietnameseMaxentTagger = new VietnameseMaxentTagger();
    private static final Pattern PROPER_NOUN = Pattern.compile("\\s(\\S+)/Np");

    public static String[] tokenizeAndGetKeywords(String rawContent) throws Exception {
        rawContent = rawContent.replace("\n|\t", "");
        // tokenize
        String tokenizedString[] = vietnameseMaxentTagger.getTokenizer().tokenize(rawContent);

        // get proper nouns
        String taggedString = vietnameseMaxentTagger.getTagger().tagTokenizedString(tokenizedString[0]);
        Matcher matcher = PROPER_NOUN.matcher(taggedString);

        Set<String> properNouns = new HashSet<>();
        while (matcher.find()) {
            properNouns.add(matcher.group(1).replace("_", " "));
        }

        List<String> result = new ArrayList<>();
        result.add(tokenizedString[0]);
        result.addAll(properNouns);
        return result.toArray(new String[] {});
    }

    public static void main(String[] args) throws Exception {
        String[] result = tokenizeAndGetKeywords("Việt Nam xuất hiện đầu tiên trong video cảm ơn châu Á của ông Trump Hình ảnh mở đầu video cảm ơn châu Á của ông Trump là người phụ nữ Việt đội nón lá và chiến sĩ cảnh sát giao thông trên đường phố.");
        System.out.println(Arrays.asList(result));
    }
}

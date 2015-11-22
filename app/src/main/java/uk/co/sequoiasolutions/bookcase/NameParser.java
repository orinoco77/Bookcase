package uk.co.sequoiasolutions.bookcase;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ajs on 20/11/2015.
 */
public class NameParser {

    private static boolean isPrepositionOrArticle(String text) {
        switch (text.toLowerCase()) {
            case "de" :
            case "del" :
            case "di" :
            case "von" :
            case "van" :
                return true;
        }
        return false;
    }
    public static List<Author> Parse(String name) {
        List<Author> authors = new ArrayList<Author>();
        String namesArr[] = name.split("; ");
        for (String authorName : namesArr) {

            String nameArr[] = authorName.split(", ");
            String reversedNameArr[] = new String[nameArr.length];
            int index = 0;
            for (int i = nameArr.length - 1; i >= 0; i--) {
                reversedNameArr[index] = nameArr[i];
                index++;
            }
            String singlename = StringUtils.join(reversedNameArr, " ");
            String singleNameArr[] = singlename.split(" ");
            int surnameStart = singleNameArr.length - 1;
            for (int i = 0; i < singleNameArr.length; i++) {
                if (isPrepositionOrArticle(singleNameArr[i])) {
                    surnameStart = i;
                }
            }
            Author author = new Author();
            String forenameArr[] = new String[surnameStart];
            for (int i = 0; i < surnameStart; i++) {
                forenameArr[i] = singleNameArr[i];
            }
            String surnameArr[] = new String[singleNameArr.length - surnameStart];
            for (int i = 0; i < singleNameArr.length - surnameStart; i++) {
                surnameArr[i] = singleNameArr[i + surnameStart];
            }

            author.Forename = StringUtils.join(forenameArr, " ");
            author.Surname = StringUtils.join(surnameArr, " ");
            authors.add(author);
        }
        return authors;
    }
}

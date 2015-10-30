package uk.co.sequoiasolutions.bookcase;

import java.util.ArrayList;
import java.util.List;

/**
 * JSON model for Ebook
 */
public class EbookJson {
    public String Title;
    public List<AuthorJson> Authors = new ArrayList<>();
    public String Description;
    public String EbookUrl;
    public long ImageData;
    public long Id;

    public static EbookJson For(Ebook ebook) {
        EbookJson json = new EbookJson();
        json.Title = ebook.Title;
        json.Description = ebook.Description;
        json.EbookUrl = ebook.EbookUrl;
        json.ImageData = ebook.ImageId;
        json.Id = ebook.Id;
        for (Author author : ebook.Authors) {
            json.Authors.add(AuthorJson.For(author));
        }
        return json;
    }
}

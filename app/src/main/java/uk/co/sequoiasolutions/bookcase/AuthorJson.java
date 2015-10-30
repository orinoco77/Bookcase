package uk.co.sequoiasolutions.bookcase;

/**
 * JSON model for Author
 */
public class AuthorJson {
    public String Name;
    public long Id;

    public static AuthorJson For(Author author) {
        AuthorJson json = new AuthorJson();
        json.Id = author.Id;
        json.Name = author.Name;
        return json;
    }
}

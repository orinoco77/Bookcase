package uk.co.sequoiasolutions.bookcase;

/**
 * JSON model for Author
 */
public class AuthorJson {
    public String Forename;
    public String Surname;
    public long Id;

    public static AuthorJson For(Author author) {
        AuthorJson json = new AuthorJson();
        json.Id = author.Id;
        json.Forename = author.Forename;
        json.Surname = author.Surname;
        return json;
    }
}

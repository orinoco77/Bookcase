package uk.co.sequoiasolutions.bookcase;

/**
 * Created by ajs on 10/08/2015.
 */
public class Ebook {
    private long _id;
    private String _title;
    private String _author;
    private String _description;
    private byte[] _imageUrl;
    private String _ebookUrl;

    public String getTitle() {
        return _title;
    }

    public void setTitle(String value) {
        _title = value;
    }

    public String getAuthor() {
        return _author;
    }

    public void setAuthor(String author) {
        this._author = author;
    }

    public String getDescription() {
        return _description;
    }

    public void setDescription(String description) {
        this._description = description;
    }

    public byte[] getImageUrl() {
        return _imageUrl;
    }

    public void setImageUrl(byte[] imageUrl) {
        this._imageUrl = imageUrl;
    }

    public String getEbookUrl() {
        return _ebookUrl;
    }

    public void setEbookUrl(String ebookUrl) {
        this._ebookUrl = ebookUrl;
    }

    public long getId() {
        return _id;
    }

    public void setId(long id) {
        this._id = id;
    }

    public Ebook() {

    }
}

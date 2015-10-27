package uk.co.sequoiasolutions.bookcase;

import org.orman.mapper.EntityList;
import org.orman.mapper.Model;
import org.orman.mapper.annotation.Entity;
import org.orman.mapper.annotation.ManyToMany;
import org.orman.mapper.annotation.PrimaryKey;

/**
 * Created by ajs on 10/08/2015.
 */
@Entity
public class Ebook extends Model<Ebook> {
    @PrimaryKey(autoIncrement = true)
    public long Id;
    public String Title;
    public String Description;
    public String ImageUrl;
    public String EbookUrl;
    @ManyToMany(toType = Author.class)
    public EntityList<Ebook, Author> Authors = new EntityList<Ebook, Author>(Ebook.class, Author.class, this);
    public Ebook() {

    }
}

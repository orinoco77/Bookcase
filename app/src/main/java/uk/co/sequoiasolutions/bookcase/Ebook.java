package uk.co.sequoiasolutions.bookcase;

import org.orman.mapper.EntityList;
import org.orman.mapper.Model;
import org.orman.mapper.annotation.Entity;
import org.orman.mapper.annotation.ManyToMany;
import org.orman.mapper.annotation.PrimaryKey;

/**
 * Ebook Entity
 */
@Entity
public class Ebook extends Model<Ebook> {
    @PrimaryKey(autoIncrement = true)
    public long Id;
    public String Title;
    public String Description;
    public long ImageId;
    public String EbookUrl;
    @ManyToMany(toType = Author.class)
    public EntityList<Ebook, Author> Authors = new EntityList<Ebook, Author>(Ebook.class, Author.class, this);
    public Ebook() {

    }
}

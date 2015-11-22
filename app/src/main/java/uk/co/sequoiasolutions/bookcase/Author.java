package uk.co.sequoiasolutions.bookcase;

import org.orman.mapper.EntityList;
import org.orman.mapper.Model;
import org.orman.mapper.annotation.Entity;
import org.orman.mapper.annotation.ManyToMany;
import org.orman.mapper.annotation.PrimaryKey;

/**
 * Author Entity
 */
@Entity
class Author extends Model<Author> {
    @PrimaryKey(autoIncrement = true)
    public long Id;
    public String Forename;
    public String Surname;
    @ManyToMany(toType = Ebook.class)
    public EntityList<Author, Ebook> Ebooks = new EntityList<Author, Ebook>(Author.class, Ebook.class, this);

    public Author() {
    }
}

package uk.co.sequoiasolutions.bookcase;

import org.orman.mapper.Model;
import org.orman.mapper.annotation.Entity;
import org.orman.mapper.annotation.PrimaryKey;

/**
 * ImageData Entity
 */
@Entity
public class ImageData extends Model<ImageData> {
    @PrimaryKey(autoIncrement = true)
    public long Id;
    public String Base64CoverImage;

    public ImageData() {
    }
}

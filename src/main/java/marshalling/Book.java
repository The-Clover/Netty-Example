package marshalling;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 *Description: Book
 *Created: 2024-09-23
 *@author Andrew.Ng
 */

@Data
public class Book implements Serializable {
    private Integer id;
    private String name;
    private Date date;
}

package models;
 
import java.util.*;
import javax.persistence.*;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.SqlRow;
 
import play.data.validation.*;
import play.modules.ebean.EbeanContext;
import play.modules.ebean.Model;
 
@Entity
public class Tag extends Model implements Comparable<Tag> {
 
    @Required
    private String name;
    
    public Tag(String name) {
        this.name = name;
    }
    
    public String getName()
    {
      return name;
    }

    public void setName(String name)
    {
      this.name = name;
    }

    public static Tag findOrCreateByName(String name) {
        Tag tag = Tag.findUnique("name=?", name);
        if(tag == null) {
            tag = new Tag(name);
        }
        return tag;
    }
    
    public static List<Map> getCloud() {
        List result = EbeanContext.server().createSqlQuery("select t.name as tag, count(p.id) as pound from post p join post_tag pt on p.id=pt.post_id join tag t on pt.tag_id=t.id group by t.name").findList();
        return result;
    }
    
    public String toString() {
        return name;
    }
    
    public int compareTo(Tag otherTag) {
        return name.compareTo(otherTag.name);
    }
 
}
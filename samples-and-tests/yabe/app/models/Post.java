package models;
 
import java.util.*;
import javax.persistence.*;

import com.avaje.ebean.RawSql;
import com.avaje.ebean.RawSqlBuilder;

import play.data.binding.*;
import play.data.validation.*;
import play.modules.ebean.EbeanContext;
import play.modules.ebean.Model;

@Entity
public class Post extends Model {
 
    @Required
    private String title;
    
    @Required @As("yyyy-MM-dd")
    private Date postedAt;
    
    @Lob
    @Required
    @MaxSize(10000)
    private String content;
    
    @Required
    @ManyToOne
    private User author;
    
    @OneToMany(mappedBy="post", cascade=CascadeType.ALL)
    private List<Comment> comments;
    
    @ManyToMany(cascade=CascadeType.PERSIST)
    private Set<Tag> tags;
    
    public Post(User author, String title, String content) { 
        this.comments = new ArrayList<Comment>();
        this.tags = new TreeSet();  
        this.author = author;
        this.title = title;
        this.content = content;
        this.postedAt = new Date();
    }
    
    
    public String getTitle()
    {
      return title;
    }


    public void setTitle(String title)
    {
      this.title = title;
    }


    public Date getPostedAt()
    {
      return postedAt;
    }


    public void setPostedAt(Date postedAt)
    {
      this.postedAt = postedAt;
    }


    public String getContent()
    {
      return content;
    }


    public void setContent(String content)
    {
      this.content = content;
    }


    public User getAuthor()
    {
      return author;
    }


    public void setAuthor(User author)
    {
      this.author = author;
    }


    public List<Comment> getComments()
    {
      return comments;
    }


    public Set<Tag> getTags()
    {
      return tags;
    }

    public void setTags(Set<Tag> tags)
    {
      this.tags = tags;
    }


    public Post addComment(String author, String content) {
        Comment newComment = new Comment(this, author, content);
        this.comments.add(newComment);
        this.save();
        return this;
    }
    
    public Post previous() {
        return Post.<Post>find("postedAt < ?", postedAt).orderBy("postedAt desc").setMaxRows(1).findUnique();
    }

    public Post next() {
      return Post.<Post>find("postedAt > ?", postedAt).orderBy("postedAt asc").setMaxRows(1).findUnique();
    }
    
    public Post tagItWith(String name) {
        tags.add(Tag.findOrCreateByName(name));
        return this;
    }
    
    public static List<Post> findTaggedWith(String tag) {
        return Post.<Post>find("tags.name=?",tag).findList();
    }
    
    public static List<Post> findTaggedWith(String... tags) {
        
          RawSql rawSql =
            RawSqlBuilder
            .parse("select p.id id from post p inner join post_tag pt on p.id=pt.post_id inner join tag t on pt.tag_id=t.id group by p.id")
            // map result columns to bean properties
 //           .columnMapping("p.id", "post.id")
 //           .columnMapping("o.status", "order.status")
 //           .columnMapping("c.id", "order.customer.id")
 //           .columnMapping("c.name", "order.customer.name")
            .create();
      
      return Post.<Post>all().setRawSql(rawSql).where().in("t.name",tags).having().eq("count(t.id)",tags.length).findList();

      //      "select distinct p.id from Post p join p.tags as t where t.name in (:tags) group by p.id having count(t.id) = :size"
      //  ).setParameter("tags", tags).setParameter("size", tags.length).findList();
    }
    
    public String toString() {
        return title;
    }
 
}

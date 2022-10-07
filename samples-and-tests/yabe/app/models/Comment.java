package models;
 
import java.util.*;
import javax.persistence.*;
 
import play.data.validation.*;
import play.modules.ebean.Model;
 
@Entity
public class Comment extends Model {
 
    @Required
    private String author;
    
    @Required
    private Date postedAt;
     
    @Lob
    @Required
    @MaxSize(10000)
    private String content;
    
    @ManyToOne
    @Required
    private Post post;
    
    public Comment(Post post, String author, String content) {
        this.post = post;
        this.author = author;
        this.content = content;
        this.postedAt = new Date();
    }
    
    public String getAuthor()
    {
      return author;
    }

    public void setAuthor(String author)
    {
      this.author = author;
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

    public Post getPost()
    {
      return post;
    }

    public void setPost(Post post)
    {
      this.post = post;
    }

    public String toString() {
        return content.length() > 50 ? content.substring(0, 50) + "..." : content;
    }
 
}
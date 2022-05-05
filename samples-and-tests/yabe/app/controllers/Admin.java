package controllers;
import play.mvc.*;
 
import java.util.*;
 
import models.*;
 
@With(Secure.class)
public class Admin extends Controller {
    
    @Before
    static void setConnectedUser() {
        if(Security.isConnected()) {
            User user = User.findUnique("email=?", Security.connected());
            renderArgs.put("user", user.getFullname());
        }
    }
 
    public static void index() {
        List<Post> posts = Post.<Post>find("author.email=?", Security.connected()).findList();
        render(posts);
    }
    
    public static void form(Long id) {
        if(id != null) {
            Post post = Post.findById(id);
            render(post);
        }
        render();
    }
    
    public static void save(Long id, String title, String content, String tags) {
        Post post;
        if(id == null) {
            // Create post
            User author = User.findUnique("email=?", Security.connected());
            post = new Post(author, title, content);
        } else {
            // Retrieve post
            post = Post.findById(id);
            post.setTitle(title);
            post.setContent(content);
            post.getTags().clear();
        }
        // Set tags list
        for(String tag : tags.split("\\s+")) {
            if(tag.trim().length() > 0) {
                post.getTags().add(Tag.findOrCreateByName(tag));
            }
        }
        // Validate
        validation.valid(post);
        if(validation.hasErrors()) {
            render("@form", post);
        }
        // Save
        post.save();
        index();
    }
    
}

import org.junit.*;
import java.util.*;

import play.modules.ebean.EbeanContext;
import play.test.*;
import models.*;
 
public class BasicTest extends UnitTest {
    
    @Before
    public void setup() {
        Fixtures.deleteAll();
    }
 
    @Test
    public void createAndRetrieveUser() {
        // Create a new user and save it
        new User("bob@gmail.com", "secret", "Bob").save();

        // Retrieve the user with bob username
        User bob = User.findUnique("email=?", "bob@gmail.com");

        // Test 
        assertNotNull(bob);
        assertEquals("Bob", bob.getFullname());
    }
    
    @Test
    public void tryConnectAsUser() {
        // Create a new user and save it
        new User("bob@gmail.com", "secret", "Bob").save();

        // Test 
        assertNotNull(User.connect("bob@gmail.com", "secret"));
        assertNull(User.connect("bob@gmail.com", "badpassword"));
        assertNull(User.connect("tom@gmail.com", "secret"));
    }
    
    @Test
  public void createPost()
  {
    EbeanContext.server().beginTransaction();
    try {
      // Create a new user and save it
      User bob = new User("bob@gmail.com", "secret", "Bob").save();

      // Create a new post
      new Post(bob, "My first post", "Hello world").save();

      // Test that the post has been created
      assertEquals(1, Post.count());

      // Retrieve all post created by bob
      List<Post> bobPosts = Post.<Post> find("author.id=?", bob.getId()).fetch("author").setUseCache(true).findList();

      // Tests
      assertEquals(1, bobPosts.size());
      Post firstPost = bobPosts.get(0);
      assertNotNull(firstPost);
      assertEquals(bob, firstPost.getAuthor());
      assertEquals("My first post", firstPost.getTitle());
      assertEquals("Hello world", firstPost.getContent());
      assertNotNull(firstPost.getPostedAt());

      EbeanContext.server().commitTransaction();
    } finally {
      EbeanContext.server().endTransaction();
    }
  }
    
    @Test
    public void postComments() {
        // Create a new user and save it
        User bob = new User("bob@gmail.com", "secret", "Bob").save();

        // Create a new post
        Post bobPost = new Post(bob, "My first post", "Hello world").save();

        // Post a first comment
        new Comment(bobPost, "Jeff", "Nice post").save();
        new Comment(bobPost, "Tom", "I knew that !").save();

        // Retrieve all comments
        List<Comment> bobPostComments = Comment.<Comment>find("post.id=?", bobPost.getId()).findList();

        // Tests
        assertEquals(2, bobPostComments.size());

        Comment firstComment = bobPostComments.get(0);
        assertNotNull(firstComment);
        assertEquals("Jeff", firstComment.getAuthor());
        assertEquals("Nice post", firstComment.getContent());
        assertNotNull(firstComment.getPostedAt());

        Comment secondComment = bobPostComments.get(1);
        assertNotNull(secondComment);
        assertEquals("Tom", secondComment.getAuthor());
        assertEquals("I knew that !", secondComment.getContent());
        assertNotNull(secondComment.getPostedAt());
    }
    
    @Test
    public void useTheCommentsRelation() {
        // Create a new user and save it
        User bob = new User("bob@gmail.com", "secret", "Bob").save();

        // Create a new post
        Post bobPost = new Post(bob, "My first post", "Hello world").save();

        // Post a first comment
        bobPost.addComment("Jeff", "Nice post");
        bobPost.addComment("Tom", "I knew that !");

        // Count things
        assertEquals(1, User.count());
        assertEquals(1, Post.count());
        assertEquals(2, Comment.count());

        // Retrieve the bob post
        bobPost = Post.<Post>find("author.id=?", bob.getId()).findList().get(0);
        assertNotNull(bobPost);

        // Navigate to comments
        assertEquals(2, bobPost.getComments().size());
        assertEquals("Jeff", bobPost.getComments().get(0).getAuthor());

        // Delete the post
        bobPost.delete();

        // Chech the all comments have been deleted
        assertEquals(1, User.count());
        assertEquals(0, Post.count());
        assertEquals(0, Comment.count());
    }
    
    @Test
    public void fullTest() {
        Fixtures.load("data.yml");

        // Count things
        assertEquals(2, User.count());
        assertEquals(3, Post.count());
        assertEquals(3, Comment.count());

        // Try to connect as users
        assertNotNull(User.connect("bob@gmail.com", "secret"));
        assertNotNull(User.connect("jeff@gmail.com", "secret"));
        assertNull(User.connect("jeff@gmail.com", "badpassword"));
        assertNull(User.connect("tom@gmail.com", "secret"));

        // Find all bob posts
        List<Post> bobPosts = Post.<Post>find("author.email=?", "bob@gmail.com").findList();
        assertEquals(2, bobPosts.size());

        // Find all comments related to bob posts
        List<Comment> bobComments = Comment.<Comment>find("post.author.email=?", "bob@gmail.com").findList();
        assertEquals(3, bobComments.size());

        // Find the most recent post
        Post frontPost = Post.<Post>all().orderBy("postedAt desc").setMaxRows(1).findUnique();
        assertNotNull(frontPost);
        assertEquals("About the model layer", frontPost.getTitle());

        // Check that this post has two comments
        assertEquals(2, frontPost.getComments().size());

        // Post a new comment
        frontPost.addComment("Jim", "Hello guys");
        assertEquals(3, frontPost.getComments().size());
        assertEquals(4, Comment.count());
    }
    
    @Test
    public void testTags() {
        // Create a new user and save it
        User bob = new User("bob@gmail.com", "secret", "Bob").save();

        // Create a new post
        Post bobPost = new Post(bob, "My first post", "Hello world").save();
        Post anotherBobPost = new Post(bob, "My second post post", "Hello world").save();
        
        // Well
        assertEquals(0, Post.findTaggedWith("Red").size());
        
        // Tag it now
        bobPost.tagItWith("Red").tagItWith("Blue").save();
        anotherBobPost.tagItWith("Red").tagItWith("Green").save();
        
        // Check
        assertEquals(2, Post.findTaggedWith("Red").size());        
        assertEquals(1, Post.findTaggedWith("Blue").size());
        assertEquals(1, Post.findTaggedWith("Green").size());
        
        assertEquals(1, Post.findTaggedWith("Red", "Blue").size());   
        assertEquals(1, Post.findTaggedWith("Red", "Green").size());   
        assertEquals(0, Post.findTaggedWith("Red", "Green", "Blue").size());  
        assertEquals(0, Post.findTaggedWith("Green", "Blue").size());    
        
        List<Map> cloud = new ArrayList<Map>();
        cloud.addAll(Tag.getCloud());
        Collections.sort(cloud, new Comparator<Map>() {
            public int compare(Map m1, Map m2) {
                return m1.get("tag").toString().compareTo(m2.get("tag").toString());
            }
        });
        assertEquals("[{tag=Blue, pound=1}, {tag=Green, pound=1}, {tag=Red, pound=2}]", cloud.toString());
        
    }
 
}
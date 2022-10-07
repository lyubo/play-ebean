package models;
 
import javax.persistence.*;
 
import play.data.validation.*;
import play.modules.ebean.Model;
 
@Entity
public class User extends Model {
 
    @Email
    @Required
    private String email;
    
    @Required
    private String password;
    
    private String fullname;
    
    private boolean isAdmin;
    
    public User(String email, String password, String fullname) {
        this.email = email;
        this.password = password;
        this.fullname = fullname;
    }
    
    public String getEmail()
    {
      return email;
    }

    public void setEmail(String email)
    {
      this.email = email;
    }

    public String getPassword()
    {
      return password;
    }

    public void setPassword(String password)
    {
      this.password = password;
    }

    public String getFullname()
    {
      return fullname;
    }

    public void setFullname(String fullname)
    {
      this.fullname = fullname;
    }

    public boolean getIsAdmin()
    {
      return isAdmin;
    }

    public void setIsAdmin(boolean isAdmin)
    {
      this.isAdmin = isAdmin;
    }

    public static User connect(String email, String password) {
        return findUnique("email=? and password=?", email, password);
    }
    
    public String toString() {
        return email;
    }
 
}
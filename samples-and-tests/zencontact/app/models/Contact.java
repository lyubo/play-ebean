package models;

import play.data.validation.*;
import play.modules.ebean.Model;

import javax.persistence.*;
import java.util.*;

@Entity
public class Contact extends Model {
    
    @Required
    private String firstname;
    
    @Required
    private String name;
    
    @Required
    private Date birthdate;
    
    @Required
    @Email
    private String email;

    public String getFirstname()
    {
      return firstname;
    }

    public void setFirstname(String firstname)
    {
      this.firstname = firstname;
    }

    public String getName()
    {
      return name;
    }

    public void setName(String name)
    {
      this.name = name;
    }

    public Date getBirthdate()
    {
      return birthdate;
    }

    public void setBirthdate(Date birthdate)
    {
      this.birthdate = birthdate;
    }

    public String getEmail()
    {
      return email;
    }

    public void setEmail(String email)
    {
      this.email = email;
    }
    
}


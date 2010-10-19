package play.modules.ebean;

import java.io.Serializable;

import javax.persistence.MappedSuperclass;

import com.avaje.ebean.EbeanServer;

import play.PlayPlugin;
//import play.db.Model;

//@MappedSuperclass
public class EbeanBase implements Serializable//, Model
{

  public void _save()
  {
    ebean().save(this);
   }

  public void _delete()
  {
    ebean().delete(this);
    PlayPlugin.postEvent("JPASupport.objectDeleted", this);
  }

  public Object _key()
  {
    return null;
    //return Model.Manager.factoryFor(this.getClass()).keyValue(this);
  }

  protected static EbeanServer ebean()
  {
    return EbeanContext.server();
  }

}

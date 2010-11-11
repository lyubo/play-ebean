package play.modules.ebean;

import java.util.List;
import java.util.Map;

import javax.persistence.MappedSuperclass;

import play.PlayPlugin;
import play.data.validation.Validation;
import play.exceptions.UnexpectedException;
import play.mvc.Scope.Params;

import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.Query;

@SuppressWarnings("unchecked")
@MappedSuperclass
public class EbeanSupport
{
  protected static EbeanServer ebean()
  {
    return EbeanContext.server();
  }

  public static <T extends EbeanSupport> T edit(T o, String name, Map<String, String[]> params)
  {
    // TODO: Entities and file attachments
    try {
      //BeanWrapper bw = new BeanWrapper(o.getClass());
      //bw.bind(name, o.getClass(), params, "", o);
      //Binder.bind(o,"",params);
      return o;

    } catch (Exception e) {
      throw new UnexpectedException(e);
    }
  }

  public <T extends EbeanSupport> T edit(String name, Params params)
  {
    return (T) edit(this, name, params.all());
  }

  public boolean validateAndSave()
  {
    if (Validation.current().valid(this).ok) {
      save();
      return true;
    }
    return false;
  }

  public <T extends EbeanSupport> T save()
  {
    ebean().save(this);
    return (T) this;
  }

  public <T extends EbeanSupport> T refresh()
  {
    ebean().refresh(this);
    return (T) this;
  }

  public <T extends EbeanSupport> T merge()
  {
    return (T) this;
  }

  public <T extends EbeanSupport> T delete()
  {
    ebean().delete(this);
    PlayPlugin.postEvent("JPASupport.objectDeleted", this);
    return (T) this;
  }

  public static <T extends EbeanSupport> T create(String name, Params params)
  {
    throw enhancementError();
  }

  public static <T extends EbeanSupport> List<T> findAll()
  {
    throw enhancementError();
  }

  public static int deleteAll()
  {
    throw enhancementError();
  }

  public static <T extends EbeanSupport> T findById(Object id)
  {
    throw enhancementError();
  }

  public static <T extends EbeanSupport> T findUnique(String property, Object value, Object... moreParams)
  {
    throw enhancementError();
  }

  protected static <T extends EbeanSupport> T findUnique(Class<T> beanType, String property, Object value, Object[] moreParams)
  {
    Query<T> q = ebean().createQuery(beanType);
    q.where().eq(property, value);
    for (int i = 0; i < moreParams.length; i += 2)
      q.where().eq(moreParams[i].toString(), moreParams[i + 1]);
    return q.findUnique();
  }

  protected static <T extends EbeanSupport> T create(Class<T> type, String name, play.mvc.Scope.Params params)
  {
    try {
      T model = type.newInstance();
      return edit(model, name, params.all());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  protected void afterLoad()
  {
  }

  protected void beforeSave(boolean isInsert)
  {
  }

  protected void afterSave(boolean isInsert)
  {
  }

  private static UnsupportedOperationException enhancementError()
  {
    return new UnsupportedOperationException("Please annotate your JPA model with @javax.persistence.Entity annotation.");
  }

}
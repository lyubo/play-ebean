package play.modules.ebean;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import play.Play;
import play.PlayPlugin;
import play.data.binding.BeanWrapper;
import play.data.binding.Binder;
import play.data.validation.Validation;
import play.db.Model;
import play.exceptions.UnexpectedException;
import play.mvc.Scope.Params;

import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.Query;

@SuppressWarnings("unchecked")
@MappedSuperclass
public class EbeanSupport implements play.db.Model
{

  public static <T extends EbeanSupport> T create(Class<?> type, String name, Map<String, String[]> params, Annotation[] annotations)
  {
    try {
      Constructor<?> c = type.getDeclaredConstructor();
      c.setAccessible(true);
      Object model = c.newInstance();
      return (T) edit(model, name, params, annotations);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public static <T extends EbeanSupport> T edit(Object o, String name, Map<String, String[]> params, Annotation[] annotations)
  {
    try {
      BeanWrapper bw = new BeanWrapper(o.getClass());
      // Start with relationst
      Set<Field> fields = new HashSet<Field>();
      Class<?> clazz = o.getClass();
      while (!clazz.equals(Object.class)) {
        Collections.addAll(fields, clazz.getDeclaredFields());
        clazz = clazz.getSuperclass();
      }
      for (Field field : fields) {
        boolean isEntity = false;
        String relation = null;
        boolean multiple = false;
        //
        if (field.isAnnotationPresent(OneToOne.class) || field.isAnnotationPresent(ManyToOne.class)) {
          isEntity = true;
          relation = field.getType().getName();
        }
        if (field.isAnnotationPresent(OneToMany.class) || field.isAnnotationPresent(ManyToMany.class)) {
          Class<?> fieldType = (Class<?>) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
          isEntity = true;
          relation = fieldType.getName();
          multiple = true;
        }

        if (isEntity) {
          Class<Model> c = (Class<Model>) Play.classloader.loadClass(relation);
          if (EbeanSupport.class.isAssignableFrom(c)) {
            String keyName = Model.Manager.factoryFor(c).keyName();
            if (multiple && Collection.class.isAssignableFrom(field.getType())) {
              Collection<Object> l = new ArrayList<Object>();
              if (SortedSet.class.isAssignableFrom(field.getType())) {
                l = new TreeSet<Object>();
              } else if (Set.class.isAssignableFrom(field.getType())) {
                l = new HashSet<Object>();
              }
              String[] ids = params.get(name + "." + field.getName() + "." + keyName);
              if (ids != null) {
                params.remove(name + "." + field.getName() + "." + keyName);
                for (String _id : ids) {
                  if (_id.equals("")) {
                    continue;
                  }
                  Query<?> q = ebean().createQuery(Class.forName(relation)).where(keyName + " = ?");
                  q.setParameter(1, Binder.directBind(_id, Model.Manager.factoryFor((Class<Model>) Play.classloader.loadClass(relation)).keyType()));
                  Object result = q.findUnique();
                  if (result != null)
                    l.add(result);
                  else
                    Validation.addError(name + "." + field.getName(), "validation.notFound", _id);
                }
                bw.set(field.getName(), o, l);
              }
            } else {
              String[] ids = params.get(name + "." + field.getName() + "." + keyName);
              if (ids != null && ids.length > 0 && !ids[0].equals("")) {
                params.remove(name + "." + field.getName() + "." + keyName);
                Query<?> q = ebean().createQuery(Class.forName(relation)).where(keyName + " = ?");
                q.setParameter(1, Binder.directBind(ids[0], Model.Manager.factoryFor((Class<Model>) Play.classloader.loadClass(relation)).keyType()));
                Object to = q.findUnique();
                if (to != null)
                  bw.set(field.getName(), o, to);
                else
                  Validation.addError(name + "." + field.getName(), "validation.notFound", ids[0]);
              } else if (ids != null && ids.length > 0 && ids[0].equals("")) {
                bw.set(field.getName(), o, null);
                params.remove(name + "." + field.getName() + "." + keyName);
              }
            }
          }
        }
      }
      bw.bind(name, o.getClass(), params, "", o, annotations != null ? annotations : new Annotation[0]);
      return (T) o;
    } catch (Exception e) {
      throw new UnexpectedException(e);
    }
  }

  public <T extends EbeanSupport> T edit(String name, Map<String, String[]> params)
  {
    edit(this, name, params, null);
    return (T) this;
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
    _save();
    return (T) this;
  }

  public <T extends EbeanSupport> T refresh()
  {
    ebean().refresh(this);
    return (T) this;
  }

  public <T extends EbeanSupport> T delete()
  {
    _delete();
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

  public static <T extends EbeanSupport> T findById(Object id)
  {
    throw enhancementError();
  }

  public static <T extends EbeanSupport> T findUnique(String property, Object value, Object... moreParams)
  {
    throw enhancementError();
  }

  public static <T extends EbeanSupport> Query<T> query()
  {
    throw enhancementError();
  }


  public static <T extends EbeanSupport> Query<T> query(Class<T> clazz)
  {
    return ebean().createQuery(clazz);
  }

  public static int deleteAll()
  {
    throw enhancementError();
  }

  protected static EbeanServer ebean()
  {
    return EbeanContext.server();
  }

  protected static <T extends EbeanSupport> T findUnique(Class<T> beanType, String property, Object value, Object[] moreParams)
  {
    Query<T> q = ebean().createQuery(beanType);
    q.where().eq(property, value);
    for (int i = 0; i < moreParams.length; i += 2)
      q.where().eq(moreParams[i].toString(), moreParams[i + 1]);
    return q.findUnique();
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

  @Override
  public void _save()
  {
    ebean().save(this);
    PlayPlugin.postEvent("JPASupport.objectPersisted", this);
  }

  @Override
  public void _delete()
  {
    ebean().delete(this);
    PlayPlugin.postEvent("JPASupport.objectDeleted", this);
  }

  @Override
  public Object _key()
  {
    return Model.Manager.factoryFor(this.getClass()).keyValue(this);
  }

}
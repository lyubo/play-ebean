package play.modules.ebean;

import java.util.List;

import javax.sql.DataSource;

import play.Logger;
import play.Play;
import play.PlayPlugin;
import play.classloading.ApplicationClasses.ApplicationClass;
import play.db.DB;
import play.db.jpa.JPAPlugin;

import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.EbeanServerFactory;
import com.avaje.ebean.config.ServerConfig;

public class EbeanPlugin extends PlayPlugin
{
  public static EbeanServer defaultServer;

  @SuppressWarnings({ "unchecked", "rawtypes" })
  public static EbeanServer createServer(String name, DataSource dataSource)
  {
    EbeanServer result = null;
    ServerConfig cfg = new ServerConfig();
    cfg.loadFromProperties();
    cfg.setName(name);
    cfg.setClasses((List) Play.classloader.getAllClasses());
    cfg.setDataSource(new EbeanDataSourceWrapper(dataSource));
    cfg.setRegister("default".equals(name));
    cfg.setDefaultServer("default".equals(name));
    cfg.add(new EbeanModelAdapter());
    try {
      result = EbeanServerFactory.create(cfg);
    } catch (Throwable t) {
      Logger.error("Failed to create ebean server", t);
    }
    return result;
  }

  public EbeanPlugin()
  {
    super();
  }

  @Override
  public void onLoad()
  {
    // TODO: Hack! We have to change this once built-in plugins may be deactivated
    for (PlayPlugin plugin : Play.plugins) {
      if (plugin instanceof JPAPlugin) {
        Play.plugins.remove(plugin);
        break;
      }
    }
  }

  @Override
  public void onApplicationStart()
  {
    if (DB.datasource != null) defaultServer = createServer("default", DB.datasource);
  }

  @Override
  public void beforeInvocation()
  {
    EbeanContext.set(defaultServer);
  }

  @Override
  public void afterInvocation()
  {
    EbeanServer ebean = EbeanContext.server();
    if (ebean != null && ebean.currentTransaction() != null) ebean.commitTransaction();
  }

  @Override
  public void invocationFinally()
  {
    EbeanServer ebean = EbeanContext.server();
    if (ebean != null) ebean.endTransaction();
    EbeanContext.set(null);
  }

  @Override
  public void enhance(ApplicationClass applicationClass) throws Exception
  {
    EbeanEnhancer.class.newInstance().enhanceThisClass(applicationClass);
  }

//  @Override
//  public Model.Factory modelFactory(Class<? extends Model> modelClass)
//  {
//    if (modelClass.isAnnotationPresent(Entity.class)) {
//      return new EbeanModelLoader(modelClass);
//    }
//    return null;
//  }
//
//  public static class EbeanModelLoader implements Model.Factory
//  {
//
//    private Class<? extends Model> clazz;
//
//    public EbeanModelLoader(Class<? extends Model> clazz)
//    {
//      this.clazz = clazz;
//    }
//
//    public Model findById(Object id)
//    {
//      if (id == null) return null;
//
//      try {
//        return EbeanContext.server().find(clazz, Binder.directBind(id.toString(), this.keyType()));
//      } catch (Exception e) {
//        return null;
//      }
//    }
//
//    public String keyName()
//    {
//      return keyField().getName();
//    }
//
//    public Class<?> keyType()
//    {
//      return keyField().getType();
//    }
//
//    public Object keyValue(Model m)
//    {
//      try {
//        return keyField().get(m);
//      } catch (Exception ex) {
//        throw new UnexpectedException(ex);
//      }
//    }
//
//    public List<Model> fetch(int offset, int length, String orderBy, String orderDirection, List<String> properties, String keywords, String where)
//    {
//      return new ArrayList<Model>();
//    }
//
//    public Long count(List<String> properties, String keywords, String where)
//    {
//      return 0l;
//    }
//
//    public void deleteAll()
//    {
//      String query = "delete from " + clazz.getSimpleName();
//      Update<?> deleteAll = EbeanContext.server().createUpdate(clazz, query);
//      deleteAll.execute();
//    }
//
//    public List<Property> listProperties()
//    {
//      List<Model.Property> properties = new ArrayList<Model.Property>();
//      Set<Field> fields = new HashSet<Field>();
//      Class<?> tclazz = clazz;
//      while (!tclazz.equals(Object.class)) {
//        Collections.addAll(fields, tclazz.getDeclaredFields());
//        tclazz = tclazz.getSuperclass();
//      }
//      for (Field f : fields) {
//        if (Modifier.isTransient(f.getModifiers())) {
//          continue;
//        }
//        if (f.isAnnotationPresent(Transient.class)) {
//          continue;
//        }
//        Model.Property mp = buildProperty(f);
//        if (mp != null) {
//          properties.add(mp);
//        }
//      }
//      return properties;
//    }
//
//    private Field keyField()
//    {
//      Class<?> c = clazz;
//      try {
//        while (!c.equals(Object.class)) {
//          for (Field field : c.getDeclaredFields()) {
//            if (field.isAnnotationPresent(Id.class)) {
//              field.setAccessible(true);
//              return field;
//            }
//          }
//          c = c.getSuperclass();
//        }
//      } catch (Exception e) {
//        throw new UnexpectedException("Error while determining the object @Id for an object of type " + clazz);
//      }
//      throw new UnexpectedException("Cannot get the object @Id for an object of type " + clazz);
//    }
//
//    private Model.Property buildProperty(final Field field)
//    {
//      Model.Property modelProperty = new Model.Property();
//      modelProperty.type = field.getType();
//      modelProperty.field = field;
//      if (Model.class.isAssignableFrom(field.getType())) {
//        if (field.isAnnotationPresent(OneToOne.class)) {
//          if (field.getAnnotation(OneToOne.class).mappedBy().equals("")) {
//            modelProperty.isRelation = true;
//            modelProperty.relationType = field.getType();
//            modelProperty.choices = new Model.Choices() {
//
//              @SuppressWarnings("unchecked")
//              public List<Object> list()
//              {
//                return JPA.em().createQuery("from " + field.getType().getName()).getResultList();
//              }
//            };
//          }
//        }
//        if (field.isAnnotationPresent(ManyToOne.class)) {
//          modelProperty.isRelation = true;
//          modelProperty.relationType = field.getType();
//          modelProperty.choices = new Model.Choices() {
//
//            @SuppressWarnings("unchecked")
//            public List<Object> list()
//            {
//              return JPA.em().createQuery("from " + field.getType().getName()).getResultList();
//            }
//          };
//        }
//      }
//      if (Collection.class.isAssignableFrom(field.getType())) {
//        final Class<?> fieldType = (Class<?>) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
//        if (field.isAnnotationPresent(OneToMany.class)) {
//          if (field.getAnnotation(OneToMany.class).mappedBy().equals("")) {
//            modelProperty.isRelation = true;
//            modelProperty.isMultiple = true;
//            modelProperty.relationType = fieldType;
//            modelProperty.choices = new Model.Choices() {
//
//              @SuppressWarnings("unchecked")
//              public List<Object> list()
//              {
//                return JPA.em().createQuery("from " + fieldType.getName()).getResultList();
//              }
//            };
//          }
//        }
//        if (field.isAnnotationPresent(ManyToMany.class)) {
//          if (field.getAnnotation(ManyToMany.class).mappedBy().equals("")) {
//            modelProperty.isRelation = true;
//            modelProperty.isMultiple = true;
//            modelProperty.relationType = fieldType;
//            modelProperty.choices = new Model.Choices() {
//
//              @SuppressWarnings("unchecked")
//              public List<Object> list()
//              {
//                return JPA.em().createQuery("from " + fieldType.getName()).getResultList();
//              }
//            };
//          }
//        }
//      }
//      if (field.getType().isEnum()) {
//        modelProperty.choices = new Model.Choices() {
//
//          @SuppressWarnings("unchecked")
//          public List<Object> list()
//          {
//            return (List<Object>) Arrays.asList(field.getType().getEnumConstants());
//          }
//        };
//      }
//      modelProperty.name = field.getName();
//      if (field.getType().equals(String.class)) {
//        modelProperty.isSearchable = true;
//      }
//      if (field.isAnnotationPresent(GeneratedValue.class)) {
//        modelProperty.isGenerated = true;
//      }
//      return modelProperty;
//    }
//  }
}
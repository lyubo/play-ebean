package play.modules.ebean;

import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import com.avaje.ebean.EbeanServer;

public class EbeanContext
{
  public static ThreadLocal<EbeanContext> local      = new ThreadLocal<EbeanContext>();

  private EbeanServer                     server;
  private Map<String, Object>             properties = new HashMap<String, Object>();

  private EbeanContext(EbeanServer server, Object... props)
  {
    this.server = server;
    for (int i = 0; i < props.length - 1; i += 2)
      properties.put(props[i].toString(), props[i + 1]);
  }

  public static EbeanContext set(EbeanServer server, Object... properties)
  {
    EbeanContext ctx = local.get();
    if (ctx != null && ctx.server != null && ctx.server.currentTransaction() != null) {
      ctx.server.endTransaction();
    }
    ctx = new EbeanContext(server, properties);
    local.set(ctx);
    return ctx;
  }

  public static EbeanContext set(String name, DataSource ds, Object... properties)
  {
    return set(EbeanPlugin.checkServer(name, ds), properties);
  }

  public static EbeanContext get()
  {
    EbeanContext result = local.get();
    if (result == null) {
      throw new IllegalStateException("The Ebean context is not initialized.");
    }
    return result;
  }

  public static EbeanServer server()
  {
    return get().server;
  }

  public static Object getProperty(String propertyName)
  {
    return get().properties.get(propertyName);
  }

  public static void setProperty(String propertyName, Object object)
  {
    get().properties.put(propertyName, object);
  }
}

package play.modules.ebean;

import java.io.IOException;
import java.io.InputStream;
import java.lang.instrument.ClassFileTransformer;

import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtMethod;
import javassist.CtNewConstructor;
import play.Logger;
import play.Play;
import play.classloading.ApplicationClasses.ApplicationClass;
import play.classloading.enhancers.Enhancer;
import play.exceptions.UnexpectedException;

import com.avaje.ebean.enhance.agent.ClassBytesReader;
import com.avaje.ebean.enhance.agent.InputStreamTransform;

public class EbeanEnhancer extends Enhancer
{
  static ClassFileTransformer transformer = new PlayAwareTransformer(new PlayClassBytesReader(), "transientInternalFields=true;debug=0");

  
  public void enhanceThisClass(ApplicationClass applicationClass) throws Exception
  {
    // Ebean transformations
    byte[] buffer = transformer.transform(Play.classloader, applicationClass.name, null, null, applicationClass.enhancedByteCode);
    if (buffer != null) applicationClass.enhancedByteCode = buffer;

    CtClass ctClass = makeClass(applicationClass);
 
    if (!ctClass.subtypeOf(classPool.get("play.modules.ebean.EbeanSupport"))) {
      // We don't want play style enhancements to happen ro classes other than subclasses of EbeanSupport
      return;
    }

    // Enhance only JPA entities
    if (!hasAnnotation(ctClass, "javax.persistence.Entity")) {
      return;
    }

    String entityName = ctClass.getName();

    // Add a default constructor if needed
    try {
      boolean hasDefaultConstructor = false;
      for (CtConstructor constructor : ctClass.getConstructors()) {
        if (constructor.getParameterTypes().length == 0) {
          hasDefaultConstructor = true;
          break;
        }
      }
      if (!hasDefaultConstructor && !ctClass.isInterface()) {
        CtConstructor defaultConstructor = CtNewConstructor.make("private " + ctClass.getSimpleName() + "() {}", ctClass);
        ctClass.addConstructor(defaultConstructor);
      }
    } catch (Exception e) {
      Logger.error(e, "Error in EbeanEnhancer");
      throw new UnexpectedException("Error in EbeanEnhancer", e);
    }

    // create     
    CtMethod create = CtMethod.make("public static play.modules.ebean.EbeanSupport create(String name, play.mvc.Scope.Params params) { return create(" + entityName + ".class,name, params); }",ctClass);
    ctClass.addMethod(create);

    // deleteAll
    CtMethod deleteAll = CtMethod.make("public static int deleteAll() { return  ebean().createUpdate(" + entityName + ".class, \"delete from " + ctClass.getSimpleName() + "\").execute(); }", ctClass);
    ctClass.addMethod(deleteAll);

    // findAll
    CtMethod findAll = CtMethod.make("public static java.util.List findAll() { return ebean().find(" + entityName + ".class).findList(); }", ctClass);
    ctClass.addMethod(findAll);

    // findById
    CtMethod findById = CtMethod.make("public static play.modules.ebean.EbeanSupport findById(Object id) { return (" + entityName + ") ebean().find(" + entityName + ".class, id); }", ctClass);
    ctClass.addMethod(findById);

    // findUnique
    CtMethod fundUnique = CtMethod.make("public static play.modules.ebean.EbeanSupport findUnique(String property, Object value, Object[] moreParams) { return findUnique(" + entityName + ".class, property,value, moreParams); }", ctClass);
    ctClass.addMethod(fundUnique);
    
    // Done.
    applicationClass.enhancedByteCode = ctClass.toBytecode();
    ctClass.defrost();

  }

  static class PlayClassBytesReader implements ClassBytesReader
  {

    public byte[] getClassBytes(String className, ClassLoader classLoader)
    {
      ApplicationClass ac = Play.classes.getApplicationClass(className.replace("/", "."));
      return ac != null ? ac.enhancedByteCode : getBytesFromClassPath(className);
    }

    private byte[] getBytesFromClassPath(String className)
    {
      String resource = className + ".class";
      byte[] classBytes = null;
      InputStream is = Play.classloader.getResourceAsStream(resource);
      try {
        classBytes = InputStreamTransform.readBytes(is);
      } catch (IOException e) {
        throw new RuntimeException("IOException reading bytes for " + className, e);
      } finally {
        if (is != null) {
          try {
            is.close();
          } catch (IOException e) {
            throw new RuntimeException("Error closing InputStream for " + className, e);
          }
        }
      }
      return classBytes;
    }

  }

}

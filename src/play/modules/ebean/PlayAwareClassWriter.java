package play.modules.ebean;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import play.Play;
import play.classloading.ApplicationClasses.ApplicationClass;

import com.avaje.ebean.enhance.asm.ClassReader;
import com.avaje.ebean.enhance.asm.ClassWriter;

public class PlayAwareClassWriter extends ClassWriter
{

  public PlayAwareClassWriter()
  {
    super(COMPUTE_FRAMES + COMPUTE_MAXS);
  }

  @Override
  protected String getCommonSuperClass(String type1, String type2)
  {
    try {
      // First put all super classes of type1, including type1 (starting with type2 is equivalent)
      Set<String> superTypes1 = new HashSet<String>();
      String s = type1;
      superTypes1.add(s);
      while (!"java/lang/Object".equals(s)) {
        s = getSuperType(s);
        superTypes1.add(s);
      }
      // Then check type2 and each of it's super classes in sequence if it is in the set
      // First match is the common superclass.
      s = type2;
      while (true) {
        if (superTypes1.contains(s)) return s;
        s = getSuperType(s);
      }
    } catch (Exception e) {
      throw new RuntimeException(e.toString());
    }
  }

  private String getSuperType(String type) throws ClassNotFoundException
  {
    ApplicationClass ac = Play.classes.getApplicationClass(type.replace('/', '.'));
    try {
      return ac != null ? new ClassReader(ac.enhancedByteCode).getSuperName() : new ClassReader(type).getSuperName();
    } catch (IOException e) {
      throw new ClassNotFoundException(type);
    }
  }

}

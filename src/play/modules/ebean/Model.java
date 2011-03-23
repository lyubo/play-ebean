package play.modules.ebean;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

@MappedSuperclass
public class Model extends EbeanSupport
{

  @Id
  @GeneratedValue
  public Long id;

  public Long getId()
  {
    return id;
  }

  @Override
  public Object _key()
  {
    return getId();
  }

}

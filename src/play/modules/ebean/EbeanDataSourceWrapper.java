package play.modules.ebean;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

// Forces autoCommit=false for database connections. Not supported by configuration in c3p0
public class EbeanDataSourceWrapper implements DataSource
{
  private DataSource                target;

  public EbeanDataSourceWrapper(DataSource ds)
  {
    this.target = ds;
  }

  public Connection getConnection() throws SQLException
  {
    Connection result = target.getConnection();
    if (result.getAutoCommit()) result.setAutoCommit(false);
    return result;
  }

  public Connection getConnection(String username, String password) throws SQLException
  {
    Connection result = target.getConnection(username,password);
    if (result.getAutoCommit()) result.setAutoCommit(false);
    return result;
  }

  public PrintWriter getLogWriter() throws SQLException
  {
    return target.getLogWriter();
  }

  public int getLoginTimeout() throws SQLException
  {
    return target.getLoginTimeout();
  }

  public void setLogWriter(PrintWriter out) throws SQLException
  {
    target.setLogWriter(out);
  }

  public void setLoginTimeout(int seconds) throws SQLException
  {
    target.setLoginTimeout(seconds);
  }

  public boolean isWrapperFor(Class<?> ds) throws SQLException
  {
    return target.getClass().equals(ds.getClass());
  }

  @SuppressWarnings("unchecked")
  public <T> T unwrap(Class<T> ds) throws SQLException
  {
    return  isWrapperFor(ds) ? (T) target : null;
  }

}

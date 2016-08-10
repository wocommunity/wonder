package er.extensions.security;

/**
 * <span class="ja">
 * このクラスはアクセス権限に使用します
 * </span>
 * 
 * @author ishimoto
 *
 */
public interface IERXAccessPermissionInterface {

  public boolean can(String key);

  public boolean canWithDefault(String key, boolean defaultValue);

  public boolean isDeveloper();

  public boolean isAdministrator();

}

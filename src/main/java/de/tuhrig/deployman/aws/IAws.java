/**
 * DeployMan # Thomas Uhrig (Stuttgart, 2014) # www.tuhrig.de
 */
package de.tuhrig.deployman.aws;

/**
 * @author tuhrig
 */
public interface IAws<T> {
  public T getClient();
}

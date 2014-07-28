/**
 * DeployMan # Thomas Uhrig (Stuttgart, 2014) # www.tuhrig.de
 */
package com.informatica.pim.cloud.repo;

/**
 * @author tuhrig
 */
public interface IRepository {
  public boolean exists();

  public void init();

  public void printInfo();
}

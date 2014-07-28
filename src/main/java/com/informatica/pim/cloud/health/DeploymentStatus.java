/**
 * DeployMan # Thomas Uhrig (Stuttgart, 2014) # www.tuhrig.de
 */
package com.informatica.pim.cloud.health;

/**
 * The deployment is devided into several steps. A step can be PENDING (it wasn't started yet),
 * INPROGRESS (it is running just right now) or DONE (it finished).
 * 
 * @author tuhrig
 */
public enum DeploymentStatus {
  PENDING, INPROGRESS, DONE
}

/**
 * DeployMan # Thomas Uhrig (Stuttgart, 2014) # www.tuhrig.de
 */
package com.informatica.pim.cloud.launch;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author tuhrig
 */
public class LaunchJobStore {
  private static Map<String, LaunchJob> map = new HashMap<>();

  public static Collection<LaunchJob> getJobs() {
    return map.values();
  }

  public static LaunchJob getJob(String id) {
    return map.get(id);
  }

  public static LaunchJob createNewJob() {
    LaunchJob job = new LaunchJob();
    LaunchJobStore.add(job);
    return job;
  }

  public static void add(LaunchJob job) {
    map.put(job.getId(), job);
  }
}

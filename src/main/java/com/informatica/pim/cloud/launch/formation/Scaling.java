/**
 * DeployMan # Thomas Uhrig (Stuttgart, 2014) # www.tuhrig.de
 */
package com.informatica.pim.cloud.launch.formation;

import com.google.gson.annotations.SerializedName;

/**
 * @author tuhrig
 */
public class Scaling {
  @SerializedName("name")
  private String name;

  @SerializedName("lb")
  private String lb;

  @SerializedName("group")
  private String group;

  @SerializedName("policy")
  private String policy;

  @SerializedName("alarm")
  private String alarm;

  @SerializedName("zone")
  private String zone;

  @SerializedName("min")
  private int min;

  @SerializedName("max")
  private int max;

  public String getZone() {
    return this.zone;
  }

  public void setZone(String zone) {
    this.zone = zone;
  }

  public int getMin() {
    return this.min;
  }

  public void setMin(int min) {
    this.min = min;
  }

  public int getMax() {
    return this.max;
  }

  public void setMax(int max) {
    this.max = max;
  }

  public String getAlarm() {
    return this.alarm;
  }

  public void setAlarm(String alarm) {
    this.alarm = alarm;
  }

  public String getPolicy() {
    return this.policy;
  }

  public void setPolicy(String policy) {
    this.policy = policy;
  }

  public String getGroup() {
    return this.group;
  }

  public void setGroup(String group) {
    this.group = group;
  }

  public String getName() {
    return this.name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getLb() {
    return this.lb;
  }

  public void setLb(String lb) {
    this.lb = lb;
  }
}

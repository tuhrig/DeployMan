/**
 * DeployMan # Thomas Uhrig (Stuttgart, 2014) # www.tuhrig.de
 */
package com.informatica.pim.cloud.health;

import com.github.dockerjava.client.DockerClient;
import com.github.dockerjava.client.DockerException;
import com.github.dockerjava.client.model.Container;
import com.github.dockerjava.client.model.ContainerInspectResponse;

/**
 * @author tuhrig
 */
public class DockerContainerInfo {
  private ContainerInspectResponse containerInspectResponse;
  private Container container;

  public DockerContainerInfo(DockerClient client, Container container) throws DockerException {
    ContainerInspectResponse containerInspectResponse =
        client.inspectContainerCmd(container.getId()).exec();

    addDockerContainerInfo(containerInspectResponse);
    addContainer(container);
  }

  public void addContainer(Container container) {
    this.container = container;
  }

  public void addDockerContainerInfo(ContainerInspectResponse containerInspectResponse) {
    this.containerInspectResponse = containerInspectResponse;
  }

  public ContainerInspectResponse getContainerInspectResponse() {
    return this.containerInspectResponse;
  }

  public void setContainerInspectResponse(ContainerInspectResponse containerInspectResponse) {
    this.containerInspectResponse = containerInspectResponse;
  }

  public Container getContainer() {
    return this.container;
  }

  public void setContainer(Container container) {
    this.container = container;
  }
}

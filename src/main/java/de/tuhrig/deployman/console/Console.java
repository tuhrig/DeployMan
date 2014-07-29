/**
 * DeployMan # Thomas Uhrig (Stuttgart, 2014) # www.tuhrig.de
 */
package de.tuhrig.deployman.console;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.rds.model.DBInstance;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.bethecoder.ascii_table.impl.CollectionASCIITableAware;
import com.bethecoder.ascii_table.spec.IASCIITableAware;
import com.github.dockerjava.client.model.Container;
import com.github.dockerjava.client.model.Image;
import com.github.dockerjava.client.model.Info;

import de.tuhrig.deployman.health.MachineStatus;
import de.tuhrig.deployman.health.Status;
import de.tuhrig.deployman.model.Size;
import de.tuhrig.deployman.repo.LocaleRepository;
import de.tuhrig.deployman.repo.RemoteRepository;
import static de.tuhrig.deployman.DeployMan.*;

/**
 * This class is used to write messages. It takes an IConsole instance (which could write to STDOUT,
 * a file or StringBuilder for example) and provides methods to write messages, exceptions and
 * tables. It is used extensivly in the command line interface to present information to the user.
 * 
 * @author tuhrig
 */
public class Console {
  private static final SimpleDateFormat sdf = new SimpleDateFormat("dd/MMM/yyyy HH:mm:ss"); //$NON-NLS-1$

  public static final String NAME = "Name"; //$NON-NLS-1$
  public static final String SIZE = "Size"; //$NON-NLS-1$
  public static final String MODIFIED = "Modified"; //$NON-NLS-1$
  public static final String CONFIG = "Config"; //$NON-NLS-1$
  public static final String ID = "ID"; //$NON-NLS-1$
  public static final String PUBLIC_DNS = "Public DNS"; //$NON-NLS-1$
  public static final String PUBLIC_IP = "Public IP"; //$NON-NLS-1$
  public static final String STATE = "State"; //$NON-NLS-1$
  public static final String COMMAND = "Command"; //$NON-NLS-1$
  public static final String IMAGE = "Image"; //$NON-NLS-1$
  public static final String IMAGES = "Images"; //$NON-NLS-1$
  public static final String CONTAINERS = "Containers"; //$NON-NLS-1$
  public static final String STATUS = "Status"; //$NON-NLS-1$
  public static final String LXC = "LXC"; //$NON-NLS-1$
  public static final String TAG = "TAG"; //$NON-NLS-1$
  public static final String STEP = "Step"; //$NON-NLS-1$
  public static final String ENGINE = "Engine"; //$NON-NLS-1$
  public static final String STORAGE = "Storage"; //$NON-NLS-1$
  public static final String ENDPOINT = "Endpoint"; //$NON-NLS-1$
  public static final String TAGS = "Tags"; //$NON-NLS-1$

  private IConsole console;

  public Console(IConsole console) {
    this.console = console;
  }

  public void printFoldersOfFolder(File folder) {
    write("Folder: " + folder); //$NON-NLS-1$
    printFiles(new LocaleRepository().getFoldersOfFolder(folder));
  }

  public void printFilesOfFolder(File folder) {
    write("Folder: " + folder); //$NON-NLS-1$
    printFiles(new LocaleRepository().getFilesOfFolder(folder));
  }

  public void printFiles(List<File> list) {
    IASCIITableAware tableModel = getTableModel(NAME, SIZE, MODIFIED);
    for (File file : list)
      addToModel(tableModel, file.getName(), new Size(file), lastModified(file));
    printTable(tableModel);
  }

  public void printS3ObjectSummaries(List<S3ObjectSummary> remoteImages) {
    IASCIITableAware tableModel = getTableModel(NAME, SIZE, MODIFIED);
    for (S3ObjectSummary file : remoteImages)
      addToModel(tableModel, justName(file.getKey()), new Size(file), lastModified(file));
    printTable(tableModel);
  }

  public void printRemoteConfig(List<String> remoteConfigs) {
    IASCIITableAware tableModel = getTableModel(CONFIG, SIZE);
    for (String remoteConfig : remoteConfigs)
      addToModel(tableModel, justName(remoteConfig),
          new Size(new RemoteRepository().getSizeOfRemoteFolder(remoteConfig)));
    printTable(tableModel);
  }

  public void printInstanceInfo(Instance instance) {
    List<Instance> instances = new ArrayList<>();
    instances.add(instance);
    printEc2Instances(instances);
  }

  public void printEc2Instances(List<Instance> ec2Instances) {
    IASCIITableAware tableModel = getTableModel(TAGS, ID, STATE, PUBLIC_DNS, PUBLIC_IP);

    for (Instance ec2 : ec2Instances)
      addToModel(tableModel, ec2.getTags(), ec2.getInstanceId(), ec2.getState().getName(),
          ec2.getPublicDnsName(), ec2.getPublicIpAddress());

    printTable(tableModel);
  }

  public void printContainers(List<Container> containers) {
    IASCIITableAware tableModel = getTableModel(ID, IMAGE, COMMAND, STATUS);
    for (Container container : containers) {
      String shortId = container.getId().substring(0, 5) + "..."; //$NON-NLS-1$
      addToModel(tableModel, shortId, container.getImage(), container.getCommand(),
          container.getStatus());
    }
    printTable(tableModel);
  }

  public void printImages(List<Image> images) {
    IASCIITableAware tableModel = getTableModel(ID, TAG, SIZE);
    for (Image image : images)
      addToModel(tableModel, image.getId(), image.getTag(), image.getSize());
    printTable(tableModel);
  }

  public void printDockerInfo(Info info) {
    IASCIITableAware tableModel = getTableModel(IMAGES, CONTAINERS, LXC);
    addToModel(tableModel, info.getImages(), info.getContainers(), info.getLxcVersion());
    printTable(tableModel);
  }

  public void printHealth(String instanceId) {
    MachineStatus machineStatus = MachineStatus.getMachineStatus(instanceId);
    IASCIITableAware tableModel = getTableModel(STEP, STATUS);
    for (Status status : machineStatus.getSetupStates())
      addToModel(tableModel, status.getName(), status.getStatus());
    printTable(tableModel);
  }

  public void printDatabase(DBInstance dbInstance) {
    List<DBInstance> instances = new ArrayList<>();
    instances.add(dbInstance);
    printDatabases(instances);
  }

  public void printDatabases(List<DBInstance> databases) {
    IASCIITableAware tableModel =
        getTableModel(NAME, STATUS, ID, ENGINE, MODIFIED, STORAGE, ENDPOINT);
    for (DBInstance db : databases)
      addToModel(tableModel, db.getDBName(), db.getDBInstanceStatus(),
          db.getDBInstanceIdentifier(), db.getEngine(), db.getInstanceCreateTime(),
          db.getAllocatedStorage(), db.getEndpoint());
    printTable(tableModel);
  }

  public void write(Object message) {
    this.console.write(message);
  }

  public void writeNl(String message) {
    this.console.writeNl(message);
  }

  public void newLine() {
    this.console.newLine();
  }

  public void exception(Exception exception, String message) {
    this.console.exception(exception, message);
  }

  private void printTable(IASCIITableAware tableModel) {
    this.console.printTable(tableModel);
  }

  private void addToModel(IASCIITableAware tableModel, Object... values) {
    List<List<Object>> data = tableModel.getData();
    List<Object> object = new ArrayList<>();
    for (Object value : values)
      object.add(value);
    data.add(object);
  }

  private IASCIITableAware getTableModel(String... header) {
    List<Object> empty = getListWithEmptyElement();
    CollectionASCIITableAware<Object> asciiTableAware =
        new CollectionASCIITableAware<Object>(empty, header);

    List<List<Object>> data = asciiTableAware.getData();
    data.clear();

    return asciiTableAware;
  }

  private Object lastModified(File file) {
    return sdf.format(file.lastModified());
  }

  private Object lastModified(S3ObjectSummary file) {
    return sdf.format(file.getLastModified());
  }

  private List<Object> getListWithEmptyElement() {
    List<Object> empty = new ArrayList<>();
    empty.add(BLANK);
    return empty;
  }

  /**
   * Takes a string containing "/" (forward-slashes) which indicates a folder structure and returns
   * only the last part (behind the last "/") of this string. In: some/folder Out: folder
   */
  static String justName(String key) {
    if (key.contains(SLASH))
      return key.substring(key.lastIndexOf(SLASH) + 1, key.length());
    return key;
  }
}

/**
 * DeployMan # Thomas Uhrig (Stuttgart, 2014) # www.tuhrig.de
 */
package de.tuhrig.deployman.launch;

import static de.tuhrig.deployman.DeployMan.EC2_INSTANCE_KEY;
import static de.tuhrig.deployman.DeployMan.REPO_PROFILE;
import static de.tuhrig.deployman.DeployMan.SLASH;
import static de.tuhrig.deployman.DeployMan.getUserProperty;
import static de.tuhrig.deployman.DeployMan.readUserProperties;
import static de.tuhrig.deployman.DeployMan.sdf;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.apache.tools.ant.DefaultLogger;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;

import com.amazonaws.services.autoscaling.AmazonAutoScalingClient;
import com.amazonaws.services.autoscaling.model.AlreadyExistsException;
import com.amazonaws.services.autoscaling.model.AutoScalingInstanceDetails;
import com.amazonaws.services.autoscaling.model.CreateAutoScalingGroupRequest;
import com.amazonaws.services.autoscaling.model.CreateLaunchConfigurationRequest;
import com.amazonaws.services.autoscaling.model.InstanceMonitoring;
import com.amazonaws.services.autoscaling.model.PutScalingPolicyRequest;
import com.amazonaws.services.autoscaling.model.PutScalingPolicyResult;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchClient;
import com.amazonaws.services.cloudwatch.model.ComparisonOperator;
import com.amazonaws.services.cloudwatch.model.Dimension;
import com.amazonaws.services.cloudwatch.model.PutMetricAlarmRequest;
import com.amazonaws.services.cloudwatch.model.StandardUnit;
import com.amazonaws.services.cloudwatch.model.Statistic;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.BlockDeviceMapping;
import com.amazonaws.services.ec2.model.IamInstanceProfileSpecification;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.amazonaws.services.rds.model.CreateDBInstanceRequest;
import com.amazonaws.services.rds.model.DBInstance;
import com.amazonaws.services.rds.model.Endpoint;
import com.google.gson.Gson;

import de.tuhrig.deployman.DeployMan;
import de.tuhrig.deployman.aws.AutoScaling;
import de.tuhrig.deployman.aws.CloudWatch;
import de.tuhrig.deployman.aws.Ec2;
import de.tuhrig.deployman.aws.Rds;
import de.tuhrig.deployman.console.Console;
import de.tuhrig.deployman.launch.enums.Comment;
import de.tuhrig.deployman.launch.enums.Script;
import de.tuhrig.deployman.launch.enums.Variable;
import de.tuhrig.deployman.launch.formation.Container;
import de.tuhrig.deployman.launch.formation.Credential;
import de.tuhrig.deployman.launch.formation.Database;
import de.tuhrig.deployman.launch.formation.Formation;
import de.tuhrig.deployman.launch.formation.Machine;
import de.tuhrig.deployman.launch.formation.Scaling;
import de.tuhrig.deployman.repo.LocaleRepository;

/**
 * This class launches formations. It can either launch a EC2 virtual machine setup or a RDS
 * database setup. It can also launch a formation file which contains both, an EC2 setup and a RDS
 * setup.
 * 
 * @author tuhrig
 */
public class Launcher {
  public static final String HOME = "/home/ubuntu"; //$NON-NLS-1$
  public static final String DEPLOYMENT_LOG_FILE = HOME + "/deployman.log"; //$NON-NLS-1$
  public static final String DOCKER_LOG_FILE = HOME + "/docker.log"; //$NON-NLS-1$

  private Ec2 ec2Client = new Ec2();
  private Console console;

  public Launcher() {
    this(DeployMan.console);
  }

  public Launcher(Console console) {
    this.console = console;
  }

  public void run(String formationFile) {
    Formation formation = Formation.read(formationFile);
    run(formation);
  }

  public void run(Formation formation) {
    this.console.writeNl("Start formation " + formation.getFile()); //$NON-NLS-1$

    DBInstance dbInstance = null;
    Instance instance = null;
    List<Instance> instances = null;

    if (formation.hasDatabaseDefinition())
      dbInstance = runDatabaseLaunch(formation);

    // if there is a auto scaling group defined in the
    // formation file, we prever it over the machine
    // definition!
    if (formation.hasAutoScalingDefinition())
      instances = runAutoScalingLaunch(formation);
    else if (formation.hasInstanceDefinition())
      instance = runVirtualMachineLaunch(formation);

    // we print the results at the end to present a nice
    // overview to the user after the launch processes
    // have finished
    if (dbInstance != null)
      this.console.printDatabase(dbInstance);

    if (instance != null)
      this.console.printInstanceInfo(instance);

    if (instances != null)
      this.console.printEc2Instances(instances);
  }

  /**
   * Starts a auto scaling launch configuration. This method is a little but complicated, since the
   * process to start a auto scaling configuration involves some steps: - create a launch
   * configuration which defines the machines to start - create an auto scaling definition which
   * defines how many machines should be started if something happens. - create an auto scaling
   * policy to which we add a metric - create the metric which is checked by the auto scaling -
   * print the starting instances
   */
  private List<Instance> runAutoScalingLaunch(Formation formation) {
    this.console.write("Run auto scaling..."); //$NON-NLS-1$

    Machine machine = formation.getMachine();
    Scaling scaling = machine.getScaling();

    AmazonAutoScalingClient autoScaling = new AutoScaling().getClient();

    try {
      CreateLaunchConfigurationRequest request = createLaunchConfigurationRequest(formation);
      autoScaling.createLaunchConfiguration(request);
      this.console.write("Created launch configuration " + scaling.getName()); //$NON-NLS-1$
    } catch (AlreadyExistsException e) {
      this.console.write("Launch configuration " + scaling.getName() + " already exists"); //$NON-NLS-1$ //$NON-NLS-2$
    }

    //
    //
    //

    try {
      CreateAutoScalingGroupRequest request = createAutoScalingRequest(scaling);
      autoScaling.createAutoScalingGroup(request);
      this.console.write("Created auto scaling group " + scaling.getGroup()); //$NON-NLS-1$
    } catch (AlreadyExistsException e) {
      this.console.write("Auto scaling group " + scaling.getGroup() + " already exists"); //$NON-NLS-1$ //$NON-NLS-2$
    }

    //
    //
    //

    PutScalingPolicyRequest request =
        new PutScalingPolicyRequest().withAutoScalingGroupName(scaling.getGroup())
            .withPolicyName(scaling.getPolicy()).withScalingAdjustment(1)
            .withAdjustmentType("ChangeInCapacity"); //$NON-NLS-1$

    PutScalingPolicyResult result = autoScaling.putScalingPolicy(request);

    this.console.write("Put scaling policy " + scaling.getPolicy()); //$NON-NLS-1$

    //
    //
    //

    // Scale Up

    Dimension dimension = new Dimension().withName("AutoScalingGroupName") //$NON-NLS-1$
        .withValue(scaling.getGroup());

    List<String> actions = new ArrayList<>();
    actions.add(result.getPolicyARN());

    PutMetricAlarmRequest upRequest =
        new PutMetricAlarmRequest()
            .withAlarmName(scaling.getAlarm())
            .withMetricName("CPUUtilization") //$NON-NLS-1$
            .withDimensions(dimension)
            .withNamespace("AWS/EC2") //$NON-NLS-1$
            .withComparisonOperator(ComparisonOperator.GreaterThanThreshold)
            .withStatistic(Statistic.Average).withUnit(StandardUnit.Percent).withThreshold(60d)
            .withPeriod(300).withEvaluationPeriods(2).withAlarmActions(actions);

    AmazonCloudWatchClient cloudWatch = new CloudWatch().getClient();
    cloudWatch.putMetricAlarm(upRequest);

    this.console.write("Put alarm " + scaling.getAlarm()); //$NON-NLS-1$
    this.console.newLine();

    List<Instance> instances = new ArrayList<>();

    for (AutoScalingInstanceDetails instance : autoScaling.describeAutoScalingInstances()
        .getAutoScalingInstances()) {

      if (instance.getAutoScalingGroupName().equals(scaling.getGroup())) {

        instances.add(new Ec2().getEC2InstanceById(instance.getInstanceId()));
      }
    }

    return instances;
  }

  private CreateLaunchConfigurationRequest createLaunchConfigurationRequest(Formation formation) {
    Machine machine = formation.getMachine();
    Scaling scaling = machine.getScaling();

    Properties generalProperties = getMetaInformationProperties(formation);
    CloudInitScript initScript = getParallelCloudInitScript(machine, generalProperties);
    IamInstanceProfileSpecification profil =
        this.ec2Client.getIamInstanceProfileSpecification(getUserProperty(REPO_PROFILE));

    return new CreateLaunchConfigurationRequest().withLaunchConfigurationName(scaling.getName())
        .withInstanceType(machine.getInstanceType()).withImageId(machine.getImageId())
        .withUserData(initScript.renderAsBase64()).withSecurityGroups(machine.getSecurityGroup())
        .withIamInstanceProfile(profil.getName())
        // the name NOT the ARN!
        .withKeyName(getUserProperty(EC2_INSTANCE_KEY))
        .withInstanceMonitoring(new InstanceMonitoring().withEnabled(false));

  }

  private CreateAutoScalingGroupRequest createAutoScalingRequest(Scaling scaling) {
    return new CreateAutoScalingGroupRequest().withAutoScalingGroupName(scaling.getGroup())
        .withLaunchConfigurationName(scaling.getName()).withAvailabilityZones(scaling.getZone())
        .withMaxSize(scaling.getMax()).withMinSize(scaling.getMin())
        .withLoadBalancerNames(scaling.getLb()).withHealthCheckType("EC2") //$NON-NLS-1$
        .withHealthCheckGracePeriod(300).withDefaultCooldown(600);
  }

  private DBInstance runDatabaseLaunch(Formation formation) {
    Rds rdsClient = new Rds();

    // if ( true )
    // return null;

    Database database = formation.getDatabase();
    DBInstance dbInstance = null;

    if (rdsClient.databaseExists(database.getInstanceIdentifier())) {
      this.console.writeNl("Database already exists"); //$NON-NLS-1$
      dbInstance = rdsClient.getDatabase(database.getInstanceIdentifier());
    } else {
      this.console.writeNl("Create database"); //$NON-NLS-1$

      CreateDBInstanceRequest request =
          new CreateDBInstanceRequest().withEngine(database.getEngine())
              .withEngineVersion(database.getEngineVersion())
              .withLicenseModel(database.getLicense())
              .withDBInstanceClass(database.getInstanceClass()).withMultiAZ(database.getMultiAz())
              .withAutoMinorVersionUpgrade(database.getAutoMinorVersionUpgrade())
              .withAllocatedStorage(database.getAllocatedStorage())
              .withDBInstanceIdentifier(database.getInstanceIdentifier())
              .withMasterUsername(database.getUsername())
              .withMasterUserPassword(database.getPassword()).withDBName(database.getName())
              .withPort(database.getPort());
      // .withVpcSecurityGroupIds( database.getSecurityGroup() );

      dbInstance = rdsClient.getClient().createDBInstance(request);
    }

    this.console.writeNl("Starting..."); //$NON-NLS-1$

    waitForDatabaseState(dbInstance.getDBInstanceIdentifier(), "available"); //$NON-NLS-1$

    // sleep 10 seconds to get it really ready...
    // no idea why the state 'available isn't enough
    sleep(10000);

    dbInstance = rdsClient.getDatabase(dbInstance.getDBInstanceIdentifier());
    runSetup(dbInstance, database);
    return dbInstance;
  }

  private Instance runVirtualMachineLaunch(Formation formation) {
    Properties generalProperties = getMetaInformationProperties(formation);
    Machine machine = formation.getMachine();
    CloudInitScript initScript = getParallelCloudInitScript(machine, generalProperties);

    initScript.save();
    this.console.newLine();

    // if ( true )
    // return null;

    BlockDeviceMapping volumn = this.ec2Client.getBlockDeviceMapping("/dev/sda1", 20); //$NON-NLS-1$
    IamInstanceProfileSpecification profil =
        this.ec2Client.getIamInstanceProfileSpecification(getUserProperty(REPO_PROFILE));

    RunInstancesRequest request =
        new RunInstancesRequest().withInstanceType(machine.getInstanceType())
            .withImageId(machine.getImageId()).withIamInstanceProfile(profil).withMinCount(1)
            .withMaxCount(1).withBlockDeviceMappings(volumn)
            .withUserData(initScript.renderAsBase64())
            .withSecurityGroupIds(machine.getSecurityGroup())
            .withKeyName(getUserProperty(EC2_INSTANCE_KEY));

    String instanceId = runInstance(request);

    this.console.writeNl("Starting..."); //$NON-NLS-1$

    waitForInstanceState(instanceId, "running"); //$NON-NLS-1$
    assigneIpIfAvailable(instanceId, machine);
    assigneName(instanceId, machine);

    return this.ec2Client.getEC2InstanceById(instanceId);
  }

  private void assigneName(String instanceId, Machine machine) {
    // tag the machine with its name which must not be unique
    new Ec2().tag(instanceId, machine.getName());
  }

  private String runInstance(RunInstancesRequest request) {
    AmazonEC2 ec2 = this.ec2Client.getClient();

    RunInstancesResult runInstances = ec2.runInstances(request);

    List<Instance> instances = runInstances.getReservation().getInstances();

    return instances.get(0).getInstanceId();
  }

  /**
   * Returns a CloudInit script which performs the complete initialization of a machine. The script
   * will execute all steps in a sequential order after each other. Note: This method is currently
   * not used since it is slower as the parallel deployment.
   */
  // private CloudInitScript getSequentialCloudInitScript( Machine machine, Properties
  // generalProperties )
  // {
  // CloudInitScript initScript = getBaseScript( machine, generalProperties );
  //
  // // a counter to distinguish different configurations
  // // with the same name
  // int index = 0;
  //
  // for ( Container container : machine.getContainers() )
  // {
  // Properties containerProperties = getContainerProperties( container, index );
  //
  //      CloudInitScript script = new CloudInitScript( "# container init" ).withProperties( containerProperties ) //$NON-NLS-1$
  //                                                                        .withFile( "copy_image.sh" ) //$NON-NLS-1$
  //                                                                        .withFile( "copy_config.sh" ) //$NON-NLS-1$
  //                                                                        .withFile( "docker_load.sh" ) //$NON-NLS-1$
  //                                                                        .withFile( "sync_config.sh" ) //$NON-NLS-1$
  // .withCommand( container.getCommand() );
  //
  // initScript = initScript.withScript( script );
  //
  // // increment the counter
  // index++;
  // }
  //
  //    CloudInitScript script = new CloudInitScript( "# done" ).withFile( "final_message.sh" ); //$NON-NLS-1$ //$NON-NLS-2$
  // return initScript.withScript( script );
  // }

  private void assigneIpIfAvailable(String instanceId, Machine machine) {
    // if the formation has an elastic ip (which is optional)
    // we associate this ip with the instance we created before
    String elasticIp = machine.getElasticIp();
    if (elasticIp != null && !elasticIp.equals("")) //$NON-NLS-1$
      this.ec2Client.associate(instanceId, elasticIp);
  }

  /**
   * Returns a CloudInit script which performs the complete initialization of a machine. The script
   * will execute the donwload and load steps in parallel in an arbitrary order. All containers are
   * started in the order of the formations file, but directly after each other.
   */
  private CloudInitScript getParallelCloudInitScript(Machine machine, Properties generalProperties) {
    CloudInitScript initScript = generateBaseScript(machine, generalProperties);
    CloudInitScript getScript = new CloudInitScript(Comment.COPY_IMAGES_AND_CONFIGS);
    CloudInitScript runScript = new CloudInitScript(Comment.EXEC_IMAGES);

    // a counter to distinguish different configurations
    // with the same name
    int index = 0;

    for (Container container : machine.getContainers()) {
      Properties properties = getContainerProperties(container, index);

      CloudInitScript imageDownloadScript =
          generateDownloadScriptForContainer(container, properties);
      CloudInitScript imageRunScript =
          generateRunScriptForContainer(machine, container, properties);

      getScript.withScript(imageDownloadScript);
      runScript.withScript(imageRunScript);

      // increment the counter
      index++;
    }

    CloudInitScript wait = new CloudInitScript(Comment.WAIT_FOR_SUBSHELL).withFile(Script.WAIT);

    getScript.withScript(wait);

    initScript.withScript(getScript).withScript(runScript);

    CloudInitScript script = new CloudInitScript(Comment.DONE).withFile(Script.FINAL_MESSAGE);
    return initScript.withScript(script);
  }

  private CloudInitScript generateDownloadScriptForContainer(Container container,
      Properties containerProperties) {
    if (container.hasImage())
      return getImageByDownloadFromRegistry(container, containerProperties);
    return getImageByDownloadOfTarball(containerProperties);
  }

  private CloudInitScript generateRunScriptForContainer(Machine machine, Container container,
      Properties containerProperties) {
    if (machine.hasAutoSync()) {
      return new CloudInitScript(Comment.RUN_IMAGE).withProperties(containerProperties)
          .withFile(Script.SYNC_CONFIG).withCommand(container.getCommand());
    }

    return new CloudInitScript(Comment.RUN_IMAGE).withProperties(containerProperties).withCommand(
        container.getCommand());
  }

  private CloudInitScript getImageByDownloadOfTarball(Properties containerProperties) {
    return new CloudInitScript(Comment.DOWNLOAD_TARBALL).withProperties(containerProperties)
        .withFile(Script.COPY_AND_LOAD_TARBALL_PARALLEL).withFile(Script.COPY_CONFIG_PARALLEL);
  }

  private CloudInitScript getImageByDownloadFromRegistry(Container container,
      Properties containerProperties) {
    if (container.hasCredential()) {
      return new CloudInitScript(Comment.DOWNLOAD_IMAGE).withProperties(containerProperties)
          .withFile(Script.DOCKER_LOGIN).withFile(Script.DOWNLOAD_IMAGE)
          .withFile(Script.COPY_CONFIG_PARALLEL);
    }

    return new CloudInitScript(Comment.DOWNLOAD_IMAGE).withProperties(containerProperties)
        .withFile(Script.DOWNLOAD_IMAGE).withFile(Script.COPY_CONFIG_PARALLEL);
  }

  private Properties getContainerProperties(Container setup, int index) {
    Properties properties = readUserProperties();
    addContainerProperties(properties, setup, index);
    return properties;
  }

  private void addContainerProperties(Properties properties, Container container, int index) {
    properties.put(Variable.TARBALL_KEY, container.getTarball());
    properties.put(Variable.IMAGE_NAME, container.getImage());
    properties.put(Variable.CONFIG_KEY, container.getConfig());
    properties.put(Variable.TARBALL_NAME, container.getTarballFileName());
    properties.put(Variable.CONFIG_FOLDER, HOME
        + "/config-" + container.getTarballName() + "-" + index); //$NON-NLS-1$ //$NON-NLS-2$ 
    properties.put(Variable.HOME_DIRECTORY, HOME);

    if (container.hasCredential()) {
      Credential credential = container.getCredential();
      addCredentialProperties(properties, credential);
    }
  }

  private void addCredentialProperties(Properties properties, Credential credential) {
    properties.put(Variable.REGISTRY_EMAIL, credential.getEmail());
    properties.put(Variable.REGISTRY_NAME, credential.getName());
    properties.put(Variable.REGISTRY_PASSWORD, credential.getPassword());
    properties.put(Variable.REGISTRY_SERVER, credential.getServer());
  }

  private CloudInitScript generateBaseScript(Machine machine, Properties generalProperties) {
    CloudInitScript baseScript =
        new CloudInitScript(Comment.BASH).withProperties(generalProperties)
            .withFile(Script.LOG_HEADER).withFile(Script.SET_TIMEZONE).withFile(Script.LOG_INFO);

    if (machine.installDocker())
      baseScript.withFile(Script.INSTALL_DOCKER);

    if (machine.installAwsCli())
      baseScript.withFile(Script.INSTALL_AWSCLI);

    if (machine.openDocker())
      baseScript.withFile(Script.OPEN_DOCKER);

    return baseScript;
  }

  private void waitForDatabaseState(String dbInstance, String state) {
    while (!new Rds().getDatabase(dbInstance).getDBInstanceStatus().equalsIgnoreCase(state)) {
      sleep(4000);
    }
  }

  private void waitForInstanceState(String instanceId, String state) {
    while (!new Ec2().getEC2InstanceById(instanceId).getState().getName().equalsIgnoreCase(state)) {
      sleep(3000);
    }
  }

  private void sleep(long millis) {
    try {
      Thread.sleep(millis);
    } catch (InterruptedException e) {
      //
    }
  }

  /**
   * Runs the given database configuration (~setup) on an AWS database instance. Make sure the setup
   * fits the database (e.g. a MSSQL setup for a MSSQL database).
   */
  private void runSetup(DBInstance dbInstance, Database database) {
    String setup = database.getSetup();
    String buildFilePath = new LocaleRepository().getLocation() + SLASH + setup + "/build.xml"; //$NON-NLS-1$
    Endpoint endpoint = dbInstance.getEndpoint();
    File buildFile = new File(buildFilePath);

    this.console.write("Run database setup " + buildFilePath); //$NON-NLS-1$
    this.console.write("Endpoint " + endpoint); //$NON-NLS-1$

    Project project = new Project();
    project.setUserProperty(Variable.ANT_FILE, buildFile.getAbsolutePath());
    project.setUserProperty(Variable.DEST_ROOT_LOCAL, new LocaleRepository().getLocation() + SLASH
        + "tmp"); //$NON-NLS-1$ 
    project.setUserProperty(Variable.DB_SERVER, endpoint.getAddress());
    project.setUserProperty(Variable.DB_PORT, endpoint.getPort().toString());
    project.setUserProperty(Variable.DB_USER, database.getUsername());
    project.setUserProperty(Variable.DB_PASSWORD, database.getPassword());
    project.setUserProperty(Variable.ENV_NLS_LANG, "American_America.UTF8"); //$NON-NLS-1$ 
    project.setUserProperty(Variable.HEADLESS, "true"); //$NON-NLS-1$
    project.init();

    DefaultLogger consoleLogger = createConsoleLogger();
    project.addBuildListener(consoleLogger);

    ProjectHelper helper = ProjectHelper.getProjectHelper();
    project.addReference("ant.projectHelper", helper); //$NON-NLS-1$
    helper.parse(project, buildFile);
    project.executeTarget(project.getDefaultTarget());

    this.console.newLine();
  }

  private DefaultLogger createConsoleLogger() {
    DefaultLogger consoleLogger = new DefaultLogger();
    consoleLogger.setErrorPrintStream(System.err);
    consoleLogger.setOutputPrintStream(System.out);
    consoleLogger.setMessageOutputLevel(Project.MSG_INFO);
    return consoleLogger;
  }

  public Properties getMetaInformationProperties(Formation formation) {
    Gson gson = new Gson();
    Machine machine = formation.getMachine();
    List<Container> containers = machine.getContainers();

    Properties properties = readUserProperties();
    properties.put(Variable.TIMESTAMP, sdf.format(new Date()));
    properties.put(Variable.HOST, new DeployMan().getLocalHostName());
    properties.put(Variable.FORMATION, gson.toJson(formation));
    properties.put(Variable.CONTAINERS, containers.size());
    properties.put(Variable.HOME_DIRECTORY, HOME);
    properties.put(Variable.LOG_DEPLOYMENT, DEPLOYMENT_LOG_FILE);
    properties.put(Variable.LOG_DOCKER, DOCKER_LOG_FILE);

    return properties;
  }
}

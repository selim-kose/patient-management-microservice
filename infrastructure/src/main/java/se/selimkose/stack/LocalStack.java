package se.selimkose.stack;

import software.amazon.awscdk.*;
import software.amazon.awscdk.services.ec2.InstanceClass;
import software.amazon.awscdk.services.ec2.InstanceSize;
import software.amazon.awscdk.services.ec2.InstanceType;
import software.amazon.awscdk.services.ec2.Vpc;
import software.amazon.awscdk.services.ecs.*;
import software.amazon.awscdk.services.logs.LogGroup;
import software.amazon.awscdk.services.logs.RetentionDays;
import software.amazon.awscdk.services.msk.CfnCluster;
import software.amazon.awscdk.services.rds.*;
import software.amazon.awscdk.services.route53.CfnHealthCheck;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LocalStack extends Stack {
    private final Vpc vpc;
    private final Cluster ecsCluster;

    // Boilerplate constructor for a CDK stack
    public LocalStack(final App scope, final String id, final StackProps props) {
        super(scope, id, props);

        // Create a VPC for the local stack
        this.vpc = createVpc();

        // Create RDS instances for the services
        DatabaseInstance authServiceDB = createDatabaseInstance("AuthServiceDB", "auth-service-db");
        DatabaseInstance patientServiceDB = createDatabaseInstance("PatientServiceDB", "patient-service-db");

        // Create health checks for the RDS instances
        CfnHealthCheck authServiceDBHealthCheck = createDBHealthCheck("AuthServiceHealthCheck", authServiceDB);
        CfnHealthCheck patientServiceDBHealthCheck = createDBHealthCheck("PatientServiceHealthCheck", patientServiceDB);

        CfnCluster kafkaMSKCluster = createMSKKafkaCluster();

        this.ecsCluster = createEcsCluster();

        FargateService authService =
                createFargateService("AuthService",
                        "auth-service",
                        List.of(4005),
                        authServiceDB,
                        Map.of("JWT_SECRET", "KTwIsBqH8R9umdRuYLP+bwYHmCYT9CgAJSULBhAEvyI"));

        authService.getNode().addDependency(authServiceDBHealthCheck);
        authService.getNode().addDependency(authServiceDB);

        FargateService billingService =
                createFargateService("BillingService",
                        "billing-service",
                        List.of(4001,9001),
                        null,
                        null);

        FargateService analyticsService =
                createFargateService("AnalyticsService",
                        "analytics-service",
                        List.of(4002),
                        null,
                        null);

        analyticsService.getNode().addDependency(kafkaMSKCluster);

        FargateService patientService = createFargateService("PatientService",
                "patient-service",
                List.of(4000),
                patientServiceDB,
                Map.of(
                        "BILLING_SERVICE_ADDRESS", "host.docker.internal",
                        "BILLING_SERVICE_GRPC_PORT", "9001"
                ));
        patientService.getNode().addDependency(patientServiceDB);
        patientService.getNode().addDependency(patientServiceDBHealthCheck);
        patientService.getNode().addDependency(billingService);
        patientService.getNode().addDependency(kafkaMSKCluster);
    }

    public static void main(String[] args) {
        App app = new App(AppProps.builder().outdir("./cdk.out").build());

        StackProps stackProps = StackProps.builder().synthesizer(new BootstraplessSynthesizer()).build();

        new LocalStack(app, "localStack", stackProps);
        app.synth();
        System.out.println("App synthesizing in progress...");
    }

    private Vpc createVpc() {
        return Vpc.Builder.create(this, "PatientManagementVpc")
                .vpcName("PatientManagementVpc")
                .maxAzs(2) // Limit to 2 Availability Zones
                .build();
    }

    private DatabaseInstance createDatabaseInstance(String id, String dbName) {
        return DatabaseInstance.Builder.create(this, id)
                .instanceType(InstanceType.of(InstanceClass.BURSTABLE2, InstanceSize.MICRO)) // Use a micro instance type for local development
                .vpc(this.vpc) // Use the VPC created earlier
                .engine(DatabaseInstanceEngine.postgres(
                        PostgresInstanceEngineProps.builder()
                                .version(PostgresEngineVersion.VER_17_2)
                                .build()))
                .allocatedStorage(20)
                .credentials(Credentials.fromGeneratedSecret("user")) // Use a generated secret for the database credentials
                .databaseName(dbName)
                .removalPolicy(RemovalPolicy.DESTROY)   // Destroy the database on stack deletion
                .build();
    }

    /**
     * Creates a health check for the given RDS database instance.
     * This method uses TCP health checks to ensure the database is reachable.
     *
     * @param id     The identifier for the health check resource.
     * @param dbName The RDS database instance to create a health check for.
     * @return A CfnHealthCheck resource configured for the RDS instance.
     */

    private CfnHealthCheck createDBHealthCheck(String id, DatabaseInstance dbName) {
        return CfnHealthCheck.Builder.create(this, id)
                .healthCheckConfig(CfnHealthCheck.HealthCheckConfigProperty.builder()
                        .type("TCP") // Use TCP for health checks
                        .port(Token.asNumber(dbName.getDbInstanceEndpointPort())) // Use the port of the DB instance
                        .ipAddress(dbName.getDbInstanceEndpointAddress()) // Use the endpoint address of the DB instance
                        .requestInterval(30) // Check every 30 seconds
                        .failureThreshold(3) // Fail after 3 consecutive failures
                        .build())
                .build();
    }

    /**
     * Creates an MSK (Managed Streaming for Kafka) cluster.
     * This is a placeholder method and should be called if you want to create a Kafka MSK cluster.
     */
    private CfnCluster createMSKKafkaCluster() {
        return CfnCluster.Builder.create(this, "MSKCluster")
                .clusterName("patient-management-kafka-cluster")
                .kafkaVersion("2.8.0") // Specify the Kafka version
                .numberOfBrokerNodes(1) // Use 3 broker nodes for redundancy
                .brokerNodeGroupInfo(CfnCluster.BrokerNodeGroupInfoProperty.builder()
                        .instanceType("kafka.m5.large") // Use a suitable instance type for brokers
                        .clientSubnets(this.vpc.getPrivateSubnets().stream()
                                .map(subnet -> subnet.getSubnetId())
                                .toList()) // Use private subnets for the brokers
                        .brokerAzDistribution("DEFAULT") // Distribute brokers across AZs
                        .build())
                .build();
    }

    private Cluster createEcsCluster() {
        return Cluster.Builder.create(this, "PatientManagementECSCluster")
                .defaultCloudMapNamespace(CloudMapNamespaceOptions.builder()
                        .name("patient-management.local") // Set a default namespace for Cloud Map
                        .build())
                .vpc(this.vpc) // Use the VPC created earlier
                .build();
    }

    private FargateService createFargateService(String id,
                                                String imageName,
                                                List<Integer> ports,
                                                DatabaseInstance db,
                                                Map<String, String> additionalEnvVars) {

        FargateTaskDefinition taskDefinition =
                FargateTaskDefinition.Builder.create(this, id + "Task")
                        .cpu(256)
                        .memoryLimitMiB(512)
                        .build();

        ContainerDefinitionOptions.Builder containerOptions =
                ContainerDefinitionOptions.builder()
                        .image(ContainerImage.fromRegistry(imageName))
                        .portMappings(ports.stream()
                                .map(port -> PortMapping.builder()
                                        .containerPort(port)
                                        .hostPort(port)
                                        .protocol(Protocol.TCP)
                                        .build())
                                .toList())
                        .logging(LogDriver.awsLogs(AwsLogDriverProps.builder()
                                .logGroup(LogGroup.Builder.create(this, id + "LogGroup")
                                        .logGroupName("/ecs/" + imageName)
                                        .removalPolicy(RemovalPolicy.DESTROY)
                                        .retention(RetentionDays.ONE_DAY)
                                        .build())
                                .streamPrefix(imageName)
                                .build()));

        Map<String, String> envVars = new HashMap<>();
        envVars.put("SPRING_KAFKA_BOOTSTRAP_SERVERS", "localhost.localstack.cloud:4510, localhost.localstack.cloud:4511, localhost.localstack.cloud:4512");

        if (additionalEnvVars != null) {
            envVars.putAll(additionalEnvVars);
        }

        if (db != null) {
            envVars.put("SPRING_DATASOURCE_URL", "jdbc:postgresql://%s:%s/%s-db".formatted(
                    db.getDbInstanceEndpointAddress(),
                    db.getDbInstanceEndpointPort(),
                    imageName
            ));
            envVars.put("SPRING_DATASOURCE_USERNAME", "admin");
            envVars.put("SPRING_DATASOURCE_PASSWORD",
                    db.getSecret().secretValueFromJson("password").toString());
            envVars.put("SPRING_JPA_HIBERNATE_DDL_AUTO", "update");
            envVars.put("SPRING_SQL_INIT_MODE", "always");
            envVars.put("SPRING_DATASOURCE_HIKARI_INITIALIZATION_FAIL_TIMEOUT", "60000");
        }

        containerOptions.environment(envVars);
        taskDefinition.addContainer(imageName + "Container", containerOptions.build());

        return FargateService.Builder.create(this, id)
                .cluster(ecsCluster)
                .taskDefinition(taskDefinition)
                .assignPublicIp(false)
                .serviceName(imageName)
                .build();
    }
}

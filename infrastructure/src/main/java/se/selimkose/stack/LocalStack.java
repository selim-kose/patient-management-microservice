package se.selimkose.stack;

import software.amazon.awscdk.*;
import software.amazon.awscdk.services.ec2.InstanceClass;
import software.amazon.awscdk.services.ec2.InstanceSize;
import software.amazon.awscdk.services.ec2.InstanceType;
import software.amazon.awscdk.services.ec2.Vpc;
import software.amazon.awscdk.services.rds.*;
import software.amazon.awscdk.services.route53.CfnHealthCheck;

public class LocalStack extends Stack {

    private final Vpc vpc;

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
}

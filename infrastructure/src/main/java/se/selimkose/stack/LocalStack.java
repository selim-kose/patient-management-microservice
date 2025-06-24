package se.selimkose.stack;

import software.amazon.awscdk.*;

public class LocalStack extends Stack {

    // Boilerplate code for a CDK stack
    public LocalStack(final App scope, final String id, final StackProps props) {
        super(scope, id, props);
    }

    public static void main(String[] args) {
        App app = new App(AppProps.builder().outdir("./cdk.out").build());

        StackProps stackProps = StackProps.builder().synthesizer(new BootstraplessSynthesizer()).build();

        new LocalStack(app, "localStack", stackProps);
        app.synth();
        System.out.println("App synthesizing in progress...");
    }
}

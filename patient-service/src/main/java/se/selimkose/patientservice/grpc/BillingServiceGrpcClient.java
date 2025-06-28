package se.selimkose.patientservice.grpc;

import billing.BillingRequest;
import billing.BillingResponse;
import billing.BillingServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class BillingServiceGrpcClient {

    //Synchronous gRPC client for BillingService. This client is used to make blocking calls to the BillingService.
    private final BillingServiceGrpc.BillingServiceBlockingStub billingServiceBlockingStub;
    private final Logger log = LoggerFactory.getLogger(BillingServiceGrpcClient.class);


    public BillingServiceGrpcClient(
            @Value("${billing.service.address:localhost}") String billingServiceServerAddress,
            @Value("${billing.service.grpc.port:9001}") int billingServiceServerPort
    ) {
        log.info("Connecting to Billing Service at server address: {}:{}",
                billingServiceServerAddress, billingServiceServerPort);

        // Create a gRPC channel to the BillingService
        ManagedChannel billingServiceChannel = ManagedChannelBuilder.forAddress(
                billingServiceServerAddress, billingServiceServerPort
        ).usePlaintext().build();

        // Create a blocking stub for the BillingService
        billingServiceBlockingStub = BillingServiceGrpc.newBlockingStub(billingServiceChannel);
    }

    public BillingResponse createBillingAccount(String patientId, String name, String email){
        log.info("Creating billing account for patient with ID: {}", patientId);

        // Create a request object for the BillingService
        BillingRequest request = BillingRequest.newBuilder()
                .setPatientId(patientId)
                .setName(name)
                .setEmail(email)
                .build();

        // Call the BillingService and return the response
        BillingResponse response = billingServiceBlockingStub.createBillingAccount(request);
        log.info("Billing account created with ID: {}", response.getBillingAccountId());
        return response;
    }
}

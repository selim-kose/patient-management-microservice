package se.selimkose.billingservice.grpc;

import billing.BillingResponse;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import billing.BillingServiceGrpc.BillingServiceImplBase;

@GrpcService
public class BillingGrpcService extends BillingServiceImplBase {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(BillingGrpcService.class);


    @Override
    public void createBillingAccount(billing.BillingRequest request, StreamObserver<BillingResponse> responseObserver) {
        log.info("Received request to create billing account for user: {}", request.toString());

        // Here you would typically call a service to handle the business logic

        // For demonstration, we will just create a dummy response
        BillingResponse response = BillingResponse.newBuilder()
                .setBillingAccountId("123456")
                .setStatus("ACTIVE")
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();

    }
}

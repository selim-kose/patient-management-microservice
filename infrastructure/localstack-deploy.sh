#!/bin/bash

set -e # Exit on error, undefined variable, or pipe failure

# Delete existing stack
#aws --endpoint-url=http://localhost:4566 cloudformation delete-stack \
#  --stack-name patient-management

# Deploy the stack from the local CloudFormation template
aws --endpoint-url=http://localhost:4566 cloudformation deploy \
  --stack-name patient-management \
  --template-file "./cdk.out/localStack.template.json"

# Get the DNS name of the load balancer and print it
aws --endpoint-url=http://localhost:4566 elbv2 describe-load-balancers \
  --query "LoadBalancers[0].DNSName" --output text
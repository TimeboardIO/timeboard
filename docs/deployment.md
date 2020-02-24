# Timeboard AWS Deployment guide

## Prerequisites

- AWS cli

To configure aws cli :

    aws configure

## Deploy network stack

Go to folder :

    scripts/aws
    
and run the following command :

    aws cloudformation deploy       \
        --template-file network.yml \
        --stack Timeboard-network
        
## Deploy database stack

    aws cloudformation deploy   \
        --template-file rds.yml \
        --stack Timeboard-rds   \
        --parameter-overrides   \
            DBUser=          🙈 \
            DBPassword=      🙈 \
            DBSubnetA=       🙈 \
            DBSubnetB=       🙈 \
            DBSecurityGroup= 🙈
            
See Timeboard-network stack output for missing parameters values
    
## Deploy ElasticBeanstalk App

    aws cloudformation deploy              \
        --template-file app_beanstalk.yml  \
        --stack Timeboard-ElasticBeanstalk \
        --parameter-overrides              \
            TimeboardApplicationName=TimeboardApp
            
## Deploy ElasticBeanstalk Environment

    aws cloudformation deploy \
           --template-file env_beanstalk.yml \
           --stack Timeboard-ElasticBeanstalk-env \
           --parameter-overrides \
                 ElasticBeansTalkApp=TimeboardApp \
                 TimeboardVPC=vpc-🙈 \
                 TimeboardDNSName=app.mytimeboard.net \
                 TimeboardSSLArn=arn:aws:acm:🙈 \
                 OAuthClientID=🙈 \
                 OAuthSecretID=🙈 \
                 EC2Subnet=subnet-🙈 \
                 EC2SecurityGroup=sg-🙈 \
                 ELBSubnet=subnet-🙈 \
                 DBUser=timeboard \
                 DBPassword=timeboard \
                 DBDNS=🙈.rds.amazonaws.com   
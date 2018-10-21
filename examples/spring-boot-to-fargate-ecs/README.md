RUN the command:
mvn clean install -Daws.region=us-east-1 -Daws.ecr.account.id=`aws sts get-caller-identity --output text --query 'Account'` -Pdeploy
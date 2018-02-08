/home/tony/opt/mongodb/bin/mongoimport \
 --host=ec2-34-245-8-142.eu-west-1.compute.amazonaws.com \
 --port=27017 \
 --db=autoDB \
 --collection=config \
 --mode=merge \
 --file=configRecord.json
 

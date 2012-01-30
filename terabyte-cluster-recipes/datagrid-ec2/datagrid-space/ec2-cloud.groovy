cloud {

	provider "aws-ec2"
	
	user "YOUR_EC2_ACCESS_KEY_ID"
	apiKey "YOUR_EC2_SECRET_ACCESS_KEY_ID"
	
	// relative path to gigaspaces directory
	localDirectory "tools/cli/plugins/esc/ec2/upload"
	
	remoteDirectory "/home/ec2-user/gs-files"
	
	imageId "us-east-1/ami-1b814f72"
	machineMemoryMB "68100"
	hardwareId "m2.4xlarge"

	// Security group which has the appropriate ports configured to be open for incoming and outgoing traffic
	securityGroup "default"
	
	// YOUR keypair file and name of the keypair
	keyFile "cloud-demo.pem"
	keyPair "cloud-demo"
	
	// S3 URL location where GigaSpaces is saved. Update the access properties of this location to everyone
	cloudifyUrl "https://s3.amazonaws.com/cloudify/gigaspaces.zip"
	machineNamePrefix "gs_esm_gsa_"

	dedicatedManagementMachines true
	managementOnlyFiles ([])
	connectedToPrivateIp false
	
	sshLoggingLevel java.util.logging.Level.WARNING
	managementGroup "management_machine"
	numberOfManagementMachines 2
	
	zones (["agent"])

	reservedMemoryCapacityPerMachineInMB 1024

}
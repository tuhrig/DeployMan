(
	echo "copying image {{tarball.key}}..."
	aws s3 cp --quiet --region {{aws.region}} s3://{{repo.bucket}}/{{tarball.key}} {{home.directory}}
	echo "copied image {{tarball.key}}"
) &
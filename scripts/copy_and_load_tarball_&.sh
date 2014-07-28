(
	echo "copying image {{tarball.key}}..."
	aws s3 cp --quiet --region {{aws.region}} s3://{{repo.bucket}}/{{tarball.key}} {{home.directory}}
	echo "copied image {{tarball.key}}"

	echo "loading Docker image {{tarball.key}}..."
	docker load < {{home.directory}}/{{tarball.name}}
	echo "loaded Docker image {{tarball.key}}"
) & wait_pids+=($!)
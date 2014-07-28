(
	echo "loading Docker image {{tarball.key}}..."
	docker load < {{home.directory}}/{{tarball.name}}
	echo "loaded Docker image {{tarball.key}}"
) &
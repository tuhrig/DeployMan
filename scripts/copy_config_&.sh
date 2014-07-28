(
	echo "copying config {{config.key}} to {{config.folder}}..."
	aws s3 cp --quiet --recursive --region {{aws.region}} s3://{{repo.bucket}}/{{config.key}} {{config.folder}}
	echo "copied config {{config.key}} to {{config.folder}}"
) & wait_pids+=($!)
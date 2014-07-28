echo "add crontab sync job for {{config.key}} to {{config.folder}}..."
crontab -l | { cat; echo "*/5 * * * * sudo aws s3 sync s3://{{repo.bucket}}/{{config.key}} {{config.folder}}"; } | crontab -
echo "added crontab sync job for {{config.key}} to {{config.folder}}"
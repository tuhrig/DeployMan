if [ ${#wait_pids[@]} -gt 0 ]; then
    wait "${wait_pids[@]}"
fi
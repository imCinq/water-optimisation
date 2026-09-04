#!/usr/bin/env bash

set -euo pipefail

if [[ $# -ne 1 ]]; then
	echo "Usage: $0 <26.2|1.21.1>" >&2
	exit 2
fi

target=$1
case "$target" in
	26.2|1.21.1)
		;;
	*)
		echo "Unsupported target '$target'. Use 26.2 or 1.21.1." >&2
		exit 2
		;;
esac

log_dir=${PRODUCTION_SMOKE_LOG_DIR:-build/production-smoke}
run_dir=${PRODUCTION_SMOKE_RUN_DIR:-"build/production-smoke-run/${target//./_}-$$"}
timeout_seconds=${PRODUCTION_SMOKE_TIMEOUT_SECONDS:-300}
grace_seconds=${PRODUCTION_SMOKE_GRACE_SECONDS:-5}
use_xvfb=${PRODUCTION_SMOKE_USE_XVFB:-false}
log_file="$log_dir/${target//./_}.log"

if ! [[ "$timeout_seconds" =~ ^[0-9]+$ ]] || (( timeout_seconds < 1 )); then
	echo "PRODUCTION_SMOKE_TIMEOUT_SECONDS must be a positive integer." >&2
	exit 2
fi
if ! [[ "$grace_seconds" =~ ^[0-9]+$ ]]; then
	echo "PRODUCTION_SMOKE_GRACE_SECONDS must be a non-negative integer." >&2
	exit 2
fi

mkdir -p "$log_dir" "$run_dir"
: > "$log_file"

gradle_args=(
	--no-daemon
	--console=plain
	"-Ptarget_minecraft=$target"
	"-Pminecraft_version=$target"
	"-Pproduction_run_dir=$run_dir"
	"-Pproduction_use_xvfb=$use_xvfb"
	prodClient
)

if command -v setsid >/dev/null 2>&1; then
	setsid ./gradlew "${gradle_args[@]}" >"$log_file" 2>&1 &
	launcher_pid=$!
	launcher_group=$launcher_pid
else
	./gradlew "${gradle_args[@]}" >"$log_file" 2>&1 &
	launcher_pid=$!
	launcher_group=
fi

cleanup() {
	local status=$?
	local attempt
	trap - EXIT INT TERM

	if [[ -n "$launcher_group" ]] && kill -0 -- "-$launcher_group" 2>/dev/null; then
		kill -TERM -- "-$launcher_group" 2>/dev/null || true
	elif kill -0 "$launcher_pid" 2>/dev/null; then
		kill -TERM "$launcher_pid" 2>/dev/null || true
	fi

	for ((attempt = 0; attempt < 10; attempt++)); do
		if [[ -n "$launcher_group" ]]; then
			kill -0 -- "-$launcher_group" 2>/dev/null || break
		else
			kill -0 "$launcher_pid" 2>/dev/null || break
		fi
		sleep 1
	done
	if [[ -n "$launcher_group" ]] && kill -0 -- "-$launcher_group" 2>/dev/null; then
		kill -KILL -- "-$launcher_group" 2>/dev/null || true
	elif kill -0 "$launcher_pid" 2>/dev/null; then
		kill -KILL "$launcher_pid" 2>/dev/null || true
	fi

	wait "$launcher_pid" 2>/dev/null || true
	exit "$status"
}
trap cleanup EXIT INT TERM

launcher_is_alive() {
	if ! kill -0 "$launcher_pid" 2>/dev/null; then
		return 1
	fi

	if command -v ps >/dev/null 2>&1; then
		local state
		if ! state=$(ps -o stat= -p "$launcher_pid" 2>/dev/null | tr -d '[:space:]'); then
			return 0
		fi
		[[ -z "$state" || "$state" != Z* ]]
		return
	fi

	return 0
}

deadline=$((SECONDS + timeout_seconds))
marker="Water Optimisation initialized"

while :; do
	if grep -Fq "$marker" "$log_file"; then
		echo "Production client startup marker observed for Minecraft $target."
		if (( grace_seconds > 0 )); then
			sleep "$grace_seconds"
		fi
		if launcher_is_alive; then
			echo "Production client remained alive for ${grace_seconds}s after initialization."
			exit 0
		fi
		echo "Production client exited during the post-initialization grace period." >&2
		tail -n 160 "$log_file" >&2 || true
		exit 1
	fi

	if ! launcher_is_alive; then
		if wait "$launcher_pid"; then
			status=0
		else
			status=$?
		fi
		echo "Production client exited before the startup marker (status $status)." >&2
		tail -n 160 "$log_file" >&2 || true
		if (( status == 0 )); then
			exit 1
		fi
		exit "$status"
	fi

	if (( SECONDS >= deadline )); then
		echo "Production client did not reach the startup marker within ${timeout_seconds}s." >&2
		tail -n 160 "$log_file" >&2 || true
		exit 1
	fi

	sleep 2
done

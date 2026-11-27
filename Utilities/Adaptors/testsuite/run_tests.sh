#!/bin/bash
set -e

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd "$DIR"

# Clean up any leftover processes on port 8999 or state files
lsof -ti :8999 | xargs kill -9 2>/dev/null || true
rm -f test_shmem_state test_adaptor.log

echo "=========================================================="
echo "           WebObjects Adaptor Test Suite                  "
echo "=========================================================="

echo "=== Building testsuite binaries ==="
make all

echo "=== Starting Dummy WebObjects App Server on port 8999 ==="
python3 dummy_app_server.py --port 8999 &
SERVER_PID=$!

cleanup() {
    echo ""
    echo "=== Shutting down Dummy App Server (PID $SERVER_PID) ==="
    kill -9 $SERVER_PID 2>/dev/null || true
    rm -f test_shmem_state
}
trap cleanup EXIT

# Wait for server to start
for i in {1..30}; do
    if nc -z 127.0.0.1 8999 2>/dev/null; then
        echo "Dummy App Server is ready on 127.0.0.1:8999."
        break
    fi
    sleep 0.1
done

TOTAL_TESTS=0
PASSED_TESTS=0
FAILED_TESTS=0

run_test() {
    local name="$1"
    local cmd="$2"
    local timeout_sec="$3"
    
    TOTAL_TESTS=$((TOTAL_TESTS + 1))
    echo ""
    echo "----------------------------------------------------------"
    echo "Running Test [$TOTAL_TESTS]: $name"
    echo "Command: $cmd (Timeout: ${timeout_sec}s)"
    echo "----------------------------------------------------------"

    # Execute with python3 subprocess to enforce timeout and capture signals/exit codes
    set +e
    python3 -c "
import subprocess, sys

cmd = '$cmd'.split()
try:
    res = subprocess.run(cmd, timeout=$timeout_sec)
    sys.exit(res.returncode)
except subprocess.TimeoutExpired:
    print('\n❌ [TEST FAILED]: Execution timed out after $timeout_sec seconds!')
    print('   Possible infinite loop or deadlock in linked list / cache lock.')
    sys.exit(124)
except Exception as e:
    print(f'\n❌ [TEST FAILED]: {e}')
    sys.exit(1)
"
    EXIT_CODE=$?
    set -e

    if [ $EXIT_CODE -eq 0 ]; then
        echo "✅ [RESULT]: PASSED"
        PASSED_TESTS=$((PASSED_TESTS + 1))
    elif [ $EXIT_CODE -eq 124 ]; then
        echo "❌ [RESULT]: FAILED (Timeout / Deadlock)"
        FAILED_TESTS=$((FAILED_TESTS + 1))
    elif [ $EXIT_CODE -eq 2 ] || [ $EXIT_CODE -gt 128 ]; then
        echo "❌ [RESULT]: FAILED (Process Crashed / Memory Corruption / Signal: $EXIT_CODE)"
        FAILED_TESTS=$((FAILED_TESTS + 1))
    else
        echo "❌ [RESULT]: FAILED (Errors detected, exit code: $EXIT_CODE)"
        FAILED_TESTS=$((FAILED_TESTS + 1))
    fi
}

MODE="${1:-default}"

if [ "$MODE" == "--reproduce" ]; then
    echo "Mode: REPRODUCE BUGS (expecting failures/crashes in unsynchronized builds)"
    run_test "String Cache Race (Single-threaded build under concurrency)" "./test_wastring_single 5000 16" 10
    run_test "1MB Streamed POST (Single-threaded build under concurrency)" "./test_adaptor_runner_single 20 10" 10
else
    echo "Mode: DEFAULT TEST RUN"
    run_test "String Cache Concurrency (test_wastring)" "./test_wastring 5000 16" 15
    run_test "End-to-End Adaptor 1MB Streamed Uploads (test_adaptor_runner)" "./test_adaptor_runner 20 10" 20
fi

echo ""
echo "=========================================================="
echo "                  TEST SUMMARY                            "
echo "=========================================================="
echo "Total Tests:  $TOTAL_TESTS"
echo "Passed:       $PASSED_TESTS"
echo "Failed:       $FAILED_TESTS"
echo "=========================================================="

if [ $FAILED_TESTS -gt 0 ]; then
    echo "❌ TEST SUITE FAILED!"
    exit 1
else
    echo "✅ ALL TESTS PASSED!"
    exit 0
fi

import type { Problem } from '../services/problemService';

/**
 * Returns an appropriate starter template for the given problem and language.
 */
export function getStarterTemplate(problem: Problem | null, language: string): string {
    const lang = (language || 'JAVA').toUpperCase();
    const title = problem?.title || 'Problem';

    // 1. JAVA
    if (lang === 'JAVA') {
        if (problem?.starterCode && (problem.starterLanguage === 'JAVA' || !problem.starterLanguage)) {
            return problem.starterCode;
        }
        return `import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.*;

public class Main {
    public static void main(String[] args) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        // TODO: Implement solution for ${title}
    }
}
`;
    }

    // 2. Specific problem templates for common problems
    const normalizedTitle = title.toLowerCase();

    // Two Sum
    if (normalizedTitle.includes('two sum')) {
        if (lang === 'PYTHON') {
            return `import sys

def two_sum(nums, target):
    # TODO: Return indices of the two values that sum to target
    return [0, 0]

def main():
    lines = sys.stdin.read().split()
    if not lines:
        return
    target = int(lines[0])
    n = int(lines[1])
    nums = [int(x) for x in lines[2:2 + n]]
    ans = two_sum(nums, target)
    print(f"[{ans[0]}, {ans[1]}]")

if __name__ == '__main__':
    main()
`;
        }
        if (lang === 'CPP') {
            return `#include <iostream>
#include <vector>

using namespace std;

vector<int> twoSum(const vector<int>& nums, int target) {
    // TODO: Return indices of the two values that sum to target
    return {0, 0};
}

int main() {
    int target, n;
    if (!(cin >> target >> n)) return 0;
    vector<int> nums(n);
    for (int i = 0; i < n; i++) cin >> nums[i];
    vector<int> ans = twoSum(nums, target);
    cout << "[" << ans[0] << ", " << ans[1] << "]" << endl;
    return 0;
}
`;
        }
        if (lang === 'C') {
            return `#include <stdio.h>
#include <stdlib.h>

void twoSum(const int* nums, int numsSize, int target, int* returnIndices) {
    // TODO: Return indices of the two values that sum to target
    returnIndices[0] = 0;
    returnIndices[1] = 0;
}

int main() {
    int target, n;
    if (scanf("%d %d", &target, &n) != 2) return 0;
    int* nums = (int*)malloc(n * sizeof(int));
    for (int i = 0; i < n; i++) scanf("%d", &nums[i]);
    int ans[2];
    twoSum(nums, n, target, ans);
    printf("[%d, %d]\\n", ans[0], ans[1]);
    free(nums);
    return 0;
}
`;
        }
    }

    // LRU Cache
    if (normalizedTitle.includes('lru cache')) {
        if (lang === 'PYTHON') {
            return `import sys

class LRUCache:
    def __init__(self, capacity: int):
        self.capacity = capacity

    def get(self, key: int) -> int:
        # TODO: Return value for key if present, else -1
        return -1

    def put(self, key: int, value: int) -> None:
        # TODO: Update or insert key-value pair
        pass

def main():
    lines = [line.strip() for line in sys.stdin if line.strip()]
    if not lines:
        return
    capacity = int(lines[0])
    cache = LRUCache(capacity)
    for line in lines[1:]:
        if line.startswith("put"):
            parts = line[line.find("(") + 1 : line.find(")")].split(",")
            cache.put(int(parts[0].strip()), int(parts[1].strip()))
        elif line.startswith("get"):
            key = int(line[line.find("(") + 1 : line.find(")")].strip())
            print(cache.get(key))

if __name__ == '__main__':
    main()
`;
        }
        if (lang === 'CPP') {
            return `#include <iostream>
#include <string>

using namespace std;

class LRUCache {
public:
    LRUCache(int capacity) {
        // TODO: Initialize LRU cache with given capacity
    }

    int get(int key) {
        // TODO: Return value for key if present, else -1
        return -1;
    }

    void put(int key, int value) {
        // TODO: Update or insert key-value pair
    }
};

int main() {
    int capacity;
    if (!(cin >> capacity)) return 0;
    LRUCache cache(capacity);
    string line;
    while (cin >> line) {
        if (line.rfind("put", 0) == 0) {
            int k, v;
            if (sscanf(line.c_str(), "put(%d,%d)", &k, &v) == 2) cache.put(k, v);
        } else if (line.rfind("get", 0) == 0) {
            int k;
            if (sscanf(line.c_str(), "get(%d)", &k) == 1) cout << cache.get(k) << endl;
        }
    }
    return 0;
}
`;
        }
        if (lang === 'C') {
            return `#include <stdio.h>
#include <stdlib.h>
#include <string.h>

// LRU Cache Implementation in C
int main() {
    int capacity;
    if (scanf("%d", &capacity) != 1) return 0;
    // TODO: Implement LRU Cache operations
    return 0;
}
`;
        }
    }

    // Rate Limiter
    if (normalizedTitle.includes('rate limiter')) {
        if (lang === 'PYTHON') {
            return `import sys

def allow_requests(limit: int, window_seconds: int, timestamps: list[int]) -> list[bool]:
    # TODO: Return boolean array whether each request is allowed
    return [False] * len(timestamps)

def main():
    lines = sys.stdin.read().split()
    if not lines:
        return
    limit = int(lines[0])
    window = int(lines[1])
    timestamps = [int(x) for x in lines[2:]]
    allowed = allow_requests(limit, window, timestamps)
    print(" ".join("true" if a else "false" for a in allowed))

if __name__ == '__main__':
    main()
`;
        }
        if (lang === 'CPP') {
            return `#include <iostream>
#include <vector>

using namespace std;

vector<bool> allowRequests(int limit, int windowSeconds, const vector<int>& timestamps) {
    // TODO: Return boolean array whether each request is allowed
    return vector<bool>(timestamps.size(), false);
}

int main() {
    int limit, window;
    if (!(cin >> limit >> window)) return 0;
    vector<int> timestamps;
    int ts;
    while (cin >> ts) timestamps.push_back(ts);
    vector<bool> allowed = allowRequests(limit, window, timestamps);
    for (size_t i = 0; i < allowed.size(); i++) {
        cout << (i == 0 ? "" : " ") << (allowed[i] ? "true" : "false");
    }
    cout << endl;
    return 0;
}
`;
        }
        if (lang === 'C') {
            return `#include <stdio.h>
#include <stdlib.h>
#include <stdbool.h>

int main() {
    int limit, window;
    if (scanf("%d %d", &limit, &window) != 2) return 0;
    // TODO: Implement rate limiter
    return 0;
}
`;
        }
    }

    // Concurrent Task Scheduler
    if (normalizedTitle.includes('task scheduler') || normalizedTitle.includes('concurrent task')) {
        if (lang === 'PYTHON') {
            return `import sys

def schedule_tasks(tasks: list[str]) -> list[str]:
    # TODO: Return scheduled tasks
    return []

def main():
    lines = [line.strip() for line in sys.stdin if line.strip()]
    if not lines:
        return
    count = int(lines[0])
    tasks = lines[1:1 + count]
    for task in schedule_tasks(tasks):
        print(task)

if __name__ == '__main__':
    main()
`;
        }
        if (lang === 'CPP') {
            return `#include <iostream>
#include <vector>
#include <string>

using namespace std;

vector<string> scheduleTasks(const vector<string>& tasks) {
    // TODO: Return scheduled tasks
    return {};
}

int main() {
    int count;
    if (!(cin >> count)) return 0;
    cin.ignore();
    vector<string> tasks(count);
    for (int i = 0; i < count; i++) {
        getline(cin, tasks[i]);
    }
    for (const string& task : scheduleTasks(tasks)) {
        cout << task << endl;
    }
    return 0;
}
`;
        }
        if (lang === 'C') {
            return `#include <stdio.h>
#include <stdlib.h>
#include <string.h>

int main() {
    int count;
    if (scanf("%d\\n", &count) != 1) return 0;
    char buffer[256];
    for (int i = 0; i < count; i++) {
        if (fgets(buffer, sizeof(buffer), stdin)) {
            // TODO: Process and schedule task
        }
    }
    return 0;
}
`;
        }
    }

    // 3. Generic fallback template for each language
    if (lang === 'PYTHON') {
        return `import sys

def solve():
    """
    ${title}
    """
    # Read input from standard input
    input_data = sys.stdin.read().strip()
    if not input_data:
        return

    # TODO: Implement your solution here
    pass

if __name__ == '__main__':
    solve()
`;
    }

    if (lang === 'CPP') {
        return `#include <iostream>
#include <vector>
#include <string>
#include <algorithm>

using namespace std;

/**
 * ${title}
 */
int main() {
    ios_base::sync_with_stdio(false);
    cin.tie(NULL);

    // TODO: Implement your solution here

    return 0;
}
`;
    }

    if (lang === 'C') {
        return `#include <stdio.h>
#include <stdlib.h>
#include <string.h>

/**
 * ${title}
 */
int main() {
    // TODO: Implement your solution here

    return 0;
}
`;
    }

    return '';
}

# Bowling Score Calculator: Unit Testing with JUnit 5 and JaCoCo
### Specialized Tool Exploration and Program Unit Testing: implementation guide and report

| | |
|---|---|
| **Student / Team** | Mohammed Anas 20241ISE0025, Amit Suresh 20241ISE0019 |
| **Course** | Software Testing and Quality Assurance Lab |
| **Date** | 15 November 2026 |
| **Repository** | https://github.com/mohammedanas007/bowling |
| **Live JaCoCo report** | https://mohammedanas007.github.io/bowling/ |
| **CI runs** | https://github.com/mohammedanas007/bowling/actions |

**Result in one line:** 31 test executions give **100% instruction, branch, line, complexity and method coverage** of `BowlingGame`; 5 of 5 seeded bugs are detected.

---

## Contents

1. [Objective](#1-objective-and-overview) · 2. [System under test](#2-system-under-test) · 3. [Tool setup](#3-tool-setup)
4. [Implementation steps](#4-implementation-steps) · 5. [Test design](#5-test-design) · 6. [Execution results](#6-execution-results)
7. [Coverage results](#7-coverage-results) · 8. [Experiments](#8-experiments) · 9. [Discussion](#9-discussion) · 10. [Conclusion](#10-conclusion)
11. [Troubleshooting](#11-troubleshooting) · 12. [References](#12-references)

---

## 1. Objective and overview

The objective is to evaluate **JaCoCo**, an industry-standard code-coverage tool for Java, by applying it to a standalone Java module and measuring how well a JUnit 5 suite exercises it. JaCoCo shows which code the tests actually execute, which matters because tests can pass while large parts of the code never run.

- **How it works:** a Java agent instruments bytecode as classes load and records executed instructions and branches in `target/jacoco.exec`; the `report` goal turns this file into HTML, XML and CSV.
- **Counters:** instruction, branch, line, cyclomatic complexity, method, class.
- **Limit:** coverage shows that code ran, not that assertions checked the right result (section 9).

**Scope delivered:** tool setup (Maven, Eclipse, GitHub Actions, GitHub Pages), test design (functional, boundary, error), and execution reporting (pass/fail, assertion metrics, timing, coverage, defect report).

## 2. System under test

`com.example.bowling.BowlingGame`: ten-pin bowling scoring with input validation (105 source lines, 57 executable lines, 4 methods, 38 branch outcomes, complexity 23).

| Member | Behaviour |
|---|---|
| `BowlingGame()` | Initialises roll storage and frame state. |
| `roll(int pins)` | Records a roll (0-10). `IllegalArgumentException` for an invalid pin count or frame total; `IllegalStateException` if the game is complete. |
| `isComplete()` | True once frame 10 (including fill balls) is finished. |
| `score()` | Total with strike/spare bonuses. `IllegalStateException` if the game is incomplete. |

Rules: open frames, spare (+ next roll), strike (+ next two rolls), 10th-frame fill balls (strike → 2, spare → 1, open → 0). The class suits this study because it has many decisions (`if`, `&&`, `||`, loop), state, and both normal and error paths.

## 3. Tool setup

| Item | Version |
|---|---|
| JDK | 21 (compiler release 17) |
| Maven | 3.10.2 |
| JUnit / Surefire | 5.10.2 / 3.2.5 |
| JaCoCo | 0.8.12 |
| IDE | Eclipse with m2e and EclEmma |
| CI / hosting | GitHub Actions, GitHub Pages |

**JaCoCo in `pom.xml`** (three executions of `jacoco-maven-plugin`):

| Execution | Phase | Effect |
|---|---|---|
| `prepare-agent` | initialize | Attaches the agent to the test JVM; writes `target/jacoco.exec` |
| `report` | test | Creates `target/site/jacoco/` (`index.html`, `jacoco.xml`, `jacoco.csv`) |
| `check` | verify | **Coverage gate:** fails the build if line < 90% or branch < 85% |

**Project layout**

```
bowling/
├── pom.xml                                  JUnit 5 + Surefire + JaCoCo (report + gate)
├── README.md, PROJECT_REPORT.md             documentation
├── scripts/summary.sh                       prints test + coverage numbers
├── docs/                                    coverage screenshots
├── .github/workflows/build.yml              CI: build, test, gate, artifacts, Pages
├── .gitignore
└── src
    ├── main/java/com/example/bowling/BowlingGame.java        system under test
    └── test/java/com/example/bowling/BowlingGameTest.java    test suite
```

---

## 4. Implementation steps

### 4.1 Prerequisites

| Tool | Version | Check |
|---|---|---|
| JDK | 17 or newer (`JAVA_HOME` must point to the JDK, not a JRE) | `java -version` |
| Maven | 3.9+ | `mvn -v` |
| Eclipse | IDE for Java Developers 2023-12+ (bundles m2e, Git, EclEmma) | *Help → About* |
| Git + GitHub account | any recent | `git --version` |

### 4.2 Build and run from the command line

```bash
mvn clean verify
bash scripts/summary.sh      # prints test and coverage numbers for the report
```

Order of events: compile → `prepare-agent` → Surefire runs the JUnit 5 tests → `report` writes the HTML report → `check` applies the gate. Open `target/site/jacoco/index.html` for coverage per class, method and line.

| Deliverable | Where it comes from |
|---|---|
| Pass/fail logs | Console; `target/surefire-reports/*.txt` and `TEST-*.xml` |
| Execution speed | `Time elapsed` in the `.txt` reports; per-test `time` in the XML |
| Assertion metrics | Surefire counts; assertion call sites in the test source |
| Coverage report | `target/site/jacoco/index.html` |
| Defect report | Seeded-defect experiment (section 8) |

### 4.3 Import and run in Eclipse

1. **Import:** *File → Import → Maven → Existing Maven Projects*, select the project folder, *Finish*. m2e downloads the dependencies (first time only).
2. **Java level:** *Properties → Java Compiler* → compliance 17 or higher. If errors appear: *Maven → Update Project…* (Alt+F5).
3. **Run tests:** right-click `BowlingGameTest.java` → *Run As → JUnit Test*. The JUnit view shows pass/fail and time per test.
4. **Run Maven:** right-click the project → *Run As → Maven build…* → goals `clean verify` → *Run*. Refresh (F5) to see `target/`.
5. **Coverage (EclEmma):** right-click `BowlingGameTest.java` → *Coverage As → JUnit Test*. If missing, install **EclEmma Java Code Coverage** from *Help → Eclipse Marketplace…*.
   - Green = covered, yellow = a branch taken only one way, red = never executed.
   - *Window → Show View → Coverage* gives percentages per package, class and method; *Export Session…* saves an HTML report.

### 4.4 Publish the project to GitHub from Eclipse

**A. Prepare GitHub (once)**
1. Create a **new empty repository** (no README, no `.gitignore`) and copy its HTTPS URL, e.g. `https://github.com/mohammedanas007/bowling.git`.
2. Create a **personal access token**: *Settings → Developer settings → Personal access tokens → Tokens (classic) → Generate new token (classic)*.
   Tick the scopes **`repo`** and **`workflow`** (the workflow scope is required to push `.github/workflows/`). Copy the token at once; GitHub shows it only once.

**B. Put the project under Git**
3. Right-click the project → *Team → Share Project… → Git*. Leave *"Use or create repository in parent folder"* **unticked**, select the project, *Create Repository*, *Finish*.
4. Open **Window → Show View → Other → Git → Git Staging** and **Git Repositories**.
5. In *Git Staging*, make sure `target/`, `.classpath`, `.project`, `.settings/` are **not** listed (they are in `.gitignore`). Drag all files to *Staged Changes*, enter a commit message, and click **Commit**.
   - *Commit* saves locally; *Commit and Push…* also pushes once a remote exists.
   - Optional: rename the branch to `main` (*Git Repositories → Branches → Local → right-click → Rename Branch*).

**C. Connect and push**
6. In *Git Repositories*: right-click **Remotes → Create Remote…**, name `origin`, choose *Configure push*.
7. Click *Change…*, paste the HTTPS URL, enter your **GitHub username** and the **token as password**, tick *Store in Secure Store*, then *Finish* and *Save*.
8. Optional ref mapping: *Add…* → source `main` → destination `main` (`refs/heads/main → refs/heads/main`). Skipping it is fine; the push wizard creates it.
9. Right-click the project → **Team → Remote → Push to Upstream**. The remote is `origin`; the branch is `main` (or `master`). A *Push Results* dialog confirms success.

**D. Later changes:** stage → commit → *Team → Push to Upstream* (or *Commit and Push…*). Pull first if the remote changed.

Icons in Eclipse: `?` = untracked, `+` = staged, `>` = changed since the last commit, none = committed.

### 4.5 CI pipeline and live coverage page

`.github/workflows/build.yml` runs on every push and pull request (and manually via *Run workflow*):

1. Sets up JDK 17 and runs `mvn -B clean verify` (tests, report, gate).
2. Uploads the `jacoco-report` and `surefire-reports` **artifacts** (*Actions → latest run → Artifacts*).
3. On the default branch, packages `target/site/jacoco` and the `deploy` job publishes it to **GitHub Pages**.

**One-time Pages setup:** *Settings → Pages → Build and deployment → Source → GitHub Actions* (free accounts need a **public** repository). After a green `deploy` job, the report is live at https://mohammedanas007.github.io/bowling/ (allow 1-2 minutes).

---

## 5. Test design

**26 test methods, 31 test executions** (two parameterized tests run 2 and 5 times), grouped in three nested classes.

| Category | IDs | Technique |
|---|---|---|
| Functional correctness | F-01 to F-09 | Known-score games, bonus rules |
| Boundary limits | B-01 to B-09 | 0/10 pins, 10th-frame transitions, completion edges |
| Edge / error conditions | E-01 to E-08 | Invalid input, illegal state, state kept after a rejected roll |

| ID | Input | Expected result |
|---|---|---|
| F-01 | 20 × 0 | score 0 |
| F-02 | 20 × 1 | score 20 |
| F-03 | 5, 5, 3, then 17 × 0 | 16 (spare bonus) |
| F-04 | 10, 3, 4, then 16 × 0 | 24 (strike bonus) |
| F-05 | 21 × 5 | 150 (all spares) |
| F-06 | 12 × 10 | complete, 300 |
| F-07 | X 7/ 9- X -8 8/ -6 X X X81 | 167 |
| F-08 | ten frames of 9, 0 | 90 |
| F-09 | 10, 10, 10, then 14 × 0 | 60 |
| B-01 | pins = 0 and 10 | accepted (2 runs) |
| B-02 | 18 × 0, then 10, 10, 10 | complete, 30 |
| B-03 | 18 × 0, then 4, 6, 7 | complete, 17 |
| B-04 | 18 × 0, then 4, 3 | complete (no fill ball), 7 |
| B-05 | 18 × 0, then 10, 3, 4 | complete, 17 |
| B-06 | 18 × 1 | not complete |
| B-07 | 18 × 0, then 5, 5 | not complete (fill ball pending) |
| B-08 | 18 × 0, then 10, 10, 10 | third ball of 10 accepted |
| B-09 | 18 × 0, then 10, 0, 10 | accepted, 20 |
| E-01 | -1, 11, 100, `MIN_VALUE`, `MAX_VALUE` | `IllegalArgumentException` (5 runs) |
| E-02 | 6, then 5 | `IllegalArgumentException` |
| E-03 | 18 × 0, then 6, 5 | `IllegalArgumentException` |
| E-04 | 18 × 0, then 10, 5, 6 | `IllegalArgumentException` |
| E-05 | 20 × 0, then roll 1 | `IllegalStateException` |
| E-06 | 10 × 1, then `score()` | `IllegalStateException` |
| E-07 | `score()` on a new game | `IllegalStateException` |
| E-08 | 6, rejected 5, then 4 | 4 accepted (state unchanged) |

**Techniques:** equivalence partitioning (valid 0-10 vs invalid pins; open/spare/strike frames), boundary-value analysis (-1, 0, 10, 11; frame total 10 vs 11), error guessing (`Integer.MIN_VALUE`/`MAX_VALUE`, state after a rejected roll), state-based testing (roll after completion, score before completion, 10th-frame transitions).

---

## 6. Execution results

| Metric | Value |
|---|---|
| Tests run / passed / failed / errors / skipped | 31 / 31 / 0 / 0 / 0 |
| Total execution time (s) | `[from target/surefire-reports, or run: bash scripts/summary.sh]` |
| Slowest test class | `[from target/surefire-reports]` |
| Assertion call sites | 33 (`assertEquals` 14, `assertThrows` 8, `assertTrue` 5, `assertFalse` 2, `assertDoesNotThrow` 4) |
| Assertion executions at run time | about 38 (parameterized B-01 and E-01 repeat their assertion) |
| Assertions per test method | about 1.3 |

JaCoCo generates its report only after all tests pass, so the existing report confirms a failure-free run. The first run includes JVM and agent start-up; run `mvn test` twice to see the agent's overhead (small for a suite this size).

*Screenshots to insert: Eclipse JUnit view (all green), Maven `BUILD SUCCESS`, green GitHub Actions run.*

---

## 7. Coverage results

Source: https://mohammedanas007.github.io/bowling/ (JaCoCo 0.8.12).

| Counter | Covered / total | % |
|---|---|---|
| Instruction | 307 / 307 | 100% |
| Branch | 38 / 38 | 100% |
| Line | 57 / 57 | 100% |
| Complexity | 23 / 23 | 100% |
| Method | 4 / 4 | 100% |
| Class | 1 / 1 | 100% |

**Gate:** required line ≥ 90% and branch ≥ 85%; achieved 100% and 100% → **pass**.

![Overall coverage](docs/coverage-1-overall.png)
*Figure 1: Bundle summary.*

![Package coverage](docs/coverage-2-package.png)
*Figure 2: Package `com.example.bowling`.*

| Method | Complexity | Lines | Instruction cov. | Branch cov. |
|---|---|---|---|---|
| `roll(int)` | 16 | 34 | 100% | 100% |
| `score()` | 5 | 14 | 100% | 100% |
| `BowlingGame()` | 1 | 8 | 100% | n/a |
| `isComplete()` | 1 | 1 | 100% | n/a |
| **Total** | **23** | **57** | **100%** | **100%** |

![Class coverage](docs/coverage-3-class.png)
*Figure 3: `BowlingGame` by method.*

**Analysis**
1. **Yellow or red lines:** none. 0 missed instructions, 0 of 38 missed branches, 0 missed lines.
2. **Most complex method:** `roll(int)`, complexity 16 of 23 (about 70%) and 34 of 57 lines. It is fully covered, including the 10th-frame logic and validation, which is why the boundary and error tests matter.
3. **EclEmma vs Maven:** both use the same JaCoCo engine on the same classes and tests, so they report the same figures; differences appear only when the test sets differ.

---

## 8. Experiments

### 8.1 Coverage without the error tests

Comment out the `ErrorConditions` class, run `mvn clean verify`, then restore it.

| Suite | Line % | Branch % | Gate |
|---|---|---|---|
| Full suite | 100% (57/57) | 100% (38/38) | Pass |
| Without `ErrorConditions` | 89.5% (51/57) | 81.6% (31/38) | **Fail** (line < 90%, branch < 85%) |

*Second row derived by analysing which statements and branches only the error tests reach; replace with measured values after running.* Six `throw` lines and seven branch outcomes become uncovered: `pins < 0`, `pins > 10`, `complete`, the over-10 checks (frames 1-9, 10th-frame second roll, fill balls) and the incomplete-game check in `score()`. All normal-path tests still pass, yet none of the validation is checked.

### 8.2 Seeded defects

Apply one bug at a time, run `mvn clean test`, record the failures, then revert (`git checkout -- src/main`). Results below come from replaying the 31 scenarios against each mutated copy (plain Java, no JUnit); confirm with a JUnit run. Parameterized tests may appear as `B-01 [2]`.

| ID | Bug | Failing tests | Detected |
|---|---|---|---|
| D1 | `pins > 10` → `pins >= 10` in `roll` | F-04, F-06, F-07, F-09, B-01 (10), B-02, B-05, B-08, B-09, E-04 | Yes |
| D2 | Spare bonus dropped: `10 + rolls[i + 2]` → `10` | F-03, F-05, F-07, B-03 | Yes |
| D3 | `first + second < 10` → `true` (no fill balls) | F-05, F-06, F-07, B-02, B-03, B-05, B-07, B-08, B-09, E-04 | Yes |
| D4 | Frames 1-9: `first + pins > 10` → `>= 10` | F-03, F-05, F-07, E-08 | Yes |
| D5 | 10th frame: remove the `first != 10 &&` guard | F-06, F-07, B-02, B-05, B-08, E-04 | Yes |

**Detection rate: 5 / 5.** JaCoCo coverage does not change for D2 and D4 (the same lines and both outcomes of each decision still run); only the assertions notice. **Real defects found:** none; all 31 executions pass on the unmodified code.

### 8.3 Coverage is not quality

Add this test and compare coverage:

```java
@Test
void coverageWithoutAssertions() {
    for (int i = 0; i < 12; i++) game.roll(10);
    game.score();   // executes code, asserts nothing
}
```

| Suite | Line % | Branch % | D2 detected? |
|---|---|---|---|
| Full suite | 100% | 100% | Yes |
| Assertion-free test only | 68.4% (39/57) | 50.0% (19/38) | No |

*Second row derived by analysing the paths this one test reaches (12 strikes, then `score()`); replace with measured values.* With no assertion the test can never fail on a wrong result.

### 8.4 Mutation testing (optional)

Add `pitest-maven` 1.15.8 with `pitest-junit5-plugin` 1.2.1 (target `com.example.bowling.*`) and run `mvn org.pitest:pitest-maven:mutationCoverage`; the report is in `target/pit-reports/`. Not performed in this project.

---

## 9. Discussion

**Strengths observed**
- No source changes: the agent instruments bytecode at run time.
- Fast: the report is produced in seconds as part of `mvn verify`.
- Integrated: Maven, Eclipse (EclEmma), CI, and an automatically published report.
- Coverage gate: the build fails if line or branch coverage drops below the limits.
- Branch-level detail: all 38 branch outcomes can be inspected per method.

**Limitations**
- **Coverage ≠ correctness:** 100% coverage says what ran, not whether it was verified (section 8.3, and D2/D4 in 8.2).
- It does not measure requirement coverage or test-data quality.
- Branch coverage counts decision outcomes, not condition combinations (compare MC/DC).
- Mutation testing gives a stronger quality signal.

## 10. Conclusion

Applying JaCoCo 0.8.12 to a ten-pin bowling scoring class with 31 JUnit 5 test executions (functional, boundary and error cases) gave 100% instruction (307/307), branch (38/38), line (57/57), complexity (23/23) and method (4/4) coverage, well above the 90% line and 85% branch gate. The most complex method, `roll(int)`, is fully covered, and all five seeded defects were detected, although JaCoCo coverage alone could not reveal them, so strong assertions must accompany high coverage. The tool was integrated into Maven, Eclipse and a GitHub Actions pipeline that publishes the report to GitHub Pages. Next steps: mutation testing with PIT, property-based tests, and a REST layer (`POST /games`, `POST /games/{id}/rolls`, `GET /games/{id}/score`, tested with `MockMvc`).

## 11. Troubleshooting

| Symptom | Fix |
|---|---|
| `release version 17 not supported` | JDK older than 17. Install 17+ or lower `maven.compiler.release` in `pom.xml`. |
| `Rule violated for bundle ... coverage ratio` | Expected when tests are removed (8.1). Otherwise add tests or lower the limits in `pom.xml`. |
| No `jacoco` folder in `target/site` | Tests did not run, or you ran `mvn compile`. Use `mvn clean verify`. |
| Eclipse: `Unsupported class file major version` in Coverage | Update EclEmma (*Help → Check for Updates*). |
| Eclipse: JUnit 5 tests not found | *Maven → Update Project…* and use the JUnit 5 runner. |
| Push rejected: workflow scope | Token lacks `workflow` scope; create a new token with `repo` + `workflow`. |
| Push rejected: non-fast-forward | The repo was not empty. *Team → Pull*, then push. |
| `?` icons stay after commit | New or ignored files: stage and commit again; check `.gitignore` is in the project root. |
| `deploy` job skipped or red | Set *Settings → Pages → Source → GitHub Actions*; use the default branch; check `test` produced `index.html`; for "environment protection rules" allow your branch under *Settings → Environments → github-pages*. |
| Dependencies will not download behind a proxy | Add a `<proxy>` block to `~/.m2/settings.xml`. |

## 12. References

- JaCoCo: https://www.jacoco.org/jacoco/trunk/doc/
- JUnit 5 User Guide: https://junit.org/junit5/docs/current/user-guide/
- EclEmma: https://www.eclemma.org/
- Maven Surefire Plugin: https://maven.apache.org/surefire/maven-surefire-plugin/
- GitHub Pages with Actions: https://docs.github.com/pages
- Software Testing and Quality Assurance Lab course material

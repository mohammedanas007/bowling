# Bowling Score Calculator: Unit Testing with JUnit 5 and JaCoCo

A small but non-trivial Java module (ten-pin bowling scoring with input validation) used to evaluate
**JaCoCo**, the industry-standard Java code-coverage tool. It covers:

- **Tool setup:** Maven + JaCoCo agent, Eclipse (m2e and EclEmma), GitHub Actions CI
- **Test design:** functional, boundary and error/edge-case tests (26 test methods, 31 executions)
- **Reporting:** pass/fail logs, execution time, assertion counts, coverage report, coverage gate, defect table

`REPORT_TEMPLATE.md` is a ready-to-fill write-up for the deliverables.

---

## 1. Project layout

```
bowling-jacoco/
├── pom.xml                                   JUnit 5 + Surefire + JaCoCo (report + coverage gate)
├── README.md                                 this tutorial
├── REPORT_TEMPLATE.md                        fill-in project report
├── scripts/summary.sh                        prints test + coverage numbers for the report
├── .github/workflows/build.yml               CI: build, test, coverage gate, upload reports
├── .gitignore
└── src
    ├── main/java/com/example/bowling/BowlingGame.java        System Under Test
    └── test/java/com/example/bowling/BowlingGameTest.java    test suite
```

## 2. Prerequisites

| Tool | Version | Check |
|---|---|---|
| JDK | 17 or newer | `java -version` |
| Maven | 3.9+ | `mvn -v` |
| Eclipse | "IDE for Java Developers" 2023-12 or newer (bundles m2e, Git, EclEmma) | *Help → About* |
| Git + GitHub account | any recent | `git --version` |

`JAVA_HOME` must point to the JDK, not a JRE, and `mvn -v` should report the same Java version.

## 3. Quick start (command line)

```bash
mvn clean verify
```

What happens, in order:

1. `compile` / `test-compile`: builds the SUT and the tests.
2. `prepare-agent`: JaCoCo adds its Java agent to the test JVM. The agent instruments classes as they load and records which instructions run into `target/jacoco.exec`.
3. `test`: Surefire runs the JUnit 5 tests. Logs go to the console and `target/surefire-reports/`.
4. `report` (JaCoCo): converts `jacoco.exec` into `target/site/jacoco/` (`index.html`, `jacoco.xml`, `jacoco.csv`).
5. `verify` → `check` (JaCoCo): **fails the build** if line coverage is under 90% or branch coverage is under 85% (edit in `pom.xml`).

Then print the numbers for your report:

```bash
bash scripts/summary.sh
```

Open `target/site/jacoco/index.html` in a browser to see coverage per class, per method and per source line.

### Where each deliverable comes from

| Deliverable | Source |
|---|---|
| Pass/fail logs | Console output; `target/surefire-reports/*.txt` and `TEST-*.xml` |
| Execution speed | `Time elapsed` in the `.txt` reports; per-test `time` in the XML; `scripts/summary.sh` |
| Assertion metrics | Tests run / passed / failed from Surefire; static assertion count (see `REPORT_TEMPLATE.md`) |
| Coverage report | `target/site/jacoco/index.html`: instruction, branch, line, complexity, method |
| Defect report | Seeded-defect experiment (section 8) |

## 4. Eclipse connectivity

### 4.1 Import the project

1. *File → Import → Maven → Existing Maven Projects*.
2. Browse to the `bowling-jacoco` folder and click *Finish*. m2e downloads the dependencies (first time only).
3. Right-click the project → *Properties → Java Compiler* → set compliance to 17 (or your JDK).
4. If the project shows red errors: right-click → *Maven → Update Project…* → *OK*.

### 4.2 Run the tests in Eclipse

- Right-click `BowlingGameTest.java` → *Run As → JUnit Test*.
- The **JUnit view** shows each test (with its `@DisplayName`), pass/fail, and time per test. Failures show the stack trace.

### 4.3 Run Maven from Eclipse

Right-click the project → *Run As → Maven build…* → Goals: `clean verify` → *Run*. The Console shows the same output as the command line, and `target/site/jacoco/index.html` is created. Refresh the project (F5) to see it.

### 4.4 EclEmma (JaCoCo inside Eclipse)

EclEmma is the Eclipse integration of JaCoCo and is usually already bundled. To check, right-click a test class: you should see **Coverage As**. If not:

1. *Help → Eclipse Marketplace…* → search **EclEmma Java Code Coverage** → *Install* → restart.

Then:

1. Right-click `BowlingGameTest.java` → *Coverage As → JUnit Test*.
2. Open `BowlingGame.java`. Lines are highlighted:
   - **green**: fully covered
   - **yellow**: a branch has only been taken one way
   - **red**: never executed
3. Open *Window → Show View → Coverage* to see percentages per package, class and method.
4. Export: in the Coverage view, *Export Session…* → choose **HTML report** (or XML/CSV) and a folder.

> EclEmma and the Maven plugin both use JaCoCo, so their numbers should agree. Small differences usually come from running different test sets.

## 5. GitHub setup

### 5.1 Create the repository and push

On github.com create a **new empty repository** named `bowling-jacoco` (no README, no .gitignore). Then:

```bash
cd bowling-jacoco
git init
git add .
git commit -m "Bowling game with JUnit 5 and JaCoCo"
git branch -M main
git remote add origin https://github.com/<your-username>/bowling-jacoco.git
git push -u origin main
```

From Eclipse instead: right-click project → *Team → Share Project… → Git → Create…*, then *Team → Commit…* and *Team → Push to Upstream* (add the remote URL on the first push).

### 5.2 CI pipeline

`.github/workflows/build.yml` runs on every push and pull request. It sets up JDK 17, runs `mvn -B clean verify` (tests, coverage gate), and uploads two artifacts: `jacoco-report` and `surefire-reports`.

To read results: GitHub → **Actions** tab → open the latest run → **Artifacts** → download `jacoco-report` → open `index.html`.

A **red** run means a test failed or coverage dropped below the gate, which is a useful thing to demonstrate in the report. Optional README badge (replace `<your-username>`):

```
![build](https://github.com/<your-username>/bowling-jacoco/actions/workflows/build.yml/badge.svg)
```

## 6. Test catalogue

| ID | Category | What it checks |
|---|---|---|
| F-01 | Functional | Gutter game = 0 |
| F-02 | Functional | All ones = 20 |
| F-03 | Functional | Spare bonus = next roll |
| F-04 | Functional | Strike bonus = next two rolls |
| F-05 | Functional | All spares (5/5) = 150 |
| F-06 | Functional | Perfect game = 300 |
| F-07 | Functional | Mixed game with strikes, spares and open frames = 167 |
| F-08 | Functional | Nine-and-miss ×10 = 90 |
| F-09 | Functional | Consecutive strikes build on each other |
| B-01 | Boundary | 0 and 10 pins accepted (parameterized, 2 runs) |
| B-02 | Boundary | 10th-frame strike → two fill balls |
| B-03 | Boundary | 10th-frame spare → one fill ball |
| B-04 | Boundary | Open 10th frame → game ends, no fill ball |
| B-05 | Boundary | 10th-frame strike + open fill balls |
| B-06 | Boundary | Not complete after nine frames |
| B-07 | Boundary | Not complete between 10th-frame spare and fill ball |
| B-08 | Boundary | Strike, strike, 10 allowed in the 10th |
| B-09 | Boundary | Strike, 0, 10 allowed in the 10th |
| E-01 | Error | -1, 11, 100, `MIN_VALUE`, `MAX_VALUE` rejected (parameterized, 5 runs) |
| E-02 | Error | Frame total > 10 rejected |
| E-03 | Error | 10th-frame second roll making total > 10 rejected |
| E-04 | Error | 10th-frame fill balls > 10 rejected |
| E-05 | Error | Roll after game complete → `IllegalStateException` |
| E-06 | Error | Score before completion → `IllegalStateException` |
| E-07 | Error | Score on a new game → `IllegalStateException` |
| E-08 | Error | A rejected roll leaves the state unchanged |

## 7. What JaCoCo measures

| Counter | Meaning |
|---|---|
| Instruction | Java bytecode instructions executed (JaCoCo's most basic unit) |
| Line | Source lines with at least one executed instruction |
| Branch | Both outcomes of each `if` / `&&` / `\|\|` / loop condition |
| Complexity | Cyclomatic complexity paths covered |
| Method / Class | Methods / classes executed at least once |

JaCoCo works by instrumenting bytecode on the fly through a Java agent, so no source changes are needed.
It tells you **what ran**, not **whether the assertions were meaningful**. Experiment 3 below demonstrates this.

## 8. Experiments for the report

### Experiment 1: baseline vs. incomplete suite
Comment out the `ErrorConditions` nested class, run `mvn clean verify`, and record the drop in line and branch coverage (the build will also fail the gate). Restore it afterwards.

### Experiment 2: seeded defects
Apply **one bug at a time** to `BowlingGame.java`, run `mvn clean test`, record which tests fail, then revert (`git checkout -- src/main`).

| # | Bug (edit in `BowlingGame.java`) | Tests expected to fail |
|---|---|---|
| D1 | In the validation, change `pins > 10` to `pins >= 10` | F-04, F-06, F-07, F-09, B-01 (10), B-02, B-05, B-08, B-09, E-04 |
| D2 | In `score()` spare branch, change `10 + rolls[i + 2]` to `10` (drop spare bonus) | F-03, F-05, F-07, B-03 |
| D3 | In the 10th frame, change `first + second < 10` to `true` (no fill balls) | F-05, F-06, F-07, B-02, B-03, B-05, B-07, B-08, B-09, E-04 |
| D4 | In frames 1-9, change `first + pins > 10` to `first + pins >= 10` | F-03, F-05, F-07, E-08 |
| D5 | In the 10th frame, remove the `first != 10 &&` guard | F-06, F-07, B-02, B-05, B-08, E-04 |

How the "expected" column was produced: each bug was applied to a copy of the class and the 31 test scenarios were replayed
with a plain-Java harness (no JUnit). Confirm it with the real JUnit run on your machine and put the actual results in your defect table;
IDs for parameterized tests may appear as `B-01 [2]` etc. in JUnit output.

Notice that **JaCoCo coverage stays the same for D2 and D4** (the same lines run); only the assertions notice the bug.

### Experiment 3: coverage ≠ quality
Add this test, run the build, and compare coverage with the baseline:

```java
@Test
void coverageWithoutAssertions() {
    for (int i = 0; i < 12; i++) game.roll(10);
    game.score();   // executes the code, asserts nothing
}
```

Then re-run D2 or D4 with only this kind of test and show that coverage is high while the bug is not detected.

### Experiment 4 (optional): mutation testing
Add the PIT plugin and compare the mutation score with JaCoCo coverage:

```xml
<plugin>
  <groupId>org.pitest</groupId>
  <artifactId>pitest-maven</artifactId>
  <version>1.15.8</version>
  <dependencies>
    <dependency>
      <groupId>org.pitest</groupId>
      <artifactId>pitest-junit5-plugin</artifactId>
      <version>1.2.1</version>
    </dependency>
  </dependencies>
  <configuration>
    <targetClasses><param>com.example.bowling.*</param></targetClasses>
    <targetTests><param>com.example.bowling.*</param></targetTests>
  </configuration>
</plugin>
```

Run `mvn org.pitest:pitest-maven:mutationCoverage`; the report is in `target/pit-reports/`.

## 9. Troubleshooting

| Symptom | Fix |
|---|---|
| `release version 17 not supported` | Your JDK is older than 17. Install JDK 17+ or lower `maven.compiler.release` in `pom.xml`. |
| `Rule violated for bundle ... coverage ratio` | Expected when tests are removed (Experiment 1). Otherwise add tests or lower the limits in `pom.xml`. |
| No `jacoco` folder in `target/site` | The tests did not run (check Surefire output), or you ran `mvn compile`. Use `mvn clean verify`. |
| Eclipse shows `Unsupported class file major version` in Coverage | Update EclEmma (*Help → Check for Updates*) so its JaCoCo supports your JDK. |
| Eclipse: JUnit 5 tests not found | *Maven → Update Project…* (Alt+F5), and make sure the Run configuration uses the **JUnit 5** test runner. |
| Dependencies will not download behind a company proxy | Add a `<proxy>` block to `~/.m2/settings.xml`. |
| Windows: `bash scripts/summary.sh` fails | Use Git Bash or WSL, or read the numbers directly from the HTML report. |

## 10. Optional extension: REST service

To turn the SUT into a REST service, wrap `BowlingGame` in Spring Boot with `POST /games`, `POST /games/{id}/rolls` and
`GET /games/{id}/score`. Test it with `MockMvc` or `@SpringBootTest`. JaCoCo uses the same plugin configuration. Add a
`<excludes>` entry for the `Application` class if you do not want the `main` method to count against coverage.

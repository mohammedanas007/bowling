# Specialized Tool Exploration and Unit Testing with JaCoCo
### Project report: Bowling Score Calculator (Java)

| | |
|---|---|
| **Student / Team** | ___ |
| **Course / Instructor** | ___ |
| **Date** | ___ |
| **Repository** | https://github.com/___/bowling-jacoco |
| **CI run** | link to a green GitHub Actions run |

> Instructions: replace every `___` with your own results. Items marked **(from README)** are already
> true for the provided code. Run `mvn clean verify` and `bash scripts/summary.sh` first.

---

## 1. Objective and overview

The objective is to evaluate **JaCoCo**, an industry-standard code-coverage tool for Java, by applying it to a
standalone Java module and measuring how well a unit-test suite exercises it.

Write 3 to 5 sentences covering:
- what JaCoCo is and which problem it addresses (visibility into which code the tests execute)
- how it works (a Java agent instruments bytecode at class-load time and writes `jacoco.exec`; the report goal turns it into HTML/XML/CSV)
- the counters it provides: instruction, line, branch, complexity, method, class
- why coverage alone is not a quality guarantee (see section 8)

## 2. System Under Test

**Module:** `com.example.bowling.BowlingGame` (105 lines, **(from README)**)

| Member | Behaviour |
|---|---|
| `roll(int pins)` | Records a roll (0-10). Throws `IllegalArgumentException` for an invalid pin count or frame total; throws `IllegalStateException` if the game is complete. |
| `isComplete()` | True once frame 10 (including fill balls) is finished. |
| `score()` | Computes the total with strike/spare bonuses. Throws `IllegalStateException` if the game is incomplete. |

Rules implemented: open frames, spares (+next roll), strikes (+next two rolls), 10th-frame fill balls (strike → 2, spare → 1, open → 0).

Why this SUT is suitable: many decision points (`if`, `&&`, `||`, loop), stateful logic, and both normal and error paths, so line and branch coverage differ in a meaningful way.

## 3. Tool setup

| Item | Version / detail |
|---|---|
| JDK | ___ |
| Maven | ___ |
| JUnit | 5.10.2 **(from README)** |
| Surefire | 3.2.5 **(from README)** |
| JaCoCo | 0.8.12 **(from README)** |
| IDE | Eclipse ___ with m2e and EclEmma ___ |
| CI | GitHub Actions, `.github/workflows/build.yml` |

Describe in your own words:
1. How JaCoCo is configured in `pom.xml` (`prepare-agent`, `report`, `check`) and what each execution does.
2. The coverage gate: line ≥ 90%, branch ≥ 85%.
3. Importing the project into Eclipse and running **Coverage As → JUnit Test**.
4. The pipeline: push → GitHub Actions → tests → coverage gate → artifacts.

Include screenshots: (a) Eclipse project tree, (b) Maven console showing `BUILD SUCCESS`, (c) green GitHub Actions run.

## 4. Test design

**26 test methods, 31 test executions** (two parameterized tests) **(from README)**.

| Category | IDs | Technique |
|---|---|---|
| Functional correctness | F-01 to F-09 | Known-score games, bonus rules |
| Boundary limits | B-01 to B-09 | 0/10 pins, 10th-frame transitions, game-completion edges |
| Edge / error conditions | E-01 to E-08 | Invalid input, illegal state, state integrity after a rejected roll |

Copy the full catalogue from README section 6, or add columns for *Input*, *Expected result*, and *Actual result*.

Design techniques used (name them and give an example of each): equivalence partitioning, boundary-value analysis, error guessing, state-based testing.

## 5. Execution results

Paste the output of `bash scripts/summary.sh`.

| Metric | Value |
|---|---|
| Tests run | ___ (expected 31) |
| Passed | ___ |
| Failed | ___ |
| Errors | ___ |
| Skipped | ___ |
| Total execution time (s) | ___ |
| Slowest test class | ___ (`target/surefire-reports`) |

**Assertion metrics**

| Metric | Value |
|---|---|
| Assertion call sites in the test source | 33 **(from README)** |
| `assertEquals` / `assertThrows` / `assertTrue` / `assertFalse` / `assertDoesNotThrow` | 14 / 8 / 5 / 2 / 4 |
| Assertion executions at run time | about 38 (parameterized tests re-run their assertion) |
| Assertions per test method (average) | about 1.3 |

Note on the execution time: the first run includes JVM and JaCoCo agent start-up. Run `mvn test` twice and report both, then comment on the agent's overhead (expected to be small for a suite this size).

Include: screenshot of the Eclipse JUnit view (all green) and an excerpt of `target/surefire-reports/*.txt`.

## 6. Coverage results

| Counter | Covered / total | % |
|---|---|---|
| Instruction | ___ | ___ |
| Branch | ___ | ___ |
| Line | ___ | ___ |
| Complexity | ___ | ___ |
| Method | ___ | ___ |

Include: screenshot of `target/site/jacoco/index.html` and of `BowlingGame.java.html` (green/yellow/red lines), plus the EclEmma view.

Analysis questions to answer:
1. Are there any yellow or red lines? Which branch outcome or statement is missed, and which test would cover it?
2. Which method has the highest cyclomatic complexity, and is it covered fully? (Expected: `roll`.)
3. Do EclEmma and Maven agree? If not, why?

### Experiment 1: coverage without the error tests

| Suite | Line % | Branch % | Gate result |
|---|---|---|---|
| Full suite | ___ | ___ | ___ |
| Without `ErrorConditions` | ___ | ___ | ___ |

Which lines and branches became uncovered? What does this show about the value of error-path tests?

## 7. Defect report (seeded defects)

Apply one bug at a time, run `mvn clean test`, and record the result. The expected column comes from the README.

| ID | Bug and location | Tests expected to fail | Tests that actually failed | Detected? |
|---|---|---|---|---|
| D1 | `pins > 10` → `pins >= 10` in `roll` | F-04, F-06, F-07, F-09, B-01 (10), B-02, B-05, B-08, B-09, E-04 | ___ | ___ |
| D2 | Spare bonus dropped in `score()` | F-03, F-05, F-07, B-03 | ___ | ___ |
| D3 | `first + second < 10` → `true` (no fill balls) | F-05, F-06, F-07, B-02, B-03, B-05, B-07, B-08, B-09, E-04 | ___ | ___ |
| D4 | `first + pins > 10` → `>= 10` in frames 1-9 | F-03, F-05, F-07, E-08 | ___ | ___ |
| D5 | `first != 10 &&` guard removed in the 10th frame | F-06, F-07, B-02, B-05, B-08, E-04 | ___ | ___ |

Detection rate: ___ / 5. Did JaCoCo coverage change for each bug? (For D2 and D4 it should not.)

Optional: log any **real** defects you found while writing the tests (ID, description, found by, severity, status).

## 8. Discussion: strengths and limitations of JaCoCo

Strengths to discuss: zero source changes, fast, Maven/Eclipse/CI integration, coverage gate, multiple report formats, branch-level detail.

Limitations to demonstrate with evidence:
- **Coverage ≠ correctness.** Experiment 3: a test with no assertions raises coverage but detects nothing.

  | Suite | Line % | Branch % | D2 detected? |
  |---|---|---|---|
  | Full suite | ___ | ___ | ___ |
  | Assertion-free test only | ___ | ___ | ___ |

- It does not measure requirement coverage or the quality of test data.
- Branch coverage counts decision outcomes, not combinations of conditions (compare with MC/DC).
- Optional: mutation score from PIT vs. JaCoCo coverage: ___

## 9. Conclusion

3 to 5 sentences: what was achieved, the final coverage numbers, the defect detection rate, what you would add next (e.g. mutation testing, REST layer, property-based tests).

## 10. References

- JaCoCo documentation: https://www.jacoco.org/jacoco/trunk/doc/
- JUnit 5 User Guide: https://junit.org/junit5/docs/current/user-guide/
- EclEmma: https://www.eclemma.org/
- Maven Surefire Plugin: https://maven.apache.org/surefire/maven-surefire-plugin/
- Add your course material and any other sources you used.

## Appendix: commands used

```bash
mvn clean verify
bash scripts/summary.sh
git push origin main
```

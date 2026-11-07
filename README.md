# EACO

Network routing simulator comparing Enhanced Ant Colony Optimisation (EACO) with AntNet and OSPF,
with a Swing GUI.

## Requirements
- JDK 17 or later and Maven (tested with OpenJDK 27 and Maven 3.10)

## Build and run
The GUI (`com.ds2016.Main`) runs from IntelliJ IDEA: open the repo as a project and run `Main`.
IntelliJ compiles `Gui.form`; the Maven build does not, so the GUI doesn't start from it.

## Test
```sh
mvn test
```
Success: `BUILD SUCCESS` with 30 tests, 0 failures.

## Layout
- `AcoNetworkGui/src/com/ds2016/`: simulator and algorithms (`ui/` GUI, `networks/` topologies)
- `src/test/java/`: JUnit 5 tests

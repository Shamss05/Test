# Troubleshooting Guide

## Common Issues and Solutions

### 1. Build Errors

#### Issue: "mvn: command not found"

**Solution:**

- Install Maven from https://maven.apache.org/
- Add Maven bin directory to PATH
- Restart terminal/command prompt

#### Issue: "JAVA_HOME is not set"

**Solution:**

```bash
# Windows
set JAVA_HOME=C:\Program Files\Java\jdk-21
set PATH=%JAVA_HOME%\bin;%PATH%

# Add permanently via System Environment Variables
```

#### Issue: "Unsupported class file major version"

**Solution:**

- Project requires Java 21
- Check Java version: `java -version`
- Install JDK 21 or higher
- Update JAVA_HOME to point to JDK 21

### 2. JavaFX Issues

#### Issue: "Error: JavaFX runtime components are missing"

**Solution:**

- Maven should automatically download JavaFX
- Run: `mvn clean install` to ensure dependencies are downloaded
- Check internet connection

#### Issue: "Could not find or load main class"

**Solution:**

- Use `mvn javafx:run` instead of `mvn exec:java`
- Ensure module-info.java is present
- Run `mvn clean compile` first

### 3. Runtime Issues

#### Issue: GUI not responding

**Solution:**

- Don't run simulations with > 100K points in sequential mode with visualization
- Use parallel mode for large datasets
- Clear canvas before running new simulation

#### Issue: "OutOfMemoryError"

**Solution:**

- Increase heap size:

```bash
set MAVEN_OPTS=-Xmx2g
mvn javafx:run
```

- Reduce number of points in simulation

#### Issue: Visualization not showing points

**Solution:**

- Check that points < 50,000 for visible dots
- Larger datasets only update progress (performance optimization)
- Use sequential mode for better visualization of small datasets

### 4. Performance Issues

#### Issue: Parallel slower than sequential

**Possible Causes:**

- Sample size too small (< 100K points)
- Too many threads (> CPU cores)
- Thread creation overhead dominates

**Solution:**

- Use parallel only for N > 100,000
- Set threads = number of CPU cores
- Run experiments to find optimal configuration

#### Issue: Inaccurate results

**Solution:**

- Increase sample size (more points = better accuracy)
- Run multiple trials and average
- Expected error is ~1/√N

### 5. FXML Loading Issues

#### Issue: "Location is not set"

**Solution:**

- Ensure FXML file is in correct location:
  `src/main/resources/edu/montecarlo/gui/main.fxml`
- Check package structure matches
- Run `mvn clean compile` to copy resources

#### Issue: "javafx.fxml.LoadException"

**Solution:**

- Check FXML syntax
- Ensure fx:controller matches class name
- Verify all fx:id attributes have corresponding @FXML fields

### 6. Compilation Issues

#### Issue: "Cannot find symbol" errors

**Solution:**

- Ensure all classes in correct packages
- Check import statements
- Run `mvn clean compile -X` for detailed errors

#### Issue: "Module not found"

**Solution:**

- Check module-info.java is present
- Ensure requires/exports are correct
- Maven compiler plugin version is 3.11.0+

## Getting Help

### Check These First

1. Java version: `java -version` (should be 21+)
2. Maven version: `mvn -version` (should be 3.6+)
3. Project structure matches documentation
4. All files in correct directories

### Debug Commands

```bash
# Verbose build
mvn clean compile -X

# Check dependency tree
mvn dependency:tree

# Clean everything and rebuild
mvn clean install

# Skip tests (if applicable)
mvn clean install -DskipTests
```

### IDE-Specific Issues

#### IntelliJ IDEA

- File → Project Structure → Project SDK → Set to JDK 21
- File → Project Structure → Modules → Language level → 21
- Enable annotation processing
- Invalidate caches and restart

#### Eclipse

- Right-click project → Properties → Java Build Path
- Set JRE to JDK 21
- Project → Update Maven Project
- Clean and rebuild

#### VS Code

- Install "Extension Pack for Java"
- Install "JavaFX Support"
- Set java.home in settings.json to JDK 21
- Reload window

## Performance Optimization Tips

### For Faster Builds

```bash
# Build without JavaDoc
mvn clean install -Dmaven.javadoc.skip=true

# Parallel build
mvn clean install -T 4
```

### For Better Runtime Performance

- Use parallel mode for > 100K points
- Set threads = CPU cores (check with Task Manager)
- Close other applications
- Run from command line (not IDE) for accurate timing

## Verification Steps

After solving any issue:

1. **Clean build:**

   ```bash
   mvn clean compile
   ```

2. **Run tests:**

   ```bash
   mvn test
   ```

3. **Launch GUI:**

   ```bash
   mvn javafx:run
   ```

4. **Verify functionality:**
   - Start with 1,000 points sequential
   - Check visualization appears
   - Verify π estimate is ~3.14
   - Test parallel mode
   - Run batch experiments

## Still Having Issues?

Check:

- [ ] Java 21 installed and in PATH
- [ ] Maven 3.6+ installed and in PATH
- [ ] JAVA_HOME set correctly
- [ ] Internet connection (for dependencies)
- [ ] Sufficient disk space (> 500MB)
- [ ] All files in correct directories
- [ ] No antivirus blocking Java/Maven

## Contact

If issues persist:

1. Check error messages carefully
2. Search error message online
3. Review Maven output for specific errors
4. Consult course instructor or TA

---

**Last Updated:** December 2025

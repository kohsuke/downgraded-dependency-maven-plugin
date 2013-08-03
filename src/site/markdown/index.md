What is this?
=============

One of the common pitfall of the transitive dependency resolution logic in Maven is that
the "nearest declaration wins" rule.

Let'sa say your Maven project depends on library foo and bar, and they each dependend on `commons-io`:

    org.kohsuke:my-project:1.0-SNAPSHOT
      +- com.example:foo:1.6.0
      |    +- com.example:foo-foundation:1.6.0
      |         +- commons-io:commons-io:2.0
      +- org.example:bar:2.0
           +- commons-io:commons-io:1.4

Foo depends on commons-io 2.0, and bar depends on commons-io 1.4. In this case, one normally wants
Maven to pick commons-io 2.0, but Maven actually in this case picks up 1.4, because it's closer to
the root of the dependency graph.

The reason Maven does this is so that the version you specified directly in your POM wins over
any versions specified in transitive dependencies. But for every instance this rule helps you,
I feel like there's at least equal number of instances where this rule hurts you, by picking up
a wrong old version. The problem is particularly nasty beucase such linkage error happens only at runtime.


This plugin resolves this problem by scanning the entire dependency graph and look for the occurence
of such error-prone version resolution, where an older version of an artifact is picked over a newer version.

The plugin allows you to specify a list of exclusions, which is the list of artifacts you are knowingly
picking up an olde version over a newer version used deeper in the transitive dependency graph. By explicitly
managing exclusions, you prevent surprises.

Usage
=====
Add the plugin to your POM like this:

    <build>
      <plugins>
        <plugin>
          <groupId>org.kohsuke</groupId>
          <artifactId>downgraded-dependency-maven-plugin</artifactId>
          <executions>
            <execution>
              <goals>
                <goal>check</goal>
              </goals>
              <configuration>
                <excludes>
                  <exclude>commons-io:commons-io</exclude>
                  <exclude>org.kohsuke.myproject:*</exclude>

The exclusion list is optional.

When the checker fails, run `mvn dependency:tree -Dverbose=true` to get the details about which two
paths are causing the problem. If you are OK with picking up the older version, add it to the exclusion list.
If you want Maven to pick up a newer version, you'll have to explicitly specify the dependency in your POM.
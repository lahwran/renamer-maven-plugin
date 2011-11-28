renamer-maven-plugin
====================

This plugin provides two goals:
*******************************

- **rename**: renames the classes produced by the compile step, using dependencies as static
references (defaults to dynamic renames)

- **rename-dependencies**: extracts dependency jars to the classes directory and renames them, allowing
renaming of dependencies.

both goals are intended to be run at the process-classes phase.

Configuration arguments
***********************

- **refereceArtifacts**: artifacts to include in dependency processing. Uses same
selection format as the shade plugin's artifactSet. In rename-dependencies,
these artifacts are included in the output, and in rename, they are searched in
static reference processing for classes to rename.

- **renames**: a list of renames.


The renames section
-------------------

The renames section is quite powerful. you can use it to rename by package name
or by class name. it takes a list of rename arguments, each of which has the
following subarguments:

- **from**: a regex specifying what should be renamed. Is matched on the whole
   name, as if "^%s$" % from was used. Any groups used are available in the to
   expansion.

- **to**: a regex expansion specifying what to rename to. \1-style group
   references are expanded (though this has not been tested, so if it breaks,
   please notify me).
   
- **dynamic**: a boolean indicating whether to do a dynamic rename or a static
   rename. the difference is rather subtle, and will only have a significant
   effect with dependencies or very large rename lists: dynamic renames are
   evaluation per reference to a class found in the bytecode; static renames
   are evaluated before the renamer executes, by searching the source tree for
   classes. dynamic renames rename *all references*, whereas static renames only
   rename *references to classes in the source tree or included dependencies*.
   dynamic names are also marginally slower, but this will only have a
   significant effect on very large source trees with many renames in place.
   Defaults to "true".

- **packageOnly**: a boolean indicating whether to process the pattern in this
   rename as a package name or as a class name. the difference is that for
   package names, the regex is matched on everything leading up to the class
   name, and a class rename is run on the whole name. Defaults to "true".

Examples
--------

rename-dependencies:

      <plugin>
        <groupId>net.lahwran</groupId>
        <artifactId>renamer-maven-plugin</artifactId>
        <version>1.0</version>
        <executions>
          <execution>
            <phase>process-classes</phase>
            <goals>
              <goal>rename-dependencies</goal>
            </goals>
            <configuration>
              <refereceArtifacts>
                <includes>
                  <include>net.minecraft:minecraft</include>         
                </includes>
              </refereceArtifacts>
              <renames>
                <rename>
                  <from></from>
                  <to>deobf</to>
                </rename>
              </renames>
            </configuration>
          </execution>
        </executions>
      </plugin>

rename:

      <plugin>
        <groupId>net.lahwran</groupId>
        <artifactId>renamer-maven-plugin</artifactId>
        <version>1.0</version>
        <executions>
          <execution>
            <phase>process-classes</phase>
            <goals>
              <goal>rename</goal>
            </goals>
            <configuration>
              <refereceArtifacts>
                <includes>
                  <include>net.minecraft:minecraft-deobf</include>         
                </includes>
              </refereceArtifacts>
              <renames>
                <rename>
                  <from>deobf</from>
                  <to></to>
                </rename>
              </renames>
            </configuration>
          </execution>
        </executions>
      </plugin>

Todo
----

- doesn't delete empty directories after execution
- could use a maven repo somewhere
- rename-dependencies requires a separate artifact for each renamed dependency,
   would be better if it simply renamed dependencies before the compile stage

# classtags-maven-plugin

This maven plug reads the entire classpath of your project (including system
jars and extension jars) in order to output a .classtags file in the root of you
project folder.

Once you have this list we can write vim/emacs plugins in order to help facility
auto importing and javadoc lookups

## Using

    mvn net.kelsin:classtags-maven-plugin:generate

After running this you will have a `.classtags` file in your project's base
directory. This fill is a sorted list of fully qualified classnames that are
accessible in your project's classpath.

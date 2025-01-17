#!/bin/sh

# $1 the version passed by the maven call

PACKAGE_NAME=lod2demo
VERSION=$1

echo "Building Debian package for ${MODULE_NAME}"
echo

rm -rf ../../target/deb-pkg
mkdir -p ../../target/deb-pkg

# Extract the tarball to the package workspace
#tar xfz data.tar.gz --directory ../../target/deb-pkg

# copy war file to package workspace
# remove the version in the name
cp ../../target/${PACKAGE_NAME}-${VERSION}.war ../../target/deb-pkg/lod2demo.war
cp -r ../../src/main/html/ ../../target/deb-pkg/
cp ../../src/main/html/*html ../../target/deb-pkg/
mkdir ../../target/deb-pkg/configuration
cp -r ../../src/main/configuration/* ../../target/deb-pkg/configuration
cp -r ../../src/main/configuration/lod2demo.conf ../../target/deb-pkg/
# Add the Debian control files
cp -r debian ../../target/deb-pkg
cp    Makefile ../../target/deb-pkg
cp    test.sql ../../target/deb-pkg
cp    create_user.sql ../../target/deb-pkg
cp    remove_user.sql ../../target/deb-pkg
cp    vad_install.sql ../../target/deb-pkg
cp    proxy.conf      ../../target/deb-pkg
cp    create_user_sparql.sql ../../target/deb-pkg
cp    sparqled_config.xml ../../target/deb-pkg
rapper -o rdfxml-abbrev  -i ntriples ../../src/main/configuration/configuration.nt > ../../target/deb-pkg/configuration/configuration.rdf

# Build the package
cd ../../target/deb-pkg
debuild --check-dirname-level 0 -b

#!/bin/sh

MODULE_NAME=lod2demo_1.0

echo "Building Debian package for ${MODULE_NAME}"
echo

rm -rf ../../target/deb-pkg
mkdir -p ../../target/deb-pkg

# Extract the tarball to the package workspace
#tar xfz data.tar.gz --directory ../../target/deb-pkg

# copy war file to package workspace
cp ../../target/lod2demo-1.0.war ../../target/deb-pkg
# Add the Debian control files
cp -r debian ../../target/deb-pkg

# Build the package
cd ../../target/deb-pkg
debuild --check-dirname-level 0 -b

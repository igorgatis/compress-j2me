#!/bin/sh

svn_root="https://compress-j2me.googlecode.com/svn"

project="$1"
version="$2"

if [ -z $project ] || [ -z $version ]; then
  echo "Usage: $0 <project> <version>" ;
  exit 1
fi
if [ ! -d $project ]; then
  echo "Directory './$project' does not exist."
  exit 1
fi

release="$project-$version"

output="`svn status $project`"
if [ -n "$output" ]; then
  echo "There are pending changes under './$project'."
  echo "Try: svn status ./$project"
  exit 1
fi
output="`svn status $project-test`"
if [ -n "$output" ]; then
  echo "There are pending changes under './$project-test'."
  echo "Try: svn status ./$project-test"
  exit 1
fi

msg="Relase of $project version $version."
cmd[0]="svn -m \"$msg\" mkdir $svn_root/tags/$release/"
cmd[1]="svn -m \"$msg\" copy $svn_root/trunk/src/$project $svn_root/tags/$release/"
cmd[2]="svn -m \"$msg\" copy $svn_root/trunk/src/$project-test $svn_root/tags/$release/"

fake_url="http://compress-j2me"
echo "${cmd[0]//$svn_root/$fake_url}"
echo "${cmd[1]//$svn_root/$fake_url}"
echo "${cmd[2]//$svn_root/$fake_url}"

read -p "Are you sure [y/N]? " -n 1
echo
if [[ ! $REPLY =~ ^[Yy]$ ]]; then
  echo "Aborting."
  exit 1
fi

echo "${cmd[0]} && ${cmd[1]} && ${cmd[2]}"

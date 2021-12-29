#!/bin/bash
set -xeuo

for exe in "kubernetes-cli" "kustomize" "skaffold"; do
    if ! brew list | (grep -q $exe; ret=$?; cat >/dev/null; exit $ret); then
      brew install $exe
    fi
done

file=$HOME/bin/container-structure-test
if [ ! -x "$file" ]; then
  curl -LO https://storage.googleapis.com/container-structure-test/latest/container-structure-test-linux-amd64 && chmod +x container-structure-test-linux-amd64 && mkdir -p $HOME/bin && export PATH=$PATH:$HOME/bin && mv container-structure-test-linux-amd64 $HOME/bin/container-structure-test
else
  echo "didn't need to install $file"
fi

case $(grep -q "apex.evidence.com" /etc/hosts >/dev/null; echo $?) in
  0) echo "did not modify hosts file";;
  1) echo "127.0.0.1 apex.evidence.com" | sudo tee -a /etc/hosts;;
  *) echo "unexpected error occurred";;
esac

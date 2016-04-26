.PHONY: build test

build:
	xbuild Mammoth.sln

test: build
	mono --debug packages/xunit.runner.console.2.1.0/tools/xunit.console.exe Mammoth.Tests/bin/Debug/Mammoth.Tests.dll -noshadow

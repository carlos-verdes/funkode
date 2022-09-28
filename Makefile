# Makefile

MAKEFLAGS += -j2
-include .env
export

CURRENT_BRANCH := $(shell git rev-parse --abbrev-ref HEAD)
CURRENT_PATH := $(shell pwd)
DEFAULT_BRANCH := $(shell git remote show upstream | sed -n '/HEAD branch/s/.*: //p')
AMM := ${HOME}/amm

.PHONY: gitRebase
gitRebase:
	git checkout $(DEFAULT_BRANCH) && \
		git pull upstream $(DEFAULT_BRANCH) && \
		git push origin $(DEFAULT_BRANCH) && \
		git checkout $(CURRENT_BRANCH) && \
		git rebase develop

.PHONY: gitAmmend
gitAmmend:
	git add . && git commit --amend --no-edit && git push --force origin $(CURRENT_BRANCH)

.PHONY: killJava
killJava:
	ps ax | grep java | grep -v 'grep' | cut -d '?' -f1 | xargs kill -9

.PHONY: amm
amm:
ifeq ("$(wildcard $(AMM))", "")
	@echo "Installing ammonite $(AMM)"
	sudo sh -c '(echo "#!/usr/bin/env sh" && \
		curl -L https://github.com/lihaoyi/Ammonite/releases/download/2.3.8/2.13-2.3.8) \
		> $(AMM) && \
		chmod +x $(AMM)'
endif
	$(AMM)

.PHONY: run
run:
	sbt rest/run

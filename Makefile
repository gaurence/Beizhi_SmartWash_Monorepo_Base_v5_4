SHELL := /bin/bash

ITER ?= ITER-00
APPS ?= all
NPM_CACHE_DIR := $(CURDIR)/.npm-cache

FRONTEND_APPS := agent-admin cabinet-screen customer-app device-investor platform-admin platform-cs platform-finance platform-iotops platform-ops property-portal store-admin technician-app

.PHONY: setup-tooling codegen-frontend verify-docs verify-openapi verify-asyncapi verify-page-coverage verify-traceability verify-visual backend-test frontend-build

setup-tooling: node_modules

node_modules: package.json
	npm_config_cache="$(NPM_CACHE_DIR)" npm install --no-fund --no-audit

codegen-frontend: setup-tooling
	npm run codegen:frontend

verify-docs: setup-tooling
	npm run verify:docs

verify-openapi: setup-tooling
	npm run verify:openapi

verify-asyncapi: setup-tooling
	npm run verify:asyncapi

verify-page-coverage: codegen-frontend
	ITER=$(ITER) npm run verify:page-coverage

verify-traceability: setup-tooling
	ITER=$(ITER) npm run verify:traceability

verify-visual: codegen-frontend frontend-build
	ITER=$(ITER) ENABLE_PLAYWRIGHT_VISUAL=1 npm run verify:visual

backend-test:
	cd backend && mvn -q test

frontend-build: codegen-frontend
	@set -e; \
	apps="$(APPS)"; \
	if [[ "$$apps" == "all" ]]; then \
	  apps="$(FRONTEND_APPS)"; \
	fi; \
	for app in $$apps; do \
	  echo "==> Building frontend/$$app"; \
	  if [[ ! -f "frontend/$$app/package.json" ]]; then \
	    echo "Missing frontend app: $$app"; \
	    exit 1; \
	  fi; \
	  (cd "frontend/$$app" && npm_config_cache="$(NPM_CACHE_DIR)" npm install --no-fund --no-audit && npm run build); \
	done

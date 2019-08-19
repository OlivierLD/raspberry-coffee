#!/usr/bin/env bash
swagger-codegen generate --lang jaxrs-jersey --input-spec sample.yaml --output ./generated/jaxrs --api-package samples.io --verbose

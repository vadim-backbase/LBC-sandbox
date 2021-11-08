#!/bin/bash

set -e
set -o pipefail

ACTION=run

if [[ -n $DEBUG_PORT ]]; then
	ACTION=debug

	WLP_DEBUG_SUSPEND=n
	WLP_DEBUG_REMOTE=y
	WLP_DEBUG_ADDRESS=$DEBUG_PORT

	export WLP_DEBUG_SUSPEND WLP_DEBUG_REMOTE WLP_DEBUG_ADDRESS
fi

if ${ADMIN_CENTER:-false}; then
	cat /opt/ibm/wlp/usr/servers/defaultServer/server-admin.xml > /opt/ibm/wlp/usr/servers/defaultServer/server.xml
	/opt/ibm/wlp/bin/installUtility install defaultServer
fi

exec /opt/ibm/helpers/runtime/docker-server.sh /opt/ibm/wlp/bin/server ${ACTION} defaultServer


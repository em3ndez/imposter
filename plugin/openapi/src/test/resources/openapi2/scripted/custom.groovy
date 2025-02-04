// Example of returning a specific status code, to control which
// specification example is returned in the response.

// checks for a particular parameter
logger.info("Query params: ${context.request.queryParams}")
if (context.request.queryParams.param1 == 'foo') {
    respond {
        withStatusCode 202
        skipDefaultBehaviour()
    }
}

// check for deprecated params map 1
if (context.params.param1 == 'bar') {
    respond {
        withStatusCode 202
        skipDefaultBehaviour()
    }
}

// check for deprecated params map 2
if (context.request.params.param1 == 'qux') {
    respond {
        withStatusCode 203
        skipDefaultBehaviour()
    }
}

// check for deprecated uri property
if (context.uri == 'baz') {
    respond {
        withStatusCode 400
        skipDefaultBehaviour()
    }
}

// applies to URIs ending with '/apis'
if (context.request.uri ==~ /(.*)\/apis$/) {

    // applies to PUT requests only
    switch (context.request.method) {
        case 'PUT':
            respond {
                withStatusCode 201
                withHeader("MyHeader", "MyHeaderValue")
            }
            break

        case 'GET':
            if (context.request.headers.Authorization == "AUTH_HEADER") {
                respond {
                    withStatusCode 204
                }
            } else {
                respond {
                    usingDefaultBehaviour()
                }
            }
            break

        default:
            // fallback to specification examples
            respond {
                usingDefaultBehaviour()
            }
            break
    }
}

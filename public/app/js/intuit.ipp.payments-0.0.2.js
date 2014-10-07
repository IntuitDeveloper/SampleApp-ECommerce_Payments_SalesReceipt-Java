// DO NOT REMOVE VERSION NUMBER FROM FILE. REQUIRED FOR CACHE BUSTING

if (typeof intuit === 'undefined' || !intuit) {
    intuit = {}; // since intuit is in global scope and because of a bug in IE we don't do a 'var intuit'.
}

if (!intuit.ipp) {
    intuit.ipp = {};
}

if (!intuit.ipp.payments) {
    intuit.ipp.payments = {};
}

intuit.ipp.payments = {
    version: '0.0.2',
    tokenizationUrl : function (tokenizationHost, apiKey, apptoken, isForIE) {
        if (isForIE) {
            return tokenizationHost + "/ie?intuit_apikey=" + apiKey + "&apptoken=" + apptoken;
        }
        return tokenizationHost + "?apptoken=" + apptoken;
    },
    tokenize: function (apptoken, cardData, callback) {
        // checks
        if (!apptoken || typeof apptoken != "string" || apptoken.length < 5) {
            if (console && console.log) {
                console.log("App token not passed");
            }
            return false;
        }
        if (!cardData || typeof cardData != "object") {
            if (console && console.log) {
                console.log("Card data not passed");
            }
            return false;
        }
        
        var tokenizationHost = "https://api.intuit.com/quickbooks/v4/payments/tokens";
        var apiKey = "ipp-" + apptoken;
        if (intuit.ipp.payments.domain != "appcenter.intuit.com" &&
            intuit.ipp.payments.domain != "js.appcenter.intuit.com") { // not production

            if (intuit.ipp.payments.domain == "appcenter-stage.intuit.com") {
                tokenizationHost = "https://transaction-api-e2e.payments.intuit.net/v2/tokens";
            } else {
                tokenizationHost = "https://transaction-api-qal.payments.intuit.net/v2/tokens";
            }
        }
        var xhr = new XMLHttpRequest();
        if ("withCredentials" in xhr) {

            // Check if the XMLHttpRequest object has a "withCredentials" property.
            // "withCredentials" only exists on XMLHTTPRequest2 objects.
            xhr.open("POST", intuit.ipp.payments.tokenizationUrl(tokenizationHost, apiKey, apptoken, false), true);
            xhr.setRequestHeader("authorization", "Intuit_APIKey intuit_apikey=" + apiKey);
            xhr.setRequestHeader("content-type", "application/json");
            xhr.send(JSON.stringify(cardData));
            
            xhr.onreadystatechange = function () {
                if (xhr.readyState == 4) {
                    if (xhr.status == 201) {
                        var response = null;
                        try {
                            response = JSON.parse(xhr.responseText);
                        } catch (ex) {
                            if (console) {
                                console.log("Exception while parsing JSON");
                            }
                            return callback(null, {
                                "code": "",
                                "message": "Unknown error",
                                "detail": "No detail",
                                "moreinfo": "Check response"
                            });
                        }
                        callback(response.value);
                        return false;
                    }
                    if (console) {
                        console.log("HTTP status " + xhr.status + " encountered");
                    }
                    // there was an issue
                    callback(null, {
                        "code": "",
                        "message": "Unknown error",
                        "detail": "No detail",
                        "moreinfo": "Check response"
                    });
                    return false;
                }
                return false;
            };
        } else if (typeof XDomainRequest != "undefined") {

            // Otherwise, check if XDomainRequest.
            // XDomainRequest only exists in IE, and is IE's way of making CORS requests.
            xhr = new XDomainRequest();
            xhr.open("POST", intuit.ipp.payments.tokenizationUrl(tokenizationHost, apiKey, apptoken, true), true);
            xhr.send(JSON.stringify(cardData));

            xhr.onload = function () {
                var response = null;
                try {
                    response = JSON.parse(xhr.responseText);
                } catch (ex) {
                    if (console) {
                        console.log("Exception while parsing JSON");
                    }
                    return callback(null, {
                        "code": "",
                        "message": "Unknown error",
                        "detail": "No detail",
                        "moreinfo": "Check response"
                    });
                }
                //XDomainRequest does not pass the status. Assume everything went well.
                callback(response.value);
                return false;
            };

            xhr.onerror = function() {
                // there was an issue
                return callback(null, {
                    "code": "",
                    "message": "Unknown error",
                    "detail": xhr.responseText,
                    "moreinfo": "Check response"
                });
            };
        } else {
            // Otherwise, CORS is not supported by the browser.
            xhr = null;
            if (console) {
                console.log("Browser does not support CORS, cannot execute tokenization request");
            }
            callback(null, {
                "code": "",
                "message": "Browser not supported",
                "detail": "Browser does not support CORS",
                "moreinfo": "Upgrade browser"
            });
            return false;
        }
        
        return xhr;
    },
    gup: function (name) {
        name = name.replace(/[\[]/, "\\\[").replace(/[\]]/, "\\\]");
        var regexS = "[\\?&]" + name + "=([^&#]*)";
        var regex = new RegExp(regexS);
        var results = regex.exec(window.location.href);
        if (results == null)
            return null;
        else
            return results[1];
    }
};


(function () {
    // Since scripts are executed sequentially, the currently executed script tag is always the last script tag 
    // on the page until then. So, to get the script tag, you can do
    var scripts = document.getElementsByTagName('script');
    var thisScriptTag = scripts[scripts.length - 1];
    intuit.ipp.payments.hostname = urlDomain(thisScriptTag.src);
    // to get the domain
    function urlDomain(data) {
        var a = document.createElement('a');
        a.href = data;
        return a.hostname;
    }
})();
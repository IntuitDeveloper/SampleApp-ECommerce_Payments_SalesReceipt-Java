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
    version: '0.0.1',
    tokenize: function (cardData, callback) {
        var tokenizationHost = "transaction-api.payments.intuit.net";
        var apiKey = "akyresrEyIBgD2Wpf9Dqqzm4q3Yrsa36LNSuqjUO";
        if (intuit.ipp.payments.domain != "appcenter.intuit.com") { // not production
            tokenizationHost = "transaction-api-qal.payments.intuit.net/qa2";
            apiKey = "akyresrEyIBgD2Wpf9Dqqzm4q3Yrsa36LNSuqjUO";
        }
        var xhr = new XMLHttpRequest();
        if ("withCredentials" in xhr) {

            // Check if the XMLHttpRequest object has a "withCredentials" property.
            // "withCredentials" only exists on XMLHTTPRequest2 objects.
            xhr.open("POST", "https://" + tokenizationHost + "/v2/tokens", true);
            xhr.setRequestHeader("authorization", "Intuit_APIKey intuit_apikey=" + apiKey);
            xhr.setRequestHeader("content-type", "application/json");
            xhr.setRequestHeader("company_id", "123456789");
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
            xhr.open("POST", "https://" + tokenizationHost + "/v2/tokens/ie?intuit_apikey=" + apiKey, true);
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
    // we don't supprt IE8 or 9
    var ieversion = msieversion();
    if (ieversion > 0 && ieversion < 10) {
        console.log("Tokenization API does not currently support IE versions 9 and below. Please upgrade your browser");
        return false;
    }

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
    
    function msieversion() {
        var ua = window.navigator.userAgent;
        var msie = ua.indexOf("MSIE ");

        if (msie > 0)      // If Internet Explorer, return version number
            return parseInt(ua.substring(msie + 5, ua.indexOf(".", msie)));
        else                 // If another browser, return 0
            return 0;
    }
})();
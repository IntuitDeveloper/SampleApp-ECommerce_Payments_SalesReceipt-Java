// delay.js

function mult() {
	var m = parseFloat(browser.params.timeoutMult);
	return (isNaN(m)) ? 1 : m;
}

exports.untilDisplayed = function(timeout, el, fn, params) {
	// ignoreSynchronization needs to be true while loading the page.
	var oldSetting = browser.ignoreSynchronization;
	browser.ignoreSynchronization = true;

    var promise = browser.wait(function() {
		browser.ignoreSynchronization = true;	// IgnoreSync may need to be set to true multiple times (an async process could be setting it back to false).
        return el.isPresent().then(function(isPresent) {
        	if(isPresent) {
        		el = element(el.locator());	// Re-find element (in case of StaleElementReferenceError)
	        	return el.isDisplayed().then(function(isDisplayed) {
	        		return isDisplayed;
	        	}, function(err){
		    		// Wait the full timeout (ex: page might still be loading, and StaleElementReferenceError simply means isDisplayed() should be retried).
		    		return false;
	        	});
	        } else {
	        	return false;
	        }
        }, function(err) {
        	// Wait the full timeout (ex: page might still be loading, and StaleElementReferenceError simply means isPresent() should be retried).
			return false;
        });
    }, timeout*mult());

    // Run a function on the webelement (ex: sendKeys) after it is displayed.
    if(fn) el[fn](params);

    // Reset ignoreSync
    browser.ignoreSynchronization = oldSetting;

    // Return the results of isPresent (in case test wants to utilize an expect statement).
    return promise;
};

exports.untilNotDisplayed = function(timeout, el, fn, params) {
	// ignoreSynchronization needs to be true while loading the page.
	var oldSetting = browser.ignoreSynchronization;
	browser.ignoreSynchronization = true;

	var promise = browser.wait(function() {
		browser.ignoreSynchronization = true;	// IgnoreSync may need to be set to true multiple times (an async process could be setting it back to false).
        return el.isPresent().then(function(isPresent) {
        	if(isPresent) {
        		el = element(el.locator());	// Re-find element (in case of StaleElementReferenceError)
	        	return el.isDisplayed().then(function(isDisplayed) {
	        		return isDisplayed == false;
	        	}, function(err){
		    		// Wait the full timeout (ex: page might still be loading, and StaleElementReferenceError simply means isDisplayed() should be retried).
		    		return false;
	        	});
	        } else {
	        	return false;
	        }
        }, function(err) {
        	// Wait the full timeout (ex: page might still be loading, and StaleElementReferenceError simply means isPresent() should be retried).
			return false;
        });
    }, timeout*mult());

    // Run a function on the webelement (ex: sendKeys) after it is displayed.
    if(fn) el[fn](params);

    // Reset ignoreSync
    browser.ignoreSynchronization = oldSetting;

    // Return the results of isPresent (in case test wants to utilize an expect statement).
    return promise;
};
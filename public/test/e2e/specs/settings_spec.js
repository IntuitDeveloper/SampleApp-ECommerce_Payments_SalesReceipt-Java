var delay = require('../tools/delay.js');

describe('settings page', function() {
    var ptor;
    var page = {};


    beforeEach(function() {
        console.log("I'm doing something!");
        browser.get('#/settings');
        ptor = protractor.getInstance();
        page.connectToQBButton = element.all(by.css('a.intuitPlatformConnectButton')).first();
        page.customerSyncButton = element.all(by.css('button.btn')).first();
        page.itemSyncButton = element.all(by.css('button.btn')).last();
    });

    it('should show the connect to quickbooks button and sync should be disabled', function() {
        expect(ptor.isElementPresent(page.connectToQBButton)).toBe(true);

        // ensure customerSyncButton is present and disabled
        expect(ptor.isElementPresent(page.customerSyncButton)).toBe(true);
        expect(ptor.isElementPresent(page.customerSyncButton.getAttribute('disabled'))).toBe(true);

        // ensure itemSyncButton is present and disabled
        expect(ptor.isElementPresent(page.itemSyncButton)).toBe(true);
        expect(ptor.isElementPresent(page.itemSyncButton.getAttribute('disabled'))).toBe(true);

        page.connectToQBButton.click();

        browser.getAllWindowHandles().then(function(handles) {
            browser.switchTo().window(handles[1]);
            ptor.findElement(by.id('ius-sign-in-header')); // HACK to wait for loading?

            expect(browser.getCurrentUrl()).toContain('https://appcenter.intuit.com/Connect/Begin?oauth_token=');

            var emailInput = element(by.name('Email'));
            var passwordInput = element(by.name('Password'));
            var submitButton = element(by.id('ius-sign-in-submit-btn'));
            emailInput.sendKeys(browser.params.qbo_username);
            passwordInput.sendKeys(browser.params.qbo_password);
            submitButton.click();
        });

    });
});
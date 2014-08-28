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
            ptor.findElement(By.id('ius-sign-in-header')).then(function(){
                expect(browser.getCurrentUrl()).toContain('https://appcenter.intuit.com/Connect/Begin?oauth_token=');
            });
        });

    });
});
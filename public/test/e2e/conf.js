exports.config = {
    seleniumAddress: 'http://localhost:4444/wd/hub',
    baseUrl: 'http://localhost:8080/app/index.html',

    specs: [
        './**/*_spec.js'
    ],
    onPrepare: function () {
        // The require statement must be down here, since jasmine-reporters
        // needs jasmine to be in the global and protractor does not guarantee
        // this until inside the onPrepare function.
        require('jasmine-reporters');
        var jUnitXmlReporter = new jasmine.JUnitXmlReporter('', true, true, 'e2e-');
        jasmine.getEnv().addReporter(jUnitXmlReporter);


        browser.manage().timeouts().pageLoadTimeout(40000);
        browser.manage().timeouts().implicitlyWait(25000);

        browser.ignoreSynchronization = true; //needed to allow the ConnectToQBO button to show up
    },

    jasmineNodeOpts: {
        defaultTimeoutInterval: 30000
    }
};
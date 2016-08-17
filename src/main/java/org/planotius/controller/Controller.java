package org.planotius.controller;

import org.planotius.controller.functions.SeleniumServer;
import org.planotius.helper.Config;
import org.planotius.helper.PropertiesLoader;
import java.io.File;
import java.io.FileOutputStream;
import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import org.openqa.selenium.Alert;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.UnreachableBrowserException;

/**
 *
 * @author ggodoy
 */
public class Controller {

    private static final Logger log = Logger.getLogger(Controller.class.getName());
    public static SeleniumServer server;
    private static WebDriver driver;
    private String browser;
    private String testServer;
    private String port;
    private String url;
    public static Config config;

    public Controller setUrl(String url) {
        this.url = url;
        return this;
    }

    public Config getConfig() {
        return config;
    }

    public void quit() {
        try {
            driver.quit();
        } catch (UnreachableBrowserException ube) {
            log.warn("Unreachable browser exception raised (Selenium bug). " + ube.getMessage());
        }
    }

    public SeleniumServer getServer() {
        return server;
    }

    public static WebDriver getDriver() {
        return driver;
    }

    public Controller() {
        PropertiesLoader properties = new PropertiesLoader();
        this.browser = properties.getValue("browser");

        if (System.getProperty("browser") != null) {
            this.browser = System.getProperty("browser");
        } else {
            this.browser = properties.getValue("browser");
        }

        if (System.getProperty("testserver") != null) {
            this.testServer = System.getProperty("testserver");
        } else {
            this.testServer = properties.getValue("testserver");
        }

        if (System.getProperty("port") != null) {
            this.port = System.getProperty("port");
        } else {
            this.port = properties.getValue("port");
        }

        if (System.getProperty("url") != null) {
            this.url = System.getProperty("url");
        } else {
            this.url = properties.getValue("url");
        }

        connectServer();
    }

    private void connectServer() {
        server = new SeleniumServer(browser, testServer, port);
        driver = server.startServer();
        log.info("Selenium started: [" + browser + ", " + testServer + ", " + port + "]");
    }

    public String getAlertMessage() {
        try {
            Alert alert = driver.switchTo().alert();
            String alertMessage = alert.getText();
            return alertMessage;
        } catch (Exception e) {
            return "no alert found.";
        }
    }

    public String clickAlert() {
        try {
            Alert alert = driver.switchTo().alert();
            String msg = alert.getText();
            alert.accept();
            return msg;
        } catch (Exception e) {
            log.info("no alert found.");
            return null;
        }
    }

    public Object runJavaScript(String function) {
        try {
            return ((JavascriptExecutor) getDriver()).executeScript(function);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return e;
        }

    }

    /**
     * Search in the current html for the desired text
     *
     * @param text
     * @return boolean
     */
    public boolean searchHtmlContents(String text) {
        boolean exist = Controller.getDriver().getPageSource().contains(text);
        log.info("Text : '" + text + "' finded? " + exist);
        return exist;

    }

    public boolean verifyMessage(String text) {
        boolean b = searchHtmlContents(text);
        return b;
    }

    /**
     * PrintScreen and save with the desired file name.
     *
     * @param fileName
     * @return fileName
     */
    public String printScreen(String fileName) {
        try {
            String base64Screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BASE64);

            byte[] decodedScreenshot = Base64.decodeBase64(base64Screenshot.getBytes());
            FileOutputStream fos = new FileOutputStream(new File(fileName));
            fos.write(decodedScreenshot);
            fos.close();

            File f = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            log.info("Screenshot from server '" + server.getTestServer() + "' saved in: '" + fileName + "'");
            return fileName;
        } catch (Exception e) {
            log.error(e.getMessage() + " Error when getting screenshot from server '" + server.getTestServer() + "' in: '" + fileName + "'", e);
            return null;
        }
    }

    /**
     * Open the desired url
     *
     */
    public Controller openUrl() {
        driver.get(this.url.replace("\"", ""));
        driver.manage().window().maximize();
        log.info("Url acessed: '" + this.url.replace("\"", "") + "'");
        return this;
    }

    public String getTestServer() {
        return testServer;
    }

    public String getBrowser() {
        return browser;
    }

    public String getPort() {
        return port;
    }

    public String getUrl() {
        return url;
    }
}

package raw.crawling;

import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

public class Main {
    public static void main(String[] args) {
        WebDriver driver = new ChromeDriver();
        driver.manage().window().setSize(new Dimension(1400, 1000));

        try {
            String url = "https://lida.pt/";
            Crawler.procurarPessoasOuSubsites(url, driver);
            NomeDetector.procurarNomesEmJsons("output");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            driver.quit();
        }
    }
}
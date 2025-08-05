package raw.crawling;

import org.openqa.selenium.*;
import java.util.*;

public class SubPageExtractor {

    public static List<String> extrairSubpaginasInternas(WebDriver driver, String paginaPrincipal) {
        String dominioBase = UrlUtils.getDominioBase(paginaPrincipal);
        Set<String> subpaginas = new LinkedHashSet<>();

        // Extrair links da navbar (para ignorar depois)
        Set<String> linksNavbar = new HashSet<>();
        try {
            List<WebElement> navbarLinks = driver
                    .findElements(By.cssSelector("nav a[href], header a[href], .navbar a[href]"));
            for (WebElement el : navbarLinks) {
                String href = el.getAttribute("href");
                if (href != null && !href.isEmpty())
                    linksNavbar.add(href);
            }
        } catch (Exception e) {
            System.err.println("⚠️ Erro ao extrair links da navbar: " + e.getMessage());
        }

        // Extrair todos os links da página
        List<WebElement> links = driver.findElements(By.xpath("//a[@href]"));
        for (WebElement link : links) {
            String href = link.getAttribute("href");
            if (href == null || href.isEmpty())
                continue;

            if (href.startsWith("#") || href.startsWith("javascript"))
                continue;

            if (href.startsWith("/")) {
                href = dominioBase + href;
            } else if (!href.startsWith("http")) {
                href = dominioBase + "/" + href;
            }

            if (href.equals(paginaPrincipal) || href.equals(paginaPrincipal + "/"))
                continue;

            if (linksNavbar.contains(href))
                continue;

            if (!UrlUtils.pertenceAoMesmoDominio(href, paginaPrincipal))
                continue;

            subpaginas.add(href);
        }

        // Mostrar e processar
        if (subpaginas.isEmpty()) {
            System.out.println("❌ Nenhuma subpágina encontrada.");
        } else {
            System.out.println("📂 Subpáginas encontradas:");
            for (String link : subpaginas) {
                System.out.println("   → " + link);

                try {
                    driver.get(link);
                    Thread.sleep(500);
                    PerfilExtractor.extrairLinksExternosDePerfis(Collections.singletonList(link), driver);
                } catch (Exception e) {
                    System.err.println("⚠️ Erro ao aceder perfil: " + link + " → " + e.getMessage());
                }
            }
        }

        return new ArrayList<>(subpaginas);
    }
}

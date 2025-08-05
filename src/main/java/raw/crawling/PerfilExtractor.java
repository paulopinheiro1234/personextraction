package raw.crawling;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.*;

public class PerfilExtractor {

    private static final List<String> PLATAFORMAS = Arrays.asList("orcid.org", "cienciavitae.pt", "scholar.google");

    public static void extrairLinksExternosDePerfis(List<String> subpaginas, WebDriver driver) {
        for (String subpagina : subpaginas) {
            System.out.println("\n🌐 A aceder à subpágina: " + subpagina);
            try {
                driver.get(subpagina);
                esperarCarregamento(driver);

                List<String> perfis = encontrarPerfisIndividuais(driver, subpagina);
                if (perfis.isEmpty()) {
                    System.out.println(
                            "❗ Nenhum sublink de perfil encontrado. A procurar links externos na própria página.");
                    mostrarLinksExternos(driver, subpagina); // ✅ Vai registar no ficheiro
                    continue; // não há perfis, já tratámos a página principal
                }

                boolean encontrouLinksEmAlgumPerfil = false;

                for (String perfil : perfis) {
                    System.out.println("   👤 Perfil: " + perfil);
                    driver.get(perfil);
                    esperarCarregamento(driver);

                    Map<String, List<String>> nomes = extrairLinksExternos(driver);
                    List<String> anonimos = extrairLinksSemTexto(driver);

                    List<String> todosLinks = new ArrayList<>();
                    for (List<String> listaLinks : nomes.values()) {
                        todosLinks.addAll(listaLinks);
                    }
                    todosLinks.addAll(anonimos);

                    if (!todosLinks.isEmpty()) {
                        ExternalLinkWriter.registarLinks(perfil, todosLinks);
                        encontrouLinksEmAlgumPerfil = true;

                        System.out.println("   🔗 Links externos encontrados no perfil:");
                        nomes.forEach((nome, link) -> System.out.println("      → " + nome + " → " + link));
                        anonimos.forEach(link -> System.out.println("      → (sem nome) → " + link));
                    }
                }

                // ✅ Se nenhum perfil individual tiver links, tenta na página base
                if (!encontrouLinksEmAlgumPerfil) {
                    mostrarLinksExternos(driver, subpagina);
                }

            } catch (Exception e) {
                System.err.println("⚠️ Erro ao processar subpágina " + subpagina + ": " + e.getMessage());
            }
        }
    }

    private static void mostrarLinksExternos(WebDriver driver, String paginaOrigem) {
        Map<String, List<String>> nomesELinks = extrairLinksExternos(driver);
        List<String> anonimos = extrairLinksSemTexto(driver);

        if (!nomesELinks.isEmpty()) {
            System.out.println("   🔗 Nomes com links externos encontrados:");
            nomesELinks.forEach((nome, link) -> System.out.println("      → " + nome + " → " + link));
        }

        if (!anonimos.isEmpty()) {
            System.out.println("   🔗 Links externos sem nome visível (associados à página): " + paginaOrigem);
            anonimos.forEach(link -> System.out.println("      → " + paginaOrigem + " → " + link));
        }

        if (nomesELinks.isEmpty() && anonimos.isEmpty()) {
            System.out.println("   ❌ Nenhum link externo encontrado.");
        }
        List<String> todosLinks = new ArrayList<>();
        for (List<String> listaLinks : nomesELinks.values()) {
            todosLinks.addAll(listaLinks);
        }
        todosLinks.addAll(anonimos);
        ExternalLinkWriter.registarLinks(paginaOrigem, todosLinks);
    }

    public static Map<String, List<String>> extrairLinksExternos(WebDriver driver) {
        Map<String, List<String>> linksComTexto = new LinkedHashMap<>();
        List<WebElement> links = driver.findElements(By.tagName("a"));

        for (WebElement link : links) {
            String href = link.getAttribute("href");
            String texto = link.getText().trim();

            if (href != null && !href.isEmpty()) {
                for (String plataforma : PLATAFORMAS) {
                    if (href.contains(plataforma)) {
                        texto = texto.isEmpty() ? href : texto;
                        linksComTexto.computeIfAbsent(texto, k -> new ArrayList<>()).add(href);
                    }
                }
            }
        }

        return linksComTexto;
    }

    public static List<String> extrairLinksSemTexto(WebDriver driver) {
        List<String> linksAnonimos = new ArrayList<>();
        List<WebElement> links = driver.findElements(By.tagName("a"));

        for (WebElement link : links) {
            String href = link.getAttribute("href");
            String texto = link.getText().trim();

            if (href != null && !href.isEmpty()) {
                for (String plataforma : PLATAFORMAS) {
                    if (href.contains(plataforma)) {
                        if (texto.isEmpty()) {
                            linksAnonimos.add(href);
                        }
                    }
                }
            }
        }

        return linksAnonimos;
    }

    private static List<String> encontrarPerfisIndividuais(WebDriver driver, String baseUrl) {
        Set<String> perfis = new LinkedHashSet<>();
        List<WebElement> links = driver.findElements(By.xpath("//a[@href]"));

        for (WebElement link : links) {
            String href = link.getAttribute("href");
            if (href == null || href.isEmpty())
                continue;
            if (href.equals(baseUrl) || href.equals(baseUrl + "/"))
                continue;
            if (href.startsWith(baseUrl))
                perfis.add(href);
        }

        return new ArrayList<>(perfis);
    }

    private static void esperarCarregamento(WebDriver driver) {
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(7));
            wait.until(webDriver -> ((JavascriptExecutor) webDriver)
                    .executeScript("return document.readyState").equals("complete"));
            Thread.sleep(1000); // garantir renderização de JS adicional
        } catch (Exception e) {
            System.err.println("⚠️ Timeout ao esperar pelo carregamento da página.");
        }
    }

}

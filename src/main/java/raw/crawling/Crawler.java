package raw.crawling;

import org.openqa.selenium.*;

import java.time.Duration;
import java.util.*;
import java.util.regex.Matcher;

public class Crawler {

    private static final List<String> KEYWORDS = Arrays.asList(
            "people", "equipa", "team", "staff", "membros", "investigadores", "researchers", "member");

    private static void aceitarCookies(WebDriver driver) {
        try {
            List<By> seletores = Arrays.asList(
                    By.tagName("button"),
                    By.tagName("a"),
                    By.tagName("div"),
                    By.tagName("span"));

            for (By seletor : seletores) {
                List<WebElement> elementos = driver.findElements(seletor);
                for (WebElement el : elementos) {
                    String texto = el.getText().toLowerCase().trim();
                    if (texto.contains("aceitar") || texto.contains("accept") || texto.contains("aceitar todos")
                            || texto.contains("accept all")) {
                        if (el.isDisplayed() && el.isEnabled()) {
                            try {
                                el.click();
                                System.out.println("✅ Botão de cookies aceite: " + texto);
                                Thread.sleep(1000);
                                return;
                            } catch (Exception e) {
                                System.err.println("⚠️ Erro ao clicar no botão: " + texto);
                            }
                        }
                    }
                }
            }

            List<WebElement> iframes = driver.findElements(By.tagName("iframe"));
            for (WebElement iframe : iframes) {
                try {
                    driver.switchTo().frame(iframe);
                    aceitarCookies(driver);
                    driver.switchTo().defaultContent();
                    return;
                } catch (Exception e) {
                    driver.switchTo().defaultContent();
                }
            }

            System.out.println("ℹ️ Nenhum botão de cookies foi encontrado ou clicável.");
        } catch (Exception e) {
            System.err.println("⚠️ Erro ao tentar aceitar cookies: " + e.getMessage());
        }
    }

    private static void fazerScrollCompleto(WebDriver driver) {
        JavascriptExecutor js = (JavascriptExecutor) driver;

        while (true) {
            js.executeScript("window.scrollBy(0, 600);");

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }

            boolean chegouAoFim = (boolean) js.executeScript(
                    "return (window.innerHeight + window.pageYOffset) >= document.body.scrollHeight;");

            if (chegouAoFim) {
                System.out.println("✅ Scroll completo — fim da página alcançado.");
                break;
            }
        }
    }

    public static void procurarPessoasOuSubsites(String url, WebDriver driver) {
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(2));
        driver.get(url);
        aceitarCookies(driver);

        String titulo = driver.getTitle().toLowerCase();
        System.out.println("\n🌐 Título: " + titulo);
        System.out.println("🔍 A procurar páginas com nomes de pessoas em: " + url);

        for (String keyword : KEYWORDS) {
            if (url.toLowerCase().contains(keyword) || titulo.contains(keyword)) {
                System.out.println("✔️ A URL ou o título contém keyword: " + keyword);
                fazerScrollCompleto(driver);
                extrairEVerificarNomes(driver, url);
                return;
            }
        }

        Map<String, String> linksNavbar = extrairLinksNavbarComTexto(driver, url);
        boolean encontrou = false;

        for (Map.Entry<String, String> entry : linksNavbar.entrySet()) {
            String texto = entry.getKey().toLowerCase();
            String href = entry.getValue().toLowerCase();
            for (String keyword : KEYWORDS) {
                if (texto.contains(keyword) || href.contains(keyword)) {
                    System.out.println("\n✅ Link potencial com nomes encontrado: " + texto + " → " + href);
                    driver.get(href);
                    encontrou = true;
                    extrairEVerificarNomes(driver, href);
                    break;
                }
            }
            if (encontrou)
                break;
        }

        if (!encontrou) {
            for (String href : linksNavbar.values()) {
                // ignora anchors
                if (href.startsWith("#") || href.contains("#"))
                    continue;

                String dominioAtual = UrlUtils.getDominioPrincipal(url);
                String dominioNovo = UrlUtils.getDominioPrincipal(href);

                // se for subdomínio ou domínio diferente, mas relacionado
                String dAtualLimpo = dominioAtual.replaceAll("\\W", "");
                String dNovoLimpo = dominioNovo.replaceAll("\\W", "");

                if (!dominioNovo.equals(dominioAtual) &&
                        (dNovoLimpo.contains(dAtualLimpo) || dAtualLimpo.contains(dNovoLimpo))) {
                    System.out.println("\n🔁 A seguir para subsite relacionado: " + href);
                    procurarPessoasOuSubsites(href, driver);
                    return;
                }
            }
            System.out.println("\n❌ Nenhuma página com nomes de pessoas encontrada nesta estrutura.");
        }
    }

    private static Map<String, String> extrairLinksNavbarComTexto(WebDriver driver, String urlBase) {
        Map<String, String> mapaTextoParaHref = new LinkedHashMap<>();
        Set<String> linksUnicos = new HashSet<>();

        try {
            List<WebElement> links = driver.findElements(By.cssSelector("a[href]"));

            for (WebElement el : links) {
                Point p = el.getLocation();
                if (p.getY() < 500) {
                    String href = el.getAttribute("href");
                    String texto = getTextoVisivelOuAlt(driver, el);
                    if (href != null && !href.isEmpty()
                            && !href.startsWith("javascript:")
                            && linksUnicos.add(href)
                            && (UrlUtils.pertenceAoMesmoDominio(urlBase, href) || UrlUtils.saoDominiosRelacionados(urlBase, href))) {

                        for (String keyword : KEYWORDS) {
                            if (href.toLowerCase().contains(keyword) || texto.toLowerCase().contains(keyword)) {
                                System.out.println("🎯 Link prioritário encontrado: " + texto + " → " + href);
                                Map<String, String> resultado = new LinkedHashMap<>();
                                resultado.put(texto, href);
                                return resultado;
                            }
                        }

                        mapaTextoParaHref.put(texto, href);
                        System.out.println("➡️  Link: " + texto + " --> " + href);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Erro a extrair links da navbar: " + e.getMessage());
        }
        return mapaTextoParaHref;
    }

    private static String getTextoVisivelOuAlt(WebDriver driver, WebElement el) {
        try {
            String texto = el.getText();
            if (texto != null && !texto.trim().isEmpty())
                return texto.trim();

            WebElement img = el.findElement(By.tagName("img"));
            String alt = img.getAttribute("alt");
            if (alt != null && !alt.trim().isEmpty())
                return alt.trim();
        } catch (Exception ignored) {
        }

        try {
            return ((JavascriptExecutor) driver)
                    .executeScript("return arguments[0].innerText || arguments[0].textContent || ''", el)
                    .toString().trim();
        } catch (Exception ignored) {
        }

        return "(sem texto)";
    }

    private static void extrairEVerificarNomes(WebDriver driver, String nameURL) {
        try {
            WebElement body = driver.findElement(By.tagName("body"));
            String conteudo = body.getText();

            Matcher matcher = NomeDetector.getPadraoNome().matcher(conteudo);
            Set<String> nomesEncontrados = new LinkedHashSet<>();

            while (matcher.find()) {
                nomesEncontrados.add(matcher.group(1).trim());
            }

            if (!nomesEncontrados.isEmpty()) {
                System.out.println("👥 Nomes encontrados nesta página:");
                for (String nome : nomesEncontrados) {
                    System.out.println(" → " + nome);
                }

                Map<String, List<String>> nomesComLinks = PerfilExtractor.extrairLinksExternos(driver);
                if (!nomesComLinks.isEmpty()) {
                    System.out.println("🔗 Nomes com links externos encontrados nesta página:");
                    nomesComLinks.forEach((nome, link) -> System.out.println("   → " + nome + " → " + link));
                    ExternalLinkWriter.registarNomesComLinks(nameURL, nomesComLinks);
                }

                SubPageExtractor.extrairSubpaginasInternas(driver, nameURL);

            } else {
                System.out.println("❌ Nenhum nome encontrado nesta página.");
            }

        } catch (Exception e) {
            System.err.println("Erro ao extrair nomes: " + e.getMessage());
        }
    }
}
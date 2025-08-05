package raw.crawling;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.FileReader;
import java.io.File;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NomeDetector {

    private static final Set<String> STOPWORDS = Set.of(
            "Lisboa", "Porto", "Aveiro", "Faculdade", "Investigação", "Ciência",
            "Investigador", "Estudos", "Colaborador", "Grupos", "Categoria",
            "Doutorado", "Integrado", "Equipa", "Organização", "Documentos", "Números", "Contactos", "Formação",
            "Comunicação", "Agenda", "Projetos", "Revistas", "Sobre", "Nós", "Política", "Cookies");

    private static final Pattern PADRAO_NOME = Pattern.compile(
            "\\b([A-ZÁÉÍÓÚÂÊÔÃÕÇ][a-záéíóúâêôãõç'’-]{1,}(?:\\s+[A-ZÁÉÍÓÚÂÊÔÃÕÇ][a-záéíóúâêôãõç'’.]{1,}){1,4})\\b");

    public static void procurarNomesEmJsons(String pasta) {
        File dir = new File(pasta);
        if (!dir.exists() || !dir.isDirectory()) {
            System.err.println("❌ Pasta inválida: " + pasta);
            return;
        }

        for (File file : Objects.requireNonNull(dir.listFiles((d, name) -> name.endsWith(".json")))) {
            System.out.println("\n📄 A ler ficheiro: " + file.getName());
            try (FileReader reader = new FileReader(file)) {
                JSONArray array = (JSONArray) new JSONParser().parse(reader);
                for (Object obj : array) {
                    JSONObject pagina = (JSONObject) obj;
                    String url = (String) pagina.get("url");
                    String conteudo = (String) pagina.get("conteudo");
                    System.out.println("🔍 A verificar nomes na página: " + url);

                    Set<String> nomes = extrairNomes(conteudo);
                    if (!nomes.isEmpty()) {
                        System.out.println("✔️ Nomes encontrados:");
                        nomes.forEach(nome -> System.out.println("   • " + nome));
                    } else {
                        System.out.println("❌ Nenhum nome detetado.");
                    }
                }
            } catch (Exception e) {
                System.err.println("Erro ao processar ficheiro: " + file.getName() + " → " + e.getMessage());
            }
        }
    }

    public static Set<String> extrairNomes(String texto) {
        Set<String> nomes = new LinkedHashSet<>();
        Matcher matcher = PADRAO_NOME.matcher(texto);

        while (matcher.find()) {
            String nome = matcher.group(1).trim();

            // Ignora se só tiver uma palavra (ex: "Equipa", "Filtrar")
            if (nome.split("\\s+").length < 2)
                continue;

            // Ignora se o nome for exatamente uma stopword
            if (STOPWORDS.contains(nome))
                continue;

            // Ignora se TODAS as palavras forem stopwords (ex: "Categoria Técnica")
            String[] palavras = nome.split("\\s+");
            boolean todasStopwords = true;
            for (String palavra : palavras) {
                if (!STOPWORDS.contains(palavra)) {
                    todasStopwords = false;
                    break;
                }
            }
            if (todasStopwords)
                continue;

            nomes.add(nome);
            if (nomes.size() >= 100)
                break; // evita overload
        }

        return nomes;
    }

    public static Pattern getPadraoNome() {
        return PADRAO_NOME;
    }

    public static Set<String> getStopwords() {
        return STOPWORDS;
    }
}
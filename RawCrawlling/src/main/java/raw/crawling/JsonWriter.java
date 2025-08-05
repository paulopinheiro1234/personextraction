package raw.crawling;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;


public class JsonWriter {

public static void guardarPaginas(List<PaginaConteudo> paginas, String caminhoFicheiro) {
    Gson gson = new GsonBuilder().setPrettyPrinting().create();

    // Substituir manualmente os \n por quebras de linha reais
    List<PaginaConteudo> paginasFormatadas = paginas.stream()
        .map(p -> new PaginaConteudo(p.getUrl(), p.getConteudo().replace("\\n", "\n")))
        .toList();

    try (FileWriter writer = new FileWriter(caminhoFicheiro)) {
        gson.toJson(paginasFormatadas, writer);
        System.out.println("✅ Conteúdo guardado formatado em: " + caminhoFicheiro);
    } catch (IOException e) {
        System.err.println("Erro ao guardar JSON: " + e.getMessage());
    }
}

    public static class PaginaConteudo {
        private final String url;
        private final String conteudo;

        public PaginaConteudo(String url, String conteudo) {
            this.url = url;
            this.conteudo = conteudo;
        }

        public String getUrl() {
            return url;
        }

        public String getConteudo() {
            return conteudo;
        }
    }
}

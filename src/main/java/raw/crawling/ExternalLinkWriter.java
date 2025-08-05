package raw.crawling;

import java.io.*;
import java.nio.file.Files;
import java.util.*;

public class ExternalLinkWriter {
    private static final String FICHEIRO_OUTPUT = "links_perfis.txt";
    private static final Set<String> linhasJaEscritas = new HashSet<>();

    static {
        carregarLinhasJaEscritas();
    }

    public static synchronized void registarLinks(String subpagina, List<String> linksExternos) {
        if (linksExternos == null || linksExternos.isEmpty())
            return;

        try {
            File ficheiro = new File(FICHEIRO_OUTPUT);
            List<String> linhas = ficheiro.exists() ? Files.readAllLines(ficheiro.toPath()) : new ArrayList<>();

            int indiceSubpagina = -1;
            for (int i = 0; i < linhas.size(); i++) {
                if (linhas.get(i).contains(subpagina)) {
                    indiceSubpagina = i;
                    break;
                }
            }

            if (indiceSubpagina == -1) {
                // Subpágina ainda não existe, adicionar tudo no final
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(FICHEIRO_OUTPUT, true))) {
                    writer.write("🔎 Subpágina: " + subpagina);
                    writer.newLine();
                    for (String link : linksExternos) {
                        String tipo = obterTipoLink(link);
                        String linha = "→ " + tipo + ": " + link;
                        writer.write(linha);
                        writer.newLine();
                        linhasJaEscritas.add(linha);
                    }
                    writer.newLine();
                }
            } else {
                // Subpágina já existe, inserir apenas os links novos a seguir à subpágina
                Set<String> existentes = new HashSet<>();
                int i = indiceSubpagina + 1;
                while (i < linhas.size() && !linhas.get(i).startsWith("🔎")) {
                    existentes.add(linhas.get(i).trim());
                    i++;
                }

                List<String> novasLinhas = new ArrayList<>();
                for (String link : linksExternos) {
                    String tipo = obterTipoLink(link);
                    String linhaNova = "→ " + tipo + ": " + link;
                    if (!existentes.contains(linhaNova.trim())) {
                        novasLinhas.add(linhaNova);
                        linhasJaEscritas.add(linhaNova);
                    }
                }

                if (!novasLinhas.isEmpty()) {
                    linhas.addAll(indiceSubpagina + 1, novasLinhas);
                    Files.write(ficheiro.toPath(), linhas);
                }
            }

        } catch (IOException e) {
            System.err.println("⚠️ Erro ao escrever no ficheiro: " + e.getMessage());
        }
    }

    private static String obterTipoLink(String link) {
        if (link.contains("orcid.org"))
            return "ORCID";
        if (link.contains("cienciavitae.pt"))
            return "CiênciaVitae";
        if (link.contains("scholar.google"))
            return "Google Scholar";
        return "Outro";
    }

    private static void carregarLinhasJaEscritas() {
        File file = new File(FICHEIRO_OUTPUT);
        if (!file.exists())
            return;

        try {
            List<String> linhas = Files.readAllLines(file.toPath());
            linhasJaEscritas.addAll(linhas);
        } catch (IOException e) {
            System.err.println("⚠️ Erro ao ler ficheiro existente: " + e.getMessage());
        }
    }

    public static synchronized void registarNomesComLinks(String origem, Map<String, List<String>> nomesELinks) {
        if (nomesELinks == null || nomesELinks.isEmpty())
            return;

        try {
            File ficheiro = new File(FICHEIRO_OUTPUT);
            List<String> linhas = ficheiro.exists() ? Files.readAllLines(ficheiro.toPath()) : new ArrayList<>();

            boolean paginaExiste = linhas.stream().anyMatch(l -> l.trim().equals("🔎 Página: " + origem));
            List<String> novasLinhas = new ArrayList<>();

            for (Map.Entry<String, List<String>> entry : nomesELinks.entrySet()) {
                String nome = entry.getKey();
                List<String> links = entry.getValue();

                for (String link : links) {
                    String linha = nome + " ➜ " + link;
                    if (!linhasJaEscritas.contains(linha)) {
                        novasLinhas.add(linha);
                        linhasJaEscritas.add(linha);
                    }
                }
            }

            if (!novasLinhas.isEmpty()) {
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(FICHEIRO_OUTPUT, true))) {
                    if (!paginaExiste) {
                        writer.write("🔎 Página: " + origem);
                        writer.newLine();
                    }
                    for (String linha : novasLinhas) {
                        writer.write(linha);
                        writer.newLine();
                    }
                    writer.newLine();
                }
            }

        } catch (IOException e) {
            System.err.println("⚠️ Erro ao escrever no ficheiro: " + e.getMessage());
        }
    }
}

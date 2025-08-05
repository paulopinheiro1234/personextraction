# 🕷️ RawCrawling

Projeto Java para crawling web com Selenium e Maven.

---

## ✅ Pré-requisitos

Antes de começar, certifica-te de que tens o seguinte instalado:

- [Java JDK 17 ou superior](https://adoptium.net/)
- [Apache Maven](https://maven.apache.org/)
- [Visual Studio Code](https://code.visualstudio.com/)
  - Extensão recomendada: **Java Extension Pack** (Microsoft)

---

## 🚀 Como executar o projeto

### 1. Clonar o repositório

```bash
git clone https://github.com/paulopinheiro1234/personextraction.git
```

---

### 2. Abrir o projeto no Visual Studio Code

- Abre a pasta clonada diretamente no VS Code.

---

### 3. Abrir o terminal integrado

- Vai a `Terminal` → `New Terminal`
- Garante que o terminal está na raiz do projeto (onde está o ficheiro `pom.xml`)

---

### 4. Atualizar e instalar dependências Maven

Corre os seguintes comandos no terminal:

```bash
mvn dependency:purge-local-repository
mvn clean install
```

Estes comandos vão limpar e reinstalar as dependências Maven do projeto.

---

### 5. Correr o projeto

- Pressiona `F5` no VS Code para iniciar a execução.
- Quando solicitado, escolhe **Java Debugger**.

---

## 🛠️ Dicas úteis

- Se surgirem erros relacionados com dependências, repete o **Passo 4**.
- Verifica se as variáveis de ambiente `JAVA_HOME` e `MAVEN_HOME` estão corretamente configuradas.
- Em caso de erro com ficheiros `.jar` em falta, podes apagar a pasta `.m2/repository/org/seleniumhq/selenium` e repetir o processo.

---

## 📁 Estrutura típica do projeto

```
RawCrawling/
├── src/
│   └── main/
│       └── java/
│           └── ...
├── pom.xml
└── README.md
```

---

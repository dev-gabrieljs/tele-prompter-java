# Teleprompter Java ✅

**Teleprompter Interativo** desenvolvido em Java/JavaFX para criar e apresentar roteiros com rolagem automática, snippets de código e salvamento local.

---

## 🔧 Principais funcionalidades

- Editor WYSIWYG (HTMLEditor) para criar e formatar roteiros.
- Rolagem automática controlável (play/pause, velocidade).
- Inserção de blocos de código (snippet) via script injetado no WebView.
- Salvamento, edição e exclusão de roteiros em banco H2 embutido.
- Proteção contra captura de tela em Windows usando JNA (SetWindowDisplayAffinity).
- UI personalizada construída com FXML e CSS.

---

## 🧰 Stack tecnológico

- Java 17
- JavaFX (controls/fxml/web)
- JNA (para integração com Win32)
- H2 Database (arquivo local)
- Maven (com `javafx-maven-plugin` configurado)

> Observação: o `pom.xml` referencia `javafx-controls` e `javafx-fxml` na versão 17 e `javafx-web` na versão 21 — recomendo unificar as versões do JavaFX para evitar incompatibilidades.

---

## 🚀 Como executar

Pré-requisitos:
- JDK 17 instalado
- Maven (ou use o wrapper incluído `mvnw` / `mvnw.cmd`)

Executar em desenvolvimento:

Windows (PowerShell):

```powershell
.\\mvnw.cmd clean javafx:run
```

Linux/macOS:

```bash
./mvnw clean javafx:run
```

Alternativamente rode a classe principal `org.br.prompterjava.teleprompterjava.MainApplication` pela sua IDE.

---

## 💾 Banco de dados

- Banco H2 local com URL: `jdbc:h2:./prompter_db;DB_CLOSE_DELAY=-1`
- Arquivo gerado no diretório do projeto (ex.: `prompter_db.mv.db`)
- A inicialização da tabela `textos` é feita automaticamente por `DatabaseConfig.initDatabase()`

---

## 📁 Estrutura do projeto (resumo)

- `src/main/java` – código fonte
  - `MainApplication.java` – ponto de entrada
  - `controller/` – controllers FXML (Main, Botoes, TextoPrompter, etc.)
  - `config/DatabaseConfig.java` – configuração H2/CRUD básico
  - `util/WindowUtils.java` + `interfaces/CustomUser32.java` – integração JNA / Windows
- `src/main/resources` – FXML, imagens e CSS
  - `main.fxml`, `views/` (layouts), `styles` (CSS), `images/`
- `pom.xml` – dependências e plugin JavaFX

---

## 🧩 Como funciona (alto nível)

- A UI principal (`main.fxml`) carrega o `texto_prompter.fxml` que contém um `HTMLEditor`.
- A rolagem automática é implementada por um `Timeline` que injeta `window.scrollBy(...)` no `WebView` embutido.
- Inserção de blocos de código é feita via script que cria um elemento HTML customizado (com botão de remoção).
- Proteção de captura usa `SetWindowDisplayAffinity` (apenas no Windows).

---

## 🛠️ Desenvolvimento e notas

- Para depurar a UI, abra `MainApplication` na IDE e execute.
- Verifique a compatibilidade das versões do JavaFX (unificar `javafx-*` se necessário).
- Funções (Settings, Accessibility) ainda têm controllers vazios — pontos fáceis para estender funcionalidades.

---

## Contribuição

Sinta-se à vontade para abrir issues com bugs ou sugestões. Para contribuições via PR, descreva claramente a alteração e inclua testes quando aplicável.

---

## Licença

Sem licença explícita no repositório — adicione um arquivo `LICENSE` se quiser declarar a licença do projeto.

---

Se desejar, eu posso:
- adicionar badges, exemplos de uso ou um guia de empacotamento (jlink/native image) ✅
- criar um `launch` task para o VS Code ou um script `run.bat` para Windows ⚙️

Diga o que prefere que eu inclua a seguir ✨

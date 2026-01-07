# Teleprompter Java

O **Teleprompter Java** é uma ferramenta que foi criada para você que cria vídeos. Ao contrário de teleprompters
simples, este projeto integra recursos de baixo nível do Windows para oferecer uma experiência de "tela fantasma".
---

## Funcionalidades do Projeto!

* **Sem usar .txt:** Eliminando dezenas de blocos de notas. Tudo é centralizado em um banco de dados
  local em memória que utiliza um arquivo simples (.mv.db) para não perder as informações.

* **Invisibilidade para Gravadores:** Através de chamadas nativas do Windows via JNA, o teleprompter pode ficar visível
  para você, mas **completamente invisível** no OBS, Zoom, Teams ou qualquer gravador de tela.

* **Modo Fantasma (Click-Through):** Acesse pastas, sites ou botões que estão "atrás" do teleprompter sem fechar o roteiro.
  A função pode ser ativada e desativada usando o botão **Scroll Lock**.

* **Modo Transparência:** Permite ajustar o nível de opacidade da janela através de um controle deslizante, evitando que
  o teleprompter obstrua totalmente a visão de outros conteúdos na tela.

* **Editor WYSIWYG Integrado:** Formate seu texto, use cores e negrito diretamente no app.

* **Rolagem Automática Inteligente:** Controle a velocidade milimetricamente e pause quando precisar.

* **Gestão de Roteiros:** Salve, edite e selecione diferentes textos rapidamente.

* **Snippets de Código:** Insira blocos de código formatados dentro do roteiro (ideal para tutoriais de programação).

---

## 🧰 Stack Tecnológico & Integrações

### 1. Java 17 + JavaFX

A interface é construída com JavaFX, utilizando FXML para layout e CSS para estilização.

### 2. H2 Database (Persistência Local)

* **Modo:** H2 trabalhando com arquivo local (`.mv.db`).
* **Vantagem:** Banco de dados simples em memória. Evita a criação manual de arquivos e garante que, se o app fechar,
  seus dados estarão salvos no diretório do projeto.

### 3. JNA (Java Native Access) & Win32 API

Utiliza o JNA para conversar diretamente com a biblioteca `user32.dll` do Windows (escrita em C) para acessar
recursos de baixo nível:

* **SetWindowDisplayAffinity:** Bloqueia a captura da janela por pixels em nível de kernel.
* **WS_EX_TRANSPARENT:** Permite que os cliques do mouse "atravessem" a janela.
* **Hotkeys:** Captura teclas de atalho (como Scroll Lock) mesmo se o app não estiver em foco.

---

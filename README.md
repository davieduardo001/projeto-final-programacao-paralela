# Projeto Final PoC - Servidor e Cliente Socket Java

Este projeto demonstra um servidor socket Java multithread (`server_a`) e um cliente socket (`client`) que se comunica com ele.

## Pré-requisitos

*   Java Development Kit (JDK) 17 ou superior
*   Apache Maven 3.6 ou superior

## Como Compilar o Projeto

1.  Abra um terminal ou prompt de comando.
2.  Navegue até o diretório raiz do projeto (onde o arquivo `pom.xml` está localizado):
    ```bash
    cd /caminho/para/projeto-final-poc
    ```
3.  Compile o projeto usando Maven:
    ```bash
    mvn clean compile
    ```
    Isso irá compilar todas as classes do servidor e do cliente.

## Como Executar

**Importante:** Os servidores (A e B) e o cliente devem ser executados em terminais separados.

### 1. Executar o Servidor A (`server_a.Main`)

No primeiro terminal, após compilar, execute o seguinte comando para iniciar o servidor A:
```bash
mvn exec:java -Dexec.mainClass="server_a.Main"
```
O servidor A irá iniciar e aguardar conexões na porta configurada (atualmente 3001). Você verá uma mensagem como:
`Servidor Java (server_a.Main) multithread iniciado na porta 3001`
`Aguardando conexões de clientes...`

**Observação:** Se você receber um erro "Address already in use", significa que a porta 3001 já está ocupada por outro processo. Certifique-se de que nenhuma outra instância do servidor A esteja em execução ou altere a porta no código (`src/com/java/server_a/Main.java`) para uma que esteja livre.

### 2. Executar o Servidor B (`server_b.Main`)

Em um **segundo terminal**, após compilar, execute o seguinte comando para iniciar o servidor B:
```bash
mvn exec:java -Dexec.mainClass="server_b.Main"
```
O servidor B irá iniciar e aguardar conexões na porta configurada (atualmente 4002). Você verá uma mensagem como:
`Servidor B (server_b.Main) iniciado na porta 4002`

**Observação:** Se você receber um erro "Address already in use", significa que a porta 4002 já está ocupada por outro processo. Certifique-se de que nenhuma outra instância do servidor B esteja em execução ou altere a porta no código (`src/com/java/server_b/Main.java`) para uma que esteja livre.

### 3. Executar o Servidor C (`server_c.Main`)

Em um **terceiro terminal**, após compilar, execute o seguinte comando para iniciar o servidor C:
```bash
mvn exec:java -Dexec.mainClass="server_c.Main"
```
O servidor C irá iniciar e aguardar conexões na porta configurada (atualmente 4003). Você verá uma mensagem como:
`Servidor C (server_c.Main) iniciado na porta 4003`

**Observação:** Se você receber um erro "Address already in use", significa que a porta 4003 já está ocupada por outro processo. Certifique-se de que nenhuma outra instância do servidor C esteja em execução ou altere a porta no código (`src/com/java/server_c/Main.java`) para uma que esteja livre.

### 4. Executar o Cliente (`client.Client`)

Abra um **quarto terminal**. Navegue até o diretório raiz do projeto.

Após compilar, execute um dos seguintes comandos para iniciar o cliente:

*   **Para enviar uma mensagem padrão:**
    ```bash
    mvn exec:java -Dexec.mainClass="client.Client"
    ```

*   **Para enviar uma mensagem personalizada (substitua "Sua Mensagem Aqui" pela sua mensagem):**
    ```bash
    mvn exec:java -Dexec.mainClass="client.Client" -Dexec.arguments="pipeline"
    ```

O cliente tentará se conectar ao servidor em `localhost:3001`, enviar a mensagem e exibir a resposta do servidor (que deve ser "CONECTADO COM O SERVIDOR A" em amarelo).

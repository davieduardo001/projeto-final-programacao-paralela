# Projeto Final PoC - Servidor e Cliente Socket Java

Este projeto demonstra um sistema distribuído baseado em **sockets Java**, onde um cliente se conecta a um servidor orquestrador (**Server A**), que por sua vez consulta outros dois servidores (**Server B** e **Server C**) para realizar buscas em arquivos JSON.

---

## Estrutura do Projeto

```
src/
└── com/
    └── java/
        ├── client/
        │   └── Client.java
        ├── server_a/
        │   ├── Main.java
        │   └── utils/
        │       ├── ServerBConnector.java
        │       └── ServerCConnector.java
        ├── server_b/
        │   ├── Main.java
        │   └── utils/
        │       └── JsonSearchUtil.java
        ├── server_b/
        │   └── data/
        │       └── dados_servidor_b.json
        ├── server_c/
        │   ├── Main.java
        │   └── utils/
        │       └── ServerCSearchUtil.java
        └── server_c/
            └── data/
                └── dados_servidor_c.json
pom.xml
README.md
```

---

## Pré-requisitos

* Java Development Kit (JDK) 17 ou superior
* Apache Maven 3.6 ou superior

---

## Como Compilar o Projeto

1. No terminal, navegue até o diretório raiz do projeto (onde está o `pom.xml`):
```bash
cd /caminho/para/projeto
```
2. Execute:
```bash
mvn clean compile
```

---

## Como Executar

> Cada servidor e o cliente devem ser executados em **terminais separados**.

### Servidor A
```bash
mvn exec:java -Dexec.mainClass="server_a.Main"
```

Porta padrão: **3001**

---

### Servidor B
```bash
mvn exec:java -Dexec.mainClass="server_b.Main"
```

Porta padrão: **4002**

---

### Servidor C
```bash
mvn exec:java -Dexec.mainClass="server_c.Main"
```

Porta padrão: **4003**

---

### Cliente

* **Busca padrão:**
```bash
mvn exec:java -Dexec.mainClass="client.Client"
```
* **Busca personalizada:**
```bash
mvn exec:java -Dexec.mainClass="client.Client" -Dexec.arguments="pipeline" # troque pipeline pelo seu texto de busca
```

---

## Fluxo de Dados e Funcionamento

1. O **Cliente** envia uma **string de busca** ao **Servidor A**.
2. O **Servidor A** repassa essa string para os **Servidores B e C**.
3. Tanto o **Servidor B** quanto o **Servidor C**:
   * Buscam a string dentro dos campos `"title"` e `"abstract"` do seu respectivo arquivo JSON.
   * Usam um algoritmo de busca de **força bruta (naive search)**.
   * Retornam uma lista de objetos JSON que correspondem à busca.
4. O **Servidor A**:
   * Recebe os JSONs dos servidores B e C.
   * Converte esses dados em um formato **CSV** com separador `,` e quebra de linha representada por `##NL##`.
5. O **Cliente**:
   * Recebe o CSV, converte `##NL##` em quebras de linha reais (`\n`).
   * Salva o resultado em um arquivo `.csv` local.

> Esse fluxo ocorre tanto para o **Servidor B** quanto para o **Servidor C**.

---

## Fluxograma

![img](./Diagrama%20Projeto%20Final%20Programação%20Paralela-Fluxograma.drawio.svg)
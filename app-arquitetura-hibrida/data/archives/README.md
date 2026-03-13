# Archives Directory

Este diretório é usado para armazenar eventos arquivados do Event Store.

## Estrutura

Os arquivos de eventos antigos são movidos para cá após o período de retenção configurado (padrão: 2 anos).

## Formato dos Arquivos

- Arquivos compactados em formato GZIP
- Organização por aggregate e período
- Metadados em JSON

## Configuração

Veja `application.yml` seção `eventstore.archive` para configurações.

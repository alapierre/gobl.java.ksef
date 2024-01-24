[![Sonarcloud Status](https://sonarcloud.io/api/project_badges/measure?project=alapierre_ksef-java-rest-client&metric=alert_status)](https://sonarcloud.io/dashboard?id=alapierre_java-ksef-fa)
[![Renovate enabled](https://img.shields.io/badge/renovate-enabled-brightgreen.svg)](https://renovatebot.com/)
[![Maven Central](http://img.shields.io/maven-central/v/io.alapierre.ksef.fa/java-ksef-fa)](https://search.maven.org/artifact/io.alapierre.ksef.fa/java-ksef-fa)

# GOBL to KSeF Java converter

Early stage of project

## Convert GOBL Invoice into FA(2) 

````java
Gobl gobl = new Gobl();
Invoice invoice = gobl.parseInvoice(Path.of("src/test/resources/invoice.json"));
InvoiceMapper mapper = Mappers.getMapper(InvoiceMapper.class);

Faktura faktura = mapper.invoiceToFaktura(invoice);

FakturaSerializer fakturaSerializer = new FakturaSerializer();
System.out.println(fakturaSerializer.toString(faktura, false));
````

## Build Requirements

The project can be built on JDK17+.

Building the API client library requires:
1. Java 17+
2. Maven

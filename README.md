[![Sonarcloud Status](https://sonarcloud.io/api/project_badges/measure?project=alapierre_ksef-java-rest-client&metric=alert_status)](https://sonarcloud.io/dashboard?id=alapierre_java-ksef-fa)
[![Renovate enabled](https://img.shields.io/badge/renovate-enabled-brightgreen.svg)](https://renovatebot.com/)
[![Maven Central](http://img.shields.io/maven-central/v/io.alapierre.ksef.fa/java-ksef-fa)](https://search.maven.org/artifact/io.alapierre.ksef.fa/java-ksef-fa)

# GOBL to KSeF Java converter

Early stage of project, byt for simple case sales invoice it can create valid FA(2) from Json. 

## Current project stage limitation

- almost all annotations (except `P_18`) are hardcoded in converter
- only sales invoices are supported
- conversion of the invoice type was not consulted with a tax advisors (and still, only standard VAT invoice is supported)
- only payment by bank transfer and card is now supported

Sample input: [gobl-ksef-converter/src/test/resources/invoice.json](./gobl-ksef-converter/src/test/resources/invoice.json)


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

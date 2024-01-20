package io.alapierre.ksef.fa.converter;

import io.alapierre.ksef.fa.model.gobl.InvoiceSerializer;
import io.alapierre.ksef.fa.model.xml.FakturaSerializer;
import org.gobl.model.Invoice;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import pl.gov.crd.wzor._2023._06._29._12648.Faktura;

import java.io.File;

/**
 * @author Adrian Lapierre {@literal al@alapierre.io}
 * Copyrights by original author 2024.01.16
 */
class InvoiceMapperTest {

    @Test
    void invoiceToFaktura() {

        InvoiceMapper mapper = Mappers.getMapper(InvoiceMapper.class);

        InvoiceSerializer invoiceSerializer = new InvoiceSerializer();
        Invoice invoice = invoiceSerializer.fromFile(new File("src/test/resources/invoice.json"));

        Faktura faktura = mapper.invoiceToFaktura(invoice);

        FakturaSerializer fakturaSerializer = new FakturaSerializer();

        System.out.println(fakturaSerializer.toString(faktura, false));

    }

}

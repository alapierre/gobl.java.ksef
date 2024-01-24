package io.alapierre.ksef.fa.converter;

import io.alapierre.gobl.core.Gobl;
import io.alapierre.ksef.fa.model.xml.FakturaSerializer;
import org.gobl.model.Invoice;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import pl.gov.crd.wzor._2023._06._29._12648.Faktura;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Path;

/**
 * @author Adrian Lapierre {@literal al@alapierre.io}
 * Copyrights by original author 2024.01.16
 */
class InvoiceMapperTest {

    @Test
    void invoiceToFaktura() throws IOException {

        Gobl gobl = new Gobl();
        Invoice invoice = gobl.parseInvoice(Path.of("src/test/resources/invoice.json"));
        InvoiceMapper mapper = Mappers.getMapper(InvoiceMapper.class);

        Faktura faktura = mapper.invoiceToFaktura(invoice);

        FakturaSerializer fakturaSerializer = new FakturaSerializer();
        System.out.println(fakturaSerializer.toString(faktura, false));

        Assertions.assertEquals("VAT", faktura.getFa().getRodzajFaktury().value());
        Assertions.assertEquals("2024-01-15", faktura.getFa().getP1().toString());
        Assertions.assertEquals("6891152920", faktura.getPodmiot1().getDaneIdentyfikacyjne().getNIP());
        Assertions.assertEquals("2222222222", faktura.getPodmiot2().getDaneIdentyfikacyjne().getNIP());
        Assertions.assertEquals("PLN", faktura.getFa().getKodWaluty().value());
        Assertions.assertEquals(1, faktura.getFa().getFaWiersz().size());
        Assertions.assertEquals(BigInteger.valueOf(6), faktura.getFa().getPlatnosc().getFormaPlatnosci());
        Assertions.assertEquals(1, faktura.getFa().getPlatnosc().getRachunekBankowy().size());
        Assertions.assertEquals("PKO BP", faktura.getFa().getPlatnosc().getRachunekBankowy().get(0).getNazwaBanku());
        Assertions.assertEquals("61109010140000071219812874", faktura.getFa().getPlatnosc().getRachunekBankowy().get(0).getNrRB());


    }

}
